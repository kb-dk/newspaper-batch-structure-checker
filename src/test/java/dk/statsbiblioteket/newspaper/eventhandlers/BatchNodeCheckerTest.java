package dk.statsbiblioteket.newspaper.eventhandlers;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 *Various tests for BatchNodeChecker .
 */
public class BatchNodeCheckerTest {

    /**
     * Check that the checker returns success if the name of the batch event matches the full id (ie id +
     * RT) of the batch.
     * @throws Exception
     */
    @Test
    public void testHandleNodeBegin() throws Exception {
        String batch_id = "400022028241";
        int roundtrips = 1;
        Batch batch = new Batch();
        batch.setBatchID(batch_id);
        batch.setRoundTripNumber(1);
        final TreeNode batchNode = new TreeNode(null, NodeType.BATCH, null);
        SettableTreeNodeState state = new SettableTreeNodeState(batchNode);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchNodeChecker checker = new BatchNodeChecker(batch, resultCollector, state);
        NodeBeginsParsingEvent batchBeginEvent = new NodeBeginsParsingEvent("B" + batch_id + "-RT" + roundtrips);
        NodeEndParsingEvent batchEndEvent = new NodeEndParsingEvent("B" + batch_id + "-RT" + roundtrips);
        NodeBeginsParsingEvent witBeginEvent = new NodeBeginsParsingEvent("WORKSHIFT-ISO-TARGET");
        NodeEndParsingEvent witEndEvent = new NodeEndParsingEvent("WORKSHIFT-ISO-TARGET");
        checker.handleNodeBegin(batchBeginEvent);
        state.setCurrentNode(new TreeNode(null, NodeType.WORKSHIFT_ISO_TARGET, null));
        checker.handleNodeBegin(witBeginEvent);
        checker.handleNodeEnd(witEndEvent);
        state.setCurrentNode(batchNode);
        checker.handleNodeEnd(batchEndEvent);
        assertTrue(resultCollector.isSuccess());
    }

    /**
        * Test that checking fails when WORKSHIFT-ISO-TARGET directory is missing.
        * @throws Exception
        */
       @Test
       public void testHandleBatchNoWIT() throws Exception {
           String batch_id = "400022028241";
           int roundtrips = 1;
           Batch batch = new Batch();
           batch.setBatchID(batch_id);
           batch.setRoundTripNumber(1);
           final TreeNode batchNode = new TreeNode(null, NodeType.BATCH, null);
           SettableTreeNodeState state = new SettableTreeNodeState(batchNode);
           ResultCollector resultCollector = new ResultCollector("foo", "bar");
           BatchNodeChecker checker = new BatchNodeChecker(batch, resultCollector, state);
           NodeBeginsParsingEvent batchBeginEvent = new NodeBeginsParsingEvent("B" + batch_id + "-RT" + roundtrips);
           NodeEndParsingEvent batchEndEvent = new NodeEndParsingEvent("B" + batch_id + "-RT" + roundtrips);
           NodeBeginsParsingEvent witBeginEvent = new NodeBeginsParsingEvent("WORKSHIFT-ISO-TARGET");
           NodeEndParsingEvent witEndEvent = new NodeEndParsingEvent("WORKSHIFT-ISO-TARGET");
           checker.handleNodeBegin(batchBeginEvent);
           checker.handleNodeEnd(batchEndEvent);
           assertFalse(resultCollector.isSuccess());
           assertEquals(Util.countFailures(resultCollector), 1);
       }

    /**
     * Test that checking fails when the id of the batch node doesn't match the id of the expected batch.
     * @throws Exception
     */
    @Test
    public void testHandleNodeBeginWrongId() throws Exception {
        String batch_id = "400022028241";
              int roundtrips = 1;
              Batch batch = new Batch();
              batch.setBatchID(batch_id);
              batch.setRoundTripNumber(1);
              final TreeNode batchNode = new TreeNode(null, NodeType.BATCH, null);
              SettableTreeNodeState state = new SettableTreeNodeState(batchNode);
              ResultCollector resultCollector = new ResultCollector("foo", "bar");
              BatchNodeChecker checker = new BatchNodeChecker(batch, resultCollector, state);
              NodeBeginsParsingEvent batchBeginEvent = new NodeBeginsParsingEvent("B400022028243" + "-RT" + roundtrips);
              NodeEndParsingEvent batchEndEvent = new NodeEndParsingEvent("B400022028243" + "-RT" + roundtrips);
              NodeBeginsParsingEvent witBeginEvent = new NodeBeginsParsingEvent("WORKSHIFT-ISO-TARGET");
              NodeEndParsingEvent witEndEvent = new NodeEndParsingEvent("WORKSHIFT-ISO-TARGET");
              checker.handleNodeBegin(batchBeginEvent);
              state.setCurrentNode(new TreeNode(null, NodeType.WORKSHIFT_ISO_TARGET, null));
              checker.handleNodeBegin(witBeginEvent);
              checker.handleNodeEnd(witEndEvent);
              state.setCurrentNode(batchNode);
              checker.handleNodeEnd(batchEndEvent);
        assertFalse(resultCollector.isSuccess());
        assertEquals(Util.countFailures(resultCollector), 1);
    }

    /**
     * Test for failure when batch node has unexpected name format.
     * @throws Exception
     */
    @Test
    public void testHandleNodeBeginStrangeFormat() throws Exception {
        String batch_id = "400022028241";
        int roundtrips = 1;
        Batch batch = new Batch();
        batch.setBatchID(batch_id);
        batch.setRoundTripNumber(1);
        final TreeNode batchNode = new TreeNode(null, NodeType.BATCH, null);
        SettableTreeNodeState state = new SettableTreeNodeState(batchNode);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchNodeChecker checker = new BatchNodeChecker(batch, resultCollector, state);
        NodeBeginsParsingEvent batchBeginEvent = new NodeBeginsParsingEvent("badbatch.name.foobar");
        NodeEndParsingEvent batchEndEvent = new NodeEndParsingEvent("badbatch.name.foobar");
        NodeBeginsParsingEvent witBeginEvent = new NodeBeginsParsingEvent("WORKSHIFT-ISO-TARGET");
        NodeEndParsingEvent witEndEvent = new NodeEndParsingEvent("WORKSHIFT-ISO-TARGET");
        checker.handleNodeBegin(batchBeginEvent);
        state.setCurrentNode(new TreeNode(null, NodeType.WORKSHIFT_ISO_TARGET, null));
        checker.handleNodeBegin(witBeginEvent);
        checker.handleNodeEnd(witEndEvent);
        state.setCurrentNode(batchNode);
        checker.handleNodeEnd(batchEndEvent);
        assertFalse(resultCollector.isSuccess());
        assertEquals(Util.countFailures(resultCollector), 2);
    }

    /**
     * Test for failure when the batch node contains an unexpected attribute.
     * @throws Exception
     */
    @Test
    public void testHandleUnexpectedFile() throws Exception {
        String batch_id = "400022028241";
        int roundtrips = 1;
        Batch batch = new Batch();
        batch.setBatchID(batch_id);
        batch.setRoundTripNumber(1);
        final TreeNode batchNode = new TreeNode(null, NodeType.BATCH, null);
        SettableTreeNodeState state = new SettableTreeNodeState(batchNode);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchNodeChecker checker = new BatchNodeChecker(batch, resultCollector, state);
        NodeBeginsParsingEvent batchBeginEvent = new NodeBeginsParsingEvent("B" + batch_id + "-RT" + roundtrips);
        NodeEndParsingEvent batchEndEvent = new NodeEndParsingEvent("B" + batch_id + "-RT" + roundtrips);
        NodeBeginsParsingEvent witBeginEvent = new NodeBeginsParsingEvent("WORKSHIFT-ISO-TARGET");
        NodeEndParsingEvent witEndEvent = new NodeEndParsingEvent("WORKSHIFT-ISO-TARGET");
        AttributeParsingEvent attributeParsingEvent = new FileAttributeParsingEvent("foobar", new File("foobar"));
        checker.handleNodeBegin(batchBeginEvent);
        checker.handleAttribute(attributeParsingEvent);
        state.setCurrentNode(new TreeNode(null, NodeType.WORKSHIFT_ISO_TARGET, null));
        checker.handleNodeBegin(witBeginEvent);
        checker.handleNodeEnd(witEndEvent);
        state.setCurrentNode(batchNode);
        checker.handleNodeEnd(batchEndEvent);
        assertFalse(resultCollector.isSuccess());
        assertEquals(Util.countFailures(resultCollector), 1);
    }

    private final static String TEST_BATCH_ID = "400022028241";

    /**
     * Tests the BatchNodeChecker on real file-based data.
     */
    //@Test(groups = "integrationTest")
    //Todo Refactorer as component test, where a real TreeIterstor using test data is available.
    public void testBatchNodeCheckerIT() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        //TreeIterator iterator = (new BatchStructureCheckerComponentIT()).getIterator();
        //EventRunner batchStructureChecker = new EventRunner(iterator);
        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");
        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);
        List<TreeEventHandler> handlers = new ArrayList<>();
        TreeNodeState treeNodeState = new TreeNodeState();
        handlers.add(treeNodeState);
        handlers.add(new BatchNodeChecker(batch, resultCollector, treeNodeState));
        //batchStructureChecker.runEvents(handlers);
    }
}
