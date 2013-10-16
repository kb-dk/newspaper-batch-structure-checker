package dk.statsbiblioteket.newspaper;

import java.util.Arrays;
import java.util.List;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.ParsingEventType;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import dk.statsbiblioteket.newspaper.eventhandlers.TreeEventHandler;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;

/**
 * Test of the BatchStructureChecker class.
 */
public class BatchStructureCheckerTest {

    /**
     * Verifies that the BatchStructureChecker correctly passes the event for a simple batch with two leafs
     * to the attached event handlers.
     */
    @Test
    public void testStructureCalls() throws Exception {
        // Setup fixture
        TreeIterator treeIteratorMock = mock(TreeIterator.class);
        when(treeIteratorMock.hasNext()).
                thenReturn(true).thenReturn(true).thenReturn(true).//Begins
                thenReturn(true).thenReturn(true).                 //Attributes
                thenReturn(true).thenReturn(true).thenReturn(true).//Ends
                thenReturn(false);
        NodeBeginsParsingEvent batchNodeBegin = new NodeBeginsParsingEvent("BatchNode");
        NodeBeginsParsingEvent reelNodeBegin = new NodeBeginsParsingEvent("ReelNode");
        NodeBeginsParsingEvent dateNodeBegin = new NodeBeginsParsingEvent("DateNode");
        AttributeParsingEvent pageJp2Attribute = createAttributeParsingEventStub("pageJp2Attribute");
        AttributeParsingEvent pageXmlAttribute = createAttributeParsingEventStub("pageXmlAttribute");
        NodeEndParsingEvent dateNodeEnd = new NodeEndParsingEvent("DateNode");
        NodeEndParsingEvent reelNodeEnd = new NodeEndParsingEvent("ReelNode");
        NodeEndParsingEvent batchNodeEnd = new NodeEndParsingEvent("BatchNode");
        when(treeIteratorMock.next()).
                thenReturn(batchNodeBegin).
                thenReturn(reelNodeBegin).
                thenReturn(dateNodeBegin).
                thenReturn(pageJp2Attribute).
                thenReturn(pageXmlAttribute).
                thenReturn(dateNodeEnd).
                thenReturn(reelNodeEnd).
                thenReturn(batchNodeEnd);

        ResultCollector resultCollectorMock = mock(ResultCollector.class);
        TreeEventHandler treeEventHandlerMock = mock(TreeEventHandler.class);

        //Perform test
        BatchStructureChecker batchStructureCheckerUT = new BatchStructureChecker(treeIteratorMock);
        List<TreeEventHandler> eventHandlers = Arrays.asList(new TreeEventHandler[]{treeEventHandlerMock});
        batchStructureCheckerUT.checkBatchStructure(eventHandlers, resultCollectorMock);

        //Verify
        verify(treeEventHandlerMock).handleNodeBegin(batchNodeBegin);
        verify(treeEventHandlerMock).handleNodeBegin(reelNodeBegin);
        verify(treeEventHandlerMock).handleNodeBegin(dateNodeBegin);
        verify(treeEventHandlerMock).handleAttribute(pageJp2Attribute);
        verify(treeEventHandlerMock).handleAttribute(pageXmlAttribute);
        verify(treeEventHandlerMock).handleNodeEnd(dateNodeEnd);
        verify(treeEventHandlerMock).handleNodeEnd(reelNodeEnd);
        verify(treeEventHandlerMock).handleNodeEnd(batchNodeEnd);
        verify(treeEventHandlerMock).handleFinish();
        verifyNoMoreInteractions(treeEventHandlerMock);

        verifyNoMoreInteractions(resultCollectorMock);
    }

    /**
     * @return Creates a attribute event and marks is as type 'Attribute'.
     */
    private AttributeParsingEvent createAttributeParsingEventStub(final String name) {
        AttributeParsingEvent event = mock(AttributeParsingEvent.class);
        when(event.getType()).thenReturn(ParsingEventType.Attribute);
        return event;
    }
}
