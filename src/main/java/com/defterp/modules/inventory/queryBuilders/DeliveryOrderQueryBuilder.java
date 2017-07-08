package com.defterp.modules.inventory.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class DeliveryOrderQueryBuilder {

    private static final String FIND_BY_TYPE = "SELECT d FROM DeliveryOrder d WHERE d.type = :orderType";
    private static final String FIND_BY_BACKORDER = "SELECT d FROM DeliveryOrder d WHERE d.backOrder.id = :backOrderId";
    private static final String COUNT_BY_BACKORDER = "SELECT COUNT(d) FROM DeliveryOrder d WHERE d.backOrder.id = :backOrderId ";
    private static final String FIND_BY_PARTNER = "SELECT d FROM DeliveryOrder d WHERE d.partner.id = :partnerId AND d.type = :orderType ";
    private static final String COUNT_BY_PARTNER = "SELECT COUNT(d) FROM DeliveryOrder d WHERE d.partner.id = :partnerId AND d.type = :orderType ";
    private static final String FIND_BY_SALE_ORDER = "SELECT d FROM DeliveryOrder d WHERE d.saleOrder.id = :saleOrderId";
    private static final String FIND_BY_PURCHASE_ORDER = "SELECT d FROM DeliveryOrder d WHERE d.purchaseOrder.id = :purchaseOrderId";

    public static QueryWrapper getFindDeliveryOrdersQuery() {

        return new QueryWrapper(FIND_BY_TYPE)
                .setParameter("orderType", "Sale");
    }

    public static QueryWrapper getFindProductReceiptsQuery() {

        return new QueryWrapper(FIND_BY_TYPE)
                .setParameter("orderType", "Purchase");
    }

    public static QueryWrapper getFindByBackOrderQuery(Integer backOrderId) {

        return new QueryWrapper(FIND_BY_BACKORDER)
                .setParameter("backOrderId", backOrderId);
    }

    public static QueryWrapper getCountByBackOrderQuery(Integer backOrderId) {

        return new QueryWrapper(COUNT_BY_BACKORDER)
                .setParameter("backOrderId", backOrderId);
    }

    public static QueryWrapper getFindByCustomerQuery(Integer customerId) {

        return new QueryWrapper(FIND_BY_PARTNER)
                .setParameter("partnerId", customerId)
                .setParameter("orderType", "Sale");
    }

    public static QueryWrapper getCountByCustomerQuery(Integer customerId) {

        return new QueryWrapper(COUNT_BY_PARTNER)
                .setParameter("partnerId", customerId)
                .setParameter("orderType", "Sale");
    }

    public static QueryWrapper getFindByVendorQuery(Integer vendorId) {

        return new QueryWrapper(FIND_BY_PARTNER)
                .setParameter("partnerId", vendorId)
                .setParameter("orderType", "Purchase");
    }

    public static QueryWrapper getCountByVendorQuery(Integer vendorId) {

        return new QueryWrapper(COUNT_BY_PARTNER)
                .setParameter("partnerId", vendorId)
                .setParameter("orderType", "Purchase");
    }

    public static QueryWrapper getFindBySaleOrderQuery(Integer saleOrderId) {

        return new QueryWrapper(FIND_BY_SALE_ORDER)
                .setParameter("saleOrderId", saleOrderId);
    }

    public static QueryWrapper getFindByPurchaseOrderQuery(Integer purchaseOrderId) {

        return new QueryWrapper(FIND_BY_PURCHASE_ORDER)
                .setParameter("purchaseOrderId", purchaseOrderId);
    }

}
