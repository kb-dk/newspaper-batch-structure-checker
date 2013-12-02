package dk.statsbiblioteket.newspaper.schematron;

import com.phloc.commons.io.resource.ClassPathResource;
import com.phloc.schematron.SchematronException;
import com.phloc.schematron.pure.SchematronResourcePure;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.BatchStructureCheckerComponent;
import dk.statsbiblioteket.newspaper.Validator;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.MfPakDAO;
import dk.statsbiblioteket.newspaper.mfpakintegration.database.NewspaperBatchOptions;
import dk.statsbiblioteket.util.Strings;
import dk.statsbiblioteket.util.xml.DOM;
import dk.statsbiblioteket.util.xml.XPathSelector;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.sql.SQLException;

/**
 * Class containing general-purpose functionality to validate an xml document against a schematron document and gather
 * the results in a ResultCollector object.
 */
public class StructureValidator implements Validator {

    private static final XPathSelector SCHEMATRON_XPATH_SELECTOR = DOM.createXPathSelector("s", "http://purl.oclc.org/dsdl/schematron");
    private MfPakDAO mfPakDAO;
    private final ClassPathResource schemaResource;
    private final SchematronResourcePure schematron;

    /**
     * The constructor for this class.
     * @param schematronPath the path to the schematron document. This must be on the classpath of the current
     *                       ClassLoader.
     */
    public StructureValidator(String schematronPath, MfPakDAO mfPakDAO) {
        schemaResource = new ClassPathResource(schematronPath);
        schematron = new SchematronResourcePure(schemaResource);
        if (!schematron.isValidSchematron()) {
            throw new RuntimeException("Failed to validate schematron resource as '" + schematronPath + "'");
        }

        this.mfPakDAO = mfPakDAO;
    }

    /**
     * Validate an xml document against this objects schematron and collect any failures.
     * @param batch The Batch object being validated.
     * @param contents An input stream which returns the xml to be validated.
     * @param resultCollector the ResultCollector in which the results are stored.
     * @return
     */
    @Override
    public boolean validate(Batch batch,
                            InputStream contents,
                            ResultCollector resultCollector) {
        Document document = DOM.streamToDOM(contents);
        boolean success = true;

        document = setAltoCheckFlagIfNeeded(document, batch, resultCollector);

        SchematronOutputType result = null;
        try {
            result = schematron.applySchematronValidation(document);
        } catch (SchematronException e) {
            resultCollector.addFailure(
                    batch.getFullID(), "exception", getClass().getSimpleName(),
                    "Schematron Exception. Error was " + e.toString(), Strings.getStackTrace(e));
            success = false;
            return success;
        }
        for (Object o : result.getActivePatternAndFiredRuleAndFailedAssert()) {
            if (o instanceof FailedAssert) {
                success = false;
                FailedAssert failedAssert = (FailedAssert) o;
                String message = failedAssert.getText();
                if (message == null){
                    message = "";
                }
                message = message.trim().replaceAll("\\s+"," ");
                if (message.contains(":")) {
                    resultCollector.addFailure(message.substring(0, message.indexOf(':')),
                            BatchStructureCheckerComponent.TYPE, getClass().getSimpleName(),
                            message.substring(message.indexOf(':') + 1).trim());
                } else {
                    resultCollector.addFailure(batch.getFullID(),
                            BatchStructureCheckerComponent.TYPE, getClass().getSimpleName(),
                            message);
                }
            }
        }
        return success;
    }

    Document setAltoCheckFlagIfNeeded(Document document, Batch batch, ResultCollector resultCollector) {
        String xpathForAlto = "/s:schema/s:let[@name='altoFlag']";
        NewspaperBatchOptions options;

        try {
            options = mfPakDAO.getBatchOptions(batch.getBatchID());

            if (options.isOptionB1() || options.isOptionB2()) {
                ((Element)(SCHEMATRON_XPATH_SELECTOR.selectNode(document, xpathForAlto))).setAttribute("altoFlag", "true()");
            } else {
                ((Element)(SCHEMATRON_XPATH_SELECTOR.selectNode(document, xpathForAlto))).setAttribute("altoFlag", "false()");
            }
        } catch (SQLException e) {
            resultCollector.addFailure(batch.getFullID(), "exception", getClass().getSimpleName(),
                    "Couldn't read options from mfpak:" + e.toString(),
                    Strings.getStackTrace(e));
        }

        return document;
    }
}
