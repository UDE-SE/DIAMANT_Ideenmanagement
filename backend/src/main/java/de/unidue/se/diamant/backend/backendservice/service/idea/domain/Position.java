package de.unidue.se.diamant.backend.backendservice.service.idea.domain;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
public class Position {
    /**
     * X-Position
     */
    private int x;
    /**
     * Y-Position
     */
    private int y;
    /**
     * Breite
     */
    private int width;
    /**
     * Höhe
     */
    private int height;
}
