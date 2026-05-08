package org.neoflex.credit.lib.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.neoflex.credit.lib.validation.annotation.Adult;

import java.time.LocalDate;
import java.time.Period;

public class AdultValidator implements ConstraintValidator<Adult, LocalDate> {

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (Period.between(value, LocalDate.now()).getYears() < 18) {
            return false;
        }

        return true;
    }
}
