package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
 * Class to check a edition-page node for expected content and file naming 
 */
public class EditionPageNodeChecker extends AbstractNodeChecker {

    private static final String MIX_ATTRIBUTE_SUFFIX = ".mix.xml";
    private static final String MODS_ATTRIBUTE_SUFFIX = ".mods.xml";
    private static final String ALTO_ATTRIBUTE_SUFFIX = ".alto.xml";
    private static final String JP2_CHILD_NODE_SUFFIX = ".jp2";
    
    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    
    /**
     * Checker class for edition pages. Checks that:
     * - There only is the expected attributes {mix, mods, alto}
     * - Contain the expected child node (child node for the jp2 file). 
     */
    public EditionPageNodeChecker(String newspaperID, ResultCollector resultCollector, TreeNodeState state) {
        this.resultCollector = resultCollector;
        this.treeNodeState = state;
    }
    
    @Override
    public void doCheck() {
        checkAttributes();
        checkChildNodes();
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.PAGE;
    }

    @Override
    public TreeNodeState getCurrentState() {
        return treeNodeState;
    }
    
    private void reportFailure(String type, String description) {
        resultCollector.addFailure(name, type, this.getClass().getName(), description);
    }
    
    private void checkAttributes() {
        checkAttribute(name + MODS_ATTRIBUTE_SUFFIX);
        checkAttribute(name + MIX_ATTRIBUTE_SUFFIX);
        checkAttribute(name + ALTO_ATTRIBUTE_SUFFIX);
        if(!attributes.isEmpty()) {
            for(String attribute : attributes) {
                reportFailure("unexpectedattribute", "The attribute: '" + attribute + "' was not expected.");
            }
        }
    }
    
    private void checkChildNodes() {
        if(childNodes.contains(name + JP2_CHILD_NODE_SUFFIX)) {
            childNodes.remove(name + JP2_CHILD_NODE_SUFFIX);
        }
        for(String childNode : childNodes) {
            reportFailure("unexpectedchildnode", "The child node: '" + childNode + "' was not expected.");
        }
        
    }
    
    private void checkAttribute(String attributeName) {
        if(attributes.contains(attributeName)) {
            attributes.remove(attributeName);
        } else {
            reportFailure("missingattribute", "The attribute: '" + attributeName + "' was not found.");
        }
    }

}
