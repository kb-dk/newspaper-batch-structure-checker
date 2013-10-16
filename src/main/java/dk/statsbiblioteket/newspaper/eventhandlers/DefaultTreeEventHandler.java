package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeEndParsingEvent;

/**
 * Abstract tree event handler, with no-op methods
 */
public abstract class DefaultTreeEventHandler implements TreeEventHandler {
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
    }
}
