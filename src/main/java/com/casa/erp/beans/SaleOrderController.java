package com.casa.erp.beans;

import com.casa.erp.beans.util.Countries;
import static com.casa.erp.beans.util.Countries.Version.SECOND;
import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.beans.util.Status;
import com.casa.erp.entities.DeliveryOrder;
import com.casa.erp.entities.DeliveryOrderLine;
import com.casa.erp.entities.Invoice;
import com.casa.erp.entities.InvoiceLine;
import com.casa.erp.entities.InvoiceTax;
import com.casa.erp.entities.Partner;
import com.casa.erp.entities.Product;
import com.casa.erp.entities.SaleOrder;
import com.casa.erp.entities.SaleOrderLine;
import com.casa.erp.dao.SaleOrderFacade;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.ResourceBundle;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
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
import org.primefaces.event.SelectEvent;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Named(value = "saleOrderController")
@ViewScoped
public class SaleOrderController implements Serializable {

    @Inject
    private SaleOrderFacade saleOrderFacade;
    @Inject
    @Status
    private HashMap<String, String> statuses;
    @Inject
    @Countries(version = SECOND)
    private HashMap<String, String> countries;
    private List<SaleOrder> saleOrders;
    private List<SaleOrder> filteredSaleOrders;
    private SaleOrder saleOrder;
    private List<SaleOrderLine> saleOrderLines;
    private List<SaleOrderLine> filteredSaleOrderLines;
    private SaleOrderLine saleOrderLine;
    private DeliveryOrder deliveryOrder;
    private List<DeliveryOrderLine> deliverOrderLines;
    private Invoice invoice;
    private List<InvoiceLine> invoiceLines;
    private String invoiceMethod;
    private String saleId;
    private String saleLineId;
    private String partnerId;
    private String productId;
    private String partialListType;
    private int uninvoicedlines;
    private int rowIndex;
    private List<Partner> topNCustomers;
    private Partner customer;
    private List<Product> topSoldNProducts;
    private Product product;
    private String currentPage = "/sc/saleOrder/List.xhtml";

    public String getCountry() {

        return countries.get(saleOrder.getPartner().getCountry());
    }

    public void setRowIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        rowIndex = Integer.valueOf(params.get("rowIndex"));
    }

    public void onSelectCustomer() {
        if ((customer != null) && (!topNCustomers.contains(customer))) {
            topNCustomers.add(customer);
        }
        saleOrder.setPartner(customer);
    }

    public void onSelectProduct() {

        if ((product != null)) {
            if (!topSoldNProducts.contains(product)) {
                topSoldNProducts.add(product);
            }

            if (rowIndex < 0) {

                saleOrderLine.setProduct(product);
                saleOrderLine.setPrice(product.getSalePrice());
                saleOrderLine.setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("SaleOrderForm:productMenuTwo");
                RequestContext.getCurrentInstance().update("SaleOrderForm:uom");
                RequestContext.getCurrentInstance().update("SaleOrderForm:price");

            } else {

                saleOrderLines.get(rowIndex).setProduct(product);
                saleOrderLines.get(rowIndex).setPrice(product.getSalePrice());
                saleOrderLines.get(rowIndex).setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("SaleOrderForm:datalist:" + rowIndex + ":productMenu");
                RequestContext.getCurrentInstance().update("SaleOrderForm:datalist:" + rowIndex + ":uomm");
                RequestContext.getCurrentInstance().update("SaleOrderForm:datalist:" + rowIndex + ":pricee");
            }

        }

    }

    public void updateOrder(Integer id) {
        if (getOrderStatus(id) != null) {
            if (!getOrderStatus(id).equals("Quotation")) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEditQuotation");
                currentPage = "/sc/saleOrder/View.xhtml";
            } else if (saleOrderLines.isEmpty()) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneSalesOrderLineUpdate");

            } else {

                for (SaleOrderLine OrderLine : saleOrderLines) {
                    OrderLine.setSaleOrder(saleOrder);
                }
                saleOrder.setSaleOrderLines(saleOrderLines);
                saleOrder = saleOrderFacade.update(saleOrder);

                if (partialListType == null && saleOrders != null) {
                    saleOrders.set(saleOrders.indexOf(saleOrder), saleOrder);
                } else {
                    saleOrders = saleOrderFacade.findAll();
                    partialListType = null;
                }
                currentPage = "/sc/saleOrder/View.xhtml";
            }
        }
    }

    public void createOrder() {
        if (saleOrderLines.isEmpty()) {
            JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneSalesOrderLineCreate");
        } else {

            for (SaleOrderLine OrderLine : saleOrderLines) {
                OrderLine.setSaleOrder(saleOrder);
            }

            saleOrder.setState("Quotation");
            saleOrder.setSaleOrderLines(saleOrderLines);
            saleOrder = saleOrderFacade.create(saleOrder);

            if (partialListType == null && saleOrders != null) {
                saleOrders.add(saleOrder);
            } else {
                saleOrders = saleOrderFacade.findAll();
                partialListType = null;
            }

            currentPage = "/sc/saleOrder/View.xhtml";
        }
    }

    public void deleteOrder(Integer id) {
        if (saleOrderExist(id)) {
            if (saleOrder.getState().equals("Cancelled")) {
                cancelRelations();
                try {
                    saleOrderFacade.remove(saleOrder);
                } catch (Exception e) {
                    System.out.println("Error Delete: " + e.getMessage());
                    JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                    return;
                }

                if (saleOrders.size() > 1) {
                    saleOrders.remove(saleOrder);
                } else {
                    saleOrders = saleOrderFacade.findAll();
                    partialListType = null;
                }

                saleOrder = null;
                JsfUtil.addSuccessMessage("ItemDeleted");
                currentPage = "/sc/saleOrder/List.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
            }
        }
    }

    private void cancelRelations() {
        if (!saleOrder.getInvoices().isEmpty()) {
            for (Invoice invoice : saleOrder.getInvoices()) {
                invoice.setSaleOrder(null);
                saleOrderFacade.update(invoice);
            }
            saleOrder.getInvoices().clear();
        }

        if (!saleOrder.getDeliveryOrders().isEmpty()) {
            for (DeliveryOrder deliveryOrder : saleOrder.getDeliveryOrders()) {
                deliveryOrder.setSaleOrder(null);
                saleOrderFacade.update(deliveryOrder);
            }
            saleOrder.getDeliveryOrders().clear();
        }
    }

    public void createDeliveryOrder(Integer id) {
        if (saleOrderExist(id)) {
            if (!saleOrder.getState().equals("Quotation") && !saleOrder.getState().equals("Cancelled") && saleOrder.getDeliveryOrders() != null && saleOrder.getDeliveryCreated() != true) {
                deliveryOrder = new DeliveryOrder(new Date(), saleOrder.getName(), "Draft", "Sale", Boolean.TRUE, "Complete", null, saleOrder.getPartner(), saleOrder);
                deliverOrderLines = new ArrayList<>();

                for (SaleOrderLine orderLine : saleOrder.getSaleOrderLines()) {

                    deliverOrderLines.add(new DeliveryOrderLine(
                            orderLine.getProduct(),
                            orderLine.getQuantity(),
                            0d,
                            orderLine.getUom(),
                            "New",
                            "Sale",
                            Boolean.TRUE,
                            saleOrder.getPartner(),
                            orderLine.getPrice(),
                            deliveryOrder));

                }

                deliveryOrder.setDeliveryOrderLines(deliverOrderLines);
                saleOrder.getDeliveryOrders().add(deliveryOrder);
                saleOrder.setDeliveryCreated(true);
                saleOrderFacade.create(deliveryOrder);
                saleOrder = saleOrderFacade.update(saleOrder);
                saleOrders.set(saleOrders.indexOf(saleOrder), saleOrder);

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }

    }

    private void refreshLinesToInvoice() {

        if (saleOrderLines != null && !saleOrderLines.isEmpty()) {
            List<SaleOrderLine> selectedLines = new ArrayList<>();
            selectedLines.addAll(saleOrderLines);
            prepareInvoicing();

            ListIterator<SaleOrderLine> iterator = saleOrderLines.listIterator();

            while (iterator.hasNext()) {
                SaleOrderLine saleLine = iterator.next();
                boolean exist = false;
                for (SaleOrderLine line : selectedLines) {
                    if (saleLine.getId() == line.getId()) {
                        exist = true;
                    }
                }

                if (exist == false) {
                    iterator.remove();
                }
            }

            for (SaleOrderLine line : saleOrderLines) {
                System.out.println("id: " + line.getId());
            }
        }
    }

    public void createInvoice(Integer id, boolean redirect) throws IOException {

        if (saleOrderExist(id)) {
            if (saleOrder.getState().equals("To Invoice") && saleOrder.getInvoiceMethod().equals("Partial")) {
                refreshLinesToInvoice();
                if (saleOrderLines != null && !saleOrderLines.isEmpty()) {
                    invoice = new Invoice(new Date(), "Sale", saleOrder.getName(), "Draft", Boolean.TRUE, saleOrder.getPartner(), saleOrder, saleOrderFacade.findAccount("Account Receivable"), saleOrderFacade.findJournal("INV"));
                    SumUpInvoice();
                    invoiceLines = new ArrayList<>();
                    for (SaleOrderLine lineToInvoice : saleOrderLines) {

                        for (SaleOrderLine orderLine : saleOrder.getSaleOrderLines()) {

                            if (lineToInvoice.getId() == orderLine.getId()) {

                                orderLine.setInvoiced(Boolean.TRUE);

                                invoiceLines.add(new InvoiceLine(
                                        new Date(),
                                        orderLine.getUom(),
                                        orderLine.getPrice(),
                                        orderLine.getSubTotal(),
                                        orderLine.getDiscount(),
                                        orderLine.getQuantity(),
                                        Boolean.TRUE,
                                        invoice,
                                        saleOrder.getPartner(),
                                        orderLine.getProduct(),
                                        orderLine.getTax(),
                                        saleOrderFacade.findAccount("Product Sales")));
                            }
                        }
                    }

                    if (saleOrderLines.size() == uninvoicedlines) {
                        saleOrder.setInvoiceMethod("Complete");
                        saleOrder.setState("Fully Invoiced");
                    }

                    //account_id not null in invoice line
                    invoice.setInvoiceLines(invoiceLines);
                    generateInvoiceTaxes();
                    saleOrder.getInvoices().add(invoice);
                    Integer InvId = saleOrderFacade.create(invoice).getId();
                    saleOrder = saleOrderFacade.update(saleOrder);
                    saleOrders.set(saleOrders.indexOf(saleOrder), saleOrder);

                    if (redirect) {
                        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                        context.redirect(context.getRequestContextPath() + "/sc/invoice/index.xhtml?id=" + InvId);
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

        for (SaleOrderLine orderLine : saleOrderLines) {
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
                        saleOrderFacade.findAccount("Tax Received"),
                        invoice,
                        invoiceline.getTax()));
            }
        }

        invoice.setInvoiceTaxes(invoiceTaxes);
    }

    public void prepareInvoicing() {

        saleOrderLines = new ArrayList<>();
        uninvoicedlines = 0;
        for (SaleOrderLine saleLine : saleOrder.getSaleOrderLines()) {
            if (!saleLine.getInvoiced()) {
                uninvoicedlines++;
                saleOrderLines.add(saleLine);
            }
        }
    }

    public void prepareInvoicing(Integer id) {
        if (saleOrderExist(id)) {
            if (saleOrder.getState().equals("To Invoice") && saleOrder.getInvoiceMethod().equals("Partial")) {

                saleOrderLines = new ArrayList<>();
                invoiceMethod = "Complete";
                uninvoicedlines = 0;
                for (SaleOrderLine saleLine : saleOrder.getSaleOrderLines()) {
                    if (!saleLine.getInvoiced()) {
                        uninvoicedlines++;
                        saleOrderLines.add(saleLine);
                    }
                }
            } else {
                FacesContext.getCurrentInstance().validationFailed();
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    public void updateInvoiceMethod() {

        saleOrderLines = new ArrayList<>();
        for (SaleOrderLine saleLine : saleOrder.getSaleOrderLines()) {
            if (!saleLine.getInvoiced()) {
                saleOrderLines.add(saleLine);
            }
        }
    }

    public void removeLineToInvoice(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < saleOrderLines.size()) {
            saleOrderLines.remove(rowIndex);
        }
    }

    public void confirmSale(Integer id) {
        if (saleOrderExist(id)) {
            if (saleOrder.getState().equals("Quotation")) {
                saleOrder.setState("To Invoice");
                saleOrder = saleOrderFacade.update(saleOrder);
                saleOrders.set(saleOrders.indexOf(saleOrder), saleOrder);
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    public void cancelOrder(Integer id) {
        if (saleOrderExist(id)) {
            if ((!saleOrder.getState().equals("Cancelled")) && (!saleOrder.getState().equals("Done"))) {
                boolean canBeCancelled = true;

                if (!saleOrder.getDeliveryOrders().isEmpty()) {
                    if (!isDeliveryCancelled()) {
                        canBeCancelled = false;
                        JsfUtil.addWarningMessageDialog("CannotCancelSalesOrder", "CannotCancelSalesOrder_cancelDelivery");

                    } else if (!saleOrder.getInvoices().isEmpty()) {
                        if (!isInvoiceCancelled()) {
                            canBeCancelled = false;
                            JsfUtil.addWarningMessageDialog("CannotCancelSalesOrder", "CannotCancelSalesOrder_cancelInvoice");
                        }
                    }

                } else if (!saleOrder.getInvoices().isEmpty()) {
                    if (!isInvoiceCancelled()) {
                        canBeCancelled = false;
                        JsfUtil.addWarningMessageDialog("CannotCancelSalesOrder", "CannotCancelSalesOrder_cancelInvoice");
                    }
                }

                if (canBeCancelled == true) {
                    saleOrder.setState("Cancelled");
                    saleOrder = saleOrderFacade.update(saleOrder);
                    saleOrders.set(saleOrders.indexOf(saleOrder), saleOrder);
                }
            } else if (saleOrder.getState().equals("Done")) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorCancelRecordDone");

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyCancelled");
            }
        }
    }

    private boolean isDeliveryCancelled() {

        for (DeliveryOrder delivery : saleOrder.getDeliveryOrders()) {
            if (!(delivery.getState().equals("Cancelled"))) {
                return false;
            }
        }
        return true;
    }

    private boolean isInvoiceCancelled() {

        for (Invoice invoice : saleOrder.getInvoices()) {
            if (!(invoice.getState().equals("Cancelled"))) {
                return false;
            }
        }
        return true;
    }

    public void showForm() {
        if (saleOrders != null && saleOrders.size() > 0) {
            saleOrder = saleOrders.get(0);
            currentPage = "/sc/saleOrder/View.xhtml";
        }
    }

    public void showForm(Integer id) {
        if (id != null) {
            saleOrder = saleOrderFacade.find(id);
            if (saleOrder != null) {
                partialListType = null;
                saleOrderLines = null;
                saleOrderLine = null;
                saleOrders = saleOrderFacade.findAll();
                currentPage = "/sc/saleOrder/View.xhtml";
            } else {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
            }
        }
    }

    public void showOrderLineForm() {
        if (saleOrderLines != null && saleOrderLines.size() > 0) {
            saleOrderLine = saleOrderLines.get(0);
            currentPage = "/sc/saleOrder/ViewByProduct.xhtml";
        }
    }

    public void showOrderList() {
        saleOrder = null;
        saleOrderLine = null;
        saleOrderLines = null;
        topNCustomers = null;
        topSoldNProducts = null;
        currentPage = "/sc/saleOrder/List.xhtml";
    }

    public void prepareCreate() {
        saleOrder = new SaleOrder();
        saleOrder.setDate(new Date());
        saleOrderLines = new ArrayList<>();
        saleOrderLine = new SaleOrderLine();
        topNCustomers = saleOrderFacade.findTopNCustomers(4);
        topSoldNProducts = saleOrderFacade.findTopNSoldProducts(4);
        if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
            saleOrderLine.setProduct(topSoldNProducts.get(0));
            saleOrderLine.setPrice(saleOrderLine.getProduct().getSalePrice());
            saleOrderLine.setUom(saleOrderLine.getProduct().getUom().getName());
        }
        currentPage = "/sc/saleOrder/Create.xhtml";
    }

    public void prepareEdit(Integer id) {
        if (saleOrderExist(id)) {
            if (saleOrder.getState().equals("Quotation")) {
                saleOrderLine = new SaleOrderLine();
                saleOrderLines = saleOrder.getSaleOrderLines();
                topNCustomers = saleOrderFacade.findTopNCustomers(4);
                topSoldNProducts = saleOrderFacade.findTopNSoldProducts(4);
                if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
                    saleOrderLine.setProduct(topSoldNProducts.get(0));
                    saleOrderLine.setPrice(saleOrderLine.getProduct().getSalePrice());
                    saleOrderLine.setUom(saleOrderLine.getProduct().getUom().getName());
                }

                if (!topNCustomers.contains(saleOrder.getPartner())) {
                    topNCustomers.add(saleOrder.getPartner());
                }

                for (SaleOrderLine orderLine : saleOrderLines) {
                    if (!topSoldNProducts.contains(orderLine.getProduct())) {
                        topSoldNProducts.add(orderLine.getProduct());
                    }
                }

                currentPage = "/sc/saleOrder/Edit.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEditQuotation");
            }
        }
    }

    public void prepareView() {
        if (saleOrder != null) {
            if (saleOrderExist(saleOrder.getId())) {
                currentPage = "/sc/saleOrder/View.xhtml";
            }
        }
    }

    public void cancelEdit(Integer id) {
        if (saleOrderExist(id)) {
            currentPage = "/sc/saleOrder/View.xhtml";
        }
    }

    public void viewSaleOrder() {

        if (JsfUtil.isNumeric(saleId)) {
            Integer id = Integer.valueOf(saleId);
            saleOrder = saleOrderFacade.find(id);

            if (saleOrder != null) {
                saleOrders = saleOrderFacade.findAll();
                currentPage = "/sc/saleOrder/View.xhtml";
                return;
            }
        }

        if (JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            saleOrders = saleOrderFacade.findByPartner(id);
            if (!saleOrders.isEmpty()) {
                currentPage = "/sc/saleOrder/List.xhtml";
                partialListType = "partner";
                return;
            }
        }

        if (JsfUtil.isNumeric(productId)) {
            Integer id = Integer.valueOf(productId);
            saleOrderLines = saleOrderFacade.findByProduct(id);
            if (!saleOrderLines.isEmpty()) {
                currentPage = "/sc/saleOrder/ListByProduct.xhtml";
                return;
            }
        }

        if (JsfUtil.isNumeric(saleLineId)) {
            Integer id = Integer.valueOf(saleLineId);
            saleOrderLine = saleOrderFacade.findOrderLine(id);
            if (saleOrderLine != null) {
                saleOrderLines = saleOrderFacade.findByProduct(saleOrderLine.getProduct().getId());
                productId = Integer.toString(saleOrderLine.getProduct().getId());
                currentPage = "/sc/saleOrder/ViewByProduct.xhtml";
                return;
            }
        }
        saleOrders = saleOrderFacade.findAll();
        currentPage = "/sc/saleOrder/List.xhtml";
    }

    public void duplicateSaleOrder(Integer id) {
        if (saleOrderExist(id)) {

            saleOrder.getSaleOrderLines().size();

            SaleOrder newSaleOrder = (SaleOrder) SerializationUtils.clone(saleOrder);

            newSaleOrder.setState("Quotation");
            newSaleOrder.setDeliveryOrders(null);
            newSaleOrder.setInvoices(null);
            newSaleOrder.setInvoiceMethod("Partial");
            newSaleOrder.setDate(new Date());
            newSaleOrder.setId(null);
            newSaleOrder.setPaid(Boolean.FALSE);
            newSaleOrder.setShipped(Boolean.FALSE);
            newSaleOrder.setDeliveryCreated(Boolean.FALSE);
            newSaleOrder.setNotes(null);
            newSaleOrder.setName(null);
            newSaleOrder.setActive(Boolean.TRUE);
            //newSaleOrder.setDiscount(null);

            for (SaleOrderLine orderLine : newSaleOrder.getSaleOrderLines()) {
                orderLine.setId(null);
                orderLine.setInvoiced(Boolean.FALSE);
                orderLine.setSaleOrder(newSaleOrder);
            }

            saleOrder = newSaleOrder;
            saleOrderLine = new SaleOrderLine();
            saleOrderLines = saleOrder.getSaleOrderLines();
            topNCustomers = saleOrderFacade.findTopNCustomers(4);
            topSoldNProducts = saleOrderFacade.findTopNSoldProducts(4);
            if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
                saleOrderLine.setProduct(topSoldNProducts.get(0));
                saleOrderLine.setPrice(saleOrderLine.getProduct().getSalePrice());
                saleOrderLine.setUom(saleOrderLine.getProduct().getUom().getName());
            }

            if (!topNCustomers.contains(saleOrder.getPartner())) {
                topNCustomers.add(saleOrder.getPartner());
            }

            for (SaleOrderLine orderLine : saleOrderLines) {
                if (!topSoldNProducts.contains(orderLine.getProduct())) {
                    topSoldNProducts.add(orderLine.getProduct());
                }
            }
            currentPage = "/sc/saleOrder/Create.xhtml";
        }
    }

    public void printOrder(ActionEvent actionEvent) throws IOException, JRException {

        for (SaleOrderLine orderLine : saleOrder.getSaleOrderLines()) {

            orderLine.setProductName(orderLine.getProduct().getName());
            if (orderLine.getTax() != null) {
                orderLine.setTaxName(orderLine.getTax().getName());
            } else {
                orderLine.setTaxName("");
            }
        }

        saleOrder.getPartner().setCountry(getCountry());

        ResourceBundle bundle = JsfUtil.getBundle();
        String name = bundle.getString("SaleOrder");
        String currency = bundle.getString("Currency");

        Map<String, Object> params = new HashMap<>();
        params.put("saleOrder", saleOrder);
        params.put("partner", saleOrder.getPartner());
        params.put("orderLines", saleOrder.getSaleOrderLines());
        params.put("currency", currency);
        params.put("SUBREPORT_DIR", FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/")+"/");
        

        String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/saleOrder.jasper");

        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, params, new JREmptyDataSource());
//        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, new HashMap<String,Object>(), new JRBeanArrayDataSource(new SaleOrder[]{saleOrder}));  
        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        httpServletResponse.addHeader("Content-disposition", "attachment; filename=" + name + "_" + saleOrder.getName() + ".pdf");
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
        FacesContext.getCurrentInstance().responseComplete();

    }

    public String getStatus(String status) {
        return statuses.get(status);
    }

    public String getStatusColor(String status) {
        switch (status) {
            case "Quotation":
                return "#009fd4";
            case "Fully Invoiced":
                return "#00a4a6";
            case "To Invoice":
                return "#406098";
            case "Done":
                return "#3477db";
            default:
                return "#6d8891";
        }
    }

    private boolean saleOrderExist(Integer id) {
        if (id != null) {
            saleOrder = saleOrderFacade.find(id);
            if (saleOrder == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                saleOrders = null;
                partialListType = null;
                currentPage = "/sc/saleOrder/List.xhtml";
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
            SaleOrder saleOrder = saleOrderFacade.find(id);
            if (saleOrder != null) {
                return saleOrder.getState();
            } else {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                saleOrders = null;
                partialListType = null;
                currentPage = "/sc/saleOrder/List.xhtml";
                return null;
            }
        }
        return null;
    }

    public Double getLineTax() {
        if (saleOrderLine != null && saleOrderLine.getTax() != null) {
            return JsfUtil.round(saleOrderLine.getQuantity() * saleOrderLine.getPrice() * saleOrderLine.getTax().getAmount());
        } else {
            return 0d;
        }
    }

    public Double getLineTotal() {
        return getLineTax() + saleOrderLine.getSubTotal();
    }

    public String getPage() {
        return currentPage;
    }

    public void setPage(String page) {
        this.currentPage = page;
    }

    public SaleOrder getSaleOrder() {
        if (saleOrder == null) {
            return saleOrder = new SaleOrder();
        }
        return saleOrder;
    }

    public void setSaleOrder(SaleOrder saleOrder) {
        this.saleOrder = saleOrder;
    }

    public SaleOrderLine getSaleOrderLine() {
        if (saleOrderLine == null) {
            saleOrderLine = new SaleOrderLine();
        }
        return saleOrderLine;
    }

    public void setSaleOrderLine(SaleOrderLine saleOrderLine) {
        this.saleOrderLine = saleOrderLine;
    }

    public List<SaleOrder> getSaleOrders() {
        if (saleOrders == null) {
            saleOrders = saleOrderFacade.findAll();
        }
        return saleOrders;
    }

    public void setSaleOrders(List<SaleOrder> saleOrders) {
        this.saleOrders = saleOrders;
    }

    public List<SaleOrder> getFilteredSaleOrders() {
        return filteredSaleOrders;
    }

    public void setFilteredSaleOrders(List<SaleOrder> filteredSaleOrders) {
        this.filteredSaleOrders = filteredSaleOrders;
    }

    public List<SaleOrderLine> getSaleOrderLines() {
        if (saleOrderLines == null) {
            saleOrderLines = new ArrayList<>();
        }
        return saleOrderLines;
    }

    public void setSaleOrderLines(List<SaleOrderLine> saleOrderLines) {
        this.saleOrderLines = saleOrderLines;
    }

    public List<SaleOrderLine> getFilteredSaleOrderLines() {
        return filteredSaleOrderLines;
    }

    public void setFilteredSaleOrderLines(List<SaleOrderLine> filteredSaleOrderLines) {
        this.filteredSaleOrderLines = filteredSaleOrderLines;
    }

    public List<Partner> getTopNCustomers() {
        if (topNCustomers == null) {
            topNCustomers = saleOrderFacade.findTopNCustomers(4);
        }
        return topNCustomers;

    }

    public List<Product> getTopSoldNProducts() {
        if (topSoldNProducts == null) {
            topSoldNProducts = saleOrderFacade.findTopNSoldProducts(4);
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

    public String getSaleId() {
        return saleId;
    }

    public void setSaleId(String saleId) {
        this.saleId = saleId;
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

    public String getSaleLineId() {
        return saleLineId;
    }

    public void setSaleLineId(String saleLineId) {
        this.saleLineId = saleLineId;
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

    public void setInvoiceMethod(String invoiceMethod) {
        this.invoiceMethod = invoiceMethod;
    }

    public void onRowAdd(ActionEvent event) {

        saleOrderLine.setActive(Boolean.TRUE);
        saleOrderLine.setInvoiced(Boolean.FALSE);
        saleOrderLine.setPrice(JsfUtil.round(saleOrderLine.getPrice()));
        saleOrderLine.setDiscount(JsfUtil.round(saleOrderLine.getDiscount()));
        saleOrderLine.setQuantity(JsfUtil.round(saleOrderLine.getQuantity(), saleOrderLine.getProduct().getUom().getDecimals()));

        if (saleOrderLine.getQuantity() == 0d) {
            saleOrderLine.setQuantity(1d);
        }

        if (saleOrderLine.getPrice() == 0d) {
            saleOrderLine.setDiscount(0d);
            saleOrderLine.setTax(null);
        }

        if (saleOrderLine.getDiscount() > 0d) {

            double total = JsfUtil.round((saleOrderLine.getPrice()) * (saleOrderLine.getQuantity()));
            double discount = JsfUtil.round(saleOrderLine.getPrice() * saleOrderLine.getQuantity() * saleOrderLine.getDiscount() / 100);

            saleOrderLine.setSubTotal(JsfUtil.round(total - discount));

        } else {

            saleOrderLine.setSubTotal(JsfUtil.round((saleOrderLine.getPrice()) * (saleOrderLine.getQuantity())));
        }

        System.out.println(saleOrderLine.getQuantity() > saleOrderLine.getProduct().getInventory().getQuantityAvailable());
        if (Double.compare(saleOrderLine.getQuantity(), saleOrderLine.getProduct().getInventory().getQuantityAvailable()) > 0) {
            Double quantity = saleOrderLine.getQuantity();
            String unitOfMesure = saleOrderLine.getUom();
            Double availableQuantity = saleOrderLine.getProduct().getInventory().getQuantityAvailable();
            Double quantityOnHandy = saleOrderLine.getProduct().getInventory().getQuantityOnHand();

            ResourceBundle bundle = JsfUtil.getBundle();
            String header = bundle.getString("NotEnoughStock");
            String msg = bundle.getString("NotEnoughStockMessage");
            msg = MessageFormat.format(msg, quantity, unitOfMesure, availableQuantity, unitOfMesure, quantityOnHandy, unitOfMesure);
            JsfUtil.addWarningCustomMessageDialog(header, msg);

        }

        saleOrderLines.add(saleOrderLine);
        SumUpOrder();
        saleOrderLine = new SaleOrderLine();
        if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
            saleOrderLine.setProduct(topSoldNProducts.get(0));
            saleOrderLine.setPrice(topSoldNProducts.get(0).getSalePrice());
            saleOrderLine.setUom(topSoldNProducts.get(0).getUom().getName());
        }

    }

    public void onRowDelete(int Index) {
        if (Index >= 0 && Index < saleOrderLines.size()) {
            saleOrderLines.remove(Index);
            SumUpOrder();
        }
    }

    public void onRowEditInit(SaleOrderLine orderLine) {
        saleOrderLine = (SaleOrderLine) SerializationUtils.clone(orderLine);
    }

    public void onRowEdit(int index) {

        saleOrderLines.get(index).setQuantity(JsfUtil.round(saleOrderLines.get(index).getQuantity(), saleOrderLines.get(index).getProduct().getUom().getDecimals()));
        saleOrderLines.get(index).setPrice(JsfUtil.round(saleOrderLines.get(index).getPrice()));
        saleOrderLines.get(index).setDiscount(JsfUtil.round(saleOrderLines.get(index).getDiscount()));

        if (saleOrderLines.get(index).getQuantity() == 0d) {
            saleOrderLines.get(index).setQuantity(1d);
        }

        if (saleOrderLines.get(index).getPrice() == 0d) {
            saleOrderLines.get(index).setDiscount(0d);
            saleOrderLines.get(index).setTax(null);
        }

        if (saleOrderLines.get(index).getDiscount() > 0d) {

            double total = JsfUtil.round(saleOrderLines.get(index).getPrice() * saleOrderLines.get(index).getQuantity());
            double discount = JsfUtil.round(saleOrderLines.get(index).getPrice() * saleOrderLines.get(index).getQuantity() * saleOrderLines.get(index).getDiscount() / 100);
            saleOrderLines.get(index).setSubTotal(JsfUtil.round(total - discount));

        } else {

            saleOrderLines.get(index).setSubTotal(JsfUtil.round(saleOrderLines.get(index).getPrice() * saleOrderLines.get(index).getQuantity()));
        }

        if (Double.compare(saleOrderLines.get(index).getQuantity(), saleOrderLines.get(index).getProduct().getInventory().getQuantityAvailable()) > 0) {
            Double quantity = saleOrderLines.get(index).getQuantity();
            String unitOfMesure = saleOrderLines.get(index).getUom();
            Double availableQuantity = saleOrderLines.get(index).getProduct().getInventory().getQuantityAvailable();
            Double quantityOnHandy = saleOrderLines.get(index).getProduct().getInventory().getQuantityOnHand();

            ResourceBundle bundle = JsfUtil.getBundle();
            String header = bundle.getString("NotEnoughStock");
            String msg = bundle.getString("NotEnoughStockMessage");
            msg = MessageFormat.format(msg, quantity, unitOfMesure, availableQuantity, unitOfMesure, quantityOnHandy, unitOfMesure);
            JsfUtil.addWarningCustomMessageDialog(header, msg);
        }

        SumUpOrder();
    }

    public void onRowCancel(int index) {
        saleOrderLines.remove(index);
        saleOrderLines.add(index, saleOrderLine);
        saleOrderLine = new SaleOrderLine();
        if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
            saleOrderLine.setProduct(topSoldNProducts.get(0));
            saleOrderLine.setPrice(saleOrderLine.getProduct().getSalePrice());
            saleOrderLine.setUom(saleOrderLine.getProduct().getUom().getName());
        }
    }

    public void onRowCancel() {
        saleOrderLine = new SaleOrderLine();
        if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
            saleOrderLine.setProduct(topSoldNProducts.get(0));
            saleOrderLine.setPrice(saleOrderLine.getProduct().getSalePrice());
            saleOrderLine.setUom(saleOrderLine.getProduct().getUom().getName());
        }
    }

    private void SumUpOrder() {

        saleOrder.setAmountUntaxed(0d);
        saleOrder.setAmountTax(0d);
        saleOrder.setAmountTotal(0d);

        for (SaleOrderLine orderLine : saleOrderLines) {
            saleOrder.setAmountUntaxed(saleOrder.getAmountUntaxed() + orderLine.getSubTotal());

            if (orderLine.getTax() != null) {
                saleOrder.setAmountTax(saleOrder.getAmountTax() + (orderLine.getSubTotal() * orderLine.getTax().getAmount()));
            }
        }

        saleOrder.setAmountUntaxed(JsfUtil.round(saleOrder.getAmountUntaxed()));
        saleOrder.setAmountTax(JsfUtil.round(saleOrder.getAmountTax()));
        BigDecimal amountUntaxed = BigDecimal.valueOf(saleOrder.getAmountUntaxed());
        BigDecimal amountTax = BigDecimal.valueOf(saleOrder.getAmountTax());
        BigDecimal amountTotal = amountUntaxed.add(amountTax);
        
        System.out.println("amountUntaxed: "+amountUntaxed);
        System.out.println("amountTax: "+amountTax);
        System.out.println("amountTotal: "+amountTotal);
        saleOrder.setAmountTotal(JsfUtil.round(amountTotal.doubleValue()));
    }

    public void onProductChange() {

        saleOrderLine.setPrice(saleOrderLine.getProduct().getSalePrice());
        saleOrderLine.setUom(saleOrderLine.getProduct().getUom().getName());
    }

    public void onProductChange(int rowIndex) {

        saleOrderLines.get(rowIndex).setPrice(saleOrderLines.get(rowIndex).getProduct().getSalePrice());
        saleOrderLines.get(rowIndex).setUom(saleOrderLines.get(rowIndex).getProduct().getUom().getName());
    }

    public int getSaleOrderIndex() {
        if (saleOrders != null && saleOrder != null) {
            return saleOrders.indexOf(saleOrder) + 1;
        }
        return 0;
    }

    public void nextSaleOrder() {
        if (saleOrders.indexOf(saleOrder) == (saleOrders.size() - 1)) {
            saleOrder = saleOrders.get(0);
        } else {
            saleOrder = saleOrders.get(saleOrders.indexOf(saleOrder) + 1);
        }
    }

    public void previousSaleOrder() {
        if (saleOrders.indexOf(saleOrder) == 0) {
            saleOrder = saleOrders.get(saleOrders.size() - 1);
        } else {
            saleOrder = saleOrders.get(saleOrders.indexOf(saleOrder) - 1);
        }
    }

    public void showOrderLineList() {
        saleOrderLine = null;
        currentPage = "/sc/saleOrder/ListByProduct.xhtml";
    }

    public void prepareViewOrderByProduct() {
        if (saleOrderLine != null) {
            currentPage = "/sc/saleOrder/ViewByProduct.xhtml";
        } else {
            JsfUtil.addWarningMessage("ItemDoesNotExist");
        }
    }

    public int getOrderLineIndex() {
        if (saleOrderLines != null && saleOrderLine != null) {
            return saleOrderLines.indexOf(saleOrderLine) + 1;
        }
        return 0;
    }

    public void nextOrderLine() {
        if (saleOrderLines.indexOf(saleOrderLine) == (saleOrderLines.size() - 1)) {
            saleOrderLine = saleOrderLines.get(0);
        } else {
            saleOrderLine = saleOrderLines.get(saleOrderLines.indexOf(saleOrderLine) + 1);
        }
    }

    public void previousOrderLine() {
        if (saleOrderLines.indexOf(saleOrderLine) == 0) {
            saleOrderLine = saleOrderLines.get(saleOrderLines.size() - 1);
        } else {
            saleOrderLine = saleOrderLines.get(saleOrderLines.indexOf(saleOrderLine) - 1);
        }
    }
}
