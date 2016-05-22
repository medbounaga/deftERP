
package com.casa.erp.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */



@Constraint(validatedBy = StrictlyPositiveNumberValidator.class)
@ReportAsSingleViolation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface StrictlyPositiveNumber
{

    String message() default "";

   Class<?>[] groups() default {};

   Class<? extends Payload>[] payload() default {};


}

