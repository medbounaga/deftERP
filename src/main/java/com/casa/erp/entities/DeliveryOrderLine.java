
package com.casa.erp.entities;

import com.casa.erp.validation.StrictlyPositiveNumber;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Entity
@Table(name = "delivery_order_line")
@NamedQueries({
    @NamedQuery(name = "DeliveryOrderLine.findAll", query = "SELECT d FROM DeliveryOrderLine d"),
    @NamedQuery(name = "DeliveryOrderLine.findById", query = "SELECT d FROM DeliveryOrderLine d WHERE d.id = :id"),
    @NamedQuery(name = "DeliveryOrderLine.findByPrice", query = "SELECT d FROM DeliveryOrderLine d WHERE d.price = :price"),
    @NamedQuery(name = "DeliveryOrderLine.findByQuantity", query = "SELECT d FROM DeliveryOrderLine d WHERE d.quantity = :quantity"),
    @NamedQuery(name = "DeliveryOrderLine.findByActive", query = "SELECT d FROM DeliveryOrderLine d WHERE d.active = :active"),
    @NamedQuery(name = "DeliveryOrderLine.findByUom", query = "SELECT d FROM DeliveryOrderLine d WHERE d.uom = :uom")})

public class DeliveryOrderLine extends BaseEntity{
    
    private static final long serialVersionUID = 1L;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "price")
    private double price;
    @Basic(optional = false)
    @NotNull
    @StrictlyPositiveNumber(message = "{PositiveQuantity}")
    @Column(name = "quantity")
    private Double quantity = 1d;
    @Basic(optional = false)
    @NotNull
    @Column(name = "reserved")
    private Double reserved = 0d;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "type")
    private String type;
    @Size(max = 40, message = "{LongString}")
    @Column(name = "uom")
    private String uom;
    @Basic(optional = false)
    @NotNull
    @Size(max = 64, message = "{LongString}")
    @Column(name = "state")
    private String state;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @Transient
    private String productName;
    @JoinColumn(name = "partner_id", referencedColumnName = "id")
    @ManyToOne
    private Partner partner;
    @JoinColumn(name = "delivery_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private DeliveryOrder deliveryOrder;
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @ManyToOne(optional = false,  fetch = FetchType.EAGER )
    private Product product;
        
    
    public DeliveryOrderLine() {
    }

    public DeliveryOrderLine(Product product, Double quantity, Double reserved, String uom, String state, String type, Boolean active, Partner partner, double price, DeliveryOrder deliveryOrder) {
        this.product = product;
        this.quantity = quantity;
        this.uom = uom;
        this.state = state;  
        this.type = type; 
        this.price = price;  
        this.active = active; 
        this.partner = partner;
        this.deliveryOrder = deliveryOrder;
        this.reserved = reserved;
    }


    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public DeliveryOrder getDeliveryOrder() {
        return deliveryOrder;
    }

    public void setDeliveryOrder(DeliveryOrder deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public Double getReserved() {
        return reserved;
    }

    public void setReserved(Double reserved) {
        this.reserved = reserved;
    }
    

    @Override
    public String toString() {
        return "--- DeliveryOrderLine[ id=" + super.getId() + " ] ---";
    }
    
}
