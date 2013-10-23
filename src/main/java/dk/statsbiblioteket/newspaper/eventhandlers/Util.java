package dk.statsbiblioteket.newspaper.eventhandlers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 23/10/13
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */
public class Util {
    public static String getLastTokenInPath(String name) {
        String [] nameSplit = name.split("/");
        return nameSplit[nameSplit.length -1];
    }

    /**
     * Returns true iff sequence contains a list of consecutive integers, but not necessarily in consecutive order.
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
}
