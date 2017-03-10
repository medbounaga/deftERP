package com.defterp.modules.partners.queryBuilders;

import com.defterp.util.QueryWrapper;


public class PartnerQueryBuilder
{
  private static final String FIND_PARTNERS = "SELECT p FROM Partner p";
  private static final String FIND_ACTIVE_CUSTOMERS = "SELECT p FROM Partner p WHERE p.customer = true AND p.active = true";
  private static final String FIND_CUSTOMERS = "SELECT p FROM Partner p WHERE p.customer = true";
  private static final String FIND_ACTIVE_VENDORS = "SELECT p FROM Partner p WHERE p.supplier = true AND p.active = true";
  private static final String FIND_VENDORS = "SELECT p FROM Partner p WHERE p.supplier = true";
  private static QueryWrapper query;
  
  public static QueryWrapper getFindPartnersQuery()
  {
    query = new QueryWrapper("SELECT p FROM Partner p");
    
    return query;
  }
  
  public static QueryWrapper getFindCustomersQuery()
  {
    query = new QueryWrapper("SELECT p FROM Partner p WHERE p.customer = true");
    
    return query;
  }
  
  public static QueryWrapper getFindActiveCustomersQuery()
  {
    query = new QueryWrapper("SELECT p FROM Partner p WHERE p.customer = true AND p.active = true");
    
    return query;
  }
  
  public static QueryWrapper getFindVendorsQuery()
  {
    query = new QueryWrapper("SELECT p FROM Partner p WHERE p.supplier = true");
    
    return query;
  }
  
  public static QueryWrapper getFindActiveVendorsQuery()
  {
    query = new QueryWrapper("SELECT p FROM Partner p WHERE p.supplier = true AND p.active = true");
    
    return query;
  }
}

/* Location:
 * Qualified Name:     PartnerQueryBuilder
 * Java Class Version: 8 (52.0)
 * JD-Core Version:    0.7.1
 */