package com.raxim.myscoutee.algo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;

import org.junit.jupiter.api.Test;

import com.raxim.myscoutee.algo.dto.CGraph;
import com.raxim.myscoutee.algo.dto.DGraph;
import com.raxim.myscoutee.algo.dto.Edge;
import com.raxim.myscoutee.algo.dto.Graph;

public class DGraphTest extends AbstractAlgoTest {

    @Test
    public void shouldLoadDGraph() throws AlgoLoadException {
        Graph graph = load("algo/graph.json");

        DGraph dGraph1 = new DGraph();
        dGraph1.addAll(graph.getEdges());

        assertEquals(2, dGraph1.size());
    }

    @Test
    public void shouldGetMaxFlow() throws AlgoLoadException {
        Graph graph = load("algo/graph.json");

        DGraph dGraph1 = new DGraph();
        dGraph1.addAll(graph.getEdges());

        assertEquals(2, dGraph1.size());

        Iterator<CGraph> itDGraph = dGraph1.iterator();
        CGraph cGraph1 = itDGraph.next();

        Iterator<Edge> itEdge1 = cGraph1.iterator();
        assertTrue(itEdge1.hasNext());
        Edge edge1 = itEdge1.next();
        assertEquals(15, edge1.getWeight());

        assertTrue(itEdge1.hasNext());
        Edge edge2 = itEdge1.next();
        assertEquals(12, edge2.getWeight());
        assertEquals("8", edge2.getTo().getId());
    }
}
