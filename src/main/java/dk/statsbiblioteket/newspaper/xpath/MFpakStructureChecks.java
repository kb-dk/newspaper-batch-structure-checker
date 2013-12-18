package dk.statsbiblioteket.newspaper.xpath;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.BatchStructureCheckerComponent;
import dk.statsbiblioteket.newspaper.Validator;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.util.Strings;
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
                    BatchStructureCheckerComponent.TYPE,
                    getClass().getSimpleName(),
                    "2F: Could not parse data structure of " + batch.getFullID());
            return false;
        }


        success = validateAvisId(batch, resultCollector, xpath, doc) & success;
        success = validateDateRanges(batch, resultCollector, xpath, doc) & success;
        success = validateAlto(batch, resultCollector, xpath, doc) & success;
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
                resultCollector.addFailure(batch.getFullID(), "exception", getClass().getSimpleName(),
                        "2F: Failed to find batch in MFPak", new String[]{});
                return false;
            }

            NodeList filmNodes = xpath.selectNodeList(doc, xpathFilmNode);

            if (filmNodes.getLength() != dateRanges.size()) {
                addFailure(resultCollector,batch.getFullID(),"2F-M2: Wrong number of films. File structure contains '"
                        + filmNodes.getLength()
                        + "' but mfpak contains '" + dateRanges.size() + "'");
                success = false;
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
                                    break;
                                }
                            }
                            if (selectedDateRange == null) {
                                addFailure(resultCollector, editionPath,
                                        "2F-M3: This edition is not valid according to the date ranges "
                                                + "from mfpak");
                                success = false;
                            }
                        } else {
                            if (!selectedDateRange.isIncluded(editionDate)) {
                                addFailure(resultCollector, editionPath,
                                        "2F-M3: This edition is not valid according to the date ranges "
                                                + "from mfpak");
                                success = false;
                            }
                        }

                    } catch (ParseException e) {
                        addFailure(resultCollector, editionPath,
                                "2F-M3: Failed to parse date from edition folder: " + e.toString());
                        success = false;
                    }
                }
                dateRanges.remove(selectedDateRange);
                selectedDateRange = null;
            }
            if (dateRanges.size() > 0) {
                for (NewspaperDateRange dateRange : dateRanges) {
                    addFailure(resultCollector, batch.getFullID(),"2F-M3: There should have been a film covering the dateranges "
                            + dateRange.getFromDate() + " to " + dateRange.getToDate());
                }
                success = false;

            }


        } catch (SQLException e) {
            resultCollector
                    .addFailure(batch.getFullID(), "exception", getClass().getSimpleName(),
                            "2F: Couldn't read batch information from mfpak: " + e.toString(),
                            Strings.getStackTrace(e));
            success = false;

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
                BatchStructureCheckerComponent.TYPE,
                getClass().getSimpleName(), description, details);
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
                    addFailure(resultCollector,filmXmlPath,"2F-M1: avisId mismatch. Name gives " + avisIdFromFilmXml
                            + " but mfpak gives " + avisId);
                    success = false;
                }
            }

        } catch (SQLException e) {
            resultCollector.addFailure(batch.getFullID(), "exception", getClass().getSimpleName(),
                    "2F: Couldn't read avisId from mfpak:" + e.toString(),
                    Strings.getStackTrace(e));
            success = false;
        }
        return success;
    }

    /**
     * Validate that alto-files exist (or don't exist) where needed according to options as found in the MFpak database
     * @param batch the batch we work on
     * @param resultCollector the result collector
     * @param xpath the xpath selector
     * @param doc the structure document
     * @return whether or not the existence of alto-files corresponds to the options found in the MFpak database
     */
    private boolean validateAlto(Batch batch, ResultCollector resultCollector, XPathSelector xpath, Document doc) {
        boolean success = true;

        final String xpathNonBrikScanNodes = "node[not(ends-with(@shortName, '-brik'))]";
        final String xpathEditionNodes = "node[@shortName != 'UNMATCHED' and @shortName != 'FILM-ISO-target']";
        final String xpathFilmNodes =
                "/node[@shortName='" + batch.getFullID() + "']/node[starts-with(@shortName,'" + batch.getBatchID() + "')]";
        NodeList filmNodes = xpath.selectNodeList(doc, xpathFilmNodes);

        for (int i = 0; i < filmNodes.getLength(); i++) {
            Node filmNode = filmNodes.item(i);

            NodeList editionNodes = xpath.selectNodeList(filmNode, xpathEditionNodes);

            for (int j = 0; j < editionNodes.getLength(); j++) {
                Node editionNode = editionNodes.item(j);

                NodeList nonBrikScanNodes = xpath.selectNodeList(editionNode, xpathNonBrikScanNodes);

                success &= validateAltoOrNotForNodes(nonBrikScanNodes, batch, resultCollector, xpath);
            }
        }

        return success;
    }

    /**
     * Validate that alto-files exist (or don't exist) where needed according to options as found in the MFpak database
     * @param nonBrikScanNodes List of nodes representing newspaper page scans (not brik-scans)
     * @param batch Batch for which to check for alto
     * @param resultCollector For collecting the results of the check
     * @param xpath The XPathSelector
     * @return Whether or not validation passed with success
     */
    private boolean validateAltoOrNotForNodes(NodeList nonBrikScanNodes, Batch batch, ResultCollector resultCollector,
                                              XPathSelector xpath) {
        // In this method, a "scan" means an image that was scanned from microfilm, whether a newspaper page, brik, target,...
        NewspaperBatchOptions options;
        boolean success = true;

        try {
            options = mfPakDAO.getBatchOptions(batch.getBatchID());
            if (options == null){
                success = false;
                addFailure(resultCollector,batch.getFullID(),"MFPak did not have any batch options");
                return success;
            }
            if (options.isOptionB1() || options.isOptionB2() || options.isOptionB9()) {
                // According to options, we should check for existence of alto-files
                success = checkForAltoExistence(nonBrikScanNodes, resultCollector, xpath, success);
            } else {
                // According to options, we should check that there exist no alto-files
                success = checkForAltoNonExistence(nonBrikScanNodes, resultCollector, xpath, success);
            }
        } catch (SQLException e) {
            resultCollector.addFailure(batch.getFullID(), "exception", getClass().getSimpleName(),
                    "Couldn't read options from mfpak:" + e.toString(),
                    Strings.getStackTrace(e));
            success = false;
        }

        return success;
    }

    /**
     * Check that there exist no alto-files
     * @param nonBrikScanNodes List of nodes representing newspaper page scans (not brik-scans)
     * @param resultCollector For collecting the results of the check
     * @param xpath The XPathSelector
     * @param success "Accumulating" success variable, that should be returned if no failure happened
     * @return False if alto file was found. Returns the received success value otherwise.
     */
    private boolean checkForAltoNonExistence(NodeList nonBrikScanNodes, ResultCollector resultCollector, XPathSelector xpath,
                                             boolean success) {
        for (int i = 0; i < nonBrikScanNodes.getLength(); i++) {
            Node nonBrikScanNode = nonBrikScanNodes.item(i);

            final String xpathAltoAttribute = "attribute[@shortName = concat(../@shortName,'.alto.xml')]";
            NodeList altoAttributeNodes = xpath.selectNodeList(nonBrikScanNode, xpathAltoAttribute);

            String scanName = nonBrikScanNode.getAttributes().getNamedItem("shortName").getNodeValue();
            if (altoAttributeNodes.getLength() != 0) {
                String scanPath = nonBrikScanNode.getAttributes().getNamedItem("name").getNodeValue();

                addFailure(resultCollector, scanPath, "2F-M5: Though there should be none, found alto file for "
                        + scanName);
                success = false;
            }
        }
        return success;
    }

    /**
     * Check that there existe alto-files
     * @param nonBrikScanNodes List of nodes representing newspaper page scans (not brik-scans)
     * @param resultCollector For collecting the results of the check
     * @param xpath The XPathSelector
     * @param success "Accumulating" success variable, that should be returned if no failure happened
     * @return False if alto file could not be found for one or more scans. Returns the received success value otherwise.
     */
    private boolean checkForAltoExistence(NodeList nonBrikScanNodes, ResultCollector resultCollector, XPathSelector xpath,
                                          boolean success) {
        for (int i = 0; i < nonBrikScanNodes.getLength(); i++) {
            Node nonBrikScanNode = nonBrikScanNodes.item(i);

            final String xpathAltoAttribute = "attribute[@shortName = concat(../@shortName,'.alto.xml')]";
            NodeList altoAttributeNodes = xpath.selectNodeList(nonBrikScanNode, xpathAltoAttribute);

            String scanName = nonBrikScanNode.getAttributes().getNamedItem("shortName").getNodeValue();

            if (altoAttributeNodes.getLength() != 1) {
                String scanPath = nonBrikScanNode.getAttributes().getNamedItem("name").getNodeValue();

                addFailure(resultCollector, scanPath, "2F-M4: Could not find alto file for " + scanName);
                success = false;
            }
        }
        return success;
    }
}
