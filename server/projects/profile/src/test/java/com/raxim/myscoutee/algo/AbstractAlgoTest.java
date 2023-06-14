package com.raxim.myscoutee.algo;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Graph;
import com.raxim.myscoutee.algo.dto.Node;

public abstract class AbstractAlgoTest {
    private final ObjectMapper objectMapper;

    public AbstractAlgoTest() {
        objectMapper = new ObjectMapper();
    }

    public Graph load(String fileGraph) throws AlgoLoadException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileGraph);
        GraphData graph;
        try {
            graph = objectMapper.readValue(inputStream, GraphData.class);
        } catch (IOException e) {
            System.out.println(e);
            throw new AlgoLoadException();
        }

        Map<String, Node> nodeMap = graph.getNodes().stream()
                .collect(Collectors.toMap(GraphData.Node::getId, node -> new Node(node.getId(), node.getType())));

        List<Edge> edges = graph.getEdges().stream()
                .map(edge -> new Edge(nodeMap.get(edge.getFrom()), nodeMap.get(edge.getTo()), edge.getWeight()))
                .collect(Collectors.toList());
        return new Graph(edges, nodeMap);
    }
}
