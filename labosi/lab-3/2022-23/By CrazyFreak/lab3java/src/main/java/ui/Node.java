package ui;

import java.util.*;

public class Node {

    private String label;
    private boolean isLeaf;
    private Map<String, Node> children;

    public Node(String label) {
        this.label = label;
        this.isLeaf = false;
        this.children = new HashMap<>();
    }

    public Node(String label, boolean isLeaf) {
        this.label = label;
        this.isLeaf = isLeaf;
        this.children = new HashMap<>();
    }

    public String getLabel() {
        return label;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public Map<String, Node> getChildren() {
        return children;
    }

    public void addChild(String value, Node node) {
        this.children.put(value, node);
    }

    @Override
    public String toString() {
        return "Node{" +
                "label='" + label + '\'' +
                ", isLeaf=" + isLeaf +
                ", children=" + children +
                '}';
    }
}
