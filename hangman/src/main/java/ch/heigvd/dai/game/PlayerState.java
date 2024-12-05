package ch.heigvd.dai.game;

import java.io.BufferedWriter;

public class PlayerState {
    static final int MAX_LIVES = 6;

    int nbLiveLeft;
    int nbGoodGuesses;
    String currentGuesses;
    public BufferedWriter out;
    int roomId;
    Boolean hasWon = false;

    public PlayerState(){
        nbLiveLeft = MAX_LIVES;
        nbGoodGuesses = 0;
        currentGuesses = "";
    }
    public void sethasWon(Boolean hasWon) {
        this.hasWon = hasWon;
    }
    public Boolean gethasWon() {
        return hasWon;
    }

    public int getLives() {
        return nbLiveLeft;
    }
    public int getRoomId() {
        return roomId;
    }

    public PlayerState(int wordLength,int roomId,BufferedWriter out){
        this.roomId = roomId;
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
