package org.neoflex.calculator.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.neoflex.calculator.annotations.MinimumTotalWorkExperience;

public class MinimumTotalWorkExperienceValidator implements
        ConstraintValidator<MinimumTotalWorkExperience, Integer> {

    private Integer countOfMonth;

    @Override
    public void initialize(MinimumTotalWorkExperience constraintAnnotation) {
        this.countOfMonth = constraintAnnotation.countOfMonth();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value == null){
            return false;
        }

        if (value < countOfMonth) {
            return false;
        }

        return true;
    }
}
