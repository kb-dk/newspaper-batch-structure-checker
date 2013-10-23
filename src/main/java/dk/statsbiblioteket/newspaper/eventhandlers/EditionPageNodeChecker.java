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
    
    private String filePrefix;
    private final String newspaperID;
    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    
    
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
        // prefix is: [newspaperid]-[parent-node-name]-[pictureID]
        String[] namesplit = name.split("/"); 
        filePrefix = namesplit[namesplit.length - 1];
        String parentNodeName = treeNodeState.getCurrentNode().getParent().getName();
        
        String currentNodeName = treeNodeState.getCurrentNode().getName();
        
        String[] nameParts = filePrefix.split("-");
        if(!nameParts[0].equals(newspaperID)) {
            //report re
        }
        
        if(!nameParts[1].equals("grandparantlocalname")) {
            // report bad date and edition
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
                    //report the last letter is not a upper case latin letter.
                }
                break;
            default:
                // report bad picture id
            }
        } catch (NumberFormatException e) {
            // report pictureID is not a number. 
        }
        log.debug("Found page number to be: " + pageNumber);
    }
    

}
