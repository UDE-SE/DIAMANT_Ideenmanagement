package de.unidue.se.diamant.backend.backendservice.service.vote;

import de.unidue.se.diamant.backend.backendservice.service.vote.domain.Vote;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends MongoRepository<Vote, String> {
    Optional<Vote> findByIdeaIdAndUserId(String ideaId, String userId);
    List<Vote> findByUserId(String userId);
}
