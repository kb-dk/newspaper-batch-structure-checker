package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 10/21/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class BatchIDAndRoundtripCheckerTest {

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
        BatchIDAndRoundtripChecker checker = new BatchIDAndRoundtripChecker(batch, resultCollector, state);
        NodeBeginsParsingEvent event = new NodeBeginsParsingEvent("B" + batch_id + "-RT" + roundtrips);
        checker.handleNodeBegin(event);
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
        BatchIDAndRoundtripChecker checker = new BatchIDAndRoundtripChecker(batch, resultCollector, state);
        NodeBeginsParsingEvent event = new NodeBeginsParsingEvent("B40002202821" + "-RT" + roundtrips);
        checker.handleNodeBegin(event);
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
        BatchIDAndRoundtripChecker checker = new BatchIDAndRoundtripChecker(batch, resultCollector, state);
        NodeBeginsParsingEvent event = new NodeBeginsParsingEvent("1234_helloworld.foobar");
        checker.handleNodeBegin(event);
        assertFalse(resultCollector.isSuccess());
    }
}
