
package com.casa.erp.entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Entity
@Table(name = "inventory")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Inventory.findAll", query = "SELECT i FROM Inventory i"),
    @NamedQuery(name = "Inventory.findById", query = "SELECT i FROM Inventory i WHERE i.id = :id"),
    @NamedQuery(name = "Inventory.findByMaxQty", query = "SELECT i FROM Inventory i WHERE i.maxQty = :maxQty"),
    @NamedQuery(name = "Inventory.findByMinQty", query = "SELECT i FROM Inventory i WHERE i.minQty = :minQty"),
    @NamedQuery(name = "Inventory.findByActive", query = "SELECT i FROM Inventory i WHERE i.active = :active")})
public class Inventory implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Column(name = "max_qty")
    private Double maxQty;
    @Column(name = "min_qty")
    private Double minQty;
    @NotNull
    @Column(name = "quantity")
    private Double quantityOnHand = 0d;
    @NotNull
    @Column(name = "incoming")
    private Double incomingQuantity = 0d;
    @NotNull
    @Column(name = "reserved")
    private Double reservedQuantity = 0d;
    @Basic(optional = false)
    @NotNull
    @Column(name = "unit_cost")
    private Double unitCost;
    @Basic(optional = false)
    @NotNull
    @Column(name = "total_cost")
    private Double totalCost;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @OneToOne(optional = false)
    private Product product;

    public Inventory() {
    }

    public Inventory(Integer id) {
        this.id = id;
    }

    public Inventory(Integer id, Double quantityOnHand, Boolean active) {
        this.id = id;
        this.quantityOnHand = quantityOnHand;
        this.active = active;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getMaxQty() {
        return maxQty;
    }

    public void setMaxQty(Double maxQty) {
        this.maxQty = maxQty;
    }

    public Double getMinQty() {
        return minQty;
    }

    public void setMinQty(Double minQty) {
        this.minQty = minQty;
    }

    public Double getQuantityOnHand() {
        return quantityOnHand;
    }

    public void setQuantityOnHand(Double quantityOnHand) {
        this.quantityOnHand = quantityOnHand;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Double getQuantityAvailable() {
        return (quantityOnHand + incomingQuantity - reservedQuantity );
    }

    public Double getIncomingQuantity() {
        return incomingQuantity;
    }

    public void setIncomingQuantity(Double incomingQuantity) {
        this.incomingQuantity = incomingQuantity;
    }

    public Double getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Double reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(Double unitCost) {
        this.unitCost = unitCost;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
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
        if (!(object instanceof Inventory)) {
            return false;
        }
        Inventory other = (Inventory) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.casa.erp.entities.Inventory[ id=" + id + " ]";
    }
    
}
