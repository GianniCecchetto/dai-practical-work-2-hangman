package ch.heigvd.dai.client;

import ch.heigvd.dai.server.Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {
    private String host;
    private int port;

    public enum Message {
        JOIN,
        LISTGAMES,
        GUESS,
        HELP
    }

    public static String END_OF_LINE = "\n";

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        System.out.println("[Client] Connecting to " + host + ":" + port + "...");

        try (Socket socket = new Socket(host, port);
             Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(reader);
             Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(writer);) {
            System.out.println("[Client] Connected to " + host + ":" + port);

            while (!socket.isClosed()) {
                System.out.print("> ");

                Reader inputReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
                BufferedReader bir = new BufferedReader(inputReader);
                String userInput = bir.readLine();

                try {
                    String[] userInputParts = userInput.split(" ", 2);
                    Message message = Message.valueOf(userInputParts[0].toUpperCase());

                    String request = null;

                    switch (message) {
                        case JOIN -> {
                            userInputParts = userInputParts[1].split(" ", 2);
                            String name = userInputParts[0];
                            int gameId = Integer.parseInt(userInputParts[1]);

                            request = Message.JOIN + " " + name + " " + gameId + END_OF_LINE;
                        }
                        case LISTGAMES -> {
                            request = Message.LISTGAMES + END_OF_LINE;
                        }
                        case GUESS -> {
                            String guess = userInputParts[1];

                            request = Message.GUESS + " " + guess + END_OF_LINE;
                        }
                        case HELP -> help();
                    }

                    if (request != null) {
                        out.write(request);
                        out.flush();
                    }
                } catch (Exception e) {
                    System.out.println("Invalid command. Please try again.");
                    continue;
                }

                String serverResponse = in.readLine();
                System.out.println(serverResponse);
                if (serverResponse == null) {
                    socket.close();
                    continue;
                }

                String[] serverResponseParts = serverResponse.split(" ", 2);

                Server.Message message = null;
                try {
                    message = Server.Message.valueOf(serverResponseParts[0]);
                } catch (IllegalArgumentException e) {
                    // Do nothing
                }

                try{
                    serverResponse = serverResponseParts[1];
                } catch (RuntimeException e) {
                    serverResponse = "";
                }

                switch (message) {
                    case GAMES -> System.out.println("gamelist" + serverResponse);
                    case GAMESTATE -> System.out.println("game state" + serverResponse);
                    case OK -> System.out.println("server ok." + serverResponse);
                    case ERROR -> {
                        if (serverResponseParts.length < 2) {
                            System.out.println("Invalid message. Please try again.");
                            break;
                        }

                        String error = serverResponseParts[1];
                        System.out.println("Error " + error);
                    }
                    case null, default -> System.out.println("Invalid/unknown command sent by server, ignore.");
                }
            }

            System.out.println("[Client] Closing connection...");
        } catch (IOException e) {
            System.out.println("[Client] IO exception: " + e);
        }
    }

    private static void help() {
        System.out.println("Usage:");
        System.out.println("  " + Message.JOIN + " <name> <game_id> - Join the game with the id sent with a name.");
        System.out.println("  " + Message.LISTGAMES + " - List all accessible games.");
        System.out.println("  " + Message.GUESS + " <guess> - Submit the character or word you want to guess.");
        System.out.println("  " + Message.HELP + " - Display this help message.");
    }
}
