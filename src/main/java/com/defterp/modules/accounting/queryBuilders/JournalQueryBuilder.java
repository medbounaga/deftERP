package com.defterp.modules.accounting.queryBuilders;

import com.defterp.util.QueryWrapper;


public class JournalQueryBuilder
{
  private static final String FIND_ALL_JOURNALS = "SELECT j FROM Journal j";
  private static final String FIND_JOURNAL_BY_CODE = "SELECT j FROM Journal j WHERE j.code = :code";
  
  public static QueryWrapper getFindJournalByCodeQuery(String journalCode)
  {
    QueryWrapper query = new QueryWrapper("SELECT j FROM Journal j WHERE j.code = :code").setParameter("code", journalCode);
    
    return query;
  }
  
  public static QueryWrapper getFindAllJournalsQuery()
  {
    QueryWrapper query = new QueryWrapper("SELECT j FROM Journal j");
    
    return query;
  }
}

/* Location:
 * Qualified Name:     JournalQueryBuilder
 * Java Class Version: 8 (52.0)
 * JD-Core Version:    0.7.1
 */