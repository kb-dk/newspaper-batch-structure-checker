package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.ParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import dk.statsbiblioteket.doms.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Checks the directory structure of a batch. This should run both at Ninestars and at SB.
 *
 * @author jrg
 */
public class BatchStructureChecker {//TODO implements dk.statsbiblioteket.autonomous.RunnableComponent
    private TreeIterator iterator;
    private static final String indentString = "..................................................";


    public void checkBatchStructure() throws Exception {
        checkStructure(getIterator());
    }

    public TreeIterator getIterator() throws URISyntaxException {
        if (iterator == null){
            File file = new File(Thread.currentThread().getContextClassLoader().getResource("batch").toURI());
            System.out.println(file);
            iterator = new TransformingIteratorForFileSystems(file, "\\.", "\\.jp2$", ".md5");
        }
        return iterator;
    }

    /**
     * Check the batch structure tree received for errors.
     *
     * @param newspaperIterator Iterator for the batch structure tree to check
     * @throws IOException
     */
    private void checkStructure(TreeIterator newspaperIterator) throws IOException {
        int indent = 0;
        while (newspaperIterator.hasNext()) {
            ParsingEvent next = newspaperIterator.next();
            String s;

            // TODO so far just traverses and pretty-prints.. do check for actual errors plz
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
                    System.out.println(s + printEvent(next));
                    break;
                }
                case Attribute: {
                    // This is an attribute for current node, print it
                    s = getIndent(indent);

                    AttributeParsingEvent attributeEvent = (AttributeParsingEvent) next;
                    List<String> content = IOUtils.readLines(attributeEvent.getText());
                    String checksum = attributeEvent.getChecksum();
                    System.out.println(s + printEvent(next));
                    s = getIndent(indent + 2);
                    System.out.println(s + "[" + content.size() + " lines of content]");
                    System.out.println(s + "Checksum: " + checksum);
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

    private String printEvent(ParsingEvent next) {
        switch (next.getType()){
            case NodeBegin:
                return "<"+next.getLocalname()+">";
            case NodeEnd:
                return "</"+next.getLocalname()+">";
            case Attribute:
                return "<"+next.getLocalname()+"/>";
            default:
                return next.toString();
        }
    }
}
