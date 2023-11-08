package com.raxim.myscoutee.algo.generator;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.raxim.myscoutee.algo.dto.Node;
import com.raxim.myscoutee.profile.util.AppConstants;

public class NodeGenerator implements IGenerator<Node> {

    private Random random = new Random();

    @Override
    public Set<Node> generate(Integer num) {
        Set<Node> nodeSet = new HashSet<>();
        while (nodeSet.size() < num) {
            int nodeId = random.nextInt(Integer.MAX_VALUE);
            boolean isMan = random.nextBoolean();
            nodeSet.add(new Node(String.valueOf(nodeId), isMan ? AppConstants.MAN : AppConstants.WOMAN));
        }
        return nodeSet;
    }

}
