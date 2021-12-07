package cpen221.mp3.server;

import com.google.gson.Gson;
import cpen221.mp3.wikimediator.WikiMediator;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WikiMediatorRequest {
    private String id; //all
    private String type; //all
    private String query; //search /
    private int limit; //search / zeitgeist
    private String pageTitle; //getPage
    private int timeLimitInSeconds; //trending
    private int maxItems; //trending
    private int timeout; //all
    private int timeWindowInSeconds; //windowedPeakLoad

    interface WikiMediatorResponse {
        public String respond();
    }

    class ListResponse implements WikiMediatorResponse {
        String id;
        String status;
        public List<String> response;

        public ListResponse(String id, String status, List<String> response) {
            this.id = id;
            this.status = status;
            this.response = response;

        }
        public String respond() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }

    class Response implements WikiMediatorResponse {
        String id;
        String status;
        String response;

        public Response(String id, String status, String response) {
            this.id = id;
            this.status = status;
            this.response = response;

        }
        public String respond() {
            Gson gson = new Gson();
            return gson.toJson(this);
        }
    }

    public String handle(WikiMediator wikiMediator) {
        boolean timeOut = false;
        boolean isList = true;
        String request_id = id;
        String json_response = "";
        String status;
        String sResponse = null;
        List<String> lResponse = null;
        Gson gson = new Gson();
        Future<List<String>> lResponseT = null;
        Future<String> sResponseT = null;
        //wrap this in timeout
        if (type.equals("search")) {
           lResponseT = CompletableFuture.supplyAsync(() -> wikiMediator.search(query, limit));
        }

        if (type.equals("getPage")) {
            sResponseT = CompletableFuture.supplyAsync(() -> wikiMediator.getPage(pageTitle));
        }

        if (type.equals("zeitgeist")) {
            lResponseT = CompletableFuture.supplyAsync(() -> wikiMediator.zeitgeist(limit));
        }

        if (type.equals("trending")) {
            lResponseT = CompletableFuture.supplyAsync(() -> wikiMediator.trending(timeLimitInSeconds, maxItems));
        }

        if (type.equals("windowedPeakLoad")) {
            if (timeWindowInSeconds != 0) {
                sResponseT = CompletableFuture.supplyAsync(() -> String.valueOf(wikiMediator.windowedPeakLoad(timeWindowInSeconds)));
            } else {
                sResponseT = CompletableFuture.supplyAsync(() -> String.valueOf(wikiMediator.windowedPeakLoad()));
            }
        }
        if (isList) {
            try {
                lResponse = lResponseT.get(timeout, TimeUnit.SECONDS);
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
        } else {
            try {
                sResponse = sResponseT.get(timeout, TimeUnit.SECONDS);
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
        if (type.equals("stop")) {
            json_response = "{\"id\":\"" + request_id + "\",\"response\":\"bye\"}";
            wikiMediator.stop();
        }
        return json_response;
    }
}


