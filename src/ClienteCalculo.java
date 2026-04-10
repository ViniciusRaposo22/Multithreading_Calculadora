import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * ClienteCalculo - Cliente que se conecta ao ServidorCalculo,
 * envia operações matemáticas e exibe os resultados.
 */
public class ClienteCalculo {

    private static final String HOST = "localhost";
    private static final int PORTA = 12345;

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║       CLIENTE DE CÁLCULO DISTRIBUÍDO     ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println("Conectando ao servidor " + HOST + ":" + PORTA + "...\n");

        try (
            Socket socket = new Socket(HOST, PORTA);
            BufferedReader entradaServidor = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter saidaServidor = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("✔ Conectado com sucesso!");
            System.out.println("─────────────────────────────────────────");
            System.out.println(" Digite operações matemáticas (ex: 10 + 5)");
            System.out.println(" Operadores suportados: +  -  *  /");
            System.out.println(" Digite 'sair' para encerrar.");
            System.out.println("─────────────────────────────────────────\n");

            while (true) {
                System.out.print(">> Operação: ");
                String operacao = scanner.nextLine().trim();

                if (operacao.isEmpty()) {
                    continue;
                }

                // Envia operação para o servidor
                saidaServidor.println(operacao);

                // Recebe e exibe o resultado
                String resposta = entradaServidor.readLine();
                System.out.println("   Resultado: " + resposta + "\n");

                if (operacao.equalsIgnoreCase("sair")) {
                    break;
                }
            }

            System.out.println("\nConexão encerrada. Até logo!");

        } catch (ConnectException e) {
            System.err.println("✘ Erro: Não foi possível conectar ao servidor.");
            System.err.println("  Verifique se o servidor está rodando na porta " + PORTA + ".");
        } catch (IOException e) {
            System.err.println("✘ Erro de I/O: " + e.getMessage());
        }
    }
}
