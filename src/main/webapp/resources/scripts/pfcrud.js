function handleSubmit(xhr, status, args, dialog) {
    var jqDialog = jQuery('#' + dialog.id);
    if (args.validationFailed) {
        jqDialog.effect('shake', {times: 3}, 100);
    } else {
        dialog.hide();
    }
}

function hiderow(addrow, add, args) {
    if (arguments.length === 3) {
        if (!(args.validationFailed)) {
            document.getElementById(addrow).style.display = 'none';
            document.getElementById(add).style.display = 'inline';
            showTableButtons();
        }

        else {
            document.getElementById(addrow).style.display = 'inline';
            document.getElementById(add).style.display = 'none';
            hideTableButtons();
        }

    } else if (arguments.length === 2) {
        document.getElementById(addrow).style.display = 'none';
        document.getElementById(add).style.display = 'inline';
    }
}



//function isCustomerProvided() {
//
//    if ($("select[name$='partnerId_input'] option:selected").val() === '') {
//
////      taxIdd.selectValue("value");
//
//
//
//        PF('taxId').setLabel("&nbsp;");
//        PF('taxId').selectValue("&nbsp;");
//
//        PF('growl').renderMessage({"summary": message,
//            "detail": "detail goes here",
//            "severity": "warn"});
//
//        $("#partnerId").css("border", "2px solid #cd0a0a");
//
//    }
//}

function hideTableButtons() {

    $('.ui-row-editor span.ui-icon-pencil').each(function () {
        $(this).css('visibility', 'hidden');
    });

    $('.ui-icon-trash').each(function () {
        $(this).css('display', 'none');
    });
}

function showTableButtons() {

    $('.ui-row-editor span.ui-icon-pencil').each(function () {
        $(this).css('visibility', 'visible');
    });

    $('.ui-icon-trash').each(function () {
        $(this).css('display', 'inline-block');
    });
}

function hideAdd() {
    $("#add").css("display", "none");

}

function showAdd() {
    $("#add").css("display", "inline");
}



function showRow() {

    $("#add").css("display", "none");
    $("#addrow").css("display", "block");



// alert(PF('partnerId').getSelectedValue() === '');

//function validationFailed(){
//    
//    $("#add").css("display", "inline");
//        $("#addrow").css("display", "none");
//            PF('growl').renderMessage({"summary": message,
//            "detail": "detail goes here",
//            "severity": "warn"});
//
//        $("#partnerMenu").css("border", "2px solid #cd0a0a");
//}
//
//    if (PF('partnerMenu').getSelectedValue() === '') {
//
//        $("#add").css("display", "none");
//        $("#addrow").css("display", "block");
//        
//    window.setTimeout(validationFailed, 700);    
//  
//    }
//
//    else {
//
//        $("#add").css("display", "none");
//        $("#addrow").css("display", "block");
//
//    }
}

function getWidgetVarById(id) {
    for (var propertyName in PrimeFaces.widgets) {
        if (PrimeFaces.widgets[propertyName].id === id) {
            return PrimeFaces.widgets[propertyName];
        }
    }
}

function removeUrlQuery() {
    var url = window.location.href;
    if (url.indexOf("type=") === -1) {
        if (url.indexOf("?") !== -1) {
            var newUrl = url.split("?")[0];
            window.history.pushState("", "", newUrl);
        }
    }
}

function setListURL(type) {

    var url = window.location.href;
    if (url.indexOf("type") === -1) {
        if (url.indexOf("?") !== -1) {
            var url = url.split("?")[0];
        }
        var hasQuery = url.indexOf("?") + 1;
        var hasHash = url.indexOf("#") + 1;
        var appendix = (hasQuery ? "&" : "?") + "type=" + type;
        url = hasHash ? url.replace("#", appendix + "#") : url + appendix;
        //    alert(url);
        window.history.pushState("", "", url);
        //var History = window.History;
        //History.pushState("","",url);
    }

}

function setListURL() {
    var url = window.location.href;
    if (url.indexOf("?") !== -1) {
        var url = url.split("?")[0];
        window.history.pushState("", "", url);
    }
}


//function setFormURL(id) {
//
//    var url = window.location.href;
//    if (url.indexOf("id") === -1) {
//        if (url.indexOf("?") !== -1) {
//            var url = url.split("?")[0];
//        }
//        var hasQuery = url.indexOf("?") + 1;
//        var hasHash = url.indexOf("#") + 1;
//        var appendix = (hasQuery ? "&" : "?") + "id=" + id;
//        url = hasHash ? url.replace("#", appendix + "#") : url + appendix;
////    alert(url);
//        window.history.pushState("", "", url);
//        //var History = window.History;
//        //History.pushState("","",url);
//    }
//}

function setFormURL(paramName, id) {

    var url = window.location.href;
    if (url.indexOf(paramName) === -1) {
        if (url.indexOf("?") !== -1) {
            var url = url.split("?")[0];
        }
        var hasQuery = url.indexOf("?") + 1;
        var hasHash = url.indexOf("#") + 1;
        var appendix = (hasQuery ? "&" : "?") + paramName + "=" + id;
        url = hasHash ? url.replace("#", appendix + "#") : url + appendix;
//    alert(url);
        window.history.pushState("", "", url);
        //var History = window.History;
        //History.pushState("","",url);
    }
}


//function setNextFormURL(id) {
//
//    var url = window.location.href;
//    if (url.indexOf("?") !== -1) {
//        var url = url.split("?")[0];
//    }
//    var hasQuery = url.indexOf("?") + 1;
//    var hasHash = url.indexOf("#") + 1;
//    var appendix = (hasQuery ? "&" : "?") + "id=" + id;
//    url = hasHash ? url.replace("#", appendix + "#") : url + appendix;
////    alert(url);
//    window.history.pushState("", "", url);
//    //var History = window.History;
//}

function setNextFormURL(paramName, id) {

    var url = window.location.href;
    if (url.indexOf("?") !== -1) {
        var url = url.split("?")[0];
    }
    var hasQuery = url.indexOf("?") + 1;
    var hasHash = url.indexOf("#") + 1;
    var appendix = (hasQuery ? "&" : "?") + paramName + "=" + id;
    url = hasHash ? url.replace("#", appendix + "#") : url + appendix;
//    alert(url);
    window.history.pushState("", "", url);
    //var History = window.History;
}


function updateSupplierButtonsDisplay() {

    if (PF('isSupplier').isChecked()) {
        $('.vendorButton').each(function () {
            $(this).css('display', 'inline-table');
        });

    } else {
        $('.vendorButton').each(function () {
            $(this).css('display', 'none');
        });

    }

}

function updateCustomerButtonsDisplay() {

    if (PF('isCustomer').isChecked()) {
        $('.customerButton').each(function () {
            $(this).css('display', 'inline-table');
        });

    } else {
        $('.customerButton').each(function () {
            $(this).css('display', 'none');
        });

    }
}

function isNumber(o) {
    return !isNaN(o - 0) && o !== null && o !== "" && o !== false;
}

function hidePaginator(tableId) {
    var numOfRows = getTableLenght(tableId);
    if (numOfRows < 25) {
        $(".ui-paginator-bottom").hide();
    } else {
        $(".ui-paginator-bottom").show();
    }
}

function hideGridPaginator(tableId) {
    var numOfRows = getGridLenght(tableId);
    if (numOfRows < 9) {
        $(".ui-paginator-bottom").hide();
    } else {
        $(".ui-paginator-bottom").show();
    }
}

function getTableLenght(tableId) {
    return $(document.getElementById(tableId)).find('table tbody tr').length;
}

function getTableWidth(tableId) {
    return $(document.getElementById(tableId)).find('table thead tr th').length;
}

function getGridLenght(tableId) {
    return $(document.getElementById(tableId)).find('.ui-datagrid-content .ui-grid-row').length;
}



function addEmptyRow(tableId) {
    var numOfColumns = getTableWidth(tableId);
    var numOfRows = getTableLenght(tableId);
    var rowCells;

    for (var i = 0; i < numOfColumns; i++) {
        rowCells += '<td>&#160;</td>';
    }

    if ((numOfRows % 2) === 0) {
        $(document.getElementById(tableId)).find('table > tbody:last').append('<tr class="ui-datatable-even">' + rowCells + '</tr>');
    } else {
        $(document.getElementById(tableId)).find('table > tbody:last').append('<tr class="ui-datatable-odd">' + rowCells + '</tr>');
    }
}

function addEmptyRows(tableId) {

    var numOfExtraRows;
    var numOfRows = getTableLenght(tableId);


    if (numOfRows === 0) {
        numOfExtraRows = 4;
    } else if (numOfRows === 1) {
        numOfExtraRows = 3;
    } else if (numOfRows === 2) {
        numOfExtraRows = 2;
    } else if (numOfRows === 3) {
        numOfExtraRows = 1;
    } else {
        numOfExtraRows = 0;
    }

    if (numOfExtraRows > 0) {
        var rowCells;
        var numOfColumns = getTableWidth(tableId);

        for (var i = 0; i < numOfColumns; i++) {
            rowCells += '<td>&#160;</td>';
        }

        for (var i = 0; i < numOfExtraRows; i++) {

            if ((numOfRows % 2) === 0) {
                $(document.getElementById(tableId)).find('table > tbody:last').append('<tr class="ui-datatable-even">' + rowCells + '</tr>');
            } else {
                $(document.getElementById(tableId)).find('table > tbody:last').append('<tr class="ui-datatable-odd">' + rowCells + '</tr>');
            }

            numOfRows++;
        }
    }
}

function columnSum(columnClass, totalClass) {
    var sum = 0;
    $("." + columnClass).each(function () {
        var value = $(this).text().substring(1);   
        if (value.length !== 0) {
            value = value.replace(/,/g, '');
            if (!isNaN(value)) {
                sum += parseFloat(value);
            }
        }

    });

    sum = formatNumber(sum, 2);

    $("." + totalClass).text(sum);
}

function columnSumNonCurrency(columnClass, totalClass) {
    var sum = 0;
    $("." + columnClass).each(function () {
        var value = $(this).text();
        if (value.length !== 0) {
            value = value.replace(/,/g, '');
            if (!isNaN(value)) {
                sum += parseFloat(value);
            }
        }

    });

    sum = formatNumber(sum, 2);

    $("." + totalClass).text(sum);
}


function formatNumber(num, fraction) {
    if(num !== undefined && num !== null){
        num = num.toFixed(fraction);
        num = addCommas(num);
        return num;
    }
    
    return 0;
   
    
}

function removeFraction(num) {
    if(num !== undefined && num !== null){
        num = num.toFixed(0); 
        return num;
    }    
    return 0;
}



function intToFloat(num) {
    if(num !== undefined && num !== null){
        num = num.toFixed(2);  
        return num;
    }
    return 0;
}

addCommas = function (input) {
    // If the regex doesn't match, `replace` returns the string unmodified
    return (input.toString()).replace(
            // Each parentheses group (or 'capture') in this regex becomes an argument 
            // to the function; in this case, every argument after 'match'
            /^([-+]?)(0?)(\d+)(.?)(\d+)$/g, function (match, sign, zeros, before, decimal, after) {

                // Less obtrusive than adding 'reverse' method on all strings
                var reverseString = function (string) {
                    return string.split('').reverse().join('');
                };

                // Insert commas every three characters from the right
                var insertCommas = function (string) {

                    // Reverse, because it's easier to do things from the left
                    var reversed = reverseString(string);

                    // Add commas every three characters
                    var reversedWithCommas = reversed.match(/.{1,3}/g).join(',');

                    // Reverse again (back to normal)
                    return reverseString(reversedWithCommas);
                };

                // If there was no decimal, the last capture grabs the final digit, so
                // we have to put it back together with the 'before' substring
                return sign + (decimal ? insertCommas(before) + decimal + after : insertCommas(before + after));
            }
    );
};


function showSearchLoader() {
    $(".searchLoader").css('display', 'inline');
}

function hideSearchLoader() {
    $(".searchLoader").css('display', 'none');
}

function sortTable(tableId) {

    var rows = $(document.getElementById(tableId)).find('table tbody tr').get();

    rows.sort(function (a, b) {

        var A = $(a).children('td').eq(1).text().toUpperCase();
        var B = $(b).children('td').eq(1).text().toUpperCase();

        if (A < B) {
            return -1;
        }

        if (A > B) {
            return 1;
        }

        return 0;

    });

    $.each(rows, function (index, row) {

        $(this).removeClass('ui-datatable-even');
        $(this).removeClass('ui-datatable-odd');

        if ((index % 2) === 0) {
            $(this).addClass('ui-datatable-even');
        } else {
            $(this).addClass('ui-datatable-odd');
        }

        $(document.getElementById(tableId)).find('table').children('tbody').append(row);
    });

}


//function viewSaleOrderURL(id) {
//
//    var url = window.location.href;
//    
//    if (url.indexOf("id=") === -1) {
//        var href = location.href;
//        var hasQuery = href.indexOf("?") + 1;
//        var hasHash = href.indexOf("#") + 1;
//        var appendix = (hasQuery ? "&" : "?") + "id=" + id;
//        url = hasHash ? href.replace("#", appendix + "#") : href + appendix;
//        window.history.pushState("", "", url);
//        //var History = window.History;
//        //History.pushState("","",url);
//    }
//}


PrimeFaces.locales ['fr'] = {
    closeText: 'Fermer',
    prevText: 'Précédent',
    nextText: 'Suivant',
    monthNames: ['Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre'],
    monthNamesShort: ['Jan', 'Fév', 'Mar', 'Avr', 'Mai', 'Jun', 'Jul', 'Aoû', 'Sep', 'Oct', 'Nov', 'Déc'],
    dayNames: ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'],
    dayNamesShort: ['Dim', 'Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam'],
    dayNamesMin: ['D', 'L', 'M', 'M', 'J', 'V', 'S'],
    weekHeader: 'Semaine',
    firstDay: 1,
    isRTL: false,
    showMonthAfterYear: false,
    yearSuffix: '',
    timeOnlyTitle: 'Choisir l\'heure',
    timeText: 'Heure',
    hourText: 'Heures',
    minuteText: 'Minutes',
    secondText: 'Secondes',
    currentText: 'Maintenant',
    ampm: false,
    month: 'Mois',
    week: 'Semaine',
    day: 'Jour',
    allDayText: 'Toute la journée'
};




function disableStatusLinks() {

    $(".breadcrumbs a").click(function (e) {
        e.preventDefault();
    });
}

var activeTab = 'form-tab-1';

function setActiveTab() {

    if (activeTab === 'form-tab-2') {
        $('a[href$="form-tab-2"]').parent().addClass("active");
        $("#form-tab-2").addClass("active");
        $('a[href$="form-tab-1"]').parent().removeClass("active");
        $("#form-tab-1").removeClass("active");

    } else {

        $('a[href$="form-tab-1"]').parent().addClass("active");
        $("#form-tab-1").addClass("active");
        $('a[href$="form-tab-2"]').parent().removeClass("active");
        $("#form-tab-2").removeClass("active");
    }

}

$(document).on("click", ".form-tab", function (e) {
    if ($(e.target).attr('href') === '#form-tab-1') {
        activeTab = 'form-tab-1';

    } else if ($(e.target).attr('href') === '#form-tab-2') {
        activeTab = 'form-tab-2';
    }
});




//    $(".float-number").each(function () {
//         alert('fffff');
//        var value = $(this).text();
//        alert(value.length);
//        if (value.length > 20) {
//
//            var number = parseFloat(value);
//            if (!isNaN(number)) {
//                // The string value entered in the textbox was successfully parsed to a number
//                // we can now calculate the exponential:
//                $(this).text(number.toExponential(13));
//            }
//        }
//
//    });




// if($("#SaleOrderForm\\:orderDate_input").val() === ''){
//            if(!($("#SaleOrderForm\\:orderDate_input").hasClass('ui-state-error'))){
//                PF('orderDate').setDate(new Date()); 
//            }
//        } 




