package de.unidue.se.diamant.backend.backendservice.dto.challenge.parts;

import de.unidue.se.diamant.backend.backendservice.service.challenge.domain.Visibility;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeneralInformation {
    /**
     * Titel der Challenge. Maximal 50 Zeichen.
     */
    @NotBlank
    @Length(max = 50)
    private String title;
    /**
     * Kurzbeschreibung. Maximal 100 Zeichen
     */
    @NotBlank
    @Length(max = 500)
    private String shortDescription;

    /**
     * Sichtbarkeit der Challenge
     */
    @NotNull
    private Visibility visibility;
}
