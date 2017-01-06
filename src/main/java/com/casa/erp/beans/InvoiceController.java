package com.casa.erp.beans;

import com.casa.erp.beans.util.Countries;
import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.beans.util.Status;
import com.casa.erp.dao.InvoiceFacade;
import com.casa.erp.entities.*;
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
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import static com.casa.erp.beans.util.Countries.Version.SECOND;

@Named(value = "invoiceController")
@ViewScoped
public class InvoiceController implements Serializable {

    @Inject
    private InvoiceFacade invoiceFacade;
    @Inject
    @Status
    private HashMap<String, String> statuses;
    @Inject
    @Countries(version = SECOND)
    private HashMap<String, String> countries;
    private Invoice invoice;
    private List<Invoice> invoices;
    private List<Invoice> filteredInvoices;
    private List<InvoiceLine> invoiceLines;
    private InvoiceLine invoiceLine;
    private JournalEntry journalEntry;
    private List<JournalItem> journalItems;
    private Payment payment;
    private List<Payment> outstandingPayments;
    private String currentPage = "/sc/invoice/List.xhtml";
    private String partialListType;
    private String saleId;
    private String partnerId;
    private String invoiceId;
    private Account writeOffAccount;
    private List<Account> writeOffAccounts;
    private String paymentType = "";
    private double differenceAmount;

    private int rowIndex;
    private List<Partner> topNCustomers;
    private List<Product> topSoldNProducts;
    private Partner customer;
    private Product product;

    public String getCountry() {

        return countries.get(invoice.getPartner().getCountry());
    }

    public void validateInvoice(Integer id) {
        if (invoiceExist(id)) {
            if (invoice.getState().equals("Draft")) {
                
                if(invoice.getAmountTotal() == 0d){
                    invoice.setState("Paid");
                    setSaleOrderStatus();
                }else{
                    invoice.setState("Open");
                }
                
                generateInvoiceJournalEntry();
                invoice.getPartner().setDebit(invoice.getPartner().getDebit() + invoice.getAmountTotal());
                invoice = invoiceFacade.update(invoice);
                invoices.set(invoices.indexOf(invoice), invoice);
                findOutstandingPayments();
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorValidate");
            }
        }
    }

    private void generateInvoiceJournalEntry() {

        journalItems = new ArrayList<>();
        String ref = invoice.getSaleOrder() == null ? null : invoice.getSaleOrder().getName();
        journalEntry = new JournalEntry(
                invoice.getName(),
                ref,
                invoice.getDate(),
                Boolean.TRUE,
                invoiceFacade.findJournal("INV"),
                invoice.getPartner(),
                null,
                invoice,
                "Posted",
                invoice.getAmountTotal());

        journalItems.add(new JournalItem(
                invoice.getAmountTotal(),
                0d,
                invoice.getDate(),
                invoice.getName(),
                ref,
                0d,
                0d,
                Boolean.TRUE,
                invoiceFacade.findAccount("Account Receivable"),
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
                            0d,
                            taxAmount,
                            invoice.getDate(),
                            invoiceline.getTax().getName(),
                            ref,
                            taxAmount,
                            0d,
                            Boolean.TRUE,
                            invoiceFacade.findAccount("Tax Received"),
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
                    0d,
                    invoiceline.getPriceSubtotal(),
                    invoice.getDate(),
                    invoiceline.getProduct().getName(),// name
                    ref,// ref
                    invoiceline.getPriceSubtotal(),
                    invoiceline.getQuantity(),
                    Boolean.TRUE,
                    invoiceFacade.findAccount("Product Sales"),
                    journalEntry,
                    journalEntry.getJournal(),
                    invoice.getPartner(),
                    invoiceline.getProduct(),
                    invoiceline.getProduct().getUom(),
                    JsfUtil.round(invoiceline.getQuantity() * invoiceline.getProduct().getInventory().getUnitCost()),
                    null));

        }
        journalEntry.setJournalItems(journalItems);
        journalEntry = invoiceFacade.create(journalEntry);
        invoice.setJournalEntry(journalEntry);
        journalItems = null;
        journalEntry = null;
    }

    public void payInvoice(Integer id) {
        if (invoiceExist(id)) {
            if (invoice.getState().equals("Open")) {
                double paidAmount;
                double outstandingPayment = 0d;
                String account;
                payment.setAmount(JsfUtil.round(payment.getAmount()));
                System.out.println("Amount:" + payment.getAmount());
                System.out.println("residual:" + invoice.getResidual());
                differenceAmount = JsfUtil.round(invoice.getResidual() - payment.getAmount());
                System.out.println("difference:" + differenceAmount);
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
                    invoice.setState("Paid");
                    if (paymentType.equals("keep open")) {
                        invoice.getPartner().setCredit(invoice.getPartner().getCredit() + Math.abs(differenceAmount));
                    }

                } else if (differenceAmount > 0d) {
                    paidAmount = payment.getAmount();
                    if (paymentType.equals("keep open")) {
                        invoice.setResidual(differenceAmount);
                    } else {
                        invoice.setState("Paid");
                        invoice.setResidual(0d);
                    }

                } else {
                    paidAmount = payment.getAmount();
                    invoice.setResidual(0d);
                    invoice.setState("Paid");
                }

                invoice.getPartner().setDebit(JsfUtil.round(invoice.getPartner().getDebit() - paidAmount));
                payment = new Payment(payment.getAmount(), payment.getDate(), payment.getPartner(), payment.getJournal(), "in", Boolean.TRUE, invoiceFacade.findAccount(account), null, invoice, "Posted", null, outstandingPayment, "customer");
                payment = invoiceFacade.create(payment, "Customer", "in");

                generatePaymentJournalEntry(account);
                generateInvoicePayment(invoice, payment.getJournalEntry(), netPayment, payment.getName());
                if (differenceAmount != 0d && paymentType.equals("fully paid")) {
                    generatePaymentWriteOffJournalEntry(account);
                }

                payment = invoiceFacade.update(payment);
                invoice.getPayments().add(payment);
                invoice = invoiceFacade.update(invoice);
                invoices.set(invoices.indexOf(invoice), invoice);
                findOutstandingPayments();
                if (invoice.getState().equals("Paid")) {
                    setSaleOrderStatus();
                }

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "CannotProcessInvoicePayment");
            }
        }
    }

    private void generateInvoicePayment(Invoice invoice, JournalEntry journalEntry, Double netPayment, String paymentName) {

        InvoicePayment invoicePayment = new InvoicePayment(invoice, journalEntry, netPayment, journalEntry.getDate(), paymentName);
        invoiceFacade.create(invoicePayment);
        journalEntry.getInvoicePayments().add(invoicePayment);
        invoice.getInvoicePayments().add(invoicePayment);
    }

    private void generatePaymentJournalEntry(String account) {

        List<JournalItem> journalItems = new ArrayList<>();

        JournalEntry journalEntry = new JournalEntry(
                payment.getInvoice().getOrigin(),
                payment.getDate(),
                Boolean.TRUE,
                invoiceFacade.findJournal(account),
                payment.getPartner(),
                payment,
                null,
                "Posted",
                payment.getAmount());

        journalItems.add(new JournalItem(
                0d,
                payment.getAmount(),
                payment.getDate(),
                "Customer Payment: " + payment.getInvoice().getName(),
                payment.getInvoice().getOrigin(),
                0d,
                0d,
                Boolean.TRUE,
                invoiceFacade.findAccount("Account Receivable"),
                journalEntry,
                journalEntry.getJournal(),
                payment.getPartner(),
                null,
                null,
                0d,
                null));

        journalItems.add(new JournalItem(
                payment.getAmount(),
                0d,
                payment.getDate(),
                payment.getName(),
                payment.getInvoice().getOrigin(),
                0d,
                0d,
                Boolean.TRUE,
                invoiceFacade.findAccount(account),
                journalEntry,
                journalEntry.getJournal(),
                payment.getPartner(),
                null,
                null,
                0d,
                null));

        journalEntry.setJournalItems(journalItems);
        journalEntry = invoiceFacade.create(journalEntry, account);
        payment.setJournalEntry(journalEntry);

    }

    private void generatePaymentWriteOffJournalEntry(String account) {

        double difference = Math.abs(differenceAmount);
        double writeOffCredit;
        double writeOffDebit;
        double receivableCredit;
        double receivableDebit;

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

        List<JournalItem> journalItems = new ArrayList<>();
        JournalEntry journalEntry = new JournalEntry(
                payment.getInvoice().getOrigin(),
                payment.getDate(),
                Boolean.TRUE,
                invoiceFacade.findJournal(account),
                payment.getPartner(),
                null,
                null,
                "Posted",
                difference);

        journalItems.add(new JournalItem(
                receivableDebit,
                receivableCredit,
                payment.getDate(),
                "Write-Off",
                payment.getInvoice().getOrigin(),
                0d,
                0d,
                Boolean.TRUE,
                invoiceFacade.findAccount("Account Receivable"),
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
        journalEntry = invoiceFacade.create(journalEntry, account);
        if (differenceAmount > 0d) {
            generateInvoicePayment(invoice, journalEntry, difference, "Write-Off");
        }
    }

    private void setSaleOrderStatus() {
        if (invoice.getSaleOrder() != null) {
            invoice.getSaleOrder().getSaleOrderLines().size();
            boolean saleOrderInvoiced = true;
            boolean invoicesPaid = true;

            for (SaleOrderLine line : invoice.getSaleOrder().getSaleOrderLines()) {
                if (line.getInvoiced() == false) {
                    saleOrderInvoiced = false;
                }
            }

            if (saleOrderInvoiced == true) {
                for (Invoice invoice : this.invoice.getSaleOrder().getInvoices()) {
                    if (!invoice.getState().equals("Paid")) {
                        invoicesPaid = false;
                    }
                }
            }

            if (saleOrderInvoiced == true && invoicesPaid == true) {
                invoice.getSaleOrder().setPaid(Boolean.TRUE);
                if (invoice.getSaleOrder().getShipped() == true) {
                    invoice.getSaleOrder().setState("Done");
                }
                invoiceFacade.update(invoice.getSaleOrder());
            }
        }
    }

    public void payOutstandingPayment(Integer paymentId, Integer invoiceId) {

        payment = invoiceFacade.findPayment(paymentId);
        invoice = invoiceFacade.find(invoiceId);
        Double paidAmount;
        Double newOverPayment;

        if (payment != null && invoice != null) {
            if (payment.getOverpayment() > 0d && invoice.getResidual() > 0d) {

                if (payment.getOverpayment() >= invoice.getResidual()) {
                    paidAmount = invoice.getResidual();
                    newOverPayment = JsfUtil.round(payment.getOverpayment() - invoice.getResidual());
                    invoice.setState("Paid");

                } else {
                    paidAmount = payment.getOverpayment();
                    newOverPayment = 0d;
                }

                payment.setOverpayment(newOverPayment);
                invoice.setResidual(JsfUtil.round(invoice.getResidual() - paidAmount));

                generateInvoicePayment(invoice, payment.getJournalEntry(), paidAmount, payment.getName());

                invoiceFacade.update(payment);
                invoice = invoiceFacade.update(invoice);
                invoices.set(invoices.indexOf(invoice), invoice);
                payment = null;

                if (invoice.getState().equals("Paid")) {
                    setSaleOrderStatus();
                }
                findOutstandingPayments();
            }
        }
    }

    public void removeOrderLine(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < invoiceLines.size()) {
            invoiceLines.remove(rowIndex);
        }
    }

    public void viewInvoice() {
        if (invoiceId != null && JsfUtil.isNumeric(invoiceId)) {
            Integer id = Integer.valueOf(invoiceId);
            invoice = invoiceFacade.find(id);
            if (invoice != null) {
                invoices = invoiceFacade.findOutInvoices();
                currentPage = "/sc/invoice/View.xhtml";
                findOutstandingPayments();
                return;
            }
        }

        if (saleId != null && JsfUtil.isNumeric(saleId)) {
            Integer id = Integer.valueOf(saleId);
            invoices = invoiceFacade.findBySaleId(id);
            if ((invoices != null) && (!invoices.isEmpty())) {
                currentPage = "/sc/invoice/List.xhtml";
                partialListType = "saleOrder";
                return;
            }
        }

        if (partnerId != null && JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            invoices = invoiceFacade.findByPartner(id, "Sale");
            if (invoices != null && !invoices.isEmpty()) {
                currentPage = "/sc/invoice/List.xhtml";
                partialListType = "partner";
                return;
            }
        }

        invoices = invoiceFacade.findOutInvoices();
        currentPage = "/sc/invoice/List.xhtml";
    }

    private void findOutstandingPayments() {
        if (invoice.getState().equals("Open")) {
            outstandingPayments = invoiceFacade.findCustomerOutstandingPayments(invoice.getPartner().getId());
        } else {
            outstandingPayments = null;
        }
    }

    public void prepareView() {

        if (invoice != null) {
            if (invoiceExist(invoice.getId())) {
                findOutstandingPayments();
                currentPage = "/sc/invoice/View.xhtml";
            }
        }
    }

    public void showInvoiceList() {
        invoice = null;
        invoiceLines = null;
        invoiceLine = null;
        topNCustomers = null;
        topSoldNProducts = null;
        currentPage = "/sc/invoice/List.xhtml";
    }

    public void showInvoiceForm() {
        if (invoices.size() > 0) {
            invoice = invoices.get(0);
            findOutstandingPayments();
            currentPage = "/sc/invoice/View.xhtml";
        }
    }

    public int getInvoiceIndex() {
        if (invoices != null && invoice != null) {
            return invoices.indexOf(invoice) + 1;
        }
        return 0;
    }

    public void nextInvoice() {
        if (invoices.indexOf(invoice) == (invoices.size() - 1)) {
            invoice = invoices.get(0);
        } else {
            invoice = invoices.get(invoices.indexOf(invoice) + 1);
        }
        findOutstandingPayments();
    }

    public void previousInvoice() {
        if (invoices.indexOf(invoice) == 0) {
            invoice = invoices.get(invoices.size() - 1);
        } else {
            invoice = invoices.get(invoices.indexOf(invoice) - 1);
        }
        findOutstandingPayments();
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

    public String getPage() {
        return currentPage;
    }

    public void setPage(String page) {
        this.currentPage = page;
    }

    public Invoice getInvoice() {
        if (invoice == null) {
            invoice = new Invoice();
        }
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public InvoiceLine getInvoiceLine() {
        if (invoiceLine == null) {
            invoiceLine = new InvoiceLine();
        }
        return invoiceLine;
    }

    public void setInvoiceLine(InvoiceLine invoiceLine) {
        this.invoiceLine = invoiceLine;
    }

    public List<Invoice> getInvoices() {
        if (invoices == null) {
            invoices = invoiceFacade.findOutInvoices();
        }
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

    public List<InvoiceLine> getInvoiceLines() {
        if (invoiceLines == null) {
            invoiceLines = new ArrayList<>();
        }
        return invoiceLines;
    }

    public void setInvoiceLines(List<InvoiceLine> invoiceLines) {
        this.invoiceLines = invoiceLines;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(String currentPage) {
        this.currentPage = currentPage;
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

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getInvoiceId() {
        return invoiceId;
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

    public void preparePayment(Integer id) {
        if (invoiceExist(id)) {
            if (invoice.getState().equals("Open")) {
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
        }
    }

    public void updateDifferenceAmount() {
        payment.setAmount(JsfUtil.round(payment.getAmount()));
        differenceAmount = JsfUtil.round(invoice.getResidual() - payment.getAmount());
        if (differenceAmount != 0d) {
            if ("".equals(paymentType)) {
                paymentType = "keep open";
            } else if ("fully paid".equals(paymentType) && differenceAmount > 0d) {
                writeOffAccounts = invoiceFacade.findAccountByName("Expenses");
                writeOffAccount = writeOffAccounts.get(0);
            } else if ("fully paid".equals(paymentType) && differenceAmount < 0d) {
                writeOffAccounts = invoiceFacade.findAccountByName("Extra Payment");
                writeOffAccount = writeOffAccounts.get(0);
            }
        }
    }

    public void onPaymentDifferenceStrategyChange() {

        if ("fully paid".equals(paymentType) && differenceAmount > 0d) {
            writeOffAccounts = invoiceFacade.findAccountByName("Expenses");
            writeOffAccount = writeOffAccounts.get(0);
        } else if ("fully paid".equals(paymentType) && differenceAmount < 0d) {
            writeOffAccounts = invoiceFacade.findAccountByName("Extra Payment");
            writeOffAccount = writeOffAccounts.get(0);
        }
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public void setRowIndex() {
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        rowIndex = Integer.valueOf(params.get("rowIndex"));
    }

    public void updateOrder(Integer id) {
        if (getInvoiceStatus(id) != null) {
            if (!getInvoiceStatus(id).equals("Draft")) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
                currentPage = "/sc/invoice/View.xhtml";
            } else if (invoiceLines.isEmpty()) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneInvoiceLineUpdate");
            } else {

                for (InvoiceLine invLine : invoiceLines) {
//                    invLine.setAccount(invoiceFacade.findAccount("Product Sales"));
                    invLine.setPartner(invoice.getPartner());
                    invLine.setInvoice(invoice);
                }

                invoice.setResidual(invoice.getAmountTotal());

                invoice.setInvoiceLines(invoiceLines);
                generateInvoiceTaxes();
                invoice = invoiceFacade.update(invoice);

                if (partialListType == null && invoices != null) {
                    invoices.set(invoices.indexOf(invoice), invoice);
                } else {
                    invoices = invoiceFacade.findOutInvoices();
                    partialListType = null;
                }

                currentPage = "/sc/invoice/View.xhtml";
            }
        }
    }

    public void createInvoice() {

        if (invoiceLines.isEmpty()) {
            JsfUtil.addWarningMessageDialog("InvalidAction", "AtLeastOneInvoiceLineCreate");
        } else {

            for (InvoiceLine invLine : invoiceLines) {
                invLine.setAccount(invoiceFacade.findAccount("Product Sales"));
                invLine.setPartner(invoice.getPartner());
                invLine.setInvoice(invoice);
            }

            invoice.setType("Sale");
            invoice.setState("Draft");
            invoice.setActive(Boolean.TRUE);
            invoice.setResidual(invoice.getAmountTotal());

            invoice.setInvoiceLines(invoiceLines);
            generateInvoiceTaxes();
            invoice = invoiceFacade.create(invoice, "invoice");

            if (partialListType == null && invoices != null) {
                invoices.add(invoice);
            } else {
                invoices = invoiceFacade.findOutInvoices();
                partialListType = null;
            }

            currentPage = "/sc/invoice/View.xhtml";
        }
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
                        invoiceFacade.findAccount("Tax Received"),
                        invoice,
                        invoiceline.getTax()));
            }
        }

        invoice.setInvoiceTaxes(invoiceTaxes);
    }

    public List<Partner> getTopNCustomers() {
        if (topNCustomers == null) {
            topNCustomers = invoiceFacade.findTopNCustomers(4);
        }
        return topNCustomers;

    }

    public void onRowEditInit(InvoiceLine orderLine) {
        invoiceLine = (InvoiceLine) SerializationUtils.clone(orderLine);
    }

    public void onRowEdit(int index) {
        if (index >= 0 && index < invoiceLines.size()) {
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

            if (invoiceLines.get(index).getDiscount() > 0) {

                double total = JsfUtil.round(invoiceLines.get(index).getPrice() * invoiceLines.get(index).getQuantity());
                double discount = JsfUtil.round(invoiceLines.get(index).getPrice() * invoiceLines.get(index).getQuantity() * invoiceLines.get(index).getDiscount() / 100);

                invoiceLines.get(index).setPriceSubtotal(JsfUtil.round(total - discount));

            } else {

                invoiceLines.get(index).setPriceSubtotal(JsfUtil.round(invoiceLines.get(index).getPrice() * invoiceLines.get(index).getQuantity()));
            }

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
            if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
                invoiceLine.setProduct(topSoldNProducts.get(0));
                invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
                invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
            }
        }
    }

    public void onRowCancel() {
        invoiceLine = new InvoiceLine();
        if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
            invoiceLine.setProduct(topSoldNProducts.get(0));
            invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
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
        invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
        invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
    }

    public void onProductChange(int index) {
        if (index >= 0 && index < invoiceLines.size()) {
            invoiceLines.get(index).setPrice(invoiceLines.get(index).getProduct().getSalePrice());
            invoiceLines.get(index).setUom(invoiceLines.get(index).getProduct().getUom().getName());
        }
    }

    public List<Product> getTopSoldNProducts() {
        if (topSoldNProducts == null) {
            topSoldNProducts = invoiceFacade.findTopNSoldProducts(4);
        }
        return topSoldNProducts;
    }

    public void onRowAdd(ActionEvent event) {

        invoiceLine.setActive(Boolean.TRUE);
        invoiceLine.setPrice(JsfUtil.round(invoiceLine.getPrice()));
        invoiceLine.setQuantity(JsfUtil.round(invoiceLine.getQuantity(), invoiceLine.getProduct().getUom().getDecimals()));
        invoiceLine.setDiscount(JsfUtil.round(invoiceLine.getDiscount()));

        if (invoiceLine.getQuantity() == 0d) {
            invoiceLine.setQuantity(1d);
        }

        if (invoiceLine.getPrice() == 0d) {
            invoiceLine.setDiscount(0d);
            invoiceLine.setTax(null);
        }

        if (invoiceLine.getDiscount() > 0) {

            double total = JsfUtil.round((invoiceLine.getPrice()) * (invoiceLine.getQuantity()));
            double discount = JsfUtil.round(invoiceLine.getPrice() * invoiceLine.getQuantity() * invoiceLine.getDiscount() / 100);

            invoiceLine.setPriceSubtotal(JsfUtil.round(total - discount));

        } else {

            invoiceLine.setPriceSubtotal(JsfUtil.round((invoiceLine.getPrice()) * (invoiceLine.getQuantity())));
        }

        invoiceLines.add(invoiceLine);
        SumUpOrder();
        invoiceLine = new InvoiceLine();
        if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
            invoiceLine.setProduct(topSoldNProducts.get(0));
            invoiceLine.setPrice(topSoldNProducts.get(0).getSalePrice());
            invoiceLine.setUom(topSoldNProducts.get(0).getUom().getName());
        }

    }

    public void onSelectCustomer() {
        if ((customer != null) && (!topNCustomers.contains(customer))) {
            topNCustomers.add(customer);
        }
        invoice.setPartner(customer);
    }

    public void onSelectProduct() {

        if ((product != null)) {
            if (!topSoldNProducts.contains(product)) {
                topSoldNProducts.add(product);
            }

            if (rowIndex < 0) {

                invoiceLine.setProduct(product);
                invoiceLine.setPrice(product.getSalePrice());
                invoiceLine.setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("InvoiceForm:productMenuTwo");
                RequestContext.getCurrentInstance().update("InvoiceForm:price");

            } else {

                invoiceLines.get(rowIndex).setProduct(product);
                invoiceLines.get(rowIndex).setPrice(product.getSalePrice());
                invoiceLines.get(rowIndex).setUom(product.getUom().getName());

                RequestContext.getCurrentInstance().update("InvoiceForm:datalist:" + rowIndex + ":productMenu");
                RequestContext.getCurrentInstance().update("InvoiceForm:datalist:" + rowIndex + ":pricee");
            }
        }
    }

    public void prepareCreate() {

//        if (partialListType == null) {
//            invoices = null;
//        }
//        
        invoice = new Invoice();
        invoiceLines = new ArrayList<>();
        invoiceLine = new InvoiceLine();
        invoice.setAccount(invoiceFacade.findAccount("Account Receivable"));
        invoice.setJournal(invoiceFacade.findJournal("INV"));
        topNCustomers = invoiceFacade.findTopNCustomers(4);
        topSoldNProducts = invoiceFacade.findTopNSoldProducts(4);
        if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
            invoiceLine.setProduct(topSoldNProducts.get(0));
            invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
            invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
        }
        currentPage = "/sc/invoice/Create.xhtml";
    }

    public void prepareEdit(Integer id) {

        if (invoiceExist(id)) {
            if (invoice.getState().equals("Draft")) {
                invoiceLine = new InvoiceLine();
                invoiceLines = invoice.getInvoiceLines();
                topNCustomers = invoiceFacade.findTopNCustomers(4);
                topSoldNProducts = invoiceFacade.findTopNSoldProducts(4);
                if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
                    invoiceLine.setProduct(topSoldNProducts.get(0));
                    invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
                    invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
                }

                if (!topNCustomers.contains(invoice.getPartner())) {
                    topNCustomers.add(invoice.getPartner());
                }

                for (InvoiceLine orderLine : invoiceLines) {
                    if (!topSoldNProducts.contains(orderLine.getProduct())) {
                        topSoldNProducts.add(orderLine.getProduct());
                    }
                }
                currentPage = "/sc/invoice/Edit.xhtml";

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEdit");
            }
        }
    }

    public void cancelEdit(Integer id) {
        if (invoiceExist(id)) {
            currentPage = "/sc/invoice/View.xhtml";
        }
    }

    public void deleteInvoice(Integer id) {
        if (invoiceExist(id)) {
            if (invoice.getState().equals("Cancelled")) {
                cancelRelations();

                try {
                    invoiceFacade.remove(invoice);
                } catch (Exception e) {
                    System.out.println("Error Delete: " + e.getMessage());
                    JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                    return;
                }

                if (invoices.size() > 1) {
                    invoices.remove(invoice);
                } else {
                    partialListType = null;
                    invoices = invoiceFacade.findOutInvoices();
                }
                invoice = null;
                JsfUtil.addSuccessMessage("ItemDeleted");
                currentPage = "/sc/invoice/List.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete");
            }
        }
    }

    private void cancelRelations() {

        if (invoice.getSaleOrder() != null) {
            SaleOrder saleOrder = invoiceFacade.findSaleOrder(invoice.getSaleOrder().getId());
            saleOrder.getInvoices().size();
            saleOrder.getInvoices().remove(invoice);
            invoice.setSaleOrder(null);
            invoiceFacade.update(saleOrder);
        }
    }

    private boolean invoiceExist(Integer id) {
        if (id != null) {
            invoice = invoiceFacade.find(id);
            if (invoice == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                invoices = null;
                partialListType = null;
                currentPage = "/sc/invoice/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    private String getInvoiceStatus(Integer id) {
        if (id != null) {
            Invoice invoice = invoiceFacade.find(id);
            if (invoice != null) {
                return invoice.getState();
            } else {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                invoices = null;
                partialListType = null;
                currentPage = "/sc/invoice/List.xhtml";
                return null;
            }
        }
        return null;
    }

    public void cancelInvoice(Integer id) {
        if (invoiceExist(id)) {
            if (!invoice.getState().equals("Cancelled")) {

                if (invoice.getState().equals("Open") || invoice.getState().equals("Paid")) {
                    cancelPaymentds();
                    invoice.getJournalEntry().setState("Unposted");
                    invoiceFacade.update(invoice.getJournalEntry());
                }
                invoice.setState("Cancelled");
                invoice.setResidual(0d);
                invoice = invoiceFacade.update(invoice);
                invoices.set(invoices.indexOf(invoice), invoice);
                findOutstandingPayments();

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorAlreadyModified");
            }
        }
    }

    private void cancelPaymentds() {
        if (invoice.getInvoicePayments() != null && !invoice.getInvoicePayments().isEmpty()) {
            JsfUtil.addSuccessMessageDialog("Info", "invoicePaymentToOutstanding");
            for (InvoicePayment invoicePayment : invoice.getInvoicePayments()) {
                if (invoicePayment.getJournalEntry().getPayment() != null) {
                    invoicePayment.getJournalEntry().getPayment().setOverpayment(invoicePayment.getJournalEntry().getPayment().getOverpayment() + invoicePayment.getPaidAmount());
                    invoicePayment.getJournalEntry().getPayment().setInvoice(null);
                    invoiceFacade.update(invoicePayment.getJournalEntry().getPayment());
                    invoiceFacade.remove(invoicePayment);
                }
            }
            invoice.setPayments(null);
            invoice.setInvoicePayments(null);
        }
    }

    public void duplicateInvoice(Integer id) {
        if (invoiceExist(id)) {

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
            newInvoice.setState("Draft");
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
            topNCustomers = invoiceFacade.findTopNCustomers(4);
            topSoldNProducts = invoiceFacade.findTopNSoldProducts(4);
            if (topSoldNProducts != null && !topSoldNProducts.isEmpty()) {
                invoiceLine.setProduct(topSoldNProducts.get(0));
                invoiceLine.setPrice(invoiceLine.getProduct().getSalePrice());
                invoiceLine.setUom(invoiceLine.getProduct().getUom().getName());
            }

            if (!topNCustomers.contains(invoice.getPartner())) {
                topNCustomers.add(invoice.getPartner());
            }

            for (InvoiceLine orderLine : invoiceLines) {
                if (!topSoldNProducts.contains(orderLine.getProduct())) {
                    topSoldNProducts.add(orderLine.getProduct());
                }
            }

            currentPage = "/sc/invoice/Create.xhtml";

        }
    }

    public void printInvoice(ActionEvent actionEvent) throws IOException, JRException {

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
        String name = bundle.getString("Invoice");
        String currency = bundle.getString("Currency");
        

        String reportPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/invoice.jasper");

        Map<String, Object> params = new HashMap<>();
        params.put("invoice", invoice);
        params.put("partner", invoice.getPartner());
        params.put("orderLines", invoice.getInvoiceLines());
        params.put("currency", currency);
        params.put("SUBREPORT_DIR", FacesContext.getCurrentInstance().getExternalContext().getRealPath("/reports/")+"/");

        JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, params, new JREmptyDataSource());
//      JasperPrint jasperPrint = JasperFillManager.fillReport(reportPath, new HashMap<String,Object>(), new JRBeanArrayDataSource(new SaleOrder[]{saleOrder}));  
        HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
        httpServletResponse.addHeader("Content-disposition", "attachment; filename=" + name + "_" + invoice.getName() + ".pdf");
        ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, servletOutputStream);
        FacesContext.getCurrentInstance().responseComplete();

    }

}
