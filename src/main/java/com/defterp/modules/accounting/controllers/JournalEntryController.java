package com.defterp.modules.accounting.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.translation.annotations.Status;
import com.defterp.modules.accounting.entities.JournalEntry;
import com.defterp.modules.accounting.queryBuilders.JournalEntryQueryBuilder;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
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
public class JournalEntryController extends AbstractController {

    @Inject
    @Status
    private HashMap<String, String> statuses;
    private List<JournalEntry> journalEntries;
    private List<JournalEntry> filteredJournalEntries;
    private JournalEntry journalEntry;
    private String journalEntryId;
    private String partnerId;
    private QueryWrapper query;

    public JournalEntryController() {
        super("/sc/journalEntry/");
    }

    public void resolveRequestParams() {

        if (JsfUtil.isNumeric(journalEntryId)) {
            Integer id = Integer.valueOf(journalEntryId);
            journalEntry = super.findItemById(id, JournalEntry.class);
            if (journalEntry != null) {
                query = JournalEntryQueryBuilder.getFindAllQuery();
                journalEntries = super.findWithQuery(query);
                currentForm = VIEW_URL;
                return;
            }
        }

        query = JournalEntryQueryBuilder.getFindAllQuery();
        journalEntries = super.findWithQuery(query);

        if ((journalEntries != null) && (!journalEntries.isEmpty())) {
            journalEntry = journalEntries.get(0);
        }
        currentForm = VIEW_URL;
    }

    public void prepareView() {

        if ((journalEntry != null) && (journalEntryExist(journalEntry.getId()))) {
            currentForm = VIEW_URL;
        }
    }

    private boolean journalEntryExist(Integer id) {
        if (id != null) {
            journalEntry = super.findItemById(id, JournalEntry.class);
            if (journalEntry == null) {
                
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = VIEW_URL;

                if ((journalEntries != null) && (journalEntries.size() > 1)) {
                    journalEntries.remove(journalEntry);
                    journalEntry = journalEntries.get(0);
                } else {
                    query = JournalEntryQueryBuilder.getFindAllQuery();
                    journalEntries = super.findWithQuery(query);
                    if ((journalEntries != null) && (!journalEntries.isEmpty())) {
                        journalEntry = journalEntries.get(0);
                    }
                }
                
                return false;
            }
            return true;
        }
        return false;
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
