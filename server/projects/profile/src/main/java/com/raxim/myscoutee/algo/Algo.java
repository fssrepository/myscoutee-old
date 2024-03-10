package com.raxim.myscoutee.algo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.FGraph;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.Range;

//can be Runnable
public class Algo {

    public List<Set<Node>> run(FGraph fGraph, List<String> types, Range range) {
        DGraph dGraph = new DGraph();
        dGraph.addAll(fGraph.getEdges());

        List<BCTree> bcTrees = dGraph.stream().map(cGraph -> {
            CTree cTree = new CTree(cGraph, types,
                    fGraph.getIgnoredEdges());
            return new BCTree(cTree, range, fGraph.getIgnoredNodes(), true);
        }).toList();

        List<Set<Node>> nodesByGroup = new ArrayList<>();
        bcTrees.forEach(bcTree -> bcTree.forEach(cGroup -> {
            Set<Node> nodes = cGroup.stream().collect(Collectors.toSet());
            nodesByGroup.add(nodes);
        }));

        return nodesByGroup;
    }
}
