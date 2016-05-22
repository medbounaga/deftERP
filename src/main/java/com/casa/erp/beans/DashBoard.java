package com.casa.erp.beans;

import com.casa.erp.beans.util.UserLocale;
import com.casa.erp.dao.DashBoardFacade;
import com.google.gson.Gson;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.primefaces.context.RequestContext;

/**
 * 
 * @author MOHAMMED BOUNAGA
 * 
 * github.com/medbounaga
 */

@Named(value = "dashBoard")
@ViewScoped
public class DashBoard implements Serializable {

    @Inject
    DashBoardFacade dashBoardFacade;
    @Inject
    @UserLocale
    Locale locale;
    String period;
    List<Object[]> resultList;

    String compareTable;
    String salesCogsProfit;
    String purchasesAmount;
    String topSalesByProduct;
    String topPurchasesByProduct;
    String topSalesByCustomer;
    String topPurchasesByVendor;
    String topReceivablesByCustomer;
    String topPayablesByVendor;
    String newCustomers;
    String reminders;
    String payableReceivable;
    String customerPayment;
    String vendorPayment;

    int interval;
    int weekInterval = 8;
    int monthInterval = 6;
    int dayInterval = 10;
    int quarterInterval = 4;
    int topItems = 5;

    @PostConstruct
    public void init() {

        period = "Month";

        resultList = dashBoardFacade.salesOrderCount(2, period);
        resultList.addAll(dashBoardFacade.salesAmount(2, period));
        resultList.addAll(dashBoardFacade.costOfGoodsSold(2, period));
        resultList.addAll(dashBoardFacade.profit(2, period));
        compareTable = new Gson().toJson(resultList);

        resultList = dashBoardFacade.salesAmount(monthInterval, period);
        resultList.addAll(dashBoardFacade.costOfGoodsSold(monthInterval, period));
        resultList.addAll(dashBoardFacade.profit(monthInterval, period));
        resultList.add(resolveMonthName(monthInterval));
        salesCogsProfit = new Gson().toJson(resultList);

        resultList = dashBoardFacade.PurchasesAmount(monthInterval, period);
        resultList.add(resolveMonthName(monthInterval));
        purchasesAmount = new Gson().toJson(resultList);

        resultList = dashBoardFacade.topSalesByProduct(topItems, period, 0);
        topSalesByProduct = new Gson().toJson(resultList);

        resultList = dashBoardFacade.topPurchasesByProduct(topItems, period, 0);
        topPurchasesByProduct = new Gson().toJson(resultList);

        resultList = dashBoardFacade.topSalesByCustomer(topItems, period, 0);
        topSalesByCustomer = new Gson().toJson(resultList);

        resultList = dashBoardFacade.topPurchasesByVendor(topItems, period, 0);
        topPurchasesByVendor = new Gson().toJson(resultList);

        resultList = dashBoardFacade.topReceivablesByCustomer();
        topReceivablesByCustomer = new Gson().toJson(resultList);

        resultList = dashBoardFacade.topPayablesByVendor();
        topPayablesByVendor = new Gson().toJson(resultList);

        resultList = dashBoardFacade.newCustomers(monthInterval, period);
        resultList.add(resolveMonthName(monthInterval));
        newCustomers = new Gson().toJson(resultList);

        resultList = dashBoardFacade.salesOrdersToConfirm();
        resultList.addAll(dashBoardFacade.purchaseOrdersToConfirm());
        resultList.addAll(dashBoardFacade.invoicesToConfirm());
        resultList.addAll(dashBoardFacade.billsToConfirm());
        reminders = new Gson().toJson(resultList);

        resultList = dashBoardFacade.receivables();
        resultList.addAll(dashBoardFacade.payables());
        payableReceivable = new Gson().toJson(resultList);

        resultList = dashBoardFacade.customerPayment(monthInterval, period);
        resultList.add(resolveMonthName(monthInterval));
        customerPayment = new Gson().toJson(resultList);

        resultList = dashBoardFacade.vendorPayment(monthInterval, period);
        resultList.add(resolveMonthName(monthInterval));
        vendorPayment = new Gson().toJson(resultList);

    }

    public void performanceIndicators() {

        RequestContext reqCtx = RequestContext.getCurrentInstance();

        resultList = dashBoardFacade.salesOrderCount(2, period);
        resultList.addAll(dashBoardFacade.salesAmount(2, period));
        resultList.addAll(dashBoardFacade.costOfGoodsSold(2, period));
        resultList.addAll(dashBoardFacade.profit(2, period));

        reqCtx.addCallbackParam("compareTable", new Gson().toJson(resultList));
    }

    public void saleAmount() {
        resultList = dashBoardFacade.salesAmount(6, "Month");
        resultList.add(resolveMonthName(6));

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("salesAmount", new Gson().toJson(resultList));
    }

    public void topSalesByProduct() {

        resultList = dashBoardFacade.topSalesByProduct(topItems, period, 0);

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("topSalesByProduct", new Gson().toJson(resultList));
    }

    public void topPurchasesByProduct() {

        resultList = dashBoardFacade.topPurchasesByProduct(topItems, period, 0);

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("topPurchasesByProduct", new Gson().toJson(resultList));
    }

    public void topPurchasesByVendor() {

        resultList = dashBoardFacade.topPurchasesByVendor(topItems, period, 0);

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("topPurchasesByVendor", new Gson().toJson(resultList));
    }

    public void topSalesByCustomer() {

        resultList = dashBoardFacade.topSalesByCustomer(topItems, period, 0);

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("topSalesByCustomer", new Gson().toJson(resultList));
    }

    public void topReceivablesByCustomer() {

        resultList = dashBoardFacade.topReceivablesByCustomer();

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("topReceivablesByCustomer", new Gson().toJson(resultList));
    }

    public void topPayablesByVendor() {

        resultList = dashBoardFacade.topPayablesByVendor();

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("topPayablesByVendor", new Gson().toJson(resultList));
    }

    public void newCustomers() {

        resultList = dashBoardFacade.newCustomers(interval, period);
        resultList.add(getPeriodLabels());

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("newCustomers", new Gson().toJson(resultList));
    }

    public void salesOrdersToConfirm() {

        resultList = dashBoardFacade.salesOrdersToConfirm();

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("salesOrdersToConfirm", new Gson().toJson(resultList));
    }

    public void purchaseOrdersToConfirm() {

        resultList = dashBoardFacade.purchaseOrdersToConfirm();

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("purchaseOrdersToConfirm", new Gson().toJson(resultList));
    }

    public void invoicesToConfirm() {

        resultList = dashBoardFacade.invoicesToConfirm();

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("invoicesToConfirm", new Gson().toJson(resultList));
    }

    public void billsToConfirm() {

        resultList = dashBoardFacade.billsToConfirm();

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("billsToConfirm", new Gson().toJson(resultList));
    }

    public void receivables() {

        resultList = dashBoardFacade.receivables();

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("receivables", new Gson().toJson(resultList));
    }

    public void payables() {

        resultList = dashBoardFacade.payables();

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("payables", new Gson().toJson(resultList));
    }

    public void customerPayment() {

        resultList = dashBoardFacade.customerPayment(interval, period);
        resultList.add(getPeriodLabels());

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("customerPayment", new Gson().toJson(resultList));
    }

    public void vendorPayment() {

        resultList = dashBoardFacade.vendorPayment(interval, period);
        resultList.add(getPeriodLabels());

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("vendorPayment", new Gson().toJson(resultList));
    }

    public void invoiceCount() {
        List<Object[]> invoices = dashBoardFacade.invoiceCount(6, period);
        invoices.add(resolveMonthName(6));

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("invoiceCountByMonth", new Gson().toJson(invoices));//additional serialized data to be sent
    }

    public void salesCogsProfit() {

        resultList = dashBoardFacade.salesAmount(interval, period);
        resultList.addAll(dashBoardFacade.costOfGoodsSold(interval, period));
        resultList.addAll(dashBoardFacade.profit(interval, period));
        resultList.add(getPeriodLabels());

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("salesCogsProfit", new Gson().toJson(resultList));

    }

    public void purchasesAmount() {

        resultList = dashBoardFacade.PurchasesAmount(interval, period);
        resultList.add(getPeriodLabels());

        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.addCallbackParam("purchasesAmount", new Gson().toJson(resultList));
    }

    private Object[] getPeriodLabels() {
        switch (period) {
            case "Quarter":
                return resolveQuarterName(interval);
            case "Month":
                return resolveMonthName(interval);
            case "Week":
                return resolveWeekNumber(interval);
            default:
                return resolveDays(interval);
        }
    }
    
    
    private Object[] resolveQuarterName(int interval) {
        return dashBoardFacade.getQuarters(interval).get(0);         
    }        
            

    private Object[] resolveMonthName(int interval) {

        Object[] months = new Object[interval];
        DateTime date = new DateTime();

        for (int i = 0; i < interval; i++) {
            if (i > 0) {
                date = date.minusMonths(1);
            }
            months[i] = date.monthOfYear().getAsShortText(locale);
        }
        return months;
    }

    private Object[] resolveWeekNumber(int interval) {

        Object[] weeks = new Object[interval];
        DateTime date = new DateTime();
        DateTimeFormatter formater = DateTimeFormat.forPattern("dd-MM");

        for (int i = 0; i < interval; i++) {

            if (i > 0) {
                date = date.minusWeeks(1);
            }
            weeks[i] = formater.print(date.withDayOfWeek(DateTimeConstants.SUNDAY));
        }
        return weeks;
    }

    private Object[] resolveDays(int interval) {

        Object[] days = new Object[interval];
        DateTime date = new DateTime();
        DateTimeFormatter formater = DateTimeFormat.forPattern("dd-MM");
        String dayName;

        for (int i = 0; i < interval; i++) {
            if (i > 0) {
                date = date.minusDays(1);
            }
            System.out.println("DayName: " + date.dayOfWeek().getAsShortText(locale));
            dayName = date.dayOfWeek().getAsShortText(locale);
            days[i] = dayName + " - " + formater.print(date);
        }
        return days;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;

        switch (period) {
            case "Month":
                interval = monthInterval;
                break;
            case "Quarter":
                interval = quarterInterval;
                break;    
            case "Week":
                interval = weekInterval;
                break;
            default:
                interval = dayInterval;
                break;
        }
    }

    public String getCompareTable() {
        return compareTable;
    }

    public String getSalesCogsProfit() {
        return salesCogsProfit;
    }

    public String getPurchasesAmount() {
        return purchasesAmount;
    }

    public String getTopSalesByProduct() {
        return topSalesByProduct;
    }

    public String getTopPurchasesByProduct() {
        return topPurchasesByProduct;
    }

    public String getTopSalesByCustomer() {
        return topSalesByCustomer;
    }

    public String getTopPurchasesByVendor() {
        return topPurchasesByVendor;
    }

    public String getTopReceivablesByCustomer() {
        return topReceivablesByCustomer;
    }

    public String getTopPayablesByVendor() {
        return topPayablesByVendor;
    }

    public String getNewCustomers() {
        return newCustomers;
    }

    public String getPayableReceivable() {
        return payableReceivable;
    }

    public String getCustomerPayment() {
        return customerPayment;
    }

    public String getVendorPayment() {
        return vendorPayment;
    }

    public String getReminders() {
        return reminders;
    }

}
