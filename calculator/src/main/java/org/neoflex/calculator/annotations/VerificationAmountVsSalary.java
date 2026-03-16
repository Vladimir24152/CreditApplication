package org.neoflex.calculator.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.neoflex.calculator.validation.AmountVsSalaryValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AmountVsSalaryValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface VerificationAmountVsSalary {

    String message() default "Сумма кредита не должна превышать зарплату более чем в {times} раз";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int times() default 24;
}
