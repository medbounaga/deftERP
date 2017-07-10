package com.defterp.modules.inventory.controllers;

import com.defterp.util.JsfUtil;
import com.defterp.modules.inventory.entities.Inventory;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.modules.inventory.queryBuilders.InventoryQueryBuilder;
import com.defterp.validators.annotations.StrictlyPositiveNumber;
import java.util.List;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.faces.view.ViewScoped;

/**
 *
 * @author MOHAMMED BOUNAGA
 *
 * github.com/medbounaga
 */

@Named(value = "inventoryController")
@ViewScoped
public class InventoryController extends AbstractController {

    @StrictlyPositiveNumber(message = "{PositiveQuantity}")
    private Double newQuantityOnHand;
    private List<Inventory> inventory;
    private Inventory productInventory;
    private String inventoryId;
    private List<Inventory> filteredInventory;
    private QueryWrapper query;

    public InventoryController() {
        super("/sc/inventory/");
    }

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
            productInventory = super.updateItem(productInventory);
            inventory.set(inventory.indexOf(productInventory), productInventory);
            newQuantityOnHand = null;
        }
    }

    public void resolveRequestParams() {

        currentForm = VIEW_URL;

        if (JsfUtil.isNumeric(inventoryId)) {
            Integer id = Integer.valueOf(inventoryId);
            productInventory = super.findItemById(id, Inventory.class);
            if (productInventory != null) {
                query = InventoryQueryBuilder.getFindAllQuery();
                inventory = super.findWithQuery(query);
                return;
            }
        }

        query = InventoryQueryBuilder.getFindAllQuery();
        inventory = super.findWithQuery(query);

        if ((inventory != null) && (!inventory.isEmpty())) {
            productInventory = inventory.get(0);
        }
    }

    public void prepareViewInventory() {
        if (productInventory != null) {
            if (inventoryExist(productInventory.getId())) {
                currentForm = VIEW_URL;
            }
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
            productInventory = super.findItemById(id, Inventory.class);
            if (productInventory == null) {

                JsfUtil.addWarningMessage("ItemDoesNotExist");
                currentForm = VIEW_URL;

                if ((inventory != null) && (inventory.size() > 1)) {
                    inventory.remove(productInventory);
                    productInventory = inventory.get(0);
                } else {
                    query = InventoryQueryBuilder.getFindAllQuery();
                    inventory = super.findWithQuery(query);
                    if ((inventory != null) && (!inventory.isEmpty())) {
                        productInventory = inventory.get(0);
                    }
                }

                return false;
            }
            return true;
        }
        return false;
    }

    public List<Inventory> getInventory() {
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
