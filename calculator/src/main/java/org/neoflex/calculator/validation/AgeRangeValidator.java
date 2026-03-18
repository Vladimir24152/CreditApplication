package org.neoflex.calculator.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.neoflex.calculator.annotations.AgeRange;

import java.time.LocalDate;

public class AgeRangeValidator  implements ConstraintValidator<AgeRange, LocalDate> {

    private int minAge;
    private int maxAge;

    @Override
    public void initialize(AgeRange constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
        this.maxAge = constraintAnnotation.maxAge();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        if (LocalDate.now().minusYears(minAge).isBefore(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.format(
                    "Отказ в займе клиентам младше %d лет"
                    ,minAge)
            ).addConstraintViolation();
            return false;
        }

        if (!LocalDate.now().minusYears(maxAge).isBefore(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(String.format(
                    "Отказ в займе клиентам старше %d лет"
                    ,maxAge)
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
