package dk.statsbiblioteket.newspaper.schematron;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;

import java.io.IOException;

/**
 * An event handler which builds an XML representation of the directory structure for later validation with schematron.
 */
public class XmlBuilderEventHandler implements TreeEventHandler {

    int indentSize;
    String indent;
    String extra;

    StringBuilder xmlBuilder;
    boolean finished = false;

    /**
     * Construct an instance of this class with standard indentation.
     */
    public XmlBuilderEventHandler() {
        this(2);
    }

    /**
     * Construct an instance of this class with the given indentation.
     * @param indentSize the size of the indentation.
     */
    public XmlBuilderEventHandler(int indentSize) {
        this.indentSize = indentSize;
        xmlBuilder = new StringBuilder();
        indent = "";
        extra = "";
        for (int i = 0; i < indentSize; i++) {
            extra = extra + " ";
        }
    }

    /**
     * Returns the xml representation of the directory tree.
     * @return the xml.
     * @throws RuntimeException if the handler has not completed the traversal of the directory tree.
     */
    public String getXml() {
        if (!finished) {
            throw new RuntimeException("No xml. This handler has not been run.");
        } else {
            return xmlBuilder.toString();
        }
    }

    @Override
    public void handleNodeBegin(NodeBeginsParsingEvent event) {
        xmlBuilder.append(indent + "<node name=\"" + event.getName() + "\">\n");
        indent = indent + extra;
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        indent = indent.replaceFirst(extra, "");
        xmlBuilder.append(indent + "</node>\n");
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        if (event instanceof AttributeParsingEvent) {
            AttributeParsingEvent attributeParsingEvent = (AttributeParsingEvent) event;
            try {
                xmlBuilder.append(indent + "<attribute name=\"" + event.getName() + "\" checksum=\"" + attributeParsingEvent
                        .getChecksum() + "\" />\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void handleFinish() {
        finished = true;
    }
}
