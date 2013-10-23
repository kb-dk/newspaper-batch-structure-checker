package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 23/10/13
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
public class FilmNodeCheckerTest {

    static class SettableTreeNodeState extends TreeNodeState {
        private TreeNode currentNode;

        SettableTreeNodeState(TreeNode currentNode) {
            this.currentNode = currentNode;
        }

        public void setCurrentNode(TreeNode node) {
            currentNode = node;
        }

        @Override
        public TreeNode getCurrentNode() {
            return currentNode;
        }
    }

    /**
     * Simulate parsing a FILM node with an appropriate sequence of parsing events.
     */
    @Test
    public void testChecker() {
        String batch_id = "400022028241";
        Batch batch = new Batch();
        batch.setBatchID(batch_id);
        batch.setRoundTripNumber(1);
        final TreeNode node = new TreeNode(null, NodeType.FILM, null);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SettableTreeNodeState state = new SettableTreeNodeState(node);
        FilmNodeChecker checker = new FilmNodeChecker(batch, state, resultCollector);
        NodeBeginsParsingEvent filmStartEvent = new NodeBeginsParsingEvent("400022028241-14");
        AttributeParsingEvent filexmlEvent = new FileAttributeParsingEvent("Politiken-" +batch_id + "-14.film.xml", new File("foobar"));
        NodeBeginsParsingEvent firstEditionStart = new NodeBeginsParsingEvent("2001-01-01-03");
        NodeEndParsingEvent firstEditionEnd = new NodeEndParsingEvent("2001-01-01-03");
        NodeBeginsParsingEvent secondEditionStart = new NodeBeginsParsingEvent("2001-01-01-04");
        NodeEndParsingEvent secondEditionEnd = new NodeEndParsingEvent("2001-01-01-04");
        NodeEndParsingEvent filmEndEvent = new NodeEndParsingEvent("400022028241-14");
        checker.handleNodeBegin(filmStartEvent);
        checker.handleAttribute(filexmlEvent);
        state.setCurrentNode(new TreeNode(null, NodeType.EDITION, null));
        checker.handleNodeBegin(firstEditionStart);
        checker.handleNodeEnd(firstEditionEnd);
        checker.handleNodeBegin(secondEditionStart);
        checker.handleNodeEnd(secondEditionEnd);
        state.setCurrentNode(new TreeNode(null, NodeType.FILM, null));
        checker.handleNodeEnd(filmEndEvent);
        assertTrue(resultCollector.isSuccess());
    }

    @Test
    public void testNoFilmXml() {
        String batch_id = "400022028241";
        Batch batch = new Batch();
        batch.setBatchID(batch_id);
        batch.setRoundTripNumber(1);
        final TreeNode node = new TreeNode(null, NodeType.FILM, null);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SettableTreeNodeState state = new SettableTreeNodeState(node);
        FilmNodeChecker checker = new FilmNodeChecker(batch, state, resultCollector);
        NodeBeginsParsingEvent filmStartEvent = new NodeBeginsParsingEvent("400022028241-14");
        NodeBeginsParsingEvent firstEditionStart = new NodeBeginsParsingEvent("2001-01-01-03");
        NodeEndParsingEvent firstEditionEnd = new NodeEndParsingEvent("2001-01-01-03");
        NodeEndParsingEvent filmEndEvent = new NodeEndParsingEvent("400022028241-14");
        checker.handleNodeBegin(filmStartEvent);
        state.setCurrentNode(new TreeNode(null, NodeType.EDITION, null));
        checker.handleNodeBegin(firstEditionStart);
        checker.handleNodeEnd(firstEditionEnd);
        state.setCurrentNode(new TreeNode(null, NodeType.FILM, null));
        checker.handleNodeEnd(filmEndEvent);
        assertFalse(resultCollector.isSuccess());
    }

    /**
     * Simulate parsing a FILM node with an appropriate sequence of parsing events.
     */
    @Test
    public void testCheckerNonConsecutiveEditions() {
        String batch_id = "400022028241";
        Batch batch = new Batch();
        batch.setBatchID(batch_id);
        batch.setRoundTripNumber(1);
        final TreeNode node = new TreeNode(null, NodeType.FILM, null);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SettableTreeNodeState state = new SettableTreeNodeState(node);
        FilmNodeChecker checker = new FilmNodeChecker(batch, state, resultCollector);
        NodeBeginsParsingEvent filmStartEvent = new NodeBeginsParsingEvent("400022028241-14");
        AttributeParsingEvent filexmlEvent = new FileAttributeParsingEvent("Politiken-" +batch_id + "-14.film.xml", new File("foobar"));
        NodeBeginsParsingEvent firstEditionStart = new NodeBeginsParsingEvent("2001-01-01-03");
        NodeEndParsingEvent firstEditionEnd = new NodeEndParsingEvent("2001-01-01-03");
        NodeBeginsParsingEvent secondEditionStart = new NodeBeginsParsingEvent("2001-01-01-05");
        NodeEndParsingEvent secondEditionEnd = new NodeEndParsingEvent("2001-01-01-05");
        NodeEndParsingEvent filmEndEvent = new NodeEndParsingEvent("400022028241-14");
        checker.handleNodeBegin(filmStartEvent);
        checker.handleAttribute(filexmlEvent);
        state.setCurrentNode(new TreeNode(null, NodeType.EDITION, null));
        checker.handleNodeBegin(firstEditionStart);
        checker.handleNodeEnd(firstEditionEnd);
        checker.handleNodeBegin(secondEditionStart);
        checker.handleNodeEnd(secondEditionEnd);
        state.setCurrentNode(new TreeNode(null, NodeType.FILM, null));
        checker.handleNodeEnd(filmEndEvent);
        assertFalse(resultCollector.isSuccess());
    }

}
