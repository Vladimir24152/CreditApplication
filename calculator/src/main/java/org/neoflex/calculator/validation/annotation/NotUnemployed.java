package org.neoflex.calculator.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.neoflex.calculator.validation.validator.NotUnemployedValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = NotUnemployedValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface NotUnemployed {
    String message() default "Отказ в займе не трудоустроенным клиентам";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
