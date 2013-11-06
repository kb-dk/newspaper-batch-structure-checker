package dk.statsbiblioteket.newspaper.schematron;

import com.phloc.commons.io.resource.ClassPathResource;
import com.phloc.schematron.SchematronException;
import com.phloc.schematron.pure.SchematronResourcePure;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.Validator;
import dk.statsbiblioteket.util.Strings;
import dk.statsbiblioteket.util.xml.DOM;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.w3c.dom.Document;

import java.io.InputStream;

/**
 * Class containing general-purpose functionality to validate an xml documents against a schematron document and gather
 * the results in a ResultCollector object.
 */
public class StructureValidator implements Validator {

    public static final String TYPE = "BatchDirectoryStructure";
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
            throw new RuntimeException("Failed to validate schematron resource as '"+schematronPath+"'");
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
        boolean success= true;
        //<!-- TODO: Her ville vi skulle tage flag fra mf-pak om hvorvidt vi skulle forvente alto. Flag kunne indkodes i denne .sch fil before run-->
        SchematronOutputType result = null;
        try {
            result = schematron.applySchematronValidation(document);
        } catch (SchematronException e) {
            resultCollector.addFailure(batch.getFullID(),
                    TYPE,
                    getComponent(),
                    "Schematron Exception. Error was " + e
                            .toString(),
                    Strings.getStackTrace(e));
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
                resultCollector.addFailure(batch.getFullID(),
                        TYPE,
                        getComponent(),
                        message);
            }
        }
        return success;
    }

    /**
        * Get the name of this component for error reporting purposes.
        *
        * @return the component name.
        */
       private String getComponent() {
           return getClass().getName() + "-" + getClass().getPackage().getImplementationVersion();
       }


}
