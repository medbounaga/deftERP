package com.defterp.modules.partners.controllers;

import com.defterp.translation.annotations.Countries;
import static com.defterp.translation.annotations.Countries.Version.SECOND;
import com.defterp.util.JsfUtil;
import com.defterp.modules.accounting.entities.Account;
import com.defterp.modules.partners.entities.Partner;
import com.defterp.modules.accounting.queryBuilders.AccountQueryBuilder;
import com.defterp.modules.accounting.queryBuilders.InvoiceQueryBuilder;
import com.defterp.modules.accounting.queryBuilders.PaymentQueryBuilder;
import com.defterp.modules.inventory.queryBuilders.DeliveryOrderQueryBuilder;
import com.defterp.modules.partners.queryBuilders.PartnerQueryBuilder;
import com.defterp.modules.purchases.queryBuilders.PurchaseOrderQueryBuilder;
import com.defterp.modules.sales.queryBuilders.SaleOrderQueryBuilder;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author MOHAMMED BOUNAGA
 *
 * github.com/medbounaga
 */

@Named(value = "customerController")
@ViewScoped
public class CustomerController extends AbstractController {

    @Inject
    @Countries(version = SECOND)
    private HashMap<String, String> countries;
    private Partner partner;
    private List<Partner> partners;
    private List<Partner> filteredPartners;
    private String partnerId;
    private String partnerType;
    private String searchKey;
    boolean isGridView;
    private String currentForm;
    private String currentList;
    private Part image;
    private boolean imageModified;
    private QueryWrapper query;

    
    public CustomerController() {
        
        super("/sc/customer/");
        
        currentList = super.GRID_URL;
        currentForm = super.VIEW_URL;
        
        isGridView = true;
        
        partnerType = "Customer";
    }

    public void setImage(Part image) {
        if (image != null) {
            try {
                InputStream input = image.getInputStream();
                partner.setImage(IOUtils.toByteArray(input));
            } catch (IOException ex) {
                Logger.getLogger(CustomerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (image == null && partner.getImage() != null && imageModified == true) {
            partner.setImage(null);
        }
    }

    public Part getImage() {
        return image;
    }

    public void deleteCustomer() {

        if (customerExist(partner.getId())) {
            
            boolean deleted = super.deleteItem(partner);
            
            if (deleted) {
                JsfUtil.addSuccessMessage("ItemDeleted");
                currentForm = VIEW_URL;
                showCustomers();
                
            } else {
                partner.setCountry(countries.get(partner.getCountry()));
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
            }
        }
    }

    public void prepareCreateCustomer() {
        List<Account> accounts;
        partner = new Partner();
        partner.setCountry("MA");
        partner.setCustomer(Boolean.TRUE);
        partner.setCreateDate(new Date());
        partner.setActive(Boolean.TRUE);
        currentForm = CREATE_URL;
        
        query = AccountQueryBuilder.getFindByTypeQuery("Receivable");
        accounts = super.findWithQuery(query);

        if (accounts != null && !accounts.isEmpty()) {
            partner.setAccountReceivable(accounts.get(0));
        }
        
        query = AccountQueryBuilder.getFindByTypeQuery("Payable");
        accounts = super.findWithQuery(query);
        
        if (accounts != null && !accounts.isEmpty()) {
            partner.setAccountPayable(accounts.get(0));
        }
    }

    public void prepareEditCustomer() {
        if (customerExist(partner.getId())) {
            imageModified = false;
            currentForm = EDIT_URL;
        }
    }

    public void cancelCustomerEdit() {
        if (customerExist(partner.getId())) {
            partners = null;
            filteredPartners = null;

            switch (partnerType) {
                case "Vendor":
                    partners = getSuppliers();
                    break;
                case "Partner":
                    partners = getPartners();
                    break;
                default:
                    partners = getCustomers();
                    break;
            }

            if (partners != null && !partners.isEmpty()) {
                currentForm = VIEW_URL;
            }
        }
    }

    public void editCustomer() {
        if (!customerExistTwo(partner.getId())) {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
        } else if (partner != null) {
            partner = super.updateItem(partner);
            partner.setCountry(countries.get(partner.getCountry()));
            partners.set(partners.indexOf(partner), partner);
        }
        currentForm = VIEW_URL;
    }

    public Long countSaleOrders() {
        if (partner != null) {
            query = SaleOrderQueryBuilder.getCountByCustomerQuery(partner.getId());
            return (Long) super.findSingleWithQuery(query);
        }
        return 0L;
    }
    
    public Long countSaleOrders(Integer partnerId) {
        if (partnerId != null) {
            query = SaleOrderQueryBuilder.getCountByCustomerQuery(partnerId);
            return (Long) super.findSingleWithQuery(query);
        }
        return 0L;
    }

    public Long countPurchaseOrders() {
        if (partner != null) {
            query = PurchaseOrderQueryBuilder.getCountByVendorQuery(partner.getId());
            return (Long) super.findSingleWithQuery(query);
        }
        return 0L;
    }

    public Long countPurchaseOrders(Integer partnerId) {
        if (partnerId != null) {
            query = PurchaseOrderQueryBuilder.getCountByVendorQuery(partnerId);
            return (Long) super.findSingleWithQuery(query);
        }
        return 0L;
    }

    public Long countCustomerInvoices() {
        if (partner != null) {
            query = InvoiceQueryBuilder.getCountByCustomerQuery(partner.getId());
            return (Long) super.findSingleWithQuery(query);
        }
        return 0L;
    }

    public Long countSupplierInvoices() {
        if (partner != null) {
            query = InvoiceQueryBuilder.getCountByVendorQuery(partner.getId());
            return (Long) super.findSingleWithQuery(query);
        }
        return 0L;
    }


    public Long countInShipments() {
        if (partner != null) {
            query = DeliveryOrderQueryBuilder.getCountByVendorQuery(partner.getId());
            return (Long) super.findSingleWithQuery(query);
        }
        return 0L;
    }

    public Long countOutShipments() {
        if (partner != null) {
            query = DeliveryOrderQueryBuilder.getCountByCustomerQuery(partner.getId());
            return (Long) super.findSingleWithQuery(query);
        }
        return 0L;
    }

    public Long countCustomerPayments() {
        if (partner != null) {
            query = PaymentQueryBuilder.getCountByCustomerQuery(partner.getId());
            return (Long) super.findSingleWithQuery(query);
        }
        return 0L;
    }

    public Long countSupplierPayments() {
        if (partner != null) {
            query = PaymentQueryBuilder.getCountByVendorQuery(partner.getId());
            return (Long) super.findSingleWithQuery(query);
        }
        return 0L;
    }

    public Double getTotalPayables(Integer partnerId) {
        if (partnerId != null) {
            query = InvoiceQueryBuilder.getTotalDueAmountByVendorQuery(partnerId);
            return (Double) super.findSingleWithQuery(query);
        }
        return 0d;
    }

    public Double getTotalReceivales(Integer partnerId) {
        if (partnerId != null) {
            query = InvoiceQueryBuilder.getTotalDueAmountByCustomerQuery(partnerId);
            return (Double) super.findSingleWithQuery(query);
        }
        return 0d;
    }

    public Double getTotalPayables() {
        if (partner != null) {
            query = InvoiceQueryBuilder.getTotalDueAmountByVendorQuery(partner.getId());
            return (Double) super.findSingleWithQuery(query);
        }
        return 0d;
    }

    public Double getTotalReceivales() {
        if (partner != null) {
            query = InvoiceQueryBuilder.getTotalDueAmountByCustomerQuery(partner.getId());
            return (Double) super.findSingleWithQuery(query);
        }
        return 0d;
    }

//    public Double getTotalInvoicedSales() {
//        if (partner != null) {
//            return partnerFacade.getInvoicedSum(partner.getId(), "Sale");
//        }
//        return 0d;
//    }
//
//    public Double getTotalInvoicedPurchases() {
//        if (partner != null) {
//            return partnerFacade.getInvoicedSum(partner.getId(), "Purchase");
//        }
//        return 0d;
//    }
    
    public HashMap<String, String> getCountries() {
        return countries;
    }

    public void prepareViewPartner() {
        if (customerExist(partner.getId())) {

            partners = null;
            filteredPartners = null;

            switch (partnerType) {
                case "Vendor":
                    partners = getSuppliers();
                    break;
                case "Partner":
                    partners = getPartners();
                    break;
                default:
                    partners = getCustomers();
                    break;
            }

            if (partners != null && !partners.isEmpty()) {
                currentForm = VIEW_URL;
            }
        }
    }

    public void prepareViewPartner(int id) {
        if (customerExist(id)) {

            partners = null;
            filteredPartners = null;

            switch (partnerType) {
                case "Vendor":
                    partners = getSuppliers();
                    break;
                case "Partner":
                    partners = getPartners();
                    break;
                default:
                    partners = getCustomers();
                    break;
            }

            if (partners != null && !partners.isEmpty()) {
                currentForm = VIEW_URL;
            }
        }
    }

//    public void prepareViewCustomer(int index, int id) {
//        if (customerExist(id)) {
//            if ((partners != null) && (index < partners.size()) && (index >= 0) && (partners.get(index).getId() == id)) {
//                partner = partners.get(index);
//                currentForm = VIEW_URL;
//                searchKey = null;
//            }
//        }
//    }
    
    public void resolveRequestParams() {

        if (JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            partner = super.findItemById(id, Partner.class);
            if (partner != null) {
                partner.setCountry(countries.get(partner.getCountry()));
                partners = getCustomers();
                replaceCountryCodeWithName();
                if (!partners.contains(partner)) {
                    partners.add(partner);
                }
                currentForm = VIEW_URL;
                currentList = GRID_URL;
                return;
            }
        }

        partners = getCustomers();

        if (!partners.isEmpty()) {
            partner = partners.get(0);
        }
        currentForm = VIEW_URL;
        currentList = GRID_URL;
    }

    private void replaceCountryCodeWithName() {
        for (Partner partner : partners) {
            if (partner.getCountry() != null && !partner.getCountry().isEmpty()) {
                partner.setCountry(countries.get(partner.getCountry()));
            }
        }
    }

    public void createCustomer() {
        if (partner != null) {

            partner.setCredit(0d);
            partner.setDebit(0d);
            partner = super.createItem(partner);
            partner.setCountry(countries.get(partner.getCountry()));
            partners.add(partner);
            currentForm = VIEW_URL;
        }
    }

    public void searchByName() {

        if (partners != null && filteredPartners != null) {
            if (searchKey != null && !searchKey.isEmpty()) {

                partners = new ArrayList<>();

                for (Partner part : filteredPartners) {
                    if (StringUtils.containsIgnoreCase(part.getName(), searchKey)) {
                        partners.add(part);
                    }
                }
            } else if (searchKey != null && searchKey.isEmpty()) {
                partners = new ArrayList<>();
                partners.addAll(filteredPartners);
            }
        }
        searchKey = null;
    }

    public void showCustomerList() {
        isGridView = false;
        showCustomers();
    }

    public void showCustomerGrid() {
        isGridView = true;
        showCustomers();
    }

    public void cancelCreate() {

        partners = null;
        filteredPartners = null;

        switch (partnerType) {
            case "Vendor":
                partners = getSuppliers();
                break;
            case "Partner":
                partners = getPartners();
                break;
            default:
                partners = getCustomers();
                break;
        }

        if (partners != null && !partners.isEmpty()) {
            partner = partners.get(0);
            currentForm = VIEW_URL;
        }
    }

    public void showCustomers() {
        partner = null;
        partners = null;
        filteredPartners = null;
        updatePartnerType();

        if (isGridView) {
            currentList = GRID_URL;
        } else {
            currentList = LIST_URL;
        }
    }

    public int getPartnerIndex() {
        if (partner != null && partners != null && !partners.isEmpty()) {
            return partners.indexOf(partner) + 1;
        }
        return 0;
    }

    public void nextPartner() {
        if (partners != null && !partners.isEmpty() && partner != null) {
            if (partners.contains(partner)) {
                if (partners.indexOf(partner) == (partners.size() - 1)) {
                    partner = partners.get(0);
                } else {
                    partner = partners.get(partners.indexOf(partner) + 1);
                }
            } else {
                partner = partners.get(0);
            }
        }
    }

    public void previousPartner() {
        if (partners != null && !partners.isEmpty() && partner != null) {
            if (partners.contains(partner)) {
                if (partners.indexOf(partner) == 0) {
                    partner = partners.get(partners.size() - 1);
                } else {
                    partner = partners.get(partners.indexOf(partner) - 1);
                }
            } else {
                partner = partners.get(0);
            }
        }
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

    public String getGridDefaultImage(int PartnerId) {

        int modulos = PartnerId % 5;
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

    public String getFormDefaultImage() {

        int modulos = partner.getId() % 5;
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

    public void updatePartnerType() {

        partners = null;
        filteredPartners = null;

        switch (partnerType) {
            case "Vendor":
                partners = getSuppliers();
                break;
            case "Partner":
                partners = getPartners();
                break;
            default:
                partners = getCustomers();
                break;
        }

        if (partners != null && !partners.isEmpty()) {
            partner = partners.get(0);
        }

        searchKey = null;
    }

    private boolean customerExist(Integer id) {
        if (id != null) {
            partner = super.findItemById(id, Partner.class);
            if (partner == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                showCustomers();
                return false;
            } else {
                partner.setCountry(countries.get(partner.getCountry()));
                return true;
            }

        } else {
            return false;
        }
    }

    private boolean customerExistTwo(Integer id) {
        if (id != null) {
            Partner partner = super.findItemById(id, Partner.class);
            return partner != null;
        }
        return false;
    }

    public List<Partner> getSuppliers() {
        if (partners == null) {
            
            query = PartnerQueryBuilder.getFindVendorsQuery();
            partners = super.findWithQuery(query);
            
            replaceCountryCodeWithName();
            
            filteredPartners = new ArrayList<>();
            filteredPartners.addAll(partners);
            
            partnerType = "Vendor";
        }
        return partners;
    }

    public List<Partner> getCustomers() {

        if (partners == null) {
            
            query = PartnerQueryBuilder.getFindCustomersQuery();
            partners = super.findWithQuery(query);
            
            replaceCountryCodeWithName();
            
            filteredPartners = new ArrayList<>();
            filteredPartners.addAll(partners);
            
            partnerType = "Customer";
        }
        return partners;
    }

    public List<Partner> getPartners() {
        if (partners == null) {
            
            query = PartnerQueryBuilder.getFindPartnersQuery();
            partners = super.findWithQuery(query);
            
            replaceCountryCodeWithName();
            
            filteredPartners = new ArrayList<>();
            filteredPartners.addAll(partners);
            
            partnerType = "Partner";
        }
        return partners;
    }
    
    public List<Account> getReceivableAccounts() {
        query = AccountQueryBuilder.getFindByTypeQuery("Receivable");
        return super.findWithQuery(query);
    }

    public List<Account> getPayableAccounts() {
        query = AccountQueryBuilder.getFindByTypeQuery("Payable");
        return super.findWithQuery(query);
    }

    public List<Partner> getFilteredPartners() {
        return filteredPartners;
    }

    public void setFilteredPartners(List<Partner> filteredPartners) {
        this.filteredPartners = filteredPartners;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartnerType() {
        return partnerType;
    }

    public void setPartnerType(String partnerType) {
        this.partnerType = partnerType;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public boolean getImageModified() {
        return imageModified;
    }

    public void setImageModified(boolean imageModified) {
        this.imageModified = imageModified;
    }

    public String getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(String currentForm) {
        this.currentForm = currentForm;
    }

    public String getCurrentList() {
        return currentList;
    }

    public void setCurrentList(String currentList) {
        this.currentList = currentList;
    }
    
}
