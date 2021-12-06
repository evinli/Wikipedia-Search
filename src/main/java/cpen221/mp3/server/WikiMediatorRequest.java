package cpen221.mp3.server;

import com.google.gson.Gson;
import cpen221.mp3.wikimediator.WikiMediator;

import java.util.List;

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
        String request_id = id;
        String json_response = "";
        String status;
        String sResponse = null;
        List<String> lResponse = null;
        Gson gson = new Gson();
        if (type.equals("search")) {
           try {
               lResponse = wikiMediator.search(query, limit);
               status = "success";
           } catch (Exception e) {
               status = "failed";
           }
           ListResponse r = new ListResponse(request_id, status, lResponse);
           json_response = r.respond();
        }

        if (type.equals("getPage")) {
            try {
                sResponse = wikiMediator.getPage(pageTitle);
                status = "success";
            } catch (Exception e) {
                status = "failed";
            }
            Response r = new Response(request_id, status, sResponse);
            json_response = r.respond();
        }

        if (type.equals("zeitgeist")) {
            try {
                lResponse = wikiMediator.zeitgeist(limit);
                status = "success";
            } catch (Exception e) {
                status = "failed";
            }
            ListResponse r = new ListResponse(request_id, status, lResponse);
            json_response = r.respond();
        }

        if (type.equals("trending")) {
            try {
                lResponse = wikiMediator.trending(timeLimitInSeconds, maxItems);
                status = "success";
            } catch (Exception e) {
                status = "failed";
            }
            ListResponse r = new ListResponse(request_id, status, lResponse);
            json_response = r.respond();
        }

        if (type.equals("windowedPeakLoad")) {
            try {
                if (timeWindowInSeconds != 0) {
                    sResponse = String.valueOf(wikiMediator.windowedPeakLoad(timeWindowInSeconds));
                } else {
                    sResponse = String.valueOf(wikiMediator.windowedPeakLoad());
                }
                status = "success";
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


