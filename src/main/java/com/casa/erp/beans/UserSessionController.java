package com.casa.erp.beans;

import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.entities.User;
import com.casa.erp.dao.LoginFacade;
import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Named(value = "userSessionController")
@SessionScoped
public class UserSessionController implements Serializable {

    @Inject
    private LoginFacade loginFacade;

    private static final long serialVersionUID = 7765876811740798583L;
    private User user;
    private String username;
    private String password;
    private boolean loggedIn;

    public String doLogin() {

        if (username == null || password == null) {
            JsfUtil.addErrorMessage("InvalidLogin");
            return "/sc/loginPage.xhtml";

        } 
        
        user = loginFacade.userExist(username, password);
        
        if (user != null) {           
            loggedIn = true;
            return "/sc/dashboard.xhtml?faces-redirect=true";

        } 
            
        JsfUtil.addErrorMessage("InvalidLogin");
        return "/sc/loginPage.xhtml";

    }


    public String doLogout() {
        user = null;
        loggedIn = false;
        return "/sc/loginPage.xhtml";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoggedIn() {
        if (loggedIn == false) {
            System.out.println("FacesContext instance is null: " + (FacesContext.getCurrentInstance() == null));
            if (FacesContext.getCurrentInstance() != null) {
                HttpServletRequest requestObj = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
                requestObj.getSession().invalidate();
            }

        }

        System.out.println("LogedIn: " + loggedIn);
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
