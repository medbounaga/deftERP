package com.defterp.dataAccess;

import com.defterp.modules.commonClasses.BaseEntity;
import com.defterp.modules.commonClasses.QueryWrapper;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateful;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.Query;

@Stateful
public class GenericDAO implements Serializable {

    private static final long serialVersionUID = -169258812805375171L;

    @PersistenceContext(type = PersistenceContextType.EXTENDED, name = "CasaERP_PU")
    private EntityManager entityManager;

    public <T> T save(T t) throws Exception {

        try {
            entityManager.persist(t);
            entityManager.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            entityManager.clear();
        }

        return t;
    }

    public <T> List<T> save(List<T> ts) throws Exception {

        List<T> list = new ArrayList();

        for (T t : ts) {
            list.add(save(t));
        }

        return list;
    }

    public <T> T update(T t) throws Exception {

        try {
            entityManager.merge(t);
            entityManager.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            entityManager.clear();
        }

        return t;
    }

    public <T> List<T> update(List<T> ts) throws Exception {

        List<T> list = new ArrayList();

        for (T t : ts) {
            list.add(update(t));
        }

        return list;
    }

    public <T> T delete(T t) throws Exception {

        try {
            entityManager.remove(entityManager.getReference(t.getClass(), ((BaseEntity) t).getId()));
            entityManager.flush();
        } catch (Exception e) {
            throw e;
        } finally {
            entityManager.clear();
        }

        return t;
    }

    public <T> List<T> delete(List<T> ts) throws Exception {

        List<T> list = new ArrayList();

        for (T t : ts) {
            list.add(delete(t));
        }

        return list;
    }

    public <T> T findById(Object id, Class c) {

        T entity = (T) entityManager.find(c, id);
        entityManager.detach(entity);
        return entity;
    }

    public <T> List<T> findWithNamedQuery(QueryWrapper qw) throws Exception {

        return findWithNamedQuery(qw, 0);
    }

    public <T> List<T> findWithNamedQuery(QueryWrapper qw, int resultLimit) throws Exception {

        Query query = entityManager.createNamedQuery(qw.getQuery());

        if (resultLimit > 0) {
            query.setMaxResults(resultLimit);
        }

        if (qw.getParameters() != null && !qw.getParameters().isEmpty()) {
            qw.getParameters().entrySet().forEach((e) -> {
                query.setParameter((String) e.getKey(), e.getValue());
            });
        }

        List<T> result = query.getResultList();

        entityManager.flush();
        entityManager.clear();

        return result;
    }

    public <T> T findSingleWithNamedQuery(QueryWrapper qw) throws Exception {

        Query query = entityManager.createNamedQuery(qw.getQuery());

        if (qw.getParameters() != null && !qw.getParameters().isEmpty()) {

            qw.getParameters().entrySet().forEach((entry) -> {
                query.setParameter(entry.getKey(), entry.getValue());
            });
        }

        Object result = query.getSingleResult();

        entityManager.flush();
        entityManager.clear();

        return (T) result;
    }

    public <T> List<T> findWithQuery(QueryWrapper qw) throws Exception {

        return findWithQuery(qw, 0);
    }

    public <T> List<T> findWithQuery(QueryWrapper qw, int resultLimit) throws Exception {

        Query query = entityManager.createQuery(qw.getQuery());

        if (resultLimit > 0) {
            query.setMaxResults(resultLimit);
        }

        if (qw.getParameters() != null && !qw.getParameters().isEmpty()) {
            qw.getParameters().entrySet().forEach((entry) -> {
                query.setParameter(entry.getKey(), entry.getValue());
            });
        }

        List<T> result = query.getResultList();

        entityManager.flush();
        entityManager.clear();

        return result;
    }

    public <T> T findSingleWithQuery(QueryWrapper qw) throws Exception {

        Query query = entityManager.createQuery(qw.getQuery());

        if (qw.getParameters() != null && !qw.getParameters().isEmpty()) {
            qw.getParameters().entrySet().forEach((entry) -> {
                query.setParameter(entry.getKey(), entry.getValue());
            });
        }

        T result = (T) query.getSingleResult();

        entityManager.flush();
        entityManager.clear();

        return result;
    }

    public <T> List<T> findWithNativeQuery(String nativeQuery) throws Exception {
        return findWithNativeQuery(nativeQuery, 0);
    }

    public <T> List<T> findWithNativeQuery(String nativeQuery, int resultLimit) throws Exception {

        Query query = entityManager.createNativeQuery(nativeQuery);

        if (resultLimit > 0) {
            query.setMaxResults(resultLimit);
        }

        List<T> result = query.getResultList();

        entityManager.flush();
        entityManager.clear();

        return result;
    }

    public <T> T findSingleWithNativeQuery(String nativeQuery) throws Exception {

        Query query = entityManager.createNativeQuery(nativeQuery);

        T result = (T) query.getSingleResult();

        entityManager.flush();
        entityManager.clear();

        return result;
    }
}
