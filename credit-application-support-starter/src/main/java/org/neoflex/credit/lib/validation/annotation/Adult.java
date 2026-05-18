package org.neoflex.credit.lib.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.neoflex.credit.lib.validation.validator.AdultValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AdultValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Adult {
    String message() default "Отказ в займе - Неверная дата рождения, Клиент должен быть совершеннолетним";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
