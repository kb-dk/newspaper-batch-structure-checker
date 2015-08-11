package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.FilmSuffixSequenceChecker;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static dk.statsbiblioteket.newspaper.eventhandlers.Util.getMethodName;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class FilmSuffixSequenceCheckerTest {
    private TreeNodeState treeNodeState;
    private FilmSuffixSequenceChecker checker;
    private ResultCollector resultCollector;

    @BeforeMethod
    public void setupBilledIDSequenceChecker () {
        resultCollector = mock(ResultCollector.class);
        treeNodeState = mock(TreeNodeState.class);
        checker = new FilmSuffixSequenceChecker(resultCollector, treeNodeState);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void simpleSuccessTest() {
        System.out.println("Running test: " + getMethodName(0));

        registerBatchBegin();

        registerFilm(1);
        registerFilm(2);

        registerBatchEnd();
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void pageHighStartFailureTest() {
        registerBatchBegin();

        registerFilm(2);

        registerBatchEnd();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void simpleMissingTargetTest() {
        registerBatchBegin();

        registerFilm(1);
        registerFilm(3);

        registerBatchEnd();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    private void registerFilm(int number) {
        String targetname = "BatchID-" + number;
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode(targetname, NodeType.FILM, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent(targetname));
        when(treeNodeState.getPreviousNode()).thenReturn(new TreeNode(targetname, NodeType.FILM, null));
        checker.handleNodeEnd(new NodeEndParsingEvent(targetname));
    }

    private void registerBatchBegin() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("BatchID", NodeType.BATCH, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("BatchID"));
    }

    private void registerBatchEnd() {
        when(treeNodeState.getPreviousNode()).thenReturn(new TreeNode("BatchID", NodeType.BATCH, null));
        checker.handleNodeEnd(new NodeEndParsingEvent("BatchID"));
    }
}
