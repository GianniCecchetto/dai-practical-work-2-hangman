package ch.heigvd.dai.game;

public class PlayerState {
    static final int MAX_LIVES = 10;

    int nbLiveLeft;
    int nbGoodGuesses;
    String currentGuesses;

    public PlayerState(){
        nbLiveLeft = MAX_LIVES;
        nbGoodGuesses = 0;
        currentGuesses = "";
    }

    public PlayerState(int wordLength){
        currentGuesses = "_".repeat(wordLength);
    }

    public void reset(int wordLength){
        currentGuesses = "_".repeat(wordLength);
        nbLiveLeft = PlayerState.MAX_LIVES;
        nbGoodGuesses = 0;
    }
}
