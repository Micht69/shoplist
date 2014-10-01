
/**
 * Method to allow JS page customization.
 * 
 * @param pageName the name of the page
 */
function doPageCustomize(pageName) {
	try {
		$("#mainForm .col-value input:visible, #mainForm .col-value select:visible").first().focus();
	} catch (e) {}
}

/**
 * Method to allow JS list customization.
 * 
 * @param queryName the name of the query
 * @param pageName the name of the page displaying the list
 */
function doListCustomize(queryName, pageName) {
	$(".filter").focus();
	
	// Handle action fix
	$(window).scroll(function() {
		var $actions = $('.actions');
		if ($(this).scrollTop() > 165) {
			$actions.addClass('fixed');
			$actions.parent().css('padding-top', $actions.height());
		} else {
			$actions.removeClass('fixed');
			$actions.parent().css('padding-top', '0px');
		}
	});
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
