package dk.statsbiblioteket.newspaper.schematron;

import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;

import java.io.IOException;

/**
 * Class which can build an xml structure from a TreeIterator.
 */
public class TreeToXMLBuilder {

    public String buildXMLStructure(TreeIterator iterator) {
        String extra = "  ";
        String indent = "";
        StringBuilder builder = new StringBuilder();
        while (iterator.hasNext()) {
            ParsingEvent parsingEvent = iterator.next();
            switch (parsingEvent.getType()) {
                       case NodeBegin:
                           builder.append(indent + "<node name=\"" + parsingEvent.getName() + "\">\n");
                           indent+=extra;
                           break;
                       case NodeEnd:
                           indent = indent.replaceFirst(extra, "");
                           builder.append(indent + "</node>\n");
                           break;
                       case Attribute:
                           if (parsingEvent instanceof AttributeParsingEvent) {
                               AttributeParsingEvent attributeParsingEvent = (AttributeParsingEvent) parsingEvent;
                               try {
                                   builder.append(indent + "<attribute name=\"" + parsingEvent.getName() + "\" checksum=\"" + attributeParsingEvent
                                           .getChecksum() + "\" />\n");
                               } catch (IOException e) {
                                   throw new RuntimeException(e);
                               }
                           }
                           break;
                       default:
                            throw new RuntimeException("Unknown attribute: " + parsingEvent.toString());
                   }
        }
        return builder.toString();
    }


}
