/*package com.raxim.myscoutee.algo;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.raxim.myscoutee.algo.dto.CGraph;
import com.raxim.myscoutee.algo.dto.CNode;
import com.raxim.myscoutee.algo.dto.Edge;

public class BCTree extends CTree {
    private final Map<String, PriorityQueue<CNode>> nodesOrderedByType;
    private final List<String> types;
    private int currentIdx;

    public BCTree(CGraph cGraph, List<String> types) {
        super(cGraph);

        this.types = types;
        this.nodesOrderedByType = new ConcurrentHashMap<>();

        cGraph.forEach(cNode -> {
            if (!nodesOrderedByType.containsKey(cNode.getNode().getType())) {

                PriorityQueue<CNode> nodes = new PriorityQueue<>(
                        Comparator.comparing(CNode::getWeight).reversed().thenComparing(CNode::getNode));

                nodesOrderedByType.put(cNode.getNode().getType(), nodes);
            }

            nodesOrderedByType.get(cNode.getNode().getType()).add(cNode);
        });
    }

    @Override
    public Iterator<Edge> iterator() {
        return new CTreeIterator(this);
    }

    public CNode getNode(String key) {
        return super.getNode(key);
    }

    public CNode poll() {
        CNode cNode = nodesOrderedByType.get(types.get(currentIdx)).poll();
        currentIdx++;
        return cNode;
    }

    public boolean isEmpty() {
        return nodesOrderedByType.entrySet().stream().anyMatch(entry -> entry.getValue().isEmpty());
    }
}*/
