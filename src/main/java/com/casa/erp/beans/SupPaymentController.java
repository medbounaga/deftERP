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
    String currentPage = "/sc/supPayment/List.xhtml";

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
                payments = paymentFacade.findSupplierPayment();
                partialListType = null;
                currentPage = "/sc/supPayment/List.xhtml";
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
                payments = paymentFacade.findSupplierPayment();
                partialListType = null;
                currentPage = "/sc/supPayment/List.xhtml";
                return null;
            }
        }
        return null;
    }

    public void deleteSupPayment(Integer id) {
        if (supPaymentExist(id)) {
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
                    payments = paymentFacade.findSupplierPayment();
                    partialListType = null;
                }

                payment = null;
                JsfUtil.addSuccessMessage("ItemDeleted");
                currentPage = "/sc/supPayment/List.xhtml";
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete2");
            }
        }
    }

    public void validateSupPayment(Integer id) {
        if (supPaymentExist(id)) {
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

    public void cancelSupPaymentEdit(Integer id) {
        if (supPaymentExist(id)) {
            currentPage = "/sc/supPayment/View.xhtml";
        }
    }

    public void updateSupPayment(Integer id) {
        if (getPaymentStatus(id) != null) {
            if (!getPaymentStatus(id).equals("Draft")) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
                currentPage = "/sc/supPayment/View.xhtml";
            } else {
                payment.setAmount(JsfUtil.round(payment.getAmount()));

                if (payment.getAmount() == 0d) {
                    JsfUtil.addWarningMessage("PositivePayment");
                    return;
                }

                if (paymentType.equals("receive money")) {
                    payment.setType("in");
                    payment.setOverpayment(0d);
                    payment.setName(new IdGenerator().generateSupplierInPayment(payment.getId()));
                } else {
                    payment.setType("out");
                    payment.setOverpayment(payment.getAmount());
                    payment.setName(new IdGenerator().generateSupplierOutPayment(payment.getId()));
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
                    payments = paymentFacade.findSupplierPayment();
                    partialListType = null;
                }

                currentPage = "/sc/supPayment/View.xhtml";
            }
        }
    }

    public void prepareSupPaymentEdit(Integer id) {
        if (supPaymentExist(id)) {
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
                currentPage = "/sc/supPayment/Edit.xhtml";
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
        currentPage = "/sc/supPayment/Create.xhtml";
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

        if (partialListType == null && payments != null) {
            payments.add(payment);
        } else {
            payments = paymentFacade.findSupplierPayment();
            partialListType = null;
        }

        currentPage = "/sc/supPayment/View.xhtml";
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

    public void duplicateSupPayment(Integer id) {
        if (supPaymentExist(id)) {

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

            currentPage = "/sc/supPayment/Create.xhtml";
        }
    }

    public void onSelectSupplier() {
        if ((supplier != null) && (!topNSuppliers.contains(supplier))) {
            topNSuppliers.add(supplier);
        }
        payment.setPartner(supplier);
    }

    public void viewPayment() {

        if (paymentId != null && JsfUtil.isNumeric(paymentId)) {
            Integer id = Integer.valueOf(paymentId);
            payment = paymentFacade.find(id);
            if (payment != null) {
                payments = paymentFacade.findSupplierPayment();
                currentPage = "/sc/supPayment/View.xhtml";
                return;
            }
        }

        if (partnerId != null && JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            payments = paymentFacade.findByPartner(id, "supplier");
            if (payments != null && !payments.isEmpty()) {
                currentPage = "/sc/supPayment/List.xhtml";
                partialListType = "partner";
                return;
            }
        }

        payments = null;
        currentPage = "/sc/supPayment/List.xhtml";
    }

//    public void viewSupplierPayment() {
//
//        if (paymentId != null && JsfUtil.isNumeric(paymentId)) {
//            Integer id = Integer.valueOf(paymentId);
//            payment = paymentFacade.find(id);
//            if (payment != null) {
//                payments = paymentFacade.findSupplierPayment();
//                currentPage = "/sc/supPayment/View.xhtml";
//                return;
//            }
//        }
//
//        if (partnerId != null && JsfUtil.isNumeric(partnerId)) {
//            Integer id = Integer.valueOf(partnerId);
//            payments = paymentFacade.findByPartner(id, "supplier");
//            if (payments != null && !payments.isEmpty()) {
//                currentPage = "/sc/supPayment/List.xhtml";
//                partialListType = "partner";
//                return;
//            }
//        }
//
//        payments = null;
//        currentPage = "/sc/supPayment/List.xhtml";
//    }
    public void prepareViewSupplierPayment() {
        if (payment != null) {
            if (supPaymentExist(payment.getId())) {
                currentPage = "/sc/supPayment/View.xhtml";
            }
        }
    }

    public void showSupplierPaymentList() {
        payment = null;
        currentPage = "/sc/supPayment/List.xhtml";
    }

    public void showSupplierPaymentForm() {
        if (payments.size() > 0) {
            payment = payments.get(0);
            currentPage = "/sc/supPayment/View.xhtml";
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
