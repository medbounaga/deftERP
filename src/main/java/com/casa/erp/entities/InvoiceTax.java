
package com.casa.erp.entities;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Entity
@Table(name = "invoice_tax")
@NamedQueries({
    @NamedQuery(name = "InvoiceTax.findAll", query = "SELECT i FROM InvoiceTax i"),
    @NamedQuery(name = "InvoiceTax.findById", query = "SELECT i FROM InvoiceTax i WHERE i.id = :id"),
    @NamedQuery(name = "InvoiceTax.findByDate", query = "SELECT i FROM InvoiceTax i WHERE i.date = :date"),
    @NamedQuery(name = "InvoiceTax.findByTaxAmount", query = "SELECT i FROM InvoiceTax i WHERE i.taxAmount = :taxAmount"),
    @NamedQuery(name = "InvoiceTax.findByName", query = "SELECT i FROM InvoiceTax i WHERE i.name = :name"),
    @NamedQuery(name = "InvoiceTax.findByBaseAmount", query = "SELECT i FROM InvoiceTax i WHERE i.baseAmount = :baseAmount"),
    @NamedQuery(name = "InvoiceTax.findByActive", query = "SELECT i FROM InvoiceTax i WHERE i.active = :active")})

public class InvoiceTax extends BaseEntity {
    
    private static final long serialVersionUID = 1L;

    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;   
    @Column(name = "tax_amount")
    private Double taxAmount;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @Column(name = "base_amount")
    private Double baseAmount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @ManyToOne
    private Account account;
    @JoinColumn(name = "invoice_id", referencedColumnName = "id")
    @ManyToOne
    private Invoice invoice;
    @JoinColumn(name = "tax_id", referencedColumnName = "id")
    @ManyToOne
    private Tax tax;

    public InvoiceTax() {
    }

    public InvoiceTax(Date date, Double taxAmount, Double baseAmount, Boolean active, Account account, Invoice invoice, Tax tax) {
        this.date = date;
        this.taxAmount = taxAmount;
        this.baseAmount = baseAmount;
        this.active = active;
        this.account = account;
        this.invoice = invoice;
        this.tax = tax;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(Double baseAmount) {
        this.baseAmount = baseAmount;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    
    @Override
    public String toString() {
        return "--- InvoiceTax[ id=" + super.getId() + " ] ---";
    }
    
}
