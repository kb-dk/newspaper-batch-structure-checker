package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.ParsingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: jrg
 * Date: 10/15/13
 * Time: 3:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class ChecksumExistenceChecker extends DefaultTreeEventHandler {
    private static Logger log = LoggerFactory.getLogger(ChecksumExistenceChecker.class);
    private final ResultCollector resultCollector;

    public ChecksumExistenceChecker(ResultCollector r) {
        resultCollector = r;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (!hasChecksum(event)) {
            resultCollector.addFailure("ref", "typ", "comp", "Missing checksum for " + event.getLocalname());
        }
    }

    /**
     * Returns whether event has a checksum attached. An event being either a file or a DOMS object representing a file.
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

        if (checksum == null) {
            return false;
        }

        return true;

    }
}
