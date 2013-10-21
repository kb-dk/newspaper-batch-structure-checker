package dk.statsbiblioteket.newspaper.treenode;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.newspaper.eventhandlers.TestEventHelper;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class TreeNodeStateTest {
    @Test
    public void batchNodeTest() throws Exception {
        TreeNodeState treeNodeState = new TreeNodeState();

        assertNull(treeNodeState.getCurrentNode());

        NodeBeginsParsingEvent batchNodeBegin = TestEventHelper.createBatchBeginEvent(1);
        treeNodeState.handleNodeBegin(batchNodeBegin);
        assertNotNull(treeNodeState.getCurrentNode());
        assertEquals(treeNodeState.getCurrentNode().getName(), batchNodeBegin.getName());
        assertEquals(treeNodeState.getCurrentNode().getType(), NodeType.BATCH);
        assertNull(treeNodeState.getCurrentNode().getParent());
    }

    @Test
    public void workshiftNodeTest() {
        TreeNodeState treeNodeState = new TreeNodeState();

        assertNull(treeNodeState.getCurrentNode());

        TestEventHelper.createBatchBeginEvent(1);
        treeNodeState.handleNodeBegin(TestEventHelper.createBatchBeginEvent(1));
        NodeBeginsParsingEvent workNodeBegin = new NodeBeginsParsingEvent("WORKSHIFT-ISO-TARGET");
        treeNodeState.handleNodeBegin(workNodeBegin);

        assertNotNull(treeNodeState.getCurrentNode());
        assertEquals(treeNodeState.getCurrentNode().getName(), workNodeBegin.getName());
        assertEquals(treeNodeState.getCurrentNode().getType(), NodeType.WORKSHIFT_ISO_TARGET);
        assertNotNull(treeNodeState.getCurrentNode().getParent());
        assertEquals(treeNodeState.getCurrentNode().getParent().getType(), NodeType.BATCH);
    }
}
