package org.neoflex.calculator.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.neoflex.calculator.validation.RatioOfTheAmountAndSalaryValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = RatioOfTheAmountAndSalaryValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RatioOfTheAmountAndSalary {

    String message() default "Отказ в займе - Сумма кредита не должна превышать зарплату более чем в {times} раз";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int times() default 24;
}
