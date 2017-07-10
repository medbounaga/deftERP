package com.defterp.modules.purchases.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class PurchaseOrderLineQueryBuilder {

    private static final String FIND_BY_PRODUCT = "SELECT p FROM PurchaseOrderLine p WHERE p.product.id = :productId";
    private static final String TOTAL_PRODUCT_PURCHASED_QUANTITY = "SELECT SUM(p.quantity) FROM PurchaseOrderLine p WHERE p.product.id = :productId ";

    public static QueryWrapper getFindByProductQuery(Integer productId) {

        return new QueryWrapper(FIND_BY_PRODUCT)
                .setParameter("productId", productId);
    }

    public static QueryWrapper getTotalProductPurchasedQuantityQuery(Integer productId) {

        return new QueryWrapper(TOTAL_PRODUCT_PURCHASED_QUANTITY)
                .setParameter("productId", productId);
    }

}
