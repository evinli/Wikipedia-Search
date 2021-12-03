package cpen221.mp3;

import cpen221.mp3.wikimediator.WikiPage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

public class Tests {

    @Test
    public void testRun() {
        HashMap<Integer, Integer> test1 = new HashMap<>();
        HashMap<Integer, Integer> test2 = new HashMap<>();
        int capacity = 1;

        test1.put(0, 1);
        test2.put(0, 1);

        test1.put(1, 2);
        test2.put(1, 2);

        test1.put(2, 3);
        test2.put(2, 3);

        test1.put(3, 4);
        test2.put(3, 4);

        test1.put(5, 6);
        test2.put(5, 6);

        test1.entrySet().removeIf(e -> e.getValue() == 2);
        test2.entrySet().removeIf(e -> !test1.containsKey(e.getKey()));

        System.out.println(test1);
        System.out.println(test2);

        if (test1.size() >= capacity) {
            int id = test1.entrySet().stream().min((e1, e2) -> e1.getValue() > e2.getValue() ? 1: -1).get().getKey();
            test1.remove(id);
            test2.remove(id);
        }

        Assertions.assertEquals(true,true);
    }
}
