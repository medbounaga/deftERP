package com.casa.erp.beans;

import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.entities.Account;
import com.casa.erp.dao.AccountFacade;
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


@Named(value = "accountController")
@ViewScoped
public class AccountController implements Serializable {

    @Inject
    private AccountFacade accountFacade;
    private Account account;
    private List<Account> accounts;
    private List<Account> filteredAccounts;
    private String currentPage = "/sc/account/List.xhtml";
    private String accountId;
    private List<String> accountTypes;
    
    @PostConstruct
    public void init(){
        accountTypes = new ArrayList<>();
        accountTypes.add("Receivable"); 
        accountTypes.add("Payable"); 
        accountTypes.add("Current Liabilities"); 
        accountTypes.add("Income"); 
        accountTypes.add("Bank and Cash"); 
        accountTypes.add("Expenses");
        accountTypes.add("Current Assets"); 

        
    }

    public void viewAccount() {
        if (accountId != null && JsfUtil.isNumeric(accountId)) {
            Integer id = Integer.valueOf(accountId);
            account = accountFacade.find(id);
            if (account != null) {
                accounts = accountFacade.findAll();
                currentPage = "/sc/account/View.xhtml";
                return;
            }
        }

        accounts = accountFacade.findAll();
        currentPage = "/sc/account/List.xhtml";
    }

    public void createAccount() {
        if (account != null) {
            account.setName(account.getCode()+" "+account.getTitle());
            account = accountFacade.create(account);
            accounts = accountFacade.findAll();
            currentPage = "/sc/account/View.xhtml";
        }
    }

    public void prepareEditAccount(Integer id) {
        if (accountExist(id)) {
            currentPage = "/sc/account/Edit.xhtml";
        }
    }

    public void prepareCreateAccount() {
        account = new Account();
        account.setActive(Boolean.TRUE);
        currentPage = "/sc/account/Create.xhtml";
    }

    public void cancelEditAccount(Integer id) {
        if (accountExist(id)) {
            currentPage = "/sc/account/View.xhtml";
        }
    }
    
    public void deleteAccount(Integer id) {
        if (accountExist(id)) {
            try {
                accountFacade.remove(account);
            } catch (Exception e) {
                System.out.println("Error Delete: "+e.getMessage());
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                return;
            }
            
            JsfUtil.addSuccessMessage("ItemDeleted");
            currentPage = "/sc/account/List.xhtml";
            accounts.remove(account);
            account = null;
            
        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void updateAccount(Integer id) {
        if (accountExistTwo(id)) {
            account.setName(account.getCode()+" "+account.getTitle());
            account = accountFacade.update(account);
            accounts = accountFacade.findAll();
            currentPage = "/sc/account/View.xhtml";
        }
    }

    private boolean accountExist(Integer id) {
        if (id != null) {
            account = accountFacade.find(id);
            if (account == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                accounts = null;
                currentPage = "/sc/account/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }
    
    private boolean accountExistTwo(Integer id) {
        if (id != null) {
            Account account = accountFacade.find(id);
            if (account == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                accounts = null;
                currentPage = "/sc/account/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void prepareView() {

        if (account != null) {
            if (accountExist(account.getId())) {
                currentPage = "/sc/account/View.xhtml";
            }
        }
    }

    public void showAccountList() {
        account = null;
        currentPage = "/sc/account/List.xhtml";
    }

    public void showAccountForm() {
        if (accounts.size() > 0) {
            account = accounts.get(0);
            currentPage = "/sc/account/View.xhtml";
        }
    }

    public int getAccountIndex() {
        if (accounts != null && account != null) {
            return accounts.indexOf(account) + 1;
        }
        return 0;
    }

    public void nextAccount() {
        if (accounts.indexOf(account) == (accounts.size() - 1)) {
            account = accounts.get(0);
        } else {
            account = accounts.get(accounts.indexOf(account) + 1);
        }
    }

    public void previousAccount() {
        if (accounts.indexOf(account) == 0) {
            account = accounts.get(accounts.size() - 1);
        } else {
            account = accounts.get(accounts.indexOf(account) - 1);
        }
    }

    public List<Account> getReceivableAccounts() {
        
        return accountFacade.findByType("Receivable");
    }

    public List<Account> getPayableAccounts() {
        
        return accountFacade.findByType("Payable");
    }

    public List<Account> getProductSaleAccount() {

        return accountFacade.findByName("Product Sales");
    }

    public List<Account> getInvoiceAccounts() {

        return accountFacade.findByName("Account Receivable");
    }

    public List<Account> getBillAccounts() {

        return accountFacade.findByName("Account Payable");
    }
    
    public Double getTotalCredit() {
        if (account != null) {
            return JsfUtil.round(accountFacade.getTotalCredit(account.getId()));
        }
        return 0d;
    }
    
    public Double getTotalDebit() {
        if (account != null) {
            return JsfUtil.round(accountFacade.getTotalDebit(account.getId()));
        }
        return 0d;
    }
    
    

    public List<Account> getAccounts() {
        if (accounts == null) {
            accounts = accountFacade.findAll();
        }
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public List<Account> getFilteredAccounts() {
        return filteredAccounts;
    }

    public void setFilteredAccounts(List<Account> filteredAccounts) {
        this.filteredAccounts = filteredAccounts;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getPage() {
        return currentPage;
    }

    public void setPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public List<String> getAccountTypes() {
        return accountTypes;
    }

    public void setAccountTypes(List<String> accountTypes) {
        this.accountTypes = accountTypes;
    }

}
