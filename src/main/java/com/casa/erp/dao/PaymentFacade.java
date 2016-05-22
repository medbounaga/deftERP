package com.casa.erp.dao;

import com.casa.erp.beans.util.IdGenerator;
import com.casa.erp.entities.Account;
import com.casa.erp.entities.Journal;
import com.casa.erp.entities.JournalEntry;
import com.casa.erp.entities.Partner;
import com.casa.erp.entities.Payment;
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
public class PaymentFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;

    private IdGenerator idGeerator = new IdGenerator();

    public Payment create(Payment entity) {
        em.persist(entity);
        return entity;
    }

    public Payment update(Payment entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(Payment entity) {
        em.remove(em.merge(entity));
    }

    public Payment find(Object id) {
        return em.find(Payment.class, id);
    }

    public List<Payment> findAll() {
        List<Payment> partners = em.createNamedQuery("Payment.findAll")
                .getResultList();
        return partners;
    }

    public List<Payment> findSupplierPayment() {
        List<Payment> payments = em.createNamedQuery("Payment.findByPartnerType")
                .setParameter("partnerType", "supplier")
                .getResultList();
        return payments;
    }

    public List<Payment> findCustomerPayment() {
        List<Payment> payments = em.createNamedQuery("Payment.findByPartnerType")
                .setParameter("partnerType", "customer")
                .getResultList();
        return payments;
    }

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
  
    
    public List<Payment> findByPartner(Integer partnerId, String partnerType) {
        List<Payment> payments = em.createNamedQuery("Payment.findByPartner")
                .setParameter("partnerId", partnerId)
                .setParameter("partnerType", partnerType)
                .getResultList();

        return payments;
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
}
