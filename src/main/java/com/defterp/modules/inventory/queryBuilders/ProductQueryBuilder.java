package com.defterp.modules.inventory.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class ProductQueryBuilder
{
  private static final String FIND_ALL_PRODUCTS = "SELECT p FROM Product p";
  private static final String FIND_SOLD_PRODUCTS = "SELECT p FROM Product p WHERE p.saleOk = true";
  private static final String FIND_ACTIVE_SOLD_PRODUCTS = "SELECT p FROM Product p WHERE p.saleOk = true AND p.active = true";
  private static final String FIND_PURCHASED_PRODUCTS = "SELECT p FROM Product p WHERE p.purchaseOk = true";
  private static final String FIND_ACTIVE_PURCHASED_PRODUCTS = "SELECT p FROM Product p WHERE p.purchaseOk = true AND p.active = true";
  
  public static QueryWrapper getFindAllProductsQuery()
  {
    QueryWrapper query = new QueryWrapper("SELECT p FROM Product p");
    
    return query;
  }
  
  public static QueryWrapper getFindSoldProductsQuery()
  {
    QueryWrapper query = new QueryWrapper("SELECT p FROM Product p WHERE p.saleOk = true");
    
    return query;
  }
  
  public static QueryWrapper getFindActiveSoldProductsQuery()
  {
    QueryWrapper query = new QueryWrapper("SELECT p FROM Product p WHERE p.saleOk = true AND p.active = true");
    
    return query;
  }
  
  public static QueryWrapper getFindPurchasedProductsQuery()
  {
    QueryWrapper query = new QueryWrapper("SELECT p FROM Product p WHERE p.purchaseOk = true");
    
    return query;
  }
  
  public static QueryWrapper getFindActivePurchasedProductsQuery()
  {
    QueryWrapper query = new QueryWrapper("SELECT p FROM Product p WHERE p.purchaseOk = true AND p.active = true");
    
    return query;
  }
}

/* Location:
 * Qualified Name:     ProductQueryBuilder
 * Java Class Version: 8 (52.0)
 * JD-Core Version:    0.7.1
 */