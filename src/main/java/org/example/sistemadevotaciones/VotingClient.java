package org.example.sistemadevotaciones;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
        import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
        import java.net.Socket;

public class VotingClient extends Application {
    private BufferedReader in;
    private PrintWriter out;
    private Label option1Count;
    private Label option2Count;
    private Label option3Count;
    private Label totalCount;

    // el método main() inicia la aplicación
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    // el método start() crea la interfaz gráfica de usuario
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Voting Client");

        // crea un VBox para organizar los nodos
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        // crea un TextField para la dirección IP
        TextField ipField = new TextField("localhost");
        TextField portField = new TextField("12345");
        Button connectButton = new Button("Connect");

        // agrega los nodos al VBox
        root.getChildren().addAll(new Label("IP Address:"), ipField, new Label("Port:"), portField, connectButton);

        // define el evento para el botón de conexión
        connectButton.setOnAction(event -> {
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            connectToServer(ip, port);
            showVotingScreen(primaryStage);
        });

        Scene scene = new Scene(root, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // el método connectToServer() establece la conexión con el servidor
    private void connectToServer(String ip, int port) {
        // crea un socket para la conexión
        try {
            Socket socket = new Socket(ip, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            // crea un hilo para recibir actualizaciones del servidor
            new Thread(() -> {
                // lee los mensajes del servidor
                try {
                    String input;
                    while ((input = in.readLine()) != null) {
                        if (input.startsWith("UPDATE")) {
                            String counts = input.substring(7);
                            updateVoteCounts(counts);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // el método showVotingScreen() muestra la pantalla de votación
    private void showVotingScreen(Stage primaryStage) {
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        // crea botones para votar por las opciones
        Button option1Button = new Button("Vote Option1");
        Button option2Button = new Button("Vote Option2");
        Button option3Button = new Button("Vote Option3");
        // crea etiquetas para mostrar los recuentos de votos
        option1Count = new Label("Option1: 0");
        option2Count = new Label("Option2: 0");
        option3Count = new Label("Option3: 0");
        totalCount = new Label("Total Votes: 0");

        // define los eventos para los botones de votación
        option1Button.setOnAction(event -> sendVote("Option1"));
        option2Button.setOnAction(event -> sendVote("Option2"));
        option3Button.setOnAction(event -> sendVote("Option3"));

        // agrega los nodos al VBox
        root.getChildren().addAll(option1Button, option2Button, option3Button, option1Count, option2Count, option3Count, totalCount);

        // crea una escena y muestra la ventana
        Scene scene = new Scene(root, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // el método sendVote() envía un voto al servidor
    private void sendVote(String option) {
        out.println("VOTE " + option);
    }

    // el método updateVoteCounts() actualiza los recuentos de votos
    private void updateVoteCounts(String counts) {
        Platform.runLater(() -> {
            String[] parts = counts.split(" ");
            for (String part : parts) {
                if (part.startsWith("Option1:")) {
                    option1Count.setText(part);
                } else if (part.startsWith("Option2:")) {
                    option2Count.setText(part);
                } else if (part.startsWith("Option3:")) {
                    option3Count.setText(part);
                } else if (part.startsWith("Total:")) {
                    totalCount.setText(part);
                }
            }
        });
    }
}


