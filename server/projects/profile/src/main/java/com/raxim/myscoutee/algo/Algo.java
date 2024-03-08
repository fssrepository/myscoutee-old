package com.raxim.myscoutee.algo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.dto.CGroup;
import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;

public class Algo {

    public Set<Node> runPriority(FGraph fGraph, List<String> types, Range range) {
        DGraph dGraph = new DGraph();
        dGraph.addAll(fGraph.getEdges());

        List<BCTree> bcTrees = dGraph.stream().map(cGraph -> {
            CTree cTree = new CTree(cGraph, types, fGraph.getIgnoredEdges());
            return new BCTree(cTree, range, fGraph.getIgnoredNodes(), true);
        }).toList();

        Iterator<BCTree> itBCTree = bcTrees.iterator();
        if (itBCTree.hasNext()) {
            BCTree bcTree = itBCTree.next();
            BCTreeIterator itCGroup = (BCTreeIterator) bcTree.iterator();
            if (itCGroup.hasAnyNext()) {
                CGroup cGroup = itCGroup.next();
                return cGroup.stream().collect(Collectors.toSet());
            }
        }

        return new HashSet<>();
    }

    public List<List<Node>> runRandom(FGraph fGraph, List<String> types, Range range) {
        DGraph dGraph = new DGraph();
        dGraph.addAll(fGraph.getEdges());
        
        List<BCTree> bcTrees = dGraph.stream().map(cGraph -> {
            CTree cTree = new CTree(cGraph, types,
                    fGraph.getIgnoredEdges());
            return new BCTree(cTree, range);
        }).toList();

        List<List<Node>> membersByGroup = new ArrayList<>();
        bcTrees.forEach(bcTree -> bcTree.forEach(cGroup -> {
            List<Node> nodes = cGroup.stream().toList();
            membersByGroup.add(nodes);
        }));

        return membersByGroup;
    }
}
