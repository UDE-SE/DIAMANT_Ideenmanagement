package de.unidue.se.diamant.backend.backendservice.dto.idea.validation;

import de.unidue.se.diamant.backend.backendservice.service.idea.domain.CanvasElement;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UniqueIDsValidator implements ConstraintValidator<UniqueIDsConstraint, List<CanvasElement>> {


    @Override
    public void initialize(UniqueIDsConstraint constraintAnnotation) {
    }

    @Override
    public boolean isValid(List<CanvasElement> list, ConstraintValidatorContext context) {
        Set<String> ids = new HashSet<>();
        for (CanvasElement elementWithId : list) {
            if(! ids.contains(elementWithId.getId())){
                ids.add(elementWithId.getId());
            } else {
                return false;
            }
        }
        return true;
    }
}
