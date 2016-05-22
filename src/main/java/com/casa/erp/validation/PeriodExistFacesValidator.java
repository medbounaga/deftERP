
package com.casa.erp.validation;

import com.casa.erp.dao.InvoiceFacade;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import javax.inject.Inject;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */



@FacesValidator("PeriodExistValidator")
public class PeriodExistFacesValidator implements Validator{
    
    @Inject
    private InvoiceFacade invoiceFacade;

    @Override
    public void validate(FacesContext context, UIComponent component, Object date) throws ValidatorException {
//        Period acountingPeriod;
//        acountingPeriod = invoiceFacade.findPeriod(date);
//        if (acountingPeriod == null){
//            String formatedDate =  new SimpleDateFormat("dd-MM-yyyy").format(date);
////             throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, msg));
//        FacesMessage msg =
//              new FacesMessage(" There is no period defined for this date: " + formatedDate +
//                      ". Please select a date within " + Calendar.getInstance().get(Calendar.YEAR));
//      msg.setSeverity(FacesMessage.SEVERITY_WARN);
//      throw new ValidatorException(msg);
//        }
        
    }
    
}
