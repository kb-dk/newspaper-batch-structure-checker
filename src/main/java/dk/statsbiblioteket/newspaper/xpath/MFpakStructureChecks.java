package dk.statsbiblioteket.newspaper.xpath;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.BatchStructureCheckerComponent;
import dk.statsbiblioteket.newspaper.BatchStructureCheckerExecutable;
import dk.statsbiblioteket.newspaper.Validator;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContext;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *  This class performs the structure checks that require information from MFpak.
 *
 */
public class MFpakStructureChecks implements Validator {
    private static Logger log = LoggerFactory.getLogger(MFpakStructureChecks.class);
    private BatchContext context;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public MFpakStructureChecks(BatchContext context) {
        this.context = context;
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
    protected boolean validateDateRanges(Batch batch,
                                       ResultCollector resultCollector,
                                       XPathSelector xpath,
                                       Document doc) {
        boolean success = true;
        final String xpathFilmNode =
                "/node[@shortName='" + batch.getFullID() + "']/node[starts-with(@shortName,'" + batch.getBatchID()
                        + "')]";

        List<NewspaperDateRange> mfpakDateRanges = new ArrayList<NewspaperDateRange>(context.getDateRanges());

        NodeList filmNodes = xpath.selectNodeList(doc, xpathFilmNode);

        if (filmNodes.getLength() != mfpakDateRanges.size()) {
            addFailure(resultCollector,batch.getFullID(),"2F-M2: Wrong number of films. File structure contains '"
                    + filmNodes.getLength()
                    + "' but mfpak contains '" + mfpakDateRanges.size() + "'");
            success = false;
        }

        List<FilmDateRange> filmRanges = buildBatchStructureDateRanges(filmNodes, xpath, resultCollector);
        if(!resultCollector.isSuccess()) {
            success = false;
        }
        
        Set<NewspaperDateRange> eliminatedNewspaperDateRanges;
        Set<FilmDateRange> eliminatedFilmDateRanges;
        do {
            eliminatedNewspaperDateRanges = new HashSet<>();
            eliminatedFilmDateRanges = new HashSet<>();
            eliminateUniqueDateRanges(filmRanges, mfpakDateRanges, eliminatedFilmDateRanges, eliminatedNewspaperDateRanges); 
        } while (!eliminatedNewspaperDateRanges.isEmpty() || !eliminatedFilmDateRanges.isEmpty());

        if(!filmRanges.isEmpty()) {
            for(FilmDateRange dateRange : filmRanges) {
                if(dateRange.getMatchedRanges() < 1) {
                    addFailure(resultCollector, dateRange.getFilmShortName(),
                            "2F-M3: The date range (" +
                                    simpleDateFormat.format(dateRange.getDateRange().getFromDate()) + " - " +
                                    simpleDateFormat.format(dateRange.getDateRange().getToDate()) + ") for the film editions are not valid " +
                                    "according to any date range from mfpak");
                } else if(dateRange.getMatchedRanges() > 1) {
                    addFailure(resultCollector, dateRange.getFilmShortName(),
                            "2F-M3: The date range (" +
                                    simpleDateFormat.format(dateRange.getDateRange().getFromDate()) + " - " +
                                    simpleDateFormat.format(dateRange.getDateRange().getToDate()) + ") for the film editions match more than one (" +
                                    dateRange.getMatchedRanges() + ") date range from mfpak");
                } else {
                    log.error("A date range was matched excatly one time, "
                            + "this should have gone in the bucket with uniquely matched ranges, not lefte over!");
                }
            }
            success = false;
        }
        
        if (!mfpakDateRanges.isEmpty()) {
            for (NewspaperDateRange dateRange : mfpakDateRanges) {
                addFailure(resultCollector, batch.getFullID(),"2F-M3: There should have been a film covering the dateranges "
                        + simpleDateFormat.format(dateRange.getFromDate()) + " - "
                        + simpleDateFormat.format(dateRange.getToDate()));
            }
            success = false;

        }
        
        return success;
    }
    
    /**
     * Method to eliminate the unique NewspaperDateRange in the two lists filmRanges and mfpakDateRanges. 
     * The unique NewspaperDateRanges is removed from the lists and placed in the set returned by the method. 
     * @param filmRanges FilmDateRange objects that is was discovered in the batch structure
     * @param mfpakDateRanges NewspaperDateRanges that was found in the mfpak database
     * @param uniqueFilmRanges FilmDateRanges uniquely identified
     * @param uniqueNewspaperDateRanges NewspaperDateRanges uniquely identified
     */
    private void eliminateUniqueDateRanges(List<FilmDateRange> filmRanges, List<NewspaperDateRange> mfpakDateRanges,
            Set<FilmDateRange> uniqueFilmRanges, Set<NewspaperDateRange> uniqueNewspaperDateRanges) {
        for(FilmDateRange filmRange : filmRanges) {
            filmRange.setMatchedRanges(0);
        }
        
        for(NewspaperDateRange dateRange : mfpakDateRanges) {
            int matchesFound = 0;
            for(FilmDateRange filmRange : filmRanges) {
                if(dateRange.isIncluded(filmRange.getDateRange().getFromDate()) && dateRange.isIncluded(filmRange.getDateRange().getToDate())) {
                    filmRange.setMatchedRanges(filmRange.getMatchedRanges() + 1);
                    matchesFound++;
                }
            }
            if(matchesFound == 1) {
                uniqueNewspaperDateRanges.add(dateRange);
            }
        }

        for(FilmDateRange filmRange : filmRanges) {
            if(filmRange.getMatchedRanges() == 1) {
                uniqueFilmRanges.add(filmRange);
            }
        }
        
        mfpakDateRanges.removeAll(uniqueNewspaperDateRanges);
        filmRanges.removeAll(uniqueFilmRanges);
    }
    
    private List<FilmDateRange> buildBatchStructureDateRanges(NodeList filmNodes, XPathSelector xpath, ResultCollector resultCollector) {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final String xpathEditionNode = "node[@shortName != 'UNMATCHED' and @shortName != 'FILM-ISO-target']";
        List<FilmDateRange> batchStructureDateRanges = new ArrayList<>();
        
        for (int i = 0; i < filmNodes.getLength(); i++) {
            Node filmNode = filmNodes.item(i);
            String filmShortName = filmNode.getAttributes().getNamedItem("shortName").getNodeValue();

            NodeList editionNodes = xpath.selectNodeList(filmNode, xpathEditionNode);
            Date firstEdition = null;
            Date lastEdition = null;

            for (int j = 0; j < editionNodes.getLength(); j++) {
                Node editionNode = editionNodes.item(j);
                String editionShortName = editionNode.getAttributes().getNamedItem("shortName").getNodeValue();

                String editionPath = editionNode.getAttributes().getNamedItem("name").getNodeValue();

                try {
                    Date editionDate = dateFormat.parse(editionShortName);
                    if (firstEdition == null) {
                        firstEdition = lastEdition = editionDate;
                    } else {
                        if (firstEdition.after(editionDate)) {
                            firstEdition = editionDate;
                        }
                        if (lastEdition.before(editionDate)) {
                            lastEdition = editionDate;
                        }
                    }
                } catch (ParseException e) {
                    addFailure(resultCollector, editionPath,
                            "2F-M3: Failed to parse date from edition folder: " + e.toString());
                }
            }
            
            NewspaperDateRange range = new NewspaperDateRange(firstEdition, lastEdition);
            batchStructureDateRanges.add(new FilmDateRange(filmShortName, range));
        }
        
        return batchStructureDateRanges;
    }
    

    /**
     * Utility method to add failure
     * @param resultCollector the result collector to add to
     * @param refToFailedThing the ref to the thing that failed
     * @param description the description of the failure
     * @param details the details of the failure
     * @return false
     */
    protected boolean addFailure(ResultCollector resultCollector,
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
    protected boolean validateAvisId(Batch batch,
                                   ResultCollector resultCollector,
                                   XPathSelector xpath,
                                   Document doc) {
        boolean success = true;
        final String xpathFilmXml =
                "/node[@shortName='" + batch.getFullID() + "']/node[starts-with(@shortName,'" + batch.getBatchID()
                        + "')]/attribute";
        String avisId = null;
        avisId = context.getAvisId();
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

        final String xpathNonEmptyOrBrikScanNodes = "node[not(ends-with(@shortName, '-brik') or matches(@shortName, '.*-X[0-9]{4}$'))]";
        final String xpathEditionNodes = "node[@shortName != 'UNMATCHED' and @shortName != 'FILM-ISO-target']";
        final String xpathFilmNodes =
                "/node[@shortName='" + batch.getFullID() + "']/node[starts-with(@shortName,'" + batch.getBatchID() + "')]";
        NodeList filmNodes = xpath.selectNodeList(doc, xpathFilmNodes);

        for (int i = 0; i < filmNodes.getLength(); i++) {
            Node filmNode = filmNodes.item(i);

            NodeList editionNodes = xpath.selectNodeList(filmNode, xpathEditionNodes);

            for (int j = 0; j < editionNodes.getLength(); j++) {
                Node editionNode = editionNodes.item(j);

                NodeList nonEmptyOrBrikScanNodes = xpath.selectNodeList(editionNode, xpathNonEmptyOrBrikScanNodes);

                success &= validateAltoOrNotForNodes(nonEmptyOrBrikScanNodes, batch, resultCollector, xpath);
            }
        }

        return success;
    }

    /**
     * Validate that alto-files exist (or don't exist) where needed according to options as found in the MFpak database
     * @param nonEmptyOrBrikScanNodes List of nodes representing newspaper page scans (not brik-scans)
     * @param batch Batch for which to check for alto
     * @param resultCollector For collecting the results of the check
     * @param xpath The XPathSelector
     * @return Whether or not validation passed with success
     */
    private boolean validateAltoOrNotForNodes(NodeList nonEmptyOrBrikScanNodes, Batch batch, ResultCollector resultCollector,
                                              XPathSelector xpath) {
        // In this method, a "scan" means an image that was scanned from microfilm, whether a newspaper page, brik, target,...
        NewspaperBatchOptions options;
        boolean success = true;

        options = context.getBatchOptions();
        if (options == null){
            success = false;
            addFailure(resultCollector,batch.getFullID(),"MFPak did not have any batch options");
            return success;
        }
        if (options.isOptionB1() || options.isOptionB2() || options.isOptionB9()) {
            // According to options, we should check for existence of alto-files
            success = checkForAltoExistence(nonEmptyOrBrikScanNodes, resultCollector, xpath, success);
        } else {
            // According to options, we should check that there exist no alto-files
            success = checkForAltoNonExistence(nonEmptyOrBrikScanNodes, resultCollector, xpath, success);
        }

        return success;
    }

    /**
     * Check that there exist no alto-files
     * @param nonEmptyOrBrikScanNodes List of nodes representing newspaper page scans (not brik-scans)
     * @param resultCollector For collecting the results of the check
     * @param xpath The XPathSelector
     * @param success "Accumulating" success variable, that should be returned if no failure happened
     * @return False if alto file was found. Returns the received success value otherwise.
     */
    private boolean checkForAltoNonExistence(NodeList nonEmptyOrBrikScanNodes, ResultCollector resultCollector,
                                             XPathSelector xpath, boolean success) {
        for (int i = 0; i < nonEmptyOrBrikScanNodes.getLength(); i++) {
            Node nonEmptyOrBrikScanNode = nonEmptyOrBrikScanNodes.item(i);

            final String xpathAltoAttribute = "attribute[@shortName = concat(../@shortName,'.alto.xml')]";
            NodeList altoAttributeNodes = xpath.selectNodeList(nonEmptyOrBrikScanNode, xpathAltoAttribute);

            String scanName = nonEmptyOrBrikScanNode.getAttributes().getNamedItem("shortName").getNodeValue();
            if (altoAttributeNodes.getLength() != 0) {
                String scanPath = nonEmptyOrBrikScanNode.getAttributes().getNamedItem("name").getNodeValue();

                addFailure(resultCollector, scanPath, "2F-M5: Though there should be none, found alto file for "
                        + scanName);
                success = false;
            }
        }
        return success;
    }

    /**
     * Check that there existe alto-files
     * @param nonEmptyOrBrikScanNodes List of nodes representing newspaper page scans (not brik-scans)
     * @param resultCollector For collecting the results of the check
     * @param xpath The XPathSelector
     * @param success "Accumulating" success variable, that should be returned if no failure happened
     * @return False if alto file could not be found for one or more scans. Returns the received success value otherwise.
     */
    private boolean checkForAltoExistence(NodeList nonEmptyOrBrikScanNodes, ResultCollector resultCollector,
                                          XPathSelector xpath, boolean success) {
        for (int i = 0; i < nonEmptyOrBrikScanNodes.getLength(); i++) {
            Node nonEmptyOrBrikScanNode = nonEmptyOrBrikScanNodes.item(i);

            final String xpathAltoAttribute = "attribute[@shortName = concat(../@shortName,'.alto.xml')]";
            NodeList altoAttributeNodes = xpath.selectNodeList(nonEmptyOrBrikScanNode, xpathAltoAttribute);

            String scanName = nonEmptyOrBrikScanNode.getAttributes().getNamedItem("shortName").getNodeValue();

            if (altoAttributeNodes.getLength() != 1) {
                String scanPath = nonEmptyOrBrikScanNode.getAttributes().getNamedItem("name").getNodeValue();

                addFailure(resultCollector, scanPath, "2F-M4: Could not find alto file for " + scanName);
                success = false;
            }
        }
        return success;
    }
    
    private class FilmDateRange {
        String filmShortName;
        NewspaperDateRange dateRange;
        private int matchedRanges = 0;

        public FilmDateRange(String filmShortName, NewspaperDateRange dateRange) {
            this.filmShortName = filmShortName;
            this.dateRange = dateRange;
        }
        
        public String getFilmShortName() {
            return filmShortName;
        }
        
        public NewspaperDateRange getDateRange() {
            return dateRange;
        }

        public int getMatchedRanges() {
            return matchedRanges;
        }

        public void setMatchedRanges(int matchedRanges) {
            this.matchedRanges = matchedRanges;
        }
        
    }
}
