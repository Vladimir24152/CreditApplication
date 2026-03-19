package org.neoflex.calculator.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.neoflex.calculator.validation.MinimumTotalWorkExperienceValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MinimumTotalWorkExperienceValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MinimumTotalWorkExperience {
    String message() default "Отказ в займе клиентам с общим стажем менее {countOfMonth} месяцев";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int countOfMonth() default 18;
}
