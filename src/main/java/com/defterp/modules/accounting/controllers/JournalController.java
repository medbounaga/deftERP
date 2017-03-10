package com.defterp.modules.accounting.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.modules.accounting.entities.Journal;
import com.casa.erp.dao.JournalFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

/**
 *
 * @author MOHAMMED BOUNAGA
 *
 * github.com/medbounaga
 */
@Named(value = "journalController")
@ViewScoped
public class JournalController implements Serializable {

    @Inject
    private JournalFacade journalFacade;
    private List<Journal> paymentJournals;
    private List<Journal> journals;
    private List<Journal> filteredJournals;
    private Journal journal;
    private String journalId;
    private List<String> journalTypes;
    private String currentForm = "/sc/journal/View.xhtml";

    @PostConstruct
    public void init() {
        journalTypes = new ArrayList<>();
        journalTypes.add("Sale");
        journalTypes.add("Purchase");
        journalTypes.add("Cash");
        journalTypes.add("Bank");
    }

    public void resolveRequestParams() {

        currentForm = "/sc/journal/View.xhtml";

        if (JsfUtil.isNumeric(journalId)) {

            Integer id = Integer.valueOf(journalId);
            journal = journalFacade.find(id);

            if (journal != null) {
                journals = journalFacade.findAll();
                return;
            }
        }

        journals = journalFacade.findAll();
        if (journals != null && !journals.isEmpty()) {
            journal = journals.get(0);
        }
    }

    public void prepareCreateJournal() {
        journal = new Journal();
        journal.setActive(Boolean.TRUE);
        currentForm = "/sc/journal/Create.xhtml";
    }

    public void deleteJournal() {
        if (journalExist(journal.getId())) {
            try {
                journalFacade.remove(journal);
            } catch (Exception e) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                return;
            }

            JsfUtil.addSuccessMessage("ItemDeleted");
            currentForm = "/sc/journal/View.xhtml";

            if ((journals != null) && (journals.size() > 1)) {
                journals.remove(journal);
                journal = journals.get(0);
            } else {
                journals = journalFacade.findAll();
                if ((journals != null) && (!journals.isEmpty())) {
                    journal = journals.get(0);
                }
            }

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void cancelEditJournal() {
        if (journalExist(journal.getId())) {
            currentForm = "/sc/journal/View.xhtml";
        }
    }

    public void cancelCreateJournal() {
        currentForm = "/sc/journal/View.xhtml";

        if ((journals != null) && (!journals.isEmpty())) {
            journal = journals.get(0);
        } else {
            journals = journalFacade.findAll();
            if ((journals != null) && (!journals.isEmpty())) {
                journal = journals.get(0);
            }
        }
    }

    public void updateJournal() {
        if (journalExistTwo(journal.getId())) {
            journal = journalFacade.update(journal);
            journals.set(journals.indexOf(journal), journal);
            currentForm = "/sc/journal/View.xhtml";
        }
    }

    private boolean journalExistTwo(Integer id) {
        if (id != null) {
            Journal journ = journalFacade.find(id);
            if (journ == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                if ((journals != null) && (journals.size() > 1)) {
                    journals.remove(journal);
                    journal = journals.get(0);
                } else {
                    journals = journalFacade.findAll();
                    if ((journals != null) && (!journals.isEmpty())) {
                        journal = journals.get(0);
                    }
                }
                currentForm = "/sc/journal/View.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void createJournal() {
        if (journal != null) {
            journal = journalFacade.create(journal);

            if ((journals != null) && (!journals.isEmpty())) {
                journals.add(journal);
            } else {
                journals = journalFacade.findAll();
            }
            currentForm = "/sc/journal/View.xhtml";
        }
    }

    public void prepareEditJournal() {
        if (journalExist(journal.getId())) {
            currentForm = "/sc/journal/Edit.xhtml";
        }
    }

    private boolean journalExist(Integer id) {
        if (id != null) {
            journal = journalFacade.find(id);
            if (journal == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");

                if ((journals != null) && (journals.size() > 1)) {
                    journals.remove(journal);
                    journal = journals.get(0);
                } else {
                    journals = journalFacade.findAll();
                    if ((journals != null) && (!journals.isEmpty())) {
                        journal = journals.get(0);
                    }
                }
                currentForm = "/sc/journal/View.xhtml";
                return false;
            }
            return true;
        }
        return false;
    }

    public void prepareViewJournal() {

        if (journal != null && journalExist(journal.getId())) {
            currentForm = "/sc/journal/View.xhtml";
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

        paymentJournals = journalFacade.findJournalByType("Cash");
        if (paymentJournals != null) {
            paymentJournals.addAll(journalFacade.findJournalByType("Bank"));
        }

        return paymentJournals;

    }

    public List<Journal> getInvoiceJournals() {

        paymentJournals = journalFacade.findByName("Customer Invoices");
        return paymentJournals;

    }

    public List<Journal> getBillJournals() {

        paymentJournals = journalFacade.findByName("Vendor Bills");
        return paymentJournals;

    }

    public List<Journal> getJournals() {
        if (journals == null) {
            journals = journalFacade.findAll();
        }
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

    public String getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(String currentForm) {
        this.currentForm = currentForm;
    }

    public List<String> getJournalTypes() {
        return journalTypes;
    }

    public void setJournalTypes(List<String> journalTypes) {
        this.journalTypes = journalTypes;
    }

}
