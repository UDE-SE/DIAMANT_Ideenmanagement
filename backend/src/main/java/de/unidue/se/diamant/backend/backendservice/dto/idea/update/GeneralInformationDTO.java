package de.unidue.se.diamant.backend.backendservice.dto.idea.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class GeneralInformationDTO {
    /**
     * Name des Teams das die Idee entwickelt
     */
    @NotBlank
    private String teamName;

    /**
     * Kurzbeschreibung der Idee. Wird auf der Übersichtsseite der Challenge angezeigt!
     */
    @NotBlank
    private String shortDescription;
}
