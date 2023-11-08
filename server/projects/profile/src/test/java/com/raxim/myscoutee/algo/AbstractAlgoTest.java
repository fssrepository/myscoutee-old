package com.raxim.myscoutee.algo;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Graph;
import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.common.util.FileUtil;
import com.raxim.myscoutee.profile.data.document.mongo.Member;

public abstract class AbstractAlgoTest {

    public Graph load(String fileGraph, ObjectMapper objectMapper) throws AlgoLoadException {
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

    public Graph load(String fileGraph) throws AlgoLoadException {
        return load(fileGraph, new ObjectMapper());
    }

    public boolean matchAll(List<Edge> edges, List<List<String>> ids) {
        boolean allEdgesMatched = IntStream.range(0, Math.min(edges.size(), ids.size()))
                .allMatch(i -> ids.get(i).get(0).equals(edges.get(i).getFrom().getId())
                        && ids.get(i).get(1).equals(edges.get(i).getTo().getId()));
        return allEdgesMatched;
    }

    public boolean matchAll(Set<Member> members, List<UUID> uuids) {
        boolean allEdgesMatched = uuids.stream().allMatch(
                id -> members.stream().anyMatch(
                        member -> member.getProfile().getId().equals(id)));
        return allEdgesMatched;
    }

    public boolean matchAll(Set<Member> members, Set<Member> pMembers) {
        boolean allEdgesMatched = pMembers.stream().allMatch(
                pMember -> members.stream().anyMatch(
                        member -> pMember.getStatus().equals(member.getStatus())
                                && pMember.getProfile().getId().equals(member.getProfile().getId())));
        return allEdgesMatched;
    }

    public List<Edge> getEdges(String fileName) throws AlgoLoadException {
        return getEdges(fileName, List.of());
    }

    public List<Edge> getEdges(String fileName, List<Set<Edge>> ignoredEdges) throws AlgoLoadException {
        Graph graph = load(fileName);

        DGraph dGraph = new DGraph();
        dGraph.addAll(graph.getEdges());

        List<Edge> edges = new ArrayList<>();
        dGraph.stream().map(cGraph -> new CTree(cGraph)).forEach(cTree -> {
            cTree.getIgnoredEdges().addAll(ignoredEdges);
            cTree.forEach(edge -> {
                edges.add(edge);
            });
        });
        return edges;
    }

    public <T> T loadJson(Object obj, String jsonFile, Class<T> targetClass, ObjectMapper objectMapper)
            throws IOException {
        Path filePath = FileUtil.getResourcePath(obj, jsonFile);
        String jsonData = new String(Files.readAllBytes(filePath));

        return objectMapper.readValue(jsonData, targetClass);
    }

    public String jsonToString(Object object, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (IOException e) {
            // Handle the exception appropriately
            e.printStackTrace();
            return null;
        }
    }
}
