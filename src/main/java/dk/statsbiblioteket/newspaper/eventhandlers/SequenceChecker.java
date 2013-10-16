package dk.statsbiblioteket.newspaper.eventhandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the the scanned pages are named in sequence. The image files for the scanned pages are used for the sequence
 * number check The rules are: <ol>
 *     <li>sequence numbers are in the format NNNN or NNNNA/NNNNB, the later in case of two pages on a single physical
 *     image scan</li>.
 *     <li>The most either on page of the format NNNN or a page pair in the format NNNNA/NNNNB for each int
 *     in the sequence.</li>
 * </ol>
 */
public class SequenceChecker extends DefaultTreeEventHandler {
    private static Logger log = LoggerFactory.getLogger(SequenceChecker.class);
    private final ResultCollector resultCollector;
    private List<String> billedIDs = new ArrayList<>();

    public SequenceChecker(ResultCollector r) {
        resultCollector = r;
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        billedIDs.add(event.getLocalname());
    }

    @Override
    public void handleFinish() {
        //ToDO Check the collected billedIDs, properly by filtering them first.
        Collections.sort(billedIDs);
        for (String billedID : billedIDs) {

        }
    }
}
