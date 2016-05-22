
package com.casa.erp.facade;

import com.casa.erp.entities.LoginHistory;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author MOHAMMED
 */
@Stateless
public class LoginHistoryFacade {

    @PersistenceContext(unitName = "com.casa_ERPapplication_war_1.0-SNAPSHOTPU")
    private EntityManager em;
    

    public void remove(LoginHistory entity) {
        em.remove(em.merge(entity));
    }

    public LoginHistory find(Object id) {
        LoginHistory entity = em.find(LoginHistory.class, id);
        return entity;
    }


    public List<LoginHistory> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(LoginHistory.class));
        return em.createQuery(cq).getResultList();
    }


}
