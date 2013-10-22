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
public class SequenceChecker extends DefaultTreeEventHandler {
    private static Logger log = LoggerFactory.getLogger(SequenceChecker.class);
    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    private List<String> billedIDs = new ArrayList<>();

    public SequenceChecker(ResultCollector r, TreeNodeState treeNodeState) {
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
        int pageCounter = 0;
        String lastScan;
        for (String billedID : billedIDs) {
            // initialize the first time.
            if (pageCounter == 0) {
                //pageCounter =
            }

        }
        billedIDs = new ArrayList<>();
    }

    private String getBilledID(String fileName) {
        int lenght = fileName.length();
        int startIndex = fileName.indexOf(".jp2");
        int endIndex = fileName.indexOf(".jp2");
        return fileName.substring(startIndex, endIndex);
    }
}
