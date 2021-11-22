package de.unidue.se.diamant.backend.backendservice.service.news;

import de.unidue.se.diamant.backend.backendservice.service.news.domain.NewsItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NewsRepository extends MongoRepository<NewsItem, String> {
    List<NewsItem> findByChallengeId(String challengeId);
}
