package org.neoflex.calculator.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.neoflex.calculator.validation.validator.MinimumCurrentWorkExperienceValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = MinimumCurrentWorkExperienceValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MinimumCurrentWorkExperience {
    String message() default "Отказ в займе клиентам с текущим стажем менее {countOfMonth} месяцев";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int countOfMonth() default 3;
}
