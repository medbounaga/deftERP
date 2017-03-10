package com.defterp.modules.inventory.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.modules.inventory.entities.ProductUomCategory;
import com.casa.erp.dao.ProductUomCategoryFacade;
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
@Named(value = "productUomCategoryController")
@ViewScoped
public class ProductUomCategoryController implements Serializable {

    @Inject
    private ProductUomCategoryFacade productUomCategoryFacade;
    private ProductUomCategory productUomCategory;
    private List<ProductUomCategory> productUomCategories;
    private List<ProductUomCategory> filteredProductUomCategories;
    private String productUomCategoryId;
    private String currentForm = "/sc/productUomCtg/View.xhtml";

    public void prepareCreateProductUomCategory() {
        productUomCategory = new ProductUomCategory();
        currentForm = "/sc/productUomCtg/Create.xhtml";
    }

    public void deleteProductUomCategory() {
        if (productUomCategoryExist(productUomCategory.getId())) {
            try {
                productUomCategoryFacade.remove(productUomCategory);
            } catch (Exception e) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                return;
            }

            JsfUtil.addSuccessMessage("ItemDeleted");
            currentForm = "/sc/productUomCtg/View.xhtml";

            if ((productUomCategories != null) && (productUomCategories.size() > 1)) {
                productUomCategories.remove(productUomCategory);
                productUomCategory = productUomCategories.get(0);
            } else {

                productUomCategories = productUomCategoryFacade.findAll();
                if ((productUomCategories != null) && (!productUomCategories.isEmpty())) {
                    productUomCategory = productUomCategories.get(0);
                }
            }

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void cancelEditProductUomCategory() {
        if (productUomCategoryExist(productUomCategory.getId())) {
            currentForm = "/sc/productUomCtg/View.xhtml";
        }
    }

    public void cancelCreateProductUomCategory() {
        if ((productUomCategories != null) && (!productUomCategories.isEmpty())) {
            productUomCategory = productUomCategories.get(0);
            currentForm = "/sc/productUomCtg/View.xhtml";
        } else {
            productUomCategories =  productUomCategoryFacade.findAll();
            if ((productUomCategories != null) && (!productUomCategories.isEmpty())) {
                productUomCategory = productUomCategories.get(0);
                currentForm = "/sc/productUomCtg/View.xhtml";
            }
        }
    }

    public void updateProductUomCategory() {
        if (productUomCategoryExistTwo(productUomCategory.getId())) {
            productUomCategory = productUomCategoryFacade.update(productUomCategory);
            productUomCategories = productUomCategoryFacade.findAll();
            currentForm = "/sc/productUomCtg/View.xhtml";
        }
    }

    private boolean productUomCategoryExistTwo(Integer id) {
        if (id != null) {
            ProductUomCategory productUomCat = productUomCategoryFacade.find(id);
            if (productUomCat == null) {

                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = "/sc/productUomCtg/View.xhtml";

                if ((productUomCategories != null) && (productUomCategories.size() > 1)) {
                    productUomCategories.remove(productUomCategory);
                    productUomCategory = productUomCategories.get(0);
                } else {

                    productUomCategories = productUomCategoryFacade.findAll();
                    if ((productUomCategories != null) && (!productUomCategories.isEmpty())) {
                        productUomCategory = productUomCategories.get(0);
                    }
                }
                return false;
            }
            return true;
        }
        return false;
    }

    public void resolveRequestParams() {

        currentForm = "/sc/productUomCtg/View.xhtml";

        if (JsfUtil.isNumeric(productUomCategoryId)) {
            Integer id = Integer.valueOf(productUomCategoryId);
            productUomCategory = productUomCategoryFacade.find(id);
            if (productUomCategory != null) {
                productUomCategories = productUomCategoryFacade.findAll();
                return;
            }
        }

        productUomCategories = productUomCategoryFacade.findAll();
        if (productUomCategories != null && !productUomCategories.isEmpty()) {
            productUomCategory = productUomCategories.get(0);
        }
    }

    public void createProductUomCategory() {
        if (productUomCategory != null) {
            productUomCategory = productUomCategoryFacade.create(productUomCategory);
            if (productUomCategories != null && !productUomCategories.isEmpty()) {
                productUomCategories.add(productUomCategory);
            } else {

                productUomCategories = productUomCategoryFacade.findAll();
            }
            currentForm = "/sc/productUomCtg/View.xhtml";
        }
    }

    public void prepareEditProductUomCategory() {
        if (productUomCategoryExist(productUomCategory.getId())) {
            currentForm = "/sc/productUomCtg/Edit.xhtml";
        }
    }

    private boolean productUomCategoryExist(Integer id) {
        if (id != null) {
            productUomCategory = productUomCategoryFacade.find(id);
            if (productUomCategory == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");

                if ((productUomCategories != null) && (productUomCategories.size() > 1)) {
                    productUomCategories.remove(productUomCategory);
                    productUomCategory = productUomCategories.get(0);
                } else {
                    productUomCategories = productUomCategoryFacade.findAll();
                    if ((productUomCategories != null) && (!productUomCategories.isEmpty())) {
                        productUomCategory = productUomCategories.get(0);
                    }
                }
                currentForm = "/sc/productUomCtg/View.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void prepareViewUomCategory() {

        if (productUomCategory != null) {
            if (productUomCategoryExist(productUomCategory.getId())) {
                currentForm = "/sc/productUomCtg/View.xhtml";
            }
        }
    }

    public int getProductUomCategoryIndex() {
        if (productUomCategories != null && productUomCategory != null) {
            return productUomCategories.indexOf(productUomCategory) + 1;
        }
        return 0;
    }

    public void nextProductUomCategory() {
        if (productUomCategories.indexOf(productUomCategory) == (productUomCategories.size() - 1)) {
            productUomCategory = productUomCategories.get(0);
        } else {
            productUomCategory = productUomCategories.get(productUomCategories.indexOf(productUomCategory) + 1);
        }
    }

    public void previousProductUomCategory() {
        if (productUomCategories.indexOf(productUomCategory) == 0) {
            productUomCategory = productUomCategories.get(productUomCategories.size() - 1);
        } else {
            productUomCategory = productUomCategories.get(productUomCategories.indexOf(productUomCategory) - 1);
        }
    }

    public ProductUomCategory getProductUomCategory() {
        return productUomCategory;
    }

    public void setProductUomCategory(ProductUomCategory productUomCategory) {
        this.productUomCategory = productUomCategory;
    }

    public List<ProductUomCategory> getProductUomCategories() {
        if (productUomCategories == null) {
            productUomCategories = productUomCategoryFacade.findAll();
        }
        return productUomCategories;
    }

    public void setProductUomCategories(List<ProductUomCategory> productUomCategories) {
        this.productUomCategories = productUomCategories;
    }

    public List<ProductUomCategory> getFilteredProductUomCategories() {
        return filteredProductUomCategories;
    }

    public void setFilteredProductUomCategories(List<ProductUomCategory> filteredProductUomCategories) {
        this.filteredProductUomCategories = filteredProductUomCategories;
    }

    public String getProductUomCategoryId() {
        return productUomCategoryId;
    }

    public void setProductUomCategoryId(String productUomCategoryId) {
        this.productUomCategoryId = productUomCategoryId;
    }

    public String getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(String currentForm) {
        this.currentForm = currentForm;
    }

}
