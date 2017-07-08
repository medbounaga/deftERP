package com.defterp.modules.inventory.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class ProductUomCategoryQueryBuilder {

    private static final String FIND_ALL = "SELECT p FROM ProductUomCategory p";

    public static QueryWrapper getFindAllQuery() {

        return new QueryWrapper(FIND_ALL);
    }

}
