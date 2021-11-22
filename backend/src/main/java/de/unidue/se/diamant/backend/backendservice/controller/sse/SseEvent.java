package de.unidue.se.diamant.backend.backendservice.controller.sse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class SseEvent {

    public enum SseEventType {
        TEAM_CHAT, REFEREES_CHAT, TEAM_AND_REFEREES_CHAT
    }

    private final long id;
    private final String channelId;
    private final SseEventType field;
}
