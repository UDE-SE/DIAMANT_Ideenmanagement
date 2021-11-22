package de.unidue.se.diamant.backend.backendservice.service.time.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document
public class DateTime {
    private String id;
    private Date currentTime;
}
