package ch.heigvd.dai.client;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Client {
    private String host;
    private int port;

    private static final String TEXTUAL_DATA = "hangman client";

    public static String END_OF_LINE = "\n";

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {
        System.out.println("[Client] Connecting to " + host + ":" + port + "...");

        try (Socket socket = new Socket(host, port);
             Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
             BufferedReader in = new BufferedReader(reader);
             Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
             BufferedWriter out = new BufferedWriter(writer); ) {
            System.out.println("[Client] Connected to " + host + ":" + port);
            System.out.println(
                    "[Client] Sending textual data to server " + host + ":" + port + ": " + TEXTUAL_DATA);

            out.write(TEXTUAL_DATA + END_OF_LINE);
            out.flush();

            System.out.println("[Client] Received textual data from server: " + in.readLine());
            System.out.println("[Client] Closing connection...");
        } catch (IOException e) {
            System.out.println("[Client] IO exception: " + e);
        }
    }
}
