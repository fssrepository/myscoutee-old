package com.raxim.myscoutee.common.merge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class MergingSetTest {

    class Node implements Mergeable<Node>, Comparable<Node> {
        private final Set<String> edges;

        public Node() {
            this.edges = new HashSet<>();
        }

        @Override
        public boolean canMerge(Node other) {
            Set<String> merged = new HashSet<>(edges);
            merged.retainAll(other.edges);
            return !merged.isEmpty();
        }

        @Override
        public void merge(Node node) {
            this.edges.addAll(node.edges);
        }

        public void add(String edge) {
            this.edges.add(edge);
        }

        @Override
        public int compareTo(Node arg0) {
            return Integer.compare(this.hashCode(), arg0.hashCode());
        }

    }

    @Test
    public void shouldMerge() {
        MergingSet<Node> mergedSet = new MergingSet<>();

        Node n1 = new Node();
        n1.add("e1");
        n1.add("e2");
        mergedSet.add(n1);

        Node n2 = new Node();
        n2.add("e3");
        n2.add("e4");
        mergedSet.add(n2);

        assertEquals(2, mergedSet.size());

        Node n3 = new Node();
        n3.add("e3");
        n3.add("e2");
        mergedSet.add(n3);

        assertEquals(1, mergedSet.size());
        assertEquals(4, mergedSet.first().edges.size());
    }
}
