package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.FileAttributeParsingEvent;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Tests for FilmNodeChecker.
 */
public class FilmNodeCheckerTest {

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
        NodeBeginsParsingEvent filmStartEvent = new NodeBeginsParsingEvent("B400022028241-RT1/400022028241-14");
        AttributeParsingEvent filexmlEvent = new FileAttributeParsingEvent("B400022028241-RT1/400022028241-14/Politiken-" +batch_id + "-14.film.xml", new File("foobar"));
        NodeBeginsParsingEvent firstEditionStart = new NodeBeginsParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-03");
        NodeEndParsingEvent firstEditionEnd = new NodeEndParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-03");
        NodeBeginsParsingEvent secondEditionStart = new NodeBeginsParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-04");
        NodeEndParsingEvent secondEditionEnd = new NodeEndParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-04");
        NodeEndParsingEvent filmEndEvent = new NodeEndParsingEvent("B400022028241-RT1/400022028241-14");
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

    /**
     * Test that checking fails when film.xml file is missing,
     */
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
        NodeBeginsParsingEvent filmStartEvent = new NodeBeginsParsingEvent("B400022028241-RT1/400022028241-14");
        NodeBeginsParsingEvent firstEditionStart = new NodeBeginsParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-03");
        NodeEndParsingEvent firstEditionEnd = new NodeEndParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-03");
        NodeEndParsingEvent filmEndEvent = new NodeEndParsingEvent("B400022028241-RT1/400022028241-14");
        checker.handleNodeBegin(filmStartEvent);
        state.setCurrentNode(new TreeNode(null, NodeType.EDITION, null));
        checker.handleNodeBegin(firstEditionStart);
        checker.handleNodeEnd(firstEditionEnd);
        state.setCurrentNode(new TreeNode(null, NodeType.FILM, null));
        checker.handleNodeEnd(filmEndEvent);
        assertFalse(resultCollector.isSuccess());
        assertEquals(Util.countFailures(resultCollector), 1);
    }

    /**
     * Test that checking fails when editions are not consectutive.
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
        NodeBeginsParsingEvent filmStartEvent = new NodeBeginsParsingEvent("B400022028241-RT1/400022028241-14");
        AttributeParsingEvent filexmlEvent = new FileAttributeParsingEvent("B400022028241-RT1/400022028241-14/Politiken-" +batch_id + "-14.film.xml", new File("foobar"));
        NodeBeginsParsingEvent firstEditionStart = new NodeBeginsParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-03");
        NodeEndParsingEvent firstEditionEnd = new NodeEndParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-03");
        NodeBeginsParsingEvent secondEditionStart = new NodeBeginsParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-05");
        NodeEndParsingEvent secondEditionEnd = new NodeEndParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-05");
        NodeEndParsingEvent filmEndEvent = new NodeEndParsingEvent("B400022028241-RT1/400022028241-14");
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
        assertEquals(Util.countFailures(resultCollector), 1);
    }

    /**
     * Test that it is checked that edition number is two characters,
     */
    @Test
    public void testEditionNotTwoCharacters() {
        String batch_id = "400022028241";
        Batch batch = new Batch();
        batch.setBatchID(batch_id);
        batch.setRoundTripNumber(1);
        final TreeNode node = new TreeNode(null, NodeType.FILM, null);
        ResultCollector resultCollector = new ResultCollector("foo", "bar");
        SettableTreeNodeState state = new SettableTreeNodeState(node);
        FilmNodeChecker checker = new FilmNodeChecker(batch, state, resultCollector);
        NodeBeginsParsingEvent filmStartEvent = new NodeBeginsParsingEvent("B400022028241-RT1/400022028241-14");
        AttributeParsingEvent filexmlEvent = new FileAttributeParsingEvent("B400022028241-RT1/400022028241-14/Politiken-" +batch_id + "-14.film.xml", new File("foobar"));
        NodeBeginsParsingEvent firstEditionStart = new NodeBeginsParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-03");
        NodeEndParsingEvent firstEditionEnd = new NodeEndParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-03");
        NodeBeginsParsingEvent secondEditionStart = new NodeBeginsParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-4");
        NodeEndParsingEvent secondEditionEnd = new NodeEndParsingEvent("B400022028241-RT1/400022028241-14/2001-01-01-04");
        NodeEndParsingEvent filmEndEvent = new NodeEndParsingEvent("B400022028241-RT1/400022028241-14");
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
        assertEquals(Util.countFailures(resultCollector), 1);
    }

}
