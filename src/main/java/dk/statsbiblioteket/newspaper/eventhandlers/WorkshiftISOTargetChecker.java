package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Performs various checks on WORKSHIFT_ISO_TARGET folder and its files.
 *
 * @author jrg
 */
public class WorkshiftISOTargetChecker extends DefaultTreeEventHandler {
    private static Logger log = LoggerFactory.getLogger(BilledIDSequenceChecker.class);

    private final String WORKSHIFT_ISO_TARGET_NAME = "WORKSHIFT-ISO-TARGET";

    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    // make class mapping string (filename without extension) to pair of bools, for checking matching jp2's + mix's TODO
    private List<String> targetSerialisedNumbers = new ArrayList<>();

    public WorkshiftISOTargetChecker(ResultCollector r, TreeNodeState treeNodeState) {
        resultCollector = r;
        this.treeNodeState = treeNodeState;
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        // If this is not a WORKSHIFT-ISO-TARGET folder, but our parent is, it's an error TODO
        if (!treeNodeState.getCurrentNode().getName().equals(WORKSHIFT_ISO_TARGET_NAME)
                && treeNodeState.getCurrentNode().getParent().getName().equals(WORKSHIFT_ISO_TARGET_NAME)) {
            resultCollector.addFailure(treeNodeState.getCurrentNode().getName(), "filestructure",
                    "WorkshiftISOTargetChecker", "Unexpected directory in " + WORKSHIFT_ISO_TARGET_NAME);
        }
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        // Parent folder must be called WORKSHIFT-ISO-TARGET
        if (!treeNodeState.getCurrentNode().getParent().getName().equals(WORKSHIFT_ISO_TARGET_NAME)) {
            return;
        }

        // If file is not a (correctly named) target-file, report an error TODO


        // File is a target file, set targetFilesExist to true TODO

        // Mark current jp2 or mix file as found TODO

        // Collect targetSerialisedNumbers and billedIDs for later processing
        if (treeNodeState.getCurrentNode().getType().equals(NodeType.IMAGE)) {
            //targetSerialisedNumbers.add(event.getName()); // TODO fix
        }
    }

    @Override
    public void handleFinish() {
        // Check that there is exactly one mix file for each jp2, and vice versa TODO

        // Check that collected targetSerialisedNumbers are sequential and starting at 1 TODO
    }
}
