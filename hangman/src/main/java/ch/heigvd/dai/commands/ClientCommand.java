package ch.heigvd.dai.commands;
import ch.heigvd.dai.client.Client;

import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "client", description = "Start the client for HANGMAN G@MING.")
public class ClientCommand implements Callable<Integer> {
    public enum Message {
        GUESS,
        RESTART,
        HELP,
        QUIT,
    }

    // End of line character
    public static String END_OF_LINE = "\n";

    @CommandLine.Option(
            names = {"-H", "--host"},
            description = "Host to connect to.",
            required = true)
    private String host;

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "Port to use (default: ${DEFAULT-VALUE}).",
            defaultValue = "1902")
    protected int port;

    @Override
    public Integer call() {
        Client client = new Client(host, port);
        client.run();

        return 0;
    }

    private static void help() {
        System.out.println("Usage:");
        System.out.println("  " + Message.GUESS + " <number> - Submit the number you want to guess.");
        System.out.println("  " + Message.RESTART + " - Restart the game.");
        System.out.println("  " + Message.QUIT + " - Close the connection to the server.");
        System.out.println("  " + Message.HELP + " - Display this help message.");
    }
}