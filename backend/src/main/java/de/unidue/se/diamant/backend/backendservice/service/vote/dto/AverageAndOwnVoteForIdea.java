package de.unidue.se.diamant.backend.backendservice.service.vote.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AverageAndOwnVoteForIdea {
    private String ideaId;

    @Builder.Default
    private Integer ownVote = 0;

    @Builder.Default
    private Double averageVote = 0d;
}
