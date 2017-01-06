
package com.casa.erp.entities;

import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Entity
@Table(name = "product_uom")
@NamedQueries({
    @NamedQuery(name = "ProductUom.findAll", query = "SELECT p FROM ProductUom p"),
    @NamedQuery(name = "ProductUom.findById", query = "SELECT p FROM ProductUom p WHERE p.id = :id"),
    @NamedQuery(name = "ProductUom.findByName", query = "SELECT p FROM ProductUom p WHERE p.name = :name"),
    @NamedQuery(name = "ProductUom.findByActive", query = "SELECT p FROM ProductUom p WHERE p.active = :active")})

public class ProductUom extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64, message = "{LongString}")
    @Column(name = "name")
    private String name;    
    @Basic(optional = false)
    @NotNull
    @Column(name = "decimals")
    private Integer decimals;       
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @OneToMany(mappedBy = "uom")
    private List<Product> products;
    @OneToMany(mappedBy = "uom")
    private List<JournalItem> journalItems;
    @JoinColumn(name = "category_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private ProductUomCategory category;
    

    public ProductUom() {
    }


    public ProductUom(String name, Boolean active) {
        this.name = name;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getDecimals() {
        return decimals;
    }

    public void setDecimals(Integer decimals) {
        this.decimals = decimals;
    }
 
    public ProductUomCategory getCategory() {
        return category;
    }

    public void setCategory(ProductUomCategory category) {
        this.category = category;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<JournalItem> getJournalItems() {
        return journalItems;
    }

    public void setJournalItemList(List<JournalItem> journalItems) {
        this.journalItems = journalItems;
    }

    
    @Override
    public String toString() {
        return "--- ProductUom[ id=" + super.getId() + " ] ---";
    }
    
}
