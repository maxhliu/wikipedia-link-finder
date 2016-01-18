import com.mashape.unirest.http.exceptions.UnirestException;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Created by Max on 15-11-28.
 */
public abstract class Searcher {

    public long startTime = 0;

    private ArrayList<Node> currentLevel;
    private Node root;

//    void iterate() {
//        try {
//            currentLevel = Node.toNodes(getLinks(Node.toStrings(currentLevel)));
//        } catch (UnirestException e) {
//            e.printStackTrace();
//        }
//    }

    void iterate() throws TimeoutException, InvalidClassException {
        for (Node n : currentLevel) {
            if (System.currentTimeMillis() - startTime > 20000) {
                throw new TimeoutException();
            }
            ArrayList<Node> articles = null;
            try {
                articles = Node.toNodes(getLinks(n.getValue()));
            } catch (UnirestException e) {
                e.printStackTrace();
            }
            n.addChildren(articles);
            currentLevel = articles;
        }
    }

        Searcher(String value) {
        root = new Node(value);
        currentLevel = new ArrayList<>();
        currentLevel.add(root);
    }
    int getSize() {
        return currentLevel.size();
    }
    ArrayList<Node> getCurrentLevel() {
        return currentLevel;
    }
    abstract ArrayList<String> getLinks(ArrayList<String> articles) throws UnirestException;
    abstract ArrayList<String> getLinks(String article) throws UnirestException, InvalidClassException;
    abstract public ArrayList<String> tracePath(Node n);
}
