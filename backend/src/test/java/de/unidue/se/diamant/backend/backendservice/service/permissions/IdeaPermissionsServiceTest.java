package de.unidue.se.diamant.backend.backendservice.service.permissions;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.*;
import de.unidue.se.diamant.backend.backendservice.service.time.TimeService;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IdeaPermissionsServiceTest {

    @Test
    public void checkUpdateIdeaStatePermissionAllowsToSubmitted() {
        TimeService timeService = Mockito.mock(TimeService.class);
        Mockito.when(timeService.getTime()).thenReturn(new Date());
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(null, timeService));

        Challenge challenge = Challenge.builder().id("5dc40f47d270c23c1bc43b8a").build();
        String ideaId = "5dc40f47d270c23cccc43b8a";
        Person creator = Person.builder().email("owner").build();
        Idea idea = Idea.builder().id(ideaId).challengeId(challenge.getId()).ideaState(IdeaState.DRAFT).creator(creator).build();

        Mockito.when(sut.checkIsAllowedToSubmitIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.any(Challenge.class), Mockito.any(Idea.class),  Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsAllowedToUpdateIdeaState(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.any(IdeaState.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenCallRealMethod();

        Assertions.assertThat(sut.checkIsAllowedToUpdateIdeaState(challenge, idea, IdeaState.SUBMITTED, creator.getEmail(), new StringBuilder())).isTrue();

        Mockito.verify(sut, Mockito.times(1)).checkIsAllowedToSubmitIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(creator.getEmail()), Mockito.any(Date.class), Mockito.any(StringBuilder.class));
        Mockito.verify(sut, Mockito.times(1)).checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkUpdateIdeaStatePermissionAllowsToReadyForVote() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        TimeService timeService = Mockito.mock(TimeService.class);
        Mockito.when(timeService.getTime()).thenReturn(new Date());
        IdeaPermissionsService sut = new IdeaPermissionsService(null, timeService);

        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        Person currentUser = Person.builder().email("owner").build();
        Idea idea = Idea.builder().id(ideaId).challengeId(challengeId).ideaState(IdeaState.SUBMITTED).build();
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .reviewStart(simpleDateFormat.parse("05.02.2019"))
                .votingStart(simpleDateFormat.parse("05.02.2050"))
                .referees(new HashSet<>(Collections.singletonList(currentUser))).build();

        assertTrue(sut.checkIsAllowedToUpdateIdeaState(challenge, idea, IdeaState.READY_FOR_VOTE, currentUser.getEmail(), new StringBuilder()));
    }

    @Test
    public void checkUpdateIdeaStatePermissionFailsForSomeOtherCases() {
        IdeaPermissionsService sut = new IdeaPermissionsService(null, null);

        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String ideaId = "5dc40f47d270c23cccc43b8a";
        Person currentUser = Person.builder().email("owner").build();
        Idea idea = Idea.builder().id(ideaId).challengeId(challengeId).ideaState(IdeaState.DRAFT).build();
        Challenge challenge = Challenge.builder().caller(currentUser).build();

        assertFalse(sut.checkIsAllowedToUpdateIdeaState(challenge, idea, IdeaState.READY_FOR_VOTE, currentUser.getEmail(), new StringBuilder()));
    }

    @Test
    public void checkAvailableCorrectChallenge() {
        IdeaPermissionsService sut = new IdeaPermissionsService(null, null);

        String challengeId = "5dc40f47d270c23c1bc43b8a";

        Challenge challenge = Challenge.builder().id(challengeId).build();
        Idea idea = Idea.builder().challengeId(challengeId).build();

        assertTrue(sut.checkIfIdeaBelongsToChallenge(challenge, idea, new StringBuilder()));
    }

    @Test
    public void checkAvailableCorrectChallengeWrongChallengeId() {
        IdeaPermissionsService sut = new IdeaPermissionsService(null, null);

        String challengeId = "5dc40f47d270c23c1bc43b8a";
        Challenge challenge = Challenge.builder().id(challengeId).build();
        Idea ideaBelongsToOtherChallenge = Idea.builder().challengeId("andereChallengeId").build();

        assertFalse(sut.checkIfIdeaBelongsToChallenge(challenge, ideaBelongsToOtherChallenge, new StringBuilder()));
    }

    @Test
    public void checkIfUserIsAllowedToNominateIdeaForVotingDuringSubmissionPhase() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String currentUser = "caller@example.com";

        IdeaPermissionsService sut = new IdeaPermissionsService(null, null);
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .submissionStart(simpleDateFormat.parse("01.02.2020"))
                .reviewStart(simpleDateFormat.parse("01.03.2020"))
                .refactoringStart(simpleDateFormat.parse("15.03.2020"))
                .votingStart(simpleDateFormat.parse("01.04.2020"))
                .referees(Collections.singleton(Person.builder().email(currentUser).build()))
                .build();
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.SUBMITTED).build();
        Date now = simpleDateFormat.parse("22.02.2020");
        StringBuilder errorBuffer = new StringBuilder();


        Assertions.assertThat(sut.checkIsAllowedToNominateIdeaForVoting(challenge, idea, currentUser, now, errorBuffer)).isTrue();
    }

    @Test
    public void checkIfUserIsAllowedToNominateIdeaForVotingAfterVotingStart() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";

        IdeaPermissionsService sut = new IdeaPermissionsService(null, null);

        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .submissionStart(simpleDateFormat.parse("01.02.2020"))
                .reviewStart(simpleDateFormat.parse("01.03.2020"))
                .refactoringStart(simpleDateFormat.parse("15.03.2020"))
                .votingStart(simpleDateFormat.parse("01.04.2020"))
                .build();
        Idea idea = Idea.builder().challengeId(challengeId).build();
        Date now = simpleDateFormat.parse("02.04.2020");
        StringBuilder errorBuffer = new StringBuilder();
        String currentUser = "caller@example.com";

        Assertions.assertThat(sut.checkIsAllowedToNominateIdeaForVoting(challenge, idea, currentUser, now, errorBuffer)).isFalse();
    }

    @Test
    public void checkIfUserIsAllowedToNominateIdeaForVotingNotReferee() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";

        IdeaPermissionsService sut = new IdeaPermissionsService(null, null);

        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .submissionStart(simpleDateFormat.parse("01.02.2020"))
                .reviewStart(simpleDateFormat.parse("01.03.2020"))
                .refactoringStart(simpleDateFormat.parse("15.03.2020"))
                .votingStart(simpleDateFormat.parse("01.04.2020"))
                .build();
        challenge.getReferees().add(Person.builder().email("someReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).build();
        Date now = simpleDateFormat.parse("14.03.2020");
        StringBuilder errorBuffer = new StringBuilder();
        String currentUser = "caller@example.com";

        Assertions.assertThat(sut.checkIsAllowedToNominateIdeaForVoting(challenge, idea, currentUser, now, errorBuffer)).isFalse();
    }

    @Test
    public void checkIfUserIsAllowedToNominateIdeaForVotingNotSubmitted() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";

        IdeaPermissionsService sut = new IdeaPermissionsService(null, null);

        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .submissionStart(simpleDateFormat.parse("01.02.2020"))
                .reviewStart(simpleDateFormat.parse("01.03.2020"))
                .refactoringStart(simpleDateFormat.parse("15.03.2020"))
                .votingStart(simpleDateFormat.parse("01.04.2020"))
                .build();
        challenge.getReferees().add(Person.builder().email("someReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.DRAFT).build();
        Date now = simpleDateFormat.parse("14.03.2020");
        StringBuilder errorBuffer = new StringBuilder();
        String currentUser = "caller@example.com";

        Assertions.assertThat(sut.checkIsAllowedToNominateIdeaForVoting(challenge, idea, currentUser, now, errorBuffer)).isFalse();

        idea = Idea.builder().ideaState(IdeaState.READY_FOR_VOTE).challengeId(challengeId).build();
        Assertions.assertThat(sut.checkIsAllowedToNominateIdeaForVoting(challenge, idea, currentUser, now, errorBuffer)).isFalse();
    }

    @Test
    public void checkIfUserIsAllowedToNominateIdeaForVoting() throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";

        IdeaPermissionsService sut = new IdeaPermissionsService(null, null);

        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .submissionStart(simpleDateFormat.parse("01.02.2020"))
                .reviewStart(simpleDateFormat.parse("01.03.2020"))
                .refactoringStart(simpleDateFormat.parse("15.03.2020"))
                .votingStart(simpleDateFormat.parse("01.04.2020"))
                .build();
        challenge.getReferees().add(Person.builder().email("someReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.SUBMITTED).build();
        Date now = simpleDateFormat.parse("14.03.2020");
        StringBuilder errorBuffer = new StringBuilder();
        Person currentUser = Person.builder().email("caller@example.com").build();
        challenge.getReferees().add(currentUser);

        Assertions.assertThat(sut.checkIsAllowedToNominateIdeaForVoting(challenge, idea, currentUser.getEmail().toUpperCase(), now, errorBuffer)).isTrue();
    }

    @Test
    public void checkIfUserIsAllowedToSubmitIdea(){
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class);

        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        challenge.getReferees().add(Person.builder().email("someReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.DRAFT).build();

        Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(user), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsAllowedToSubmitIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(user), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();

        assertTrue(sut.checkIsAllowedToSubmitIdea(challenge, idea, user, new Date(), new StringBuilder()));
    }

    @Test
    public void checkIfUserIsAllowedToSubmitIdeaFailsIfWrongState(){
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class);

        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        challenge.getReferees().add(Person.builder().email("someReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();

        Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(user), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsAllowedToSubmitIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(user), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();

        assertFalse(sut.checkIsAllowedToSubmitIdea(challenge, idea, user, new Date(), new StringBuilder()));

        idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.SUBMITTED).build();
        assertFalse(sut.checkIsAllowedToSubmitIdea(challenge, idea, user, new Date(), new StringBuilder()));
    }

    @Test
    public void checkIfUserIsAllowedToSubmitIdeaFailsIfNotAllowedToEdit(){
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class);

        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        challenge.getReferees().add(Person.builder().email("someReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.DRAFT).build();

        Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(user), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(sut.checkIsAllowedToSubmitIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(user), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();

        assertFalse(sut.checkIsAllowedToSubmitIdea(challenge, idea, user, new Date(), new StringBuilder()));
    }

    @Test
    public void checkIsAllowedToViewIdeaDoesNotBelongToChallenge(){
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));


        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        challenge.getReferees().add(Person.builder().email("someReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.DRAFT).build();

        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(sut.checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);

        Mockito.when(sut.checkIsAllowedToViewIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();

        assertFalse(sut.checkIsAllowedToViewIdea(challenge, idea, user, new Date(), new StringBuilder()));
        Mockito.verify(sut, Mockito.never()).checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.never()).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));

    }

    @Test
    public void checkIsAllowedToViewIdeaFailsIfNotAllowedToViewChallenge(){
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));


        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        challenge.getReferees().add(Person.builder().email("someReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.DRAFT).build();

        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(false);

        Mockito.when(sut.checkIsAllowedToViewIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();

        assertFalse(sut.checkIsAllowedToViewIdea(challenge, idea, user, new Date(), new StringBuilder()));
        Mockito.verify(sut, Mockito.never()).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToViewIdeaAlwaysAllowTeamMember(){
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));


        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        challenge.getReferees().add(Person.builder().email("someReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.DRAFT).build();

        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);

        Mockito.when(sut.checkIsAllowedToViewIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();

        assertTrue(sut.checkIsAllowedToViewIdea(challenge, idea, user, new Date(), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToViewIdeaReferee(){
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));

        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String currentUserMail = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        challenge.getReferees().add(Person.builder().email(currentUserMail).build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.DRAFT).build();

        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);

        Mockito.when(challengePermissionService.checkIsReferee(Mockito.any(Challenge.class),  Mockito.anyString(), Mockito.any(StringBuilder.class))).thenCallRealMethod();
        Mockito.when(sut.checkIsAllowedToViewIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();

        assertFalse(sut.checkIsAllowedToViewIdea(challenge, idea, currentUserMail, new Date(), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(currentUserMail), Mockito.any(StringBuilder.class));

        Idea idea2 = Idea.builder().challengeId(challengeId).ideaState(IdeaState.SUBMITTED).build();
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea2), Mockito.any(StringBuilder.class))).thenReturn(true);
        assertTrue(sut.checkIsAllowedToViewIdea(challenge, idea2, currentUserMail, new Date(), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea2), Mockito.eq(currentUserMail), Mockito.any(StringBuilder.class));

        Idea idea3 = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea3), Mockito.any(StringBuilder.class))).thenReturn(true);
        assertTrue(sut.checkIsAllowedToViewIdea(challenge, idea3, currentUserMail, new Date(), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea3), Mockito.eq(currentUserMail), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToViewIdeaNotRefereeAndNotTeamMemberAndNotReadyForVote(){
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));


        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        challenge.getReferees().add(Person.builder().email("someOtherReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.SUBMITTED).build();

        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);

        Mockito.when(sut.checkIsAllowedToViewIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();

        assertFalse(sut.checkIsAllowedToViewIdea(challenge, idea, user, new Date(), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));

    }

    @Test
    public void checkIsAllowedToViewIdeaNotRefereeAndNotTeamMemberVotingPhaseStarted() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .votingStart(simpleDateFormat.parse("09.03.2020"))
                .build();
        challenge.getReferees().add(Person.builder().email("someOtherReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(false);

        Mockito.when(sut.checkIsAllowedToViewIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();

        assertTrue(sut.checkIsAllowedToViewIdea(challenge, idea, user, simpleDateFormat.parse("11.03.2020"), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToViewIdeaNotRefereeAndNotTeamMemberVotingPhaseNotStarted() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");

        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .votingStart(simpleDateFormat.parse("09.03.2021"))
                .build();
        challenge.getReferees().add(Person.builder().email("someOtherReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(false);

        Mockito.when(sut.checkIsAllowedToViewIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();

        assertFalse(sut.checkIsAllowedToViewIdea(challenge, idea, user, simpleDateFormat.parse("11.03.2020"), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));
    }


    @Test
    public void checkIsTeamMember(){
        IdeaPermissionsService sut = new IdeaPermissionsService(null, null);

        String userMail = "user@exmaple.com";
        Idea idea = Idea.builder().build();
        idea.getTeamMember().add(TeamMember.fromPerson(Person.builder().email(userMail).build()));

        assertTrue(sut.checkIsTeamMember(idea, userMail.toUpperCase(), new StringBuilder()));
        assertTrue(sut.checkIsTeamMember(idea, userMail, new StringBuilder()));
        assertFalse(sut.checkIsTeamMember(idea, "someOther@example.com", new StringBuilder()));
    }


    @Test
    public void checkIsAllowedToEditIdeaIdeaDoesNotBelongToChallenge() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(sut.checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .votingStart(simpleDateFormat.parse("09.03.2021"))
                .build();
        challenge.getReferees().add(Person.builder().email("someOtherReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();

        assertFalse(sut.checkIsAllowedToEditIdea(challenge, idea, user, new Date(), new StringBuilder()));
        Mockito.verify(sut, Mockito.never()).checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.never()).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToEditIdeaNotTeamMember() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .votingStart(simpleDateFormat.parse("09.03.2021"))
                .build();
        challenge.getReferees().add(Person.builder().email("someOtherReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();

        assertFalse(sut.checkIsAllowedToEditIdea(challenge, idea, user, new Date(), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToEditNotAllowedToViewChallenge() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .votingStart(simpleDateFormat.parse("09.03.2021"))
                .build();
        challenge.getReferees().add(Person.builder().email("someOtherReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();

        assertFalse(sut.checkIsAllowedToEditIdea(challenge, idea, user, new Date(), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class));
        Mockito.verify(sut, Mockito.never()).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToEditIdeaNotInInitialSubmitPhaseOrRefactoringPhase() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .submissionStart(simpleDateFormat.parse("09.03.2020"))
                .reviewStart(simpleDateFormat.parse("09.04.2020"))
                .refactoringStart(simpleDateFormat.parse("09.04.2021"))
                .votingStart(simpleDateFormat.parse("09.05.2021"))
                .build();
        challenge.getReferees().add(Person.builder().email("someOtherReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();

        assertFalse(sut.checkIsAllowedToEditIdea(challenge, idea, user, simpleDateFormat.parse("09.05.2019"), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToEditIdeaInInitialSubmit() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .submissionStart(simpleDateFormat.parse("09.03.2020"))
                .reviewStart(simpleDateFormat.parse("09.04.2020"))
                .refactoringStart(simpleDateFormat.parse("09.04.2021"))
                .votingStart(simpleDateFormat.parse("09.05.2021"))
                .build();
        challenge.getReferees().add(Person.builder().email("someOtherReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();

        assertTrue(sut.checkIsAllowedToEditIdea(challenge, idea, user, simpleDateFormat.parse("10.03.2020"), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToEditIdeaInInitialSubmitButRefactoringIsNull() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .submissionStart(simpleDateFormat.parse("09.03.2020"))
                .reviewStart(simpleDateFormat.parse("09.04.2020"))
                .refactoringStart(null)
                .votingStart(simpleDateFormat.parse("09.05.2021"))
                .build();
        challenge.getReferees().add(Person.builder().email("someOtherReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();

        assertTrue(sut.checkIsAllowedToEditIdea(challenge, idea, user, simpleDateFormat.parse("10.03.2020"), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToEditIdeaInRefactoringPhase() throws ParseException {
        ChallengePermissionService challengePermissionService = Mockito.mock(ChallengePermissionService.class);
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(challengePermissionService, null));

        Mockito.when(challengePermissionService.checkIsAllowedToViewChallenge(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIfIdeaBelongsToChallenge(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsTeamMember(Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenCallRealMethod();


        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy");
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .submissionStart(simpleDateFormat.parse("09.03.2020"))
                .reviewStart(simpleDateFormat.parse("09.04.2020"))
                .refactoringStart(simpleDateFormat.parse("09.04.2021"))
                .votingStart(simpleDateFormat.parse("09.05.2021"))
                .build();
        challenge.getReferees().add(Person.builder().email("someOtherReferee@exmaple.com").build());
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();

        assertTrue(sut.checkIsAllowedToEditIdea(challenge, idea, user, simpleDateFormat.parse("10.04.2021"), new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIfIdeaBelongsToChallenge(Mockito.eq(challenge), Mockito.eq(idea), Mockito.any(StringBuilder.class));
        Mockito.verify(sut, Mockito.times(1)).checkIsTeamMember(Mockito.eq(idea), Mockito.eq(user), Mockito.any(StringBuilder.class));
        Mockito.verify(challengePermissionService, Mockito.times(1)).checkIsAllowedToViewChallenge(Mockito.eq(challenge), Mockito.eq(user), Mockito.any(StringBuilder.class));
    }


    @Test
    public void checkIsAllowedToUploadAttachmentToIdeaNotAllowedToEdit() {
        TimeService timeService = Mockito.mock(TimeService.class);
        Mockito.when(timeService.getTime()).thenReturn(new Date());
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(null, timeService));
        Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenReturn(false);
        Mockito.when(sut.checkIsAllowedToUploadAttachmentToIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenCallRealMethod();


        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();
        String uploadElementId = "aaa40f47d270c23c1bc43b8a";

        assertFalse(sut.checkIsAllowedToUploadAttachmentToIdea(challenge, idea, uploadElementId, user, new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIsAllowedToEditIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(user), Mockito.any(Date.class), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToUploadAttachmentToIdeaIdToUploadIsTextElement() {
        TimeService timeService = Mockito.mock(TimeService.class);
        Mockito.when(timeService.getTime()).thenReturn(new Date());
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(null, timeService));Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsAllowedToUploadAttachmentToIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenCallRealMethod();


        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();
        String uploadElementId = "aaa40f47d270c23c1bc43b8a";
        idea.getCanvasElements().add(CanvasElement.builder().type(CanvasElementType.TEXT).id(uploadElementId).build());
        idea.getCanvasElements().add(CanvasElement.builder().type(CanvasElementType.ATTACHMENT).id("someOtherId").build());

        assertFalse(sut.checkIsAllowedToUploadAttachmentToIdea(challenge, idea, uploadElementId, user, new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIsAllowedToEditIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(user), Mockito.any(Date.class), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToUploadAttachmentToIdeaIdToUploadIsNotPartOfIdea() {
        TimeService timeService = Mockito.mock(TimeService.class);
        Mockito.when(timeService.getTime()).thenReturn(new Date());
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(null, timeService));Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsAllowedToUploadAttachmentToIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenCallRealMethod();


        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();
        String uploadElementId = "aaa40f47d270c23c1bc43b8a";
        idea.getCanvasElements().add(CanvasElement.builder().type(CanvasElementType.TEXT).id("notTheIdYouAreLookingFor").build());
        idea.getCanvasElements().add(CanvasElement.builder().type(CanvasElementType.ATTACHMENT).id("someOtherId").build());

        assertFalse(sut.checkIsAllowedToUploadAttachmentToIdea(challenge, idea, uploadElementId, user, new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIsAllowedToEditIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(user), Mockito.any(Date.class), Mockito.any(StringBuilder.class));
    }

    @Test
    public void checkIsAllowedToUploadAttachmentToIdea() {
        TimeService timeService = Mockito.mock(TimeService.class);
        Mockito.when(timeService.getTime()).thenReturn(new Date());
        IdeaPermissionsService sut = Mockito.mock(IdeaPermissionsService.class, Mockito.withSettings().useConstructor(null, timeService));Mockito.when(sut.checkIsAllowedToEditIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.any(Date.class), Mockito.any(StringBuilder.class))).thenReturn(true);
        Mockito.when(sut.checkIsAllowedToUploadAttachmentToIdea(Mockito.any(Challenge.class), Mockito.any(Idea.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenCallRealMethod();


        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String user = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        Idea idea = Idea.builder().challengeId(challengeId).ideaState(IdeaState.READY_FOR_VOTE).build();
        String uploadElementId = "aaa40f47d270c23c1bc43b8a";
        idea.getCanvasElements().add(CanvasElement.builder().type(CanvasElementType.TEXT).id("notTheIdYouAreLookingFor").build());
        idea.getCanvasElements().add(CanvasElement.builder().type(CanvasElementType.ATTACHMENT).id(uploadElementId).build());

        assertTrue(sut.checkIsAllowedToUploadAttachmentToIdea(challenge, idea, uploadElementId, user, new StringBuilder()));
        Mockito.verify(sut, Mockito.times(1)).checkIsAllowedToEditIdea(Mockito.eq(challenge), Mockito.eq(idea), Mockito.eq(user), Mockito.any(Date.class), Mockito.any(StringBuilder.class));
    }
}
