package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeEndParsingEvent;

/**
 * Created with IntelliJ IDEA.
 * User: jrg
 * Date: 10/15/13
 * Time: 1:35 PM
 * To change this template use File | Settings | File Templates.
 */
public interface TreeEventHandler {
    public void handleNodeBegin(NodeBeginsParsingEvent event);
    public void handleNodeEnd(NodeEndParsingEvent event);
    public void handleAttribute(AttributeParsingEvent event);
}
