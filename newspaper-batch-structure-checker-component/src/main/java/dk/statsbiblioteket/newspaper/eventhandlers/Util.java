package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Some handy utility methods for checking.
 */
public class Util {

    /**
     * We use a constant "/" as file separator in DOMS, not the system-dependent file-separator, so this
     * method finds the last token in a path assuming that "/" is the file separator.
     * @param name
     * @return
     */
    public static String getLastTokenInPath(String name) {
        String [] nameSplit = name.split("/");
        return nameSplit[nameSplit.length -1];
    }

    /**
     * Returns true iff sequence contains a (possibly empty) list of consecutive integers (but not necessarily in consecutive order).
     * @param sequence
     * @return
     */
    public static boolean validateRunningSequence(List<Integer> sequence) {
        Collections.sort(sequence);
        Integer prevElement = null;
        for (Integer element: sequence) {
             if (prevElement != null) {
                 if (!(prevElement + 1 == element)) {
                     return false;
                 }
             }
            prevElement = element;
        }
        return true;
    }

    /**
     * Uses standard library routines to count the number of <failure> elements in the xml representation of
     * a ResultCollector.
     * @param resultCollector
     * @return the number of failures.
     */
    public static int countFailures(ResultCollector resultCollector) {
        String resultCollectorXml = resultCollector.toReport();
        return StringUtils.countMatches(resultCollectorXml, "<failure>");
    }
}
