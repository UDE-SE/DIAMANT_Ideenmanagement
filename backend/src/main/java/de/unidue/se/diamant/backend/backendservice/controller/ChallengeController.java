package de.unidue.se.diamant.backend.backendservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.model.GridFSFile;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.*;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.parts.GeneralInformation;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.parts.MileStones;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.update.UpdateAttachments;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.update.UpdateIdeaFieldDTO;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.update.UpdatePersons;
import de.unidue.se.diamant.backend.backendservice.service.AttachmentService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.ChallengeService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Visibility;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.IdeaService;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.invitation.InvitationInformation;
import de.unidue.se.diamant.backend.backendservice.service.invitation.InvitationType;
import de.unidue.se.diamant.backend.backendservice.service.invitation.JWTService;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUserService;
import de.unidue.se.diamant.backend.backendservice.service.keycloak.KeycloakUtils;
import de.unidue.se.diamant.backend.backendservice.service.news.NewsService;
import de.unidue.se.diamant.backend.backendservice.service.news.domain.NewsItem;
import de.unidue.se.diamant.backend.backendservice.service.permissions.DenyAccessInterceptor;
import de.unidue.se.diamant.backend.backendservice.service.permissions.PermissionServiceFacade;
import de.unidue.se.diamant.backend.backendservice.service.vote.VoteService;
import de.unidue.se.diamant.backend.backendservice.service.vote.dto.AverageAndOwnVoteForIdea;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.validation.*;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Log
@RestController
@RequestMapping(value = "/api")
public class ChallengeController {

    @Value("${app.server.base.url}")
    private String baseURL;

    @Value("${app.invitation.expiry.time.in.hours}")
    private int invitationLinkExpiryTime;

    private ChallengeService challengeService;
    private IdeaService ideaService;
    private AttachmentService attachmentService;
    private VoteService voteService;
    private NewsService newsService;
    private PermissionServiceFacade permissionService;
    private DenyAccessInterceptor denyAccessInterceptor;
    private KeycloakUserService keycloakUserService;
    private JWTService jwtService;

    private SmartValidator validator;


    @Autowired
    public ChallengeController(ChallengeService challengeService, IdeaService ideaService, AttachmentService attachmentService, VoteService voteService, NewsService newsService, PermissionServiceFacade permissionService, DenyAccessInterceptor denyAccessInterceptor, KeycloakUserService keycloakUserService, JWTService jwtService, SmartValidator validator) {
        this.challengeService = challengeService;
        this.ideaService = ideaService;
        this.attachmentService = attachmentService;
        this.voteService = voteService;
        this.newsService = newsService;
        this.permissionService = permissionService;
        this.denyAccessInterceptor = denyAccessInterceptor;
        this.keycloakUserService = keycloakUserService;
        this.jwtService = jwtService;
        this.validator = validator;
    }

    /**
     * Gibt alle Challenges zurück
     */
    @GetMapping("/challenge/preview")
    public ResponseEntity<List<ChallengePreviewDTO>> getChallengePreview(Principal principal){
        String currentUserMail = principal != null ? principal.getName() : "unbekannt";

        return ResponseEntity.ok(
                challengeService.getChallengePreview().stream()
                        .filter(challenge -> permissionService.checkIsAllowedToViewChallenge(challenge, currentUserMail, new StringBuilder()))
                        .map(ChallengePreviewDTO::fromChallenge)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Speichert eine neue Challenge
     */
    @PreAuthorize("hasRole('caller')")
    @PostMapping("/challenge")
    public ResponseEntity<String> saveChallenge(@RequestBody @Valid NewChallengeDTO newChallengeDTO, BindingResult bindingResult, Authentication authentication) throws BindException {
        Person currentUserPerson = KeycloakUtils.getPersonWithoutClassCastException(authentication);
        Challenge challenge = Challenge.fromNewChallengeDTO(newChallengeDTO);
        if(challenge.getReferees() != null){
            for (Person referee : challenge.getReferees()) {
                if(!keycloakUserService.checkIfUserExists(referee.getEmail())){
                    bindingResult.rejectValue("referees", "", String.format("Die Person %s existiert nicht auf der Plattform", referee.getEmail()));
                }
            }
        }
        if (Visibility.INVITE == challenge.getVisibility()) {
            if(challenge.getInvitedUsers() != null){
                for (Person invitedUser : challenge.getInvitedUsers()) {
                    if(!keycloakUserService.checkIfUserExists(invitedUser.getEmail())){
                        bindingResult.rejectValue("invitedUsers", "", String.format("Die Person %s existiert nicht auf der Plattform", invitedUser.getEmail()));
                    }
                }
            }
        } else {
            challenge.setInvitedUsers(null);
        }

        if(bindingResult.hasErrors()){
            throw new BindException(bindingResult);
        }
        return ResponseEntity.ok(challengeService.saveChallenge(challenge, currentUserPerson));
    }

    /**
     * Gibt die Details einer Challenge zurück
     * @param challengeId Id der Challenge
     * @return DTO mit den Details
     */
    @GetMapping("/challenge/{challengeId}")
    public ResponseEntity<ChallengeDetailsDTO> getChallengeDetails(@PathVariable String challengeId, Authentication authentication){
        denyAccessInterceptor.interceptViewChallenge(challengeId, authentication);

        Challenge challengeDetails = challengeService.getChallengeDetails(challengeId);
        if(challengeDetails == null) {
            return ResponseEntity.notFound().build();
        }
        ChallengeDetailsDTO result = ChallengeDetailsDTO.from(challengeDetails);
        result.getNews().addAll(newsService.getNews(challengeDetails).stream().map(NewsItemDTO::from).collect(Collectors.toList()));

        for (GridFSFile attachmentFile : attachmentService.getChallengeAttachmentsByChallengeId(challengeId)) {
            result.getAttachments().add(ChallengeAttachmentDTO.builder()
                    .name(attachmentFile.getMetadata() != null ? attachmentFile.getMetadata().getString(AttachmentService.META_DATA_FIELD_FILE_NAME) : "Unbekannt")
                    .type(attachmentFile.getMetadata() != null ? attachmentFile.getMetadata().getString(AttachmentService.META_DATA_FIELD_CONTENT_TYPE) : "Unbekannt")
                    .url(baseURL + linkTo(methodOn(ChallengeAttachmentController.class).getChallengeAttachment(challengeId, attachmentFile.getObjectId().toHexString(), null)).toUri().getPath())
                    .build());
        }

        String currentUserMail = KeycloakUtils.getUserOrDefault(authentication);

        List<Idea> ideasOfTheChallenge = ideaService.findByChallengeId(challengeId)
                .stream().filter(idea -> permissionService.checkIsAllowedToViewIdea(challengeDetails, idea, currentUserMail, new StringBuilder()))
                .collect(Collectors.toList());
        for (Idea idea : ideasOfTheChallenge) {
            IdeaPreview currentIdea = IdeaPreview.fromIdea(idea);
            currentIdea.setCurrentUserPermissions(ideaService.getPermissions(challengeDetails, idea, currentUserMail));
            result.getIdeas().add(currentIdea);
        }

        boolean viewingVotingsInChallengeAllowed = permissionService.checkIsAllowedToViewVotes(challengeDetails, new StringBuilder());
        List<AverageAndOwnVoteForIdea> votings = voteService.getAvgAndOwnVoteForIdeaIds(ideasOfTheChallenge.stream().map(Idea::getId).collect(Collectors.toList()), currentUserMail);
        for (IdeaPreview idea : result.getIdeas()) {
            for (AverageAndOwnVoteForIdea voting : votings) {
                if(idea.getId().equals(voting.getIdeaId())){
                    idea.setOwnVoting(voting.getOwnVote());
                    if(viewingVotingsInChallengeAllowed) {
                        idea.setAverageVoting(voting.getAverageVote());
                    }
                }
            }
        }

        result.setEditPermission(permissionService.checkHasEditPermission(challengeDetails, currentUserMail, new StringBuilder()));

        try {
            if(authentication != null) {
                denyAccessInterceptor.interceptEditChallenge(challengeId, authentication);
                if (Visibility.INVITE.equals(result.getVisibility())) {
                    result.setInvitationLinkInvitedUsers(
                            baseURL + "/invitation/" +
                                    jwtService.generateInvitationToken(
                                            InvitationInformation.builder()
                                                    .challengeId(challengeId)
                                                    .type(InvitationType.INVITED_USER)
                                                    .creator(KeycloakUtils.getPersonWithoutClassCastException(authentication).getName())
                                                    .additionalInformation(result.getTitle())
                                                    .expiryDate(InvitationInformation.calculateExpiryDate(new Date(), invitationLinkExpiryTime))
                                                    .build()
                                    )
                    );
                }
                result.setInvitationLinkReferee(
                        baseURL + "/invitation/" +
                                jwtService.generateInvitationToken(
                                        InvitationInformation.builder()
                                                .challengeId(challengeId)
                                                .type(InvitationType.REFEREE)
                                                .creator(KeycloakUtils.getPersonWithoutClassCastException(authentication).getName())
                                                .additionalInformation(result.getTitle())
                                                .expiryDate(InvitationInformation.calculateExpiryDate(new Date(), invitationLinkExpiryTime))
                                                .build()
                                )
                );
            }
        } catch (AccessDeniedException ae) {
            log.info("Einladungslinks nicht zur Challenge hinzugefügt, weil der aktuelle nicht die Rechte hierzu besitzt");
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Aktualisiert ein bestimmtes Feld der Challenge
     *
     * @param challengeId Id der Challenge
     * @return Id der Challenge (unverändert)
     */
    @PreAuthorize("hasRole('caller')")
    @PutMapping("/challenge/{challengeId}/update")
    public ResponseEntity<String> updateChallenge(@PathVariable String challengeId, @RequestBody @Valid UpdateIdeaFieldDTO updateDTO, BindingResult bindingResult, Authentication authentication) throws BindException {
        UpdateIdeaFieldDTO.Field field = updateDTO.getField();
        ObjectMapper mapper = new ObjectMapper();
        denyAccessInterceptor.interceptEditChallenge(challengeId, authentication);
        try {
            switch (field) {
                case GENERAL_INFORMATION:
                    GeneralInformation generalInformationDTO = mapper.convertValue(updateDTO.getNewValueDTO(), GeneralInformation.class);
                    validator.validate(generalInformationDTO, bindingResult);
                    if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                    }
                    challengeService.updateGeneralInformation(challengeId, generalInformationDTO);
                    break;
                case MILESTONES:
                    MileStones mileStonesDTO = mapper.convertValue(updateDTO.getNewValueDTO(), MileStones.class);
                    Errors tempErrors = new BeanPropertyBindingResult(mileStonesDTO, "newValueDTO");
                    validator.validate(mileStonesDTO, tempErrors);
                    for (ObjectError error : tempErrors.getAllErrors()) {
                        bindingResult.addError(error);
                    }
                    if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                    }
                    challengeService.updateMileStones(challengeId, mileStonesDTO);
                    break;
                case DESCRIPTION:
                    if(StringUtils.isEmpty(updateDTO.getNewValueDTO())){
                        bindingResult.rejectValue("newValueDTO", "NotBlank", "Die Beschreibung darf nicht leer sein!");
                        throw new BindException(bindingResult);
                    }
                    challengeService.updateDescription(challengeId, (String) updateDTO.getNewValueDTO());
                    break;
                case AWARDS:
                    if(StringUtils.isEmpty(updateDTO.getNewValueDTO())){
                        bindingResult.rejectValue("newValueDTO", "NotBlank", "Die Preise dürfen nicht leer sein!");
                        throw new BindException(bindingResult);
                    }
                    challengeService.updateAwards(challengeId, (String) updateDTO.getNewValueDTO());
                    break;
                case REFEREES:
                    UpdatePersons referees = mapper.convertValue(updateDTO.getNewValueDTO(), UpdatePersons.class);
                    validator.validate(referees, bindingResult);
                    for (Person person : referees.getPersons()) {
                        if(!keycloakUserService.checkIfUserExists(person.getEmail())){
                            bindingResult.rejectValue("newValueDTO", "", String.format("Die Person %s existiert nicht auf der Plattform!", person.getEmail()));
                        }
                    }
                    if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                    }
                    challengeService.updateReferees(challengeId, referees.getPersons(), challengeService.getChallengeDetails(challengeId).getCaller());
                    break;
                case INVITED_USERS:
                    UpdatePersons invitedUsers = mapper.convertValue(updateDTO.getNewValueDTO(), UpdatePersons.class);
                    validator.validate(invitedUsers, bindingResult);
                    for (Person person : invitedUsers.getPersons()) {
                        if(!keycloakUserService.checkIfUserExists(person.getEmail())){
                            bindingResult.rejectValue("newValueDTO", "", String.format("Die Person %s existiert nicht auf der Plattform!", person.getEmail()));
                        }
                    }
                    if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                    }
                    challengeService.updateInvitedUsers(challengeId, invitedUsers.getPersons());
                    break;
                case ATTACHMENTS:
                    if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                    }
                    UpdateAttachments dto = mapper.convertValue(updateDTO.getNewValueDTO(), UpdateAttachments.class);
                    attachmentService.deleteAllUnreferencedChallengeAttachments(challengeId, dto.getRemainingAttachments());
                    break;
                case NEW_NEWS_ENTRY:
                    NewsItemDTO newNewsDTO = mapper.convertValue(updateDTO.getNewValueDTO(), NewsItemDTO.class);
                    validator.validate(newNewsDTO, bindingResult);
                    if(NewsItem.NewsItemType.MILESTONE.equals(newNewsDTO.getType())){
                        bindingResult.rejectValue("newValueDTO", "", "Es können keine Newseinträge vom Type MILESTONE gepostet werden!");
                    }
                    if (bindingResult.hasErrors()) {
                        throw new BindException(bindingResult);
                    }
                    NewsItem itemToBeSaved = NewsItem.builder()
                            .challengeId(challengeId).content(newNewsDTO.getContent()).type(newNewsDTO.getType()).date(new Date(newNewsDTO.getDate()))
                            .build();
                    newsService.save(itemToBeSaved);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + field);
            }
        } catch (ClassCastException | IllegalArgumentException e) {
            log.log(Level.WARNING, e.getMessage());
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(challengeId);
    }
}
