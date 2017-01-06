
package com.casa.erp.entities;

import com.casa.erp.validation.StrictlyPositiveNumber;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Entity
@Table(name = "tax")
@NamedQueries({
    @NamedQuery(name = "Tax.findByType", query = "SELECT t FROM Tax t WHERE t.typeTaxUse = :typeTaxUse"),
    @NamedQuery(name = "Tax.findAll", query = "SELECT t FROM Tax t"),
    @NamedQuery(name = "Tax.findById", query = "SELECT t FROM Tax t WHERE t.id = :id"),
    @NamedQuery(name = "Tax.findByName", query = "SELECT t FROM Tax t WHERE t.name = :name"),
    @NamedQuery(name = "Tax.findByAmount", query = "SELECT t FROM Tax t WHERE t.amount = :amount"),
    @NamedQuery(name = "Tax.findByActive", query = "SELECT t FROM Tax t WHERE t.active = :active")})

public class Tax extends BaseEntity {
    
    private static final long serialVersionUID = 1L;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @Basic(optional = false)
    @NotNull
    @Column(name = "amount")
    @StrictlyPositiveNumber(message = "{PositiveTaxAmount}")
    private Double amount;
    @Basic(optional = false)
    @NotNull
    @Max(value=100, message="{MaxTaxAmount}") 
    @StrictlyPositiveNumber(message = "{PositiveTaxAmount}")
    @Column(name = "percent")
    private Double percent;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 40, message = "{LongString}")
    @Column(name = "type_tax_use")
    private String typeTaxUse;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @OneToMany(mappedBy = "tax")
    private List<InvoiceLine> invoiceLines;
    @OneToMany(mappedBy = "tax")
    private List<PurchaseOrderLine> purchaseOrderLines;
    @OneToMany(mappedBy = "tax")
    private List<InvoiceTax> invoiceTaxes;
    @OneToMany(mappedBy = "tax")
    private List<SaleOrderLine> saleOrderLines;
    @OneToMany(mappedBy = "tax")
    private List<JournalItem> journalItems;

    public Tax() {
    }

    public Tax(String name, double amount, String typeTaxUse, Boolean active) {
        this.name = name;
        this.amount = amount;
        this.typeTaxUse = typeTaxUse;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    public String getTypeTaxUse() {
        return typeTaxUse;
    }

    public void setTypeTaxUse(String typeTaxUse) {
        this.typeTaxUse = typeTaxUse;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<InvoiceLine> getInvoiceLines() {
        return invoiceLines;
    }

    public void setInvoiceLines(List<InvoiceLine> invoiceLines) {
        this.invoiceLines = invoiceLines;
    }

    public List<PurchaseOrderLine> getPurchaseOrderLines() {
        return purchaseOrderLines;
    }

    public void setPurchaseOrderLines(List<PurchaseOrderLine> purchaseOrderLines) {
        this.purchaseOrderLines = purchaseOrderLines;
    }

    public List<InvoiceTax> getInvoiceTaxes() {
        return invoiceTaxes;
    }

    public void setInvoiceTaxes(List<InvoiceTax> invoiceTaxes) {
        this.invoiceTaxes = invoiceTaxes;
    }

    public List<SaleOrderLine> getSaleOrderLines() {
        return saleOrderLines;
    }

    public void setSaleOrderLines(List<SaleOrderLine> saleOrderLines) {
        this.saleOrderLines = saleOrderLines;
    }

    public List<JournalItem> getJournalItems() {
        return journalItems;
    }

    public void setJournalItems(List<JournalItem> journalItems) {
        this.journalItems = journalItems;
    }
   

    @Override
    public String toString() {
        return "--- Tax[ id=" + super.getId() + " ] ---";
    }
    
}
