package com.defterp.modules.inventory.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class InventoryQueryBuilder {

    private static final String FIND_ALL = "SELECT i FROM Inventory i";

    public static QueryWrapper getFindAllQuery() {

        return new QueryWrapper(FIND_ALL);
    }

}
