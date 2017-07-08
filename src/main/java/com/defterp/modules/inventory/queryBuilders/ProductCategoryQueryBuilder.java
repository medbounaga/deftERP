package com.defterp.modules.inventory.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class ProductCategoryQueryBuilder {

    private static final String FIND_ALL = "SELECT p FROM ProductCategory p";

    public static QueryWrapper getFindAllQuery() {

        return new QueryWrapper(FIND_ALL);
    }
}
