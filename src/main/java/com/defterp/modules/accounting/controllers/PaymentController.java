package com.defterp.modules.accounting.controllers;

import com.defterp.modules.accounting.entities.Account;
import com.defterp.modules.accounting.entities.Journal;
import com.defterp.modules.accounting.entities.JournalEntry;
import com.defterp.modules.accounting.entities.JournalItem;
import com.defterp.modules.accounting.entities.Payment;
import com.defterp.modules.accounting.queryBuilders.AccountQueryBuilder;
import com.defterp.modules.accounting.queryBuilders.PaymentQueryBuilder;
import com.defterp.modules.commonClasses.AbstractController;
import com.defterp.modules.partners.entities.Partner;
import com.defterp.modules.commonClasses.IdGenerator;
import com.defterp.util.JsfUtil;
import com.defterp.modules.commonClasses.QueryWrapper;
import com.defterp.modules.partners.queryBuilders.PartnerQueryBuilder;
import com.defterp.translation.annotations.Status;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.commons.lang.SerializationUtils;

@Named("paymentController")
@ViewScoped
public class PaymentController extends AbstractController {

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
    private QueryWrapper query;

    public PaymentController() {
        super("/sc/payment/");
    }

    public Partner getCustomer() {
        return customer;
    }

    public void setCustomer(Partner customer) {
        this.customer = customer;
    }

    public List<Partner> getTopNCustomers() {
        return topNCustomers;
    }

    public void setTopNCustomers(List<Partner> topNCustomers) {
        this.topNCustomers = topNCustomers;
    }

    private boolean paymentExist(Integer id) {
        if (id != null) {
            payment = super.findItemById(id, Payment.class);
            if (payment == null) {
                JsfUtil.addWarningMessage("ItemDoesNotExist");

                if ((payments != null) && (payments.size() > 1)) {
                    payments.remove(payment);
                    payment = payments.get(0);
                } else {
                    query = PaymentQueryBuilder.getFindAllCustomerPaymentsQuery();
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
                query = PaymentQueryBuilder.getFindAllCustomerPaymentsQuery();
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

    public void deletePayment() {
        if (paymentExist(payment.getId())) {
            
            if (payment.getState().equals("Draft")) {

                boolean deleted = super.deleteItem(payment);

                if (deleted) {

                    JsfUtil.addSuccessMessage("ItemDeleted");
                    currentForm = VIEW_URL;

                    if ((payments != null) && (payments.size() > 1)) {
                        payments.remove(payment);
                        payment = payments.get(0);
                    } else {
                        
                        query = PaymentQueryBuilder.getFindAllCustomerPaymentsQuery();
                        payments = super.findWithQuery(query);
                        
                        if ((payments != null) && (!payments.isEmpty())) {
                            payment = payments.get(0);
                            partialListType = null;
                        }
                    }

                } else {
                    JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete3");
                }

            } else {
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorDelete2");
            }
        }
    }

    public void cancelCreatePayment() {

        if ((payments != null) && (!payments.isEmpty())) {
            payment = payments.get(0);
            currentForm = VIEW_URL;
        }
    }

    public void validatePayment() {
        if (paymentExist(payment.getId())) {
            
            if (payment.getState().equals("Draft")) {
                
                payment.setState("Posted");
                payment.setJournalEntry(generatePaymentJournalEntry());
                payment = super.updateItem(payment);
                payments.set(payments.indexOf(payment), payment);
            } else {
                
                JsfUtil.addWarningMessageDialog("InvalidAction", "ErrorValidate");
            }
        }
    }

    public void cancelEdit() {
        if (paymentExist(payment.getId())) {
            currentForm = VIEW_URL;
        }
    }

    public void updatePayment() {
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
                    payment.setOverpayment(payment.getAmount());
                    payment.setName(IdGenerator.generateCustomerInPayment(payment.getId()));
                } else {
                    payment.setOverpayment(0d);
                    payment.setName(IdGenerator.generateCustomerOutPayment(payment.getId()));
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
                    query = PaymentQueryBuilder.getFindAllCustomerPaymentsQuery();
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

    public void prepareEdit() {
        if (paymentExist(payment.getId())) {
            if (payment.getState().equals("Draft")) {

                query = PartnerQueryBuilder.getFindActiveCustomersQuery();
                topNCustomers = super.findWithQuery(query, 4);

                if (!topNCustomers.contains(payment.getPartner())) {
                    topNCustomers.add(payment.getPartner());
                }

                currentForm = EDIT_URL;
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
        payment.setType("in");
        query = PartnerQueryBuilder.getFindActiveCustomersQuery();
        topNCustomers = super.findWithQuery(query, 4);
        currentForm = CREATE_URL;
    }

    public void createPayment() {

        Double outstandingPayment;
        String accountName;
        
        payment.setAmount(JsfUtil.round(payment.getAmount()));

        if (payment.getAmount() == 0d) {
            JsfUtil.addWarningMessage("PositivePayment");
            return;
        }

        if (payment.getType().equals("in")) {
            outstandingPayment = payment.getAmount();
        } else {
            outstandingPayment = 0.0d;
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
        payment.setPartnerType("customer");  
        payment.setJournalEntry(null);
        payment.setInvoice(null);

        payment = super.createItem(payment);
        
        if (payment.getType().equals("in")) {
            payment.setName(IdGenerator.generateCustomerInPayment(payment.getId()));
        } else {
            payment.setName(IdGenerator.generateCustomerOutPayment(payment.getId()));
        }
        
        payment =  super.createItem(payment);

        if ((payments != null) && (!payments.isEmpty())) {
            payments.add(payment);
        } else {
            query = PaymentQueryBuilder.getFindAllCustomerPaymentsQuery();
            payments = super.findWithQuery(query);
            partialListType = null;
        }
        
        currentForm = VIEW_URL;
    }

    private JournalEntry generatePaymentJournalEntry() {
        
        JournalEntry journalEntry = new JournalEntry();
        JournalItem journalItem = new JournalItem();
        List<JournalItem> journalItems = new ArrayList();
        
        String paymentType;
        double CashBankCredit;
        double CashBankDebit;
        double receivableCredit;
        double receivableDebit;
        
        if (payment.getType().equals("in")) {
            paymentType = "Customer Payment";
            CashBankCredit = 0.0d;
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
        
        journalEntry.setJournal(payment.getJournal());
        journalEntry.setRef(null);
        journalEntry.setDate(payment.getDate());
        journalEntry.setActive(Boolean.TRUE);
        journalEntry.setPartner(payment.getPartner());
        journalEntry.setPayment(payment);
        journalEntry.setInvoice(null);
        journalEntry.setState("Posted");
        journalEntry.setAmount(payment.getAmount());

        query = AccountQueryBuilder.getFindByNameQuery("Account Receivable");
        journalItem.setAccount((Account) super.findSingleWithQuery(query));
        
        journalItem.setDebit(receivableDebit);
        journalItem.setCredit(receivableCredit);
        journalItem.setDate(payment.getDate());
        journalItem.setName(paymentType);
        journalItem.setRef(null);
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

        journalItem.setAccount(payment.getAccount());
        journalItem.setDebit(CashBankDebit);
        journalItem.setCredit(CashBankCredit);
        journalItem.setDate(payment.getDate());
        journalItem.setName(payment.getName());
        journalItem.setRef(null);
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
        
        if (payment.getAccount().getName().equals("Cash")) {
            journalEntry.setName(IdGenerator.generatePaymentCashEntryId(journalEntry.getId()));
        } else if (payment.getAccount().getName().equals("Bank")) {
            journalEntry.setName(IdGenerator.generatePaymentBankEntryId(journalEntry.getId()));
        }
        
        journalEntry = super.updateItem(journalEntry);

        return journalEntry;
    }

    public void duplicatePayment() {
        if (paymentExist(payment.getId())) {

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

            query = PartnerQueryBuilder.getFindActiveCustomersQuery();
            topNCustomers = super.findWithQuery(query, 4);

            if (!topNCustomers.contains(payment.getPartner())) {
                topNCustomers.add(payment.getPartner());
            }
            currentForm = CREATE_URL;
        }

        System.out.println("Payment Journal: " + payment.getJournal().getName());
    }

    public void onSelectCustomer() {
        if ((customer != null) && (!topNCustomers.contains(customer))) {
            topNCustomers.add(customer);
        }
        payment.setPartner(customer);
    }

    public void resolveRequestParams() {

        currentForm = VIEW_URL;

        if (JsfUtil.isNumeric(paymentId)) {
            Integer id = Integer.valueOf(paymentId);
            payment = super.findItemById(id, Payment.class);
            if (payment != null) {
                query = PaymentQueryBuilder.getFindAllCustomerPaymentsQuery();
                payments = super.findWithQuery(query);
                return;
            }
        }
        if (JsfUtil.isNumeric(partnerId)) {
            Integer id = Integer.valueOf(partnerId);
            query = PaymentQueryBuilder.getFindByCustomerQuery(id);
            payments = super.findWithQuery(query);
            if ((payments != null) && (!payments.isEmpty())) {
                payment = payments.get(0);
                partialListType = "partner";
                return;
            }
        }
        query = PaymentQueryBuilder.getFindAllCustomerPaymentsQuery();
        payments = super.findWithQuery(query);
        payment = payments.get(0);
    }

    public void prepareViewPayment() {

        if (payment != null && paymentExist(payment.getId())) {
            currentForm = VIEW_URL;
        }
    }

    public int getPaymentIndex() {
        return payments.indexOf(payment) + 1;
    }

    public void nextPayment() {
        if (payments.indexOf(payment) == payments.size() - 1) {
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
