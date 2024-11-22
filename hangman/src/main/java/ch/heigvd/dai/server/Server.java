package ch.heigvd.dai.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server
{
    private int port;

    private static final String TEXTUAL_DATA = "Test hangman server";

    public static String END_OF_LINE = "\n";

    public Server(int port){
        this.port = port;
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("[Server] Listening on port " + port);

            while (true) {
                try (Socket socket = serverSocket.accept();
                     Reader reader = new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8);
                     BufferedReader in = new BufferedReader(reader);
                     Writer writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8);
                     BufferedWriter out = new BufferedWriter(writer)) {

                    System.out.println(
                            "[Server] New client connected from "
                                    + socket.getInetAddress().getHostAddress()
                                    + ":"
                                    + socket.getPort());

                    System.out.println("[Server] Received textual data from client: " + in.readLine());

                    System.out.println("[Server] Sending textual data to client: " + TEXTUAL_DATA);

                    out.write(TEXTUAL_DATA + END_OF_LINE);
                    out.flush();

                    System.out.println("[Server] Closing connection");

                } catch (IOException e) {
                    System.out.println("[Server] IO exception: " + e);
                }
            }
        } catch (IOException e) {
            System.out.println("[Server] IO exception: " + e);
        }
    }


}
