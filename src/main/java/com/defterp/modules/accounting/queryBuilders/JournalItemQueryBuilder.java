package com.defterp.modules.accounting.queryBuilders;

import com.defterp.util.QueryWrapper;


public class JournalItemQueryBuilder
{
  private static final String FIND_CREDIT_SUM_BY_ACCOUNT = "SELECT SUM(j.credit) FROM JournalItem j WHERE j.account.id = :accountId ";
  private static final String FIND_DEBIT_SUM_BY_ACCOUNT = "SELECT SUM(j.debit) FROM JournalItem j WHERE j.account.id = :accountId";
  
  public static QueryWrapper getFindCreditSumByAccountQuery(Integer accountId)
  {
    QueryWrapper qw = new QueryWrapper("SELECT SUM(j.credit) FROM JournalItem j WHERE j.account.id = :accountId ").setParameter("accountId", accountId);
    
    return qw;
  }
  
  public static QueryWrapper getFindDebitSumByAccountQuery(Integer accountId)
  {
    QueryWrapper qw = new QueryWrapper("SELECT SUM(j.debit) FROM JournalItem j WHERE j.account.id = :accountId").setParameter("accountId", accountId);
    
    return qw;
  }
}

/* Location:
 * Qualified Name:     JournalItemQueryBuilder
 * Java Class Version: 8 (52.0)
 * JD-Core Version:    0.7.1
 */