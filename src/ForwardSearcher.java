import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import java.io.InvalidClassException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Max on 15-11-28.
 */
public class ForwardSearcher extends Searcher {

    HttpRequest getRequest(ArrayList<String> articles) {
        StringBuilder sb = new StringBuilder();
        int i, length = articles.size() - 1;
        for (i = 0; i < length; i++) {
            sb.append(articles.get(i) + "|");
        }
        sb.append(articles.get(i));
        HttpRequest request = Unirest.get("https://en.wikipedia.org/w/api.php")
                .queryString("action", "query")
                .queryString("prop", "links")
                .queryString("format", "json")
                .queryString("titles", sb)
                .queryString("pllimit", "max")
                .queryString("plnamespace", "0");
        return request;
    }

    ArrayList<String> getLinks(ArrayList<String> articles) throws UnirestException {
        ArrayList <String> backlinks = new ArrayList<>();
        HttpResponse<JsonNode> jsonResponse = getRequest(articles).asJson();
        Map<String, Object> continueBlock;
        while (true) {
            backlinks.addAll(JsonPath.read(jsonResponse.getBody().toString(), "$..links..title"));
            try {
                continueBlock = JsonPath.read(jsonResponse.getBody().toString(), "$.continue");
            } catch (PathNotFoundException e) {
                break;
            }
            jsonResponse = getRequest(articles).queryString(continueBlock).asJson();
        }
        return backlinks;
    }

    ArrayList<String> getLinks(String article) throws UnirestException, InvalidClassException {
        ArrayList<String> forwardLinks;
        HttpResponse<JsonNode> jsonResponse;
        //establish forward links
        jsonResponse = Unirest.get("https://en.wikipedia.org/w/api.php")
                .queryString("action", "query")
                .queryString("prop", "links")
                .queryString("format", "json")
                .queryString("titles", article)
                .queryString("pllimit", "500")
                .asJson();
        forwardLinks = JsonPath.read(jsonResponse.getBody().toString(), "$..links..title");
        if (forwardLinks.isEmpty()) {
            throw new InvalidClassException("lol");
        }
        while (true) {
            Map<String, Object> continueBlock;
            try {
                continueBlock = JsonPath.read(jsonResponse.getBody().toString(), "$.continue");
            } catch (PathNotFoundException e) {
                break;
            }
            jsonResponse = Unirest.get("https://en.wikipedia.org/w/api.php")
                    .queryString("action", "query")
                    .queryString("prop", "links")
                    .queryString("format", "json")
                    .queryString("titles", article)
                    .queryString("pllimit", "500")
                    .queryString(continueBlock)
                    .asJson();
            forwardLinks.addAll(JsonPath.read(jsonResponse.getBody().toString(), "$..links..title"));
        }
        return forwardLinks;
    }

    public ArrayList<String> tracePath(Node n) {
        ArrayList<String> path = new ArrayList<>();
        while (n.getParent() != null) {
            n = n.getParent();
            path.add(n.getValue());
        }
        Collections.reverse(path);
        return path;
    }

    ForwardSearcher(String value) {
        super(value);
    }
}
