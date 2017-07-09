package com.defterp.modules.accounting.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.modules.accounting.entities.Journal;
import com.defterp.modules.accounting.queryBuilders.JournalQueryBuilder;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author MOHAMMED BOUNAGA
 *
 * github.com/medbounaga
 */

@Named(value = "journalController")
@ViewScoped
public class JournalController extends AbstractController {

    private List<Journal> paymentJournals;
    private List<Journal> journals;
    private List<Journal> filteredJournals;
    private Journal journal;
    private String journalId;
    private List<String> journalTypes;
    private QueryWrapper query;

    public JournalController() {
        super("/sc/journal/");
    }

    @PostConstruct
    public void init() {

        journalTypes = new ArrayList<>();
        journalTypes.add("Sale");
        journalTypes.add("Purchase");
        journalTypes.add("Cash");
        journalTypes.add("Bank");
    }

    public void resolveRequestParams() {

        currentForm = VIEW_URL;

        if (JsfUtil.isNumeric(journalId)) {

            Integer id = Integer.valueOf(journalId);
            journal = super.findItemById(id, Journal.class);

            if (journal != null) {
                query = JournalQueryBuilder.getFindAllQuery();
                journals = super.findWithQuery(query);
                return;
            }
        }
        
        query = JournalQueryBuilder.getFindAllQuery();
        journals = super.findWithQuery(query);
        
        if (journals != null && !journals.isEmpty()) {
            journal = journals.get(0);
        }
    }

    public void prepareCreateJournal() {
        journal = new Journal();
        journal.setActive(Boolean.TRUE);
        currentForm = CREATE_URL;
    }

    public void deleteJournal() {

        journal = super.findItemById(journal.getId(), journal.getClass());

        if (journal != null) {

            boolean deleted = super.deleteItem(journal);

            if (deleted) {
                if ((journals != null) && (journals.size() > 1)) {
                    journals.remove(journal);
                    journal = journals.get(0);
                } else {
                    query = JournalQueryBuilder.getFindAllQuery();
                    journals = super.findWithQuery(query);
                    if ((journals != null) && (!journals.isEmpty())) {
                        journal = journals.get(0);
                    }
                }
                JsfUtil.addSuccessMessage("ItemDeleted");
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
            }
        } else {
            journalNotFound();
        }
    }

    public void cancelEditJournal() {

        journal = super.findItemById(journal.getId(), journal.getClass());
        if (journal != null) {
            currentForm = VIEW_URL;
        } else {
            journalNotFound();
        }
    }

    public void cancelCreateJournal() {

        if ((journals != null) && (!journals.isEmpty())) {
            journal = journals.get(0);
            currentForm = VIEW_URL;
        } else {
            query = JournalQueryBuilder.getFindAllQuery();
            journals = super.findWithQuery(query);
            if ((journals != null) && (!journals.isEmpty())) {
                journal = journals.get(0);
                currentForm = VIEW_URL;
            }
        }
    }

    public void updateJournal() {

        Journal journ = super.findItemById(journal.getId(), journal.getClass());
        if (journ != null) {
            journal = super.updateItem(journal);
            journals.set(journals.indexOf(journal), journal);
            currentForm = VIEW_URL;
        } else {
            journalNotFound();
        }
    }

    public void createJournal() {
        
        if (journal != null) {
            journal = super.createItem(journal);
            if (journals != null) {
                journals.add(journal);
            }else{
                journals = super.findWithQuery(JournalQueryBuilder.getFindAllQuery());
            }
            currentForm = VIEW_URL;
        }
    }

    public void prepareEditJournal() {

        journal = super.findItemById(journal.getId(), journal.getClass());
        if (journal != null) {
            currentForm = EDIT_URL;
        } else {
            journalNotFound();
        }
    }

    public void prepareViewJournal() {

        if (journal != null) {
            journal = super.findItemById(journal.getId(), journal.getClass());
            if (journal != null) {
                currentForm = VIEW_URL;
            } else {
                journalNotFound();
            }
        }
    }

    protected void journalNotFound() {

        JsfUtil.addWarningMessage("ItemDoesNotExist");
        currentForm = VIEW_URL;

        if ((journals != null) && (journals.size() > 1)) {
            journals.remove(journal);
            journal = journals.get(0);
        } else {
            query = JournalQueryBuilder.getFindAllQuery();
            journals = super.findWithQuery(query);
            if ((journals != null) && (journals.size() > 1)) {
                journal = journals.get(0);
            }
        }
    }

    public int getJournalIndex() {

        if (journals != null && journal != null) {
            return journals.indexOf(journal) + 1;
        }
        return 0;
    }

    public void nextJournal() {
        if (journals.indexOf(journal) == (journals.size() - 1)) {
            journal = journals.get(0);
        } else {
            journal = journals.get(journals.indexOf(journal) + 1);
        }
    }

    public void previousJournal() {
        if (journals.indexOf(journal) == 0) {
            journal = journals.get(journals.size() - 1);
        } else {
            journal = journals.get(journals.indexOf(journal) - 1);
        }
    }

    public List<Journal> getPaymentJournals() {

        query = JournalQueryBuilder.getFindByTypeQuery("Cash");
        paymentJournals = super.findWithQuery(query);

        query = JournalQueryBuilder.getFindByTypeQuery("Bank");

        if (paymentJournals != null) {
            paymentJournals.addAll((List<Journal>)(Journal)super.findWithQuery(query));
        }

        return paymentJournals;
    }

    public List<Journal> getInvoiceJournals() {

        query = JournalQueryBuilder.getFindByNameQuery("Customer Invoices");
        return super.findWithQuery(query);
    }

    public List<Journal> getBillJournals() {

        query = JournalQueryBuilder.getFindByNameQuery("Vendor Bills");
        return super.findWithQuery(query);

    }

    public List<Journal> getJournals() {
        return journals;
    }

    public void setJournals(List<Journal> journals) {
        this.journals = journals;
    }

    public List<Journal> getFilteredJournals() {
        return filteredJournals;
    }

    public void setFilteredJournals(List<Journal> filteredJournals) {
        this.filteredJournals = filteredJournals;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public String getJournalId() {
        return journalId;
    }

    public void setJournalId(String journalId) {
        this.journalId = journalId;
    }

    public List<String> getJournalTypes() {
        return journalTypes;
    }

    public void setJournalTypes(List<String> journalTypes) {
        this.journalTypes = journalTypes;
    }

}
