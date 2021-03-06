
/**
 * Method to allow JS page customization.
 * 
 * @param pageName the name of the page
 */
function doPageCustomize(pageName) {
	try {
		$("#mainForm .col-value input:visible, #mainForm .col-value select:visible").first().focus();
	} catch (e) {}
	// Handle numeric fields for mobile display
	if ($(window).width() < 980) {
		if (pageName == 'SHOP_LIST_L_ARTICLE') {
			$("input[id$='shopListLArticle_quantity']").attr('type', 'number');
		} else if (pageName == 'SHOP_SHELF') {
			$("input[id$='shopShelf_position']").attr('type', 'number');
		}
	}
}

/**
 * Method to allow JS list customization.
 * 
 * @param queryName
 *            the name of the query
 * @param pageName
 *            the name of the page displaying the list
 */
function doListCustomize(queryName, pageName) {
	$(".filter").focus();
	
	// Handle actions position for small screens
	if ($(window).width() < 980) {
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
