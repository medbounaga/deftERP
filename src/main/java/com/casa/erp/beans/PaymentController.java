package com.casa.erp.beans;

import com.casa.erp.beans.util.IdGenerator;
import com.casa.erp.beans.util.JsfUtil;
import com.casa.erp.beans.util.Status;
import com.casa.erp.entities.JournalEntry;
import com.casa.erp.entities.JournalItem;
import com.casa.erp.entities.Partner;
import com.casa.erp.entities.Payment;
import com.casa.erp.dao.PaymentFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.inject.Named;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import org.apache.commons.lang.SerializationUtils;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Named(value = "paymentController")
@ViewScoped
public class PaymentController implements Serializable {

    @Inject
    private PaymentFacade paymentFacade;
    @Inject
    @Status
    private HashMap<String, String> statuses;
    private List<Payment> payments;
    private List<Payment> filteredPayments;
    private String paymentId;
    private String partnerId;
    private Payment payment;
    private Partner customer;
    private List<Partner> topNCustomers;
    private String partialListType;
    private String paymentType = "";
    String currentPage = "/sc/payment/List.xhtml";

    public Partner getCustomer() {
        return customer;
    }

    public void setCustomer(Partner customer) {
        this.customer = customer;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public List<Partner> getTopNCustomers() {
        return topNCustomers;
    }

    public void setTopNCustomers(List<Partner> topNCustomers) {
        this.topNCustomers = topNCustomers;
    }

    private boolean paymentExist(Integer id) {
        if (id != null) {
            payment = paymentFacade.find(id);
            if (payment == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                payments = paymentFacade.findCustomerPayment();
                partialListType = null;
                currentPage = "/sc/payment/List.xhtml";
                return false;
            } else {
                return true;
            }

        } else {
            return false;
        }
    }

    private String getPaymentStatus(Integer id) {
        if (id != null) {
            Payment payment = paymentFacade.find(id);
            if (payment != null) {
                return payment.getState();
            } else {
                JsfUtil.addWarningMessage("ItemDoesNotExist");
                payments = paymentFacade.findCustomerPayment();
                partialListType = null;
                currentPage = "/sc/payment/List.xhtml";
                return null;
            }
        }
        return null;
    }

    public void deletePayment(Integer id) {
        if (paymentExist(id)) {
            if (payment.getState().equals("Draft")) {

                try {
                    paymentFacade.remove(payment);
                } catch (Exception e) {
                    System.out.println("Error Delete: " + e.getMessage());
                    JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                    return;
                }

                if (payments.size() > 1) {
                    payments.remove(payment);
                } else {
                    payments = paymentFacade.findCustomerPayment();
                    partialListType = null;
                }

                payment = null;
                JsfUtil.addSuccessMessage("ItemDeleted");
                currentPage = "/sc/payment/List.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete2");
            }
        }
    }

    public void validatePayment(Integer id) {
        if (paymentExist(id)) {
            if (payment.getState().equals("Draft")) {
                payment.setState("Posted");
                generatePaymentJournalEntry();
                payment = paymentFacade.update(payment);
                payments.set(payments.indexOf(payment), payment);
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorValidate");
            }
        }
    }

    public void cancelEdit(Integer id) {
        if (paymentExist(id)) {
            currentPage = "/sc/payment/View.xhtml";
        }
    }

    public void updatePayment(Integer id) {
        if (getPaymentStatus(id) != null) {
            if (!getPaymentStatus(id).equals("Draft")) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
                currentPage = "/sc/payment/View.xhtml";
            } else {
                payment.setAmount(JsfUtil.round(payment.getAmount()));

                if (payment.getAmount() == 0d) {
                    JsfUtil.addWarningMessage("PositivePayment");
                    return;
                }

                if (paymentType.equals("receive money")) {
                    payment.setType("in");
                    payment.setOverpayment(payment.getAmount());
                    payment.setName(new IdGenerator().generateCustomerInPayment(payment.getId()));
                } else {
                    payment.setType("out");
                    payment.setOverpayment(0d);
                    payment.setName(new IdGenerator().generateCustomerOutPayment(payment.getId()));
                }

                if (payment.getJournal().getName().equals("Cash")) {
                    payment.setAccount(paymentFacade.findAccount("Cash"));
                } else {
                    payment.setAccount(paymentFacade.findAccount("Bank"));
                }

                payment = paymentFacade.update(payment);

                if (partialListType == null && payments != null) {
                    payments.set(payments.indexOf(payment), payment);
                } else {
                    payments = paymentFacade.findCustomerPayment();
                    partialListType = null;
                }

                currentPage = "/sc/payment/View.xhtml";
            }
        }
    }

    public void prepareEdit(Integer id) {
        if (paymentExist(id)) {
            if (payment.getState().equals("Draft")) {
                topNCustomers = paymentFacade.findTopNCustomers(4);

                if (payment.getType().equals("in")) {
                    paymentType = "receive money";
                } else {
                    paymentType = "send money";
                }

                if (!topNCustomers.contains(payment.getPartner())) {
                    topNCustomers.add(payment.getPartner());
                }
                currentPage = "/sc/payment/Edit.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEdit");
            }
        }
    }

    public void prepareCreate() {

        payment = new Payment();
        payment.setState("Draft");
        payment.setPartnerType("customer");
        payment.setAmount(0d);
        paymentType = "receive money";
        topNCustomers = paymentFacade.findTopNCustomers(4);
        currentPage = "/sc/payment/Create.xhtml";
    }

    public void createPayment() {

        String paymentInOut;
        String account;
        Double outstandingPayment;
        payment.setAmount(JsfUtil.round(payment.getAmount()));

        if (payment.getAmount() == 0d) {
            JsfUtil.addWarningMessage("PositivePayment");
            return;
        }

        if (paymentType.equals("receive money")) {
            paymentInOut = "in";
            outstandingPayment = payment.getAmount();
        } else {
            paymentInOut = "out";
            outstandingPayment = 0d;
        }

        if (payment.getJournal().getName().equals("Cash")) {
            account = "Cash";
        } else {
            account = "Bank";
        }

        payment.setAmount(JsfUtil.round(payment.getAmount()));
        payment = new Payment(payment.getAmount(), payment.getDate(), payment.getPartner(), payment.getJournal(), paymentInOut, Boolean.TRUE, paymentFacade.findAccount(account), null, null, payment.getState(), outstandingPayment, "customer");
        payment = paymentFacade.create(payment, "Customer", paymentInOut);

        if (partialListType == null && payments != null) {
            payments.add(payment);
        } else {
            payments = paymentFacade.findCustomerPayment();
            partialListType = null;
        }

        currentPage = "/sc/payment/View.xhtml";
    }

    private void generatePaymentJournalEntry() {

        String paymentType;
        double CashBankCredit;
        double CashBankDebit;
        double receivableCredit;
        double receivableDebit;

        if (payment.getType().equals("in")) {
            paymentType = "Customer Payment";
            CashBankCredit = 0d;
            CashBankDebit = payment.getAmount();
            receivableCredit = payment.getAmount();
            receivableDebit = 0d;

        } else {
            paymentType = "Customer Refund";
            CashBankCredit = payment.getAmount();
            CashBankDebit = 0d;
            receivableCredit = 0d;
            receivableDebit = payment.getAmount();
        }

        List<JournalItem> journalItems = new ArrayList<>();

        JournalEntry journalEntry = new JournalEntry(
                null,
                payment.getDate(),
                Boolean.TRUE,
                payment.getJournal(),
                payment.getPartner(),
                payment,
                null,
                "Posted",
                payment.getAmount());

        journalItems.add(new JournalItem(
                receivableDebit,
                receivableCredit,
                payment.getDate(),
                paymentType,
                null,
                0d,
                0d,
                Boolean.TRUE,
                paymentFacade.findAccount("Account Receivable"),
                journalEntry,
                journalEntry.getJournal(),
                payment.getPartner(),
                null,
                null,
                0d,
                null));

        journalItems.add(new JournalItem(
                CashBankDebit,
                CashBankCredit,
                payment.getDate(),
                payment.getName(),
                null,
                0d,
                0d,
                Boolean.TRUE,
                payment.getAccount(),
                journalEntry,
                journalEntry.getJournal(),
                payment.getPartner(),
                null,
                null,
                0d,
                null));

        journalEntry.setJournalItems(journalItems);
        journalEntry = paymentFacade.create(journalEntry, payment.getAccount().getName());
        payment.setJournalEntry(journalEntry);

    }

    public void duplicatePayment(Integer id) {
        if (paymentExist(id)) {

            payment = (Payment) SerializationUtils.clone(payment);
            payment.setOverpayment(0d);
            payment.setAccount(null);
            payment.setId(null);
            payment.setJournalEntry(null);
            payment.setDate(new Date());
            payment.setName(null);
            payment.setInvoice(null);
            payment.setReference(null);
            payment.setState("Draft");

            topNCustomers = paymentFacade.findTopNCustomers(4);

            if (payment.getType().equals("in")) {
                paymentType = "receive money";
            } else {
                paymentType = "send money";
            }

            if (!topNCustomers.contains(payment.getPartner())) {
                topNCustomers.add(payment.getPartner());
            }

            currentPage = "/sc/payment/Create.xhtml";
        }
    }

    public void onSelectCustomer() {
        if ((customer != null) && (!topNCustomers.contains(customer))) {
            topNCustomers.add(customer);
        }
        payment.setPartner(customer);
    }

    public void viewPayment() {

        if (paymentId != null && JsfUtil.isNumeric(paymentId)) {
            Integer id = Integer.valueOf(paymentId);
            payment = paymentFacade.find(id);
            if (payment != null) {
                payments = paymentFacade.findCustomerPayment();
                currentPage = "/sc/payment/View.xhtml";
                return;
            }
        }

        if (partnerId != null && JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            payments = paymentFacade.findByPartner(id, "customer");
            if (payments != null && !payments.isEmpty()) {
                currentPage = "/sc/payment/List.xhtml";
                partialListType = "partner";
                return;
            }
        }

        payments = null;
        currentPage = "/sc/payment/List.xhtml";
    }

    public void prepareView() {
        if (payment != null) {
            if (paymentExist(payment.getId())) {
                currentPage = "/sc/payment/View.xhtml";
            }
        }
    }

    public void showPaymentList() {
        payment = null;
        currentPage = "/sc/payment/List.xhtml";
    }

    public void showPaymentForm() {
        if (payments.size() > 0) {
            payment = payments.get(0);
            currentPage = "/sc/payment/View.xhtml";
        }
    }

    public int getPaymentIndex() {
        return payments.indexOf(payment) + 1;
    }

    public void nextPayment() {
        if (payments.indexOf(payment) == (payments.size() - 1)) {
            payment = payments.get(0);
        } else {
            payment = payments.get(payments.indexOf(payment) + 1);
        }
    }

    public void previousPayment() {
        if (payments.indexOf(payment) == 0) {
            payment = payments.get(payments.size() - 1);
        } else {
            payment = payments.get(payments.indexOf(payment) - 1);
        }
    }

    public String getStatus(String status) {
        return statuses.get(status);
    }

    public String getStatusColor(String status) {
        switch (status) {
            case "Draft":
                return "#009fd4";
            default:
                return "#3477db";
        }
    }

    public List<Payment> getPayments() {
        if (payments == null) {
            payments = paymentFacade.findCustomerPayment();
        }
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public List<Payment> getFilteredPayments() {
        return filteredPayments;
    }

    public void setFilteredPayments(List<Payment> filteredPayments) {
        this.filteredPayments = filteredPayments;
    }

    public Payment getPayment() {
        if (payment == null) {
            payment = new Payment();
        }
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPage() {
        return currentPage;
    }

    public void setPage(String page) {
        this.currentPage = page;
    }

    public String getPartnerId() {
        return partnerId;
    }

    public void setPartnerId(String partnerId) {
        this.partnerId = partnerId;
    }

    public String getPartialListType() {
        return partialListType;
    }

    public void setPartialListType(String partialListType) {
        this.partialListType = partialListType;
    }
}
