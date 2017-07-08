package com.defterp.modules.purchases.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class PurchaseOrderQueryBuilder {

    private static final String FIND_ALL = "SELECT p FROM PurchaseOrder p ";
    private static final String FIND_BY_PARTNER = "SELECT p FROM PurchaseOrder p WHERE p.partner.id = :partnerId ";
    private static final String COUNT_BY_PARTNER = "SELECT COUNT(p) FROM PurchaseOrder p WHERE p.partner.id = :partnerId ";

    public static QueryWrapper getFindAllQuery() {

        return new QueryWrapper(FIND_ALL);
    }

    public static QueryWrapper getFindByVendorQuery(Integer vendorId) {

        return new QueryWrapper(FIND_BY_PARTNER)
                .setParameter("partnerId", vendorId);
    }

    public static QueryWrapper getCountByVendorQuery(Integer vendorId) {

        return new QueryWrapper(COUNT_BY_PARTNER)
                .setParameter("partnerId", vendorId);
    }

}
