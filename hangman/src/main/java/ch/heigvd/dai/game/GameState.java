package ch.heigvd.dai.game;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GameState {
    private Map<String, PlayerState> players;
    private String wordToGuess;

    public GameState() {
        players = new HashMap<String, PlayerState>();

        startGame();
    }

    public void newPlayer(String username, BufferedWriter out) throws IOException {
        players.put(username, new PlayerState(wordToGuess.length(), out));
    }

    public int getPlayerLives(String username) {
        return players.get(username).nbLiveLeft;
    }

    public int getPlayerGoodGuess(String username) {
        return players.get(username).nbGoodGuesses;
    }

    public String getPlayerCurrentGuesses(String username) {
        return players.get(username).currentGuesses;
    }

    public boolean playerGuess(String username, String guess) {
        PlayerState currentPlayer = players.get(username);

        if (guess.length() <= 1) {
            char charGuess = guess.toUpperCase().charAt(0);

            if (!wordToGuess.contains(guess.toUpperCase())) {
                currentPlayer.nbLiveLeft--;
                return false;
            }

            currentPlayer.nbGoodGuesses++;

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
            return false;
        }
        if(wordToGuess.equals(guess.toUpperCase())){
            currentPlayer.currentGuesses = guess.toUpperCase();
            return true;
        }
        return false;
    }

    public String getUpdate(String username) {
        return players.get(username).nbLiveLeft + " " + players.get(username).nbGoodGuesses + " " + players.get(username).currentGuesses;
    }

    public boolean playerExist(String username) {
        return players.containsKey(username);
    }

    public PlayerState[] getPlayers(){
        return players.values().toArray(new PlayerState[players.size()]);
    }

    public void startGame() {
        wordToGuess = "NEWGAME";

        for (PlayerState player : players.values()) {
            player.reset(wordToGuess.length());
        }
    }
}
