package de.unidue.se.diamant.backend.backendservice.dto.idea.update;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateWinningPlace {
    public enum WinningPlace {
        WINNER_1, WINNER_2, WINNER_3, NOT_WON
    }

    private WinningPlace winningPlace;
}
