package com.defterp.modules.accounting.controllers;

import com.defterp.modules.accounting.entities.Account;
import com.defterp.modules.commonClasses.IdGenerator;
import com.defterp.util.JsfUtil;
import com.defterp.translation.annotations.Status;
import com.defterp.modules.accounting.entities.JournalEntry;
import com.defterp.modules.accounting.entities.JournalItem;
import com.defterp.modules.partners.entities.Partner;
import com.defterp.modules.accounting.entities.Payment;
import com.defterp.modules.accounting.queryBuilders.AccountQueryBuilder;
import com.defterp.modules.accounting.queryBuilders.PaymentQueryBuilder;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.modules.partners.queryBuilders.PartnerQueryBuilder;
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
public class SupPaymentController extends AbstractController {

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
    private QueryWrapper query;

    public SupPaymentController() {
        super("/sc/supPayment/");
    }

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

    private boolean supPaymentExist(Integer id) {
        if (id != null) {
            payment = super.findItemById(id, Payment.class);
            if (payment == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");

                if ((payments != null) && (payments.size() > 1)) {
                    payments.remove(payment);
                    payment = payments.get(0);
                } else {
                    query = PaymentQueryBuilder.getFindAllVendorPaymentsQuery();
                    payments = super.findWithQuery(query);
                    if ((payments != null) && (!payments.isEmpty())) {
                        payment = payments.get(0);
                        partialListType = null;
                    }
                }
                currentForm = VIEW_URL;
                return false;
            }
            return true;
        }
        return false;
    }

    private String getPaymentStatus(Integer id) {
        if (id != null) {
            Payment paym = super.findItemById(id, Payment.class);
            if (paym != null) {
                return paym.getState();
            }
            JsfUtil.addWarningMessage("ItemDoesNotExist");
            if ((payments != null) && (payments.size() > 1)) {
                payments.remove(payment);
                payment = payments.get(0);
            } else {
                query = PaymentQueryBuilder.getFindAllVendorPaymentsQuery();
                payments = super.findWithQuery(query);
                if ((payments != null) && (!payments.isEmpty())) {
                    payment = payments.get(0);
                    partialListType = null;
                }
            }
            currentForm = VIEW_URL;
            return null;
        }
        return null;
    }

    public void deleteSupPayment() {
        if (supPaymentExist(payment.getId())) {
            if (payment.getState().equals("Draft")) {

                boolean deleted = super.deleteItem(payment);

                if (deleted) {
                    
                    JsfUtil.addSuccessMessage("ItemDeleted");
                    currentForm = VIEW_URL;

                    if ((payments != null) && (payments.size() > 1)) {
                        payments.remove(payment);
                        payment = payments.get(0);
                    } else {
                        query = PaymentQueryBuilder.getFindAllVendorPaymentsQuery();
                        payments = super.findWithQuery(query);
                        if ((payments != null) && (!payments.isEmpty())) {
                            payment = payments.get(0);
                            partialListType = null;
                        }
                    }
                    

                }else{
                    
                    JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                
                }
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
                payment = super.updateItem(payment);
                payments.set(payments.indexOf(payment), payment);
                
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorValidate");
            }
        }
    }

    public void cancelSupPaymentEdit() {
        if (supPaymentExist(payment.getId())) {
            currentForm = VIEW_URL;
        }
    }

    public void cancelCreateSupPayment() {

        if ((payments != null) && (!payments.isEmpty())) {
            payment = payments.get(0);
            currentForm = VIEW_URL;
        }
    }

    public void updateSupPayment() {
        if (getPaymentStatus(payment.getId()) != null) {
            if (!getPaymentStatus(payment.getId()).equals("Draft")) {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorProceedEdit");
                currentForm = VIEW_URL;
            } else {
                payment.setAmount(JsfUtil.round(payment.getAmount()));

                if (payment.getAmount() == 0d) {
                    JsfUtil.addWarningMessage("PositivePayment");
                    return;
                }

                if (payment.getType().equals("in")) {
                    payment.setOverpayment(0d);
                    payment.setName(IdGenerator.generateSupplierInPayment(payment.getId()));
                } else {
                    payment.setOverpayment(payment.getAmount());
                    payment.setName(IdGenerator.generateSupplierOutPayment(payment.getId()));
                }

                if (payment.getJournal().getName().equals("Cash")) {
                    query = AccountQueryBuilder.getFindByNameQuery("Cash");
                    payment.setAccount((Account)super.findSingleWithQuery(query));
                } else {
                    query = AccountQueryBuilder.getFindByNameQuery("Bank");
                    payment.setAccount((Account)super.findSingleWithQuery(query));
                }

                payment = super.updateItem(payment);

                if ((payments != null) && (!payments.isEmpty())) {
                    payments.set(payments.indexOf(payment), payment);
                } else {
                    query = PaymentQueryBuilder.getFindAllVendorPaymentsQuery();
                    payments = super.findWithQuery(query);
                    if ((payments != null) && (!payments.isEmpty())) {
                        payment = payments.get(0);
                        partialListType = null;
                    }
                }
                currentForm = VIEW_URL;
            }
        }
    }

    public void prepareSupPaymentEdit() {
        if (supPaymentExist(payment.getId())) {
            if (payment.getState().equals("Draft")) {
                             
                query = PartnerQueryBuilder.getFindActiveVendorsQuery();
                topNSuppliers = super.findWithQuery(query, 4);

                if (!topNSuppliers.contains(payment.getPartner())) {
                    topNSuppliers.add(payment.getPartner());
                }
                
                currentForm = EDIT_URL;
            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorEdit");
            }
        }
    }

    public void prepareSupPaymentCreate() {

        payment = new Payment();
        payment.setState("Draft");
        payment.setPartnerType("supplier");
        payment.setType("out");
        payment.setAmount(0d);

        query = PartnerQueryBuilder.getFindActiveVendorsQuery();
        topNSuppliers = super.findWithQuery(query, 4);
        
        currentForm = CREATE_URL;
    }

    public void createSupPayment() {

        String accountName;
        Double outstandingPayment;
        
        payment.setAmount(JsfUtil.round(payment.getAmount()));

        if (payment.getAmount() == 0d) {
            JsfUtil.addWarningMessage("PositivePayment");
            return;
        }

        if (payment.getType().equals("in")) {
            outstandingPayment = 0d;

        } else {
            outstandingPayment = payment.getAmount();
        }

        if (payment.getJournal().getName().equals("Cash")) {
            accountName = "Cash";
        } else {
            accountName = "Bank";
        }
        
        query = AccountQueryBuilder.getFindByNameQuery(accountName);
        payment.setAccount((Account)super.findSingleWithQuery(query));
        
        payment.setActive(Boolean.TRUE);
        payment.setState("Draft");
        payment.setOverpayment(outstandingPayment);
        payment.setPartnerType("supplier");  
        payment.setJournalEntry(null);
        payment.setInvoice(null);
        
        payment = super.createItem(payment);
        
        if (payment.getType().equals("out")) {
            payment.setName(IdGenerator.generateSupplierOutPayment(payment.getId()));
        } else {
            payment.setName(IdGenerator.generateSupplierInPayment(payment.getId()));
        }
        
        payment =  super.createItem(payment);

        if ((payments != null) && (!payments.isEmpty())) {
            payments.add(payment);
        } else {
            query = PaymentQueryBuilder.getFindAllVendorPaymentsQuery();
            payments = super.findWithQuery(query);
            partialListType = null;
        }

        currentForm = VIEW_URL;
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
                (Account)super.findSingleWithQuery(AccountQueryBuilder.getFindByNameQuery("Account Payable")),
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
        
        journalEntry = super.createItem(journalEntry);
        
        if (payment.getAccount().getName().equals("Cash")) {
            journalEntry.setName(IdGenerator.generatePaymentCashEntryId(journalEntry.getId()));
        } else if (payment.getAccount().getName().equals("Bank")) {
            journalEntry.setName(IdGenerator.generatePaymentBankEntryId(journalEntry.getId()));
        }
        
        journalEntry = super.updateItem(journalEntry);
        
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

            query = PartnerQueryBuilder.getFindActiveVendorsQuery();
            topNSuppliers = super.findWithQuery(query, 4);

            if (!topNSuppliers.contains(payment.getPartner())) {
                topNSuppliers.add(payment.getPartner());
            }
            currentForm = CREATE_URL;
        }
    }

    public void onSelectSupplier() {
        if ((supplier != null) && (!topNSuppliers.contains(supplier))) {
            topNSuppliers.add(supplier);
        }
        payment.setPartner(supplier);
    }

    public void resolveRequestParams() {

        currentForm = VIEW_URL;

        if (JsfUtil.isNumeric(paymentId)) {
            Integer id = Integer.valueOf(paymentId);
            payment = super.findItemById(id, Payment.class);
            if (payment != null) {
                query = PaymentQueryBuilder.getFindAllVendorPaymentsQuery();
                payments = super.findWithQuery(query);
                return;
            }
        }
        if (JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            query = PaymentQueryBuilder.getFindByVendorQuery(id);
            payments = super.findWithQuery(query);
            if ((payments != null) && (!payments.isEmpty())) {
                payment = payments.get(0);
                partialListType = "partner";
                return;
            }
        }

        query = PaymentQueryBuilder.getFindAllVendorPaymentsQuery();
        payments = super.findWithQuery(query);
        payment = payments.get(0);
    }

    public void prepareViewSupplierPayment() {
        if ((payment != null) && (supPaymentExist(payment.getId()))) {
            currentForm = VIEW_URL;
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
