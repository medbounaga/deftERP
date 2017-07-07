package com.defterp.modules.commonClasses;

import java.util.Calendar;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

public class IdGenerator {
    
    private static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);

    public static String generateSaleId(int id) {

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

    public static String generatePurchaseId(int id) {

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

    public static String generateDeliveryOutId(int id) {

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

    public static String generateDeliveryInId(int id) {

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

    public static String generateInvoiceId(int id) {

        if (id < 10) {
            return "INV/" + CURRENT_YEAR + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "INV/" + CURRENT_YEAR + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "INV/" + CURRENT_YEAR + "/0" + id;

        } else {
            return "INV/" + CURRENT_YEAR + "/" + id;
        }
    }
    
    

    public static String generateBillId(Integer id) {

        if (id < 10) {
            return "BILL/" + CURRENT_YEAR + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "BILL/" + CURRENT_YEAR + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "BILL/" + CURRENT_YEAR + "/0" + id;

        } else {
            return "BILL/" + CURRENT_YEAR + "/" + id;
        }
    }

    public static String generateCustomerInPayment(Integer id) {

        if (id < 10) {
            return "CUST.IN/" + CURRENT_YEAR + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "CUST.IN/" + CURRENT_YEAR + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "CUST.IN/" + CURRENT_YEAR + "/0" + id;

        } else {
            return "CUST.IN/" + CURRENT_YEAR + "/" + id;
        }
    }
    
    public static String generateCustomerOutPayment(Integer id) {;

        if (id < 10) {
            return "CUST.OUT/" + CURRENT_YEAR + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "CUST.OUT/" + CURRENT_YEAR + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "CUST.OUT/" + CURRENT_YEAR + "/0" + id;

        } else {
            return "CUST.OUT/" + CURRENT_YEAR + "/" + id;
        }
    }

    public static String generateSupplierInPayment(Integer id) {

        if (id < 10) {
            return "VEND.IN/" + CURRENT_YEAR + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "VEND.IN/" + CURRENT_YEAR + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "VEND.IN/" + CURRENT_YEAR + "/0" + id;

        } else {
            return "VEND.IN/" + CURRENT_YEAR + "/" + id;
        }
    }
    
    public static String generateSupplierOutPayment(Integer id) {

        if (id < 10) {
            return "VEND.OUT/" + CURRENT_YEAR + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "VEND.OUT/" + CURRENT_YEAR + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "VEND.OUT/" + CURRENT_YEAR + "/0" + id;

        } else {
            return "VEND.OUT/" + CURRENT_YEAR + "/" + id;
        }
    }

    public static String generatePaymentCashEntryId(Integer id) {

        if (id < 10) {
            return "CASH/" + CURRENT_YEAR + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "CASH/" + CURRENT_YEAR + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "CASH/" + CURRENT_YEAR + "/0" + id;

        } else {
            return "CASH/" + CURRENT_YEAR + "/" + id;
        }
    }
    
    public static String generatePaymentBankEntryId(Integer id) {

        if (id < 10) {
            return "BANK/" + CURRENT_YEAR + "/000" + id;

        } else if (id >= 10 && id < 100) {
            return "BANK/" + CURRENT_YEAR + "/00" + id;

        } else if (id >= 100 && id < 1000) {
            return "BANK/" + CURRENT_YEAR + "/0" + id;

        } else {
            return "BANK/" + CURRENT_YEAR + "/" + id;
        }
    }
}
