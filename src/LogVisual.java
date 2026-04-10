/**
 * LogVisual — Utilitário de saída colorida para demonstrar
 * o multithreading de forma clara no terminal.
 *
 * Como usar: substitua os System.out.println existentes
 * pelas chamadas LogVisual.servidor(...) e LogVisual.thread(...).
 *
 * Compatível com terminais que suportam ANSI (Linux, macOS,
 * Windows Terminal, IntelliJ, VS Code, etc.)
 */
public class LogVisual {

    // Cores ANSI — cada thread recebe uma cor diferente
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";

    // Paleta de cores para as threads (até 5 simultâneas)
    private static final String[] CORES_THREAD = {
        "\u001B[94m",  // azul claro   → Thread-Cliente-1
        "\u001B[91m",  // vermelho      → Thread-Cliente-2
        "\u001B[96m",  // ciano         → Thread-Cliente-3
        "\u001B[93m",  // amarelo       → Thread-Cliente-4
        "\u001B[95m",  // magenta       → Thread-Cliente-5
    };

    private static final String COR_SERVIDOR = "\u001B[92m"; // verde
    private static final String COR_ERRO     = "\u001B[31m"; // vermelho escuro

    // ─────────────────────────────────────────────────────────
    //  Exibe o banner de inicialização do servidor
    // ─────────────────────────────────────────────────────────
    public static void banner(int porta) {
        System.out.println();
        System.out.println(COR_SERVIDOR + BOLD +
            "╔══════════════════════════════════════════════════════╗" + RESET);
        System.out.println(COR_SERVIDOR + BOLD +
            "║        SERVIDOR DE CÁLCULO DISTRIBUÍDO               ║" + RESET);
        System.out.println(COR_SERVIDOR + BOLD +
            "╚══════════════════════════════════════════════════════╝" + RESET);
        System.out.println(DIM + "  Porta: " + porta +
            "  |  Multithreading ativo  |  Aguardando clientes..." + RESET);
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────
    //  Log do servidor principal (thread de aceitação)
    // ─────────────────────────────────────────────────────────
    public static void servidor(String msg) {
        System.out.printf("%s%s[Servidor]%s %s%n",
            timestamp(), COR_SERVIDOR + BOLD, RESET, msg);
    }

    // ─────────────────────────────────────────────────────────
    //  Log de uma thread de cliente específica
    // ─────────────────────────────────────────────────────────
    public static void thread(int idCliente, String msg) {
        String cor = CORES_THREAD[(idCliente - 1) % CORES_THREAD.length];
        System.out.printf("%s%s[Thread-Cliente-%-2d]%s %s%n",
            timestamp(), cor + BOLD, idCliente, RESET, msg);
    }

    // ─────────────────────────────────────────────────────────
    //  Log de conexão: mostra a barra de threads ativas
    // ─────────────────────────────────────────────────────────
    public static void conexao(int idCliente, String ip, int totalAtivos) {
        String cor = CORES_THREAD[(idCliente - 1) % CORES_THREAD.length];
        System.out.println();
        System.out.printf("%s%s┌─ NOVA CONEXÃO ─────────────────────────────────┐%s%n",
            timestamp(), cor, RESET);
        System.out.printf("%s%s│%s  Cliente #%d  │  IP: %-15s  │%s%n",
            timestamp(), cor, RESET, idCliente, ip, "");
        System.out.printf("%s%s│%s  Thread: %-36s │%s%n",
            timestamp(), cor, RESET,
            "Thread-Cliente-" + idCliente, "");
        System.out.printf("%s%s└─ Threads ativas: %s %s%n",
            timestamp(), cor, barraThreads(totalAtivos), RESET);
        System.out.println();
    }

    // ─────────────────────────────────────────────────────────
    //  Log de operação: destaca a expressão e o resultado
    // ─────────────────────────────────────────────────────────
    public static void operacao(int idCliente, String expressao, String resultado) {
        String cor = CORES_THREAD[(idCliente - 1) % CORES_THREAD.length];
        System.out.printf("%s%s[Thread-%-2d]%s  %s%s%s  →  %s%s%s%n",
            timestamp(),
            cor + BOLD, idCliente, RESET,
            BOLD, expressao, RESET,
            "\u001B[92m" + BOLD, resultado, RESET);
    }

    // ─────────────────────────────────────────────────────────
    //  Log de desconexão de cliente
    // ─────────────────────────────────────────────────────────
    public static void desconexao(int idCliente, int totalAtivos) {
        String cor = CORES_THREAD[(idCliente - 1) % CORES_THREAD.length];
        System.out.printf("%s%s[Thread-%-2d]%s  %sCliente #%d desconectado%s  →  threads: %s%n",
            timestamp(), cor + BOLD, idCliente, RESET,
            DIM, idCliente, RESET,
            barraThreads(totalAtivos));
    }

    // ─────────────────────────────────────────────────────────
    //  Log de erro
    // ─────────────────────────────────────────────────────────
    public static void erro(String msg) {
        System.out.printf("%s%s[ERRO] %s%s%n",
            timestamp(), COR_ERRO + BOLD, RESET + msg, RESET);
    }

    // ─────────────────────────────────────────────────────────
    //  Helpers internos
    // ─────────────────────────────────────────────────────────

    /** Retorna a hora atual formatada entre colchetes em cinza. */
    private static String timestamp() {
        java.time.LocalTime t = java.time.LocalTime.now();
        return String.format(DIM + "[%02d:%02d:%02d] " + RESET,
            t.getHour(), t.getMinute(), t.getSecond());
    }

    /**
     * Gera uma barra visual de threads ativas.
     * Exemplo com 3 ativos: [■■■□□]
     */
    private static String barraThreads(int ativas) {
        int max = 5;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < max; i++) {
            if (i < ativas) {
                String cor = CORES_THREAD[i % CORES_THREAD.length];
                sb.append(cor).append("■").append(RESET);
            } else {
                sb.append(DIM).append("□").append(RESET);
            }
        }
        sb.append("] ").append(BOLD).append(ativas).append("/").append(max).append(RESET);
        return sb.toString();
    }
}