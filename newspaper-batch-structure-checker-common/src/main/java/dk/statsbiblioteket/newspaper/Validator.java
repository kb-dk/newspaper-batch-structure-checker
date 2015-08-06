package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;

import java.io.InputStream;

public interface Validator {
    /**
     * Validate an xml document against this objects schematron and collect any failures.
     * @param batch The Batch object being validated.
     * @param contents An input stream which returns the xml to be validated.
     * @param resultCollector the ResultCollector in which the results are stored.
     * @return if the batch was valid
     */
    boolean validate(Batch batch,
                     InputStream contents,
                     ResultCollector resultCollector);
}
