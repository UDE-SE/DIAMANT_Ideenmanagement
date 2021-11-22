package de.unidue.se.diamant.backend.backendservice.controller;

import de.unidue.se.diamant.backend.backendservice.dto.user.NewUserDTO;
import de.unidue.se.diamant.backend.backendservice.service.challenge.ChallengeService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.IdeaService;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.TeamMember;
import de.unidue.se.diamant.backend.backendservice.service.invitation.InvitationInformation;
import de.unidue.se.diamant.backend.backendservice.service.invitation.InvitationType;
import de.unidue.se.diamant.backend.backendservice.service.invitation.JWTService;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUserService;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUtils;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.dto.KeycloakUser;
import de.unidue.se.diamant.backend.backendservice.service.permissions.DenyAccessInterceptor;
import de.unidue.se.diamant.backend.backendservice.service.permissions.PermissionServiceFacade;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Log
@RestController
@RequestMapping(value = "/api")
public class InviteController {
    private final DenyAccessInterceptor denyAccessInterceptor;
    private final ChallengeService challengeService;
    private final IdeaService ideaService;
    private final JWTService jwtService;

    @Autowired
    public InviteController(PermissionServiceFacade permissionService, DenyAccessInterceptor denyAccessInterceptor, ChallengeService challengeService, KeycloakUserService keycloakUserService, IdeaService ideaService, JWTService jwtService) {
        this.denyAccessInterceptor = denyAccessInterceptor;
        this.challengeService = challengeService;
        this.ideaService = ideaService;
        this.jwtService = jwtService;
    }

    /**
     * Akzeptiert eine Einladung
     *
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/invitation/accept")
    public ResponseEntity<Void> acceptInvitation(@RequestBody @Valid @NotBlank String token, BindingResult bindingResult, Authentication authentication) throws BindException {
        InvitationInformation invitationInformationFromToken;
        try {
            invitationInformationFromToken = jwtService.getInvitationInformationFromToken(token);
            Person currentUserPerson = KeycloakUtils.getPersonWithoutClassCastException(authentication);

            if(InvitationType.REFEREE.equals(invitationInformationFromToken.getType())){
                String challengeId = invitationInformationFromToken.getChallengeId();
                Challenge challengeDetails = challengeService.getChallengeDetails(challengeId);
                Set<Person> referees = challengeDetails.getReferees();
                referees.add(currentUserPerson);
                challengeService.updateReferees(challengeId, referees, challengeService.getChallengeDetails(challengeId).getCaller());
            }

            if(InvitationType.INVITED_USER.equals(invitationInformationFromToken.getType())){
                String challengeId = invitationInformationFromToken.getChallengeId();
                Challenge challengeDetails = challengeService.getChallengeDetails(challengeId);
                Set<Person> invitedUsers = challengeDetails.getInvitedUsers();
                invitedUsers.add(currentUserPerson);
                challengeService.updateInvitedUsers(challengeId, invitedUsers);
            }

            if(InvitationType.TEAM_MEMBER.equals(invitationInformationFromToken.getType())){
                denyAccessInterceptor.interceptViewChallenge(invitationInformationFromToken.getChallengeId(), authentication);
                Idea idea = ideaService.findById(invitationInformationFromToken.getIdeaId());
                idea.getTeamMember().add(TeamMember.fromPerson(currentUserPerson));
                ideaService.updateTeamMember(invitationInformationFromToken.getIdeaId(), new ArrayList<>(idea.getTeamMember()));
            }


        } catch (IllegalArgumentException iae) {
            bindingResult.rejectValue("token", "", "Der Einladungstoken ist nicht (mehr) gültig!");

        }
        if(bindingResult.hasErrors()){
            throw new BindException(bindingResult);
        }
        return ResponseEntity.ok().build();
    }

}
