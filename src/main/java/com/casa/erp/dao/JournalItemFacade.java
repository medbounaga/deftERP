
package com.casa.erp.dao;

import com.casa.erp.entities.Journal;
import com.casa.erp.entities.JournalItem;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.joda.time.DateTime;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Stateless
public class JournalItemFacade {

    @PersistenceContext(unitName = "CasaERP_PU")
    private EntityManager em;

    public Journal findJournal(Object code) {
         List<Journal> journals = em.createNamedQuery("Journal.findByCode")
                .setParameter("code", code)
                .getResultList();

        if (journals != null && !journals.isEmpty()) {
            return journals.get(0);
        }

        return null;
    }


    public List<JournalItem> findJournalItems(Integer month, Journal journal) {

        DateTime date = new DateTime();
        Date monthStart = null;
        Date monthEnd = null;

        if (month != null) {
            monthStart = date.withMonthOfYear(month).dayOfMonth().withMinimumValue().toDate();
            monthEnd = date.withMonthOfYear(month).dayOfMonth().withMaximumValue().toDate();
        }
        Date yearStart = date.withMonthOfYear(1).dayOfMonth().withMinimumValue().toDate();
        Date yearEnd = date.withMonthOfYear(12).dayOfMonth().withMaximumValue().toDate();

        List<JournalItem> journalItems;

        if (month != null && journal != null) {

            journalItems = em.createNamedQuery("JournalItem.findByJournalPeriod")
                    .setParameter("monthStart", monthStart)
                    .setParameter("monthEnd", monthEnd)
                    .setParameter("journalId", journal.getId())
                    .getResultList();

            return journalItems;

        } else if (month != null && journal == null) {

            journalItems = em.createNamedQuery("JournalItem.findByPeriod")
                    .setParameter("monthStart", monthStart)
                    .setParameter("monthEnd", monthEnd)
                    .getResultList();

            return journalItems;

        } else if (month == null && journal != null) {

            journalItems = em.createNamedQuery("JournalItem.findByJournal")
                    .setParameter("yearStart", yearStart)
                    .setParameter("yearEnd", yearEnd)
                    .setParameter("journalId", journal.getId())
                    .getResultList();

            return journalItems;

        } else {

            journalItems = em.createNamedQuery("JournalItem.findAll").getResultList();
            return journalItems;
        }

    }

    public JournalItem find(Object id) {
        return em.find(JournalItem.class, id);
    }

    public List<JournalItem> findAll() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(JournalItem.class));
        return em.createQuery(cq).getResultList();
    }

    public List<Journal> findAllJournals() {
        javax.persistence.criteria.CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
        cq.select(cq.from(Journal.class));
        return em.createQuery(cq).getResultList();
    }

}
