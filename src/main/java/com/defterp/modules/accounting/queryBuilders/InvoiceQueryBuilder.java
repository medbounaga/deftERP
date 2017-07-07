package com.defterp.modules.accounting.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class InvoiceQueryBuilder {

    private static final String FIND_ALL_INVOICES = "SELECT i FROM Invoice i WHERE i.type = 'Sale Refund' OR i.type = 'Sale'";
    private static final String FIND_ALL_BILLS = "SELECT i FROM Invoice i WHERE i.type = 'Purchase Refund' OR i.type = 'Purchase'";
    private static final String FIND_BY_SALE_ORDER = "SELECT i FROM Invoice i WHERE i.saleOrder.id = :saleId";
    private static final String FIND_BY_PURCHASE_ORDER = "SELECT i FROM Invoice i WHERE i.purchaseOrder.id = :purchaseId";
    private static final String FIND_BY_PARTNER = "SELECT i FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type";
    private static final String INVOICED_SUM_BY_PARTNER = "SELECT SUM(i.amountUntaxed) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type ";
    private static final String TOTAL_DUE_AMOUNT_BY_PARTNER = "SELECT SUM(i.residual) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type ";
    private static final String COUNT_BY_PARTNER = "SELECT COUNT(i) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type ";

    public static QueryWrapper getCountByVendorQuery(Integer vendorId) {

        return new QueryWrapper(COUNT_BY_PARTNER)
                .setParameter("partnerId", vendorId)
                .setParameter("type", "Purchase");
    }

    public static QueryWrapper getCountByCustomerQuery(Integer customerId) {

        return new QueryWrapper(COUNT_BY_PARTNER)
                .setParameter("partnerId", customerId)
                .setParameter("type", "Sale");
    }

    public static QueryWrapper getFindBySaleOrderQuery(Integer saleOrderId) {

        return new QueryWrapper(FIND_BY_SALE_ORDER)
                .setParameter("saleId", saleOrderId);
    }

    public static QueryWrapper getFindByPurchaseOrderQuery(Integer purchaseOrderId) {

        return new QueryWrapper(FIND_BY_PURCHASE_ORDER)
                .setParameter("purchaseId", purchaseOrderId);
    }

    public static QueryWrapper getFindByCustomerQuery(Integer customerId) {

        return new QueryWrapper(FIND_BY_PARTNER)
                .setParameter("partnerId", customerId)
                .setParameter("type", "Sale");
    }

    public static QueryWrapper getFindByVendorQuery(Integer vendorId) {

        return new QueryWrapper(FIND_BY_PARTNER)
                .setParameter("partnerId", vendorId)
                .setParameter("type", "Purchase");
    }

    public static QueryWrapper getInvoiceSumByCustomerQuery(Integer customerId) {

        return new QueryWrapper(INVOICED_SUM_BY_PARTNER)
                .setParameter("partnerId", customerId)
                .setParameter("type", "Sale");
    }

    public static QueryWrapper getInvoiceSumByVendorQuery(Integer vendorId) {
        
        return new QueryWrapper(INVOICED_SUM_BY_PARTNER)
                .setParameter("partnerId", vendorId)
                .setParameter("type", "Purchase");
    }

    public static QueryWrapper getTotalDueAmountByCustomerQuery(Integer customerId) {
        
        return new QueryWrapper(TOTAL_DUE_AMOUNT_BY_PARTNER)
                .setParameter("partnerId", customerId)
                .setParameter("type", "Sale");
    }

    public static QueryWrapper getTotalDueAmountByVendorQuery(Integer vendorId) {
        
        return new QueryWrapper(TOTAL_DUE_AMOUNT_BY_PARTNER)
                .setParameter("partnerId", vendorId)
                .setParameter("type", "Purchase");
    }

    public static QueryWrapper getFindAllInvoicesQuery() {
       
        return new QueryWrapper(FIND_ALL_INVOICES);
    }

    public static QueryWrapper getFindAllBillsQuery() {
        
        return new QueryWrapper(FIND_ALL_BILLS);
    }
}
