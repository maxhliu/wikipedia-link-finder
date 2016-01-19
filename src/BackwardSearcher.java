import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/**
 * Created by Max on 15-11-28.
 */
public class BackwardSearcher extends Searcher {

    HttpRequest getRequest(ArrayList<String> articles) {
        StringBuilder sb = new StringBuilder();
        int i, length = articles.size() - 1;
        for (i = 0; i < length; i++) {
            sb.append(articles.get(i) + "|");
        }
        sb.append(articles.get(i));
        HttpRequest request = Unirest.get("https://en.wikipedia.org/w/api.php")
                .queryString("action", "query")
                .queryString("prop", "linkshere")
                .queryString("format", "json")
                .queryString("titles", sb)
                .queryString("lhlimit", "max")
                .queryString("lhnamespace", "0");
        return request;
    }

    ArrayList<String> getLinks(ArrayList<String> articles) throws UnirestException {
        ArrayList <String> backlinks = new ArrayList<>();
        HttpResponse<JsonNode> jsonResponse = getRequest(articles).asJson();
        Map<String, Object> continueBlock;
        while (true) {
            backlinks.addAll(JsonPath.read(jsonResponse.getBody().toString(), "$..linkshere..title"));
            try {
                continueBlock = JsonPath.read(jsonResponse.getBody().toString(), "$.continue");
            } catch (PathNotFoundException e) {
                break;
            }
            jsonResponse = getRequest(articles).queryString(continueBlock).asJson();
        }
        return backlinks;
    }

    ArrayList<String> getLinks(String article) throws UnirestException, InvalidClassException, TimeoutException {
        ArrayList <String> backlinks;
        HttpResponse<JsonNode> jsonResponse;
        //establish forward links
        jsonResponse = Unirest.get("https://en.wikipedia.org/w/api.php")
                .queryString("action", "query")
                .queryString("prop", "info")
                .queryString("list", "backlinks")
                .queryString("format", "json")
                .queryString("bltitle", article)
                .queryString("bllimit", "500")
                .asJson();
        backlinks = JsonPath.read(jsonResponse.getBody().toString(), "$..backlinks..title");
        if (backlinks.isEmpty()) {
            throw new InvalidClassException("lol");
        }
        while (true) {
            if (System.currentTimeMillis() - startTime > 20000) {
                throw new TimeoutException();
            }
            Map<String, Object> continueBlock;
            try {
                continueBlock = JsonPath.read(jsonResponse.getBody().toString(), "$.continue");
            } catch (PathNotFoundException e) {
                break;
            }
            jsonResponse = Unirest.get("https://en.wikipedia.org/w/api.php")
                    .queryString("action", "query")
                    .queryString("prop", "info")
                    .queryString("format", "json")
                    .queryString("list", "backlinks")
                    .queryString("bltitle", article)
                    .queryString("bllimit", "500")
                    .queryString(continueBlock)
                    .asJson();
            backlinks.addAll(JsonPath.read(jsonResponse.getBody().toString(), "$..backlinks..title"));
        }
        return backlinks;
    }

    public ArrayList<String> tracePath(Node n) {
        ArrayList<String> path = new ArrayList<>();
        while (n.getParent() != null) {
            n = n.getParent();
            path.add(n.getValue());
        }
        return path;
    }

    BackwardSearcher(String value) {
        super(value);
    }
}
