package dk.statsbiblioteket.newspaper.xpath;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.Validator;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.sql.SQLException;

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
                    resultCollector.addFailure(filmXmlPath,
                                               FILESTRUCTURE,
                                               getClass().getName(),
                                               "avisId mismatch. Name gives " + avisIdFromFilmXml
                                               + " but mfpak gives " + avisId);
                    success = false;
                }


            }


        } catch (SQLException e) {
            resultCollector.addFailure(batch.getFullID(),
                                       FILESTRUCTURE,
                                       getClass().getName(),
                                       "Couldn't read avisId from mfpak.",
                                       e.getMessage());
            return false;
        }
        return success;
    }
}
