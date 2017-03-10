package com.defterp.modules.accounting.controllers;

import com.defterp.modules.accounting.entities.Account;
import com.defterp.modules.accounting.queryBuilders.AccountQueryBuilder;
import com.defterp.modules.accounting.queryBuilders.JournalItemQueryBuilder;
import com.defterp.modules.common.AbstractController;
import com.defterp.util.JsfUtil;
import com.defterp.util.QueryWrapper;
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
        query = AccountQueryBuilder.getFindAllAccountsQuery();

        if (JsfUtil.isNumeric(accountId)) {

            Integer id = Integer.valueOf(accountId);
            account = findItemById(id, Account.class);

            if (account != null) {
                accounts = findWithQuery(query);
                return;
            }
        }
        
        accounts = findWithQuery(query);
        if (accounts != null && !accounts.isEmpty()) {
            account = accounts.get(0);
        }
    }

    public void createAccount() {
        if (account != null) {
            account.setName(account.getCode() + " " + account.getTitle());
            account = createItem(account);
            if (accounts != null && !accounts.isEmpty()) {
                accounts.add(account);
            }
            currentForm = VIEW_URL;
        }
    }

    public void prepareViewAccount() {
        if (account != null) {
            account = findItemById(account.getId(), account.getClass());
            if (account != null) {
                currentForm = VIEW_URL;
            } else {
                accountNotFound();
            }
        }
    }

    public void prepareUpdateAccount() {
        account = findItemById(account.getId(), account.getClass());
        if (account != null) {
            currentForm = EDIT_URL;
        } else {
            accountNotFound();
        }
    }

    public void prepareCreateAccount() {
        account = new Account();
        account.setActive(Boolean.TRUE);
        currentForm = CREATE_URL;
    }

    public void cancelUpdateAccount() {
        account = ((Account) findItemById(account.getId(), account.getClass()));
        if (account != null) {
            currentForm = VIEW_URL;
        } else {
            accountNotFound();
        }
    }

    public void cancelCreateAccount() {

        if ((accounts != null) && (!accounts.isEmpty())) {
            account = accounts.get(0);
            currentForm = VIEW_URL;
        } else {
            query = AccountQueryBuilder.getFindAllAccountsQuery();
            accounts = findWithQuery(query);
            if ((accounts != null) && (!accounts.isEmpty())) {
                account = accounts.get(0);
                currentForm = VIEW_URL;
            }
        }
    }

    public void deleteAccount() {
        account = findItemById(account.getId(), account.getClass());
        if (account != null) {
            boolean deleted = deleteItem(account);
            if (deleted) {
                if ((accounts != null) && (accounts.size() > 1)) {
                    accounts.remove(account);                   
                    account = accounts.get(0);
                    System.out.println("-----------first-------------");
                } else {
                    query = AccountQueryBuilder.getFindAllAccountsQuery();
                    accounts = findWithQuery(query);
                    if ((accounts != null) && (!accounts.isEmpty())) {
                        System.out.println("-----------second-------------");
                        account = accounts.get(0);
                    }
                }
                JsfUtil.addSuccessMessage("ItemDeleted");
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
            }
        } else {
            accountNotFound();
        }
    }

    public void updateAccount() {
        account = findItemById(account.getId(), account.getClass());
        if (account != null) {
            account.setName(account.getCode() + " " + account.getTitle());
            account = ((Account) updateItem(account));
            accounts.set(accounts.indexOf(account), account);
            currentForm = VIEW_URL;
        } else {
            accountNotFound();
        }
    }

    public List<Account> getReceivableAccounts() {
        query = AccountQueryBuilder.getFindByTypeQuery("Receivable");
        return super.findWithQuery(query);
    }

    public List<Account> getPayableAccounts() {
        query = AccountQueryBuilder.getFindByTypeQuery("Payable");
        return super.findWithQuery(query);
    }

    public List<Account> getProductSaleAccount() {
        query = AccountQueryBuilder.getFindByNameQuery("Product Sales");
        return super.findWithQuery(query);
    }

    public List<Account> getInvoiceAccounts() {
        query = AccountQueryBuilder.getFindByNameQuery("Account Receivable");
        return super.findWithQuery(query);
    }

    public List<Account> getBillAccounts() {
        query = AccountQueryBuilder.getFindByNameQuery("Account Payable");
        return super.findWithQuery(query);
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

    protected void accountNotFound() {
        JsfUtil.addWarningMessage("ItemDoesNotExist");

        if ((accounts != null) && (accounts.size() > 1)) {
            accounts.remove(account);
            account = accounts.get(0);
        } else {
            query = AccountQueryBuilder.getFindAllAccountsQuery();
            accounts = findWithQuery(query);
            if ((accounts != null) && (accounts.size() > 1)) {
                account = accounts.get(0);
            }
        }
        currentForm = VIEW_URL;
    }

    public void nextAccount() {
        if (accounts.indexOf(account) == accounts.size() - 1) {
            account = ((Account) accounts.get(0));
        } else {
            account = ((Account) accounts.get(accounts.indexOf(account) + 1));
        }
    }

    public void previousAccount() {
        if (accounts.indexOf(account) == 0) {
            account = ((Account) accounts.get(accounts.size() - 1));
        } else {
            account = ((Account) accounts.get(accounts.indexOf(account) - 1));
        }
    }

    public int getAccountIndex() {
        if ((accounts != null) && (account != null)) {
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
