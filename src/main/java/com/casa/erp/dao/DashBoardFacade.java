package com.casa.erp.dao;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */


@Stateless
public class DashBoardFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;
    
    public List<Object[]> getQuarters(int interval) {

        String query = "SELECT ";

                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "concat(YEAR(CURDATE() - interval " + i + " Quarter),'-Q',QUARTER(CURDATE() - interval " + i + " Quarter)), ";
                    } else {
                        query += "concat(YEAR(CURDATE() - interval " + i + " Quarter),'-Q',QUARTER(CURDATE() - interval " + i + " Quarter)); ";
                    }
                }

        Query q = em.createNativeQuery(query);
        return q.getResultList();
    }


    public List<Object[]> salesOrderCount(int interval, String period) {

        String query = "SELECT ";

        switch (period) {
            case "Day":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE(date) = SUBDATE(CURDATE()," + i + ") THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(date) = SUBDATE(CURDATE()," + i + ") THEN 1 ELSE 0 END) ";
                    }
                }
                break;
            case "Month":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN 1 ELSE 0 END) ";
                    }
                }
                break;
            case "Quarter":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(date),QUARTER(date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN 1 ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(date),QUARTER(date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN 1 ELSE 0 END) ";
                    }
                }
                break;
            default:
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN yearweek(date) = yearweek(CURDATE() - interval " + i + " week) THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(date) = yearweek(CURDATE() - interval " + i + " week) THEN 1 ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `erp`.`sale_order`;";

        Query q = em.createNativeQuery(query);
        return q.getResultList();
    }

    public List<Object[]> salesAmount(int interval, String period) {

        String query = "SELECT ";

        switch (period) {
            case "Day":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.credit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.credit ELSE 0 END) ";
                    }
                }
                break;
            case "Month":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.credit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.credit ELSE 0 END) ";
                    }
                }
                break;
            case "Quarter":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.credit ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.credit ELSE 0 END) ";
                    }
                }
                break;    
            default:
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.credit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.credit ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `erp`.`journal_item` item \n"
                + "join `erp`.`journal` journal on item.journal_id = journal.id \n"
                + "WHERE journal.name = 'Customer Invoices' ;";

        Query q = em.createNativeQuery(query);
        return q.getResultList();
    }
    
     public List<Object[]> costOfGoodsSold(int interval, String period) {

        String query = "SELECT ";

        switch (period) {
            case "Day":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.cost_of_goods_sold ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.cost_of_goods_sold ELSE 0 END) ";
                    }
                }
                break;
            case "Month":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.cost_of_goods_sold ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.cost_of_goods_sold ELSE 0 END) ";
                    }
                }
                break;
            case "Quarter":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.cost_of_goods_sold ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.cost_of_goods_sold ELSE 0 END) ";
                    }
                }
                break;    
            default:
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.cost_of_goods_sold ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.cost_of_goods_sold ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `erp`.`journal_item` item \n"
                + "join `erp`.`journal` journal on item.journal_id = journal.id \n"
                + "WHERE journal.name = 'Customer Invoices' ;";

        Query q = em.createNativeQuery(query);
        return q.getResultList();
    }

    
     public List<Object[]> profit(int interval, String period) {

        String query = "SELECT ";

        switch (period) {
            case "Day":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END) ";
                    }
                }
                break;
            case "Month":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END) ";
                    }
                }
                break;
            case "Quarter":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END) ";
                    }
                }
                break;    
            default:
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN (item.credit - item.cost_of_goods_sold) ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `erp`.`journal_item` item \n"
                + "join `erp`.`journal` journal on item.journal_id = journal.id \n"
                + "WHERE journal.name = 'Customer Invoices' ;";

        Query q = em.createNativeQuery(query);
        return q.getResultList();
    }


    public List<Object[]> PurchasesAmount(int interval, String period) {

        String query = "SELECT ";

        switch (period) {
            case "Day":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.debit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(item.date) = SUBDATE(CURDATE()," + i + ") THEN item.debit ELSE 0 END) ";
                    }
                }
                break;
            case "Month":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.debit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(item.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN item.debit ELSE 0 END) ";
                    }
                }
                break;
            case "Quarter":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.debit ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(item.date),QUARTER(item.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN item.debit ELSE 0 END) ";
                    }
                }
                break;    
            default:
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.debit ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(item.date) = yearweek(CURDATE() - interval " + i + " week) THEN item.debit ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `erp`.`journal_item` item \n"
                + "join `erp`.`journal` journal on item.journal_id = journal.id \n"
                + "WHERE journal.name = 'Vendor Bills' ;";

        Query q = em.createNativeQuery(query);
        return q.getResultList();
    }

   
    public List<Object[]> invoiceCount(int interval, String period) {

        String query = "SELECT ";

        if (period.equals("Month")) {
            for (int i = 0; i < interval; i++) {
                if (i < interval - 1) {
                    query += "SUM(CASE WHEN DATE_FORMAT(date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN 1 ELSE 0 END), ";
                } else {
                    query += "SUM(CASE WHEN DATE_FORMAT(date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN 1 ELSE 0 END) ";
                }
            }
        }

        if (period.equals("Week")) {
            for (int i = 0; i < interval; i++) {
                if (i < interval - 1) {
                    query += "SUM(CASE WHEN yearweek(date) = yearweek(CURDATE() - interval " + i + " week) THEN 1 ELSE 0 END), ";
                } else {
                    query += "SUM(CASE WHEN yearweek(date) = yearweek(CURDATE() - interval " + i + " week) THEN 1 ELSE 0 END) ";
                }
            }
        }

        query += "FROM  `erp`.`invoice` WHERE state <> 'Cancelled' GROUP BY state ORDER BY state ASC;";

        Query q = em.createNativeQuery(query);
        return q.getResultList();
    }

    public List<Object[]> topSalesByProduct(int nProducts, String period, int interval) {

        if (interval < 0) {
            interval = 0;
        }

        if (nProducts < 0) {
            nProducts = 5;
        }

        String query = "SELECT  pr.name , SUM(line.sub_total), SUM(line.quantity) "
                + "FROM `erp`.`sale_order_line` line "
                + "join `erp`.`product` pr on line.product_id = pr.id "
                + "join `erp`.`sale_order` sale on line.order_id = sale.id WHERE ";

        switch (period) {
            case "Month":
                query += "DATE_FORMAT(sale.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + interval + " MONTH, '%Y%m') ";
                break;
            case "Quarter":
                query += "concat(YEAR(sale.date),QUARTER(sale.date)) = concat(YEAR(CURDATE() - interval " + interval + " QUARTER), QUARTER(CURDATE() - interval " + interval + " QUARTER))";
                break;      
            case "Day":
                query += "DATE(sale.date) = SUBDATE(CURDATE()," + interval + ") ";
                break;
            default:
                query += "yearweek(sale.date) = yearweek(CURDATE() - interval " + interval + " week) ";
                break;
        }

        query += "GROUP BY pr.name ORDER BY SUM(line.sub_total) DESC LIMIT " + nProducts + ";";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> topPurchasesByProduct(int nProducts, String period, int interval) {

        if (interval < 0) {
            interval = 0;
        }

        if (nProducts < 0) {
            nProducts = 5;
        }

        String query = "SELECT  pr.name , SUM(line.sub_total), SUM(line.quantity) "
                + "FROM `erp`.`purchase_order_line` line "
                + "join `erp`.`product` pr on line.product_id = pr.id "
                + "join `erp`.`purchase_order` purchase on line.order_id = purchase.id WHERE ";

        switch (period) {
            case "Month":
                query += "DATE_FORMAT(purchase.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + interval + " MONTH, '%Y%m') ";
                break;
            case "Quarter":
                query += "concat(YEAR(purchase.date),QUARTER(purchase.date)) = concat(YEAR(CURDATE() - interval " + interval + " QUARTER), QUARTER(CURDATE() - interval " + interval + " QUARTER))";
                break;    
            case "Day":
                query += "DATE(purchase.date) = SUBDATE(CURDATE()," + interval + ") ";
                break;
            default:
                query += "yearweek(purchase.date) = yearweek(CURDATE() - interval " + interval + " week) ";
                break;
        }

        query += "GROUP BY pr.name ORDER BY SUM(line.sub_total) DESC LIMIT " + nProducts + ";";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> topPurchasesByVendor(int nPartners, String period, int interval) {

        if (interval < 0) {
            interval = 0;
        }

        if (nPartners < 0) {
            nPartners = 5;
        }

        String query = "SELECT  par.name , SUM(purchase.amount_total), count(*)\n"
                + "FROM `erp`.`purchase_order` purchase join `erp`.`partner` par on purchase.partner_id = par.id WHERE ";

        switch (period) {
            case "Month":
                query += "DATE_FORMAT(purchase.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + interval + " MONTH, '%Y%m') ";
                break;
            case "Quarter":
                query += "concat(YEAR(purchase.date),QUARTER(purchase.date)) = concat(YEAR(CURDATE() - interval " + interval + " QUARTER), QUARTER(CURDATE() - interval " + interval + " QUARTER))";
                break;      
            case "Day":
                query += "DATE(purchase.date) = SUBDATE(CURDATE()," + interval + ") ";
                break;
            default:
                query += "yearweek(purchase.date) = yearweek(CURDATE() - interval " + interval + " week) ";
                break;
        }

        query += "AND par.supplier = 1 GROUP BY par.name ORDER BY SUM(purchase.amount_total) DESC LIMIT " + nPartners + ";";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> topSalesByCustomer(int nPartners, String period, int interval) {

        if (interval < 0) {
            interval = 0;
        }

        if (nPartners < 0) {
            nPartners = 5;
        }
        
        String query = "SELECT  par.name , SUM(sale.amount_total), count(*)\n"
                + "FROM `erp`.`sale_order` sale join `erp`.`partner` par on sale.partner_id = par.id WHERE ";

        switch (period) {
            case "Month":
                query += "DATE_FORMAT(sale.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + interval + " MONTH, '%Y%m') ";
                break;
            case "Quarter":
                query += "concat(YEAR(sale.date),QUARTER(sale.date)) = concat(YEAR(CURDATE() - interval " + interval + " QUARTER), QUARTER(CURDATE() - interval " + interval + " QUARTER))";
                break;    
            case "Day":
                query += "DATE(sale.date) = SUBDATE(CURDATE()," + interval + ") ";
                break;
            default:
                query += "yearweek(sale.date) = yearweek(CURDATE() - interval " + interval + " week) ";
                break;
        }

        query += "AND par.customer = 1 GROUP BY par.name ORDER BY SUM(sale.amount_total) DESC LIMIT " + nPartners + ";";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> newCustomers(int interval, String period) {

        String query = "SELECT ";

        switch (period) {
            case "Day":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE(create_date) = SUBDATE(CURDATE()," + i + ") THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(create_date) = SUBDATE(CURDATE()," + i + ") THEN 1 ELSE 0 END) ";
                    }
                }
                break;
            case "Month":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(create_date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(create_date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN 1 ELSE 0 END) ";
                    }
                }
                break;
            case "Quarter":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(create_date),QUARTER(create_date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN 1 ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(create_date),QUARTER(create_date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN 1 ELSE 0 END) ";
                    }
                }
                break;    
            default:
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN yearweek(create_date) = yearweek(CURDATE() - interval " + i + " week) THEN 1 ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(create_date) = yearweek(CURDATE() - interval " + i + " week) THEN 1 ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `erp`.`partner` WHERE customer = 1;";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> salesOrdersToConfirm() {

        String query = "SELECT count(*), IFNULL(SUM(amount_total),0)\n"
                + "FROM `erp`.`sale_order` \n"
                + "WHERE state = 'Quotation' ; ";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> purchaseOrdersToConfirm() {

        String query = "SELECT count(*), IFNULL(SUM(amount_total),0)\n"
                + "FROM `erp`.`purchase_order` \n"
                + "WHERE state = 'Quotation' ; ";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> invoicesToConfirm() {

        String query = "SELECT count(*), IFNULL(SUM(amount_total),0)\n"
                + "FROM `erp`.`invoice` \n"
                + "WHERE state = 'Draft' AND type='Sale' ; ";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> billsToConfirm() {

        String query = "SELECT count(*), IFNULL(SUM(amount_total),0)\n"
                + "FROM `erp`.`invoice` \n"
                + "WHERE state = 'Draft' AND type='Purchase' ; ";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> topReceivablesByCustomer() {

        String query = "SELECT  par.name , SUM(inv.residual)\n"
                + "FROM `erp`.`invoice` inv join `erp`.`partner` par on inv.partner_id = par.id \n"
                + "WHERE  inv.state = 'Open' AND type='Sale' AND inv.residual > 0 \n"
                + "GROUP BY par.name\n"
                + "ORDER BY SUM(inv.residual) DESC limit 5;";

        Query q = em.createNativeQuery(query);
        return q.getResultList();
    }
    
    public List<Object[]> topPayablesByVendor() {

        String query = "SELECT  par.name , SUM(inv.residual)\n"
                + "FROM `erp`.`invoice` inv join `erp`.`partner` par on inv.partner_id = par.id \n"
                + "WHERE  inv.state = 'Open' AND type='Purchase' AND inv.residual > 0 \n"
                + "GROUP BY par.name\n"
                + "ORDER BY SUM(inv.residual) DESC limit 5;";

        Query q = em.createNativeQuery(query);
        return q.getResultList();
    }

    public List<Object[]> receivables() {

        String query = "SELECT  SUM(inv.residual)\n"
                + "FROM `erp`.`invoice` inv \n"
                + "WHERE  inv.state = 'Open' AND type = 'Sale';";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> payables() {

        String query = "SELECT  SUM(inv.residual)\n"
                + "FROM `erp`.`invoice` inv \n"
                + "WHERE  inv.state = 'Open' AND type = 'Purchase';";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> customerPayment(int interval, String period) {

        String query = "SELECT ";

        switch (period) {
            case "Day":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE(invPay.date) = SUBDATE(CURDATE()," + i + ") THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(invPay.date) = SUBDATE(CURDATE()," + i + ") THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
            case "Month":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(invPay.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(invPay.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
            case "Quarter":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(invPay.date),QUARTER(invPay.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN invPay.paid_amount ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(invPay.date),QUARTER(invPay.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;     
            default:
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN yearweek(invPay.date) = yearweek(CURDATE() - interval " + i + " week) THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(invPay.date) = yearweek(CURDATE() - interval " + i + " week) THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `erp`.`invoice_payment` invPay \n"
                + "join `erp`.`journal_entry` entry on invPay.journal_entry_id = entry.id \n"
                + "join `erp`.`payment` pay on pay.entry_id = entry.id  \n"
                + "join `erp`.`journal` journal on pay.journal_id = journal.id    \n"
                + "where pay.type = 'in' AND pay.partner_type = 'customer' AND invPay.name <> 'Write-Off' \n"
                + "GROUP BY journal.name ORDER BY journal.name ASC;";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

    }

    public List<Object[]> vendorPayment(int interval, String period) {

        String query = "SELECT ";

        switch (period) {
            case "Day":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE(invPay.date) = SUBDATE(CURDATE()," + i + ") THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE(invPay.date) = SUBDATE(CURDATE()," + i + ") THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
            case "Month":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN DATE_FORMAT(invPay.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN DATE_FORMAT(invPay.date, '%Y%m') = DATE_FORMAT(CURDATE() - interval " + i + " MONTH, '%Y%m') THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
            case "Quarter":
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN concat(YEAR(invPay.date),QUARTER(invPay.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN invPay.paid_amount ELSE 0 END),";
                    } else {
                        query += "SUM(CASE WHEN concat(YEAR(invPay.date),QUARTER(invPay.date)) = concat(YEAR(CURDATE() - interval " + i + " QUARTER), QUARTER(CURDATE() - interval " + i + " QUARTER)) THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;    
            default:
                for (int i = 0; i < interval; i++) {
                    if (i < interval - 1) {
                        query += "SUM(CASE WHEN yearweek(invPay.date) = yearweek(CURDATE() - interval " + i + " week) THEN invPay.paid_amount ELSE 0 END), ";
                    } else {
                        query += "SUM(CASE WHEN yearweek(invPay.date) = yearweek(CURDATE() - interval " + i + " week) THEN invPay.paid_amount ELSE 0 END) ";
                    }
                }
                break;
        }

        query += "FROM `erp`.`invoice_payment` invPay \n"
                + "join `erp`.`journal_entry` entry on invPay.journal_entry_id = entry.id \n"
                + "join `erp`.`payment` pay on pay.entry_id = entry.id  \n"
                + "join `erp`.`journal` journal on pay.journal_id = journal.id    \n"
                + "where pay.type = 'out' AND pay.partner_type = 'supplier' AND invPay.name <> 'Write-Off' \n"
                + "GROUP BY journal.name ORDER BY journal.name ASC;";

        Query q = em.createNativeQuery(query);
        return q.getResultList();

        
    }

}
