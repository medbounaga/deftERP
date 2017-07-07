package com.defterp.modules.accounting.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class InvoiceQueryBuilder
{
  private static final String FIND_ALL_INVOICES = "SELECT i FROM Invoice i WHERE i.type = 'Sale Refund' OR i.type = 'Sale'";
  private static final String FIND_ALL_BILLS = "SELECT i FROM Invoice i WHERE i.type = 'Purchase Refund' OR i.type = 'Purchase'";
  private static final String FIND_BY_SALE_ORDER = "SELECT i FROM Invoice i WHERE i.saleOrder.id = :saleId";
  private static final String FIND_BY_PARTNER = "SELECT i FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type";
  private static final String INVOICED_SUM_BY_PARTNER = "SELECT SUM(i.amountUntaxed) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type ";
  private static final String TOTAL_DUE_AMOUNT_BY_PARTNER = "SELECT SUM(i.residual) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type ";
  
  public static QueryWrapper getFindBySaleOrderQuery(Integer saleOrderId)
  {
    QueryWrapper qw = new QueryWrapper("SELECT i FROM Invoice i WHERE i.saleOrder.id = :saleId").setParameter("saleId", saleOrderId);
    
    return qw;
  }
  
  public static QueryWrapper getFindByCustomerQuery(Integer partnerId)
  {
    QueryWrapper qw = new QueryWrapper("SELECT i FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type").setParameter("partnerId", partnerId).setParameter("type", "Sale");
    
    return qw;
  }
  
  public static QueryWrapper getFindByVendorQuery(Integer partnerId)
  {
    QueryWrapper qw = new QueryWrapper("SELECT i FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type").setParameter("partnerId", partnerId).setParameter("type", "Purchase");
    
    return qw;
  }
  
  public static QueryWrapper getInvoiceSumByCustomerQuery(Integer partnerId)
  {
    QueryWrapper qw = new QueryWrapper("SELECT SUM(i.amountUntaxed) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type ").setParameter("partnerId", partnerId).setParameter("type", "Sale");
    
    return qw;
  }
  
  public static QueryWrapper getTotalDueAmountByCustomerQuery(Integer partnerId)
  {
    QueryWrapper qw = new QueryWrapper("SELECT SUM(i.residual) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type ").setParameter("partnerId", partnerId).setParameter("type", "Sale");
    
    return qw;
  }
  
  public static QueryWrapper getInvoiceSumByVendorQuery(Integer partnerId)
  {
    QueryWrapper qw = new QueryWrapper("SELECT SUM(i.amountUntaxed) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type ").setParameter("partnerId", partnerId).setParameter("type", "Purchase");
    
    return qw;
  }
  
  public static QueryWrapper getTotalDueAmountByVendorQuery(Integer partnerId)
  {
    QueryWrapper qw = new QueryWrapper("SELECT SUM(i.residual) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type ").setParameter("partnerId", partnerId).setParameter("type", "Purchase");
    
    return qw;
  }
  
  public static QueryWrapper getFindAllInvoicesQuery()
  {
    QueryWrapper qw = new QueryWrapper("SELECT i FROM Invoice i WHERE i.type = 'Sale Refund' OR i.type = 'Sale'");
    
    return qw;
  }
  
  public static QueryWrapper getFindAllBillsQuery()
  {
    QueryWrapper qw = new QueryWrapper("SELECT i FROM Invoice i WHERE i.type = 'Purchase Refund' OR i.type = 'Purchase'");
    
    return qw;
  }
}

/* Location:
 * Qualified Name:     InvoiceQueryBuilder
 * Java Class Version: 8 (52.0)
 * JD-Core Version:    0.7.1
 */