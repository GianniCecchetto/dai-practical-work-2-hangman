package ch.heigvd.dai.client;

import ch.heigvd.dai.server.Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Client class represents the client-side application for connecting to a multiplayer game server.
 * It provides functionality for sending commands, receiving server messages, and interacting with the game.
 */
public class Client {
    private String host;
    private int port;
    static private String userName = "default";
    private static Display display = new Display();
    static Boolean tryJoin = false,isGameJoined = false;

    static int roomId;

    /**
     * Enumeration of available client commands.
     */
    public enum Message {
        JOIN,       // Join a game room.
        LEAVE,      // Leave a game room.
        LISTGAMES,  // List available game rooms.
        GUESS,      // Make a guess in the current game.
        HELP,       // Display help information.
        QUIT        // Quit the application.
    }

    public static String END_OF_LINE = "\n";

    /**
     * Constructs a Client with the specified host and port.
     *
     * @param host The server's hostname or IP address.
     * @param port The server's port number.
     */
    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Starts the client application, connecting to the server and handling user input and server communication.
     */
    public void run() {
        System.out.println("[Client] Connecting to " + host + ":" + port + "...");

        try (Socket socket = new Socket(host, port);
             Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(writer)) {

            System.out.println("[Client] Connected to " + host + ":" + port);

            // Start a thread to handle incoming messages from the server.
            ExecutorService executor = Executors.newCachedThreadPool();
            executor.submit(new MessageReceiver(socket));

            // Display the command prompt for user input.
            display.displayCmdPrompt();

            // Process user input until the socket is closed.
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

                            if(isGameJoined){
                                System.out.println("You must leave the game before joining an another one.");
                                display.displayCmdPrompt();
                                break;
                            }

                            userInputParts = userInputParts[1].split(" ", 2);
                            userName = userInputParts[0];
                            roomId = Integer.parseInt(userInputParts[1]);

                            request = Message.JOIN + " " + userName + " " + roomId + END_OF_LINE;
                            tryJoin = true;
                        }
                        case LISTGAMES -> {
                            request = Message.LISTGAMES + END_OF_LINE;
                        }
                        case GUESS -> {
                            if (isGameJoined) {
                                if (userInputParts[1] == null) break;
                                if (userInputParts[1].isEmpty()) {
                                    System.out.println("Empty guesses are not allowed.");
                                    display.displayCmdPrompt();
                                    break;
                                }
                                if (display.getHasWon()) {
                                    System.out.println("You already found the word. Wait for the game to restart or join another game.");
                                    display.displayCmdPrompt();
                                    break;
                                }
                                if (display.getLivesLeft() <= 0) {
                                    System.out.println("You are dead. :( Wait for the game to restart or join another game.");
                                    display.displayCmdPrompt();
                                    break;
                                }

                                String guess = userInputParts[1];
                                request = Message.GUESS + " " + userName + " " + guess + END_OF_LINE;
                            } else {
                                System.out.println("You must join a game to guess.");
                                display.displayCmdPrompt();
                            }
                        }
                        case LEAVE -> {
                            if (isGameJoined) {
                                request = Message.LEAVE + " " + userName + " " + roomId + END_OF_LINE;
                                isGameJoined = false;
                                display.clearDisplay();
                                display.displayCmdPrompt();
                            } else {
                                System.out.println("You are not in a game.");
                                display.displayCmdPrompt();
                            }
                        }
                        case HELP -> display.help();
                        case QUIT -> {
                            if (isGameJoined) {
                                request = Message.LEAVE + " " + userName + " " + roomId + END_OF_LINE;
                                isGameJoined = false;
                                display.clearDisplay();
                                out.write(request);
                                out.flush();
                            }
                            System.out.println("Exiting the client. Goodbye!");
                            try {
                                socket.close();
                            } catch (IOException e) {
                                System.err.println("Error closing the socket: " + e.getMessage());
                            }
                            System.exit(0);
                        }
                    }

                    if (request != null) {
                        out.write(request);
                        out.flush();
                    }
                } catch (Exception e) {
                    System.out.println("Invalid command. Please try again.");
                    System.out.println(e.getMessage());
                    display.displayCmdPrompt();
                }
            }

            System.out.println("[Client] Closing connection...");
        } catch (IOException e) {
            System.out.println("[Client] IO exception: " + e);
        }
    }

    /**
     * Inner class to handle incoming messages from the server asynchronously.
     */
    static class MessageReceiver  implements Runnable {
        private Socket socket;

        /**
         * Constructs a MessageReceiver for the given socket.
         *
         * @param socket The socket connected to the server.
         */
        public MessageReceiver (Socket socket) {
            this.socket = socket;
        }
        /**
         *
         * Reads and processes server messages in a separate thread.
         */
        @Override
        public void run() {

            try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                while (!socket.isClosed()) {
                    String serverResponse = in.readLine();
                  if (serverResponse == null) {
                      socket.close();
                      continue;
                  }

                  String[] serverResponseParts = serverResponse.split(" ", 2);

                  Server.Message message = null;
                  try {
                      message = Server.Message.valueOf(serverResponseParts[0]);
                  } catch (IllegalArgumentException e) {}

                  try{
                      serverResponse = serverResponseParts[1];
                  } catch (RuntimeException e) {
                      serverResponse = "";
                  }

                  switch (message) {
                      case GAMES -> {
                          if(serverResponseParts.length == 1){
                              System.out.println("\nNo game yet.");
                              display.displayCmdPrompt();
                              break;
                          }

                          display.setGameList(serverResponseParts[1]);
                          if(isGameJoined){
                                  display.clearDisplay();
                                  display.setGameListDisplayed(true);
                          }else{
                              display.displayGamelist();
                              display.displayCmdPrompt();
                          }

                      }
                      case CURRENTGUESS -> {
                          display.clearDisplay();
                          display.setCurrentWordState(serverResponse);
                      }
                      case GAMESTATE -> {
                          serverResponseParts = serverResponseParts[1].split(" ", 3);
                          if(serverResponseParts[2].equals(userName)) {
                              display.setLivesLeft(Integer.parseInt(serverResponseParts[0]));
                              display.setHasWon(Boolean.valueOf(serverResponseParts[1]));
                          }else{
                            display.setOpponentLives(serverResponseParts[2], Integer.parseInt(serverResponseParts[0]), Boolean.valueOf(serverResponseParts[1]));
                          }
                          display.clearDisplay();
                      }
                      case OK -> {
                            isGameJoined = true;
                            display.clearRoomData();
                            display.setUserName(userName);
                            display.setRoomId(roomId);
                            display.setLivesLeft(Integer.valueOf(serverResponse));
                            display.clearDisplay();
                      }
                      case LEFT -> {
                          if (serverResponseParts[1].equals(userName)) {
                              isGameJoined = false;
                              display.clearDisplay();
                              display.clearRoomData();
                              System.out.println("You have left the game.");
                          } else {

                              display.removePlayer(serverResponseParts[1]);
                              display.clearDisplay();
                              System.out.println(serverResponseParts[1] + " has left the game.");
                          }
                          display.displayCmdPrompt();
                      }

                      case ERROR -> {
                          if (serverResponseParts.length < 2) {
                              System.out.println("Invalid message. Please try again.");
                          }else{
                              System.out.println("Error " + serverResponseParts[1]);
                          }
                          display.displayCmdPrompt();
                      }
                      case null, default -> {
                          System.out.println(serverResponse);
                          System.out.println("Invalid/unknown command sent by server, ignore.");
                      }
                  }
                  if(isGameJoined){
                      display.updateGameState();
                      display.displayCmdPrompt();
                  }


                }
            } catch (IOException e) {
                System.err.println("Connexion to server lost : " + e.getMessage());
            }
        }
    }
}
