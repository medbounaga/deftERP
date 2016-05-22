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



public class StatusProducer {

    @Produces
    @Status
    public HashMap<String, String> getStatus() {

        HashMap<String, String> status = new HashMap<>();
        ResourceBundle bundle = JsfUtil.getBundle();
        
        try{
            status.put("Draft", bundle.getString("Draft"));
            status.put("Open", bundle.getString("Open"));
            status.put("Paid", bundle.getString("Paid"));
            status.put("Cancelled", bundle.getString("Cancelled"));
            status.put("Posted", bundle.getString("Posted"));
            status.put("Unposted", bundle.getString("Unposted"));
            status.put("Waiting Availability", bundle.getString("WaitingAvailability"));
            status.put("Partially Available", bundle.getString("PartiallyAvailable"));
            status.put("Available", bundle.getString("Available"));
            status.put("Done", bundle.getString("Done"));
            status.put("Purchase Order", bundle.getString("PurchaseOrder"));
            status.put("Quotation", bundle.getString("Quotation"));
            status.put("New", bundle.getString("New"));
            status.put("Fully Invoiced", bundle.getString("FullyInvoiced"));
            status.put("To Invoice", bundle.getString("ToInvoice"));
            
        }catch(Exception e){
            System.out.println("Bundle String not found");
        }

            return status;  
    }
}
