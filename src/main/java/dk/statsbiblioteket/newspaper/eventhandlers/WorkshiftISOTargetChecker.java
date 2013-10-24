package dk.statsbiblioteket.newspaper.eventhandlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * - The serializedNumbersFromFilenames are sequential starting at 1
 *
 * @author jrg
 */
public class WorkshiftISOTargetChecker extends AbstractNodeChecker {
    private static Logger log = LoggerFactory.getLogger(WorkshiftISOTargetChecker.class);

    private final String WORKSHIFT_ISO_TARGET_NAME = "WORKSHIFT-ISO-TARGET";

    private final ResultCollector resultCollector;
    private final TreeNodeState treeNodeState;
    private List<Integer> serializedNumbersFromFilenames = new ArrayList<>();
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

    /**
     * Called at the end node of folder by the name specified in getNodeType().
     * Does the actual checks.
     */
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
        for (String childNode : childNodes) {
            String nodeName = Util.getLastTokenInPath(childNode);
            if (correctTargetFolderName(nodeName)) {
                targetFoldersExist = true;
                collectTargetFolderNumber(nodeName);
            } else {
                resultCollector.addFailure(name, "filestructure", this.getClass().getName(),
                        "Unexpected folder: '" + nodeName + "'");
            }
        }
        if (!targetFoldersExist) {
            // Note that at Ninestars, our "target folder" are actually files
            resultCollector.addFailure(name, "filestructure", this.getClass().getName(),
                    "No targets under " + WORKSHIFT_ISO_TARGET_NAME);
            return;
        }

        // Check: The serializedNumbersFromFilenames are sequential starting at 1
        Collections.sort(serializedNumbersFromFilenames);
        int firstSerializedNumberFromFilenames = serializedNumbersFromFilenames.get(0);
        int lastSerializedNumberFromFilenames = serializedNumbersFromFilenames.get(serializedNumbersFromFilenames.size() - 1);
        if (firstSerializedNumberFromFilenames != 1) {
            resultCollector.addFailure(name, "filestructure", this.getClass().getName(),
                    "serializedNumbersFromFilenames of targets under " + WORKSHIFT_ISO_TARGET_NAME + " not starting at 1");
        } else {
            for (int wantedSerializedNumber = 1;
                 wantedSerializedNumber < lastSerializedNumberFromFilenames;
                 wantedSerializedNumber++) {
                if (serializedNumbersFromFilenames.indexOf(wantedSerializedNumber) == -1) {
                    resultCollector.addFailure(name, "filestructure", this.getClass().getName(),
                            "serializedNumbersFromFilenames of targets under " + WORKSHIFT_ISO_TARGET_NAME
                                    + " are missing number " + wantedSerializedNumber);
                }
            }
        }
    }

    /**
     * Specifies the kind of node to do checks on.
     *
     * @return The kind of node to do checks on.
     */
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
            serializedNumbersFromFilenames.add(Integer.parseInt(matcher.group(1)));
        }
    }
}
