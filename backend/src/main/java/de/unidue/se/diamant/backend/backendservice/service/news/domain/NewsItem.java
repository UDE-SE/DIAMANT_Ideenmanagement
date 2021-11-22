package de.unidue.se.diamant.backend.backendservice.service.news.domain;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class NewsItem {

    public enum NewsItemType {
        MILESTONE,
        UPDATE,  //Allgemeines Update
        PLANING, //Die konkrete Umsetzung der Idee wird gerade erarbeitet
        PILOTING, //Die Umsetzung wird in einem Feldversuch getestet
        SUCCESS, // Die Idee wurde erfolgreich umgesetzt
        NOT_IMPLEMENTED //Die Idee konnte nicht umgesetzt werden
    }

    private String id;
    private String challengeId;
    private Date date;
    private NewsItemType type;
    private String content;
}
