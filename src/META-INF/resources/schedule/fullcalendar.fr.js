$.fullCalendar.setDefaults({
	axisFormat: 'HH(:mm)',
	buttonText: {
		today:	'Aujourd\'hui',
		month:	'Mois',
		week:	'Semaine',
		day:	'Jour'
	},
	columnFormat: {
		month:	'dddd',
		week:	'dddd dd',
		day:	'dddd'
	},
	dayNames: ['Dimanche', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi'],
	dayNamesShort: ['Dim', 'Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam'],
	firstDay: 1,
	firstHour: 9,
	monthNames: ['Janvier', 'F&eacute;vrier', 'Mars', 'Avril', 'Mai', 'Juin', 'Juillet', 'Ao&ucirc;t', 'Septembre', 'Octobre', 'Novembre', 'D&eacute;cembre'],
	monthNamesShort: ['Jan', 'F&eacute;v', 'Mar', 'Avr', 'Mai', 'Juin', 'Juil', 'Aou', 'Sep', 'Oct', 'Nov', 'D&eacute;c'],
	timeFormat: 'H:mm',
	titleFormat: {
		month:	'MMMM yyyy',
		week:	'd[ MMMM][ yyyy]{ \'-\' d MMMM yyyy}',
		day:	'd MMMM yyyy'
	}
});
