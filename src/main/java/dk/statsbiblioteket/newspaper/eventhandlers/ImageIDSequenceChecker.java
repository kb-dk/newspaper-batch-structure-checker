package dk.statsbiblioteket.newspaper.eventhandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the the scanned pages are named in sequence. The image files for the scanned pages are used for the sequence
 * number check. The sequence covers a full film, eg. the MISMATCH dir and all the edition dir. The rules are: <ol>
 *     <li>sequence numbers are in the format NNNN or NNNNA/NNNNB NNNNA/NNNNB/NNNNC/NNNND, the later in case of two or
 *     four pages on a single physical image.</li>.
 *     <li>The most either on page of the format NNNN or a page pair in the format NNNNA/NNNNB for each int
 *     in the sequence.</li>
 * </ol>
 * Nodes not adhering to the naming stadard are just ignored, eg. not considered relevant for the sequence numbering. 
 * The format check is considered to be the responsability of another checker. 
 */
public class ImageIDSequenceChecker extends DefaultTreeEventHandler {
    private static Logger log = LoggerFactory.getLogger(ImageIDSequenceChecker.class);
    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    private final SequenceModel sequenceModel;

    public ImageIDSequenceChecker(ResultCollector r, TreeNodeState treeNodeState) {
        resultCollector = r;
        this.treeNodeState = treeNodeState;
        this.sequenceModel = new SequenceModel();
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (treeNodeState.getCurrentNode().getType().equals(NodeType.PAGE_IMAGE)) {
            if (!treeNodeState.getCurrentNode().getName().contains("brik")) {
                sequenceModel.addImageID(event.getName());
            }
        }
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        TreeNode currentNode = treeNodeState.getCurrentNode();
        if (currentNode != null && //Finished
                currentNode.getType().equals(NodeType.BATCH)) {
            sequenceModel.verifySequence();
        }
    }

    private String getImageID(String fileName) {
        int startIndex = fileName.lastIndexOf('-') + 1;
        int endIndex = fileName.indexOf(".jp2");
        return fileName.substring(startIndex, endIndex);
    }

    /**
     * Returns true if the ImageID is in the format NNNN, eg. not followed by a letter.
     * @return
     */
    private boolean isCleanNumber(String imageIDCounter) {
        return imageIDCounter.length() == 4;
    }

    private int getPageCounter(String imageIDCounter) {
        String pageCounterString;
        if (imageIDCounter.length() == 5) {
            pageCounterString =  imageIDCounter.substring(0,4);
        } else {
            pageCounterString = imageIDCounter;
        }
        return Integer.parseInt(pageCounterString);
    }

    private void registerFailure(String imageID, String description) {
        log.info("Found ImageID sequence problem: " + description + " for imageID: " + imageID);
        resultCollector.addFailure(imageID, "PageSequenceCheck", getClass().getSimpleName(), description);
    }

    private class SequenceModel {
        private List<String> imageIDs = new ArrayList<>();

        public void addImageID(String imageID) {
            imageIDs.add(imageID);
        }

        public void verifySequence() {
            Collections.sort(imageIDs);
            int lastPageCounter = 0;
            String lastImageIDCounter = null;
            for (String imageID : imageIDs) {
                String currentImageIDCounter = getImageID(imageID);
                int currentPageCounter = getPageCounter(currentImageIDCounter);
                if (lastPageCounter == 0) {
                    if (!isCleanNumber(currentImageIDCounter) && !currentImageIDCounter.endsWith("A")) {
                        registerFailure(imageID, "The first ImageID in a edition must either be a clean number or contain a 'A' postfix");
                    } else if (currentPageCounter != 1) {
                        registerFailure(imageID, "The first ImageID must start with 1");
                    }
                    else if (lastPageCounter != 0 && lastImageIDCounter != null) {
                        if (lastImageIDCounter.endsWith("A") && !currentImageIDCounter.endsWith("B")) {
                            registerFailure(imageID, "NNNNA imageID found without matching NNNNB imageID");
                        } else if (currentImageIDCounter.endsWith("B") && !lastImageIDCounter.endsWith("A")) {
                            registerFailure(imageID, "NNNNB imageID found without matching NNNNA imageID");
                        } else if (currentImageIDCounter.endsWith("B")) {
                            if (lastPageCounter != currentPageCounter) {
                                registerFailure(imageID, "NNNNB imageID found without matching NNNNA imageID");
                            }
                        } else if (currentImageIDCounter.endsWith("A") || !currentImageIDCounter.endsWith("B")) {
                            if (currentPageCounter != lastPageCounter + 1)
                                registerFailure(imageID, "Missing sequence number, the previous imageID was " + lastImageIDCounter);
                        }
                    }
                }

                lastImageIDCounter =  currentImageIDCounter;
                lastPageCounter = currentPageCounter;
            }
            imageIDs = new ArrayList<>();
        }

        private abstract class PriorCounterChecker {

        }
    }
}
