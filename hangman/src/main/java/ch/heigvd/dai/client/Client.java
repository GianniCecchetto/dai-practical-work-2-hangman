package ch.heigvd.dai.client;

import ch.heigvd.dai.server.Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private String host;
    private int port;
    static private String userName = "default";

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
             Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(writer);) {
            System.out.println("[Client] Connected to " + host + ":" + port);

            ExecutorService executor = Executors.newCachedThreadPool();
            executor.submit(new MessageReceiver(socket));

            while (!socket.isClosed()) {
                Reader inputReader = new InputStreamReader(System.in, StandardCharsets.UTF_8);
                BufferedReader bir = new BufferedReader(inputReader);
                String userInput = bir.readLine();
                String request = null;

                try {
                    String[] userInputParts = userInput.split(" ", 2);
                    Message message = Message.valueOf(userInputParts[0].toUpperCase());

                    request = null;

                    switch (message) {
                        case JOIN -> {
                            userInputParts = userInputParts[1].split(" ", 2);
                            userName = userInputParts[0];
                            int gameId = Integer.parseInt(userInputParts[1]);

                            request = Message.JOIN + " " + userName + " " + gameId + END_OF_LINE;
                        }
                        case LISTGAMES -> {
                            request = Message.LISTGAMES + END_OF_LINE;
                        }
                        case GUESS -> {
                            String guess = userInputParts[1];

                            request = Message.GUESS + " " + userName + " " + guess + END_OF_LINE;
                        }
                        case HELP -> help();
                    }

                    if (request != null) {
                        out.write(request);
                        out.flush();
                    }
                } catch (Exception e) {
                    System.out.println("Invalid command. Please try again.");
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

    static class MessageReceiver  implements Runnable {
        private Socket socket;

        public MessageReceiver (Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {


          
            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String serverMessage;

                while (!socket.isClosed()) {
                    System.out.print("> ");

                    String serverResponse = in.readLine();
                  //System.out.println(serverResponse);
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
                      case CURRENTGUESS -> System.out.println("current guess " + serverResponse);
                      case GAMESTATE -> System.out.println("game state " + serverResponse);
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
            } catch (IOException e) {
                System.err.println("Connexion au serveur perdue : " + e.getMessage());
            }
        }
    }
}
