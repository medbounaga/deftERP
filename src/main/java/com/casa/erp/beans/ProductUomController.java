package com.casa.erp.beans;

import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.entities.ProductUom;
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
    private String currentPage = "/sc/productUom/List.xhtml";

    public void deleteProductUom(Integer id) {
        if (productUomExist(id)) {
            try {
                productUomFacade.remove(productUom);
            } catch (Exception e) {
                System.out.println("Error Delete: "+e.getMessage());
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                return;
            }

            JsfUtil.addSuccessMessage("ItemDeleted");
            currentPage = "/sc/productUom/List.xhtml";
            productUoms.remove(productUom);
            productUom = null;

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void cancelEditProductUom(Integer id) {
        if (productUomExist(id)) {
            currentPage = "/sc/productUom/View.xhtml";
        }
    }

    public void updateProductUom(Integer id) {
        if (productUomExistTwo(id)) {
            productUom = productUomFacade.update(productUom);
            productUoms = productUomFacade.findAll();
            currentPage = "/sc/productUom/View.xhtml";
        }
    }

    private boolean productUomExistTwo(Integer id) {
        if (id != null) {
            ProductUom productUom = productUomFacade.find(id);
            if (productUom == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                productUoms = null;
                currentPage = "/sc/productUom/List.xhtml";
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
        currentPage = "/sc/productUom/Create.xhtml";
    }

    public void viewProductUom() {
        if (productUomId != null && JsfUtil.isNumeric(productUomId)) {
            Integer id = Integer.valueOf(productUomId);
            productUom = productUomFacade.find(id);
            if (productUom != null) {
                productUoms = productUomFacade.findAll();
                currentPage = "/sc/productUom/View.xhtml";
                return;
            }
        }

        productUoms = productUomFacade.findAll();
        currentPage = "/sc/productUom/List.xhtml";
    }

    public void createProductUom() {
        if (productUom != null) {
            productUom = productUomFacade.create(productUom);
            productUoms = productUomFacade.findAll();
            currentPage = "/sc/productUom/View.xhtml";
        }
    }

    public void prepareEditProductUom(Integer id) {
        if (productUomExist(id)) {
            currentPage = "/sc/productUom/Edit.xhtml";
        }
    }

    private boolean productUomExist(Integer id) {
        if (id != null) {
            productUom = productUomFacade.find(id);
            if (productUom == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                productUoms = null;
                currentPage = "/sc/productUom/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public void prepareView() {

        if (productUom != null) {
            if (productUomExist(productUom.getId())) {
                currentPage = "/sc/productUom/View.xhtml";
            }
        }
    }

    public void showProductUomList() {
        productUom = null;
        currentPage = "/sc/productUom/List.xhtml";
    }

    public void showProductUomForm() {
        if (productUoms.size() > 0) {
            productUom = productUoms.get(0);
            currentPage = "/sc/productUom/View.xhtml";
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

    public String getPage() {
        return currentPage;
    }

    public void setPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public ProductUom getProductUom() {
        return productUom;
    }

    public void setProductUom(ProductUom productUom) {
        this.productUom = productUom;
    }

}
