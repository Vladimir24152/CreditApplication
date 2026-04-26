package org.neoflex.calculator.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.neoflex.calculator.validation.validator.AgeRangeValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AgeRangeValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface AgeRange {
    String message() default "Отказ в займе для клиентов младше {minAge} или старше {maxAge} лет";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int minAge() default 20;

    int maxAge() default 65;
}
