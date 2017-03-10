package com.defterp.modules.inventory.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.modules.inventory.entities.ProductCategory;
import com.casa.erp.dao.ProductCategoryFacade;
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
@Named(value = "productCategoryController")
@ViewScoped
public class ProductCategoryController implements Serializable {

    @Inject
    private ProductCategoryFacade productCategoryFacade;
    private List<ProductCategory> productCategories;
    private List<ProductCategory> filteredProductCategories;
    private ProductCategory productCategory;
    private String productCategoryId;
    private String currentForm = "/sc/productCtg/View.xhtml";

    public void prepareCreateProductCategory() {
        productCategory = new ProductCategory();
        productCategory.setActive(Boolean.TRUE);
        currentForm = "/sc/productCtg/Create.xhtml";
    }

    public void deleteProductCategory() {
        if (productCategoryExist(productCategory.getId())) {
            try {
                productCategoryFacade.remove(productCategory);
            } catch (Exception e) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                return;
            }

            JsfUtil.addSuccessMessage("ItemDeleted");
            currentForm = "/sc/productCtg/View.xhtml";

            if ((productCategories != null) && (productCategories.size() > 1)) {
                productCategories.remove(productCategory);
                productCategory = productCategories.get(0);
            } else {

                productCategories = productCategoryFacade.findAll();
                if ((productCategories != null) && (!productCategories.isEmpty())) {
                    productCategory = productCategories.get(0);
                }
            }

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void cancelEditProductCategory() {
        if (productCategoryExist(productCategory.getId())) {
            currentForm = "/sc/productCtg/View.xhtml";
        }
    }

    public void cancelCreateProductCategory() {
        
        currentForm = "/sc/productCtg/View.xhtml";

        if ((productCategories != null) && (!productCategories.isEmpty())) {
            productCategory = productCategories.get(0);
        } else {
            
            productCategories = productCategoryFacade.findAll();
            
            if ((productCategories != null) && (!productCategories.isEmpty())) {
                productCategory = productCategories.get(0);
            }
        }
    }

    public void updateProductCategory() {
        if (productCategoryExistTwo(productCategory.getId())) {
            productCategory = productCategoryFacade.update(productCategory);
            productCategories = productCategoryFacade.findAll();
            currentForm = "/sc/productCtg/View.xhtml";
        }
    }

    private boolean productCategoryExistTwo(Integer id) {
        if (id != null) {
            ProductCategory productCat = productCategoryFacade.find(id);
            if (productCat == null) {

                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = "/sc/productCtg/View.xhtml";

                if ((productCategories != null) && (productCategories.size() > 1)) {
                    productCategories.remove(productCategory);
                    productCategory = productCategories.get(0);
                } else {

                    productCategories = productCategoryFacade.findAll();
                    if ((productCategories != null) && (!productCategories.isEmpty())) {
                        productCategory = productCategories.get(0);
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

    public void resolveRequestParams() {

        currentForm = "/sc/productCtg/View.xhtml";

        if (JsfUtil.isNumeric(productCategoryId)) {
            Integer id = Integer.valueOf(productCategoryId);
            productCategory = productCategoryFacade.find(id);
            if (productCategory != null) {
                productCategories = productCategoryFacade.findAll();
                return;
            }
        }

        productCategories = productCategoryFacade.findAll();
        if (productCategories != null && !productCategories.isEmpty()) {
            productCategory = productCategories.get(0);
        }
    }

    public void createProductCategory() {
        if (productCategory != null) {
            productCategory = productCategoryFacade.create(productCategory);
            if (productCategories != null && !productCategories.isEmpty()) {
                productCategories.add(productCategory);
            } else {

                productCategories = productCategoryFacade.findAll();
            }
            currentForm = "/sc/productCtg/View.xhtml";
        }
    }

    public void prepareEditProductCategory() {
        if (productCategoryExist(productCategory.getId())) {
            currentForm = "/sc/productCtg/Edit.xhtml";
        }
    }

    private boolean productCategoryExist(Integer id) {
        if (id != null) {
            productCategory = productCategoryFacade.find(id);
            if (productCategory == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = "/sc/productCtg/View.xhtml";

                if ((productCategories != null) && (productCategories.size() > 1)) {
                    productCategories.remove(productCategory);
                    productCategory = productCategories.get(0);
                } else {
                    productCategories = productCategoryFacade.findAll();
                    if ((productCategories != null) && (!productCategories.isEmpty())) {
                        productCategory = productCategories.get(0);
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

    public void prepareViewProductCategory() {

        if (productCategory != null) {
            if (productCategoryExist(productCategory.getId())) {
                currentForm = "/sc/productCtg/View.xhtml";
            }
        }
    }

    public int getProductCategoryIndex() {
        if (productCategories != null && productCategory != null) {
            return productCategories.indexOf(productCategory) + 1;
        }
        return 0;
    }

    public void nextProductCategory() {
        if (productCategories.indexOf(productCategory) == (productCategories.size() - 1)) {
            productCategory = productCategories.get(0);
        } else {
            productCategory = productCategories.get(productCategories.indexOf(productCategory) + 1);
        }
    }

    public void previousProductCategory() {
        if (productCategories.indexOf(productCategory) == 0) {
            productCategory = productCategories.get(productCategories.size() - 1);
        } else {
            productCategory = productCategories.get(productCategories.indexOf(productCategory) - 1);
        }
    }

    public List<ProductCategory> getProductCategories() {
        if (productCategories == null) {
            productCategories = productCategoryFacade.findAll();
        }
        return productCategories;
    }

    public void setProductCategories(List<ProductCategory> productCategories) {

        this.productCategories = productCategories;
    }

    public List<ProductCategory> getFilteredProductCategories() {
        return filteredProductCategories;
    }

    public void setFilteredProductCategories(List<ProductCategory> filteredProductCategories) {
        this.filteredProductCategories = filteredProductCategories;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductCategoryId() {
        return productCategoryId;
    }

    public void setProductCategoryId(String productCategoryId) {
        this.productCategoryId = productCategoryId;
    }

    public String getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(String currentForm) {
        this.currentForm = currentForm;
    }

}
