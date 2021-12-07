package cpen221.mp3.wikimediator;

import com.google.gson.Gson;
import cpen221.mp3.exceptions.InvalidObjectException;
import cpen221.mp3.fsftbuffer.FSFTBuffer;
import org.fastily.jwiki.core.Wiki;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * A immutable data type that represents a mediator service for Wikipedia.
 */
public class WikiMediator {
    Wiki wiki = new Wiki.Builder().withDomain("en.wikipedia.org").build();
    private static final int CONVERT_MS_TO_S = 1000;
    private FSFTBuffer<WikiPage> cache;
    private HashMap<String, ArrayList<Integer>> pageSearches;
    private ArrayList<Integer> methodsCalls;
    private int startTime;

    /**
     * Abstraction function:
     *      AF(cache) = data cache that stores all wikipedia pages for a given
     *                  staleness interval and cache capacity
     *      AF(pageSearches.keySet()) = page names of all wikipedia pages
     *                                  used in search() or getPage() requests
     *      AF(pageSearches.get(pageName)) = timestamps for search() and
     *                                       getPage() requests called using
     *                                       pageName
     *      AF(methodCalls) = stores timestamps for all method requests
     *      AF(methodCalls.size()) = the total number of method requests
     *      AF(startTime) = reference time
     */

    /**
     * Rep invariant:
     *      every timestamp in pageSearches also exists in methodCalls
     */

    /**
     * Thread-safety argument:
     *      all critical regions are locked in a synchronized (this) block to
     *          prevent multiple threads from accessing shared data at the
     *          same time
     */

    /**
     * GSON mapping class used to store data between WikiMediatorServer
     * sessions.
     */
    class WikiMediatorData {
        private HashMap<String, ArrayList<Integer>> pageSearch;
        private ArrayList methodCall;
        private int startTime;

        /**
         * Initializer:
         * Parameters are a subset of the WikiMediator class variables
         */
        public WikiMediatorData(HashMap<String, ArrayList<Integer>> pageSearch,
                                ArrayList methodCall, int startTime) {
            this.pageSearch = pageSearch;
            this.methodCall = methodCall;
            this.startTime = startTime;
        }
    }

    /**
     * Creates a mediator service that accesses Wikipedia to obtain pages
     * and other relevant information. In addition to collecting statistical
     * information about requests, the mediator service should also cache
     * Wikipedia pages to minimize network accesses. Note that timestamps for
     * requests are cached based on when calls to the API Jwiki are recorded
     * rather than when they're initially called (accounts for subtle time
     * delays).
     *
     * @param capacity the maximum number of Wikipedia pages the WikiMediator
     *                 can store
     * @param stalenessInterval the time it takes for a page to become stale in
     *                          the WikiMediator
     */
    public WikiMediator(int capacity, int stalenessInterval) {
        try {
            Gson gson = new Gson();
            String oldData = new Scanner(new File
                    ("local/WikiMediatorSave.txt")).useDelimiter("\\Z")
                    .next();
            WikiMediatorData wd = gson.fromJson(oldData,
                    WikiMediatorData.class);
            pageSearches = wd.pageSearch;
            methodsCalls = wd.methodCall;
            startTime = wd.startTime;
        } catch (Exception e) {
            pageSearches = new HashMap<>();
            methodsCalls = new ArrayList<>();
            startTime = (int) (System.currentTimeMillis() / CONVERT_MS_TO_S);
        }
        cache = new FSFTBuffer<>(capacity, stalenessInterval);
    }


    /**
     * Given a query, return up to limit page titles that match the query string
     * (per Wikipedia's search service).
     *
     * @param query the query string to be searched up
     * @param limit the max number of page titles to return
     * @return list of up to {@code limit} page titles that match the query
     */
    public List<String> search(String query, int limit) {
        List<String> matches = wiki.search(query, limit);
        int absoluteTime = (int) (System.currentTimeMillis() /
                CONVERT_MS_TO_S);

        synchronized (this) {
            if (pageSearches.containsKey(query)) {
                pageSearches.get(query).add(absoluteTime - startTime);
                methodsCalls.add(absoluteTime - startTime);
            } else {
                pageSearches.put(query, new ArrayList<>());
                pageSearches.get(query).add(absoluteTime - startTime);
                methodsCalls.add(absoluteTime - startTime);
            }
        }

        return matches;
    }

    /**
     * Given a page title, return the text associated with the Wikipedia page
     * that matches the page title.
     *
     * @param pageTitle the page title to retrieve text from
     * @return the text associated with the Wikipedia page that matches
     * {@code pageTitle}
     */
    public synchronized String getPage(String pageTitle) {
        String text;
        WikiPage page = new WikiPage(pageTitle);

        try {
            text = cache.get(pageTitle).text();
        } catch (InvalidObjectException e) {
            text = wiki.getPageText(pageTitle);
            cache.put(page);
        }

        int absoluteTime = (int) (System.currentTimeMillis() / CONVERT_MS_TO_S);
        if (pageSearches.containsKey(pageTitle)) {
            pageSearches.get(pageTitle).add(absoluteTime - startTime);
            methodsCalls.add(absoluteTime - startTime);
        } else {
            pageSearches.put(pageTitle, new ArrayList<>());
            pageSearches.get(pageTitle).add(absoluteTime - startTime);
            methodsCalls.add(absoluteTime - startTime);
        }

        return text;
    }

    /**
     * Return the most common query strings used in search() and getPage()
     * requests, with items sorted in decreasing count order up to a maximum of
     * {@code limit} items.
     *
     * @param limit the maximum number of items that should be returned
     * @return list of most common query strings used in search() and getPage()
     * requests in decreasing count order
     */
    public List<String> zeitgeist(int limit) {
        List<String> mostCommon;
        HashMap<String, Integer> reduced = new HashMap<>();
        int absoluteTime = (int) (System.currentTimeMillis() /
                CONVERT_MS_TO_S);

        synchronized (this) {
            methodsCalls.add(absoluteTime - startTime);
            for (String key : pageSearches.keySet()) {
                int size = pageSearches.get(key).size();
                reduced.put(key, size);
            }
        }
        mostCommon = reduced.entrySet().stream().sorted(Map.Entry.<String,
                Integer>comparingByValue().reversed()).limit(limit).map(e ->
                e.getKey()).collect(Collectors.toList());

        return mostCommon;
    }

    /**
     * Returns the most frequent requests made in the last
     * {@code timeLimitInSeconds} seconds, reporting up to a maximum of
     * {@code maxItems} of the most frequent requests.
     *
     * @param timeLimitInSeconds the time window to return requests from
     * @param maxItems           the maximum number of items that should be
     *                           returned
     * @return returns list of most frequent requests made in last
     * {@code timeLimitInSeconds} seconds
     */
    public List<String> trending(int timeLimitInSeconds, int maxItems) {
        List<String> mostFrequent;
        HashMap<String, Integer> reduced = new HashMap<>();
        int currentTime = (int) (System.currentTimeMillis() /
                CONVERT_MS_TO_S) - startTime;

        synchronized (this) {
            methodsCalls.add(currentTime);
            for (String key : pageSearches.keySet()) {
                int count = 0;
                int thresholdTime = currentTime - timeLimitInSeconds;

                for (int i = pageSearches.get(key).size() - 1; i >= 0; i--) {
                    int nextTime = pageSearches.get(key).get(i);
                    if (nextTime < thresholdTime) {
                        break;
                    }
                    count++;
                }
                if (count > 0) {
                    reduced.put(key, count);
                }
            }
        }
        mostFrequent = reduced.entrySet().stream().sorted(Map.Entry.<String,
                Integer>comparingByValue().reversed()).limit(maxItems).map(e ->
                e.getKey()).collect(Collectors.toList());

        return mostFrequent;
    }

    /**
     * Returns the maximum number of requests seen in any time window of a given
     * length. Note that the request count includes all requests made using the
     * WikiMediator, and therefore counts all four method calls, including
     * itself.
     *
     * @param timeWindowInSeconds the specified time window to find maximum
     *                            number of requests from
     * @return the maximum number of requests seen at any time window of a given
     * length
     */
    public int windowedPeakLoad(int timeWindowInSeconds) {
        int maxRequest = 0;
        int tempMax = 0;
        int absoluteTime = (int) (System.currentTimeMillis() /
                CONVERT_MS_TO_S);

        synchronized (this) {
            methodsCalls.add(absoluteTime - startTime);
            for (int i = 0; i < methodsCalls.size(); i++) {
                for (int j = i; j < methodsCalls.size(); j++) {
                    int valueJ = methodsCalls.get(j);
                    int valueI = methodsCalls.get(i);

                    if ((valueJ - valueI) > timeWindowInSeconds) {
                        if ((j - i) > tempMax) {
                            tempMax = (j - i);
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

    /**
     * Returns the maximum number of requests seen in any time window of 30
     * seconds.
     *
     * @return the maximum number of requests seen at any time window of 30
     * seconds
     */
    public int windowedPeakLoad() {
        return windowedPeakLoad(30);
    }


    /**
     * Returns the shortest path between two Wikipedia pages. If there are two
     * or more shortest paths, then the one with the lowest lexicographical
     * value is returned.
     *
     * @return a list of page titles (including the starting and ending pages)
     * on the shortest computed path; if no path exists, return an empty list
     */
    public List<String> shortestPath(String pageTitle1, String pageTitle2,
                                     int timeout) throws TimeoutException {
        return null;
    }


    /**
     * Saves data needed by zeitgeist() and trending() requests to a local file.
     * Note: This does not shut down the server.
     */
    public void close() {
        Gson gson = new Gson();
        WikiMediatorData wd = new WikiMediatorData(this.pageSearches,
                this.methodsCalls, this.startTime);
        String json = gson.toJson(wd);
        try (PrintWriter out = new PrintWriter
                ("local/WikiMediatorSave.txt")) {
            out.println(json);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to file");
        }
    }
}
