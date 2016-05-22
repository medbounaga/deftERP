
package com.casa.erp.dao;

import com.casa.erp.entities.Journal;
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
public class JournalFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;


    public Journal create(Journal entity) {
        
        em.persist(entity);
        return entity;
    }
    

    public Journal update(Journal entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(Journal entity) {
        em.remove(em.merge(entity));
    }

    public Journal find(Object id) {
        return em.find(Journal.class, id);
    }
    
    
    public List<Journal> findJournalByType(String type) {
       List<Journal> journal = em.createNamedQuery("Journal.findByType")
             .setParameter("type", type)
             .getResultList();
        
        return journal;
    }
    
    public List<Journal> findByName(Object name) {
       List<Journal> journal = em.createNamedQuery("Journal.findByName")
             .setParameter("name", name)
             .getResultList();
        
        return journal;
    }   
       

    public List<Journal> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Journal.class));
        return em.createQuery(cq).getResultList();
    }

}
