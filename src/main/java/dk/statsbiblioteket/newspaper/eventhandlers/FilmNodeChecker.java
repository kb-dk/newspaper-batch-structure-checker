package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Following https://sbforge.org/display/NEWSPAPER/Batch+structure+checks ,
 */
public class FilmNodeChecker extends AbstractNodeChecker {

    private TreeNodeState state;
    private ResultCollector resultCollector;
    private Batch batch;
    public static final String EDITION_PATTERN_STRING = "(.*-.*-.*)-(.*)";
    public static final Pattern EDITION_PATTERN = Pattern.compile(EDITION_PATTERN_STRING);
    public static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public FilmNodeChecker(Batch batch, TreeNodeState state, ResultCollector resultCollector) {
        this.state = state;
        this.resultCollector = resultCollector;
        this.batch = batch;
    }


    @Override
    public void doCheck() {
        checkAttributes();
        checkChildNodes();
    }

    private void checkChildNodes() {
        Map<String, List<Integer>> editionsMap = new HashMap<>();  //Map of all editions for a given date
        for (String childNode: childNodes) {
            String childNodeName = Util.getLastTokenInPath(childNode);
            switch (childNodeName) {
                case ("FILM-ISO-target"):
                    break;
                case ("UNMATCHED"):
                    break;
                default:
                    Matcher m = EDITION_PATTERN.matcher(childNodeName);
                    if (m.matches()) {
                        String dateString = m.group(1);
                        if (!editionsMap.containsKey(dateString)) {
                            editionsMap.put(dateString, new ArrayList<Integer>());
                        }
                        String editionString = m.group(2);
                        try {
                            SIMPLE_DATE_FORMAT.parse(dateString);
                            //TODO check this date against MF-PAK
                        } catch (ParseException e) {
                            addFailure("dateformat", "In " + childNodeName + " the date part is not a valid date in yyyy-MM-dd format.");
                        }
                        Integer edition = null;
                        try {
                            if (!(editionString.length() == 2)) {
                                addFailure("editionformat", "In " + childNodeName + " the edition part " + editionString + " is not two characters long" +
                                        "." );
                            }
                            edition = Integer.parseInt(editionString);
                            editionsMap.get(dateString).add(edition);
                        } catch (NumberFormatException e) {
                            addFailure("editionformat", "In " + childNodeName + " the edition part " + editionString + " is not a number.");
                        }
                    } else {
                        addFailure("unknowndirectory", "Found unexpected directory " + childNodeName + " in name.");
                    }
            }
        }
        if (editionsMap.size() == 0) {
            addFailure("missingdirectory", "There are no edition directories in " + name);
        }
        for (Map.Entry<String, List<Integer>> editionsEntry: editionsMap.entrySet()) {
            if (!Util.validateRunningSequence(editionsEntry.getValue())) {
                addFailure("editionsequence", "The sequence of editions in " + name + " on date " + editionsEntry.getKey() + " is not consecutive.");
            }
        }
    }

    private void checkAttributes() {
        String filmxmlPatternString = "(.*)-" + Util.getLastTokenInPath(name) + ".film.xml";
        Pattern filmxmlPattern = Pattern.compile(filmxmlPatternString);
        boolean foundFilmXml = false;
        for (String attribute: attributes) {
            String attributeFilename = Util.getLastTokenInPath(attribute);
            Matcher m = filmxmlPattern.matcher(attributeFilename);
            if (m.matches()) {
                //TODO extract the avisID and check it against MF-PAK
                String avisID = m.group(1);
                foundFilmXml = true;
            } else {
                addFailure("unexpectedfile", "Found unexpected file " + attributeFilename + " in " + name);
            }
            if (attributes.size() > 1) {
                addFailure("unexpectedfile", "Expected to find only one file in " + name + " but found " + attributes.size());
            }
        }
        if (!foundFilmXml) {
            addFailure("missingFile", "Failed to find film.xml file in " + name );
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.FILM;
    }

    @Override
    public TreeNodeState getCurrentState() {
        return state;
    }

    private void addFailure(String type, String description) {
        resultCollector.addFailure(name, type, this.getClass().getName(), description);
    }
}
