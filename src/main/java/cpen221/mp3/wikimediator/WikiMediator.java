package cpen221.mp3.wikimediator;

import cpen221.mp3.exceptions.InvalidObjectException;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WikiMediator {
    FSFTBuffer cache;
    Wiki wiki;
    HashMap<String, ArrayList> pageSearches; //stores timestamps for search and get queries
    ArrayList methodsCalls; // stores tiemstamps for all method calls
    Long StartTime;


    public WikiMediator(int capacity, int stalenessInterval){
        Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();
        cache = new FSFTBuffer(capacity, stalenessInterval);
        StartTime = System.currentTimeMillis() / 1000L;
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

            //adds the timestamp of the request to the hasmap pageSearches
            if(pageSearches.containsKey(query)){
                pageSearches.get(query).add((int)(System.currentTimeMillis() / 1000L - StartTime));
                methodsCalls.add((int)(System.currentTimeMillis() / 1000L - StartTime));
            }
            else{
                pageSearches.put(query, new ArrayList());
                pageSearches.get(query).add((int)(System.currentTimeMillis() / 1000L - StartTime));
                methodsCalls.add((int)(System.currentTimeMillis() / 1000L - StartTime));
            }


        return matches;
    }

    /**
     * Given a pageTitle, return the text associated with the Wikipedia page that matches pageTitle.
     * @param pageTitle
     * @return
     */
    public String getPage(String pageTitle){
        String text = new String();

        try{
            text = cache.get(pageTitle).toString();

        } catch (InvalidObjectException e) {
            e.printStackTrace();
            text = wiki.getPageText(pageTitle);
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
    public List<Map.Entry<String, Integer>> zeitgeist(int limit){
        List<Map.Entry<String, Integer>> mostCommon= new ArrayList<>();
        HashMap<String, Integer> reduced = new HashMap<String, Integer>();

        for (Map.Entry<String, ArrayList> entry : pageSearches.entrySet()) {
            String k = entry.getKey();
            ArrayList v = entry.getValue();
            reduced.put(k, v.size());
        }

        mostCommon = reduced.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue()).limit(limit).toList();


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
        for(int i = 0; i < methodsCalls.size(); i++){
            for(int j = i; j < methodsCalls.size(); j++){
                int valueJ = (int) methodsCalls.get(j);
                int valueI = (int) methodsCalls.get(i);

                if((valueJ - valueI) > timeWindowInSeconds){
                    if(((j - 1) - i) > tempMax){
                        tempMax = ((j - 1) - i);
                    }
                    break;
                }
            }
        }
        maxRequest = tempMax;
        return maxRequest;
    }

    public int windowedPeakLoad(){
        int timeWindowInSeconds = 30;
        int maxRequest = 0;

        return maxRequest;
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
