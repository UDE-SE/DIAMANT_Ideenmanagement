package de.unidue.se.diamant.backend.backendservice.service.chat;

import de.unidue.se.diamant.backend.backendservice.controller.ChatController;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.owasp.html.HtmlStreamEventProcessor;
import org.owasp.html.HtmlStreamEventReceiver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ReplaceCanvasElementsWithATagsHtmlStreamEventReceiverWrapperTest {

    @Test
    public void testCloseTagReplacesCanvasElementTagWithATag(){
        HtmlStreamEventReceiver mock = Mockito.mock(HtmlStreamEventReceiver.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doNothing().when(mock).closeTag(stringArgumentCaptor.capture());
        ReplaceCanvasElementsWithATagsHtmlStreamEventReceiverWrapper sut = new ReplaceCanvasElementsWithATagsHtmlStreamEventReceiverWrapper(mock);

        sut.closeTag(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE);
        sut.closeTag("div");
        sut.closeTag("a");

        Assertions.assertThat(stringArgumentCaptor.getAllValues()).containsExactly("a", "div", "a");
    }

    @Test
    public void testOpenTagReplacesCanvasElementTagWithATag(){
        HtmlStreamEventReceiver mock = Mockito.mock(HtmlStreamEventReceiver.class);
        ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<List> attrsCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.doNothing().when(mock).openTag(stringArgumentCaptor.capture(), attrsCaptor.capture());
        ReplaceCanvasElementsWithATagsHtmlStreamEventReceiverWrapper sut = new ReplaceCanvasElementsWithATagsHtmlStreamEventReceiverWrapper(mock);

        sut.openTag(ChatController.TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE, Arrays.asList(ChatController.ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE, "#test"));
        sut.openTag("div", Collections.emptyList());
        sut.openTag("a", Arrays.asList("href", "test.example.com"));

        Assertions.assertThat(stringArgumentCaptor.getAllValues()).containsExactly("a", "div", "a");
        Assertions.assertThat(attrsCaptor.getAllValues()).containsExactly(Arrays.asList("href", "#test"), Collections.emptyList(), Arrays.asList("href", "test.example.com"));
    }

}
