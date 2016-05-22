package com.casa.erp.beans;

import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.entities.Tax;
import com.casa.erp.dao.TaxFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

import javax.inject.Inject;

@Named(value = "taxController")
@ViewScoped
public class TaxController implements Serializable {

    @Inject
    private TaxFacade taxFacade;
    private List<Tax> taxes;
    private List<Tax> filteredTaxes;
    private Tax tax;
    private String taxId;
    private List<String> taxTypes;
    private String currentPage = "/sc/tax/List.xhtml";

    @PostConstruct
    public void init() {
        taxTypes = new ArrayList<>();
        taxTypes.add("Sale");
        taxTypes.add("Purchase");
    }

    public void deleteTax(Integer id) {
        if (taxExist(id)) {
            try {
                taxFacade.remove(tax);
            } catch (Exception e) {
                System.out.println("Error Delete: " + e.getMessage());
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                return;
            }
            JsfUtil.addSuccessMessage("ItemDeleted");
            currentPage = "/sc/tax/List.xhtml";
            taxes.remove(tax);
            tax = null;

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void cancelEditTax(Integer id) {
        if (taxExist(id)) {
            currentPage = "/sc/tax/View.xhtml";
        }
    }

    public void updateTax(Integer id) {
        if (taxExistTwo(id)) {
            tax.setPercent(JsfUtil.round(tax.getPercent()));
            if (tax.getPercent() == 0d) {
                JsfUtil.addWarningMessage("PositiveTaxAmount");
                return;
            }
            tax.setAmount(tax.getPercent() / 100);
            tax = taxFacade.update(tax);
            taxes = taxFacade.findAll();
            currentPage = "/sc/tax/View.xhtml";
        }
    }

    private boolean taxExistTwo(Integer id) {
        if (id != null) {
            Tax tax = taxFacade.find(id);
            if (tax == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                taxes = null;
                currentPage = "/sc/tax/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void prepareCreateTax() {
        tax = new Tax();
        tax.setPercent(0d);
        tax.setActive(Boolean.TRUE);
        currentPage = "/sc/tax/Create.xhtml";
    }

    public void viewTax() {
        if (taxId != null && JsfUtil.isNumeric(taxId)) {
            Integer id = Integer.valueOf(taxId);
            tax = taxFacade.find(id);
            if (tax != null) {
                taxes = taxFacade.findAll();
                currentPage = "/sc/tax/View.xhtml";
                return;
            }
        }

        taxes = taxFacade.findAll();
        currentPage = "/sc/tax/List.xhtml";
    }

    public void createTax() {
        if (tax != null) {
            tax.setPercent(JsfUtil.round(tax.getPercent()));
            if (tax.getPercent() == 0d) {
                JsfUtil.addWarningMessage("PositiveTaxAmount");
                return;
            }
            tax.setAmount(tax.getPercent() / 100);
            tax = taxFacade.create(tax);
            taxes = taxFacade.findAll();
            currentPage = "/sc/tax/View.xhtml";
        }
    }

    public void prepareEditTax(Integer id) {
        if (taxExist(id)) {
            currentPage = "/sc/tax/Edit.xhtml";
        }
    }

    private boolean taxExist(Integer id) {
        if (id != null) {
            tax = taxFacade.find(id);
            if (tax == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                taxes = null;
                currentPage = "/sc/tax/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void prepareView() {

        if (tax != null) {
            if (taxExist(tax.getId())) {
                currentPage = "/sc/tax/View.xhtml";
            }
        }
    }

    public void showTaxList() {
        tax = null;
        currentPage = "/sc/tax/List.xhtml";
    }

    public void showTaxForm() {
        if (taxes.size() > 0) {
            tax = taxes.get(0);
            currentPage = "/sc/tax/View.xhtml";
        }
    }

    public int getTaxIndex() {
        if (taxes != null && tax != null) {
            return taxes.indexOf(tax) + 1;
        }
        return 0;
    }

    public void nextTax() {
        if (taxes.indexOf(tax) == (taxes.size() - 1)) {
            tax = taxes.get(0);
        } else {
            tax = taxes.get(taxes.indexOf(tax) + 1);
        }
    }

    public void previousTax() {
        if (taxes.indexOf(tax) == 0) {
            tax = taxes.get(taxes.size() - 1);
        } else {
            tax = taxes.get(taxes.indexOf(tax) - 1);
        }
    }

    public List<Tax> getPurchaseTaxes() {
        if (taxes == null) {
            taxes = taxFacade.findPurchaseTaxes();
        }
        return taxes;
    }

    public List<Tax> getSaleTaxes() {
        if (taxes == null) {
            taxes = taxFacade.findSaleTaxes();
        }
        return taxes;
    }

    public List<Tax> getTaxes() {
        if (taxes == null) {
            taxes = taxFacade.findAll();
        }
        return taxes;
    }

    public void setTaxes(List<Tax> taxes) {
        this.taxes = taxes;
    }

    public List<Tax> getFilteredTaxes() {
        return filteredTaxes;
    }

    public void setFilteredTaxes(List<Tax> filteredTaxes) {
        this.filteredTaxes = filteredTaxes;
    }

    public Tax getTax() {
        return tax;
    }

    public void setTax(Tax tax) {
        this.tax = tax;
    }

    public String getPage() {
        return currentPage;
    }

    public void setPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public List<String> getTaxTypes() {
        return taxTypes;
    }

    public void setTaxTypes(List<String> taxTypes) {
        this.taxTypes = taxTypes;
    }

}
