package com.raxim.myscoutee.algo.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;

public class GeneratorTest {

    private final static int NODE_NUM = 5;
    private final static int EDGE_NUM = 4;

    @Test
    @DisplayName("Should EdgeGenerator Use All Nodes")
    public void shouldEdgeGeneratorUseAllNodes() {
        IGenerator<Node> nodeGenerator = new NodeGenerator();
        Set<Node> nodes = nodeGenerator.generate(NODE_NUM);

        IGenerator<Edge> edgeGenerator = new EdgeGenerator(nodes);
        Set<Edge> edges = edgeGenerator.generate(EDGE_NUM);

        Set<String> nodesWeighted = new HashSet<>();
        for (Edge edge : edges) {
            nodesWeighted.add(edge.getFrom().getId());
            nodesWeighted.add(edge.getTo().getId());
        }

        System.out.println("nodes");
        System.out.println(nodes);
        System.out.println();
        System.out.println("edges");
        System.out.println(edges);

        assertEquals(NODE_NUM, nodesWeighted.size());
    }
}
