
package com.casa.erp.entities;

import java.io.Serializable;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
@Table(name = "product_uom_category")
@NamedQueries({
    @NamedQuery(name = "ProductUomCategory.findAll", query = "SELECT p FROM ProductUomCategory p"),
    @NamedQuery(name = "ProductUomCategory.findById", query = "SELECT p FROM ProductUomCategory p WHERE p.id = :id"),
    @NamedQuery(name = "ProductUomCategory.findByName", query = "SELECT p FROM ProductUomCategory p WHERE p.name = :name")})
public class ProductUomCategory implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @OneToMany(mappedBy = "category")
    private List<ProductUom> uoms;

    public ProductUomCategory() {
    }

    public ProductUomCategory(Integer id) {
        this.id = id;
    }

    public ProductUomCategory(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ProductUom> getUoms() {
        return uoms;
    }

    public void setUoms(List<ProductUom> uoms) {
        this.uoms = uoms;
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
        if (!(object instanceof ProductUomCategory)) {
            return false;
        }
        ProductUomCategory other = (ProductUomCategory) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.casa.erp.entities.ProductUomCategory[ id=" + id + " ]";
    }
    
}
