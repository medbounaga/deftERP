
package com.casa.erp.beans;

import java.io.Serializable;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */
 

@Named(value = "navigationBean")
@SessionScoped
public class NavigationController implements Serializable {
 
    private static final long serialVersionUID = 1L;
 
    /**
     * Redirect to login page.
     * @return Login page name.
     */
    public String redirectToLogin() {
        return "/login.xhtml?faces-redirect=true";
    }
     
    /**
     * Go to login page.
     * @return Login page name.
     */
    public String toLogin() {
        return "/login.xhtml";
    }
     
    /**
     * Redirect to info page.
     * @return Info page name.
     */
    public String redirectToInfo() {
        return "/info.xhtml?faces-redirect=true";
    }
     
    /**
     * Go to info page.
     * @return Info page name.
     */
    public String toInfo() {
        return "/info.xhtml";
    }
     
    /**
     * Redirect to welcome page.
     * @return Welcome page name.
     */
    public String redirectToWelcome() {
//        return "/secured/welcome.xhtml?faces-redirect=true";
        System.out.println("redirectToWelcome  ");
        return "/sc/dashboard.xhtml?faces-redirect=true";
    }
     
    /**
     * Go to welcome page.
     * @return Welcome page name.
     */
    public String toWelcome() {
        return "/sc/dashboard.xhtml";
    }
     
}
