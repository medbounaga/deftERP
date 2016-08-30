
package com.casa.erp.entities;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
@Table(name = "journal_item")
@NamedQueries({
    @NamedQuery(name = "JournalItem.CreditSum", query = "SELECT SUM(j.credit) FROM JournalItem j WHERE j.account.id = :accountId"),
    @NamedQuery(name = "JournalItem.DebitSum", query = "SELECT SUM(j.debit) FROM JournalItem j WHERE j.account.id = :accountId"),
    @NamedQuery(name = "JournalItem.findByJournalPeriod", query = "SELECT j FROM JournalItem j WHERE j.journal.id = :journalId AND j.date BETWEEN :monthStart AND :monthEnd"),
    @NamedQuery(name = "JournalItem.findByJournal", query = "SELECT j FROM JournalItem j WHERE j.journal.id = :journalId AND j.date BETWEEN :yearStart AND :yearEnd"),
    @NamedQuery(name = "JournalItem.findByPeriod", query = "SELECT j FROM JournalItem j WHERE j.date BETWEEN :monthStart AND :monthEnd"),
    @NamedQuery(name = "JournalItem.findAll", query = "SELECT j FROM JournalItem j"),
    @NamedQuery(name = "JournalItem.findById", query = "SELECT j FROM JournalItem j WHERE j.id = :id"),
    @NamedQuery(name = "JournalItem.findByDebit", query = "SELECT j FROM JournalItem j WHERE j.debit = :debit"),
    @NamedQuery(name = "JournalItem.findByCredit", query = "SELECT j FROM JournalItem j WHERE j.credit = :credit"),
    @NamedQuery(name = "JournalItem.findByDate", query = "SELECT j FROM JournalItem j WHERE j.date = :date"),
    @NamedQuery(name = "JournalItem.findByName", query = "SELECT j FROM JournalItem j WHERE j.name = :name"),
    @NamedQuery(name = "JournalItem.findByRef", query = "SELECT j FROM JournalItem j WHERE j.ref = :ref"),
    @NamedQuery(name = "JournalItem.findByTaxAmount", query = "SELECT j FROM JournalItem j WHERE j.taxAmount = :taxAmount"),
    @NamedQuery(name = "JournalItem.findByQuantity", query = "SELECT j FROM JournalItem j WHERE j.quantity = :quantity"),
    @NamedQuery(name = "JournalItem.findByActive", query = "SELECT j FROM JournalItem j WHERE j.active = :active")})

public class JournalItem extends BaseEntity{
    
    private static final long serialVersionUID = 1L;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "debit")
    private Double debit;
    @Basic(optional = false)
    @NotNull
    @Column(name = "credit")
    private Double credit;
    @Basic(optional = false)
    @NotNull
    @Column(name = "cost_of_goods_sold")
    private Double costOfGoodsSold;
    @Basic(optional = false)
    @NotNull
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "ref")
    private String ref;
    @Column(name = "tax_amount")
    private Double taxAmount;
    @Column(name = "quantity")
    private Double quantity;
    @Column(name = "residual_amount")
    private Double residualAmount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @ManyToOne
    private Account account;
    @JoinColumn(name = "entry_id", referencedColumnName = "id")
    @ManyToOne
    private JournalEntry journalEntry;
    @JoinColumn(name = "journal_id", referencedColumnName = "id")
    @ManyToOne
    private Journal journal;
    @JoinColumn(name = "partner_id", referencedColumnName = "id")
    @ManyToOne
    private Partner partner;
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @ManyToOne
    private Product product;
    @JoinColumn(name = "uom_id", referencedColumnName = "id")
    @ManyToOne
    private ProductUom uom;
    @JoinColumn(name = "tax_id", referencedColumnName = "id")
    @ManyToOne
    private Tax tax;
    

    public JournalItem() {
    }

    public JournalItem(Double debit, Double credit, Date date, String name, String ref, Double taxAmount, Double quantity, Boolean active, Account account, JournalEntry journalEntry, Journal journal, Partner partner, Product product, ProductUom uom, Double costOfGoodsSold, Tax tax) {
        
        this.debit = debit;
        this.credit = credit;
        this.date = date;
        this.name = name;
        this.ref = ref;
        this.taxAmount = taxAmount;
        this.tax = tax;
        this.quantity = quantity;
        this.active = active;
        this.account = account;
        this.journalEntry = journalEntry;
        this.journal = journal;
        this.partner = partner;
        this.product= product;
        this.uom = uom;
        this.costOfGoodsSold = costOfGoodsSold;
    }


    public Double getDebit() {
        return debit;
    }

    public void setDebit(Double debit) {
        this.debit = debit;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public Double getCostOfGoodsSold() {
        return costOfGoodsSold;
    }

    public void setCostOfGoodsSold(Double costOfGoodsSold) {
        this.costOfGoodsSold = costOfGoodsSold;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(Double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public Double getResidualAmount() {
        return residualAmount;
    }

    public void setResidualAmount(Double residualAmount) {
        this.residualAmount = residualAmount;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public ProductUom getUom() {
        return uom;
    }

    public void setUom(ProductUom uom) {
        this.uom = uom;
    }

    @Override
    public String toString() {
        return "--- JournalItem[ id=" + super.getId() + " ] ---";
    }
    
}
