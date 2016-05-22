
package com.casa.erp.dao;


import com.casa.erp.entities.User;
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
public class UserFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;


    public User create(User entity) {
        
        em.persist(entity);
        return entity;
    }
    

    public User update(User entity) {
        em.merge(entity);
        return entity;
    }

    public void remove(User entity) {
        em.remove(em.merge(entity));
    }

    public User find(Object id) {
        return em.find(User.class, id);
    }

    
    public List<User> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(User.class));
        return em.createQuery(cq).getResultList();
    }

}
