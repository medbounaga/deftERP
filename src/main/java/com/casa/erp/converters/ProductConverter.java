package com.casa.erp.converters;

import com.casa.erp.entities.Product;
import com.casa.erp.dao.ProductFacade;
import com.casa.erp.beans.util.JsfUtil;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@FacesConverter(value = "productConverter")
public class ProductConverter implements Converter {

    @Inject
    private ProductFacade ejbFacade;

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component, String value) {
        if (value == null || value.length() == 0 || JsfUtil.isDummySelectItem(component, value) || !isNumeric(value)) {
            return null;
        }
        
        try {
            return this.ejbFacade.find(getKey(value));
        } catch (Exception e) {
            return null;
        }
        
    }

    java.lang.Integer getKey(String value) throws Exception  {
        java.lang.Integer key;
        key = Integer.valueOf(value);
        return key;
    }

    String getStringKey(java.lang.Integer value) {
        StringBuffer sb = new StringBuffer();
        sb.append(value);
        return sb.toString();
    }
    
    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getAsString(FacesContext facesContext, UIComponent component, Object object) {
        if (object == null
                || (object instanceof String && ((String) object).length() == 0)) {
            return null;
        }
        if (object instanceof Product) {
            Product o = (Product) object;
            return getStringKey(o.getId());
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "object {0} is of type {1}; expected type: {2}", new Object[]{object, object.getClass().getName(), Product.class.getName()});
            return null;
        }
    }

}
