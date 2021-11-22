package de.unidue.se.diamant.backend.backendservice.service.vote;

import de.unidue.se.diamant.backend.backendservice.service.vote.dto.AverageAndOwnVoteForIdea;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@AutoConfigureDataMongo
@AutoConfigureMockMvc
@SpringBootTest
public class VoteServiceTest {
    @Autowired
    private VoteService sut;

    private String challengeId1 = "1dc40f47d270c23c1bc43b81";
    private String challengeId2 = "1dc40f47d270c23c1bc43b82";
    private String ideaId1Chal1 = "adc40f47d270c23cccc43b81";
    private String ideaId2Chal1 = "adc40f57d270c23cccc43b82";
    private String ideaId1Chal2 = "bdc40f57d270c23cccc43b81";
    private String userId1 = "user1@example.com";
    private String userId2 = "user2@example.com";

    @Before
    public void init(){
        sut.saveVote(challengeId1, ideaId1Chal1, userId1, 5);
        sut.saveVote(challengeId1, ideaId2Chal1, userId1, 5);
        sut.saveVote(challengeId1, ideaId1Chal1, userId2, 1);
        sut.saveVote(challengeId2, ideaId1Chal2, userId1, 4);
    }

    @Test
    public void getAvgAndOwnVoteForIdea() {
        AverageAndOwnVoteForIdea expected = AverageAndOwnVoteForIdea.builder().averageVote(3D).ownVote(5).ideaId(ideaId1Chal1).build();
        Assertions.assertThat(sut.getAvgAndOwnVoteForIdea(ideaId1Chal1, userId1)).isEqualTo(expected);
    }

    @Test
    public void getAvgAndOwnVoteForChallenge() {
        AverageAndOwnVoteForIdea firstExpected = AverageAndOwnVoteForIdea.builder().averageVote(3D).ownVote(5).ideaId(ideaId1Chal1).build();
        AverageAndOwnVoteForIdea secondExpected = AverageAndOwnVoteForIdea.builder().averageVote(5D).ownVote(5).ideaId(ideaId2Chal1).build();
        Assertions.assertThat(sut.getAvgAndOwnVoteForChallenge(challengeId1, userId1)).containsExactlyInAnyOrder(firstExpected, secondExpected);
    }

    @Test
    public void getAvgAndOwnVoteForIdeaIds() {
        AverageAndOwnVoteForIdea r1 = AverageAndOwnVoteForIdea.builder().averageVote(3D).ownVote(1).ideaId(ideaId1Chal1).build();
        AverageAndOwnVoteForIdea r2 = AverageAndOwnVoteForIdea.builder().averageVote(5D).ownVote(0).ideaId(ideaId2Chal1).build();
        Assertions.assertThat(sut.getAvgAndOwnVoteForIdeaIds(Arrays.asList(ideaId1Chal1, ideaId2Chal1), userId2)).containsExactlyInAnyOrder(r1, r2);
    }
}
