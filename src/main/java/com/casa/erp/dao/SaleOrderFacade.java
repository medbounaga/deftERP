package com.casa.erp.dao;

import com.defterp.modules.commonClasses.IdGenerator;
import com.defterp.modules.accounting.entities.Account;
import com.defterp.modules.inventory.entities.DeliveryOrder;
import com.defterp.modules.accounting.entities.Invoice;
import com.defterp.modules.accounting.entities.Journal;
import com.defterp.modules.partners.entities.Partner;
import com.defterp.modules.inventory.entities.Product;
import com.defterp.modules.sales.entities.SaleOrder;
import com.defterp.modules.sales.entities.SaleOrderLine;
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
public class SaleOrderFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;

    public SaleOrder create(SaleOrder entity) {

        em.persist(entity);
        em.flush();
        entity.setName(IdGenerator.generateSaleId(entity.getId()));
        return entity;
    }

    public void create(DeliveryOrder entity) {
        em.persist(entity);
        em.flush();
        entity.setName(IdGenerator.generateDeliveryOutId(entity.getId()));

    }

    public Invoice create(Invoice entity) {
        em.persist(entity);
        em.flush();
        entity.setName(IdGenerator.generateInvoiceId(entity.getId()));
        return entity;

    }

    public SaleOrder update(SaleOrder entity) {
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

    public void remove(SaleOrder entity) {
        em.remove(em.merge(entity));
    }

    public SaleOrder find(Object id) {
        return em.find(SaleOrder.class, id);
    }

    public SaleOrderLine findOrderLine(Object id) {
        return em.find(SaleOrderLine.class, id);
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

    public List<SaleOrder> findDrafts() {
        List<SaleOrder> draftOrders = em.createNamedQuery("SaleOrder.findDrafts")
                .setParameter("draft", "Draft Quotation")
                .setParameter("cancelled", "Cancelled")
                .getResultList();

        return draftOrders;
    }

    public List<SaleOrder> findConfirmed() {
        List<SaleOrder> confirmedOrders = em.createNamedQuery("SaleOrder.findConfirmed")
                .setParameter("draft", "Draft Quotation")
                .setParameter("cancelled", "Cancelled")
                .getResultList();

        return confirmedOrders;
    }

    public List<SaleOrder> findByPartner(Integer partnerId) {
        List<SaleOrder> OrdersByPartner = em.createNamedQuery("SaleOrder.findByPartner")
                .setParameter("partnerId", partnerId)
                .getResultList();

        return OrdersByPartner;
    }

    public List<SaleOrderLine> findByProduct(Integer productId) {
        List<SaleOrderLine> OrderLinesByProduct = em.createNamedQuery("SaleOrderLine.findByProduct")
                .setParameter("productId", productId)
                .getResultList();

        return OrderLinesByProduct;
    }

    public List<Partner> findTopNCustomers(int n) {
        List result = em.createNamedQuery("Partner.findByCustomer")
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

    public List<SaleOrder> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(SaleOrder.class));
        return em.createQuery(cq).getResultList();
    }

}
