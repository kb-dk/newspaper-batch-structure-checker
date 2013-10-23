package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventHandlerFactory;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.EventRunner;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.newspaper.BatchStructureCheckerComponent;
import dk.statsbiblioteket.newspaper.BatchStructureCheckerComponentIT;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 10/21/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
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
        final TreeNode node = new TreeNode(null, NodeType.BATCH, null);
        TreeNodeState state = new TreeNodeState() {
            @Override
            public TreeNode getCurrentNode() {
                return node;
            }
        };
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchNodeChecker checker = new BatchNodeChecker(batch, resultCollector, state);
        NodeBeginsParsingEvent event = new NodeBeginsParsingEvent("B" + batch_id + "-RT" + roundtrips);
        NodeEndParsingEvent event2 = new NodeEndParsingEvent("B" + batch_id + "-RT" + roundtrips);
        checker.handleNodeBegin(event);
        checker.handleNodeEnd(event2);
        assertTrue(resultCollector.isSuccess());
    }

    @Test
    public void testHandleNodeBeginWrongId() throws Exception {
        String batch_id = "400022028241";
        int roundtrips = 1;
        Batch batch = new Batch();
        batch.setBatchID(batch_id);
        batch.setRoundTripNumber(1);
        final TreeNode node = new TreeNode(null, NodeType.BATCH, null);
        TreeNodeState state = new TreeNodeState() {
            @Override
            public TreeNode getCurrentNode() {
                return node;
            }
        };
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchNodeChecker checker = new BatchNodeChecker(batch, resultCollector, state);
        NodeBeginsParsingEvent event = new NodeBeginsParsingEvent("B40002202821" + "-RT" + roundtrips);
        NodeEndParsingEvent event2 = new NodeEndParsingEvent("B" + batch_id + "-RT" + roundtrips);
        checker.handleNodeBegin(event);
        checker.handleNodeEnd(event2);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void testHandleNodeBeginStrangeFormat() throws Exception {
        String batch_id = "400022028241";
        int roundtrips = 1;
        Batch batch = new Batch();
        batch.setBatchID(batch_id);
        batch.setRoundTripNumber(1);
        final TreeNode node = new TreeNode(null, NodeType.BATCH, null);
        TreeNodeState state = new TreeNodeState() {
            @Override
            public TreeNode getCurrentNode() {
                return node;
            }
        };
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchNodeChecker checker = new BatchNodeChecker(batch, resultCollector, state);
        NodeBeginsParsingEvent event = new NodeBeginsParsingEvent("1234_helloworld.foobar");
        NodeEndParsingEvent event2 = new NodeEndParsingEvent("B" + batch_id + "-RT" + roundtrips);
        checker.handleNodeBegin(event);
        checker.handleNodeEnd(event2);
        assertFalse(resultCollector.isSuccess());
    }

    @Test
    public void testHandleUnexpectedFile() throws Exception {
        String batch_id = "400022028241";
        int roundtrips = 1;
        Batch batch = new Batch();
        batch.setBatchID(batch_id);
        batch.setRoundTripNumber(1);
        final TreeNode node = new TreeNode(null, NodeType.BATCH, null);
        TreeNodeState state = new TreeNodeState() {
            @Override
            public TreeNode getCurrentNode() {
                return node;
            }
        };
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        BatchNodeChecker checker = new BatchNodeChecker(batch, resultCollector, state);
        NodeBeginsParsingEvent event = new NodeBeginsParsingEvent("B" + batch_id + "-RT" + roundtrips);
        AttributeParsingEvent attributeParsingEvent = new FileAttributeParsingEvent("foobar", new File("foobar"));
        NodeEndParsingEvent event2 = new NodeEndParsingEvent("B" + batch_id + "-RT" + roundtrips);
        checker.handleNodeBegin(event);
        checker.handleAttribute(attributeParsingEvent);
        checker.handleNodeEnd(event2);
        assertFalse(resultCollector.isSuccess());
    }

    private final static String TEST_BATCH_ID = "400022028241";

    /**
     * Tests the BatchNodeChecker on real file-based data.
     */
    @Test(groups = "integrationTest")
    public void testBatchNodeCheckerIT() throws Exception {
        String pathToProperties = System.getProperty("integration.test.newspaper.properties");
        Properties properties = new Properties();
        properties.load(new FileInputStream(pathToProperties));
        TreeIterator iterator = (new BatchStructureCheckerComponentIT()).getIterator();
        EventRunner batchStructureChecker = new EventRunner(iterator);
        ResultCollector resultCollector = new ResultCollector("Batch Structure Checker", "v0.1");
        Batch batch = new Batch();
        batch.setBatchID(TEST_BATCH_ID);
        batch.setRoundTripNumber(1);
        List<TreeEventHandler> handlers = new ArrayList<>();
        TreeNodeState treeNodeState = new TreeNodeState();
        handlers.add(treeNodeState);
        handlers.add(new BatchNodeChecker(batch, resultCollector, treeNodeState));
        batchStructureChecker.runEvents(handlers);
    }
}
