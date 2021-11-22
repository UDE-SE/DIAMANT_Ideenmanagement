package de.unidue.se.diamant.backend.backendservice.dto.challenge;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.parts.GeneralInformation;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.parts.MileStones;
import de.unidue.se.diamant.backend.backendservice.dto.challenge.validation.DateOrderConstraint;
import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Visibility;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class NewChallengeDTO {

    @Valid
    @JsonUnwrapped
    @NotNull
    private GeneralInformation generalInformation;

    @Valid
    @JsonUnwrapped
    @NotNull
    private MileStones mileStones;

    /**
     * Preise für die Sieger der Challenge
     */
    @NotBlank
    private String awards;

    /**
     * Ausführliche Beschreibung
     */
    @NotBlank
    private String description;

    /**
     * Liste der Gutachter. Identifiziert über die E-Mail-Adresse.
     */
    @Valid
    private Set<Person> referees;

    /**
     * Zusätzliche, explizit eingeladene Teilnehmer.
     * Identifiziert über die E-Mail-Adresse.
     */
    @Valid
    private Set<Person> invitedUsers;
}
