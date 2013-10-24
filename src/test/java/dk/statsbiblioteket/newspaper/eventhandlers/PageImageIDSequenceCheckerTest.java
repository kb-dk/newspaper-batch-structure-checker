package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class PageImageIDSequenceCheckerTest {
    private TreeNodeState treeNodeState;
    private PageImageIDSequenceChecker checker;
    private ResultCollector resultCollector;

    @BeforeMethod
    public void setupBilledIDSequenceChecker () {
        resultCollector = mock(ResultCollector.class);
        treeNodeState = mock(TreeNodeState.class);
        checker = new PageImageIDSequenceChecker(resultCollector, treeNodeState);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void pageSimpleSuccessTest() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("First page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0001.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Second page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0002.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Third page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0003.jp2"));

        finishFilm();
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void pageHighStartFailureTest() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("First page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0004.jp2"));

        finishFilm();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void pageABSuccessTest() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("First page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0001.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Second page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0002A.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Third page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0002B.jp2"));

        finishFilm();
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void simpleMissingPageTest() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("First page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0001.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Third page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0003.jp2"));

        finishFilm();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void missingFinalBPageTest() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("First page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0001.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Last page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0002A.jp2"));

        finishFilm();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void missingBPageTest() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("First page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0001.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Second page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0002A.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Final page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0003.jp2"));

        finishFilm();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void cPageTest() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("First page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0001.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Second page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0002A.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Final page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0002B.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Final page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0002C.jp2"));

        finishFilm();
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void cPageMissingBPageTest() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("First page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0001.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Second page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0002A.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Final page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0002C.jp2"));

        finishFilm();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void pageMultiFilmResetTest() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("First page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0001.jp2"));
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Second page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0002.jp2"));
        finishFilm();
        verifyNoMoreInteractions(resultCollector);

        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("Final page", NodeType.PAGE_IMAGE, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("JP2-PAGE_IMAGE-0003.jp2"));

        finishFilm();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    private void finishFilm() {
        when(treeNodeState.getCurrentNode()).
                thenReturn(new TreeNode("Finished Udgave", NodeType.FILM, null)).
                thenReturn(new TreeNode("Finished Udgave", NodeType.BATCH, null));
        checker.handleNodeEnd(new NodeEndParsingEvent(""));
        checker.handleNodeEnd(new NodeEndParsingEvent(""));
    }
}
