package com.defterp.modules.accounting.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class TaxQueryBuilder {

    private static final String FIND_ALL = "SELECT t FROM Tax t";
    private static final String FIND_BY_TYPE = "SELECT t FROM Tax t WHERE t.typeTaxUse = :taxType";

    public static QueryWrapper getFindAllQuery() {

        return new QueryWrapper(FIND_ALL);
    }

    public static QueryWrapper getFindPurchaseTaxesQuery() {

        return new QueryWrapper(FIND_BY_TYPE)
                .setParameter("taxType", "Purchase");
    }

    public static QueryWrapper getFindSaleTaxesQuery() {

        return new QueryWrapper(FIND_BY_TYPE)
                .setParameter("taxType", "Sale");
    }

}
