
package com.defterp.modules.partners.entities;


import com.defterp.modules.inventory.entities.DeliveryOrder;
import com.defterp.modules.inventory.entities.DeliveryOrderLine;
import com.defterp.modules.accounting.entities.Payment;
import com.defterp.modules.accounting.entities.JournalEntry;
import com.defterp.modules.accounting.entities.JournalItem;
import com.defterp.modules.accounting.entities.Invoice;
import com.defterp.modules.accounting.entities.Account;
import com.defterp.modules.accounting.entities.InvoiceLine;
import com.defterp.modules.commonClasses.BaseEntity;
import com.defterp.modules.purchases.entities.PurchaseOrder;
import com.defterp.modules.sales.entities.SaleOrder;
import com.defterp.validators.annotations.InDateRange;
import java.util.Date;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
@Table(name = "partner")
@NamedQueries({
    @NamedQuery(name = "Partner.findAll", query = "SELECT p FROM Partner p"),
    @NamedQuery(name = "Partner.findById", query = "SELECT p FROM Partner p WHERE p.id = :id"),
    @NamedQuery(name = "Partner.findByName", query = "SELECT p FROM Partner p WHERE p.name = :name"),
    @NamedQuery(name = "Partner.findByCity", query = "SELECT p FROM Partner p WHERE p.city = :city"),
    @NamedQuery(name = "Partner.findByStreet", query = "SELECT p FROM Partner p WHERE p.street = :street"),
    @NamedQuery(name = "Partner.findBySupplier", query = "SELECT p FROM Partner p WHERE p.supplier = 1"),
    @NamedQuery(name = "Partner.findByActiveSupplier", query = "SELECT p FROM Partner p WHERE p.supplier = 1 AND p.active = 1"),
    @NamedQuery(name = "Partner.findByCustomer", query = "SELECT p FROM Partner p WHERE p.customer = 1"),
    @NamedQuery(name = "Partner.findByActiveCustomer", query = "SELECT p FROM Partner p WHERE p.customer = 1 AND p.active = 1"),
    @NamedQuery(name = "Partner.findByEmail", query = "SELECT p FROM Partner p WHERE p.email = :email"),
    @NamedQuery(name = "Partner.findByWebsite", query = "SELECT p FROM Partner p WHERE p.website = :website"),
    @NamedQuery(name = "Partner.findByFax", query = "SELECT p FROM Partner p WHERE p.fax = :fax"),
    @NamedQuery(name = "Partner.findByPhone", query = "SELECT p FROM Partner p WHERE p.phone = :phone"),
    @NamedQuery(name = "Partner.findByCredit", query = "SELECT p FROM Partner p WHERE p.credit = :credit"),
    @NamedQuery(name = "Partner.findByDebit", query = "SELECT p FROM Partner p WHERE p.debit = :debit"),
    @NamedQuery(name = "Partner.findByMobile", query = "SELECT p FROM Partner p WHERE p.mobile = :mobile"),
    @NamedQuery(name = "Partner.findByIsCompany", query = "SELECT p FROM Partner p WHERE p.isCompany = :isCompany"),
    @NamedQuery(name = "Partner.findByPurchaseDeals", query = "SELECT p FROM Partner p WHERE p.purchaseDeals = :purchaseDeals"),
    @NamedQuery(name = "Partner.findBySaleDeals", query = "SELECT p FROM Partner p WHERE p.saleDeals = :saleDeals"),
    @NamedQuery(name = "Partner.findByActive", query = "SELECT p FROM Partner p WHERE p.active = :active")})

public class Partner extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    @Column(name = "name" , nullable = false)
    @NotNull
    @Size(min = 1, max = 128, message = "{LongString}")
    private String name;
    @Lob
    @Column(name = "image")
    private byte[] image;
    @Size(max = 128, message = "{LongString}")
    @Column(name = "city")
    private String city;
    @Size(max = 128, message = "{LongString}")
    @Column(name = "street")
    private String street;
    @Column(name = "supplier")
    private Boolean supplier;
    @Column(name = "customer")
    private Boolean customer;
    @Size(max = 240, message = "{LongString}")
    @Column(name = "email")
    private String email;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "website")
    private String website;
    @Size(max = 128, message = "{LongString}")
    @Column(name = "country")
    private String country;
    @Basic(optional = false)
    @NotNull
    @Column(name = "create_date")
    @InDateRange
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "fax")
    private String fax;
     @Size(max = 64, message = "{LongString}")
    @Column(name = "phone")
    private String phone;
    @NotNull
    @Column(name = "credit")
    private Double credit;
    @NotNull
    @Column(name = "debit")
    private Double debit;
    @Lob
    @Column(name = "image_medium")
    private byte[] imageMedium;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "mobile")
    private String mobile;
    @Column(name = "is_company")
    private Boolean isCompany;
    @Column(name = "purchase_deals")
    private Integer purchaseDeals;
    @Column(name = "sale_deals")
    private Integer saleDeals;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @JoinColumn(name = "accountReceivable_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Account accountReceivable;
    @JoinColumn(name = "accountPayable_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Account accountPayable;   
    @OneToMany(mappedBy = "partner")
    private List<JournalItem> journalItems;
    @OneToMany(mappedBy = "partner")
    private List<DeliveryOrderLine> deliveryOrderLines;
    @OneToMany(mappedBy = "partner")
    private List<DeliveryOrder> deliveryOrders;
    @OneToMany(mappedBy = "partner")
    private List<JournalEntry> journalEntries;
    @OneToMany(mappedBy = "partner")
    private List<InvoiceLine> invoiceLines;
    @OneToMany(mappedBy = "partner")
    private List<PurchaseOrder> purchaseOrderList;
    @OneToMany(mappedBy = "partner")
    private List<SaleOrder> saleOrders;
    @OneToMany(mappedBy = "partner")
    private List<Payment> payments;
    @OneToMany(mappedBy = "partner")
    private List<Invoice> invoices;
   
    
    public Partner() {
    }


    public Partner(String name, Boolean active) {
        this.name = name;
        this.active = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Boolean getSupplier() {
        return supplier;
    }

    public void setSupplier(Boolean supplier) {
        this.supplier = supplier;
    }

    public Boolean getCustomer() {
        return customer;
    }

    public void setCustomer(Boolean customer) {
        this.customer = customer;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Double getCredit() {
        if(credit == null){            
            credit = 0d;          
        }          
            return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public Double getDebit() {
        if(debit == null){            
            debit = 0d;           
        }  
        return debit;
    }

    public void setDebit(Double debit) {
        this.debit = debit;
    }

    public byte[] getImageMedium() {
        return imageMedium;
    }

    public void setImageMedium(byte[] imageMedium) {
        this.imageMedium = imageMedium;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Boolean getIsCompany() {
        return isCompany;
    }

    public void setIsCompany(Boolean isCompany) {
        this.isCompany = isCompany;
    }

    public Integer getPurchaseDeals() {
        return purchaseDeals;
    }

    public void setPurchaseDeals(Integer purchaseDeals) {
        this.purchaseDeals = purchaseDeals;
    }

    public Integer getSaleDeals() {
        return saleDeals;
    }

    public void setSaleDeals(Integer saleDeals) {
        this.saleDeals = saleDeals;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Account getAccountReceivable() {
        return accountReceivable;
    }

    public void setAccountReceivable(Account accountReceivable) {
        this.accountReceivable = accountReceivable;
    }

    public Account getAccountPayable() {
        return accountPayable;
    }

    public void setAccountPayable(Account accountPayable) {
        this.accountPayable = accountPayable;
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

    public List<DeliveryOrder> getDeliveryOrders() {
        return deliveryOrders;
    }

    public void setDeliveryOrders(List<DeliveryOrder> deliveryOrders) {
        this.deliveryOrders = deliveryOrders;
    }

    public List<JournalEntry> getJournalEntries() {
        return journalEntries;
    }

    public void setJournalEntries(List<JournalEntry> journalEntries) {
        this.journalEntries = journalEntries;
    }

    public List<InvoiceLine> getInvoiceLines() {
        return invoiceLines;
    }

    public void setInvoiceLines(List<InvoiceLine> invoiceLines) {
        this.invoiceLines = invoiceLines;
    }

    public List<PurchaseOrder> getPurchaseOrderList() {
        return purchaseOrderList;
    }

    public void setPurchaseOrderList(List<PurchaseOrder> purchaseOrderList) {
        this.purchaseOrderList = purchaseOrderList;
    }

    public List<SaleOrder> getSaleOrders() {
        return saleOrders;
    }

    public void setSaleOrders(List<SaleOrder> saleOrders) {
        this.saleOrders = saleOrders;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }


    @Override
    public String toString() {
        return "--- Partner[ id=" + super.getId() + " ] ---";
    }
    
}
