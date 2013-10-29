package dk.statsbiblioteket.newspaper.schematron;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import com.phloc.commons.io.resource.ClassPathResource;
import com.phloc.schematron.SchematronException;
import com.phloc.schematron.pure.SchematronResourcePure;
import dk.statsbiblioteket.util.Strings;
import dk.statsbiblioteket.util.xml.DOM;
import org.oclc.purl.dsdl.svrl.ActivePattern;
import org.oclc.purl.dsdl.svrl.FailedAssert;
import org.oclc.purl.dsdl.svrl.FiredRule;
import org.oclc.purl.dsdl.svrl.SchematronOutputType;
import org.oclc.purl.dsdl.svrl.SuccessfulReport;
import org.w3c.dom.Document;

import java.io.InputStream;

/**
 *
 */
public class StructureValidator {

    public static final String TYPE = "BatchDirectoryStructure";
    private final ClassPathResource schemaResource;
    private final SchematronResourcePure schematron;

    public StructureValidator(String schematronPath) {
        schemaResource = new ClassPathResource(schematronPath);
        schematron = new SchematronResourcePure(schemaResource);
        if (!schematron.isValidSchematron()) {
            throw new RuntimeException("Failed to validate schematron resource as '"+schematronPath+"'");
        }

    }

    public boolean validate(Batch batch, InputStream contents, ResultCollector resultCollector) {
        Document document = DOM.streamToDOM(contents);
        boolean success= true;
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
                resultCollector.addFailure(batch.getFullID(),
                        TYPE,
                        getComponent(),
                        failedAssert.getText(),
                        "Location: '" + failedAssert.getLocation() + "'",
                        "Test: '" + failedAssert.getTest() + "'");
            } else if (o instanceof ActivePattern) {
                ActivePattern activePattern = (ActivePattern) o;
                //do nothing
            } else if (o instanceof FiredRule) {
                FiredRule firedRule = (FiredRule) o;
                //a rule that was run
            } else if (o instanceof SuccessfulReport) {
                SuccessfulReport successfulReport = (SuccessfulReport) o;
                //ever?
            } else {
                //unknown type of o.
                throw new RuntimeException("Unknown result from schematron library: " + o.getClass().getName());
            }
        }
        return success;
    }

    /**
        * Get the name of this component for error reporting purposes
        *
        * @return
        */
       private String getComponent() {
           return getClass().getName() + "-" + getClass().getPackage().getImplementationVersion();
       }


}
