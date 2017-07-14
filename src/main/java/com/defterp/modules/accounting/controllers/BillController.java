package com.defterp.modules.accounting.controllers;

import com.defterp.modules.purchases.entities.PurchaseOrder;
import com.defterp.modules.purchases.entities.PurchaseOrderLine;
import com.defterp.modules.inventory.entities.Product;
import com.defterp.modules.partners.entities.Partner;
import com.defterp.modules.accounting.entities.Payment;
import com.defterp.modules.accounting.entities.*;
import com.defterp.modules.accounting.constants.BillStatus;
import com.defterp.modules.accounting.queryBuilders.AccountQueryBuilder;
import com.defterp.modules.accounting.queryBuilders.InvoiceQueryBuilder;
import com.defterp.modules.accounting.queryBuilders.JournalQueryBuilder;
import com.defterp.modules.accounting.queryBuilders.PaymentQueryBuilder;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.util.JsfUtil;
import com.defterp.translation.annotations.Status;
import com.defterp.modules.commonClasses.IdGenerator;
import com.defterp.modules.inventory.queryBuilders.ProductQueryBuilder;
import com.defterp.modules.partners.queryBuilders.PartnerQueryBuilder;
import net.sf.jasperreports.engine.*;
import org.apache.commons.lang.SerializationUtils;
import org.primefaces.context.RequestContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

@Named(value = "billController")
@ViewScoped
public class BillController extends AbstractController {

    @Inject
    @Status
    private HashMap<String, String> billTranslatedStatuses;
    private List<Invoice> bills;
    private List<Invoice> filteredBills;
    private Invoice bill;
    private List<InvoiceLine> billLines;
    private InvoiceLine billLine;
    private JournalEntry journalEntry;
    private List<JournalItem> journalItems;
    private Payment payment;
    private List<Payment> vendorOutstandingPayments;
    private String listType;
    private String purchaseId;
    private String vendorId;
    private String billId;
    private Account paymentWriteOffAccount;
    private List<Account> paymentWriteOffAccounts;
    private String paymentType = "";
    private double differenceAmount;
    private int rowIndex;
    private List<Partner> topNActiveVendors;
    private List<Partner> activeVendors;
    private List<Partner> filteredActiveVendors;
    private List<Product> topNActivePurchasedProducts;
    private List<Product> activePurchasedProducts;
    private List<Product> filteredActivePurchasedProducts;
    private Partner vendor;
    private Product product;
    private QueryWrapper query;


    public BillController() {
        super("/sc/supInvoice/");
    }

    public void resolveRequestParams() {

        currentForm = VIEW_URL;

        if (JsfUtil.isNumeric(billId)) {
            Integer id = Integer.valueOf(billId);
            bill = super.findItemById(id, Invoice.class);
            if (bill != null) {
                query = InvoiceQueryBuilder.getFindAllBillsQuery();
                bills = super.findWithQuery(query);
                findVendorOutstandingPayments();
                listType = "allBills";
                return;
            }
        }

        if (JsfUtil.isNumeric(purchaseId)) {
            Integer id = Integer.valueOf(purchaseId);
            query = InvoiceQueryBuilder.getFindByPurchaseOrderQuery(id);
            bills = super.findWithQuery(query);
            if ((bills != null) && (!bills.isEmpty())) {
                bill = bills.get(0);
                findVendorOutstandingPayments();
                listType = "byPurchaseOrder";
                return;
            }
        }

        if (JsfUtil.isNumeric(vendorId)) {
            Integer id = Integer.valueOf(vendorId);
            query = InvoiceQueryBuilder.getFindByVendorQuery(id);
            bills = super.findWithQuery(query);
            if (bills != null && !bills.isEmpty()) {
                bill = bills.get(0);
                findVendorOutstandingPayments();
                listType = "byVendor";
                return;
            }
        }

        query = InvoiceQueryBuilder.getFindAllBillsQuery();
        bills = super.findWithQuery(query);

        if (bills != null && !bills.isEmpty()) {
            bill = bills.get(0);
            findVendorOutstandingPayments();
        }
    }

    public void prepareCreateBill() {

        bill = new Invoice();
        billLines = new ArrayList<>();
        billLine = new InvoiceLine();
        bill.setAccount((Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Account Payable")));
        bill.setJournal((Journal) super.findSingleWithQuery(JournalQueryBuilder.getFindByCodeQuery("BILL")));

        loadActivePurchasedProducts();
        loadActiveVendors();

        if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
            billLine.setProduct(topNActivePurchasedProducts.get(0));
            billLine.setPrice(billLine.getProduct().getPurchasePrice());
            billLine.setUom(billLine.getProduct().getUom().getName());
        }
        currentForm = CREATE_URL;
    }

    public void createBill() {

        if (billLines.isEmpty()) {
            JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneInvoiceLineCreate");
        } else {

            for (InvoiceLine bLine : billLines) {
                bLine.setAccount((Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Product Purchases")));
                bLine.setPartner(bill.getPartner());
                bLine.setInvoice(bill);
            }

            bill.setType("Purchase");
            bill.setState(BillStatus.DRAFT.value());
            bill.setActive(Boolean.TRUE);
            bill.setResidual(bill.getAmountTotal());
            bill.setInvoiceLines(billLines);
            generateBillTaxes();

            bill = super.createItem(bill);

            bill.setName(IdGenerator.generateBillId(bill.getId()));

            bill = super.updateItem(bill);

            if (listType.equals("allBills") && bills != null) {
                bills.add(bill);
            } else {
                query = InvoiceQueryBuilder.getFindAllBillsQuery();
                bills = super.findWithQuery(query);
                listType = "allBills";
            }

            currentForm = VIEW_URL;
        }
    }

    public void cancelCreateBill() {

        billLine = null;
        billLines = null;
        bill = null;

        loadActivePurchasedProducts();
        loadActiveVendors();

        currentForm = VIEW_URL;

        resetListAndCurrentBill();
    }

    public void prepareUpdateBill() {

        bill = super.findItemById(bill.getId(), Invoice.class);

        if (bill != null) {

            if (bill.getState().equals(BillStatus.DRAFT.value())) {
                billLine = new InvoiceLine();
                billLines = bill.getInvoiceLines();

                loadActivePurchasedProducts();
                loadActiveVendors();

                if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
                    billLine.setProduct(topNActivePurchasedProducts.get(0));
                    billLine.setPrice(billLine.getProduct().getPurchasePrice());
                    billLine.setUom(billLine.getProduct().getUom().getName());
                }

                if (!topNActiveVendors.contains(bill.getPartner())) {
                    topNActiveVendors.add(bill.getPartner());
                }

                for (InvoiceLine orderLine : billLines) {
                    if (!topNActivePurchasedProducts.contains(orderLine.getProduct())) {
                        topNActivePurchasedProducts.add(orderLine.getProduct());
                    }
                }
                currentForm = EDIT_URL;

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEdit");
            }

        } else {
            billNotFound();
        }
    }

    public void updateBill(Integer id) {

        String billCurrentStatus = getBillCurrentStatus();

        if (billCurrentStatus != null) {

            if (!billCurrentStatus.equals(BillStatus.DRAFT.value())) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
                currentForm = VIEW_URL;
            } else if (billLines.isEmpty()) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneInvoiceLineUpdate");
            } else {

                for (InvoiceLine bLine : billLines) {
//                    bLine.setAccount((Account)super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Expenses")));
                    bLine.setPartner(bill.getPartner());
                    bLine.setInvoice(bill);
                }

                bill.setResidual(bill.getAmountTotal());

                bill.setInvoiceLines(billLines);
                generateBillTaxes();
                bill = super.updateItem(bill);

                if (listType.equals("allBills") && bills != null) {
                    bills.set(bills.indexOf(bill), bill);
                } else {
                    query = InvoiceQueryBuilder.getFindAllBillsQuery();
                    bills = super.findWithQuery(query);
                    listType = "allBills";
                }

                currentForm = VIEW_URL;
            }
        } else {
            billNotFound();
        }
    }

    private void generateBillTaxes() {

        List<InvoiceTax> billTaxes = new ArrayList<>();

        for (InvoiceLine billline : bill.getInvoiceLines()) {
            if (billline.getTax() != null) {

                double taxAmount = JsfUtil.round(billline.getPriceSubtotal() * billline.getTax().getAmount());
//                billTaxes.add(new InvoiceTax(
//                        bill.getDate(),
//                        taxAmount,
//                        billline.getPriceSubtotal(),
//                        Boolean.TRUE,
//                        (Account)super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Tax Paid")),
//                        bill,
//                        billline.getTax()));
            }
        }

        bill.setInvoiceTaxes(billTaxes);
    }

    public void cancelUpdateBill() {

        billLine = null;
        billLines = null;
        topNActiveVendors = null;
        topNActivePurchasedProducts = null;

        bill = super.findItemById(bill.getId(), Invoice.class);

        if (bill != null) {
            currentForm = VIEW_URL;
        } else {
            billNotFound();
        }
    }

    public void validateBill() {

        bill = super.findItemById(bill.getId(), Invoice.class);

        if (bill != null) {

            if (bill.getState().equals(BillStatus.DRAFT.value())) {

                if (bill.getAmountTotal() == 0d) {
                    bill.setState(BillStatus.PAID.value());
                    setPurchaseOrderStatus();
                } else {
                    bill.setState(BillStatus.OPEN.value());
                }

                generateBillJournalEntry();
                bill.getPartner().setCredit(bill.getPartner().getCredit() + bill.getAmountTotal());
                bill = super.updateItem(bill);
                bills.set(bills.indexOf(bill), bill);
                findVendorOutstandingPayments();

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorValidate");
            }
        } else {
            billNotFound();
        }
    }

    private void generateBillJournalEntry() {

        journalItems = new ArrayList<>();

        String journalEntryReference = ((bill.getReference() == null) || (bill.getReference().isEmpty())) ? bill.getOrigin() : bill.getReference();

        journalEntry = new JournalEntry(
                bill.getName(),
                journalEntryReference,
                bill.getDate(),
                Boolean.TRUE,
                (Journal) super.findSingleWithQuery(JournalQueryBuilder.getFindByCodeQuery("BILL")),
                bill.getPartner(),
                null,
                bill,
                "Posted",
                bill.getAmountTotal());

        journalItems.add(new JournalItem(
                0d,
                bill.getAmountTotal(),
                bill.getDate(),
                bill.getName(),
                journalEntryReference,
                0d,
                0d,
                Boolean.TRUE,
                (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Account Payable")),
                journalEntry,
                journalEntry.getJournal(),
                bill.getPartner(),
                null,
                null,
                0d,
                null));

        if (bill.getAmountTax() > 0d) {
            for (InvoiceLine billline : bill.getInvoiceLines()) {
                if (billline.getTax() != null) {

                    double taxAmount = JsfUtil.round(billline.getPriceSubtotal() * billline.getTax().getAmount());
                    journalItems.add(new JournalItem(
                            taxAmount,
                            0d,
                            bill.getDate(),
                            billline.getTax().getName(),
                            journalEntryReference,
                            taxAmount,
                            0d,
                            Boolean.TRUE,
                            (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Tax Paid")),
                            journalEntry,
                            journalEntry.getJournal(),
                            bill.getPartner(),
                            null,
                            null,
                            0d,
                            billline.getTax()));
                }

            }
        }

        for (InvoiceLine billline : bill.getInvoiceLines()) {

            journalItems.add(new JournalItem(
                    billline.getPriceSubtotal(),
                    0d,
                    bill.getDate(),
                    billline.getProduct().getName(),
                    journalEntryReference,
                    billline.getPriceSubtotal(),
                    billline.getQuantity(),
                    Boolean.TRUE,
                    (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Product Purchases")),
                    journalEntry,
                    journalEntry.getJournal(),
                    bill.getPartner(),
                    billline.getProduct(),
                    billline.getProduct().getUom(),
                    0d,
                    null));

        }
        journalEntry.setJournalItems(journalItems);
        journalEntry = super.createItem(journalEntry);
        bill.setJournalEntry(journalEntry);
        journalItems = null;
        journalEntry = null;
    }

    public void deleteBill() {

        bill = super.findItemById(bill.getId(), Invoice.class);

        if (bill != null) {

            if (bill.getState().equals(BillStatus.CANCELLED.value())) {
                cancelBillRelations();

                boolean deleted = super.deleteItem(bill);

                if (deleted) {

                    JsfUtil.addSuccessMessage("ItemDeleted");
                    currentForm = VIEW_URL;

                    if (bills != null && bill != null) {
                        bills.remove(bill);
                    }

                    resetListAndCurrentBill();

                } else {
                    JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                }

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
            }

        } else {
            billNotFound();
        }
    }

    private void cancelBillRelations() {

        if (bill.getPurchaseOrder() != null) {
            PurchaseOrder purchaseOrder = super.findItemById(bill.getPurchaseOrder().getId(), PurchaseOrder.class);
            purchaseOrder.getInvoices().size();
            purchaseOrder.getInvoices().remove(bill);
            bill.setPurchaseOrder(null);
            super.updateItem(purchaseOrder);
        }
    }

    public void cancelBill() {

        bill = super.findItemById(bill.getId(), Invoice.class);

        if (bill != null) {

            if (!bill.getState().equals(BillStatus.CANCELLED.value())) {

                if (bill.getState().equals(BillStatus.OPEN.value()) || bill.getState().equals(BillStatus.PAID.value())) {
                    cancelBillPayments();
                    bill.getJournalEntry().setState("Unposted");
                    super.updateItem(bill.getJournalEntry());
                }
                bill.setState(BillStatus.CANCELLED.value());
                bill.setResidual(0d);
                bill = super.updateItem(bill);
                bills.set(bills.indexOf(bill), bill);
                findVendorOutstandingPayments();

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }

        } else {
            billNotFound();
        }
    }

    public void prepareViewBill() {

        bill = super.findItemById(bill.getId(), Invoice.class);

        if (bill != null) {
            findVendorOutstandingPayments();
            currentForm = VIEW_URL;
        } else {
            billNotFound();
        }
    }

    public void duplicateBill() {

        bill = super.findItemById(bill.getId(), Invoice.class);

        if (bill != null) {

            bill.getInvoiceLines().size();
            bill.getInvoiceTaxes().size();
            Invoice newInvoice = (Invoice) SerializationUtils.clone(bill);

            newInvoice.setId(null);
            newInvoice.setJournalEntry(null);
            newInvoice.setDate(new Date());
            newInvoice.setComment(null);
            newInvoice.setInvoicePayments(null);
            newInvoice.setOrigin(null);
            newInvoice.setName(null);
            newInvoice.setPayments(null);
            newInvoice.setNumber(null);
            newInvoice.setPurchaseOrder(null);
            newInvoice.setSaleOrder(null);
            newInvoice.setReference(null);
            newInvoice.setSupplierInvoiceNumber(null);
            newInvoice.setState(BillStatus.DRAFT.value());
            newInvoice.setResidual(newInvoice.getAmountTotal());

            for (InvoiceLine bLine : newInvoice.getInvoiceLines()) {
                bLine.setId(null);
                bLine.setInvoice(newInvoice);
            }

            for (InvoiceTax invTax : newInvoice.getInvoiceTaxes()) {
                invTax.setId(null);
                invTax.setInvoice(newInvoice);
            }

            bill = newInvoice;
            billLine = new InvoiceLine();
            billLines = bill.getInvoiceLines();

            loadActivePurchasedProducts();
            loadActiveVendors();

            if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
                billLine.setProduct(topNActivePurchasedProducts.get(0));
                billLine.setPrice(billLine.getProduct().getPurchasePrice());
                billLine.setUom(billLine.getProduct().getUom().getName());
            }

            if (!topNActiveVendors.contains(bill.getPartner())) {
                topNActiveVendors.add(bill.getPartner());
            }

            for (InvoiceLine orderLine : billLines) {
                if (!topNActivePurchasedProducts.contains(orderLine.getProduct())) {
                    topNActivePurchasedProducts.add(orderLine.getProduct());
                }
            }

            currentForm = CREATE_URL;

        } else {
            billNotFound();
        }
    }

    private void setPurchaseOrderStatus() {
        if (bill.getPurchaseOrder() != null) {
            bill.getPurchaseOrder().getPurchaseOrderLines().size();
            boolean purchaseOrderInvoiced = true;
            boolean billsPaid = true;

            for (PurchaseOrderLine line : bill.getPurchaseOrder().getPurchaseOrderLines()) {
                if (line.getInvoiced() == false) {
                    purchaseOrderInvoiced = false;
                }
            }

            if (purchaseOrderInvoiced == true) {
                for (Invoice bill : this.bill.getPurchaseOrder().getInvoices()) {
                    if (!bill.getState().equals(BillStatus.PAID.value())) {
                        billsPaid = false;
                    }
                }
            }

            if (purchaseOrderInvoiced == true && billsPaid == true) {
                bill.getPurchaseOrder().setPaid(Boolean.TRUE);
                if (bill.getPurchaseOrder().getShipped() == true) {
                    bill.getPurchaseOrder().setState("Done");
                }
                super.updateItem(bill.getPurchaseOrder());
            }
        }
    }

    public void printBill() throws IOException, JRException {

        for (InvoiceLine orderLine : bill.getInvoiceLines()) {
            orderLine.setProductName(orderLine.getProduct().getName());

            if (orderLine.getTax() != null) {
                orderLine.setTaxName(orderLine.getTax().getName());
            } else {
                orderLine.setTaxName("");
            }
        }

        ResourceBundle bundle = JsfUtil.getBundle();
        String name = bundle.getString("Bill");
        String currency = bundle.getString("Currency");

        String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/bill.jasper");

        Map<String, Object> params = new HashMap<>();
        params.put("bill", bill);
        params.put("partner", bill.getPartner());
        params.put("orderLines", bill.getInvoiceLines());
        params.put("currency", currency);
        params.put("SUBREPORT_DIR", FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/") + "/");

        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, params, new JREmptyDataSource());
//      JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, new HashMap<String,Object>(), new JRBeanArrayDataSource(new SaleOrder[]{saleOrder}));  
        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        httpServletResponse.addHeader("Content-disposition", "attachment; filename=" + name + "_" + bill.getName() + ".pdf");
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
        FacesContext.getCurrentInstance().responseComplete();

    }

    public void preparePayment() {

        bill = super.findItemById(bill.getId(), Invoice.class);

        if (bill != null) {
            if (bill.getState().equals(BillStatus.OPEN.value())) {
                payment = new Payment();
                if (bill.getPurchaseOrder() != null) {
                    String paymentReference = ((bill.getPurchaseOrder().getReference() == null) || (bill.getPurchaseOrder().getReference().isEmpty())) ? bill.getPurchaseOrder().getName() : bill.getPurchaseOrder().getReference();
                    payment.setReference(paymentReference);
                }
                paymentType = "keep open";
                differenceAmount = 0d;
                payment.setAmount(bill.getResidual());
                payment.setPartner(bill.getPartner());
                paymentWriteOffAccounts = null;
                paymentWriteOffAccount = null;
            } else {
                FacesContext.getCurrentInstance().validationFailed();
                JsfUtil.addWarningMessageDialog("InvalidAction", "CannotProcessBillPayment");
            }
        } else {
            billNotFound();
        }
    }

    public void makePayment(Integer id) {

        bill = super.findItemById(id, Invoice.class);

        if (bill != null) {

            if (bill.getState().equals(BillStatus.OPEN.value())) {

                double paidAmount;
                double outstandingPayment = 0d;
                String account;
                payment.setAmount(JsfUtil.round(payment.getAmount()));
                differenceAmount = JsfUtil.round(bill.getResidual() - payment.getAmount());
                double overPayment = differenceAmount < 0d ? Math.abs(differenceAmount) : 0d;
                double netPayment = JsfUtil.round(payment.getAmount() - overPayment);

                if (differenceAmount < 0d && paymentType.equals("keep open")) {
                    outstandingPayment = Math.abs(differenceAmount);
                }

                if (payment.getJournal().getName().equals("Cash")) {
                    account = "Cash";
                } else {
                    account = "Bank";
                }

                if (differenceAmount < 0d) {
                    paidAmount = bill.getResidual();
                    bill.setResidual(0d);
                    bill.setState(BillStatus.PAID.value());
                    if (paymentType.equals("keep open")) {
                        bill.getPartner().setDebit(bill.getPartner().getDebit() + Math.abs(differenceAmount));
                    }

                } else if (differenceAmount > 0d) {
                    paidAmount = payment.getAmount();
                    if (paymentType.equals("keep open")) {
                        bill.setResidual(differenceAmount);
                    } else {
                        bill.setState(BillStatus.PAID.value());
                        bill.setResidual(0d);
                    }

                } else {
                    paidAmount = payment.getAmount();
                    bill.setResidual(0d);
                    bill.setState(BillStatus.PAID.value());
                }

                bill.getPartner().setCredit(JsfUtil.round(bill.getPartner().getCredit() - paidAmount));
                payment = new Payment(payment.getAmount(),
                        payment.getDate(),
                        payment.getPartner(),
                        payment.getJournal(),
                        "out", Boolean.TRUE,
                        (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery(account)),
                        null,
                        bill,
                        "Posted",
                        null,
                        outstandingPayment,
                        "supplier");

                payment = super.createItem(payment);
                payment.setName(IdGenerator.generateSupplierOutPayment(payment.getId()));
                payment = super.updateItem(payment);

                generatePaymentJournalEntry(account);
                generateBillPayment(bill, payment.getJournalEntry(), netPayment, payment.getName());
                if (differenceAmount != 0d && paymentType.equals("fully paid")) {
                    generatePaymentWriteOffJournalEntry(account);
                }

                payment = super.updateItem(payment);
                bill.getPayments().add(payment);
                bill = super.updateItem(bill);
                bills.set(bills.indexOf(bill), bill);

                findVendorOutstandingPayments();

                if (bill.getState().equals(BillStatus.PAID.value())) {
                    setPurchaseOrderStatus();
                }

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "CannotProcessBillPayment");
            }

        } else {
            billNotFound();
        }
    }

    private void generatePaymentJournalEntry(String account) {

        journalItems = new ArrayList<>();
        String journalEntryReference = ((payment.getReference() == null) || (payment.getReference().isEmpty())) ? bill.getOrigin() : payment.getReference();

        journalEntry = new JournalEntry(
                journalEntryReference,
                payment.getDate(),
                Boolean.TRUE,
                (Journal) super.findSingleWithQuery(JournalQueryBuilder.getFindByCodeQuery(account)),
                payment.getPartner(),
                payment,
                null,
                "Posted",
                payment.getAmount());

        journalItems.add(new JournalItem(
                payment.getAmount(),
                0d,
                payment.getDate(),
                "Vendor Payment: " + payment.getInvoice().getName(),
                journalEntryReference,
                0d,
                0d,
                Boolean.TRUE,
                (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Account Payable")),
                journalEntry,
                journalEntry.getJournal(),
                payment.getPartner(),
                null,
                null,
                0d,
                null));

        journalItems.add(new JournalItem(
                0d,
                payment.getAmount(),
                payment.getDate(),
                payment.getName(),
                journalEntryReference,
                0d,
                0d,
                Boolean.TRUE,
                (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery(account)),
                journalEntry,
                journalEntry.getJournal(),
                payment.getPartner(),
                null,
                null,
                0d,
                null));

        journalEntry.setJournalItems(journalItems);

        journalEntry = super.createItem(journalEntry);

        if (account.equals("Cash")) {
            journalEntry.setName(IdGenerator.generatePaymentCashEntryId(journalEntry.getId()));
        } else if (account.equals("Bank")) {
            journalEntry.setName(IdGenerator.generatePaymentBankEntryId(journalEntry.getId()));
        }

        journalEntry = super.updateItem(journalEntry);

        payment.setJournalEntry(journalEntry);
        journalItems = null;
        journalEntry = null;

    }

    private void generatePaymentWriteOffJournalEntry(String account) {

        double difference = Math.abs(differenceAmount);
        double writeOffCredit;
        double writeOffDebit;
        double payableCredit;
        double payableDebit;

        if (differenceAmount < 0d) {
            writeOffCredit = 0d;
            writeOffDebit = difference;
            payableCredit = difference;
            payableDebit = 0d;

        } else {
            writeOffCredit = difference;
            writeOffDebit = 0d;
            payableCredit = 0d;
            payableDebit = difference;

        }

        List<JournalItem> journalItems = new ArrayList<>();
        JournalEntry journalEntry = new JournalEntry(
                payment.getInvoice().getOrigin(),
                payment.getDate(),
                Boolean.TRUE,
                (Journal) super.findSingleWithQuery(JournalQueryBuilder.getFindByCodeQuery(account)),
                payment.getPartner(),
                null,
                null,
                "Posted",
                difference);

        journalItems.add(new JournalItem(
                payableDebit,
                payableCredit,
                payment.getDate(),
                "Write-Off",
                payment.getInvoice().getOrigin(),
                0d,
                0d,
                Boolean.TRUE,
                (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Account Payable")),
                journalEntry,
                journalEntry.getJournal(),
                payment.getPartner(),
                null,
                null,
                0d,
                null));

        journalItems.add(new JournalItem(
                writeOffDebit,
                writeOffCredit,
                payment.getDate(),
                "Write-Off",
                payment.getInvoice().getOrigin(),
                0d,
                0d,
                Boolean.TRUE,
                paymentWriteOffAccount,
                journalEntry,
                journalEntry.getJournal(),
                payment.getPartner(),
                null,
                null,
                0d,
                null));

        journalEntry.setJournalItems(journalItems);

        journalEntry = super.createItem(journalEntry);

        if (account.equals("Cash")) {
            journalEntry.setName(IdGenerator.generatePaymentCashEntryId(journalEntry.getId()));
        } else if (account.equals("Bank")) {
            journalEntry.setName(IdGenerator.generatePaymentBankEntryId(journalEntry.getId()));
        }

        journalEntry = super.updateItem(journalEntry);

        if (differenceAmount > 0d) {
            generateBillPayment(bill, journalEntry, difference, "Write-Off");
        }
    }

    public void payUsingOutstandingPayment(Integer paymentId) {

        payment = super.findItemById(paymentId, Payment.class);
        bill = super.findItemById(bill.getId(), Invoice.class);
        Double paidAmount;
        Double newOverPayment;

        if (payment != null && bill != null) {
            if (payment.getOverpayment() > 0d && bill.getResidual() > 0d) {

                if (payment.getOverpayment() >= bill.getResidual()) {
                    paidAmount = bill.getResidual();
                    newOverPayment = JsfUtil.round(payment.getOverpayment() - bill.getResidual());
                    bill.setState(BillStatus.PAID.value());

                } else {
                    paidAmount = payment.getOverpayment();
                    newOverPayment = 0d;
                }

                payment.setOverpayment(newOverPayment);
                bill.setResidual(JsfUtil.round(bill.getResidual() - paidAmount));

                generateBillPayment(bill, payment.getJournalEntry(), paidAmount, payment.getName());

                super.updateItem(payment);
                bill = super.updateItem(bill);
                bills.set(bills.indexOf(bill), bill);
                payment = null;

                if (bill.getState().equals(BillStatus.PAID.value())) {
                    setPurchaseOrderStatus();
                }
                findVendorOutstandingPayments();
            }
        }
    }

    private void generateBillPayment(Invoice bill, JournalEntry journalEntry, Double netPayment, String paymentName) {

        InvoicePayment billPayment = new InvoicePayment(bill, journalEntry, netPayment, journalEntry.getDate(), paymentName);
        billPayment = super.createItem(billPayment);
        journalEntry.getInvoicePayments().add(billPayment);
        bill.getInvoicePayments().add(billPayment);
    }

    private void findVendorOutstandingPayments() {

        vendorOutstandingPayments = null;

        if (bill != null && bill.getState().equals(BillStatus.OPEN.value())) {
            vendorOutstandingPayments = super.findWithQuery(PaymentQueryBuilder.getFindOutstandingByVendorQuery(bill.getPartner().getId()));
        }
    }

    private void cancelBillPayments() {
        if (bill.getInvoicePayments() != null && !bill.getInvoicePayments().isEmpty()) {
            JsfUtil.addSuccessMessageDialog("Info", "BillPaymentToOutstanding");
            for (InvoicePayment billPayment : bill.getInvoicePayments()) {
                if (billPayment.getJournalEntry().getPayment() != null) {
                    billPayment.getJournalEntry().getPayment().setOverpayment(billPayment.getJournalEntry().getPayment().getOverpayment() + billPayment.getPaidAmount());
                    billPayment.getJournalEntry().getPayment().setInvoice(null);
                    super.updateItem(billPayment.getJournalEntry().getPayment());
                    super.deleteItem(billPayment);
                }
            }
            bill.setPayments(null);
            bill.setInvoicePayments(null);
        }
    }

    public void updateDifferenceAmount() {
        payment.setAmount(JsfUtil.round(payment.getAmount()));
        differenceAmount = JsfUtil.round(bill.getResidual() - payment.getAmount());
        if (differenceAmount != 0d) {
            if ("".equals(paymentType)) {
                paymentType = "keep open";
            } else if ("fully paid".equals(paymentType) && differenceAmount > 0d) {
                paymentWriteOffAccounts = super.findWithQuery(AccountQueryBuilder.getFindByNameQuery("Expenses"));
                paymentWriteOffAccount = paymentWriteOffAccounts.get(0);
            } else if ("fully paid".equals(paymentType) && differenceAmount < 0d) {
                paymentWriteOffAccounts = super.findWithQuery(AccountQueryBuilder.getFindByNameQuery("Extra Payment"));
                paymentWriteOffAccount = paymentWriteOffAccounts.get(0);
            }
        }
    }

    public void onPaymentDifferenceStrategyChange() {

        if ("fully paid".equals(paymentType) && differenceAmount > 0d) {
            paymentWriteOffAccounts = super.findWithQuery(AccountQueryBuilder.getFindByNameQuery("Expenses"));
            paymentWriteOffAccount = paymentWriteOffAccounts.get(0);
        } else if ("fully paid".equals(paymentType) && differenceAmount < 0d) {
            paymentWriteOffAccounts = super.findWithQuery(AccountQueryBuilder.getFindByNameQuery("Extra Payment"));
            paymentWriteOffAccount = paymentWriteOffAccounts.get(0);
        }
    }

    private void billNotFound() {

        JsfUtil.addWarningMessage("ItemDoesNotExist");
        currentForm = VIEW_URL;

        if (bills != null && bill != null) {
            bills.remove(bill);
        }

        resetListAndCurrentBill();
    }

    private String getBillCurrentStatus() {

        if (bill != null) {

            Invoice tempItem = super.findItemById(bill.getId(), Invoice.class);

            if (tempItem != null) {
                return tempItem.getState();
            } else {
                return null;
            }
        }
        return null;
    }

    private void resetListAndCurrentBill() {

        if (bills != null && !bills.isEmpty()) {
            bill = bills.get(0);
        } else {
            listType = "allBills";
            query = InvoiceQueryBuilder.getFindAllBillsQuery();
            bills = super.findWithQuery(query);

            if ((bills != null) && !bills.isEmpty()) {
                bill = bills.get(0);
            }
        }

        findVendorOutstandingPayments();
    }

    public void onRowEditInit(InvoiceLine orderLine) {
        billLine = (InvoiceLine) SerializationUtils.clone(orderLine);
    }

    public void onRowEdit(int index) {
        if (index >= 0 && index < billLines.size()) {
            billLines.get(index).setQuantity(JsfUtil.round(billLines.get(index).getQuantity(), billLines.get(index).getProduct().getUom().getDecimals()));
            billLines.get(index).setPrice(JsfUtil.round(billLines.get(index).getPrice()));

            if (billLines.get(index).getQuantity() == 0d) {
                billLines.get(index).setQuantity(1d);
            }

            if (billLines.get(index).getPrice() == 0d) {
                billLines.get(index).setTax(null);
            }

            billLines.get(index).setPriceSubtotal(JsfUtil.round(billLines.get(index).getPrice() * billLines.get(index).getQuantity()));
            SumUpBill();
        }
    }

    public void onRowCancel(int index) {
        if (index >= 0 && index < billLines.size()) {
            billLines.remove(index);
            billLines.add(index, billLine);
            billLine = new InvoiceLine();
            if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
                billLine.setProduct(topNActivePurchasedProducts.get(0));
                billLine.setPrice(billLine.getProduct().getPurchasePrice());
                billLine.setUom(billLine.getProduct().getUom().getName());
            }
        }
    }

    public void onRowCancel() {
        billLine = new InvoiceLine();
        if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
            billLine.setProduct(topNActivePurchasedProducts.get(0));
            billLine.setPrice(billLine.getProduct().getPurchasePrice());
            billLine.setUom(billLine.getProduct().getUom().getName());
        }
    }

    public void onRowAdd(ActionEvent event) {

        billLine.setActive(Boolean.TRUE);
        billLine.setPrice(JsfUtil.round(billLine.getPrice()));
        billLine.setQuantity(JsfUtil.round(billLine.getQuantity(), billLine.getProduct().getUom().getDecimals()));

        if (billLine.getQuantity() == 0d) {
            billLine.setQuantity(1d);
        }

        if (billLine.getPrice() == 0d) {
            billLine.setTax(null);
        }

        billLine.setPriceSubtotal(JsfUtil.round((billLine.getPrice()) * (billLine.getQuantity())));
        billLines.add(billLine);
        SumUpBill();
        billLine = new InvoiceLine();
        if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
            billLine.setProduct(topNActivePurchasedProducts.get(0));
            billLine.setPrice(topNActivePurchasedProducts.get(0).getPurchasePrice());
            billLine.setUom(topNActivePurchasedProducts.get(0).getUom().getName());
        }

    }

    public void onRowDelete(int index) {
        if (index >= 0 && index < billLines.size()) {
            billLines.remove(index);
            SumUpBill();
        }
    }

    public void onProductChange() {

        billLine.setPrice(billLine.getProduct().getPurchasePrice());
        billLine.setUom(billLine.getProduct().getUom().getName());
    }

    public void onProductChange(int index) {
        if (index >= 0 && index < billLines.size()) {
            billLines.get(index).setPrice(billLines.get(index).getProduct().getPurchasePrice());
            billLines.get(index).setUom(billLines.get(index).getProduct().getUom().getName());
        }
    }

    public void setRowIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        rowIndex = Integer.valueOf(params.get("rowIndex"));
    }

    private void SumUpBill() {

        bill.setAmountUntaxed(0d);
        bill.setAmountTax(0d);
        bill.setAmountTotal(0d);

        for (InvoiceLine bLine : billLines) {
            bill.setAmountUntaxed(bill.getAmountUntaxed() + bLine.getPriceSubtotal());
            if (bLine.getTax() != null) {
                bill.setAmountTax(bill.getAmountTax() + (bLine.getPriceSubtotal() * bLine.getTax().getAmount()));
            }
        }

        bill.setAmountUntaxed(JsfUtil.round(bill.getAmountUntaxed()));
        bill.setAmountTax(JsfUtil.round(bill.getAmountTax()));
        BigDecimal amountUntaxed = BigDecimal.valueOf(bill.getAmountUntaxed());
        BigDecimal amountTax = BigDecimal.valueOf(bill.getAmountTax());
        BigDecimal amountTotal = amountUntaxed.add(amountTax);
        bill.setAmountTotal(JsfUtil.round(amountTotal.doubleValue()));
    }

    public void onSelectProduct() {

        if (product != null) {
            if (!topNActivePurchasedProducts.contains(product)) {
                topNActivePurchasedProducts.add(product);
            }

            if (rowIndex < 0) {

                billLine.setProduct(product);
                billLine.setPrice(product.getPurchasePrice());
                billLine.setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("mainForm:productMenuTwo");
                RequestContext.getCurrentInstance().update("mainForm:price");

            } else {

                billLines.get(rowIndex).setProduct(product);
                billLines.get(rowIndex).setPrice(product.getPurchasePrice());
                billLines.get(rowIndex).setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("mainForm:datalist:" + rowIndex + ":productMenu");
                RequestContext.getCurrentInstance().update("mainForm:datalist:" + rowIndex + ":pricee");
            }
        }
    }

    public void onSelectSupplier() {

        if (vendor != null) {
            if (topNActiveVendors != null && !topNActiveVendors.contains(vendor)) {
                topNActiveVendors.add(vendor);
            }
            bill.setPartner(vendor);
        }
    }

    private void loadActiveVendors() {

        query = PartnerQueryBuilder.getFindActiveVendorsQuery();
        activeVendors = super.findWithQuery(query);

        if (activeVendors != null && activeVendors.size() > MAX_DROPDOWN_ITEMS) {
            topNActiveVendors = activeVendors.subList(0, MAX_DROPDOWN_ITEMS);
        } else {
            topNActiveVendors = activeVendors;
        }
    }

    private void loadActivePurchasedProducts() {

        query = ProductQueryBuilder.getFindActivePurchasedProductsQuery();
        activePurchasedProducts = super.findWithQuery(query);

        if (activePurchasedProducts != null && activePurchasedProducts.size() > MAX_DROPDOWN_ITEMS) {
            topNActivePurchasedProducts = activePurchasedProducts.subList(0, MAX_DROPDOWN_ITEMS);
        } else {
            topNActivePurchasedProducts = activePurchasedProducts;
        }
    }

    public String getBillStatus(String billStatus) {
        return billTranslatedStatuses.get(billStatus);
    }

    public String getBillStatusColor(String status) {
        switch (status) {
            case "Draft":
                return "#009fd4";
            case "Open":
                return "#406098";
            case "Paid":
                return "#3477db";
            default:
                return "#6d8891";
        }

    }

    public int getInvoiceIndex() {
        if (bills != null && bill != null) {
            return bills.indexOf(bill) + 1;
        }
        return 0;
    }

    public void nextInvoice() {
        if (bills != null && bill != null) {
            if (bills.indexOf(bill) == (bills.size() - 1)) {
                bill = bills.get(0);
            } else {
                bill = bills.get(bills.indexOf(bill) + 1);
            }
            findVendorOutstandingPayments();
        }
    }

    public void previousInvoice() {
        if (bills != null && bill != null) {
            if (bills.indexOf(bill) == 0) {
                bill = bills.get(bills.size() - 1);
            } else {
                bill = bills.get(bills.indexOf(bill) - 1);
            }
            findVendorOutstandingPayments();
        }
    }

    public List<Account> getBillAccounts() {
        query = AccountQueryBuilder.getFindByNameQuery("Account Payable");
        return super.findWithQuery(query);
    }

    public List<Invoice> getBills() {
        return bills;
    }

    public void setBills(List<Invoice> bills) {
        this.bills = bills;
    }

    public List<Invoice> getFilteredBills() {
        return filteredBills;
    }

    public void setFilteredBills(List<Invoice> filteredBills) {
        this.filteredBills = filteredBills;
    }

    public Invoice getBill() {
        return bill;
    }

    public void setBill(Invoice bill) {
        this.bill = bill;
    }

    public List<InvoiceLine> getBillLines() {
        return billLines;
    }

    public void setBillLines(List<InvoiceLine> billLines) {
        this.billLines = billLines;
    }

    public InvoiceLine getBillLine() {
        return billLine;
    }

    public void setBillLine(InvoiceLine billLine) {
        this.billLine = billLine;
    }

    public String getVendorId() {
        return vendorId;
    }

    public void setVendorId(String vendorId) {
        this.vendorId = vendorId;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public Partner getVendor() {
        return vendor;
    }

    public void setVendor(Partner vendor) {
        this.vendor = vendor;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getListType() {
        return listType;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

    public List<Payment> getVendorOutstandingPayments() {
        return vendorOutstandingPayments;
    }

    public void setVendorOutstandingPayments(List<Payment> vendorOutstandingPayments) {
        this.vendorOutstandingPayments = vendorOutstandingPayments;
    }

    public Double getDifferenceAmount() {
        return differenceAmount;
    }

    public void setDifferenceAmount(Double differenceAmount) {
        this.differenceAmount = differenceAmount;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public List<Partner> getTopNActiveVendors() {
        return topNActiveVendors;
    }

    public void setTopNActiveVendors(List<Partner> topNActiveVendors) {
        this.topNActiveVendors = topNActiveVendors;
    }

    public List<Partner> getActiveVendors() {
        return activeVendors;
    }

    public void setActiveVendors(List<Partner> activeVendors) {
        this.activeVendors = activeVendors;
    }

    public List<Partner> getFilteredActiveVendors() {
        return filteredActiveVendors;
    }

    public void setFilteredActiveVendors(List<Partner> filteredActiveVendors) {
        this.filteredActiveVendors = filteredActiveVendors;
    }

    public List<Product> getTopNActivePurchasedProducts() {
        return topNActivePurchasedProducts;
    }

    public void setTopNActivePurchasedProducts(List<Product> topNActivePurchasedProducts) {
        this.topNActivePurchasedProducts = topNActivePurchasedProducts;
    }

    public List<Product> getActivePurchasedProducts() {
        return activePurchasedProducts;
    }

    public void setActivePurchasedProducts(List<Product> activePurchasedProducts) {
        this.activePurchasedProducts = activePurchasedProducts;
    }

    public List<Product> getFilteredActivePurchasedProducts() {
        return filteredActivePurchasedProducts;
    }

    public void setFilteredActivePurchasedProducts(List<Product> filteredActivePurchasedProducts) {
        this.filteredActivePurchasedProducts = filteredActivePurchasedProducts;
    }

    public Account getPaymentWriteOffAccount() {
        return paymentWriteOffAccount;
    }

    public void setPaymentWriteOffAccount(Account paymentWriteOffAccount) {
        this.paymentWriteOffAccount = paymentWriteOffAccount;
    }

    public List<Account> getPaymentWriteOffAccounts() {
        return paymentWriteOffAccounts;
    }

    public void setPaymentWriteOffAccounts(List<Account> paymentWriteOffAccounts) {
        this.paymentWriteOffAccounts = paymentWriteOffAccounts;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

}
