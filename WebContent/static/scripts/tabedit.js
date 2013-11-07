tabedit = {};

tabedit.CREATE_ACTION_NAME = "create";
tabedit.MODIFY_ACTION_NAME = "modify";
tabedit.CANCEL_ACTION_NAME = "cancel";
tabedit.ROWNUM_FOR_CREATION = "-1";

tabedit.busy = false;


/* Access to the next row to be edited */

tabedit.nextTable = null;
tabedit.nextRow = null;
tabedit.nextRowFocusIndex = null;

/** Returns the next row : one previously set up, or the last row of the table */
tabedit.getNextRow = function() {
	if (tabedit.nextRow === undefined || tabedit.nextRow == null) {
		return tabedit.nextTable.getLastRow().get(0);
	} else {
		return tabedit.nextRow;
	}
};

/** Returns the index to which the focus should be set when starting edition on the next row */
tabedit.getNextRowIndex = function() {
	return tabedit.nextRowFocusIndex;
};

/** Sets a table and a row for the next edition. */
tabedit.setNext = function(nextTable, nextRow, nextRowFocusIndex) {
	tabedit.nextTable = nextTable;
	tabedit.nextRow = nextRow;
	if (nextRowFocusIndex === undefined) {
		tabedit.nextRowFocusIndex = 0;
	} else {
		tabedit.nextRowFocusIndex = nextRowFocusIndex;
	}
};

/** Sets a table to be edited next on its last row, corresponding to no currently existing row. */
tabedit.setNextAsNew = function(nextTable) {
	tabedit.nextTable = nextTable;
	tabedit.nextRow = undefined;
	tabedit.nextRowFocusIndex = 0;
};

/** Clears the next row */
tabedit.clearNext = function() {
	tabedit.nextTable = null;
	tabedit.nextRow = null;
	tabedit.nextRowFocusIndex = null;
};



/* Access to the currently edited row */

tabedit.workingTable = null;
tabedit.workingRow = null;

/** Returns true if there is a row being edited */
tabedit.isWorking = function() {
	return (tabedit.workingTable != null && tabedit.workingRow != null);
};

/** Returns the row being edited */
tabedit.getWorkingRow = function() {
	return tabedit.workingRow;
};

/** Sets the row being edited */
tabedit.setWorking = function(newTable, newRow) {
	tabedit.workingTable = newTable;
	tabedit.workingRow = newRow;
	tabedit.workingTable.setupEditionButtons();
};

/** Clears the row being edited : no more row is being edited until we call tabedit.setWorking again. */
tabedit.clearWorking = function() {
	if (tabedit.workingTable != null) {
		tabedit.workingTable.removeEditionButtons();
	}
	tabedit.workingRow = null;
	tabedit.workingTable = null;
};


/**
 * Commodity function : moves the next row to be the current row, then clears the next row. Returns the focus index to
 * set.
 */
tabedit.moveNextToWorking = function() {
	var index = tabedit.nextRowFocusIndex;
	tabedit.setWorking(tabedit.nextTable, tabedit.getNextRow());
	tabedit.clearNext();
	return index;
};



/* The tables */

/** All referenced editable tables on this page */
tabedit.table = {};

tabedit.registerEditableTable = function(myTableId, myFormId) {
	cgi.debug("Tabedit register :", myTableId, myFormId);

	if (!$(toJqId(myTableId)).is("*") || !$(toJqId(myFormId)).is("*")) {
		/* no hidden form or table, probably an error on the page */
		cgi.debug("No editable table present");
		return;
	}

	tabedit.table[myTableId] = {

		tableId : myTableId,
		formId : myFormId,
		$tableId : toJqId(myTableId),
		$formId : toJqId(myFormId),

		getCurrentAction : function() {
			return $(this.$formId).find(".tabedit-current-action").val();
		},

		getNextAction : function() {
			return $(this.$formId).find(".tabedit-next-action").val();
		},

		setNextAction : function(action) {
			$(this.$formId).find(".tabedit-next-action").val(action);
		},

		getLastRow : function() {
			return $(this.$tableId).children("tbody").children("tr").last();
		},

		/**
		 * Prepare a new row for edition. The edition will start when the server has loaded the corresponding entity.
		 * @param focusFieldIndex the index (0-based) of the field to which focus should be given. Default is 0 (first
		 *            field).
		 */
		prepareNewRow : function($row, focusFieldIndex) {
			/* fill the next rownum we need on the hidden form */
			cgi.debug("Next rownum is", tabedit.getRownum($row));
			$(this.$formId).find(".tabedit-next-rownum").val(tabedit.getRownum($row));

			/* the next row will be set editable when the server response arrives */
			tabedit.setNext(this, $row.get(0), focusFieldIndex);

			/* find the correct next action : new line or existing line ? */
			var action = $(this.$formId).find(".tabedit-next-rownum").val() == tabedit.ROWNUM_FOR_CREATION ? tabedit.CREATE_ACTION_NAME : tabedit.MODIFY_ACTION_NAME;
			cgi.debug("Next action is", action);
			this.setNextAction(action);
		},

		/**
		 * Prepare a new row for creation. The edition will start when the server has loaded the corresponding entity.
		 */
		prepareNewCreationRow : function() {
			/* fill the next rownum we need on the hidden form */
			cgi.debug("Next rownum is", tabedit.ROWNUM_FOR_CREATION);
			$(this.$formId).find(".tabedit-next-rownum").val(tabedit.ROWNUM_FOR_CREATION);

			/* the new row will be set editable when the server response arrives */
			tabedit.setNextAsNew(this);

			/* find the correct next action : new line or existing line ? */
			cgi.debug("Next action is", tabedit.CREATE_ACTION_NAME);
			this.setNextAction(tabedit.CREATE_ACTION_NAME);
		},

		/**
		 * Method to call on server response : correct entity loaded and previous row (if any) was correctly exited.
		 * Finish setting up the row - if it's still here.
		 */
		onPrepareSuccess : function() {
			/* next row is now the current row */
			var index = tabedit.moveNextToWorking();

			/* set the row editable */
			this.setEditable($(tabedit.getWorkingRow()), index);
		},

		/** Validate the row being edited */
		validateCurrentRow : function() {
			var $row = $(tabedit.getWorkingRow());

			/* stop any further edition on the row */
			tabedit.disableRow($row);

			/* copy each field on the form */
			var writes = $(this.$formId).find(".tabedit-hidden-form-data").find(".tabedit-editable-write");
			$row.find(".tabedit-editable-write").each(function(index) {
				var $rowDiv = $(this);
				var $hiddenFormDiv = writes.eq(index);
				$hiddenFormDiv.children().children().val($rowDiv.children().children().val());
			});

			/* copy the current rownum */
			$(this.$formId).find(".tabedit-current-rownum").val(tabedit.getRownum($row));
		},

		/** On server response : finish closing the row if okay. Returns true if everything is alright. */
		onSaveSuccess : function() {
			/* the row is valid if and only if there are no error messages */
			var success = $(this.$formId).find(".tabedit-success").val() == "true";

			if (success) {
				/* the row that was sent is not editable any more, as it got refreshed from JSF 2 */
				/* we only need to mark there are no more editable row */
				tabedit.clearWorking();
				return true;
			} else {
				/* restore editable row, table might have been reloaded */
				var currentRownum = $(this.$formId).find(".tabedit-current-rownum").val();
				var $input = $(this.$tableId).find("input.rownum[value=" + currentRownum + "]");
				var $row = $input.closest("tr");
				tabedit.setWorking(this, $row.get(0));

				/* find the error field, to which we will give focus */
				var $data = $(this.$formId).find(".tabedit-hidden-form-data");
				var fields = $data.find(".tabedit-editable-write");
				var index = $.inArray(fields.filter(".error").get(0), fields);

				/* Invalid values on the old row. Set it editable again. */
				this.setEditable($row, index);

				return false;
			}
		},

		/** Send the form to the server and let it work its magic. */
		sendFormToServer : function(event) {
			var currentAction = this.getCurrentAction();
			var nextAction = this.getNextAction();

			cgi.debug(this.formId, "sendFormToServer with actions current=" + currentAction + " and next=" + nextAction);

			var options = {};

			/* send the whole hidden form */
			options.execute = this.formId;

			/* Always need to render the hidden form, even if no action (an entity may have been loaded in preparation) */
			options.render = this.formId + " ";

			/* Rest of the render (for the current action) : whole table for creation, only the row for edition. */
			if (currentAction == tabedit.CREATE_ACTION_NAME) {
				options.render += this.tableId;
			} else if (currentAction == tabedit.MODIFY_ACTION_NAME) {
				options.render += tabedit.getRenderingForRow($(tabedit.getWorkingRow()));
			}

			/* the "this" object within options.onSuccess is the HTML document, not the current object */
			var that = this;

			/* prepare the success */
			options.onSuccess = function() {
				if (that.onSaveSuccess() && nextAction != "") {
					that.onPrepareSuccess();
				}
				tabedit.busy = false;
			};

			options.$button = this.get$SubmissionButton();
			tabedit.ajaxCall(options);
		},

		/** Discard the current row without any new row being set up. */
		discardCurrentRow : function() {
			if (tabedit.getWorkingRow() == null) {
				return;
			}

			var $row = $(tabedit.getWorkingRow());
			var $write = $row.find(".tabedit-editable-write");
			var $read = $row.find(".tabedit-editable-read");
			
			$write.remove();
			$read.show();

			/* set the proper style on each cell */
			$read.closest("td").removeClass("tabedit-active");

			tabedit.clearWorking();

			/* Empty current action */
			$(this.$formId).find(".tabedit-current-action").val("");

			/* Empty form data */
			$(this.$formId).find(".tabedit-hidden-form-data").children().remove();

			/* Clean dirty state */
			cleanDirty();
			cgi.debug("Row discarded");
		},

		/**
		 * The argument row is set up to be editable.
		 * @param focusIndex is the 0-based index of the field to set editable. Default is 0.
		 */
		setEditable : function($row, focusIndex) {
			focusIndex = focusIndex === undefined ? 0 : focusIndex;
			cgi.debug("Setting editable :", $row.get(0));

			/* preparing input div and fields for the current line */
			var $writeDivs = $(this.$formId).find(".tabedit-hidden-form-data").find(".tabedit-editable-write").clone();
			cgi.debug("New fields :", $writeDivs);

			if ($writeDivs.length == 0) {
				throw "Entity was not properly loaded !";
			}

			/* iterating the read divs that need to be replaced */
			var $readDivs = $row.find(".tabedit-editable-read");
			$readDivs.each(function(index) {
				var $read = $readDivs.eq(index);
				var $write = $writeDivs.eq(index);

				cgi.debug("Setting editable field :", $write.get(0));

				/* change the id to differentiate it from the prototype */
				var oldId = $write.children().children("input").first().attr("id");
				$write.children().children("input").first().attr("id", oldId + "_current");
				
				/* insert it to the right place */
				$write.insertAfter($read);
				
				/* we need to show it now, or we have weird mistakes on calculating sizes */
				$read.hide();
				$write.show();
				
				var $td = $write.closest("td");
				
				/* set the proper style on each cell */
				$td.addClass("tabedit-active");
				var availableWidth  = $td.innerWidth()  - parseInt($td.css('padding-left')) - parseInt($td.css('padding-right'));
				
				var $input;
				
				/* Special treatment for jQuery UI DatePicker */
				if ($write.has("input.date-picker").length || $write.has("input.time-picker").length) {
					cgi.debug("Special case for datepicker");
					
					$input = $write.find("input");
					var $img = $write.find("img.ui-datepicker-trigger");

					/*
					 * Looks like : <input type="text" id="..." class="datePicker hasDatepicker" * "
					 * onchange="completeDate(this);markAsDirty()" /> <img class="ui-datepicker-trigger" src="..." />
					 */

					/* we're going to create a new one */
					$input.removeClass("hasDatepicker");
					$img.remove();

					var options = {
						altFormat : "dd/mm/yy",
						buttonImage : $img.attr("src"),
						buttonImageOnly : true,
						dateFormat : "dd/mm/yy",
						showOn : "button",
						showButtonPanel : true,
						showWeek : true
					};

					if ($input.is(".time-picker")) {
						options.controlType = 'select';
						options.timeFormat = "HH:mm";
						options.showTime = false;
						options.useLocalTimezone = true;
						$input.datetimepicker(options);
					} else {
						$input.datepicker(options);
					}

					availableWidth = availableWidth - $write.find("img.ui-datepicker-trigger").outerWidth(true);

				} else {
					$input = $write.children().children();
				}				
				
				/* Correct dimensions  */
				$input.width(   availableWidth  - parseInt($input.css('padding-left')) - parseInt($input.css('padding-right'))  - parseInt($input.css('border-left-width')) - parseInt($input.css('border-right-width'))  );
								
				/* apply the table alignement to the input field */
				$input.css("text-align", $read.css("text-align"));
			});

			/* be ready to use shortkeys on the newly set input fields */
			this.registerShortkeys($writeDivs);

			/* select the target field */
			$writeDivs.eq(focusIndex).children().children().focus().select();
		},

		/**
		 * The submission button is not always the element with the class. At some part during the loading of the page,
		 * it gets wrapped by a <a> tag with his class.
		 */
		get$SubmissionButton : function() {
			var $ajaxClassed = $(this.$formId).find(".tabedit-hidden-button-ajax");
			if ($ajaxClassed.is("input"))
				return $ajaxClassed;
			else
				return $ajaxClassed.find("input").first();
		},

		/**
		 * On click on the table : first cancel the edited row, then prepare for next row if necessary. Exception if
		 * clicking on the currently editable line.
		 */
		onClick : function(event) {

			/*
			 * if there is a row being edited, and we are clicking on it : do nothing special and go on (maybe we
			 * clicked on a calendar...)
			 */
			if (tabedit.isWorking() && (tabedit.getWorkingRow() === event.target || $.contains(tabedit.getWorkingRow(), event.target))) {
				cgi.debug(this.tableId + '#onclick : click on the same line, ignoring click');
				return;
			}

			/* Do not process inputs while we are working */
			if (tabedit.busy) {
				cgi.debug(this.tableId + '#onclick : busy, ignoring click');
				return;
			} else {
				tabedit.busy = true;
			}

			var that = this;

			/* prepare the next row, will be launched if the user is OK to abandon its current changes */
			var goOn = function() {
				tabedit.busy = true;

				if (tabedit.isWorking() != null) {
					that.discardCurrentRow();
				}

				var $source = $(event.target);
				var $clickedRow = $source.closest("tr");
				if ($clickedRow.children().is(".tabedit-editable-cell")) {
					/* if an editable row of this table was clicked */
					cgi.debug(that.tableId + '#onclick : editable row was targeted : ', $clickedRow.get(0));
					var clickedDivIndex = tabedit.getFieldIndexInRow($source);
					that.prepareNewRow($clickedRow, clickedDivIndex);

					cgi.debug(that.tableId + '#onclick : sending form to server');
					that.sendFormToServer(event);

				} else {
					cgi.debug(this.tableId + '#onclick : no click event for', event.target);
					tabedit.busy = false;
				}
			};

			/* stop being busy and give focus, for the case where we are not leaving after all */
			var stopBeingBusy = function() {
				tabedit.busy = false;
				$(tabedit.getWorkingRow()).find(".tabedit-editable-write").find("input").first().focus();
			};

			/* there is potentially an editable row opened, with modifications */
			cgi.withCheckDirty(goOn, stopBeingBusy);
		},

		/** Capture key event to set up shortkeys on the table */
		registerShortkeys : function($writeDivs) {
			cgi.debug("Capturing shorkey events for :", $writeDivs);

			/* the "this" object within keydown is the HTML input field, not the current object */
			var that = this;

			$writeDivs.children().children().keydown(function(event) {
				if (!tabedit.busy) {
					var flag;
					if (event.keyCode == KEYCODE_ESCAPE) {
						flag = that.handleShortkeyEscape();
					} else if (event.keyCode == KEYCODE_TAB) {
						flag = that.handleShortkeyTab(event.target, event.shiftKey);
					} else if (event.keyCode == KEYCODE_ENTER) {
						flag = that.handleShortkeyEnter(event.target);
					} else if (event.keyCode == KEYCODE_UP_ARROW) {
						flag = that.handleShortkeyUpDown(event.target, true);
					} else if (event.keyCode == KEYCODE_DOWN_ARROW) {
						flag = that.handleShortkeyUpDown(event.target, false);
					} else if (event.keyCode == KEYCODE_LEFT_ARROW) {
						flag = that.handleShortkeyLeftRight(event.target, true);
					} else if (event.keyCode == KEYCODE_RIGHT_ARROW) {
						flag = that.handleShortkeyLeftRight(event.target, false);
					} else {
						/* other keys, work normally */
						flag = true;
					}

					if (!flag) {
						event.preventDefault();
					}
					return flag;

				} else {
					cgi.debug("Ignoring shortkeys at the moment");
				}
			});
		},

		/** React to the tab key being pressed. */
		handleShortkeyTab : function(target, shift) {
			cgi.debug("Pressed tab !");

			var $cell = $(target).closest("td");

			var $cellsToLook = shift ? $cell.prevAll() : $cell.nextAll();
			var $nextCell = $cellsToLook.has(".tabedit-editable-write").first();

			if ($nextCell.length == 0) {
				var $siblings = $cell.siblings().has(".tabedit-editable-write");
				$nextCell = shift ? $siblings.last() : $siblings.first();
			}

			var $nextInput = $nextCell.find(".tabedit-editable-write").children().children();
			$nextInput.focus().select();

			return false;
		},

		/** React to the enter key being pressed. */
		handleShortkeyEnter : function(target) {
			cgi.debug("Pressed enter !");

			tabedit.busy = true;

			this.validateCurrentRow();

			var $row = $(target).closest("tr");
			var $nextRow = $row.nextAll().has(".tabedit-editable-read").first();
			if ($nextRow.length > 0) {
				this.prepareNewRow($nextRow, 0);
			} else {
				/* creation mode */
				this.prepareNewCreationRow();
			}

			this.sendFormToServer();

			return false;
		},

		/** React to the escape key being pressed. */
		handleShortkeyEscape : function() {
			cgi.debug("Pressed escape !");
			this.discardCurrentRow();
			return false;
		},

		/** React to the up or down key being pressed. */
		handleShortkeyUpDown : function(target, up) {
			cgi.debug("Pressed", up ? "up !" : "down !");
			var $source = $(target);

			if ($source.is("select")) {
				return true;
			}

			var $row = $source.closest("tr");
			var $possibleTargetRow = up ? $row.prev() : $row.next();

			var $targetRow = $possibleTargetRow.has(".tabedit-content");
			if ($targetRow.length == 0) {
				/* first or last row, do nothing special */
				return false;
			}

			var position = tabedit.getFieldIndexInRow($source);
			var $targetCell = $targetRow.children().has(".tabedit-content").eq(position);

			var that = this;

			/* Preparing the ondirty check... */
			var abandonChanges = function() {
				cgi.debug("Dirty and abandoning changes");
				that.discardCurrentRow();
				$targetCell.click();
			};

			var stopBeingBusy = function() {
				tabedit.busy = false;
				$(tabedit.getWorkingRow()).find(".tabedit-editable-write").find("input").first().focus();
			};

			tabedit.busy = true;
			cgi.withCheckDirty(abandonChanges, stopBeingBusy);

			return false;
		},

		/** React to the left or right key being pressed. */
		handleShortkeyLeftRight : function(target, left) {
			cgi.debug("Pressed", left ? "left !" : "right !");

			/*
			 * target.selectionStart : position beginning the selection, before moving target.selectionEnd : position
			 * ending the selection, before moving
			 */

			if (target.selectionStart === undefined) {
				/* not a text field */
				return this.handleShortkeyTab(target, left);
			}

			if (target.selectionEnd > target.selectionStart) {
				return true;
			}

			var positionOfCursor = target.selectionStart;
			var textLength = $(target).val().length;

			if (left) {
				if (positionOfCursor == 0 || positionOfCursor === undefined || positionOfCursor === NaN) {
					return this.handleShortkeyTab(target, true);
				}
			} else {
				if (positionOfCursor == textLength || positionOfCursor === undefined || positionOfCursor === NaN) {
					return this.handleShortkeyTab(target, false);
				}
			}

			return true;
		},

		setupEditionButtons : function() {
			cgi.debug("Set up edition buttons");
			var that = this;

			/* if a removal is waiting */
			if (this.removeEditionButtonsTimer != null) {
				window.clearTimeout(this.removeEditionButtonsTimer);
				this.removeEditionButtonsTimer = null;
			}

			/* careful not to double them */
			if ($(this.$tableId).closest(".table_container").find(".actions").find(".tabedit-action-buttons").length > 0) {
				return;
			}

			var tabeditActionsSpan = $(this.$formId).find(".tabedit-action-buttons").clone();
			tabeditActionsSpan.find(".tabedit-action-validate-button").click(function() {
				tabedit.busy = true;
				that.validateCurrentRow();
				that.sendFormToServer();
			});
			tabeditActionsSpan.find(".tabedit-action-cancel-button").click(function() {
				that.discardCurrentRow();
			});

			var actionsSpan = $(this.$tableId).closest(".table_container").find(".allActionButtons");

			actionsSpan.after(tabeditActionsSpan);
			actionsSpan.hide();
		},

		/** Removal of buttons is delayed to avoid the very aesthetically displeasing blinking effect. */
		removeEditionButtons : function() {
			if (this.removeEditionButtonsTimer != null) {
				/* already a timer, delete the old one */
				window.clearTimeout(this.removeEditionButtonsTimer);
				this.removeEditionButtonsTimer = null;
			}

			var that = this;

			this.removeEditionButtonsTimer = window.setTimeout(function() {
				cgi.debug("Remove edition buttons");
				var tabeditActionsSpan = $(that.$tableId).closest(".table_container").find(".actions").find(".tabedit-action-buttons");
				tabeditActionsSpan.remove();
				var actionsSpan = $(that.$tableId).closest(".table_container").find(".allActionButtons");
				actionsSpan.show();
			}, 100);

		}
	};

	/*
	 * Register a click event listener to manage the editable table. Listener is called during the bubble phase on the
	 * div wrapping the table. The listener is not directly on the table, as this can be reloaded with Ajax. The inner
	 * function trick is here to ensure the 'this' object within onClick refers to the table object, not the HTML
	 * element (as is the case if onClick is directly used as the argument of $.click()).
	 */
	var $clickTarget = $(toJqId(myTableId)).closest("div");
	$clickTarget.click(function(event) {
		// $(document).click(function(event) {
		tabedit.table[myTableId].onClick(event);
		return true;
	});

	cgi.debug("Added onclick event on :", $clickTarget);
};

/** Disable all fields on an editable row */
tabedit.disableRow = function($row) {
	/* disable the fields of this currently editable row */
	$row.find(".tabedit-editable-write").children().children().prop('disabled', true);
};

/**
 * Call ajax for an event. Options specify everything that may change. No parameter is mandatory. :
 * <ul>
 * <li><b>options.execute</b> is the space-separated list of ids that we need to execute in addition to the form
 * actions</li>
 * <li><b>options.render</b> is what to render in addition to the usual : hidden form, messages and dirty state</li>
 * <li><b>options.onSuccess</b> is the function to call when the ajax calls end with success</li>
 * <li><b>options.immediate</b> is <code>true</code> if the call must bypasse the normal JSF lifecycle (see the
 * definition of immediate on h:commandButton)</li>
 * </ul>
 */
tabedit.ajaxCall = function(options, event) {

	/* force a click event, necessary for JSF2 */
	if (event === undefined) {
		options.$button.one("click", function(event) {
			event.preventDefault();
			tabedit.ajaxCall(options, event);
			return false;
		});
		options.$button.click();
		return;
	}

	var executing = "mainForm:dirty ";
	if (options.execute !== undefined)
		executing += options.execute;
	cgi.debug("Executing :", executing);

	var rendering = "mainForm:dirty mainForm:messages ";
	if (options.render !== undefined)
		rendering += options.render;
	cgi.debug("Rendering :", rendering);

	/* prepare the success */
	var indicator = prepareProgressIndicator();
	var handleAjaxEvent = function(data) {
		if (data.status == "success") {
			if (options.onSuccess != undefined) {
				options.onSuccess();
			}
			indicator.close();
		}
	};

	var ajaxOptions = {
		execute : executing,
		render : rendering,
		onevent : handleAjaxEvent,
		onerror : logAjaxError
	};

	jsf.ajax.request(options.$button.attr("id"), event, ajaxOptions);
	indicator.open();
};

/**
 * Return the index of the field orresponding to $source in the row. If $source is not on an editable content, returns
 * 0. Works wether the row is in edit or read mode.
 * @param $source is the field on something else in the same td
 */
tabedit.getFieldIndexInRow = function($source) {
	var index = 0;
	var $sourceCell = $source.closest("td").has(".tabedit-content");
	if ($sourceCell.length > 0) {
		index = $.inArray($sourceCell.get(0), $sourceCell.closest("tr").find("td").has(".tabedit-content"));
	}
	return index;
};

/** Space-separated list of the ids to render for this row */
tabedit.getRenderingForRow = function($row) {
	var rendering = "";
	$row.find(".tabedit-editable-read").parent().each(function() {
		rendering = rendering + this.id + " ";
	});
	return rendering;
};

/** Returns the rownum for a row */
tabedit.getRownum = function($row) {
	return $row.find("input.rownum").val().trim();
};
