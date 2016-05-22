package com.casa.erp.beans;

import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.beans.util.Status;
import com.casa.erp.entities.DeliveryOrder;
import com.casa.erp.entities.DeliveryOrderLine;
import com.casa.erp.entities.Invoice;
import com.casa.erp.entities.InvoiceLine;
import com.casa.erp.entities.InvoiceTax;
import com.casa.erp.entities.Partner;
import com.casa.erp.entities.Product;
import com.casa.erp.entities.PurchaseOrder;
import com.casa.erp.entities.PurchaseOrderLine;
import com.casa.erp.dao.PurchaseOrderFacade;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
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

@Named(value = "purchaseOrderController")
@ViewScoped
public class PurchaseOrderController implements Serializable {

    @Inject
    private PurchaseOrderFacade purchaseOrderFacade;
    @Inject
    @Status
    private HashMap<String, String> statuses;
    private List<PurchaseOrder> filteredPurchaseOrders;
    private List<PurchaseOrder> purchaseOrders;
    private PurchaseOrder purchaseOrder;
    private List<PurchaseOrderLine> purchaseOrderLines;
    private List<PurchaseOrderLine> filteredPurchaseOrderLines;
    private PurchaseOrderLine purchaseOrderLine;
    private DeliveryOrder deliveryOrder;
    private List<DeliveryOrderLine> deliverOrderLines;
    private Invoice invoice;
    private List<InvoiceLine> invoiceLines;
    private String invoiceMethod;
    private String purchaseId;
    private String purchaseLineId;
    private String partnerId;
    private String productId;
    private String partialListType;
    private int uninvoicedlines;
    private int rowIndex;
    private List<Partner> topNSuppliers;
    private Partner supplier;
    private List<Product> topPurchasedNProducts;
    private Product product;
    private String currentPage = "/sc/purchaseOrder/List.xhtml";

    public void setRowIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        rowIndex = Integer.valueOf(params.get("rowIndex"));
    }

    public List<Product> getTopPurchasedNProducts() {
        if (topPurchasedNProducts == null) {
            topPurchasedNProducts = purchaseOrderFacade.findTopNPurchasedProducts(4);
        }
        return topPurchasedNProducts;
    }

    public List<Partner> getTopNSuppliers() {
        if (topNSuppliers == null) {
            topNSuppliers = purchaseOrderFacade.findTopNSuppliers(4);
        }
        return topNSuppliers;
    }

    public void onSelectSupplier() {
        if ((supplier != null) && (!topNSuppliers.contains(supplier))) {
            topNSuppliers.add(supplier);
        }
        purchaseOrder.setPartner(supplier);
    }

    public void onSelectProduct() {

        if ((product != null)) {
            if (!topPurchasedNProducts.contains(product)) {
                topPurchasedNProducts.add(product);
            }

            if (rowIndex < 0) {

                purchaseOrderLine.setProduct(product);
                purchaseOrderLine.setPrice(product.getPurchasePrice());
                purchaseOrderLine.setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("PurchaseOrderForm:productMenuTwo");
                RequestContext.getCurrentInstance().update("PurchaseOrderForm:uom");
                RequestContext.getCurrentInstance().update("PurchaseOrderForm:price");

            } else {

                purchaseOrderLines.get(rowIndex).setProduct(product);
                purchaseOrderLines.get(rowIndex).setPrice(product.getPurchasePrice());
                purchaseOrderLines.get(rowIndex).setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("PurchaseOrderForm:datalist:" + rowIndex + ":productMenu");
                RequestContext.getCurrentInstance().update("PurchaseOrderForm:datalist:" + rowIndex + ":uomm");
                RequestContext.getCurrentInstance().update("PurchaseOrderForm:datalist:" + rowIndex + ":pricee");
            }
        }
    }

    public void updateOrder(Integer id) {
        if (getOrderStatus(id) != null) {
            if (!getOrderStatus(id).equals("Quotation")) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEditQuotation");
                currentPage = "/sc/purchaseOrder/View.xhtml";
            } else if (purchaseOrderLines.isEmpty()) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOnePurchaseOrderLineUpdate");
            } else {

                for (PurchaseOrderLine OrderLine : purchaseOrderLines) {
                    OrderLine.setPurchaseOrder(purchaseOrder);
                }
                purchaseOrder.setPurchaseOrderLines(purchaseOrderLines);
                purchaseOrder = purchaseOrderFacade.update(purchaseOrder);

                if (partialListType == null && purchaseOrders != null) {
                    purchaseOrders.set(purchaseOrders.indexOf(purchaseOrder), purchaseOrder);
                } else {
                    purchaseOrders = purchaseOrderFacade.findAll();
                    partialListType = null;
                }

                currentPage = "/sc/purchaseOrder/View.xhtml";
            }
        }
    }

    public void createOrder() {
        if (purchaseOrderLines.isEmpty()) {
            JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOnePurchaseOrderLineCreate");
        } else {

            for (PurchaseOrderLine OrderLine : purchaseOrderLines) {
                OrderLine.setPurchaseOrder(purchaseOrder);
            }
            purchaseOrder.setState("Quotation");
            purchaseOrder.setPurchaseOrderLines(purchaseOrderLines);
            purchaseOrder = purchaseOrderFacade.create(purchaseOrder);

            if (partialListType == null && purchaseOrders != null) {
                purchaseOrders.add(purchaseOrder);
            } else {
                purchaseOrders = purchaseOrderFacade.findAll();
                partialListType = null;
            }

            currentPage = "/sc/purchaseOrder/View.xhtml";
        }
    }

    public void deleteOrder(Integer id) {
        if (purchaseOrderExist(id)) {
            if (purchaseOrder.getState().equals("Cancelled")) {
                cancelRelations();
                try {
                    purchaseOrderFacade.remove(purchaseOrder);
                } catch (Exception e) {
                    System.out.println("Error Delete: " + e.getMessage());
                    JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                    return;
                }

                if (purchaseOrders.size() > 1) {
                    purchaseOrders.remove(purchaseOrder);
                } else {
                    purchaseOrders = purchaseOrderFacade.findAll();
                    partialListType = null;
                }

                purchaseOrder = null;
                JsfUtil.addSuccessMessage("ItemDeleted");
                currentPage = "/sc/purchaseOrder/List.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
            }
        }
    }

    private void cancelRelations() {
        if (!purchaseOrder.getInvoices().isEmpty()) {
            for (Invoice invoice : purchaseOrder.getInvoices()) {
                invoice.setPurchaseOrder(null);
                purchaseOrderFacade.update(invoice);
            }
            purchaseOrder.getInvoices().clear();
        }

        if (!purchaseOrder.getDeliveryOrders().isEmpty()) {
            for (DeliveryOrder deliveryOrder : purchaseOrder.getDeliveryOrders()) {
                deliveryOrder.setPurchaseOrder(null);
                purchaseOrderFacade.update(deliveryOrder);
            }
            purchaseOrder.getDeliveryOrders().clear();
        }
    }

    public void createDeliveryOrder(Integer id) {
        if (purchaseOrderExist(id)) {
            if (purchaseOrder.getState().equals("Purchase Order") && purchaseOrder.getDeliveryOrders() != null && purchaseOrder.getDeliveryCreated() != true) {

                deliveryOrder = new DeliveryOrder(new Date(), purchaseOrder.getName(), "Draft", "Purchase", Boolean.TRUE, "Complete", null, purchaseOrder.getPartner(), purchaseOrder);
                deliverOrderLines = new ArrayList<>();

                for (PurchaseOrderLine purchaseLine : purchaseOrder.getPurchaseOrderLines()) {

                    deliverOrderLines.add(new DeliveryOrderLine(
                            purchaseLine.getProduct(),
                            purchaseLine.getQuantity(),
                            0d,
                            purchaseLine.getUom(),
                            "New",
                            "Purchase",
                            Boolean.TRUE,
                            purchaseOrder.getPartner(),
                            purchaseLine.getPrice(),
                            deliveryOrder));
                }
                deliveryOrder.setDeliveryOrderLines(deliverOrderLines);
                purchaseOrder.getDeliveryOrders().add(deliveryOrder);
                purchaseOrder.setDeliveryCreated(true);
                purchaseOrderFacade.create(deliveryOrder);
                purchaseOrder = purchaseOrderFacade.update(purchaseOrder);
                purchaseOrders.set(purchaseOrders.indexOf(purchaseOrder), purchaseOrder);
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    private void refreshLinesToInvoice() {

        if (purchaseOrderLines != null && !purchaseOrderLines.isEmpty()) {
            List<PurchaseOrderLine> selectedLines = new ArrayList<>();
            selectedLines.addAll(purchaseOrderLines);
            prepareInvoicing();

            ListIterator<PurchaseOrderLine> iterator = purchaseOrderLines.listIterator();

            while (iterator.hasNext()) {
                PurchaseOrderLine purchaseLine = iterator.next();
                boolean exist = false;
                for (PurchaseOrderLine line : selectedLines) {
                    if (purchaseLine.getId() == line.getId()) {
                        exist = true;
                    }
                }

                if (exist == false) {
                    iterator.remove();
                }
            }

            for (PurchaseOrderLine line : purchaseOrderLines) {
                System.out.println("id: " + line.getId());
            }
        }
    }

    public void createInvoice(Integer id, boolean redirect) throws IOException {
        if (purchaseOrderExist(id)) {
            if ((purchaseOrder.getState().equals("Purchase Order")) && (purchaseOrder.getInvoiceMethod().equals("Partial"))) {
                refreshLinesToInvoice();
                if (purchaseOrderLines != null && !purchaseOrderLines.isEmpty()) {
                    String invoiceReference = ((purchaseOrder.getReference() == null) || (purchaseOrder.getReference().isEmpty())) ? purchaseOrder.getName() : purchaseOrder.getReference();
                    invoice = new Invoice(new Date(), "Purchase", purchaseOrder.getName(), "Draft", Boolean.TRUE, purchaseOrder.getPartner(), purchaseOrder, purchaseOrderFacade.findAccount("Account Payable"), purchaseOrderFacade.findJournal("BILL"), invoiceReference);
                    SumUpInvoice();
                    invoiceLines = new ArrayList<>();
                    for (PurchaseOrderLine lineToInvoice : purchaseOrderLines) {

                        for (PurchaseOrderLine purchaseLine : purchaseOrder.getPurchaseOrderLines()) {

                            if (lineToInvoice.getId() == purchaseLine.getId()) {

                                purchaseLine.setInvoiced(Boolean.TRUE);

                                invoiceLines.add(new InvoiceLine(
                                        new Date(),
                                        purchaseLine.getUom(),
                                        purchaseLine.getPrice(),
                                        purchaseLine.getSubTotal(),
                                        0d,
                                        purchaseLine.getQuantity(),
                                        Boolean.TRUE,
                                        invoice,
                                        purchaseOrder.getPartner(),
                                        purchaseLine.getProduct(),
                                        purchaseLine.getTax(),
                                        purchaseOrderFacade.findAccount("Product Purchases")));
                            }
                        }
                    }

                    if (purchaseOrderLines.size() == uninvoicedlines) {
                        purchaseOrder.setInvoiceMethod("Complete");
                    }

                    //account_id not null in invoice line
                    invoice.setInvoiceLines(invoiceLines);
                    generateInvoiceTaxes();
                    purchaseOrder.getInvoices().add(invoice);
                    Integer InvId = purchaseOrderFacade.create(invoice).getId();
                    purchaseOrder = purchaseOrderFacade.update(purchaseOrder);
                    purchaseOrders.set(purchaseOrders.indexOf(purchaseOrder), purchaseOrder);

                    if (redirect) {
                        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                        context.redirect(context.getRequestContextPath() + "/sc/supInvoice/index.xhtml?id=" + InvId);
                    }
                }
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    private void SumUpInvoice() {

        invoice.setAmountUntaxed(0d);
        invoice.setAmountTax(0d);
        invoice.setAmountTotal(0d);

        for (PurchaseOrderLine orderLine : purchaseOrderLines) {
            invoice.setAmountUntaxed(invoice.getAmountUntaxed() + orderLine.getSubTotal());

            if (orderLine.getTax() != null) {
                invoice.setAmountTax(invoice.getAmountTax() + (orderLine.getSubTotal() * orderLine.getTax().getAmount()));
            }
        }

        invoice.setAmountUntaxed(JsfUtil.round(invoice.getAmountUntaxed()));
        invoice.setAmountTax(JsfUtil.round(invoice.getAmountTax()));
        BigDecimal amountUntaxed = BigDecimal.valueOf(invoice.getAmountUntaxed());
        BigDecimal amountTax = BigDecimal.valueOf(invoice.getAmountTax());
        BigDecimal amountTotal = amountUntaxed.add(amountTax);
        invoice.setAmountTotal(JsfUtil.round(amountTotal.doubleValue()));
        invoice.setResidual(invoice.getAmountTotal());
    }

    private void generateInvoiceTaxes() {

        List<InvoiceTax> invoiceTaxes = new ArrayList<>();

        for (InvoiceLine invoiceline : invoice.getInvoiceLines()) {
            if (invoiceline.getTax() != null) {

                double taxAmount = JsfUtil.round(invoiceline.getPriceSubtotal() * invoiceline.getTax().getAmount());
                invoiceTaxes.add(new InvoiceTax(
                        invoice.getDate(),
                        taxAmount,
                        invoiceline.getPriceSubtotal(),
                        Boolean.TRUE,
                        purchaseOrderFacade.findAccount("Tax Paid"),
                        invoice,
                        invoiceline.getTax()));
            }
        }

        invoice.setInvoiceTaxes(invoiceTaxes);
    }

    public void prepareInvoicing() {
        purchaseOrderLines = new ArrayList<>();
        uninvoicedlines = 0;
        for (PurchaseOrderLine purchaseLine : purchaseOrder.getPurchaseOrderLines()) {
            if (!purchaseLine.getInvoiced()) {
                uninvoicedlines++;
                purchaseOrderLines.add(purchaseLine);
            }
        }
    }

    public void prepareInvoicing(Integer id) {
        if (purchaseOrderExist(id)) {
            if ((purchaseOrder.getState().equals("Purchase Order")) && (!purchaseOrder.getInvoiceMethod().equals("Complete"))) {
                purchaseOrderLines = new ArrayList<>();
                invoiceMethod = "Complete";
                uninvoicedlines = 0;
                for (PurchaseOrderLine purchaseLine : purchaseOrder.getPurchaseOrderLines()) {
                    if (!purchaseLine.getInvoiced()) {
                        uninvoicedlines++;
                        purchaseOrderLines.add(purchaseLine);
                    }
                }
            } else {
                FacesContext.getCurrentInstance().validationFailed();
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    public void updateInvoiceMethod() {
        purchaseOrderLines = new ArrayList<>();
        for (PurchaseOrderLine purchaseLine : purchaseOrder.getPurchaseOrderLines()) {
            if (!purchaseLine.getInvoiced()) {
                purchaseOrderLines.add(purchaseLine);
            }
        }
    }

    public void removeLineToInvoice(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < purchaseOrderLines.size()) {
            purchaseOrderLines.remove(rowIndex);
        }
    }

    public void confirmPurchase(Integer id) {
        if (purchaseOrderExist(id)) {
            if (purchaseOrder.getState().equals("Quotation")) {
                purchaseOrder.setState("Purchase Order");
                purchaseOrder = purchaseOrderFacade.update(purchaseOrder);
                purchaseOrders.set(purchaseOrders.indexOf(purchaseOrder), purchaseOrder);

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    public void cancelOrder(Integer id) {
        if (purchaseOrderExist(id)) {
            if ((!purchaseOrder.getState().equals("Cancelled")) && (!purchaseOrder.getState().equals("Done"))) {
                boolean canBeCancelled = true;

                if (!purchaseOrder.getDeliveryOrders().isEmpty()) {
                    if (!isDeliveryCancelled()) {
                        canBeCancelled = false;
                        JsfUtil.addWarningMessageDialog("CannotCancelPurchaseOrder", "CannotCancelPurchaseOrder_cancelDelivery");

                    } else if (!purchaseOrder.getInvoices().isEmpty()) {
                        if (!isInvoiceCancelled()) {
                            canBeCancelled = false;
                            JsfUtil.addWarningMessageDialog("CannotCancelPurchaseOrder", "CannotCancelPurchaseOrder_cancelInvoice");
                        }
                    }

                } else if (!purchaseOrder.getInvoices().isEmpty()) {
                    if (!isInvoiceCancelled()) {
                        canBeCancelled = false;
                        JsfUtil.addWarningMessageDialog("CannotCancelPurchaseOrder", "CannotCancelPurchaseOrder_cancelInvoice");
                    }
                }

                if (canBeCancelled == true) {
                    purchaseOrder.setState("Cancelled");
                    purchaseOrder = purchaseOrderFacade.update(purchaseOrder);
                    purchaseOrders.set(purchaseOrders.indexOf(purchaseOrder), purchaseOrder);
                }
            } else if (purchaseOrder.getState().equals("Done")) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorCancelRecordDone");

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyCancelled");
            }
        }
    }

    private boolean isDeliveryCancelled() {

        for (DeliveryOrder delivery : purchaseOrder.getDeliveryOrders()) {
            if (!(delivery.getState().equals("Cancelled"))) {
                return false;
            }
        }
        return true;
    }

    private boolean isInvoiceCancelled() {

        for (Invoice invoice : purchaseOrder.getInvoices()) {
            if (!(invoice.getState().equals("Cancelled"))) {
                return false;
            }
        }
        return true;
    }

    public void showForm() {
        if (purchaseOrders != null && purchaseOrders.size() > 0) {
            purchaseOrder = purchaseOrders.get(0);
            currentPage = "/sc/purchaseOrder/View.xhtml";
        }
    }

    public void showForm(Integer id) {
        if (id != null) {
            purchaseOrder = purchaseOrderFacade.find(id);
            if (purchaseOrder != null) {
                partialListType = null;
                purchaseOrderLines = null;
                purchaseOrderLine = null;
                purchaseOrders = purchaseOrderFacade.findAll();
                currentPage = "/sc/purchaseOrder/View.xhtml";
            } else {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
            }
        }

    }

    public void showOrderLineForm() {
        if (purchaseOrderLines != null && purchaseOrderLines.size() > 0) {
            purchaseOrderLine = purchaseOrderLines.get(0);
            currentPage = "/sc/purchaseOrder/ViewByProduct.xhtml";
        }
    }

    public void showOrderList() {

        purchaseOrder = null;
        purchaseOrderLine = null;
        purchaseOrderLines = null;
        topNSuppliers = null;
        topPurchasedNProducts = null;
        currentPage = "/sc/purchaseOrder/List.xhtml";
    }

    public void prepareCreate() {
        purchaseOrder = new PurchaseOrder();
        purchaseOrder.setDate(new Date());
        purchaseOrderLines = new ArrayList<>();
        purchaseOrderLine = new PurchaseOrderLine();
        topNSuppliers = purchaseOrderFacade.findTopNSuppliers(4);
        topPurchasedNProducts = purchaseOrderFacade.findTopNPurchasedProducts(4);
        if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
            purchaseOrderLine.setProduct(topPurchasedNProducts.get(0));
            purchaseOrderLine.setPrice(purchaseOrderLine.getProduct().getPurchasePrice());
            purchaseOrderLine.setUom(purchaseOrderLine.getProduct().getUom().getName());
        }
        currentPage = "/sc/purchaseOrder/Create.xhtml";
    }

    public void prepareEdit(Integer id) {
        if (purchaseOrderExist(id)) {
            if (purchaseOrder.getState().equals("Quotation")) {
                purchaseOrderLine = new PurchaseOrderLine();
                purchaseOrderLines = purchaseOrder.getPurchaseOrderLines();
                topNSuppliers = purchaseOrderFacade.findTopNSuppliers(4);
                topPurchasedNProducts = purchaseOrderFacade.findTopNPurchasedProducts(4);
                if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
                    purchaseOrderLine.setProduct(topPurchasedNProducts.get(0));
                    purchaseOrderLine.setPrice(purchaseOrderLine.getProduct().getPurchasePrice());
                    purchaseOrderLine.setUom(purchaseOrderLine.getProduct().getUom().getName());
                }

                if (!topNSuppliers.contains(purchaseOrder.getPartner())) {
                    topNSuppliers.add(purchaseOrder.getPartner());
                }

                for (PurchaseOrderLine orderLine : purchaseOrderLines) {
                    if (!topPurchasedNProducts.contains(orderLine.getProduct())) {
                        topPurchasedNProducts.add(orderLine.getProduct());
                    }
                }

                currentPage = "/sc/purchaseOrder/Edit.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEditQuotation");
            }
        }
    }

    public void prepareView() {
        if (purchaseOrder != null) {
            if (purchaseOrderExist(purchaseOrder.getId())) {
                currentPage = "/sc/purchaseOrder/View.xhtml";
            }
        }
    }

    public void cancelEdit(Integer id) {
        if (purchaseOrderExist(id)) {
            currentPage = "/sc/purchaseOrder/View.xhtml";
        }
    }

    public void viewPurchaseOrder() {

        if (JsfUtil.isNumeric(purchaseId)) {
            Integer id = Integer.valueOf(purchaseId);
            purchaseOrder = purchaseOrderFacade.find(id);

            if (purchaseOrder != null) {
                purchaseOrders = purchaseOrderFacade.findAll();
                currentPage = "/sc/purchaseOrder/View.xhtml";
                return;
            }
        }

        if (JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            purchaseOrders = purchaseOrderFacade.findByPartner(id);
            if (!purchaseOrders.isEmpty()) {
                currentPage = "/sc/purchaseOrder/List.xhtml";
                partialListType = "partner";
                return;
            }
        }

        if (JsfUtil.isNumeric(productId)) {
            Integer id = Integer.valueOf(productId);
            purchaseOrderLines = purchaseOrderFacade.findByProduct(id);
            if (!purchaseOrderLines.isEmpty()) {
                currentPage = "/sc/purchaseOrder/ListByProduct.xhtml";
                return;
            }
        }

        if (JsfUtil.isNumeric(purchaseLineId)) {
            Integer id = Integer.valueOf(purchaseLineId);
            purchaseOrderLine = purchaseOrderFacade.findOrderLine(id);
            if (purchaseOrderLine != null) {
                purchaseOrderLines = purchaseOrderFacade.findByProduct(purchaseOrderLine.getProduct().getId());
                productId = Integer.toString(purchaseOrderLine.getProduct().getId());
                currentPage = "/sc/purchaseOrder/ViewByProduct.xhtml";
                return;
            }
        }

        purchaseOrders = purchaseOrderFacade.findAll();
        currentPage = "/sc/purchaseOrder/List.xhtml";
    }

    public void duplicatePurchaseOrder(Integer id) {
        if (purchaseOrderExist(id)) {

            purchaseOrder.getPurchaseOrderLines().size();

            PurchaseOrder newPurchaseOrder = (PurchaseOrder) SerializationUtils.clone(purchaseOrder);

            newPurchaseOrder.setId(null);
            newPurchaseOrder.setDeliveryOrders(null);
            newPurchaseOrder.setInvoiceMethod("Partial");
            newPurchaseOrder.setInvoices(null);
            newPurchaseOrder.setPaid(Boolean.FALSE);
            newPurchaseOrder.setShipped(Boolean.FALSE);
            newPurchaseOrder.setDeliveryCreated(Boolean.FALSE);
            newPurchaseOrder.setDate(new Date());
            newPurchaseOrder.setNotes(null);
            //newPurchaseOrder.setDiscount(null);
            newPurchaseOrder.setName(null);
            newPurchaseOrder.setReference(null);
            newPurchaseOrder.setState("Quotation");
            newPurchaseOrder.setActive(Boolean.TRUE);

            for (PurchaseOrderLine Line : newPurchaseOrder.getPurchaseOrderLines()) {
                Line.setId(null);
                Line.setInvoiced(Boolean.FALSE);
                Line.setPurchaseOrder(newPurchaseOrder);
            }

            purchaseOrder = newPurchaseOrder;
            purchaseOrderLine = new PurchaseOrderLine();
            purchaseOrderLines = purchaseOrder.getPurchaseOrderLines();
            topNSuppliers = purchaseOrderFacade.findTopNSuppliers(4);
            topPurchasedNProducts = purchaseOrderFacade.findTopNPurchasedProducts(4);
            if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
                purchaseOrderLine.setProduct(topPurchasedNProducts.get(0));
                purchaseOrderLine.setPrice(purchaseOrderLine.getProduct().getPurchasePrice());
                purchaseOrderLine.setUom(purchaseOrderLine.getProduct().getUom().getName());
            }

            if (!topNSuppliers.contains(purchaseOrder.getPartner())) {
                topNSuppliers.add(purchaseOrder.getPartner());
            }

            for (PurchaseOrderLine orderLine : purchaseOrderLines) {
                if (!topPurchasedNProducts.contains(orderLine.getProduct())) {
                    topPurchasedNProducts.add(orderLine.getProduct());
                }
            }

            currentPage = "/sc/purchaseOrder/Create.xhtml";
        }
    }

    public void printOrder(ActionEvent actionEvent) throws IOException, JRException {

        for (PurchaseOrderLine orderLine : purchaseOrder.getPurchaseOrderLines()) {

            orderLine.setProductName(orderLine.getProduct().getName());
            if (orderLine.getTax() != null) {
                orderLine.setTaxName(orderLine.getTax().getName());
            } else {
                orderLine.setTaxName("");
            }
        }

        ResourceBundle bundle = JsfUtil.getBundle();
        String name = bundle.getString("PurchaseOrder");
        String currency = bundle.getString("Currency");

        Map<String, Object> params = new HashMap<>();
        params.put("purchaseOrder", purchaseOrder);
        params.put("partner", purchaseOrder.getPartner());
        params.put("orderLines", purchaseOrder.getPurchaseOrderLines());
        params.put("currency", currency);
        params.put("SUBREPORT_DIR", FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/")+"/");

        String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/purchaseOrder.jasper");
        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, params, new JREmptyDataSource());
//        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, new HashMap<String,Object>(), new JRBeanArrayDataSource(new SaleOrder[]{saleOrder}));  
        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        httpServletResponse.addHeader("Content-disposition", "attachment; filename=" + name + "_" + purchaseOrder.getName() + ".pdf");
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
        FacesContext.getCurrentInstance().responseComplete();

    }

    public void onRowAdd(ActionEvent event) {
        purchaseOrderLine.setActive(Boolean.TRUE);
        purchaseOrderLine.setInvoiced(Boolean.FALSE);
        purchaseOrderLine.setPrice(JsfUtil.round(purchaseOrderLine.getPrice()));
        purchaseOrderLine.setQuantity(JsfUtil.round(purchaseOrderLine.getQuantity(), purchaseOrderLine.getProduct().getUom().getDecimals()));
       

        if (purchaseOrderLine.getQuantity() == 0d) {
            purchaseOrderLine.setQuantity(1d);
        }

        if (purchaseOrderLine.getPrice() == 0d) {
            purchaseOrderLine.setTax(null);
        }
        
        purchaseOrderLine.setSubTotal(JsfUtil.round((purchaseOrderLine.getPrice()) * (purchaseOrderLine.getQuantity())));
        purchaseOrderLines.add(purchaseOrderLine);
        SumUpOrder();
        purchaseOrderLine = new PurchaseOrderLine();
        if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
            purchaseOrderLine.setProduct(topPurchasedNProducts.get(0));
            purchaseOrderLine.setPrice(topPurchasedNProducts.get(0).getPurchasePrice());
            purchaseOrderLine.setUom(topPurchasedNProducts.get(0).getUom().getName());
        }
    }

    public void onRowDelete(int Index) {
        purchaseOrderLines.remove(Index);
        SumUpOrder();
    }

    public void onRowEditInit(PurchaseOrderLine orderLine) {
        purchaseOrderLine = (PurchaseOrderLine) SerializationUtils.clone(orderLine);
    }

    public void onRowEdit(int index) {
        purchaseOrderLines.get(index).setQuantity(JsfUtil.round(purchaseOrderLines.get(index).getQuantity(), purchaseOrderLines.get(index).getProduct().getUom().getDecimals()));
        purchaseOrderLines.get(index).setPrice(JsfUtil.round(purchaseOrderLines.get(index).getPrice()));
        
        if (purchaseOrderLines.get(index).getQuantity() == 0d) {
            purchaseOrderLines.get(index).setQuantity(1d);
        }

        if (purchaseOrderLines.get(index).getPrice() == 0d) {
            purchaseOrderLines.get(index).setTax(null);
        }
        
        purchaseOrderLines.get(index).setSubTotal(JsfUtil.round(purchaseOrderLines.get(index).getPrice() * purchaseOrderLines.get(index).getQuantity()));
        SumUpOrder();
    }

    public void onRowCancel(int index) {
        purchaseOrderLines.remove(index);
        purchaseOrderLines.add(index, purchaseOrderLine);
        purchaseOrderLine = new PurchaseOrderLine();
        if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
            purchaseOrderLine.setProduct(topPurchasedNProducts.get(0));
            purchaseOrderLine.setPrice(purchaseOrderLine.getProduct().getPurchasePrice());
            purchaseOrderLine.setUom(purchaseOrderLine.getProduct().getUom().getName());
        }
    }

    public void onRowCancel() {
        purchaseOrderLine = new PurchaseOrderLine();
        if (topPurchasedNProducts != null && !topPurchasedNProducts.isEmpty()) {
            purchaseOrderLine.setProduct(topPurchasedNProducts.get(0));
            purchaseOrderLine.setPrice(purchaseOrderLine.getProduct().getPurchasePrice());
            purchaseOrderLine.setUom(purchaseOrderLine.getProduct().getUom().getName());
        }
    }

    public void onProductChange() {
        purchaseOrderLine.setPrice(purchaseOrderLine.getProduct().getPurchasePrice());
        purchaseOrderLine.setUom(purchaseOrderLine.getProduct().getUom().getName());
    }

    public void onProductChange(int rowIndex) {
        purchaseOrderLines.get(rowIndex).setPrice(purchaseOrderLines.get(rowIndex).getProduct().getPurchasePrice());
        purchaseOrderLines.get(rowIndex).setUom(purchaseOrderLines.get(rowIndex).getProduct().getUom().getName());
    }

    private void SumUpOrder() {
        purchaseOrder.setAmountUntaxed(0d);
        purchaseOrder.setAmountTax(0d);
        purchaseOrder.setAmountTotal(0d);

        for (PurchaseOrderLine orderLine : purchaseOrderLines) {
            purchaseOrder.setAmountUntaxed(purchaseOrder.getAmountUntaxed() + orderLine.getSubTotal());
            if (orderLine.getTax() != null) {
                purchaseOrder.setAmountTax(purchaseOrder.getAmountTax() + (orderLine.getSubTotal() * orderLine.getTax().getAmount()));
            }
        }

        purchaseOrder.setAmountUntaxed(JsfUtil.round(purchaseOrder.getAmountUntaxed()));
        purchaseOrder.setAmountTax(JsfUtil.round(purchaseOrder.getAmountTax()));
        BigDecimal amountUntaxed = BigDecimal.valueOf(purchaseOrder.getAmountUntaxed());
        BigDecimal amountTax = BigDecimal.valueOf(purchaseOrder.getAmountTax());
        BigDecimal amountTotal = amountUntaxed.add(amountTax);
        purchaseOrder.setAmountTotal(JsfUtil.round(amountTotal.doubleValue()));

    }

    public String getStatus(String status) {
        return statuses.get(status);
    }

    public String getStatusColor(String status) {
        switch (status) {
            case "Quotation":
                return "#009fd4";
            case "Purchase Order":
                return "#406098";
            case "Done":
                return "#3477db";
            default:
                return "#6d8891";
        }
    }

    private boolean purchaseOrderExist(Integer id) {
        if (id != null) {
            purchaseOrder = purchaseOrderFacade.find(id);
            if (purchaseOrder == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                purchaseOrders = null;
                partialListType = null;
                currentPage = "/sc/purchaseOrder/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    private String getOrderStatus(Integer id) {
        if (id != null) {
            PurchaseOrder purchaseOrder = purchaseOrderFacade.find(id);
            if (purchaseOrder != null) {
                return purchaseOrder.getState();
            } else {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                purchaseOrders = null;
                partialListType = null;
                currentPage = "/sc/purchaseOrder/List.xhtml";
                return null;
            }
        }
        return null;
    }

    public Double getLineTax() {
        if (purchaseOrderLine != null && purchaseOrderLine.getTax() != null) {
            return JsfUtil.round(purchaseOrderLine.getQuantity() * purchaseOrderLine.getPrice() * purchaseOrderLine.getTax().getAmount());
        } else {
            return 0d;
        }
    }

    public Double getLineTotal() {
        return getLineTax() + purchaseOrderLine.getSubTotal();
    }

    public String getPage() {
        return currentPage;
    }

    public void setPage(String page) {
        this.currentPage = page;
    }

    public PurchaseOrder getPurchaseOrder() {
        if (purchaseOrder == null) {
            return purchaseOrder = new PurchaseOrder();
        }
        return purchaseOrder;
    }

    public void setPurchaseOrder(PurchaseOrder purchaseOrder) {
        this.purchaseOrder = purchaseOrder;
    }

    public PurchaseOrderLine getPurchaseOrderLine() {
        if (purchaseOrderLine == null) {
            purchaseOrderLine = new PurchaseOrderLine();
        }
        return purchaseOrderLine;
    }

    public void setPurchaseOrderLine(PurchaseOrderLine purchaseOrderLine) {
        this.purchaseOrderLine = purchaseOrderLine;
    }

    public List<PurchaseOrder> getPurchaseOrders() {
        if (purchaseOrders == null) {
            purchaseOrders = purchaseOrderFacade.findAll();
        }
        return purchaseOrders;
    }

    public List<PurchaseOrder> getFilteredPurchaseOrders() {
        return filteredPurchaseOrders;
    }

    public void setFilteredPurchaseOrders(List<PurchaseOrder> filteredPurchaseOrders) {
        this.filteredPurchaseOrders = filteredPurchaseOrders;
    }

    public void setPurchaseOrders(List<PurchaseOrder> purchaseOrders) {
        this.purchaseOrders = purchaseOrders;
    }

    public List<PurchaseOrderLine> getPurchaseOrderLines() {
        if (purchaseOrderLines == null) {
            purchaseOrderLines = new ArrayList<>();
        }
        return purchaseOrderLines;
    }

    public void setPurchaseOrderLines(List<PurchaseOrderLine> purchaseOrderLines) {
        this.purchaseOrderLines = purchaseOrderLines;
    }

    public List<PurchaseOrderLine> getFilteredPurchaseOrderLines() {
        return filteredPurchaseOrderLines;
    }

    public void setFilteredPurchaseOrderLines(List<PurchaseOrderLine> filteredPurchaseOrderLines) {
        this.filteredPurchaseOrderLines = filteredPurchaseOrderLines;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

    public String getPurchaseLineId() {
        return purchaseLineId;
    }

    public void setPurchaseLineId(String purchaseLineId) {
        this.purchaseLineId = purchaseLineId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getPartialListType() {
        return partialListType;
    }

    public void setPartialListType(String partialListType) {
        this.partialListType = partialListType;
    }

    public String getInvoiceMethod() {
        return invoiceMethod;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Partner getSupplier() {
        return supplier;
    }

    public void setSupplier(Partner supplier) {
        this.supplier = supplier;
    }

    public void setInvoiceMethod(String invoiceMethod) {
        this.invoiceMethod = invoiceMethod;
    }

    public int getPurchaseOrderIndex() {
        if (purchaseOrders != null && purchaseOrder != null) {
            return purchaseOrders.indexOf(purchaseOrder) + 1;
        }
        return 0;
    }

    public void nextPurchaseOrder() {
        if (purchaseOrders.indexOf(purchaseOrder) == (purchaseOrders.size() - 1)) {
            purchaseOrder = purchaseOrders.get(0);
        } else {
            purchaseOrder = purchaseOrders.get(purchaseOrders.indexOf(purchaseOrder) + 1);
        }
    }

    public void previousPurchaseOrder() {
        if (purchaseOrders.indexOf(purchaseOrder) == 0) {
            purchaseOrder = purchaseOrders.get(purchaseOrders.size() - 1);
        } else {
            purchaseOrder = purchaseOrders.get(purchaseOrders.indexOf(purchaseOrder) - 1);
        }
    }

    public void showOrderLineList() {
        purchaseOrderLine = null;
        currentPage = "/sc/purchaseOrder/ListByProduct.xhtml";
    }

    public void prepareViewOrderByProduct() {
        if (purchaseOrderLine != null) {
            currentPage = "/sc/purchaseOrder/ViewByProduct.xhtml";
        } else {
            JsfUtil.addWarningMessage("ItemDoesNotExist");
        }
    }

    public int getOrderLineIndex() {
        if (purchaseOrderLines != null && purchaseOrderLine != null) {
            return purchaseOrderLines.indexOf(purchaseOrderLine) + 1;
        }
        return 0;
    }

    public void nextOrderLine() {
        if (purchaseOrderLines.indexOf(purchaseOrderLine) == (purchaseOrderLines.size() - 1)) {
            purchaseOrderLine = purchaseOrderLines.get(0);
        } else {
            purchaseOrderLine = purchaseOrderLines.get(purchaseOrderLines.indexOf(purchaseOrderLine) + 1);
        }
    }

    public void previousOrderLine() {
        if (purchaseOrderLines.indexOf(purchaseOrderLine) == 0) {
            purchaseOrderLine = purchaseOrderLines.get(purchaseOrderLines.size() - 1);
        } else {
            purchaseOrderLine = purchaseOrderLines.get(purchaseOrderLines.indexOf(purchaseOrderLine) - 1);
        }
    }

}
