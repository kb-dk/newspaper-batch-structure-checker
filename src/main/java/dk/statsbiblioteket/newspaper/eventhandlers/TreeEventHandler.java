package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeEndParsingEvent;

/**
 * Interface for tree event handlers
 */
public interface TreeEventHandler {
    public void handleNodeBegin(NodeBeginsParsingEvent event);
    public void handleNodeEnd(NodeEndParsingEvent event);
    public void handleAttribute(AttributeParsingEvent event);
}
