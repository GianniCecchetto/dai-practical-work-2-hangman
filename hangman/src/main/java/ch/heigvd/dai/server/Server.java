package ch.heigvd.dai.server;

import ch.heigvd.dai.client.Client;
import ch.heigvd.dai.game.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public enum Message {
        GAMES,
        GAMESTATE,
        CURRENTGUESS,
        OK,
        ERROR,
    }

    private int port;

    public static String END_OF_LINE = "\n";

    static private GameState gameState;

    public Server(int port) {
        this.port = port;

        gameState = new GameState();
        gameState.startGame();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port);
             ExecutorService executor = Executors.newCachedThreadPool();) {
            System.out.println("[Server] listening on port " + port);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("[Server] exception: " + e);
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (socket;
                 Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
                 BufferedReader in = new BufferedReader(reader);
                 Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
                 BufferedWriter out = new BufferedWriter(writer)) {

                System.out.println(
                        "[Server] New client connected from "
                                + socket.getInetAddress().getHostAddress()
                                + ":"
                                + socket.getPort());

                // Set game in progress
                boolean gameInProgress = true;
                String wordToguess = "test";

                System.out.println("[Server] The word to guess is: " + wordToguess);

                while (!socket.isClosed()) {
                    String clientRequest = in.readLine();

                    if (clientRequest == null) {
                        socket.close();
                        continue;
                    }

                    String[] clientRequestParts = clientRequest.split(" ", 2);
                    Client.Message message = null;
                    try {
                        message = Client.Message.valueOf(clientRequestParts[0]);
                    } catch (Exception e) {
                        // Do nothing
                    }
                    System.out.println(message);
                    String response = "";
                    System.out.println("[Server] Got a message: " + message + " From user : " + clientRequestParts[1].split(" ", 2)[0]);
                    switch (message) {
                        case JOIN -> {
                            clientRequestParts = clientRequestParts[1].split(" ", 2);

                            if (clientRequestParts.length < 2) {
                                response = Message.ERROR + " not enough arguments" + END_OF_LINE;
                                break;
                            }

                            gameState.newPlayer(clientRequestParts[0], out);
                            System.out.println("[Server] " + clientRequestParts[0] + " joined the game " + clientRequestParts[1]);
                            response = Message.OK + END_OF_LINE;
                        }
                        case LISTGAMES -> {
                            if (gameInProgress) {
                                response =
                                        Message.ERROR + " 1: a game is already launched" + END_OF_LINE;
                                break;
                            }

                            System.out.println("[Server] Sending game list");
                            response = Message.GAMES + " currently supporting 1 game" + END_OF_LINE;
                        }
                        case GUESS -> {
                            clientRequestParts = clientRequestParts[1].split(" ", 2);

                            if (clientRequestParts.length < 2) {
                                response = Message.ERROR + " not enough arguments" + END_OF_LINE;
                                break;
                            }

                            if (clientRequestParts[1].length() > 50) {
                                response = Message.ERROR + " 2: more than 50 character" + END_OF_LINE;
                                break;
                            } else if (clientRequestParts[1].isEmpty()) {
                                response = Message.ERROR + " 1: empty string" + END_OF_LINE;
                                break;
                            } else if (!gameState.playerExist(clientRequestParts[0])) {
                                response = Message.ERROR + " 3: Player doesn't exist in this game" + END_OF_LINE;
                                break;
                            }

                            boolean hasWon = gameState.playerGuess(clientRequestParts[0], clientRequestParts[1]);
                            response = Message.GAMESTATE + " " + gameState.getUpdate(clientRequestParts[0]) + " " + hasWon + " " + clientRequestParts[0] + END_OF_LINE;
                            System.out.println("response to specifique player: " + response);

                            for (PlayerState player : gameState.getPlayers()) {
                                String messageToAll = Message.GAMESTATE + " " + player.getLives() + " " + hasWon + " " + clientRequestParts[0] + END_OF_LINE;
                                System.out.println("broadcasted response: " + messageToAll);

                                player.out.write(messageToAll);
                                player.out.flush();
                            }
                        }
                        case null, default -> {
                            response = Message.ERROR + " -1: invalid message" + END_OF_LINE;
                        }
                    }
                    out.write(response);
                    out.flush();
                }

                System.out.println("[Server] Closing connection");
            } catch (IOException e) {
                System.out.println("[Server] IO exception: " + e);
                //return 1;
            }
        }
    }

}
