
package com.casa.erp.dao;

import com.casa.erp.beans.util.IdGenerator;
import com.casa.erp.entities.Account;
import com.casa.erp.entities.DeliveryOrder;
import com.casa.erp.entities.Invoice;
import com.casa.erp.entities.Journal;
import com.casa.erp.entities.Partner;
import com.casa.erp.entities.Product;
import com.casa.erp.entities.PurchaseOrder;
import com.casa.erp.entities.PurchaseOrderLine;
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
public class PurchaseOrderFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;

    private IdGenerator idGeerator = new IdGenerator();

    public PurchaseOrder create(PurchaseOrder entity) {

        em.persist(entity);
        em.flush();
        entity.setName(idGeerator.generatePurchaseId(entity.getId()));
        return entity;
    }

    public void create(DeliveryOrder entity) {
        em.persist(entity);
        em.flush();
        entity.setName(idGeerator.generateDeliveryInId(entity.getId()));
    }

    public Invoice create(Invoice entity) {
        em.persist(entity);
        em.flush();
        entity.setName(idGeerator.generateInvoiceInId(entity.getId()));
        return entity;
    }

    public PurchaseOrder update(PurchaseOrder entity) {
        em.merge(entity);
        return entity;
    }
    
    public Invoice update(Invoice entity) {
        em.merge(entity);
        return entity;
    }
    
    public DeliveryOrder update(DeliveryOrder entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(PurchaseOrder entity) {
        em.remove(em.merge(entity));
    }

    public PurchaseOrder find(Object id) {
        return em.find(PurchaseOrder.class, id);
    }
    
    public PurchaseOrderLine findOrderLine(Object id) {
        return em.find(PurchaseOrderLine.class, id);
    }
    
    public Partner findPartner(Object id) {
        return em.find(Partner.class, id);
    }
    
    public List<Partner> findTopNSuppliers(int n) {
        List result = em.createNamedQuery("Partner.findBySupplier")
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
    
    public List<PurchaseOrderLine> findByProduct(Integer productId) {
        List<PurchaseOrderLine> OrderLinesByProduct = em.createNamedQuery("PurchaseOrderLine.findByProduct")
                .setParameter("productId", productId)
                .getResultList();
        
        return OrderLinesByProduct;  
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


    public List<PurchaseOrder> findDrafts() {
        List<PurchaseOrder> draftOrders = em.createNamedQuery("PurchaseOrder.findDrafts")
                .setParameter("draft", "Draft PO")
                .setParameter("cancelled", "Cancelled")
                .getResultList();
        return draftOrders;
    }

    public List<PurchaseOrder> findConfirmed() {
        List<PurchaseOrder> confirmedOrders = em.createNamedQuery("PurchaseOrder.findConfirmed")
                .setParameter("draft", "Draft PO")
                .setParameter("cancelled", "Cancelled")
                .getResultList();
        return confirmedOrders;
    }
    
    public List<PurchaseOrder> findByPartner(Integer partnerId) {
        List<PurchaseOrder> OrdersByPartner = em.createNamedQuery("PurchaseOrder.findByPartner")
                .setParameter("partnerId", partnerId)
                .getResultList();
        
        return OrdersByPartner;  
    }

    public List<PurchaseOrder> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(PurchaseOrder.class));
        return em.createQuery(cq).getResultList();
    }

    public List<PurchaseOrder> findRange(int[] range) {

        return null;
    }

    public int count() {
        return 1;

    }

}
