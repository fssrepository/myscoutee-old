package com.raxim.myscoutee.algo.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Node;

public class EdgeGenerator implements IGenerator<Edge> {

    private RandomPair randomPair = new RandomPair();
    private Random random = new Random();

    private final List<Node> nodes;

    public EdgeGenerator(Set<Node> nodeSet) {
        Set<Node> nodeUSet = Collections.unmodifiableSet(nodeSet);
        nodes = new ArrayList<>(nodeUSet);
    }

    @Override
    public Set<Edge> generate(Integer num) {
        Set<Edge> edges = new HashSet<>();
        while (edges.size() < num) {
            List<Integer> idxPair = randomPair.nextInt(0, nodes.size());

            int weight = random.nextInt(Byte.MAX_VALUE);

            Edge edge = new Edge(nodes.get(idxPair.get(0)), nodes.get(idxPair.get(1)), weight);
            edges.add(edge);
        }
        return edges;
    }
    
}
