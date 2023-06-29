package com.raxim.myscoutee.algo.dto;

public class Edge {

    private final Node from;
    private final Node to;
    private final double weight;
    private final boolean ignored;

    public boolean isIgnored() {
        return ignored;
    }

    public Node getFrom() {
        return from;
    }

    public Node getTo() {
        return to;
    }

    public double getWeight() {
        return weight;
    }

    public Edge(String from, String to) {
        this(new Node(from), new Node(to), 0, false);
    }

    public Edge(Node from, Node to) {
        this(from, to, 0d);
    }

    public Edge(Node from, Node to, double weight) {
        this(from, to, weight, false);
    }

    public Edge(Node from, Node to, double weight, boolean ignored) {
        this.from = from;
        this.to = to;
        this.weight = weight;
        this.ignored = ignored;
    }

    @Override
    public int hashCode() {
        int hashCode1 = from.hashCode() ^ to.hashCode();
        int hashCode2 = to.hashCode() ^ from.hashCode();
        return hashCode1 ^ hashCode2;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Edge other = (Edge) obj;
        if (from == null) {
            if (other.from != null)
                return false;
        } else if (!from.equals(other.from) && !to.equals(other.from))
            return false;
        if (to == null) {
            if (other.to != null)
                return false;
        } else if (!to.equals(other.to) && !from.equals(other.to))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Edge [from=" + from + ", to=" + to + ", weight=" + weight + "]";
    }
}
