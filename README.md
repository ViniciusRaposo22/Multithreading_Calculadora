# Servidor de Cálculo Distribuído — Java Sockets + Multithreading

Projeto acadêmico que implementa um servidor de cálculo distribuído utilizando
**Java Sockets** e **Multithreading**, onde múltiplos clientes podem enviar
operações matemáticas simultaneamente e receber os resultados.

---

## Estrutura do Projeto

```
ServidorCalculo/
├── src/
│   ├── ServidorCalculo.java     # Servidor principal com multithreading
│   ├── ClienteCalculo.java      # Cliente interativo via terminal
│   └── TesteMulticliente.java   # Teste automatizado com múltiplos clientes
├── bin/                         # Bytecode gerado após compilação
├── compile.sh                   # Script de compilação
└── README.md
```

---

## Arquitetura

```
                    ┌─────────────────────────────────┐
                    │         ServidorCalculo          │
                    │   ServerSocket (porta 12345)     │
                    │                                  │
  Cliente #1 ──────►│  Thread-Cliente-1                │
                    │  ┌─────────────────────────┐     │
  Cliente #2 ──────►│  │   ManipuladorCliente    │     │
                    │  │  - Lê operação           │     │
  Cliente #3 ──────►│  │  - Chama calcular()      │     │
                    │  │  - Envia resultado        │     │
  Cliente #N ──────►│  └─────────────────────────┘     │
                    │  Thread-Cliente-2, 3, N...        │
                    └─────────────────────────────────┘
```

---

## Conceitos Aplicados

### Sockets
- `ServerSocket`: aguarda conexões na porta 12345
- `Socket`: representa a conexão com cada cliente
- `BufferedReader` / `PrintWriter`: para leitura e escrita de texto via socket

### Multithreading
- `Thread` + `Runnable`: cada cliente conectado ganha sua própria thread
- `AtomicInteger`: contador thread-safe de clientes conectados
- `ExecutorService` (no teste): gerencia pool de threads dos clientes simulados
- `CountDownLatch` (no teste): sincroniza o término de todos os clientes

---

## Como Compilar e Executar

### Compilação
```bash
chmod +x compile.sh
./compile.sh
```

Ou manualmente:
```bash
mkdir -p bin
javac -d bin src/ServidorCalculo.java src/ClienteCalculo.java src/TesteMulticliente.java
```

### Execução

**Terminal 1 — Iniciar o Servidor:**
```bash
java -cp bin ServidorCalculo
```

**Terminal 2 — Cliente Interativo:**
```bash
java -cp bin ClienteCalculo
```

**Terminal 2 (alternativo) — Teste com Múltiplos Clientes:**
```bash
java -cp bin TesteMulticliente
```

---

## Operações Suportadas

| Operador | Exemplo     | Resultado |
|----------|-------------|-----------|
| `+`      | `10 + 5`    | `= 15`    |
| `-`      | `100 - 37`  | `= 63`    |
| `*`      | `8 * 7`     | `= 56`    |
| `/`      | `200 / 4`   | `= 50`    |

- Suporta números decimais: `3.14 * 2`
- Divisão por zero é tratada com mensagem de erro
- Formato inválido retorna mensagem de erro clara
- Digite `sair` para encerrar a conexão do cliente

---

## Exemplo de Saída

### Servidor
```
╔══════════════════════════════════════════╗
║   SERVIDOR DE CÁLCULO DISTRIBUÍDO        ║
║   Aguardando conexões na porta 12345...  ║
╚══════════════════════════════════════════╝

[Servidor] Cliente #1 conectado: 127.0.0.1
[Servidor] Thread iniciada: Thread-Cliente-1 | Clientes ativos: 2
[Thread-Cliente-1] Recebido de Cliente #1: 10 + 5
[Thread-Cliente-1] Resposta para Cliente #1: 10 + 5 = 15
```

### Cliente
```
>> Operação: 10 + 5
   Resultado: 10 + 5 = 15

>> Operação: 200 / 8
   Resultado: 200 / 8 = 25
```

---

## Tratamento de Erros

- Divisão por zero → mensagem de erro explicativa
- Formato de expressão inválido → instrução de uso correto
- Servidor offline → mensagem clara ao cliente
- Desconexão inesperada → thread encerra graciosamente e fecha o socket
