package de.unidue.se.diamant.backend.backendservice.service.vote;

import de.unidue.se.diamant.backend.backendservice.service.vote.dto.AverageAndOwnVoteForIdea;
import de.unidue.se.diamant.backend.backendservice.service.vote.domain.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class VoteService {

    private VoteRepository voteRepository;
    private MongoTemplate mongoTemplate;

    @Autowired
    public VoteService(VoteRepository voteRepository, MongoTemplate mongoTemplate) {
        this.voteRepository = voteRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public void saveVote(String challengeId, String ideaId, String userMail, Integer vote){
        Vote voteToBeUpdatedOrSaved = voteRepository.findByIdeaIdAndUserId(ideaId, userMail).orElse(new Vote(null, challengeId, ideaId, userMail, vote));
        voteToBeUpdatedOrSaved.setVote(Math.max(1, Math.min(vote, 5)));
        voteRepository.save(voteToBeUpdatedOrSaved);
    }

    public Integer getVoteForIdeaAndUser(String ideaId, String userId) {
        return voteRepository.findByIdeaIdAndUserId(ideaId, userId).orElse(Vote.builder().vote(0).build()).getVote();
    }

    public AverageAndOwnVoteForIdea getAvgAndOwnVoteForIdea(String ideaId, String userId){
        MatchOperation byIdea = Aggregation.match(new Criteria("ideaId").is(ideaId));
        GroupOperation groupByIdeaIdAndAvgVote = Aggregation.group("ideaId").avg("vote").as("averageVote");
        ProjectionOperation addIdeaId = Aggregation.project().and("ideaId").previousOperation().and("averageVote").as("averageVote");
        Aggregation aggregation = Aggregation.newAggregation(byIdea, groupByIdeaIdAndAvgVote, addIdeaId);

        AggregationResults<AverageAndOwnVoteForIdea> output = mongoTemplate.aggregate(aggregation, "vote", AverageAndOwnVoteForIdea.class);
        AverageAndOwnVoteForIdea result = output.getUniqueMappedResult();
        if(result == null){
            result = new AverageAndOwnVoteForIdea(ideaId, 0, 0D);
        }
        result.setOwnVote(getVoteForIdeaAndUser(ideaId, userId));
        return result;
    }

    /**
     * Gibt für alle Ideen der Challenge die Durchschnittliche und die eigene Bewertung zurück
     */
    public List<AverageAndOwnVoteForIdea> getAvgAndOwnVoteForChallenge(String challengeId, String userId){
        MatchOperation byChallenge = Aggregation.match(new Criteria("challengeId").is(challengeId));
        GroupOperation groupByIdeaIdAndAvgVote = Aggregation.group("ideaId").avg("vote").as("averageVote");
        ProjectionOperation addIdeaId = Aggregation.project().and("ideaId").previousOperation().and("averageVote").as("averageVote");
        Aggregation aggregation = Aggregation.newAggregation(byChallenge, groupByIdeaIdAndAvgVote, addIdeaId);

        AggregationResults<AverageAndOwnVoteForIdea> aggregationResults = mongoTemplate.aggregate(aggregation, "vote", AverageAndOwnVoteForIdea.class);
        List<Vote> ownVotes = voteRepository.findByUserId(userId);
        for (AverageAndOwnVoteForIdea aggregationResult : aggregationResults.getMappedResults()) {
            for (Vote ownVote : ownVotes) {
                if(ownVote.getIdeaId().equals(aggregationResult.getIdeaId())){
                    aggregationResult.setOwnVote(ownVote.getVote());
                }
            }
        }
        return aggregationResults.getMappedResults();
    }

    /**
     * Gibt für alle Ideen angeführten Ids die Durchschnittliche und die eigene Bewertung zurück
     */
    public List<AverageAndOwnVoteForIdea> getAvgAndOwnVoteForIdeaIds(List<String> ideaIds, String userId){
        MatchOperation findByIds = Aggregation.match(new Criteria("ideaId").in(ideaIds));
        GroupOperation groupByIdeaIdAndAvgVote = Aggregation.group("ideaId").avg("vote").as("averageVote");
        ProjectionOperation addIdeaId = Aggregation.project().and("ideaId").previousOperation().and("averageVote").as("averageVote");
        Aggregation aggregation = Aggregation.newAggregation(findByIds, groupByIdeaIdAndAvgVote, addIdeaId);
        List<AverageAndOwnVoteForIdea> averageVotingResult = mongoTemplate.aggregate(aggregation, "vote", AverageAndOwnVoteForIdea.class).getMappedResults();

        GroupOperation ownVote = Aggregation.group("ideaId", "userId").sum("vote").as("ownVote");
        MatchOperation onlyOwnUser = Aggregation.match(new Criteria("userId").is(userId));
        Aggregation findOwnVoteForIds = Aggregation.newAggregation(findByIds, onlyOwnUser, ownVote);
        List<AverageAndOwnVoteForIdea> ownVotingResult = mongoTemplate.aggregate(findOwnVoteForIds, "vote", AverageAndOwnVoteForIdea.class).getMappedResults();

        for (AverageAndOwnVoteForIdea averageAndOwnVoteForIdea : averageVotingResult) {
            for (AverageAndOwnVoteForIdea andOwnVoteForIdea : ownVotingResult) {
                if(Objects.equals(averageAndOwnVoteForIdea.getIdeaId(), andOwnVoteForIdea.getIdeaId())){
                    averageAndOwnVoteForIdea.setOwnVote(andOwnVoteForIdea.getOwnVote());
                    break;
                }
            }
        }

        return averageVotingResult;
    }




}
