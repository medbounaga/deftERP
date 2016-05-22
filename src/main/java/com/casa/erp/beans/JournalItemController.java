package com.casa.erp.beans;

import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.entities.Journal;
import com.casa.erp.entities.JournalItem;
import com.casa.erp.dao.JournalItemFacade;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.joda.time.DateTime;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Named(value = "journalItemController")
@ViewScoped
public class JournalItemController implements Serializable {

    @Inject
    private JournalItemFacade journalItemFacade;

    private List<JournalItem> journalItems;
    private List<JournalItem> filteredJournalItems;
    private JournalItem journalItem;
    private String journalItemId;
    private List<Journal> journals;
    private Journal journal;
    private Integer period;
    String currentPage = "/sc/journalItem/List.xhtml";

    @PostConstruct
    public void init() {

        journals = journalItemFacade.findAllJournals();
        journalItems = journalItemFacade.findJournalItems(period, journal);
    }
    
    public void viewJournalItem(){
        if (journalItemId != null && JsfUtil.isNumeric(journalItemId)) {
            Integer id = Integer.valueOf(journalItemId);
            journalItem = journalItemFacade.find(id);
            if (journalItem != null) {
                journalItems = journalItemFacade.findJournalItems(period, journal);
                currentPage = "/sc/journalItem/View.xhtml";
                return;
            }
        }

        journalItems = journalItemFacade.findJournalItems(period, journal);
        journals = journalItemFacade.findAllJournals();
        currentPage = "/sc/journalItem/List.xhtml";
    }
    
    public void prepareView() {
        if (journalItem != null) {
            if (journalItemExist(journalItem.getId())) {
                currentPage = "/sc/journalItem/View.xhtml";
            }
        }
    }

    
     public void showJournalItemList() {
        journalItem = null;
//        journalItems = null;
        currentPage = "/sc/journalItem/List.xhtml";
    }

    public void showJournalItemForm() {
        if (journalItems.size() > 0) {
            journalItem = journalItems.get(0);
            currentPage = "/sc/journalItem/View.xhtml";
        }
    }

    public int getJournalItemIndex() {
        if (journalItems != null && journalItem != null) {
            return journalItems.indexOf(journalItem) + 1;
        }
        return 0;
    }

    public void nextJournalItem() {
        if (journalItems.indexOf(journalItem) == (journalItems.size() - 1)) {
            journalItem = journalItems.get(0);
        } else {
            journalItem = journalItems.get(journalItems.indexOf(journalItem) + 1);
        }
    }

    public void previousJournalItem() {
        if (journalItems.indexOf(journalItem) == 0) {
            journalItem = journalItems.get(journalItems.size() - 1);
        } else {
            journalItem = journalItems.get(journalItems.indexOf(journalItem) - 1);
        }
    }

    private boolean journalItemExist(Integer id) {
        if (id != null) {
            journalItem = journalItemFacade.find(id);
            if (journalItem == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                journalItems = journalItemFacade.findJournalItems(period, journal);
                filteredJournalItems = null;
                currentPage = "/sc/journalItem/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

   

    public void updateJournalItemsTable() {
        journalItems = journalItemFacade.findJournalItems(period, journal);
        filteredJournalItems = null;
    }

    public List<JournalItem> getJournalItems() {
        if(journalItems == null){
            journalItems = journalItemFacade.findJournalItems(period, journal);
            filteredJournalItems = null;       
        }
        return journalItems;
    }

    public void setJournalItems(List<JournalItem> journalItems) {
        this.journalItems = journalItems;
    }

    public JournalItem getJournalItem() {
        return journalItem;
    }

    public void setJournalItem(JournalItem journalItem) {
        this.journalItem = journalItem;
    }

    public List<JournalItem> getFilteredJournalItems() {
        return filteredJournalItems;
    }

    public void setFilteredJournalItems(List<JournalItem> filteredJournalItems) {
        this.filteredJournalItems = filteredJournalItems;
    }

    public String getPage() {
        return currentPage;
    }

    public void setPage(String page) {
        this.currentPage = page;
    }

    public List<Journal> getJournals() {
        return journals;
    }

    public void setJournals(List<Journal> journals) {
        this.journals = journals;
    }

    public Journal getJournal() {
        return journal;
    }

    public void setJournal(Journal journal) {
        this.journal = journal;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public String getJournalItemId() {
        return journalItemId;
    }

    public void setJournalItemId(String journalItemId) {
        this.journalItemId = journalItemId;
    }


    public Integer getYear() {
        return new DateTime().getYear();
    }

    public String getPeriodName(Date date) {
        
        DateTime dateTime = new DateTime(date);

        if (dateTime.getMonthOfYear() < 10) {
           
            return "X 0" + dateTime.getMonthOfYear() + "-" + dateTime.getYear();

        }else{
           
            return "X " + dateTime.getMonthOfYear() + "-" + dateTime.getYear();         
        }
    }
}
