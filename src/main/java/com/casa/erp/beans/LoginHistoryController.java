
package com.casa.erp.beans;

import com.casa.erp.entities.LoginHistory;
import com.casa.erp.facade.LoginHistoryFacade;
import java.io.Serializable;
import java.util.List;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

@Named(value = "loginHistoryController")
@ViewScoped
public class LoginHistoryController implements Serializable {

    @Inject
    private LoginHistoryFacade loginHistoryFacade;
    private List<LoginHistory> loginHistory;
    private List<LoginHistory> filteredLoginHistory;
    private String currentPage = "/sc/loginHistory/List.xhtml";


    public void viewLogin() {
        loginHistory = loginHistoryFacade.findAll();
        currentPage = "/sc/loginHistory/List.xhtml";
    }


    public List<LoginHistory> getLoginHistory() {
        if (loginHistory == null) {
            loginHistory = loginHistoryFacade.findAll();
        }
        return loginHistory;
    }

    public void setLoginHistory(List<LoginHistory> loginHistory) {
        this.loginHistory = loginHistory;
    }

    public List<LoginHistory> getFilteredLoginHistory() {
        return filteredLoginHistory;
    }

    public void setFilteredLoginHistory(List<LoginHistory> filteredLoginHistory) {
        this.filteredLoginHistory = filteredLoginHistory;
    }

    public String getPage() {
        return currentPage;
    }

    public void setPage(String currentPage) {
        this.currentPage = currentPage;
    }

}
