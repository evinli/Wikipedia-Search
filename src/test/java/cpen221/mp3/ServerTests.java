package cpen221.mp3;

import cpen221.mp3.server.WikiMediatorClient;
import cpen221.mp3.server.WikiMediatorServer;
import cpen221.mp3.wikimediator.WikiMediator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServerTests {
    public static WikiMediatorClient client1;
    public static WikiMediatorClient client2;
    public static WikiMediatorClient client3;
    public static WikiMediatorClient client4;
    public static WikiMediatorClient client5;


    @BeforeAll
    public static void setupTests() {
        try {
            WikiMediatorServer server = new WikiMediatorServer
                    (WikiMediatorServer.WIKIMEDIATOR_PORT, 4,
                            new WikiMediator(10, 30));
            Thread serverThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        server.serve();
                    } catch (IOException e) {
                        throw new RuntimeException("Server could not be initialized.");
                    }
                }
            });
            serverThread.start();

            client1 = new WikiMediatorClient("127.0.0.1",
                    WikiMediatorServer.WIKIMEDIATOR_PORT);
            client2 = new WikiMediatorClient("127.0.0.1",
                    WikiMediatorServer.WIKIMEDIATOR_PORT);
            client3 = new WikiMediatorClient("127.0.0.1",
                    WikiMediatorServer.WIKIMEDIATOR_PORT);
            client4 = new WikiMediatorClient("127.0.0.1",
                    WikiMediatorServer.WIKIMEDIATOR_PORT);
            client5 = new WikiMediatorClient("127.0.0.1",
                    WikiMediatorServer.WIKIMEDIATOR_PORT);
        } catch (Exception IOException) {
            throw new AssertionFailedError();
        }
    }

    @Test
    public void searchTest() {
        String request = "{\"id\":\"1\",\"type\":\"search\",\"query\":\"" +
                "Barack Obama\",\"limit\":\"12\",\"timeout\":\"1\"}";
        try {
            client1.sendRequest(request);
            String reply = client1.getReply();
            Assertions.assertEquals(true, reply.contains("Obama"));
            //Assertions.assertNotNull(reply);
        } catch (Exception IOException) {
            throw new AssertionFailedError();
        }
    }

    @Test
    public void searchTestTimeout1() {
        String request = "{\"id\":\"1\",\"type\":\"search\",\"query\":\"" +
                "Earth\",\"limit\":\"1000\",\"timeout\":\"2\"}";
        try {
            client1.sendRequest(request);
            String reply = client1.getReply();
            Assertions.assertEquals(true, reply.contains("failed"));
        } catch (Exception IOException) {
            throw new AssertionFailedError();
        }
    }

    @Test
    public void searchTestTimeout2() {
        String request = "{\"id\":\"1\",\"type\":\"search\",\"query\":\"" +
                "Earth\",\"limit\":\"10\",\"timeout\":\"2\"}";
        try {
            client1.sendRequest(request);
            String reply = client1.getReply();
            Assertions.assertEquals(true, reply.contains("success"));
        } catch (Exception IOException) {
            throw new AssertionFailedError();
        }
    }

    @Test
    public void searchTestTimeout3() {
        String request = "{\"id\":\"1\",\"type\":\"search\",\"query\":\"" +
                "Earth\",\"limit\":\"1000\",\"timeout\":\"10\"}";
        try {
            client1.sendRequest(request);
            String reply = client1.getReply();
            Assertions.assertEquals(true, reply.contains("success"));
        } catch (Exception IOException) {
            throw new AssertionFailedError();
        }
    }

    @Test
    public void getPageTest() {
        String request = "{" +
                "\t\"id\": \"6\"," +
                "\t\"type\": \"getPage\"," +
                "\t\"pageTitle\": \"Markus Thormeyer\"" +
                "}";
        try {
            client1.sendRequest(request);
            String reply = client1.getReply();
            Assertions.assertEquals(true, reply.contains("Markus Thormeyer"));
        } catch (Exception IOException) {
            throw new AssertionFailedError();
        }
    }

    @Test
    public void getPageTestCon() {
        String request = "{" +
                "\t\"id\": \"6\"," +
                "\t\"type\": \"getPage\"," +
                "\t\"pageTitle\": \"Markus Thormeyer\"" +
                "}";
        String reply;
        for (int i = 0; i < 50; i++) {
            try {
                client1.sendRequest(request);
            } catch (Exception IOException) {
                throw new AssertionFailedError();
            }
        }
        for (int i = 0; i < 50; i++) {
            try {
                reply = (client1.getReply());
                Assertions.assertEquals(true, reply.contains("Markus Thormeyer"));
            } catch (Exception IOException) {
                throw new AssertionFailedError();
            }
        }
    }

    @Test
    public void zeitgeistTest() {
        String request = "{" +
                "\t\"id\": \"two\"," +
                "\t\"type\": \"zeitgeist\"," +
                "\t\"limit\": \"5\"" +
                "}";
        try {
            client1.sendRequest(request);
            String reply = client1.getReply();
            Assertions.assertEquals(true, reply.contains("success"));
        } catch (Exception IOException) {
            throw new AssertionFailedError();
        }
    }

    @Test
    public void stopTest() {
        String request = "{\"id\":\"ten\",\"type\":\"stop\"}";
        try {
            client1.sendRequest(request);
            String reply = client1.getReply();
            Assertions.assertEquals("{\"id\":\"ten\",\"" +
                    "response\":\"bye\"}", reply);
        } catch (Exception IOException) {
            throw new AssertionFailedError();
        }
    }
}
