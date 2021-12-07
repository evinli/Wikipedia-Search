package cpen221.mp3.server;

import com.google.gson.Gson;
import cpen221.mp3.wikimediator.WikiMediator;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class WikiMediatorServer {
    WikiMediator wikiMediator;
    public static final int WIKIMEDIATOR_PORT = 9000;
    public static final int MAX_CLIENTS = 32;
    private int num_clients = 0;
    private ServerSocket serverSocket;

    /**
     * Start a server at a given port number, with the ability to process
     * up to n requests concurrently.
     *
     * @param port the port number to bind the server to, 9000 <= {@code port}
     *             <= 9999
     * @param n the number of concurrent requests the server can handle, 0 <
     *          {@code n} <= 32
     * @param wikiMediator the WikiMediator instance to use for the server,
     *                     {@code wikiMediator} is not {@code null}
     */
    public WikiMediatorServer(int port, int n, WikiMediator wikiMediator) {
        this.wikiMediator = wikiMediator;
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            throw new RuntimeException();
        }

    }

    /**
     * Run the server, listening for connections and handling them.
     *
     * @throws IOException if the main server socket is broken
     */
    public void serve() throws IOException {
        while (true) {
            final Socket socket = serverSocket.accept();
            num_clients++;

            Thread handler = new Thread(new Runnable() {
                public void run() {
                    try {
                        try {
                            handle(socket);
                        } finally {
                            socket.close();
                            if (num_clients > 0) num_clients--;
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            });
            handler.start();
        }
    }

    /**
     * Handle one client connection. Returns when client disconnects.
     *
     * @param socket socket where client is connected
     * @throws IOException if connection encounters an error
     */
    private void handle(Socket socket) throws IOException {
        System.err.println("client connected");
        Gson gson = new Gson();

        BufferedReader in = new BufferedReader(new InputStreamReader(
                socket.getInputStream()));

        PrintWriter out = new PrintWriter(new OutputStreamWriter(
                socket.getOutputStream()), true);

        try {
            for (String line = in.readLine(); line != null; line = in
                    .readLine()) {
                WikiMediatorRequest request = gson.fromJson(line,
                        WikiMediatorRequest.class);
                String response = request.handle(wikiMediator);
                out.println(response);
                if (response.contains("\"response\":\"bye\"")) {
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException();
        } finally {
            out.close();
            in.close();
        }
    }

    /**
     * Start a WikiMediatorServer running on the default port.
     */
    public static void main(String[] args) {
        try {
            WikiMediatorServer server = new WikiMediatorServer
                    (WIKIMEDIATOR_PORT, MAX_CLIENTS,
                            new WikiMediator(10, 30));
            server.serve();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
