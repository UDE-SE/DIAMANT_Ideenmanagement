package de.unidue.se.diamant.backend.backendservice.service;

import de.unidue.se.diamant.backend.backendservice.dto.challenge.NewChallengeDTO;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.parts.GeneralInformation;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.parts.MileStones;
import de.unidue.se.diamant.backend.backendservice.service.challenge.ChallengeRepository;
import de.unidue.se.diamant.backend.backendservice.service.challenge.ChallengeService;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Challenge;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Visibility;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;

public class ChallengeServiceTest {

    private static Date parseDate(String date) throws ParseException {
        return (new SimpleDateFormat("dd.MM.yyyy hh:mm:ss")).parse(date);
    }

    @Test
    public void saveChallengeSetsCallerAndReferees() throws ParseException {
        ChallengeRepository challengeRepository = Mockito.mock(ChallengeRepository.class);
        ChallengeService sut = new ChallengeService(challengeRepository, null);

        NewChallengeDTO challengeDTO = new NewChallengeDTO(
                new GeneralInformation("Titel",
                        "Kurzbeschreibung",
                        Visibility.INTERNAL),
                new MileStones(
                        parseDate("17.08.2019 10:10:00").getTime(),
                        parseDate("18.08.2019 10:10:00").getTime(),
                        null,
                        parseDate("19.08.2019 10:10:00").getTime(),
                        parseDate("20.08.2019 10:10:00").getTime(),
                        parseDate("21.08.2019 10:10:00").getTime()
                ),
                "Der Gewinner bekommt: *Trommelwirbel* nichts",
                "Lange ausführliche Beschreibung",
                new HashSet<>(Arrays.asList(Person.builder().email("Gutachter1@uni-due.de").build(), Person.builder().email("Gutachter2@uni-due.de").build())),
                new HashSet<>()
        );

        ArgumentCaptor<Challenge> saveCaptor = ArgumentCaptor.forClass(Challenge.class);
        String saved_challenge_id = "5c6c25fe3accaf414c792f49";
        Mockito.when(challengeRepository.save(saveCaptor.capture())).thenReturn(Challenge.builder().id(saved_challenge_id).build());

        Person caller = Person.builder().email("callEr@uni-due.de").build();
        Challenge challenge = Challenge.fromNewChallengeDTO(challengeDTO);
        String savedId = sut.saveChallenge(challenge, caller);

        Assertions.assertThat(savedId).isEqualTo(saved_challenge_id);
        Assertions.assertThat(saveCaptor.getValue().getReferees().stream().anyMatch((p) -> p.getEmail().equals(caller.getEmail()))).isTrue();
        Assertions.assertThat(saveCaptor.getValue().getCaller()).isEqualTo(caller);

    }
}
