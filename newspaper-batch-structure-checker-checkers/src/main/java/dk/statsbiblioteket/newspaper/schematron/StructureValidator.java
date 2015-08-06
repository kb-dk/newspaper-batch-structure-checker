package dk.statsbiblioteket.newspaper.schematron;

import com.phloc.commons.io.resource.ClassPathResource;
import com.phloc.schematron.SchematronException;
import com.phloc.schematron.pure.SchematronResourcePure;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.Validator;
import dk.statsbiblioteket.newspaper.structureChecker.Constants;
import dk.statsbiblioteket.util.Strings;
import dk.statsbiblioteket.util.xml.DOM;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.w3c.dom.Document;

import java.io.InputStream;

/**
 * Class containing general-purpose functionality to validate an xml document against a schematron document and gather
 * the results in a ResultCollector object.
 */
public class StructureValidator implements Validator {
    private final ClassPathResource schemaResource;
    private final SchematronResourcePure schematron;

    /**
     * The constructor for this class.
     * @param schematronPath the path to the schematron document. This must be on the classpath of the current
     *                       ClassLoader.
     */
    public StructureValidator(String schematronPath) {
        schemaResource = new ClassPathResource(schematronPath);
        schematron = new SchematronResourcePure(schemaResource);
        if (!schematron.isValidSchematron()) {
            throw new RuntimeException("Failed to validate schematron resource as '" + schematronPath + "'");
        }

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
        return validate(batch,document,resultCollector);
    }

        /**
         * Validate an xml document against this objects schematron and collect any failures.
         * @param batch The Batch object being validated.
         * @param document The xml to be validated
         * @param resultCollector the ResultCollector in which the results are stored.
         * @return
         */
    public boolean validate(Batch batch,
                            Document document,
                            ResultCollector resultCollector) {
        boolean success = true;

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
                            Constants.TYPE, getClass().getSimpleName(),
                            message.substring(message.indexOf(':') + 1).trim());
                } else {
                    resultCollector.addFailure(batch.getFullID(),
                            Constants.TYPE, getClass().getSimpleName(),
                            message);
                }
            }
        }
        return success;
    }
}
