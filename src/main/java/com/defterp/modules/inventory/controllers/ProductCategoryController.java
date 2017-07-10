package com.defterp.modules.inventory.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.modules.inventory.entities.ProductCategory;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.modules.inventory.queryBuilders.ProductCategoryQueryBuilder;
import java.util.List;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author MOHAMMED BOUNAGA
 *
 * github.com/medbounaga
 */

@Named(value = "productCategoryController")
@ViewScoped
public class ProductCategoryController extends AbstractController {

    private List<ProductCategory> productCategories;
    private List<ProductCategory> filteredProductCategories;
    private ProductCategory productCategory;
    private String productCategoryId;
    private QueryWrapper query;

    public ProductCategoryController() {
        super("/sc/productCtg/");
    }

    public void prepareCreateProductCategory() {
        productCategory = new ProductCategory();
        productCategory.setActive(Boolean.TRUE);
        currentForm = CREATE_URL;
    }

    public void deleteProductCategory() {
        if (productCategoryExist(productCategory.getId())) {

            boolean deleted = super.deleteItem(productCategory);

            if (deleted) {

                JsfUtil.addSuccessMessage("ItemDeleted");
                currentForm = VIEW_URL;

                if (productCategories != null && productCategories.size() > 1) {
                    productCategories.remove(productCategory);
                    productCategory = productCategories.get(0);
                } else {
                    query = ProductCategoryQueryBuilder.getFindAllQuery();
                    productCategories = super.findWithQuery(query);

                    if (productCategories != null && !productCategories.isEmpty()) {
                        productCategory = productCategories.get(0);
                    }
                }

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
            }
        } 
    }

    public void cancelEditProductCategory() {
        if (productCategoryExist(productCategory.getId())) {
            currentForm = VIEW_URL;
        }
    }

    public void cancelCreateProductCategory() {

        currentForm = VIEW_URL;

        if ((productCategories != null) && (!productCategories.isEmpty())) {
            productCategory = productCategories.get(0);
        } else {

            query = ProductCategoryQueryBuilder.getFindAllQuery();
            productCategories = super.findWithQuery(query);

            if ((productCategories != null) && (!productCategories.isEmpty())) {
                productCategory = productCategories.get(0);
            }
        }
    }

    public void updateProductCategory() {
        if (productCategoryExistTwo(productCategory.getId())) {
            productCategory = super.updateItem(productCategory);
            query = ProductCategoryQueryBuilder.getFindAllQuery();
            productCategories = super.findWithQuery(query);
            currentForm = VIEW_URL;
        }
    }

    private boolean productCategoryExistTwo(Integer id) {
        if (id != null) {
            ProductCategory productCat = super.findItemById(id, ProductCategory.class);
            if (productCat == null) {

                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = VIEW_URL;

                if ((productCategories != null) && (productCategories.size() > 1)) {
                    productCategories.remove(productCategory);
                    productCategory = productCategories.get(0);
                } else {

                    query = ProductCategoryQueryBuilder.getFindAllQuery();
                    productCategories = super.findWithQuery(query);
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

        currentForm = VIEW_URL;

        if (JsfUtil.isNumeric(productCategoryId)) {
            Integer id = Integer.valueOf(productCategoryId);
            productCategory = super.findItemById(id, ProductCategory.class);
            if (productCategory != null) {
                query = ProductCategoryQueryBuilder.getFindAllQuery();
                productCategories = super.findWithQuery(query);
                return;
            }
        }

        query = ProductCategoryQueryBuilder.getFindAllQuery();
        productCategories = super.findWithQuery(query);
        if (productCategories != null && !productCategories.isEmpty()) {
            productCategory = productCategories.get(0);
        }
    }

    public void createProductCategory() {
        if (productCategory != null) {
            productCategory = super.createItem(productCategory);
            if (productCategories != null && !productCategories.isEmpty()) {
                productCategories.add(productCategory);
            } else {

                query = ProductCategoryQueryBuilder.getFindAllQuery();
                productCategories = super.findWithQuery(query);
            }
            currentForm = VIEW_URL;
        }
    }

    public void prepareEditProductCategory() {
        if (productCategoryExist(productCategory.getId())) {
            currentForm = EDIT_URL;
        }
    }

    private boolean productCategoryExist(Integer id) {
        if (id != null) {
            productCategory = super.findItemById(id, ProductCategory.class);
            if (productCategory == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = VIEW_URL;

                if ((productCategories != null) && (productCategories.size() > 1)) {
                    productCategories.remove(productCategory);
                    productCategory = productCategories.get(0);
                } else {
                    query = ProductCategoryQueryBuilder.getFindAllQuery();
                    productCategories = super.findWithQuery(query);
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
                currentForm = VIEW_URL;
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
            query = ProductCategoryQueryBuilder.getFindAllQuery();
            productCategories = super.findWithQuery(query);
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

}
