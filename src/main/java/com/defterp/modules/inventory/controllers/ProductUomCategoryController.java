package com.defterp.modules.inventory.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.modules.inventory.entities.ProductUomCategory;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.modules.inventory.queryBuilders.ProductUomCategoryQueryBuilder;
import java.util.List;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author MOHAMMED BOUNAGA
 *
 * github.com/medbounaga
 */

@Named(value = "productUomCategoryController")
@ViewScoped
public class ProductUomCategoryController extends AbstractController {

    private ProductUomCategory productUomCategory;
    private List<ProductUomCategory> productUomCategories;
    private List<ProductUomCategory> filteredProductUomCategories;
    private String productUomCategoryId;
    private QueryWrapper query;

    public ProductUomCategoryController() {
        super("/sc/productUomCtg/");
    }

    public void prepareCreateProductUomCategory() {
        productUomCategory = new ProductUomCategory();
        currentForm = CREATE_URL;
    }

    public void deleteProductUomCategory() {
        if (productUomCategoryExist(productUomCategory.getId())) {

            boolean deleted = super.deleteItem(productUomCategory);

            if (deleted) {

                JsfUtil.addSuccessMessage("ItemDeleted");
                currentForm = VIEW_URL;

                if ((productUomCategories != null) && (productUomCategories.size() > 1)) {
                    productUomCategories.remove(productUomCategory);
                    productUomCategory = productUomCategories.get(0);
                } else {
                    query = ProductUomCategoryQueryBuilder.getFindAllQuery();
                    productUomCategories = super.findWithQuery(query);
                    if ((productUomCategories != null) && (!productUomCategories.isEmpty())) {
                        productUomCategory = productUomCategories.get(0);
                    }
                }

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
            }
            
        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void cancelEditProductUomCategory() {
        if (productUomCategoryExist(productUomCategory.getId())) {
            currentForm = VIEW_URL;
        }
    }

    public void cancelCreateProductUomCategory() {
        if ((productUomCategories != null) && (!productUomCategories.isEmpty())) {
            productUomCategory = productUomCategories.get(0);
            currentForm = VIEW_URL;
        } else {
            query = ProductUomCategoryQueryBuilder.getFindAllQuery();
            productUomCategories = super.findWithQuery(query);
            if ((productUomCategories != null) && (!productUomCategories.isEmpty())) {
                productUomCategory = productUomCategories.get(0);
                currentForm = VIEW_URL;
            }
        }
    }

    public void updateProductUomCategory() {
        if (productUomCategoryExistTwo(productUomCategory.getId())) {
            productUomCategory = super.updateItem(productUomCategory);
            query = ProductUomCategoryQueryBuilder.getFindAllQuery();
            productUomCategories = super.findWithQuery(query);
            currentForm = VIEW_URL;
        }
    }

    private boolean productUomCategoryExistTwo(Integer id) {
        if (id != null) {
            ProductUomCategory productUomCat = super.findItemById(id, ProductUomCategory.class);
            if (productUomCat == null) {

                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = VIEW_URL;

                if ((productUomCategories != null) && (productUomCategories.size() > 1)) {
                    productUomCategories.remove(productUomCategory);
                    productUomCategory = productUomCategories.get(0);
                } else {

                    query = ProductUomCategoryQueryBuilder.getFindAllQuery();
                    productUomCategories = super.findWithQuery(query);
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

        currentForm = VIEW_URL;

        if (JsfUtil.isNumeric(productUomCategoryId)) {
            Integer id = Integer.valueOf(productUomCategoryId);
            productUomCategory = super.findItemById(id, ProductUomCategory.class);
            if (productUomCategory != null) {
                query = ProductUomCategoryQueryBuilder.getFindAllQuery();
                productUomCategories = super.findWithQuery(query);
                return;
            }
        }

        query = ProductUomCategoryQueryBuilder.getFindAllQuery();
        productUomCategories = super.findWithQuery(query);

        if (productUomCategories != null && !productUomCategories.isEmpty()) {
            productUomCategory = productUomCategories.get(0);
        }
    }

    public void createProductUomCategory() {
        if (productUomCategory != null) {
            productUomCategory = super.createItem(productUomCategory);
            if (productUomCategories != null && !productUomCategories.isEmpty()) {
                productUomCategories.add(productUomCategory);
            } else {

                query = ProductUomCategoryQueryBuilder.getFindAllQuery();
                productUomCategories = super.findWithQuery(query);
            }
            currentForm = VIEW_URL;
        }
    }

    public void prepareEditProductUomCategory() {
        if (productUomCategoryExist(productUomCategory.getId())) {
            currentForm = EDIT_URL;
        }
    }

    private boolean productUomCategoryExist(Integer id) {
        if (id != null) {
            productUomCategory = super.findItemById(id, ProductUomCategory.class);
            if (productUomCategory == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");

                if ((productUomCategories != null) && (productUomCategories.size() > 1)) {
                    productUomCategories.remove(productUomCategory);
                    productUomCategory = productUomCategories.get(0);
                } else {
                    query = ProductUomCategoryQueryBuilder.getFindAllQuery();
                    productUomCategories = super.findWithQuery(query);
                    if ((productUomCategories != null) && (!productUomCategories.isEmpty())) {
                        productUomCategory = productUomCategories.get(0);
                    }
                }
                currentForm = VIEW_URL;
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
                currentForm = VIEW_URL;
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

}
