package ch.heigvd.dai.server;

import ch.heigvd.dai.client.Client;
import ch.heigvd.dai.game.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    public enum Message {
        GAMES,
        GAMESTATE,
        CURRENTGUESS,
        LEFT,
        OK,
        ERROR,
    }

    private int port;

    public static String END_OF_LINE = "\n";

    //static private GameState gameState;
    static private Map<Integer, GameState> gameStates = new HashMap<Integer, GameState>();
    static private Map<String, Integer> usernameToRoomId = new HashMap<>();

    public Server(int port) {
        this.port = port;
        //gameState = new GameState();
        //gameState.startGame();
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
                int roomId = 0;
                String playerName = "default";

                String wordToguess = "test";

                System.out.println("[Server] The word to guess is: " + wordToguess);

                while (!socket.isClosed()) {
                    String clientRequest = in.readLine();

                    if (clientRequest == null) {
                        socket.close();
                        //supprimer le joueur lorsqu'il quit
                        gameStates.get(usernameToRoomId.get(playerName)).removePlayer(playerName);
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
                    //System.out.println("[Server] Got a message: " + message + " From user : " + clientRequestParts[1].split(" ", 2)[0]);
                    switch (message) {
                        case JOIN -> {
                            clientRequestParts = clientRequestParts[1].split(" ", 2);

                            if (clientRequestParts.length < 2) {
                                response = Message.ERROR + " not enough arguments" + END_OF_LINE;
                                break;
                            }

                            playerName = clientRequestParts[0];
                            try {
                                roomId = Integer.parseInt(clientRequestParts[1]);
                            } catch (NumberFormatException e) {
                                response = Message.ERROR + " invalid room ID" + END_OF_LINE;
                                break;
                            }

                            // Vérifiez si le nom d'utilisateur existe déjà dans cette salle
                            if (gameStates.containsKey(roomId)) {
                                GameState gameState = gameStates.get(roomId);
                                if (gameState.playerExists(playerName)) {
                                    response = Message.ERROR + " username already taken in this room" + END_OF_LINE;
                                    break;
                                }
                                gameState.newPlayer(playerName, roomId, out);
                                System.out.println("[Server] Room " + roomId + " already exists. Adding player to this room.");
                            } else {
                                gameStates.put(roomId, new GameState());
                                gameStates.get(roomId).startGame();
                                gameStates.get(roomId).newPlayer(playerName, roomId, out);
                                System.out.println("[Server] Room " + roomId + " does not exist. Creating new room.");
                            }

                            usernameToRoomId.put(playerName, roomId);
                            System.out.println("[Server] " + playerName + " joined the game " + roomId);
                            response = Message.OK + " " + gameStates.get(usernameToRoomId.get(clientRequestParts[0])).getPlayerLives(clientRequestParts[0]) + END_OF_LINE;
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
                            } /*else if (!gameState.playerExist(clientRequestParts[0])) {
                                response = Message.ERROR + " 3: Player doesn't exist in this game" + END_OF_LINE;
                                break;
                            }*/

                            boolean hasWon = gameStates.get(usernameToRoomId.get(clientRequestParts[0])).playerGuess(clientRequestParts[0], clientRequestParts[1]);
                            response = Message.CURRENTGUESS + " " + gameStates.get(usernameToRoomId.get(clientRequestParts[0])).getPlayerCurrentGuesses(clientRequestParts[0])  + END_OF_LINE;
                            System.out.println("response to specifique player: " + response);

                            for (PlayerState player : gameStates.get(usernameToRoomId.get(clientRequestParts[0])).getPlayers()) {
                                if(player.getRoomId() == gameStates.get(usernameToRoomId.get(clientRequestParts[0])).getPlayerRoomId(clientRequestParts[0])){
                                    String messageToAll = Message.GAMESTATE + " " +
                                            gameStates.get(usernameToRoomId.get(clientRequestParts[0])).getPlayerLives(clientRequestParts[0]) +
                                            " " + hasWon + " " + clientRequestParts[0] + END_OF_LINE;
                                    System.out.println("broadcasted response: " + messageToAll);
                                    player.out.write(messageToAll);
                                    player.out.flush();
                                }

                            }
                        }
                        case LISTGAMES -> {
                            if(gameStates.isEmpty()){
                                System.out.println("test");
                                response = Message.GAMES  + END_OF_LINE;
                                break;
                            }


                            Set<Integer> roomIds = gameStates.keySet();
                            if (roomIds.isEmpty()) {
                                response = Message.GAMES  + END_OF_LINE;
                                System.out.printf(response);
                            } else {
                                response = Message.GAMES +" "+ roomIds + END_OF_LINE;
                                System.out.printf(response);
                            }

                        }
                        case LEAVE -> {
                            clientRequestParts = clientRequestParts[1].split(" ", 2);

                            if (clientRequestParts.length < 2) {
                                response = Message.ERROR + " not enough arguments" + END_OF_LINE;
                                break;
                            }

                            playerName = clientRequestParts[0];
                            try {
                                roomId = Integer.parseInt(clientRequestParts[1]);
                            } catch (NumberFormatException e) {
                                response = Message.ERROR + " invalid room ID" + END_OF_LINE;
                                break;
                            }

                            if (!gameStates.containsKey(roomId)) {
                                response = Message.ERROR + " room does not exist" + END_OF_LINE;
                                break;
                            }

                            GameState gameState = gameStates.get(roomId);
                            if (!gameState.removePlayer(playerName)) {
                                response = Message.ERROR + " player not found in room" + END_OF_LINE;
                                break;
                            }

                            usernameToRoomId.remove(playerName);
                            System.out.println("[Server] " + playerName + " left the game " + roomId);

                            // Broadcast to all players in the room
                            //String broadcastMessage = Message.LEAVE + " " + playerName + " has left the game." + END_OF_LINE;
                            for (PlayerState player : gameState.getPlayers()) {
                                try {
                                    player.out.write(Message.LEFT +" "+ playerName + END_OF_LINE);
                                    player.out.flush();
                                } catch (IOException e) {
                                    System.err.println("[Server] Error broadcasting leave message: " + e.getMessage());
                                }
                            }

                            // Check if the room is empty and remove it
                            if (gameState.isEmpty()) {
                                gameStates.remove(roomId);
                                System.out.println("[Server] Room " + roomId + " is empty and has been removed.");
                            }

                            response = Message.LEFT + " " + playerName + END_OF_LINE;
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

            }
        }
    }

}
