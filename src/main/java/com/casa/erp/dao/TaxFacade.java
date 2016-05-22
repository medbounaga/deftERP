
package com.casa.erp.dao;

import com.casa.erp.entities.Tax;

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
public class TaxFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;    

    public Tax create(Tax entity) {
        em.persist(entity);
        return entity;
    }

    public Tax update(Tax entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(Tax entity) {
        em.remove(em.merge(entity));
    }

    public Tax find(Object id) {
        return em.find(Tax.class, id);
    }
    
    public List<Tax> findSaleTaxes() {
        List<Tax> taxes = em.createNamedQuery("Tax.findByType")
                .setParameter("typeTaxUse", "Sale")
                .getResultList();
        return taxes;
    }
    
    public List<Tax> findPurchaseTaxes() {
        List<Tax> taxes = em.createNamedQuery("Tax.findByType")
                .setParameter("typeTaxUse", "Purchase")
                .getResultList();
        return taxes;
    }

    public List<Tax> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Tax.class));
        return em.createQuery(cq).getResultList();
    }

    public List<Tax> findRange(int[] range) {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Tax.class));
        javax.persistence.Query q = em.createQuery(cq);
        q.setMaxResults(range[1] - range[0] + 1);
        q.setFirstResult(range[0]);
        return q.getResultList();
    }

    public int count() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        javax.persistence.criteria.Root<Tax> rt = cq.from(Tax.class);
        cq.select(em.getCriteriaBuilder().count(rt));
        javax.persistence.Query q = em.createQuery(cq);
        return ((Long) q.getSingleResult()).intValue();
    }

}
