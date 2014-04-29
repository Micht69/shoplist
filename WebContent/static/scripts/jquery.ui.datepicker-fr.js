/* French initialisation for the jQuery UI date picker plugin. */
/* Written by Keith Wood (kbwood{at}iinet.com.au),
			  Stéphane Nahmani (sholby@sholby.net),
			  Stéphane Raimbault <stephane.raimbault@gmail.com> */
jQuery(function($){
	$.datepicker.regional['fr'] = {
		closeText: 'Fermer',
		prevText: '&lt;',
		nextText: '&gt;',
		currentText: 'Aujourd\'hui',
		monthNames: ['Janvier','Février','Mars','Avril','Mai','Juin',
		'Juillet','Août','Septembre','Octobre','Novembre','Décembre'],
		monthNamesShort: ['Janv.','Févr.','Mars','Avril','Mai','Juin',
		'Juil.','Août','Sept.','Oct.','Nov.','Déc.'],
		dayNames: ['Dimanche','Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'],
		dayNamesShort: ['Dim.','Lun.','Mar.','Mer.','Jeu.','Ven.','Sam.'],
		dayNamesMin: ['D','L','M','M','J','V','S'],
		weekHeader: 'Sem.',
		dateFormat: 'dd/mm/yy',
		firstDay: 1,
		isRTL: false,
		showMonthAfterYear: false,
		yearSuffix: ''};
	$.datepicker.setDefaults($.datepicker.regional['fr']);
});

/* French initialisation for the jQuery UI dateTime picker plugin. */
$.timepicker.regional['fr'] = {
	currentText: 'Maintenant',
	closeText: 'Fermer',
	timeFormat: 'HH:mm',
	timeSuffix: '',
	timeOnlyTitle: 'Sélectionner une heure',
	timeText: '',
	hourText: 'Heure :',
	minuteText: '',
	secondText: '',
	millisecText: '',
	timezoneText: 'Time Zone',
	isRTL: false
};
$.timepicker.setDefaults($.timepicker.regional['fr']);

$.timepicker.setDefaults({
	controlType: 'select',
	showTime: false,
	millisecMax: 0,
	timezoneList: []
});

/* datepicker widget does not use the widget factory, so to override it, we use the old school way. */
/* We keep a reference to the original function _attachHandlers. */
var __attachHandlers = $.datepicker._attachHandlers;

/* Override of the function _attachHandlers. */
$.datepicker._attachHandlers = function(inst) {
	var dialog = $(inst.dpDiv);
	var header = dialog.find(".ui-datepicker-header");

	/* Adding previous year button. */
	var prevYear = header.find(".ui-datepicker-prev-year");

	if (prevYear.length == 0) {
		prevYear = "<a class='ui-datepicker-prev ui-datepicker-prev-year ui-corner-all' data-handler='prevYear' data-event='click'" +
		" title='" + "&lt;&lt;" + "'><span>" + "&lt;&lt;" + "</span></a>";
		header.prepend(prevYear);
	}

	/* Adding previous next button. */
	var nextYear = header.find(".ui-datepicker-next-year");

	if (nextYear.length == 0) {
		nextYear = "<a class='ui-datepicker-next ui-datepicker-next-year ui-corner-all' data-handler='nextYear' data-event='click'" +
		" title='" + "&gt;&gt;" + "'><span>" + "&gt;&gt;" + "</span></a>";
		header.append(nextYear);
	}

	/* Calling the original function to handle events. */
	__attachHandlers.apply(this, arguments);

	var id = "#" + inst.id.replace( /\\\\/g, "\\" );

	/* Adding handlers to the new buttons. */
	dialog.find("[data-handler]").map(function () {
		var handler = {
			prevYear: function () {
				$.datepicker._adjustDate(id, -12, "M");
			},
			nextYear: function () {
				$.datepicker._adjustDate(id, 12, "M");
			}
		};
		$(this).bind(this.getAttribute("data-event"), handler[this.getAttribute("data-handler")]);
	});

	/* Adding draggable capabilities. */
	dialog.draggable({
		cursor: "move",
		handle: ".ui-datepicker-title",
		zIndex: 1000
	});
};
