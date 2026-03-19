package org.neoflex.calculator.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.neoflex.calculator.annotations.RatioOfTheAmountAndSalary;
import org.neoflex.calculator.dto.ScoringDataDto;

import java.math.BigDecimal;

public class RatioOfTheAmountAndSalaryValidator implements
        ConstraintValidator<RatioOfTheAmountAndSalary, ScoringDataDto> {

    private int times;

    @Override
    public void initialize(RatioOfTheAmountAndSalary constraintAnnotation) {
        this.times = constraintAnnotation.times();
    }

    @Override
    public boolean isValid(ScoringDataDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        BigDecimal amount = dto.getAmount();
        BigDecimal salary = dto.getEmployment() != null ?
                dto.getEmployment().getSalary() : null;

        if (amount == null || salary == null) {
            return false;
        }

        BigDecimal maxAllowedAmount = salary.multiply(BigDecimal.valueOf(times));

        if (amount.compareTo(maxAllowedAmount) > 0) {
            return false;
        }

        return true;
    }
}