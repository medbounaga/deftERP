package com.casa.erp.beans;

import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.beans.util.Status;
import com.casa.erp.entities.JournalEntry;
import com.casa.erp.dao.JournalEntryFacade;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Named(value = "journalEntryController")
@ViewScoped
public class JournalEntryController implements Serializable {

    @Inject
    private JournalEntryFacade journalEntryFacade;
    @Inject
    @Status
    private HashMap<String, String> statuses;
    private List<JournalEntry> journalEntries;
    private List<JournalEntry> filteredJournalEntries;
    private JournalEntry journalEntry;
    private String journalEntryId;
    private String partnerId;
    String currentPage = "/sc/journalEntry/List.xhtml";
    
    public void viewJournalEntry() {

        if (journalEntryId != null && JsfUtil.isNumeric(journalEntryId)) {
            Integer id = Integer.valueOf(journalEntryId);
            journalEntry = journalEntryFacade.find(id);
            if (journalEntry != null) {
                journalEntries = journalEntryFacade.findAll();
                currentPage = "/sc/journalEntry/View.xhtml";
                return;
            }
        }
        
        if (partnerId != null && JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            journalEntries = journalEntryFacade.findByPartner(id);
            if (!journalEntries.isEmpty()) {
                currentPage = "/sc/journalEntry/ListByPartner.xhtml";
                return;
            }
        }

        journalEntries = journalEntryFacade.findAll();
        currentPage = "/sc/journalEntry/List.xhtml";
    }


    public void prepareView() {

        if (journalEntry != null) {
            currentPage = "/sc/journalEntry/View.xhtml";
        }
    }

    public void showList() {
        
        journalEntry = null;       
        currentPage = "/sc/journalEntry/List.xhtml";
    }
    
    public void showForm() {
        
        if (journalEntries.size() > 0) {
            journalEntry = journalEntries.get(0);
            currentPage = "/sc/journalEntry/View.xhtml";
        }
    }
    
    public int getJournalEntryIndex() {
        return journalEntries.indexOf(journalEntry) + 1;
    }

    public void nextJournalEntry() {
        if (journalEntries.indexOf(journalEntry) == (journalEntries.size() - 1)) {
            journalEntry = journalEntries.get(0);
        } else {
            journalEntry = journalEntries.get(journalEntries.indexOf(journalEntry) + 1);
        }
    }

    public void previousJournalEntry() {
        if (journalEntries.indexOf(journalEntry) == 0) {
            journalEntry = journalEntries.get(journalEntries.size() - 1);
        } else {
            journalEntry = journalEntries.get(journalEntries.indexOf(journalEntry) - 1);
        }
    }
    
    public String getStatus(String status) {
        return statuses.get(status);
    }
    
    

    public String getStatusColor(String status) {       
        switch (status) {
            case "Posted":
                return "#009fd4";
            default:
                return "#6d8891";
        }
    }

    public List<JournalEntry> getJournalEntries() {
        if (journalEntries == null) {
            journalEntries = journalEntryFacade.findAll();
        }
        return journalEntries;
    }

    public void setJournalEntries(List<JournalEntry> journalEntries) {
        this.journalEntries = journalEntries;
    }

    public List<JournalEntry> getFilteredJournalEntries() {
        return filteredJournalEntries;
    }

    public void setFilteredJournalEntries(List<JournalEntry> filteredJournalEntries) {
        this.filteredJournalEntries = filteredJournalEntries;
    } 

    public JournalEntry getJournalEntry() {
        return journalEntry;
    }

    public void setJournalEntry(JournalEntry journalEntry) {
        this.journalEntry = journalEntry;
    }

    public String getPage() {
        return currentPage;
    }

    public void setPage(String page) {
        this.currentPage = page;
    }

    public String getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(String journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

}
