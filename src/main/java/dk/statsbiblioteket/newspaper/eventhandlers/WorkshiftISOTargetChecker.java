package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Performs various checks on WORKSHIFT-ISO-TARGET folder and its files.
 *
 * The expected structure of folders (node begin/end) and files (attributes) :
 *
 * begin "WORKSHIFT-ISO-TARGET"
 *      begin Target-dddddd-dddd
 *          attr Target-dddddd-dddd.mix.xml
 *          begin Target-dddddd-dddd.jp2
 *              attr "contents"
 *          end Target-dddddd-dddd.jp2
 *      end Target-dddddd-dddd
 * end "WORKSHIFT-ISO-TARGET"
 *
 * This class checks that
 * - There are no files (attributes) in WORKSHIFT-ISO-TARGET
 * - There ARE Target-dddddd-dddd folder(s) with names of that format
 * - There are no folders with other names
 * - The targetSerialisedNumbers are sequential starting at 1           TODO
 *
 * @author jrg
 */
public class WorkshiftISOTargetChecker extends AbstractNodeChecker {
    private static Logger log = LoggerFactory.getLogger(BilledIDSequenceChecker.class);

    private final String WORKSHIFT_ISO_TARGET_NAME = "WORKSHIFT-ISO-TARGET";

    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    private List<String> targetSerialisedNumbers = new ArrayList<>();
    private boolean targetFilesExist = false;


    /**
     * Constructor
     * @param r ResultCollector vessel to put result into
     * @param treeNodeState State information of the current node
     */
    public WorkshiftISOTargetChecker(ResultCollector r, TreeNodeState treeNodeState) {
        resultCollector = r;
        this.treeNodeState = treeNodeState;
    }

    @Override
    public void doCheck() {
        // Check: There are no files (attributes) in WORKSHIFT-ISO-TARGET
        if (!attributes.isEmpty()) {
            for (String attribute : attributes) {
                resultCollector.addFailure(name, "filestructure", this.getClass().getName(),
                        "Unexpected file: '" + attribute + "'");
            }
        }

        // Check: There ARE Target-dddddd-dddd folder(s) with names of that format
        // and: There are no folders with other names
        boolean targetFoldersExist = false;
        for (String nodeName : childNodes) {
            if (correctTargetFolderName(nodeName)) {
                targetFoldersExist = true;
            } else {
                resultCollector.addFailure(name, "filestructure", this.getClass().getName(),
                        "Unexpected folder: '" + nodeName + "'");
            }
        }
        if (!targetFoldersExist) {
            // Note that at Ninestars, our "target folder" are actually files
            resultCollector.addFailure(name, "filestructure", this.getClass().getName(),
                    "Error: no targets under " + WORKSHIFT_ISO_TARGET_NAME);
        }


    }

    @Override
    public NodeType getNodeType() {
        return NodeType.WORKSHIFT_ISO_TARGET;
    }

    @Override
    public TreeNodeState getCurrentState() {
        return treeNodeState;
    }

    private boolean correctTargetFolderName(String name) {
        // Desired format: "Target-[targetSerialisedNumber]-[billedID]"
        Pattern pattern = Pattern.compile("^Target-(\\d{6})-(\\d{4})$");
        Matcher matcher = pattern.matcher(name);

        if (matcher.find()) {
            return true;
        } else {
            return false;
        }

    }

    private void collectTargetFolderNumber(String name) {
        // Desired format: "Target-[targetSerialisedNumber]-[billedID]"
        Pattern pattern = Pattern.compile("^Target-(\\d{6})-(\\d{4})$");
        Matcher matcher = pattern.matcher(name);

        if (matcher.find()) {
            targetSerialisedNumbers.add(matcher.group(1));
        }
    }














    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        // If this is not a Target-dddddd-dddd folder,
        // and our parent is a WORKSHIFT-ISO-TARGET folder, it's an error TODO

        // If this is not a Target-dddddd-dddd.jp2 folder,
        // our parent is a Target-dddddd-dddd folder,
        // and out grandparent is a WORKSHIFT-ISO-TARGET folder, it's an error TODO




        // If this is not a WORKSHIFT-ISO-TARGET folder, but our parent is, it's an error
        if (!treeNodeState.getCurrentNode().getName().equals(WORKSHIFT_ISO_TARGET_NAME)
                && treeNodeState.getCurrentNode().getParent().getName().equals(WORKSHIFT_ISO_TARGET_NAME)) {
            resultCollector.addFailure(treeNodeState.getCurrentNode().getName(), "filestructure",
                    "WorkshiftISOTargetChecker", "Unexpected directory in " + WORKSHIFT_ISO_TARGET_NAME);
        }
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        // Grandparent folder must be called WORKSHIFT-ISO-TARGET
        if (!treeNodeState.getCurrentNode().getParent().getParent().getName().equals(WORKSHIFT_ISO_TARGET_NAME)) {
            return;
        }

        // String attributeName = event.getName();


        // Mark current jp2 or mix file as found TODO

        // Collect targetSerialisedNumbers and billedIDs for later processing
        //if (treeNodeState.getCurrentNode().getType().equals(NodeType.IMAGE)) {
        //targetSerialisedNumbers.add(event.getName()); // TODO fix
        //}
    }

    @Override
    public void handleFinish() {
        if (!targetFilesExist) {
            resultCollector.addFailure(WORKSHIFT_ISO_TARGET_NAME, "filestructure",
                    "WorkshiftISOTargetChecker", "No target files found");
        }

        // Check that there is exactly one mix file for each jp2, and vice versa TODO

        // Check that collected targetSerialisedNumbers are sequential and starting at 1 TODO
    }
}
