package cpen221.mp3;

import cpen221.mp3.exceptions.InvalidObjectException;
import cpen221.mp3.fsftbuffer.Buffer;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FSFTTests {

    public static FSFTBuffer<Buffer> buffer1, buffer2, buffer3;
    public static Buffer b1, b2, b3;

    @BeforeAll
    public static void setUpTests() {
        buffer1 = new FSFTBuffer(2, 20);
        buffer2 = new FSFTBuffer(20, 3);
        buffer3 = new FSFTBuffer(15, 2);
        b1 = new Buffer("a");
        b2 = new Buffer("b");
        b3 = new Buffer("c");
    }

    @Test
    public void testPutWithException_EvictedObject() throws InterruptedException {
        Assertions.assertTrue(buffer1.put(b1));
        Assertions.assertTrue(buffer1.put(b2));
        Assertions.assertTrue(buffer1.put(b3));
        Assertions.assertFalse(buffer1.put(null));

        try {
            Assertions.assertEquals(b1, buffer1.get("a"));
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMultiplePuts_AccessTimeUnchanged_StaleObject() throws InterruptedException {
        buffer3.put(b1);
        buffer3.put(b2);
        buffer3.put(b3);
        Thread.sleep(2 * 1000);
        buffer3.put(b1); // shouldn't change access time of b1
        Thread.sleep(1 * 1000);

        try {
            Assertions.assertEquals(b1, buffer3.get("a"));
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetWithException_StaleObject() throws InterruptedException {
        buffer2.put(b1);
        buffer2.put(b2);
        buffer2.put(b3);
        Thread.sleep(3 * 1000);
        buffer2.touch("a");

        Thread.sleep(4 * 1000);

        try {
            Assertions.assertEquals(b1, buffer2.get("a"));
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetSuccess_NonStaleObject() throws InterruptedException {
        buffer2.put(b1);
        buffer2.put(b2);
        buffer2.put(b3);
        Thread.sleep(3 * 1000);
        buffer2.touch("a");

        Thread.sleep(3 * 1000);

        try {
            Assertions.assertEquals(b1, buffer2.get("a"));
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testUpdate() {

    }


    @Test
    public void testTouch() {

    }

    // get rid of later on
    @Test
    public void testUpdateBuffer() {

    }
}
