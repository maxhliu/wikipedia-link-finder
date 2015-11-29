import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Main {

    static int compareSize(Searcher a, Searcher b) {
        if (a.getSize() > b.getSize()) {
            return -1;
        } else if (b.getSize() > a.getSize()) {
            return 1;
        } else {
            return 0;
        }
    }

    static ArrayList<String> getPath(Searcher a, Searcher b) {
        for (Node n1 : a.getCurrentLevel()) {
            for (Node n2 : b.getCurrentLevel()) {
                if (n1.getValue().equals(n2.getValue())) {
                    ArrayList<String> path = new ArrayList<>();
                    path.addAll(a.tracePath(n1));
                    path.add(n1.getValue());
                    path.addAll(b.tracePath(n2));
                    return path;
                }
            }
        }
        return null;
    }

    public static void main (String args[]) throws UnirestException {
        Searcher start = new ForwardSearcher("Danny");
        Searcher end = new BackwardSearcher("Illuminati");
        ArrayList<String> path = getPath(start, end);
        while (path == null) {
            if (compareSize(start, end) >= 0) {
                start.iterate();w
            } else {
                end.iterate();
            }
            path = getPath(start, end);
        }
        for (String s : path) {
            System.out.println(s);
        }
    }
}
