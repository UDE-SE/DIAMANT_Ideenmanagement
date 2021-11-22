package de.unidue.se.diamant.backend.backendservice.dto.challenge.parts;

import de.unidue.se.diamant.backend.backendservice.dto.challenge.validation.DateOrderConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@DateOrderConstraint.List({
        @DateOrderConstraint(field = DateOrderConstraint.DateField.SUBMISSION_START),
        @DateOrderConstraint(field = DateOrderConstraint.DateField.REVIEW_START),
        @DateOrderConstraint(field = DateOrderConstraint.DateField.REFACTORING_START),
        @DateOrderConstraint(field = DateOrderConstraint.DateField.VOTING_START),
        @DateOrderConstraint(field = DateOrderConstraint.DateField.IMPLEMENTATION_START),
        @DateOrderConstraint(field = DateOrderConstraint.DateField.CHALLENGE_END)
})
public class MileStones {
    /**
     * Start der Einreichungsfrist
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    @NotNull
    private Long submissionStart;

    /**
     * Start der Begutachtungszeit (geleichzeitig Ende der Einreichungszeit)
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    @NotNull
    private Long reviewStart;

    /**
     * Start der Überarbeitungszeit (geleichzeitig Ende der Begutachtungszeit). Optional
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    private Long refactoringStart;

    /**
     * Start der Voting-Phase (geleichzeitig Ende (optionalen) der Überarbeitungszeit / bzw. Einreichungszeit).
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    @NotNull
    private Long votingStart;

    /**
     * Start der Umsetzungsphase / Siegerverkündung (geleichzeitig Ende Votingzeit).
     * Angabe in Millisekunden seit Start der Epoche:
     * <code>1970-01-01T00:00:00Z + timeStamp</code>
     */
    @NotNull
    private Long implementationStart;

    /**
     * Ende der Umsetzungsphase und der Challenge.
     */
    @NotNull
    private Long challengeEnd;
}
