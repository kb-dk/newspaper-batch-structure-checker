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
    private SequenceModel sequenceModel;

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
            sequenceModel = new SequenceModel();
        }
    }

    private String getImageID(String fileName) {
        int startIndex = fileName.lastIndexOf('-') + 1;
        int endIndex = fileName.indexOf(".jp2");
        return fileName.substring(startIndex, endIndex);
    }

    private void registerFailure(String imageID, String description) {
        log.info("Found ImageID sequence problem: " + description + " for imageID: " + imageID);
        resultCollector.addFailure(imageID, "PageSequenceCheck", getClass().getSimpleName(), description);
    }

    /**
     * Implements the page image sequence rules by breaking the check down into 4 rules depending on the type
     * of page image: <ol>
     *     <li>First page image. The rules are implemented in the #checkFirstImage(String)</li>
     *     <li>First page image in </li>
     * </ol>
     */
    public class SequenceModel {
        private List<String> imageIDs = new ArrayList<>();
        private int lastPageCounter = 0;
        private String lastImageIDCounter = null;
        private String currentImageIDCounter;
        private int currentPageCounter;

        public void addImageID(String imageID) {
            imageIDs.add(imageID);
        }

        public void verifySequence() {
            Collections.sort(imageIDs);
            for (String imageID : imageIDs) {
                currentImageIDCounter = getImageID(imageID);
                currentPageCounter = getPageCounter(currentImageIDCounter);
                if (lastPageCounter == 0) {
                    checkFirstImage(imageID);
                } else if (currentImageIDCounter.endsWith("A") || isCleanNumber(currentImageIDCounter)) {
                    checkFirstPartImage(imageID);
                } else {
                    checkFollowingPartImage(imageID);
                }

                lastImageIDCounter =  currentImageIDCounter;
                lastPageCounter = currentPageCounter;
            }
            if (currentImageIDCounter != null) {
                checkFinalPartImage(currentImageIDCounter);
            }
            imageIDs = new ArrayList<>();
        }

        /**
         * Implements the rules for checking the very first page image.
         */
        public void checkFirstImage(String imageID) {
            if (!isCleanNumber(currentImageIDCounter) && !currentImageIDCounter.endsWith("A")) {
                registerFailure(imageID, "The first ImageID in a edition must either be a clean number or contain a 'A' postfix");
            } else if (currentPageCounter != 1) {
                registerFailure(imageID, "The first ImageID must start with 1");
            }
        }

        /**
         * Implements the rules for checking the first page image part of a physical image. This means either
         * a NNNN or a NNNNA image
         */
        public void checkFirstPartImage(String imageID) {
            if (lastImageIDCounter.endsWith("A") || !isCleanNumber(lastImageIDCounter)) {
                registerFailure(lastImageIDCounter, "NNNNA imageID found without matching NNNNB imageID");
            }
            if (currentPageCounter > lastPageCounter + 1)
                registerFailure(imageID, "Missing sequence number, the previous imageID was " + lastImageIDCounter);
            if (currentPageCounter == lastPageCounter)
                registerFailure(imageID, "Duplicate sequence number, the previous imageID was " + lastImageIDCounter);
        }

        /**
         * Implements the rules for checking the second or later page image part of a physical image. This means either
         * a NNNN or a NNNNA image
         */
        public void checkFollowingPartImage(String imageID) {
            if (currentPageCounter != lastPageCounter)
                registerFailure(imageID, "Missing sequence number, the previous imageID was " + lastImageIDCounter);
            if (currentPageCounter != lastPageCounter)
                registerFailure(imageID, "Missing sequence number, the previous imageID was " + lastImageIDCounter);
            //if ()
        }

        /**
         * Implements the rules for checking the second or later page image part of a physical image. This means either
         * a NNNN or a NNNNA image
         */
        public void checkFinalPartImage(String imageID) {
            if (imageID.endsWith("A"))
                registerFailure(imageID, "Final imageID found without matching B imageID");
        }

        /**
         * Returns true if the ImageID is in the format NNNN, eg. not followed by a letter.
         * @return
         */
        public boolean isCleanNumber(String imageIDCounter) {
            return imageIDCounter.length() == 4;
        }

        /**
         * Returns true if the ImageID is in the format NNNN, eg. not followed by a letter.
         * @return
         */
        public boolean isFollowPartNumber(String imageIDCounter) {
            return imageIDCounter.length() == 5 && !imageIDCounter.endsWith("A");
        }

        private String getPriorPageIDForLetter(String letter) {
            int charValue = letter.charAt(0);
            return String.valueOf( (char) (charValue - 1));
        }

        public int getPageCounter(String imageIDCounter) {
            String pageCounterString;
            if (imageIDCounter.length() == 5) {
                pageCounterString =  imageIDCounter.substring(0,4);
            } else {
                pageCounterString = imageIDCounter;
            }
            return Integer.parseInt(pageCounterString);
        }

    }
}
