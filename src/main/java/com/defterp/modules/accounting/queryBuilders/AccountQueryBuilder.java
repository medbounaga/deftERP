package com.defterp.modules.accounting.queryBuilders;

import com.defterp.util.QueryWrapper;


public class AccountQueryBuilder{
    
  private static final String FIND_BY_NAME = "SELECT a FROM Account a WHERE a.title = :name";
  private static final String FIND_BY_TYPE = "SELECT a FROM Account a WHERE a.type = :type ";
  private static final String FIND_ALL_ACCOUNTS = "SELECT a FROM Account a";
  
  public static QueryWrapper getFindByNameQuery(String accountName){
    QueryWrapper qw = new QueryWrapper(FIND_BY_NAME)
                          .setParameter("name", accountName);
    
    return qw;
  }
  
  public static QueryWrapper getFindByTypeQuery(String accountType){
    QueryWrapper qw = new QueryWrapper(FIND_BY_TYPE)
                          .setParameter("type", accountType);
    
    return qw;
  }
  
  public static QueryWrapper getFindAllAccountsQuery(){
    QueryWrapper qw = new QueryWrapper(FIND_ALL_ACCOUNTS);
    
    return qw;
  }
}
