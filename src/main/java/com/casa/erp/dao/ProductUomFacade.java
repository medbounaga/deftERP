
package com.casa.erp.dao;


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
public class ProductUomFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;


    public ProductUom create(ProductUom entity) {
        
        em.persist(entity);
        return entity;
    }
    

    public ProductUom update(ProductUom entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(ProductUom entity) {
        em.remove(em.merge(entity));
    }

    public ProductUom find(Object id) {
        return em.find(ProductUom.class, id);
    }
    

    public List<ProductUom> findActiveUoms() {
        List<ProductUom> productUoms = em.createNamedQuery("ProductUom.findByActive")
                .setParameter("active", true)
                .getResultList();

        return productUoms;
    }

    
    public List<ProductUom> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(ProductUom.class));
        return em.createQuery(cq).getResultList();
    }

}
