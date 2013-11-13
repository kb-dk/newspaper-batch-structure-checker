package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.FilmIsoTargetSequenceChecker;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class FilmIsoTargetSequenceCheckerTest {
    private TreeNodeState treeNodeState;
    private FilmIsoTargetSequenceChecker checker;
    private ResultCollector resultCollector;

    @BeforeMethod
    public void setupBilledIDSequenceChecker () {
        resultCollector = mock(ResultCollector.class);
        treeNodeState = mock(TreeNodeState.class);
        checker = new FilmIsoTargetSequenceChecker(resultCollector, treeNodeState);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void simpleSuccessTest() {
        registerFilmIsoTargetBegin();

        registerFilmTarget(1);
        registerFilmTarget(2);

        registerFilmIsoTargetEnd();
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void pageHighStartFailureTest() {
        registerFilmIsoTargetBegin();

        registerFilmTarget(2);

        registerFilmIsoTargetEnd();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void simpleMissingTargetTest() {
        registerFilmIsoTargetBegin();

        registerFilmTarget(1);
        registerFilmTarget(3);

        registerFilmIsoTargetEnd();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    private void registerFilmTarget(int number) {
        String targetname = "Papername-filmid-ISO-" + number;
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode(targetname, NodeType.FILM_TARGET, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent(targetname));
        when(treeNodeState.getPreviousNode()).thenReturn(new TreeNode(targetname, NodeType.FILM_TARGET, null));
        checker.handleNodeEnd(new NodeEndParsingEvent(targetname));
    }

    private void registerFilmIsoTargetBegin() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("FILM-ISO-TARGET", NodeType.FILM_ISO_TARGET, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("FILM-ISO-TARGET"));
    }

    private void registerFilmIsoTargetEnd() {
        when(treeNodeState.getPreviousNode()).thenReturn(new TreeNode("FILM-ISO-TARGET", NodeType.FILM_ISO_TARGET, null));
        checker.handleNodeEnd(new NodeEndParsingEvent("FILM-ISO-TARGET"));
    }
}
