package org.example.sistemadevotaciones;import java.io.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class VotingServer {
    private static final int PORT = 12345;
    private static Map<String, Integer> votingOptions = new ConcurrentHashMap<>();
    private static List<PrintWriter> clientWriters = new ArrayList<>();

    public static void main(String[] args) {
        // Initialize voting options
        votingOptions.put("Option1", 0);
        votingOptions.put("Option2", 0);
        votingOptions.put("Option3", 0);

        // Start server
        System.out.println("Server is running...");

        // Create server socket
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Client handler thread
    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;

        // Constructor
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        // Run method
        @Override
        public void run() {
            // Create input stream
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out = new PrintWriter(socket.getOutputStream(), true);
                synchronized (clientWriters) {
                    clientWriters.add(out);
                }

                // lector de mensajes
                String input;
                while ((input = in.readLine()) != null) {
                    if (input.startsWith("VOTE")) {
                        String option = input.split(" ")[1];
                        votingOptions.merge(option, 1, Integer::sum);
                        sendUpdateToAllClients();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    synchronized (clientWriters) {
                        clientWriters.remove(out);
                    }
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // mensaje de actualizacion
        private void sendUpdateToAllClients() {
            StringBuilder updateMessage = new StringBuilder("UPDATE ");
            int totalVotes = 0;
            // recorrer opciones
            for (Map.Entry<String, Integer> entry : votingOptions.entrySet()) {
                updateMessage.append(entry.getKey()).append(":").append(entry.getValue()).append(" ");
                totalVotes += entry.getValue();
            }
            // mensaje de total
            updateMessage.append("Total:").append(totalVotes);
            String message = updateMessage.toString();

            // enviar mensaje a todos los clientes
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
