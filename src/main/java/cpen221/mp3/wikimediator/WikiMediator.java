package cpen221.mp3.wikimediator;

import cpen221.mp3.exceptions.InvalidObjectException;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



public class WikiMediator {
    Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();
    FSFTBuffer cache;
    HashMap<String, ArrayList> pageSearches; //stores timestamps for search and get queries
    ArrayList methodsCalls; // stores tiemstamps for all method calls
    Integer StartTime;



    public WikiMediator(int capacity, int stalenessInterval){
        cache = new FSFTBuffer(capacity, stalenessInterval);
        StartTime = (int)(System.currentTimeMillis() / 1000L);
        pageSearches = new HashMap<String, ArrayList>();
        methodsCalls = new ArrayList<Integer>();
    }


    /**
     * Given a query, return up to limit page titles that match the query string
     * (per Wikipedia's search service).
     * @param query
     * @param limit
     * @return
     */
    public List<String> search(String query, int limit){
        List<String> matches = new ArrayList<>();

            matches = wiki.search(query, limit);

            //adds the timestamp of the request to the hashmap pageSearches
        synchronized (this) {
            if (pageSearches.containsKey(query)) {
                pageSearches.get(query).add((int) (System.currentTimeMillis() / 1000L - StartTime));
                methodsCalls.add((int) (System.currentTimeMillis() / 1000L - StartTime));
            } else {
                pageSearches.put(query, new ArrayList());
                pageSearches.get(query).add((int) (System.currentTimeMillis() / 1000L - StartTime));
                methodsCalls.add((int) (System.currentTimeMillis() / 1000L - StartTime));
            }
        }

        return matches;
    }

    /**
     * Given a pageTitle, return the text associated with the Wikipedia page that matches pageTitle.
     * @param pageTitle
     * @return
     */
    public synchronized String getPage(String pageTitle){
        String text = new String();
        WikiPage page = new WikiPage(pageTitle);

        try{
            text = cache.get(pageTitle).toString();

        } catch (InvalidObjectException e) {
            e.printStackTrace();
            text = wiki.getPageText(pageTitle);
            cache.put(page);
        }

        if(pageSearches.containsKey(pageTitle)){
            pageSearches.get(pageTitle).add((int)(System.currentTimeMillis() / 1000L - StartTime));
            methodsCalls.add((int)(System.currentTimeMillis() / 1000L - StartTime));
        }
        else{
            pageSearches.put(pageTitle, new ArrayList());
            pageSearches.get(pageTitle).add((int)(System.currentTimeMillis() / 1000L - StartTime));
            methodsCalls.add((int)(System.currentTimeMillis() / 1000L - StartTime));
        }

        return text;
    }

    /**
     * Most common strings used in search and getpage requests,
     * with items being sorted in non-increasing count order.
     * Return only limit items
     *
     * @param limit
     * @return
     */
    public List<String> zeitgeist(int limit){
        List<String> mostCommon= new ArrayList<>();
        HashMap<String, Integer> reduced = new HashMap<String, Integer>();

        synchronized (this) {
            for (String key : pageSearches.keySet()) {
                int size = pageSearches.get(key).size();

                reduced.put(key, size);
            }
            methodsCalls.add((int) (System.currentTimeMillis() / 1000L - StartTime));
        }

        mostCommon = reduced.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(limit).map(e -> e.getKey())
                .collect(Collectors.toList());



        return mostCommon;
    }


    /**
     * returns list of most frequent requests made in last timelimitinseconds seconds,
     * items are sorted in non-increasing count order. report at most maxItems of the most
     * frequent requests
     *
     * @param timeLimitInSeconds
     * @param maxItems
     * @return
     */
    public List<String> trending(int timeLimitInSeconds, int maxItems){
        List<String> mostFrequent = new ArrayList<>();
        HashMap<String, Integer> reduced = new HashMap<String, Integer>();
        //current time = the time right now with respect to the time the object was created
        int currentTime = (int)(System.currentTimeMillis() / 1000L - StartTime);

        synchronized (this) {
            for (String key : pageSearches.keySet()) {
                int count = 0;
                //threshold time = count trending items between threshold time to current time
                int ThresholdTime = currentTime - timeLimitInSeconds;


                for (int i = pageSearches.get(key).size() - 1; i >= 0; i--) {
                    int nextTime = (int) pageSearches.get(key).get(i);
                    if (nextTime < ThresholdTime) {
                        break;
                    }
                    count++;
                }

                reduced.put(key, count);
            }

            methodsCalls.add((int) (System.currentTimeMillis() / 1000L - StartTime));
        }

        mostFrequent = reduced.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(maxItems).map(e -> e.getKey())
                .collect(Collectors.toList());


        return mostFrequent;
    }

    /**
     * returns maximum number of requests seen at anytime window of a given length,
     * request count includes all request made using WikiMediator. Counts all 5 methods listed as basic page
     * requests
     * @param timeWindowInSeconds
     * @return
     */
    public int windowedPeakLoad(int timeWindowInSeconds){
        int maxRequest = 0;
        int tempMax = 0;

        synchronized (this) {
            for (int i = 0; i < methodsCalls.size(); i++) {
                for (int j = i; j < methodsCalls.size(); j++) {
                    int valueJ = (int) methodsCalls.get(j);
                    int valueI = (int) methodsCalls.get(i);

                    if ((valueJ - valueI) > timeWindowInSeconds) {
                        if (((j) - i) > tempMax) {
                            tempMax = ((j) - i);
                        }
                        break;
                    } else if (j == methodsCalls.size() - 1) {
                        if (((j + 1) - i) > tempMax) {
                            tempMax = (j + 1) - i;
                        }
                        break;
                    }
                }
            }
        }
        maxRequest = tempMax;
        return maxRequest;
    }
 //TODO
    //does peak load count itself

    public int windowedPeakLoad(){

        return windowedPeakLoad(30);
    }

    /* TODO: Implement this datatype

        You must implement the methods with the exact signatures
        as provided in the statement for this mini-project.

        You must add method signatures even for the methods that you
        do not plan to implement. You should provide skeleton implementation
        for those methods, and the skeleton implementation could return
        values like null.

     */

}
