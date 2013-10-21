package dk.statsbiblioteket.newspaper.eventhandlers.filter;

import dk.statsbiblioteket.newspaper.eventhandlers.TreeEventHandler;
import org.testng.annotations.Test;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;

import static org.mockito.Mockito.*;

public class LeafFilterTest {
    @Test
    public void mixXmlLeafTest() throws Exception {
        TreeEventHandler treeEventHandlerMock = mock(TreeEventHandler.class);
        LeafFilter filter = new LeafFilter(LeafType.MIX_XML, treeEventHandlerMock);

        AttributeParsingEvent testMixXmlEvent = mock(AttributeParsingEvent.class);
        when(testMixXmlEvent.getName()).thenReturn("paper-issue-number.mix.xml");
        filter.handleAttribute(testMixXmlEvent);
        verify(treeEventHandlerMock).handleAttribute(testMixXmlEvent);
        verifyNoMoreInteractions(treeEventHandlerMock);


        AttributeParsingEvent testAltoXmlEvent = mock(AttributeParsingEvent.class);
        when(testAltoXmlEvent.getName()).thenReturn("paper-issue-number.alto.xml");
        filter.handleAttribute(testAltoXmlEvent);
        verifyNoMoreInteractions(treeEventHandlerMock);

        NodeBeginsParsingEvent nodeEvent = new NodeBeginsParsingEvent("NodeEvent");
        filter.handleNodeBegin(nodeEvent);
        verifyNoMoreInteractions(treeEventHandlerMock);
    }

    @Test
    public void jp2LeafTest() throws Exception {
        TreeEventHandler treeEventHandlerMock = mock(TreeEventHandler.class);
        LeafFilter filter = new LeafFilter(LeafType.JP2, treeEventHandlerMock);

        AttributeParsingEvent testJp2Event = mock(AttributeParsingEvent.class);
        when(testJp2Event.getName()).thenReturn("paper-issue-number.jp2");
        filter.handleAttribute(testJp2Event);
        verify(treeEventHandlerMock).handleAttribute(testJp2Event);
        verifyNoMoreInteractions(treeEventHandlerMock);


        AttributeParsingEvent testBrikEvent = mock(AttributeParsingEvent.class);
        when(testBrikEvent.getName()).thenReturn("paper-issue-number-brik.jp2");
        filter.handleAttribute(testBrikEvent);
        verifyNoMoreInteractions(treeEventHandlerMock);

        NodeBeginsParsingEvent nodeEvent = new NodeBeginsParsingEvent("NodeEvent");
        filter.handleNodeBegin(nodeEvent);
        verifyNoMoreInteractions(treeEventHandlerMock);
    }

    @Test
    public void brikLeafTest() throws Exception {
        TreeEventHandler treeEventHandlerMock = mock(TreeEventHandler.class);
        LeafFilter filter = new LeafFilter(LeafType.BRIK, treeEventHandlerMock);

        AttributeParsingEvent testJp2Event = mock(AttributeParsingEvent.class);
        when(testJp2Event.getName()).thenReturn("paper-issue-number-brik.jp2");
        filter.handleAttribute(testJp2Event);
        verify(treeEventHandlerMock).handleAttribute(testJp2Event);
        verifyNoMoreInteractions(treeEventHandlerMock);


        AttributeParsingEvent testBrikEvent = mock(AttributeParsingEvent.class);
        when(testBrikEvent.getName()).thenReturn("paper-issue-number.jp2");
        filter.handleAttribute(testBrikEvent);
        verifyNoMoreInteractions(treeEventHandlerMock);

        NodeBeginsParsingEvent nodeEvent = new NodeBeginsParsingEvent("NodeEvent");
        filter.handleNodeBegin(nodeEvent);
        verifyNoMoreInteractions(treeEventHandlerMock);
    }
}
