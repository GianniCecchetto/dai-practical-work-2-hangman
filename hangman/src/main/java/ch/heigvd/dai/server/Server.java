package ch.heigvd.dai.server;

import ch.heigvd.dai.client.Client;

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
        OK,
        ERROR,
    }

    private int port;

    public static String END_OF_LINE = "\n";

    public Server(int port){
        this.port = port;
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port);
             ExecutorService executor = Executors.newCachedThreadPool(); ) {
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

                        String response = "";
                        System.out.println("[Server] Got a message: " + message);
                        switch (message) {
                            case JOIN -> {
                                System.out.println("[Server] New client joined the game on " + clientRequestParts[1]);
                                response = Message.OK + END_OF_LINE;
                            }
                            case LISTGAMES -> {
                                if (gameInProgress) {
                                    response =
                                            Message.ERROR + " 1: a game is already launched" + END_OF_LINE;
                                } else {
                                    System.out.println("[Server] Sending game list");
                                    response = "la list mdr" + END_OF_LINE;
                                    //response = Message.OK + END_OF_LINE;
                                }
                            }
                            case GUESS -> {
                                if (clientRequestParts[1].length() > 50) {
                                    response = Message.ERROR + " 2: more than 50 character" + END_OF_LINE;
                                    break;
                                }else if(clientRequestParts[1].isEmpty()) {
                                    response = Message.ERROR + " 1: empty string" + END_OF_LINE;
                                    break;
                                }

                                try {
                                    response = Message.OK + END_OF_LINE;
                                } catch (NumberFormatException e) {
                                    response = Message.ERROR + " 2: the guess is not a character or a word" + END_OF_LINE;
                                }
                            }
                            case null, default -> {
                                response = Message.ERROR + " -1: invalid message" + END_OF_LINE;
                            }
                        }
                        // System.out.println(response);
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
