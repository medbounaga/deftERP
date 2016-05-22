package com.casa.erp.beans;

import com.casa.erp.beans.util.Countries;
import static com.casa.erp.beans.util.Countries.Version.SECOND;
import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.entities.Account;
import com.casa.erp.entities.Partner;
import com.casa.erp.dao.PartnerFacade;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
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
public class CustomerController implements Serializable {

    @Inject
    private PartnerFacade partnerFacade;
    @Inject
    @Countries(version = SECOND)
    private HashMap<String, String> countries;
    private Partner partner;
    private List<Partner> partners;
    private List<Partner> filteredPartners;
    private String partnerId;
    private String partnerType = "Customer";
    private String searchKey;
    boolean isGridView = true;
    private String currentPage = "/sc/customer/Grid.xhtml";
    private Part image;
    private boolean imageModified;

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

    public void deleteCustomer(Integer id) {

        if (customerExist(id)) {
            try {
                partnerFacade.remove(partner);
            } catch (Exception e) {
                partner.setCountry(countries.get(partner.getCountry()));
                System.out.println("Error Delete: " + e.getMessage());
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                return;
            }

            JsfUtil.addSuccessMessage("ItemDeleted");
            showCustomers();

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void prepareCreateCustomer() {
        List<Account> accounts;
        partner = new Partner();
        partner.setCountry("MA");
        partner.setCustomer(Boolean.TRUE);
        partner.setCreateDate(new Date());
        partner.setActive(Boolean.TRUE);
        currentPage = "/sc/customer/Create.xhtml";

        accounts = partnerFacade.findByType("Receivable");
        if (accounts != null && !accounts.isEmpty()) {
            partner.setAccountReceivable(accounts.get(0));
        }
        accounts = partnerFacade.findByType("Payable");
        if (accounts != null && !accounts.isEmpty()) {
            partner.setAccountPayable(accounts.get(0));
        }

    }

    public void prepareEditCustomer(Integer id) {
        if (customerExist(id)) {
            imageModified = false;
            currentPage = "/sc/customer/Edit.xhtml";
        }
    }

    public void cancelCustomerEdit(Integer id) {
        if (customerExist(id)) {
            partner.setCountry(countries.get(partner.getCountry()));
            currentPage = "/sc/customer/View.xhtml";
        }
    }

    public void editCustomer(Integer id) {
        if (!customerExistTwo(id)) {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
            currentPage = "/sc/customer/View.xhtml";
        } else if (partner != null) {
            partner = partnerFacade.update(partner);
            partner.setCountry(countries.get(partner.getCountry()));
            partners.set(partners.indexOf(partner), partner);
            currentPage = "/sc/customer/View.xhtml";
        }
    }

    public Long countSaleOrders() {
        if (partner != null) {
            return partnerFacade.countSalesOrders(partner.getId());
        }
        return 0L;
    }

    public Long countSaleOrders(Integer partnerId) {
        if (partnerId != null) {
            return partnerFacade.countSalesOrders(partnerId);
        }
        return 0L;
    }

    public Long countPurchaseOrders() {
        if (partner != null) {
            return partnerFacade.countPurchaseOrders(partner.getId());
        }
        return 0L;
    }

    public Long countPurchaseOrders(Integer partnerId) {
        if (partnerId != null) {
            return partnerFacade.countPurchaseOrders(partnerId);
        }
        return 0L;
    }

    public Long countCustomerInvoices() {
        if (partner != null) {
            return partnerFacade.countInvoices(partner.getId(), "Sale");
        }
        return 0L;
    }

    public Long countSupplierInvoices() {
        if (partner != null) {
            return partnerFacade.countInvoices(partner.getId(), "Purchase");
        }
        return 0L;
    }

    public Long countJournalEntries() {
        if (partner != null) {
            return partnerFacade.countJournalEntries(partner.getId());
        }
        return 0L;
    }

    public Long countInShipments() {
        if (partner != null) {
            return partnerFacade.countShipments(partner.getId(), "Purchase");
        }
        return 0L;
    }

    public Long countOutShipments() {
        if (partner != null) {
            return partnerFacade.countShipments(partner.getId(), "Sale");
        }
        return 0L;
    }

    public Long countCustomerPayments() {
        if (partner != null) {
            return partnerFacade.countPayments(partner.getId(), "customer");
        }
        return 0L;
    }

    public Long countSupplierPayments() {
        if (partner != null) {
            return partnerFacade.countPayments(partner.getId(), "supplier");
        }
        return 0L;
    }

    public Double getTotalPayables(Integer partnerId) {
        if (partnerId != null) {
            return partnerFacade.getTotalDueAmount(partnerId, "Purchase");
        }
        return 0d;
    }

    public Double getTotalReceivales(Integer partnerId) {
        if (partnerId != null) {
            return partnerFacade.getTotalDueAmount(partnerId, "Sale");
        }
        return 0d;
    }

    public Double getTotalPayables() {
        if (partner != null) {
            return partnerFacade.getTotalDueAmount(partner.getId(), "Purchase");
        }
        return 0d;
    }

    public Double getTotalReceivales() {
        if (partner != null) {
            return partnerFacade.getTotalDueAmount(partner.getId(), "Sale");
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

    public void prepareViewCustomer() {
        if (customerExist(partner.getId())) {
            partner.setCountry(countries.get(partner.getCountry()));
            currentPage = "/sc/customer/View.xhtml";
        }
    }

    public void prepareViewCustomer(int index, int id) {

        if ((partners != null) && (index < partners.size()) && (index >= 0) && (partners.get(index).getId() == id)) {
            partner = partners.get(index);
            currentPage = "/sc/customer/View.xhtml";
        }
    }

    public void viewCustomer() {

        if (partnerId != null && JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            partner = partnerFacade.find(id);
            if (partner != null) {
                partner.setCountry(countries.get(partner.getCountry()));
                partners = partnerFacade.findCustomers();
                setCountryCodeToName();
                if (!partners.contains(partner)) {
                    partners.add(partner);
                }
                currentPage = "/sc/customer/View.xhtml";
                return;
            }
        }
        filteredPartners = null;
        partners = null;
        currentPage = "/sc/customer/Grid.xhtml";
    }

    private void setCountryCodeToName() {
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
            partner = partnerFacade.create(partner);
            partner.setCountry(countries.get(partner.getCountry()));

            partners.add(partner);
            currentPage = "/sc/customer/View.xhtml";
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

    public void showCustomers() {
        partner = null;
        partners = null;
        filteredPartners = null;
        updatePartnerType();

        if (isGridView) {
            currentPage = "/sc/customer/Grid.xhtml";
        } else {
            currentPage = "/sc/customer/List.xhtml";
        }
    }

    public void showCustomerForm() {

        switch (partnerType) {
            case "Vendor":
                partners = partnerFacade.findSuppliers();
                break;
            case "Partner":
                partners = partnerFacade.findAll();
                break;
            default:
                partners = partnerFacade.findCustomers();
                partnerType = "Customer";
                break;
        }

        if (partners.size() > 0) {
            setCountryCodeToName();
            partner = partners.get(0);
            currentPage = "/sc/customer/View.xhtml";
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
            if (partners.indexOf(partner) == (partners.size() - 1)) {
                partner = partners.get(0);
            } else {
                partner = partners.get(partners.indexOf(partner) + 1);
            }
        }
    }

    public void previousPartner() {
        if (partners != null && !partners.isEmpty() && partner != null) {
            if (partners.indexOf(partner) == 0) {
                partner = partners.get(partners.size() - 1);
            } else {
                partner = partners.get(partners.indexOf(partner) - 1);
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
        if (modulos == 0) {
            return "img/partnerPlaceholder.png";
        } else if (modulos == 1) {
            return "img/partnerPlaceholder1.png";
        } else if (modulos == 2) {
            return "img/partnerPlaceholder2.png";
        } else if (modulos == 3) {
            return "img/partnerPlaceholder3.png";
        } else {
            return "img/partnerPlaceholder4.png";
        }

    }

    public String getFormDefaultImage() {

        int modulos = partner.getId() % 5;
        if (modulos == 0) {
            return "img/partnerPlaceholder.png";
        } else if (modulos == 1) {
            return "img/partnerPlaceholder1.png";
        } else if (modulos == 2) {
            return "img/partnerPlaceholder2.png";
        } else if (modulos == 3) {
            return "img/partnerPlaceholder3.png";
        } else {
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
                partnerType = "Customer";
                break;
        }

        if (searchKey != null && !searchKey.isEmpty() && currentPage.equals("/sc/customer/Grid.xhtml")) {
            searchByName();
        }
    }

    private boolean customerExist(Integer id) {
        if (id != null) {
            partner = partnerFacade.find(id);
            if (partner == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                showCustomers();
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    private boolean customerExistTwo(Integer id) {
        if (id != null) {
            Partner partner = partnerFacade.find(id);
            return partner != null;
        }
        return false;
    }

    public List<Partner> getSuppliers() {
        if (partners == null) {
            partners = partnerFacade.findSuppliers();
            setCountryCodeToName();
            filteredPartners = new ArrayList<>();
            filteredPartners.addAll(partners);
        }
        return partners;
    }

    public List<Partner> getCustomers() {

        if (partners == null) {
            partners = partnerFacade.findCustomers();
            setCountryCodeToName();
            filteredPartners = new ArrayList<>();
            filteredPartners.addAll(partners);
        }
        return partners;
    }

    public List<Partner> getCustomerList() {

        if (partners == null) {
            partners = partnerFacade.findCustomers();
        }
        return partners;
    }

    public List<Partner> getPartners() {
        if (partners == null) {
            partners = partnerFacade.findAll();
            setCountryCodeToName();
            filteredPartners = new ArrayList<>();
            filteredPartners.addAll(partners);
        }
        return partners;
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

    public String getPage() {
        return currentPage;
    }

    public void setPage(String currentPage) {
        this.currentPage = currentPage;
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
}
