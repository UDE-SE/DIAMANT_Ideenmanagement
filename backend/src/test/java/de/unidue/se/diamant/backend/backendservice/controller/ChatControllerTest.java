package de.unidue.se.diamant.backend.backendservice.controller;

import de.unidue.se.diamant.backend.backendservice.controller.sse.SseEvent;
import de.unidue.se.diamant.backend.backendservice.dto.idea.ChatEntryDTO;
import de.unidue.se.diamant.backend.backendservice.service.chat.ChatService;
import de.unidue.se.diamant.backend.backendservice.service.chat.domain.ChatEntry;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.IdeaService;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.permissions.DenyAccessInterceptor;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Date;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChatControllerTest extends RestDocTest {

    @MockBean
    private DenyAccessInterceptor interceptor;

    @MockBean
    private IdeaService ideaService;

    @MockBean
    private ChatService chatService;

    @MockBean
    private ServerSendEventController sseController;

    private final String userMail = "caller@uni-due.de";

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void saveTeamChatMessage() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        Idea idea = Idea.builder().id(ideaId).build();
        idea.getTeamChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Mustermann").email(userMail).build()).text("Erste Nachricht").build());
        idea.getTeamChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Mustermann").email(userMail).build()).text("Zweite Nachricht").build());

        String postArguments = "Coole neue Chat Nachricht <lösungs-element id='#test'>Canvas Element Referenz</lösungs-element>";
        ArgumentCaptor<ChatEntry> chatEntryArgumentCaptor = ArgumentCaptor.forClass(ChatEntry.class);
        Mockito.doNothing().when(interceptor).interceptSaveTeamMessage(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.when(ideaService.findById(Mockito.eq(ideaId))).thenReturn(idea);
        Mockito.doNothing().when(chatService).saveTeamMessage(Mockito.eq(ideaId), chatEntryArgumentCaptor.capture());

        this.mockMvc.perform(
                post("/api/challenge/" + challengeId + "/idea/" + ideaId + "/chat/team")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postArguments)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(idea.getTeamChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet()))));

        Mockito.verify(interceptor, Mockito.times(1)).interceptSaveTeamMessage(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.verify(ideaService, Mockito.times(1)).findById(Mockito.eq(ideaId));
        Mockito.verify(chatService, Mockito.times(1)).saveTeamMessage(Mockito.eq(ideaId), chatEntryArgumentCaptor.capture());
        Mockito.verify(sseController, Mockito.times(1)).sendSseEvent(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.eq(SseEvent.SseEventType.TEAM_CHAT));
        Assertions.assertThat(chatEntryArgumentCaptor.getValue().getText()).isEqualTo("Coole neue Chat Nachricht <a href=\"#test\">Canvas Element Referenz</a>");
        Assertions.assertThat(chatEntryArgumentCaptor.getValue().getAuthor()).isEqualTo(Person.builder().name(userMail + " " + userMail).email(userMail).build());
        Assertions.assertThat(chatEntryArgumentCaptor.getValue().getDate()).isNotNull();
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void saveRefereesChatMessage() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        Idea idea = Idea.builder().id(ideaId).build();
        idea.getTeamChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Mustermann").email(userMail).build()).text("Erste Nachricht").build());
        idea.getTeamChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Mustermann").email(userMail).build()).text("Zweite Nachricht").build());
        idea.getRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Mustermann").email(userMail).build()).text("Gutachter Nachricht").build());

        String postArguments = "Coole neue Chat Nachricht <lösungs-element id='#test'>Canvas Element Referenz</lösungs-element>";
        ArgumentCaptor<ChatEntry> chatEntryArgumentCaptor = ArgumentCaptor.forClass(ChatEntry.class);
        Mockito.doNothing().when(interceptor).interceptSaveRefereesMessage(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.when(ideaService.findById(Mockito.eq(ideaId))).thenReturn(idea);
        Mockito.doNothing().when(chatService).saveRefereesMessage(Mockito.eq(ideaId), chatEntryArgumentCaptor.capture());

        this.mockMvc.perform(
                post("/api/challenge/" + challengeId + "/idea/" + ideaId + "/chat/referees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postArguments)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(idea.getRefereesChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet()))));

        Mockito.verify(interceptor, Mockito.times(1)).interceptSaveRefereesMessage(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.verify(ideaService, Mockito.times(1)).findById(Mockito.eq(ideaId));
        Mockito.verify(chatService, Mockito.times(1)).saveRefereesMessage(Mockito.eq(ideaId), chatEntryArgumentCaptor.capture());
        Mockito.verify(sseController, Mockito.times(1)).sendSseEvent(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.eq(SseEvent.SseEventType.REFEREES_CHAT));
        Assertions.assertThat(chatEntryArgumentCaptor.getValue().getText()).isEqualTo("Coole neue Chat Nachricht <a href=\"#test\">Canvas Element Referenz</a>");
        Assertions.assertThat(chatEntryArgumentCaptor.getValue().getAuthor()).isEqualTo(Person.builder().name(userMail + " " + userMail).email(userMail).build());
        Assertions.assertThat(chatEntryArgumentCaptor.getValue().getDate()).isNotNull();
    }

    @Test
    @WithMockUser(username = userMail, roles = {"user"})
    public void saveTeamAndRefereesChatMessage() throws Exception {
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        Idea idea = Idea.builder().id(ideaId).build();
        idea.getTeamChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Mustermann").email(userMail).build()).text("Erste Nachricht").build());
        idea.getTeamChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Mustermann").email(userMail).build()).text("Zweite Nachricht").build());
        idea.getRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Mustermann").email(userMail).build()).text("Gutachter Nachricht").build());
        idea.getTeamAndRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Mustermann").email(userMail).build()).text("Gutachter Nachricht").build());
        idea.getTeamAndRefereesChat().add(ChatEntry.builder().date(new Date()).author(Person.builder().name("Max Mustermann1").email(userMail).build()).text("Team Nachricht").build());

        String postArguments = "Coole neue Chat Nachricht <lösungs-element id='#test'>Canvas Element Referenz</lösungs-element>";
        ArgumentCaptor<ChatEntry> chatEntryArgumentCaptor = ArgumentCaptor.forClass(ChatEntry.class);
        Mockito.doNothing().when(interceptor).interceptSaveTeamAndRefereesMessage(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.when(ideaService.findById(Mockito.eq(ideaId))).thenReturn(idea);
        Mockito.doNothing().when(chatService).saveTeamAndRefereesMessage(Mockito.eq(ideaId), chatEntryArgumentCaptor.capture());

        this.mockMvc.perform(
                post("/api/challenge/" + challengeId + "/idea/" + ideaId + "/chat/teamandreferees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postArguments)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(idea.getTeamAndRefereesChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet()))));

        Mockito.verify(interceptor, Mockito.times(1)).interceptSaveTeamAndRefereesMessage(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.any(Authentication.class));
        Mockito.verify(ideaService, Mockito.times(1)).findById(Mockito.eq(ideaId));
        Mockito.verify(chatService, Mockito.times(1)).saveTeamAndRefereesMessage(Mockito.eq(ideaId), chatEntryArgumentCaptor.capture());
        Mockito.verify(sseController, Mockito.times(1)).sendSseEvent(Mockito.eq(challengeId), Mockito.eq(ideaId), Mockito.eq(SseEvent.SseEventType.TEAM_AND_REFEREES_CHAT));
        Assertions.assertThat(chatEntryArgumentCaptor.getValue().getText()).isEqualTo("Coole neue Chat Nachricht <a href=\"#test\">Canvas Element Referenz</a>");
        Assertions.assertThat(chatEntryArgumentCaptor.getValue().getAuthor()).isEqualTo(Person.builder().name(userMail + " " + userMail).email(userMail).build());
        Assertions.assertThat(chatEntryArgumentCaptor.getValue().getDate()).isNotNull();
    }

    @Test
    public void sanitizeText(){
        ChatController sut = new ChatController(null, null, null, null);
        String s = sut.sanitizeString("<script>test</script>Hallo <a href='test'>link</a> <lösungs-element id='#test'>das hier</lösungs-element>");
        Assertions.assertThat(s).isEqualTo("Hallo link <a href=\"#test\">das hier</a>");
    }

}
