package de.unidue.se.diamant.backend.backendservice.service.challenge;

import de.unidue.se.diamant.backend.backendservice.dto.challenge.parts.GeneralInformation;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.parts.MileStones;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Visibility;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Log
@RestController
public class ChallengeService {

    private ChallengeRepository challengeRepository;
    private MongoTemplate mongoTemplate;

    @Autowired
    public ChallengeService(ChallengeRepository challengeRepository, MongoTemplate mongoTemplate) {
        this.challengeRepository = challengeRepository;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Gibt alle Challenges zurück
     */
    public List<Challenge> getChallengePreview() {
        return challengeRepository.findAll();
    }

    /**
     * Speichert eine neue Challenge
     */
    public String saveChallenge(Challenge challenge, Person caller) {
        if(challenge.getReferees() == null) {
            challenge.setReferees(new HashSet<>());
        }
        challenge.getReferees().add(caller);
        challenge.setCaller(caller);

        Challenge savedChallenge = challengeRepository.save(challenge);

        return savedChallenge.getId();
    }

    /**
     * Gibt die angefragte Challenge oder Null zurück
     *
     * @param id Id der Challenge
     */
    public Challenge getChallengeDetails(String id) {
        return challengeRepository.findById(id).orElse(null);
    }

    public void updateGeneralInformation(String challengeId, GeneralInformation generalInformation) {
        Query query1 = new Query(Criteria.where("id").is(challengeId));
        Update update1 = new Update();
        update1.set("title", generalInformation.getTitle());
        update1.set("shortDescription", generalInformation.getShortDescription());
        update1.set("visibility", generalInformation.getVisibility());
        if(Visibility.INVITE != generalInformation.getVisibility()){
            update1.set("invitedUsers", Collections.emptySet());
        }
        mongoTemplate.updateFirst(query1, update1, Challenge.class);
    }

    public void updateMileStones(String challengeId, MileStones mileStonesDTO) {
        Query query1 = new Query(Criteria.where("id").is(challengeId));
        Update update1 = new Update();
        update1.set("submissionStart", mileStonesDTO.getSubmissionStart());
        update1.set("reviewStart", mileStonesDTO.getReviewStart());
        update1.set("refactoringStart", mileStonesDTO.getRefactoringStart());
        update1.set("votingStart", mileStonesDTO.getVotingStart());
        update1.set("implementationStart", mileStonesDTO.getImplementationStart());
        update1.set("challengeEnd", mileStonesDTO.getChallengeEnd());
        mongoTemplate.updateFirst(query1, update1, Challenge.class);
    }

    public void updateDescription(String challengeId, String description) {
        Query query1 = new Query(Criteria.where("id").is(challengeId));
        Update update1 = new Update();
        update1.set("description", description);
        mongoTemplate.updateFirst(query1, update1, Challenge.class);
    }

    public void updateAwards(String challengeId, String awards) {
        Query query1 = new Query(Criteria.where("id").is(challengeId));
        Update update1 = new Update();
        update1.set("awards", awards);
        mongoTemplate.updateFirst(query1, update1, Challenge.class);
    }

    public void updateInvitedUsers(String challengeId, Set<Person> invitedUsers){
        Query query1 = new Query(Criteria.where("id").is(challengeId));
        Update update1 = new Update();
        update1.set("invitedUsers", invitedUsers);
        mongoTemplate.updateFirst(query1, update1, Challenge.class);
    }

    public void updateReferees(String challengeId, @NotNull Set<Person> referees, Person caller){
        referees.add(caller);
        Query query1 = new Query(Criteria.where("id").is(challengeId));
        Update update1 = new Update();
        update1.set("referees", referees);
        mongoTemplate.updateFirst(query1, update1, Challenge.class);
    }
}
