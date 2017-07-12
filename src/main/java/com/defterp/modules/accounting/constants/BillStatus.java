package com.defterp.modules.accounting.constants;

public enum BillStatus {

    DRAFT("Draft"),
    OPEN("Open"),
    CANCELLED("Cancelled"),
    PAID("Paid");

    private final String status;

    BillStatus(String status) {
        this.status = status;
    }

    public String value() {
        return status;
    }
}
