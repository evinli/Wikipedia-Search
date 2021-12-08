package cpen221.mp3.server;

import java.io.*;
import java.net.Socket;

public class WikiMediatorClient {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Abstraction function:
     *      1
     *      2
     */

    /**
     * Rep invariant:
     *      socket, in, out != null
     */

    /**
     * Thread-safety argument:
     *      1
     *      2
     */

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
     * Send a request to the server. Requires this is "open".
     *
     * @param request query for WikiMediator, must be in JSON string formatted
     *                to the spec of WikiMediatorRequest
     * @throws IOException if network or server failure
     */
    public void sendRequest(String request) throws IOException {
        out.print(request + "\n");
        out.flush();
    }

    /**
     * Get a reply from the next request that was submitted.
     * Requires this is "open".
     *
     * @return the response from WikiMediator
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
