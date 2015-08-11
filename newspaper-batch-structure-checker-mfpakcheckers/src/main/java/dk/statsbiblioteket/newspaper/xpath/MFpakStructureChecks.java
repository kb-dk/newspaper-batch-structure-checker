package dk.statsbiblioteket.newspaper.xpath;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.FuzzyDate;
import dk.statsbiblioteket.newspaper.Validator;
import dk.statsbiblioteket.newspaper.mfpakintegration.batchcontext.BatchContext;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperDateRange;
import dk.statsbiblioteket.newspaper.structureChecker.Constants;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *  This class performs the structure checks that require information from MFpak.
 *
 */
public class MFpakStructureChecks implements Validator {
    private final static Logger log = LoggerFactory.getLogger(MFpakStructureChecks.class);
    private BatchContext context;
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

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
                    Constants.TYPE,
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
        final String xpathFilmNode =
                "/node[@shortName='" + batch.getFullID() + "']/node[starts-with(@shortName,'" + batch.getBatchID()
                        + "')]";

        List<NewspaperDateRange> mfpakDateRanges = new ArrayList<NewspaperDateRange>(context.getDateRanges());

        NodeList filmNodes = xpath.selectNodeList(doc, xpathFilmNode);

        if (filmNodes.getLength() != mfpakDateRanges.size()) {
            addFailure(resultCollector,batch.getFullID(),"2F-M2: Wrong number of films. File structure contains '"
                    + filmNodes.getLength()
                    + "' but mfpak contains '" + mfpakDateRanges.size() + "'");
        }

        List<FilmDateRange> unmappedFilmRanges = buildBatchStructureDateRanges(filmNodes, xpath, resultCollector);
       
        eliminateUniqueDateRanges(unmappedFilmRanges, mfpakDateRanges);
        
        if(!unmappedFilmRanges.isEmpty()) {
            for(FilmDateRange dateRange : unmappedFilmRanges) {
                if(dateRange.getMatchedRanges() < 1) {
                    addFailure(resultCollector, dateRange.getFilmShortName(),
                            "2F-M3: The date range (" + dateRange.getStartDate().asString() + " - " +
                                    dateRange.getEndDate().asString() + ") for the film editions are not valid " +
                                    "according to any date range from mfpak");
                } else if(dateRange.getMatchedRanges() > 1) {
                    addFailure(resultCollector, dateRange.getFilmShortName(),
                            "2F-M3: The date range (" + dateRange.getStartDate().asString() + " - " +
                                    dateRange.getEndDate().asString() + ") for the film editions match more than one (" +
                                    dateRange.getMatchedRanges() + ") date range from mfpak");
                } else {
                    log.error("This should never happen. A date range was matched excatly one time, "
                            + "this should have gone in the bucket with uniquely matched ranges, not left over!");
                }
            }
        }
        
        if (!mfpakDateRanges.isEmpty()) {
            for (NewspaperDateRange dateRange : mfpakDateRanges) {
                addFailure(resultCollector, batch.getFullID(),"2F-M3: There should have been a film covering the dateranges "
                        + simpleDateFormat.format(dateRange.getFromDate()) + " - "
                        + simpleDateFormat.format(dateRange.getToDate()));
            }
        }
        
        return resultCollector.isSuccess();
    }
    
    /**
     * Method to eliminate the unique NewspaperDateRange in the two lists filmRanges and mfpakDateRanges. 
     * The unique matches of NewspaperDateRanges and FilmDateRanges is removed from the lists during the invocation of the method. 
     * @param filmRanges FilmDateRange objects that is was discovered in the batch structure. This list is updated by the method. 
     * Elements remaining in the list is FilmDateRanges that could not be matched (uniquely or at all)
     * @param mfpakDateRanges NewspaperDateRanges that was found in the mfpak database. This list is updated by the method. 
     * Elements remaining in the list after the return of the method could not be uniquely matched to a FilmDateRange. 
     */
    private void eliminateUniqueDateRanges(List<FilmDateRange> filmRanges, List<NewspaperDateRange> mfpakDateRanges) {
        Map<NewspaperDateRange, Set<FilmDateRange>> matchMap = new HashMap<>();
        boolean matchedFound = false;

        while(!matchedFound && filmRanges.size()>0) {
            matchedFound = true;
            matchMap = new HashMap<>();

            Set<FilmDateRange> eliminatedFilmRanges = new HashSet<>();
            Set<NewspaperDateRange> eliminatedNewspaperRanges = new HashSet<>();

            // Build the matchMap
            for(NewspaperDateRange dateRange : mfpakDateRanges) {
                matchMap.put(dateRange, new HashSet<FilmDateRange>());
                for(FilmDateRange filmRange : filmRanges) {
                    if(isFilmDateRangeIncluded(dateRange, filmRange)) {
                        matchMap.get(dateRange).add(filmRange);
                    }
                }
            }
            
            for(NewspaperDateRange range : matchMap.keySet()) {
                Set<FilmDateRange> matchingFilmDateRanges = matchMap.get(range);
                if(matchingFilmDateRanges.size() == 1) {
                    matchedFound = false;
                    eliminatedFilmRanges.addAll(matchingFilmDateRanges);
                    eliminatedNewspaperRanges.add(range);
                }
            }
            filmRanges.removeAll(eliminatedFilmRanges);
            mfpakDateRanges.removeAll(eliminatedNewspaperRanges);
        }
        
        if(!matchMap.isEmpty()) {
            for(NewspaperDateRange range : matchMap.keySet()) {
                for(FilmDateRange filmRange : matchMap.get(range)) {
                    filmRange.setMatchedRanges(filmRange.getMatchedRanges() + 1);
                }
            }
        }
        
    }
    
    private boolean isFilmDateRangeIncluded(NewspaperDateRange mfpakRange, FilmDateRange filmRange) {
        return (isFuzzyDateIncluded(mfpakRange, filmRange.getStartDate()) && isFuzzyDateIncluded(mfpakRange, filmRange.getEndDate()));
    }
    
    private boolean isFuzzyDateIncluded(NewspaperDateRange range, FuzzyDate date) {
        boolean included = true;
        
        if(date.before(range.getFromDate())) {
            included = false;
        } 
        if(date.after(range.getToDate())) {
            included = false;
        }
        
        return included;
    }
    
    protected List<FilmDateRange> buildBatchStructureDateRanges(NodeList filmNodes, XPathSelector xpath, ResultCollector resultCollector) {
        final String xpathEditionNode = "node[@shortName != 'UNMATCHED' and @shortName != 'FILM-ISO-target']";
        List<FilmDateRange> batchStructureDateRanges = new ArrayList<>();
        
        for (int i = 0; i < filmNodes.getLength(); i++) {
            Node filmNode = filmNodes.item(i);
            String filmShortName = filmNode.getAttributes().getNamedItem("shortName").getNodeValue();

            NodeList editionNodes = xpath.selectNodeList(filmNode, xpathEditionNode);
            FuzzyDate firstEdition = null;
            FuzzyDate lastEdition = null;

            for (int j = 0; j < editionNodes.getLength(); j++) {
                Node editionNode = editionNodes.item(j);
                String editionShortName = editionNode.getAttributes().getNamedItem("shortName").getNodeValue();
                String editionPath = editionNode.getAttributes().getNamedItem("name").getNodeValue();
                
                String editionDateString = editionShortName.substring(0, editionShortName.lastIndexOf('-'));
                try {
                    FuzzyDate editionDate = new FuzzyDate(editionDateString);
                    if (firstEdition == null) {
                        firstEdition = lastEdition = editionDate;
                    } else {
                        if (editionDate.before(firstEdition)) {
                            firstEdition = editionDate;
                        }
                        if (editionDate.after(lastEdition)) {
                            lastEdition = editionDate;
                        }
                    }
                } catch (ParseException e) {
                    addFailure(resultCollector, editionPath,
                            "2F-M3: Failed to parse date from edition folder: " + e.toString());
                }
            }
            
            batchStructureDateRanges.add(new FilmDateRange(filmShortName, firstEdition, lastEdition));
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
                Constants.TYPE,
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
    
    class FilmDateRange {

        String filmShortName;
        FuzzyDate startDate;
        FuzzyDate endDate;
        private int matchedRanges = 0;

        public FilmDateRange(String filmShortName, FuzzyDate startDate, FuzzyDate endDate) {
            this.filmShortName = filmShortName;
            this.startDate = startDate;
            this.endDate = endDate;
        }
        
        public String getFilmShortName() {
            return filmShortName;
        }
        
        public FuzzyDate getStartDate() {
            return startDate;
        }

        public FuzzyDate getEndDate() {
            return endDate;
        }
        
        public int getMatchedRanges() {
            return matchedRanges;
        }

        public void setMatchedRanges(int matchedRanges) {
            this.matchedRanges = matchedRanges;
        }

        @Override public String toString() {
            return "FilmDateRange{" +
                    "filmShortName='" + filmShortName + '\'' +
                    ", startDate=" + startDate +
                    ", endDate=" + endDate +
                    ", matchedRanges=" + matchedRanges +
                    '}';
        }
    }
}
