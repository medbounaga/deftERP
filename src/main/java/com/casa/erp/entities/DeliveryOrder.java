
package com.casa.erp.entities;

import com.casa.erp.validation.InDateRange;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
@Table(name = "delivery_order")
@NamedQueries({
    @NamedQuery(name = "DeliveryOrder.findByPartner", query = "SELECT d FROM DeliveryOrder d WHERE d.partner.id = :partnerId AND d.type = :type "),
    @NamedQuery(name = "DeliveryOrder.countByPartner", query = "SELECT COUNT(d) FROM DeliveryOrder d WHERE d.partner.id = :partnerId AND d.type = :type "),
    @NamedQuery(name = "DeliveryOrder.countByBackOrder", query = "SELECT COUNT(d) FROM DeliveryOrder d WHERE d.backOrder.id = :id"),
    @NamedQuery(name = "DeliveryOrder.findByBackOrder", query = "SELECT d FROM DeliveryOrder d WHERE d.backOrder.id = :id"),
    @NamedQuery(name = "DeliveryOrder.findInDelivery", query = "SELECT d FROM DeliveryOrder d WHERE d.type = :type"),
    @NamedQuery(name = "DeliveryOrder.findOutDelivery", query = "SELECT d FROM DeliveryOrder d WHERE d.type = :type"),
    @NamedQuery(name = "DeliveryOrder.findAll", query = "SELECT d FROM DeliveryOrder d"),
    @NamedQuery(name = "DeliveryOrder.findBySaleOrder", query = "SELECT d FROM DeliveryOrder d WHERE d.saleOrder.id = :id"),
    @NamedQuery(name = "DeliveryOrder.findByPurchaseOrder", query = "SELECT d FROM DeliveryOrder d WHERE d.purchaseOrder.id = :id"),
    @NamedQuery(name = "DeliveryOrder.findById", query = "SELECT d FROM DeliveryOrder d WHERE d.id = :id"),
    @NamedQuery(name = "DeliveryOrder.findByDate", query = "SELECT d FROM DeliveryOrder d WHERE d.date = :date"),
    @NamedQuery(name = "DeliveryOrder.findByOrigin", query = "SELECT d FROM DeliveryOrder d WHERE d.origin = :origin"),
    @NamedQuery(name = "DeliveryOrder.findByName", query = "SELECT d FROM DeliveryOrder d WHERE d.name = :name"),
    @NamedQuery(name = "DeliveryOrder.findByActive", query = "SELECT d FROM DeliveryOrder d WHERE d.active = :active")})
public class DeliveryOrder implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date")
    @InDateRange
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "origin")
    private String origin;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "delivery_method")
    private String deliveryMethod;
    @Basic(optional = false)
    @NotNull
    @Size(max = 64, message = "{LongString}")
    @Column(name = "state")
    private String state;
    @Basic(optional = false)
    @NotNull
    @Size(max = 64, message = "{LongString}")
    @Column(name = "type")
    private String type;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "deliveryOrder", fetch = FetchType.EAGER, orphanRemoval=true)
    private List<DeliveryOrderLine> deliveryOrderLines;
    @JoinColumn(name = "partner_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Partner partner;
    @ManyToOne
    @JoinColumn(name = "purchase_id", referencedColumnName = "id")
    private PurchaseOrder purchaseOrder;
    @ManyToOne
    @JoinColumn(name = "sale_id", referencedColumnName = "id")
    private SaleOrder saleOrder;
    @ManyToOne
    @JoinColumn(name = "back_Order_id", referencedColumnName = "id")
    private DeliveryOrder backOrder;
    @OneToMany(mappedBy = "backOrder")
    private List<DeliveryOrder> children;

    

    public DeliveryOrder() {
    }

    public DeliveryOrder(Integer id) {
        this.id = id;
    }

    public DeliveryOrder(Date date, String origin, String state, String type, Boolean active, String deliveryMethod, DeliveryOrder backOrder,Partner partner, SaleOrder saleOrder) {

        this.date = date;
        this.origin = origin;
        this.state = state;
        this.type = type;
        this.active = active;
        this.partner = partner;
        this.saleOrder = saleOrder;
        this.deliveryMethod = deliveryMethod;
        this.backOrder = backOrder;

    }
    
     public DeliveryOrder(Date date, String origin, String state, String type, Boolean active, String deliveryMethod, DeliveryOrder backOrder,Partner partner, PurchaseOrder purchaseOrder) {

        this.date = date;
        this.origin = origin;
        this.state = state;
        this.type = type;
        this.active = active;
        this.partner = partner;
        this.purchaseOrder = purchaseOrder;
        this.deliveryMethod = deliveryMethod;
        this.backOrder = backOrder;

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public List<DeliveryOrderLine> getDeliveryOrderLines() {
        return deliveryOrderLines;
    }

    public void setDeliveryOrderLines(List<DeliveryOrderLine> deliveryOrderLines) {
        this.deliveryOrderLines = deliveryOrderLines;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public PurchaseOrder getPurchaseOrder() {
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public SaleOrder getSaleOrder() {
        return saleOrder;
    }

    public void setSaleOrder(SaleOrder saleOrder) {
        this.saleOrder = saleOrder;
    }

    public DeliveryOrder getBackOrder() {
        return backOrder;
    }

    public void setBackOrder(DeliveryOrder backOrder) {
        this.backOrder = backOrder;
    }
    
    public List<DeliveryOrder> getChildren() {
        return children;
    }

    public void setChildren(List<DeliveryOrder> children) {
        this.children = children;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DeliveryOrder)) {
            return false;
        }
        DeliveryOrder other = (DeliveryOrder) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.casa.erp.entities.DeliveryOrder[ id=" + id + " ]";
    }

}
