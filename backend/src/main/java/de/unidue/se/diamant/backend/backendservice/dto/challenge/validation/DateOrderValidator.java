package de.unidue.se.diamant.backend.backendservice.dto.challenge.validation;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static de.unidue.se.diamant.backend.backendservice.dto.challenge.validation.DateOrderConstraint.DateField;

public class DateOrderValidator implements ConstraintValidator<DateOrderConstraint, Object> {

    private DateField field;

    public void initialize(DateOrderConstraint constraintAnnotation) {
        this.field = constraintAnnotation.field();
    }

    public boolean isValid(Object value, ConstraintValidatorContext context) {
        Object fieldValue = new BeanWrapperImpl(value).getPropertyValue(field.getFieldName());
        if(field == DateField.REFACTORING_START && fieldValue == null){
            return true;
        }
        // Kein Check für den Start der Challenge
        if(field.ordinal() == 0){
            return true;
        }

        if(fieldValue == null) {
            return false;
        }

        if(! (fieldValue instanceof Long)){
            throw new IllegalArgumentException(String.format("Der Wert des Feldes '%s' muss vom Type Long sein!", field.name()));
        }

        DateField previousDeadline = DateField.values()[field.ordinal() -1];
        Object previousDeadlineValue = new BeanWrapperImpl(value).getPropertyValue(previousDeadline.getFieldName());

        if(DateField.REFACTORING_START == previousDeadline && previousDeadlineValue == null){
            previousDeadline = DateField.values()[previousDeadline.ordinal() -1];
            previousDeadlineValue = new BeanWrapperImpl(value).getPropertyValue(previousDeadline.getFieldName());
        }

        if(previousDeadlineValue == null) {
            return false;
        }

        if(! (previousDeadlineValue instanceof Long)){
            throw new IllegalArgumentException(String.format("Der Wert des Feldes '%s' muss vom Type Long sein!", previousDeadline.name()));
        }

        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(
                String.format("Das Datum %s muss später als das Datum von %s sein", field.name(), previousDeadline.name())
        ).addPropertyNode(field.getFieldName()).addConstraintViolation();

        return (long) fieldValue > (long) previousDeadlineValue;
    }
}
