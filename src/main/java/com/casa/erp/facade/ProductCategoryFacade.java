package com.casa.erp.facade;


import com.casa.erp.entities.ProductCategory;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Stateless
public class ProductCategoryFacade {

    @PersistenceContext(unitName = "com.casa_ERPapplication_war_1.0-SNAPSHOTPU")
    private EntityManager em;    

    public ProductCategory create(ProductCategory entity) {
        em.persist(entity);
        return entity;
    }

    public ProductCategory update(ProductCategory entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(ProductCategory entity) {
        em.remove(em.merge(entity));
    }

    public ProductCategory find(Object id) {
        return em.find(ProductCategory.class, id);
    }
    

    public List<ProductCategory> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(ProductCategory.class));
        return em.createQuery(cq).getResultList();
    }

    public List<ProductCategory> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(ProductCategory.class));
        javax.persistence.Query q = em.createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<ProductCategory> rt = cq.from(ProductCategory.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        javax.persistence.Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}
