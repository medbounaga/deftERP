package com.defterp.modules.accounting.controllers;

import com.defterp.modules.purchases.entities.PurchaseOrder;
import com.defterp.modules.purchases.entities.PurchaseOrderLine;
import com.defterp.modules.inventory.entities.Product;
import com.defterp.modules.partners.entities.Partner;
import com.defterp.modules.accounting.entities.Payment;
import com.defterp.modules.accounting.entities.*;
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

@Named(value = "supInvoiceController")
@ViewScoped
public class SupInvoiceController extends AbstractController {

    @Inject
    @Status
    private HashMap<String, String> statuses;
    private List<Invoice> invoices;
    private List<Invoice> filteredInvoices;
    private Invoice invoice;
    private List<InvoiceLine> invoiceLines;
    private InvoiceLine invoiceLine;
    private JournalEntry journalEntry;
    private List<JournalItem> journalItems;
    private Payment payment;
    private List<Payment> outstandingPayments;
    private String partialListType;
    private String purchaseId;
    private String partnerId;
    private String invoiceId;
    private Account writeOffAccount;
    private List<Account> writeOffAccounts;
    private String paymentType = "";
    private double differenceAmount;
    private int rowIndex;
    private List<Partner> topNActiveVendors;
    private List<Partner> activeVendors;
    private List<Partner> filteredActiveVendors;
    private List<Product> topNActivePurchasedProducts;
    private List<Product> activePurchasedProducts;
    private List<Product> filteredActivePurchasedProducts;
    private Partner supplier;
    private Product product;
    private QueryWrapper query;

    public SupInvoiceController() {
        super("/sc/supInvoice/");
    }

    private enum BillStatus {

        DRAFT("Draft"),
        OPEN("Open"),
        CANCELLED("Cancelled"),
        PAID("Paid");

        private final String status;

        BillStatus(String status) {
            this.status = status;
        }

        public String value() {
            return status;
        }
    }

    public void resolveRequestParams() {

        currentForm = VIEW_URL;

        if (JsfUtil.isNumeric(invoiceId)) {
            Integer id = Integer.valueOf(invoiceId);
            invoice = super.findItemById(id, Invoice.class);
            if (invoice != null) {
                query = InvoiceQueryBuilder.getFindAllBillsQuery();
                invoices = super.findWithQuery(query);
                findVendorOutstandingPayments();
                return;
            }
        }

        if (JsfUtil.isNumeric(purchaseId)) {
            Integer id = Integer.valueOf(purchaseId);
            query = InvoiceQueryBuilder.getFindByPurchaseOrderQuery(id);
            invoices = super.findWithQuery(query);
            if ((invoices != null) && (!invoices.isEmpty())) {
                invoice = invoices.get(0);
                findVendorOutstandingPayments();
                partialListType = "purchaseOrder";
                return;
            }
        }

        if (JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            query = InvoiceQueryBuilder.getFindByVendorQuery(id);
            invoices = super.findWithQuery(query);
            if (invoices != null && !invoices.isEmpty()) {
                invoice = invoices.get(0);
                findVendorOutstandingPayments();
                partialListType = "partner";
                return;
            }
        }

        query = InvoiceQueryBuilder.getFindAllBillsQuery();
        invoices = super.findWithQuery(query);

        if (invoices != null && !invoices.isEmpty()) {
            invoice = invoices.get(0);
            findVendorOutstandingPayments();
        }
    }

    public void validateBill() {

        invoice = super.findItemById(invoice.getId(), Invoice.class);

        if (invoice != null) {

            if (invoice.getState().equals(BillStatus.DRAFT.value())) {

                if (invoice.getAmountTotal() == 0d) {
                    invoice.setState(BillStatus.PAID.value());
                    setPurchaseOrderStatus();
                } else {
                    invoice.setState(BillStatus.OPEN.value());
                }

                generateInvoiceJournalEntry();
                invoice.getPartner().setCredit(invoice.getPartner().getCredit() + invoice.getAmountTotal());
                invoice = super.updateItem(invoice);
                invoices.set(invoices.indexOf(invoice), invoice);
                findVendorOutstandingPayments();

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorValidate");
            }
        } else {
            billNotFound();
        }
    }

    private void generateInvoiceJournalEntry() {

        journalItems = new ArrayList<>();

        String journalEntryReference = ((invoice.getReference() == null) || (invoice.getReference().isEmpty())) ? invoice.getOrigin() : invoice.getReference();

        journalEntry = new JournalEntry(
                invoice.getName(),
                journalEntryReference,
                invoice.getDate(),
                Boolean.TRUE,
                (Journal) super.findSingleWithQuery(JournalQueryBuilder.getFindByCodeQuery("BILL")),
                invoice.getPartner(),
                null,
                invoice,
                "Posted",
                invoice.getAmountTotal());

        journalItems.add(new JournalItem(
                0d,
                invoice.getAmountTotal(),
                invoice.getDate(),
                invoice.getName(),
                journalEntryReference,
                0d,
                0d,
                Boolean.TRUE,
                (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Account Payable")),
                journalEntry,
                journalEntry.getJournal(),
                invoice.getPartner(),
                null,
                null,
                0d,
                null));

        if (invoice.getAmountTax() > 0d) {
            for (InvoiceLine invoiceline : invoice.getInvoiceLines()) {
                if (invoiceline.getTax() != null) {

                    double taxAmount = JsfUtil.round(invoiceline.getPriceSubtotal() * invoiceline.getTax().getAmount());
                    journalItems.add(new JournalItem(
                            taxAmount,
                            0d,
                            invoice.getDate(),
                            invoiceline.getTax().getName(),
                            journalEntryReference,
                            taxAmount,
                            0d,
                            Boolean.TRUE,
                            (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Tax Paid")),
                            journalEntry,
                            journalEntry.getJournal(),
                            invoice.getPartner(),
                            null,
                            null,
                            0d,
                            invoiceline.getTax()));
                }

            }
        }

        for (InvoiceLine invoiceline : invoice.getInvoiceLines()) {

            journalItems.add(new JournalItem(
                    invoiceline.getPriceSubtotal(),
                    0d,
                    invoice.getDate(),
                    invoiceline.getProduct().getName(),
                    journalEntryReference,
                    invoiceline.getPriceSubtotal(),
                    invoiceline.getQuantity(),
                    Boolean.TRUE,
                    (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Product Purchases")),
                    journalEntry,
                    journalEntry.getJournal(),
                    invoice.getPartner(),
                    invoiceline.getProduct(),
                    invoiceline.getProduct().getUom(),
                    0d,
                    null));

        }
        journalEntry.setJournalItems(journalItems);
        journalEntry = super.createItem(journalEntry);
        invoice.setJournalEntry(journalEntry);
        journalItems = null;
        journalEntry = null;
    }

    private void findVendorOutstandingPayments() {

        outstandingPayments = null;

        if (invoice != null && invoice.getState().equals(BillStatus.OPEN.value())) {
            outstandingPayments = super.findWithQuery(PaymentQueryBuilder.getFindOutstandingByVendorQuery(invoice.getPartner().getId()));
        }
    }

    public void payInvoice(Integer id) {

        invoice = super.findItemById(id, Invoice.class);

        if (invoice != null) {

            if (invoice.getState().equals(BillStatus.OPEN.value())) {

                double paidAmount;
                double outstandingPayment = 0d;
                String account;
                payment.setAmount(JsfUtil.round(payment.getAmount()));
                differenceAmount = JsfUtil.round(invoice.getResidual() - payment.getAmount());
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
                    paidAmount = invoice.getResidual();
                    invoice.setResidual(0d);
                    invoice.setState(BillStatus.PAID.value());
                    if (paymentType.equals("keep open")) {
                        invoice.getPartner().setDebit(invoice.getPartner().getDebit() + Math.abs(differenceAmount));
                    }

                } else if (differenceAmount > 0d) {
                    paidAmount = payment.getAmount();
                    if (paymentType.equals("keep open")) {
                        invoice.setResidual(differenceAmount);
                    } else {
                        invoice.setState(BillStatus.PAID.value());
                        invoice.setResidual(0d);
                    }

                } else {
                    paidAmount = payment.getAmount();
                    invoice.setResidual(0d);
                    invoice.setState(BillStatus.PAID.value());
                }

                invoice.getPartner().setCredit(JsfUtil.round(invoice.getPartner().getCredit() - paidAmount));
                payment = new Payment(payment.getAmount(),
                        payment.getDate(),
                        payment.getPartner(),
                        payment.getJournal(),
                        "out", Boolean.TRUE,
                        (Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery(account)),
                        null,
                        invoice,
                        "Posted",
                        null,
                        outstandingPayment,
                        "supplier");

                payment = super.createItem(payment);
                payment.setName(IdGenerator.generateSupplierOutPayment(payment.getId()));
                payment = super.updateItem(payment);

                generatePaymentJournalEntry(account);
                generateInvoicePayment(invoice, payment.getJournalEntry(), netPayment, payment.getName());
                if (differenceAmount != 0d && paymentType.equals("fully paid")) {
                    generatePaymentWriteOffJournalEntry(account);
                }

                payment = super.updateItem(payment);
                invoice.getPayments().add(payment);
                invoice = super.updateItem(invoice);
                invoices.set(invoices.indexOf(invoice), invoice);

                findVendorOutstandingPayments();

                if (invoice.getState().equals(BillStatus.PAID.value())) {
                    setPurchaseOrderStatus();
                }

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "CannotProcessBillPayment");
            }

        } else {
            billNotFound();
        }
    }

    private void generateInvoicePayment(Invoice invoice, JournalEntry journalEntry, Double netPayment, String paymentName) {

        InvoicePayment invoicePayment = new InvoicePayment(invoice, journalEntry, netPayment, journalEntry.getDate(), paymentName);
        invoicePayment = super.createItem(invoicePayment);
        journalEntry.getInvoicePayments().add(invoicePayment);
        invoice.getInvoicePayments().add(invoicePayment);
    }

    private void generatePaymentJournalEntry(String account) {

        journalItems = new ArrayList<>();
        String journalEntryReference = ((payment.getReference() == null) || (payment.getReference().isEmpty())) ? invoice.getOrigin() : payment.getReference();

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
                writeOffAccount,
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
            generateInvoicePayment(invoice, journalEntry, difference, "Write-Off");
        }
    }

    public void payUsingOutstandingPayment(Integer paymentId) {

        payment = super.findItemById(paymentId, Payment.class);
        invoice = super.findItemById(invoice.getId(), Invoice.class);
        Double paidAmount;
        Double newOverPayment;

        if (payment != null && invoice != null) {
            if (payment.getOverpayment() > 0d && invoice.getResidual() > 0d) {

                if (payment.getOverpayment() >= invoice.getResidual()) {
                    paidAmount = invoice.getResidual();
                    newOverPayment = JsfUtil.round(payment.getOverpayment() - invoice.getResidual());
                    invoice.setState(BillStatus.PAID.value());

                } else {
                    paidAmount = payment.getOverpayment();
                    newOverPayment = 0d;
                }

                payment.setOverpayment(newOverPayment);
                invoice.setResidual(JsfUtil.round(invoice.getResidual() - paidAmount));

                generateInvoicePayment(invoice, payment.getJournalEntry(), paidAmount, payment.getName());

                super.updateItem(payment);
                invoice = super.updateItem(invoice);
                invoices.set(invoices.indexOf(invoice), invoice);
                payment = null;

                if (invoice.getState().equals(BillStatus.PAID.value())) {
                    setPurchaseOrderStatus();
                }
                findVendorOutstandingPayments();
            }
        }
    }

    private void setPurchaseOrderStatus() {
        if (invoice.getPurchaseOrder() != null) {
            invoice.getPurchaseOrder().getPurchaseOrderLines().size();
            boolean purchaseOrderInvoiced = true;
            boolean billsPaid = true;

            for (PurchaseOrderLine line : invoice.getPurchaseOrder().getPurchaseOrderLines()) {
                if (line.getInvoiced() == false) {
                    purchaseOrderInvoiced = false;
                }
            }

            if (purchaseOrderInvoiced == true) {
                for (Invoice invoice : this.invoice.getPurchaseOrder().getInvoices()) {
                    if (!invoice.getState().equals(BillStatus.PAID.value())) {
                        billsPaid = false;
                    }
                }
            }

            if (purchaseOrderInvoiced == true && billsPaid == true) {
                invoice.getPurchaseOrder().setPaid(Boolean.TRUE);
                if (invoice.getPurchaseOrder().getShipped() == true) {
                    invoice.getPurchaseOrder().setState("Done");
                }
                super.updateItem(invoice.getPurchaseOrder());
            }
        }
    }

    public void removeOrderLine(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < invoiceLines.size()) {
            invoiceLines.remove(rowIndex);
        }
    }

    public void prepareView() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {
            findVendorOutstandingPayments();
            currentForm = VIEW_URL;
        } else {
            billNotFound();
        }
    }

    public void preparePayment() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {
            if (invoice.getState().equals(BillStatus.OPEN.value())) {
                payment = new Payment();
                if (invoice.getPurchaseOrder() != null) {
                    String paymentReference = ((invoice.getPurchaseOrder().getReference() == null) || (invoice.getPurchaseOrder().getReference().isEmpty())) ? invoice.getPurchaseOrder().getName() : invoice.getPurchaseOrder().getReference();
                    payment.setReference(paymentReference);
                }
                paymentType = "keep open";
                differenceAmount = 0d;
                payment.setAmount(invoice.getResidual());
                payment.setPartner(invoice.getPartner());
                writeOffAccounts = null;
                writeOffAccount = null;
            } else {
                FacesContext.getCurrentInstance().validationFailed();
                JsfUtil.addWarningMessageDialog("InvalidAction", "CannotProcessBillPayment");
            }
        } else {
            billNotFound();
        }
    }

    public void updateDifferenceAmount() {
        payment.setAmount(JsfUtil.round(payment.getAmount()));
        differenceAmount = JsfUtil.round(invoice.getResidual() - payment.getAmount());
        if (differenceAmount != 0d) {
            if ("".equals(paymentType)) {
                paymentType = "keep open";
            } else if ("fully paid".equals(paymentType) && differenceAmount > 0d) {
                writeOffAccounts = super.findWithQuery(AccountQueryBuilder.getFindByNameQuery("Expenses"));
                writeOffAccount = writeOffAccounts.get(0);
            } else if ("fully paid".equals(paymentType) && differenceAmount < 0d) {
                writeOffAccounts = super.findWithQuery(AccountQueryBuilder.getFindByNameQuery("Extra Payment"));
                writeOffAccount = writeOffAccounts.get(0);
            }
        }
    }

    public void onPaymentDifferenceStrategyChange() {

        if ("fully paid".equals(paymentType) && differenceAmount > 0d) {
            writeOffAccounts = super.findWithQuery(AccountQueryBuilder.getFindByNameQuery("Expenses"));
            writeOffAccount = writeOffAccounts.get(0);
        } else if ("fully paid".equals(paymentType) && differenceAmount < 0d) {
            writeOffAccounts = super.findWithQuery(AccountQueryBuilder.getFindByNameQuery("Extra Payment"));
            writeOffAccount = writeOffAccounts.get(0);
        }
    }

    public void updateBill(Integer id) {

        String billCurrentStatus = getBillCurrentStatus();

        if (billCurrentStatus != null) {

            if (!billCurrentStatus.equals(BillStatus.DRAFT.value())) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
                currentForm = VIEW_URL;
            } else if (invoiceLines.isEmpty()) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneInvoiceLineUpdate");
            } else {

                for (InvoiceLine invLine : invoiceLines) {
//                    invLine.setAccount((Account)super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Expenses")));
                    invLine.setPartner(invoice.getPartner());
                    invLine.setInvoice(invoice);
                }

                invoice.setResidual(invoice.getAmountTotal());

                invoice.setInvoiceLines(invoiceLines);
                generateInvoiceTaxes();
                invoice = super.updateItem(invoice);

                if (partialListType == null && invoices != null) {
                    invoices.set(invoices.indexOf(invoice), invoice);
                } else {
                    query = InvoiceQueryBuilder.getFindAllBillsQuery();
                    invoices = super.findWithQuery(query);
                    partialListType = null;
                }

                currentForm = VIEW_URL;
            }
        } else {
            billNotFound();
        }
    }

    public void createBill() {

        if (invoiceLines.isEmpty()) {
            JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneInvoiceLineCreate");
        } else {

            for (InvoiceLine invLine : invoiceLines) {
                invLine.setAccount((Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Product Purchases")));
                invLine.setPartner(invoice.getPartner());
                invLine.setInvoice(invoice);
            }

            invoice.setType("Purchase");
            invoice.setState(BillStatus.DRAFT.value());
            invoice.setActive(Boolean.TRUE);
            invoice.setResidual(invoice.getAmountTotal());
            invoice.setInvoiceLines(invoiceLines);
            generateInvoiceTaxes();

            invoice = super.createItem(invoice);

            invoice.setName(IdGenerator.generateBillId(invoice.getId()));

            invoice = super.updateItem(invoice);

            if (partialListType == null && invoices != null) {
                invoices.add(invoice);
            } else {
                query = InvoiceQueryBuilder.getFindAllBillsQuery();
                invoices = super.findWithQuery(query);
                partialListType = null;
            }

            currentForm = VIEW_URL;
        }
    }

    public void cancelCreateBill() {

        invoiceLine = null;
        invoiceLines = null;
        invoice = null;

        loadActivePurchasedProducts();
        loadActiveVendors();

        currentForm = VIEW_URL;

        resetListAndCurrentItem();
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
//                        (Account)super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Tax Paid")),
//                        invoice,
//                        invoiceline.getTax()));
            }
        }

        invoice.setInvoiceTaxes(invoiceTaxes);
    }

    public void onRowEditInit(InvoiceLine orderLine) {
        invoiceLine = (InvoiceLine) SerializationUtils.clone(orderLine);
    }

    public void onRowEdit(int index) {
        if (index >= 0 && index < invoiceLines.size()) {
            invoiceLines.get(index).setQuantity(JsfUtil.round(invoiceLines.get(index).getQuantity(), invoiceLines.get(index).getProduct().getUom().getDecimals()));
            invoiceLines.get(index).setPrice(JsfUtil.round(invoiceLines.get(index).getPrice()));

            if (invoiceLines.get(index).getQuantity() == 0d) {
                invoiceLines.get(index).setQuantity(1d);
            }

            if (invoiceLines.get(index).getPrice() == 0d) {
                invoiceLines.get(index).setTax(null);
            }

            invoiceLines.get(index).setPriceSubtotal(JsfUtil.round(invoiceLines.get(index).getPrice() * invoiceLines.get(index).getQuantity()));
            SumUpOrder();
        }
    }

    private void SumUpOrder() {

        invoice.setAmountUntaxed(0d);
        invoice.setAmountTax(0d);
        invoice.setAmountTotal(0d);

        for (InvoiceLine orderLine : invoiceLines) {
            invoice.setAmountUntaxed(invoice.getAmountUntaxed() + orderLine.getPriceSubtotal());
            if (orderLine.getTax() != null) {
                invoice.setAmountTax(invoice.getAmountTax() + (orderLine.getPriceSubtotal() * orderLine.getTax().getAmount()));
            }
        }

        invoice.setAmountUntaxed(JsfUtil.round(invoice.getAmountUntaxed()));
        invoice.setAmountTax(JsfUtil.round(invoice.getAmountTax()));
        BigDecimal amountUntaxed = BigDecimal.valueOf(invoice.getAmountUntaxed());
        BigDecimal amountTax = BigDecimal.valueOf(invoice.getAmountTax());
        BigDecimal amountTotal = amountUntaxed.add(amountTax);
        invoice.setAmountTotal(JsfUtil.round(amountTotal.doubleValue()));
    }

    public void onRowCancel(int index) {
        if (index >= 0 && index < invoiceLines.size()) {
            invoiceLines.remove(index);
            invoiceLines.add(index, invoiceLine);
            invoiceLine = new InvoiceLine();
            if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
                invoiceLine.setProduct(topNActivePurchasedProducts.get(0));
                invoiceLine.setPrice(invoiceLine.getProduct().getPurchasePrice());
                invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
            }
        }
    }

    public void onRowCancel() {
        invoiceLine = new InvoiceLine();
        if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
            invoiceLine.setProduct(topNActivePurchasedProducts.get(0));
            invoiceLine.setPrice(invoiceLine.getProduct().getPurchasePrice());
            invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
        }
    }

    public void onRowDelete(int index) {
        if (index >= 0 && index < invoiceLines.size()) {
            invoiceLines.remove(index);
            SumUpOrder();
        }
    }

    public void onProductChange() {

        invoiceLine.setPrice(invoiceLine.getProduct().getPurchasePrice());
        invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
    }

    public void onProductChange(int index) {
        if (index >= 0 && index < invoiceLines.size()) {
            invoiceLines.get(index).setPrice(invoiceLines.get(index).getProduct().getPurchasePrice());
            invoiceLines.get(index).setUom(invoiceLines.get(index).getProduct().getUom().getName());
        }
    }

    public void onRowAdd(ActionEvent event) {

        invoiceLine.setActive(Boolean.TRUE);
        invoiceLine.setPrice(JsfUtil.round(invoiceLine.getPrice()));
        invoiceLine.setQuantity(JsfUtil.round(invoiceLine.getQuantity(), invoiceLine.getProduct().getUom().getDecimals()));

        if (invoiceLine.getQuantity() == 0d) {
            invoiceLine.setQuantity(1d);
        }

        if (invoiceLine.getPrice() == 0d) {
            invoiceLine.setTax(null);
        }

        invoiceLine.setPriceSubtotal(JsfUtil.round((invoiceLine.getPrice()) * (invoiceLine.getQuantity())));
        invoiceLines.add(invoiceLine);
        SumUpOrder();
        invoiceLine = new InvoiceLine();
        if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
            invoiceLine.setProduct(topNActivePurchasedProducts.get(0));
            invoiceLine.setPrice(topNActivePurchasedProducts.get(0).getPurchasePrice());
            invoiceLine.setUom(topNActivePurchasedProducts.get(0).getUom().getName());
        }

    }

    public void onSelectSupplier() {
        
        if (supplier != null) {
            if (topNActiveVendors != null && !topNActiveVendors.contains(supplier)) {
                topNActiveVendors.add(supplier);
            }
            invoice.setPartner(supplier);
        }
    }

    public void onSelectProduct() {

        if (product != null) {
            if (!topNActivePurchasedProducts.contains(product)) {
                topNActivePurchasedProducts.add(product);
            }

            if (rowIndex < 0) {

                invoiceLine.setProduct(product);
                invoiceLine.setPrice(product.getPurchasePrice());
                invoiceLine.setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("mainForm:productMenuTwo");
                RequestContext.getCurrentInstance().update("mainForm:price");

            } else {

                invoiceLines.get(rowIndex).setProduct(product);
                invoiceLines.get(rowIndex).setPrice(product.getPurchasePrice());
                invoiceLines.get(rowIndex).setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("mainForm:datalist:" + rowIndex + ":productMenu");
                RequestContext.getCurrentInstance().update("mainForm:datalist:" + rowIndex + ":pricee");
            }
        }
    }

    public void prepareCreateBill() {

        invoice = new Invoice();
        invoiceLines = new ArrayList<>();
        invoiceLine = new InvoiceLine();
        invoice.setAccount((Account) super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Account Payable")));
        invoice.setJournal((Journal) super.findSingleWithQuery(JournalQueryBuilder.getFindByCodeQuery("BILL")));

        loadActivePurchasedProducts();
        loadActiveVendors();

        if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
            invoiceLine.setProduct(topNActivePurchasedProducts.get(0));
            invoiceLine.setPrice(invoiceLine.getProduct().getPurchasePrice());
            invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
        }
        currentForm = CREATE_URL;
    }

    public void prepareUpdateBill() {

        invoice = super.findItemById(invoice.getId(), Invoice.class);

        if (invoice != null) {

            if (invoice.getState().equals(BillStatus.DRAFT.value())) {
                invoiceLine = new InvoiceLine();
                invoiceLines = invoice.getInvoiceLines();

                loadActivePurchasedProducts();
                loadActiveVendors();

                if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
                    invoiceLine.setProduct(topNActivePurchasedProducts.get(0));
                    invoiceLine.setPrice(invoiceLine.getProduct().getPurchasePrice());
                    invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
                }

                if (!topNActiveVendors.contains(invoice.getPartner())) {
                    topNActiveVendors.add(invoice.getPartner());
                }

                for (InvoiceLine orderLine : invoiceLines) {
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

    public void cancelUpdateBill() {

        invoiceLine = null;
        invoiceLines = null;
        topNActiveVendors = null;
        topNActivePurchasedProducts = null;

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {
            currentForm = VIEW_URL;
        } else {
            billNotFound();
        }
    }

    public void cancelCreateInvoice() {

        invoiceLine = null;
        invoiceLines = null;
        invoice = null;
        topNActiveVendors = null;
        topNActivePurchasedProducts = null;

        currentForm = VIEW_URL;

        resetListAndCurrentItem();
    }

    public void deleteBill() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {

            if (invoice.getState().equals(BillStatus.CANCELLED.value())) {
                cancelBillRelations();

                boolean deleted = super.deleteItem(invoice);

                if (deleted) {

                    JsfUtil.addSuccessMessage("ItemDeleted");
                    currentForm = VIEW_URL;

                    if (invoices != null && invoice != null) {
                        invoices.remove(invoice);
                    }

                    resetListAndCurrentItem();

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

        if (invoice.getPurchaseOrder() != null) {
            PurchaseOrder purchaseOrder = super.findItemById(invoice.getPurchaseOrder().getId(), PurchaseOrder.class);
            purchaseOrder.getInvoices().size();
            purchaseOrder.getInvoices().remove(invoice);
            invoice.setPurchaseOrder(null);
            super.updateItem(purchaseOrder);
        }
    }

    private String getBillCurrentStatus() {

        if (invoice != null) {

            Invoice tempItem = super.findItemById(invoice.getId(), invoice.getClass());

            if (tempItem != null) {
                return tempItem.getState();
            } else {
                return null;
            }
        }
        return null;
    }

    public void cancelBill() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {

            if (!invoice.getState().equals(BillStatus.CANCELLED.value())) {

                if (invoice.getState().equals(BillStatus.OPEN.value()) || invoice.getState().equals(BillStatus.PAID.value())) {
                    cancelPaymentds();
                    invoice.getJournalEntry().setState("Unposted");
                    super.updateItem(invoice.getJournalEntry());
                }
                invoice.setState(BillStatus.CANCELLED.value());
                invoice.setResidual(0d);
                invoice = super.updateItem(invoice);
                invoices.set(invoices.indexOf(invoice), invoice);
                findVendorOutstandingPayments();

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }

        } else {
            billNotFound();
        }
    }

    private void cancelPaymentds() {
        if (invoice.getInvoicePayments() != null && !invoice.getInvoicePayments().isEmpty()) {
            JsfUtil.addSuccessMessageDialog("Info", "BillPaymentToOutstanding");
            for (InvoicePayment invoicePayment : invoice.getInvoicePayments()) {
                if (invoicePayment.getJournalEntry().getPayment() != null) {
                    invoicePayment.getJournalEntry().getPayment().setOverpayment(invoicePayment.getJournalEntry().getPayment().getOverpayment() + invoicePayment.getPaidAmount());
                    invoicePayment.getJournalEntry().getPayment().setInvoice(null);
                    super.updateItem(invoicePayment.getJournalEntry().getPayment());
                    super.deleteItem(invoicePayment);
                }
            }
            invoice.setPayments(null);
            invoice.setInvoicePayments(null);
        }
    }

    public void duplicateBill() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {

            invoice.getInvoiceLines().size();
            invoice.getInvoiceTaxes().size();
            Invoice newInvoice = (Invoice) SerializationUtils.clone(invoice);

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

            for (InvoiceLine invLine : newInvoice.getInvoiceLines()) {
                invLine.setId(null);
                invLine.setInvoice(newInvoice);
            }

            for (InvoiceTax invTax : newInvoice.getInvoiceTaxes()) {
                invTax.setId(null);
                invTax.setInvoice(newInvoice);
            }

            invoice = newInvoice;
            invoiceLine = new InvoiceLine();
            invoiceLines = invoice.getInvoiceLines();

            loadActivePurchasedProducts();
            loadActiveVendors();

            if (topNActivePurchasedProducts != null && !topNActivePurchasedProducts.isEmpty()) {
                invoiceLine.setProduct(topNActivePurchasedProducts.get(0));
                invoiceLine.setPrice(invoiceLine.getProduct().getPurchasePrice());
                invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
            }

            if (!topNActiveVendors.contains(invoice.getPartner())) {
                topNActiveVendors.add(invoice.getPartner());
            }

            for (InvoiceLine orderLine : invoiceLines) {
                if (!topNActivePurchasedProducts.contains(orderLine.getProduct())) {
                    topNActivePurchasedProducts.add(orderLine.getProduct());
                }
            }

            currentForm = CREATE_URL;

        } else {
            billNotFound();
        }
    }

    public void printBill() throws IOException, JRException {

        for (InvoiceLine orderLine : invoice.getInvoiceLines()) {
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
        params.put("bill", invoice);
        params.put("partner", invoice.getPartner());
        params.put("orderLines", invoice.getInvoiceLines());
        params.put("currency", currency);
        params.put("SUBREPORT_DIR", FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/") + "/");

        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, params, new JREmptyDataSource());
//      JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, new HashMap<String,Object>(), new JRBeanArrayDataSource(new SaleOrder[]{saleOrder}));  
        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        httpServletResponse.addHeader("Content-disposition", "attachment; filename=" + name + "_" + invoice.getName() + ".pdf");
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
        FacesContext.getCurrentInstance().responseComplete();

    }

    private void billNotFound() {

        JsfUtil.addWarningMessage("ItemDoesNotExist");
        currentForm = VIEW_URL;

        if (invoices != null && invoice != null) {
            invoices.remove(invoice);
        }

        resetListAndCurrentItem();
    }

    private void resetListAndCurrentItem() {

        if (invoices != null && !invoices.isEmpty()) {
            invoice = invoices.get(0);
        } else {
            partialListType = null;
            query = InvoiceQueryBuilder.getFindAllBillsQuery();
            invoices = super.findWithQuery(query);

            if ((invoices != null) && !invoices.isEmpty()) {
                invoice = invoices.get(0);
            }
        }

        findVendorOutstandingPayments();
    }

    public String getStatus(String status) {
        return statuses.get(status);
    }

    public String getStatusColor(String status) {
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
        if (invoices != null && invoice != null) {
            return invoices.indexOf(invoice) + 1;
        }
        return 0;
    }

    public void nextInvoice() {
        if (invoices != null && invoice != null) {
            if (invoices.indexOf(invoice) == (invoices.size() - 1)) {
                invoice = invoices.get(0);
            } else {
                invoice = invoices.get(invoices.indexOf(invoice) + 1);
            }
            findVendorOutstandingPayments();
        }
    }

    public void previousInvoice() {
        if (invoices != null && invoice != null) {
            if (invoices.indexOf(invoice) == 0) {
                invoice = invoices.get(invoices.size() - 1);
            } else {
                invoice = invoices.get(invoices.indexOf(invoice) - 1);
            }
            findVendorOutstandingPayments();
        }
    }

    public void setRowIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        rowIndex = Integer.valueOf(params.get("rowIndex"));
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

    public List<Account> getBillAccounts() {
        query = AccountQueryBuilder.getFindByNameQuery("Account Payable");
        return super.findWithQuery(query);
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public InvoiceLine getInvoiceLine() {
        return invoiceLine;
    }

    public void setInvoiceLine(InvoiceLine invoiceLine) {
        this.invoiceLine = invoiceLine;
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }

    public List<InvoiceLine> getInvoiceLines() {
        return invoiceLines;
    }

    public void setInvoiceLines(List<InvoiceLine> invoiceLines) {
        this.invoiceLines = invoiceLines;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public List<Invoice> getFilteredInvoices() {
        return filteredInvoices;
    }

    public void setFilteredInvoices(List<Invoice> filteredInvoices) {
        this.filteredInvoices = filteredInvoices;
    }

    public String getPartialListType() {
        return partialListType;
    }

    public String getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(String purchaseId) {
        this.purchaseId = purchaseId;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public List<Payment> getOutstandingPayments() {
        return outstandingPayments;
    }

    public void setOutstandingPayments(List<Payment> outstandingPayments) {
        this.outstandingPayments = outstandingPayments;
    }

    public Double getDifferenceAmount() {
        return differenceAmount;
    }

    public void setDifferenceAmount(Double differenceAmount) {
        this.differenceAmount = differenceAmount;
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

    public List<Partner> getTopNActiveVendors() {
        return topNActiveVendors;
    }

    public List<Partner> getActiveVendors() {
        return activeVendors;
    }

    public List<Partner> getFilteredActiveVendors() {
        return filteredActiveVendors;
    }

    public List<Product> getTopNActivePurchasedProducts() {
        return topNActivePurchasedProducts;
    }

    public List<Product> getActivePurchasedProducts() {
        return activePurchasedProducts;
    }

    public List<Product> getFilteredActivePurchasedProducts() {
        return filteredActivePurchasedProducts;
    }

    public Account getWriteOffAccount() {
        return writeOffAccount;
    }

    public void setWriteOffAccount(Account writeOffAccount) {
        this.writeOffAccount = writeOffAccount;
    }

    public List<Account> getPaymentWriteOffAccounts() {
        return writeOffAccounts;
    }

    public void setPaymentWriteOffAccounts(List<Account> writeOffAccounts) {
        this.writeOffAccounts = writeOffAccounts;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

}
