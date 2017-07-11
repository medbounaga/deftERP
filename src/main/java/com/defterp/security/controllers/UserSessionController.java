package com.defterp.security.controllers;

import com.defterp.modules.users.entities.User;
import com.defterp.util.JsfUtil;
import com.defterp.dataAccess.GenericDAO;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.modules.users.queryBuilders.UserQueryBuilder;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private GenericDAO dataAccess;

    private static final long serialVersionUID = 7765876811740798583L;
    private User user;
    private String username;
    private String password;
    private boolean loggedIn;
    private QueryWrapper query;

    public String doLogin() {

        if (username == null || password == null) {
            JsfUtil.addErrorMessage("InvalidLogin");
            return "/sc/loginPage.xhtml";
        }

        query = UserQueryBuilder.getUserExistQuery(username, password);

        try {
            user = dataAccess.findSingleWithQuery(query);
        } catch (Exception ex) {
            Logger.getLogger(UserSessionController.class.getName()).log(Level.SEVERE, null, ex);
        }

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

}
