package com.defterp.modules.sales.controllers;

import com.defterp.modules.accounting.entities.Account;
import com.defterp.translation.annotations.Countries;
import static com.defterp.translation.annotations.Countries.Version.SECOND;
import com.defterp.util.JsfUtil;
import com.defterp.translation.annotations.Status;
import com.defterp.modules.inventory.entities.DeliveryOrder;
import com.defterp.modules.inventory.entities.DeliveryOrderLine;
import com.defterp.modules.accounting.entities.Invoice;
import com.defterp.modules.accounting.entities.InvoiceLine;
import com.defterp.modules.accounting.entities.InvoiceTax;
import com.defterp.modules.accounting.entities.Journal;
import com.defterp.modules.accounting.queryBuilders.AccountQueryBuilder;
import com.defterp.modules.accounting.queryBuilders.JournalQueryBuilder;
import com.defterp.modules.partners.entities.Partner;
import com.defterp.modules.inventory.entities.Product;
import com.defterp.modules.sales.entities.SaleOrder;
import com.defterp.modules.sales.entities.SaleOrderLine;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.modules.inventory.queryBuilders.ProductQueryBuilder;
import com.defterp.modules.partners.queryBuilders.PartnerQueryBuilder;
import com.defterp.modules.sales.queryBuilders.SaleOrderLineQueryBuilder;
import com.defterp.modules.sales.queryBuilders.SaleOrderQueryBuilder;
import com.defterp.modules.commonClasses.IdGenerator;
import java.io.IOException;
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
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
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

@Named(value = "saleOrderController")
@ViewScoped
public class SaleOrderController extends AbstractController {

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
    private List<Partner> topNActiveCustomers;
    private List<Partner> activeCustomers;
    private List<Partner> filteredActiveCustomers;
    private List<Product> topNActiveSoldProducts;
    private List<Product> activeSoldProducts;
    private List<Product> filteredActiveSoldProducts;
    private Partner customer;
    private Product product;
    private String currentForm = "/sc/saleOrder/View.xhtml";
    private String currentList = "/sc/saleOrder/List.xhtml";
    private QueryWrapper query;

    public SaleOrderController() {
        super("/sc/saleOrder/");
    }

    public String getCountry() {

        return countries.get(saleOrder.getPartner().getCountry());
    }

    public void setRowIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        rowIndex = Integer.valueOf(params.get("rowIndex"));
    }

    public void onSelectCustomer() {
        if ((customer != null) && (!topNActiveCustomers.contains(customer))) {
            topNActiveCustomers.add(customer);
        }
        saleOrder.setPartner(customer);
    }

    public void onSelectProduct() {

        if ((product != null)) {
            if (!topNActiveSoldProducts.contains(product)) {
                topNActiveSoldProducts.add(product);
            }

            if (rowIndex < 0) {

                saleOrderLine.setProduct(product);
                saleOrderLine.setPrice(product.getSalePrice());
                saleOrderLine.setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("mainForm:productMenuTwo");
                RequestContext.getCurrentInstance().update("mainForm:uom");
                RequestContext.getCurrentInstance().update("mainForm:price");

            } else {

                saleOrderLines.get(rowIndex).setProduct(product);
                saleOrderLines.get(rowIndex).setPrice(product.getSalePrice());
                saleOrderLines.get(rowIndex).setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("mainForm:datalist:" + rowIndex + ":productMenu");
                RequestContext.getCurrentInstance().update("mainForm:datalist:" + rowIndex + ":uomm");
                RequestContext.getCurrentInstance().update("mainForm:datalist:" + rowIndex + ":pricee");
            }
        }
    }

    public void updateOrder() {
        if (getOrderStatus(saleOrder.getId()) != null) {
            if (!getOrderStatus(saleOrder.getId()).equals("Quotation")) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEditQuotation");
                currentForm = "/sc/saleOrder/View.xhtml";
            } else if (saleOrderLines.isEmpty()) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneSalesOrderLineUpdate");

            } else {

                for (SaleOrderLine OrderLine : saleOrderLines) {
                    OrderLine.setSaleOrder(saleOrder);
                }
                saleOrder.setSaleOrderLines(saleOrderLines);
                saleOrder = super.updateItem(saleOrder);

                if (partialListType == null && saleOrders != null) {
                    saleOrders.set(saleOrders.indexOf(saleOrder), saleOrder);
                } else {
                    query = SaleOrderQueryBuilder.getFindAllQuery();
                    saleOrders = super.findWithQuery(query);
                    partialListType = null;
                }
                currentForm = "/sc/saleOrder/View.xhtml";
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

            saleOrder = super.createItem(saleOrder);
            saleOrder.setName(IdGenerator.generateSaleId(saleOrder.getId()));
            saleOrder = super.updateItem(saleOrder);

            if (partialListType == null && saleOrders != null) {
                saleOrders.add(saleOrder);
            } else {
                query = SaleOrderQueryBuilder.getFindAllQuery();
                saleOrders = super.findWithQuery(query);
                partialListType = null;
            }

            currentForm = "/sc/saleOrder/View.xhtml";
        }
    }

    public void deleteOrder(Integer id) {
        if (saleOrderExist(id)) {
            if (saleOrder.getState().equals("Cancelled")) {
                cancelRelations();

                boolean deleted = super.deleteItem(saleOrder);

                if (deleted) {

                    JsfUtil.addSuccessMessage("ItemDeleted");
                    currentForm = VIEW_URL;

                    if (saleOrders != null && saleOrders.size() > 1) {
                        saleOrders.remove(saleOrder);
                        saleOrder = saleOrders.get(0);
                    } else {
                        partialListType = null;
                        query = SaleOrderQueryBuilder.getFindAllQuery();
                        saleOrders = super.findWithQuery(query);

                        if (saleOrders != null && !saleOrders.isEmpty()) {
                            saleOrder = saleOrders.get(0);
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
        if (!saleOrder.getInvoices().isEmpty()) {
            for (Invoice invoice : saleOrder.getInvoices()) {
                invoice.setSaleOrder(null);
                super.updateItem(invoice);
            }
            saleOrder.getInvoices().clear();
        }

        if (!saleOrder.getDeliveryOrders().isEmpty()) {
            for (DeliveryOrder deliveryOrder : saleOrder.getDeliveryOrders()) {
                deliveryOrder.setSaleOrder(null);
                super.updateItem(deliveryOrder);
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

                deliveryOrder = super.createItem(deliveryOrder);
                deliveryOrder.setName(IdGenerator.generateDeliveryOutId(deliveryOrder.getId()));
                super.updateItem(deliveryOrder);

                saleOrder = super.updateItem(saleOrder);
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
                    invoice = new Invoice(
                            new Date(),
                            "Sale",
                            saleOrder.getName(),
                            "Draft",
                            Boolean.TRUE,
                            saleOrder.getPartner(),
                            saleOrder,
                            (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Account Receivable")),
                            (Journal) super.findSingleWithQuery(JournalQueryBuilder.getFindByCodeQuery("INV")));

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
                                        (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Product Sales"))));
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

                    invoice = super.createItem(invoice);
                    invoice.setName(IdGenerator.generateInvoiceId(invoice.getId()));
                    invoice = super.updateItem(invoice);

                    saleOrder = super.updateItem(saleOrder);
                    saleOrders.set(saleOrders.indexOf(saleOrder), saleOrder);

                    if (redirect) {
                        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
                        context.redirect(context.getRequestContextPath() + "/sc/invoice/index.xhtml?id=" + invoice.getId());
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
//                invoiceTaxes.add(new InvoiceTax(
//                        invoice.getDate(),
//                        taxAmount,
//                        invoiceline.getPriceSubtotal(),
//                        Boolean.TRUE,
//                        saleOrderFacade.findAccount("Tax Received"),
//                        invoice,
//                        invoiceline.getTax()));
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

    public void confirmSale(int id) {
        System.out.println("----------inside confirmSale()--------");
        if (saleOrderExist(id)) {
            if (saleOrder.getState().equals("Quotation")) {
                System.out.println("----------is Quotation--------");
                saleOrder.setState("To Invoice");
                saleOrder = super.updateItem(saleOrder);
                saleOrders.set(saleOrders.indexOf(saleOrder), saleOrder);
            } else {
                System.out.println("----------already modified--------");
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
                    saleOrder = super.updateItem(saleOrder);
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

    public void cancelCreate() {

        saleOrderLine = null;
        saleOrderLines = null;
        topNActiveCustomers = null;
        topNActiveSoldProducts = null;

        if (saleOrders != null && !saleOrders.isEmpty()) {
            saleOrder = saleOrders.get(0);
            currentForm = "/sc/saleOrder/View.xhtml";
        }
    }

    public void cancelEdit() {

        saleOrderLine = null;
        saleOrderLines = null;
        topNActiveCustomers = null;
        topNActiveSoldProducts = null;

        if (saleOrderExist(saleOrder.getId())) {
            currentForm = "/sc/saleOrder/View.xhtml";
        }
    }

    public void showForm() {
        if (saleOrders != null && !saleOrders.isEmpty()) {
            saleOrder = saleOrders.get(0);
            currentForm = "/sc/saleOrder/View.xhtml";
        }

        currentForm = "/sc/saleOrder/View.xhtml";
    }

    public void showForm(Integer id) {
        if (id != null) {
            saleOrder = super.findItemById(id, SaleOrder.class);
            if (saleOrder != null) {
                partialListType = null;
                saleOrderLines = null;
                saleOrderLine = null;
                query = SaleOrderQueryBuilder.getFindAllQuery();
                saleOrders = super.findWithQuery(query);
                currentForm = "/sc/saleOrder/View.xhtml";
            } else {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
            }
        }
    }

    public void showOrderLineForm() {
        if (saleOrderLines != null && saleOrderLines.size() > 0) {
            saleOrderLine = saleOrderLines.get(0);
            currentForm = "/sc/saleOrder/ViewByProduct.xhtml";
        }
    }

    public void showOrderList() {
        saleOrder = null;
        saleOrderLine = null;
        saleOrderLines = null;
        topNActiveCustomers = null;
        topNActiveSoldProducts = null;
        currentForm = "/sc/saleOrder/List.xhtml";
    }

    public void prepareCreate() {
        System.out.println("----------prepareCreate(): started -----------");

        saleOrder = new SaleOrder();
        saleOrder.setDate(new Date());
        saleOrderLines = new ArrayList<>();
        saleOrderLine = new SaleOrderLine();

        loadActiveCustomers();
        loadActiveSoldProducts();

        if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
            saleOrderLine.setProduct(topNActiveSoldProducts.get(0));
            saleOrderLine.setPrice(saleOrderLine.getProduct().getSalePrice());
            saleOrderLine.setUom(saleOrderLine.getProduct().getUom().getName());
        }
        currentForm = "/sc/saleOrder/Create.xhtml";

        System.out.println("----------prepareCreate(): finished -----------");

    }

    public void prepareEdit(Integer id) {
        if (saleOrderExist(id)) {
            if (saleOrder.getState().equals("Quotation")) {
                saleOrderLine = new SaleOrderLine();
                saleOrderLines = saleOrder.getSaleOrderLines();

                loadActiveCustomers();
                loadActiveSoldProducts();

                if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
                    saleOrderLine.setProduct(topNActiveSoldProducts.get(0));
                    saleOrderLine.setPrice(saleOrderLine.getProduct().getSalePrice());
                    saleOrderLine.setUom(saleOrderLine.getProduct().getUom().getName());
                }

                if (!topNActiveCustomers.contains(saleOrder.getPartner())) {
                    topNActiveCustomers.add(saleOrder.getPartner());
                }

                for (SaleOrderLine orderLine : saleOrderLines) {
                    if (!topNActiveSoldProducts.contains(orderLine.getProduct())) {
                        topNActiveSoldProducts.add(orderLine.getProduct());
                    }
                }

                currentForm = "/sc/saleOrder/Edit.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEditQuotation");
            }
        }
    }

    public void prepareView() {
        if (saleOrder != null) {
            if (saleOrderExist(saleOrder.getId())) {
                currentForm = "/sc/saleOrder/View.xhtml";
            }
        }
    }

    public void resolveRequestParams() {

        if (JsfUtil.isNumeric(saleId)) {
            Integer id = Integer.valueOf(saleId);
            saleOrder = super.findItemById(id, SaleOrder.class);
            if (saleOrder != null) {
                query = SaleOrderQueryBuilder.getFindAllQuery();
                saleOrders = super.findWithQuery(query);
                currentList = "/sc/saleOrder/List.xhtml";
                currentForm = "/sc/saleOrder/View.xhtml";
                return;
            }
        }

        if (JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            query = SaleOrderQueryBuilder.getFindByCustomerQuery(id);
            saleOrders = super.findWithQuery(query);
            if (saleOrders != null && !saleOrders.isEmpty()) {
                saleOrder = saleOrders.get(0);
                partialListType = "partner";
                currentList = "/sc/saleOrder/List.xhtml";
                currentForm = "/sc/saleOrder/View.xhtml";
                return;
            }
        }

        if (JsfUtil.isNumeric(productId)) {
            Integer id = Integer.valueOf(productId);
            query = SaleOrderLineQueryBuilder.getFindByProductQuery(id);
            saleOrderLines = super.findWithQuery(query);
            if (!saleOrderLines.isEmpty()) {
                saleOrderLine = saleOrderLines.get(0);
                currentList = "/sc/saleOrder/ListByProduct.xhtml";
                currentForm = "/sc/saleOrder/ViewByProduct.xhtml";
                return;
            }
        }

        if (JsfUtil.isNumeric(saleLineId)) {
            Integer id = Integer.valueOf(saleLineId);
            saleOrderLine = super.findItemById(id, SaleOrderLine.class);
            if (saleOrderLine != null) {
                query = SaleOrderLineQueryBuilder.getFindByProductQuery(saleOrderLine.getProduct().getId());
                saleOrderLines = super.findWithQuery(query);
                productId = Integer.toString(saleOrderLine.getProduct().getId());
                currentList = "/sc/saleOrder/ListByProduct.xhtml";
                currentForm = "/sc/saleOrder/ViewByProduct.xhtml";
                return;
            }
        }

        currentList = "/sc/saleOrder/List.xhtml";
        currentForm = "/sc/saleOrder/View.xhtml";

        query = SaleOrderQueryBuilder.getFindAllQuery();
        saleOrders = super.findWithQuery(query);
        if (saleOrders != null && !saleOrders.isEmpty()) {
            saleOrder = saleOrders.get(0);
        }
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

            loadActiveCustomers();
            loadActiveSoldProducts();

            if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
                saleOrderLine.setProduct(topNActiveSoldProducts.get(0));
                saleOrderLine.setPrice(saleOrderLine.getProduct().getSalePrice());
                saleOrderLine.setUom(saleOrderLine.getProduct().getUom().getName());
            }

            if (!topNActiveCustomers.contains(saleOrder.getPartner())) {
                topNActiveCustomers.add(saleOrder.getPartner());
            }

            for (SaleOrderLine orderLine : saleOrderLines) {
                if (!topNActiveSoldProducts.contains(orderLine.getProduct())) {
                    topNActiveSoldProducts.add(orderLine.getProduct());
                }
            }
            currentForm = "/sc/saleOrder/Create.xhtml";
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
        params.put("SUBREPORT_DIR", FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/") + "/");

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
            saleOrder = super.findItemById(id, SaleOrder.class);
            if (saleOrder == null) {
                System.out.println("saleOrderExist: no!!");
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                saleOrders.remove(saleOrder);
                saleOrder = saleOrders.get(0);
//                partialListType = null; (new modification)
                currentForm = "/sc/saleOrder/View.xhtml"; // saleOrderLines !!!
                currentList = "/sc/saleOrder/List.xhtml"; // saleOrderLines !!!
                return false;
            } else {
                System.out.println("saleOrderExist: yes!!");
                return true;
            }

        } else {
            return false;
        }
    }

    private String getOrderStatus(Integer id) {
        if (id != null) {
            SaleOrder saleOrder = super.findItemById(id, SaleOrder.class);
            if (saleOrder != null) {
                return saleOrder.getState();
            } else {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                saleOrders.remove(saleOrder);
                this.saleOrder = saleOrders.get(0);
//                partialListType = null;
                currentForm = "/sc/saleOrder/View.xhtml"; // saleOrderLines !!!
                currentList = "/sc/saleOrder/List.xhtml"; // saleOrderLines !!!
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

    public String getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(String currentForm) {
        this.currentForm = currentForm;
    }

    public String getCurrentList() {
        return currentList;
    }

    public void setCurrentList(String currentList) {
        this.currentList = currentList;
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
            query = SaleOrderQueryBuilder.getFindAllQuery();
            saleOrders = super.findWithQuery(query);
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
        if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
            saleOrderLine.setProduct(topNActiveSoldProducts.get(0));
            saleOrderLine.setPrice(topNActiveSoldProducts.get(0).getSalePrice());
            saleOrderLine.setUom(topNActiveSoldProducts.get(0).getUom().getName());
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
        if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
            saleOrderLine.setProduct(topNActiveSoldProducts.get(0));
            saleOrderLine.setPrice(saleOrderLine.getProduct().getSalePrice());
            saleOrderLine.setUom(saleOrderLine.getProduct().getUom().getName());
        }
    }

    public void onRowCancel() {
        saleOrderLine = new SaleOrderLine();
        if (topNActiveSoldProducts != null && !topNActiveSoldProducts.isEmpty()) {
            saleOrderLine.setProduct(topNActiveSoldProducts.get(0));
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

        System.out.println("amountUntaxed: " + amountUntaxed);
        System.out.println("amountTax: " + amountTax);
        System.out.println("amountTotal: " + amountTotal);
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
        currentForm = "/sc/saleOrder/ListByProduct.xhtml";
    }

    public void prepareViewOrderByProduct() {
        if (saleOrderLine != null) {
            currentForm = "/sc/saleOrder/ViewByProduct.xhtml";
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

    public List<Partner> getTopNActiveCustomers() {
        return topNActiveCustomers;
    }

    public List<Partner> getActiveCustomers() {
        return activeCustomers;
    }

    public List<Partner> getFilteredActiveCustomers() {
        return filteredActiveCustomers;
    }

    public List<Product> getTopNActiveSoldProducts() {
        return topNActiveSoldProducts;
    }

    public List<Product> getActiveSoldProducts() {
        return activeSoldProducts;
    }

    public List<Product> getFilteredActiveSoldProducts() {
        return filteredActiveSoldProducts;
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
}
