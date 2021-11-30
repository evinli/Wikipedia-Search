package cpen221.mp3.wikimediator;

import cpen221.mp3.exceptions.InvalidObjectException;
import cpen221.mp3.fsftbuffer.Bufferable;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WikiMediator {
    FSFTBuffer cache;
    Wiki wiki;




    public WikiMediator(int capacity, int stalenessInterval){
        Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();
        cache = new FSFTBuffer(capacity, stalenessInterval);
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
        Object results = new Object();
        try {
            results =  cache.get(query);

        } catch (InvalidObjectException e) {
            e.printStackTrace();
            matches = wiki.search(query, limit)
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

        text = wiki.getPageText(pageTitle);
        return text;
    }

    /**
     * Most common strings used in searchand getpage requests,
     * with items being sorted in non-increasing count order.
     * Return only limit items
     *
     * @param limit
     * @return
     */
    public List<String> zeitgeist(int limit){
        List<String> mostCommon= new ArrayList<String>();

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
