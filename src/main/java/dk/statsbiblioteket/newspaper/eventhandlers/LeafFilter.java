package dk.statsbiblioteket.newspaper.eventhandlers;

import java.util.Arrays;
import java.util.List;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;

/**
 * May be used to filter the leaf events according to certain leaf types.
 */
public class LeafFilter extends DefaultTreeEventHandler {
    private final List<LeafType> allowedTypes;
    private final TreeEventHandler leafHandler;

    public LeafFilter(List<LeafType> allowedTypes, TreeEventHandler leafHandler) {
        this.allowedTypes = allowedTypes;
        this.leafHandler = leafHandler;
    }

    public LeafFilter(LeafType allowedType, TreeEventHandler leafHandler) {
        this.allowedTypes = Arrays.asList(new LeafType[] { allowedType });
        this.leafHandler = leafHandler;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        for (LeafType leafType : allowedTypes) {
            if(event.getLocalname().endsWith(leafType.value())) {
                leafHandler.handleAttribute(event);
                break;
            }
        }
    }
}
