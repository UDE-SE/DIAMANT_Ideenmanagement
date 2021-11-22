package de.unidue.se.diamant.backend.backendservice.dto.challenge;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class ChallengeAttachmentDTO {
    private String name;
    private String type;
    private String url;
}
