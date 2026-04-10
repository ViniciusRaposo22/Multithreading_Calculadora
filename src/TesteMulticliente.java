import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * TesteMulticliente - Simula múltiplos clientes conectando ao servidor
 * simultaneamente para demonstrar o funcionamento do multithreading.
 */
public class TesteMulticliente {

    private static final String HOST        = "localhost";
    private static final int    PORTA       = 12345;
    private static final int    NUM_CLIENTES = 5;

    // ── Cores ANSI ────────────────────────────────────────────────────────────
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";

    // Uma cor distinta por cliente — prova visual de paralelismo
    private static final String[] COR = {
        "\u001B[94m",   // azul      → Cliente 1
        "\u001B[91m",   // vermelho  → Cliente 2
        "\u001B[96m",   // ciano     → Cliente 3
        "\u001B[93m",   // amarelo   → Cliente 4
        "\u001B[95m",   // magenta   → Cliente 5
    };
    private static final String[] SIMBOLO = { "●", "●", "●", "●", "●" };

    // Contador compartilhado de operações concluídas (thread-safe)
    private static final AtomicInteger totalOps      = new AtomicInteger(0);
    private static final AtomicInteger clientesAtivos = new AtomicInteger(0);

    // ── main ──────────────────────────────────────────────────────────────────
    public static void main(String[] args) throws InterruptedException {

        imprimirBanner();

        ExecutorService  executor = Executors.newFixedThreadPool(NUM_CLIENTES);
        CountDownLatch   latch    = new CountDownLatch(NUM_CLIENTES);

        String[][] operacoes = {
            {"15 + 27",  "100 - 45",  "8 * 7"  },
            {"200 / 4",  "33 + 67",   "12 * 12"},
            {"500 - 123","9 * 9",     "81 / 9" },
            {"1000 / 8", "55 + 45",   "7 * 13" },
            {"99 - 11",  "144 / 12",  "25 + 75"},
        };

        System.out.println(DIM + "  Conectando clientes..." + RESET);
        System.out.println();

        for (int i = 0; i < NUM_CLIENTES; i++) {
            final int      id  = i + 1;
            final String[] ops = operacoes[i];

            executor.submit(() -> {
                executarCliente(id, ops);
                latch.countDown();
            });

            Thread.sleep(200);   // escalonamento original mantido
        }

        latch.await();
        executor.shutdown();

        imprimirResumoFinal();
    }

    // ── Lógica do cliente (igual ao original, só o print mudou) ───────────────
    private static void executarCliente(int id, String[] operacoes) {
        clientesAtivos.incrementAndGet();
        String cor = COR[(id - 1) % COR.length];

        try (
            Socket         socket  = new Socket(HOST, PORTA);
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter    saida   = new PrintWriter(socket.getOutputStream(), true)
        ) {
            log(id, cor, "conectado ao servidor " + BOLD + HOST + ":" + PORTA + RESET);
            imprimirBarraAtivos();

            for (String operacao : operacoes) {
                saida.println(operacao);
                String resposta = entrada.readLine();
                totalOps.incrementAndGet();

                // Destaca expressão (amarelo) e resultado (verde)
                System.out.printf("  %s%s[Cliente #%d]%s  %s%-14s%s  →  %s%s%s%n",
                    cor, BOLD, id, RESET,
                    "\u001B[33m", operacao, RESET,
                    "\u001B[92m" + BOLD, resposta, RESET);

                Thread.sleep(300);
            }

            saida.println("sair");
            entrada.readLine();

            log(id, cor, "encerrou conexão " + DIM + "(todas as ops concluídas)" + RESET);

        } catch (ConnectException e) {
            System.err.println(cor + BOLD + "  [Cliente #" + id + "]" + RESET
                + " \u001B[31m✘ Servidor não encontrado na porta " + PORTA
                + ". Inicie o servidor primeiro.\u001B[0m");
        } catch (IOException | InterruptedException e) {
            System.err.println("  [Cliente #" + id + "] Erro: " + e.getMessage());
        } finally {
            clientesAtivos.decrementAndGet();
        }
    }

    // ── Helpers visuais ────────────────────────────────────────────────────────

    private static void imprimirBanner() {
        System.out.println();
        System.out.println(BOLD + "╔══════════════════════════════════════════════════════════╗" + RESET);
        System.out.println(BOLD + "║          TESTE DE MÚLTIPLOS CLIENTES SIMULTÂNEOS         ║" + RESET);
        System.out.println(BOLD + "╚══════════════════════════════════════════════════════════╝" + RESET);
        System.out.println();

        // Legenda de cores — mostra qual cor pertence a qual cliente
        System.out.print("  Threads: ");
        for (int i = 0; i < NUM_CLIENTES; i++) {
            System.out.print(COR[i] + BOLD + "● Cliente #" + (i + 1) + RESET + "  ");
        }
        System.out.println("\n");
        System.out.println(DIM + "  Cada cor = uma thread diferente rodando ao mesmo tempo" + RESET);
        System.out.println(DIM + "  A intercalação das linhas prova o paralelismo real" + RESET);
        System.out.println();
        System.out.println("  " + DIM + "─".repeat(56) + RESET);
        System.out.println();
    }

    /** Linha de log padrão com timestamp, cor e ícone do cliente. */
    private static void log(int id, String cor, String msg) {
        System.out.printf("  %s%s%s[Cliente #%d]%s %s%n",
            timestamp(), cor, BOLD, id, RESET, msg);
    }

    /** Barra visual mostrando quais threads estão ativas no momento. */
    private static synchronized void imprimirBarraAtivos() {
        System.out.print("\n  " + DIM + "Threads ativas agora → " + RESET);
        for (int i = 0; i < NUM_CLIENTES; i++) {
            // Heurística simples: considera ativo se clientesAtivos > i
            if (i < clientesAtivos.get()) {
                System.out.print(COR[i] + BOLD + "■ " + RESET);
            } else {
                System.out.print(DIM + "□ " + RESET);
            }
        }
        System.out.println();
        System.out.println();
    }

    /** Resumo final com contagem de operações processadas. */
    private static void imprimirResumoFinal() {
        System.out.println();
        System.out.println("  " + DIM + "─".repeat(56) + RESET);
        System.out.println();
        System.out.println("  \u001B[92m" + BOLD + "✔ Teste concluído!" + RESET);
        System.out.println("  " + DIM + "Clientes simulados : " + RESET + BOLD + NUM_CLIENTES + RESET);
        System.out.println("  " + DIM + "Operações enviadas : " + RESET + BOLD + totalOps.get() + RESET);
        System.out.println("  " + DIM + "Threads usadas     : " + RESET + BOLD + NUM_CLIENTES + RESET
            + DIM + " (ExecutorService com pool fixo)" + RESET);
        System.out.println();

        // Mini barra de resultado por cliente
        System.out.println("  " + DIM + "Operações por cliente:" + RESET);
        int opsPorCliente = totalOps.get() / NUM_CLIENTES;
        for (int i = 0; i < NUM_CLIENTES; i++) {
            String barra = "█".repeat(opsPorCliente);
            System.out.println("  " + COR[i] + BOLD + "Cliente #" + (i+1) + RESET
                + "  " + COR[i] + barra + RESET
                + "  " + DIM + opsPorCliente + " ops" + RESET);
        }
        System.out.println();
    }

    /** Timestamp curto no formato [HH:mm:ss] em cinza. */
    private static String timestamp() {
        java.time.LocalTime t = java.time.LocalTime.now();
        return String.format(DIM + "[%02d:%02d:%02d] " + RESET,
            t.getHour(), t.getMinute(), t.getSecond());
    }
}