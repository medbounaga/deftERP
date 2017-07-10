package com.defterp.modules.sales.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class SaleOrderLineQueryBuilder {

    private static final String FIND_BY_PRODUCT = "SELECT s FROM SaleOrderLine s WHERE s.product.id = :productId";
    private static final String TOTAL_PRODUCT_SOLD_QUANTITY = "SELECT SUM(s.quantity) FROM SaleOrderLine s WHERE s.product.id = :productId ";

    public static QueryWrapper getFindByProductQuery(Integer productId) {

        return new QueryWrapper(FIND_BY_PRODUCT)
                .setParameter("productId", productId);
    }

    public static QueryWrapper getTotalProductSoldQuantityQuery(Integer productId) {

        return new QueryWrapper(TOTAL_PRODUCT_SOLD_QUANTITY)
                .setParameter("productId", productId);
    }

}
