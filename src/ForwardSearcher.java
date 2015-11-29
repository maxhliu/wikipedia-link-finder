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
public class ForwardSearcher extends Searcher {
    ArrayList<String> getLinks(String article) throws UnirestException {
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
