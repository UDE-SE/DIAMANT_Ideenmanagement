package de.unidue.se.diamant.backend.backendservice.dto.idea.validation;

import de.unidue.se.diamant.backend.backendservice.service.idea.domain.CanvasElement;
import org.assertj.core.api.Assertions;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;

public class UniqueIDsValidatorTest {

    private UniqueIDsValidator sut = new UniqueIDsValidator();

    @Test
    public void onlyElementsWithDifferentIDsShouldReturnTrue() {
        List<CanvasElement> input = Arrays.asList(
                CanvasElement.builder().id("Erste ID").build(),
                CanvasElement.builder().id("Erste zweite ID").build()
        );
        assertThat(sut.isValid(input, null)).isTrue();
    }

    @Test
    public void onlyElementsWithSameIDsShouldReturnfalse() {
        List<CanvasElement> input = Arrays.asList(
                CanvasElement.builder().id("Erste ID").build(),
                CanvasElement.builder().id("Erste zweite ID").build(),
                CanvasElement.builder().id("Erste ID").build()
        );
        assertThat(sut.isValid(input, null)).isFalse();
    }
}
