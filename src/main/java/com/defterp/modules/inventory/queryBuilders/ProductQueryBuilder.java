package com.defterp.modules.inventory.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class ProductQueryBuilder {

    private static final String FIND_ALL = "SELECT p FROM Product p";
    private static final String FIND_SOLD_PRODUCTS = "SELECT p FROM Product p WHERE p.saleOk = true";
    private static final String FIND_ACTIVE_SOLD_PRODUCTS = "SELECT p FROM Product p WHERE p.saleOk = true AND p.active = true";
    private static final String FIND_PURCHASED_PRODUCTS = "SELECT p FROM Product p WHERE p.purchaseOk = true";
    private static final String FIND_ACTIVE_PURCHASED_PRODUCTS = "SELECT p FROM Product p WHERE p.purchaseOk = true AND p.active = true";

    public static QueryWrapper getFindAllQuery() {

        return new QueryWrapper(FIND_ALL);
    }

    public static QueryWrapper getFindSoldProductsQuery() {

        return new QueryWrapper(FIND_SOLD_PRODUCTS);
    }

    public static QueryWrapper getFindActiveSoldProductsQuery() {

        return new QueryWrapper(FIND_ACTIVE_SOLD_PRODUCTS);
    }

    public static QueryWrapper getFindPurchasedProductsQuery() {

        return new QueryWrapper(FIND_PURCHASED_PRODUCTS);
    }

    public static QueryWrapper getFindActivePurchasedProductsQuery() {

        return new QueryWrapper(FIND_ACTIVE_PURCHASED_PRODUCTS);
    }
}
