package de.unidue.se.diamant.backend.backendservice.service.permissions;

import de.unidue.se.diamant.backend.backendservice.controller.ChatController;
import de.unidue.se.diamant.backend.backendservice.dto.ResourceNotFoundException;
import de.unidue.se.diamant.backend.backendservice.service.challenge.ChallengeService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.idea.IdeaService;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.IdeaState;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUtils;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.logging.Level;

@Service
@Log
public class DenyAccessInterceptor {

    private PermissionServiceFacade permissionServiceFacade;
    private ChallengeService challengeService;
    private IdeaService ideaService;


    @Autowired
    public DenyAccessInterceptor(PermissionServiceFacade permissionServiceFacade, ChallengeService challengeService, IdeaService ideaService) {
        this.permissionServiceFacade = permissionServiceFacade;
        this.challengeService = challengeService;
        this.ideaService = ideaService;
    }

    public void interceptUploadAttachmentToChallenge(String challengeId, Authentication authentication) {
        String currentUser = KeycloakUtils.getUser(authentication);
        Challenge challenge = getChallenge(challengeId);
        StringBuilder errorBuffer = new StringBuilder();
        if(! permissionServiceFacade.checkIsAllowedToUploadAttachmentToChallenge(challenge, currentUser, errorBuffer)){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, Anhänge zu der Challenge '%s' hinzuzufügen!", currentUser, challengeId));
        }
    }

    public void interceptViewChallenge(String challengeId, Authentication authentication) {
        String currentUser = KeycloakUtils.getUserOrDefault(authentication);
        Challenge challenge = getChallenge(challengeId);
        StringBuilder errorBuffer = new StringBuilder();
        if(! permissionServiceFacade.checkIsAllowedToViewChallenge(challenge, currentUser, errorBuffer)){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, auf die Challenge '%s' zuzugreifen oder diese befindet sich in einem Status, der dies nicht erlaubt!", currentUser, challengeId));
        }
    }

    public void interceptUploadAttachmentToIdea(String challengeId, String ideaId, String attachmentId, Authentication authentication) {
        String currentUser = KeycloakUtils.getUser(authentication);
        Challenge challenge = getChallenge(challengeId);
        Idea idea = getIdea(ideaId);
        StringBuilder errorBuffer = new StringBuilder();
        if(! permissionServiceFacade.checkIsAllowedToUploadAttachmentToIdea(challenge, idea, attachmentId, currentUser, errorBuffer)){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException("Hochladen des Anhangs zu der Idee nicht möglich!");
        }
    }

    public void interceptUpdateIdeaState(String challengeId, String ideaId, IdeaState newState, Authentication authentication) {
        String currentUser = KeycloakUtils.getUser(authentication);
        Challenge challenge = getChallenge(challengeId);
        Idea idea = getIdea(ideaId);
        StringBuilder errorBuffer = new StringBuilder();
        if (! permissionServiceFacade.checkIsAllowedToUpdateIdeaState(challenge, idea, newState, currentUser, errorBuffer)) {
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, den Zustand der Idee %s in den Zustand '%s' zu ändern!", currentUser, ideaId, newState));
        }
    }

    public void interceptVote(String challengeId, String ideaId, Authentication authentication) {
        String currentUser = KeycloakUtils.getUser(authentication);
        Challenge challenge = getChallenge(challengeId);
        Idea idea = getIdea(ideaId);
        StringBuilder errorBuffer = new StringBuilder();
        if(! permissionServiceFacade.checkIsAllowedToVote(challenge, idea, currentUser, errorBuffer)){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, über die Idee %s abzustimmen!", currentUser, ideaId));
        }
    }

    public void interceptViewIdea(String challengeId, String ideaId, Authentication authentication) {
        String currentUser = KeycloakUtils.getUserOrDefault(authentication);
        Challenge challenge = getChallenge(challengeId);
        Idea idea = getIdea(ideaId);
        StringBuilder errorBuffer = new StringBuilder();
        if(! permissionServiceFacade.checkIsAllowedToViewIdea(challenge, idea, currentUser, errorBuffer)){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, über die Idee %s anzusehen!", currentUser, ideaId));
        }
    }

    public void interceptViewIdeaChat(String challengeId, String ideaId, ChatController.ChatType chatType, Authentication authentication) {
        String currentUser = KeycloakUtils.getUserOrDefault(authentication);
        Challenge challenge = getChallenge(challengeId);
        Idea idea = getIdea(ideaId);
        StringBuilder errorBuffer = new StringBuilder();
        boolean isAllowedToView = false;
        switch (chatType) {
            case TEAM:
                isAllowedToView = permissionServiceFacade.checkIsAllowedToViewTeamChat(challenge, idea, currentUser, errorBuffer);
                break;
            case REFEREES:
                isAllowedToView = permissionServiceFacade.checkIsAllowedToViewRefereeChat(challenge, idea, currentUser, errorBuffer);
                break;
            case TEAM_AND_REFEREES:
                isAllowedToView = permissionServiceFacade.checkIsAllowedToViewTeamAndRefereeChat(challenge, idea, currentUser, errorBuffer);
                break;
        }
        if(! isAllowedToView){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, den Chat %s der Idee %s anzusehen!", currentUser, chatType.toString(), ideaId));
        }
    }

    public void interceptEditIdea(String challengeId, Idea idea, Authentication authentication) {
        String currentUser = KeycloakUtils.getUserOrDefault(authentication);
        Challenge challenge = getChallenge(challengeId);
        StringBuilder errorBuffer = new StringBuilder();
        if(! permissionServiceFacade.checkIsAllowedToEditIdea(challenge, idea, currentUser, errorBuffer)){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, über die Idee %s zu bearbeiten!", currentUser, idea.getId()));
        }
    }

    public void interceptSaveTeamMessage(String challengeId, String ideaId, Authentication authentication) {
        String currentUser = KeycloakUtils.getUserOrDefault(authentication);
        Challenge challenge = getChallenge(challengeId);
        Idea idea = getIdea(ideaId);
        StringBuilder errorBuffer = new StringBuilder();
        if(! permissionServiceFacade.checkIsAllowedToViewTeamChat(challenge, idea, currentUser, errorBuffer)){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, über Nachrichten zum Team-Chat der Idee %s hinzuzufügen!", currentUser, idea.getId()));
        }
    }

    public void interceptSaveRefereesMessage(String challengeId, String ideaId, Authentication authentication) {
        String currentUser = KeycloakUtils.getUserOrDefault(authentication);
        Challenge challenge = getChallenge(challengeId);
        Idea idea = getIdea(ideaId);
        StringBuilder errorBuffer = new StringBuilder();
        if(! permissionServiceFacade.checkIsAllowedToViewRefereeChat(challenge, idea, currentUser, errorBuffer)){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, über Nachrichten zum Gutachter-Chat der Idee %s hinzuzufügen!", currentUser, idea.getId()));
        }
    }

    public void interceptSaveTeamAndRefereesMessage(String challengeId, String ideaId, Authentication authentication) {
        String currentUser = KeycloakUtils.getUserOrDefault(authentication);
        Challenge challenge = getChallenge(challengeId);
        Idea idea = getIdea(ideaId);
        StringBuilder errorBuffer = new StringBuilder();
        if(! permissionServiceFacade.checkIsAllowedToViewTeamAndRefereeChat(challenge, idea, currentUser, errorBuffer)){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, über Nachrichten zum Team-und-Gutachter-Chat der Idee %s hinzuzufügen!", currentUser, idea.getId()));
        }
    }

    public void interceptEditIdea(String challengeId, String ideaId, Authentication authentication) {
        interceptEditIdea(challengeId, getIdea(ideaId), authentication);
    }

    public void interceptEditChallenge(String challengeId, Authentication authentication) {
        String currentUser = KeycloakUtils.getUserOrDefault(authentication);
        Challenge challenge = getChallenge(challengeId);
        StringBuilder errorBuffer = new StringBuilder();
        if(! permissionServiceFacade.checkIsCaller(challenge, currentUser, errorBuffer)){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, die Challenge %s zu bearbeiten", currentUser, challengeId));
        }
    }

    public void interceptNominateAsWinner(String challengeId, Authentication authentication) {
        String currentUser = KeycloakUtils.getUserOrDefault(authentication);
        Challenge challenge = getChallenge(challengeId);
        StringBuilder errorBuffer = new StringBuilder();
        if(! permissionServiceFacade.checkIsAllowedToNominateAsWinner(challenge, currentUser, errorBuffer)){
            log.log(Level.WARNING, errorBuffer.toString());
            throw new AccessDeniedException(String.format("Der Nutzer '%s' hat nicht die Rechte, Ideen der Challenge %s als Gewinner zu nominieren", currentUser, challengeId));
        }
    }


    protected Challenge getChallenge(String challengeId){
        Challenge challenge = challengeService.getChallengeDetails(challengeId);
        if(challenge == null){
            String error = "Keine Challenge mit der ID " + challengeId + " gefunden!";
            log.log(Level.WARNING, error);
            throw new ResourceNotFoundException(error);
        }
        return challenge;
    }

    protected Idea getIdea(String ideaId){
        Idea idea = ideaService.findById(ideaId);
        if(idea == null){
            String error = "Keine Idee mit der ID " + ideaId + " gefunden!";
            log.log(Level.WARNING, error);
            throw new ResourceNotFoundException(error);
        }
        return idea;
    }
}
