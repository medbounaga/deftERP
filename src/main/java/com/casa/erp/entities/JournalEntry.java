
package com.casa.erp.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author MOHAMMED
 */
@Entity
@Table(name = "journal_entry")
@XmlRootElement
@NamedQueries({ 
    @NamedQuery(name = "JournalEntry.countByPartner", query = "SELECT COUNT(j) FROM JournalEntry j WHERE j.partner.id = :partnerId"),
    @NamedQuery(name = "JournalEntry.findByPartner", query = "SELECT j FROM JournalEntry j WHERE j.partner.id = :partnerId"),
    @NamedQuery(name = "JournalEntry.findAll", query = "SELECT j FROM JournalEntry j"),
    @NamedQuery(name = "JournalEntry.findById", query = "SELECT j FROM JournalEntry j WHERE j.id = :id"),
    @NamedQuery(name = "JournalEntry.findByName", query = "SELECT j FROM JournalEntry j WHERE j.name = :name"),
    @NamedQuery(name = "JournalEntry.findByRef", query = "SELECT j FROM JournalEntry j WHERE j.ref = :ref"),
    @NamedQuery(name = "JournalEntry.findByDate", query = "SELECT j FROM JournalEntry j WHERE j.date = :date"),
    @NamedQuery(name = "JournalEntry.findByActive", query = "SELECT j FROM JournalEntry j WHERE j.active = :active")})
public class JournalEntry implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "ref")
    private String ref;
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @Basic(optional = false)
    @NotNull
    @Column(name = "amount")
    private double amount;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "state")
    private String state;
    @Basic(optional = false)
    @NotNull
    @Column(name = "active")
    private Boolean active;
    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL)
    private List<JournalItem> journalItems;
    @JoinColumn(name = "journal_id", referencedColumnName = "id")
    @ManyToOne
    private Journal journal;
    @JoinColumn(name = "partner_id", referencedColumnName = "id")
    @ManyToOne
    private Partner partner;
    @OneToOne(mappedBy = "journalEntry")
    private Payment payment;
    @OneToOne(mappedBy = "journalEntry")
    private Invoice invoice;
    @OneToMany(mappedBy = "journalEntry")
    private List<InvoicePayment> invoicePayments;

    public JournalEntry() {
    }

    public JournalEntry(String name, String ref, Date date, Boolean active, Journal journal, Partner partner, Payment payment, Invoice invoice, String state, double amount) {
        this.name = name;
        this.ref = ref;
        this.date = date;
        this.active = active;
        this.journal = journal;
        this.partner = partner;
        this.payment = payment;
        this.invoice = invoice;
        this.state = state;
        this.amount = amount;
    }
    
    public JournalEntry(String ref, Date date, Boolean active, Journal journal, Partner partner, Payment payment, Invoice invoice, String state, double amount) {
        this.ref = ref;
        this.date = date;
        this.active = active;
        this.journal = journal;
        this.partner = partner;
        this.payment = payment;
        this.invoice = invoice;
        this.state = state;
        this.amount = amount;
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

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @XmlTransient
    public List<JournalItem> getJournalItems() {
        return journalItems;
    }

    public void setJournalItems(List<JournalItem> journalItems) {
        this.journalItems= journalItems;
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
    

    @XmlTransient
    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    @XmlTransient
    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
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
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof JournalEntry)) {
            return false;
        }
        JournalEntry other = (JournalEntry) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.casa.erp.entities.JournalEntry[ id=" + id + " ]";
    }
    
}
