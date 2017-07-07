package com.defterp.modules.accounting.queryBuilders;

import com.defterp.modules.commonClasses.QueryWrapper;

public class PaymentQueryBuilder {

    private static final String FIND_BY_PARTNER_TYPE = "SELECT p FROM Payment p WHERE p.partnerType = :partnerType ";
    private static final String FIND_BY_PARTNER = "SELECT p FROM Payment p WHERE p.partnerType = :partnerType AND p.partner.id = :partnerId";
    private static final String FIND_OUTSTANDING_BY_PARTNER = "SELECT p FROM Payment p WHERE p.partner.id = :partnerId AND p.type = :paymentType AND p.state = 'Posted' AND p.partnerType = :partnerType AND p.overpayment > 0";
    private static final String COUNT_BY_PARTNER = "SELECT COUNT(p) FROM Payment p WHERE p.partner.id = :partnerId AND p.partnerType = :partnerType ";

    public static QueryWrapper getFindAllCustomerPaymentsQuery() {

        return new QueryWrapper(FIND_BY_PARTNER_TYPE)
                .setParameter("partnerType", "customer");
    }

    public static QueryWrapper getFindAllVendorPaymentsQuery() {

        return new QueryWrapper(FIND_BY_PARTNER_TYPE)
                .setParameter("partnerType", "supplier");
    }

    public static QueryWrapper getCountByVendorQuery(Integer vendorId) {

        return new QueryWrapper(COUNT_BY_PARTNER)
                .setParameter("partnerId", vendorId)
                .setParameter("partnerType", "supplier");
    }

    public static QueryWrapper getCountByCustomerQuery(Integer customerId) {

        return new QueryWrapper(COUNT_BY_PARTNER)
                .setParameter("partnerId", customerId)
                .setParameter("partnerType", "customer");
    }

    public static QueryWrapper getFindByCustomerQuery(Integer customerId) {

        return new QueryWrapper(FIND_BY_PARTNER)
                .setParameter("partnerId", customerId)
                .setParameter("partnerType", "customer");
    }

    public static QueryWrapper getFindByVendorQuery(Integer vendorId) {

        return new QueryWrapper(FIND_BY_PARTNER)
                .setParameter("partnerId", vendorId)
                .setParameter("partnerType", "supplier");
    }

    public static QueryWrapper getFindOutstandingByCustomerQuery(Integer partnerId) {

        return new QueryWrapper(FIND_OUTSTANDING_BY_PARTNER)
                .setParameter("partnerId", partnerId)
                .setParameter("partnerType", "customer")
                .setParameter("paymentType", "in");
    }

    public static QueryWrapper getFindOutstandingByVendorQuery(Integer partnerId) {

        return new QueryWrapper(FIND_OUTSTANDING_BY_PARTNER)
                .setParameter("partnerId", partnerId)
                .setParameter("partnerType", "supplier")
                .setParameter("paymentType", "out");
    }
}
