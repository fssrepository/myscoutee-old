package com.raxim.myscoutee.algo;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.raxim.myscoutee.algo.dto.CNode;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;

public class CTreeIterator implements Iterator<Edge> {

    public static final String DEFAULT_TYPE = "d";

    private final CTree cTree;

    private final Map<String, PriorityQueue<CNode>> nodesOrderedByType;
    private final List<String> types;

    private final Set<String> visited = new HashSet<>();

    private final Set<Node> used = new HashSet<>();

    private int currentIdx = 0;
    private Edge currEdge;

    public CTreeIterator(final CTree cTree, List<String> types) {
        this.cTree = cTree;
        if (!types.isEmpty()) {
            this.types = types;
        } else {
            this.types = List.of(DEFAULT_TYPE);
        }
        this.nodesOrderedByType = new ConcurrentHashMap<>();

        for (String type : this.types) {
            PriorityQueue<CNode> nodes = new PriorityQueue<>(
                    Comparator.comparing(CNode::getWeight).reversed().thenComparing(CNode::getNode));

            nodesOrderedByType.put(type, nodes);
        }
    }

    @Override
    public boolean hasNext() {
        String type = types.get(currentIdx);
        if (cTree.isEmpty()
                && nodesOrderedByType.get(type).isEmpty()) {
            return false;
        }

        while (!cTree.isEmpty()
                && nodesOrderedByType.get(type).isEmpty()) {
            CNode cNode;
            Node node;
            do {
                cNode = cTree.poll();
                node = cNode.getNode();
            } while (!cTree.isEmpty() && used.contains(node));

            if (cTree.isEmpty()) {
                return false;
            }

            if (node.getType() == null) {
                node = new Node(node.getId(), DEFAULT_TYPE);
            }
            if (type.equals(node.getType())) {
                String nodeFrom = node.getId();
                visited.add(nodeFrom);
            }

            nodesOrderedByType.get(node.getType()).add(cNode);
        }

        CNode cNode;
        do {
            if (nodesOrderedByType.get(type).isEmpty()) {
                break;
            }

            cNode = nodesOrderedByType.get(type).peek();
            currEdge = cNode.poll();
            if (currEdge != null && used.contains(currEdge.getFrom())) {
                break;
            }

            nodesOrderedByType.get(type).remove(cNode);
            if (cNode.getDegree() > 0) {
                nodesOrderedByType.get(type).add(cNode);
            }
        } while (currEdge != null
                && (visited.contains(currEdge.getTo().getId()) || used.contains(currEdge.getTo()))
                && !nodesOrderedByType.get(type).isEmpty());

        if ((currEdge == null || visited.contains(currEdge.getTo().getId()))
                && (!cTree.isEmpty() || !nodesOrderedByType.get(type).isEmpty())) {
            currentIdx = ++currentIdx % types.size();
            hasNext();
        }

        if (currEdge != null) {
            if (used.contains(currEdge.getTo()) || used.contains(currEdge.getFrom())) {
                hasNext();
            } else if (currEdge.isIgnored() && !visited.contains(currEdge.getTo().getId())
                    && (!cTree.isEmpty() || !nodesOrderedByType.get(type).isEmpty())) {
                next();
                hasNext();
            }
        }

        return (currEdge != null && !visited.contains(currEdge.getTo().getId()))
                && (!cTree.isEmpty() || !nodesOrderedByType.get(type).isEmpty());
    }

    @Override
    public Edge next() {
        if (currEdge != null) {
            if (!currEdge.isIgnored()) {
                visited.add(currEdge.getFrom().getId());
                visited.add(currEdge.getTo().getId());
            } else {
                visited.remove(currEdge.getFrom().getId());
            }
            currentIdx = ++currentIdx % types.size();
        }

        loadNodeTo();
        if (!currEdge.isIgnored()) {
            System.out.println(currEdge);
        }
        return currEdge;
    }

    private void loadNodeTo() {
        CNode cNodeTo = cTree.getNode(currEdge.getTo().getId());

        if (cNodeTo != null) {
            Node node = cNodeTo.getNode();
            if (node.getType() == null) {
                node = new Node(node.getId(), DEFAULT_TYPE);
            }
            nodesOrderedByType.get(node.getType()).add(cNodeTo);
            cTree.remove(cNodeTo);
        }
    }

    public List<String> getTypes() {
        return types;
    }

    public Set<Node> getUsed() {
        return used;
    }

}
