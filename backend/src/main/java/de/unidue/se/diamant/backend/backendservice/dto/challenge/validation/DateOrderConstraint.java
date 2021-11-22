package de.unidue.se.diamant.backend.backendservice.dto.challenge.validation;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = DateOrderValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface DateOrderConstraint {

    @AllArgsConstructor
    public enum DateField {
        SUBMISSION_START("submissionStart"),
        REVIEW_START("reviewStart"),
        REFACTORING_START("refactoringStart"),
        VOTING_START("votingStart"),
        IMPLEMENTATION_START("implementationStart"),
        CHALLENGE_END("challengeEnd");

        @Getter
        private final String fieldName;
    }

    String message() default "Reihenfolge der Daten ist nicht korrekt!";

    DateField field();

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    @Target({ ElementType.TYPE })
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        DateOrderConstraint[] value();
    }
}
