package dk.statsbiblioteket.newspaper.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
 * Class to check a edition-page node for expected content and file naming 
 */
public class EditionPageNodeChecker extends AbstractNodeChecker {

    private Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String MIX_ATTRIBUTE_SUFFIX = ".mix.xml";
    private static final String MODS_ATTRIBUTE_SUFFIX = ".mods.xml";
    private static final String ALTO_ATTRIBUTE_SUFFIX = ".alto.xml";
    private static final String JP2_CHILD_NODE_SUFFIX = ".jp2";
    
    private String filePrefix;
    private final String newspaperID;
    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    
    /**
     * Checker class for edition pages. Checks that:
     * - All names is of the form [newspaperid]-[parent-node-name]-[pictureID].[suffix]
     * - There only is the expected attributes {mix, mods, alto}
     * - Contain the expected child node (child node for the jp2 file). 
     */
    public EditionPageNodeChecker(String newspaperID, ResultCollector resultCollector, TreeNodeState state) {
        this.newspaperID = newspaperID;
        this.resultCollector = resultCollector;
        this.treeNodeState = state;
    }
    
    @Override
    public void doCheck() {
        buildAndCheckFilePrefix();
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
    
    private void buildAndCheckFilePrefix() {
        filePrefix = getLocalName(name);
       
        String parentName = getLocalName(treeNodeState.getCurrentNode().getName());
        
        String[] nameParts = filePrefix.split("-");
        if(!nameParts[0].equals(newspaperID)) {
            reportFailure("badnewspaperid", "The name of the page: '" + name + "' does not contain the correct newspaperID ('"
                    + newspaperID +"')");
        }
        
        if(!nameParts[1].equals(parentName)) {
            reportFailure("badeditionid", "The name of the page: '" + name + "' does not contain the correct editionID ('"
                    + parentName +"')");
        }
        
        checkPictureID(nameParts[2]);
    }
    
    private void checkAttributes() {
        checkAttribute(filePrefix + MODS_ATTRIBUTE_SUFFIX);
        checkAttribute(filePrefix + MIX_ATTRIBUTE_SUFFIX);
        checkAttribute(filePrefix + ALTO_ATTRIBUTE_SUFFIX);
        if(!attributes.isEmpty()) {
            for(String attribute : attributes) {
                reportFailure("unexpectedattribute", "The attribute: '" + attribute + "' was not expected.");
            }
        }
    }
    
    private void checkChildNodes() {
        if(childNodes.contains(filePrefix + JP2_CHILD_NODE_SUFFIX)) {
            childNodes.remove(filePrefix + JP2_CHILD_NODE_SUFFIX);
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
    
    private void checkPictureID(String pictureID) {
        Integer pageNumber = null;
        try {
            switch(pictureID.length()) {
            case 4:
                //single page
                pageNumber = Integer.parseInt(pictureID);
                break;
            case 5:
                //A, B, C or D page
                pageNumber = Integer.parseInt(pictureID.substring(0, 3));
                if(!pictureID.substring(4).matches("[A-Z]")) {
                    reportFailure("badpictureid", "The pictureID: '" + pictureID + "' is not of the correct form ([0-9]{4}[A-Z]{0-1})" );
                }
                break;
            default:
                reportFailure("badpictureid", "The pictureID: '" + pictureID +"' is not of the correct form ([0-9]{4}[A-Z]{0-1})" );
            }
        } catch (NumberFormatException e) {
            reportFailure("badpictureid", "The pictureID '" + pictureID + "' can't be interpreded as a number."); 
        }
        log.debug("Found page number to be: " + pageNumber);
    }
    
    private String getLocalName(String fullPath) {
        String[] namesplit = fullPath.split("/"); 
        return namesplit[namesplit.length - 1];
    }

}
