package ch.heigvd.dai.client;

import ch.heigvd.dai.server.Server;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {
    private String host;
    private int port;
    static private String userName = "default";
    private static Display display = new Display();
    static Boolean tryJoin = false,isGameJoined = false;

    static int roomId;

    public enum Message {
        JOIN,
        LEAVE,
        LISTGAMES,
        GUESS,
        HELP,
        QUIT,
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

            display.displayCmdPrompt();
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
                            roomId = Integer.parseInt(userInputParts[1]);

                            request = Message.JOIN + " " + userName + " " + roomId + END_OF_LINE;
                            tryJoin = true;
                        }
                        case LISTGAMES -> {
                            request = Message.LISTGAMES + END_OF_LINE;
                        }
                        case GUESS -> {
                            if(isGameJoined){
                                if(userInputParts[1] == null)
                                    break;
                                if(userInputParts[1].isEmpty()){
                                    System.out.println("Empty guess are not allowed.");
                                    display.displayCmdPrompt();
                                    break;
                                }

                                if(display.getHasWon()){
                                    System.out.println("You already found the word.\nWait for the game to restart or join another game");
                                    display.displayCmdPrompt();
                                    break;
                                }

                                if(display.getLivesLeft() <= 0){
                                    System.out.println("You are Dead :(.\nWait for the game to restart or join another game");
                                    display.displayCmdPrompt();
                                    break;
                                }

                                String guess = userInputParts[1];


                                request = Message.GUESS + " " + userName + " " + guess + END_OF_LINE;
                            }else{
                                System.out.println("You must join a game to guess.");
                                display.displayCmdPrompt();
                            }

                        }
                        case LEAVE -> {
                            if (isGameJoined) {
                                request = Message.LEAVE + " " + userName + " " + roomId + END_OF_LINE;
                                isGameJoined = false;
                                display.clearDisplay();
                                display.waitingForJoin();
                            } else {
                                System.out.println("You are not in a game.");
                            }
                        }
                        case HELP -> display.help();
                        case QUIT -> {
                            System.out.println("Exiting the client. Goodbye!");
                            try {
                                socket.close();
                            } catch (IOException e) {
                                System.err.println("Error closing the socket: " + e.getMessage());
                            }
                            System.exit(0);
                        }
                    }

                    if (request != null ) {
                        out.write(request);
                        out.flush();
                    }
                } catch (Exception e) {
                    System.out.println("Invalid command. Please try again.");
                    display.displayCmdPrompt();
                }

            }

            System.out.println("[Client] Closing connection...");
        } catch (IOException e) {
            System.out.println("[Client] IO exception: " + e);
        }
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
                          System.out.println("server ok." + serverResponse);
                        if(tryJoin){
                            isGameJoined = true;
                            display.setUserName(userName);
                            display.setRoomId(roomId);
                            display.setLivesLeft(Integer.valueOf(serverResponse));
                            display.clearDisplay();
                        }
                      }
                      case LEFT -> {
                          if (serverResponseParts[1].equals(userName)) {
                              isGameJoined = false;
                              display.clearDisplay();
                              display.clearRoomData();
                              System.out.println("You have left the game.");
                          } else {

                              display.removePlayer(serverResponseParts[1]); // Supprime le joueur de la liste dans l'affichage
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
