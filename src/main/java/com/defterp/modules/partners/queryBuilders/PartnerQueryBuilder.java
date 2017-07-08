package com.defterp.modules.partners.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class PartnerQueryBuilder {

    private static final String FIND_PARTNERS = "SELECT p FROM Partner p";
    private static final String FIND_CUSTOMERS = "SELECT p FROM Partner p WHERE p.customer = true";
    private static final String FIND_ACTIVE_CUSTOMERS = "SELECT p FROM Partner p WHERE p.customer = true AND p.active = true";
    private static final String FIND_VENDORS = "SELECT p FROM Partner p WHERE p.supplier = true";
    private static final String FIND_ACTIVE_VENDORS = "SELECT p FROM Partner p WHERE p.supplier = true AND p.active = true";

    public static QueryWrapper getFindPartnersQuery() {

        return new QueryWrapper(FIND_PARTNERS);
    }

    public static QueryWrapper getFindCustomersQuery() {

        return new QueryWrapper(FIND_CUSTOMERS);
    }

    public static QueryWrapper getFindActiveCustomersQuery() {

        return new QueryWrapper(FIND_ACTIVE_CUSTOMERS);
    }

    public static QueryWrapper getFindVendorsQuery() {

        return new QueryWrapper(FIND_VENDORS);
    }

    public static QueryWrapper getFindActiveVendorsQuery() {

        return new QueryWrapper(FIND_ACTIVE_VENDORS);
    }
}
