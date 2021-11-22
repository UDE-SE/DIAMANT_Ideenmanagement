package de.unidue.se.diamant.backend.backendservice.dto.idea;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Voting {

    @NotNull
    /**
     * Eigenes Abstimmergebnis
     */
    private Integer vote;
}
