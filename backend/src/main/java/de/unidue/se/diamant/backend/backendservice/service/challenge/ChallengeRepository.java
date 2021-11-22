package de.unidue.se.diamant.backend.backendservice.service.challenge;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.challenge.dto.CallerObject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface ChallengeRepository extends MongoRepository<Challenge, String> {

    @Query(value = "{_id: ?0}", fields = "{_id: 0, caller: 1}")
    Optional<CallerObject> findCallerById(String id);
}
