package dk.statsbiblioteket.newspaper.treenode;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
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

    @Test
    public void filmNodeTest() {
        TreeNodeState treeNodeState = new TreeNodeState();

        TestEventHelper.createBatchBeginEvent(1);
        treeNodeState.handleNodeBegin(TestEventHelper.createBatchBeginEvent(1));
        NodeBeginsParsingEvent workNodeBegin = new NodeBeginsParsingEvent("400022028241-14");
        treeNodeState.handleNodeBegin(workNodeBegin);

        assertNotNull(treeNodeState.getCurrentNode());
        assertEquals(treeNodeState.getCurrentNode().getName(), workNodeBegin.getName());
        assertEquals(treeNodeState.getCurrentNode().getType(), NodeType.FILM);
        assertNotNull(treeNodeState.getCurrentNode().getParent());
        assertEquals(treeNodeState.getCurrentNode().getParent().getType(), NodeType.BATCH);
    }

    @Test
    public void unmatchedTest() {
        TreeNodeState treeNodeState = new TreeNodeState();

        TestEventHelper.createBatchBeginEvent(1);
        treeNodeState.handleNodeBegin(TestEventHelper.createBatchBeginEvent(1));
        treeNodeState.handleNodeBegin(new NodeBeginsParsingEvent("400022028241-14"));
        NodeBeginsParsingEvent unmatchNodeBegin = new NodeBeginsParsingEvent("UNMATCHED");
        treeNodeState.handleNodeBegin(unmatchNodeBegin);

        assertNotNull(treeNodeState.getCurrentNode());
        assertEquals(treeNodeState.getCurrentNode().getType(), NodeType.UNMATCHED);
        assertEquals(treeNodeState.getCurrentNode().getName(), unmatchNodeBegin.getName());
        assertNotNull(treeNodeState.getCurrentNode().getParent());
        assertEquals(treeNodeState.getCurrentNode().getParent().getType(), NodeType.FILM);
    }

    @Test
    public void filmIsoTargetTest() {
        TreeNodeState treeNodeState = new TreeNodeState();

        TestEventHelper.createBatchBeginEvent(1);
        treeNodeState.handleNodeBegin(TestEventHelper.createBatchBeginEvent(1));
        treeNodeState.handleNodeBegin(new NodeBeginsParsingEvent("400022028241-14"));
        NodeBeginsParsingEvent unmatchNodeBegin = new NodeBeginsParsingEvent("FILM-ISO-TARGET");
        treeNodeState.handleNodeBegin(unmatchNodeBegin);

        assertNotNull(treeNodeState.getCurrentNode());
        assertEquals(treeNodeState.getCurrentNode().getType(), NodeType.ISO_TARGET_ON_FILM);
        assertEquals(treeNodeState.getCurrentNode().getName(), unmatchNodeBegin.getName());
        assertNotNull(treeNodeState.getCurrentNode().getParent());
        assertEquals(treeNodeState.getCurrentNode().getParent().getType(), NodeType.FILM);
    }


    @Test
    public void udgaveTest() {
        TreeNodeState treeNodeState = new TreeNodeState();

        TestEventHelper.createBatchBeginEvent(1);
        treeNodeState.handleNodeBegin(TestEventHelper.createBatchBeginEvent(1));
        treeNodeState.handleNodeBegin(new NodeBeginsParsingEvent("400022028241-14"));
        NodeBeginsParsingEvent unmatchNodeBegin = new NodeBeginsParsingEvent("1860-10-18-01");
        treeNodeState.handleNodeBegin(unmatchNodeBegin);

        assertNotNull(treeNodeState.getCurrentNode());
        assertEquals(treeNodeState.getCurrentNode().getType(), NodeType.UDGAVE);
        assertEquals(treeNodeState.getCurrentNode().getName(), unmatchNodeBegin.getName());
        assertNotNull(treeNodeState.getCurrentNode().getParent());
        assertEquals(treeNodeState.getCurrentNode().getParent().getType(), NodeType.FILM);
    }


    @Test
    public void nodeEndTest() {
        TreeNodeState treeNodeState = new TreeNodeState();

        TestEventHelper.createBatchBeginEvent(1);
        treeNodeState.handleNodeBegin(TestEventHelper.createBatchBeginEvent(1));
        treeNodeState.handleNodeBegin(new NodeBeginsParsingEvent("400022028241-14"));
        NodeBeginsParsingEvent udgaveNodeBegin = new NodeBeginsParsingEvent("1860-10-18-01");
        treeNodeState.handleNodeBegin(udgaveNodeBegin);

        assertNotNull(treeNodeState.getCurrentNode());
        assertEquals(treeNodeState.getCurrentNode().getType(), NodeType.UDGAVE);

        treeNodeState.handleNodeEnd(new NodeEndParsingEvent(udgaveNodeBegin.getName()));
        assertEquals(treeNodeState.getCurrentNode().getType(), NodeType.FILM);

        treeNodeState.handleNodeEnd(new NodeEndParsingEvent(""));
        assertEquals(treeNodeState.getCurrentNode().getType(), NodeType.BATCH);

        treeNodeState.handleFinish();
    }
}
