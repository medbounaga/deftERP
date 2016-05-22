
package com.casa.erp.dao;

import com.casa.erp.entities.Inventory;
import com.casa.erp.entities.Product;
import com.casa.erp.entities.ProductCategory;
import com.casa.erp.entities.ProductUom;
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
public class ProductFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;    

    public Product create(Product entity) {
        em.persist(entity);
        return entity;
    }

    public Product update(Product entity) {
        em.merge(entity);
        return entity;
    }
    
    public Inventory update(Inventory entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(Product entity) {
        em.remove(em.merge(entity));
    }

    public Product find(Object id) {
        return em.find(Product.class, id);
    }
    
    public List<Product> findSoldProducts() {
        List<Product> products = em.createNamedQuery("Product.findBySaleOk")
                .getResultList();
        
        System.out.println("findSoldProducts: "+products.size());
        return products;
    }
    
    public List<Product> findPurchasedProducts() {
        List<Product> products = em.createNamedQuery("Product.findByPurchaseOk")
                .getResultList();
         System.out.println("findPurchasedProducts: "+products.size());
        return products;
    }
    
    public List<ProductCategory> findTopNProductCategories(int n) {
        List result = em.createNamedQuery("ProductCategory.findAll")
                .setMaxResults(n)
                .getResultList();

        return result;
    }
    
    public List<ProductUom> findTopNUnitsOfMeasure(int n) {
        List result = em.createNamedQuery("ProductUom.findAll")
                .setMaxResults(n)
                .getResultList();

        return result;
    }
    
    public Double countProductSales(Integer productId) {
        Double count = (Double) em.createNamedQuery("SaleOrderLine.countByProduct")
                .setParameter("productId", productId)
                .getSingleResult();

        if (count == null) {
            count = 0d;
        }
        return count;
    }
    
    public Double countProductPurchases(Integer productId) {
        Double count = (Double) em.createNamedQuery("PurchaseOrderLine.countByProduct")
                .setParameter("productId", productId)
                .getSingleResult();

        if (count == null) {
            count = 0d;
        }
        return count;
    }

    public List<Product> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Product.class));
        return em.createQuery(cq).getResultList();
    }

    public List<Product> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Product.class));
        javax.persistence.Query q = em.createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<Product> rt = cq.from(Product.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        javax.persistence.Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}
