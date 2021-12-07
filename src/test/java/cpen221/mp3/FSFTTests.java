package cpen221.mp3;

import cpen221.mp3.exceptions.InvalidObjectException;
import cpen221.mp3.fsftbuffer.Buffer;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FSFTTests {

    public static FSFTBuffer<Buffer> buffer1, buffer2, buffer3,
            buffer4, buffer5, buffer6, buffer7;
    public static Buffer b1, b2, b3, b4;

    @BeforeAll
    public static void setUpTests() {
        buffer1 = new FSFTBuffer(2, 20);
        buffer2 = new FSFTBuffer(20, 3);
        buffer3 = new FSFTBuffer(15, 2);
        buffer4 = new FSFTBuffer(5, 2);
        buffer5 = new FSFTBuffer(3, 5);
        buffer6 = new FSFTBuffer(15, 15);
        buffer7 = new FSFTBuffer();
        b1 = new Buffer("a", "aaa");
        b2 = new Buffer("b", "bbb");
        b3 = new Buffer("c", "ccc");
        b4 = new Buffer("d", "ddd");
    }

    @Test
    public void testPutWithException_EvictedObject() {
        Assertions.assertTrue(buffer1.put(b1));
        Assertions.assertTrue(buffer1.put(b2));
        Assertions.assertTrue(buffer1.put(b3));
        Assertions.assertFalse(buffer1.put(null));

        // expected: invalid object, evicted
        try {
            Assertions.assertEquals(b1, buffer1.get("a"));
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMultiplePuts_AccessTimeUnchanged_StaleObject()
            throws InterruptedException {
        buffer3.put(b1);
        buffer3.put(b2);
        buffer3.put(b3);
        Thread.sleep(1 * 1000);

        // repeated put shouldn't change access time
        buffer3.put(b1);
        Thread.sleep(1 * 1000);

        // expected: invalid object, timed out
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
        Thread.sleep(2 * 1000);
        buffer2.touch("a");
        Thread.sleep(2 * 1000);

        // expected: object still in buffer
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
        Thread.sleep(2 * 1000);
        Assertions.assertFalse(buffer2.touch("a"));
        Thread.sleep(2 * 1000);

        // expected: object still in buffer
        try {
            Assertions.assertEquals(b1, buffer2.get("a"));
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testTouch_StaleObject() throws InterruptedException {
        buffer2.put(b1);
        buffer2.put(b2);
        buffer2.put(b3);
        Thread.sleep(3 * 1000);

        // can't touch a stale object
        Assertions.assertFalse(buffer2.update(b1));
    }

    @Test
    public void testTouch_NonStaleObject() throws InterruptedException {
        buffer2.put(b1);
        buffer2.put(b2);
        buffer2.put(b3);
        Thread.sleep(2 * 1000);

        // can reset timeout timer for non-stale object
        Assertions.assertTrue(buffer2.touch("a"));
    }

    @Test
    public void testGet_VaryingAccessAndUsageTimes()
            throws InterruptedException {
        buffer5.put(b1);
        buffer5.put(b2);
        buffer5.put(b3);
        Thread.sleep(1 * 1000);
        buffer5.touch("a");
        Thread.sleep(2 * 1000);
        buffer5.touch("b");
        Thread.sleep(1 * 1000);
        buffer5.touch("c");

        // expected: object still in buffer
        try {
            Assertions.assertEquals(b1, buffer5.get("a"));
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }

        // forces b2 to be evicted since capacity is maxed out
        buffer5.put(b4);
        Assertions.assertFalse(buffer5.touch("b"));

        Thread.sleep(4 * 1000);

        // only b3 and b4 are still in buffer
        Assertions.assertFalse(buffer5.touch("a"));
        Assertions.assertFalse(buffer5.touch("b"));
        Assertions.assertTrue(buffer5.touch("c"));
        Assertions.assertTrue(buffer5.touch("d"));
    }

    @Test
    public void testUpdate_NonStaleObject() {
        buffer6.put(b1);
        buffer6.put(b2);
        buffer6.put(b3);
        buffer6.put(b4);

        Buffer b5 = new Buffer("a", "updated_aaa");
        Assertions.assertTrue(buffer6.update(b5));

        // expected: b5 is the updated version of b1
        try {
            Assertions.assertEquals(b5, buffer6.get("a"));
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOverloadedConstructor() {
        buffer7.put(b1);
        buffer7.put(b2);
        buffer7.put(b3);
        buffer7.put(b4);

        Buffer b5 = new Buffer("a", "updated_aaa");
        Assertions.assertTrue(buffer7.update(b5));

        // expected: b5 is the updated version of b1
        try {
            Assertions.assertEquals(b5, buffer7.get("a"));
            Assertions.assertEquals("updated_aaa",
                    buffer7.get("a").text());
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
    }
}
