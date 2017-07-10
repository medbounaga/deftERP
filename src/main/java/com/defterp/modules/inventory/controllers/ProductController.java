package com.defterp.modules.inventory.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.modules.inventory.entities.Product;
import com.defterp.modules.inventory.entities.ProductCategory;
import com.defterp.modules.inventory.entities.ProductUom;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.modules.inventory.queryBuilders.ProductQueryBuilder;
import com.defterp.modules.purchases.queryBuilders.PurchaseOrderLineQueryBuilder;
import com.defterp.modules.sales.queryBuilders.SaleOrderLineQueryBuilder;
import com.defterp.validators.annotations.StrictlyPositiveNumber;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.servlet.http.Part;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author MOHAMMED BOUNAGA
 *
 * github.com/medbounaga
 */

@Named(value = "productController")
@ViewScoped
public class ProductController extends AbstractController {

    private List<Product> products;
    private List<Product> filteredProducts;
    private Product product;
    private String productId;
    private String searchKey;
    @StrictlyPositiveNumber(message = "{PositiveQuantity}")
    private Double newQuantityOnHand;
    private boolean isGridView = true;
    private ProductCategory productCategory;
    private List<ProductCategory> topNProductCategories;
    private ProductUom unitOfMeasure;
    private List<ProductUom> topNUnitsOfMeasure;
    private Part image;
    private boolean imageModified;
    private QueryWrapper query;

    public ProductController() {
        super("/sc/product/");
    }     

    public void deleteProduct(Integer id) {

        if (productExist(id)) {
            
            boolean deleted = super.deleteItem(product);

            if (deleted) {
                showProducts();
                JsfUtil.addSuccessMessage("ItemDeleted");
                currentForm = VIEW_URL;
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
            }

        } else {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
        }
    }

    public void duplicateProduct(Integer id) {
        if (productExist(id)) {
            Product newProduct = new Product();
            newProduct.setName(product.getName() + " " + JsfUtil.getBundleString("Copy"));
            newProduct.setActive(Boolean.TRUE);
            newProduct.setPurchaseOk(Boolean.TRUE);
            newProduct.setSaleOk(Boolean.TRUE);
            newProduct.setCategory(product.getCategory());
            newProduct.setPurchasePrice(product.getPurchasePrice());
            newProduct.setSalePrice(product.getSalePrice());
            newProduct.setUom(product.getUom());
            newProduct.setVolume(product.getVolume());
            newProduct.setWeight(product.getWeight());
            newProduct.setLength(product.getLength());
            product = newProduct;

            currentForm = "/sc/product/Create.xhtml";
        }
    }

    public void setImage(Part image) {

        if (image != null) {
            try {
                InputStream input = image.getInputStream();
                product.setImage(IOUtils.toByteArray(input));
            } catch (IOException ex) {
                Logger.getLogger(ProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else if (image == null && product.getImage() != null && imageModified == true) {
            product.setImage(null);
        }
    }

    public Part getImage() {
        return image;
    }

    public void prepareCreateProduct() {

        product = new Product();
        product.setActive(Boolean.TRUE);
        product.setPurchaseOk(Boolean.TRUE);
        product.setSaleOk(Boolean.TRUE);
        currentForm = "/sc/product/Create.xhtml";
    }

    public void prepareViewProduct() {
        if (productExist(product.getId())) {
            query = ProductQueryBuilder.getFindAllQuery();
            products = super.findWithQuery(query);
            filteredProducts = new ArrayList<>();
            filteredProducts.addAll(products);
            currentForm = "/sc/product/View.xhtml";
        }
    }

    public void prepareViewProduct(int id) {
        if (productExist(id)) {
            
            query = ProductQueryBuilder.getFindAllQuery();
            products = super.findWithQuery(query);
            filteredProducts = new ArrayList<>();
            filteredProducts.addAll(products);
            currentForm = "/sc/product/View.xhtml";
        }
    }

    public void prepareEditProduct(Integer id) {
        if (productExist(id)) {
            imageModified = false;
            currentForm = "/sc/product/Edit.xhtml";
        }
    }

    public void cancelEditProduct(Integer id) {
        if (productExist(id)) {
            currentForm = "/sc/product/View.xhtml";
        }
    }

    public void editProduct(Integer id) {
        if (!productExistTwo(id)) {
            JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
            currentForm = "/sc/product/View.xhtml";
        } else if (product != null) {

            product.setPurchasePrice(JsfUtil.round(product.getPurchasePrice()));
            product.setSalePrice(JsfUtil.round(product.getSalePrice()));

            if (product.getSalePrice() == 0d || product.getPurchasePrice() == 0d) {
                if (product.getSalePrice() == 0d) {
                    JsfUtil.addWarningMessage("PositiveSalePrice");
                }

                if (product.getPurchasePrice() == 0d) {
                    JsfUtil.addWarningMessage("PositiveCost");

                }
                return;
            }

            if (product.getSalePrice() <= product.getPurchasePrice()) {
                JsfUtil.addWarningMessage("CostSalePriceError");
                return;
            }

            if (product.getDefaultCode() != null && !product.getDefaultCode().isEmpty()) {
                product.setDescription("[" + product.getDefaultCode() + "] " + product.getName());
            } else {
                product.setDescription(product.getName());
            }

            product.setLength(JsfUtil.round(product.getLength()));
            product.setVolume(JsfUtil.round(product.getVolume()));
            product.setWeight(JsfUtil.round(product.getWeight()));
            product = super.updateItem(product);
            products.set(products.indexOf(product), product);

            currentForm = "/sc/product/View.xhtml";
        }
    }

    public void createProduct() {
        UIInput nameInput = (UIInput) FacesContext.getCurrentInstance().getViewRoot().findComponent("mainForm:productCategoryMenu");
        nameInput.setValue(null);
        nameInput.setSubmittedValue(null);
        nameInput.setLocalValueSet(false);
        nameInput.setValid(true);
        if (product != null) {

            product.setPurchasePrice(JsfUtil.round(product.getPurchasePrice()));
            product.setSalePrice(JsfUtil.round(product.getSalePrice()));

            if (product.getSalePrice() == 0d || product.getPurchasePrice() == 0d) {
                if (product.getSalePrice() == 0d) {
                    JsfUtil.addWarningMessage("PositiveSalePrice");
                }

                if (product.getPurchasePrice() == 0d) {
                    JsfUtil.addWarningMessage("PositiveCost");

                }
                return;
            }

            if (product.getSalePrice() <= product.getPurchasePrice()) {
                JsfUtil.addWarningMessage("CostSalePriceError");
                return;
            }

            if (product.getDefaultCode() != null && !product.getDefaultCode().isEmpty()) {
                product.setDescription("[" + product.getDefaultCode() + "] " + product.getName());
            } else {
                product.setDescription(product.getName());
            }

            product.setLength(JsfUtil.round(product.getLength()));
            product.setVolume(JsfUtil.round(product.getVolume()));
            product.setWeight(JsfUtil.round(product.getWeight()));
            product.getInventory().setQuantityOnHand(0d);
            product.getInventory().setIncomingQuantity(0d);
            product.getInventory().setReservedQuantity(0d);
            product.getInventory().setActive(Boolean.TRUE);
            product.getInventory().setUnitCost(product.getPurchasePrice());
            product.getInventory().setTotalCost(0d);
            product.getInventory().setProduct(product);

            product = super.createItem(product);
            
            query = ProductQueryBuilder.getFindAllQuery();
            products = super.findWithQuery(query);
            
            currentForm = "/sc/product/View.xhtml";
        }
    }

    public void cancelCreate() {
        
        query = ProductQueryBuilder.getFindAllQuery();
        products = super.findWithQuery(query);
        
        filteredProducts = new ArrayList<>();
        filteredProducts.addAll(products);
        
        if (products != null && !products.isEmpty()) {           
            product = products.get(0);
            currentForm = "/sc/product/View.xhtml";
        }
    }

    public void prepareProductUpdate(Integer id) {
        if (productExist(id)) {
            newQuantityOnHand = product.getInventory().getQuantityOnHand();
        }
    }

    public void updateQuantity(Integer id) {
        if (productExist(id)) {
            newQuantityOnHand = JsfUtil.round(newQuantityOnHand, product.getUom().getDecimals());
            if (newQuantityOnHand == 0d) {
                JsfUtil.addWarningMessage("PositiveQuantity");
                FacesContext.getCurrentInstance().validationFailed();
            } else {

                product.getInventory().setQuantityOnHand(newQuantityOnHand);
                product.getInventory().setTotalCost(JsfUtil.round(product.getInventory().getUnitCost() * product.getInventory().getQuantityOnHand()));
                product.setInventory(super.updateItem(product.getInventory()));
                products.set(products.indexOf(product), product);
                newQuantityOnHand = null;
            }
        }
    }

    public void searchByName() {

        if (products != null && filteredProducts != null) {
            if (searchKey != null && !searchKey.isEmpty()) {

                products = new ArrayList<>();

                for (Product part : filteredProducts) {
                    if (StringUtils.containsIgnoreCase(part.getName(), searchKey)) {
                        products.add(part);
                    }
                }
            } else if (searchKey != null && searchKey.isEmpty()) {
                products = new ArrayList<>();
                products.addAll(filteredProducts);
            }
        }
    }

    private boolean productExist(Integer id) {
        if (id != null) {
            product = super.findItemById(id, Product.class);
            if (product == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                products = null;
                currentForm = "/sc/product/View.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    private boolean productExistTwo(Integer id) {
        if (id != null) {
            Product product = super.findItemById(id, Product.class);
            return product != null;
        }
        return false;
    }

    public void resolveRequestParams() {

        if (JsfUtil.isNumeric(productId)) {
            Integer id = Integer.valueOf(productId);
            product = super.findItemById(id, Product.class);
            if (product != null) {
                query = ProductQueryBuilder.getFindAllQuery();
                products = super.findWithQuery(query);
                currentForm = "/sc/product/View.xhtml";
                currentList = "/sc/product/Grid.xhtml";
                return;
            }
        }

        query = ProductQueryBuilder.getFindAllQuery();
        products = super.findWithQuery(query);
        
        filteredProducts = new ArrayList<>();
        filteredProducts.addAll(products);
        
        if (products != null && !products.isEmpty()) {
            product = products.get(0);
        }
        currentForm = "/sc/product/View.xhtml";
        currentList = "/sc/product/Grid.xhtml";
    }

    public void showProductList() {
        isGridView = false;
        showProducts();
    }

    public void showProductGrid() {
        isGridView = true;
        showProducts();
    }

    public void showProducts() {
        product = null;
        products = null;
        filteredProducts = null;

        if (isGridView) {
            currentList = "/sc/product/Grid.xhtml";

        } else {
            currentList = "/sc/product/List.xhtml";
        }
    }

//    public void showProductForm() {
//
//        query = ProductQueryBuilder.getFindAllProductsQuery();
//        products = super.findWithQuery(query);
//
//        if (products.size() > 0) {
//            product = products.get(0);
//            currentForm = "/sc/product/View.xhtml";
//        }
//    }

    public int getProductIndex() {
        if (product != null && products != null && !products.isEmpty()) {
            return products.indexOf(product) + 1;
        }
        return 0;
    }

    public void nextProduct() {
        if (products != null && !products.isEmpty() && product != null) {
            if (products.indexOf(product) == (products.size() - 1)) {
                product = products.get(0);
            } else {
                product = products.get(products.indexOf(product) + 1);
            }
        }
    }

    public void previousProduct() {
        if (products != null && !products.isEmpty() && product != null) {
            if (products.indexOf(product) == 0) {
                product = products.get(products.size() - 1);
            } else {
                product = products.get(products.indexOf(product) - 1);
            }
        }
    }

    public Integer countProductSales() {
        if (product != null) {
            query = SaleOrderLineQueryBuilder.getTotalProductSoldQuantityQuery(product.getId());
            return (Integer) super.findSingleWithQuery(query);
        }
        return 0;
    }

    public Integer countProductPurchases() {
        if (product != null) {
            query = PurchaseOrderLineQueryBuilder.getTotalProductPurchasedQuantityQuery(product.getId());
            return (Integer) super.findSingleWithQuery(query);
        }
        return 0;
    }

//    public List<ProductCategory> getTopNProductCategories() {
//        if (topNProductCategories == null) {
//            topNProductCategories = productFacade.findTopNProductCategories(1);
//        }
//        return topNProductCategories;
//    }
//
//    public List<ProductUom> getTopNUnitsOfMeasure() {
//        if (topNUnitsOfMeasure == null) {
//            topNUnitsOfMeasure = productFacade.findTopNUnitsOfMeasure(1);
//        }
//        return topNUnitsOfMeasure;
//    }

//    public void onSelectProductCategory() {
//        if ((productCategory != null) && (!topNProductCategories.contains(productCategory))) {
//            topNProductCategories.add(productCategory);
//        }
//        product.setCategory(productCategory);
//        System.out.println(productCategory == null);
//    }

    public void onSelectUnitOfMeasure() {
        if ((unitOfMeasure != null) && (!topNUnitsOfMeasure.contains(unitOfMeasure))) {
            topNUnitsOfMeasure.add(unitOfMeasure);
        }
        product.setUom(unitOfMeasure);
        System.out.println(unitOfMeasure == null);

    }

    public void validateImage(FacesContext ctx, UIComponent comp, Object value) {
        if (value != null) {
            Part file = (Part) value;

            if (!file.getContentType().startsWith("image")) {
                String msg = JsfUtil.getBundle().getString("NotImage");
                JsfUtil.throwWarningValidatorException(msg);
            }
            if (file.getSize() > 200024) {
                String msg = JsfUtil.getBundle().getString("ImageTooBig");
                JsfUtil.throwWarningValidatorException(msg);
            }
        }
    }

    public void refreshProductsList() {
        products = null;
    }

    public List<Product> getProducts() {

        if (products == null) {
            query = ProductQueryBuilder.getFindAllQuery();
            products = super.findWithQuery(query);
            filteredProducts = new ArrayList<>();
            filteredProducts.addAll(products);
        }
        return products;
    }

//    public List<Product> getPurchasedProducts() {
//        if (products == null) {
//            products = productFacade.findPurchasedProducts();
//        }
//        return products;
//    }
//
//    public List<Product> getSoldProducts() {
//        if (products == null) {
//            products = productFacade.findSoldProducts();
//        }
//        return products;
//    }

    public List<Product> getFilteredProducts() {
        return filteredProducts;
    }

    public void setFilteredProducts(List<Product> filteredProducts) {
        this.filteredProducts = filteredProducts;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public ProductCategory getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(ProductCategory productCategory) {
        this.productCategory = productCategory;
        System.out.println(this.productCategory == null);
    }

    public ProductUom getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(ProductUom unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getSearchKey() {
        return searchKey;
    }

    public Double getNewQuantityOnHand() {
        return newQuantityOnHand;
    }

    public void setNewQuantityOnHand(Double newQuantityOnHand) {
        this.newQuantityOnHand = newQuantityOnHand;
    }

    public void setSearchKey(String searchKey) {
        this.searchKey = searchKey;
    }

    public boolean getImageModified() {
        return imageModified;
    }

    public void setImageModified(boolean imageModified) {
        this.imageModified = imageModified;
    }

}
