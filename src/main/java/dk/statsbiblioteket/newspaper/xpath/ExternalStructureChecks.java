package dk.statsbiblioteket.newspaper.xpath;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.Validator;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.w3c.dom.Document;

import java.io.InputStream;

public class ExternalStructureChecks implements Validator {

    private static final String FILESTRUCTURE = "filestructure";
    private MfPakDAO mfPakDAO;

    public ExternalStructureChecks(MfPakDAO mfPakDAO) {
        this.mfPakDAO = mfPakDAO;
    }

    @Override
    public boolean validate(Batch batch,
                            InputStream contents,
                            ResultCollector resultCollector) {

        XPathSelector xpath = DOM.createXPathSelector();
        Document doc;

        doc = DOM.streamToDOM(contents);
        if (doc == null) {
            resultCollector.addFailure(batch.getFullID(),
                                       FILESTRUCTURE,
                                       getClass().getName(),
                                       "Could not parse data structure of " + batch.getFullID());
            return false;
        }


        //2C-11
    /*    //TODO
        final String xpath2C11 = "mods:mods/mods:relatedItem/mods:titleInfo[@type='uniform' and @authority='Statens "
                                 + "Avissamling']/mods:title";
        String avisId = null;
        try {
            avisId = mfPakDAO.getNewspaperID(batch.getBatchID());
            String modsAvisId = xpath.selectString(doc, xpath2C11);
            if (modsAvisId == null || avisId == null || !modsAvisId.equals(avisId)) {
                resultCollector.addFailure(batch.getFullID(),

                                           FILESTRUCTURE,
                                           getClass().getName(),
                                           "2C-11: avisId mismatch. Document gives " + modsAvisId + " but mfpak gives "
                                           + avisId,
                                           xpath2C11);
            }
        } catch (SQLException e) {
            resultCollector.addFailure(batch.getFullID(),
                                       FILESTRUCTURE,
                                       getClass().getName(),
                                       "2C-11: Couldn't read avisId from mfpak.",
                                       e.getMessage());
        }
*/
        return true;
    }
}
