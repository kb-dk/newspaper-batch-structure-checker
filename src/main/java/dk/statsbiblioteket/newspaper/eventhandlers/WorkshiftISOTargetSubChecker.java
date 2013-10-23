package dk.statsbiblioteket.newspaper.eventhandlers;

/**
 * Performs various checks on Target-dddddd-dddd folder (under WORKSHIFT_ISO_TARGET) and its files.
 *
 * The expected structure of folders (node begin/end) and files (attributes) :
 *
 * begin "WORKSHIFT-ISO-TARGET"
 *      begin Target-dddddd-dddd
 *          attr Target-dddddd-dddd.mix.xml
 *          begin Target-dddddd-dddd.jp2
 *              attr "contents"
 *          end Target-dddddd-dddd.jp2
 *      end Target-dddddd-dddd
 * end "WORKSHIFT-ISO-TARGET"
 *
 * This class checks that
 * - There is exactly one mix file for every jp2-folder
 *
 * @author jrg
 */
public class WorkshiftISOTargetSubChecker {
}
