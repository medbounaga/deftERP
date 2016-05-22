package com.casa.erp.beans.util;

import java.util.HashMap;
import java.util.ResourceBundle;
import javax.enterprise.inject.Produces;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */



public class DayMonthProducer{

    @Produces
    @DayMonth
    public HashMap<String, String> getLocalizedNames() {

        HashMap<String, String> status = new HashMap<>();
        ResourceBundle bundle = JsfUtil.getBundle();
            
            status.put("Monday", bundle.getString("Monday"));
            status.put("Tuesday", bundle.getString("Tuesday"));
            status.put("Wednesday", bundle.getString("Wednesday"));
            status.put("Thursday", bundle.getString("Thursday"));
            status.put("Friday", bundle.getString("Friday"));
            status.put("Saturday", bundle.getString("Saturday"));
            status.put("Sunday", bundle.getString("Sunday"));
            
            status.put("January", bundle.getString("January"));
            status.put("February", bundle.getString("February"));
            status.put("March", bundle.getString("March"));
            status.put("April", bundle.getString("April"));
            status.put("May", bundle.getString("May"));
            status.put("June", bundle.getString("June"));
            status.put("July", bundle.getString("July"));           
            status.put("August", bundle.getString("August"));
            status.put("September", bundle.getString("September"));
            status.put("October", bundle.getString("October"));
            status.put("November", bundle.getString("November"));
            status.put("December", bundle.getString("December"));

            return status;  
    }
}
