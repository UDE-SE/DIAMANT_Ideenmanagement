package de.unidue.se.diamant.backend.backendservice.controller;

import de.unidue.se.diamant.backend.backendservice.controller.sse.SseEvent;
import de.unidue.se.diamant.backend.backendservice.dto.idea.ChatEntryDTO;
import de.unidue.se.diamant.backend.backendservice.service.chat.AllowElementsWhereFirstIdAttributeStartsWithHashElementPolicy;
import de.unidue.se.diamant.backend.backendservice.service.chat.ChatService;
import de.unidue.se.diamant.backend.backendservice.service.chat.ReplaceCanvasElementsWithATagsHtmlStreamEventReceiverWrapper;
import de.unidue.se.diamant.backend.backendservice.service.chat.domain.ChatEntry;
import de.unidue.se.diamant.backend.backendservice.service.idea.IdeaService;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUtils;
import de.unidue.se.diamant.backend.backendservice.service.permissions.DenyAccessInterceptor;
import lombok.extern.java.Log;
import org.owasp.html.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@Log
@RestController
@RequestMapping(value = "/api")
public class ChatController {

    public static final String TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE = "lösungs-element";
    public static final String ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE = "id";
    public enum ChatType {
        TEAM, REFEREES, TEAM_AND_REFEREES
    }

    private DenyAccessInterceptor denyAccessInterceptor;
    private IdeaService ideaService;
    private ChatService chatService;
    private ServerSendEventController serverSendEventController;

    @Autowired
    public ChatController(DenyAccessInterceptor denyAccessInterceptor, IdeaService ideaService, ChatService chatService, ServerSendEventController serverSendEventController) {
        this.denyAccessInterceptor = denyAccessInterceptor;
        this.ideaService = ideaService;
        this.chatService = chatService;
        this.serverSendEventController = serverSendEventController;
    }

    /**
     * Gibt die Nachrichten des Teamchats zurück
     *
     * @param challengeId Id der Challenge
     * @param ideaId      Id der Idee
     * @return DTO mit den Details
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/challenge/{challengeId}/idea/{ideaId}/chat/team")
    public Set<ChatEntryDTO> getTeamChatMessage(@PathVariable String challengeId, @PathVariable String ideaId, Authentication authentication) {
        denyAccessInterceptor.interceptViewIdeaChat(challengeId, ideaId, ChatType.TEAM, authentication);
        return getChatMessages(ideaId, ChatType.TEAM);
    }

    /**
     * Speichert eine neue Nachricht in dem Team-Chat
     *
     * @param challengeId Id der Challenge
     * @param ideaId      Id der Idee
     * @return DTO mit den Details
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/challenge/{challengeId}/idea/{ideaId}/chat/team")
    public Set<ChatEntryDTO> saveTeamChatMessage(@PathVariable String challengeId, @PathVariable String ideaId, @RequestBody String content, Authentication authentication) {
        denyAccessInterceptor.interceptSaveTeamMessage(challengeId, ideaId, authentication);
        chatService.saveTeamMessage(ideaId, ChatEntry.builder().date(new Date()).author(KeycloakUtils.getPersonWithoutClassCastException(authentication)).text(sanitizeString(content)).build());
        serverSendEventController.sendSseEvent(challengeId, ideaId, SseEvent.SseEventType.TEAM_CHAT);
        return ideaService.findById(ideaId).getTeamChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet());
    }

    /**
     * Speichert eine neue Nachricht in dem Gutachter-Chat
     *
     * @param challengeId Id der Challenge
     * @param ideaId      Id der Idee
     * @return DTO mit den Details
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/challenge/{challengeId}/idea/{ideaId}/chat/referees")
    public Set<ChatEntryDTO> saveRefereesChatMessage(@PathVariable String challengeId, @PathVariable String ideaId, @RequestBody String content, Authentication authentication) {
        denyAccessInterceptor.interceptSaveRefereesMessage(challengeId, ideaId, authentication);
        chatService.saveRefereesMessage(ideaId, ChatEntry.builder().date(new Date()).author(KeycloakUtils.getPersonWithoutClassCastException(authentication)).text(sanitizeString(content)).build());
        serverSendEventController.sendSseEvent(challengeId, ideaId, SseEvent.SseEventType.REFEREES_CHAT);
        return ideaService.findById(ideaId).getRefereesChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/challenge/{challengeId}/idea/{ideaId}/chat/referees")
    public Set<ChatEntryDTO> getRefereesChatMessage(@PathVariable String challengeId, @PathVariable String ideaId, Authentication authentication) {
        denyAccessInterceptor.interceptViewIdeaChat(challengeId, ideaId, ChatType.REFEREES, authentication);
        return getChatMessages(ideaId, ChatType.REFEREES);
    }

    /**
     * Speichert eine neue Nachricht in dem Team-und-Gutachter-Chat
     *
     * @param challengeId Id der Challenge
     * @param ideaId      Id der Idee
     * @return DTO mit den Details
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/challenge/{challengeId}/idea/{ideaId}/chat/teamandreferees")
    public Set<ChatEntryDTO> saveTeamAndRefereesChatMessage(@PathVariable String challengeId, @PathVariable String ideaId, @RequestBody String content, Authentication authentication) {
        denyAccessInterceptor.interceptSaveTeamAndRefereesMessage(challengeId, ideaId, authentication);
        chatService.saveTeamAndRefereesMessage(ideaId, ChatEntry.builder().date(new Date()).author(KeycloakUtils.getPersonWithoutClassCastException(authentication)).text(sanitizeString(content)).build());
        serverSendEventController.sendSseEvent(challengeId, ideaId, SseEvent.SseEventType.TEAM_AND_REFEREES_CHAT);
        return ideaService.findById(ideaId).getTeamAndRefereesChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/challenge/{challengeId}/idea/{ideaId}/chat/teamandreferees")
    public Set<ChatEntryDTO> getTeamAndRefereesChatMessage(@PathVariable String challengeId, @PathVariable String ideaId, Authentication authentication) {
        denyAccessInterceptor.interceptViewIdeaChat(challengeId, ideaId, ChatType.TEAM_AND_REFEREES, authentication);
        return getChatMessages(ideaId, ChatType.TEAM_AND_REFEREES);
    }

    protected String sanitizeString(String input){
        PolicyFactory policyFactory = new HtmlPolicyBuilder()
                .allowAttributes(ATTRIBUTE_NAME_OF_CANVAS_ELEMENT_REFERENCE).onElements(TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE)
                .allowElements(new AllowElementsWhereFirstIdAttributeStartsWithHashElementPolicy(), TAG_NAME_OF_CANVAS_ELEMENT_REFERENCE)
                .withPostprocessor(ReplaceCanvasElementsWithATagsHtmlStreamEventReceiverWrapper::new)
                .toFactory();
        return policyFactory.sanitize(input);
    }

    private Set<ChatEntryDTO> getChatMessages(String ideaId, ChatType chatType){
        Idea idea = ideaService.findById(ideaId);
        switch (chatType) {
            case TEAM:
                return idea.getTeamChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet());
            case REFEREES:
                return idea.getRefereesChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet());
            case TEAM_AND_REFEREES:
                return idea.getTeamAndRefereesChat().stream().map(ChatEntryDTO::fromChatEntry).collect(Collectors.toSet());
            default:
                throw new IllegalStateException("Unexpected value: " + chatType);
        }
    }
}
