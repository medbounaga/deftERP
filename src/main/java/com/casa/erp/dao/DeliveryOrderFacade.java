
package com.casa.erp.dao;

import com.casa.erp.beans.util.IdGenerator;
import com.casa.erp.entities.DeliveryOrder;
import com.casa.erp.entities.Inventory;
import com.casa.erp.entities.Partner;
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
public class DeliveryOrderFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;

    private IdGenerator idGeerator = new IdGenerator();
    
    

    public DeliveryOrder createInDelivery(DeliveryOrder entity) {
        
        em.persist(entity);
        em.flush();
        entity.setName(idGeerator.generateDeliveryInId(entity.getId()));
        return entity;
    }
    
    public DeliveryOrder createOutDelivery(DeliveryOrder entity) {
        
        em.persist(entity);
        em.flush();
        entity.setName(idGeerator.generateDeliveryOutId(entity.getId()));
        return entity;
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

    public DeliveryOrder update(DeliveryOrder entity) {
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
    
    public void update(Inventory entity) {
        em.merge(entity);
    }
    
    public DeliveryOrder refresh(DeliveryOrder entity) {
        em.refresh(entity);
        return entity;
    }
      
    public List<DeliveryOrder> findBySaleId(Integer saleId) {
     List result = em.createNamedQuery("DeliveryOrder.findBySaleOrder")
             .setParameter("id", saleId)
             .getResultList();
     
     return result;
    }
    
    public List<DeliveryOrder> findByBackOrder(Integer id) {
     List result = em.createNamedQuery("DeliveryOrder.findByBackOrder")
             .setParameter("id", id)
             .getResultList();
     
     return result;
    }
    
     public Long countByBackOrder(Integer id) {
         Long count = (Long) em.createNamedQuery("DeliveryOrder.countByBackOrder")
                .setParameter("id", id)
                .getSingleResult();

        if (count == null) {
            count = 0L;
        }
        return count;   
    }
      
    
    public List<DeliveryOrder> findByPurchaseId(int purchaseId) {
     List result = em.createNamedQuery("DeliveryOrder.findByPurchaseOrder")
             .setParameter("id", purchaseId)
             .getResultList();
     
     return result;
    }
    
    public List<DeliveryOrder> findInDelivery() 
    {
        List<DeliveryOrder> inDeliveries =  em.createNamedQuery("DeliveryOrder.findInDelivery")
             .setParameter("type", "Purchase")
             .getResultList();        
        return inDeliveries;
    }
    
    public List<DeliveryOrder> findOutDelivery() 
    {
        List<DeliveryOrder> outDeliveries =  em.createNamedQuery("DeliveryOrder.findOutDelivery")
             .setParameter("type", "Sale")
             .getResultList();        
        return outDeliveries;
    }
    
    public List<DeliveryOrder> findByPartner(Integer partnerId, String type) {
        List<DeliveryOrder> deliveries = em.createNamedQuery("DeliveryOrder.findByPartner")
                .setParameter("partnerId", partnerId)
                .setParameter("type", type)
                .getResultList();

        return deliveries;
    }
    
     public SaleOrder updateSaleOrder(SaleOrder entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(DeliveryOrder entity) {
        em.remove(em.merge(entity));
        System.out.println("contains: "+em.contains(entity));
        em.detach(entity);
    }

    public DeliveryOrder find(Object id) {
        DeliveryOrder entity = em.find(DeliveryOrder.class, id);
//        em.refresh(entity);
        return entity;
    }
    
    
    public Inventory findInventory(Object id) {   
        Inventory inventory =  em.find(Inventory.class, id);
        em.detach(inventory);
        return inventory;  
    }
    
    public Boolean contains(DeliveryOrder entity) {
        return em.contains(entity);   
    }
    
    public SaleOrder findSaleOrder(Object id) {
        return em.find(SaleOrder.class, id);
    }
    
    public PurchaseOrder findPurchaseOrder(Object id) {
        return em.find(PurchaseOrder.class, id);
    }
    
    

    public List<DeliveryOrder> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(DeliveryOrder.class));
        return em.createQuery(cq).getResultList();
    }

}
