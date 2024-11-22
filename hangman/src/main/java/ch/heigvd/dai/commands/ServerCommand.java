package ch.heigvd.dai.commands;

import ch.heigvd.dai.server.Server;
import picocli.CommandLine;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "server", description = "Start the server for HANGMAN G@MING.")
public class ServerCommand implements Callable<Integer> {
    public enum Message {
        HIGHER,
        LOWER,
        CORRECT,
        OK,
        ERROR,
    }

    // End of line character
    public static String END_OF_LINE = "\n";

    @CommandLine.Option(
            names = {"-p", "--port"},
            description = "Port to use (default: ${DEFAULT-VALUE}).",
            defaultValue = "1902")
    protected int port;

    @Override
    public Integer call() {
        Server server = new Server(port);
        server.run();

        return 0;
    }
}