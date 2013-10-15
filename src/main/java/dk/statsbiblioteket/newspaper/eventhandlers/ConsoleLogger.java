package dk.statsbiblioteket.newspaper.eventhandlers;

import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.ParsingEvent;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jrg
 * Date: 10/15/13
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConsoleLogger extends DefaultTreeEventHandler {
    private static final String indentString = "..................................................";
    int indentLevel = 0;

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        printIndentNode(event);
        indentLevel += 2;
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        // We have exited a node, decrease indentLevel-level again
        indentLevel -= 2;
        printIndentNode(event);
    }

    /**
     * This is an attribute for current node, print it
     */
    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        try {
            List<String> content = IOUtils.readLines(event.getText());

            String checksum = event.getChecksum();
            printIndentNode(event);
            printIndentAttribute("[" + content.size() + " lines of content]");
            printIndentAttribute("Checksum: " + checksum);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void printIndentNode(ParsingEvent event) {
        System.out.println(getIndentString() + printEvent(event));
    }

    private void printIndentAttribute(String attributeString) {
        System.out.println(getIndentString() + ".." + attributeString);
    }


    /**
     * Create a string of dots, used for indenting.
     *
     * @return A string of dots, as long as specified by input parameter 'indentLevel'
     */
    private String getIndentString() {
        String s;
        if (indentLevel > 0){
            s = indentString.substring(0, indentLevel);
        } else {
            s = "";
        }
        return s;
    }

    private String printEvent(ParsingEvent event) {
        switch (event.getType()){
            case NodeBegin:
                return "<"+event.getLocalname()+">";
            case NodeEnd:
                return "</"+event.getLocalname()+">";
            case Attribute:
                return "<"+event.getLocalname()+"/>";
            default:
                return event.toString();
        }
    }
}
