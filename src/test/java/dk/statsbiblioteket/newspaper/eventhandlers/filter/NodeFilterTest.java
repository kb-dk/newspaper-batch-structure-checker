package dk.statsbiblioteket.newspaper.eventhandlers.filter;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.newspaper.eventhandlers.TestEventHelper;
import dk.statsbiblioteket.newspaper.eventhandlers.TreeEventHandler;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

public class NodeFilterTest {
    @Test
    public void udgaveNodeTest() throws Exception {
        TreeEventHandler treeEventHandlerMock = mock(TreeEventHandler.class);
        NodeFilter filter = new IncludeNodeFilter(NodeType.UDGAVE, treeEventHandlerMock);

        NodeBeginsParsingEvent batchNodeBegin = TestEventHelper.createBatchBeginEvent(1);
        NodeBeginsParsingEvent reelNodeBegin = TestEventHelper.createReelBeginEvent(1);
        NodeBeginsParsingEvent dateNodeBegin = TestEventHelper.createUdgaveBeginEvent(1);
        AttributeParsingEvent pageJp2Attribute = TestEventHelper.createAttributeParsingEventStub("pageJp2Attribute");
        NodeEndParsingEvent dateNodeEnd = new NodeEndParsingEvent(batchNodeBegin.getName());
        NodeEndParsingEvent reelNodeEnd = new NodeEndParsingEvent(reelNodeBegin.getName());
        NodeEndParsingEvent batchNodeEnd = new NodeEndParsingEvent(dateNodeBegin.getName());

        filter.handleNodeBegin(batchNodeBegin);
        verify(treeEventHandlerMock).handleNodeBegin(batchNodeBegin);
    }
}
