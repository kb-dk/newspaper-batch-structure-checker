package dk.statsbiblioteket.newspaper.eventhandlers.filter;

import java.util.Arrays;
import java.util.List;

import dk.statsbiblioteket.newspaper.eventhandlers.TreeEventHandler;

public class IncludeNodeFilter extends NodeFilter {
    private final List<NodeType> includeNodeType;

    public IncludeNodeFilter(List<NodeType> allowedNodes, TreeEventHandler leafHandler) {
        super(leafHandler);
        this.includeNodeType = allowedNodes;
    }

    public IncludeNodeFilter(NodeType allowedNode, TreeEventHandler leafHandler) {
        super(leafHandler);
        this.includeNodeType = Arrays.asList(new NodeType[]{allowedNode});
    }

    @Override
    public boolean shouldIgnoreNode() {
        return includeNodeType.contains(currentNode.getType());
    }
}
