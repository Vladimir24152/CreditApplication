package org.neoflex.calculator.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.neoflex.calculator.annotations.NotUnemployed;
import org.neoflex.calculator.enums.EmploymentStatus;

public class NotUnemployedValidator implements ConstraintValidator<NotUnemployed, EmploymentStatus>{

    @Override
    public boolean isValid(EmploymentStatus value, ConstraintValidatorContext context) {
        if (value == null) {
            return false;
        }

        if (EmploymentStatus.UNEMPLOYED.equals(value)) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Отказ в займе не трудоустроенным клиентам"
            ).addConstraintViolation();
            return false;
        }

        return true;
    }
}
