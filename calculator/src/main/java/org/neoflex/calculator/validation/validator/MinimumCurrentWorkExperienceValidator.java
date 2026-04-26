package org.neoflex.calculator.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.neoflex.calculator.validation.annotation.MinimumCurrentWorkExperience;

public class MinimumCurrentWorkExperienceValidator  implements
        ConstraintValidator<MinimumCurrentWorkExperience, Integer> {

    private int countOfMonth;

    @Override
    public void initialize(MinimumCurrentWorkExperience constraintAnnotation) {
        this.countOfMonth = constraintAnnotation.countOfMonth();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null){
            return true;
        }

        if (value < countOfMonth) {
            return false;
        }

        return true;
    }
}
