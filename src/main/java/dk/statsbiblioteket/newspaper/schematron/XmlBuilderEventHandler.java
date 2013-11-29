package dk.statsbiblioteket.newspaper.schematron;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeEndParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.eventhandlers.TreeEventHandler;
import dk.statsbiblioteket.newspaper.eventhandlers.Util;

import java.io.IOException;

/**
 * An event handler which builds an XML representation of the directory structure for later validation with schematron.
 */
public class XmlBuilderEventHandler implements TreeEventHandler {

    int indentSize;

    /**
     * blank-string for the current indentation.
     */
    String currentIndent;

    /**
     * blank string of length indentSize for augmenting/decrementing the current indent.
     */
    String extraIndentPerLevel;

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
        currentIndent = "";
        extraIndentPerLevel = "";
        for (int i = 0; i < indentSize; i++) {
            extraIndentPerLevel = extraIndentPerLevel + " ";
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
        String shortName = Util.getLastTokenInPath(event.getName());
        xmlBuilder.append(currentIndent + "<node name=\"" + event.getName() + "\" shortName=\"" + shortName + "\">\n");
        currentIndent = currentIndent + extraIndentPerLevel;
    }

    @Override
    public void handleNodeEnd(NodeEndParsingEvent event) {
        currentIndent = currentIndent.replaceFirst(extraIndentPerLevel, "");
        xmlBuilder.append(currentIndent + "</node>\n");
    }

    @Override
    public void handleAttribute(AttributeParsingEvent event) {
        String shortName = Util.getLastTokenInPath(event.getName());

        try {
            xmlBuilder.append(currentIndent + "<attribute name=\"" + event.getName() + "\" shortName=\"" + shortName + "\"  checksum=\"" + event
                    .getChecksum() + "\" />\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void handleFinish() {
        finished = true;
    }
}
