package com.defterp.modules.accounting.controllers;

import com.defterp.util.IdGenerator;
import com.defterp.util.JsfUtil;
import com.defterp.util.Status;
import com.defterp.modules.accounting.entities.JournalEntry;
import com.defterp.modules.accounting.entities.JournalItem;
import com.defterp.modules.partners.entities.Partner;
import com.defterp.modules.accounting.entities.Payment;
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
@Named(value = "supPaymentController")
@ViewScoped
public class SupPaymentController implements Serializable {

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
    private Partner supplier;
    private List<Partner> topNSuppliers;
    private String partialListType;
    private String paymentType = "";
    String currentForm = "/sc/supPayment/View.xhtml";

    public Partner getSupplier() {
        return supplier;
    }

    public void setSupplier(Partner supplier) {
        this.supplier = supplier;
    }

    public List<Partner> getTopNSuppliers() {
        return topNSuppliers;
    }

    public void setTopNSuppliers(List<Partner> topNSuppliers) {
        this.topNSuppliers = topNSuppliers;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    private boolean supPaymentExist(Integer id) {
        if (id != null) {
            payment = paymentFacade.find(id);
            if (payment == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");

                if ((payments != null) && (payments.size() > 1)) {
                    payments.remove(payment);
                    payment = payments.get(0);
                } else {
                    payments = paymentFacade.findSupplierPayment();
                    if ((payments != null) && (!payments.isEmpty())) {
                        payment = payments.get(0);
                        partialListType = null;
                    }
                }
                currentForm = "/sc/supPayment/View.xhtml";
                return false;
            }
            return true;
        }
        return false;
    }

    private String getPaymentStatus(Integer id) {
        if (id != null) {
            Payment paym = paymentFacade.find(id);
            if (paym != null) {
                return paym.getState();
            }
            JsfUtil.addWarningMessage("ItemDoesNotExist");
            if ((payments != null) && (payments.size() > 1)) {
                payments.remove(payment);
                payment = payments.get(0);
            } else {
                payments = paymentFacade.findSupplierPayment();
                if ((payments != null) && (!payments.isEmpty())) {
                    payment = payments.get(0);
                    partialListType = null;
                }
            }
            currentForm = "/sc/supPayment/View.xhtml";
            return null;
        }
        return null;
    }

    public void deleteSupPayment() {
        if (supPaymentExist(payment.getId())) {
            if (payment.getState().equals("Draft")) {
                try {
                    paymentFacade.remove(payment);
                } catch (Exception e) {
                    JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                    return;
                }
                if ((payments != null) && (payments.size() > 1)) {
                    payments.remove(payment);
                    payment = payments.get(0);
                } else {
                    payments = paymentFacade.findSupplierPayment();
                    if ((payments != null) && (!payments.isEmpty())) {
                        payment = payments.get(0);
                        partialListType = null;
                    }
                }
                JsfUtil.addSuccessMessage("ItemDeleted");
                currentForm = "/sc/supPayment/View.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete2");
            }
        }
    }

    public void validateSupPayment() {
        if (supPaymentExist(payment.getId())) {
            if (payment.getState().equals("Draft")) {
                payment.setState("Posted");
                generateSupPaymentJournalEntry();
                payment = paymentFacade.update(payment);
                payments.set(payments.indexOf(payment), payment);
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorValidate");
            }
        }
    }

    public void cancelSupPaymentEdit() {
        if (supPaymentExist(payment.getId())) {
            currentForm = "/sc/supPayment/View.xhtml";
        }
    }

    public void cancelCreateSupPayment() {

        if ((payments != null) && (!payments.isEmpty())) {
            payment = payments.get(0);
            currentForm = "/sc/supPayment/View.xhtml";
        }
    }

    public void updateSupPayment() {
        if (getPaymentStatus(payment.getId()) != null) {
            if (!getPaymentStatus(payment.getId()).equals("Draft")) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
                currentForm = "/sc/supPayment/View.xhtml";
            } else {
                payment.setAmount(JsfUtil.round(payment.getAmount()));

                if (payment.getAmount() == 0d) {
                    JsfUtil.addWarningMessage("PositivePayment");
                    return;
                }

                if (paymentType.equals("receive money")) {
                    payment.setType("in");
                    payment.setOverpayment(0d);
                    payment.setName(IdGenerator.generateSupplierInPayment(payment.getId()));
                } else {
                    payment.setType("out");
                    payment.setOverpayment(payment.getAmount());
                    payment.setName(IdGenerator.generateSupplierOutPayment(payment.getId()));
                }

                if (payment.getJournal().getName().equals("Cash")) {
                    payment.setAccount(paymentFacade.findAccount("Cash"));
                } else {
                    payment.setAccount(paymentFacade.findAccount("Bank"));
                }

                payment = paymentFacade.update(payment);

                if ((payments != null) && (!payments.isEmpty())) {
                    payments.set(payments.indexOf(payment), payment);
                } else {
                    payments = paymentFacade.findSupplierPayment();
                    if ((payments != null) && (!payments.isEmpty())) {
                        payment = payments.get(0);
                        partialListType = null;
                    }
                }
                currentForm = "/sc/supPayment/View.xhtml";
            }
        }
    }

    public void prepareSupPaymentEdit() {
        if (supPaymentExist(payment.getId())) {
            if (payment.getState().equals("Draft")) {
                topNSuppliers = paymentFacade.findTopNSuppliers(4);

                if (payment.getType().equals("in")) {
                    paymentType = "receive money";
                } else {
                    paymentType = "send money";
                }

                if (!topNSuppliers.contains(payment.getPartner())) {
                    topNSuppliers.add(payment.getPartner());
                }
                currentForm = "/sc/supPayment/Edit.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEdit");
            }
        }
    }

    public void prepareSupPaymentCreate() {

        payment = new Payment();
        payment.setState("Draft");
        payment.setPartnerType("supplier");
        payment.setAmount(0d);
        paymentType = "send money";
        topNSuppliers = paymentFacade.findTopNSuppliers(4);
        currentForm = "/sc/supPayment/Create.xhtml";
    }

    public void createSupPayment() {

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
            outstandingPayment = 0d;

        } else {
            paymentInOut = "out";
            outstandingPayment = payment.getAmount();
        }

        if (payment.getJournal().getName().equals("Cash")) {
            account = "Cash";
        } else {
            account = "Bank";
        }

        payment.setAmount(JsfUtil.round(payment.getAmount()));
        payment = new Payment(payment.getAmount(), payment.getDate(), payment.getPartner(), payment.getJournal(), paymentInOut, Boolean.TRUE, paymentFacade.findAccount(account), null, null, payment.getState(), outstandingPayment, "supplier");
        payment = paymentFacade.create(payment, "Supplier", paymentInOut);

        if ((payments != null) && (!payments.isEmpty())) {
            payments.add(payment);
        } else {
            payments = paymentFacade.findSupplierPayment();
            partialListType = null;
        }

        currentForm = "/sc/supPayment/View.xhtml";
    }

    private void generateSupPaymentJournalEntry() {

        String paymentType;
        double CashBankCredit;
        double CashBankDebit;
        double payablebleCredit;
        double payableDebit;

        if (payment.getType().equals("out")) {
            paymentType = "Vendor Payment";
            CashBankCredit = payment.getAmount();
            CashBankDebit = 0d;
            payablebleCredit = 0d;
            payableDebit = payment.getAmount();

        } else {
            paymentType = "Vendor Refund";
            CashBankCredit = 0d;
            CashBankDebit = payment.getAmount();
            payablebleCredit = payment.getAmount();
            payableDebit = 0d;
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
                payableDebit,
                payablebleCredit,
                payment.getDate(),
                paymentType,
                null,
                0d,
                0d,
                Boolean.TRUE,
                paymentFacade.findAccount("Account Payable"),
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

    public void duplicateSupPayment() {
        if (supPaymentExist(payment.getId())) {

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

            topNSuppliers = paymentFacade.findTopNSuppliers(4);

            if (payment.getType().equals("in")) {
                paymentType = "receive money";
            } else {
                paymentType = "send money";
            }

            if (!topNSuppliers.contains(payment.getPartner())) {
                topNSuppliers.add(payment.getPartner());
            }
            currentForm = "/sc/supPayment/Create.xhtml";
        }
    }

    public void onSelectSupplier() {
        if ((supplier != null) && (!topNSuppliers.contains(supplier))) {
            topNSuppliers.add(supplier);
        }
        payment.setPartner(supplier);
    }

    public void resolveRequestParams() {

        currentForm = "/sc/supPayment/View.xhtml";

        if (JsfUtil.isNumeric(paymentId)) {
            Integer id = Integer.valueOf(paymentId);
            payment = paymentFacade.find(id);
            if (payment != null) {
                payments = paymentFacade.findSupplierPayment();
                return;
            }
        }
        if (JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            payments = paymentFacade.findByPartner(id, "supplier");
            if ((payments != null) && (!payments.isEmpty())) {
                payment = payments.get(0);
                partialListType = "partner";
                return;
            }
        }
        payments = paymentFacade.findSupplierPayment();
        payment = payments.get(0);
    }

    public void prepareViewSupplierPayment() {
        if ((payment != null) && (supPaymentExist(payment.getId()))) {
            currentForm = "/sc/supPayment/View.xhtml";
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
            payments = paymentFacade.findSupplierPayment();
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

    public String getCurrentForm() {
        return currentForm;
    }

    public void setCurrentForm(String currentForm) {
        this.currentForm = currentForm;
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
