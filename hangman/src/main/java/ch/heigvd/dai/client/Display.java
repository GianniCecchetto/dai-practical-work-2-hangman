package ch.heigvd.dai.client;

import java.util.HashMap;
import java.util.Map;

/**
 * The Display class manages the user interface for the Hangman game client.
 * It tracks the game state, including player progress, lives, and opponent status,
 * and provides methods for rendering the game's visual representation and prompts.
 */
public class Display {
    private String currentWordState, userName, gameList;
    private int livesLeft, roomId;
    private Boolean hasWon = false, gameListDisplayed = false;

    static private Map<String, Map.Entry<Integer, Boolean>> opponentLives = new HashMap<>();
    private final String swords = """
               ====)------------- * -------------(====
                """;
    private final String[] hangmanState = {
            """
              +---+
              |   |
                  |
                  |
                  |
                  |
            =========
            """,
            """
              +---+
              |   |
              O   |
                  |
                  |
                  |
            =========
            """,
            """
              +---+
              |   |
              O   |
              |   |
                  |
                  |
            =========
            """,
            """
              +---+
              |   |
              O   |
             /|   |
                  |
                  |
            =========
            """,
            """
              +---+
              |   |
              O   |
             /|\\  |
                  |
                  |
            =========
            """,
            """
              +---+
              |   |
              O   |
             /|\\  |
             /    |
                  |
            =========
            """,
            """
              +---+
              |   |
              O   |
             /|\\  |
             / \\  |
                  |
            =========
            """
    };

    /**
     * Default constructor for the Display class.
     */
    public Display() {}

    /**
     * Clears all game-related data, resetting the display state.
     */
    public void clearRoomData() {
        opponentLives.clear();
        currentWordState = null;
        livesLeft = 0;
        roomId = 0;
        hasWon = false;
        gameListDisplayed = false;
    }

    /**
     * Updates the current state of the word being guessed.
     *
     * @param currentWordState The current state of the word as a string.
     */
    public void setCurrentWordState(String currentWordState) {
        this.currentWordState = currentWordState;
    }

    /**
     * Updates the number of lives left for the player.
     *
     * @param livesLeft The number of lives remaining.
     */
    public void setLivesLeft(int livesLeft) {
        this.livesLeft = livesLeft;
    }

    /**
     * Sets the room ID for the current game.
     *
     * @param roomId The room ID as an integer.
     */
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    /**
     * Sets the username of the player.
     *
     * @param userName The username as a string.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Updates the list of available games.
     *
     * @param gameList A string containing game information.
     */
    public void setGameList(String gameList) {
        this.gameList = gameList;
    }

    /**
     * Sets whether the player has won the game.
     *
     * @param hasWon True if the player has won, false otherwise.
     */
    public void setHasWon(Boolean hasWon) {
        this.hasWon = hasWon;
    }

    /**
     * Gets the player's win status.
     *
     * @return True if the player has won, false otherwise.
     */
    public Boolean getHasWon() {
        return hasWon;
    }

    /**
     * Gets the number of lives left for the player.
     *
     * @return The number of lives as an integer.
     */
    public int getLivesLeft() {
        return livesLeft;
    }

    /**
     * Updates whether the game list is displayed.
     *
     * @param gameListDisplayed True if the game list is displayed, false otherwise.
     */
    public void setGameListDisplayed(Boolean gameListDisplayed) {
        this.gameListDisplayed = gameListDisplayed;
    }

    /**
     * Removes a player from the list of opponents.
     *
     * @param playerName The name of the player to remove.
     */
    public void removePlayer(String playerName) {
        if (opponentLives.containsKey(playerName)) {
            opponentLives.remove(playerName);
            System.out.println(playerName + " has been removed from the game.");
        } else {
            System.out.println(playerName + " was not found in the game.");
        }
    }

    /**
     * Updates the lives and win status for an opponent.
     *
     * @param userName The name of the opponent.
     * @param lives    The number of lives remaining for the opponent.
     * @param hasWon   True if the opponent has won, false otherwise.
     */
    public void setOpponentLives(String userName, int lives, Boolean hasWon) {
        opponentLives.put(userName, Map.entry(lives, hasWon));
    }

    /**
     * Clears the console display.
     */
    public void clearDisplay() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /**
     * Displays the command prompt to the user.
     */
    public void displayCmdPrompt() {
        System.out.print("CMD > ");
    }

    /**
     * Displays the list of available games.
     */
    void displayGamelist() {
        System.out.println("<===Available Games ===>");
        System.out.println(gameList);
    }

    /**
     * Updates and renders the current state of the game.
     */
    void updateGameState() {
        if (gameListDisplayed) {
            displayGamelist();
        }
        System.out.println("\n=====HANGMAN=====");
        System.out.println("--------");
        System.out.println("Current room: " + roomId);
        System.out.println("Current username: " + userName);
        System.out.println("--------\n\n");

        System.out.println("Opponent:");
        System.out.println(swords);

        for (Map.Entry<String, Map.Entry<Integer, Boolean>> entry : opponentLives.entrySet()) {
            if (entry.getValue().getValue()) {
                System.out.println(" - " + entry.getKey() + ": Has found the word");
            } else if (entry.getValue().getKey() == 0) {
                System.out.println(" - " + entry.getKey() + ": Is dead");
            } else {
                System.out.println(" - " + entry.getKey() + ": " + entry.getValue().getKey() + " lives");
            }
        }
        System.out.println(swords);

        if (hasWon) {
            System.out.println("You found the word");
        }

        System.out.println(hangmanState[6 - livesLeft]);
        if (livesLeft == 0) {
            System.out.println("You died :(");
        } else {
            System.out.println("Lives: " + livesLeft);
        }
        System.out.println("PROGRESS : " + currentWordState);
    }

    /**
     * Displays help instructions for using the client.
     */
    public void help() {
        System.out.println("Usage:");
        System.out.println("  " + Client.Message.JOIN + " <name> <game_id> - Join the game with the id sent with a name.");
        System.out.println("  " + Client.Message.LISTGAMES + " - List all accessible games.");
        System.out.println("  " + Client.Message.GUESS + " <guess> - Submit the character or word you want to guess.");
        System.out.println("  " + Client.Message.LEAVE + " - Leave the current game.");
        System.out.println("  " + Client.Message.QUIT + " - Quit the client completely.");
        System.out.println("  " + Client.Message.HELP + " - Display this help message.");

        displayCmdPrompt();
    }
}
