package dk.statsbiblioteket.newspaper.xpath;

import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.Validator;

import java.io.InputStream;

public class ExternalStructureChecks implements Validator {
    @Override
    public boolean validate(Batch batch,
                            InputStream contents,
                            ResultCollector resultCollector) {

        //TODO here do xpath checks
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
