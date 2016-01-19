import com.jayway.jsonpath.JsonPath;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jdk.nashorn.internal.parser.JSONParser;
import net.minidev.json.JSONArray;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Main {

    //sees whether it is more efficient to find backlinks or forward links
    static int compareSize(Searcher a, Searcher b) {
        if (a.getSize() > b.getSize()) {
            return -1;
        } else if (b.getSize() > a.getSize()) {
            return 1;
        } else {
            return 0;
        }
    }

    //gets the path in linear arraylist form from the two searchers
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

    //the method which actually finds the path
    static ArrayList<String> search(String a, String b) throws TimeoutException, InvalidClassException {
        Searcher start = new ForwardSearcher(a);
        Searcher end = new BackwardSearcher(b);
        long startTime = System.currentTimeMillis();
        start.startTime = startTime;
        end.startTime = startTime;
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

    //gets urls of articles. Used to get urls of the path
    static ArrayList<String> getURLs(ArrayList<String> path) {
        ArrayList<String> URLs = new ArrayList<>();
        for (String s : path) {
            HttpResponse<JsonNode> jsonResponse = null;
            try {
                jsonResponse = Unirest.get("https://en.wikipedia.org/w/api.php")
                        .queryString("action", "query")
                        .queryString("prop", "info")
                        .queryString("format", "json")
                        .queryString("titles", s)
                        .queryString("inprop", "url")
                        .asJson();
            } catch (UnirestException e) {
                e.printStackTrace();
            }
            String URL = ((JSONArray) JsonPath.read(jsonResponse.getBody().toString(), "$..fullurl")).get(0).toString();
            URLs.add(URL);
        }
        return URLs;
    }

//    static ArrayList<String> getURLs(ArrayList<String> path) {
//        ArrayList<String> URLs = new ArrayList<>();
//        StringBuilder sb = new StringBuilder();
//        int i, length = path.size() - 1;
//        for (i = 0; i < length; i++) {
//            sb.append(path.get(i) + "|");
//        }
//        sb.append(path.get(i));
//        try {
//            // /w/api.php?action=query&prop=info&format=json&inprop=url&titles=Kanye%20West%7CJesus
//            HttpResponse<JsonNode> jsonResponse = Unirest.get("https://en.wikipedia.org/w/api.php")
//                    .queryString("action", "query")
//                    .queryString("prop", "info")
//                    .queryString("format", "json")
//                    .queryString("titles", sb)
//                    .queryString("inprop", "url")
//                    .asJson();
//            URLs = JsonPath.read(jsonResponse.getBody().toString(), "$..fullurl");
//        } catch (UnirestException e) {
//            e.printStackTrace();
//        }
//        return URLs;
//    }

    //method which is called whenever you need to start a new search
    static void startSearch(Path filePath) throws IOException, TimeoutException {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Scanner s = new Scanner(filePath.toFile());
        //gets the actual path
        ArrayList<String> path = search(s.nextLine(), s.nextLine());
        //writes the path
        FileUtils.writeLines(filePath.resolveSibling("outputTitles.txt").toFile(), path);
        //gets the urls of the path to have active links
        FileUtils.writeLines(filePath.resolveSibling("outputLinks.txt").toFile(), getURLs(path));
        //print the path for debugging
        for (String string : path) {
            System.out.println(string);
        }
    }

    public static void main (String args[]) throws UnirestException, IOException, InterruptedException {
        //path is "/Users/dannyliu/Documents/Wikiproject/textfiles/link.txt"
        System.out.println("Java has run. Cwd is : " + System.getProperty("user.dir"));
        final Path path = Paths.get(System.getProperty("user.dir") + "/textfiles/");
//        final Path path = FileSystems.getDefault().getPath(System.getProperty("user.home"), "Desktop");
//        final Path path = Paths.get("/Users/dannyliu/Documents/Wikiproject/textfiles/");
        try (final WatchService watchService = FileSystems.getDefault().newWatchService()) {
            final WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                final WatchKey wk = watchService.take();
                for (WatchEvent<?> event : wk.pollEvents()) {
                    final Path changed = (Path) event.context();
                    if (changed.endsWith("inputTitles.txt")) {
                        //get file contents and do the search
                        try {
                            startSearch(path.resolve(changed));
                        } catch (TimeoutException e) {
                            FileUtils.writeStringToFile(path.resolve(changed).resolveSibling("done.txt").toFile(),
                                    Double.toString(Math.random()));
                        } catch (InvalidClassException e) {
                            FileUtils.writeStringToFile(path.resolve(changed).resolveSibling("invalid.txt").toFile(),
                                    Double.toString(Math.random()));
                        }
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
