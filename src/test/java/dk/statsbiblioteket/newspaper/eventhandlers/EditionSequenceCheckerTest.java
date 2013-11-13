package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.EditionSequenceChecker;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class EditionSequenceCheckerTest {
    private TreeNodeState treeNodeState;
    private EditionSequenceChecker checker;
    private ResultCollector resultCollector;

    @BeforeMethod
    public void setupBilledIDSequenceChecker () {
        resultCollector = mock(ResultCollector.class);
        treeNodeState = mock(TreeNodeState.class);
        checker = new EditionSequenceChecker(resultCollector, treeNodeState);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void simpleSuccessTest() {
        registerFilmBegin();

        registerEdition("1860-10-18-01");
        registerEdition("1860-10-18-02");

        registerFilmEnd();
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void pageHighStartFailureTest() {
        registerFilmBegin();

        registerEdition("1860-10-18-02");

        registerFilmEnd();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void simpleMissingEditionTest() {
        registerFilmBegin();

        registerEdition("1860-10-18-01");
        registerEdition("1860-10-18-03");

        registerFilmEnd();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void multipleDatesTest() {
        registerFilmBegin();

        registerEdition("1860-10-18-01");
        registerEdition("1860-10-19-01");

        registerFilmEnd();
        verifyNoMoreInteractions(resultCollector);
    }

    private void registerEdition(String edition) {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode(edition, NodeType.EDITION, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent(edition));
        when(treeNodeState.getPreviousNode()).thenReturn(new TreeNode(edition, NodeType.EDITION, null));
        checker.handleNodeEnd(new NodeEndParsingEvent(edition));
    }

    private void registerFilmBegin() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("FilmID", NodeType.FILM, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("FilmID"));
    }

    private void registerFilmEnd() {
        when(treeNodeState.getPreviousNode()).thenReturn(new TreeNode("FilmID", NodeType.FILM, null));
        checker.handleNodeEnd(new NodeEndParsingEvent("FilmID"));
    }
}