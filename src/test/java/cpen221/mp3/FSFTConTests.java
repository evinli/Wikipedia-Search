package cpen221.mp3;

import cpen221.mp3.exceptions.InvalidObjectException;
import cpen221.mp3.fsftbuffer.Buffer;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

public class FSFTConTests {

    public static FSFTBuffer<Buffer> buffer1;
    public static Buffer b1, b2, b3;

    @BeforeAll
    public static void setUpTests() {
        buffer1 = new FSFTBuffer(3, 3);
        b1 = new Buffer("a", "aaa");
        b2 = new Buffer("b", "bbb");
        b3 = new Buffer("c", "ccc");
    }

    @Test
    public void testMultithreadingPuts_NotStale() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);

        Thread t1 = new Thread(() -> {
            buffer1.put(b1);
            latch.countDown();
        });
        Thread t2 = new Thread(() -> {
            buffer1.put(b2);
            latch.countDown();
        });
        Thread t3 = new Thread(() -> {
            buffer1.put(b3);
            latch.countDown();
        });

        t1.start();
        t2.start();
        t3.start();

        latch.await();

        Assertions.assertTrue(buffer1.touch("a"));
        Assertions.assertTrue(buffer1.touch("b"));
        Assertions.assertTrue(buffer1.touch("c"));

        Assertions.assertTrue(buffer1.update(b1));
        Assertions.assertTrue(buffer1.update(b2));
        Assertions.assertTrue(buffer1.update(b3));

        Thread.sleep(2 * 1000);

        try {
            Assertions.assertEquals(b1, buffer1.get("a"));
            Assertions.assertEquals(b2, buffer1.get("b"));
            Assertions.assertEquals(b3, buffer1.get("c"));
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMultithreadingUpdates_Stale() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(3);
        Buffer b5 = new Buffer("a", "updatedaaa");
        Buffer b6 = new Buffer("b", "updatedbbb");
        Buffer b7 = new Buffer("c", "updatedccc");

        buffer1.put(b1);
        buffer1.put(b2);
        buffer1.put(b3);

        Thread t1 = new Thread(() -> {
            buffer1.update(b5);
            latch.countDown();
        });
        Thread t2 = new Thread(() -> {
            buffer1.update(b6);
            latch.countDown();
        });
        Thread t3 = new Thread(() -> {
            buffer1.update(b7);
            latch.countDown();
        });

        t1.start();
        t2.start();
        t3.start();

        latch.await();

        Assertions.assertTrue(buffer1.touch("a"));
        Assertions.assertTrue(buffer1.touch("b"));
        Assertions.assertTrue(buffer1.touch("c"));

        Thread.sleep(3 * 1000);

        try {
            Assertions.assertEquals(b5, buffer1.get("a"));
            Assertions.assertEquals(b6, buffer1.get("b"));
            Assertions.assertEquals(b7, buffer1.get("c"));
        } catch (InvalidObjectException e) {
            e.printStackTrace();
        }
    }
}
