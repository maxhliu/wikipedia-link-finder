import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.ArrayList;

/**
 * Created by Max on 15-11-28.
 */
public abstract class Searcher {
    private ArrayList<Node> currentLevel;
    private Node root;

//    void iterate() {
//        try {
//            currentLevel = Node.toNodes(getLinks(Node.toStrings(currentLevel)));
//        } catch (UnirestException e) {
//            e.printStackTrace();
//        }
//    }

    void iterate() {
        for (Node n : currentLevel) {
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
    abstract ArrayList<String> getLinks(String article) throws UnirestException;
    abstract public ArrayList<String> tracePath(Node n);
}
