package ch.heigvd.dai.game;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The GameState class manages the state of the Hangman game, including players, the word to guess, and the game's rules.
 */
public class GameState {
    private Map<String, PlayerState> players;
    private String wordToGuess;

    private String[] possibleWords = {
            "apple", "banana", "orange", "grape", "peach", "lemon", "cherry", "pear", "melon", "plum"
    };

    /**
     * Constructs a new GameState, initializing the players map and starting the game.
     */
    public GameState() {
        players = new HashMap<String, PlayerState>();
        startGame();
    }

    /**
     * Removes a player from the game.
     *
     * @param playerName The name of the player to remove.
     * @return True if the player was successfully removed, otherwise false.
     */
    public boolean removePlayer(String playerName) {
        if (players.containsKey(playerName)) {
            players.remove(playerName);
            return true;
        }
        return false;
    }

    /**
     * Checks if the game has no players.
     *
     * @return True if there are no players in the game, otherwise false.
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }

    /**
     * Adds a new player to the game with a given username, room ID, and output stream.
     *
     * @param username The player's username.
     * @param roomId The room ID the player belongs to.
     * @param out The output stream to communicate with the player.
     * @throws IOException If an error occurs while setting up the player's output stream.
     */
    public void newPlayer(String username, int roomId, BufferedWriter out) throws IOException {
        players.put(username, new PlayerState(wordToGuess.length(), roomId, out));
    }

    /**
     * Gets the room ID for a player.
     *
     * @param username The username of the player.
     * @return The room ID the player belongs to.
     */
    public int getPlayerRoomId(String username) {
        return players.get(username).roomId;
    }

    /**
     * Gets the number of lives remaining for a player.
     *
     * @param username The username of the player.
     * @return The number of lives left for the player.
     */
    public int getPlayerLives(String username) {
        return players.get(username).nbLiveLeft;
    }

    /**
     * Gets the current guesses of a player.
     *
     * @param username The username of the player.
     * @return The current guesses string for the player.
     */
    public String getPlayerCurrentGuesses(String username) {
        return players.get(username).currentGuesses;
    }

    /**
     * Checks if a player exists in the game.
     *
     * @param playerName The username of the player.
     * @return True if the player exists in the game, otherwise false.
     */
    public boolean playerExists(String playerName) {
        return players.containsKey(playerName);
    }

    /**
     * Sets the win status for a player.
     *
     * @param username The username of the player.
     * @param hasWon The win status of the player.
     */
    public void playerHasWon(String username, Boolean hasWon) {
        players.get(username).sethasWon(hasWon);
    }

    /**
     * Checks if the game has finished, i.e., if all players have either won or lost.
     *
     * @return True if the game is finished, otherwise false.
     */
    public Boolean isGameFinished() {
        for (PlayerState player : players.values()) {
            if (player.nbLiveLeft > 0 && !player.gethasWon()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Processes a player's guess and updates the game state accordingly.
     *
     * @param username The username of the player making the guess.
     * @param guess The guess made by the player (a single character or the entire word).
     * @return True if the guess was correct and the player has completed the word, otherwise false.
     */
    public boolean playerGuess(String username, String guess) {
        PlayerState currentPlayer = players.get(username);

        if (guess.length() <= 1) {
            char charGuess = guess.toUpperCase().charAt(0);

            if (!wordToGuess.contains(guess.toUpperCase())) {
                currentPlayer.nbLiveLeft--;
                return false;
            }

            String newCurrentGuesses = "";
            for (int i = 0; i < wordToGuess.length(); i++) {
                if (currentPlayer.currentGuesses.charAt(i) != '_') {
                    newCurrentGuesses += currentPlayer.currentGuesses.charAt(i);
                    continue;
                }

                if (wordToGuess.charAt(i) == charGuess) {
                    newCurrentGuesses += charGuess;
                    continue;
                }

                newCurrentGuesses += '_';
            }

            currentPlayer.currentGuesses = newCurrentGuesses;
            if (wordToGuess.equals(currentPlayer.currentGuesses))
                return true;

            return false;
        }
        if (wordToGuess.equals(guess.toUpperCase())) {
            currentPlayer.currentGuesses = guess.toUpperCase();
            return true;
        }
        return false;
    }

    /**
     * Gets the list of players in the game.
     *
     * @return An array of all players' states.
     */
    public PlayerState[] getPlayers() {
        return players.values().toArray(new PlayerState[players.size()]);
    }

    /**
     * Starts a new game by selecting a random word and resetting the players' states.
     */
    public void startGame() {
        wordToGuess = possibleWords[(int) (Math.random() * possibleWords.length)];
        wordToGuess = wordToGuess.toUpperCase();

        for (PlayerState player : players.values()) {
            player.reset(wordToGuess.length());
        }
    }
}
