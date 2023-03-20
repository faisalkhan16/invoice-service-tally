package com.invoice.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ElementType.TYPE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = InvoiceRequestValidator.class)
public @interface InvoiceRequestConstraint {

    String message() default "Request body does not contain all mandatory fields";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
