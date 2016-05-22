package com.casa.erp.dao;

import com.casa.erp.entities.Account;
import com.casa.erp.entities.Partner;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Stateless
public class PartnerFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;

    public Partner create(Partner entity) {
        em.persist(entity);
        return entity;
    }

    public Partner update(Partner entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(Partner entity) {
        em.remove(em.merge(entity));
    }

    public Partner find(Object id) {
        return em.find(Partner.class, id);
    }
    
     public List<Partner> findAll() {
        List<Partner> partners = em.createNamedQuery("Partner.findAll")
                .getResultList();
        return partners;
    }

    public List<Partner> findSuppliers() {
        List<Partner> suppliers = em.createNamedQuery("Partner.findBySupplier")
                .getResultList();
        return suppliers;
    }
    
    public List<Partner> findActiveSuppliers() {
        List<Partner> suppliers = em.createNamedQuery("Partner.findByActiveSupplier")
                .getResultList();
        return suppliers;
    }

    public List<Partner> findCustomers() {
        List<Partner> customers = em.createNamedQuery("Partner.findByCustomer")
                .getResultList();
        return customers;
    }
    
    public List<Partner> findActiveCustomers() {
        List<Partner> customers = em.createNamedQuery("Partner.findByActiveCustomer")
                .getResultList();
        return customers;
    }

    public Long countSalesOrders(Integer partnerId) {
        Long count = (Long) em.createNamedQuery("SaleOrder.countByPartner")
                .setParameter("partnerId", partnerId)
                .getSingleResult();

        if (count == null) {
            count = 0L;
        }
        return count;
    }

    public Long countPurchaseOrders(Integer partnerId) {
        Long count = (Long) em.createNamedQuery("PurchaseOrder.countByPartner")
                .setParameter("partnerId", partnerId)
                .getSingleResult();

        if (count == null) {
            count = 0L;
        }
        return count;
    }

    public Long countInvoices(Integer partnerId, String type) {
        Long count = (Long) em.createNamedQuery("Invoice.countByPartner")
                .setParameter("partnerId", partnerId)
                .setParameter("type", type)
                .getSingleResult();

        if (count == null) {
            count = 0L;
        }
        return count;
    }

    public Long countJournalEntries(Integer partnerId) {
        Long count = (Long) em.createNamedQuery("JournalEntry.countByPartner")
                .setParameter("partnerId", partnerId)
                .getSingleResult();

        if (count == null) {
            count = 0L;
        }
        return count;
    }
    
    
    public Long countShipments(Integer partnerId, String type) {
        Long count = (Long) em.createNamedQuery("DeliveryOrder.countByPartner")
                .setParameter("partnerId", partnerId)
                .setParameter("type", type)
                .getSingleResult();

        if (count == null) {
            count = 0L;
        }
        return count;
    }
    
     public Long countPayments(Integer partnerId, String partnerType) {
        Long count = (Long) em.createNamedQuery("Payment.countByPartner")
                .setParameter("partnerId", partnerId)
                .setParameter("partnerType", partnerType)
                .getSingleResult();

        if (count == null) {
            count = 0L;
        }
        return count;
    }

    

        
    public Double getTotalDueAmount(Integer partnerId, String type) {
        Double totalDueAmount = (Double) em.createNamedQuery("Invoice.TotalDueAmount")
                .setParameter("partnerId", partnerId)
                .setParameter("type", type)
                .getSingleResult();

        if (totalDueAmount == null) {
            totalDueAmount = 0d;
        }
        return totalDueAmount;
    }    

    public Double getInvoicedSum(Integer partnerId, String type) {
        Double invoicedSum = (Double) em.createNamedQuery("Invoice.InvoicedSum")
                .setParameter("partnerId", partnerId)
                .setParameter("type", type)
                .getSingleResult();

        if (invoicedSum == null) {
            invoicedSum = 0d;
        }
        return invoicedSum;
    }
    
    public List<Account> findByType(String type) {
     List result = em.createNamedQuery("Account.findByType")
             .setParameter("type", type)
             .getResultList();
     
     return result;
    }

//    public Double getInvoicedSum(Integer partnerId) {
//        Double InvoicedSaleSum = (Double) em.createNamedQuery("Invoice.InvoicedSum")
//                .setParameter("partnerId", partnerId)
//                .setParameter("type", "Sale")
//                .getSingleResult();
//
//        Double InvoicedPurchaseSum = (Double) em.createNamedQuery("Invoice.InvoicedSum")
//                .setParameter("partnerId", partnerId)
//                .setParameter("type", "Purchase")
//                .getSingleResult();
//
//        if (InvoicedSaleSum == null) {
//            InvoicedSaleSum = 0d;
//        }
//        if (InvoicedPurchaseSum == null) {
//            InvoicedPurchaseSum = 0d;
//        }
//        return (InvoicedSaleSum - InvoicedPurchaseSum);
//    }
    
//    public List<Partner> findAll() {
//        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
//        cq.select(cq.from(Partner.class));
//        return em.createQuery(cq).getResultList();
//    }

    public List<Partner> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Partner.class));
        javax.persistence.Query q = em.createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<Partner> rt = cq.from(Partner.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        javax.persistence.Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}
