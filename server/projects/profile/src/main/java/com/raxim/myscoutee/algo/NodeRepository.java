package com.raxim.myscoutee.algo;

import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.NodeInfo;
import com.raxim.myscoutee.algo.dto.WeightNode;

public class NodeRepository {
    private final ConcurrentMap<String, Node> nodes;
    private final PriorityQueue<NodeInfo> nodeForest;
    private final ConcurrentMap<String, NodeInfo> nodeInfos;

    public NodeRepository() {
        this.nodes = new ConcurrentHashMap<>();
        this.nodeForest = new PriorityQueue<>(
                Comparator.comparing(NodeInfo::getWeight).reversed().thenComparing(NodeInfo::getId));
        this.nodeInfos = new ConcurrentHashMap<>();
    }

    public void addAll(Collection<Edge> edges) {
        edges.forEach(edge -> {
            this.nodes.putIfAbsent(edge.getFrom().getId(), edge.getFrom());
            this.nodes.putIfAbsent(edge.getTo().getId(), edge.getTo());

            add(edge.getFrom().getId(), edge.getTo().getId(), edge.getWeight());
            add(edge.getTo().getId(), edge.getFrom().getId(), edge.getWeight());
        });

        this.nodeInfos.forEach((i, nodeInfo) -> {
            this.nodeForest.add(nodeInfo);
        });
    }

    public void add(String id, String toId, long weight) {
        if (!this.nodeInfos.containsKey(id)) {
            PriorityQueue<WeightNode> weightNodes = new PriorityQueue<>(
                    Comparator.comparing(WeightNode::getWeight).reversed());
            NodeInfo nodeInfo = new NodeInfo(id, weightNodes);
            this.nodeInfos.put(id, nodeInfo);
        }

        NodeInfo nodeInfo = this.nodeInfos.get(id);

        WeightNode weightNode = new WeightNode(toId, weight);
        nodeInfo.add(weightNode);
    }

    public ConcurrentMap<String, Node> getNodes() {
        return nodes;
    }

    public PriorityQueue<NodeInfo> getNodeForest() {
        return this.nodeForest;
    }

    public ConcurrentMap<String, NodeInfo> getNodeInfos() {
        return nodeInfos;
    }
}
