package de.unidue.se.diamant.backend.backendservice.service.chat;

import de.unidue.se.diamant.backend.backendservice.controller.ChatController;
import org.owasp.html.ElementPolicy;

import javax.annotation.Nullable;
import java.util.List;

public class AllowElementsWhereFirstIdAttributeStartsWithHashElementPolicy implements ElementPolicy {

    @Override
    public String apply(String elementName, List<String> attrs) {
        for (int i = 0; i < attrs.size(); i++) {
            if(attrs.get(i).equals(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE)) {
                String attributeValue = attrs.get(i + 1);
                if(attributeValue.startsWith("#") && attributeValue.length() > 1){
                    return elementName;
                }
            }
        }
        return null;
    }
}
