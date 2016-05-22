package com.casa.erp.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */
 
@Documented
@Constraint(validatedBy = InDateRangeValidator.class)
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InDateRange {

    String message() default "{InDateRange}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    String min() default "1900";

    String max() default "2100";
}