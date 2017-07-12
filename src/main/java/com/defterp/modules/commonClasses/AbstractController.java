
package com.defterp.modules.commonClasses;

import com.defterp.dataAccess.GenericDAO;
import com.defterp.util.JsfUtil;
import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.validation.ConstraintViolationException;



public abstract class AbstractController implements Serializable {

    private static final long serialVersionUID = 1;
    
    @Inject
    private GenericDAO dataAccess;
    
    protected String currentForm;
    protected String currentList;
    protected String BASE_URL;
    protected String LIST_URL;
    protected String GRID_URL;
    protected String VIEW_URL;
    protected String EDIT_URL;
    protected String CREATE_URL;
    
    protected Integer MAX_DROPDOWN_ITEMS;

    public AbstractController(String moduleURL) {       
        BASE_URL = moduleURL;
        LIST_URL = BASE_URL + "List.xhtml";
        GRID_URL = BASE_URL + "Grid.xhtml";
        VIEW_URL = BASE_URL + "View.xhtml";
        EDIT_URL = BASE_URL + "Edit.xhtml";
        CREATE_URL = BASE_URL + "Create.xhtml";
        currentForm = VIEW_URL;
        currentList = LIST_URL;
        MAX_DROPDOWN_ITEMS = Integer.valueOf(JsfUtil.getBundleString("maxDropDownItems"));
    }


    protected <Type extends BaseEntity> Type findItemById(Integer itemId, Class itemClass) {
        return this.dataAccess.findById(itemId, itemClass);
    }

    protected <Type extends BaseEntity> Type updateItem(Type item) {
        try {
            item = this.dataAccess.update(item);
        } catch (Exception ex) {
            this.displayPersistenceError(ex);
        }
        return item;
    }

    protected <Type extends BaseEntity> Type createItem(Type item) {
        try {
            item = this.dataAccess.save(item);
        } catch (Exception ex) {
            this.displayPersistenceError(ex);
        }
        return item;
    }

    protected <Type extends BaseEntity> boolean deleteItem(Type item) {
        try {
            this.dataAccess.delete(item);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    protected <Type extends BaseEntity> List<Type> findWithNamedQuery(QueryWrapper wq) {
        return this.findWithNamedQuery(wq, 0);
    }

    protected <Type extends BaseEntity> List<Type> findWithNamedQuery(QueryWrapper wq, int resultLimit) {
        List result = null;
        try {
            result = this.dataAccess.findWithNamedQuery(wq, resultLimit);
        } catch (Exception ex) {
            this.displayPersistenceError(ex);
        }
        return result;
    }

    protected <Type> Type findSingleWithNamedQuery(QueryWrapper wq) {
        Object result = null;
        try {
            result = this.dataAccess.findSingleWithNamedQuery(wq);
        } catch (Exception ex) {
            this.displayPersistenceError(ex);
        }
        return (Type) result;
    }

    protected <Type extends BaseEntity> List<Type> findWithQuery(QueryWrapper query) {
        return this.findWithQuery(query, 0);
    }

    protected <Type extends BaseEntity> List<Type> findWithQuery(QueryWrapper query, int resultLimit) {
        List result = null;
        try {
            result = this.dataAccess.findWithQuery(query, resultLimit);
        } catch (Exception ex) {
            this.displayPersistenceError(ex);
        }
        return result;
    }

    protected <Type> Type findSingleWithQuery(QueryWrapper query) {
        Object result = null;
        try {
            result = this.dataAccess.findSingleWithQuery(query);
        } catch (Exception ex) {
            this.displayPersistenceError(ex);
        }
        return (Type) result;
    }

    public String getCurrentForm() {
        return this.currentForm;
    }   

    public String getCurrentList() {
        return currentList;
    } 
    

    private void displayPersistenceError(Exception ex) {
        if (ex instanceof EJBException) {
            Throwable cause = JsfUtil.getRootCause((Throwable) ex.getCause());
            if (cause != null) {
                if (cause instanceof ConstraintViolationException) {
                    ConstraintViolationException excp = (ConstraintViolationException) cause;
                    excp.getConstraintViolations().stream().forEach(s -> {
                        JsfUtil.addErrorMessage((String) s.getMessage());
                    }
                    );
                } else {
                    String msg = cause.getLocalizedMessage();
                    if (msg.length() > 0) {
                        JsfUtil.addErrorMessage((String) msg);
                    } else {
                        JsfUtil.addErrorMessage((Exception) ex, (String) ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                    }
                }
            }
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
            JsfUtil.addErrorMessage((Exception) ex, (String) ResourceBundle.getBundle("/MyBundle").getString("PersistenceErrorOccured"));
        }
    }
}
