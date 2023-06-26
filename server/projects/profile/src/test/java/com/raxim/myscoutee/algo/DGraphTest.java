package com.raxim.myscoutee.algo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.Graph;

public class DGraphTest extends AbstractAlgoTest {

    @Test
    public void shouldLoadDGraph() throws AlgoLoadException {
        Graph graph = load("algo/graph.json");

        DGraph dGraph1 = new DGraph();
        dGraph1.addAll(graph.getEdges());

        assertEquals(2, dGraph1.size());
    }
}
