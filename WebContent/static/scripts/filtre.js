	//
	// Filtrage des lignes
	//
	var checkCount = new Array();
	
	function initSearch() {
		var filterField = $("#mainForm\\:globalFilter");
		
		if (filterField.length) {
			$(".mainTable_container").each(function (idx) {
				var listName = $(this).attr('id').substring(5);
				showLines(null, filterField.val(), listName);
				setFocusListe();
			});
		}
		
	}
	
	function setFocusListe(){
		document.getElementById("mainForm:globalFilter").select();
	}
	
	function showLines(e, data, listName) {
		var t = $("table[id$='table" + listName + "']");
		$.uiTableFilter(t, data);
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
		var checkList = $("input[type='checkbox'][name*='" + listName + "']");
		
		if (mainCheckBox.checked) {
			checkList.prop('checked', true);
			checkCount[listName] = checkList.size();
		} else {
			checkList.prop('checked', false);
			checkCount[listName] = 0;
		}
		
		manageButtons(listName);
	}
	
	
	function clickCheck(check, listName) {
		if (check.checked) {
			checkCount[listName] = checkCount[listName]+1;
		} else {
			checkCount[listName] = checkCount[listName]-1;
		}
		
		manageButtons(listName);
	}
	
	function clickCheckTreeTable(listName) {
		checkCount[listName] = $('.ui-icon-check').size() + $('.ui-icon-minus').size();
		manageButtons(listName);
	}
	
	function manageButtons(listName) {
		var checkDisabled = true;
		if (checkCount[listName] > 0) {
			checkDisabled = false;
		}

		$("#button" + listName + " input[type='submit']").attr('disabled', checkDisabled);
	}
