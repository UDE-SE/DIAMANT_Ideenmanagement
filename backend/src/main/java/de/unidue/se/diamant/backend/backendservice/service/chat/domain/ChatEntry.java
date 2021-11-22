package de.unidue.se.diamant.backend.backendservice.service.chat.domain;

import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class ChatEntry {
    private Person author;
    private Date date;
    private String text;
}
