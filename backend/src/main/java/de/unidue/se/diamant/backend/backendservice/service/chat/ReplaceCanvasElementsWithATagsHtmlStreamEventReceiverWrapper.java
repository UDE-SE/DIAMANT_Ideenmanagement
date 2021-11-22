package de.unidue.se.diamant.backend.backendservice.service.chat;

import de.unidue.se.diamant.backend.backendservice.controller.ChatController;
import org.owasp.html.HtmlStreamEventReceiver;
import org.owasp.html.HtmlStreamEventReceiverWrapper;

import java.util.Arrays;
import java.util.List;

public class ReplaceCanvasElementsWithATagsHtmlStreamEventReceiverWrapper extends HtmlStreamEventReceiverWrapper {
    public ReplaceCanvasElementsWithATagsHtmlStreamEventReceiverWrapper(HtmlStreamEventReceiver underlying) {
        super(underlying);
    }

    @Override public void openTag(String elementName, List<String> attrs) {
        if (ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE.equals(elementName)) {
            for (int i = 0; i < attrs.size(); i++) {
                if(attrs.get(i).equals(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE)){
                    List<String> modifiedAttrs = Arrays.asList("href", attrs.get(i +1));
                    super.openTag("a", modifiedAttrs);
                    return;
                }
            }
        } else {
            super.openTag(elementName, attrs);
        }
    }

    @Override
    public void closeTag(String elementName) {
        if (ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE.equals(elementName)) {
            super.closeTag("a");
        } else {
            super.closeTag(elementName);
        }
    }
}
