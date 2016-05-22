
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

public class CurrentYearValidator implements ConstraintValidator<CurrentYear, Date> {


    @Override
    public void initialize(CurrentYear constraintAnnotation) {

    }

    @Override
    public boolean isValid(Date date, ConstraintValidatorContext context) {

        DateTime dateTime = new DateTime(date);
        DateTime now = new DateTime();
        
        if (dateTime.getYear() != now.getYear()) {
            return false;
        }
        return true;
    }

}
