import java.util.ArrayList;

public class Node {
    private String value;
    private Node parent;
    private ArrayList<Node> children;
    static ArrayList<String> toStrings(ArrayList<Node> nodes) {
        ArrayList<String> strings = new ArrayList<>();
        for (Node n : nodes) {
            strings.add(n.getValue());
        }
        return strings;
    }
    static ArrayList<Node> toNodes(ArrayList<String> strings) {
        ArrayList<Node> nodes = new ArrayList<>();
        for (String s : strings) {
            nodes.add(new Node(s));
        }
        return nodes;
    }
    public Node(String value) {
        this.value = value;
        parent = null;
        children = new ArrayList<>();
    }
    public void addChildren(ArrayList<Node> newChildren) {
        for (Node n : newChildren) {
            n.setParent(this);
        }
        children.addAll(newChildren);
    }
    public void setParent(Node parent) {
        this.parent = parent;
    }
    public Node getParent() {
        return parent;
    }
    public String getValue() {
        return value;
    }
}
