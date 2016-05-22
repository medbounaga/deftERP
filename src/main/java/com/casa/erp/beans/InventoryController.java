package com.casa.erp.beans;

import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.entities.Inventory;
import com.casa.erp.dao.InventoryFacade;
import com.casa.erp.validation.StrictlyPositiveNumber;
import java.io.Serializable;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Named(value = "inventoryController")
@ViewScoped
public class InventoryController implements Serializable {

    @Inject
    private InventoryFacade inventoryFacade;
    private List<Inventory> inventory;
    private Inventory productInventory;
    @StrictlyPositiveNumber(message = "{PositiveQuantity}")
    private Double newQuantityOnHand;
    private String inventoryId;
    private List<Inventory> filteredInventory;
    private String currentPage = "/sc/inventory/List.xhtml";

    public void prepareProductUpdate() {
        newQuantityOnHand = productInventory.getQuantityOnHand();
    }

    public void updateQuantity() {

        newQuantityOnHand = JsfUtil.round(newQuantityOnHand, productInventory.getProduct().getUom().getDecimals());
        if (newQuantityOnHand == 0d) {
            JsfUtil.addWarningMessage("PositiveQuantity");
            FacesContext.getCurrentInstance().validationFailed();
        } else {

            productInventory.setQuantityOnHand(newQuantityOnHand);
            productInventory.setTotalCost(JsfUtil.round(productInventory.getUnitCost() * productInventory.getQuantityOnHand()));
            productInventory = inventoryFacade.update(productInventory);
            inventory.set(inventory.indexOf(productInventory), productInventory);
            newQuantityOnHand = null;
        }
    }

    public void viewInventory() {
        if (inventoryId != null && JsfUtil.isNumeric(inventoryId)) {
            Integer id = Integer.valueOf(inventoryId);
            productInventory = inventoryFacade.find(id);
            if (productInventory != null) {
                inventory = inventoryFacade.findAll();
                currentPage = "/sc/inventory/View.xhtml";
                return;
            }
        }

        inventory = inventoryFacade.findAll();
        currentPage = "/sc/inventory/List.xhtml";
    }

    public void prepareView() {
        if (productInventory != null) {
            if (inventoryExist(productInventory.getId())) {
                currentPage = "/sc/inventory/View.xhtml";
            }
        }
    }

    public void showInventoryList() {
        productInventory = null;
        currentPage = "/sc/inventory/List.xhtml";
    }

    public void showInventoryForm() {
        if (inventory.size() > 0) {
            productInventory = inventory.get(0);
            currentPage = "/sc/inventory/View.xhtml";
        }
    }

    public int getInventoryIndex() {
        if (inventory != null && productInventory != null) {
            return inventory.indexOf(productInventory) + 1;
        }
        return 0;
    }

    public void nextInventory() {
        if (inventory.indexOf(productInventory) == (inventory.size() - 1)) {
            productInventory = inventory.get(0);
        } else {
            productInventory = inventory.get(inventory.indexOf(productInventory) + 1);
        }
    }

    public void previousInventory() {
        if (inventory.indexOf(productInventory) == 0) {
            productInventory = inventory.get(inventory.size() - 1);
        } else {
            productInventory = inventory.get(inventory.indexOf(productInventory) - 1);
        }
    }

    private boolean inventoryExist(Integer id) {
        if (id != null) {
            productInventory = inventoryFacade.find(id);
            if (productInventory == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                inventory = null;
                currentPage = "/sc/inventory/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    public List<Inventory> getInventory() {
        if (inventory == null) {
            inventory = inventoryFacade.findAll();
        }
        return inventory;
    }

    public void setInventory(List<Inventory> inventory) {
        this.inventory = inventory;
    }

    public Inventory getProductInventory() {
        return productInventory;
    }

    public void setProductInventory(Inventory productInventory) {
        this.productInventory = productInventory;
    }

    public List<Inventory> getFilteredInventory() {
        return filteredInventory;
    }

    public void setFilteredInventory(List<Inventory> filteredInventory) {
        this.filteredInventory = filteredInventory;
    }

    public String getPage() {
        return currentPage;
    }

    public void setPage(String currentPage) {
        this.currentPage = currentPage;
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Double getNewQuantityOnHand() {
        return newQuantityOnHand;
    }

    public void setNewQuantityOnHand(Double newQuantityOnHand) {
        this.newQuantityOnHand = newQuantityOnHand;
    }

}
