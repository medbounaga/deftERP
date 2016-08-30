
package com.casa.erp.entities;

import com.casa.erp.validation.InDateRange;
import com.casa.erp.validation.StrictlyPositiveNumber;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
@Table(name = "payment")
@NamedQueries({
    @NamedQuery(name = "Payment.findByPartner", query = "SELECT p FROM Payment p WHERE p.partner.id = :partnerId AND p.partnerType = :partnerType "),
    @NamedQuery(name = "Payment.countByPartner", query = "SELECT COUNT(p) FROM Payment p WHERE p.partner.id = :partnerId AND p.partnerType = :partnerType"),
    @NamedQuery(name = "Payment.findOutstandingByPartner", query = "SELECT p FROM Payment p WHERE p.partner.id = :partnerId AND p.type = :type AND p.state = :status AND p.partnerType = :partnerType AND p.overpayment > 0"),
    @NamedQuery(name = "Payment.findAll", query = "SELECT p FROM Payment p"),
    @NamedQuery(name = "Payment.findByPartnerType", query = "SELECT p FROM Payment p WHERE p.partnerType = :partnerType"),
    @NamedQuery(name = "Payment.findById", query = "SELECT p FROM Payment p WHERE p.id = :id"),
    @NamedQuery(name = "Payment.findByDate", query = "SELECT p FROM Payment p WHERE p.date = :date"),
    @NamedQuery(name = "Payment.findByAmount", query = "SELECT p FROM Payment p WHERE p.amount = :amount"),
    @NamedQuery(name = "Payment.findByActive", query = "SELECT p FROM Payment p WHERE p.active = :active")})

public class Payment extends BaseEntity {

    private static final long serialVersionUID = 1L;
    
    @Column(name = "date")
    @Basic(optional = false)
    @NotNull
    @InDateRange
    @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();
    @Basic(optional = false)
    @NotNull
    @StrictlyPositiveNumber(message = "{PositivePayment}")
    @Column(name = "amount")
    private Double amount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "overpayment")
    private double overpayment;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "type")
    private String type;
    @Basic(optional = false)
    @NotNull
    @Size(max = 64, message = "{LongString}")
    @Column(name = "partner_type")
    private String partnerType;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "reference")
    private String reference;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "state")
    private String state;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    @ManyToOne
    private Account account;
    @JoinColumn(name = "entry_id", referencedColumnName = "id")
    @OneToOne(cascade = CascadeType.PERSIST)
    private JournalEntry journalEntry;
    @JoinColumn(name = "invoice_id", referencedColumnName = "id")
    @ManyToOne
    private Invoice invoice;
    @JoinColumn(name = "journal_id", referencedColumnName = "id")
    @ManyToOne
    private Journal journal;
    @JoinColumn(name = "partner_id", referencedColumnName = "id")
    @ManyToOne
    private Partner partner;
    

    public Payment() {

    }

    public Payment(double amount, Date date, Partner partner, Journal journal, String type, Boolean active, Account account, JournalEntry journalEntry, Invoice invoice, String state, double overpayment, String partnerType) {
        this.type = type;
        this.active = active;
        this.account = account;
        this.journalEntry = journalEntry;
        this.invoice = invoice;
        this.amount = amount;
        this.date = date;
        this.partner = partner;
        this.journal = journal;
        this.state = state;
        this.overpayment = overpayment;
        this.partnerType = partnerType;
    }
    

    public Payment(double amount, Date date, Partner partner, Journal journal, String type, Boolean active, Account account, JournalEntry journalEntry, Invoice invoice, String state, String reference, double overpayment, String partnerType) {
        this.type = type;
        this.active = active;
        this.account = account;
        this.journalEntry = journalEntry;
        this.invoice = invoice;
        this.amount = amount;
        this.date = date;
        this.partner = partner;
        this.journal = journal;
        this.state = state;
        this.reference = reference;
        this.overpayment = overpayment;
        this.partnerType = partnerType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public double getOverpayment() {
        return overpayment;
    }

    public void setOverpayment(double overpayment) {
        this.overpayment = overpayment;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPartnerType() {
        return partnerType;
    }

    public void setPartnerType(String partnerType) {
        this.partnerType = partnerType;
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

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
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


    @Override
    public String toString() {
        return "--- Payment[ id=" + super.getId() + " ] ---";
    }

}
