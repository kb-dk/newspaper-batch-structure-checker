package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.doms.iterator.common.AttributeEvent;
import dk.statsbiblioteket.doms.iterator.common.Event;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import dk.statsbiblioteket.doms.iterator.filesystem.TransformingIteratorForFileSystems;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks the directory structure of a batch. This should run both at Ninestars and at SB.
 *
 * @author jrg
 */
public class BatchStructureChecker {
    private TreeIterator iterator;
    private static final String indentString = "..................................................";


    public void checkBatchStructure() throws Exception {
        printStructure(getIterator());
    }

    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null){
            File file = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
            System.out.println(file);
            iterator = new TransformingIteratorForFileSystems(file, "\\.", "\\.jp2$");
        }
        return iterator;
    }

    /**
     * Pretty-print the batch structure tree received.
     *
     * @param newspaperIterator Iterator for the batch structure tree to print
     * @throws IOException
     */
    private void printStructure(TreeIterator newspaperIterator) throws IOException {
        int indent = 0;
        while (newspaperIterator.hasNext()) {
            Event next = newspaperIterator.next();
            String s;

            switch (next.getType()){
                case NodeBegin: {
                    // We have entered a node, increase indent-level
                    s = getIndent(indent);
                    System.out.println(s + printEvent(next));
                    indent += 2;
                    break;
                }
                case NodeEnd: {
                    // We have exited a node, decrease indent-level again
                    indent -= 2;
                    s = getIndent(indent);
                    //System.out.println(s + printEvent(next));
                    break;
                }
                case Attribute: {
                    // This is an attribute for current node, print it (not)
                    s = getIndent(indent);

                    AttributeEvent attributeEvent = (AttributeEvent) next;
                    List<String> content = IOUtils.readLines(attributeEvent.getText());
                    //System.out.println(s + printEvent(next));
                    s = getIndent(indent + 2);
                    //System.out.println(s + "[" + content.size() + " lines of content]");
                    break;
                }
            }
        }
    }

    /**
     * Create a string of dots, used for indenting.
     *
     * @param indent Number of characters to indent
     * @return A string of dots, as long as specified by input parameter 'indent'
     */
    private static String getIndent(int indent) {
        String s;
        if (indent > 0){
            s = indentString.substring(0, indent);
        } else {
            s = "";
        }
        return s;
    }

    private String printEvent(Event next) {
        switch (next.getType()){
            case NodeBegin:
                return "<"+next.getPath()+">";
            case NodeEnd:
                return "</"+next.getPath()+">";
            case Attribute:
                return "<"+next.getPath()+"/>";
            default:
                return next.toString();
        }
    }

    public void testIteratorWithSkipping() throws Exception {
        List<TreeIterator> avisIterators = new ArrayList<TreeIterator>();

        System.out.println("Print the batch and film, and store the iterators for the aviser");
        int indent = 0;
        while (getIterator().hasNext()){
            Event next = getIterator().next();

            String s;
            switch (next.getType()){
                case NodeBegin:
                    s = getIndent(indent);
                    System.out.println(s+printEvent(next));
                    indent+=2;
                    if (indent > 4){
                        TreeIterator avis = getIterator().skipToNextSibling();
                        avisIterators.add(avis);
                        indent-=2;
                    }
                    break;
                case NodeEnd:
                    indent-=2;
                    s = getIndent(indent);
                    System.out.println(s+printEvent(next));
                    break;
                case Attribute:
                    s = getIndent(indent);
                    System.out.println(s+printEvent(next));
                    break;
            }
        }

        System.out.println("Print each of the newspapers in order");
        for (TreeIterator avisIterator : avisIterators) {
            System.out.println("We found this newspaper");
            printStructure(avisIterator);
        }

    }
}
