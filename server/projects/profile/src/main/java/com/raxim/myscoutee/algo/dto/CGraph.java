package com.raxim.myscoutee.algo.dto;

import java.util.Arrays;
import java.util.List;

import com.raxim.myscoutee.common.merge.Mergeable;
import com.raxim.myscoutee.common.merge.MergingSet;

/*
 * connected graph
 */
public class CGraph extends MergingSet<CNode> implements Mergeable<CGraph>, Comparable<CGraph> {
    public void addAll(List<Edge> edges) {
        edges.forEach(edge -> {
            add(edge);

            Edge edgeReversed = new Edge(edge.getTo(), edge.getFrom(), edge.getWeight());
            add(edgeReversed);
        });
    }

    public void add(Edge edge) {
        CNode aNode = new CNode(edge.getFrom());
        aNode.add(edge);

        super.add(aNode);
    }

    @Override
    public boolean canMerge(CGraph other) {
        return super.stream().anyMatch(cNode -> other.contains(cNode));
    }

    @Override
    public void merge(CGraph node) {
        CNode[] cNodes = node.toArray(new CNode[node.size()]);
        super.addAll(Arrays.asList(cNodes));
    }

    @Override
    public int compareTo(CGraph arg0) {
        return Integer.compare(this.hashCode(), arg0.hashCode());
    }

    @Override
    public String toString() {
        CNode[] cNodes = super.toArray(new CNode[super.size()]);
        return "CGraph [nodesOrdered=" + cNodes + "]";
    }
}
