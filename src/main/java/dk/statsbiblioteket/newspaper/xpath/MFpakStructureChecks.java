package dk.statsbiblioteket.newspaper.xpath;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.Validator;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 *  This class performs the structure checks that require information from MFpak.
 *
 */
public class MFpakStructureChecks implements Validator {

    private static final String FILESTRUCTURE = "filestructure";
    private MfPakDAO mfPakDAO;

    public MFpakStructureChecks(MfPakDAO mfPakDAO) {
        this.mfPakDAO = mfPakDAO;
    }

    @Override
    public boolean validate(Batch batch,
                            InputStream contents,
                            ResultCollector resultCollector) {

        XPathSelector xpath = DOM.createXPathSelector();
        Document doc;

        boolean success = false;

        doc = DOM.streamToDOM(contents);
        if (doc == null) {
            resultCollector.addFailure(batch.getFullID(),
                                       FILESTRUCTURE,
                                       getClass().getName(),
                                       "Could not parse data structure of " + batch.getFullID());
            return false;
        }


        success = validateAvisId(batch, resultCollector, xpath, doc) & success;
        success = validateDateRanges(batch, resultCollector, xpath, doc) & success;
        return success;
    }

    /**
     * Validate that
     * 1. The batch contains the correct number of films
     * 2. Each film only contains editions from dates that are expected from MFpak
     * @param batch the batch to work on
     * @param resultCollector the result collector
     * @param xpath the xpathSelector
     * @param doc the structure document
     * @return true if these tests are valid
     */
    private boolean validateDateRanges(Batch batch,
                                       ResultCollector resultCollector,
                                       XPathSelector xpath,
                                       Document doc) {
        boolean success = true;


        final String xpathFilmNode =
                "/node[@shortName='" + batch.getFullID() + "']/node[starts-with(@shortName,'" + batch.getBatchID()
                + "')]";

        final String xpathEditionNode = "node[@shortName != 'UNMATCHED' and @shortName != 'FILM-ISO-target']";
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            List<NewspaperDateRange> dateRanges = mfPakDAO.getBatchDateRanges(batch.getBatchID());

            if (dateRanges == null) {
                resultCollector.addFailure(batch.getFullID(),
                                           FILESTRUCTURE,
                                           getClass().getName(),
                                           "Failed to find batch in MFPak");
                return false;

            }


            NodeList filmNodes = xpath.selectNodeList(doc, xpathFilmNode);

            if (filmNodes.getLength() != dateRanges.size()) {
                success = addFailure(resultCollector,batch.getFullID(),"Wrong number of films. File structure contains '" + filmNodes.getLength()
                                                           + "' but mfpak contains '" + dateRanges.size() + "'");
            }

            NewspaperDateRange selectedDateRange = null;

            for (int i = 0; i < filmNodes.getLength(); i++) {
                Node filmNode = filmNodes.item(i);

                NodeList editionNodes = xpath.selectNodeList(filmNode, xpathEditionNode);

                for (int j = 0; j < editionNodes.getLength(); j++) {
                    Node editionNode = editionNodes.item(j);
                    String editionShortName = editionNode.getAttributes().getNamedItem("shortName").getNodeValue();

                    String editionPath = editionNode.getAttributes().getNamedItem("name").getNodeValue();

                    try {
                        Date editionDate = dateFormat.parse(editionShortName);
                        if (selectedDateRange == null) {
                            for (NewspaperDateRange dateRange : dateRanges) {
                                if (dateRange.isIncluded(editionDate)) {
                                    selectedDateRange = dateRange;
                                }
                            }
                            if (selectedDateRange == null) {
                                success = addFailure(resultCollector, editionPath,
                                                     "This edition is not valid according to the date ranges " + "from mfpak");

                            }
                        } else {
                            if (!selectedDateRange.isIncluded(editionDate)) {
                                success = addFailure(resultCollector, editionPath,
                                                     "This edition is not valid according to the date ranges " + "from mfpak");
                            }
                        }

                    } catch (ParseException e) {
                        success = addFailure(resultCollector, editionPath,
                                             "Failed to parse date from edition folder");
                    }

                }
                dateRanges.remove(selectedDateRange);
            }
            if (dateRanges.size() > 0) {
                for (NewspaperDateRange dateRange : dateRanges) {
                    success = addFailure(resultCollector, batch.getFullID(),"There should have been a film covering the dateranges "
                                                                   + dateRange.getFromDate() + " to " + dateRange.getToDate());

                }
                success = false;

            }


        } catch (SQLException e) {
            success = addFailure(resultCollector,batch.getFullID(),"Couldn't read avisId from mfpak.",
                                                   e.getMessage());
        }
        return success;
    }

    /**
     * Utility method to add failure
     * @param resultCollector the result collector to add to
     * @param refToFailedThing the ref to the thing that failed
     * @param description the description of the failure
     * @param details the details of the failure
     * @return false
     */
    private boolean addFailure(ResultCollector resultCollector,
                               String refToFailedThing,
                               String description,
                               String... details) {
        resultCollector.addFailure(refToFailedThing,
                                   FILESTRUCTURE,
                                   getClass().getName(), description,details);
        return false;
    }

    /**
     * Validate that all films are about avisIDs that is correct according the MFpak database
     * @param batch the batch we work on
     * @param resultCollector the result collector
     * @param xpath the xpath selector
     * @param doc the structure document
     * @return false if any film contains a avisID not expected in MFpak.
     */
    private boolean validateAvisId(Batch batch,
                                   ResultCollector resultCollector,
                                   XPathSelector xpath,
                                   Document doc) {
        boolean success = true;
        final String xpathFilmXml =
                "/node[@shortName='" + batch.getFullID() + "']/node[starts-with(@shortName,'" + batch.getBatchID()
                + "')]/attribute";
        String avisId = null;
        try {
            avisId = mfPakDAO.getNewspaperID(batch.getBatchID());
            NodeList filmXmls = xpath.selectNodeList(doc, xpathFilmXml);

            for (int i = 0; i < filmXmls.getLength(); i++) {
                Node filmXmlNode = filmXmls.item(i);

                String filmXmlName = filmXmlNode.getAttributes().getNamedItem("shortName").getNodeValue();

                String filmXmlPath = filmXmlNode.getAttributes().getNamedItem("name").getNodeValue();

                String avisIdFromFilmXml = filmXmlName.replaceFirst("-.*$", "");

                if (avisIdFromFilmXml == null || avisId == null || !avisIdFromFilmXml.equals(avisId)) {
                    success = addFailure(resultCollector,filmXmlPath,"avisId mismatch. Name gives " + avisIdFromFilmXml + " but mfpak gives "
                                                                   + avisId);
                }


            }


        } catch (SQLException e) {
            addFailure(resultCollector,batch.getFullID(),"Couldn't read avisId from mfpak.",
                                                   e.getMessage());
        }
        return success;
    }
}
