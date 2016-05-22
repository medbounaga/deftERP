package com.casa.erp.beans;

import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.entities.ProductUomCategory;
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
    private String currentPage = "/sc/productUomCtg/List.xhtml";

    public void prepareCreateProductUomCategory() {
        productUomCategory = new ProductUomCategory();
        currentPage = "/sc/productUomCtg/Create.xhtml";
    }

    public void deleteProductUomCategory(Integer id) {
        if (productUomCategoryExist(id)) {
            try {
                productUomCategoryFacade.remove(productUomCategory);
            } catch (Exception e) {
                System.out.println("Error Delete: "+e.getMessage());
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                return;
            }

            JsfUtil.addSuccessMessage("ItemDeleted");
            currentPage = "/sc/productUomCtg/List.xhtml";
            productUomCategories.remove(productUomCategory);
            productUomCategory = null;

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void cancelEditProductUomCategory(Integer id) {
        if (productUomCategoryExist(id)) {
            currentPage = "/sc/productUomCtg/View.xhtml";
        }
    }

    public void updateProductUomCategory(Integer id) {
        if (productUomCategoryExistTwo(id)) {
            productUomCategory = productUomCategoryFacade.update(productUomCategory);
            productUomCategories = productUomCategoryFacade.findAll();
            currentPage = "/sc/productUomCtg/View.xhtml";
        }
    }

    private boolean productUomCategoryExistTwo(Integer id) {
        if (id != null) {
            ProductUomCategory productUomCategory = productUomCategoryFacade.find(id);
            if (productUomCategory == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                productUomCategories = null;
                currentPage = "/sc/productUomCtg/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void viewProductUomCategory() {
        if (productUomCategoryId != null && JsfUtil.isNumeric(productUomCategoryId)) {
            Integer id = Integer.valueOf(productUomCategoryId);
            productUomCategory = productUomCategoryFacade.find(id);
            if (productUomCategory != null) {
                productUomCategories = productUomCategoryFacade.findAll();
                currentPage = "/sc/productUomCtg/View.xhtml";
                return;
            }
        }

        productUomCategories = productUomCategoryFacade.findAll();
        currentPage = "/sc/productUomCtg/List.xhtml";
    }

    public void createProductUomCategory() {
        if (productUomCategory != null) {
            productUomCategory = productUomCategoryFacade.create(productUomCategory);
            productUomCategories = productUomCategoryFacade.findAll();
            currentPage = "/sc/productUomCtg/View.xhtml";
        }
    }

    public void prepareEditProductUomCategory(Integer id) {
        if (productUomCategoryExist(id)) {
            currentPage = "/sc/productUomCtg/Edit.xhtml";
        }
    }

    private boolean productUomCategoryExist(Integer id) {
        if (id != null) {
            productUomCategory = productUomCategoryFacade.find(id);
            if (productUomCategory == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                productUomCategories = null;
                currentPage = "/sc/productUomCtg/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void prepareView() {

        if (productUomCategory != null) {
            if (productUomCategoryExist(productUomCategory.getId())) {
                currentPage = "/sc/productUomCtg/View.xhtml";
            }
        }
    }

    public void showProductUomCategoryList() {
        productUomCategory = null;
        currentPage = "/sc/productUomCtg/List.xhtml";
    }

    public void showProductUomCategoryForm() {
        if (productUomCategories.size() > 0) {
            productUomCategory = productUomCategories.get(0);
            currentPage = "/sc/productUomCtg/View.xhtml";
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

    public String getPage() {
        return currentPage;
    }

    public void setPage(String currentPage) {
        this.currentPage = currentPage;
    }

}
