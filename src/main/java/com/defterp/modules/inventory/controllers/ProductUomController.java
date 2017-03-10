package com.defterp.modules.inventory.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.modules.inventory.entities.ProductUom;
import com.casa.erp.dao.ProductUomFacade;
import java.io.Serializable;
import java.util.List;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

/**
 *
 * @author MOHAMMED BOUNAGA
 *
 * github.com/medbounaga
 */
@Named(value = "productUomController")
@ViewScoped
public class ProductUomController implements Serializable {

    @Inject
    private ProductUomFacade productUomFacade;
    private ProductUom productUom;
    private List<ProductUom> productUoms;
    private List<ProductUom> filteredProductUoms;
    private String productUomId;
    private String decimals;
    private String currentForm = "/sc/productUom/View.xhtml";

    public void deleteProductUom() {
        if (productUomExist(productUom.getId())) {
            try {
                productUomFacade.remove(productUom);
            } catch (Exception e) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                return;
            }

            JsfUtil.addSuccessMessage("ItemDeleted");
            currentForm = "/sc/productUom/View.xhtml";

            if ((productUoms != null) && (productUoms.size() > 1)) {
                productUoms.remove(productUom);
                productUom = productUoms.get(0);
            } else {

                productUoms = productUomFacade.findAll();
                if ((productUoms != null) && (!productUoms.isEmpty())) {
                    productUom = productUoms.get(0);
                }
            }

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void cancelEditProductUom() {
        if (productUomExist(productUom.getId())) {
            currentForm = "/sc/productUom/View.xhtml";
        }
    }

    public void cancelCreateProductUom() {

        currentForm = "/sc/productUom/View.xhtml";

        if ((productUoms != null) && (!productUoms.isEmpty())) {
            productUom = productUoms.get(0);
        } else {

            productUoms = productUomFacade.findAll();

            if ((productUoms != null) && (!productUoms.isEmpty())) {
                productUom = productUoms.get(0);
            }
        }
    }

    public void updateProductUom() {
        if (productUomExistTwo(productUom.getId())) {
            productUom = productUomFacade.update(productUom);
            productUoms = productUomFacade.findAll();
            currentForm = "/sc/productUom/View.xhtml";
        }
    }

    private boolean productUomExistTwo(Integer id) {
        if (id != null) {
            ProductUom prodUom = productUomFacade.find(id);
            if (prodUom == null) {

                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = "/sc/productUom/View.xhtml";

                if ((productUoms != null) && (productUoms.size() > 1)) {
                    productUoms.remove(productUom);
                    productUom = productUoms.get(0);
                } else {

                    productUoms = productUomFacade.findAll();
                    if ((productUoms != null) && (!productUoms.isEmpty())) {
                        productUom = productUoms.get(0);
                    }
                }
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void prepareCreateProductUom() {
        productUom = new ProductUom();
        productUom.setActive(Boolean.TRUE);
        currentForm = "/sc/productUom/Create.xhtml";
    }

    public void resolveRequestParams() {

        currentForm = "/sc/productUom/View.xhtml";

        if (JsfUtil.isNumeric(productUomId)) {
            Integer id = Integer.valueOf(productUomId);
            productUom = productUomFacade.find(id);
            if (productUom != null) {
                productUoms = productUomFacade.findAll();
                return;
            }
        }

        productUoms = productUomFacade.findAll();
        if (productUoms != null && !productUoms.isEmpty()) {
            productUom = productUoms.get(0);
        }
    }

    public void createProductUom() {
        if (productUom != null) {
            productUom = productUomFacade.create(productUom);
            if (productUoms != null && !productUoms.isEmpty()) {
                productUoms.add(productUom);
            } else {

                productUoms = productUomFacade.findAll();
            }
            currentForm = "/sc/productUom/View.xhtml";
        }
    }

    public void prepareEditProductUom() {
        if (productUomExist(productUom.getId())) {
            currentForm = "/sc/productUom/Edit.xhtml";
        }
    }

    private boolean productUomExist(Integer id) {
        if (id != null) {
            productUom = productUomFacade.find(id);
            if (productUom == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = "/sc/productUom/View.xhtml";

                if ((productUoms != null) && (productUoms.size() > 1)) {
                    productUoms.remove(productUom);
                    productUom = productUoms.get(0);
                } else {
                    productUoms = productUomFacade.findAll();
                    if ((productUoms != null) && (!productUoms.isEmpty())) {
                        productUom = productUoms.get(0);
                    }
                }
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void prepareViewProductUom() {

        if (productUom != null) {
            if (productUomExist(productUom.getId())) {
                currentForm = "/sc/productUom/View.xhtml";
            }
        }
    }

    public int getProductUomIndex() {
        if (productUoms != null && productUom != null) {
            return productUoms.indexOf(productUom) + 1;
        }
        return 0;
    }

    public void nextProductUom() {
        if (productUoms.indexOf(productUom) == (productUoms.size() - 1)) {
            productUom = productUoms.get(0);
        } else {
            productUom = productUoms.get(productUoms.indexOf(productUom) + 1);
        }
    }

    public void previousProductUom() {
        if (productUoms.indexOf(productUom) == 0) {
            productUom = productUoms.get(productUoms.size() - 1);
        } else {
            productUom = productUoms.get(productUoms.indexOf(productUom) - 1);
        }
    }

    public List<ProductUom> getProductUoms() {
        if (productUoms == null) {
            productUoms = productUomFacade.findAll();
        }
        return productUoms;
    }

    public List<ProductUom> getActiveUoms() {
        if (productUoms == null) {
            productUoms = productUomFacade.findActiveUoms();
        }
        return productUoms;
    }

    public void setProductUoms(List<ProductUom> productUoms) {
        this.productUoms = productUoms;
    }

    public List<ProductUom> getFilteredProductUoms() {
        return filteredProductUoms;
    }

    public void setFilteredProductUoms(List<ProductUom> filteredProductUoms) {
        this.filteredProductUoms = filteredProductUoms;
    }

    public String getProductUomId() {
        return productUomId;
    }

    public void setProductUomId(String productUomId) {
        this.productUomId = productUomId;
    }

    public String getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(String currentForm) {
        this.currentForm = currentForm;
    }

    public ProductUom getProductUom() {
        return productUom;
    }

    public void setProductUom(ProductUom productUom) {
        this.productUom = productUom;
    }

}
