package com.defterp.modules.accounting.controllers;

import com.defterp.modules.accounting.queryBuilders.*;
import com.defterp.modules.accounting.entities.*;
import com.defterp.modules.accounting.constants.InvoiceStatus;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.inventory.queryBuilders.ProductQueryBuilder;
import com.defterp.modules.inventory.entities.Product;
import com.defterp.modules.partners.queryBuilders.PartnerQueryBuilder;
import com.defterp.modules.partners.entities.Partner;
import com.defterp.modules.sales.entities.SaleOrder;
import com.defterp.modules.sales.entities.SaleOrderLine;
import com.defterp.translation.annotations.Countries;
import static com.defterp.translation.annotations.Countries.Version.SECOND;
import com.defterp.modules.commonClasses.IdGenerator;
import com.defterp.util.JsfUtil;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.translation.annotations.Status;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.context.FacesContext;
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

@Named("invoiceController")
@ViewScoped
public class InvoiceController extends AbstractController {

    @Inject
    @Status
    private HashMap<String, String> statuses;
    @Inject
    @Countries(version = SECOND)
    private HashMap<String, String> countries;
    protected Invoice invoice;
    protected List<Invoice> invoices;
    protected List<Invoice> filteredInvoices;
    private List<InvoiceLine> invoiceLines;
    private InvoiceLine invoiceLine;
    private Payment payment;
    private List<Payment> outstandingPayments;
    private String partialListType;
    private String invoiceId;
    private String saleId;
    private String partnerId;
    private Account writeOffAccount;
    private List<Account> writeOffAccounts;
    private String paymentType = "";
    private double differenceAmount;
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

    public InvoiceController() {
        super("/sc/invoice/");
    }

    public void resolveRequestParams() {

        currentForm = VIEW_URL;

        if (JsfUtil.isNumeric(invoiceId)) {
            Integer InvId = Integer.valueOf(invoiceId);
            invoice = super.findItemById(InvId, Invoice.class);
            if (invoice != null) {
                query = InvoiceQueryBuilder.getFindAllInvoicesQuery();
                invoices = super.findWithQuery(query);
                findCustomerOutstandingPayments();
                return;
            }
        }

        if (JsfUtil.isNumeric(saleId)) {
            Integer id = Integer.valueOf(saleId);
            query = InvoiceQueryBuilder.getFindBySaleOrderQuery(id);
            invoices = super.findWithQuery(query);
            if ((invoices != null) && (!invoices.isEmpty())) {
                invoice = invoices.get(0);
                findCustomerOutstandingPayments();
                partialListType = "saleOrder";
                return;
            }
        }

        if (JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);

            query = InvoiceQueryBuilder.getFindByCustomerQuery(id);
            invoices = super.findWithQuery(query);
            if ((invoices != null) && (!invoices.isEmpty())) {
                invoice = invoices.get(0);
                findCustomerOutstandingPayments();
                partialListType = "partner";
                return;
            }
        }

        query = InvoiceQueryBuilder.getFindAllInvoicesQuery();
        invoices = super.findWithQuery(query);

        if (invoices != null && !invoices.isEmpty()) {
            invoice = invoices.get(0);
            findCustomerOutstandingPayments();
        }
    }

    public void validateInvoice() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {
            if (invoice.getState().equals(InvoiceStatus.DRAFT.value())) {
                if (invoice.getAmountTotal() == 0d) {
                    invoice.setState(InvoiceStatus.PAID.value());
                    updateSaleOrderStatus();
                } else {
                    invoice.setState(InvoiceStatus.OPEN.value());
                }
                invoice.setJournalEntry(generateInvoiceJournalEntry());
                invoice.getPartner().setDebit(invoice.getPartner().getDebit() + invoice.getAmountTotal());
                invoice = super.updateItem(invoice);
                invoices.set(invoices.indexOf(invoice), invoice);
                findCustomerOutstandingPayments();
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorValidate");
            }
        } else {
            invoiceNotFound();
        }
    }

    private JournalEntry generateInvoiceJournalEntry() {

        JournalEntry journalEntry = new JournalEntry();
        List<JournalItem> journalItems = new ArrayList();
        JournalItem journalItem = new JournalItem();
        String ref = invoice.getSaleOrder() == null ? null : invoice.getSaleOrder().getName();

        query = JournalQueryBuilder.getFindByCodeQuery("INV");
        journalEntry.setJournal((Journal) super.findSingleWithQuery(query));
        journalEntry.setName(invoice.getName());
        journalEntry.setRef(ref);
        journalEntry.setDate(invoice.getDate());
        journalEntry.setActive(Boolean.TRUE);
        journalEntry.setPartner(invoice.getPartner());
        journalEntry.setPayment(null);
        journalEntry.setInvoice(invoice);
        journalEntry.setState("Posted");
        journalEntry.setAmount(invoice.getAmountTotal());

        query = AccountQueryBuilder.getFindByNameQuery("Account Receivable");
        journalItem.setAccount((Account) super.findSingleWithQuery(query));
        journalItem.setDebit(invoice.getAmountTotal());
        journalItem.setCredit(0d);
        journalItem.setDate(invoice.getDate());
        journalItem.setName(invoice.getName());
        journalItem.setRef(ref);
        journalItem.setTaxAmount(0d);
        journalItem.setQuantity(0d);
        journalItem.setActive(Boolean.TRUE);
        journalItem.setJournalEntry(journalEntry);
        journalItem.setJournal(journalEntry.getJournal());
        journalItem.setPartner(invoice.getPartner());
        journalItem.setProduct(null);
        journalItem.setUom(null);
        journalItem.setCostOfGoodsSold(0d);
        journalItem.setTax(null);

        journalItems.add(journalItem);
        for (InvoiceLine invoiceline : invoice.getInvoiceLines()) {
            journalItem = new JournalItem();

            query = AccountQueryBuilder.getFindByNameQuery("Product Sales");
            journalItem.setAccount((Account) super.findSingleWithQuery(query));
            journalItem.setDebit(0d);
            journalItem.setCredit(invoiceline.getPriceSubtotal());
            journalItem.setDate(invoice.getDate());
            journalItem.setName(invoiceline.getProduct().getName());
            journalItem.setRef(ref);
            journalItem.setTaxAmount(invoiceline.getPriceSubtotal());
            journalItem.setQuantity(invoiceline.getQuantity());
            journalItem.setActive(Boolean.TRUE);
            journalItem.setJournalEntry(journalEntry);
            journalItem.setJournal(journalEntry.getJournal());
            journalItem.setPartner(invoice.getPartner());
            journalItem.setProduct(invoiceline.getProduct());
            journalItem.setUom(invoiceline.getProduct().getUom());
            journalItem.setCostOfGoodsSold(JsfUtil.round(invoiceline.getQuantity() * invoiceline.getProduct().getInventory().getUnitCost()));
            journalItem.setTax(null);

            journalItems.add(journalItem);
        }
        for (InvoiceLine invoiceline : invoice.getInvoiceLines()) {
            if (invoiceline.getTax() != null) {
                journalItem = new JournalItem();
                double taxAmount = JsfUtil.round(invoiceline.getPriceSubtotal() * invoiceline.getTax().getAmount());

                query = AccountQueryBuilder.getFindByNameQuery("Tax Received");
                Account account = (Account) super.findSingleWithQuery(query);
                journalItem.setAccount(account);
                journalItem.setDebit(0d);
                journalItem.setCredit(taxAmount);
                journalItem.setDate(invoice.getDate());
                journalItem.setName(invoiceline.getTax().getName());
                journalItem.setRef(ref);
                journalItem.setTaxAmount(taxAmount);
                journalItem.setQuantity(0d);
                journalItem.setActive(Boolean.TRUE);
                journalItem.setJournalEntry(journalEntry);
                journalItem.setJournal(journalEntry.getJournal());
                journalItem.setPartner(invoice.getPartner());
                journalItem.setProduct(null);
                journalItem.setUom(null);
                journalItem.setCostOfGoodsSold(0d);
                journalItem.setTax(invoiceline.getTax());

                journalItems.add(journalItem);
            }
        }
        journalEntry.setJournalItems(journalItems);
        journalEntry = super.createItem(journalEntry);

        return journalEntry;
    }

    public void payInvoice() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {
            if (invoice.getState().equals(InvoiceStatus.OPEN.value())) {
                double outstandingPayment = 0d;
                String accountName;
                double paidAmount;
                payment.setAmount(JsfUtil.round(payment.getAmount()));
                differenceAmount = JsfUtil.round(invoice.getResidual() - payment.getAmount());
                double overPayment = differenceAmount < 0d ? Math.abs(differenceAmount) : 0d;
                double netPayment = JsfUtil.round(payment.getAmount() - overPayment);
                if ((differenceAmount < 0d) && (paymentType.equals("keep open"))) {
                    outstandingPayment = Math.abs(differenceAmount);
                }

                if (payment.getJournal().getName().equals("Cash")) {
                    accountName = "Cash";
                } else {
                    accountName = "Bank";
                }

                if (differenceAmount < 0d) {
                    paidAmount = invoice.getResidual();
                    invoice.setResidual(0d);
                    invoice.setState(InvoiceStatus.PAID.value());
                    if (paymentType.equals("keep open")) {
                        invoice.getPartner().setCredit(invoice.getPartner().getCredit() + Math.abs(differenceAmount));
                    }
                } else if (differenceAmount > 0d) {
                    paidAmount = payment.getAmount();
                    if (paymentType.equals("keep open")) {
                        invoice.setResidual(differenceAmount);
                    } else {
                        invoice.setState(InvoiceStatus.PAID.value());
                        invoice.setResidual(0d);
                    }
                } else {

                    paidAmount = payment.getAmount();
                    invoice.setResidual(0d);
                    invoice.setState(InvoiceStatus.PAID.value());
                }

                invoice.getPartner().setDebit(JsfUtil.round(invoice.getPartner().getDebit() - paidAmount));

                query = AccountQueryBuilder.getFindByNameQuery(accountName);
                payment.setAccount((Account) super.findSingleWithQuery(query));
                payment.setAmount(payment.getAmount());
                payment.setDate(payment.getDate());
                payment.setJournal(payment.getJournal());
                payment.setPartner(invoice.getPartner());
                payment.setType("in");
                payment.setActive(Boolean.TRUE);
                payment.setJournalEntry(null);
                payment.setInvoice(invoice);
                payment.setState("Posted");
                payment.setReference(null);
                payment.setOverpayment(outstandingPayment);
                payment.setPartnerType("customer");

                payment = super.createItem(payment);
                payment.setName(IdGenerator.generateCustomerInPayment(payment.getId()));
                payment = super.updateItem(payment);

                payment.setJournalEntry(generatePaymentJournalEntry(accountName));

                generateInvoicePayment(invoice, payment.getJournalEntry(), netPayment, payment.getName());

                if ((differenceAmount != 0d) && (paymentType.equals("fully paid"))) {
                    generatePaymentWriteOffJournalEntry(accountName);
                }

                payment = super.updateItem(payment);

                System.out.println("JournalEntry:" + payment.getJournalEntry().getName());
                invoice.getPayments().add(payment);
                invoice = super.updateItem(invoice);
                invoices.set(invoices.indexOf(invoice), invoice);
                findCustomerOutstandingPayments();
                if (invoice.getState().equals(InvoiceStatus.PAID.value())) {
                    updateSaleOrderStatus();
                }
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "CannotProcessInvoicePayment");
            }
        } else {
            invoiceNotFound();
        }
    }

    private void generateInvoicePayment(Invoice invoice, JournalEntry journalEntry, Double netPayment, String paymentName) {

        InvoicePayment invoicePayment = new InvoicePayment();
        invoicePayment.setInvoice(invoice);
        invoicePayment.setJournalEntry(journalEntry);
        invoicePayment.setPaidAmount(netPayment);
        invoicePayment.setDate(journalEntry.getDate());
        invoicePayment.setName(paymentName);

        invoicePayment = super.createItem(invoicePayment);
        journalEntry.getInvoicePayments().add(invoicePayment);
        invoice.getInvoicePayments().add(invoicePayment);
    }

    private JournalEntry generatePaymentJournalEntry(String account) {

        List<JournalItem> journalItems = new ArrayList();
        JournalItem journalItem = new JournalItem();
        JournalEntry journalEntry = new JournalEntry();

        query = JournalQueryBuilder.getFindByCodeQuery(account);
        journalEntry.setJournal((Journal) super.findSingleWithQuery(query));

        journalEntry.setRef(payment.getInvoice().getOrigin());
        journalEntry.setDate(payment.getDate());
        journalEntry.setActive(Boolean.TRUE);
        journalEntry.setPartner(payment.getPartner());
        journalEntry.setPayment(payment);
        journalEntry.setInvoice(null);
        journalEntry.setState("Posted");
        journalEntry.setAmount(payment.getAmount());

        query = AccountQueryBuilder.getFindByNameQuery("Account Receivable");
        journalItem.setAccount((Account) super.findSingleWithQuery(query));
        journalItem.setDebit(0d);
        journalItem.setCredit(payment.getAmount());
        journalItem.setDate(payment.getDate());
        journalItem.setName("Customer Payment: " + payment.getInvoice().getName());
        journalItem.setRef(payment.getInvoice().getOrigin());
        journalItem.setTaxAmount(0d);
        journalItem.setQuantity(0d);
        journalItem.setActive(Boolean.TRUE);
        journalItem.setJournalEntry(journalEntry);
        journalItem.setJournal(journalEntry.getJournal());
        journalItem.setPartner(payment.getPartner());
        journalItem.setProduct(null);
        journalItem.setUom(null);
        journalItem.setCostOfGoodsSold(0d);
        journalItem.setTax(null);

        journalItems.add(journalItem);

        journalItem = new JournalItem();

        query = AccountQueryBuilder.getFindByNameQuery(account);
        journalItem.setAccount((Account) super.findSingleWithQuery(query));
        journalItem.setDebit(payment.getAmount());
        journalItem.setCredit(0d);
        journalItem.setDate(payment.getDate());
        journalItem.setName(payment.getName());
        journalItem.setRef(payment.getInvoice().getOrigin());
        journalItem.setTaxAmount(0d);
        journalItem.setQuantity(0d);
        journalItem.setActive(Boolean.TRUE);
        journalItem.setJournalEntry(journalEntry);
        journalItem.setJournal(journalEntry.getJournal());
        journalItem.setPartner(payment.getPartner());
        journalItem.setProduct(null);
        journalItem.setUom(null);
        journalItem.setCostOfGoodsSold(0d);
        journalItem.setTax(null);

        journalItems.add(journalItem);

        journalEntry.setJournalItems(journalItems);

        journalEntry = super.createItem(journalEntry);

        if (account.equals("Cash")) {
            journalEntry.setName(IdGenerator.generatePaymentCashEntryId(journalEntry.getId()));
        } else if (account.equals("Bank")) {
            journalEntry.setName(IdGenerator.generatePaymentBankEntryId(journalEntry.getId()));
        }

        journalEntry = super.updateItem(journalEntry);

        System.out.println("--------------journalEntry's payment-------------" + journalEntry.getPayment().getName());

        return journalEntry;
    }

    private JournalEntry generatePaymentWriteOffJournalEntry(String account) {
        List<JournalItem> journalItems = new ArrayList();
        JournalItem journalItem = new JournalItem();
        JournalEntry journalEntry = new JournalEntry();

        double difference = Math.abs(differenceAmount);
        double receivableDebit;
        double writeOffCredit;
        double writeOffDebit;
        double receivableCredit;

        if (differenceAmount < 0d) {
            writeOffCredit = difference;
            writeOffDebit = 0d;
            receivableCredit = 0d;
            receivableDebit = difference;
        } else {
            writeOffCredit = 0d;
            writeOffDebit = difference;
            receivableCredit = difference;
            receivableDebit = 0d;
        }

        query = JournalQueryBuilder.getFindByCodeQuery(account);
        journalEntry.setJournal((Journal) super.findSingleWithQuery(query));

        journalEntry.setRef(payment.getInvoice().getOrigin());
        journalEntry.setDate(payment.getDate());
        journalEntry.setActive(Boolean.TRUE);
        journalEntry.setPartner(payment.getPartner());
        journalEntry.setPayment(null);
        journalEntry.setInvoice(null);
        journalEntry.setState("Posted");
        journalEntry.setAmount(difference);

        query = AccountQueryBuilder.getFindByNameQuery("Account Receivable");
        journalItem.setAccount((Account) super.findSingleWithQuery(query));

        journalItem.setDebit(receivableDebit);
        journalItem.setCredit(receivableCredit);
        journalItem.setDate(payment.getDate());
        journalItem.setName("Write-Off");
        journalItem.setRef(payment.getInvoice().getOrigin());
        journalItem.setTaxAmount(0d);
        journalItem.setQuantity(0d);
        journalItem.setActive(Boolean.TRUE);
        journalItem.setJournalEntry(journalEntry);
        journalItem.setJournal(journalEntry.getJournal());
        journalItem.setPartner(payment.getPartner());
        journalItem.setProduct(null);
        journalItem.setUom(null);
        journalItem.setCostOfGoodsSold(0d);
        journalItem.setTax(null);

        journalItems.add(journalItem);
        journalItem = new JournalItem();

        journalItem.setAccount(writeOffAccount);
        journalItem.setDebit(writeOffDebit);
        journalItem.setCredit(writeOffCredit);
        journalItem.setDate(payment.getDate());
        journalItem.setName("Write-Off");
        journalItem.setRef(payment.getInvoice().getOrigin());
        journalItem.setTaxAmount(0d);
        journalItem.setQuantity(0d);
        journalItem.setActive(Boolean.TRUE);
        journalItem.setJournalEntry(journalEntry);
        journalItem.setJournal(journalEntry.getJournal());
        journalItem.setPartner(payment.getPartner());
        journalItem.setProduct(null);
        journalItem.setUom(null);
        journalItem.setCostOfGoodsSold(0d);
        journalItem.setTax(null);

        journalItems.add(journalItem);

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
        return journalEntry;
    }

    private void updateSaleOrderStatus() {
        if (this.invoice.getSaleOrder() != null) {
            this.invoice.getSaleOrder().getSaleOrderLines().size();
            boolean saleOrderInvoiced = true;
            boolean invoicesPaid = true;
            for (SaleOrderLine line : this.invoice.getSaleOrder().getSaleOrderLines()) {
                if (!line.getInvoiced()) {
                    saleOrderInvoiced = false;
                }
            }
            if (saleOrderInvoiced == true) {
                for (Invoice invoice : this.invoice.getSaleOrder().getInvoices()) {
                    if (!invoice.getState().equals(InvoiceStatus.PAID.value())) {
                        invoicesPaid = false;
                    }
                }
            }
            if ((saleOrderInvoiced == true) && (invoicesPaid == true)) {
                this.invoice.getSaleOrder().setPaid(Boolean.TRUE);
                if (this.invoice.getSaleOrder().getShipped() == true) {
                    this.invoice.getSaleOrder().setState("Done");
                }
                super.updateItem(this.invoice.getSaleOrder());
            }
        }
    }

    public void payUsingOutstandingPayment(Integer paymentId) {

        invoice = super.findItemById(invoice.getId(), Invoice.class);

        if (invoice != null) {

            payment = super.findItemById(paymentId, Payment.class);

            if ((payment != null) && (payment.getOverpayment() > 0d) && (invoice.getResidual() > 0d)) {

                Double paidAmount;
                Double newOverPayment;

                if (payment.getOverpayment() >= invoice.getResidual()) {
                    paidAmount = invoice.getResidual();
                    newOverPayment = JsfUtil.round(payment.getOverpayment() - invoice.getResidual());
                    invoice.setState(InvoiceStatus.PAID.value());
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
                if (invoice.getState().equals(InvoiceStatus.PAID.value())) {
                    updateSaleOrderStatus();
                }
                findCustomerOutstandingPayments();
            }
        } else {
            invoiceNotFound();
        }
    }

    public void removeOrderLine(int rowIndex) {
        if ((rowIndex >= 0) && (rowIndex < invoiceLines.size())) {
            invoiceLines.remove(rowIndex);
        }
    }

    private void findCustomerOutstandingPayments() {

        outstandingPayments = null;

        if (invoice != null && invoice.getState().equals(InvoiceStatus.OPEN.value())) {
            query = PaymentQueryBuilder.getFindOutstandingByCustomerQuery(invoice.getPartner().getId());
            outstandingPayments = super.findWithQuery(query);
        }
    }

    public void prepareViewInvoice() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {
            findCustomerOutstandingPayments();
            currentForm = VIEW_URL;
        } else {
            invoiceNotFound();
        }
    }

    public String getStatus(String status) {
        if (statuses != null) {
            return statuses.get(status);
        }
        return "";
    }

    public String getStatusColor(String status) {
        switch (status) {
            case "Draft":
                return "#009fd4";
            case "Open":
                return "#406098";
            case "Paid":
                return "#3477db";
        }
        return "#6d8891";
    }

    public void preparePayment() {

        invoice = super.findItemById(invoice.getId(), Invoice.class);

        if (invoice != null) {
            if (invoice.getState().equals(InvoiceStatus.OPEN.value())) {
                payment = new Payment();
                paymentType = "keep open";
                differenceAmount = 0d;
                payment.setAmount(invoice.getResidual());
                payment.setPartner(invoice.getPartner());
                writeOffAccounts = null;
                writeOffAccount = null;
            } else {
                FacesContext.getCurrentInstance().validationFailed();
                JsfUtil.addWarningMessageDialog("InvalidAction", "CannotProcessInvoicePayment");
            }
        } else {
            invoiceNotFound();
        }
    }

    public void updateDifferenceAmount() {

        payment.setAmount(JsfUtil.round(payment.getAmount()));
        differenceAmount = JsfUtil.round(invoice.getResidual() - payment.getAmount());

        if (differenceAmount != 0d) {
            if ("".equals(paymentType)) {
                paymentType = "keep open";
            } else if (("fully paid".equals(paymentType)) && (differenceAmount > 0d)) {
                query = AccountQueryBuilder.getFindByNameQuery("Expenses");
                writeOffAccounts = super.findWithQuery(query);
                if (writeOffAccounts != null && !writeOffAccounts.isEmpty()) {
                    writeOffAccount = writeOffAccounts.get(0);
                }
            } else if (("fully paid".equals(paymentType)) && (differenceAmount < 0d)) {
                query = AccountQueryBuilder.getFindByNameQuery("Extra Payment");
                writeOffAccounts = super.findWithQuery(query);
                if (writeOffAccounts != null && !writeOffAccounts.isEmpty()) {
                    writeOffAccount = writeOffAccounts.get(0);
                }
            }
        }
    }

    public void onPaymentDifferenceStrategyChange() {
        if (("fully paid".equals(paymentType)) && (differenceAmount > 0d)) {
            query = AccountQueryBuilder.getFindByNameQuery("Expenses");
            writeOffAccounts = super.findWithQuery(query);
            if (writeOffAccounts != null && !writeOffAccounts.isEmpty()) {
                writeOffAccount = writeOffAccounts.get(0);
            }
        } else if (("fully paid".equals(paymentType)) && (differenceAmount < 0d)) {
            query = AccountQueryBuilder.getFindByNameQuery("Extra Payment");
            writeOffAccounts = super.findWithQuery(query);
            if (writeOffAccounts != null && !writeOffAccounts.isEmpty()) {
                writeOffAccount = writeOffAccounts.get(0);
            }
        }
    }

    public void updateInvoice() {

        String invoiceCurrentStatus = getInvoiceCurrentStatus();

        if (invoiceCurrentStatus != null) {
            if (!invoiceCurrentStatus.equals(InvoiceStatus.DRAFT.value())) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
                currentForm = VIEW_URL;
            } else if (invoiceLines.isEmpty()) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneInvoiceLineUpdate");
            } else {
                for (InvoiceLine invLine : invoiceLines) {
                    invLine.setPartner(invoice.getPartner());
                    invLine.setInvoice(invoice);
                }
                invoice.setResidual(invoice.getAmountTotal());

                invoice.setInvoiceLines(invoiceLines);
                invoice.setInvoiceTaxes(generateInvoiceTaxes());
                invoice = super.updateItem(invoice);

                if ((partialListType == null) && (invoices != null)) {
                    invoices.set(invoices.indexOf(invoice), invoice);
                } else {
                    query = InvoiceQueryBuilder.getFindAllInvoicesQuery();
                    invoices = super.findWithQuery(query);
                    partialListType = null;
                }

                currentForm = VIEW_URL;
            }

        } else {
            invoiceNotFound();
        }
    }

    public void createInvoice() {
        if (invoiceLines.isEmpty()) {
            JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneInvoiceLineCreate");
        } else {
            for (InvoiceLine invLine : invoiceLines) {
                query = AccountQueryBuilder.getFindByNameQuery("Product Sales");
                invLine.setAccount((Account) super.findSingleWithQuery(query));
                invLine.setPartner(invoice.getPartner());
                invLine.setInvoice(invoice);
            }
            invoice.setType("Sale");
            invoice.setState(InvoiceStatus.DRAFT.value());
            invoice.setActive(Boolean.TRUE);
            invoice.setResidual(invoice.getAmountTotal());

            invoice.setInvoiceLines(invoiceLines);
            invoice.setInvoiceTaxes(generateInvoiceTaxes());

            invoice = super.createItem(invoice);

            invoice.setName(IdGenerator.generateInvoiceId(invoice.getId()));

            invoice = super.updateItem(invoice);

            if ((partialListType == null) && (invoices != null)) {
                invoices.add(invoice);
            } else {
                query = InvoiceQueryBuilder.getFindAllInvoicesQuery();
                invoices = super.findWithQuery(query);
                partialListType = null;
            }

            currentForm = VIEW_URL;
        }
    }

    public void cancelCreateInvoice() {

        invoiceLine = null;
        invoiceLines = null;
        invoice = null;
        topNActiveCustomers = null;
        topNActiveSoldProducts = null;

        currentForm = VIEW_URL;

        resetListAndCurrentItem();
    }

    private List<InvoiceTax> generateInvoiceTaxes() {

        List<InvoiceTax> invoiceTaxes = new ArrayList();
        InvoiceTax invoiceTax;
        Double taxAmount;

        for (InvoiceLine invoiceline : invoice.getInvoiceLines()) {
            if (invoiceline.getTax() != null) {

                invoiceTax = new InvoiceTax();
                taxAmount = JsfUtil.round(invoiceline.getPriceSubtotal() * invoiceline.getTax().getAmount());

                query = AccountQueryBuilder.getFindByNameQuery("Tax Received");
                invoiceTax.setAccount((Account) super.findSingleWithQuery(query));
                invoiceTax.setDate(invoice.getDate());
                invoiceTax.setActive(true);
                invoiceTax.setTaxAmount(taxAmount);
                invoiceTax.setBaseAmount(invoiceline.getPriceSubtotal());
                invoiceTax.setTax(invoiceline.getTax());
                invoiceTax.setInvoice(invoice);

                invoiceTaxes.add(invoiceTax);
            }
        }
        return invoiceTaxes;
    }

    public void onRowEditInit(InvoiceLine orderLine) {
        invoiceLine = (InvoiceLine) SerializationUtils.clone(orderLine);
    }

    public void onRowEdit(int index) {
        if ((index >= 0) && (index < invoiceLines.size())) {
            invoiceLines.get(index).setPrice(JsfUtil.round(invoiceLines.get(index).getPrice()));
            invoiceLines.get(index).setQuantity(JsfUtil.round(invoiceLines.get(index).getQuantity(), invoiceLines.get(index).getProduct().getUom().getDecimals()));
            invoiceLines.get(index).setDiscount(JsfUtil.round(invoiceLines.get(index).getDiscount()));
            if (invoiceLines.get(index).getQuantity() == 0d) {
                invoiceLines.get(index).setQuantity(1d);
            }
            if (invoiceLines.get(index).getPrice() == 0d) {
                invoiceLines.get(index).setDiscount(0d);
                invoiceLines.get(index).setTax(null);
            }
            if (invoiceLines.get(index).getDiscount() > 0d) {
                double total = JsfUtil.round(invoiceLines.get(index).getPrice() * (invoiceLines.get(index)).getQuantity());
                double discount = JsfUtil.round(invoiceLines.get(index).getPrice() * invoiceLines.get(index).getQuantity() * (invoiceLines.get(index).getDiscount() / 100));

                invoiceLines.get(index).setPriceSubtotal(JsfUtil.round(total - discount));
            } else {
                invoiceLines.get(index).setPriceSubtotal(JsfUtil.round(invoiceLines.get(index).getPrice() * invoiceLines.get(index).getQuantity()));
            }
            sumUpInvoice();
        }
    }

    private void sumUpInvoice() {
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
        if ((index >= 0) && (index < invoiceLines.size())) {
            invoiceLines.remove(index);
            invoiceLines.add(index, invoiceLine);
            invoiceLine = new InvoiceLine();
            if ((topNActiveSoldProducts != null) && (!topNActiveSoldProducts.isEmpty())) {
                invoiceLine.setProduct(topNActiveSoldProducts.get(0));
                invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
                invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
            }
        }
    }

    public void onRowCancel() {
        invoiceLine = new InvoiceLine();
        if ((topNActiveSoldProducts != null) && (!topNActiveSoldProducts.isEmpty())) {
            invoiceLine.setProduct(topNActiveSoldProducts.get(0));
            invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
            invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
        }
    }

    public void onRowDelete(int index) {
        if ((index >= 0) && (index < invoiceLines.size())) {
            invoiceLines.remove(index);
            sumUpInvoice();
        }
    }

    public void onProductChange() {
        invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
        invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
    }

    public void onProductChange(int index) {
        if ((index >= 0) && (index < invoiceLines.size())) {
            invoiceLines.get(index).setPrice(invoiceLines.get(index).getProduct().getSalePrice());
            invoiceLines.get(index).setUom(invoiceLines.get(index).getProduct().getUom().getName());
        }
    }

    public void onRowAdd() {
        invoiceLine.setActive(Boolean.TRUE);
        invoiceLine.setPrice(JsfUtil.round(invoiceLine.getPrice()));
        invoiceLine.setQuantity(JsfUtil.round(invoiceLine.getQuantity(), invoiceLine.getProduct().getUom().getDecimals()));
        invoiceLine.setDiscount(JsfUtil.round(invoiceLine.getDiscount()));
        if (invoiceLine.getQuantity() == 0d) {
            invoiceLine.setQuantity(1.0d);
        }
        if (invoiceLine.getPrice() == 0d) {
            invoiceLine.setDiscount(0d);
            invoiceLine.setTax(null);
        }
        if (invoiceLine.getDiscount() > 0d) {
            double total = JsfUtil.round(invoiceLine.getPrice() * invoiceLine.getQuantity());
            double discount = JsfUtil.round(invoiceLine.getPrice() * invoiceLine.getQuantity() * invoiceLine.getDiscount() / 100d);

            invoiceLine.setPriceSubtotal(JsfUtil.round(total - discount));
        } else {
            invoiceLine.setPriceSubtotal(JsfUtil.round(invoiceLine.getPrice() * invoiceLine.getQuantity()));
        }

        invoiceLines.add(invoiceLine);
        sumUpInvoice();

        invoiceLine = new InvoiceLine();

        if ((topNActiveSoldProducts != null) && (!topNActiveSoldProducts.isEmpty())) {
            invoiceLine.setProduct(topNActiveSoldProducts.get(0));
            invoiceLine.setPrice(topNActiveSoldProducts.get(0).getSalePrice());
            invoiceLine.setUom(topNActiveSoldProducts.get(0).getUom().getName());
        }
    }

    public void onSelectCustomer() {

        if (customer != null) {
            if (topNActiveCustomers != null && !topNActiveCustomers.contains(customer)) {
                topNActiveCustomers.add(customer);
            }
            invoice.setPartner(customer);
        }
    }

    public void onSelectProduct() {
        if (product != null) {

            if (!topNActiveSoldProducts.contains(product)) {
                topNActiveSoldProducts.add(product);
            }

            if (rowIndex < 0) {
                invoiceLine.setProduct(product);
                invoiceLine.setPrice(product.getSalePrice());
                invoiceLine.setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("mainForm:productMenuTwo");
                RequestContext.getCurrentInstance().update("mainForm:price");
            } else {
                invoiceLines.get(rowIndex).setProduct(product);
                invoiceLines.get(rowIndex).setPrice(product.getSalePrice());
                invoiceLines.get(rowIndex).setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("mainForm:datalist:" + rowIndex + ":productMenu");
                RequestContext.getCurrentInstance().update("mainForm:datalist:" + rowIndex + ":pricee");
            }
        }
    }

    public void prepareCreateInvoice() {

        invoice = new Invoice();
        invoiceLines = new ArrayList();
        invoiceLine = new InvoiceLine();

        query = AccountQueryBuilder.getFindByNameQuery("Account Receivable");
        invoice.setAccount((Account) super.findSingleWithQuery(query));
        query = JournalQueryBuilder.getFindByCodeQuery("INV");
        invoice.setJournal((Journal) super.findSingleWithQuery(query));

        loadActiveCustomers();
        loadActiveSoldProducts();

        if ((topNActiveSoldProducts != null) && (!topNActiveSoldProducts.isEmpty())) {
            invoiceLine.setProduct(topNActiveSoldProducts.get(0));
            invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
            invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
        }
        currentForm = CREATE_URL;
    }

    public void prepareEditInvoice() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {
            if (invoice.getState().equals(InvoiceStatus.DRAFT.value())) {
                invoiceLine = new InvoiceLine();
                invoiceLines = invoice.getInvoiceLines();

                loadActiveCustomers();
                loadActiveSoldProducts();

                if ((topNActiveSoldProducts != null) && (!topNActiveSoldProducts.isEmpty())) {
                    invoiceLine.setProduct(topNActiveSoldProducts.get(0));
                    invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
                    invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
                }

                if (!topNActiveCustomers.contains(invoice.getPartner())) {
                    topNActiveCustomers.add(invoice.getPartner());
                }

                invoiceLines.stream().filter((orderLine)
                        -> (!topNActiveSoldProducts.contains(orderLine.getProduct()))).forEachOrdered((orderLine)
                        -> {
                    topNActiveSoldProducts.add(orderLine.getProduct());
                }
                );

                currentForm = EDIT_URL;

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEdit");
            }
        } else {
            invoiceNotFound();
        }
    }

    public void cancelEditInvoice() {

        invoiceLine = null;
        invoiceLines = null;
        topNActiveCustomers = null;
        topNActiveSoldProducts = null;

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {
            currentForm = VIEW_URL;
        } else {
            invoiceNotFound();
        }
    }

    public void deleteInvoice() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {
            if (invoice.getState().equals(InvoiceStatus.CANCELLED.value())) {

                cancelInvoiceRelations();

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
            invoiceNotFound();
        }
    }

    private void cancelInvoiceRelations() {
        if (invoice.getSaleOrder() != null) {
            SaleOrder saleOrder = super.findItemById(invoice.getSaleOrder().getId(), SaleOrder.class);
            saleOrder.getInvoices().size();
            saleOrder.getInvoices().remove(invoice);
            invoice.setSaleOrder(null);
            super.updateItem(saleOrder);
        }
    }

    private String getInvoiceCurrentStatus() {
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

    public void cancelInvoice() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {
            if (!invoice.getState().equals(InvoiceStatus.CANCELLED.value())) {
                if ((invoice.getState().equals(InvoiceStatus.OPEN.value())) || (invoice.getState().equals(InvoiceStatus.PAID.value()))) {
                    cancelPayments();
                    invoice.getJournalEntry().setState("Unposted");
                    super.updateItem(invoice.getJournalEntry());
                }
                invoice.setState(InvoiceStatus.CANCELLED.value());
                invoice.setResidual(0d);
                invoice = super.updateItem(invoice);
                invoices.set(invoices.indexOf(invoice), invoice);
                findCustomerOutstandingPayments();
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }

        } else {
            invoiceNotFound();
        }
    }

    private void cancelPayments() {
        if ((invoice.getInvoicePayments() != null) && (!invoice.getInvoicePayments().isEmpty())) {
            JsfUtil.addSuccessMessageDialog("Info", "invoicePaymentToOutstanding");
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

    public void duplicateInvoice() {

        invoice = super.findItemById(invoice.getId(), invoice.getClass());

        if (invoice != null) {
            invoice.getInvoiceLines().size();
            invoice.getInvoiceTaxes().size();
            Invoice newInvoice = (Invoice) SerializationUtils.clone(invoice);

            System.out.println("Name----------------: " + newInvoice.getPartner().getName());

            newInvoice.setId(null);
//            newInvoice.setPartner(invoice.getPartner());
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
            newInvoice.setState(InvoiceStatus.DRAFT.value());
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

            loadActiveCustomers();
            loadActiveSoldProducts();

            if ((topNActiveSoldProducts != null) && (!topNActiveSoldProducts.isEmpty())) {
                invoiceLine.setProduct(topNActiveSoldProducts.get(0));
                invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
                invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
            }

            if (!topNActiveCustomers.contains(invoice.getPartner())) {
                topNActiveCustomers.add(invoice.getPartner());
            }

            for (InvoiceLine orderLine : invoiceLines) {
                if (!topNActiveSoldProducts.contains(orderLine.getProduct())) {
                    topNActiveSoldProducts.add(orderLine.getProduct());
                }
            }

            currentForm = CREATE_URL;

        } else {
            invoiceNotFound();
        }
    }

    public void printInvoice() throws IOException, JRException {

        for (InvoiceLine orderLine : invoice.getInvoiceLines()) {

            orderLine.setProductName(orderLine.getProduct().getName());

            if (orderLine.getTax() != null) {
                orderLine.setTaxName(orderLine.getTax().getName());
            } else {
                orderLine.setTaxName("");
            }
        }

        invoice.getPartner().setCountry(getCountry());

        ResourceBundle bundle = JsfUtil.getBundle();
        String documentType = bundle.getString("Invoice");
        String currency = bundle.getString("Currency");

        String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/invoice.jasper");

        Map<String, Object> params = new HashMap();
        params.put("invoice", invoice);
        params.put("partner", invoice.getPartner());
        params.put("orderLines", invoice.getInvoiceLines());
        params.put("currency", currency);
        params.put("SUBREPORT_DIR", FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/") + "/");

        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, params, new JREmptyDataSource());

        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        httpServletResponse.addHeader("Content-disposition", "attachment; filename=" + documentType + "_" + invoice.getName() + ".pdf");
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
        FacesContext.getCurrentInstance().responseComplete();
    }

    private void invoiceNotFound() {

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
            query = InvoiceQueryBuilder.getFindAllInvoicesQuery();
            invoices = super.findWithQuery(query);

            if (invoices != null && !invoices.isEmpty()) {
                invoice = invoices.get(0);
            }
        }

        findCustomerOutstandingPayments();
    }

    public void nextInvoice() {
        if (invoices.indexOf(invoice) == invoices.size() - 1) {
            invoice = invoices.get(0);
        } else {
            invoice = invoices.get(invoices.indexOf(invoice) + 1);
        }
        findCustomerOutstandingPayments();
    }

    public void previousInvoice() {
        if (invoices.indexOf(invoice) == 0) {
            invoice = invoices.get(invoices.size() - 1);
        } else {
            invoice = invoices.get(invoices.indexOf(invoice) - 1);
        }
        findCustomerOutstandingPayments();
    }
    
     public int getInvoiceIndex() {
        if ((invoices != null) && (invoice != null)) {
            return invoices.indexOf(invoice) + 1;
        }
        return 0;
    }

    public void setRowIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        rowIndex = Integer.valueOf(params.get("rowIndex"));
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

    public String getCountry() {
        return countries.get(invoice.getPartner().getCountry());
    }

    public List<Account> getInvoiceAccounts() {
        query = AccountQueryBuilder.getFindByNameQuery("Account Receivable");
        return super.findWithQuery(query);
    }

    public List<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }

    public List<Invoice> getFilteredInvoices() {
        return filteredInvoices;
    }

    public void setFilteredInvoices(List<Invoice> filteredInvoices) {
        this.filteredInvoices = filteredInvoices;
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

    public List<InvoiceLine> getInvoiceLines() {
        return invoiceLines;
    }

    public void setInvoiceLines(List<InvoiceLine> invoiceLines) {
        this.invoiceLines = invoiceLines;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public List<Payment> getOutstandingPayments() {
        return outstandingPayments;
    }

    public void setOutstandingPayments(List<Payment> outstandingPayments) {
        this.outstandingPayments = outstandingPayments;
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

    public Double getDifferenceAmount() {
        return differenceAmount;
    }

    public void setDifferenceAmount(Double differenceAmount) {
        this.differenceAmount = differenceAmount;
    }

    public String getPartialListType() {
        return partialListType;
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
