package com.defterp.modules.inventory.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class ProductUomQueryBuilder {

    private static final String FIND_ALL = "SELECT p FROM ProductUom p";
    private static final String FIND_ACTIVE_PRODUCT_UOMS = "SELECT p FROM ProductUom p WHERE p.active = true";

    public static QueryWrapper getFindAllQuery() {

        return new QueryWrapper(FIND_ALL);
    }

    public static QueryWrapper getFindActiveProductUomsQuery() {

        return new QueryWrapper(FIND_ACTIVE_PRODUCT_UOMS);
    }

}
