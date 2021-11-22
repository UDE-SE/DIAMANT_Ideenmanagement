package de.unidue.se.diamant.backend.backendservice.dto.challenge;

import de.unidue.se.diamant.backend.backendservice.service.news.domain.NewsItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsItemDTO {
    @NotNull
    private Long date;
    @NotNull
    private NewsItem.NewsItemType type;
    @NotBlank
    private String content;

    public static NewsItemDTO from(NewsItem newsItem){
        return new NewsItemDTO(newsItem.getDate().getTime(), newsItem.getType(), newsItem.getContent());
    }
}
