package de.unidue.se.diamant.backend.backendservice.service.idea;

import de.unidue.se.diamant.backend.backendservice.dto.idea.IdeaPermissions;
import de.unidue.se.diamant.backend.backendservice.dto.idea.NewIdeaDTO;
import de.unidue.se.diamant.backend.backendservice.dto.idea.update.GeneralInformationDTO;
import de.unidue.se.diamant.backend.backendservice.dto.idea.update.UpdateCanvasElementDTO;
import de.unidue.se.diamant.backend.backendservice.dto.idea.update.UpdateWinningPlace;
import de.unidue.se.diamant.backend.backendservice.service.AttachmentService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.CanvasElement;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.IdeaState;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.TeamMember;
import de.unidue.se.diamant.backend.backendservice.service.permissions.PermissionServiceFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class IdeaService {

    private IdeaRepository ideaRepository;
    private MongoTemplate mongoTemplate;
    private PermissionServiceFacade permissionService;
    private AttachmentService attachmentService;

    @Autowired
    public IdeaService(IdeaRepository ideaRepository, MongoTemplate mongoTemplate, PermissionServiceFacade permissionService, AttachmentService attachmentService) {
        this.ideaRepository = ideaRepository;
        this.mongoTemplate = mongoTemplate;
        this.permissionService = permissionService;
        this.attachmentService = attachmentService;
    }


    /**
     * Speichert eine Idee und setzt die notwendigen Parameter (challengeId, Creator, Teammitglieder).
     *
     * @param idea Zuspeichernede Idee
     * @return Id der erstellten Idee
     */
    public String saveIdea(Idea idea) {
        return ideaRepository.save(idea).getId();
    }

    public Idea createIdea(NewIdeaDTO newIdeaDTO, String challengeId, Person creator) {
        Idea idea = Idea.fromIdeaDTO(newIdeaDTO);
        idea.setChallengeId(challengeId);
        idea.setCreator(creator);
        idea.setTeamMember(new HashSet<>());
        if (newIdeaDTO.getTeamMember() != null) {
            for (Person teamMember : newIdeaDTO.getTeamMember()) {
                idea.getTeamMember().add(TeamMember.fromPerson(teamMember));
            }
        }
        idea.getTeamMember().add(TeamMember.fromPerson(creator));
        return idea;
    }

    public Idea findById(String ideaId) {
        return ideaRepository.findById(ideaId).orElse(null);
    }

    public List<Idea> findByChallengeId(String challengeId) {
        return ideaRepository.findByChallengeId(challengeId);
    }

    public List<IdeaPermissions> getPermissions(Challenge challenge, Idea idea, String currentUser) {
        List<IdeaPermissions> ideaPermissions = new ArrayList<>();
        if (permissionService.checkIsAllowedToVote(challenge, idea, currentUser, new StringBuilder())) {
            ideaPermissions.add(IdeaPermissions.VOTE);
            return ideaPermissions;
        }
        if (permissionService.checkIsAllowedToEditIdea(challenge, idea, currentUser, new StringBuilder())) {
            ideaPermissions.add(IdeaPermissions.EDIT);
        }
        if (permissionService.checkIsAllowedToNominateIdeaForVoting(challenge, idea, currentUser, new StringBuilder())) {
            ideaPermissions.add(IdeaPermissions.CHANGE_STATE_TO_READY_FOR_VOTE);
        }
        if(permissionService.checkIsAllowedToViewRefereeChat(challenge, idea, currentUser, new StringBuilder())){
            ideaPermissions.add(IdeaPermissions.REFEREE);
        }
        if(permissionService.checkIsAllowedToNominateAsWinner(challenge, currentUser, new StringBuilder())){
            ideaPermissions.add(IdeaPermissions.NOMINATE_AS_WINNER);
        }
        return ideaPermissions;
    }

    /**
     * Aktualisiert den Status einer Idee
     *
     * @param ideaId   Id der Idee
     * @param newState neuer Status
     */
    public void updateState(String ideaId, IdeaState newState) {
        Query query1 = new Query(Criteria.where("id").is(ideaId));
        Update update1 = new Update();
        update1.set("ideaState", newState);
        mongoTemplate.updateFirst(query1, update1, Idea.class);
    }

    public void updateGeneralInformation(String ideaId, GeneralInformationDTO generalInformationDTO) {
        Query query1 = new Query(Criteria.where("id").is(ideaId));
        Update update1 = new Update();
        update1.set("teamName", generalInformationDTO.getTeamName());
        update1.set("shortDescription", generalInformationDTO.getShortDescription());
        mongoTemplate.updateFirst(query1, update1, Idea.class);
    }

    /**
     * Updated die Idee mit den neuen CanvasElementen. Löscht alte Elemente, die nicht mehr Referenziert werden
     */
    public void updateCanvasElements(String ideaId, UpdateCanvasElementDTO updateCanvasElements) {
        attachmentService.deleteAllUnreferencedAttachments(ideaId, updateCanvasElements.getCanvasElements().stream().map(CanvasElement::getId).collect(Collectors.toList()));
        Query query1 = new Query(Criteria.where("id").is(ideaId));
        Update update1 = new Update();
        update1.set("canvasElements", updateCanvasElements.getCanvasElements());
        mongoTemplate.updateFirst(query1, update1, Idea.class);
    }

    /**
     * Updated die Idee mit dem neuen Team
     */
    public void updateTeamMember(String ideaId, List<TeamMember> newTeam) {
        Query query1 = new Query(Criteria.where("id").is(ideaId));
        Update update1 = new Update();
        update1.set("teamMember", newTeam);
        mongoTemplate.updateFirst(query1, update1, Idea.class);
    }

    public void updateWinningPlace(String ideaId, UpdateWinningPlace.WinningPlace newWinningPlace) {
        int updateValue = 0;
        switch (newWinningPlace) {
            case WINNER_1:
                updateValue = 1;
                break;
            case WINNER_2:
                updateValue = 2;
                break;
            case WINNER_3:
                updateValue = 3;
                break;
            case NOT_WON:
                break;
        }
        Query query1 = new Query(Criteria.where("id").is(ideaId));
        Update update1 = new Update();
        update1.set("winningPlace", updateValue);
        mongoTemplate.updateFirst(query1, update1, Idea.class);
    }
}
