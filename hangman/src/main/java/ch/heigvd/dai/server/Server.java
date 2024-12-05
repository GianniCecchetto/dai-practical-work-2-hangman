package ch.heigvd.dai.server;

import ch.heigvd.dai.client.Client;
import ch.heigvd.dai.game.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Server class manages the server-side logic for the Hangman game.
 * It handles incoming client connections, processes their requests,
 * and manages the game states for different rooms.
 */
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

    static private Map<Integer, GameState> gameStates = new HashMap<Integer, GameState>();
    static private Map<String, Integer> usernameToRoomId = new HashMap<>();

    /**
     * Constructs a Server with the specified port.
     *
     * @param port The port on which the server will listen for client connections.
     */
    public Server(int port) {
        this.port = port;
    }

    /**
     * Starts the server, listens for client connections, and handles them in separate threads.
     */
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port);
             ExecutorService executor = Executors.newCachedThreadPool();) {
            System.out.println("[Server] creating server with address " + InetAddress.getLocalHost());
            System.out.println("[Server] listening on port " + port);

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.out.println("[Server] exception: " + e);
        }
    }

    /**
     * Handles the communication with a client, processes their requests, and manages game state updates.
     */
    static class ClientHandler implements Runnable {
        private final Socket socket;

        /**
         * Constructs a ClientHandler to handle the given client socket.
         *
         * @param socket The socket for communication with the client.
         */
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        /**
         * The run method processes client requests in a loop, handling messages such as JOIN, GUESS, LISTGAMES, etc.
         */
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

                int roomId = 0;
                String playerName = "default";
                String wordToguess = "test";
                Boolean gameIsRestarting = false;

                System.out.println("[Server] The word to guess is: " + wordToguess);

                while (!socket.isClosed()) {
                    gameIsRestarting = false;
                    String clientRequest = in.readLine();

                    if (clientRequest == null) {
                        socket.close();
                        gameStates.get(usernameToRoomId.get(playerName)).removePlayer(playerName);
                        continue;
                    }

                    String[] clientRequestParts = clientRequest.split(" ", 2);
                    Client.Message message = null;
                    try {
                        message = Client.Message.valueOf(clientRequestParts[0]);
                    } catch (Exception e) {}
                    System.out.println(message);
                    String response = "";
                    switch (message) {
                        case JOIN -> {
                            // Handle player joining the game.
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

                            GameState gameState;
                            Boolean usrnmtaken = false;
                            for (Integer rid : gameStates.keySet()) {
                                gameState = gameStates.get(rid);

                                if (gameState.playerExists(playerName)) {
                                    System.out.println("username already taken");
                                    response = Message.ERROR + " username already taken" + END_OF_LINE;
                                    usrnmtaken = true;
                                    break;
                                }
                            }
                            if (usrnmtaken)
                                break;

                            gameState = gameStates.get(roomId);

                            if (gameStates.containsKey(roomId)) {
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
                            // Handle player's guess request.
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
                            }

                            boolean hasWon = gameStates.get(usernameToRoomId.get(clientRequestParts[0])).playerGuess(clientRequestParts[0], clientRequestParts[1]);
                            gameStates.get(usernameToRoomId.get(clientRequestParts[0])).playerHasWon(clientRequestParts[0],hasWon);
                            response = Message.CURRENTGUESS + " " + gameStates.get(usernameToRoomId.get(clientRequestParts[0])).getPlayerCurrentGuesses(clientRequestParts[0])  + END_OF_LINE;
                            System.out.println("response to specifique player: " + response);

                            for (PlayerState player : gameStates.get(usernameToRoomId.get(clientRequestParts[0])).getPlayers()) {
                                if (player.getRoomId() == gameStates.get(usernameToRoomId.get(clientRequestParts[0])).getPlayerRoomId(clientRequestParts[0])) {
                                    String messageToAll = Message.GAMESTATE + " " +
                                            gameStates.get(usernameToRoomId.get(clientRequestParts[0])).getPlayerLives(clientRequestParts[0]) +
                                            " " + hasWon + " " + clientRequestParts[0] + END_OF_LINE;
                                    System.out.println("broadcasted response: " + messageToAll);
                                    player.out.write(messageToAll);
                                    player.out.flush();
                                }
                            }

                            if (gameStates.get(usernameToRoomId.get(clientRequestParts[0])).isGameFinished()) {
                                out.write(response);
                                out.flush();

                                System.out.println("The game will restart in 5 seconds");
                                gameIsRestarting = true;
                                try {
                                    Thread.sleep(5000);
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    System.err.println("Error during sleep: " + e.getMessage());
                                }

                                gameStates.get(usernameToRoomId.get(clientRequestParts[0])).startGame();

                                GameState restartedGame = gameStates.get(usernameToRoomId.get(clientRequestParts[0]));
                                for (PlayerState player : restartedGame.getPlayers()) {
                                    try {
                                        String restartmsg = Message.OK + " " + player.getLives() + END_OF_LINE;
                                        player.out.write(restartmsg);
                                        player.out.flush();
                                        System.out.println("[Server] Broadcasted end game message to player : " + message);
                                    } catch (IOException e) {
                                        System.err.println("[Server] Error broadcasting end game message: " + e.getMessage());
                                    }
                                }
                            }
                        }

                        case LISTGAMES -> {
                            // List all available game rooms.
                            if (gameStates.isEmpty()) {
                                response = Message.GAMES + END_OF_LINE;
                                break;
                            }

                            Set<Integer> roomIds = gameStates.keySet();
                            if (roomIds.isEmpty()) {
                                response = Message.GAMES + END_OF_LINE;
                            } else {
                                response = Message.GAMES + " " + roomIds + END_OF_LINE;
                            }
                        }

                        case LEAVE -> {
                            // Handle player leaving the game.
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

                            for (PlayerState player : gameState.getPlayers()) {
                                try {
                                    player.out.write(Message.LEFT + " " + playerName + END_OF_LINE);
                                    player.out.flush();
                                } catch (IOException e) {
                                    System.err.println("[Server] Error broadcasting leave message: " + e.getMessage());
                                }
                            }

                            if (gameState.isEmpty()) {
                                gameStates.remove(roomId);
                                System.out.println("[Server] Room " + roomId + " is empty and has been removed.");
                            }

                            response = Message.LEFT + " " + playerName + END_OF_LINE;
                        }

                        case null, default -> {
                            // Handle invalid message types.
                            response = Message.ERROR + " -1: invalid message" + END_OF_LINE;
                        }
                    }

                    // Send the response to the client if the game is not restarting.
                    if (!gameIsRestarting) {
                        out.write(response);
                        out.flush();
                    }
                }

                System.out.println("[Server] Closing connection");
            } catch (IOException e) {
                System.out.println("[Server] IO exception: " + e);
            }
        }
    }
}
