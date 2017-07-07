package com.defterp.modules.accounting.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class JournalQueryBuilder {

    private static final String FIND_ALL = "SELECT j FROM Journal j";
    private static final String FIND_BY_CODE = "SELECT j FROM Journal j WHERE j.code = :code";
    private static final String FIND_BY_TYPE = "SELECT j FROM Journal j WHERE j.type = :type";
    private static final String FIND_BY_NAME = "SELECT j FROM Journal j WHERE j.name = :name";

    public static QueryWrapper getFindAllQuery() {

        return new QueryWrapper(FIND_ALL);
    }

    public static QueryWrapper getFindByCodeQuery(String journalCode) {

        return new QueryWrapper(FIND_BY_CODE)
                .setParameter("code", journalCode);
    }

    public static QueryWrapper getFindByNameQuery(String journalName) {

        return new QueryWrapper(FIND_BY_NAME)
                .setParameter("name", journalName);
    }

    public static QueryWrapper getFindByTypeQuery(String journalType) {

        return new QueryWrapper(FIND_BY_TYPE)
                .setParameter("type", journalType);
    }
}
