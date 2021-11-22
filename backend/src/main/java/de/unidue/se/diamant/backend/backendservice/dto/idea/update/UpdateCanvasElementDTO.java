package de.unidue.se.diamant.backend.backendservice.dto.idea.update;

import de.unidue.se.diamant.backend.backendservice.dto.idea.validation.UniqueIDsConstraint;
import de.unidue.se.diamant.backend.backendservice.service.idea.domain.CanvasElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCanvasElementDTO {

    /**
     * Liste der Elemente des Canvas. Die IDs der Elemente dürfen nicht doppelt vorkommen!
     */
    @UniqueIDsConstraint
    @NotEmpty
    @Valid
    private List<CanvasElement> canvasElements;

}
