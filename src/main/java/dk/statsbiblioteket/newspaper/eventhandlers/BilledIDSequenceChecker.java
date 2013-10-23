package dk.statsbiblioteket.newspaper.eventhandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the the scanned pages are named in sequence. The image files for the scanned pages are used for the sequence
 * number check The rules are: <ol>
 *     <li>sequence numbers are in the format NNNN or NNNNA/NNNNB, the later in case of two pages on a single physical
 *     image scan</li>.
 *     <li>The most either on page of the format NNNN or a page pair in the format NNNNA/NNNNB for each int
 *     in the sequence.</li>
 * </ol>
 */
public class BilledIDSequenceChecker extends DefaultTreeEventHandler {
    private static Logger log = LoggerFactory.getLogger(BilledIDSequenceChecker.class);
    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    private List<String> billedIDs = new ArrayList<>();

    public BilledIDSequenceChecker(ResultCollector r, TreeNodeState treeNodeState) {
        resultCollector = r;
        this.treeNodeState = treeNodeState;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if (treeNodeState.getCurrentNode().getType().equals(NodeType.PAGE_IMAGE)) {
            if (!treeNodeState.getCurrentNode().getName().contains("brik")) {
                billedIDs.add(event.getName());
            }
        }
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        if (treeNodeState.getCurrentNode() != null && //Finished
            treeNodeState.getCurrentNode().getType().equals(NodeType.FILM)) {
            checkSequence();
        }
    }

    public void checkSequence() {
        //ToDO Check the collected billedIDs, properly by filtering them first.
        Collections.sort(billedIDs);
        int lastPageCounter = 0;
        String lastBilledIDCounter = null;
        for (String billedID : billedIDs) {
            String currentBilledIDCounter = getBilledID(billedID);
            int currentPageCounter = getPageCounter(currentBilledIDCounter);

            if (lastPageCounter != 0 && lastBilledIDCounter != null) {
                if (lastBilledIDCounter.endsWith("A") && !currentBilledIDCounter.endsWith("B")) {
                    registerFailure(billedID, "NNNNA billedID found without matching NNNNB billedID");
                } else if (currentBilledIDCounter.endsWith("B") && !lastBilledIDCounter.endsWith("A")) {
                    registerFailure(billedID, "NNNNB billedID found without matching NNNNA billedID");
                }
                //pageCounter =
            } else {
                if (!(currentBilledIDCounter.length() == 4 || currentBilledIDCounter.endsWith("A"))) {
                    registerFailure(billedID, "The first BilledID in a Udgavemust either be a clean number or contain a 'A' postfix");
                }
            }

            lastBilledIDCounter =  currentBilledIDCounter;
            lastPageCounter = currentPageCounter;
        }
        billedIDs = new ArrayList<>();
    }

    private String getBilledID(String fileName) {
        int startIndex = fileName.lastIndexOf('-') + 1;
        int endIndex = fileName.indexOf(".jp2");
        return fileName.substring(startIndex, endIndex);
    }

    private int getPageCounter(String billedIDCounter) {
        String pageCounterString;
        if (billedIDCounter.length() == 5) {
            pageCounterString =  billedIDCounter.substring(0,4);
        } else {
            pageCounterString = billedIDCounter;
        }
        return Integer.parseInt(pageCounterString);
    }

    private void registerFailure(String billedID, String description) {
        resultCollector.addFailure(billedID, "PageSequenceCheck", getClass().getSimpleName(), description);
    }
}
