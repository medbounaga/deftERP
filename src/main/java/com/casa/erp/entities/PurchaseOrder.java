package com.casa.erp.entities;

import com.casa.erp.validation.InDateRange;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
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
@Table(name = "purchase_order")
@NamedQueries({
    @NamedQuery(name = "PurchaseOrder.findByPartner", query = "SELECT p FROM PurchaseOrder p WHERE p.partner.id = :partnerId "),
    @NamedQuery(name = "PurchaseOrder.countByPartner", query = "SELECT COUNT(p) FROM PurchaseOrder p WHERE p.partner.id = :partnerId "),
    @NamedQuery(name = "PurchaseOrder.findDrafts", query = "SELECT p FROM PurchaseOrder p WHERE p.state = :draft OR p.state = :cancelled "),
    @NamedQuery(name = "PurchaseOrder.findConfirmed", query = "SELECT p FROM PurchaseOrder p WHERE p.state <> :draft AND p.state <> :cancelled "),
    @NamedQuery(name = "PurchaseOrder.findAll", query = "SELECT p FROM PurchaseOrder p"),
    @NamedQuery(name = "PurchaseOrder.findById", query = "SELECT p FROM PurchaseOrder p WHERE p.id = :id"),
    @NamedQuery(name = "PurchaseOrder.findByDate", query = "SELECT p FROM PurchaseOrder p WHERE p.date = :date"),
    @NamedQuery(name = "PurchaseOrder.findByAmountTax", query = "SELECT p FROM PurchaseOrder p WHERE p.amountTax = :amountTax"),
    @NamedQuery(name = "PurchaseOrder.findByAmountTotal", query = "SELECT p FROM PurchaseOrder p WHERE p.amountTotal = :amountTotal"),
    @NamedQuery(name = "PurchaseOrder.findByAmountUntaxed", query = "SELECT p FROM PurchaseOrder p WHERE p.amountUntaxed = :amountUntaxed"),
    @NamedQuery(name = "PurchaseOrder.findByShipped", query = "SELECT p FROM PurchaseOrder p WHERE p.shipped = :shipped"),
    @NamedQuery(name = "PurchaseOrder.findByDiscount", query = "SELECT p FROM PurchaseOrder p WHERE p.discount = :discount"),
    @NamedQuery(name = "PurchaseOrder.findByActive", query = "SELECT p FROM PurchaseOrder p WHERE p.active = :active"),
    @NamedQuery(name = "PurchaseOrder.findByName", query = "SELECT p FROM PurchaseOrder p WHERE p.name = :name")})

public class PurchaseOrder extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "date")
    @InDateRange
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Column(name = "amount_tax")
    private Double amountTax = 0d;
    @Column(name = "amount_total")
    private Double amountTotal = 0d;
    @Column(name = "amount_untaxed")
    private Double amountUntaxed = 0d;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "state")
    private String state;
    @Column(name = "shipped")
    private Boolean shipped = false;
    @Column(name = "delivery_created")
    private Boolean deliveryCreated = false;
    @Column(name = "paid")
    private Boolean paid = false;
    @Column(name = "unpaid")
    private Double unpaid = 0d;
    @Lob
    @Size(max = 2147483647)
    @Column(name = "notes")
    private String notes;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "invoice_method")
    private String invoiceMethod = "Partial";
    @Column(name = "discount")
    private Integer discount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active = true;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "reference")
    private String reference;
    
    @JoinColumn(name = "partner_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Partner partner;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "purchaseOrder", orphanRemoval=true)
    private List<PurchaseOrderLine> purchaseOrderLines;
    @OneToMany(mappedBy = "purchaseOrder")
    private List<DeliveryOrder> deliveryOrders;
    @OneToMany(mappedBy = "purchaseOrder")
    private List<Invoice> invoices;

    public PurchaseOrder() {
    }


    public PurchaseOrder(Date date, Boolean active) {
        this.date = date;
        this.active = active;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getAmountTax() {
        return amountTax;
    }

    public void setAmountTax(Double amountTax) {
        this.amountTax = amountTax;
    }

    public Double getAmountTotal() {
        return amountTotal;
    }

    public void setAmountTotal(Double amountTotal) {
        this.amountTotal = amountTotal;
    }

    public Double getAmountUntaxed() {
        return amountUntaxed;
    }

    public void setAmountUntaxed(Double amountUntaxed) {
        this.amountUntaxed = amountUntaxed;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean getShipped() {
        return shipped;
    }

    public void setShipped(Boolean shipped) {
        this.shipped = shipped;
    }

    public Boolean getPaid() {
        return paid;
    }

    public void setPaid(Boolean paid) {
        this.paid = paid;
    }

    public Double getUnpaid() {
        return unpaid;
    }

    public void setUnpaid(Double unpaid) {
        this.unpaid = unpaid;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getInvoiceMethod() {
        return invoiceMethod;
    }

    public void setInvoiceMethod(String invoiceMethod) {
        this.invoiceMethod = invoiceMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    
    public List<DeliveryOrder> getDeliveryOrders() {
        return deliveryOrders;
    }

    public void setDeliveryOrders(List<DeliveryOrder> deliveryOrders) {
        this.deliveryOrders = deliveryOrders;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }
    
    public Boolean getDeliveryCreated() {
        return deliveryCreated;
    }

    public void setDeliveryCreated(Boolean deliveryCreated) {
        this.deliveryCreated = deliveryCreated;
    }

    
    public List<PurchaseOrderLine> getPurchaseOrderLines() {
        return purchaseOrderLines;
    }

    public void setPurchaseOrderLines(List<PurchaseOrderLine> purchaseOrderLines) {
        this.purchaseOrderLines= purchaseOrderLines;
    }

    
    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }


    @Override
    public String toString() {
        return "---- PurchaseOrder[ id=" + super.getId() + " ] ----";
    }
    
}
