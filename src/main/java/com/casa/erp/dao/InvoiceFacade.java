
package com.casa.erp.dao;

import com.defterp.modules.commonClasses.IdGenerator;
import com.defterp.modules.accounting.entities.Account;
import com.defterp.modules.accounting.entities.Invoice;
import com.defterp.modules.accounting.entities.InvoiceLine;
import com.defterp.modules.accounting.entities.InvoicePayment;
import com.defterp.modules.accounting.entities.Journal;
import com.defterp.modules.accounting.entities.JournalEntry;
import com.defterp.modules.partners.entities.Partner;
import com.defterp.modules.accounting.entities.Payment;
import com.defterp.modules.inventory.entities.Product;
import com.defterp.modules.purchases.entities.PurchaseOrder;
import com.defterp.modules.sales.entities.SaleOrder;
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


    public Invoice create(Invoice entity, String type) {
        em.persist(entity);
        em.flush();
        if(type.equals("invoice")){
            entity.setName(IdGenerator.generateInvoiceId(entity.getId()));
        }else{
            entity.setName(IdGenerator.generateBillId(entity.getId()));
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
                entity.setName(IdGenerator.generateCustomerInPayment(entity.getId()));
            } else if (partnerType.equals("Customer") && type.equals("out")) {
                entity.setName(IdGenerator.generateCustomerOutPayment(entity.getId()));
            } else if (partnerType.equals("Supplier") && type.equals("in")) {
                entity.setName(IdGenerator.generateSupplierInPayment(entity.getId()));
            } else if (partnerType.equals("Supplier") && type.equals("out")) {
                entity.setName(IdGenerator.generateSupplierOutPayment(entity.getId()));
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
            entity.setName(IdGenerator.generatePaymentCashEntryId(entity.getId()));
        } else if (account.equals("Bank")) {
            entity.setName(IdGenerator.generatePaymentBankEntryId(entity.getId()));
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
