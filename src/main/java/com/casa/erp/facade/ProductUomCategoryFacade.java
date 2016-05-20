
package com.casa.erp.facade;


import com.casa.erp.entities.ProductUomCategory;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author MOHAMMED
 */
@Stateless
public class ProductUomCategoryFacade {

    @PersistenceContext(unitName = "com.casa_ERPapplication_war_1.0-SNAPSHOTPU")
    private EntityManager em;


    public ProductUomCategory  create(ProductUomCategory  entity) {
        
        em.persist(entity);
        return entity;
    }
    

    public ProductUomCategory  update(ProductUomCategory  entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(ProductUomCategory  entity) {
        em.remove(em.merge(entity));
    }

    public ProductUomCategory  find(Object id) {
        return em.find(ProductUomCategory .class, id);
    }

    
    public List<ProductUomCategory > findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(ProductUomCategory.class));
        return em.createQuery(cq).getResultList();
    }

}
