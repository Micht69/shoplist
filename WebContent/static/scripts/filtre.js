//
// Filtrage des lignes
//
var checkCount = new Array();

function setFocusListe(){
	document.getElementById("mainForm:globalFilter").select();
}

function showLines(e, data, listName) {
	var t = $("table[id$='table" + listName + "']");
	$.uiTableFilter(t, data, null, linesHidden);
}

function linesHidden() {
	// Display create line on editable list
	var inputNewRow = $("input.rownum[value=-1]");
	if (inputNewRow)
		inputNewRow.parents("tr").show();
}

function cleanFilter(listName) {
	$("#mainForm\\:globalFilter_"+listName).val('');
}

function initButtons() {
	$(".listButtons").each(function (idx) {
		var listName = $(this).attr('id').substring(6);
		checkCount[listName] = $("[id$='" + listName + "'] td.first input[type='checkbox']:checked").size();
		manageButtons(listName);
	});
}

function manageChecks(mainCheckBox, listName) {
	var checkList = $("input[type='checkbox'][name*='" + listName + ":']");

	if (mainCheckBox.checked) {
		checkCount[listName] = 0;
		checkList.each(function() {
			if ($(this).parent().parent().css("display") != "none") {
				$(this).prop('checked', true);
				checkCount[listName]++;
			}
		});
	} else {
		checkList.prop('checked', false);
		checkCount[listName] = 0;
	}
	manageButtons(listName);
}

function clickCheck(check, listName) {
	checkCount[listName] = checkCount[listName] + (check.checked ? 1 : -1);
	manageButtons(listName);
}

function clickCheckTreeTable(listName) {
	checkCount[listName] = $('.ui-icon-check').size() + $('.ui-icon-minus').size();
	manageButtons(listName);
}

function manageButtons(listName) {
	$("#button" + listName + " input[type='submit']").attr('disabled', !(checkCount[listName] > 0));
}
