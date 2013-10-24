package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implements batch-level checks as defined in  https://sbforge.org/display/NEWSPAPER/Batch+structure+checks
 */
public class BatchNodeChecker extends AbstractNodeChecker {

    private Batch batch;
    private TreeNodeState state;
    private ResultCollector resultCollector;

    private static final String batchNamePatternString = "B([0-9]*)-RT[0-9]*";
    private static final Pattern batchNamePattern = Pattern.compile(batchNamePatternString);

    /**
     * TODO add a BatchContext parameter containing the batch information extracted from MF-PAK.
     * @param batch
     * @param resultCollector
     * @param state
     */
    public BatchNodeChecker(Batch batch, ResultCollector resultCollector, TreeNodeState state) {
        this.batch = batch;
        this.resultCollector = resultCollector;
        this.state = state;
    }

    @Override
    public void doCheck() {
        if (!batch.getFullID().equals(name)) {
            addFailure("batchnamemismatch", "Directory name " + name + " does not match batch name " + batch.getFullID());
        }
        if (!attributes.isEmpty()) {
            for (String attribute: attributes) {
                addFailure("unexpectedfile", "Found unexpected file " + Util.getLastTokenInPath(attribute) + " in " + name);
            }
        }
        String batchNumberString = null;
        Matcher m = batchNamePattern.matcher(Util.getLastTokenInPath(name));
        if (!m.matches()) {
            addFailure("batchname", "The name of the batch '" + name + "' does not match the expected pattern");
            return;
        } else {
            batchNumberString = m.group(1);
        }
        boolean foundWorkshopIsoTarget = false;
        int numberOfFilmDirectories = 0; //TODO This should equal number of BatchContent entries for this batch in MF-PAK
                                         //TODO Also collect file suffices and check that they are sequential?

        for (String childNode: childNodes) {
            String childNodeName = Util.getLastTokenInPath(childNode);
            switch (childNodeName) {
                case("WORKSHIFT-ISO-TARGET"):
                    foundWorkshopIsoTarget = true;
                    break;
                default:    //Should be a film directory
                    String expectedName = batchNumberString + "-[0-9]{2}";
                    if (!childNodeName.matches(expectedName)) {
                        addFailure("unexpecteddirectory", "Found unexpected directory " + childNodeName + " in " + name);
                    } else {
                        numberOfFilmDirectories++;
                    }
            }
        }
        if (!foundWorkshopIsoTarget) {
            addFailure("missingdirectory", "No WORKSHOP-ISO-TARGET directory found in " + name);
        }
    }

    private void addFailure(String type, String description) {
        resultCollector.addFailure(name, type, this.getClass().getName(), description);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.BATCH;
    }

    @Override
    public TreeNodeState getCurrentState() {
        return state;
    }
}
