package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
 * STUB IMPLEMENTATION !!!
 */
public class FilmNodeChecker extends AbstractNodeChecker {

    private TreeNodeState state;
    private ResultCollector resultCollector;

    public FilmNodeChecker(TreeNodeState state, ResultCollector resultCollector) {
        this.state = state;
        this.resultCollector = resultCollector;
    }

    /**
     * Currently just a stub that prints some information.
     */
    @Override
    public void doCheck() {
        System.out.println("-----------------------------------------------------------");
        System.out.println("Check for " + name);
        System.out.print("Child Nodes [");
        for (String childNode: childNodes) {
            System.out.print(childNode + ",");
        }
        System.out.println("]");
        System.out.print("Attributes [");
        for (String attribute: attributes) {
            System.out.print(attribute + ",");
        }
        System.out.println("]");
        System.out.println("------------------------------------------------------------");
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.FILM;
    }

    @Override
    public TreeNodeState getCurrentState() {
        return state;
    }
}
