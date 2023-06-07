package com.raxim.myscoutee.algo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Set;

import com.raxim.myscoutee.algo.dto.GroupAlgo;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.NodeInfo;
import com.raxim.myscoutee.algo.dto.WeightNode;

public class GroupSetIterator implements Iterator<GroupAlgo> {

    private final PriorityQueue<NodeInfo> nodeTree = new PriorityQueue<>(
            Comparator.comparing(NodeInfo::getWeight).reversed().thenComparing(NodeInfo::getId));
    private final Set<String> visited = new HashSet<>();

    private boolean isCollect = false;
    private GroupAlgo group = new GroupAlgo(new ArrayList<>());
    private volatile int partition = -1;
    
    private final GroupSet groupSet;

    public GroupSet getGroupSet() {
        return groupSet;
    }

    public GroupSetIterator(final GroupSet groupSet) {
        this.groupSet = groupSet;
    }

    @Override
    public boolean hasNext() {
        NodeInfo nodeInfo;

        do {
            nodeInfo = groupSet.getNodeRepository().getNodeForest().peek();

            if (nodeInfo != null) {
                WeightNode wn = nodeInfo.peek();
                while (wn != null && visited.contains(wn.getId())) {
                    nodeInfo.poll();

                    if (nodeInfo.getDegree() == 0) {
                        break;
                    }
                    wn = nodeInfo.peek();
                }
                if (nodeInfo.getDegree() == 0) {
                    groupSet.getNodeRepository().getNodeForest().poll();
                }
            }
        } while (!groupSet.getNodeRepository().getNodeForest().isEmpty() && nodeInfo != null
                && nodeInfo.getDegree() == 0);

        NodeInfo tNodeInfo;

        do {
            tNodeInfo = nodeTree.peek();

            if (tNodeInfo != null) {
                WeightNode wn = tNodeInfo.peek();
                while (wn != null && visited.contains(wn.getId())) {
                    tNodeInfo.poll();

                    if (tNodeInfo.getDegree() == 0) {
                        break;
                    }
                    wn = tNodeInfo.peek();
                }
                if (tNodeInfo.getDegree() == 0) {
                    nodeTree.poll();
                }
            }
        } while (!nodeTree.isEmpty() && tNodeInfo != null && tNodeInfo.getDegree() == 0);

        return nodeInfo != null && nodeInfo.getDegree() > 0
                && (isCollect
                        || groupSet.getNodeRepository().getNodes().size() - visited.size() >= groupSet.getRange()
                                .getMin());
    }

    @Override
    public GroupAlgo next() {

        if (!isCollect) {
            group = new GroupAlgo(new ArrayList<>());
            group.setPartition(partition);
        }

        if (nodeTree.isEmpty()) {
            NodeInfo nodeInfo = groupSet.getNodeRepository().getNodeForest().peek();
            nodeTree.add(nodeInfo);

            if (!visited.contains(nodeInfo.getId())) {
                Node node = groupSet.getNodeRepository().getNodes().get(nodeInfo.getId());
                group.getNodes().add(node);
                visited.add(nodeInfo.getId());
            }

            group.setPartition(++partition);
        }

        while (group.getNodes().size() < groupSet.getRange().getMax()) {
            for (Node node : group.getNodes()) {
                if (groupSet.getNodeRepository().getNodeInfos().containsKey(node.getId())) {
                    NodeInfo nodeInfo = groupSet.getNodeRepository().getNodeInfos().get(node.getId());

                    WeightNode wn = nodeInfo.peek();
                    while (wn != null && visited.contains(wn.getId())) {
                        nodeInfo.poll();

                        if (nodeInfo.getDegree() == 0) {
                            break;
                        }
                        wn = nodeInfo.peek();
                    }

                    if (nodeInfo.getDegree() == 0) {
                        groupSet.getNodeRepository().getNodeForest().remove(nodeInfo);
                        continue;
                    }

                    groupSet.getNodeRepository().getNodeForest().remove(nodeInfo);
                    if (nodeInfo.getDegree() > 0) {
                        groupSet.getNodeRepository().getNodeForest().add(nodeInfo);
                    }

                    nodeTree.add(nodeInfo);

                    NodeInfo sNodeInfo = groupSet.getNodeRepository().getNodeInfos().get(wn.getId());

                    wn = sNodeInfo.peek();
                    while (wn != null && visited.contains(wn.getId())) {
                        sNodeInfo.poll();

                        if (sNodeInfo.getDegree() == 0) {
                            break;
                        }
                        wn = sNodeInfo.peek();
                    }

                    if (sNodeInfo.getDegree() == 0) {
                        groupSet.getNodeRepository().getNodeForest().remove(sNodeInfo);
                        continue;
                    }

                    groupSet.getNodeRepository().getNodeForest().remove(sNodeInfo);
                    if (sNodeInfo.getDegree() > 0) {
                        groupSet.getNodeRepository().getNodeForest().add(sNodeInfo);
                    }
                    nodeTree.add(sNodeInfo);
                }
            }

            if (nodeTree.isEmpty())
                break;

            NodeInfo nodeInfo = nodeTree.poll();

            WeightNode wn = nodeInfo.peek();
            while (wn != null && visited.contains(wn.getId())) {
                nodeInfo.poll();

                if (nodeInfo.getDegree() == 0) {
                    break;
                }
                wn = nodeInfo.peek();
            }

            if (nodeInfo.getDegree() == 0) {
                groupSet.getNodeRepository().getNodeForest().remove(nodeInfo);
                nodeTree.remove(nodeInfo);
                continue;
            }

            groupSet.getNodeRepository().getNodeForest().remove(nodeInfo);
            if (nodeInfo.getDegree() > 0) {
                groupSet.getNodeRepository().getNodeForest().add(nodeInfo);
            }

            nodeTree.remove(nodeInfo);
            if (nodeInfo.getDegree() > 0) {
                nodeTree.add(nodeInfo);
            }

            Node node = groupSet.getNodeRepository().getNodes().get(wn.getId());
            group.getNodes().add(node);
            visited.add(wn.getId());

            group.getWeight().addAndGet(wn.getWeight());

        }

        if (group.getNodes().size() < groupSet.getRange().getMin()) {

            isCollect = true;
            if (hasNext()) {
                group = next();
            }
            isCollect = false;
        }

        return group;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Remove is not supported!", null);
    }

}
