package de.unidue.se.diamant.backend.backendservice.dto.challenge.update;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
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
        GENERAL_INFORMATION,
        MILESTONES,
        DESCRIPTION,
        ATTACHMENTS,
        AWARDS,
        REFEREES,
        INVITED_USERS,
        NEW_NEWS_ENTRY
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
