package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks that every event has a checksum attached. An event being either a file or a DOMS object
 * representing a file.
 *
 * @author jrg
 */
public class ChecksumExistenceChecker extends DefaultTreeEventHandler {
    private static Logger log = LoggerFactory.getLogger(ChecksumExistenceChecker.class);
    private final ResultCollector resultCollector;

    public ChecksumExistenceChecker(ResultCollector r) {
        resultCollector = r;
    }

    /**
     * Mark it as a failure if given event does not have a checksum
     * @param event Event to be checked
     */
    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (!hasChecksum(event)) {
            resultCollector.addFailure(event.getName(), "filestructure", "ChecksumExistenceChecker",
                    "Missing checksum for " + event.getName());
        }
    }

    /**
     * Returns whether event has a checksum attached. An event being either a file or a DOMS object
     * representing a file.
     *
     * @param event The event to check
     * @return Whether event has a checksum attached
     */
    private boolean hasChecksum(ParsingEvent event) {
        AttributeParsingEvent attributeEvent = (AttributeParsingEvent) event;
        String checksum;
        try {
            checksum = attributeEvent.getChecksum();
        } catch (Exception e) {
            log.warn("Unable to get checksum on attributeEvent", e);
            return false;
        }

        return (checksum != null);

    }
}
