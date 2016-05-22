
package com.casa.erp.dao;

import com.casa.erp.entities.Account;
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
public class AccountFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;

   
    public Account create(Account entity) {       
        em.persist(entity);
        return entity;
    }

    public Account update(Account entity) {
        em.merge(entity);
        return entity;
    }  

    public void remove(Account entity) {
        em.remove(em.merge(entity));
    }

    public Account find(Object id) {
        Account entity = em.find(Account.class, id);
        return entity;
    }
    
    public List<Account> findByType(String type) {
     List result = em.createNamedQuery("Account.findByType")
             .setParameter("type", type)
             .getResultList();
     
     return result;
    }  
    
    public List<Account> findByName(String name) {
     List result = em.createNamedQuery("Account.findByName")
             .setParameter("name", name)
             .getResultList();
     
     return result;
    } 

    public List<Account> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Account.class));
        return em.createQuery(cq).getResultList();
    }

    public Double getTotalCredit(Integer accountId) {
        Double totalCredit = (Double) em.createNamedQuery("JournalItem.CreditSum")
                .setParameter("accountId", accountId)
                .getSingleResult();

        if (totalCredit == null) {
            totalCredit = 0d;
        }
        return totalCredit;
    }
    


    public Double getTotalDebit(Integer accountId) {
         Double totalDebit = (Double) em.createNamedQuery("JournalItem.DebitSum")
                .setParameter("accountId", accountId)
                .getSingleResult();

        if (totalDebit == null) {
            totalDebit = 0d;
        }
        return totalDebit;
    }


}
