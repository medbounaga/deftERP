package com.defterp.modules.accounting.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class JournalItemQueryBuilder {

    private static final String FIND_CREDIT_SUM_BY_ACCOUNT = "SELECT SUM(j.credit) FROM JournalItem j WHERE j.account.id = :accountId ";
    private static final String FIND_DEBIT_SUM_BY_ACCOUNT = "SELECT SUM(j.debit) FROM JournalItem j WHERE j.account.id = :accountId";

    public static QueryWrapper getFindCreditSumByAccountQuery(Integer accountId) {

        return new QueryWrapper(FIND_CREDIT_SUM_BY_ACCOUNT)
                   .setParameter("accountId", accountId);
    }

    public static QueryWrapper getFindDebitSumByAccountQuery(Integer accountId) {

        return new QueryWrapper(FIND_DEBIT_SUM_BY_ACCOUNT)
                   .setParameter("accountId", accountId);
    }
}
