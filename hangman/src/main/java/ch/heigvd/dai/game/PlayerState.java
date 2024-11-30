package ch.heigvd.dai.game;

import java.io.BufferedWriter;

public class PlayerState {
    static final int MAX_LIVES = 10;

    int nbLiveLeft;
    int nbGoodGuesses;
    String currentGuesses;
    public BufferedWriter out;

    public PlayerState(){
        nbLiveLeft = MAX_LIVES;
        nbGoodGuesses = 0;
        currentGuesses = "";
    }

    public int getLives() {
        return nbLiveLeft;
    }

    public PlayerState(int wordLength, BufferedWriter out){
        nbLiveLeft = MAX_LIVES;
        currentGuesses = "_".repeat(wordLength);
        this.out = out;
    }

    public void reset(int wordLength){
        currentGuesses = "_".repeat(wordLength);
        nbLiveLeft = PlayerState.MAX_LIVES;
        nbGoodGuesses = 0;
    }
}
