package com.casa.erp.beans;

import com.casa.erp.beans.util.Countries;
import static com.casa.erp.beans.util.Countries.Version.SECOND;
import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.entities.DeliveryOrder;
import com.casa.erp.entities.DeliveryOrderLine;
import com.casa.erp.entities.Inventory;
import com.casa.erp.entities.Partner;
import com.casa.erp.entities.Product;
import com.casa.erp.entities.SaleOrder;
import com.casa.erp.dao.DeliveryOrderFacade;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import javax.ejb.EJBException;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
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
public class DeliveryOrderController implements Serializable {

    @Inject
    private DeliveryOrderFacade deliveryOrderFacade;
    @Inject
    @com.casa.erp.beans.util.Status
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
    private String currentPage = "/sc/deliveryOrder/List.xhtml";
    private String saleId;
    private String deliveryId;
    private String partnerId;
    private String listType;
    private int rowIndex;
    private List<Partner> topNCustomers;
    private List<Product> topSoldNProducts;
    private Partner customer;
    private Product product;

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

    public void checkAvailability(Integer id) {
        if (deliveryExist(id)) {
            if (deliveryOrder.getState().equals(Status.WAITING_AVAILABILITY.value()) || deliveryOrder.getState().equals(Status.PARTIALLY_AVAILABLE.value())) {
                if (deliveryOrder.getState().equals(Status.WAITING_AVAILABILITY.value())) {
                    resetOrderLinesAvailability();
                }
                refreshInventory(deliveryOrder.getDeliveryOrderLines());
                updateOrderLinesAvailability();
                updateOrderLinesStatus();
                updateOrderStatus();
                updateInventory(deliveryOrder.getDeliveryOrderLines());
                deliveryOrder = deliveryOrderFacade.update(deliveryOrder);
                deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    private void refreshInventory(List<DeliveryOrderLine> orderLines) {

        for (DeliveryOrderLine orderLine : orderLines) {
            Inventory upToDateInventory = deliveryOrderFacade.findInventory(orderLine.getProduct().getInventory().getId());
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
            deliveryOrderFacade.update(orderLine.getProduct().getInventory());
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

    public void confirmDelivery(Integer id) {
        if (deliveryExist(id)) {
            if (deliveryOrder.getState().equals(Status.DRAFT.value())) {
                deliveryOrder.setState(Status.WAITING_AVAILABILITY.value());
                for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
                    orderLine.setState(Status.WAITING_AVAILABILITY.value());
                }
                refreshInventory(deliveryOrder.getDeliveryOrderLines());
                updateOrderLinesAvailability();
                deliveryOrder = deliveryOrderFacade.update(deliveryOrder);
                deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorValidate");
            }
        }
    }

    public void deliver(Integer id) {
        if (deliveryExist(id)) {
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
            prepareAvailableProducts();

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
            deliveryOrderFacade.update(deliveryOrder.getSaleOrder());
        }

        updateInventory(deliveryOrder.getDeliveryOrderLines());

        deliveryOrder = deliveryOrderFacade.update(deliveryOrder);
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

        backOrder = deliveryOrderFacade.createOutDelivery(backOrder);
        if (backOrder.getSaleOrder() != null) {
            deliveryOrderFacade.update(backOrder.getSaleOrder());
        }

        deliveryOrder.setDeliveryMethod("Partial");
        deliveryOrder = deliveryOrderFacade.update(deliveryOrder);
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

                for (int i = 0; i < tobeDeliveredOrderLines.size(); i++) {
                    for (int j = 0; j < tobeDeliveredOrderLines.size(); j++) {
                        if (tobeDeliveredOrderLines.get(i).getProduct().getId() == tobeDeliveredOrderLines.get(j).getProduct().getId() && tobeDeliveredOrderLines.get(i) != tobeDeliveredOrderLines.get(j)) {

                            tobeDeliveredOrderLines.get(i).setQuantity(tobeDeliveredOrderLines.get(i).getQuantity() + tobeDeliveredOrderLines.get(j).getQuantity());
                            tobeDeliveredOrderLines.remove(j);
                        }
                    }
                    tobeDeliveredOrderLines.get(i).setReserved(tobeDeliveredOrderLines.get(i).getQuantity());
                }
            } else {
                FacesContext.getCurrentInstance().validationFailed();
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    public void prepareAvailableProducts() {

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

    public void cancelDelivery(Integer id) {

        if (deliveryExist(id)) {
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
                deliveryOrder = deliveryOrderFacade.update(deliveryOrder);
                deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);

            } else if (deliveryOrder.getState().equals(Status.DONE.value())) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorCancelDelivery");

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyCancelled");
            }

        }
    }

    public void unreserveDelivery(Integer id) {

        if (deliveryExist(id)) {
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
                deliveryOrder = deliveryOrderFacade.update(deliveryOrder);
                deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    public void deleteDelivery(Integer id) {
        if (deliveryExist(id)) {
            if (deliveryOrder.getState().equals("Cancelled")) {
                cancelRelations();

                try {
                    deliveryOrderFacade.remove(deliveryOrder);
                } catch (Exception e) {
                    System.out.println("Error Delete: " + e.getMessage());
                    JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                    return;
                }

                if (deliveryOrders.size() > 1) {
                    deliveryOrders.remove(deliveryOrder);
                } else {
                    listType = null;
                    deliveryOrders = deliveryOrderFacade.findOutDelivery();
                }

                deliveryOrder = null;
                JsfUtil.addSuccessMessage("ItemDeleted");
                currentPage = "/sc/deliveryOrder/List.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
            }
        }
    }

    private void cancelRelations() {
        deliveryOrder.setChildren(deliveryOrderFacade.findByBackOrder(deliveryOrder.getId()));

        if (deliveryOrder.getChildren() != null && !deliveryOrder.getChildren().isEmpty()) {
            for (DeliveryOrder delivery : deliveryOrder.getChildren()) {
                delivery.setBackOrder(null);
                delivery = deliveryOrderFacade.update(delivery);
                deliveryOrders.set(deliveryOrders.indexOf(delivery), delivery);
            }

            deliveryOrder.getChildren().clear();
        }

        if (deliveryOrder.getSaleOrder() != null) {
            SaleOrder saleOrder = deliveryOrderFacade.findSaleOrder(deliveryOrder.getSaleOrder().getId());
            saleOrder.getDeliveryOrders().size();
            saleOrder.getDeliveryOrders().remove(deliveryOrder);
            deliveryOrder.setSaleOrder(null);
            deliveryOrderFacade.update(saleOrder);
        }
    }

    public void showDeliveryList() {
        deliveryOrder = null;
        currentPage = "/sc/deliveryOrder/List.xhtml";
    }

    public void viewDeliveryOrder() {

        if (deliveryId != null && JsfUtil.isNumeric(deliveryId)) {
            Integer id = Integer.valueOf(deliveryId);
            deliveryOrder = deliveryOrderFacade.find(id);
            if (deliveryOrder != null) {
                deliveryOrders = deliveryOrderFacade.findOutDelivery();
                currentPage = "/sc/deliveryOrder/View.xhtml";
                return;
            }
        }

        if (partnerId != null && JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            deliveryOrders = deliveryOrderFacade.findByPartner(id, "Sale");
            if (deliveryOrders != null && !deliveryOrders.isEmpty()) {
                currentPage = "/sc/deliveryOrder/List.xhtml";
                listType = "partner";
                return;
            }
        }

        if (saleId != null && JsfUtil.isNumeric(saleId)) {
            Integer id = Integer.valueOf(saleId);
            deliveryOrders = deliveryOrderFacade.findBySaleId(id);
            if ((deliveryOrders != null) && (!deliveryOrders.isEmpty())) {
                currentPage = "/sc/deliveryOrder/List.xhtml";
                listType = "saleOrder";
                return;
            }
        }
        try {
            deliveryOrders = deliveryOrderFacade.findOutDelivery();
        } catch (EJBException ex) {
            Throwable cause = JsfUtil.getRootCause(ex.getCause());
            if (cause != null) {
                if (cause instanceof ConstraintViolationException) {
                    ConstraintViolationException excp = (ConstraintViolationException) cause;
                    for (ConstraintViolation s : excp.getConstraintViolations()) {
                        System.out.println("Errooooor:" + s.getMessage());
                    }
                } else {
                    String msg = cause.getLocalizedMessage();
                    if (msg.length() > 0) {
                        System.out.println("Errooooor:" + msg);
                    } else {
                        System.out.println("Errooooor:" + ResourceBundle.getBundle("/Bundle").getString("PersistenceErrorOccured"));
                    }
                }
            }
        }
        currentPage = "/sc/deliveryOrder/List.xhtml";

    }

    public void prepareView() {

        if (deliveryOrder != null) {
            if (deliveryExist(deliveryOrder.getId())) {
                currentPage = "/sc/deliveryOrder/View.xhtml";
            }
        }
    }

    public void showForm() {

        if (deliveryOrders.size() > 0) {
            deliveryOrder = deliveryOrders.get(0);
            currentPage = "/sc/deliveryOrder/View.xhtml";
        }
    }

    public void showForm(Integer id) {
        if (deliveryExist(id)) {
            listType = null;
            deliveryOrders = deliveryOrderFacade.findOutDelivery();
            currentPage = "/sc/deliveryOrder/View.xhtml";

        }
    }

    public void viewPartialDelivries(Integer id) {
        if (deliveryExist(id)) {
            deliveryOrders = deliveryOrderFacade.findByBackOrder(id);
            currentPage = "/sc/deliveryOrder/List.xhtml";
            listType = "partialDelivery";
        }
    }

    public Long countPartialDelivries() {
        if (deliveryOrder != null) {
            return deliveryOrderFacade.countByBackOrder(deliveryOrder.getId());
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

    private boolean deliveryExist(Integer id) {
        if (id != null) {
            deliveryOrder = deliveryOrderFacade.find(id);
            if (deliveryOrder == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                deliveryOrders = null;
                listType = null;
                currentPage = "/sc/deliveryOrder/List.xhtml";
                return false;
            } else {
                deliveryOrder.getDeliveryOrderLines().size();
                return true;
            }

        } else {
            return false;
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
        params.put("SUBREPORT_DIR", FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/")+"/");

        String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/deliveryOrder.jasper");
        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, params, new JREmptyDataSource());
//        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, new HashMap<String,Object>(), new JRBeanArrayDataSource(new SaleOrder[]{saleOrder}));  
        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        httpServletResponse.addHeader("Content-disposition", "attachment; filename=" + name + "_" + deliveryOrder.getName() + ".pdf");
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
        FacesContext.getCurrentInstance().responseComplete();

    }

    public String getPage() {
        return currentPage;
    }

    public void setPage(String page) {
        this.currentPage = page;
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
        if (deliveryOrders == null) {
            deliveryOrders = deliveryOrderFacade.findOutDelivery();
        }
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

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
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

    public void setRowIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        rowIndex = Integer.valueOf(params.get("rowIndex"));
    }

    public void onSelectCustomer() {
        if ((customer != null) && (!topNCustomers.contains(customer))) {
            topNCustomers.add(customer);
        }
        deliveryOrder.setPartner(customer);
    }

    public void onSelectProduct() {

        if ((product != null)) {
            if (!topSoldNProducts.contains(product)) {
                topSoldNProducts.add(product);
            }

            if (rowIndex < 0) {

                deliveryOrderLine.setProduct(product);
                deliveryOrderLine.setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("DeliveryOrderForm:productMenuTwo");
                RequestContext.getCurrentInstance().update("DeliveryOrderForm:uom");

            } else {

                deliveryOrderLines.get(rowIndex).setProduct(product);
                deliveryOrderLines.get(rowIndex).setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("DeliveryOrderForm:datalist:" + rowIndex + ":productMenu");
                RequestContext.getCurrentInstance().update("DeliveryOrderForm:datalist:" + rowIndex + ":uomm");
            }
        }
    }

    public void updateOrder(Integer id) {
        if (getOrderStatus(id) != null) {
            if (!getOrderStatus(id).equals(Status.DRAFT.value())) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
                currentPage = "/sc/DeliveryOrder/View.xhtml";
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
                deliveryOrder = deliveryOrderFacade.update(deliveryOrder);

                if (listType == null && deliveryOrders != null) {
                    deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);
                } else {
                    deliveryOrders = deliveryOrderFacade.findOutDelivery();
                    listType = null;
                }

                currentPage = "/sc/deliveryOrder/View.xhtml";
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
            deliveryOrder = deliveryOrderFacade.createOutDelivery(deliveryOrder);

            if (listType == null && deliveryOrders != null) {
                deliveryOrders.add(deliveryOrder);
            } else {
                deliveryOrders = deliveryOrderFacade.findOutDelivery();
                listType = null;
            }
            currentPage = "/sc/deliveryOrder/View.xhtml";
        }
    }

    public void prepareCreate() {
        deliveryOrder = new DeliveryOrder();
        deliveryOrderLines = new ArrayList<>();
        deliveryOrderLine = new DeliveryOrderLine();
        topNCustomers = deliveryOrderFacade.findTopNCustomers(4);
        topSoldNProducts = deliveryOrderFacade.findTopNSoldProducts(4);
        if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topSoldNProducts.get(0));
            deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
        }
        deliveryOrderLine.setState(Status.NEW.value());
        currentPage = "/sc/deliveryOrder/Create.xhtml";
    }

    public void prepareEdit(Integer id) {
        if (deliveryExist(id)) {
            if (deliveryOrder.getState().equals(Status.DRAFT.value())) {
                deliveryOrderLine = new DeliveryOrderLine();
                deliveryOrderLines = deliveryOrder.getDeliveryOrderLines();
                topNCustomers = deliveryOrderFacade.findTopNCustomers(4);
                topSoldNProducts = deliveryOrderFacade.findTopNSoldProducts(4);
                if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
                    deliveryOrderLine.setProduct(topSoldNProducts.get(0));
                    deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
                }
                deliveryOrderLine.setState(Status.NEW.value());

                if (!topNCustomers.contains(deliveryOrder.getPartner())) {
                    topNCustomers.add(deliveryOrder.getPartner());
                }

                for (DeliveryOrderLine orderLine : deliveryOrderLines) {
                    if (!topSoldNProducts.contains(orderLine.getProduct())) {
                        topSoldNProducts.add(orderLine.getProduct());
                    }
                }

                currentPage = "/sc/deliveryOrder/Edit.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEdit");
            }
        }
    }

    public void cancelEdit(Integer id) {
        if (deliveryExist(id)) {
            currentPage = "/sc/deliveryOrder/View.xhtml";
        }
    }

    public void duplicateDeliveryOrder(Integer id) {
        if (deliveryExist(id)) {

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
            topNCustomers = deliveryOrderFacade.findTopNCustomers(4);
            topSoldNProducts = deliveryOrderFacade.findTopNSoldProducts(4);
            if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
                deliveryOrderLine.setProduct(topSoldNProducts.get(0));
                deliveryOrderLine.setPrice(deliveryOrderLine.getProduct().getSalePrice());
                deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
            }

            if (!topNCustomers.contains(deliveryOrder.getPartner())) {
                topNCustomers.add(deliveryOrder.getPartner());
            }

            for (DeliveryOrderLine orderLine : deliveryOrderLines) {
                if (!topSoldNProducts.contains(orderLine.getProduct())) {
                    topSoldNProducts.add(orderLine.getProduct());
                }
            }
            currentPage = "/sc/deliveryOrder/Create.xhtml";
        }
    }

    public List<Partner> getTopNCustomers() {
        if (topNCustomers == null) {
            topNCustomers = deliveryOrderFacade.findTopNCustomers(4);
        }
        return topNCustomers;

    }

    public List<Product> getTopSoldNProducts() {
        if (topSoldNProducts == null) {
            topSoldNProducts = deliveryOrderFacade.findTopNSoldProducts(4);
        }
        return topSoldNProducts;
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
        if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topSoldNProducts.get(0));
            deliveryOrderLine.setUom(topSoldNProducts.get(0).getUom().getName());
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
        if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topSoldNProducts.get(0));
            deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
        }
        deliveryOrderLine.setState(Status.NEW.value());
    }

    public void onRowCancel() {
        deliveryOrderLine = new DeliveryOrderLine();
        if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topSoldNProducts.get(0));
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

    private String getOrderStatus(Integer id) {
        if (id != null) {
            DeliveryOrder delivery = deliveryOrderFacade.find(id);
            if (delivery != null) {
                return delivery.getState();
            } else {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                deliveryOrders = null;
                listType = null;
                currentPage = "/sc/deliveryOrder/List.xhtml";
                return null;
            }
        }
        return null;
    }

}
