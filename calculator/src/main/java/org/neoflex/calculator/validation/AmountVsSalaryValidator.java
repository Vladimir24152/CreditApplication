package org.neoflex.calculator.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.neoflex.calculator.annotations.VerificationAmountVsSalary;
import org.neoflex.calculator.dto.ScoringDataDto;

import java.math.BigDecimal;

public class AmountVsSalaryValidator implements
        ConstraintValidator<VerificationAmountVsSalary, ScoringDataDto> {

    private int times;

    @Override
    public void initialize(VerificationAmountVsSalary constraintAnnotation) {
        this.times = constraintAnnotation.times();
    }

    @Override
    public boolean isValid(ScoringDataDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return false;
        }

        BigDecimal amount = dto.getAmount();
        BigDecimal salary = dto.getEmployment() != null ?
                dto.getEmployment().getSalary() : null;

        if (amount == null || salary == null) {
            return false;
        }

        BigDecimal maxAllowedAmount = salary.multiply(BigDecimal.valueOf(times));

        if (amount.compareTo(maxAllowedAmount) > 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    String.format("Отказ в займе - сумма кредита не должна превышать зарплату более чем в %d раз",
                            times)
            ).addPropertyNode("amount").addConstraintViolation();
            return false;
        }

        return true;
    }
}