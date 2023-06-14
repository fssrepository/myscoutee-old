package com.raxim.myscoutee.algo.generator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.raxim.myscoutee.algo.dto.Node;

public class NodeGenerator implements IGenerator<Node> {

    private Random random = new Random();

    @Override
    public Set<Node> generate(Integer num) {
        Set<Node> nodeSet = new HashSet<>();
        while (nodeSet.size() < num) {
            int nodeId = random.nextInt(Integer.MAX_VALUE);
            nodeSet.add(new Node(String.valueOf(nodeId), "m"));
        }
        return nodeSet;
    }

}
