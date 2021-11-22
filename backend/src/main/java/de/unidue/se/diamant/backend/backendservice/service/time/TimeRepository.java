package de.unidue.se.diamant.backend.backendservice.service.time;

import de.unidue.se.diamant.backend.backendservice.service.time.domain.DateTime;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TimeRepository extends MongoRepository<DateTime, String> {
}
