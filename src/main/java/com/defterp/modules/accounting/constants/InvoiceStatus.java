package com.defterp.modules.accounting.constants;

public enum InvoiceStatus {

    DRAFT("Draft"),
    OPEN("Open"),
    CANCELLED("Cancelled"),
    PAID("Paid");

    private final String status;

    InvoiceStatus(String status) {
        this.status = status;
    }

    public String value() {
        return status;
    }
}
