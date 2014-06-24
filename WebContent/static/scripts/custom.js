
/**
 * Method to allow JS page customization.
 * 
 * @param pageName the name of the page
 */
function doPageCustomize(pageName) {
	try {
		$("#mainForm .colFieldSetRight input:visible, #mainForm .colFieldSetRight select:visible").first().focus();
	} catch (e) {}
}

/**
 * Method to allow JS list customization.
 * 
 * @param queryName the name of the query
 * @param pageName the name of the page displaying the list
 */
function doListCustomize(queryName, pageName) {
}

/**
 * Disable fixed list header
 */
function initList(listName) {
	$('#datatable-div-header-' + listName).remove();
	$('#datatable-div-data-' + listName).css('visibility', 'visible');
	$('td[class="first"]').click(function(event) {
		event.stopPropagation();
	});
}