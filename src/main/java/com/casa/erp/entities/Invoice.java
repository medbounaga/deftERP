
package com.casa.erp.entities;

import com.casa.erp.validation.InDateRange;
import java.util.ArrayList;
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
import javax.persistence.OneToOne;
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
@Table(name = "invoice")
@NamedQueries({

    @NamedQuery(name = "Invoice.InvoicedSum", query = "SELECT SUM(i.amountUntaxed) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type"),
    @NamedQuery(name = "Invoice.TotalDueAmount", query = "SELECT SUM(i.residual) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type"),
    @NamedQuery(name = "Invoice.findByPartner", query = "SELECT i FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type "),
    @NamedQuery(name = "Invoice.countByPartner", query = "SELECT COUNT(i) FROM Invoice i WHERE i.partner.id = :partnerId AND i.type = :type "),
    @NamedQuery(name = "Invoice.findInInvoices", query = "SELECT i FROM Invoice i WHERE i.type = 'Purchase Refund' OR i.type = 'Purchase'"),
    @NamedQuery(name = "Invoice.findOutInvoices", query = "SELECT i FROM Invoice i WHERE i.type = 'Sale Refund' OR i.type = 'Sale'"),
    @NamedQuery(name = "Invoice.findAll", query = "SELECT i FROM Invoice i"),
    @NamedQuery(name = "Invoice.findBySaleOrder", query = "SELECT i FROM Invoice i WHERE i.saleOrder.id = :id"),
    @NamedQuery(name = "Invoice.findByPurchaseId", query = "SELECT i FROM Invoice i WHERE i.purchaseOrder.id = :id"),
    @NamedQuery(name = "Invoice.findById", query = "SELECT i FROM Invoice i WHERE i.id = :id"),
    @NamedQuery(name = "Invoice.findByDate", query = "SELECT i FROM Invoice i WHERE i.date = :date"),
    @NamedQuery(name = "Invoice.findByAmountUntaxed", query = "SELECT i FROM Invoice i WHERE i.amountUntaxed = :amountUntaxed"),
    @NamedQuery(name = "Invoice.findByAmountTotal", query = "SELECT i FROM Invoice i WHERE i.amountTotal = :amountTotal"),
    @NamedQuery(name = "Invoice.findByAmountTax", query = "SELECT i FROM Invoice i WHERE i.amountTax = :amountTax"),
    @NamedQuery(name = "Invoice.findByOrigin", query = "SELECT i FROM Invoice i WHERE i.origin = :origin"),
    @NamedQuery(name = "Invoice.findByReference", query = "SELECT i FROM Invoice i WHERE i.reference = :reference"),
    @NamedQuery(name = "Invoice.findByResidual", query = "SELECT i FROM Invoice i WHERE i.residual = :residual"),
    @NamedQuery(name = "Invoice.findByNumber", query = "SELECT i FROM Invoice i WHERE i.number = :number"),
    @NamedQuery(name = "Invoice.findBySupplierInvoiceNumber", query = "SELECT i FROM Invoice i WHERE i.supplierInvoiceNumber = :supplierInvoiceNumber"),
    @NamedQuery(name = "Invoice.findByActive", query = "SELECT i FROM Invoice i WHERE i.active = :active"),
    @NamedQuery(name = "Invoice.findByName", query = "SELECT i FROM Invoice i WHERE i.name = :name")})

public class Invoice extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "date")
    @InDateRange
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();
    @Column(name = "amount_untaxed")
    private Double amountUntaxed = 0d;
    @Column(name = "amount_total")
    private Double amountTotal = 0d;
    @Column(name = "amount_tax")
    private Double amountTax = 0d;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "type") 
    private String type;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "origin")
    private String origin;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "reference")
    private String reference;
    @Lob
    @Size(max = 2147483647, message = "{LongString}")
    @Column(name = "comment")
    private String comment;
    @Column(name = "residual")
    private Double residual = 0d;
    @Basic(optional = false)
    @NotNull
    @Size(max = 64, message = "{LongString}")
    @Column(name = "state")
    private String state;
    @Size(max = 32, message = "{LongString}")
    @Column(name = "number")
    private String number;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "supplier_invoice_number")
    private String supplierInvoiceNumber;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active = true;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "invoice", orphanRemoval=true)
    private List<InvoiceLine> invoiceLines;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "invoice", orphanRemoval=true)
    private List<InvoiceTax> invoiceTaxes;
    @OneToMany(mappedBy = "invoice")
    private List<Payment> payments;
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @ManyToOne
    private Account account;
    @JoinColumn(name = "entry_id", referencedColumnName = "id")
    @OneToOne(cascade = CascadeType.REMOVE)
    private JournalEntry journalEntry;   
    @JoinColumn(name = "journal_id", referencedColumnName = "id")
    @ManyToOne(optional = false)
    private Journal journal;
    @JoinColumn(name = "partner_id", referencedColumnName = "id")
    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    private Partner partner;
    @ManyToOne
    @JoinColumn(name = "purchase_id", referencedColumnName = "id")
    private PurchaseOrder purchaseOrder;
    @ManyToOne
    @JoinColumn(name = "sale_id", referencedColumnName = "id")
    private SaleOrder saleOrder;    
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.REMOVE)
    private List<InvoicePayment> invoicePayments;
    

    public Invoice() {
    }


    public Invoice(Date date, String type, String origin, String state, Boolean active, Partner partner, SaleOrder saleOrder, Account account, Journal journal) {
        this.date = date;
        this.type = type;
        this.origin = origin;
        this.state = state;
        this.active = active;
        this.partner = partner;
        this.saleOrder = saleOrder;
        this.account = account; 
        this.journal = journal;     
    }
    
    public Invoice(Date date, String type, String origin, String state, Boolean active, Partner partner, PurchaseOrder purchaseOrder, Account account, Journal journal, String reference) {
        this.date = date;
        this.type = type;
        this.origin = origin;
        this.state = state;
        this.active = active;
        this.partner = partner;
        this.purchaseOrder = purchaseOrder;
        this.account = account; 
        this.journal = journal;     
        this.reference = reference;
    }


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getAmountUntaxed() {
        return amountUntaxed;
    }

    public void setAmountUntaxed(Double amountUntaxed) {
        this.amountUntaxed = amountUntaxed;
    }

    public Double getAmountTotal() {
        return amountTotal;
    }

    public void setAmountTotal(Double amountTotal) {
        this.amountTotal = amountTotal;
    }

    public Double getAmountTax() {
        return amountTax;
    }

    public void setAmountTax(Double amountTax) {
        this.amountTax = amountTax;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Double getResidual() {
        return residual;
    }

    public void setResidual(Double residual) {
        this.residual = residual;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSupplierInvoiceNumber() {
        return supplierInvoiceNumber;
    }

    public void setSupplierInvoiceNumber(String supplierInvoiceNumber) {
        this.supplierInvoiceNumber = supplierInvoiceNumber;
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

    public List<InvoiceLine> getInvoiceLines() {
        return invoiceLines;
    }

    public void setInvoiceLines(List<InvoiceLine> invoiceLines) {
        this.invoiceLines = invoiceLines;
    }

    public List<InvoiceTax> getInvoiceTaxes() {
        return invoiceTaxes;
    }

    public void setInvoiceTaxes(List<InvoiceTax> invoiceTaxes) {
        this.invoiceTaxes = invoiceTaxes;
    }

    public List<Payment> getPayments() {
        if(payments == null){
           
            return payments = new ArrayList<>();
        }  
            return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
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

    public List<InvoicePayment> getInvoicePayments() {
        if (invoicePayments == null) {
            invoicePayments = new ArrayList<>();
        }
        return invoicePayments;
    }

    public void setInvoicePayments(List<InvoicePayment> invoicePayments) {
        this.invoicePayments = invoicePayments;
    }


    @Override
    public String toString() {
        return "--- Invoice[ id=" + super.getId() + " ] ---";
    }
    
}
