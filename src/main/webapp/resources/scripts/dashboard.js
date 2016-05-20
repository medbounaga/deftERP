function fillNewCustomersArray() {
    newCustomersArray = new Array();

    for (var i = newCustomers_JSON[0].length - 1; i >= 0; i--) {
        var obj = {newCustomers: newCustomers_JSON[0][i], Month: newCustomers_JSON[1][i]};
        newCustomersArray.push(obj);
    }
}

function updateNewCustomers(xhr, status, args) {
    newCustomers_JSON = JSON.parse(args.newCustomers);
    fillNewCustomersArray();
    $("#newCustomersLineTab").addClass("active");
    $("#newCustomersBarTab").removeClass("active");
    $('a[href^="#newCustomersLineTab"]').addClass("active");
    $('a[href^="#newCustomersBarTab"]').removeClass("active");
    $('#newCustomersBar').empty();
    newCustomersLineChart.setData(newCustomersArray);
}

function fillCustomerPaymentArray() {
    customerPaymentArray = new Array();
    var total;
    for (var i = customerPayment_JSON[0].length - 1; i >= 0; i--) {
        total = customerPayment_JSON[0][i] + customerPayment_JSON[1][i];
        total = removeFraction(total);
        customerPayment_JSON[0][i] =  removeFraction(customerPayment_JSON[0][i]);
        customerPayment_JSON[1][i] =  removeFraction(customerPayment_JSON[1][i]);
        var obj = {Bank: customerPayment_JSON[0][i], Cash: customerPayment_JSON[1][i], Month: customerPayment_JSON[2][i], Total: total};
        customerPaymentArray.push(obj);
    }
}

function updateCustomerPayment(xhr, status, args) {
    customerPayment_JSON = JSON.parse(args.customerPayment);
    fillCustomerPaymentArray();
    $("#customerPaymentBarTab").addClass("active");
    $("#customerPaymentLineTab").removeClass("active");
    $('a[href^="#customerPaymentBarTab"]').addClass("active");
    $('a[href^="#customerPaymentLineTab"]').removeClass("active");
    $('#customerPaymentLine').empty();
    customerPaymentBarChart.setData(customerPaymentArray);
}

function fillVendorPaymentArray() {
    vendorPaymentArray = new Array();
    var total;
    for (var i = vendorPayment_JSON[0].length - 1; i >= 0; i--) {
        total = vendorPayment_JSON[0][i] + vendorPayment_JSON[1][i];
        total = removeFraction(total);
        vendorPayment_JSON[0][i] =  removeFraction(vendorPayment_JSON[0][i]);
        vendorPayment_JSON[1][i] =  removeFraction(vendorPayment_JSON[1][i]);
        var obj = {Bank: vendorPayment_JSON[0][i], Cash: vendorPayment_JSON[1][i], Month: vendorPayment_JSON[2][i], Total: total};
        vendorPaymentArray.push(obj);
    }
}

function updateVendorPayment(xhr, status, args) {
    vendorPayment_JSON = JSON.parse(args.vendorPayment);
    fillVendorPaymentArray();
    $("#vendorPaymentBarTab").addClass("active");
    $("#vendorPaymentLineTab").removeClass("active");
    $('a[href^="#vendorPaymentBarTab"]').addClass("active");
    $('a[href^="#vendorPaymentLineTab"]').removeClass("active");
    $('#vendorPaymentLine').empty();
    vendorPaymentBarChart.setData(vendorPaymentArray);
}

function fillPurchasesAmountArray() {
    purchasesAmountArray = new Array();
    for (var i = purchasesAmount_JSON[0].length - 1; i >= 0; i--) {
        var obj = {Purchases: removeFraction(purchasesAmount_JSON[0][i]), Month: purchasesAmount_JSON[1][i]};
        purchasesAmountArray.push(obj);
    }
}

function updatePurchasesAmount(xhr, status, args) {
    purchasesAmount_JSON = JSON.parse(args.purchasesAmount);
    fillPurchasesAmountArray();
    $("#purchasesAmountLineTab").addClass("active");
    $("#purchasesAmountBarTab").removeClass("active");
    $('a[href^="#purchasesAmountLineTab"]').addClass("active");
    $('a[href^="#purchasesAmountBarTab"]').removeClass("active");
    $('#purchasesAmountBar').empty();
    purchasesAmountLineChart.setData(purchasesAmountArray);
}

function fillSalesCogsProfitArray() {
    salesCogsProfitArray = new Array();
    for (var i = salesCogsProfit_JSON[0].length - 1; i >= 0; i--) {
        var obj = {Sales: removeFraction(salesCogsProfit_JSON[0][i], 1), COGS: removeFraction(salesCogsProfit_JSON[1][i]), Profit: removeFraction(salesCogsProfit_JSON[2][i]), Month: salesCogsProfit_JSON[3][i]};
        salesCogsProfitArray.push(obj);
    }
}

function updateSalesCogsProfit(xhr, status, args) {
    salesCogsProfit_JSON = JSON.parse(args.salesCogsProfit);
    fillSalesCogsProfitArray();
    $("#salesCogsProfitBarTab").addClass("active");
    $("#salesCogsProfitLineTab").removeClass("active");
    $('a[href^="#salesCogsProfitBarTab"]').addClass("active");
    $('a[href^="#salesCogsProfitLineTab"]').removeClass("active");
    $('#salesCogsProfitLine').empty();
    salesCogsProfitBarChart.setData(salesCogsProfitArray);

}


function updateCompareTable(xhr, status, args) {
    compareTable_JSON = JSON.parse(args.compareTable);
    compareTable();
}

function fillTopSalesByProductArray() {
    topSalesByProductArray = new Array();
    for (var i = topSalesByProduct_JSON.length - 1; i >= 0; i--) {
        var obj = {label: topSalesByProduct_JSON[i][0], value: removeFraction(topSalesByProduct_JSON[i][1])};
        topSalesByProductArray.push(obj);
    }
}

function updateTopSalesByProduct(xhr, status, args) {
    topSalesByProduct_JSON = JSON.parse(args.topSalesByProduct);
    fillTopSalesByProductArray();
    $("#topSalesByProductTableTab").addClass("active");
    $("#topSalesByProductBarTab").removeClass("active");
    $('a[href^="#topSalesByProductTableTab"]').addClass("active");
    $('a[href^="#topSalesByProductBarTab"]').removeClass("active");
    $('#topSalesByProductBar').empty();
    topSalesByProductTable();
}

function fillTopPurchasesByProductArray() {
    topPurchasesByProductArray = new Array();
    for (var i = topPurchasesByProduct_JSON.length - 1; i >= 0; i--) {
        var obj = {label: topPurchasesByProduct_JSON[i][0], value: removeFraction(topPurchasesByProduct_JSON[i][1])};
        topPurchasesByProductArray.push(obj);
    }
}

function updateTopPurchasesByProduct(xhr, status, args) {

    topPurchasesByProduct_JSON = JSON.parse(args.topPurchasesByProduct);
    fillTopPurchasesByProductArray();
    $("#topPurchasesByProductTableTab").addClass("active");
    $("#topPurchasesByProductBarTab").removeClass("active");
    $('a[href^="#topPurchasesByProductTableTab"]').addClass("active");
    $('a[href^="#topPurchasesByProductBarTab"]').removeClass("active");
    $('#topPurchasesByProductBar').empty();
    topPurchasesByProductTable();
}

function fillTopPurchasesByVendorArray() {
    topPurchasesByVendorArray = new Array();

    for (var i = topPurchasesByVendor_JSON.length - 1; i >= 0; i--) {
        var obj = {label: topPurchasesByVendor_JSON[i][0], value: removeFraction(topPurchasesByVendor_JSON[i][1])};
        topPurchasesByVendorArray.push(obj);
    }
}

function updateTopPurchasesByVendor(xhr, status, args) {

    topPurchasesByVendor_JSON = JSON.parse(args.topPurchasesByVendor);
    fillTopPurchasesByVendorArray();
    $("#topPurchasesByVendorTableTab").addClass("active");
    $("#topPurchasesByVendorBarTab").removeClass("active");
    $('a[href^="#topPurchasesByVendorTableTab"]').addClass("active");
    $('a[href^="#topPurchasesByVendorBarTab"]').removeClass("active");
    $('#topPurchasesByVendorBar').empty();
    topPurchasesByVendorTable();
}

function fillTopSalesByCustomerArray() {
    topSalesByCustomerArray = new Array();

    for (var i = topSalesByCustomer_JSON.length - 1; i >= 0; i--) {
        var obj = {label: topSalesByCustomer_JSON[i][0], value: removeFraction(topSalesByCustomer_JSON[i][1])};
        topSalesByCustomerArray.push(obj);
    }
}


function updateTopSalesByCustomer(xhr, status, args) {
    topSalesByCustomer_JSON = JSON.parse(args.topSalesByCustomer);
    fillTopSalesByCustomerArray();
    $("#topSalesByCustomerTableTab").addClass("active");
    $("#topSalesByCustomerBarTab").removeClass("active");
    $('a[href^="#topSalesByCustomerTableTab"]').addClass("active");
    $('a[href^="#topSalesByCustomerBarTab"]').removeClass("active");
    $('#topSalesByCustomerBar').empty();
    topSalesByCustomerTable();
}

function fillTopReceivablesByCustomerArray() {
    topReceivablesByCustomerArray = new Array();

    for (var i = topReceivablesByCustomer_JSON.length - 1; i >= 0; i--) {
        var obj = {label: topReceivablesByCustomer_JSON[i][0], value: removeFraction(topReceivablesByCustomer_JSON[i][1])};
        topReceivablesByCustomerArray.push(obj);
    }
}


function fillTopPayablesByVendorArray() {
    topPayablesByVendorArray = new Array();

    for (var i = topPayablesByVendor_JSON.length - 1; i >= 0; i--) {
        var obj = {label: topPayablesByVendor_JSON[i][0], value: removeFraction(topPayablesByVendor_JSON[i][1])};
        topPayablesByVendorArray.push(obj);
    }
}



function compareTable() {

    if ($("select#DashboardForm\\:compareTableMenu_input").val() === 'Day') {
        $("#compareTable tr.compareTable_header").find('th:nth-child(2)').text(today);
        $("#compareTable tr.compareTable_header").find('th:nth-child(3)').text(yesterday);
    } else if ($("select#DashboardForm\\:compareTableMenu_input").val() === 'Week') {
        $("#compareTable tr.compareTable_header").find('th:nth-child(2)').text(thisWeek);
        $("#compareTable tr.compareTable_header").find('th:nth-child(3)').text(lastWeek);
    }else if ($("select#DashboardForm\\:compareTableMenu_input").val() === 'Quarter') {
        $("#compareTable tr.compareTable_header").find('th:nth-child(2)').text(thisQuarter);
        $("#compareTable tr.compareTable_header").find('th:nth-child(3)').text(lastQuarter);
    } else {
        $("#compareTable tr.compareTable_header").find('th:nth-child(2)').text(thisMonth);
        $("#compareTable tr.compareTable_header").find('th:nth-child(3)').text(lastMonth);
    }

    fillRow(compareTable_JSON[0], 'salesOrderCount', 'count');
    fillRow(compareTable_JSON[1], 'salesAmount', 'amount');
    fillRow(compareTable_JSON[2], 'cogs', 'amount');
    fillRow(compareTable_JSON[3], 'profit', 'amount');
}



function fillRow(array, rowClass, type) {
    var percentage;
    var difference;
    var performance;
    var upDown;
    var curr;
    var icon;


    if (array[0] === array[1]) {
        percentage = 0;
    } else if (array[0] === 0) {
        percentage = (-100);
    } else {
        percentage = ((array[0] - array[1]) / array[0] * 100);
    }


    percentage = Math.abs(percentage);
    percentage = formatNumber(percentage, 1);


    if (type === 'count') {
        difference = array[0] - array[1];
        curr = '';
    } else {
        difference = removeFraction(array[0] - array[1]);
        curr = currency+' ';
    }

    if (difference < 0) {
        icon = 'fa fa-arrow-down';
        upDown = 'down';
        difference = '- '+curr+ Math.abs(difference);
    } else if (difference > 0) {
        upDown = 'up';
        icon = 'fa fa-arrow-up';
        difference = '+ '+curr+ difference;
    } else {
        upDown = 'zero';
        difference = '+ '+curr+ difference;
        icon = 'fa fa-arrow-up';
    }

    performance = '<div class="diff_percentage ' + upDown + '">\n\
                            <div class="arrow"><span class="' + icon + '"></span></div>\n\
                            <div class="percentage"><span>' + percentage + '%</span></div>\n\
                            </div> ';


    $('#compareTable tr.' + rowClass + ' td:nth-child(2)').text(curr + removeFraction(array[0]));
    $('#compareTable tr.' + rowClass + ' td:nth-child(3)').text(curr + removeFraction(array[1]));
    $('#compareTable tr.' + rowClass + ' td:nth-child(4)').text(difference);
    $('#compareTable tr.' + rowClass + ' td:nth-child(5)').empty();
    $('#compareTable tr.' + rowClass + ' td:nth-child(5)').append(performance);

}

function topSalesByProductBar() {

    Morris.Donut({
        element: 'topSalesByProductBar',
        data: topSalesByProductArray,
        formatter: function (y, data) {
            return currency + y;
        }
    });


//    Morris.Bar({
//        element: 'topSalesByProductBar',
//        data: topSalesByProductArray,
//        xkey: 'Product',
//        ykeys: ['Sales'],
//        labels: [Sales],
//        xLabelMargin: 5,
//        xLabelAngle: 60,
//        barColors: ['#E67A77', '#D9DD81', '#79D1CF']
//    });
}

function topSalesByProductTable() {
    var row;
    $('#topSalesByProductTable tbody').empty();
    for (var i = 0; i < topSalesByProduct_JSON.length; i++) {
        row = '<tr>\n\
                  <td class="top-num" >' + (i + 1) + '</td>\n\
                  <td class="highlight">' + topSalesByProduct_JSON[i][0] + '</td>\n\
                  <td>'+currency+' '+ removeFraction(topSalesByProduct_JSON[i][1]) + '</td>\n\
                  <td>' + topSalesByProduct_JSON[i][2] + '</td>\n\
                  < /tr>';
        $('#topSalesByProductTable tbody').append(row);
    }

    if (topSalesByProduct_JSON.length < 5) {
        for (var i = topSalesByProduct_JSON.length; i < 5; i++) {
            row = '<tr>\n\
                  <td class="top-num">' + (i + 1) + '</td>\n\
                  <td class="highlight">--</td>\n\
                  <td>--</td>\n\
                  <td>--</td>\n\
                  < /tr>';
            $('#topSalesByProductTable tbody').append(row);
        }
    }
}

function topPurchasesByProductBar() {

    Morris.Donut({
        element: 'topPurchasesByProductBar',
        data: topPurchasesByProductArray,
        formatter: function (y, data) {
            return currency + y;
        }
    });


//    Morris.Bar({
//        element: 'topPurchasesByProductBar',
//        data: topPurchasesByProductArray,
//        xkey: 'Product',
//        ykeys: ['Purchases'],
//        labels: [Purchases],
//        xLabelMargin: 5,
//        xLabelAngle: 60,
//        barColors: ['#E67A77', '#D9DD81', '#79D1CF']
//    });
}

function topPurchasesByProductTable() {
    var row;
    $('#topPurchasesByProductTable tbody').empty();
    for (var i = 0; i < topPurchasesByProduct_JSON.length; i++) {
        row = '<tr>\n\
                  <td class="top-num">' + (i + 1) + '</td>\n\
                  <td class="highlight">' + topPurchasesByProduct_JSON[i][0] + '</td>\n\
                  <td>'+currency+' '+removeFraction(topPurchasesByProduct_JSON[i][1]) + '</td>\n\
                  <td>' + topPurchasesByProduct_JSON[i][2] + '</td>\n\
                  </tr>';
        $('#topPurchasesByProductTable tbody').append(row);
    }

    if (topPurchasesByProduct_JSON.length < 5) {
        for (var i = topPurchasesByProduct_JSON.length; i < 5; i++) {
            row = '<tr>\n\
                  <td class="top-num">' + (i + 1) + '</td>\n\
                  <td class="highlight">--</td>\n\
                  <td>--</td>\n\
                  <td>--</td>\n\
                  </tr>';
            $('#topPurchasesByProductTable tbody').append(row);
        }
    }
}

function topSalesByCustomerBar() {

    Morris.Donut({
        element: 'topSalesByCustomerBar',
        data: topSalesByCustomerArray,
        formatter: function (y, data) {
            return currency + y;
        }
    });

//    Morris.Bar({
//        element: 'topSalesByCustomerBar',
//        data: topSalesByCustomerArray,
//        xkey: 'Customer',
//        ykeys: ['Sales'],
//        labels: [Sales],
//        xLabelMargin: 5,
//        xLabelAngle: 60,
//        padding: 50,
//        horizontal :true,
//        barColors: ['#E67A77', '#D9DD81', '#79D1CF']
//    });
}

function topSalesByCustomerTable() {
    var row;
    $('#topSalesByCustomerTable tbody').empty();
    for (var i = 0; i < topSalesByCustomer_JSON.length; i++) {
        row = '<tr>\n\
                  <td class="top-num">' + (i + 1) + '</td>\n\
                  <td class="highlight">' + topSalesByCustomer_JSON[i][0] + '</td>\n\
                  <td>'+currency+' '+removeFraction(topSalesByCustomer_JSON[i][1]) + '</td>\n\
                  <td>' + topSalesByCustomer_JSON[i][2] + '</td>\n\
                  < /tr>';
        $('#topSalesByCustomerTable tbody').append(row);
    }

    if (topSalesByCustomer_JSON.length < 5) {
        for (var i = topSalesByCustomer_JSON.length; i < 5; i++) {
            row = '<tr>\n\
                  <td class="top-num">' + (i + 1) + '</td>\n\
                  <td class="highlight">--</td>\n\
                  <td>--</td>\n\
                  <td>--</td>\n\
                  < /tr>';
            $('#topSalesByCustomerTable tbody').append(row);
        }
    }
}

function topPurchasesByVendorBar() {

    Morris.Donut({
        element: 'topPurchasesByVendorBar',
        data: topPurchasesByVendorArray,
        formatter: function (y, data) {
            return currency + y;
        }
    });
//    Morris.Bar({
//        element: 'topPurchasesByVendorBar',
//        data: topPurchasesByVendorArray,
//        xkey: 'Vendor',
//        ykeys: ['Purchases'],
//        labels: [Purchases],
//        xLabelMargin: 5,
//        xLabelAngle: 60,
//        barColors: ['#E67A77', '#D9DD81', '#79D1CF']
//    });
//}
}


function topPurchasesByVendorTable() {
    var row;
    $('#topPurchasesByVendorTable tbody').empty();
    for (var i = 0; i < topPurchasesByVendor_JSON.length; i++) {
        row = '<tr>\n\
                  <td class="top-num">' + (i + 1) + '</td>\n\
                  <td class="highlight">' + topPurchasesByVendor_JSON[i][0] + '</td>\n\
                  <td>'+currency+' '+removeFraction(topPurchasesByVendor_JSON[i][1]) + '</td>\n\
                  <td>' + topPurchasesByVendor_JSON[i][2] + '</td>\n\
                  < /tr>';
        $('#topPurchasesByVendorTable tbody').append(row);
    }

    if (topPurchasesByVendor_JSON.length < 5) {
        for (var i = topPurchasesByVendor_JSON.length; i < 5; i++) {
            row = '<tr>\n\
                  <td class="top-num">' + (i + 1) + '</td>\n\
                  <td class="highlight">--</td>\n\
                  <td>--</td>\n\
                  <td>--</td>\n\
                  < /tr>';
            $('#topPurchasesByVendorTable tbody').append(row);
        }
    }
}

function topReceivablesByCustomerBar() {

    Morris.Donut({
        element: 'topReceivablesByCustomerBar',
        data: topReceivablesByCustomerArray,
        formatter: function (y, data) {
            return currency + y;
        }
    });
//    Morris.Bar({
//        element: 'topReceivablesByCustomerBar',
//        data: topReceivablesByCustomerArray,
//        xkey: 'Customer',
//        ykeys: ['Receivables'],
//        labels: [Receivables],
//        xLabelMargin: 5,
//        xLabelAngle: 30,
//        barColors: ['#E67A77', '#D9DD81', '#79D1CF']
//    });
}

function topReceivablesByCustomerTable() {
    var row;
    $('#topReceivablesByCustomerTable tbody').empty();
    for (var i = 0; i < topReceivablesByCustomer_JSON.length; i++) {
        row = '<tr>\n\
                  <td class="top-num">' + (i + 1) + '</td>\n\
                  <td class="highlight">' + topReceivablesByCustomer_JSON[i][0] + '</td>\n\
                  <td>'+currency+' '+removeFraction(topReceivablesByCustomer_JSON[i][1]) + '</td>\n\
                  < /tr>';
        $('#topReceivablesByCustomerTable tbody').append(row);
    }

    if (topReceivablesByCustomer_JSON.length < 5) {
        for (var i = topReceivablesByCustomer_JSON.length; i < 5; i++) {
            row = '<tr>\n\
                  <td class="top-num">' + (i + 1) + '</td>\n\
                  <td class="highlight">--</td>\n\
                  <td>--</td>\n\
                  < /tr>';
            $('#topReceivablesByCustomerTable tbody').append(row);
        }
    }
}

function topPayablesByVendorBar() {

    Morris.Donut({
        element: 'topPayablesByVendorBar',
        data: topPayablesByVendorArray,
        formatter: function (y, data) {
            return currency + y;
        }
    });
//
//    Morris.Bar({
//        element: 'topPayablesByVendorBar',
//        data: topPayablesByVendorArray,
//        xkey: 'Vendor',
//        ykeys: ['Payables'],
//        labels: [Payables],
//        xLabelMargin: 5,
//        xLabelAngle: 60,
//        barColors: ['#E67A77', '#D9DD81', '#79D1CF']
//    });
}

function topPayablesByVendorTable() {
    var row;
    $('#topPayablesByVendorTable tbody').empty();
    for (var i = 0; i < topPayablesByVendor_JSON.length; i++) {
        row = '<tr>\n\
                  <td class="top-num">' + (i + 1) + '</td>\n\
                  <td class="highlight">' + topPayablesByVendor_JSON[i][0] + '</td>\n\
                  <td>'+currency+' '+removeFraction(topPayablesByVendor_JSON[i][1]) + '</td>\n\
                  < /tr>';
        $('#topPayablesByVendorTable tbody').append(row);
    }

    if (topPayablesByVendor_JSON.length < 5) {
        for (var i = topPayablesByVendor_JSON.length; i < 5; i++) {
            row = '<tr>\n\
                  <td class="top-num">' + (i + 1) + '</td>\n\
                  <td class="highlight">--</td>\n\
                  <td>--</td>\n\
                  < /tr>';
            $('#topPayablesByVendorTable tbody').append(row);
        }
    }
}

function reminders() {

    var row;
    $('#reminders tbody').empty();
  
        row = '<tr>\n\\n\
                  <td class="highlight">' + reminders_JSON[0][0] + '</td>\n\
                  <td>'+salesToConfirm+' ('+currency+' '+removeFraction(reminders_JSON[0][1]) + ')</td>\n\
                  < /tr>';
        $('#reminders tbody').append(row);


 
        row = '<tr>\n\\n\
                  <td class="highlight">' + reminders_JSON[1][0] + '</td>\n\
                  <td>'+purchasesToConfirm+' ('+currency+' '+removeFraction(reminders_JSON[1][1]) + ')</td>\n\
                  < /tr>';
        $('#reminders tbody').append(row);



        row = '<tr>\n\\n\
                  <td class="highlight">' + reminders_JSON[2][0] + '</td>\n\
                  <td>'+invoicesToConfirm+' ('+currency+' '+removeFraction(reminders_JSON[2][1]) + ')</td>\n\
                  < /tr>';
        $('#reminders tbody').append(row);


 
        row = '<tr>\n\\n\
                  <td class="highlight">' + reminders_JSON[3][0] + '</td>\n\
                  <td>'+billsToConfirm+' ('+currency+' '+removeFraction(reminders_JSON[3][1]) + ')</td>\n\
                  < /tr>';
        $('#reminders tbody').append(row);
  
}


function payableReceivable() {
    var row;
    
//    $('#receivables-amount').text(formatNumber(payableReceivable_JSON[0], 1));
//    $('#payables-amount').text(formatNumber(payableReceivable_JSON[1], 1));
    
    $('#payableReceivable tbody').empty();
    row = '<tr>\n\\n\
                  <td class="highlight">'+receivables+'</td>\n\
                  <td>'+currency+' '+ removeFraction(payableReceivable_JSON[0]) + '</td>\n\
                  < /tr>';
    $('#payableReceivable tbody').append(row);
    row = '<tr>\n\\n\
                  <td class="highlight">'+payables+'</td>\n\
                  <td>'+currency+' '+removeFraction(payableReceivable_JSON[1]) + '</td>\n\
                  < /tr>';
    $('#payableReceivable tbody').append(row);
}


function salesCogsProfitBar() {

    salesCogsProfitBarChart = Morris.Bar({
        element: 'salesCogsProfitBar',
        data: salesCogsProfitArray,
        hoverCallback: function (index, options, content) {
            var data = options.data[index];
            $("#salesCogsProfitBar .morris-hover").html('<div style="font-weight:bold;margin-bottom:5px;">' + data.Month + '</div>\n\
                                            <div style="color:#E67A77">' + sales + ': '+currency+' '+ data.Sales+'</div>\n\
                                            <div style="color:#D9DD81">' + COGS + ': '+currency+' '+ data.COGS +'</div>\n\
                                            <div style="color:#79D1CF">' + profit + ': '+currency+' '+ data.Profit +'</div>');
        },
        xkey: 'Month',
        ykeys: ['Sales', 'COGS', 'Profit'],
        labels: [sales, COGS, profit],
        xLabelMargin: 5,
        xLabelAngle: 35,
        barSizeRatio: 0.5,
        barColors: ['#E67A77', '#D9DD81', '#79D1CF']
    });
}

function salesCogsProfitLine() {

    salesCogsProfitLineChart = Morris.Line({
        element: 'salesCogsProfitLine',
        data: salesCogsProfitArray,
        hoverCallback: function (index, options, content) {
            var data = options.data[index];
            $("#salesCogsProfitLine .morris-hover").html('<div style="font-weight:bold;margin-bottom:5px;">' + data.Month + '</div>\n\
                                            <div style="color:#E67A77">' + sales + ': '+currency+' '+ data.Sales+'</div>\n\
                                            <div style="color:#D9DD81">' + COGS + ': '+currency+' '+ data.COGS +'</div>\n\
                                            <div style="color:#79D1CF">' + profit + ': '+currency+' '+ data.Profit +'</div>');
        },
        xkey: 'Month',
        ykeys: ['Sales', 'COGS', 'Profit'],
        labels: [sales, COGS, profit],
        parseTime: false,
        xLabelMargin: 5,
        xLabelAngle: 35,
        lineColors: ['#E67A77', '#D9DD81', '#79D1CF']
    });
}

function purchasesAmountLine() {

    purchasesAmountLineChart = Morris.Line({
        element: 'purchasesAmountLine',
        data: purchasesAmountArray,
        hoverCallback: function (index, options, content) {
            var data = options.data[index];
            $("#purchasesAmountLine .morris-hover").html('<div style="font-weight:bold;margin-bottom:5px;">' + data.Month + '</div>\n\
                                            <div style="color:#E67A77">' + purchases + ': '+currency+' '+ data.Purchases +'</div>');
        },
        xkey: 'Month',
        ykeys: ['Purchases'],
        labels: [purchases],
        parseTime: false,
        xLabelMargin: 5,
        xLabelAngle: 35,
        lineColors: ['#E67A77']
    });
}


function purchasesAmountBar() {

    purchasesAmountBarChart = Morris.Bar({
        element: 'purchasesAmountBar',
        data: purchasesAmountArray,
        hoverCallback: function (index, options, content) {
            var data = options.data[index];
            $("#purchasesAmountBar .morris-hover").html('<div style="font-weight:bold;margin-bottom:5px;">' + data.Month + '</div>\n\
                                            <div style="color:#E67A77">' + purchases + ': '+currency+' '+ data.Purchases +'</div>');
        },
        xkey: 'Month',
        ykeys: ['Purchases'],
        labels: [purchases],
        xLabelMargin: 5,
        xLabelAngle: 35,
        barGap: 4,
        barSizeRatio: 0.3,
        barColors: ['#E67A77', '#D9DD81', '#79D1CF']
    });
}

function newCustomersBar() {

    newCustomersBarChart = Morris.Bar({
        element: 'newCustomersBar',
        data: newCustomersArray,
        xkey: 'Month',
        ykeys: ['newCustomers'],
        labels: [newCustomers],
        xLabelMargin: 5,
        xLabelAngle: 35,
        barGap: 4,
        barSizeRatio: 0.3,
        gridIntegers: true,
        ymin: 0,
        barColors: ['#E67A77', '#D9DD81', '#79D1CF']
    });
}




function newCustomersLine() {

    newCustomersLineChart = Morris.Line({
        element: 'newCustomersLine',
        data: newCustomersArray,
        xkey: 'Month',
        ykeys: ['newCustomers'],
        labels: [newCustomers],
        parseTime: false,
        gridIntegers: true,
        xLabelMargin: 5,
        xLabelAngle: 35,
        ymin: 0,
        lineColors: ['#E67A77', '#D9DD81', '#79D1CF']
    });
}

function customerPaymentLine() {

    customerPaymentLineChart = Morris.Line({
        element: 'customerPaymentLine',
        data: customerPaymentArray,
        xkey: 'Month',
        ykeys: ['Total'],
        labels: [bank, cash],
        hoverCallback: function (index, options, content) {
            var data = options.data[index];
            $("#customerPaymentLine .morris-hover").html('<div style="font-weight:bold;margin-bottom:5px;">'+currency+' '+ data.Total +'</div>\n\
                                            <div style="color:#79D1CF">'+cash+': '+currency+' '+ data.Cash +'</div>\n\
                                            <div style="color:#E67A77">'+bank+': '+currency+' '+ data.Bank +'</div>');
        },
        parseTime: false,
        xLabelMargin: 5,
        xLabelAngle: 35,
        lineColors: ['#E67A77', '#D9DD81', '#79D1CF']
    });
}

function customerPaymentBar() {

    customerPaymentBarChart = Morris.Bar({
        element: 'customerPaymentBar',
        data: customerPaymentArray,
        stacked: true,
        xkey: 'Month',
        ykeys: ['Bank', 'Cash'],
        labels: [bank, cash],
        xLabelMargin: 5,
        xLabelAngle: 35,
        barSizeRatio: 0.3,
        barColors: ['#E67A77', '#D9DD81', '#79D1CF'],
        hoverCallback: function (index, options, content) {
            var data = options.data[index];
            $("#customerPaymentBar .morris-hover").html('<div style="font-weight:bold;margin-bottom:5px;">'+currency+' '+ data.Total +'</div>\n\
                                            <div style="color:#79D1CF">'+cash+': '+currency+' '+ data.Cash +'</div>\n\
                                            <div style="color:#E67A77">'+bank+': '+currency+' '+ data.Bank +'</div>');
        }
    });
}



function vendorPaymentLine() {

    vendorPaymentLineChart = Morris.Line({
        element: 'vendorPaymentLine',
        data: vendorPaymentArray,
        xkey: 'Month',
        ykeys: ['Total'],
        labels: [bank, cash],
        parseTime: false,
        xLabelMargin: 5,
        xLabelAngle: 35,
        lineColors: ['#E67A77', '#D9DD81', '#79D1CF'],
        hoverCallback: function (index, options, content) {
            var data = options.data[index];
            $("#vendorPaymentLine .morris-hover").html('<div style="font-weight:bold;margin-bottom:5px;">'+currency+' '+ data.Total +'</div>\n\
                                            <div style="color:#79D1CF">'+cash+': '+currency+' '+ data.Cash +'</div>\n\
                                            <div style="color:#E67A77">'+bank+': '+currency+' '+ data.Bank +'</div>');
        }
    });
}

function vendorPaymentBar() {

    vendorPaymentBarChart = Morris.Bar({
        element: 'vendorPaymentBar',
        data: vendorPaymentArray,
        stacked: true,
        xkey: 'Month',
        ykeys: ['Bank', 'Cash'],
        labels: [bank, cash],
        xLabelMargin: 5,
        xLabelAngle: 35,
        barSizeRatio: 0.3,
        barColors: ['#E67A77', '#D9DD81', '#79D1CF'],
        hoverCallback: function (index, options, content) {
            var data = options.data[index];
            $("#vendorPaymentBar .morris-hover").html('<div style="font-weight:bold;margin-bottom:5px;">'+currency+' '+ data.Total +'</div>\n\
                                            <div style="color:#79D1CF">'+cash+': '+currency+' '+ data.Cash +'</div>\n\
                                            <div style="color:#E67A77">'+bank+': '+currency+' '+ data.Bank +'</div>');
        }
    });
}











//function invoiceCountChart() {
//
//
//    var Draft = 'Draft';
//    var Open = 'Open';
//    var Paid = 'Paid';
//    var invoiceCount = new Array();
//    var total;
//
//    for (var i = invoiceCount_JSON[0].length - 1; i >= 0; i--) {
//        total = invoiceCount_JSON[0][i] + invoiceCount_JSON[1][i] + invoiceCount_JSON[2][i];
//        var obj = {Draft: invoiceCount_JSON[0][i], Open: invoiceCount_JSON[1][i], Paid: invoiceCount_JSON[2][i], Month: invoiceCount_JSON[3][i], Total: total};
//        invoiceCount.push(obj);
//    }
//
//
//    Morris.Bar({
//        element: 'invoiceCountChart',
//        data: invoiceCount,
//        stacked: true,
//        hoverCallback: function (index, options, content) {
//            var data = options.data[index];
//            $("#invoiceCountChart .morris-hover").html('<div style="font-weight:bold;margin-bottom:5px;">' + data.Total + ' Invoices</div>\n\
//                                            <div style="color:#79D1CF">Paid: ' + data.Paid + '</div>\n\
//                                            <div style="color:#D9DD81">Open: ' + data.Open + '</div>\n\
//                                            <div style="color:#E67A77">Draft: ' + data.Draft + '</div>');
//        },
//        xkey: 'Month',
//        ykeys: ['Draft', 'Open', 'Paid'],
//        labels: [Draft, Open, Paid],
//        xLabelMargin: 5,
//        barColors: ['#E67A77', '#D9DD81', '#79D1CF']
//    });
//
//}


