package de.unidue.se.diamant.backend.backendservice.dto.idea;

import de.unidue.se.diamant.backend.backendservice.service.idea.domain.CanvasElement;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.CanvasElementType;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.Position;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CanvasElementDetailDTO extends CanvasElement {
    /**
     * Datei-Name
     */
    @NotBlank
    private String fileName = "Siehe content-Property!";

    public CanvasElementDetailDTO(CanvasElement canvasElement) {
        super(canvasElement.getId(), canvasElement.getPosition(), canvasElement.getType(), canvasElement.getContent());
    }
}
