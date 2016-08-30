package com.casa.erp.entities;

import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
@Table(name = "invoice_payment")
@NamedQueries({
    @NamedQuery(name = "InvoicePayment.findById", query = "SELECT i FROM InvoicePayment i WHERE i.id = :id")})

public class InvoicePayment extends BaseEntity {
    
    private static final long serialVersionUID = 1L;
     
    @Basic(optional = false)
    @NotNull
    @Column(name = "paid_amount")
    private Double paidAmount;
    @Size(max = 64, message = "{LongString}")
    @Column(name = "name")
    private String name;
    @NotNull
    @Column(name = "date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;
    @JoinColumn(name = "journal_entry_id", referencedColumnName = "id")
    @ManyToOne
    private JournalEntry journalEntry;    
    @JoinColumn(name = "invoice_id", referencedColumnName = "id")
    @ManyToOne
    private Invoice invoice;
    

    public InvoicePayment() {
        
    }
    
    public InvoicePayment(Invoice invoice, JournalEntry journalEntry, Double paidAmount, Date date, String name) {
        this.invoice = invoice;
        this.journalEntry = journalEntry;
        this.paidAmount = paidAmount;  
        this.date = date;
        this.name = name;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(Double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
    

    @Override
    public String toString() {
        return "--- InvoicePayment[ id=" + super.getId() + " ] ---";
    }
    
}
