package com.casa.erp.beans;

import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.entities.DeliveryOrder;
import com.casa.erp.entities.DeliveryOrderLine;
import com.casa.erp.entities.Inventory;
import com.casa.erp.entities.Partner;
import com.casa.erp.entities.Product;
import com.casa.erp.entities.PurchaseOrder;
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
 */


@Named(value = "supDeliveryOrderController")
@ViewScoped
public class SupDeliveryOrderController implements Serializable {

    @Inject
    private DeliveryOrderFacade deliveryOrderFacade;
    @Inject
    @com.casa.erp.beans.util.Status
    private HashMap<String, String> statuses;
    private List<DeliveryOrder> deliveryOrders;
    private List<DeliveryOrder> filteredDeliveryOrders;
    private DeliveryOrder deliveryOrder;
    private List<DeliveryOrderLine> deliveryOrderLines;
    private List<DeliveryOrderLine> tobeDeliveredOrderLines;
    private DeliveryOrderLine deliveryOrderLine;
    private String currentPage = "/sc/supDeliveryOrder/List.xhtml";
    private String purchaseId;
    private String listType;
    private String deliveryId;
    private String partnerId;
    private int rowIndex;
    private List<Partner> topNSuppliers;
    private List<Product> topPurchasedNProducts;
    private Partner supplier;
    private Product product;

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

    private void refreshInventory(List<DeliveryOrderLine> orderLines) {

        for (DeliveryOrderLine orderLine : orderLines) {
            Inventory upToDateInventory = deliveryOrderFacade.findInventory(orderLine.getProduct().getInventory().getId());
            orderLine.getProduct().setInventory(upToDateInventory);
        }
    }

    private void updateInventory(List<DeliveryOrderLine> orderLines) {

        for (DeliveryOrderLine orderLine : orderLines) {
//            orderLine.getProduct().getInventory().setQuantityAvailable(orderLine.getProduct().getInventory().getQuantityAvailable() + 2);
            deliveryOrderFacade.update(orderLine.getProduct().getInventory());
        }
    }

    public void confirmDelivery(Integer id) {

        if (deliveryExist(id)) {
            if (deliveryOrder.getState().equals(Status.DRAFT.value())) {
                refreshInventory(deliveryOrder.getDeliveryOrderLines());
                deliveryOrder.setState(Status.AVAILABLE.value());
                for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
                    orderLine.setState(Status.AVAILABLE.value());
                    orderLine.getProduct().getInventory().setIncomingQuantity(orderLine.getProduct().getInventory().getIncomingQuantity() + orderLine.getQuantity());
                }
                updateInventory(deliveryOrder.getDeliveryOrderLines());
                deliveryOrder = deliveryOrderFacade.update(deliveryOrder);
                deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorValidate");
            }
        }
    }

    public void deliver(Integer id) {
        if (deliveryExist(id)) {
            if (deliveryOrder.getState().equals(Status.AVAILABLE.value())) {
                refreshAvailableProducts();
                if (tobeDeliveredOrderLines != null && !tobeDeliveredOrderLines.isEmpty()) {
                    refreshInventory(tobeDeliveredOrderLines);
                    prepareOrderLinesToDeliver();
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
            prepareDelivery();

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

            for (DeliveryOrderLine line : tobeDeliveredOrderLines) {
                System.out.println(line.getProduct().getName() + " " + line.getQuantity());
            }
        }
    }

    private void prepareOrderLinesToDeliver() {

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
                        "Purchase",
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

            if (!deliveryLine.getState().equals("Extra")) {
                deliveryLine.getProduct().getInventory().setIncomingQuantity(JsfUtil.round((deliveryLine.getProduct().getInventory().getIncomingQuantity() - deliveryLine.getQuantity()), deliveryLine.getProduct().getUom().getDecimals()));
            }
            System.out.println("Purchase Price: " + deliveryLine.getPrice());
            System.out.println("TotalCost: " + Math.abs(deliveryLine.getProduct().getInventory().getTotalCost()));
            System.out.println("Cost: " + Math.abs(deliveryLine.getProduct().getInventory().getUnitCost()));
            System.out.println("Quantity: " + Math.abs(deliveryLine.getProduct().getInventory().getQuantityOnHand()));

            Double totalCost = (deliveryLine.getPrice() * deliveryLine.getQuantity()) + Math.abs(deliveryLine.getProduct().getInventory().getTotalCost());
            Double totalQuantity = deliveryLine.getQuantity() + Math.abs(deliveryLine.getProduct().getInventory().getQuantityOnHand());

            System.out.println("New TotalCost: " + totalQuantity);
            System.out.println("New Quantity: " + totalQuantity);

            deliveryLine.getProduct().getInventory().setUnitCost(JsfUtil.round(totalCost / totalQuantity));
            System.out.println("New Cost: " + (JsfUtil.round(totalCost / totalQuantity)));
            deliveryLine.getProduct().getInventory().setQuantityOnHand(deliveryLine.getProduct().getInventory().getQuantityOnHand() + deliveryLine.getQuantity());
            deliveryLine.getProduct().getInventory().setTotalCost(JsfUtil.round(deliveryLine.getProduct().getInventory().getQuantityOnHand() * deliveryLine.getProduct().getInventory().getUnitCost()));
//          deliveryLine.getProduct().getInventory().setTotalCost(JsfUtil.round((deliveryLine.getPrice() * deliveryLine.getQuantity()) + Math.abs(deliveryLine.getProduct().getInventory().getTotalCost())));
//          deliveryLine.getProduct().getInventory().setUnitCost(JsfUtil.round(deliveryLine.getProduct().getInventory().getTotalCost() / deliveryLine.getProduct().getInventory().getQuantityOnHand()));

            deliveryLine.setDeliveryOrder(deliveryOrder);
            deliveryLine.setState(Status.DONE.value());
        }

        deliveryOrder.setDeliveryOrderLines(tobeDeliveredOrderLines);
        deliveryOrder.setState(Status.DONE.value());

        if (deliveryOrder.getPurchaseOrder() != null) {
            if (deliveryOrder.getPurchaseOrder().getPaid()) {
                deliveryOrder.getPurchaseOrder().setState(Status.DONE.value());
            }
            deliveryOrder.getPurchaseOrder().setShipped(Boolean.TRUE);
            deliveryOrderFacade.update(deliveryOrder.getPurchaseOrder());
        }

        updateInventory(deliveryOrder.getDeliveryOrderLines());

        deliveryOrder = deliveryOrderFacade.update(deliveryOrder);
        deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);

        tobeDeliveredOrderLines = null;
        deliveryOrderLines = null;

    }

    private void partialDelivery() {

        DeliveryOrder backOrder = new DeliveryOrder(new Date(), deliveryOrder.getOrigin(), Status.DONE.value(), "Purchase", Boolean.TRUE, "Partial", deliveryOrder, deliveryOrder.getPartner(), deliveryOrder.getPurchaseOrder());

        for (DeliveryOrderLine deliveryLine : tobeDeliveredOrderLines) {

            if (!deliveryLine.getState().equals("Extra")) {
                deliveryLine.getProduct().getInventory().setIncomingQuantity(JsfUtil.round((deliveryLine.getProduct().getInventory().getIncomingQuantity() - deliveryLine.getQuantity()), deliveryLine.getProduct().getUom().getDecimals()));
            }

            deliveryLine.getProduct().getInventory().setQuantityOnHand(deliveryLine.getProduct().getInventory().getQuantityOnHand() + deliveryLine.getQuantity());
            deliveryLine.getProduct().getInventory().setTotalCost(JsfUtil.round((deliveryLine.getPrice() * deliveryLine.getQuantity()) + deliveryLine.getProduct().getInventory().getTotalCost()));
            deliveryLine.getProduct().getInventory().setUnitCost(JsfUtil.round(deliveryLine.getProduct().getInventory().getTotalCost() / deliveryLine.getProduct().getInventory().getQuantityOnHand()));

            deliveryLine.setDeliveryOrder(backOrder);
            deliveryLine.setState(Status.DONE.value());
        }

        backOrder.setDeliveryOrderLines(tobeDeliveredOrderLines);
        if (backOrder.getPurchaseOrder() != null) {
            backOrder.getPurchaseOrder().getDeliveryOrders().add(backOrder);
        }

        updateInventory(backOrder.getDeliveryOrderLines());

        backOrder = deliveryOrderFacade.createInDelivery(backOrder);

        if (backOrder.getPurchaseOrder() != null) {
            deliveryOrderFacade.update(backOrder.getPurchaseOrder());
        }

        deliveryOrder.setDeliveryMethod("Partial");
        deliveryOrder = deliveryOrderFacade.update(deliveryOrder);

        deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);
        deliveryOrders.add(backOrder);
        deliveryOrder = backOrder;
        tobeDeliveredOrderLines = null;
        deliveryOrderLines = null;
    }

    public void prepareDelivery() {

        tobeDeliveredOrderLines = new ArrayList<>();

        for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {

            if (orderLine.getState().equals(Status.AVAILABLE.value())) {

                DeliveryOrderLine Line = new DeliveryOrderLine(
                        orderLine.getProduct(),
                        orderLine.getQuantity(),
                        0d,
                        orderLine.getUom(),
                        Status.AVAILABLE.value(),
                        "Purchase",
                        Boolean.TRUE,
                        deliveryOrder.getPartner(),
                        orderLine.getPrice(),
                        deliveryOrder);

                tobeDeliveredOrderLines.add(Line);

            }
        }

        for (int i = 0; i < tobeDeliveredOrderLines.size(); i++) {
            for (int j = 0; j < tobeDeliveredOrderLines.size(); j++) {
                if (tobeDeliveredOrderLines.get(i).getProduct().getId() == tobeDeliveredOrderLines.get(j).getProduct().getId() && tobeDeliveredOrderLines.get(i) != tobeDeliveredOrderLines.get(j)) {

                    tobeDeliveredOrderLines.get(i).setQuantity(tobeDeliveredOrderLines.get(i).getQuantity() + tobeDeliveredOrderLines.get(j).getQuantity());
                    tobeDeliveredOrderLines.remove(j);
                }
            }
        }
    }

    public void prepareDelivery(Integer id) {

        if (deliveryExist(id)) {
            if (deliveryOrder.getState().equals(Status.AVAILABLE.value())) {
                tobeDeliveredOrderLines = new ArrayList<>();

                for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {

                    if (orderLine.getState().equals(Status.AVAILABLE.value())) {
                        System.out.println("yes line");
                        DeliveryOrderLine Line = new DeliveryOrderLine(
                                orderLine.getProduct(),
                                orderLine.getQuantity(),
                                0d,
                                orderLine.getUom(),
                                Status.AVAILABLE.value(),
                                "Purchase",
                                Boolean.TRUE,
                                deliveryOrder.getPartner(),
                                orderLine.getPrice(),
                                deliveryOrder);

                        tobeDeliveredOrderLines.add(Line);

                    }
                }

                for (int i = 0; i < tobeDeliveredOrderLines.size(); i++) {
                    for (int j = 0; j < tobeDeliveredOrderLines.size(); j++) {
                        if (tobeDeliveredOrderLines.get(i).getProduct().getId() == tobeDeliveredOrderLines.get(j).getProduct().getId() && tobeDeliveredOrderLines.get(i) != tobeDeliveredOrderLines.get(j)) {

                            tobeDeliveredOrderLines.get(i).setQuantity(tobeDeliveredOrderLines.get(i).getQuantity() + tobeDeliveredOrderLines.get(j).getQuantity());
                            tobeDeliveredOrderLines.remove(j);
                        }
                    }
                }
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
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

                deliveryOrder.setState(Status.CANCELLED.value());
                for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
                    orderLine.setState(Status.CANCELLED.value());
                }
                if (!deliveryOrder.getState().equals(Status.DRAFT.value())) {
                    refreshInventory(deliveryOrder.getDeliveryOrderLines());
                    for (DeliveryOrderLine orderLine : deliveryOrder.getDeliveryOrderLines()) {
                        orderLine.getProduct().getInventory().setIncomingQuantity(JsfUtil.round((orderLine.getProduct().getInventory().getIncomingQuantity() - orderLine.getQuantity()), orderLine.getProduct().getUom().getDecimals()));
                    }
                    updateInventory(deliveryOrder.getDeliveryOrderLines());
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

    public void deleteDelivery(Integer id) {
        if (deliveryExist(id)) {
            if (deliveryOrder.getState().equals("Cancelled")) {
                cancelRelations();
                try {
                    deliveryOrderFacade.remove(deliveryOrder);
                } catch (Exception e) {
                    System.out.println("Error Delete: "+e.getMessage());
                    JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                    return;
                }

                if (deliveryOrders.size() > 1) {
                    deliveryOrders.remove(deliveryOrder);
                } else {
                    listType = null;
                    deliveryOrders = deliveryOrderFacade.findInDelivery();
                }

                deliveryOrder = null;
                JsfUtil.addSuccessMessage("ItemDeleted");
                currentPage = "/sc/supDeliveryOrder/List.xhtml";
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

        if (deliveryOrder.getPurchaseOrder() != null) {
            PurchaseOrder purchaseOrder = deliveryOrderFacade.findPurchaseOrder(deliveryOrder.getPurchaseOrder().getId());
            purchaseOrder.getDeliveryOrders().size();
            purchaseOrder.getDeliveryOrders().remove(deliveryOrder);
            deliveryOrder.setPurchaseOrder(null);
            deliveryOrderFacade.update(purchaseOrder);
        }
    }

    public void showDeliveryList() {
        deliveryOrder = null;
        currentPage = "/sc/supDeliveryOrder/List.xhtml";
    }

    public void showForm() {

        if (deliveryOrders.size() > 0) {
            deliveryOrder = deliveryOrders.get(0);
            currentPage = "/sc/supDeliveryOrder/View.xhtml";
        }
    }

    public void showForm(Integer id) {
        if (deliveryExist(id)) {
            listType = null;
            deliveryOrders = deliveryOrderFacade.findInDelivery();
            currentPage = "/sc/supDeliveryOrder/View.xhtml";
        }
    }

    public void viewPartialDelivries(Integer id) {
        if (deliveryExist(id)) {
            deliveryOrders = deliveryOrderFacade.findByBackOrder(id);
            currentPage = "/sc/supDeliveryOrder/List.xhtml";
            listType = "partialDelivery";
        }
    }

    public Long countPartialDelivries() {
        if (deliveryOrder != null) {
            return deliveryOrderFacade.countByBackOrder(deliveryOrder.getId());
        }
        return 0L;
    }

    public void viewDeliveryOrder() {

        if (deliveryId != null && JsfUtil.isNumeric(deliveryId)) {
            Integer id = Integer.valueOf(deliveryId);
            deliveryOrder = deliveryOrderFacade.find(id);
            if (deliveryOrder != null) {
                deliveryOrders = deliveryOrderFacade.findInDelivery();
                currentPage = "/sc/supDeliveryOrder/View.xhtml";
                return;
            }
        }

        if (partnerId != null && JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            deliveryOrders = deliveryOrderFacade.findByPartner(id, "Purchase");
            if (deliveryOrders != null && !deliveryOrders.isEmpty()) {
                currentPage = "/sc/supDeliveryOrder/List.xhtml";
                listType = "partner";
                return;
            }
        }

        if (purchaseId != null && JsfUtil.isNumeric(purchaseId)) {
            Integer id = Integer.valueOf(purchaseId);
            deliveryOrders = deliveryOrderFacade.findByPurchaseId(id);
            if ((deliveryOrders != null) && (!deliveryOrders.isEmpty())) {
                currentPage = "/sc/supDeliveryOrder/List.xhtml";
                listType = "purchaseOrder";
                return;
            }
        }

        deliveryOrders = deliveryOrderFacade.findInDelivery();
        currentPage = "/sc/supDeliveryOrder/List.xhtml";
    }

    public void prepareView() {
        if (deliveryOrder != null) {
            if (deliveryExist(deliveryOrder.getId())) {
                currentPage = "/sc/supDeliveryOrder/View.xhtml";
            }
        }
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
                currentPage = "/sc/supDeliveryOrder/List.xhtml";
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

        ResourceBundle bundle = JsfUtil.getBundle();
        String name = bundle.getString("Receipt");

        Map<String, Object> params = new HashMap<>();
        params.put("receipt", deliveryOrder);
        params.put("partner", deliveryOrder.getPartner());
        params.put("orderLines", deliveryOrder.getDeliveryOrderLines());
        params.put("SUBREPORT_DIR", FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/")+"/");

        String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/receipt.jasper");
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
            deliveryOrders = deliveryOrderFacade.findInDelivery();
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

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
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

    public String getListType() {
        return listType;
    }

    public void setListType(String listType) {
        this.listType = listType;
    }

    public void setRowIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        rowIndex = Integer.valueOf(params.get("rowIndex"));
    }

    public void onSelectSupplier() {
        if ((supplier != null) && (!topNSuppliers.contains(supplier))) {
            topNSuppliers.add(supplier);
        }
        deliveryOrder.setPartner(supplier);
    }

    public void onSelectProduct() {

        if ((product != null)) {
            if (!topPurchasedNProducts.contains(product)) {
                topPurchasedNProducts.add(product);
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
                currentPage = "/sc/supDeliveryOrder/View.xhtml";
            } else if (deliveryOrderLines.isEmpty()) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneDeliveryOrderLineUpdate");

            } else {

                for (DeliveryOrderLine orderLine : deliveryOrderLines) {
                    orderLine.setDeliveryOrder(deliveryOrder);
                    orderLine.setPartner(deliveryOrder.getPartner());
                    orderLine.setUom(orderLine.getProduct().getUom().getName());
                    orderLine.setPrice(orderLine.getProduct().getPurchasePrice());
                    orderLine.setReserved(0d);
                    orderLine.setState(Status.NEW.value());
                    orderLine.setType("Purchase");
                    orderLine.setActive(Boolean.TRUE);
                }

                deliveryOrder.setDeliveryOrderLines(deliveryOrderLines);
                deliveryOrder = deliveryOrderFacade.update(deliveryOrder);

                if (listType == null && deliveryOrders != null) {
                    deliveryOrders.set(deliveryOrders.indexOf(deliveryOrder), deliveryOrder);
                } else {
                    deliveryOrders = deliveryOrderFacade.findInDelivery();
                    listType = null;
                }

                currentPage = "/sc/supDeliveryOrder/View.xhtml";
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
                OrderLine.setType("Purchase");
                OrderLine.setActive(Boolean.TRUE);
                OrderLine.setUom(OrderLine.getProduct().getUom().getName());
                OrderLine.setPrice(OrderLine.getProduct().getPurchasePrice());
            }

            deliveryOrder.setState(Status.DRAFT.value());
            deliveryOrder.setType("Purchase");
            deliveryOrder.setActive(Boolean.TRUE);
            deliveryOrder.setDeliveryMethod("Complete");
            deliveryOrder.setDeliveryOrderLines(deliveryOrderLines);
            deliveryOrder = deliveryOrderFacade.createInDelivery(deliveryOrder);

            if (listType == null && deliveryOrders != null) {
                deliveryOrders.add(deliveryOrder);
            } else {
                deliveryOrders = deliveryOrderFacade.findInDelivery();
                listType = null;
            }

            currentPage = "/sc/supDeliveryOrder/View.xhtml";
        }
    }

    public void prepareCreate() {
        deliveryOrder = new DeliveryOrder();
        deliveryOrderLines = new ArrayList<>();
        deliveryOrderLine = new DeliveryOrderLine();
        topNSuppliers = deliveryOrderFacade.findTopNSuppliers(4);
        topPurchasedNProducts = deliveryOrderFacade.findTopNPurchasedProducts(4);
        if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topPurchasedNProducts.get(0));
            deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
        }
        deliveryOrderLine.setState(Status.NEW.value());
        currentPage = "/sc/supDeliveryOrder/Create.xhtml";
    }

    public void prepareEdit(Integer id) {
        if (deliveryExist(id)) {
            if (deliveryOrder.getState().equals(Status.DRAFT.value())) {
                deliveryOrderLine = new DeliveryOrderLine();
                deliveryOrderLines = deliveryOrder.getDeliveryOrderLines();
                topNSuppliers = deliveryOrderFacade.findTopNSuppliers(4);
                topPurchasedNProducts = deliveryOrderFacade.findTopNPurchasedProducts(4);
                if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
                    deliveryOrderLine.setProduct(topPurchasedNProducts.get(0));
                    deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
                }
                deliveryOrderLine.setState(Status.NEW.value());

                if (!topNSuppliers.contains(deliveryOrder.getPartner())) {
                    topNSuppliers.add(deliveryOrder.getPartner());
                }

                for (DeliveryOrderLine orderLine : deliveryOrderLines) {
                    if (!topPurchasedNProducts.contains(orderLine.getProduct())) {
                        topPurchasedNProducts.add(orderLine.getProduct());
                    }
                }

                currentPage = "/sc/supDeliveryOrder/Edit.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEdit");
            }
        }
    }

    public void cancelEdit(Integer id) {
        if (deliveryExist(id)) {
            currentPage = "/sc/supDeliveryOrder/View.xhtml";
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
            newDeliveryOrder.setPurchaseOrder(null);
            newDeliveryOrder.setDate(new Date());
            newDeliveryOrder.setName(null);
            newDeliveryOrder.setState(Status.DRAFT.value());
            newDeliveryOrder.setType("Purchase");
            newDeliveryOrder.setDeliveryMethod("Complete");
            newDeliveryOrder.setActive(Boolean.TRUE);
            newDeliveryOrder.setOrigin(null);

            for (DeliveryOrderLine orderLine : newDeliveryOrder.getDeliveryOrderLines()) {
                orderLine.setId(null);
                orderLine.setDeliveryOrder(newDeliveryOrder);
                orderLine.setPartner(deliveryOrder.getPartner());
                orderLine.setReserved(0d);
                orderLine.setState(Status.NEW.value());
                orderLine.setType("Purchase");
                orderLine.setActive(Boolean.TRUE);
                orderLine.setUom(orderLine.getProduct().getUom().getName());
                orderLine.setPrice(orderLine.getProduct().getPurchasePrice());
            }

            deliveryOrder = newDeliveryOrder;
            deliveryOrderLine = new DeliveryOrderLine();
            deliveryOrderLines = deliveryOrder.getDeliveryOrderLines();
            topNSuppliers = deliveryOrderFacade.findTopNSuppliers(4);
            topPurchasedNProducts = deliveryOrderFacade.findTopNPurchasedProducts(4);
            if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
                deliveryOrderLine.setProduct(topPurchasedNProducts.get(0));
                deliveryOrderLine.setPrice(deliveryOrderLine.getProduct().getPurchasePrice());
                deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
            }

            if (!topNSuppliers.contains(deliveryOrder.getPartner())) {
                topNSuppliers.add(deliveryOrder.getPartner());
            }

            for (DeliveryOrderLine orderLine : deliveryOrderLines) {
                if (!topPurchasedNProducts.contains(orderLine.getProduct())) {
                    topPurchasedNProducts.add(orderLine.getProduct());
                }
            }
            currentPage = "/sc/supDeliveryOrder/Create.xhtml";
        }
    }

    public List<Partner> getTopNSuppliers() {
        if (topNSuppliers == null) {
            topNSuppliers = deliveryOrderFacade.findTopNSuppliers(4);
        }
        return topNSuppliers;

    }

    public List<Product> getTopPurchasedNProducts() {
        if (topPurchasedNProducts == null) {
            topPurchasedNProducts = deliveryOrderFacade.findTopNPurchasedProducts(4);
        }
        return topPurchasedNProducts;
    }

    public Partner getSupplier() {
        return supplier;
    }

    public void setSupplier(Partner supplier) {
        this.supplier = supplier;
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
        if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topPurchasedNProducts.get(0));
            deliveryOrderLine.setUom(topPurchasedNProducts.get(0).getUom().getName());
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
        if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topPurchasedNProducts.get(0));
            deliveryOrderLine.setUom(deliveryOrderLine.getProduct().getUom().getName());
        }
        deliveryOrderLine.setState(Status.NEW.value());
    }

    public void onRowCancel() {
        deliveryOrderLine = new DeliveryOrderLine();
        if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
            deliveryOrderLine.setProduct(topPurchasedNProducts.get(0));
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
            DeliveryOrder deliveryOrder = deliveryOrderFacade.find(id);
            if (deliveryOrder != null) {
                return deliveryOrder.getState();
            } else {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                deliveryOrders = null;
                listType = null;
                currentPage = "/sc/supDeliveryOrder/List.xhtml";
                return null;
            }
        }
        return null;
    }

}
