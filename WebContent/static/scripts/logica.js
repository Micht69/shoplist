/* I'm starting to put functions in a namespace, because I'm worried a function named debug already exists somewhere else.
 * To be continued on other functions in here. */
cgi = {};

KEYCODE_TAB = 9;
KEYCODE_ENTER = 13;
KEYCODE_ESCAPE = 27;
KEYCODE_UP_ARROW = 38;
KEYCODE_DOWN_ARROW = 40;
KEYCODE_LEFT_ARROW = 37;
KEYCODE_RIGHT_ARROW = 39;

/* Debug logs */
cgi.DEBUG_ENABLED = false;
cgi.debug = function() {
	if (cgi.DEBUG_ENABLED) {
		var a = arguments;
		/* AWFULLY UGLY. But console.log.apply is implementation-dependent... */
		if (a.length == 1)
			console.log(a[0]);
		else if (a.length == 2)
			console.log(a[0], a[1]);
		else if (a.length == 3)
			console.log(a[0], a[1], a[2]);
		else if (a.length == 4)
			console.log(a[0], a[1], a[2], a[3]);
		else if (a.length == 5)
			console.log(a[0], a[1], a[2], a[3], a[4]);
		else
			for (arg in a)
				console.log(arg);
	}
};

/* Adding string.trim() on Internet Explorer <9 */
if (String.prototype.trim === undefined) {
	String.prototype.trim = function() {
		return this.replace(/^\s+|\s+$/g,"");
	};
}

// Caract�res invalides, exemple:
// var invalidChars = ['a','b','c'];
// Attention, les caract�res hors a-z et 0-9 doivent �tre en unicode
// Ci-dessous, interdiction du caract�re euro
var invalidChars = [ '\u20ac' ];
// Caract�re d'autosplit
var autosplit = '||';

var listInitialized = true; 

function displayCriteres() {
	var $criteresBody = $('#criteresBody');
	$criteresBody.toggle();
	$('#openBody').toggle();
	$('#closeBody').toggle();

	var $resultatsBody = $('#resultatsBody');
	$resultatsBody.toggle();
	$('#openResultats').toggle();
	$('#closeResultats').toggle();

	if ($criteresBody.is(":visible")) {
		$criteresBody.closest(".ui-accordion").addClass("developed").removeClass("reduced");
	} else {
		$criteresBody.closest(".ui-accordion").addClass("reduced").removeClass("developed");
	}

	if ($resultatsBody.is(":visible")) {
		$resultatsBody.closest(".ui-accordion").addClass("developed").removeClass("reduced");
	} else {
		$resultatsBody.closest(".ui-accordion").addClass("reduced").removeClass("developed");
	}
	
	$("input[id$='displayCriterias']").val($criteresBody.is(":visible")); 
	
	if (!listInitialized) {
		var listName = $('#tableIdList').val();
		$('#datatable-div-data-' + listName).css('height', '');
		$('#datatable-div-data-' + listName).css('visibility', 'visible');
		$('.list_results_container').css('height', '');
		initList(listName);
		listInitialized = true;
	}
}

function displayResults() {
	displayCriteres();
}

$(document).ready(function() {
	/* Collapsable fieldsets */
	$('.actionContent').each(function() {
		this.closeOpen = function() {
			var contentImage = $(this);
			var contentTable;
			var block;

			if (contentImage.parent().hasClass('table_title')) {
				block = contentImage.parents('.mainTable_container').first();
				contentTable = block.children('.table_container');
			} else {
				block = contentImage.parents('.form_container').first();
				contentTable = block.children('table');
			}

			if (contentTable.length) {
				if (contentTable.is(':visible')) {
					contentTable.stop().hide();
					contentImageSrc = contentImage.attr('src').replace(/close/ig, "open");
					block.addClass("reduced").removeClass("developed");
				} else {
					contentTable.stop().show();
					contentImageSrc = contentImage.attr('src').replace(/open/ig, "close");
					block.addClass("developed").removeClass("reduced");
					if (window.initMap) {
						initMap();
					}
					contentTable.find('.schedule').each(function() {
						var schedule = $(this).children('.ui-widget');

						if (schedule.length > 0 && schedule.data('full-calendar')) {
							schedule.fullCalendar('render');
						}
					});
					displayTables(contentTable);
				}
				contentImage.attr('src', contentImageSrc);
			}
		};
	});

	$('.actionContent').click(function() {this.closeOpen();});
	$('.actionContent').parent().click(function() {$(this).children('.actionContent')[0].closeOpen();});

	// Gestion des champs "disabled"
	$("select:disabled").each(function(idx) {
		$(this).hide();

		var str = '';
		$("option:selected", $(this)).each(function() {
			str += $(this).text() + " ";
		});
		$(this).before(str);
	});
	$("input:disabled").not("[type='submit']").not("[type='button']").not("[type='checkbox']").each(function(idx) {
		$(this).hide();
		$(this).before($(this).val());
	});
	$("textarea:disabled").each(function(idx) {
		$(this).hide();
		$(this).before($(this).val().replace(/\n/g, '<br/>\n'));
	});

	var selectedTabs = new Array();

	$.each($("input[id*='tabs-selected']"), function(index, value) {
		// value is the inputText field which may contain previously selected tab
		if (value.value != '') {
			var selected = "#tabs-" + value.value;
			selectedTabs[value.id.replace("mainForm:", "") + "-tabs"] = selected;
		}
	});

	// Tabs
	$(".jquery-ui-tabs").tabs({
		activate : function(event, ui) {
			// ui.panel.id ends with "tabs-tabGroupName_groupTemplateName".
			// We need only parent Name
			var tabId = ui.newPanel.attr('id');
			var tabGroupName = tabId.split('_')[0].substring(5);
			var tabsSelectedMapId = 'tabs-selected_' + tabGroupName;
			$("input[id$='" + tabsSelectedMapId + "']").val(tabId.substring(5));

			textareaResizeToContent(ui.newPanel);
			if (window.initMap) {
				try {
					initMap(tabId);
				} catch (e) {
					if (window.console && console.error)
						console.error(e.message);
				}
			}
			displayTables(ui.newPanel);
		}
	});
	textareaResizeToContent();
	if (window.initMap) {
		try {
			initMap();
		} catch (e) {
			if (window.console && console.error)
				console.error(e.message);
		}
	}

	// Edit the tabs
	$(".ui-tabs .ui-tabs-nav li").prepend('<img class="imgleft" src="../static/img/layout/titlebar_top_left.gif" alt="" />');
	$(".ui-tabs .ui-tabs-nav li").append('<img class="imgright" src="../static/img/layout/titlebar_top_right.gif" alt="" />');

	// Clean enpty TD look
	$("td:empty").html("&nbsp;");

	// Menu
	$("#menu").menu({
		position : {
			my : "left top",
			at : "left bottom",
			using : function(hash, feedback) {
				var parentMenus = feedback.element.element.parentsUntil("#menu", ".ui-menu");

				if (parentMenus.length > 0) {
					feedback.element.element.css({
						top : hash.top - feedback.target.height,
						left : feedback.target.width
					});
				} else {
					feedback.element.element.css({
						top : hash.top,
						left : hash.left
					});
				}
			}
		}
	});
	$('#menu li.ui-menu-item').each(function() {
		if ($.trim($(this).text()) == "") {
			$(this).empty().remove();
		}
	});
	$('#menu ul.ui-menu').each(function() {
		if ($.trim($(this).text()) == "") {
			$(this).parent().empty().remove();
		}
	});
	// Do it twice to handle 3 levels menus
	$('#menu ul.ui-menu').each(function() {
		if ($.trim($(this).text()) == "") {
			$(this).parent().empty().remove();
		}
	});

	imageRollover();
	submitRollover();

	// Textareas
	$(".textareacontrol1 textarea").bind('keyup', function() {
		doTextareaControlChange(this);
	});

	$(".textareacontrol1 textarea").each(function(idx) {
		doTextareaControlChange(this);
	});

	for (tabId in selectedTabs) {
		$("#" + tabId).tabs("option", "active", $(selectedTabs[tabId]).index() - 1);
	}

	var linkActionButtons = $('.link-actions-buttons');
	/* Empty list items are removed (link has not been rendered). */
	linkActionButtons.find('li:empty').remove();
	/* For each action list without any list item, action anchor and action div are removed. */
	linkActionButtons.find('ul').each(function(idx, element) {
		var ul = $(element);

		if (ul.find('li').length == 0) {
			var div = ul.parent();
			div.parent().find('a.link-actions').remove();
			div.remove();
		}
	});

	$(".link-actions").click(function(event) {
		var divClick = $(this).children().first();
		divClick.toggleClass('ui-icon-circle-triangle-n');
		var divButtons = $(this).next();
		var hidden = divButtons.css('display') != 'block';

		if (hidden) {
			divButtons.removeClass('inverse-x');
			divButtons.removeClass('inverse-y');

			var divClickPos = divClick.position();
			var divClickOffset = divClick.offset();
			var divButtonsMarginLeft = divButtons.css('margin-left');
			divButtonsMarginLeft = parseInt(divButtonsMarginLeft.replace('px', ''));

			if (divButtonsMarginLeft < 0 && (divClickOffset.left + divButtonsMarginLeft) < 0) {
				divButtons.addClass('inverse-x');
			}
			divButtons.css('left', (divClickPos.left + 16) + 'px');

			var body = $("body");
			var bodyHeight = body.innerHeight() - parseInt(body.css('padding-bottom').replace('px', ''));
			var divButtonsHeight = divButtons.outerHeight(true);

			if (divClickOffset.top + divButtonsHeight > bodyHeight) {
				divButtons.addClass('inverse-y');
				divButtons.css('top', divClickPos.top - divButtonsHeight + 'px');
			} else {
				divButtons.css('top', divClickPos.top + 'px');
			}
		}
		divButtons.toggle();
	});
	
	onChangeCriteriaCombo();
	initPopup();

	$("input[id$='_launchActionSchedule']").val('');

	initButtons();

	$('[id$="maxRowCriteria"]').keyup(function () { 
		this.value = this.value.replace(/[^0-9\.]/g,'');
	});
	$('.linklist-filter').trigger('keyup');
});
Array.prototype.contains = function(obj) {
	var i = this.length;
	while (i--) {
		if (this[i] == obj) {
			return true;
		}
	}
	return false;
};
String.prototype.txtAreaCheck = function() {
	var arr = this.split('');

	for ( var i = 0; i < arr.length; i++) {
		if (invalidChars.contains(arr[i])) {
			arr[i] = '<span class="txtAreaError">' + arr[i] + '</span>';
		}
	}
	return arr.join('');
};
String.prototype.replaceAll = function(searchValue, newValue) {
	var arr = this.split(searchValue);

	for ( var i = 0; i < arr.length - 1; i++) {
		arr[i] = arr[i] + newValue;
	}
	return arr.join('');
};

function doTextareaControlChange(textarea) {
	var txt = $(textarea);
	var v = txt.val();
	v = v.replace(/</g, '&lt;').replace(/>/g, '&gt;');
	v = v.txtAreaCheck();
	v = v.replace(/\n/g, '<br>\n');

	// Edit div
	txt.parents(".colFieldSetRight:first").find(".textareacontrol2").html(v);

	// count chars
	txt.parents(".colFieldSetRight:first").find(".textarealength").html(txt.val().length + ' ');
}
function textareaResizeToContent($parent) {
	var $elements = (undefined !== $parent) ? $parent.find('textarea') : $('textarea');
	$elements.each(function() {
		var $textarea = $(this);
		var h = $textarea.height();
		if (h < 50)
			h = 50;
		if (h > 300)
			h = 300;
		$textarea.height(h);
	});
}

var imageRollover = function() {
	$("img.rollover").hover(function() {
		$(this).attr('src', newImg($(this).attr('src')));
	}, function() {
		$(this).attr('src', oldImg($(this).attr('src')));
	});

	var newImg = function(src) {
		return src.substring(0, src.search(/(\.[a-z]+)$/)) + '_on' + src.match(/(\.[a-z]+)$/)[0];
	};
	var oldImg = function(src) {
		return src.replace(/_on\./, '.');
	};
};

var submitRollover = function() {
	$('input[type=submit],input[type=reset]').each(function() {
		$(this).wrap('<a href="javascript:void(0)" class="' + this.className + '"></a>');
		this.className = "";
	});
};


function testDate(s) {
	if (s.value != "") {
		ok = /^(\d{1,2})([-\/\ \.]?)(\d{1,2})\2?(\d{1,4})$/.test(s.value); // tester date
		if (!ok) {
			errDate(s);
			return;
		}
		var jour = Math.abs(RegExp.$1), mois = Math.abs(RegExp.$3), an = Math.abs(RegExp.$4); // assigner int
		/* pour les années 0 à 39, on considére qu'il s'agit de 2000 à 2039 */
		/* pour les années 40 à 99, on considére qu'il s'agit de 1940 à 1999 */
		if (an < 40)
			an += 2000;
		else if (an < 100)
			an += 1900;
		/* Années bissextiles : divisibles par 4, sauf siécles. Les millénaires sont bissextiles. */
		var joursMois = new Array(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
		if (an % 4 == 0 && (an % 100 > 0 || an % 1000 == 0))
			joursMois[1] = 29;
		/* Tester jours et mois */
		if (jour == 0 || mois == 0 || mois > 12 || jour > joursMois[mois - 1]) {
			errDate(s);
			return;
		}
		// Si OK, retour formatté
		if (jour < 10)
			jour = "0" + jour;
		if (mois < 10)
			mois = "0" + mois;
		s.value = jour + "/" + mois + "/" + an;
	}
}
function errDate(s) {
	// En cas d'erreur, on ne fait rien
}

function cleanDirty() {
	document.getElementById("mainForm:dirty").value = "false";
}

function markAsDirty(element) {
	document.getElementById("mainForm:dirty").value = "true";
}

function isPageDirty() {
	var dirty = document.getElementById("mainForm:dirty");
	return dirty != null && dirty.value == "true";
}

function downloadFile() {
	$(".btn_download").children().click();
}

function goToDefaultPage() {
	$(".btn_medium").children().click();
}

/** Default checkDirtyState : on a yes, click again on the same element that triggered the call. */
function checkDirtyState(trigger) {

	var clickAgain = function() {
		$(trigger).click();
	};

	var onYes = function() {
		cleanDirty();
		setTimeout(clickAgain, 100);
		return false;
	};

	return cgi.doCheckDirtyState(onYes);
}

/**
 * If the state of the page is not dirty, returns true. If it is, returns false then set up a modal pop-up with two
 * buttons Yes and No.
 * @param onAbandon : function to execute when chosing to abandon modifications (by clicking the yes button).
 * @param onClose : function to execute when closing the modal in any way (clicking yes, no or hitting Escape), default
 *            is to do nothing. Called before onAbandon, if both are pertinent.
 * @param evaluation {Function} : Function used to test the dirty state, default is function isPageDirty.
 */
cgi.doCheckDirtyState = function(onAbandon, onClose, evaluation) {
	var evaluationFunc = evaluation || isPageDirty;

	if (!evaluationFunc()) {
		return true;
	}

	var onCloseFunc = onClose || $.noop;
	var modalBox = $('#modalConfirmDirty');
	var boxTitle =  modalBox.attr("title");
	var yesButton = $('#modalYesButton').attr("title");
	var noButton = $('#modalNoButton').attr("title");

	var buttons = {};
	buttons[yesButton] = function() {
		$(this).dialog('close');
		onAbandon();
	};
	buttons[noButton] = function() {
		$(this).dialog('close');
	};

	modalBox.dialog({
		modal : true,
		resizable : false,
		closeOnEscape : true,
		draggable : true,
		position : [ 'center', 200 ],
		title : boxTitle,
		buttons : buttons,
		close : onCloseFunc
	});

	/* don't transmit clicks */
	$(".ui-front").click(function() {
		return false;
	});

	/* copy missing title attribute */
	modalBox.attr("title", boxTitle);

	/* no closing icon */
	modalBox.parent().find(".ui-dialog-titlebar-close").hide();

	return false;
};

/**
 * If the state of the page is not dirty or the user validates anyway, execute the onContinue function. The onAnyCase
 * function will be called in any case, just before executing onContinue if both are pertinent, but after closing the
 * modal if one is necessary.
 * @return true if no confirmation box was necessary
 */
cgi.withCheckDirty = function(onContinue, onAnyCase, evaluation) {
	var bool = cgi.doCheckDirtyState(onContinue, onAnyCase, evaluation);

	if (bool) {
		onAnyCase();
		onContinue();
	}
	return bool;
};

var padding = 0;

function initList(listName) {
	// Compute list height
	var listResultsContainer = $('.list_results_container').first();
	var newListHeight;

	if (!listResultsContainer.attr('style')) {
		var windowHeight = $(window).height();
		var documentHeight = $(document).height();
		var overflow = documentHeight - windowHeight;

		newListHeight = listResultsContainer.outerHeight(true) - overflow - 20; // FIXME Why 20 as magic number ?
		listResultsContainer.height(newListHeight);
	} else {
		newListHeight = listResultsContainer.height();
	}
	
	// Adjust with list headers
	var listHeaderHeight = $('#datatable-div-header-' + listName).height();
	$('#datatable-div-data-' + listName).height(newListHeight - listHeaderHeight);

	// Compute columns width
	datatableAlignColumns(listName);
	
	// Has list headers height changed ?
	if (listHeaderHeight != $('#datatable-div-header-' + listName).height()) {
		$('#datatable-div-data-' + listName).height(newListHeight - $('#datatable-div-header-' + listName).height());
	}

	$('td[class="first"]').click(function(event) {
		event.stopPropagation();
	});
}

function datatableAlignColumns(listName, forceAlignment) {
	if (!$('#datatable-div-header-' + listName).is(':visible')) {
		return; // Do not touch non visible tables (in tabs for instance)
	}
	if ($('#datatable-div-header-' + listName).data('initialized') && !forceAlignment) {
		return; // Initialize only once 
	}
	var headers = $('#datatable-div-header-' + listName + ' col');
	var cols = $('#datatable-div-data-' + listName + ' col');

	$('#datatable-div-data-' + listName + ' table tr th').each(function(index) {
		if (index == 0 || index == (headers.size() - 1)) {
			// First column with checkbox is set with CSS, last column just fills end of line
			return true;
		}
		var innerWidth = $(this).innerWidth()+1;
		var width = $(this).width();

		$(cols[index]).width(innerWidth);
		$(headers[index]).width(innerWidth);
	});
	$('#datatable-div-data-' + listName + ' thead').css('visibility', 'hidden');
	var headerHeight = $('#datatable-div-data-' + listName + ' thead').innerHeight() + 1;
	$('#datatable-div-data-' + listName + ' table').css('margin-top', -headerHeight);
	$('#datatable-div-header-' + listName).css('visibility', 'visible');
	$('#datatable-div-data-' + listName).css('visibility', 'visible');
	$('#datatable-div-header-' + listName).data('initialized', true);
}

function initTreeTable(listName) {
	var treeTable = $('.ui-treetable-scrollable-body').first();
	treeTable.css('display', 'none');
	// FIXME : Corriger formule ici
	var height = $(".wrapper").first().height() - $(".content").first().outerHeight(true);
	height -= (treeTable.outerHeight(true) - treeTable.innerHeight());
	height -= (treeTable.innerHeight() - treeTable.height());
	height -= 20;
	treeTable.css('height', height);
	treeTable.css('display', 'block');
}

var actionClicked = false;
function launchAction(encodedKey) {
	if (actionClicked) return;
	actionClicked = true;
	
	$("input[id$='launchActionSelected']").val(encodedKey);
	$("input[id$='launchAction']").click();
}

function launchLinkAction(encodedKey, listName) {
	if (actionClicked) return;
	actionClicked = true;
	var commandName = listName + '_launchAction';
	$("input[id$='_launchActionSelected']").val(encodedKey);
	$("input[id$='" + commandName + "']").click();
}

var launchTreeActionClicked = false;
function launchTreeAction(encodedKey) {
	encodedKey = $('[id$="nodeSelected"]').val();
	if (encodedKey == "")
		return false;
	if (launchTreeActionClicked)
		return false;
	launchTreeActionClicked = true;
	$("input[id$='launchActionSelected']").val(encodedKey);
	$("input[id$='launchAction']")[0].click();
	return true;
}

function completeDate(elem) {
	var $elem = $(elem);
	var dateStr = $elem.val();
	var date = undefined;

	try {
		date = $.datepicker.parseDate('dd/mm/yy', dateStr);
	} catch (e) {

	}

	if (!date) {
		var patterns = [ 'dd/mm/y', 'ddmmyy', 'ddmmy', 'dd/mm', 'ddmm' ];
		var i = 0;

		while (!date && i < patterns.length) {

			try {
				date = $.datepicker.parseDate(patterns[i], dateStr);
			} catch (e) {

			}
			i++;
		}

	}

	if (date) {
		$elem.val($.datepicker.formatDate('dd/mm/yy', date));
	} else {
		return false;
	}
}

/**
 * Adds an Event Listener to the current document. Type is the event type, NOT prefixed by "on", e.g. "click",
 * "keyDown". Listener can take on argument, which will be the event.
 * 
 * You can define wether it should be call during the capture phase (capture=true) or the bubble phase (capture=false).
 * Default is capture phase.
 */
addDocumentEventListener = function(type, listener, capture) {
	capture = (typeof capture !== 'undefined') ? capture : true;
	if (document.addEventListener) {
		// IE >=9 ou non-IE
		return document.addEventListener(type, listener, capture);
	} else if (document.attachEvent) {
		// IE <=8
		return document.attachEvent("on" + type, listener);
	} else {
		console.log("addDocumentEventListener impossible :", type);
	}
};

/**
 * Adds a single use Event Listener to the current document. Works as addDocumentEventListener, but the event listener
 * should return a boolean. If it returns true, then it is discarded after use.
 * 
 * You can define wether it should be call during the capture phase (capture=true) or the bubble phase (capture=false).
 * Default is capture phase.
 * 
 */
addSingleUseDocumentEventListener = function(type, listener, capture) {
	capture = (typeof capture !== 'undefined') ? capture : true;
	if (document.addEventListener) {
		// IE >=9 ou non-IE
		var useOnce = function(event) {
			if (listener(event)) {
				document.removeEventListener(type, arguments.callee, capture);
			}
		};
		document.addEventListener(type, useOnce, true);
	} else if (document.attachEvent) {
		// IE <=8
		var useOnce = function(event) {
			if (listener(event)) {
				document.detachEvent("on" + type, arguments.callee);
			}
		};
		document.attachEvent("on" + type, useOnce);
	} else {
		console.log("addSingleUseDocumentEventListener impossible", type);
	}
};

logAjaxEvent = function(data) {
	if (data.status == "begin") {
		cgi.debug("Begin Ajax call on", data.source);
	} else if (data.status == "complete") {
		cgi.debug("Completion of Ajax call on", data.source, "with code [", data.responseCode, "]");
	} else if (data.status == "success") {
		cgi.debug("Success of Ajax call on", data.source, "with code [", data.responseCode, "]");
	} else {
		console.log("Unknown status", data.status, "of Ajax call on", data.source);
	}
};

logAjaxError = function(data) {
	if (data.status == "httpError") {
		console.log("HTTP error for Ajax call on", data.source, "with :", status.description, "|", status.errorName, "|", status.errorMessage);
	} else if (data.status == "emptyResponse") {
		console.log("Empty response for Ajax call on", data.source, "with :", status.description, "|", status.errorName, "|", status.errorMessage);
	} else if (data.status == "malformedXML") {
		console.log("Malformed XML for Ajax call on", data.source, "with :", status.description, "|", status.errorName, "|", status.errorMessage);
	} else if (data.status == "serverError") {
		console.log("Server error for Ajax call on", data.source, "with :", status.description, "|", status.errorName, "|", status.errorMessage);
	} else {
		console.log("Unknown status", data.status + " of Ajax error on", data.source);
	}
};

/**
 * Delay the opening of a dialog box ; if it is closed before the delay expires it will not be shown at all.
 * 
 * @param jqDialog is a JQuery dialog already set up (with the autoOpen option set to false).
 * @param delay in milliseconds. Default is 100.
 * @param onceOpened a function to run when the dialog box has been opened. Default is nothing.
 * @return modal controller, on which you can call two methods : start to begin the delay countdown, and close to
 *         interrupt the countdown or close the dialog if it has been opened.
 */
delayedModal = function(jqDialog, delay, onceOpened) {
	delay = (typeof delay !== 'undefined') ? delay : 100;
	return {
		timer : null,
		opened : false,
		closed : false,

		open : function() {
			this.opened = true;
			if (!this.closed) {
				this.timer = window.setTimeout(function() {
					jqDialog.dialog("open");
					if (onceOpened !== undefined)
						onceOpened();
				}, delay);
			}
			return this;
		},

		close : function() {
			this.closed = true;
			if (this.timer != null) {
				window.clearTimeout(this.timer);
				if (jqDialog.dialog("isOpen")) {
					jqDialog.dialog("close");
				}
			}
		},

		running : function() {
			return this.opened && !this.closed;
		}
	};

};

/**
 * Set up a progress indicator for potentially long actions. Returns a control object : call start when the long process
 * starts, and close when it is over. Close can be called before start, (process is faster than the javascript code
 * being executed, for instance), in which case the indicator won't be displayed.
 * 
 * Uses the "saveInProgess div defined in the generic template.
 */
prepareProgressIndicator = function() {
	var div = $("#modalWorkInProgress");

	/* preparing modal dialog */
	div.dialog({
		autoOpen : false,
		modal : true,
		closeOnEscape : false,
		resizable : false,
		draggable : true,
		title : div.title,
		buttons : {}
	});

	var onceOpened = function() {
		/* don't transmit clicks */
		$(".ui-front").click(function() {
			return false;
		});

		/* no closing icon */
		$("#modalWorkInProgress").parent().find(".ui-dialog-titlebar-close").hide();
	};

	return delayedModal(div, 500, onceOpened);
};

/** Add the # prefix and escape special characters (especially colons...) */
toJqId = function(myid) {
	return "#" + myid.replace(/(:|\.|\[|\])/g, "\\$1");
};

function onChangeCriteriaCombo() {
	if ($(".filterCombo").val() == "") {
		$(".filter_load_command").css("display", "none");
		$(".filter_modify_command").css("display", "none");
		$(".filter_delete_command").css("display", "none");
	} else {
		$(".filter_load_command").css("display", "block");
		$(".filter_modify_command").css("display", "block");
		$(".filter_delete_command").css("display", "block");
	}
}

Object.size = function(arr) 
{
    var size = 0;
    for (var key in arr) 
    {
        if (arr.hasOwnProperty(key)) size++;
    }
    return size;
};

function filterLoadCommand() {
	$('input[type="radio"]:checked').filter('[value=""]').prop('checked', false);
	$(".link-actions").click();
	$("#mainForm\\\:criteriaLoadButton").click();
}

function filterModifyCommand() {
	$('input[type="radio"]:checked').filter('[value=""]').prop('checked', false);
	$(".link-actions").click();
	$("#mainForm\\\:criteriaModifyButton").click();
}

function filterDeleteCommand() {
	$('input[type="radio"]:checked').filter('[value=""]').prop('checked', false);
	$(".link-actions").click();
	$("#mainForm\\\:criteriaDeleteButton").click();
}

function filterCreateCommand() {
	$('input[type="radio"]:checked').filter('[value=""]').prop('checked', false);
	$(".link-actions").click();
	$("#createNewCriteriaDialog").dialog("open");
}

function initPopup() {
	 $("#createNewCriteriaDialog").dialog({
		 autoOpen: false,
		 appendTo: "#mainForm",
		 height: 150,
		 width: 350,
		 modal: true,
		 buttons: {
			 "Valider": function() {
				 $("#mainForm\\\:criteriaCreateButton").click();
				 $("#mainForm\\\:createNewCriteriaDialogName").val("");
				 $(this).dialog("close");
			 },
			 "Annuler": function() {
				$(this).dialog("close");
				$("#mainForm\\\:createNewCriteriaDialogName").val("");
			 }
		 },
		 close: function() {
				$("#mainForm\\\:createNewCriteriaDialogName").val("");
		 }
	 });
}

function displayTables($container) {
	var tables = $container.find('.mainTable_container');

	if (tables.length > 0) {
		tables.each(function() {
			var datatableId = $(this).attr('id');
			if (datatableId) {
				datatableAlignColumns(datatableId.substring(5));
			}
		});
	}
}
