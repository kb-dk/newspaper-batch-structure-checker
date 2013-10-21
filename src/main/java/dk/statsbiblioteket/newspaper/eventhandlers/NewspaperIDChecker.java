package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;

/**
 * Class that checks the naming of files. 
 * I.e. all files not in WORKSHIFT-ISO-TARGET should start with the newspaper id belonging to the batch 
 */
public class NewspaperIDChecker extends DefaultTreeEventHandler {

    private static final String WORKSHIFT_DIR = "WORKSHIFT-ISO-TARGET";
    
    boolean checkBranch = true;
    private final String newspaperID;
    private final ResultCollector resultCollector;
    
    public NewspaperIDChecker(String newspaperID, ResultCollector resultCollector) {
        this.newspaperID = newspaperID;
        this.resultCollector = resultCollector;
    }
    
    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        if(event.getName().endsWith(WORKSHIFT_DIR)) {
            checkBranch = false;
        }
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        if(event.getName().endsWith(WORKSHIFT_DIR)) {
            checkBranch = true;
        }       
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if(checkBranch) {
             if(!event.getName().startsWith(newspaperID)) {
                 resultCollector.addFailure(event.getName(), "filestructure", "NewspaperIDChecker",
                         "Bad newspaperID for " + event.getName());
             }
        }
    }

    @Override
    public void handleFinish() {
        
    }
}
