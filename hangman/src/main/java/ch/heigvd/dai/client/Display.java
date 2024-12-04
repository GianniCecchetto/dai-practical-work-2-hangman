package ch.heigvd.dai.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Display {
    private String currentWordState,userName,gameList;
    private int livesLeft,roomId;
    private Boolean hasWon = false,gameListDisplayed = false;

    static private Map<String,Map.Entry<Integer,Boolean>> opponentLives = new HashMap<>();
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

    public Display() {}

    public void setCurrentWordState(String currentWordState) {
        this.currentWordState = currentWordState;
    }

    public void setLivesLeft(int livesLeft) {
        this.livesLeft = livesLeft;
    }
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public void setGameList(String gameList) {
        this.gameList = gameList;
    }
    public void setHasWon(Boolean hasWon) {
        this.hasWon = hasWon;
    }

    public void setGameListDisplayed(Boolean gameListDisplayed) {
        this.gameListDisplayed = gameListDisplayed;
    }

    public void setOpponentLives(String userName, int lives, Boolean hasWon) {
        opponentLives.put(userName, Map.entry(lives,hasWon));
    }
    public void clearDisplay(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
    public void displayCmdPrompt(){
        System.out.print("CMD > ");
    }
    void displayGamelist() {
        System.out.println("<===Available Games ===>");
        System.out.println(gameList);

    }

    void waitingForJoin(){
        System.out.println("  " + Client.Message.JOIN + " <name> <game_id> - Join the game with the id sent with a name.");
        System.out.println("  " + Client.Message.LISTGAMES + " - List all accessible games.");

    }

    void updateGameState() {
       /* System.out.printf("\033[s");
        for (int i = 0; i < 24;++i){
            if(i != 0){
                System.out.printf("\033[2K");
            }
            System.out.printf("\033[F");
        }*/

        if(gameListDisplayed)
            displayGamelist();
        System.out.println("\n=====HANGMAN=====");
        System.out.println("--------");
        System.out.println("Current room: " + roomId);
        System.out.println("Current username: " + userName);
        System.out.println("--------\n\n");

        System.out.println("Opponent:");
        System.out.println(swords);

        for (Map.Entry<String,Map.Entry<Integer,Boolean>> entry : opponentLives.entrySet()) {
            if(entry.getValue().getValue()) {
                System.out.println(" - " + entry.getKey() + ": " + "Has found the word");
            }else if(entry.getValue().getKey() == 0){
                System.out.println(" - " + entry.getKey() + ": " + "Is dead");
            }else{
                System.out.println(" - " + entry.getKey() + ": " + entry.getValue().getKey() + " lives");
            }
        }
        System.out.println(swords);

        if(hasWon){
            System.out.println("You found the word");
        }

        System.out.println(hangmanState[6-livesLeft]);
        if(livesLeft == 0){
            System.out.println("You died :(");
        }else{
            System.out.println("Lives: " + livesLeft);
        }
        System.out.println("PROGRESS : " + currentWordState);

        //System.out.printf("\033[u");
    }
}
