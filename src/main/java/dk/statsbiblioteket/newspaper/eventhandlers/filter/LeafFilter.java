
package dk.statsbiblioteket.newspaper.eventhandlers.filter;

import java.util.Arrays;
import java.util.List;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.DefaultTreeEventHandler;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;

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
            if(event.getName().endsWith(leafType.value())) {
                // Special case for jp2 inclusion, brik exclusion.
                if (event.getName().endsWith("brik.jp2") &&
                        !allowedTypes.contains(LeafType.BRIK)) {
                    // Special case for jp2 inclusion, brik exclusion, no hit.
                } else {
                    leafHandler.handleAttribute(event);
                    break;
                }
            }
        }
    }

    @Override
    public void handleFinish() {
        leafHandler.handleFinish();
    }
}
