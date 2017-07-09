package com.defterp.modules.accounting.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class JournalEntryQueryBuilder {

    private static final String FIND_ALL = "SELECT j FROM JournalEntry j";

    public static QueryWrapper getFindAllQuery() {

        return new QueryWrapper(FIND_ALL);
    }

}
