package com.defterp.modules.dashboard.queryBuilders;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

public class DashboardQueryBuilder {

    
    public static String getGenerateYearQuartersNamesQuery(int numberOfPeriods) {

        String query = "SELECT ";

                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "concat(YEAR(CURDATE() - interval " + i + " Quarter),'-Q',QUARTER(CURDATE() - interval " + i + " Quarter)), ";
                    } else {
                        query += "concat(YEAR(CURDATE() - interval " + i + " Quarter),'-Q',QUARTER(CURDATE() - interval " + i + " Quarter)); ";
                    }
                }
        
        return query;
    }


    public static String getNumberOfSaleOrdersByPeriodQuery(int numberOfPeriods, String periodType) {

        String query = "SELECT ";

        switch (periodType) {
            
            case "Day":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE(date) = SUBDATE(CURDATE()," + i + ") THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(date) = SUBDATE(CURDATE()," + i + ") THEN 1 ELSE 0 END) ";
                    }
                }
                
                break;
                
            case "Month":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN 1 ELSE 0 END) ";
                    }
                }
                
                break;
                
            case "Quarter":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(date),QUARTER(date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN 1 ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(date),QUARTER(date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN 1 ELSE 0 END) ";
                    }
                }
                
                break;
                
            default:
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN yearweek(date) = yearweek(CURDATE() - interval " + i + " week) THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(date) = yearweek(CURDATE() - interval " + i + " week) THEN 1 ELSE 0 END) ";
                    }
                }
                
                break;
        }

        query += "FROM `defterp`.`sale_order`;";
        
        return query;

    }

    public static String getSalesAmountByPeriodQuery(int numberOfPeriods, String periodType) {

        String query = "SELECT ";

        switch (periodType) {
            
            case "Day":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.credit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.credit ELSE 0 END) ";
                    }
                }
                break;
                
            case "Month":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.credit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.credit ELSE 0 END) ";
                    }
                }
                break;
                
            case "Quarter":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.credit ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.credit ELSE 0 END) ";
                    }
                }
                break; 
                
            default:
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.credit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.credit ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `defterp`.`journal_item` item \n"
                + "join `defterp`.`journal` journal on item.journal_id = journal.id \n"
                + "WHERE journal.name = 'Customer Invoices' ;";

        return query;
    }
    
     public static String getCostOfGoodsSoldByPeriodQuery(int numberOfPeriods, String periodType) {

        String query = "SELECT ";

        switch (periodType) {
            
            case "Day":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.cost_of_goods_sold ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.cost_of_goods_sold ELSE 0 END) ";
                    }
                }
                break;
                
            case "Month":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.cost_of_goods_sold ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.cost_of_goods_sold ELSE 0 END) ";
                    }
                }
                break;
                
            case "Quarter":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.cost_of_goods_sold ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.cost_of_goods_sold ELSE 0 END) ";
                    }
                }
                break;   
                
            default:
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.cost_of_goods_sold ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.cost_of_goods_sold ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `defterp`.`journal_item` item \n"
                + "join `defterp`.`journal` journal on item.journal_id = journal.id \n"
                + "WHERE journal.name = 'Customer Invoices' ;";

        return query;
    }

    
     public static String getTotalProfitByPeriodQuery(int numberOfPeriods, String periodType) {

        String query = "SELECT ";

        switch (periodType) {
            
            case "Day":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END) ";
                    }
                }
                break;
                
            case "Month":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END) ";
                    }
                }
                break;
                
            case "Quarter":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END) ";
                    }
                }
                break;    
                
            default:
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `defterp`.`journal_item` item \n"
                + "join `defterp`.`journal` journal on item.journal_id = journal.id \n"
                + "WHERE journal.name = 'Customer Invoices' ;";
      
        return query;
    }


    public static String getPurchasesAmountByPeriodQuery(int numberOfPeriods, String periodType) {

        String query = "SELECT ";

        switch (periodType) {
            
            case "Day":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.debit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.debit ELSE 0 END) ";
                    }
                }
                break;
                
            case "Month":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.debit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.debit ELSE 0 END) ";
                    }
                }
                break;
                
            case "Quarter":
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.debit ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.debit ELSE 0 END) ";
                    }
                }
                break; 
                
            default:
                
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.debit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.debit ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `defterp`.`journal_item` item \n"
                + "join `defterp`.`journal` journal on item.journal_id = journal.id \n"
                + "WHERE journal.name = 'Vendor Bills' ;";
      
        return query;
    }


    public static String getTopSalesByProductQuery(int nProducts, String periodType) {

        if (nProducts <= 0) {
            nProducts = 5;
        }

        String query = "SELECT  pr.name , SUM(line.sub_total), SUM(line.quantity) "
                + "FROM `defterp`.`sale_order_line` line "
                + "join `defterp`.`product` pr on line.product_id = pr.id "
                + "join `defterp`.`sale_order` sale on line.order_id = sale.id WHERE ";

        switch (periodType) {
            case "Month":
                query += "DATE_FORMAT(sale.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + 0 + " MONTH, '%Y%m') ";
                break; 
                
            case "Quarter":
                query += "concat(YEAR(sale.date),QUARTER(sale.date)) = concat(YEAR(CURDATE() - interval " + 0 + " QUARTER), QUARTER(CURDATE() - interval " + 0 + " QUARTER))";
                break; 
                
            case "Day":
                query += "DATE(sale.date) = SUBDATE(CURDATE()," + 0 + ") ";
                break;
                
            default:
                query += "yearweek(sale.date) = yearweek(CURDATE() - interval " + 0 + " week) ";
                break;
        }

        query += "GROUP BY pr.name ORDER BY SUM(line.sub_total) DESC LIMIT " + nProducts + ";";

        return query;
    }

    public static String getTopPurchasesByProductQuery(int nProducts, String periodType) {

        if (nProducts <= 0) {
            nProducts = 5;
        }

        String query = "SELECT  pr.name , SUM(line.sub_total), SUM(line.quantity) "
                + "FROM `defterp`.`purchase_order_line` line "
                + "join `defterp`.`product` pr on line.product_id = pr.id "
                + "join `defterp`.`purchase_order` purchase on line.order_id = purchase.id WHERE ";

        switch (periodType) {
            case "Month":
                query += "DATE_FORMAT(purchase.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + 0 + " MONTH, '%Y%m') ";
                break;
            case "Quarter":
                query += "concat(YEAR(purchase.date),QUARTER(purchase.date)) = concat(YEAR(CURDATE() - interval " + 0 + " QUARTER), QUARTER(CURDATE() - interval " + 0 + " QUARTER))";
                break;    
            case "Day":
                query += "DATE(purchase.date) = SUBDATE(CURDATE()," + 0 + ") ";
                break;
            default:
                query += "yearweek(purchase.date) = yearweek(CURDATE() - interval " + 0 + " week) ";
                break;
        }

        query += "GROUP BY pr.name ORDER BY SUM(line.sub_total) DESC LIMIT " + nProducts + ";";

        return query;
    }

    public static String getTopPurchasesByVendorQuery(int nPartners, String periodType) {

        if (nPartners <= 0) {
            nPartners = 5;
        }

        String query = "SELECT  par.name , SUM(purchase.amount_total), count(*)\n"
                + "FROM `defterp`.`purchase_order` purchase join `defterp`.`partner` par on purchase.partner_id = par.id WHERE ";

        switch (periodType) {
            
            case "Month":
                query += "DATE_FORMAT(purchase.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + 0 + " MONTH, '%Y%m') ";
                break;
                
            case "Quarter":
                query += "concat(YEAR(purchase.date),QUARTER(purchase.date)) = concat(YEAR(CURDATE() - interval " + 0 + " QUARTER), QUARTER(CURDATE() - interval " + 0 + " QUARTER))";
                break;  
                
            case "Day":
                query += "DATE(purchase.date) = SUBDATE(CURDATE()," + 0 + ") ";
                break;
                
            default:
                query += "yearweek(purchase.date) = yearweek(CURDATE() - interval " + 0 + " week) ";
                break;
        }

        query += "AND par.supplier = 1 GROUP BY par.name ORDER BY SUM(purchase.amount_total) DESC LIMIT " + nPartners + ";";

        return query;
    }

    public static String getTopSalesByCustomerQuery(int nPartners, String periodType) {

        if (nPartners < 0) {
            nPartners = 5;
        }
        
        String query = "SELECT  par.name , SUM(sale.amount_total), count(*)\n"
                + "FROM `defterp`.`sale_order` sale join `defterp`.`partner` par on sale.partner_id = par.id WHERE ";

        switch (periodType) {
            
            case "Month":
                query += "DATE_FORMAT(sale.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + 0 + " MONTH, '%Y%m') ";
                break;
                
            case "Quarter":
                query += "concat(YEAR(sale.date),QUARTER(sale.date)) = concat(YEAR(CURDATE() - interval " + 0 + " QUARTER), QUARTER(CURDATE() - interval " + 0 + " QUARTER))";
                break;   
                
            case "Day":
                query += "DATE(sale.date) = SUBDATE(CURDATE()," + 0 + ") ";
                break;
                
            default:
                query += "yearweek(sale.date) = yearweek(CURDATE() - interval " + 0 + " week) ";
                break;
        }

        query += "AND par.customer = 1 GROUP BY par.name ORDER BY SUM(sale.amount_total) DESC LIMIT " + nPartners + ";";

        return query;
    }

    public static String getNumberOfNewCustomersByPeriodQuery(int numberOfPeriods, String periodType) {

        String query = "SELECT ";

        switch (periodType) {
            
            case "Day":
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE(create_date) = SUBDATE(CURDATE()," + i + ") THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(create_date) = SUBDATE(CURDATE()," + i + ") THEN 1 ELSE 0 END) ";
                    }
                }
                break;
                
            case "Month":
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(create_date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(create_date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN 1 ELSE 0 END) ";
                    }
                }
                break;
                
            case "Quarter":
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(create_date),QUARTER(create_date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN 1 ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(create_date),QUARTER(create_date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN 1 ELSE 0 END) ";
                    }
                }
                break; 
                
            default:
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN yearweek(create_date) = yearweek(CURDATE() - interval " + i + " week) THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(create_date) = yearweek(CURDATE() - interval " + i + " week) THEN 1 ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `defterp`.`partner` WHERE customer = 1;";

        return query;
    }

    public static String getNumberOfSaleOrdersToConfirmQuery() {

        String query = "SELECT count(*), IFNULL(SUM(amount_total),0)\n"
                + "FROM `defterp`.`sale_order` \n"
                + "WHERE state = 'Quotation' ; ";

        
        return query;
    }

    public static String getNumberOfPurchaseOrdersToConfirmQuery() {

        String query = "SELECT count(*), IFNULL(SUM(amount_total),0)\n"
                + "FROM `defterp`.`purchase_order` \n"
                + "WHERE state = 'Quotation' ; ";

        return query;

    }

    public static String getNumberOfInvoicesToConfirmQuery() {

        String query = "SELECT count(*), IFNULL(SUM(amount_total),0)\n"
                + "FROM `defterp`.`invoice` \n"
                + "WHERE state = 'Draft' AND type='Sale' ; ";
       
        return query;
    }

    public static String getNumberOfBillsToConfirmQuery() {

        String query = "SELECT count(*), IFNULL(SUM(amount_total),0)\n"
                + "FROM `defterp`.`invoice` \n"
                + "WHERE state = 'Draft' AND type='Purchase' ; ";

        return query;
    }

    public static String getTopReceivablesByCustomerQuery() {

        String query = "SELECT  par.name , SUM(inv.residual)\n"
                + "FROM `defterp`.`invoice` inv join `defterp`.`partner` par on inv.partner_id = par.id \n"
                + "WHERE  inv.state = 'Open' AND type='Sale' AND inv.residual > 0 \n"
                + "GROUP BY par.name\n"
                + "ORDER BY SUM(inv.residual) DESC limit 5;";

        return query;
    }
    
    public static String getTopPayablesByVendorQuery() {

        String query = "SELECT  par.name , SUM(inv.residual)\n"
                + "FROM `defterp`.`invoice` inv join `defterp`.`partner` par on inv.partner_id = par.id \n"
                + "WHERE  inv.state = 'Open' AND type='Purchase' AND inv.residual > 0 \n"
                + "GROUP BY par.name\n"
                + "ORDER BY SUM(inv.residual) DESC limit 5;";

        return query;
    }

    public static String getTotalReceivablesQuery() {

        String query = "SELECT  SUM(inv.residual)\n"
                + "FROM `defterp`.`invoice` inv \n"
                + "WHERE  inv.state = 'Open' AND type = 'Sale';";

        return query;
    }

    public static String getTotalPayablesQuery() {

        String query = "SELECT  SUM(inv.residual)\n"
                + "FROM `defterp`.`invoice` inv \n"
                + "WHERE  inv.state = 'Open' AND type = 'Purchase';";

        return query;
    }

    public static String getCustomerPaymentsByPeriodQuery(int numberOfPeriods, String periodType) {

        String query = "SELECT ";

        switch (periodType) {
            
            case "Day":
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE(invPay.date) = SUBDATE(CURDATE()," + i + ") THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(invPay.date) = SUBDATE(CURDATE()," + i + ") THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
                
            case "Month":
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(invPay.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(invPay.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
                
            case "Quarter":
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(invPay.date),QUARTER(invPay.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN invPay.paid_amount ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(invPay.date),QUARTER(invPay.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;  
                
            default:
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN yearweek(invPay.date) = yearweek(CURDATE() - interval " + i + " week) THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(invPay.date) = yearweek(CURDATE() - interval " + i + " week) THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `defterp`.`invoice_payment` invPay \n"
                + "join `defterp`.`journal_entry` entry on invPay.journal_entry_id = entry.id \n"
                + "join `defterp`.`payment` pay on pay.entry_id = entry.id  \n"
                + "join `defterp`.`journal` journal on pay.journal_id = journal.id    \n"
                + "where pay.type = 'in' AND pay.partner_type = 'customer' AND invPay.name <> 'Write-Off' \n"
                + "GROUP BY journal.name ORDER BY journal.name ASC;";

        return query;
    }

    public static String getVendorPaymentsByPeriodQuery(int numberOfPeriods, String periodType) {

        String query = "SELECT ";

        switch (periodType) {
            
            case "Day":
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE(invPay.date) = SUBDATE(CURDATE()," + i + ") THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(invPay.date) = SUBDATE(CURDATE()," + i + ") THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
                
            case "Month":
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(invPay.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(invPay.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
                
            case "Quarter":
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(invPay.date),QUARTER(invPay.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN invPay.paid_amount ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(invPay.date),QUARTER(invPay.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;    
                
            default:
                for (int i = 0; i < numberOfPeriods; i++) {
                    if (i < numberOfPeriods - 1) {
                        query += "SUM(CASE WHEN yearweek(invPay.date) = yearweek(CURDATE() - interval " + i + " week) THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(invPay.date) = yearweek(CURDATE() - interval " + i + " week) THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `defterp`.`invoice_payment` invPay \n"
                + "join `defterp`.`journal_entry` entry on invPay.journal_entry_id = entry.id \n"
                + "join `defterp`.`payment` pay on pay.entry_id = entry.id  \n"
                + "join `defterp`.`journal` journal on pay.journal_id = journal.id    \n"
                + "where pay.type = 'out' AND pay.partner_type = 'supplier' AND invPay.name <> 'Write-Off' \n"
                + "GROUP BY journal.name ORDER BY journal.name ASC;";

        return query;    
    }

}
