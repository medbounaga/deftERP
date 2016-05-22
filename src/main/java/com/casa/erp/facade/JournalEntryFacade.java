/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.casa.erp.facade;

import com.casa.erp.entities.JournalEntry;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author MOHAMMED
 */
@Stateless
public class JournalEntryFacade {

    @PersistenceContext(unitName = "com.casa_ERPapplication_war_1.0-SNAPSHOTPU")
    private EntityManager em;

    
    public JournalEntry find(Object id) {
        return em.find(JournalEntry.class, id);
    }
    

    public List<JournalEntry> findByPartner(Integer partnerId) {
        List<JournalEntry> JournalEntriesByPartner = em.createNamedQuery("JournalEntry.findByPartner")
                .setParameter("partnerId", partnerId)
                .getResultList();
        
        return JournalEntriesByPartner;  
    }
 
    public List<JournalEntry> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(JournalEntry.class));     
        return em.createQuery(cq).getResultList();
    }

}
