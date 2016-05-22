package com.casa.erp.beans;

import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.entities.User;
import com.casa.erp.dao.UserFacade;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Named(value = "userController")
@ViewScoped
public class UserController implements Serializable {

    @Inject
    private UserFacade userFacade;
    private User user;
    private List<User> users;
    private List<User> filteredUsers;
    private List<String> userTypes;
    private String userId;
    private Part image;
    private boolean imageModified;
    private String currentPage = "/sc/userInfo/List.xhtml";

    @PostConstruct
    public void init() {
        userTypes = new ArrayList<>();
        userTypes.add("Admin");
        userTypes.add("User");
    }

    public void setImage(Part image) {
        if (image != null) {
            try {
                InputStream input = image.getInputStream();
                user.setImage(IOUtils.toByteArray(input));
            } catch (IOException ex) {
                Logger.getLogger(UserController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (image == null && user.getImage() != null && imageModified == true) {
            user.setImage(null);
        }
    }

    public Part getImage() {
        return image;
    }

    public void validateImage(FacesContext ctx, UIComponent comp, Object value) {
        if (value != null) {
            Part file = (Part) value;

            if (!file.getContentType().startsWith("image")) {
                String msg = JsfUtil.getBundle().getString("NotImage");
                JsfUtil.throwWarningValidatorException(msg);
            }
            if (file.getSize() > 200024) {
                String msg = JsfUtil.getBundle().getString("ImageTooBig");
                JsfUtil.throwWarningValidatorException(msg);
            }
        }
    }

    public String getFormDefaultImage() {

        int modulos = user.getId() % 5;
        switch (modulos) {
            case 0:
                return "img/partnerPlaceholder.png";
            case 1:
                return "img/partnerPlaceholder1.png";
            case 2:
                return "img/partnerPlaceholder2.png";
            case 3:
                return "img/partnerPlaceholder3.png";
            default:
                return "img/partnerPlaceholder4.png";
        }

    }

    public void deleteUser(Integer id) {
        if (userExist(id)) {
            try {
                userFacade.remove(user);
            } catch (Exception e) {
                System.out.println("Error Delete: "+e.getMessage());
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                return;
            }

            JsfUtil.addSuccessMessage("ItemDeleted");
            currentPage = "/sc/userInfo/List.xhtml";
            users.remove(user);
            user = null;

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void cancelEditUser(Integer id) {
        if (userExist(id)) {
            currentPage = "/sc/userInfo/View.xhtml";
        }
    }

    public void updateUser(Integer id) {
        if (userExistTwo(id)) {
            user = userFacade.update(user);
            users = userFacade.findAll();
            currentPage = "/sc/userInfo/View.xhtml";
        }
    }

    private boolean userExistTwo(Integer id) {
        if (id != null) {
            User user = userFacade.find(id);
            if (user == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                users = null;
                currentPage = "/sc/userInfo/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void prepareCreateUser() {
        user = new User();
        user.setActive(Boolean.TRUE);
        currentPage = "/sc/userInfo/Create.xhtml";
    }

    public void viewUser() {
        if (userId != null && JsfUtil.isNumeric(userId)) {
            Integer id = Integer.valueOf(userId);
            user = userFacade.find(id);
            if (user != null) {
                users = userFacade.findAll();
                currentPage = "/sc/userInfo/View.xhtml";
                return;
            }
        }

        users = userFacade.findAll();
        currentPage = "/sc/userInfo/List.xhtml";
    }

    public void createUser() {
        if (user != null) {
            user = userFacade.create(user);
            users = userFacade.findAll();
            currentPage = "/sc/userInfo/View.xhtml";
        }
    }

    public void prepareEditUser(Integer id) {
        if (userExist(id)) {
            imageModified = false;
            currentPage = "/sc/userInfo/Edit.xhtml";
        }
    }

    private boolean userExist(Integer id) {
        if (id != null) {
            user = userFacade.find(id);
            if (user == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                users = null;
                currentPage = "/sc/userInfo/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void prepareView() {

        if (user != null) {
            if (userExist(user.getId())) {
                currentPage = "/sc/userInfo/View.xhtml";
            }
        }
    }

    public void showUserList() {
        user = null;
        currentPage = "/sc/userInfo/List.xhtml";
    }

    public void showUserForm() {
        if (users.size() > 0) {
            user = users.get(0);
            currentPage = "/sc/userInfo/View.xhtml";
        }
    }

    public int getUserIndex() {
        if (users != null && user != null) {
            return users.indexOf(user) + 1;
        }
        return 0;
    }

    public void nextUser() {
        if (users.indexOf(user) == (users.size() - 1)) {
            user = users.get(0);
        } else {
            user = users.get(users.indexOf(user) + 1);
        }
    }

    public void previousUser() {
        if (users.indexOf(user) == 0) {
            user = users.get(users.size() - 1);
        } else {
            user = users.get(users.indexOf(user) - 1);
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<User> getUsers() {
        if (users == null) {
            users = userFacade.findAll();
        }

        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<User> getFilteredUsers() {
        return filteredUsers;
    }

    public void setFilteredUsers(List<User> filteredUsers) {
        this.filteredUsers = filteredUsers;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPage() {
        return currentPage;
    }

    public void setPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public boolean getImageModified() {
        return imageModified;
    }

    public void setImageModified(boolean imageModified) {
        this.imageModified = imageModified;
    }

    public List<String> getUserTypes() {
        return userTypes;
    }

    public void setUserTypes(List<String> userTypes) {
        this.userTypes = userTypes;
    }

}
