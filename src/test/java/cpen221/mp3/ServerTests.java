package cpen221.mp3;

import cpen221.mp3.server.WikiMediatorClient;
import cpen221.mp3.server.WikiMediatorServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

public class ServerTests {
    public static WikiMediatorClient client;

    @BeforeAll
    public static void setupTests() {
        try {
            client = new WikiMediatorClient("127.0.0.1",
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
            client.sendRequest(request);
            String reply = client.getReply();
            //client.close();
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
            client.sendRequest(request);
            String reply = client.getReply();
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
            client.sendRequest(request);
            String reply = client.getReply();
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
            client.sendRequest(request);
            String reply = client.getReply();
            Assertions.assertEquals(true, reply.contains("success"));
        } catch (Exception IOException) {
            throw new AssertionFailedError();
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
            client.sendRequest(request);
            String reply = client.getReply();
            //client.close();
            Assertions.assertEquals(true, reply.contains("success"));
        } catch (Exception IOException) {
            throw new AssertionFailedError();
        }
    }

    @Test
    public void stopTest() {
        String request = "{\"id\":\"ten\",\"type\":\"stop\"}";
        try {
            client.sendRequest(request);
            String reply = client.getReply();
            //client.close();
            Assertions.assertEquals("{\"id\":\"ten\",\"" +
                    "response\":\"bye\"}", reply);
        } catch (Exception IOException) {
            throw new AssertionFailedError();
        }
    }
}
