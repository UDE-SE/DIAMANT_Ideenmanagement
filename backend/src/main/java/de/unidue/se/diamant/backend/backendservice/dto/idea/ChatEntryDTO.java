package de.unidue.se.diamant.backend.backendservice.dto.idea;

import de.unidue.se.diamant.backend.backendservice.service.chat.domain.ChatEntry;
import de.unidue.se.diamant.backend.backendservice.service.common.domain.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatEntryDTO {

    /**
     * Datum des Chat-Eintrags
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    private Long date;

    /**
     * Autor
     */
    private Person author;

    /**
     * Nachricht des Chat-Eintrags
     */
    private String text;

    public static ChatEntryDTO fromChatEntry(ChatEntry chatEntry) {
        return ChatEntryDTO.builder().text(chatEntry.getText()).date(chatEntry.getDate().getTime()).author(chatEntry.getAuthor()).build();
    }
}
