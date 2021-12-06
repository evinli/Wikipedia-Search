package cpen221.mp3.server;

import cpen221.mp3.wikimediator.WikiMediator;

import java.io.*;
import java.lang.reflect.InaccessibleObjectException;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.gson.Gson;

public class WikiMediatorServer {
    WikiMediator wikiMediator;
    /** Default port number where the server listens for connections. */
    public static final int WIKIMEDIATOR_PORT = 4949;
    public static final int MAX_CLIENTS = 32;

    private int num_clients = 0;

    private ServerSocket serverSocket;

    /**
     * Start a server at a given port number, with the ability to process
     * up to n requests concurrently.
     *
     * @param port the port number to bind the server to, 9000 <= {@code port} <= 9999
     * @param n the number of concurrent requests the server can handle, 0 < {@code n} <= 32
     * @param wikiMediator the WikiMediator instance to use for the server, {@code wikiMediator} is not {@code null}
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
         * Make a FibonacciServerMulti that listens for connections on port.
         *
         * @param port
         *            port number, requires 0 <= port <= 65535
         */

        /**
         * Run the server, listening for connections and handling them.
         *
         * @throws IOException
         *             if the main server socket is broken
         */
        public void serve() throws IOException {
            while (true) {
                // block until a client connects
                final Socket socket = serverSocket.accept();
                num_clients++;
                // create a new thread to handle that client
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
                            // this exception wouldn't terminate serve(),
                            // since we're now on a different thread, but
                            // we still need to handle it
                            ioe.printStackTrace();
                        }
                    }
                });
                handler.start(); //start the thread
            }
        }

        /**
         * Handle one client connection. Returns when client disconnects.
         *
         * @param socket
         *            socket where client is connected
         * @throws IOException
         *             if connection encounters an error
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
                    WikiMediatorRequest request = gson.fromJson(line, WikiMediatorRequest.class);
                    String response = request.handle(wikiMediator);
                    out.println(response);
                    if (response.contains("bye")) {
                        System.exit(0);
                    }
                    // important! our PrintWriter is auto-flushing, but if it were
                    // not:
                    // out.flush();*/
                }
            } catch (IOException e) {
                throw new RuntimeException();
            } catch (InaccessibleObjectException e) {
                throw new RuntimeException("Could not parse JSON");
            }finally {
                out.close();
                in.close();
            }
        }

        /**
         * Start a WikiMediatorServer running on the default port.
         */
        public static void main(String[] args) {
            try {
                WikiMediatorServer server = new WikiMediatorServer(WIKIMEDIATOR_PORT, MAX_CLIENTS, new WikiMediator(10, 30));
                server.serve();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
}
