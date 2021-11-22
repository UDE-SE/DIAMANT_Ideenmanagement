package de.unidue.se.diamant.backend.backendservice.dto.debug;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DebugDateTimeDTO {
    @NotBlank
    private String password;
    @NotBlank
    private String dateTime;
}
