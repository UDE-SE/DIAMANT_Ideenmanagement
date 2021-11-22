package de.unidue.se.diamant.backend.backendservice.dto.idea;

import de.unidue.se.diamant.backend.backendservice.dto.idea.validation.UniqueIDsConstraint;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.CanvasElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewIdeaDTO {
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

    /**
     * Liste der Elemente des Canvas. Die IDs der Elemente dürfen nicht doppelt vorkommen!
     */
    @UniqueIDsConstraint
    @NotEmpty
    @Valid
    private List<CanvasElement> canvasElements;

    /**
     * Teammitglieder
     */
    @Valid
    private Set<Person> teamMember = new HashSet<>();
}
