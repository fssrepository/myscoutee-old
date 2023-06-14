package com.raxim.myscoutee.algo;

import java.util.List;

public class GraphData {
    private List<Node> nodes;
    private List<EdgeData> edges;

    // Getters and setters

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<EdgeData> getEdges() {
        return edges;
    }

    public void setEdges(List<EdgeData> edges) {
        this.edges = edges;
    }

    public static class Node {
        private String id;
        private String type;

        public void setType(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }

    public static class EdgeData {
        private String from;
        private String to;
        private int weight;
        
        public String getFrom() {
            return from;
        }
        public void setFrom(String from) {
            this.from = from;
        }
        public String getTo() {
            return to;
        }
        public void setTo(String to) {
            this.to = to;
        }
        public int getWeight() {
            return weight;
        }
        public void setWeight(int weight) {
            this.weight = weight;
        }
    }
}
