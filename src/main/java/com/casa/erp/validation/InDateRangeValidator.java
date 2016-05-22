package com.casa.erp.validation;

import java.util.Date;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.joda.time.DateTime;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */
 
public class InDateRangeValidator implements ConstraintValidator<InDateRange, Date> {

    private final Date maxDate = new DateTime(2101, 1, 1, 0, 0).toDate();
    private final Date minDate = new DateTime(1899, 12, 31, 0, 0).toDate();
    
    @Override
    public void initialize(InDateRange constraintAnnotation) {}
    
    @Override
    public boolean isValid(Date value, ConstraintValidatorContext context) {
            return value == null || (value.after(minDate) && value.before(maxDate));
    }    
}