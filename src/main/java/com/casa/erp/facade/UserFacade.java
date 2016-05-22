
package com.casa.erp.facade;


import com.casa.erp.entities.User;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author MOHAMMED
 */
@Stateless
public class UserFacade {

    @PersistenceContext(unitName = "com.casa_ERPapplication_war_1.0-SNAPSHOTPU")
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
