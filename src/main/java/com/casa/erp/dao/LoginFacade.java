
package com.casa.erp.dao;
import com.casa.erp.entities.User;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */


@Stateless
public class LoginFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;

    public User userExist(String username, String password) {

        TypedQuery<User> query = em.createQuery("SELECT u FROM User u WHERE u.login = :login AND u.password = :password AND u.active = 1", User.class);

        query.setParameter("login", username);
        query.setParameter("password", password);
        try {
            User user = query.getSingleResult();
            System.out.println("User exists");
            return user;
        } catch (NoResultException e) {
            System.out.println("User doesn't exist");
            return null;
        }
    }


}
