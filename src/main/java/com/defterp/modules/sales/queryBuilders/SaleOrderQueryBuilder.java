package com.defterp.modules.sales.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class SaleOrderQueryBuilder {

    private static final String FIND_ALL = "SELECT s FROM SaleOrder s ";
    private static final String COUNT_BY_PARTNER = "SELECT COUNT(s) FROM SaleOrder s WHERE s.partner.id = :partnerId ";
    private static final String FIND_BY_PARTNER = "SELECT s FROM SaleOrder s WHERE s.partner.id = :partnerId ";

    public static QueryWrapper getFindAllQuery() {

        return new QueryWrapper(FIND_ALL);
    }

    public static QueryWrapper getFindByCustomerQuery(Integer customerId) {

        return new QueryWrapper(FIND_BY_PARTNER)
                .setParameter("partnerId", customerId);
    }

    public static QueryWrapper getCountByCustomerQuery(Integer customerId) {

        return new QueryWrapper(COUNT_BY_PARTNER)
                .setParameter("partnerId", customerId);
    }

}
