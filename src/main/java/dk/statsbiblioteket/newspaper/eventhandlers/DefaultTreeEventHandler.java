package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeEndParsingEvent;

/**
 * Created with IntelliJ IDEA.
 * User: jrg
 * Date: 10/15/13
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
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
