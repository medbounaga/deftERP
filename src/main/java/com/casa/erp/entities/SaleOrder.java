
package com.casa.erp.entities;


import com.casa.erp.validation.InDateRange;
import java.util.List;
import java.util.Date;
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
@Table(name = "sale_order")
@NamedQueries({
    
    @NamedQuery(name = "SaleOrder.findByPartner", query = "SELECT s FROM SaleOrder s WHERE s.partner.id = :partnerId "),
    @NamedQuery(name = "SaleOrder.countByPartner", query = "SELECT COUNT(s) FROM SaleOrder s WHERE s.partner.id = :partnerId "),
    @NamedQuery(name = "SaleOrder.findDrafts", query = "SELECT s FROM SaleOrder s WHERE s.state = :draft OR s.state = :cancelled "),
    @NamedQuery(name = "SaleOrder.findConfirmed", query = "SELECT s FROM SaleOrder s WHERE s.state <> :draft AND s.state <> :cancelled "),
    @NamedQuery(name = "SaleOrder.findAll", query = "SELECT s FROM SaleOrder s"),
    @NamedQuery(name = "SaleOrder.findById", query = "SELECT s FROM SaleOrder s WHERE s.id = :id"),
    @NamedQuery(name = "SaleOrder.findByDate", query = "SELECT s FROM SaleOrder s WHERE s.date = :date"),
    @NamedQuery(name = "SaleOrder.findByAmountTax", query = "SELECT s FROM SaleOrder s WHERE s.amountTax = :amountTax"),
    @NamedQuery(name = "SaleOrder.findByAmountTotal", query = "SELECT s FROM SaleOrder s WHERE s.amountTotal = :amountTotal"),
    @NamedQuery(name = "SaleOrder.findByAmountUntaxed", query = "SELECT s FROM SaleOrder s WHERE s.amountUntaxed = :amountUntaxed"),
    @NamedQuery(name = "SaleOrder.findByShipped", query = "SELECT s FROM SaleOrder s WHERE s.shipped = :shipped"),
    @NamedQuery(name = "SaleOrder.findByDiscount", query = "SELECT s FROM SaleOrder s WHERE s.discount = :discount"),
    @NamedQuery(name = "SaleOrder.findByActive", query = "SELECT s FROM SaleOrder s WHERE s.active = :active"),
    @NamedQuery(name = "SaleOrder.findByName", query = "SELECT s FROM SaleOrder s WHERE s.name = :name")})

public class SaleOrder extends BaseEntity {

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
    @Size(max = 40, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @JoinColumn(name = "partner_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Partner partner;
    @OneToMany(mappedBy = "saleOrder", cascade = CascadeType.REMOVE)
    private List<DeliveryOrder> deliveryOrders;
    @OneToMany(mappedBy = "saleOrder", cascade = CascadeType.REMOVE)
    private List<Invoice> invoices;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "saleOrder", orphanRemoval=true)
    private List<SaleOrderLine> saleOrderLines;

    public SaleOrder() {
    }


    public SaleOrder(Date date, Boolean active) {
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getInvoiceMethod() {
        return invoiceMethod;
    }

    public void setInvoiceMethod(String invoiceMethod) {
        this.invoiceMethod = invoiceMethod;
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

    
    public List<DeliveryOrder> getDeliveryOrders() {
        return deliveryOrders;
    }

    public void setDeliveryOrders(List<DeliveryOrder> deliveryOrders) {
        this.deliveryOrders = deliveryOrders;
    }


        public List<SaleOrderLine> getSaleOrderLines() {
        return saleOrderLines;
    }

    public void setSaleOrderLines(List<SaleOrderLine> saleOrderLines) {
        this.saleOrderLines = saleOrderLines;
    }

   
    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }


    @Override
        public String toString() {
        return "--- SaleOrder[ id=" + super.getId() + " ] ---";
    }
    
}
