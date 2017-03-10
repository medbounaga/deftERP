package com.defterp.modules.accounting.queryBuilders;

import com.defterp.util.QueryWrapper;


public class PaymentQueryBuilder
{
  private static final String FIND_OUTSTANDING_BY_CUSTOMER = "SELECT p FROM Payment p WHERE p.partner.id = :partnerId AND p.type = 'in' AND p.state = 'Posted' AND p.partnerType = 'customer' AND p.overpayment > 0";
  private static final String FIND_OUTSTANDING_BY_VENDOR = "SELECT p FROM Payment p WHERE p.partner.id = :partnerId AND p.type = 'out' AND p.state = 'Posted' AND p.partnerType = 'supplier' AND p.overpayment > 0";
  
  public static QueryWrapper getFindOutstandingByCustomer(Integer partnerId)
  {
    QueryWrapper qw = new QueryWrapper("SELECT p FROM Payment p WHERE p.partner.id = :partnerId AND p.type = 'in' AND p.state = 'Posted' AND p.partnerType = 'customer' AND p.overpayment > 0").setParameter("partnerId", partnerId);
    
    return qw;
  }
  
  public static QueryWrapper getFindOutstandingByVendor(Integer partnerId)
  {
    QueryWrapper qw = new QueryWrapper("SELECT p FROM Payment p WHERE p.partner.id = :partnerId AND p.type = 'out' AND p.state = 'Posted' AND p.partnerType = 'supplier' AND p.overpayment > 0").setParameter("partnerId", partnerId);
    
    return qw;
  }
}

/* Location:
 * Qualified Name:     PaymentQueryBuilder
 * Java Class Version: 8 (52.0)
 * JD-Core Version:    0.7.1
 */