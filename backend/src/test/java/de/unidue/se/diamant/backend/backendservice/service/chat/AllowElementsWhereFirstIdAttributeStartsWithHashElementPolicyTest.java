package de.unidue.se.diamant.backend.backendservice.service.chat;

import de.unidue.se.diamant.backend.backendservice.controller.ChatController;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNull;

public class AllowElementsWhereFirstIdAttributeStartsWithHashElementPolicyTest {

    @Test
    public void testInputs() {
        AllowElementsWhereFirstIdAttributeStartsWithHashElementPolicy sut = new AllowElementsWhereFirstIdAttributeStartsWithHashElementPolicy();
        assertNull(sut.apply("canvas-element", Collections.emptyList()));
        assertNull(sut.apply("other", Collections.emptyList()));
        assertNull(sut.apply("canvas-element", Arrays.asList("abc", "def")));
        assertNull(sut.apply("canvas-element", Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "def")));
        assertNull(sut.apply("canvas-element", Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "https://example.com")));
        assertNull(sut.apply("canvas-element", Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "https://example.com#")));
        assertNull(sut.apply("canvas-element", Arrays.asList("test", "#")));
        assertNull(sut.apply("canvas-element", Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "#")));
        assertNull(sut.apply("canvas-element", Arrays.asList("otherAttr", "a", ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "#")));
        assertNull(sut.apply("canvas-element", Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "test", ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "#")));
        Assertions.assertEquals("canvas-element", sut.apply("canvas-element", Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "#element1")));
        Assertions.assertEquals("a", sut.apply("a", Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "#element1")));assertNull(sut.apply("canvas-element", Collections.emptyList()));
        assertNull(sut.apply(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Collections.emptyList()));
        assertNull(sut.apply(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Arrays.asList("abc", "def")));
        assertNull(sut.apply(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "def")));
        assertNull(sut.apply(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "https://example.com")));
        assertNull(sut.apply(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "https://example.com#")));
        assertNull(sut.apply(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Arrays.asList("test", "#")));
        assertNull(sut.apply(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "#")));
        assertNull(sut.apply(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Arrays.asList("otherAttr", "a", ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "#")));
        assertNull(sut.apply(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Arrays.asList(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, "test", ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "#")));
        Assertions.assertEquals(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, sut.apply(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "#element1")));
        Assertions.assertEquals(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, sut.apply(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "#element1")));
    }

}
