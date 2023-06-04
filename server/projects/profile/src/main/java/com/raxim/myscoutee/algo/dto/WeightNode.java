package com.raxim.myscoutee.algo.dto;

public class WeightNode {
    private final String id;
    private final long weight;

    public WeightNode(String id, long weight) {
        this.id = id;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public long getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return "Item [id=" + id + ", weight=" + weight + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Long.hashCode(weight);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        WeightNode other = (WeightNode) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
}
