package cpen221.mp3;

import cpen221.mp3.wikimediator.WikiMediator;
import cpen221.mp3.wikimediator.WikiPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class WikiTests {

    public static WikiPage wiki1;
    public static WikiMediator wikiMed1;
    public static WikiMediator wikiMed2;
    public static WikiMediator wikiMed3;
    public static WikiMediator wikiMed4;
    public static WikiMediator wikiMed5;
    public static WikiMediator wikiMed6;
    public static WikiMediator wikiMed7;
    public static WikiMediator wikiMed8;
    public static WikiMediator wikiMed9;
    public static WikiMediator wikiMed10;
    public static WikiMediator wikiMedThreadSafe;

    @BeforeAll
    public static void setUpTests() {
        wiki1 = new WikiPage("test1");
        wikiMed1 = new WikiMediator(20, 10);
        wikiMed2 = new WikiMediator(20, 10);
        wikiMed3 = new WikiMediator(20, 10);
        wikiMed4 = new WikiMediator(20, 10);
        wikiMed5 = new WikiMediator(20, 10);
        wikiMed6 = new WikiMediator(20, 10);
        wikiMed7 = new WikiMediator(20, 10);
        wikiMed8 = new WikiMediator(20, 10);
        wikiMed9 = new WikiMediator(20, 10);
        wikiMed10 = new WikiMediator(20, 10);
        wikiMedThreadSafe = new WikiMediator(3, 5);
    }

    @Test
    public void testGetPageSearch() {
        System.out.println(wikiMed9.getPage("Doja Cat"));
        System.out.println(wikiMed9.search("Doja Cat", 3));
    }

    @Test
    public void windowedPeakLoadTest1() throws InterruptedException {
        int expected = 5;
        int result;
        int timeWindow = 30;

        wikiMed1.getPage("sushi");
        wikiMed1.search("spaghetti", 1);
        wikiMed1.search("poke", 2);

        Thread.sleep(32 * 1000);

        wikiMed1.getPage("sushi");
        wikiMed1.search("spaghetti", 1);
        wikiMed1.search("poke", 2);
        wikiMed1.getPage("Cardi B");

        result = wikiMed1.windowedPeakLoad(30);
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void windowedPeakLoadTest2() throws InterruptedException {
        int expected = 4;
        int result;
        int timeWindow = 30;

        wikiMed2.getPage("sushi");
        wikiMed2.search("spaghetti", 1);
        wikiMed2.search("poke", 2);
        wikiMed2.getPage("sushi");

        Thread.sleep(32 * 1000);

        wikiMed2.search("spaghetti", 1);
        wikiMed2.search("poke", 2);
        wikiMed2.getPage("Cardi B");

        result = wikiMed2.windowedPeakLoad(30);
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void windowedPeakLoadTest3() throws InterruptedException {
        int expected = 5;
        int result;
        int timeWindow = 30;

        wikiMed3.getPage("sushi");
        wikiMed3.search("spaghetti", 1);
        wikiMed3.search("poke", 2);
        wikiMed3.getPage("sushi");

        Thread.sleep(32 * 1000);

        wikiMed3.search("spaghetti", 1);
        wikiMed3.search("poke", 2);
        wikiMed3.getPage("Cardi B");
        wikiMed3.search("poke", 2);
        wikiMed3.search("poke", 2);

        Thread.sleep(32 * 1000);

        wikiMed3.search("spaghetti", 1);
        wikiMed3.search("poke", 2);
        wikiMed3.getPage("Cardi B");

        result = wikiMed3.windowedPeakLoad();
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void testZeigeist1() {
        int limit = 2;
        List<String> results = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        expected.add("poke");
        expected.add("spaghetti");

        wikiMed5.search("poke", 3);
        wikiMed5.search("poke", 3);
        wikiMed5.search("poke", 3);
        wikiMed5.search("poke", 3);

        wikiMed5.search("spaghetti", 1);
        wikiMed5.getPage("spaghetti");
        wikiMed5.getPage("spaghetti");

        wikiMed5.getPage("Cardi B");
        wikiMed5.getPage("Cardi B");

        wikiMed5.getPage("drake");
        wikiMed5.search("halsey", 10);

        results = wikiMed5.zeitgeist(2);
        Assertions.assertLinesMatch(expected, results);
    }

    @Test
    public void testTrending1() {
        List<String> results = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        expected.add("poke");
        expected.add("spaghetti");
        expected.add("Cardi B");

        wikiMed6.search("poke", 3);
        wikiMed6.search("poke", 3);
        wikiMed6.search("poke", 3);
        wikiMed6.search("poke", 3);

        wikiMed6.search("spaghetti", 1);
        wikiMed6.getPage("spaghetti");
        wikiMed6.getPage("spaghetti");

        wikiMed6.getPage("Cardi B");
        wikiMed6.getPage("Cardi B");

        wikiMed6.getPage("drake");
        wikiMed6.search("halsey", 10);

        results = wikiMed6.trending(30, 3);
        Assertions.assertLinesMatch(expected, results);
    }

    //test trending for the last 10 seconds, which does not include some items
    @Test
    public void testTrending2() throws InterruptedException {
        int limit = 2;
        List<String> results = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        expected.add("Cardi B");
        expected.add("drake");
        expected.add("halsey");

        wikiMed7.search("poke", 3);
        wikiMed7.search("poke", 3);
        wikiMed7.search("poke", 3);
        wikiMed7.search("poke", 3);

        Thread.sleep(12 * 1000);

        wikiMed7.getPage("Cardi B");
        wikiMed7.getPage("Cardi B");
        wikiMed7.getPage("Cardi B");


        wikiMed7.getPage("drake");
        wikiMed7.getPage("drake");

        wikiMed7.search("halsey", 10);

        results = wikiMed7.trending(10, 3);
        Assertions.assertLinesMatch(expected, results);
    }

    // test trending for the last 3 seconds where no methods were called,
    // should return empty arraylist
    @Test
    public void testTrendingEmpty() throws InterruptedException {
        int limit = 10;
        List<String> expected = new ArrayList<>();
        List<String> results = new ArrayList<>();

        wikiMed8.getPage("pho");
        wikiMed8.search("curry", 7);

        Thread.sleep(5 * 1000);

        results = wikiMed8.trending(3, limit);

        Assertions.assertLinesMatch(expected, results);
    }

    //assert that calling windowed peak load alone will return 1
    @Test
    public void windowedPeakLoadTest5() throws InterruptedException {
        int expected = 1;
        int result;
        int timeWindow = 30;

        result = wikiMed10.windowedPeakLoad(timeWindow);
        Assertions.assertEquals(expected, result);
    }

    @Test
    public void wikiThreadSafe() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(4);
        int result;

        Thread t1 = new Thread(() -> {
            wikiMedThreadSafe.getPage("market");
            latch.countDown();
        });

        Thread t2 = new Thread(() -> {
            wikiMedThreadSafe.getPage("math");
            latch.countDown();
        });

        Thread t3 = new Thread(() -> {
            wikiMedThreadSafe.getPage("physics");
            latch.countDown();
        });

        Thread t4 = new Thread(() -> {
            wikiMedThreadSafe.getPage("BBL");
            latch.countDown();
        });

        t1.start();
        t2.start();
        t3.start();
        t4.start();

        latch.await();
        result = wikiMedThreadSafe.windowedPeakLoad();

        Assertions.assertEquals(5, result);
    }

    @Test
    public void wikiThreadSafe1() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(4);
        List<String> expectedList = new ArrayList<>();
        int result;

        expectedList.add("math");
        expectedList.add("market");

        Thread t1 = new Thread(() -> {
            wikiMedThreadSafe.getPage("market");
            latch.countDown();
        });

        Thread t2 = new Thread(() -> {
            wikiMedThreadSafe.getPage("math");
            latch.countDown();
        });

        Thread t3 = new Thread(() -> {
            wikiMedThreadSafe.getPage("math");
            latch.countDown();
        });

        Thread t4 = new Thread(() -> {
            wikiMedThreadSafe.getPage("math");
            latch.countDown();
        });
        Thread t5 = new Thread(() -> {
            wikiMedThreadSafe.search("math", 2);
            latch.countDown();
        });

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();

        latch.await();
        result = wikiMedThreadSafe.windowedPeakLoad();

        Assertions.assertEquals(6, result);
        Assertions.assertLinesMatch(expectedList, wikiMedThreadSafe.
                zeitgeist(2));
    }
}
