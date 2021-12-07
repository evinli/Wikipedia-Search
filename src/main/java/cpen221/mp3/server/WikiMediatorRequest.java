package cpen221.mp3.server;

import com.google.gson.Gson;
import cpen221.mp3.wikimediator.WikiMediator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WikiMediatorRequest {
    //Values to be mapped to by Gson
    private String id; //all
    private String type; //all
    private String query; //search
    private int limit; //search / zeitgeist
    private String pageTitle; //getPage
    private int timeLimitInSeconds; //trending
    private int maxItems; //trending
    private int timeout; //all
    private int timeWindowInSeconds; //windowedPeakLoad

    /**
     * Interface to be implemented by response classes;
     */
    interface WikiMediatorResponse {
        /**
         * Generates properly formatted json response
         * @return Json response formatted by Gson
         */
        public String respond();
    }

    /**
     * Gson mapping class for WikiMediator methods which return a list.
     * Entire purpose of class is to generate json formatted response using Gson.
     *
     */

    class ListResponse implements WikiMediatorResponse {
        String id;
        String status;
        public List<String> response;

        /**
         * Initializer for class. Takes values to be transformed into json formatted string.
         * @param id id of the request
         * @param status status of the request ("success" or "failed")
         * @param response response to request
         */
        public ListResponse(String id, String status, List<String> response) {
            this.id = id;
            this.status = status;
            this.response = response;
        }

        /**
         * Generates properly formatted json response
         * @return Json response formatted by Gson
         */
        public String respond() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }

    /**
     * Gson mapping class for WikiMediator methods which return a string.
     * Entire purpose of class is to generate json formatted response using Gson.
     *
     */
    class Response implements WikiMediatorResponse {
        String id;
        String status;
        String response;

        /**
         * Initializer for class. Takes values to be transformed into json formatted string.
         * @param id id of the request
         * @param status status of the request ("success" or "failed")
         * @param response response to request
         */
        public Response(String id, String status, String response) {
            this.id = id;
            this.status = status;
            this.response = response;

        }
        /**
         * Generates properly formatted json response
         * @return Json response formatted by Gson
         */
        public String respond() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }

    /**
     * Generates a json-formatted response to a request based on the wikimediator instance
     * provided and the values of the variable within the WikiMediatorRequest.
     * @param wikiMediator WikiMediator instance to be queried
     * @return json formatted string response
     */
    public String handle(WikiMediator wikiMediator) {
        boolean timeOut = false; //true if operations times out
        boolean isList = true; //true if request calls a method that returns a list, true by default

        String request_id = id;
        String json_response = "";
        String status;

        //null by default as gson ignores null values
        String sResponse = null;
        List<String> lResponse = null;
        Future<List<String>> lResponseT = null;
        Future<String> sResponseT = null;

        //Assigning futures
        if (type.equals("search")) {
           lResponseT = CompletableFuture.supplyAsync(() -> wikiMediator.search(query, limit));
        }

        if (type.equals("getPage")) {
            isList = false;
            sResponseT = CompletableFuture.supplyAsync(() -> wikiMediator.getPage(pageTitle));
        }

        if (type.equals("zeitgeist")) {
            lResponseT = CompletableFuture.supplyAsync(() -> wikiMediator.zeitgeist(limit));
        }

        if (type.equals("trending")) {
            lResponseT = CompletableFuture.supplyAsync(() -> wikiMediator.trending(timeLimitInSeconds, maxItems));
        }

        if (type.equals("windowedPeakLoad")) {
            isList = false;
            if (timeWindowInSeconds != 0) {
                sResponseT = CompletableFuture.supplyAsync(() -> String.valueOf(wikiMediator.windowedPeakLoad(timeWindowInSeconds)));
            } else {
                sResponseT = CompletableFuture.supplyAsync(() -> String.valueOf(wikiMediator.windowedPeakLoad()));
            }
        }
        //formatting json for list
        if (isList) {
            try {
                //checks to see if timeout is set
                if (timeout > 0) lResponse = lResponseT.get(timeout, TimeUnit.SECONDS);
                else lResponse = lResponseT.get();
                status = "success";
            } catch (TimeoutException t) {
                timeOut = true;
                status = "failed";
                sResponse = "Operation timed out";
                lResponseT.cancel(true);
            } catch (Exception e) {
                status = "failed";
            }
            if (timeOut) {
                Response r = new Response(request_id, status, sResponse);
                json_response = r.respond();
            } else {
                ListResponse r = new ListResponse(request_id, status, lResponse);
                json_response = r.respond();
            }
        //Formatting json for string
        } else {
            try {
                //checks to see if timeout is set
                if (timeout > 0) sResponse = sResponseT.get(timeout, TimeUnit.SECONDS);
                else sResponse = sResponseT.get();
                status = "success";
            } catch (TimeoutException t) {
                timeOut = true;
                status = "failed";
                sResponse = "Operation timed out";
                sResponseT.cancel(true);
            } catch (Exception e) {
                status = "failed";
            }
            Response r = new Response(request_id, status, sResponse);
            json_response = r.respond();
        }
        //formatting json for
        if (type.equals("stop")) {
            json_response = "{\"id\":\"" + request_id + "\",\"response\":\"bye\"}";
            wikiMediator.close();
        }
        return json_response;
    }
}


