
package com.defterp.modules.inventory.entities;

import com.defterp.modules.commonClasses.BaseEntity;
import com.defterp.modules.purchases.entities.PurchaseOrderLine;
import com.defterp.modules.sales.entities.SaleOrderLine;
import com.defterp.modules.accounting.entities.JournalItem;
import com.defterp.modules.accounting.entities.InvoiceLine;
import com.defterp.validators.annotations.StrictlyPositiveNumber;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
@Table(name = "product")
@NamedQueries({
    @NamedQuery(name = "Product.findAll", query = "SELECT p FROM Product p"),
    @NamedQuery(name = "Product.findById", query = "SELECT p FROM Product p WHERE p.id = :id"),
    @NamedQuery(name = "Product.findByDefaultCode", query = "SELECT p FROM Product p WHERE p.defaultCode = :defaultCode"),
    @NamedQuery(name = "Product.findByName", query = "SELECT p FROM Product p WHERE p.name = :name"),
    @NamedQuery(name = "Product.findBySalePrice", query = "SELECT p FROM Product p WHERE p.salePrice = :salePrice"),
    @NamedQuery(name = "Product.findByPurchasePrice", query = "SELECT p FROM Product p WHERE p.purchasePrice = :purchasePrice"),
    @NamedQuery(name = "Product.findByWeight", query = "SELECT p FROM Product p WHERE p.weight = :weight"),
    @NamedQuery(name = "Product.findByVolume", query = "SELECT p FROM Product p WHERE p.volume = :volume"),
    @NamedQuery(name = "Product.findBySaleOk", query = "SELECT p FROM Product p WHERE p.saleOk = 1"),
    @NamedQuery(name = "Product.findByPurchaseOk", query = "SELECT p FROM Product p WHERE p.purchaseOk = 1"),
    @NamedQuery(name = "Product.findByActive", query = "SELECT p FROM Product p WHERE p.active = :active")})

public class Product extends BaseEntity {

    private static final long serialVersionUID = 1L;
    
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "image")
    private byte[] image;
    @Size(max = 25, message = "{LongString}")
    @Column(name = "default_code")
    private String defaultCode;
    @Basic(optional = false)
    @NotNull
    @Size(max = 45, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @Basic(fetch = FetchType.LAZY)
    @Lob
    @Column(name = "image_medium")
    private byte[] imageMedium;
    @Column(name = "sale_price")
    @StrictlyPositiveNumber(message = "{PositiveSalePrice}")
    private Double salePrice = 2d;
    @Column(name = "purchase_price")
    @StrictlyPositiveNumber(message = "{PositiveCost}")
    private Double purchasePrice = 1d;
    @Size(max = 75, message = "{LongString}")
    @Column(name = "description")
    private String description;
    @Column(name = "weight")
    private Double weight = 0d;
    @Column(name = "volume")
    private Double volume = 0d;
    @Column(name = "lenght")
    private Double length = 0d;
    @Column(name = "sale_ok")
    private Boolean saleOk;
    @Column(name = "purchase_ok")
    private Boolean purchaseOk;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @JoinColumn(name = "categ_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private ProductCategory category;
    @JoinColumn(name = "uom_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private ProductUom uom;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "product")
    private Inventory inventory;
    @OneToMany(mappedBy = "product")
    private List<JournalItem> journalItems;
    @OneToMany(mappedBy = "product")
    private List<DeliveryOrderLine> deliveryOrderLines;
    @OneToMany(mappedBy = "product")
    private List<InvoiceLine> invoiceLines;
    @OneToMany(mappedBy = "product")
    private List<PurchaseOrderLine> purchaseOrderLines;
    @OneToMany(mappedBy = "product")
    private List<SaleOrderLine> saleOrderLines;

    public Product() {
    }


    public Product(String defaultCode, String name, Boolean active) {
        this.defaultCode = defaultCode;
        this.name = name;
        this.active = active;
    }


    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getDefaultCode() {
        return defaultCode;
    }

    public void setDefaultCode(String defaultCode) {
        this.defaultCode = defaultCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getImageMedium() {
        return imageMedium;
    }

    public void setImageMedium(byte[] imageMedium) {
        this.imageMedium = imageMedium;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public Double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getLength() {
        return length;
    }

    public void setLength(Double length) {
        this.length = length;
    }

    public Boolean getSaleOk() {
        return saleOk;
    }

    public void setSaleOk(Boolean saleOk) {
        this.saleOk = saleOk;
    }

    public Boolean getPurchaseOk() {
        return purchaseOk;
    }

    public void setPurchaseOk(Boolean purchaseOk) {
        this.purchaseOk = purchaseOk;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public void setCategory(ProductCategory category) {
        this.category = category;
    }

    public ProductUom getUom() {
        return uom;
    }

    public void setUom(ProductUom uom) {
        this.uom = uom;
    }

    public Inventory getInventory() {
        if (inventory == null) {
            inventory = new Inventory();
        }
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public List<JournalItem> getJournalItems() {
        return journalItems;
    }

    public void setJournalItems(List<JournalItem> journalItems) {
        this.journalItems = journalItems;
    }

    public List<DeliveryOrderLine> getDeliveryOrderLines() {
        return deliveryOrderLines;
    }

    public void setDeliveryOrderLines(List<DeliveryOrderLine> deliveryOrderLines) {
        this.deliveryOrderLines = deliveryOrderLines;
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

    public List<SaleOrderLine> getSaleOrderLines() {
        return saleOrderLines;
    }

    public void setSaleOrderLines(List<SaleOrderLine> saleOrderLines) {
        this.saleOrderLines = saleOrderLines;
    }


    @Override
    public String toString() {
        return "--- Product[ id=" + super.getId() + " ] ---";
    }

}
