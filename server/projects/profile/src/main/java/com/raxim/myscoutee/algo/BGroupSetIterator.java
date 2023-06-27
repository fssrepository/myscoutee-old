package com.raxim.myscoutee.algo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingDeque;

import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.exception.NodeTypeMissingError;

/*
 * BGroupSet takescare of the type of the nodes also, not just the weight of the edge!
 * It should be extended with shadow likes (that likes can be used to put same people together again
 * balanced and shadow -> BSHGroupSetIterator - the change should be in the GroupSetIterator which balanced iterator depends on)
 */
/*public class BGroupSetIterator implements Iterator<CGroup> {
    private ConcurrentMap<String, LinkedBlockingDeque<Node>> nodesByType;
    private volatile int partition = -1;
    private CGroup currGroup = null;

    private CGroup bGroup = new CGroup(new ArrayList<>());

    private final BGroupSet bGroupSet;
    private final GroupSetIterator groupSetIterator;

    public BGroupSetIterator(final BGroupSet groupSet) {
        this.bGroupSet = groupSet;
        this.groupSetIterator = new GroupSetIterator(groupSet);
        this.nodesByType = new ConcurrentHashMap<>();
    }

    @Override
    public boolean hasNext() {
        bGroup = new CGroup(new ArrayList<>());

        List<String> types = bGroupSet.getTypes();
        if (types == null || types.size() < 2)
            return false;

        Iterator<String> itTypes = types.iterator();
        String type = itTypes.next();

        while (bGroup.getNodes().size() < bGroupSet.getRange().getMax()
                && groupSetIterator.hasNext()) {

            if (currGroup == null) {
                currGroup = groupSetIterator.next();
            }

            int lPartition = currGroup.getPartition();
            if (partition == -1) {
                partition = lPartition;
                bGroup.setPartition(partition);
            } else if (partition != lPartition) {
                this.nodesByType = new ConcurrentHashMap<>();

                if (bGroup.getNodes().size() > 0
                        && bGroup.getNodes().size() % types.size() == 0) {
                    break;
                } else {
                    partition = lPartition;
                    bGroup.setPartition(partition);
                }
            }

            for (Node gNode : currGroup.getNodes()) {
                if (gNode.getType() == null) {
                    throw new NodeTypeMissingError();
                }

                if (!nodesByType.containsKey(gNode.getType())) {
                    nodesByType.put(gNode.getType(), new LinkedBlockingDeque<Node>());
                }
                nodesByType.get(gNode.getType()).add(gNode);

                while (bGroup.getNodes().size() < bGroupSet.getRange().getMax()) {
                    if (!itTypes.hasNext()) {
                        itTypes = types.iterator();
                    }

                    if (!nodesByType.containsKey(type)
                            || nodesByType.get(type).peek() == null) {
                        break;
                    }

                    Node node = nodesByType.get(type).poll();
                    bGroup.getNodes().add(node);

                    type = itTypes.next();
                }
            }

            currGroup = null;
        }

        if (bGroup.getNodes().size() >= bGroupSet.getRange().getMin()) {
            if (bGroup.getNodes().size() % types.size() != 0) {
                int lastIdx = bGroup.getNodes().size() - 1;
                bGroup.getNodes().remove(lastIdx);
            }
        } else {
            if (groupSetIterator.hasNext()) {
                hasNext();
            }
        }

        return bGroup.getNodes().size() >= bGroupSet.getRange().getMin()
                && bGroup.getNodes().size() % types.size() == 0;
    }

    @Override
    public CGroup next() {
        return bGroup;
    }
}*/