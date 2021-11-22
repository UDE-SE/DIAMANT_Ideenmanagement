package de.unidue.se.diamant.backend.backendservice.service.news;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.news.domain.NewsItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class NewsService {
    private NewsRepository newsRepository;

    @Autowired
    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }


    public List<NewsItem> getNews(Challenge challenge){
        List<NewsItem> result = new ArrayList<>();
        result.add(NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getChallengeEnd()).content("challengeEnd").build());
        result.add(NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getImplementationStart()).content("implementationStart").build());
        result.add(NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getVotingStart()).content("votingStart").build());
        if(challenge.getRefactoringStart() != null){
            result.add(NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getRefactoringStart()).content("refactoringStart").build());

        }
        result.add(NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getReviewStart()).content("reviewStart").build());
        result.add(NewsItem.builder().type(NewsItem.NewsItemType.MILESTONE).date(challenge.getSubmissionStart()).content("submissionStart").build());
        result.addAll(newsRepository.findByChallengeId(challenge.getId()));
        result.sort(Comparator.comparing(NewsItem::getDate));
        return result;
    }

    public void save(NewsItem newItem){
        newItem.setId(null);
        newsRepository.save(newItem);
    }

}
