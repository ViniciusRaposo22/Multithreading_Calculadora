import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ServidorCalculo - Servidor de cálculo distribuído com multithreading
 * Aceita múltiplos clientes simultaneamente, cada um em uma thread separada.
 */
public class ServidorCalculo {

    private static final int PORTA = 12345;
    private static final AtomicInteger contadorClientes = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   SERVIDOR DE CÁLCULO DISTRIBUÍDO        ║");
        System.out.println("║   Aguardando conexões na porta " + PORTA + "...  ║");
        System.out.println("╚══════════════════════════════════════════╝\n");

        try (ServerSocket serverSocket = new ServerSocket(PORTA)) {
            // Loop principal: aceita conexões indefinidamente
            while (true) {
                Socket clienteSocket = serverSocket.accept();
                int idCliente = contadorClientes.incrementAndGet();

                System.out.println("[Servidor] Cliente #" + idCliente + " conectado: "
                        + clienteSocket.getInetAddress().getHostAddress());

                // Cria e inicia uma nova thread para cada cliente
                Thread threadCliente = new Thread(new ManipuladorCliente(clienteSocket, idCliente));
                threadCliente.setName("Thread-Cliente-" + idCliente);
                threadCliente.start();

                System.out.println("[Servidor] Thread iniciada: " + threadCliente.getName()
                        + " | Clientes ativos: " + Thread.activeCount());
            }
        } catch (IOException e) {
            System.err.println("[Servidor] Erro ao iniciar o servidor: " + e.getMessage());
        }
    }
}


/**
 * ManipuladorCliente - Runnable que processa as requisições de um cliente específico.
 * Cada instância roda em sua própria thread.
 */
class ManipuladorCliente implements Runnable {

    private final Socket socket;
    private final int idCliente;

    public ManipuladorCliente(Socket socket, int idCliente) {
        this.socket = socket;
        this.idCliente = idCliente;
    }

    @Override
    public void run() {
        System.out.println("[" + Thread.currentThread().getName() + "] Iniciando atendimento ao Cliente #" + idCliente);

        try (
            BufferedReader entrada = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter saida = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String linha;

            // Processa múltiplas operações do mesmo cliente até ele desconectar
            while ((linha = entrada.readLine()) != null) {
                linha = linha.trim();

                if (linha.equalsIgnoreCase("sair")) {
                    saida.println("Até logo! Encerrando conexão.");
                    break;
                }

                System.out.println("[" + Thread.currentThread().getName() + "] Recebido de Cliente #"
                        + idCliente + ": " + linha);

                String resultado = calcular(linha);
                saida.println(resultado);

                System.out.println("[" + Thread.currentThread().getName() + "] Resposta para Cliente #"
                        + idCliente + ": " + resultado);
            }

        } catch (IOException e) {
            System.err.println("[" + Thread.currentThread().getName() + "] Erro com Cliente #"
                    + idCliente + ": " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.err.println("[" + Thread.currentThread().getName() + "] Erro ao fechar socket: " + e.getMessage());
            }
            System.out.println("[" + Thread.currentThread().getName() + "] Cliente #" + idCliente + " desconectado.");
        }
    }

    /**
     * Avalia uma expressão matemática simples no formato: número operador número
     * Suporta: +, -, *, /
     */
    private String calcular(String expressao) {
        try {
            // Remove espaços extras e divide a expressão
            expressao = expressao.trim();

            // Regex para capturar: número [operador] número (com suporte a negativos e decimais)
            String regex = "(-?\\d+\\.?\\d*)\\s*([+\\-*/])\\s*(-?\\d+\\.?\\d*)";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);
            java.util.regex.Matcher matcher = pattern.matcher(expressao);

            if (!matcher.matches()) {
                return "ERRO: Formato inválido. Use: número operador número (ex: 10 + 5)";
            }

            double num1 = Double.parseDouble(matcher.group(1));
            String operador = matcher.group(2);
            double num2 = Double.parseDouble(matcher.group(3));

            double resultado;

            switch (operador) {
                case "+":
                    resultado = num1 + num2;
                    break;
                case "-":
                    resultado = num1 - num2;
                    break;
                case "*":
                    resultado = num1 * num2;
                    break;
                case "/":
                    if (num2 == 0) {
                        return "ERRO: Divisão por zero não é permitida.";
                    }
                    resultado = num1 / num2;
                    break;
                default:
                    return "ERRO: Operador desconhecido: " + operador;
            }

            // Retorna inteiro se não houver parte decimal relevante
            if (resultado == Math.floor(resultado) && !Double.isInfinite(resultado)) {
                return expressao + " = " + (long) resultado;
            } else {
                return expressao + " = " + resultado;
            }

        } catch (NumberFormatException e) {
            return "ERRO: Número inválido na expressão.";
        } catch (Exception e) {
            return "ERRO: Não foi possível processar a operação: " + e.getMessage();
        }
    }
}
