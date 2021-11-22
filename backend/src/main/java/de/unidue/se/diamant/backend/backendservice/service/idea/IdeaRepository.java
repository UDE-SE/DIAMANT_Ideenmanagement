package de.unidue.se.diamant.backend.backendservice.service.idea;

import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface IdeaRepository extends MongoRepository<Idea, String> {
    List<Idea> findByChallengeId(String challengeId);
}
