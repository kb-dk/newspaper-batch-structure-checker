package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Checks that workshift ISO target files have proper naming.
 *
 * @author jrg
 */
public class WorkshiftISOTargetChecker extends DefaultTreeEventHandler {
    private static Logger log = LoggerFactory.getLogger(ImageIDSequenceChecker.class);
    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    private List<String> targetSerialisedNumbers = new ArrayList<>();
    private List<String> billedIDs = new ArrayList<>();

    public WorkshiftISOTargetChecker(ResultCollector r, TreeNodeState treeNodeState) {
        resultCollector = r;
        this.treeNodeState = treeNodeState;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        // TODO
    }

    @Override
    public void handleFinish() {
        // TODO
    }
}
