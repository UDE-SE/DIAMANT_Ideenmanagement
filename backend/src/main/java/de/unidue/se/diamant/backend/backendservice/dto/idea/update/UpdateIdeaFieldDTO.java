package de.unidue.se.diamant.backend.backendservice.dto.idea.update;

import de.unidue.se.diamant.backend.backendservice.service.idea.domain.IdeaState;
import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class UpdateIdeaFieldDTO {

    @AllArgsConstructor
    public enum Field {
        STATE,
        GENERAL_INFORMATION,
        CANVAS,
        TEAM_MEMBER,
        WINNING_PLACE
    }

    /**
     * Feld, das geupdated wird
     */
    @NotNull
    private Field field;

    /**
     * Neuer Wert
     */
    @NotNull
    private Object newValueDTO;
}
