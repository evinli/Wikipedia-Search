package cpen221.mp3.server;

import java.io.*;
import java.net.Socket;

public class WikiMediatorClient {
    private static final int N = 100;
    private Socket socket;
    private BufferedReader in;
    // Rep invariant: socket, in, out != null
    private PrintWriter out;

    /**
     * Make a WikiMediatorClient and connect it to a server running on
     * hostname at the specified port.
     *
     * @throws IOException if can't connect
     */
    public WikiMediatorClient(String hostname, int port) throws IOException {
        socket = new Socket(hostname, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    /**
     * Test search
     */
    public static void main(String[] args) {
        try {
            WikiMediatorClient client = new WikiMediatorClient("127.0.0.1",
                    WikiMediatorServer.WIKIMEDIATOR_PORT);

            String x = "{ \"id\": \"1\", \"type\": \"search\", \"query\": \"Barack Obama\", \"limit\": \"12\"}";

            client.sendRequest(x);
            System.out.println("search " + x + " = ?");


            String y = client.getReply();
            System.out.println("search " + x + " = " + y);

            client.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Send a request to the server. Requires this is "open".
     *
     * @param x to find Fibonacci(x)
     * @throws IOException if network or server failure
     */
    public void sendRequest(String x) throws IOException {
        out.print(x + "\n");
        out.flush(); // important! make sure x actually gets sent
    }

    /**
     * Get a reply from the next request that was submitted.
     * Requires this is "open".
     *
     * @return the requested Fibonacci number
     * @throws IOException if network or server failure
     */
    public String getReply() throws IOException {
        String reply = in.readLine();
        if (reply == null) {
            throw new IOException("connection terminated unexpectedly");
        }

        return reply;
    }

    /**
     * Closes the client's connection to the server.
     * This client is now "closed". Requires this is "open".
     *
     * @throws IOException if close fails
     */
    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
