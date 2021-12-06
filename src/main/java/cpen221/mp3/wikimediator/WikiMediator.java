package cpen221.mp3.wikimediator;

import com.google.gson.Gson;
import cpen221.mp3.exceptions.InvalidObjectException;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;



public class WikiMediator {
    Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();
    FSFTBuffer cache;
    HashMap<String, ArrayList> pageSearches; //stores timestamps for search and get queries
    ArrayList methodsCalls; // stores timestamps for all method calls
    Integer StartTime;

    /**
     * Abstraction function:
     *      AF(cache) = Cache that holds wikipedia page for a specified time window
     *      AF(cache.keySet()) = all the page names of wikipedia pages stored in the cache
     *      AF(pageSearches) = holds timestamps for search and get queries
     *      AF(pageSearches.keySet()) = all the page names of wikipedia pages used in search or getpage
     *      AF(methodCalls) = stores timestamps for all method calls
     *      AF(methodCalls.size()) = the total number of method calls
     *      AF(StartTime) = reference time = 0
     */

    /**
     * Rep invariant:
     *      every object in the Wikimediator cache is a Wikipage
     *      every timestamp in pageSearches also exists in methodCalls
     */

    class WikiMediatorData {
        private HashMap<String, ArrayList> pageSearch;
        private ArrayList methodCall;

        public WikiMediatorData(HashMap<String, ArrayList> pageSearch, ArrayList methodCall) {
            this.pageSearch = pageSearch;
            this.methodCall = methodCall;
        }
    }

    /**
     * Creates a WikiMediator that acts as a cache for wikipedia pages
     *
     * @param capacity the maximum number of items the WikiMediator can store
     * @param stalenessInterval the time it takes for a page to become stale in the WikiMediator
     */
    public WikiMediator(int capacity, int stalenessInterval){
        try {
            Gson gson = new Gson();
            String oldData = new Scanner(new File("local/WikiMediatorSave.txt")).useDelimiter("\\Z").next();
            WikiMediatorData wd = gson.fromJson(oldData, WikiMediatorData.class);
            pageSearches = wd.pageSearch;
            methodsCalls = wd.methodCall;
        } catch (Exception e) {
            pageSearches = new HashMap<String, ArrayList>();
            methodsCalls = new ArrayList<Integer>();
        }
        cache = new FSFTBuffer(capacity, stalenessInterval);
        StartTime = (int)(System.currentTimeMillis() / 1000L);
    }


    /**
     * Given a query, return up to limit page titles that match the query string
     * (per Wikipedia's search service).
     * @param query
     * @param limit the max number of page titles it can return
     * @return List of up to limit page titles that match the query string
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
     * @param pageTitle
     * @return the text associated with the Wikipedia page that matches pageTitle.
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
     * Return only limit items
     *
     * @param limit the number of items the list should contain
     * @return list of most common strings used in search and getpage requests
     * with items being sorted in non-increasing count order
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
     * items are sorted in non-increasing count order. report at most maxItems of the most
     * frequent requests
     *
     * @param timeLimitInSeconds
     * @param maxItems number of items the list should contain
     * @return returns list of most frequent requests made in last timelimitinseconds seconds
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

        mostFrequent = reduced.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).limit(maxItems).map(e -> e.getKey()).collect(Collectors.toList());

        return mostFrequent;
    }

    /**
     * request count includes all request made using WikiMediator. Counts all 5 methods listed as basic page
     * requests
     * @param timeWindowInSeconds
     * @return the maximum number of requests seen at any time window of a given length
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
    /**
     * request count includes all request made using WikiMediator. Counts all 5 methods listed as basic page
     * requests
     * @return the maximum number of requests seen at any time window of 30 seconds
     */
    public int windowedPeakLoad(){

        return windowedPeakLoad(30);
    }

    public void stop() {
        Gson gson = new Gson();
        WikiMediatorData wd = new WikiMediatorData(this.pageSearches,this.methodsCalls);
        String json = gson.toJson(wd);
        try (PrintWriter out = new PrintWriter("local/WikiMediatorSave.txt")) {
            out.println(json);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file");
        }
    }

}
