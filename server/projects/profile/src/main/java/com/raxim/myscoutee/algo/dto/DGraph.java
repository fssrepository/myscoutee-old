package com.raxim.myscoutee.algo.dto;

import java.util.List;

import com.raxim.myscoutee.common.merge.MergingSet;

/*
 * disjoint graph
 */
public class DGraph extends MergingSet<CGraph> {
    private int counter;

    public void addAll(List<Edge> edges) {
        edges.forEach(edge -> {
            add(edge);
        });
    }

    public void add(Edge edge) {
        System.out.println(counter++);

        CGraph aTree = new CGraph();
        aTree.add(edge);

        Edge edgeReversed = new Edge(edge.getTo(), edge.getFrom(), edge.getWeight(), edge.isIgnored());
        aTree.add(edgeReversed);

        super.add(aTree);
    }
}
