package cpen221.mp3;

import cpen221.mp3.wikimediator.WikiMediator;
import cpen221.mp3.wikimediator.WikiPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class WikiTests {

    public static WikiPage wiki1;
    public static WikiMediator wikiMed1;

    @BeforeAll
    public static void setUpTests(){
        wiki1 = new WikiPage("test1");
        wikiMed1 = new WikiMediator(10,10);
    }



    @Test
    public void windowedPeakLoadTest1() throws InterruptedException {
        int expected = 4;
        int result;
        int timeWindow = 30;

        wikiMed1.getPage("sushi");
        wikiMed1.search("spaghetti", 1);
        wikiMed1.search("poke", 2);

        Thread.sleep(32*1000);

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

        wikiMed1.getPage("sushi");
        wikiMed1.search("spaghetti", 1);
        wikiMed1.search("poke", 2);
        wikiMed1.getPage("sushi");

        Thread.sleep(32*1000);

        wikiMed1.search("spaghetti", 1);
        wikiMed1.search("poke", 2);
        wikiMed1.getPage("Cardi B");


        result = wikiMed1.windowedPeakLoad(30);

        Assertions.assertEquals(expected, result);


    }
    @Test
    public void windowedPeakLoadTest3() throws InterruptedException {
        int expected = 5;
        int result;
        int timeWindow = 30;

        wikiMed1.getPage("sushi");
        wikiMed1.search("spaghetti", 1);
        wikiMed1.search("poke", 2);
        wikiMed1.getPage("sushi");

        Thread.sleep(32*1000);

        wikiMed1.search("spaghetti", 1);
        wikiMed1.search("poke", 2);
        wikiMed1.getPage("Cardi B");
        wikiMed1.search("poke", 2);
        wikiMed1.search("poke", 2);

        Thread.sleep(32*1000);

        wikiMed1.search("spaghetti", 1);
        wikiMed1.search("poke", 2);
        wikiMed1.getPage("Cardi B");


        result = wikiMed1.windowedPeakLoad();

        Assertions.assertEquals(expected, result);


    }

    @Test
    public void testZeigeist1(){
        int limit = 2;
        List<String> results = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        expected.add("poke");
        expected.add("spaghetti");

        wikiMed1.search("poke", 3);
        wikiMed1.search("poke", 3);
        wikiMed1.search("poke", 3);
        wikiMed1.search("poke", 3);


        wikiMed1.search("spaghetti", 1);
        wikiMed1.getPage("spaghetti");
        wikiMed1.getPage("spaghetti");


        wikiMed1.getPage("Cardi B");
        wikiMed1.getPage("Cardi B");


        wikiMed1.getPage("drake");
        wikiMed1.search("halsey", 10);

        results = wikiMed1.zeitgeist(2);

        Assertions.assertLinesMatch(expected, results);
    }

    //TODO make tests for doubles of the same value

    @Test
    public void testTrending1(){
        int limit = 2;
        List<String> results = new ArrayList<>();
        List<String> expected = new ArrayList<>();
        expected.add("poke");
        expected.add("spaghetti");
        expected.add("Cardi B");

        wikiMed1.search("poke", 3);
        wikiMed1.search("poke", 3);
        wikiMed1.search("poke", 3);
        wikiMed1.search("poke", 3);


        wikiMed1.search("spaghetti", 1);
        wikiMed1.getPage("spaghetti");
        wikiMed1.getPage("spaghetti");


        wikiMed1.getPage("Cardi B");
        wikiMed1.getPage("Cardi B");


        wikiMed1.getPage("drake");
        wikiMed1.search("halsey", 10);

        results = wikiMed1.trending(30, 3);

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

        wikiMed1.search("poke", 3);
        wikiMed1.search("poke", 3);
        wikiMed1.search("poke", 3);
        wikiMed1.search("poke", 3);

        Thread.sleep(12*1000);

        wikiMed1.getPage("Cardi B");
        wikiMed1.getPage("Cardi B");
        wikiMed1.getPage("Cardi B");


        wikiMed1.getPage("drake");
        wikiMed1.getPage("drake");

        wikiMed1.search("halsey", 10);

        results = wikiMed1.trending(10, 3);

        Assertions.assertLinesMatch(expected, results);
    }
//  test for peak load with zeigeist
    @Test
    public void windowedPeakLoadTest4() throws InterruptedException {
        int expected = 4;
        int result;
        int timeWindow = 30;

        wikiMed1.getPage("sushi");
        wikiMed1.search("spaghetti", 1);
        wikiMed1.search("poke", 2);

        Thread.sleep(32*1000);

        wikiMed1.getPage("sushi");
        wikiMed1.search("spaghetti", 1);
        wikiMed1.zeitgeist(3);
        wikiMed1.getPage("Cardi B");


        result = wikiMed1.windowedPeakLoad(30);

        Assertions.assertEquals(expected, result);


    }

}
