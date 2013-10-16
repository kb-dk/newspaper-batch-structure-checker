package dk.statsbiblioteket.newspaper;

import java.io.IOException;
import java.util.List;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.ParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import dk.statsbiblioteket.newspaper.eventhandlers.TreeEventHandler;

/**
 * Checks the directory structure of a batch. This should run both at Ninestars and at SB.
 *
 * @author jrg
 */
public class BatchStructureChecker {
    private TreeIterator iterator;

    public BatchStructureChecker(TreeIterator iterator) {
        this.iterator = iterator;
    }

    /**
     * Check the batch structure tree received for errors.
     *
     * @param resultCollector Object to collect results of the structure check
     * @throws IOException
     */
    public void checkBatchStructure(List<TreeEventHandler> eventHandlers, ResultCollector resultCollector)
            throws IOException {
        while (iterator.hasNext()) {
            ParsingEvent next = iterator.next();

            switch (next.getType()){
                case NodeBegin: {
                    for (TreeEventHandler handler : eventHandlers) {
                        handler.handleNodeBegin((NodeBeginsParsingEvent)next);
                    }
                    break;
                }
                case NodeEnd: {
                    for (TreeEventHandler handler : eventHandlers) {
                        handler.handleNodeEnd((NodeEndParsingEvent) next);
                    }
                    break;
                }
                case Attribute: {
                    for (TreeEventHandler handler : eventHandlers) {
                        handler.handleAttribute((AttributeParsingEvent) next);
                    }
                    break;
                }
            }
        }

        for (TreeEventHandler handler : eventHandlers) {
            handler.handleFinish();
        }
    }

    /**
     * Returns whether the name of the given event has the given extension
     *
     * @param event The event whose name is to be checked
     * @param extension The extension to check for
     * @return Whether the name of the given event has the given extension
     */
    private boolean hasExtension(ParsingEvent event, String extension) {
        return event.getLocalname().endsWith("." + extension);
    }

}
