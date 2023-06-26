package com.raxim.myscoutee.algo;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import com.raxim.myscoutee.algo.dto.CGraph;
import com.raxim.myscoutee.algo.dto.CNode;
import com.raxim.myscoutee.algo.dto.Edge;

public class CGraphIterator implements Iterator<Edge> {

    private final CGraph cGraph;

    private final PriorityQueue<CNode> nodesOrdered = new PriorityQueue<>(
            Comparator.comparing(CNode::getWeight).reversed().thenComparing(CNode::getNode));
    private final Set<Edge> visited = new HashSet<>();

    private Edge currEdge;

    public CGraphIterator(final CGraph cGraph) {
        this.cGraph = cGraph;
    }

    @Override
    public boolean hasNext() {
        if (nodesOrdered.isEmpty()) {
            CNode cNode = cGraph.getNodesOrdered().poll();
            nodesOrdered.add(cNode);
        }

        CNode cNode = nodesOrdered.peek();
        currEdge = cNode.poll();

        while (currEdge != null && visited.contains(currEdge)) {
            currEdge = cNode.poll();
        }

        nodesOrdered.remove(cNode);
        if (cNode.getDegree() > 0) {
            nodesOrdered.add(cNode);
        }

        if (currEdge == null
                && (!cGraph.isEmpty() || !nodesOrdered.isEmpty())) {
            hasNext();
        }

        return !cGraph.isEmpty() || !nodesOrdered.isEmpty();
    }

    @Override
    public Edge next() {
        if (currEdge != null && !visited.contains(currEdge)) {
            visited.add(currEdge);
        }

        ConcurrentMap<String, CNode> nodes = cGraph.getNodes();
        if (nodes.containsKey(currEdge.getTo().getId())) {
            CNode cNodeTo = cGraph.getNodes().get(currEdge.getTo().getId());
            nodesOrdered.add(cNodeTo);
        }
        return currEdge;
    }

}
