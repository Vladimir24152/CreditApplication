package org.neoflex.calculator.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.neoflex.calculator.enums.EmploymentStatus;
import org.neoflex.calculator.validation.annotation.NotUnemployed;

public class NotUnemployedValidator implements ConstraintValidator<NotUnemployed, EmploymentStatus>{

    @Override
    public boolean isValid(EmploymentStatus value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        if (EmploymentStatus.UNEMPLOYED.equals(value)) {
            return false;
        }

        return true;
    }
}
