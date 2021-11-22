package de.unidue.se.diamant.backend.backendservice.service.permissions;

import de.unidue.se.diamant.backend.backendservice.service.challenge.ChallengeRepository;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.IdeaState;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import static de.unidue.se.diamant.backend.backendservice.service.idea.domain.IdeaState.READY_FOR_VOTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VotingPermissionServiceTest {

    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    @Test
    public void checkAllowedToVoteVotingBeforeStartOfTheVotingPhase() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService ideaPermissionsService = Mockito.mock(IdeaPermissionsService.class);

        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        String currentUser = "user@example.com";
        Date now = formatter.parse("05.03.2020 10:10:10");
        Challenge challenge = Challenge.builder().votingStart(formatter.parse("06.05.2020 00:00:00")).implementationStart(formatter.parse("06.05.2021 00:00:00")).build();
        Idea idea = Idea.builder().id(ideaId).ideaState(IdeaState.READY_FOR_VOTE).build();

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(currentUser),  Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(ideaPermissionsService.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class))).thenReturn(true);

        VotingPermissionService sut = new VotingPermissionService(challengePermissionService, ideaPermissionsService);
        assertFalse(sut.checkIsAllowedToVote(challenge, idea, currentUser, now, new StringBuilder()));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(currentUser), Mockito.any(StringBuilder.class));
        Mockito.verify(ideaPermissionsService, Mockito.only()).checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkAllowedToVoteVotingAfterEndOfTheVotingPhase() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService ideaPermissionsService = Mockito.mock(IdeaPermissionsService.class);

        String ideaId = "5dc40f47d270c23cccc43b8a";
        String currentUser = "user@example.com";
        Date now = formatter.parse("07.05.2021 00:00:00");
        Challenge challenge = Challenge.builder().votingStart(formatter.parse("06.05.2020 00:00:00")).implementationStart(formatter.parse("06.05.2021 00:00:00")).build();
        Idea idea = Idea.builder().id(ideaId).ideaState(IdeaState.READY_FOR_VOTE).build();

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(currentUser),  Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(ideaPermissionsService.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class))).thenReturn(true);

        VotingPermissionService sut = new VotingPermissionService(challengePermissionService, ideaPermissionsService);

        assertFalse(sut.checkIsAllowedToVote(challenge, idea, currentUser, now, new StringBuilder()));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(currentUser), Mockito.any(StringBuilder.class));
        Mockito.verify(ideaPermissionsService, Mockito.only()).checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class));

    }

    @Test
    public void checkAllowedToVoteVotingForIdeaNotInReadyForVotingState() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService ideaPermissionsService = Mockito.mock(IdeaPermissionsService.class);

        String currentUser = "user@example.com";
        Date now = formatter.parse("05.05.2021 00:00:00");
        Challenge challenge = Challenge.builder().votingStart(formatter.parse("06.05.2020 00:00:00")).implementationStart(formatter.parse("06.05.2021 00:00:00")).build();
        Idea idea = Idea.builder().ideaState(IdeaState.SUBMITTED).build();

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(currentUser),  Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(ideaPermissionsService.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class))).thenReturn(true);

        VotingPermissionService sut = new VotingPermissionService(challengePermissionService, ideaPermissionsService);

        assertFalse(sut.checkIsAllowedToVote(challenge, idea, currentUser, now, new StringBuilder()));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(currentUser), Mockito.any(StringBuilder.class));
        Mockito.verify(ideaPermissionsService, Mockito.only()).checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkAllowedToVoteVoting() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService ideaPermissionsService = Mockito.mock(IdeaPermissionsService.class);

        String currentUser = "user@example.com";
        Date now = formatter.parse("05.05.2021 00:00:00");
        Challenge challenge = Challenge.builder().votingStart(formatter.parse("06.05.2020 00:00:00")).implementationStart(formatter.parse("06.05.2021 00:00:00")).build();
        Idea idea = Idea.builder().ideaState(READY_FOR_VOTE).build();

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(currentUser),  Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(ideaPermissionsService.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class))).thenReturn(true);

        VotingPermissionService sut = new VotingPermissionService(challengePermissionService, ideaPermissionsService);

        assertTrue(sut.checkIsAllowedToVote(challenge, idea, currentUser, now, new StringBuilder()));

        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(currentUser), Mockito.any(StringBuilder.class));
        Mockito.verify(ideaPermissionsService, Mockito.only()).checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToViewVotes() throws ParseException {
        VotingPermissionService sut = new VotingPermissionService(null, null);
        Date now = formatter.parse("05.05.2021 00:00:00");

        Challenge challenge = Challenge.builder().implementationStart(formatter.parse("06.05.2021 00:00:00")).build();
        assertFalse(sut.checkIsAllowedToViewVotes(challenge, now, new StringBuilder()));

        challenge = Challenge.builder().implementationStart(formatter.parse("04.05.2021 00:00:00")).build();
        assertTrue(sut.checkIsAllowedToViewVotes(challenge, now, new StringBuilder()));
    }
}
