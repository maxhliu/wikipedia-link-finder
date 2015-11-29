import com.jayway.jsonpath.JsonPath;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Scanner;

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

    static ArrayList<String> search(String a, String b) {
        Searcher start = new ForwardSearcher(a);
        Searcher end = new BackwardSearcher(b);
        ArrayList<String> path = getPath(start, end);
        while (path == null) {
            if (compareSize(start, end) >= 0) {
                start.iterate();
            } else {
                end.iterate();
            }
            path = getPath(start, end);
        }
        return path;
    }

    static ArrayList<String> getURLs(ArrayList<String> path) {
        ArrayList<String> URLs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int i, length = path.size() - 1;
        for (i = 0; i < length; i++) {
            sb.append(path.get(i) + "|");
        }
        sb.append(path.get(i));
        try {
            // /w/api.php?action=query&prop=info&format=json&inprop=url&titles=Kanye%20West%7CJesus
            HttpResponse<JsonNode> jsonResponse = Unirest.get("https://en.wikipedia.org/w/api.php")
                    .queryString("action", "query")
                    .queryString("prop", "info")
                    .queryString("format", "json")
                    .queryString("titles", sb)
                    .queryString("inprop", "url")
                    .asJson();
            URLs = JsonPath.read(jsonResponse.getBody().toString(), "$..fullurl");
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return URLs;
    }

    static void startSearch(Path filePath) throws IOException {
        Scanner s = new Scanner(filePath.toFile());
        ArrayList<String> path = search(s.nextLine(), s.nextLine());
        FileUtils.writeLines(filePath.resolveSibling("test.txt").toFile(), path);
        FileUtils.writeLines(filePath.resolveSibling("test2.txt").toFile(), getURLs(path));
    }

    public static void main (String args[]) throws UnirestException, IOException, InterruptedException {
        //path is "/Users/dannyliu/Documents/Wikiproject/textfiles/link.txt"
        final Path path = FileSystems.getDefault().getPath(System.getProperty("user.home"), "Desktop");
//        final Path path = Paths.get("/Users/dannyliu/Documents/Wikiproject/textfiles/");
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                final WatchKey wk = watchService.take();
                for (WatchEvent<?> event : wk.pollEvents()) {
                    final Path changed = (Path) event.context();
                    if (changed.endsWith("link.txt")) {
                        //get file contents and do the search
                        startSearch(path.resolve(changed));
                    }
                }
                boolean valid = wk.reset();
//                if (!valid) {
//                    //key has been unregistered
//                }
            }
        }
    }
}
