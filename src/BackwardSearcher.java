import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Max on 15-11-28.
 */
public class BackwardSearcher extends Searcher {
    ArrayList<String> getLinks(String article) throws UnirestException {
        ArrayList <String> backLinks;
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
        backLinks = JsonPath.read(jsonResponse.getBody().toString(), "$..backlinks..title");
        while (true) {
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
            backLinks.addAll(JsonPath.read(jsonResponse.getBody().toString(), "$..backlinks..title"));
        }
        return backLinks;
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
