package com.raxim.myscoutee.algo.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.profile.util.AppConstants;

public class EdgeGenerator implements IGenerator<Edge> {
    private Random random = new Random();

    private final Map<String, List<Node>> nodesByType = new HashMap<>();

    public EdgeGenerator(Set<Node> nodeSet) {
        Set<Node> nodeUSet = Collections.unmodifiableSet(nodeSet);
        nodeUSet.forEach(node -> {
            if (!nodesByType.containsKey(node.getType())) {
                nodesByType.put(node.getType(), new ArrayList<>());
            }

            nodesByType.get(node.getType()).add(node);
        });
    }

    @Override
    public Set<Edge> generate(Integer num) {
        Set<Edge> edges = new HashSet<>();
        while (edges.size() < num) {
            int ixdMan = random.nextInt(nodesByType.get(AppConstants.MAN).size());
            int ixdWoman = random.nextInt(nodesByType.get(AppConstants.WOMAN).size());

            int weight = random.nextInt(Byte.MAX_VALUE);

            Edge edge = new Edge(nodesByType.get(AppConstants.MAN).get(ixdMan),
                    nodesByType.get(AppConstants.WOMAN).get(ixdWoman), weight);
            edges.add(edge);
        }
        return edges;
    }

}
