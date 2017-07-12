package com.defterp.modules.accounting.controllers;

import com.defterp.modules.accounting.entities.Account;
import com.defterp.modules.accounting.queryBuilders.AccountQueryBuilder;
import com.defterp.modules.accounting.queryBuilders.JournalItemQueryBuilder;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.util.JsfUtil;
import com.defterp.modules.commonClasses.QueryWrapper;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named("accountController")
@ViewScoped
public class AccountController extends AbstractController {

    protected Account account;
    protected List<Account> accounts;
    protected List<Account> filteredAccounts;
    protected String accountId;
    private List<String> accountTypes;
    private QueryWrapper query;

    public AccountController() {
        super("/sc/account/");
    }

    @PostConstruct
    public void init() {

        accountTypes = new ArrayList();
        accountTypes.add("Receivable");
        accountTypes.add("Payable");
        accountTypes.add("Current Liabilities");
        accountTypes.add("Income");
        accountTypes.add("Bank and Cash");
        accountTypes.add("Expenses");
        accountTypes.add("Current Assets");
    }

    public void resolveRequestParams() {

        currentForm = VIEW_URL;
        query = AccountQueryBuilder.getFindAllQuery();

        if (JsfUtil.isNumeric(accountId)) {

            Integer id = Integer.valueOf(accountId);
            account = super.findItemById(id, Account.class);

            if (account != null) {
                accounts = super.findWithQuery(query);
                return;
            }
        }

        accounts = super.findWithQuery(query);
        if (accounts != null && !accounts.isEmpty()) {
            account = accounts.get(0);
        }
    }

    public void createAccount() {
        if (account != null) {
            account.setName(account.getCode() + " " + account.getTitle());
            account = super.createItem(account);
            if (accounts != null && !accounts.isEmpty()) {
                accounts.add(account);
            }
            currentForm = VIEW_URL;
        }
    }

    public void prepareViewAccount() {
        if (account != null) {
            account = super.findItemById(account.getId(), account.getClass());
            if (account != null) {
                currentForm = VIEW_URL;
            } else {
                ItemNotFound();
            }
        }
    }

    public void prepareUpdateAccount() {
        account = super.findItemById(account.getId(), account.getClass());
        if (account != null) {
            currentForm = EDIT_URL;
        } else {
            ItemNotFound();
        }
    }

    public void prepareCreateAccount() {
        account = new Account();
        currentForm = CREATE_URL;
    }

    public void cancelUpdateAccount() {
        account = super.findItemById(account.getId(), Account.class);
        if (account != null) {
            currentForm = VIEW_URL;
        } else {
            ItemNotFound();
        }
    }

    public void cancelCreateAccount() {
        currentForm = VIEW_URL;
        resetListAndCurrentItem();
    }

    public void deleteAccount() {
        
        account = super.findItemById(account.getId(), account.getClass());
        
        if (account != null) {
            
            boolean deleted = super.deleteItem(account);
            
            if (deleted) {

                JsfUtil.addSuccessMessage("ItemDeleted");
                currentForm = VIEW_URL;

                if (accounts != null && account != null) {
                    accounts.remove(account);
                }

                resetListAndCurrentItem();

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
            }
        } else {
            ItemNotFound();
        }
    }

    public void updateAccount() {
        Account tempAcc = super.findItemById(account.getId(), account.getClass());
        if (tempAcc != null) {
            account.setName(account.getCode() + " " + account.getTitle());
            account = super.updateItem(account);
            accounts.set(accounts.indexOf(account), account);
            currentForm = VIEW_URL;
        } else {
            ItemNotFound();
        }
    }

    public Double getTotalCredit() {
        if (account != null) {
            query = JournalItemQueryBuilder.getFindCreditSumByAccountQuery(account.getId());
            Double totalCredit = (Double) super.findSingleWithQuery(query);
            return JsfUtil.round(totalCredit);
        }
        return 0d;
    }

    public Double getTotalDebit() {
        if (account != null) {
            query = JournalItemQueryBuilder.getFindDebitSumByAccountQuery(account.getId());
            Double totalDebit = (Double) super.findSingleWithQuery(query);
            return JsfUtil.round(totalDebit);
        }
        return 0d;
    }

    private void ItemNotFound() {

        currentForm = VIEW_URL;
        JsfUtil.addWarningMessage("ItemDoesNotExist");

        if (accounts != null && account != null) {
            accounts.remove(account);
        }

        resetListAndCurrentItem();
    }

    private void resetListAndCurrentItem() {

        if (accounts != null && !accounts.isEmpty()) {
            account = accounts.get(0);
        } else {
            query = AccountQueryBuilder.getFindAllQuery();
            accounts = super.findWithQuery(query);

            if ((accounts != null) && !accounts.isEmpty()) {
                account = accounts.get(0);
            }
        }
    }

    public void nextAccount() {
        if (accounts.indexOf(account) == accounts.size() - 1) {
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

    public int getAccountIndex() {
        if (accounts != null && account != null) {
            return accounts.indexOf(account) + 1;
        }
        return 0;
    }

    public List<String> getAccountTypes() {
        return accountTypes;
    }

    public void setAccountTypes(List<String> accountTypes) {
        this.accountTypes = accountTypes;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public List<Account> getAccounts() {
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
