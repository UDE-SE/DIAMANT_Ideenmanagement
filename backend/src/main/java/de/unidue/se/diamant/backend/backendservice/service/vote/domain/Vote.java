package de.unidue.se.diamant.backend.backendservice.service.vote.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class Vote {
    @Id
    private String id;
    private String challengeId;
    private String ideaId;
    private String userId;
    private Integer vote;
}
