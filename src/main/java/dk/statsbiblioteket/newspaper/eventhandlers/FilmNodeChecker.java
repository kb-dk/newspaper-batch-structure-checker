package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.treenode.NodeType;
import dk.statsbiblioteket.newspaper.treenode.TreeNodeState;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Following https://sbforge.org/display/NEWSPAPER/Batch+structure+checks ,
 * This class must check the following:
 *
 * For the FILM node itself:
 *Potentiel eksistens af FILM-ISO-target
 *Potentiel eksistens af UNMATCHED
 *Eksistens af edition-mapper
 *Ikke andre filer og mapper
 *film.xml-fil
 *
 * For the  Film.xml-fil
 *Form: [avisID]-[batchID]-[filmSuffix].avis.xml
 *[avisId] er som forventet i MF-PAK
 *batchID er som i parent dir
 *filmSuffix er som i parent dir
 *
 * For edition childNodes:
 * Edition-mappe:
 *Form: [date]-[udgaveLbNummer]
 *[date] skal være iso8601
 *<udgaveLbNummer> fortløbende startende med 1
 *[date] svarer til informationer fra MF-PAK
 */
public class FilmNodeChecker extends AbstractNodeChecker {

    private TreeNodeState state;
    private ResultCollector resultCollector;
    private Batch batch;

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
        List<Integer> editions = new ArrayList<Integer>();
        for (String childNode: childNodes) {
            String childNodeName = Util.getLastTokenInPath(childNode);
            switch (childNodeName) {
                case ("FILM-ISO-target"):
                    break;
                case ("UNMATCHED"):
                    break;
                default:
                    String editionPatternString = "(.*-.*-.*)-(.*)";
                    Pattern editionPattern = Pattern.compile(editionPatternString);
                    Matcher m = editionPattern.matcher(childNodeName);
                    if (m.matches()) {
                        String dateString = m.group(1);
                        String editionString = m.group(2);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            sdf.parse(dateString);
                            //TODO check this date against MF-PAK
                        } catch (ParseException e) {
                            addFailure("dateformat", "In " + childNodeName + " the date part is not a valid date in yyyy-MM-dd format.");
                        }
                        Integer edition = null;
                        try {
                            edition = Integer.parseInt(editionString);
                            editions.add(edition);
                        } catch (NumberFormatException e) {
                            addFailure("editionformat", "In " + childNodeName + " the edition part " + editionString + " is not a number.");
                        }
                    } else {
                        addFailure("unknowndirectory", "Found unexpected directory " + childNodeName + " in name.");
                    }
            }
        }
        if (editions.size() == 0) {
            addFailure("missingdirectory", "There are no edition directories in " + name);
        }
        //TODO Is this right? Surely we should be checking that the editions _for any given date_ are consecutive,
        //not all the editions on any given film. And what if we are missing an edition - perhaps it was never
        //collected in the first place?
        if (!Util.validateRunningSequence(editions)) {
            addFailure("editionsequence", "The sequence of editions in " + name + " is not consecutive.");
        }
    }

    private void checkAttributes() {
        String filmxmlPatternString = "(.*)-" + name + ".film.xml";
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
