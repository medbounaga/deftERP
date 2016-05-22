
package com.casa.erp.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

public class PositiveNumberValidator implements ConstraintValidator<PositiveNumber, Object> {

    @Override
    public void initialize(PositiveNumber constraintAnnotation) {

    }

    @Override
    public boolean isValid(Object number, ConstraintValidatorContext context) {
        if (number instanceof Integer) {
            Integer num = (Integer) number;
            return (num >= 0);
            
        } else if (number instanceof Double) {
            Double num = (Double) number;
            return (num >= 0d);
            
        } else if (number instanceof Long) {
            Long num = (Long) number;
            return (num >= 0L);
            
        } else if (number instanceof Float) {
         Float num = (Float) number;
            return (num >= 0F);
            
        }else{
            return false;
        }
    }
    
}
