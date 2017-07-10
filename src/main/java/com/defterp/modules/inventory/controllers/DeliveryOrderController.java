package com.defterp.modules.inventory.controllers;

import com.defterp.translation.annotations.Countries;
import static com.defterp.translation.annotations.Countries.Version.SECOND;
import com.defterp.util.JsfUtil;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.inventory.entities.DeliveryOrder;
import com.defterp.modules.inventory.entities.DeliveryOrderLine;
import com.defterp.modules.inventory.entities.Inventory;
import com.defterp.modules.inventory.entities.Product;
import com.defterp.modules.partners.entities.Partner;
import com.defterp.modules.partners.queryBuilders.PartnerQueryBuilder;
import com.defterp.modules.sales.entities.SaleOrder;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.modules.inventory.queryBuilders.DeliveryOrderQueryBuilder;
import com.defterp.modules.inventory.queryBuilders.ProductQueryBuilder;
import com.defterp.modules.commonClasses.IdGenerator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.apache.commons.lang.SerializationUtils;
import org.primefaces.context.RequestContext;

/**
 *
 * @author MOHAMMED BOUNAGA
 *
 * github.com/medbounaga
 */

@Named(value = "deliveryOrderController")
@ViewScoped
public class DeliveryOrderController extends AbstractController {

    @Inject
    @com.defterp.translation.annotations.Status
    private HashMap<String, String> statuses;
    @Inject
    @Countries(version = SECOND)
    private HashMap<String, String> countries;
    private List<DeliveryOrder> deliveryOrders;
    private List<DeliveryOrder> filteredDeliveryOrders;
    private DeliveryOrder deliveryOrder;
    private List<DeliveryOrderLine> deliveryOrderLines;
    private List<DeliveryOrderLine> tobeDeliveredOrderLines;
    private DeliveryOrderLine deliveryOrderLine;
    private String saleId;
    private String deliveryId;
    private String partnerId;
    private String listType;
    private int rowIndex;
    private List<Partner> topNActiveCustomers;
    private List<Partner> activeCustomers;
    private List<Partner> filteredActiveCustomers;
    private List<Product> topNActiveSoldProducts;
    private List<Product> activeSoldProducts;
    private List<Product> filteredActiveSoldProducts;
    private Partner customer;
    private Product product;
    private QueryWrapper query;

    public DeliveryOrderController() {
        super("/sc/deliveryOrder/");
    }

    public String getCountry() {

        return countries.get(deliveryOrder.getPartner().getCountry());
    }

    public String getStatus(String status) {
        return statuses.get(status);
    }

    public String getStatusColor(String status) {
        switch (status) {
            case "Draft":
                return "#009fd4";
            case "New":
                return "#009fd4";
            case "Waiting Availability":
                return "#00a4a6";
            case "Partially Available":
                return "#008080";
            case "Available":
                return "#406098";
            case "Cancelled":
                return "#6d8891";
            default:
                return "#3477db";
        }
    }

    private enum Status {

        DRAFT("Draft"),
        NEW("New"),
        CANCELLED("Cancelled"),
        WAITING_AVAILABILITY("Waiting Availability"),
        PARTIALLY_AVAILABLE("Partially Available"),
        AVAILABLE("Available"),
        DONE("Done");

        private final String status;

        Status(String status) {
            this.status = status;
        }

        public String value() {
            return status;
        }
    }

    public void checkAvailability() {
        if (deliveryExist(deliveryOrder.getId())) {
            if (deliveryOrder.getState().equals(Status.WAITING_AVAILABILITY.value()) || deliveryOrder.getState().equals(Status.PARTIALLY_AVAILABLE.value())) {
                if (deliveryOrder.getState().equals(Status.WAITING_AVAILABILITY.value())) {
                    resetOrderLinesAvailability();
                }
                refreshInventory(deliveryOrder.getDeliveryOrderLines());
                updateOrderLinesAvailability();
                updateOrderLinesStatus();
                updateOrderStatus();
                updateInventory(deliveryOrder.getDeliveryOrderLines());
                deliveryOrder = super.updateItem(deliveryOrder);
                deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    private void refreshInventory(List<DeliveryOrderLine> orderLines) {

        for (DeliveryOrderLine orderLine : orderLines) {
            Inventory upToDateInventory = super.findItemById(orderLine.getProduct().getInventory().getId(), Inventory.class);
            orderLine.getProduct().setInventory(upToDateInventory);
        }
    }

    private void updateOrderLinesAvailability() {

        for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
            if ((Double.compare(orderLine.getQuantity(), orderLine.getReserved()) > 0) && (orderLine.getProduct().getInventory().getQuantityAvailable() > 0d) && (!orderLine.getState().equals(Status.AVAILABLE.value()))) {
                Double unReservedQuantity = JsfUtil.round((orderLine.getQuantity() - orderLine.getReserved()), orderLine.getProduct().getUom().getDecimals());

                if (unReservedQuantity > orderLine.getProduct().getInventory().getQuantityAvailable()) {
                    orderLine.setReserved(orderLine.getReserved() + orderLine.getProduct().getInventory().getQuantityAvailable());
                    orderLine.getProduct().getInventory().setReservedQuantity(orderLine.getProduct().getInventory().getReservedQuantity() + orderLine.getProduct().getInventory().getQuantityAvailable());
                } else {
                    orderLine.setReserved(orderLine.getReserved() + unReservedQuantity);
                    orderLine.getProduct().getInventory().setReservedQuantity(orderLine.getProduct().getInventory().getReservedQuantity() + unReservedQuantity);
                }
            } else if ((Double.compare(orderLine.getQuantity(), orderLine.getReserved()) > 0) && (orderLine.getReserved() <= 0d) && (orderLine.getProduct().getInventory().getQuantityAvailable() <= 0d) && (!orderLine.getState().equals(Status.AVAILABLE.value()))) {
                orderLine.setReserved(orderLine.getProduct().getInventory().getQuantityAvailable());
            }
        }
    }

    private void updateOrderLinesStatus() {

        for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
            if (Double.compare(orderLine.getQuantity(), orderLine.getReserved()) == 0) {
                System.out.println("Line Status: AVAILABLE");
                orderLine.setState(Status.AVAILABLE.value());
            } else if ((Double.compare(orderLine.getQuantity(), orderLine.getReserved()) > 0) && orderLine.getReserved() > 0d) {
                System.out.println("Line Status: PARTIALLY_AVAILABLE");
                orderLine.setState(Status.PARTIALLY_AVAILABLE.value());
            } else if ((Double.compare(orderLine.getQuantity(), orderLine.getReserved()) > 0) && orderLine.getReserved() <= 0d) {
                System.out.println("Line Status: WAITING_AVAILABILITY");
                orderLine.setState(Status.WAITING_AVAILABILITY.value());
            }
        }
    }

    private void updateOrderStatus() {

        int availableProducts = 0;
        int partiallyAvailableProducts = 0;

        for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
            System.out.println("Quantity: " + orderLine.getQuantity());
            System.out.println("Reserved: " + orderLine.getReserved());
            if ((orderLine.getState().equals(Status.AVAILABLE.value())) && (Double.compare(orderLine.getQuantity(), orderLine.getReserved()) == 0)) {
                availableProducts++;
            } else if (orderLine.getState().equals(Status.PARTIALLY_AVAILABLE.value()) && (Double.compare(orderLine.getQuantity(), orderLine.getReserved()) > 0) && (orderLine.getReserved() > 0d)) {
                partiallyAvailableProducts++;
            }
        }

        System.out.println("Size: " + deliveryOrder.getDeliveryOrderLines().size());
        System.out.println("Available Lines: " + availableProducts);

        if (deliveryOrder.getDeliveryOrderLines().size() == availableProducts) {
            System.out.println("Staus: AVAILABLE");
            deliveryOrder.setState(Status.AVAILABLE.value());
        } else if ((availableProducts + partiallyAvailableProducts) > 0) {
            System.out.println("Staus: PARTIALLY_AVAILABLE");
            deliveryOrder.setState(Status.PARTIALLY_AVAILABLE.value());
        } else if ((availableProducts + partiallyAvailableProducts) == 0) {
            System.out.println("Staus: WAITING_AVAILABILITY");
            deliveryOrder.setState(Status.WAITING_AVAILABILITY.value());
        }
    }

    private void updateInventory(List<DeliveryOrderLine> orderLines) {

        for (DeliveryOrderLine orderLine : orderLines) {
            super.updateItem(orderLine.getProduct().getInventory());
        }
    }

    private void resetOrderLinesAvailability() {

        for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
            orderLine.setReserved(0d);
            orderLine.setState(Status.WAITING_AVAILABILITY.value());
        }
    }

    private void unreserveOrderLines() {
        for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
            if (orderLine.getReserved() > 0d) {
                Double ReservedQuantity = orderLine.getReserved();
                orderLine.getProduct().getInventory().setReservedQuantity(JsfUtil.round((orderLine.getProduct().getInventory().getReservedQuantity() - ReservedQuantity), orderLine.getProduct().getUom().getDecimals()));
            }
        }
    }

    public void confirmDelivery() {
        if (deliveryExist(deliveryOrder.getId())) {
            if (deliveryOrder.getState().equals(Status.DRAFT.value())) {
                deliveryOrder.setState(Status.WAITING_AVAILABILITY.value());
                for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
                    orderLine.setState(Status.WAITING_AVAILABILITY.value());
                }
                refreshInventory(deliveryOrder.getDeliveryOrderLines());
                updateOrderLinesAvailability();
                deliveryOrder = super.updateItem(deliveryOrder);
                deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorValidate");
            }
        }
    }

    public void deliver() {
        if (deliveryExist(deliveryOrder.getId())) {
            if (deliveryOrder.getState().equals(Status.PARTIALLY_AVAILABLE.value()) || deliveryOrder.getState().equals(Status.AVAILABLE.value())) {
                refreshAvailableProducts();
                if (tobeDeliveredOrderLines != null && !tobeDeliveredOrderLines.isEmpty()) {
                    refreshInventory(tobeDeliveredOrderLines);
                    prepareDelivery();
                    if (deliveryOrder.getDeliveryOrderLines().isEmpty()) {
                        fullDelivery();
                    } else {
                        partialDelivery();
                    }
                }
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    private void refreshAvailableProducts() {
        if (tobeDeliveredOrderLines != null && !tobeDeliveredOrderLines.isEmpty()) {
            List<DeliveryOrderLine> selectedLines = new ArrayList<>();
            selectedLines.addAll(tobeDeliveredOrderLines);
            prepareAvailableProducts1();

            ListIterator<DeliveryOrderLine> iterator = tobeDeliveredOrderLines.listIterator();

            while (iterator.hasNext()) {
                DeliveryOrderLine deliveryLine = iterator.next();
                boolean exist = false;
                for (DeliveryOrderLine line : selectedLines) {
                    if (deliveryLine.getProduct().getId() == line.getProduct().getId()) {
                        deliveryLine.setQuantity(line.getQuantity());
                        exist = true;
                    }
                }

                if (exist == false) {
                    iterator.remove();
                }
            }
        }
    }

    private void prepareDelivery() {

        List<DeliveryOrderLine> extraLines = new ArrayList<>();

        for (DeliveryOrderLine toBeDeliveredLine : tobeDeliveredOrderLines) {

            Double quantity = toBeDeliveredLine.getQuantity();

            ListIterator<DeliveryOrderLine> iterator = deliveryOrder.getDeliveryOrderLines().listIterator();
            while (iterator.hasNext()) {
                DeliveryOrderLine deliveryLine = iterator.next();
                if (toBeDeliveredLine.getProduct().getId() == deliveryLine.getProduct().getId()) {
                    if (Double.compare(quantity, deliveryLine.getQuantity()) == 0) {
                        iterator.remove();
                        quantity = 0d;
                        break;
                    } else if (Double.compare(deliveryLine.getQuantity(), quantity) > 0) {
                        if (Double.compare(deliveryLine.getReserved(), quantity) > 0) {
                            deliveryLine.setReserved(JsfUtil.round((deliveryLine.getReserved() - quantity), deliveryLine.getProduct().getUom().getDecimals()));

                        } else {
                            deliveryLine.setReserved(0d);
                        }
                        deliveryLine.setQuantity(JsfUtil.round((deliveryLine.getQuantity() - quantity), deliveryLine.getProduct().getUom().getDecimals()));
                        quantity = 0d;
                        break;
                    } else if (Double.compare(quantity, deliveryLine.getQuantity()) > 0) {
                        iterator.remove();
                        quantity = JsfUtil.round((quantity - deliveryLine.getQuantity()), deliveryLine.getProduct().getUom().getDecimals());
                    }
                }
            }

            if (quantity > 0d) {

                toBeDeliveredLine.setQuantity(JsfUtil.round((toBeDeliveredLine.getQuantity() - quantity), toBeDeliveredLine.getProduct().getUom().getDecimals()));
                DeliveryOrderLine line = new DeliveryOrderLine(
                        toBeDeliveredLine.getProduct(),
                        quantity,
                        0d,
                        toBeDeliveredLine.getUom(),
                        "Extra",
                        "Sale",
                        Boolean.TRUE,
                        deliveryOrder.getPartner(),
                        toBeDeliveredLine.getPrice(),
                        deliveryOrder);
                extraLines.add(line);
            }
        }

        if (!extraLines.isEmpty()) {
            tobeDeliveredOrderLines.addAll(extraLines);
        }
    }

    private void fullDelivery() {

        for (DeliveryOrderLine deliveryLine : tobeDeliveredOrderLines) {

            if (deliveryLine.getReserved() > 0d) {
                if (Double.compare(deliveryLine.getQuantity(), deliveryLine.getReserved()) >= 0) {
                    deliveryLine.getProduct().getInventory().setReservedQuantity(JsfUtil.round((deliveryLine.getProduct().getInventory().getReservedQuantity() - deliveryLine.getReserved()), deliveryLine.getProduct().getUom().getDecimals()));
                    System.out.println("Reserved:" + deliveryLine.getReserved());
                } else {
                    deliveryLine.getProduct().getInventory().setReservedQuantity(JsfUtil.round((deliveryLine.getProduct().getInventory().getReservedQuantity() - deliveryLine.getQuantity()), deliveryLine.getProduct().getUom().getDecimals()));
                    System.out.println("Quantity:" + deliveryLine.getQuantity());
                }
            }

            deliveryLine.getProduct().getInventory().setQuantityOnHand(JsfUtil.round((deliveryLine.getProduct().getInventory().getQuantityOnHand() - deliveryLine.getQuantity()), deliveryLine.getProduct().getUom().getDecimals()));
            deliveryLine.getProduct().getInventory().setTotalCost(JsfUtil.round(deliveryLine.getProduct().getInventory().getUnitCost() * deliveryLine.getProduct().getInventory().getQuantityOnHand()));

            deliveryLine.setDeliveryOrder(deliveryOrder);
            deliveryLine.setReserved(0d);
            deliveryLine.setState(Status.DONE.value());
        }

        deliveryOrder.setDeliveryOrderLines(tobeDeliveredOrderLines);
        deliveryOrder.setState(Status.DONE.value());

        if (deliveryOrder.getSaleOrder() != null) {
            if (deliveryOrder.getSaleOrder().getPaid()) {
                deliveryOrder.getSaleOrder().setState(Status.DONE.value());
            }
            deliveryOrder.getSaleOrder().setShipped(Boolean.TRUE);
            super.updateItem(deliveryOrder.getSaleOrder());
        }

        updateInventory(deliveryOrder.getDeliveryOrderLines());

        deliveryOrder = super.updateItem(deliveryOrder);
        deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);

        tobeDeliveredOrderLines = null;
        deliveryOrderLines = null;

    }

    private void partialDelivery() {

        DeliveryOrder backOrder = new DeliveryOrder(new Date(), deliveryOrder.getOrigin(), Status.DONE.value(), "Sale", Boolean.TRUE, "Partial", deliveryOrder, deliveryOrder.getPartner(), deliveryOrder.getSaleOrder());

        for (DeliveryOrderLine deliveryLine : tobeDeliveredOrderLines) {

            if (deliveryLine.getReserved() > 0d) {
                if (Double.compare(deliveryLine.getQuantity(), deliveryLine.getReserved()) >= 0) {
                    deliveryLine.getProduct().getInventory().setReservedQuantity(JsfUtil.round((deliveryLine.getProduct().getInventory().getReservedQuantity() - deliveryLine.getReserved()), deliveryLine.getProduct().getUom().getDecimals()));
                    System.out.println("Reserved:" + deliveryLine.getReserved());
                } else {
                    deliveryLine.getProduct().getInventory().setReservedQuantity(JsfUtil.round((deliveryLine.getProduct().getInventory().getReservedQuantity() - deliveryLine.getQuantity()), deliveryLine.getProduct().getUom().getDecimals()));
                    System.out.println("Quantity:" + deliveryLine.getQuantity());
                }
            }

            deliveryLine.getProduct().getInventory().setQuantityOnHand(JsfUtil.round((deliveryLine.getProduct().getInventory().getQuantityOnHand() - deliveryLine.getQuantity()), deliveryLine.getProduct().getUom().getDecimals()));
            deliveryLine.getProduct().getInventory().setTotalCost(JsfUtil.round(deliveryLine.getProduct().getInventory().getUnitCost() * deliveryLine.getProduct().getInventory().getQuantityOnHand()));

            deliveryLine.setDeliveryOrder(backOrder);
            deliveryLine.setReserved(0d);
            deliveryLine.setState(Status.DONE.value());
        }

        backOrder.setDeliveryOrderLines(tobeDeliveredOrderLines);
        if (backOrder.getSaleOrder() != null) {
            backOrder.getSaleOrder().getDeliveryOrders().add(backOrder);
        }

        updateOrderLinesStatus();
        updateOrderStatus();
        updateInventory(backOrder.getDeliveryOrderLines());

        backOrder = super.createItem(backOrder);
        backOrder.setName(IdGenerator.generateDeliveryOutId(backOrder.getId()));
        backOrder = super.updateItem(backOrder);

        if (backOrder.getSaleOrder() != null) {
            super.updateItem(backOrder.getSaleOrder());
        }

        deliveryOrder.setDeliveryMethod("Partial");
        deliveryOrder = super.updateItem(deliveryOrder);
        deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);
        deliveryOrders.add(backOrder);

        deliveryOrder = backOrder;
        tobeDeliveredOrderLines = null;
        deliveryOrderLines = null;
    }

    public void prepareAvailableProducts(Integer id) {

        if (deliveryExist(id)) {
            if (deliveryOrder.getState().equals(Status.PARTIALLY_AVAILABLE.value()) || deliveryOrder.getState().equals(Status.AVAILABLE.value())) {
                tobeDeliveredOrderLines = new ArrayList<>();

                for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {

                    if (orderLine.getState().equals(Status.AVAILABLE.value()) && Double.compare(orderLine.getReserved(), orderLine.getQuantity()) == 0) {

                        DeliveryOrderLine line = new DeliveryOrderLine(
                                orderLine.getProduct(),
                                orderLine.getQuantity(),
                                0d,
                                orderLine.getUom(),
                                Status.AVAILABLE.value(),
                                "Sale",
                                Boolean.TRUE,
                                deliveryOrder.getPartner(),
                                orderLine.getPrice(),
                                deliveryOrder);

                        tobeDeliveredOrderLines.add(line);

                    } else if (orderLine.getState().equals(Status.PARTIALLY_AVAILABLE.value()) && (orderLine.getReserved() > 0d)) {

                        DeliveryOrderLine line = new DeliveryOrderLine(
                                orderLine.getProduct(),
                                orderLine.getReserved(),
                                0d,
                                orderLine.getUom(),
                                Status.AVAILABLE.value(),
                                "Sale",
                                Boolean.TRUE,
                                deliveryOrder.getPartner(),
                                orderLine.getPrice(),
                                deliveryOrder);

                        tobeDeliveredOrderLines.add(line);
                    }
                }

                System.out.println("-------------- start size: " + tobeDeliveredOrderLines.size());
                System.out.println("---------------------------------------------------------");

                for (int i = 0; i < tobeDeliveredOrderLines.size(); i++) {
                    System.out.println("--------------iiii: " + i);
                    System.out.println("---------------------------------------------------------");
                    for (int j = 0; j < tobeDeliveredOrderLines.size(); j++) {
                        System.out.println("--------------jjjj: " + j);
                        if (tobeDeliveredOrderLines.get(i).getProduct().getId() == tobeDeliveredOrderLines.get(j).getProduct().getId() && tobeDeliveredOrderLines.get(i) != tobeDeliveredOrderLines.get(j)) {

                            tobeDeliveredOrderLines.get(i).setQuantity(tobeDeliveredOrderLines.get(i).getQuantity() + tobeDeliveredOrderLines.get(j).getQuantity());
                            tobeDeliveredOrderLines.remove(j);
                        }
                    }
                    tobeDeliveredOrderLines.get(i).setReserved(tobeDeliveredOrderLines.get(i).getQuantity());
                }
                System.out.println("---------------------------------------------------------");
                System.out.println("-------------- finish size: " + tobeDeliveredOrderLines.size());
            } else {
                FacesContext.getCurrentInstance().validationFailed();
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    private void prepareAvailableProducts1() {

        tobeDeliveredOrderLines = new ArrayList<>();

        for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {

            if (orderLine.getState().equals(Status.AVAILABLE.value()) && (Double.compare(orderLine.getReserved(), orderLine.getQuantity()) == 0)) {

                DeliveryOrderLine line = new DeliveryOrderLine(
                        orderLine.getProduct(),
                        orderLine.getQuantity(),
                        0d,
                        orderLine.getUom(),
                        Status.AVAILABLE.value(),
                        "Sale",
                        Boolean.TRUE,
                        deliveryOrder.getPartner(),
                        orderLine.getPrice(),
                        deliveryOrder);

                tobeDeliveredOrderLines.add(line);

            } else if (orderLine.getState().equals(Status.PARTIALLY_AVAILABLE.value()) && (orderLine.getReserved() > 0d)) {

                DeliveryOrderLine line = new DeliveryOrderLine(
                        orderLine.getProduct(),
                        orderLine.getReserved(),
                        0d,
                        orderLine.getUom(),
                        Status.AVAILABLE.value(),
                        "Sale",
                        Boolean.TRUE,
                        deliveryOrder.getPartner(),
                        orderLine.getPrice(),
                        deliveryOrder);

                tobeDeliveredOrderLines.add(line);
            }
        }

        for (int i = 0; i < tobeDeliveredOrderLines.size(); i++) {
            for (int j = 0; j < tobeDeliveredOrderLines.size(); j++) {
                if (tobeDeliveredOrderLines.get(i).getProduct().getId() == tobeDeliveredOrderLines.get(j).getProduct().getId() && tobeDeliveredOrderLines.get(i) != tobeDeliveredOrderLines.get(j)) {

                    tobeDeliveredOrderLines.get(i).setQuantity(tobeDeliveredOrderLines.get(i).getQuantity() + tobeDeliveredOrderLines.get(j).getQuantity());
                    tobeDeliveredOrderLines.remove(j);
                }
            }
            tobeDeliveredOrderLines.get(i).setReserved(tobeDeliveredOrderLines.get(i).getQuantity());
        }
    }

    public void removeOrderLine(int rowIndex) {
        if (tobeDeliveredOrderLines != null) {
            if ((rowIndex >= 0) && (rowIndex < tobeDeliveredOrderLines.size())) {
                tobeDeliveredOrderLines.remove(rowIndex);
            }
        }
    }

    public void cancelDelivery() {

        if (deliveryExist(deliveryOrder.getId())) {
            if ((!deliveryOrder.getState().equals(Status.CANCELLED.value())) && (!deliveryOrder.getState().equals(Status.DONE.value()))) {

                if (deliveryOrder.getState().equals(Status.AVAILABLE.value()) || deliveryOrder.getState().equals(Status.PARTIALLY_AVAILABLE.value())) {
                    refreshInventory(deliveryOrder.getDeliveryOrderLines());
                    unreserveOrderLines();
                    updateInventory(deliveryOrder.getDeliveryOrderLines());
                    resetOrderLinesAvailability();
                } else {
                    resetOrderLinesAvailability();
                }

                deliveryOrder.setState(Status.CANCELLED.value());
                for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
                    orderLine.setState(Status.CANCELLED.value());
                }
                deliveryOrder = super.updateItem(deliveryOrder);
                deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);

            } else if (deliveryOrder.getState().equals(Status.DONE.value())) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorCancelDelivery");

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyCancelled");
            }
        }
    }

    public void unreserveDelivery() {

        if (deliveryExist(deliveryOrder.getId())) {
            if (deliveryOrder.getState().equals(Status.PARTIALLY_AVAILABLE.value()) || deliveryOrder.getState().equals(Status.AVAILABLE.value())) {
                deliveryOrder.setState(Status.WAITING_AVAILABILITY.value());
                for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
                    orderLine.setState(Status.WAITING_AVAILABILITY.value());
                }
                refreshInventory(deliveryOrder.getDeliveryOrderLines());
                unreserveOrderLines();
                updateInventory(deliveryOrder.getDeliveryOrderLines());
                resetOrderLinesAvailability();
                updateOrderLinesAvailability();
                deliveryOrder = super.updateItem(deliveryOrder);
                deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    public void deleteDelivery() {
        if (deliveryExist(deliveryOrder.getId())) {
            if (deliveryOrder.getState().equals("Cancelled")) {

                cancelRelations();
                boolean deleted = super.deleteItem(deliveryOrder);

                if (deleted) {

                    JsfUtil.addSuccessMessage("ItemDeleted");
                    currentForm = VIEW_URL;

                    if (deliveryOrders != null && deliveryOrders.size() > 1) {
                        deliveryOrders.remove(deliveryOrder);
                        deliveryOrder = deliveryOrders.get(0);
                    } else {
                        listType = null;
                        query = DeliveryOrderQueryBuilder.getFindDeliveryOrdersQuery();
                        deliveryOrders = super.findWithQuery(query);

                        if (deliveryOrders != null && !deliveryOrders.isEmpty()) {
                            deliveryOrder = deliveryOrders.get(0);
                        }
                    }

                } else {
                    JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                }

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
            }
        }
    }

    private void cancelRelations() {

        query = DeliveryOrderQueryBuilder.getFindByBackOrderQuery(deliveryOrder.getId());
        deliveryOrder.setChildren((List<DeliveryOrder>) (DeliveryOrder) super.findWithQuery(query));

        if (deliveryOrder.getChildren() != null && !deliveryOrder.getChildren().isEmpty()) {
            for (DeliveryOrder delivery : deliveryOrder.getChildren()) {
                delivery.setBackOrder(null);
                delivery = super.updateItem(delivery);
                deliveryOrders.set(deliveryOrders.indexOf(delivery), delivery);
            }

            deliveryOrder.getChildren().clear();
        }

        if (deliveryOrder.getSaleOrder() != null) {
            SaleOrder saleOrder = super.findItemById(deliveryOrder.getSaleOrder().getId(), SaleOrder.class);
            saleOrder.getDeliveryOrders().size();
            saleOrder.getDeliveryOrders().remove(deliveryOrder);
            deliveryOrder.setSaleOrder(null);
            super.updateItem(saleOrder);
        }
    }

    public void resolveRequestParams() {

        currentForm = VIEW_URL;

        if (JsfUtil.isNumeric(deliveryId)) {
            Integer id = Integer.valueOf(deliveryId);
            deliveryOrder = super.findItemById(id, DeliveryOrder.class);
            if (deliveryOrder != null) {
                query = DeliveryOrderQueryBuilder.getFindDeliveryOrdersQuery();
                deliveryOrders = super.findWithQuery(query);
                return;
            }
        }

        if (JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            query = DeliveryOrderQueryBuilder.getFindByCustomerQuery(id);
            deliveryOrders = super.findWithQuery(query);
            if (deliveryOrders != null && !deliveryOrders.isEmpty()) {
                deliveryOrder = deliveryOrders.get(0);
                listType = "partner";
                return;
            }
        }

        if (saleId != null && JsfUtil.isNumeric(saleId)) {
            Integer id = Integer.valueOf(saleId);
            query = DeliveryOrderQueryBuilder.getFindBySaleOrderQuery(id);
            deliveryOrders = super.findWithQuery(query);
            if (deliveryOrders != null && !deliveryOrders.isEmpty()) {
                deliveryOrder = deliveryOrders.get(0);
                listType = "saleOrder";
                return;
            }
        }

        query = DeliveryOrderQueryBuilder.getFindDeliveryOrdersQuery();
        deliveryOrders = super.findWithQuery(query);

        if (deliveryOrders != null && !deliveryOrders.isEmpty()) {
            deliveryOrder = deliveryOrders.get(0);
        }

    }

    public void prepareView() {

        if (deliveryOrder != null) {
            if (deliveryExist(deliveryOrder.getId())) {
                currentForm = VIEW_URL;
            }
        }
    }

    public void showBackOrder(Integer id) {
        if (deliveryExist(id)) {
            listType = null;
            query = DeliveryOrderQueryBuilder.getFindDeliveryOrdersQuery();
            deliveryOrders = super.findWithQuery(query);
            currentForm = VIEW_URL;

        }
    }

    public void viewPartialDelivries() {
        if (deliveryExist(deliveryOrder.getId())) {
            query = DeliveryOrderQueryBuilder.getFindByBackOrderQuery(deliveryOrder.getId());
            deliveryOrders = super.findWithQuery(query);
            deliveryOrder = deliveryOrders.get(0);
            currentForm = VIEW_URL;
            listType = "partialDelivery";
        }
    }

    public Long countPartialDelivries() {
        if (deliveryOrder != null) {
            query = DeliveryOrderQueryBuilder.getCountByBackOrderQuery(deliveryOrder.getId());
            return (Long) super.findSingleWithQuery(query);
        }
        return 0L;
    }

    public int getDeliveryIndex() {
        if (deliveryOrders != null && deliveryOrder != null) {
            return deliveryOrders.indexOf(deliveryOrder) + 1;
        }
        return 0;
    }

    public void nextDelivery() {
        if (deliveryOrders != null && deliveryOrder != null) {
            if (deliveryOrders.indexOf(deliveryOrder) == (deliveryOrders.size() - 1)) {
                deliveryOrder = deliveryOrders.get(0);
            } else {
                deliveryOrder = deliveryOrders.get(deliveryOrders.indexOf(deliveryOrder) + 1);
            }
        }
    }

    public void previousDelivery() {
        if (deliveryOrders != null && deliveryOrder != null) {
            if (deliveryOrders.indexOf(deliveryOrder) == 0) {
                deliveryOrder = deliveryOrders.get(deliveryOrders.size() - 1);
            } else {
                deliveryOrder = deliveryOrders.get(deliveryOrders.indexOf(deliveryOrder) - 1);
            }
        }
    }

    public void printDelivery(ActionEvent actionEvent) throws IOException, JRException {

        for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {

            orderLine.setProductName(orderLine.getProduct().getName());
        }

        deliveryOrder.getPartner().setCountry(getCountry());

        ResourceBundle bundle = JsfUtil.getBundle();
        String name = bundle.getString("DeliveryOrder");

        Map<String, Object> params = new HashMap<>();
        params.put("deliveryOrder", deliveryOrder);
        params.put("partner", deliveryOrder.getPartner());
        params.put("orderLines", deliveryOrder.getDeliveryOrderLines());
        params.put("SUBREPORT_DIR", FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/") + "/");

        String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/deliveryOrder.jasper");
        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, params, new JREmptyDataSource());
//        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, new HashMap<String,Object>(), new JRBeanArrayDataSource(new SaleOrder[]{saleOrder}));  
        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        httpServletResponse.addHeader("Content-disposition", "attachment; filename=" + name + "_" + deliveryOrder.getName() + ".pdf");
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
        FacesContext.getCurrentInstance().responseComplete();

    }

    public void setRowIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        rowIndex = Integer.valueOf(params.get("rowIndex"));
    }

    public void onSelectCustomer() {
        if (customer != null) {
            if (topNActiveCustomers != null && !topNActiveCustomers.contains(customer)) {
                topNActiveCustomers.add(customer);
            }
            deliveryOrder.setPartner(customer);
        }
    }

    public void onSelectProduct() {

        if ((product != null)) {
            if (!topNActiveSoldProducts.contains(product)) {
                topNActiveSoldProducts.add(product);
            }

            if (rowIndex < 0) {

                deliveryOrderLine.setProduct(product);
                deliveryOrderLine.setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("mainForm:productMenuTwo");
                RequestContext.getCurrentInstance().update("mainForm:uom");

            } else {

                deliveryOrderLines.get(rowIndex).setProduct(product);
                deliveryOrderLines.get(rowIndex).setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("mainForm:datalist:" + rowIndex + ":productMenu");
                RequestContext.getCurrentInstance().update("mainForm:datalist:" + rowIndex + ":uomm");
            }
        }
    }

    public void updateOrder() {

        String deliveryOrderStatus = getDeliveryOrderStatus();

        if (deliveryOrderStatus != null) {
            if (!deliveryOrderStatus.equals(Status.DRAFT.value())) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
                currentForm = VIEW_URL;
            } else if (deliveryOrderLines.isEmpty()) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneDeliveryOrderLineUpdate");

            } else {

                for (DeliveryOrderLine orderLine : deliveryOrderLines) {
                    orderLine.setDeliveryOrder(deliveryOrder);
                    orderLine.setPartner(deliveryOrder.getPartner());
                    orderLine.setUom(orderLine.getProduct().getUom().getName());
                    orderLine.setPrice(orderLine.getProduct().getSalePrice());
                    orderLine.setReserved(0d);
                    orderLine.setState(Status.NEW.value());
                    orderLine.setType("Sale");
                    orderLine.setActive(Boolean.TRUE);
                }

                deliveryOrder.setDeliveryOrderLines(deliveryOrderLines);
                deliveryOrder = super.updateItem(deliveryOrder);

                if (listType == null && deliveryOrders != null) {
                    deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);
                } else {
                    query = DeliveryOrderQueryBuilder.getFindDeliveryOrdersQuery();
                    deliveryOrders = super.findWithQuery(query);
                    listType = null;
                }

                currentForm = VIEW_URL;
            }
        }
    }

    public void createOrder() {
        if (deliveryOrderLines.isEmpty()) {
            JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneDeliveryOrderLineCreate");
        } else {

            for (DeliveryOrderLine OrderLine : deliveryOrderLines) {
                OrderLine.setDeliveryOrder(deliveryOrder);
                OrderLine.setPartner(deliveryOrder.getPartner());
                OrderLine.setReserved(0d);
                OrderLine.setState(Status.NEW.value());
                OrderLine.setType("Sale");
                OrderLine.setActive(Boolean.TRUE);
                OrderLine.setUom(OrderLine.getProduct().getUom().getName());
                OrderLine.setPrice(OrderLine.getProduct().getSalePrice());
            }

            deliveryOrder.setState(Status.DRAFT.value());
            deliveryOrder.setType("Sale");
            deliveryOrder.setActive(Boolean.TRUE);
            deliveryOrder.setDeliveryMethod("Complete");
            deliveryOrder.setDeliveryOrderLines(deliveryOrderLines);

            deliveryOrder = super.createItem(deliveryOrder);
            deliveryOrder.setName(IdGenerator.generateDeliveryOutId(deliveryOrder.getId()));
            deliveryOrder = super.updateItem(deliveryOrder);

            if (listType == null && deliveryOrders != null) {
                deliveryOrders.add(deliveryOrder);
            } else {
                query = DeliveryOrderQueryBuilder.getFindDeliveryOrdersQuery();
                deliveryOrders = super.findWithQuery(query);
                listType = null;
            }
            currentForm = VIEW_URL;
        }
    }

    public void prepareCreate() {
        System.out.println("prepareCreate(): start");
        deliveryOrder = new DeliveryOrder();
        deliveryOrder.setDeliveryMethod("Complete");
        deliveryOrder.setActive(Boolean.TRUE);
        deliveryOrderLines = new ArrayList<>();
        deliveryOrderLine = new DeliveryOrderLine();

        loadActiveCustomers();
        loadActiveSoldProducts();

        deliveryOrderLine.setState(Status.NEW.value());

        if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topNActiveSoldProducts.get(0));
            deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
        }

        currentForm = CREATE_URL;
        System.out.println("prepareCreate(): finish");
    }

    public void prepareEdit() {
        if (deliveryExist(deliveryOrder.getId())) {
            if (deliveryOrder.getState().equals(Status.DRAFT.value())) {
                deliveryOrderLine = new DeliveryOrderLine();
                deliveryOrderLines = deliveryOrder.getDeliveryOrderLines();

                loadActiveCustomers();
                loadActiveSoldProducts();

                if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
                    deliveryOrderLine.setProduct(topNActiveSoldProducts.get(0));
                    deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
                }
                deliveryOrderLine.setState(Status.NEW.value());

                if (!topNActiveCustomers.contains(deliveryOrder.getPartner())) {
                    topNActiveCustomers.add(deliveryOrder.getPartner());
                }

                for (DeliveryOrderLine orderLine : deliveryOrderLines) {
                    if (!topNActiveSoldProducts.contains(orderLine.getProduct())) {
                        topNActiveSoldProducts.add(orderLine.getProduct());
                    }
                }

                currentForm = EDIT_URL;
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEdit");
            }
        }
    }

    public void cancelCreate() {

        deliveryOrderLine = null;
        deliveryOrderLines = null;
        topNActiveCustomers = null;
        topNActiveSoldProducts = null;

        if (deliveryOrders != null && !deliveryOrders.isEmpty()) {
            deliveryOrder = deliveryOrders.get(0);
            currentForm = VIEW_URL;
        }
    }

    public void cancelEdit() {

        deliveryOrderLine = null;
        deliveryOrderLines = null;
        topNActiveCustomers = null;
        topNActiveSoldProducts = null;

        if (deliveryExist(deliveryOrder.getId())) {
            currentForm = VIEW_URL;
        }
    }

    public void duplicateDeliveryOrder() {
        if (deliveryExist(deliveryOrder.getId())) {

            deliveryOrder.getDeliveryOrderLines().size();

            DeliveryOrder newDeliveryOrder = (DeliveryOrder) SerializationUtils.clone(deliveryOrder);

            newDeliveryOrder.setId(null);
            newDeliveryOrder.setActive(null);
            newDeliveryOrder.setBackOrder(null);
            newDeliveryOrder.setChildren(null);
            newDeliveryOrder.setSaleOrder(null);
            newDeliveryOrder.setDate(new Date());
            newDeliveryOrder.setName(null);
            newDeliveryOrder.setState(Status.DRAFT.value());
            newDeliveryOrder.setType("Sale");
            newDeliveryOrder.setDeliveryMethod("Complete");
            newDeliveryOrder.setActive(Boolean.TRUE);
            newDeliveryOrder.setOrigin(null);

            for (DeliveryOrderLine orderLine : newDeliveryOrder.getDeliveryOrderLines()) {
                orderLine.setId(null);
                orderLine.setDeliveryOrder(newDeliveryOrder);
                orderLine.setPartner(deliveryOrder.getPartner());
                orderLine.setReserved(0d);
                orderLine.setState(Status.NEW.value());
                orderLine.setType("Sale");
                orderLine.setActive(Boolean.TRUE);
                orderLine.setUom(orderLine.getProduct().getUom().getName());
                orderLine.setPrice(orderLine.getProduct().getSalePrice());
            }

            deliveryOrder = newDeliveryOrder;
            deliveryOrderLine = new DeliveryOrderLine();
            deliveryOrderLines = deliveryOrder.getDeliveryOrderLines();

            loadActiveCustomers();
            loadActiveSoldProducts();

            if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
                deliveryOrderLine.setProduct(topNActiveSoldProducts.get(0));
                deliveryOrderLine.setPrice(deliveryOrderLine.getProduct().getSalePrice());
                deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
            }

            if (!topNActiveCustomers.contains(deliveryOrder.getPartner())) {
                topNActiveCustomers.add(deliveryOrder.getPartner());
            }

            for (DeliveryOrderLine orderLine : deliveryOrderLines) {
                if (!topNActiveSoldProducts.contains(orderLine.getProduct())) {
                    topNActiveSoldProducts.add(orderLine.getProduct());
                }
            }
            currentForm = CREATE_URL;
        }
    }

    public void onDialogRowEdit(int Index) {
        tobeDeliveredOrderLines.get(Index).setQuantity(JsfUtil.round(tobeDeliveredOrderLines.get(Index).getQuantity(), tobeDeliveredOrderLines.get(Index).getProduct().getUom().getDecimals()));

        if (tobeDeliveredOrderLines.get(Index).getQuantity() == 0d) {
            tobeDeliveredOrderLines.get(Index).setQuantity(1d);
        }

    }

    public void onDialogRowCancel() {

    }

    public void onRowAdd() {
        deliveryOrderLine.setQuantity(JsfUtil.round(deliveryOrderLine.getQuantity(), deliveryOrderLine.getProduct().getUom().getDecimals()));

        if (deliveryOrderLine.getQuantity() == 0d) {
            deliveryOrderLine.setQuantity(1d);
        }

        deliveryOrderLines.add(deliveryOrderLine);
        deliveryOrderLine = new DeliveryOrderLine();
        if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topNActiveSoldProducts.get(0));
            deliveryOrderLine.setUom(topNActiveSoldProducts.get(0).getUom().getName());
        }
        deliveryOrderLine.setState(Status.NEW.value());
    }

    public void onRowDelete(int Index) {
        if (Index >= 0 && Index < deliveryOrderLines.size()) {
            deliveryOrderLines.remove(Index);
        }
    }

    public void onRowEditInit(DeliveryOrderLine orderLine) {
        deliveryOrderLine = (DeliveryOrderLine) SerializationUtils.clone(orderLine);
    }

    public void onRowEdit(int index) {
        deliveryOrderLines.get(index).setQuantity(JsfUtil.round(deliveryOrderLines.get(index).getQuantity(), deliveryOrderLines.get(index).getProduct().getUom().getDecimals()));

        if (deliveryOrderLines.get(index).getQuantity() == 0d) {
            deliveryOrderLines.get(index).setQuantity(1d);
        }
    }

    public void onRowCancel(int index) {
        deliveryOrderLines.remove(index);
        deliveryOrderLines.add(index, deliveryOrderLine);
        deliveryOrderLine = new DeliveryOrderLine();
        if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topNActiveSoldProducts.get(0));
            deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
        }
        deliveryOrderLine.setState(Status.NEW.value());
    }

    public void onRowCancel() {
        deliveryOrderLine = new DeliveryOrderLine();
        if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topNActiveSoldProducts.get(0));
            deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
        }
        deliveryOrderLine.setState(Status.NEW.value());
    }

    public void onProductChange() {
        deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
    }

    public void onProductChange(int rowIndex) {
        deliveryOrderLines.get(rowIndex).setUom(deliveryOrderLines.get(rowIndex).getProduct().getUom().getName());
    }

    private boolean deliveryExist(Integer id) {
        if (id != null) {
            deliveryOrder = super.findItemById(id, DeliveryOrder.class);
            if (deliveryOrder == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                deliveryOrders.remove(deliveryOrder);
                deliveryOrder = deliveryOrders.get(0);
//              listType = null;
                currentForm = VIEW_URL;
                return false;
            } else {
                deliveryOrder.getDeliveryOrderLines().size();
                return true;
            }

        } else {
            return false;
        }
    }

    private String getDeliveryOrderStatus() {
        if (deliveryOrder != null) {

            DeliveryOrder tempItem = super.findItemById(deliveryOrder.getId(), deliveryOrder.getClass());

            if (tempItem != null) {
                return tempItem.getState();
            } else {
                deliveryOrderNotFound();
                return null;
            }
        }
        return null;
    }

    private void deliveryOrderNotFound() {

        JsfUtil.addWarningMessage("ItemDoesNotExist");
        currentForm = VIEW_URL;

        if ((deliveryOrders != null) && (deliveryOrders.size() > 1)) {
            deliveryOrders.remove(deliveryOrder);
            deliveryOrder = deliveryOrders.get(0);
        } else {
            listType = null;
            query = DeliveryOrderQueryBuilder.getFindDeliveryOrdersQuery();
            deliveryOrders = super.findWithQuery(query);
            if ((deliveryOrders != null) && (deliveryOrders.size() > 1)) {
                deliveryOrder = deliveryOrders.get(0);
            }
        }
    }

    private void loadActiveCustomers() {
        query = PartnerQueryBuilder.getFindActiveCustomersQuery();
        activeCustomers = super.findWithQuery(query);

        if (activeCustomers != null && activeCustomers.size() > MAX_DROPDOWN_ITEMS) {
            topNActiveCustomers = activeCustomers.subList(0, MAX_DROPDOWN_ITEMS);
        } else {
            topNActiveCustomers = activeCustomers;
        }
    }

    private void loadActiveSoldProducts() {
        query = ProductQueryBuilder.getFindActiveSoldProductsQuery();
        activeSoldProducts = super.findWithQuery(query);

        if (activeSoldProducts != null && activeSoldProducts.size() > MAX_DROPDOWN_ITEMS) {
            topNActiveSoldProducts = activeSoldProducts.subList(0, MAX_DROPDOWN_ITEMS);
        } else {
            topNActiveSoldProducts = activeSoldProducts;
        }
    }

    public Partner getCustomer() {
        return customer;
    }

    public void setCustomer(Partner customer) {
        this.customer = customer;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public DeliveryOrder getDeliveryOrder() {
        if (deliveryOrder == null) {
            return deliveryOrder = new DeliveryOrder();
        }
        return deliveryOrder;
    }

    public void setDeliveryOrder(DeliveryOrder deliveryOrder) {
        this.deliveryOrder = deliveryOrder;
    }

    public DeliveryOrderLine getDeliveryOrderLine() {
        if (deliveryOrderLine == null) {
            deliveryOrderLine = new DeliveryOrderLine();
        }
        return deliveryOrderLine;
    }

    public void setDeliveryOrderLine(DeliveryOrderLine deliveryOrderLine) {
        this.deliveryOrderLine = deliveryOrderLine;
    }

    public List<DeliveryOrder> getDeliveryOrders() {
        return deliveryOrders;
    }

    public void setDeliveryOrders(List<DeliveryOrder> deliveryOrders) {
        this.deliveryOrders = deliveryOrders;
    }

    public List<DeliveryOrder> getFilteredDeliveryOrders() {
        return filteredDeliveryOrders;
    }

    public void setFilteredDeliveryOrders(List<DeliveryOrder> filteredDeliveryOrders) {
        this.filteredDeliveryOrders = filteredDeliveryOrders;
    }

    public List<DeliveryOrderLine> getDeliveryOrderLines() {
        if (deliveryOrderLines == null) {
            deliveryOrderLines = new ArrayList<>();
        }
        return deliveryOrderLines;
    }

    public void setDeliveryOrderLines(List<DeliveryOrderLine> deliveryOrderLines) {
        this.deliveryOrderLines = deliveryOrderLines;
    }

    public List<DeliveryOrderLine> getTobeDeliveredOrderLines() {
        return tobeDeliveredOrderLines;
    }

    public void setTobeDeliveredOrderLines(List<DeliveryOrderLine> tobeDeliveredOrderLines) {
        this.tobeDeliveredOrderLines = tobeDeliveredOrderLines;
    }

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public List<Partner> getActiveCustomers() {
        return activeCustomers;
    }

    public List<Partner> getTopNActiveCustomers() {
        return topNActiveCustomers;
    }

    public List<Partner> getFilteredActiveCustomers() {
        return filteredActiveCustomers;
    }

    public List<Product> getActiveSoldProducts() {
        return activeSoldProducts;
    }

    public List<Product> getTopNActiveSoldProducts() {
        return topNActiveSoldProducts;
    }

    public List<Product> getFilteredActiveSoldProducts() {
        return filteredActiveSoldProducts;
    }

}
