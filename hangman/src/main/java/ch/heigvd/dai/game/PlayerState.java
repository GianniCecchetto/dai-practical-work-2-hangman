package ch.heigvd.dai.game;

import java.io.BufferedWriter;

/**
 * The PlayerState class represents the state of a player in the Hangman game.
 * It tracks the player's remaining lives, current guesses, and associated room ID.
 */
public class PlayerState {

    static final int MAX_LIVES = 6;
    int nbLiveLeft;
    String currentGuesses;
    public BufferedWriter out;
    int roomId;
    Boolean hasWon = false;

    /**
     * Constructs a new PlayerState with the default values.
     * The player starts with the maximum number of lives and an empty guess string.
     */
    public PlayerState(){
        nbLiveLeft = MAX_LIVES;
        currentGuesses = "";
    }

    /**
     * Sets whether the player has won the game.
     *
     * @param hasWon True if the player has won, otherwise false.
     */
    public void sethasWon(Boolean hasWon) {
        this.hasWon = hasWon;
    }

    /**
     * Gets the status of whether the player has won the game.
     *
     * @return True if the player has won, otherwise false.
     */
    public Boolean gethasWon() {
        return hasWon;
    }

    /**
     * Gets the number of lives remaining for the player.
     *
     * @return The number of lives left.
     */
    public int getLives() {
        return nbLiveLeft;
    }

    /**
     * Gets the room ID that the player belongs to.
     *
     * @return The room ID associated with the player.
     */
    public int getRoomId() {
        return roomId;
    }

    /**
     * Constructs a new PlayerState with the specified word length, room ID, and output stream.
     * The player's guesses are initialized with underscores corresponding to the word length.
     *
     * @param wordLength The length of the word to guess, which determines the initial guess string.
     * @param roomId The room ID the player belongs to.
     * @param out The output stream used to send messages to the client.
     */
    public PlayerState(int wordLength, int roomId, BufferedWriter out){
        this.roomId = roomId;
        nbLiveLeft = MAX_LIVES;
        currentGuesses = "_".repeat(wordLength);
        this.out = out;
    }

    /**
     * Resets the player's state to its initial values, including the maximum lives and
     * a new guess string with underscores based on the given word length.
     *
     * @param wordLength The length of the word to guess, which will reset the current guesses.
     */
    public void reset(int wordLength){
        currentGuesses = "_".repeat(wordLength); // Reset the current guesses to underscores.
        nbLiveLeft = PlayerState.MAX_LIVES; // Reset the number of lives to the maximum.
    }
}
