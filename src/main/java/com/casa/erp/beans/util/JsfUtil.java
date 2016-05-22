package com.casa.erp.beans.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItem;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.primefaces.context.RequestContext;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

public class JsfUtil {

    public static void addErrorMessage(Exception ex, String defaultMsg) {
        String msg = ex.getLocalizedMessage();
        if (msg != null && msg.length() > 0) {
            addErrorMessage(msg);
        } else {
            addErrorMessage(defaultMsg);
        }
    }

    public static void addErrorMessages(List<String> messages) {
        for (String message : messages) {
            addErrorMessage(message);
        }
    }

    public static void addErrorMessage(String msg) {
        ResourceBundle bundle = getBundle();
        String m = bundle.getString(msg);
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_ERROR, m, m);
        FacesContext.getCurrentInstance().addMessage("successInfo", facesMsg);
    }

    public static void addSuccessMessage(String msg) {
        ResourceBundle bundle = getBundle();
        String m = bundle.getString(msg);
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_INFO, m, m);
        FacesContext.getCurrentInstance().addMessage("successInfo", facesMsg);
    }

    public static void addWarningMessage(String msg) {
        ResourceBundle bundle = getBundle();
        String m = bundle.getString(msg);
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, m, m);
        FacesContext.getCurrentInstance().addMessage("successInfo", facesMsg);
    }

    public static void throwWarningValidatorException(String msg) {
        FacesMessage facesMsg = new FacesMessage(FacesMessage.SEVERITY_WARN, msg, msg);
        throw new ValidatorException(facesMsg);
    }

    public static void addWarningMessageDialog(String msgHeader, String msg) {
        ResourceBundle bundle = getBundle();
        String h = bundle.getString(msgHeader);
        String m = bundle.getString(msg);
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, h, m);
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public static void addSuccessMessageDialog(String msgHeader, String msg) {
        ResourceBundle bundle = getBundle();
        String h = bundle.getString(msgHeader);
        String m = bundle.getString(msg);
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, h, m);
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public static void addWarningCustomMessageDialog(String msgHeader, String msg) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, msgHeader, msg);
        RequestContext.getCurrentInstance().showMessageInDialog(message);
    }

    public static ResourceBundle getBundle() {
        FacesContext context = FacesContext.getCurrentInstance();
        ResourceBundle bundle = ResourceBundle.getBundle("Messages", context.getViewRoot().getLocale());
        return bundle;
    }

    public static String getBundleString(String str) {
        try {
            return getBundle().getString(str);
        } catch (Exception a) {
            return "";
        }
    }

    public static Throwable getRootCause(Throwable cause) {
        if (cause != null) {
            Throwable source = cause.getCause();
            if (source != null) {
                return getRootCause(source);
            } else {
                return cause;
            }
        }
        return null;
    }

    public static boolean isValidationFailed() {
        return FacesContext.getCurrentInstance().isValidationFailed();
    }

    public static boolean isDummySelectItem(UIComponent component, String value) {
        for (UIComponent children : component.getChildren()) {
            if (children instanceof UISelectItem) {
                UISelectItem item = (UISelectItem) children;
                if (item.getItemValue() == null && item.getItemLabel().equals(value)) {
                    return true;
                }
                break;
            }
        }
        return false;
    }

    public static boolean isNumeric(String str) {
        int digits = 0;
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            } else {
                digits++;
            }
        }

        return digits <= 9;
    }

//    public static Double round(Double value) {
//        if (value != null) {
//            System.out.println("value"+value);
//            BigDecimal bd = new BigDecimal(value);
//            bd = bd.setScale(2, RoundingMode.FLOOR);
//            System.out.println("valueAfter"+bd.doubleValue());
//            return bd.doubleValue();
//        } else {
//            return null;
//        }
//    }
    public static Double round(Double value) {

        if (value != null) {
            System.out.println("value: "+value);
            System.out.println("value to String: "+value.toString());
            String[] splitter = value.toString().split("\\.");
            System.out.println("decimals: "+splitter[1].length());
            if (splitter[1].length() <= 2) {
                return value;
            } else {
                return (long) (value * 1e2) / 1e2;
            }

        } else {
            return 1d;
        }
    }

//    public static Double round(Double value, Integer decimals) {
//        if (value != null) {
//            if (decimals == 3) {
//                value = (long) (value * 1e3) / 1e3;
//            }
//            if (decimals == 2) {
//                value = (long) (value * 1e2) / 1e2;
//            }
//            if (decimals == 1) {
//                value = (long) (value * 1e1) / 1e1;
//            } else {
//                value = (long) (value * 1e0) / 1e0;
//            }
//
//            if (value == 0d) {
//                return 1d;
//            } else {
//                return value;
//            }
//
//        } else {
//            return 1d;
//        }
//    }
    public static Double round(Double value, Integer decimals) {
        if (value != null) {
            
            System.out.println("value: "+value);
            System.out.println("value to String: "+value.toString());
            String[] splitter = value.toString().split("\\.");
            System.out.println("decimals: "+splitter[1].length());
            
            if (splitter[1].length() <= decimals) {
                    return value;
            }            
            if (decimals == 3) {
                return (long) (value * 1e3) / 1e3;
            }
            if (decimals == 2) {
                return (long) (value * 1e2) / 1e2;
            }
            if (decimals == 1) {
                return (long) (value * 1e1) / 1e1;
            } else {
                return (long) (value * 1e0) / 1e0;
            }

        } else {
            return null;
        }
    }
//    public static Double round(Double value, Integer decimals) {
//        if (value != null) {
//            BigDecimal bd = new BigDecimal(value);
//            bd = bd.setScale(decimals, RoundingMode.DOWN);
//            return bd.doubleValue();
//        } else {
//            return null;
//        }
//    }

    public static String getComponentMessages(String clientComponent, String defaultMessage) {
        FacesContext fc = FacesContext.getCurrentInstance();
        UIComponent component = UIComponent.getCurrentComponent(fc).findComponent(clientComponent);
        if (component instanceof UIInput) {
            UIInput inputComponent = (UIInput) component;
            if (inputComponent.isValid()) {
                return defaultMessage;
            } else {
                Iterator<FacesMessage> iter = fc.getMessages(inputComponent.getClientId());
                if (iter.hasNext()) {
                    return iter.next().getDetail();
                }
            }
        }
        return "";
    }

    public static void addErrorMessage(FacesMessage message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
