package com.defterp.modules.accounting.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.modules.accounting.entities.Tax;
import com.defterp.modules.accounting.queryBuilders.TaxQueryBuilder;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
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
@Named(value = "taxController")
@ViewScoped
public class TaxController extends AbstractController {

    private List<Tax> taxes;
    private List<Tax> filteredTaxes;
    private Tax tax;
    private String taxId;
    private List<String> taxTypes;
    private QueryWrapper query;

    public TaxController() {
        super("/sc/tax/");
    }

    @PostConstruct
    public void init() {
        taxTypes = new ArrayList<>();
        taxTypes.add("Sale");
        taxTypes.add("Purchase");
    }

    public void deleteTax() {
        if (taxExist(tax.getId())) {

            boolean deleted = super.deleteItem(tax);
            if (deleted) {

                if ((taxes != null) && (taxes.size() > 1)) {
                    taxes.remove(tax);
                    tax = taxes.get(0);
                } else {

                    query = TaxQueryBuilder.getFindAllQuery();
                    taxes = super.findWithQuery(query);

                    if ((taxes != null) && (!taxes.isEmpty())) {
                        tax = taxes.get(0);
                    }
                }

                JsfUtil.addSuccessMessage("ItemDeleted");
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
            }
        }
    }

    public void cancelEditTax() {
        if (taxExist(tax.getId())) {
            currentForm = "/sc/tax/View.xhtml";
        }
    }

    public void cancelCreateTax() {

        if ((taxes != null) && (!taxes.isEmpty())) {
            tax = taxes.get(0);
            currentForm = "/sc/tax/View.xhtml";
        } else {
            query = TaxQueryBuilder.getFindAllQuery();
            taxes = super.findWithQuery(query);
            if ((taxes != null) && (!taxes.isEmpty())) {
                tax = taxes.get(0);
                currentForm = "/sc/tax/View.xhtml";
            }
        }
    }

    public void updateTax() {
        if (taxExistTwo(tax.getId())) {
            tax.setPercent(JsfUtil.round(tax.getPercent()));
            if (tax.getPercent() == 0d) {
                JsfUtil.addWarningMessage("PositiveTaxAmount");
                return;
            }
            tax.setAmount(tax.getPercent() / 100);
            tax = super.updateItem(tax);
            taxes.set(taxes.indexOf(tax), tax);
            currentForm = "/sc/tax/View.xhtml";
        }
    }

    private boolean taxExistTwo(Integer id) {
        if (id != null) {
            Tax taxx = super.findItemById(tax.getId(), tax.getClass());
            if (taxx == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                if ((taxes != null) && (taxes.size() > 1)) {
                    taxes.remove(tax);
                    tax = taxes.get(0);
                } else {
                    query = TaxQueryBuilder.getFindAllQuery();
                    taxes = super.findWithQuery(query);
                    if ((taxes != null) && (!taxes.isEmpty())) {
                        tax = taxes.get(0);
                    }
                }
                currentForm = "/sc/tax/View.xhtml";
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
        currentForm = "/sc/tax/Create.xhtml";
    }

    public void resolveRequestParams() {

        currentForm = "/sc/tax/View.xhtml";

        if (JsfUtil.isNumeric(taxId)) {

            Integer id = Integer.valueOf(taxId);
            tax = super.findItemById(id, Tax.class);

            if (tax != null) {
                query = TaxQueryBuilder.getFindAllQuery();
                taxes = super.findWithQuery(query);
                return;
            }
        }

        query = TaxQueryBuilder.getFindAllQuery();
        taxes = super.findWithQuery(query);
        if (taxes != null && !taxes.isEmpty()) {
            tax = taxes.get(0);
        }
    }

    public void createTax() {
        if (tax != null) {
            tax.setPercent(JsfUtil.round(tax.getPercent()));
            if (tax.getPercent() == 0d) {
                JsfUtil.addWarningMessage("PositiveTaxAmount");
                return;
            }
            tax.setAmount(tax.getPercent() / 100);
            tax = super.createItem(tax);
            if ((taxes != null) && (!taxes.isEmpty())) {
                taxes.add(tax);
            } else {
                query = TaxQueryBuilder.getFindAllQuery();
                taxes = super.findWithQuery(query);
            }
            currentForm = "/sc/tax/View.xhtml";
        }
    }

    public void prepareEditTax() {
        if (taxExist(tax.getId())) {
            currentForm = "/sc/tax/Edit.xhtml";
        }
    }

    private boolean taxExist(Integer id) {
        if (id != null) {
            tax = super.findItemById(id, Tax.class);
            if (tax == null) {

                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = "/sc/tax/View.xhtml";

                if ((taxes != null) && (taxes.size() > 1)) {
                    taxes.remove(tax);
                    tax = taxes.get(0);
                } else {
                    query = TaxQueryBuilder.getFindAllQuery();
                    taxes = super.findWithQuery(query);
                    if ((taxes != null) && (!taxes.isEmpty())) {
                        tax = taxes.get(0);
                    }
                }
                return false;
            }
            return true;
        }
        return false;
    }

    public void prepareView() {

        if (tax != null && taxExist(tax.getId())) {
            currentForm = "/sc/tax/View.xhtml";
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
            query = TaxQueryBuilder.getFindPurchaseTaxesQuery();
            taxes = super.findWithQuery(query);
        }
        return taxes;
    }

    public List<Tax> getSaleTaxes() {
        if (taxes == null) {
            query = TaxQueryBuilder.getFindSaleTaxesQuery();
            taxes = super.findWithQuery(query);
        }
        return taxes;
    }

    public List<Tax> getTaxes() {
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
