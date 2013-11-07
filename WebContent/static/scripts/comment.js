
function commentPage() {
	// Open popup
	$("#comment-popup").dialog({
		width : 800,
		height : 400,
		modal : true,
		appendTo: "#mainForm",
		open : function(event, ui) {
			$('input[id$="comment-comment"]').focus();
		},
		close : function(event, ui) {
			cancelComment();
		}
	});
}

/**
 * Clean comment form
 */
function cancelComment() {
	$('textarea[id$="comment-comment"]').val('');
}

function saveComment(event) {
	if (event.status == 'complete') {
		cancelComment();
		$('#comment-popup').dialog('close');
	}
}

