package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.eventhandlers.sequencechecker.WorkshiftIsoTargetSequenceChecker;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNode;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class WorkshiftIsoTargetSequenceCheckerTest {
    private TreeNodeState treeNodeState;
    private WorkshiftIsoTargetSequenceChecker checker;
    private ResultCollector resultCollector;

    @BeforeMethod
    public void setupBilledIDSequenceChecker () {
        resultCollector = mock(ResultCollector.class);
        treeNodeState = mock(TreeNodeState.class);
        checker = new WorkshiftIsoTargetSequenceChecker(resultCollector, treeNodeState);
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void simpleSuccessTest() {
        registerWorkshiftIsoTargetBegin();

        registerWorkshiftTarget("Target-000387-0001");
        registerWorkshiftTarget("Target-000387-0002");

        registerWorkshiftIsoTargetEnd();
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void pageHighStartFailureTest() {
        registerWorkshiftIsoTargetBegin();

        registerWorkshiftTarget("Target-000387-0002");

        registerWorkshiftIsoTargetEnd();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void simpleMissingEditionTest() {
        registerWorkshiftIsoTargetBegin();

        registerWorkshiftTarget("Target-000387-0001");
        registerWorkshiftTarget("Target-000387-0003");

        registerWorkshiftIsoTargetEnd();
        verify(resultCollector).addFailure(anyString(), anyString(), anyString(), anyString());
        verifyNoMoreInteractions(resultCollector);
    }

    @Test
    public void multipleDatesTest() {
        registerWorkshiftIsoTargetBegin();

        registerWorkshiftTarget("Target-000387-0001");
        registerWorkshiftTarget("Target-000388-0001");

        registerWorkshiftIsoTargetEnd();
        verifyNoMoreInteractions(resultCollector);
    }

    private void registerWorkshiftTarget(String edition) {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode(edition, NodeType.WORKSHIFT_TARGET, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent(edition));
        when(treeNodeState.getPreviousNode()).thenReturn(new TreeNode(edition, NodeType.WORKSHIFT_TARGET, null));
        checker.handleNodeEnd(new NodeEndParsingEvent(edition));
    }

    private void registerWorkshiftIsoTargetBegin() {
        when(treeNodeState.getCurrentNode()).thenReturn(new TreeNode("WORKSHIFT-ISO-TARGET", NodeType.WORKSHIFT_ISO_TARGET, null));
        checker.handleNodeBegin(new NodeBeginsParsingEvent("WORKSHIFT-ISO-TARGET"));
    }

    private void registerWorkshiftIsoTargetEnd() {
        when(treeNodeState.getPreviousNode()).thenReturn(new TreeNode("WORKSHIFT-ISO-TARGET", NodeType.WORKSHIFT_ISO_TARGET, null));
        checker.handleNodeEnd(new NodeEndParsingEvent("WORKSHIFT-ISO-TARGET"));
    }
}