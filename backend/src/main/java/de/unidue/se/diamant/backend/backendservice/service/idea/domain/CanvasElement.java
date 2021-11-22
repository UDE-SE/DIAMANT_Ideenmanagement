package de.unidue.se.diamant.backend.backendservice.service.idea.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class CanvasElement {
    /**
     * ID des Elements
     */
    @NotBlank
    private String id;
    /**
     * Position in dem Canvas
     */
    @NotNull
    private Position position;
    /**
     * Art des Elements
     */
    @NotNull
    private CanvasElementType type;
    /**
     * Inhalt des Elements; Wenn das Element vom Type Text ist, ist der Inhalt hier direkt angegeben. Ansonsten ist das Feld leer!
     */
    @NotBlank
    private String content;
}
