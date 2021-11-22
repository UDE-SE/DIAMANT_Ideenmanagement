package de.unidue.se.diamant.backend.backendservice.service.permissions;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Visibility;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Idea;
import de.unidue.se.diamant.backend.backendservice.service.time.TimeService;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ChallengePermissionsServiceTest {


    @Test
    public void checkIfDeadLinesAllowViewing() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        ChallengePermissionService sut = new ChallengePermissionService(null);

        Date submissionStart = formatter.parse("04.03.2020 00:01");
        Date challengeEnd = formatter.parse("04.06.2020 00:01");

        Challenge challenge = Challenge.builder().submissionStart(submissionStart).challengeEnd(challengeEnd).build();

        StringBuilder buffer = new StringBuilder();

        boolean deadlinesFulfilled = sut.checkIfDeadLinesAllowViewing(challenge, formatter.parse("04.05.2020 00:01"), buffer);
        Assertions.assertThat(deadlinesFulfilled).isTrue();
        Assertions.assertThat(buffer.length()).isEqualTo(0);

        deadlinesFulfilled = sut.checkIfDeadLinesAllowViewing(challenge, formatter.parse("03.03.2020 23:59"), buffer);
        Assertions.assertThat(deadlinesFulfilled).isFalse();
        Assertions.assertThat(buffer.length()).isNotZero();

        buffer = new StringBuilder();
        assert(buffer.length() == 0);
        deadlinesFulfilled = sut.checkIfDeadLinesAllowViewing(challenge, formatter.parse("04.06.2020 00:02"), buffer);
        Assertions.assertThat(deadlinesFulfilled).isFalse();
        Assertions.assertThat(buffer.length()).isNotZero();

    }

    @Test
    public void checkIfVisibilitiesAllowViewing() {
        ChallengePermissionService sut = new ChallengePermissionService(null);

        Challenge challenge = Challenge.builder().visibility(Visibility.OPEN).build();
        StringBuilder buffer = new StringBuilder();
        Assertions.assertThat(sut.checkIfVisibilitiesAllowViewing(challenge, "blödsinn", buffer)).isTrue();
        Assertions.assertThat(buffer.length()).isZero();

        Person caller = Person.builder().email("caller@dummyCompany1.example.de").name("caller callerson").build();
        challenge = Challenge.builder().visibility(Visibility.INTERNAL).caller(caller).build();
        buffer = new StringBuilder();
        Assertions.assertThat(sut.checkIfVisibilitiesAllowViewing(challenge, "user@dummyCompany1.example.de", buffer)).isTrue();
        Assertions.assertThat(buffer.length()).isZero();

        challenge = Challenge.builder().visibility(Visibility.INTERNAL).caller(caller).build();
        buffer = new StringBuilder();
        Assertions.assertThat(sut.checkIfVisibilitiesAllowViewing(challenge, "user@dummyCompany2.example.de", buffer)).isFalse();

        challenge = Challenge.builder().visibility(Visibility.INTERNAL).caller(caller).build();
        buffer = new StringBuilder();
        Assertions.assertThat(sut.checkIfVisibilitiesAllowViewing(challenge, "unbekannt", buffer)).isFalse();
        Assertions.assertThat(buffer.length()).isNotZero();

        challenge = Challenge.builder().visibility(Visibility.INTERNAL).caller(caller).build();
        challenge.getReferees().add(Person.builder().email("testMail@mail.de").build());
        buffer = new StringBuilder();
        Assertions.assertThat(sut.checkIfVisibilitiesAllowViewing(challenge, null, buffer)).isFalse();
        Assertions.assertThat(buffer.length()).isNotZero();

        challenge = Challenge.builder().visibility(Visibility.INVITE).invitedUsers(new HashSet<>()).build();
        String currentUser = "user@dummyCompany1.example.de";
        challenge.getInvitedUsers().add(Person.builder().email(currentUser).name("currUser").build());
        buffer = new StringBuilder();
        Assertions.assertThat(sut.checkIfVisibilitiesAllowViewing(challenge, currentUser, buffer)).isTrue();
        Assertions.assertThat(buffer.length()).isZero();

        challenge = Challenge.builder().visibility(Visibility.INVITE).invitedUsers(new HashSet<>()).build();
        Person invitedUser = Person.builder().email("user@dummyCompany3.example.de").build();
        challenge.getInvitedUsers().add(invitedUser);
        buffer = new StringBuilder();
        Assertions.assertThat(sut.checkIfVisibilitiesAllowViewing(challenge, "user@dummyCompany2.example.de", buffer)).isFalse();
        Assertions.assertThat(buffer.length()).isNotZero();
    }

    @Test
    public void checkIsReferee() {
        ChallengePermissionService sut = Mockito.mock(ChallengePermissionService.class);

        Mockito.when(sut.checkIsReferee(Mockito.any(Challenge.class), Mockito.anyString(), Mockito.any(StringBuilder.class))).thenCallRealMethod();
        String challengeId = "5dc40f47d270c23c1bc43b8a";
        String referee = "user@example.com";
        Challenge challenge = Challenge.builder()
                .id(challengeId)
                .build();
        challenge.getReferees().add(Person.builder().name(referee + " " + referee).email(referee).build());

        assertFalse(sut.checkIsReferee(challenge,  "someWrong@mail.de", new StringBuilder()));
        assertTrue(sut.checkIsReferee(challenge,  referee, new StringBuilder()));
    }

    @Test
    public void checkDeadlinesAllowWinnerNomination() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        ChallengePermissionService sut = new ChallengePermissionService(null);

        Date implementationStart = formatter.parse("04.03.2020 00:01");
        Date challengeEnd = formatter.parse("04.06.2020 00:01");

        Challenge challenge = Challenge.builder().implementationStart(implementationStart).challengeEnd(challengeEnd).build();

        StringBuilder buffer = new StringBuilder();

        boolean deadlinesFulfilled = sut.checkDeadlinesAllowWinnerNomination(challenge, formatter.parse("04.05.2020 00:01"), buffer);
        Assertions.assertThat(deadlinesFulfilled).isTrue();
        Assertions.assertThat(buffer.length()).isEqualTo(0);

        deadlinesFulfilled = sut.checkDeadlinesAllowWinnerNomination(challenge, formatter.parse("03.03.2020 23:59"), buffer);
        Assertions.assertThat(deadlinesFulfilled).isFalse();
        Assertions.assertThat(buffer.length()).isNotZero();

        buffer = new StringBuilder();
        assert(buffer.length() == 0);
        deadlinesFulfilled = sut.checkDeadlinesAllowWinnerNomination(challenge, formatter.parse("04.06.2020 00:02"), buffer);
        Assertions.assertThat(deadlinesFulfilled).isFalse();
        Assertions.assertThat(buffer.length()).isNotZero();

    }

    @Test
    public void checkIsAllowedToNominateAsWinner() throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        TimeService timeService = Mockito.mock(TimeService.class);
        ChallengePermissionService sut = new ChallengePermissionService(timeService);

        Date implementationStart = formatter.parse("04.03.2020 00:01");
        Date challengeEnd = formatter.parse("04.06.2020 00:01");
        Person caller = Person.builder().email("caller@example.com").build();

        Challenge challenge = Challenge.builder().implementationStart(implementationStart).challengeEnd(challengeEnd).caller(caller).build();
        Mockito.when(timeService.getTime()).thenReturn(formatter.parse("04.05.2020 00:01"), formatter.parse("03.03.2020 23:59"), formatter.parse("04.06.2020 00:02"), formatter.parse("04.05.2020 00:01"));

        StringBuilder buffer = new StringBuilder();

        boolean deadlinesFulfilled = sut.checkIsAllowedToNominateAsWinner(challenge, caller.getEmail(), buffer);
        Assertions.assertThat(deadlinesFulfilled).isTrue();
        Assertions.assertThat(buffer.length()).isEqualTo(0);

        deadlinesFulfilled = sut.checkIsAllowedToNominateAsWinner(challenge, caller.getEmail(), buffer);
        Assertions.assertThat(deadlinesFulfilled).isFalse();
        Assertions.assertThat(buffer.length()).isNotZero();

        buffer = new StringBuilder();
        assert(buffer.length() == 0);
        deadlinesFulfilled = sut.checkIsAllowedToNominateAsWinner(challenge, caller.getEmail(), buffer);
        Assertions.assertThat(deadlinesFulfilled).isFalse();
        Assertions.assertThat(buffer.length()).isNotZero();

        buffer = new StringBuilder();
        assert(buffer.length() == 0);
        deadlinesFulfilled = sut.checkIsAllowedToNominateAsWinner(challenge, "someOtherUser@example.com", buffer);
        Assertions.assertThat(deadlinesFulfilled).isFalse();
        Assertions.assertThat(buffer.length()).isNotZero();
    }
}
