package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.ParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.TreeIterator;
import dk.statsbiblioteket.doms.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
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
public class BatchStructureChecker {
    private TreeIterator iterator;
    private static final String indentString = "..................................................";


    public void checkBatchStructure(String batchPid, ResultCollector resultCollector) throws Exception {
        checkStructure(getIterator(batchPid), resultCollector);
    }

    public TreeIterator getIterator(String batch) throws URISyntaxException {
        if (iterator == null){
            File file = new File(Thread.currentThread().getContextClassLoader().getResource(batch).toURI());
            System.out.println(file);
            iterator = new TransformingIteratorForFileSystems(file, "\\.", "\\.jp2$", ".md5");
        }
        return iterator;
    }

    /**
     * Check the batch structure tree received for errors.
     *
     * @param newspaperIterator Iterator for the batch structure tree to check
     * @param resultCollector Object to collect results of the structure check
     * @throws IOException
     */
    private void checkStructure(TreeIterator newspaperIterator, ResultCollector resultCollector)
            throws IOException {
        boolean errorFound = false;

        int indent = 0;
        while (newspaperIterator.hasNext()) {
            ParsingEvent next = newspaperIterator.next();
            String s;


            // TODO so far just traverses and pretty-prints.. do check for actual errors plz
            switch (next.getType()){
                case NodeBegin: {
                    //if (hasExtension(next, "jp2") || hasExtension(next, "xml")) {
                    //    errorFound |= ();
                    //}

                    // We have entered a node, increase indent-level TODO remove this before prod
                    s = getIndent(indent);
                    System.out.println(s + printEvent(next));
                    indent += 2;
                    break;
                }
                case NodeEnd: {
                    // We have exited a node, decrease indent-level again TODO remove this before prod
                    indent -= 2;
                    s = getIndent(indent);
                    System.out.println(s + printEvent(next));
                    break;
                }
                case Attribute: {
                    // This is an attribute for current node, print it TODO remove this before prod
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

        resultCollector.setSuccess(!errorFound);
        // TODO resultCollector.addMessage();
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

    private boolean hasExtension(ParsingEvent event, String s) {
        return event.getLocalname().endsWith("." + s);
    }

    private boolean hasChecksum(ParsingEvent event) {
        AttributeParsingEvent attributeEvent = (AttributeParsingEvent) event;
        String checksum;
        try {
            checksum = attributeEvent.getChecksum();
        } catch (Exception e) {
            return false;
        }
        return (checksum != null && !checksum.trim().equals(""));
    }
}
