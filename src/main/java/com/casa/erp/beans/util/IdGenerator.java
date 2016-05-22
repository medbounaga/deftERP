package com.casa.erp.beans.util;

import java.util.Calendar;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

public class IdGenerator {

    public String generateSaleId(int id) {

        if (id < 10) {
            return "SO000" + id;

        } else if (id >= 10 && id < 100) {
            return "SO00" + id;

        } else if (id >= 100 && id < 1000) {
            return "SO0" + id;

        } else {
            return "SO" + id;

        }
    }

    public String generatePurchaseId(int id) {

        if (id < 10) {
            return "PO000" + id;

        } else if (id >= 10 && id < 100) {
            return "PO00" + id;

        } else if (id >= 100 && id < 1000) {
            return "PO0" + id;

        } else {
            return "PO" + id;

        }
    }

    public String generateDeliveryOutId(int id) {

        if (id < 10) {
            return "WH/OUT/000" + id;

        } else if (id >= 10 && id < 100) {
            return "WH/OUT/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "WH/OUT/0" + id;

        } else {
            return "WH/OUT/" + id;

        }

    }

    public String generateDeliveryInId(int id) {

        if (id < 10) {
            return "WH/IN/000" + id;

        } else if (id >= 10 && id < 100) {
            return "WH/IN/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "WH/IN/0" + id;

        } else {
            return "WH/IN/" + id;

        }
    }

    public String generateInvoiceOutId(int id) {

        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        if (id < 10) {
            return "INV/" + currentYear + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "INV/" + currentYear + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "INV/" + currentYear + "/0" + id;

        } else {
            return "INV/" + currentYear + "/" + id;
        }
    }
    
//    public String generateRefundInvoiceOutId(int id) {
//
//        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
//
//        if (id < 10) {
//            return "RINV/" + currentYear + "/000" + id;
//
//        } else if (id >= 10 && id < 100) {
//            return "RINV/" + currentYear + "/00" + id;
//
//        } else if (id >= 100 && id < 1000) {
//            return "RINV/" + currentYear + "/0" + id;
//
//        } else {
//            return "RINV/" + currentYear + "/" + id;
//        }
//    }
    

    public String generateInvoiceInId(Integer id) {

        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        if (id < 10) {
            return "BILL/" + currentYear + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "BILL/" + currentYear + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "BILL/" + currentYear + "/0" + id;

        } else {
            return "BILL/" + currentYear + "/" + id;
        }
    }

    public String generateCustomerInPayment(Integer id) {

        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        if (id < 10) {
            return "CUST.IN/" + currentYear + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "CUST.IN/" + currentYear + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "CUST.IN/" + currentYear + "/0" + id;

        } else {
            return "CUST.IN/" + currentYear + "/" + id;
        }
    }
    
    public String generateCustomerOutPayment(Integer id) {

        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        if (id < 10) {
            return "CUST.OUT/" + currentYear + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "CUST.OUT/" + currentYear + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "CUST.OUT/" + currentYear + "/0" + id;

        } else {
            return "CUST.OUT/" + currentYear + "/" + id;
        }
    }

    public String generateSupplierInPayment(Integer id) {

        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        if (id < 10) {
            return "VEND.IN/" + currentYear + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "VEND.IN/" + currentYear + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "VEND.IN/" + currentYear + "/0" + id;

        } else {
            return "VEND.IN/" + currentYear + "/" + id;
        }
    }
    
    public String generateSupplierOutPayment(Integer id) {

        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        if (id < 10) {
            return "VEND.OUT/" + currentYear + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "VEND.OUT/" + currentYear + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "VEND.OUT/" + currentYear + "/0" + id;

        } else {
            return "VEND.OUT/" + currentYear + "/" + id;
        }
    }

    public String generatePaymentCashEntryId(Integer id) {
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        if (id < 10) {
            return "CASH/" + currentYear + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "CASH/" + currentYear + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "CASH/" + currentYear + "/0" + id;

        } else {
            return "CASH/" + currentYear + "/" + id;
        }
    }
    
    public String generatePaymentBankEntryId(Integer id) {
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));

        if (id < 10) {
            return "BANK/" + currentYear + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "BANK/" + currentYear + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "BANK/" + currentYear + "/0" + id;

        } else {
            return "BANK/" + currentYear + "/" + id;
        }
    }
}
