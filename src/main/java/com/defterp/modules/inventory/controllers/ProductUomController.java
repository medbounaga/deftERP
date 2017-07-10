package com.defterp.modules.inventory.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.modules.inventory.entities.ProductUom;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.modules.inventory.queryBuilders.ProductUomQueryBuilder;
import java.util.List;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author MOHAMMED BOUNAGA
 *
 * github.com/medbounaga
 */

@Named(value = "productUomController")
@ViewScoped
public class ProductUomController extends AbstractController {

    private ProductUom productUom;
    private List<ProductUom> productUoms;
    private List<ProductUom> filteredProductUoms;
    private String productUomId;
    private QueryWrapper query;

    public ProductUomController() {
        super("/sc/productUom/");
    }

    public void deleteProductUom() {
        if (productUomExist(productUom.getId())) {

            boolean deleted = super.deleteItem(productUom);

            if (deleted) {

                JsfUtil.addSuccessMessage("ItemDeleted");
                currentForm = VIEW_URL;

                if ((productUoms != null) && (productUoms.size() > 1)) {
                    productUoms.remove(productUom);
                    productUom = productUoms.get(0);
                } else {

                    query = ProductUomQueryBuilder.getFindAllQuery();
                    productUoms = super.findWithQuery(query);
                    if ((productUoms != null) && (!productUoms.isEmpty())) {
                        productUom = productUoms.get(0);
                    }
                }

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
            }

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void cancelEditProductUom() {
        if (productUomExist(productUom.getId())) {
            currentForm = VIEW_URL;
        }
    }

    public void cancelCreateProductUom() {

        currentForm = VIEW_URL;

        if ((productUoms != null) && (!productUoms.isEmpty())) {
            productUom = productUoms.get(0);
        } else {

            query = ProductUomQueryBuilder.getFindAllQuery();
            productUoms = super.findWithQuery(query);

            if ((productUoms != null) && (!productUoms.isEmpty())) {
                productUom = productUoms.get(0);
            }
        }
    }

    public void updateProductUom() {
        if (productUomExistTwo(productUom.getId())) {
            productUom = super.updateItem(productUom);
            query = ProductUomQueryBuilder.getFindAllQuery();
            productUoms = super.findWithQuery(query);
            currentForm = VIEW_URL;
        }
    }

    private boolean productUomExistTwo(Integer id) {
        if (id != null) {
            ProductUom prodUom = super.findItemById(id, ProductUom.class);
            if (prodUom == null) {

                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = VIEW_URL;

                if ((productUoms != null) && (productUoms.size() > 1)) {
                    productUoms.remove(productUom);
                    productUom = productUoms.get(0);
                } else {

                    query = ProductUomQueryBuilder.getFindAllQuery();
                    productUoms = super.findWithQuery(query);
                    
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
        currentForm = CREATE_URL;
    }

    public void resolveRequestParams() {

        currentForm = VIEW_URL;

        if (JsfUtil.isNumeric(productUomId)) {
            Integer id = Integer.valueOf(productUomId);
            productUom = super.findItemById(id, ProductUom.class);
            if (productUom != null) {
                
                query = ProductUomQueryBuilder.getFindAllQuery();
                productUoms = super.findWithQuery(query);
                return;
            }
        }

        query = ProductUomQueryBuilder.getFindAllQuery();
        productUoms = super.findWithQuery(query);
        
        if (productUoms != null && !productUoms.isEmpty()) {
            productUom = productUoms.get(0);
        }
    }

    public void createProductUom() {
        if (productUom != null) {
            productUom = super.createItem(productUom);
            if (productUoms != null && !productUoms.isEmpty()) {
                productUoms.add(productUom);
            } else {

                query = ProductUomQueryBuilder.getFindAllQuery();
                productUoms = super.findWithQuery(query);
            }
            currentForm = VIEW_URL;
        }
    }

    public void prepareEditProductUom() {
        if (productUomExist(productUom.getId())) {
            currentForm = EDIT_URL;
        }
    }

    private boolean productUomExist(Integer id) {
        if (id != null) {
            productUom = super.findItemById(id, ProductUom.class);
            if (productUom == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = VIEW_URL;

                if ((productUoms != null) && (productUoms.size() > 1)) {
                    productUoms.remove(productUom);
                    productUom = productUoms.get(0);
                } else {
                    query = ProductUomQueryBuilder.getFindAllQuery();
                    productUoms = super.findWithQuery(query);
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
                currentForm = VIEW_URL;
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
        return productUoms;
    }

    public List<ProductUom> getActiveUoms() {
        if (productUoms == null) {
            query = ProductUomQueryBuilder.getFindActiveProductUomsQuery();
            productUoms = super.findWithQuery(query);
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

    public ProductUom getProductUom() {
        return productUom;
    }

    public void setProductUom(ProductUom productUom) {
        this.productUom = productUom;
    }

}
