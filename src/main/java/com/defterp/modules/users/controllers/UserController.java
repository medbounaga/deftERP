package com.defterp.modules.users.controllers;

import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.util.JsfUtil;
import com.defterp.modules.users.entities.User;
import com.defterp.modules.users.queryBuilders.UserQueryBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
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
public class UserController extends AbstractController {

    private User user;
    private List<User> users;
    private List<User> filteredUsers;
    private List<String> userTypes;
    private String userId;
    private Part image;
    private boolean imageModified;
    private String currentForm = "/sc/userInfo/View.xhtml";
    private QueryWrapper query;

    public UserController() {
        super("/sc/userInfo/");
    }

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

    public void deleteUser() {
        if (userExist(user.getId())) {

            boolean deleted = super.deleteItem(user);

            if (deleted) {

                JsfUtil.addSuccessMessage("ItemDeleted");
                currentForm = VIEW_URL;

                if (users != null && users.size() > 1) {
                    users.remove(user);
                    user = users.get(0);
                } else {
                    query = UserQueryBuilder.getFindAllQuery();
                    users = super.findWithQuery(query);
                    if ((users != null) && (!users.isEmpty())) {
                        user = users.get(0);
                    }
                }
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
            }

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void cancelUpdateUser() {
        if (userExist(user.getId())) {
            currentForm = "/sc/userInfo/View.xhtml";
        }
    }

    public void cancelCreateUser() {

        if ((users != null) && (!users.isEmpty())) {
            user = users.get(0);
            currentForm = "/sc/userInfo/View.xhtml";
        } else {
            query = UserQueryBuilder.getFindAllQuery();
            users = super.findWithQuery(query);
            if ((users != null) && (!users.isEmpty())) {
                user = users.get(0);
                currentForm = "/sc/userInfo/View.xhtml";
            }
        }
    }

    public void updateUser() {
        if (userExistTwo(user.getId())) {
            user = super.updateItem(user);
            users.set(users.indexOf(user), user);
            currentForm = "/sc/userInfo/View.xhtml";
        }
    }

    private boolean userExistTwo(Integer id) {
        if (id != null) {
            User usr = super.findItemById(id, User.class);
            if (usr == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                if ((users != null) && (users.size() > 1)) {
                    users.remove(user);
                    user = users.get(0);
                } else {
                    query = UserQueryBuilder.getFindAllQuery();
                    users = super.findWithQuery(query);
                    if ((users != null) && (!users.isEmpty())) {
                        user = users.get(0);
                    }
                }
                currentForm = "/sc/userInfo/View.xhtml";
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
        currentForm = "/sc/userInfo/Create.xhtml";
    }

    public void resolveRequestParams() {

        currentForm = "/sc/userInfo/View.xhtml";

        if (JsfUtil.isNumeric(userId)) {

            Integer id = Integer.valueOf(userId);
            user = super.findItemById(id, User.class);

            if (user != null) {
                query = UserQueryBuilder.getFindAllQuery();
                users = super.findWithQuery(query);
                return;
            }
        }

        query = UserQueryBuilder.getFindAllQuery();
        users = super.findWithQuery(query);
        if (users != null && !users.isEmpty()) {
            user = users.get(0);
        }
    }

    public void createUser() {
        if (user != null) {

            user = super.createItem(user);

            if ((users != null) && (!users.isEmpty())) {
                users.add(user);
            } else {
                query = UserQueryBuilder.getFindAllQuery();
                users = super.findWithQuery(query);
            }

            currentForm = "/sc/userInfo/View.xhtml";
        }
    }

    public void prepareEditUser() {
        if (userExist(user.getId())) {
            imageModified = false;
            currentForm = "/sc/userInfo/Edit.xhtml";
        }
    }

    private boolean userExist(Integer id) {
        if (id != null) {
            user = super.findItemById(id, User.class);
            if (user == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");

                if ((users != null) && (users.size() > 1)) {
                    users.remove(user);
                    user = users.get(0);
                } else {
                    query = UserQueryBuilder.getFindAllQuery();
                    users = super.findWithQuery(query);
                    if ((users != null) && (!users.isEmpty())) {
                        user = users.get(0);
                    }
                }
                currentForm = "/sc/userInfo/View.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void prepareView() {

        if (user != null && userExist(user.getId())) {
            currentForm = "/sc/userInfo/View.xhtml";
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
            query = UserQueryBuilder.getFindAllQuery();
            users = super.findWithQuery(query);
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

    public String getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(String currentForm) {
        this.currentForm = currentForm;
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
