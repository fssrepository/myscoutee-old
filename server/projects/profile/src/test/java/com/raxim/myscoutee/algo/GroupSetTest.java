package com.raxim.myscoutee.algo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.junit.jupiter.api.Test;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Graph;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.algo.dto.NodeInfo;
import com.raxim.myscoutee.algo.dto.Range;
import com.raxim.myscoutee.algo.exception.NodeTypeMissingError;
import com.raxim.myscoutee.algo.generator.EdgeGenerator;
import com.raxim.myscoutee.algo.generator.IGenerator;
import com.raxim.myscoutee.algo.generator.NodeGenerator;

public class GroupSetTest extends AbstractAlgoTest {

    private final static int NODE_NUM = 1000;
    private final static int EDGE_NUM = 1000;

    @Test
    void shouldIterate() throws AlgoLoadException {
        NodeRepository nodeRepository = new NodeRepository();
        Graph graph = load("algo/graph.json");

        nodeRepository.addAll(graph.getEdges());

        NodeInfo nodeInfo = nodeRepository.getNodeForest().peek();
        assertEquals("3", nodeInfo.getId());

        ConcurrentMap<String, NodeInfo> nodeInfos = nodeRepository.getNodeInfos();
        System.out.println(nodeInfos);
        assertEquals(8, nodeInfos.size());

        assertEquals(3, nodeInfos.get("1").getDegree());
        assertEquals(2, nodeInfos.get("2").getDegree());

        GroupSet groupSet = new GroupSet(nodeRepository, new Range(2, 3));
        groupSet.forEach(group -> {
            System.out.println(group);
        });
    }

    @Test
    void shouldIterate2() throws AlgoLoadException {
        NodeRepository nodeRepository = new NodeRepository();
        Graph graph = load("algo/graph2.json");
        nodeRepository.addAll(graph.getEdges());

        NodeInfo nodeInfo = nodeRepository.getNodeForest().peek();
        assertEquals("1", nodeInfo.getId());

        ConcurrentMap<String, NodeInfo> nodeInfos = nodeRepository.getNodeInfos();
        System.out.println(nodeInfos);
        assertEquals(6, nodeInfos.size());

        assertEquals(4, nodeInfos.get("1").getDegree());
        assertEquals(2, nodeInfos.get("2").getDegree());

        Range range = new Range(2, 3);
        GroupSet groupSet = new GroupSet(nodeRepository, range);
        groupSet.forEach(edgeWr -> {
            System.out.println(edgeWr);
        });

        nodeRepository = new NodeRepository();
        graph = load("algo/graph2.json");
        nodeRepository.addAll(graph.getEdges());

        BGroupSet bGroupSet = new BGroupSet(nodeRepository, new Range(2, 4), Arrays.asList("m", "w"));

        Error error = assertThrows(NodeTypeMissingError.class, () -> {
            bGroupSet.forEach(group -> {
                System.out.println(group);
            });
        });

        System.out.println(error);
    }

    @Test
    void shouldIterate3() throws AlgoLoadException {
        NodeRepository nodeRepository = new NodeRepository();

        Graph graph = load("algo/graph3.json");
        nodeRepository.addAll(graph.getEdges());

        NodeInfo nodeInfo = nodeRepository.getNodeForest().peek();
        assertEquals("3", nodeInfo.getId());

        ConcurrentMap<String, NodeInfo> nodeInfos = nodeRepository.getNodeInfos();
        System.out.println(nodeInfos);
        assertEquals(8, nodeInfos.size());

        assertEquals(2, nodeInfos.get("1").getDegree());
        assertEquals(1, nodeInfos.get("2").getDegree());

        Range range = new Range(2, 3);
        GroupSet groupSet = new GroupSet(nodeRepository, range);
        groupSet.forEach(group -> {
            System.out.println(group);
        });

        nodeRepository = new NodeRepository();
        graph = load("algo/graph3.json");
        nodeRepository.addAll(graph.getEdges());

        BGroupSet bGroupSet = new BGroupSet(nodeRepository, range, Arrays.asList("m", "w"));
        bGroupSet.forEach(group -> {
            System.out.println(group);
        });
    }

    @Test
    void shouldGenIterate() {
        NodeRepository nodeRepository = new NodeRepository();

        IGenerator<Node> nodeGenerator = new NodeGenerator();
        Set<Node> nodes = nodeGenerator.generate(NODE_NUM);

        IGenerator<Edge> edgeGenerator = new EdgeGenerator(nodes);
        Set<Edge> edges = edgeGenerator.generate(EDGE_NUM);
        nodeRepository.addAll(edges);

        ConcurrentMap<String, NodeInfo> nodeInfos = nodeRepository.getNodeInfos();
        // System.out.println(nodeInfos);

        GroupSet groupSet = new GroupSet(nodeRepository, new Range(2, 12));

        Set<Node> visited = new HashSet<>();
        List<Node> lVisited = new ArrayList<>();

        long startTime = System.currentTimeMillis();
        // Code or operation to be measured

        groupSet.forEach(group -> {
            // visited.addAll(group.getNodes());
            lVisited.addAll(group.getNodes());
            // System.out.println(group);
            // System.out.println(group.nodes.size());
            // System.out.println(visited.size());
            System.out.println(lVisited.size());
        });

        long endTime = System.currentTimeMillis();

        long elapsedTime = endTime - startTime;
        System.out.println("Elapsed time: " + elapsedTime / 1000 + " seconds");
    }
}
