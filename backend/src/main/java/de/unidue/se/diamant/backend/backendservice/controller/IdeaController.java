package de.unidue.se.diamant.backend.backendservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import de.unidue.se.diamant.backend.backendservice.dto.idea.*;
import de.unidue.se.diamant.backend.backendservice.dto.idea.update.*;
import de.unidue.se.diamant.backend.backendservice.service.AttachmentService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.ChallengeService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.IdeaService;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.*;
import de.unidue.se.diamant.backend.backendservice.service.invitation.InvitationInformation;
import de.unidue.se.diamant.backend.backendservice.service.invitation.InvitationType;
import de.unidue.se.diamant.backend.backendservice.service.invitation.JWTService;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUserService;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUtils;
import de.unidue.se.diamant.backend.backendservice.service.permissions.DenyAccessInterceptor;
import de.unidue.se.diamant.backend.backendservice.service.permissions.PermissionServiceFacade;
import de.unidue.se.diamant.backend.backendservice.service.vote.VoteService;
import de.unidue.se.diamant.backend.backendservice.service.vote.dto.AverageAndOwnVoteForIdea;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.SmartValidator;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.logging.Level;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log
@RestController
@RequestMapping(value = "/api")
public class IdeaController {

    @Value("${app.server.base.url}")
    private String baseURL;

    @Value("${app.invitation.expiry.time.in.hours}")
    private int invitationLinkExpiryTime;

    private ChallengeService challengeService;
    private IdeaService ideaService;
    private AttachmentService attachmentService;
    private VoteService voteService;
    private PermissionServiceFacade permissionService;
    private DenyAccessInterceptor denyAccessInterceptor;
    private KeycloakUserService keycloakUserService;
    private JWTService jwtService;

    private SmartValidator validator;

    @Autowired
    public IdeaController(ChallengeService challengeService, IdeaService ideaService, AttachmentService attachmentService, VoteService voteService, PermissionServiceFacade permissionService, DenyAccessInterceptor denyAccessInterceptor, KeycloakUserService keycloakUserService, JWTService jwtService, SmartValidator validator) {
        this.challengeService = challengeService;
        this.ideaService = ideaService;
        this.attachmentService = attachmentService;
        this.voteService = voteService;
        this.permissionService = permissionService;
        this.denyAccessInterceptor = denyAccessInterceptor;
        this.keycloakUserService = keycloakUserService;
        this.jwtService = jwtService;
        this.validator = validator;
    }

    /**
     * Speichert eine neue Idea zu einer Challenge
     *
     * @param challengeId Id der Challenge zu der die Idee eingereicht werden soll
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/challenge/{challengeId}/idea")
    public ResponseEntity<String> saveIdea(@PathVariable String challengeId, @RequestBody @Valid NewIdeaDTO newIdeaDTO, BindingResult bindingResult, Authentication authentication) throws BindException {
        if(bindingResult.hasErrors()){
            throw new BindException(bindingResult);
        }
        Person currentUserPerson = KeycloakUtils.getPersonWithoutClassCastException(authentication);
        Idea ideaToBeSaved = ideaService.createIdea(newIdeaDTO, challengeId, currentUserPerson);
        // Prüfen, ob man überhaupt teilnehmen darf
        denyAccessInterceptor.interceptEditIdea(challengeId, ideaToBeSaved, authentication);
        Challenge challengeDetails = challengeService.getChallengeDetails(challengeId);
        for (Person person : ideaToBeSaved.getTeamMember()) {
            if(! permissionService.checkIsAllowedToViewChallenge(challengeDetails, person.getEmail(), new StringBuilder()) || !keycloakUserService.checkIfUserExists(person.getEmail())){
                bindingResult.rejectValue("teamMember", "", String.format("Die Person %s existiert nicht auf der Plattform oder hat nicht die Rechte, an der Challenge teilzunehmen", person.getEmail()));
            }
        }
        if(bindingResult.hasErrors()){
            throw new BindException(bindingResult);
        }
        return ResponseEntity.ok(ideaService.saveIdea(ideaToBeSaved));
    }

    /**
     * Gibt die Details einer Idee zurück
     *
     * @param challengeId Id der Challenge
     * @param ideaId      Id der Idee
     * @return DTO mit den Details
     */
    @GetMapping("/challenge/{challengeId}/idea/{ideaId}")
    public ResponseEntity<IdeaDetailsDTO> getIdeaDetails(@PathVariable String challengeId, @PathVariable String ideaId, Authentication authentication) {
        denyAccessInterceptor.interceptViewIdea(challengeId, ideaId, authentication);
        String currentUser = KeycloakUtils.getUserOrDefault(authentication);

        Challenge challenge = challengeService.getChallengeDetails(challengeId);
        Idea ideaDetails = ideaService.findById(ideaId);
        if (ideaDetails == null) {
            return ResponseEntity.notFound().build();
        }
        IdeaDetailsDTO result = IdeaDetailsDTO.from(ideaDetails);
        result.setChallengeVisibility(challenge.getVisibility());

        GridFSFindIterable savedAttachments = attachmentService.findIdeaAttachmentsByIdeaId(ideaId);
        for (CanvasElement canvasElement : result.getCanvasElements()) {
            if (!canvasElement.getType().equals(CanvasElementType.TEXT)) {
                canvasElement.setContent("Anhang nicht gefunden...");
            }
        }
        for (GridFSFile gridFSFile : savedAttachments) {
            assert gridFSFile.getMetadata() != null;
            for (CanvasElementDetailDTO canvasElement : result.getCanvasElements()) {
                if (canvasElement.getId().equals(gridFSFile.getMetadata().getString(AttachmentService.META_DATA_FIELD_ATTACHMENT_BUSINESS_ID))) {
                    canvasElement.setContent(baseURL + linkTo(methodOn(IdeaAttachmentController.class).getIdeaAttachment(challengeId, ideaId, gridFSFile.getObjectId().toHexString(), null)).toUri().getPath());
                    canvasElement.setFileName(gridFSFile.getMetadata().getString(AttachmentService.META_DATA_FIELD_FILE_NAME));
                }
            }
        }

        result.setCurrentUserPermissions(ideaService.getPermissions(challenge, ideaDetails, currentUser));

        AverageAndOwnVoteForIdea avgAndOwnVoteForIdea = voteService.getAvgAndOwnVoteForIdea(ideaId, currentUser);
        result.setOwnVoting(avgAndOwnVoteForIdea.getOwnVote());
        if(permissionService.checkIsAllowedToViewVotes(challenge, new StringBuilder())) {
            result.setAverageVoting(avgAndOwnVoteForIdea.getAverageVote());
        }

        if(! permissionService.checkIsAllowedToViewTeamChat(challenge, ideaDetails, currentUser, new StringBuilder())){
            result.setTeamChat(null);
        }
        if(! permissionService.checkIsAllowedToViewRefereeChat(challenge, ideaDetails, currentUser, new StringBuilder())){
            result.setRefereesChat(null);
        }
        if(! permissionService.checkIsAllowedToViewTeamAndRefereeChat(challenge, ideaDetails, currentUser, new StringBuilder())){
            result.setTeamAndRefereesChat(null);
        }
        if(! permissionService.checkIsAllowedToViewAllTeamMember(ideaDetails, currentUser, new StringBuilder())){
            for (TeamMember teamMember : result.getTeamMember()) {
                if(teamMember.isIncognitoNullSafe()){
                    teamMember.setName(TeamMember.INCOGNITO_USER_NAME);
                    teamMember.setEmail("");
                }
            }
        }

        if(result.getCurrentUserPermissions().contains(IdeaPermissions.EDIT) && authentication != null) {
            result.setInvitationLinkTeammember(
                    baseURL + "/invitation/" +
                            jwtService.generateInvitationToken(
                                    InvitationInformation.builder()
                                            .challengeId(challengeId)
                                            .ideaId(ideaId)
                                            .type(InvitationType.TEAM_MEMBER)
                                            .creator(KeycloakUtils.getPersonWithoutClassCastException(authentication).getName())
                                            .additionalInformation(result.getTeamName())
                                            .expiryDate(InvitationInformation.calculateExpiryDate(new Date(), invitationLinkExpiryTime))
                                            .build()
                            )
            );
        }


        return ResponseEntity.ok(result);
    }


    /**
     * Aktualisiert ein bestimmtes Feld der Idee
     *
     * @param challengeId Id der Challenge
     * @param ideaId      Id der Idee
     * @return Id der Challenge (unverändert)
     */
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/challenge/{challengeId}/idea/{ideaId}/update")
    public ResponseEntity<String> updateIdea(@PathVariable String challengeId, @PathVariable String ideaId, @RequestBody @Valid UpdateIdeaFieldDTO updateDTO, BindingResult bindingResult, Authentication authentication) throws BindException {
        UpdateIdeaFieldDTO.Field field = updateDTO.getField();
        ObjectMapper mapper = new ObjectMapper();
        try {
            switch (field) {
                case STATE:
                    IdeaState newState = IdeaState.valueOf((String) updateDTO.getNewValueDTO());
                    denyAccessInterceptor.interceptUpdateIdeaState(challengeId, ideaId, newState, authentication);
                    ideaService.updateState(ideaId, newState);
                    break;
                case GENERAL_INFORMATION:
                    denyAccessInterceptor.interceptEditIdea(challengeId, ideaId, authentication);
                    GeneralInformationDTO generalInformationDTO = mapper.convertValue(updateDTO.getNewValueDTO(), GeneralInformationDTO.class);
                    validator.validate(generalInformationDTO, bindingResult);
                    if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                    }
                    ideaService.updateGeneralInformation(ideaId, generalInformationDTO);
                    break;
                case CANVAS:
                    denyAccessInterceptor.interceptEditIdea(challengeId, ideaId, authentication);
                    UpdateCanvasElementDTO updateCanvasElements = mapper.convertValue(updateDTO.getNewValueDTO(), UpdateCanvasElementDTO.class);
                    validator.validate(updateCanvasElements, bindingResult);
                    if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                    }
                    ideaService.updateCanvasElements(ideaId, updateCanvasElements);
                    break;
                case TEAM_MEMBER:
                    denyAccessInterceptor.interceptEditIdea(challengeId, ideaId, authentication);
                    UpdateIdeaTeamDTO newTeam = mapper.convertValue(updateDTO.getNewValueDTO(), UpdateIdeaTeamDTO.class);
                    validator.validate(newTeam, bindingResult);
                    newTeam.getTeamMember().add(TeamMember.fromPerson(KeycloakUtils.getPersonWithoutClassCastException(authentication)));
                    Challenge challengeDetails = challengeService.getChallengeDetails(challengeId);
                    for (Person person : newTeam.getTeamMember()) {
                        if(! permissionService.checkIsAllowedToViewChallenge(challengeDetails, person.getEmail(), new StringBuilder()) || !keycloakUserService.checkIfUserExists(person.getEmail())){
                            bindingResult.rejectValue("newValueDTO", "", String.format("Die Person %s existiert nicht auf der Plattform oder hat nicht die Rechte, an der Challenge teilzunehmen", person.getEmail()));
                        }
                    }
                    if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                    }
                    ideaService.updateTeamMember(ideaId, newTeam.getTeamMember());
                    break;
                case WINNING_PLACE:
                    denyAccessInterceptor.interceptNominateAsWinner(challengeId, authentication);
                    UpdateWinningPlace.WinningPlace winningPlace = UpdateWinningPlace.WinningPlace.valueOf((String) updateDTO.getNewValueDTO());
                    ideaService.updateWinningPlace(ideaId, winningPlace);
                    break;
            }
        } catch (ClassCastException | IllegalArgumentException e) {
            log.log(Level.WARNING, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(ideaId);
    }

    /**
     * Speichert das Voting für eine Idee
     *
     * @param challengeId Id der Challenge
     * @param ideaId      Id der Idee
     * @return Id der Challenge (unverändert)
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/challenge/{challengeId}/idea/{ideaId}/vote")
    public ResponseEntity<String> saveVoting(@PathVariable String challengeId, @PathVariable String ideaId, @RequestBody @Valid Voting vote, Authentication authentication) {
        denyAccessInterceptor.interceptVote(challengeId, ideaId, authentication);
        voteService.saveVote(challengeId, ideaId, KeycloakUtils.getUser(authentication), vote.getVote());
        return ResponseEntity.ok(ideaId);
    }


}
