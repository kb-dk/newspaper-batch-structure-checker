package dk.statsbiblioteket.newspaper.eventhandlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

/**
 * Class to check Edition node for expected attributes and child nodes
 * Checks that:
 * - There's an edition attribute.
 * - The name of the childs conforms to either:
 * -- For briks: [newspaperID]-[date]-[editionid]-[pictureid]-brik
 * -- For pages: [newspaperID]-[date]-[editionid]-[pictureid]
 * - That newspaperID corresponds to what obtained from MF-PAK
 * - That date and editionid follows the Edition's own name
 * - That pictureID conforms to 
 * -- For briks: [0-9]{4}
 * -- For pages: [0-9]{4}[A-Z]{0-1}
 */
public class EditionNodeChecker extends AbstractNodeChecker {
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private static final String EDITION_ATTRIBUTE_SUFFIX = ".edition.xml";
    private static final String BRIK_NODE_SUFFIX = "brik";

    private final String newspaperID;
    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    
    public EditionNodeChecker(String newspaperID, ResultCollector resultCollector, TreeNodeState state) {
        this.resultCollector = resultCollector;
        this.treeNodeState = state;
        this.newspaperID = newspaperID;
    }
    
    @Override
    public void doCheck() {
        checkAttributes();
        checkChildNodes();      
    }
    
    @Override
    public NodeType getNodeType() {
        return NodeType.EDITION;
    }

    @Override
    public TreeNodeState getCurrentState() {
        return treeNodeState;
    }
    
    private void checkAttributes() {
        String editionAttribute = getPathPrefix() + newspaperID + "-" + name + EDITION_ATTRIBUTE_SUFFIX;
        if(attributes.contains(editionAttribute)) {
            attributes.remove(editionAttribute);
        } else {
            reportFailure("missingeditionattribute", "The expected attribute '" + editionAttribute + "' was not found.");
        }
        if(!attributes.isEmpty()) {
            for(String attribute : attributes) {
                reportFailure("unexpectedattribute", "The attribute: '" + attribute + "' was not expected.");
            }
        }
    }
    
    private void checkChildNodes() {
        for(String childNode : childNodes) {
            String node = getLocalName(childNode);
            if(!node.startsWith(newspaperID)) {
                reportFailure("badnewspaperid", "The name of the page node: '" + childNode + "' does not contain the correct newspaperID ('"
                        + newspaperID +"')");
            } 
            
            if(childNode.endsWith(BRIK_NODE_SUFFIX)) {
                checkBrikChildNode(childNode);
            } else {
                checkPageChildNode(childNode);
            }
        }
    }

    private void checkBrikChildNode(String node) {
        String localNodeName = getLocalName(node);
        String[] nameParts = localNodeName.split("-");
        String pictureID = nameParts[nameParts.length - 2];
        if(pictureID.length() != 4) {
            reportFailure("badpictureid", "The pictureID: '" + pictureID + "' for brik node '" + node 
                    + "' is not of the correct form ([0-9]{4})" );
        } else {
            try {
                checkPictureID(pictureID);
            } catch (PictureIDCheckException e) {
                reportFailure("badpictureid", "PictureID check failed for node '" + node + "'. " + e.getReason());
            }
        }
        
        String expectedBrikNodeName = newspaperID + "-" + name + "-" + pictureID + "-" + BRIK_NODE_SUFFIX;
        if(!localNodeName.equals(expectedBrikNodeName)) {
            reportFailure("badbriknodename", "The brik node '" + node + "' is not of the expected format '" 
                    + "[newspaperID]-[date]-[editionid]-[pictureid]-brik");
        }
        
    }
    
    private void checkPageChildNode(String node) {
        String localNodeName = getLocalName(node);
        String[] nameParts = localNodeName.split("-");
        String pictureID = nameParts[nameParts.length - 1];
        try {
            checkPictureID(pictureID);
        } catch (PictureIDCheckException e) {
            reportFailure("badpictureid", "PictureID check failed for node '" + node + "'. " + e.getReason());
        }
        
        String expectedPageNodeName = newspaperID + "-" + name + "-" + pictureID;
        if(!localNodeName.equals(expectedPageNodeName)) {
            reportFailure("badpagenodename", "The page node '" + node + "' is not of the expected format '" 
                    + "[newspaperID]-[date]-[editionid]-[pictureid]");
        }
        
    }
    
    private void checkPictureID(String pictureID) throws PictureIDCheckException {
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
                    throw new PictureIDCheckException("The pictureID: '" + pictureID + "' is not of the correct form ([0-9]{4}[A-Z]{0-1})");
                }
                break;
            default:
                throw new PictureIDCheckException("The pictureID: '" + pictureID +"' is not of the correct form ([0-9]{4}[A-Z]{0-1})");
            }
        } catch (NumberFormatException e) {
            throw new PictureIDCheckException("The pictureID '" + pictureID + "' can't be interpreded as a number.");
        }
        log.debug("Found page number to be: " + pageNumber);
    }
    
    private void reportFailure(String type, String description) {
        resultCollector.addFailure(name, type, this.getClass().getName(), description);
    }
    
    private String getPathPrefix() {
        return treeNodeState.getCurrentNode().getName() + "/";
    }
    
    private String getLocalName(String fullPath) {
        String[] namesplit = fullPath.split("/"); 
        return namesplit[namesplit.length - 1];
    }

    private class PictureIDCheckException extends Exception {
        private String reason;
        
        PictureIDCheckException(String reason) {
            this.reason = reason;
        }
        
        String getReason() {
            return reason;
        }
    }
    
}
