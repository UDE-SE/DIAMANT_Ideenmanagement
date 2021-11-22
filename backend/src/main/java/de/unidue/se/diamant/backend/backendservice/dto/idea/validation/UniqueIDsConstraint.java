package de.unidue.se.diamant.backend.backendservice.dto.idea.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueIDsValidator.class)
@Target( { ElementType.METHOD, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueIDsConstraint {
    String message() default "Es gibt doppelte IDs!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
