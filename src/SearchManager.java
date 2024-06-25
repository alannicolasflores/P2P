import java.net.*;
import java.io.*;
import java.util.*;

public class SearchManager {
    private Nodo nodo;
    private Index index;
    private Set<String> visitedNodes;
    private Set<String> bestNeighbors;
    private final int MAX_DEPTH = 5;
    private boolean isLocal;

    public SearchManager(Nodo nodo, Index index, boolean isLocal) {
        this.nodo = nodo;
        this.index = index;
        this.visitedNodes = new HashSet<>();
        this.bestNeighbors = chooseBestNeighbors();
        this.isLocal = isLocal;
    }

    private Set<String> chooseBestNeighbors() {
        return new HashSet<>(Arrays.asList("Node1", "Node2", "Node3"));
    }

    public void searchFile(String fileName) {
        if (index.searchFile(fileName) != null) {
            System.out.println("Archivo encontrado localmente.");
        } else {
            System.out.println("Buscando archivo en la red...");
            performDirectedBFS(fileName);
        }
    }

    private void performDirectedBFS(String fileName) {
        Queue<NodeSearchState> queue = new LinkedList<>();
        queue.add(new NodeSearchState(nodo.getNodeName(), 0));

        while (!queue.isEmpty()) {
            NodeSearchState currentState = queue.poll();
            if (currentState.depth < MAX_DEPTH) {
                for (String neighbor : bestNeighbors) {
                    if (!visitedNodes.contains(neighbor)) {
                        visitedNodes.add(neighbor);
                        if (isLocal) {
                            new MulticastSender(nodo, true, nodo.getMulticastPort()).sendRequest(fileName);
                        } else {
                            new MulticastSender(nodo, false, nodo.getMulticastPort()).sendRequest(fileName);
                        }
                        queue.add(new NodeSearchState(neighbor, currentState.depth + 1));
                    }
                }
            }
        }
    }

    private class NodeSearchState {
        String node;
        int depth;

        NodeSearchState(String node, int depth) {
            this.node = node;
            this.depth = depth;
        }
    }
}
