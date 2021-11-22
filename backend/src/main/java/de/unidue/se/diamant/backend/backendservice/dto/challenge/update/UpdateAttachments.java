package de.unidue.se.diamant.backend.backendservice.dto.challenge.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAttachments {
    private List<String> remainingAttachments;
}
