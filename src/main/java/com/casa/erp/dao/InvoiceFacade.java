
package com.casa.erp.dao;

import com.casa.erp.beans.util.IdGenerator;
import com.casa.erp.entities.Account;
import com.casa.erp.entities.Invoice;
import com.casa.erp.entities.InvoiceLine;
import com.casa.erp.entities.InvoicePayment;
import com.casa.erp.entities.Journal;
import com.casa.erp.entities.JournalEntry;
import com.casa.erp.entities.Partner;
import com.casa.erp.entities.Payment;
import com.casa.erp.entities.Product;
import com.casa.erp.entities.PurchaseOrder;
import com.casa.erp.entities.SaleOrder;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Stateless
public class InvoiceFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;
    

    private IdGenerator idGeerator = new IdGenerator();

    public Invoice create(Invoice entity, String type) {
        em.persist(entity);
        em.flush();
        if(type.equals("invoice")){
            entity.setName(idGeerator.generateInvoiceOutId(entity.getId()));
        }else{
            entity.setName(idGeerator.generateInvoiceInId(entity.getId()));
        }
        
        return entity;

    }

//    public Invoice createInvoiceRefund(Invoice entity) {
//        em.persist(entity);
//        em.flush();
//        entity.setName(idGeerator.generateRefundInvoiceOutId(entity.getId()));
//        return entity;
//
//    }

    public List<Partner> findTopNCustomers(int n) {
        List result = em.createNamedQuery("Partner.findByCustomer")
                .setMaxResults(n)
                .getResultList();

        return result;
    }
    
    public List<Partner> findTopNSuppliers(int n) {
        List result = em.createNamedQuery("Partner.findBySupplier")
                .setMaxResults(n)
                .getResultList();

        return result;
    }

    public List<Product> findTopNSoldProducts(int n) {
        List result = em.createNamedQuery("Product.findBySaleOk")
                .setMaxResults(n)
                .getResultList();

        return result;
    }
    
    
    public List<Product> findTopNPurchasedProducts(int n) {
        List result = em.createNamedQuery("Product.findByPurchaseOk")
                .setMaxResults(n)
                .getResultList();

        return result;
    }

    public JournalEntry findJournalEntry(Object id) {
        return em.find(JournalEntry.class, id);
    }

    public List<Invoice> findInInvoices() {
        List<Invoice> inInvoices = em.createNamedQuery("Invoice.findInInvoices")
                .getResultList();
        return inInvoices;
    }

    public List<Invoice> findOutInvoices() {
        List<Invoice> outInvoices = em.createNamedQuery("Invoice.findOutInvoices")
                .getResultList();
        return outInvoices;
    }

    public Payment create(Payment entity, String partnerType, String type) {
        if (entity != null) {

            em.persist(entity);
            em.flush();
            
            if (partnerType.equals("Customer") && type.equals("in")) {
                entity.setName(idGeerator.generateCustomerInPayment(entity.getId()));
            } else if (partnerType.equals("Customer") && type.equals("out")) {
                entity.setName(idGeerator.generateCustomerOutPayment(entity.getId()));
            } else if (partnerType.equals("Supplier") && type.equals("in")) {
                entity.setName(idGeerator.generateSupplierInPayment(entity.getId()));
            } else if (partnerType.equals("Supplier") && type.equals("out")) {
                entity.setName(idGeerator.generateSupplierOutPayment(entity.getId()));
            }
        }
        return entity;
    }

    public JournalEntry create(JournalEntry entity) {
        em.persist(entity);
        return entity;
    }

    public JournalEntry create(JournalEntry entity, String account) {
        em.persist(entity);
        em.flush();
        if (account.equals("Cash")) {
            entity.setName(idGeerator.generatePaymentCashEntryId(entity.getId()));
        } else if (account.equals("Bank")) {
            entity.setName(idGeerator.generatePaymentBankEntryId(entity.getId()));
        }
        return entity;
    }

    public InvoicePayment create(InvoicePayment entity) {
        em.persist(entity);
        return entity;
    }

    public Invoice update(Invoice entity) {
        em.merge(entity);
        return entity;
    }

    public Payment update(Payment entity) {
        em.merge(entity);
        return entity;
    }
    
    public JournalEntry update(JournalEntry entity) {
        em.merge(entity);
        return entity;
    }

    public SaleOrder update(SaleOrder entity) {
        em.merge(entity);
        return entity;
    }

    public PurchaseOrder update(PurchaseOrder entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(Invoice entity) {
        em.remove(em.merge(entity));
    }
    
    public void remove(InvoicePayment entity) {
        em.remove(em.merge(entity));
    }

    public Invoice find(Object id) {
        return em.find(Invoice.class, id);
    }
    
    public SaleOrder findSaleOrder(Object id) {
        return em.find(SaleOrder.class, id);
    }
    
    public PurchaseOrder findPurchaseOrder(Object id) {
        return em.find(PurchaseOrder.class, id);
    }


    public Payment findPayment(Object id) {
        return em.find(Payment.class, id);
    }

    public List<Invoice> findBySaleId(int saleId) {
        List result = em.createNamedQuery("Invoice.findBySaleOrder")
                .setParameter("id", saleId)
                .getResultList();

        return result;
    }

    public List<Invoice> findByPurchaseId(int purchaseId) {
        List result = em.createNamedQuery("Invoice.findByPurchaseId")
                .setParameter("id", purchaseId)
                .getResultList();

        return result;
    }

    public Account findAccount(Object name) {

         List<Account> accounts = em.createNamedQuery("Account.findByName")
                .setParameter("name", name)
                .getResultList();

        if (accounts != null && !accounts.isEmpty()) {
            return accounts.get(0);
        }

        return null;
    }

    public Journal findJournal(Object code) {
         List<Journal> journals = em.createNamedQuery("Journal.findByCode")
                .setParameter("code", code)
                .getResultList();

        if (journals != null && !journals.isEmpty()) {
            return journals.get(0);
        }

        return null;
    }


    public List<Invoice> findByPartner(Integer partnerId, String type) {
        List<Invoice> Invoices = em.createNamedQuery("Invoice.findByPartner")
                .setParameter("partnerId", partnerId)
                .setParameter("type", type)
                .getResultList();

        return Invoices;
    }
    
    public List<InvoiceLine> findInvoiceLines(Integer invoiceId) {
        List<InvoiceLine> InvoiceLines = em.createNamedQuery("InvoiceLine.findByInvoice")
                .setParameter("id", invoiceId)
                .getResultList();

        return InvoiceLines;
    }
    
    

    public List<Invoice> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq
                .select(cq.from(Invoice.class
                        ));
        return em.createQuery(cq)
                .getResultList();
    }

    public List<Payment> findSupplierOutstandingPayments(int partnerId) {
        List<Payment> payments = em.createNamedQuery("Payment.findOutstandingByPartner")
                .setParameter("partnerId", partnerId)
                .setParameter("type", "out")
                .setParameter("status", "Posted")
                .setParameter("partnerType", "supplier")
                .getResultList();

        return payments;
    }

    public List<Payment> findCustomerOutstandingPayments(int partnerId) {
        List<Payment> payments = em.createNamedQuery("Payment.findOutstandingByPartner")
                .setParameter("partnerId", partnerId)
                .setParameter("type", "in")
                .setParameter("status", "Posted")
                .setParameter("partnerType", "customer")
                .getResultList();

        return payments;
    }

    public List<Account> findAccountByName(String name) {

        List<Account> accounts = em.createNamedQuery("Account.findByName")
                .setParameter("name", name)
                .getResultList();

        return accounts;
    }

}
