/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.casa.erp.facade;

import com.casa.erp.entities.LoginHistory;
import com.casa.erp.entities.User;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author MOHAMMED
 */
@Stateless
public class LoginFacade {

    @PersistenceContext(unitName = "com.casa_ERPapplication_war_1.0-SNAPSHOTPU")
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

    public void createLoginHistory(LoginHistory entity) {
        em.persist(entity);
    }

}
