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
 * number check. The sequence covers a full film, eg. the UNMATCH dir and all the edition dir. The rules are: <ol>
 *     <li>sequence numbers are in the format NNNN or NNNNA/NNNNB NNNNA/NNNNB/NNNNC...., the later in case of two or
 *     four pages on a single physical image.</li>.
 *     <li>The physical image numbers are in sequence.</li>
 * </ol>
 * Nodes not adhering to the naming standard are just ignored, eg. not considered relevant for the sequence numbering.
 * The format check is considered to be the responsibility of another checker.
 */
public class PageImageIDSequenceChecker extends DefaultTreeEventHandler {
    private static Logger log = LoggerFactory.getLogger(PageImageIDSequenceChecker.class);
    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    private SequenceModel sequenceModel;

    public PageImageIDSequenceChecker(ResultCollector r, TreeNodeState treeNodeState) {
        resultCollector = r;
        this.treeNodeState = treeNodeState;
        this.sequenceModel = new SequenceModel();
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (treeNodeState.getCurrentNode().getType().equals(NodeType.PAGE_IMAGE)) {
            if (!event.getName().contains("brik")) {
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

    /**
     * Extracts the page image id number from the event string.
     * @param event The event to extract the page image ID number from.
     * @return The page image id number
     */
    private String getImageID(String event) {
        int startIndex = event.lastIndexOf('-') + 1;
        int endIndex = event.indexOf(".jp2");
        return event.substring(startIndex, endIndex);
    }

    private void registerFailure(String imageID, String description) {
        resultCollector.addFailure(imageID, "PageSequenceCheck", getClass().getSimpleName(), description);
    }

    /**
     * Implements the page image sequence rules by breaking the check down into 4 rules depending on the type
     * of page image: <ol>
     *     <li>First page image. The rules are implemented in the checkFirstPage method.</li>
     *     <li>First page image in physical film image, eg. either a NNNN or a NNNNA page. The rules are implemented
     *     in the checkFirstPartPage method</li>
     *     <li>Following page in composed film image, eg. NNNNB....The rules are implemented in the checkFollowingPart
     *      method</li>
     *     <li>Final page. The rules are implemented in the checkFirstPage method</li>
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
                    checkFirstPage(imageID);
                } else if (currentImageIDCounter.endsWith("A") || isCleanNumber(currentImageIDCounter)) {
                    checkFirstPartPage(imageID);
                } else {
                    checkFollowingPartPage(imageID);
                }

                lastImageIDCounter =  currentImageIDCounter;
                lastPageCounter = currentPageCounter;
            }
            if (currentImageIDCounter != null) {
                checkFinalPage(currentImageIDCounter);
            }
            imageIDs = new ArrayList<>();
        }

        /**
         * Implements the rules for checking the very first page number. The rules are: <ol>
         *     <li>Must either be a plain number (NNNN) or a A postfix number (NNNNA).</li>
         *     <li>The number must be 1.</li>
         * </ol>
         */
        public void checkFirstPage(String imageID) {
            if (!isCleanNumber(currentImageIDCounter) && !currentImageIDCounter.endsWith("A")) {
                registerFailure(imageID, "The first ImageID in a edition must either be a clean number or contain a 'A' postfix");
            } else if (currentPageCounter != 1) {
                registerFailure(imageID, "The first ImageID must start with 1");
            }
        }

        /**
         * Implements the rules for checking the first page image part of a physical image. The rules are: <ol>
         *     <li>If last number was clean number or a A postfix this must be a B post fix number, NNNNA/NNNNB comes in
         *     pairs.</li>
         *     <li>The previous number must be a N-1 number.</li>
         * </ol>
         */
        public void checkFirstPartPage(String imageID) {
            if (lastImageIDCounter.endsWith("A") || !isCleanNumber(lastImageIDCounter)) {
                registerFailure(lastImageIDCounter, "NNNNA imageID found without matching NNNNB imageID");
            }
            if (currentPageCounter > lastPageCounter + 1)  {
                registerFailure(imageID, "Missing sequence number, the previous imageID was " + lastImageIDCounter);
            }
            if (currentPageCounter == lastPageCounter) {
                registerFailure(imageID, "Duplicate sequence number, the previous imageID was " + lastImageIDCounter);
            }
        }

            /**
             * Implements the rules for checking the second or later page image part of a physical image.  The rules are: <ol>
             *     <li>The clean number part of this number must equal to th e clean number part of the previous number. Eg.
             *     NNNN=MMMM for NNNNA and MMMMB.</li>
             *     <li>The previous number must be a N-1 number.</li>
             * </ol>
             */
        public void checkFollowingPartPage(String imageID) {
            if (currentPageCounter != lastPageCounter) {
                registerFailure(imageID, "Missing sequence number, the previous imageID was " + lastImageIDCounter);
            }
            if (!isLetterPartPagesInSequence(lastImageIDCounter, currentImageIDCounter)) {
                registerFailure(imageID, "Missing sequence number by letter, the previous imageID was " + lastImageIDCounter);
            }
        }

        /**
         * Implements the rules for checking the second or later page image part of a physical image. This means either
         * a NNNN or a NNNNA image
         */
        public void checkFinalPage(String imageID) {
            if (imageID.endsWith("A")) {
                registerFailure(imageID, "Final imageID found without matching B imageID");
            }
        }

        /**
         * Returns true if the ImageID is in the format NNNN, eg. not followed by a letter.
         * @return
         */
        public boolean isCleanNumber(String imageIDCounter) {
            return imageIDCounter.length() == 4;
        }

        private boolean isLetterPartPagesInSequence(String firstPageID, String secondPageID) {
            return (firstPageID.charAt(4) + 1) == secondPageID.charAt(4);
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
