// this object contains the functions and variables used for managing model/manufacturer aliases (AKAs)
var akaUtil = (function ($) {

	var public = {}
	var private = {}

	private.lastAkaId = -1

	/**
	 * To add AKA text field to add AKA for model and manufacturer (common method for both)
	 * @param forWhom, 'model' or 'manufacturer' to specify which type of AKA this is
	 */
	public.addAka = function (forWhom) {
		// TODO : rmacfarlane 2/9/2017 : this is pretty messy and should probable be done in a more elegant way
		var akaId = private.lastAkaId--
		var spanId = "errSpan_" + akaId
		var textHtml = $("#akaTemplateDiv").html().replace(/errSpan/g, "errSpan_" + akaId).replace(/akaId/g, "akaId_" + akaId)
		$("#addAkaTableId").append("<tr id='akaRowId_" + akaId + "' js-is-unique='unknown'><td nowrap='nowrap'>" + textHtml +
			"<a href=\"javascript:akaUtil.deleteAkaRow('akaRowId_" + akaId + "',false,'" + forWhom + "')\"><span class='clear_filter'><u>X</u></span></a>" +
			"<br><div style='display:none' class='errors' id='errSpan_" + akaId + "'></div>" +
			"</td></tr>")
	}

	/**
	 * Used to delete text field from DOM and if it was persistent the maintain id to send at controller
	 * @param id : id of tr to remove .
	 * @param save : a flag to make sure whether to maintain deleted id .
	 * @param forWhom, 'model' or 'manufacturer' to specify which type of AKA this is
	 */
	public.deleteAkaRow = function (id, save, forWhom) {
		// remove this AKA row from the DOM
		$("#" + id).remove()
		if (save) {
			var deletedId = id.split("_")[1]
			$("#deletedAka").val() ? $("#deletedAka").val($("#deletedAka").val() + "," + deletedId) : $("#deletedAka").val(deletedId)
		}

		// revalidate all the AKAs as some may have only had errors due to being a duplicate of the now deleted AKA
		public.validateAllAka(forWhom)
	}

	/**
	 * Called when an aka field was changed in order to mark it for revalidation
	 * @param akaInput, the input element for the AKA that was changed
	 * @param forWhom, 'model' or 'manufacturer' to specify which type of AKA this is
	 */
	public.handleAkaChange = function (akaInput, forWhom) {
		// mark this AKA for server-side validation
		var akaId = $(akaInput).attr('id').replace('aka', 'akaRow')
		$('tr#' + akaId).attr('js-is-unique', 'unknown')

		// revalidate all the AKAs
		public.validateAllAka(forWhom)
	}

	/**
	 * Called when a property of the parent object changes that will affect validation (either name or manufacturer for models)
	 * @param forWhom, 'model' or 'manufacturer' to specify which type of AKAs will need to be checked
	 */
	public.handleParentPropChange = function (forWhom) {
		
		// enable/disable the "Add AKA" button based on if the model has a name
		var parentName = $('#modelNameId').val()
		public.enableAddAkaButton(parentName != "")
		
		// mark all AKAs for server-side validation (the result may be different with the new parent properties)
		$('#addAkaTableId > tr').attr('js-is-unique', 'unknown')

		// revalidate all the AKAs
		public.validateAllAka(forWhom)
	}

	/**
	 * Called to enable or disable the "Add AKA" button
	 * @param toEnable, whether we want to enable or disable the button
	 */
	public.enableAddAkaButton = function (toEnable) {
		if (toEnable)
			$('#addAkaId').removeClass('addAkaDisabled')
		else
			$('#addAkaId').addClass('addAkaDisabled')
		
	}
	
	/**
	 * Validates all the AKAs for a model
	 * @param forWhom, 'model' or 'manufacturer' to specify which type of AKA this is
	 */
	public.validateAllAka = function (forWhom) {
		// get the parent's name and id, along with the manufacturer's id if this is a model AKA
		var parentName, parentId, manufacturerId;
		if (forWhom === 'model') {
			parentName = $('#modelNameId').val();
			parentId = $('input#modelId').val();
			manufacturerId = $('#manufacturerId').val();
		} else {
			parentName = $('#name').val();
			parentId = $('input#manufacturerId').val();
		}

		// iterate through all the AKAs, performing the necessary validation on each one
		var akaList = [];
		$("#addAkaTableId > tr").each(function (i, row) {
			var akaRow = $(row);
			var akaName = akaRow.find('.akaValidate').val();
			var akaErrorDivId = akaRow.find('.errors').attr('id');
			var duplicateOf = 'none';

			// check if the AKA matches the parent's name
			if (tdsCommon.compareStringsIgnoreCase(akaName, parentName)) {
				duplicateOf = 'parent'
        		// check if the AKA matches another AKA on the list
      		} else if (tdsCommon.arrayContainsStringIgnoreCase(akaList, akaName)) {
      	  		duplicateOf = 'local'
        		// if this AKA is new, check it's validity against other models on the server
      		} else if (akaRow.attr('js-is-unique') === 'unknown') {
				duplicateOf = private.validateAkaOnServer(forWhom, akaRow, {
					'alias': akaName,
					'id': parentId,
					'manufacturerId': manufacturerId,
					'parentName': parentName
				});
        		// check if this AKA has previously been marked as invalid
			} else if (akaRow.attr('js-is-unique') === 'false') {
				duplicateOf = 'other';
				// otherwise this AKA is not a duplicate
      		}
			akaList.push(akaName);
			public.setAkaErrorStatus(akaErrorDivId, akaName, duplicateOf, forWhom);
		});

		// if there are no AKAs left, enable the save button
		if (akaList.size() === 0) {
            public.handleAkaForSaveButton(forWhom);
        }
	};

	/**
	 * Checks to see if an AKA exists on the server
	 * @param forWhom, to determine which controller we need to send the requst.
	 * @param akaRow, the row element of the AKA being checked
	 * @param params, the params to use for the AJAX call
	 * @return 'none' if the AKA is valid, 'other' otherwise
	 */
	private.validateAkaOnServer = function (forWhom, akaRow, params) {
		var duplicateOf = 'none';
		$.ajax({
			url: contextPath + '/' + forWhom + '/validateAliasForForm',
			data: params,
			async: false,
			complete: function (e) {
				if (e.responseText === 'valid') {
					duplicateOf = 'none';
					akaRow.attr('js-is-unique', 'true')
				} else if (e.responseText === 'invalid') {
					duplicateOf = 'other';
					akaRow.attr('js-is-unique', 'false')
				} else if (status === 'error') {
					alert('An unexpected error occurred while validating AKA');
				}
			}
		});
		return duplicateOf;
	};

	/**
	 * handles showing/hiding the error text for when an AKA is invalid
	 * @param spanId the ID of the error span
	 * @param akaMessage if present, the error will be displayed with this message, if null, there will be no error
	 */
	public.setAkaErrorStatus = function (errorId, akaName, duplicateOf, forWhom) {
		var akaMessage = ''
		if (duplicateOf == 'parent')
			akaMessage = 'AKA should be different from the ' + forWhom + ' name'
		else if (duplicateOf == 'local')
			akaMessage = 'AKA ' + akaName + ' already entered'
		else if (duplicateOf == 'other')
			akaMessage = 'AKA ' + akaName + ' already exists'

		var errorDiv = $('#' + errorId)
		if (akaMessage) {
			errorDiv.html(akaMessage)
			errorDiv.css('display', 'block')
			errorDiv.addClass('hasErrors')
		} else {
			errorDiv.html("")
			errorDiv.css('display', 'none')
			errorDiv.removeClass('hasErrors')
		}

		// now that we know if this AKA has an error check if the save button should be disabled
		public.handleAkaForSaveButton(forWhom)
	}

	/**
	 * handle disabling/enabling the save button based on the presence of errors
	 */
	public.handleAkaForSaveButton = function (forWhom) {
		var hasErrors = $('#addAkaTableId div.hasErrors')
		var saveButton = $('#saveModelId')
		if (forWhom == 'manufacturer')
			saveButton = $('#saveManufacturerId')
		if (hasErrors.size() > 0)
			// disable the button
			saveButton.attr('disabled', 'disabled').addClass('disableButton')
		else
			// enable the button
			saveButton.attr('disabled', null).removeClass('disableButton')
	}

	// return the public functions and variables to make them accessible
	return public
})(jQuery);

/**
 * convert values from Amps to Watts OR Watts to Amps
 * @param value
 * @param name
 */
function convertPowerType(value, whom) {
	if (value == "Watts") {

		/* =========
			Power Max
		 	========= */
		var powerNameplate = ($('#powerNameplateIdH').val() && $('#powerNameplateIdH').val() != '0') ? $('#powerNameplateIdH').val() * 120 : ($('#powerNameplate' + whom + 'Id').val() * 120)
        $('#powerNameplate' + whom + 'Id').val(powerNameplate.toFixed(0));

		/*	===========
		 	Power Used
		 	========== */
        var powerUsed = ($('#powerUseIdH').val() && $('#powerUseIdH').val() != '0') ? $('#powerUseIdH').val() * 120 : ($('#powerUse' + whom + 'Id').val() * 120)
        $('#powerUse' + whom + 'Id').val(powerUsed.toFixed(0));

        /*	============
		 	Power Design
		 	============ */
		var powerDesign = ($('#powerDesignIdH').val() && $('#powerDesignIdH').val() != '0') ? $('#powerDesignIdH').val() * 120 : ($('#powerDesign' + whom + 'Id').val() * 120)
        $('#powerDesign' + whom + 'Id').val(powerDesign.toFixed(0));

        updateHiddenPowerValues(whom, powerNameplate, powerUsed, powerDesign);

	} else if (value == "Amps") {

		/* 	=========
        	Power Max
		 	========= */
		var powerNameplateA = ($('#powerNameplateIdH').val() && $('#powerNameplateIdH').val() != '0') ? $('#powerNameplateIdH').val() / 120 : ($('#powerNameplate' + whom + 'Id').val() / 120);
		$('#powerNameplate' + whom + 'Id').val(powerNameplateA.toFixed(1));

		/*	===========
		 	Power Used
		 	========== */
        var powerUseA = ($('#powerUseIdH').val() && $('#powerUseIdH').val() != '0') ? $('#powerUseIdH').val() / 120 : ($('#powerUse' + whom + 'Id').val() / 120);
        $('#powerUse' + whom + 'Id').val(powerUseA.toFixed(1));

		/*	============
			 Power Design
			 ============ */
		var powerDesignA = ($('#powerDesignIdH').val() && $('#powerDesignIdH').val() != '0') ? $('#powerDesignIdH').val() / 120 : ($('#powerDesign' + whom + 'Id').val() / 120);
		$('#powerDesign' + whom + 'Id').val(powerDesignA.toFixed(1));

		updateHiddenPowerValues(whom, powerNameplateA, powerUseA, powerDesignA);

	}
}

function updateHiddenPowerValues(whom, powerNameplate, powerUse, powerDesign){
    if (whom == 'Edit'){
        $('#powerNameplateIdH').val(powerNameplate)
        $('#powerUseIdH').val(powerUse);
        $("#powerDesignIdH").val(powerDesign);
    }
}



function createModelManuDetails(controllerName, forWhom) {
	jQuery.ajax({
		url: contextPath + '/' + controllerName + '/create',
		data: { 'forWhom': 'modelDialog' },
		type: 'POST',
		success: function (data) {

			$("#create" + forWhom + "View").html(data);
			$("#create" + forWhom + "View").dialog('option', 'width', 'auto')
			$("#create" + forWhom + "View").dialog('option', 'position', ['center', 'top']);
			$("#show" + forWhom + "View").dialog('close');
			$("#edit" + forWhom + "View").dialog('close');
			$("#create" + forWhom + "View").dialog('option', 'modal', 'true');
			$("#create" + forWhom + "View").dialog('open');
			$("#create" + forWhom + "View")
				.off('submit', "#manufacturerDialogForm")
				.on('submit', "#manufacturerDialogForm", function (e) {
					var obj = $("#create" + forWhom + "View #manufacturerDialogForm").serializeObject();
					jQuery.ajax(
						{
							url: contextPath + '/' + controllerName + '/save',
							data: obj,
							type: 'POST',
							success: function (response) {
								if (response.indexOf('<div class="errors">') !== -1)
									$("#create" + forWhom + "View").html(response);
								else {
									$("#create" + forWhom + "View").dialog('close');
									$('#messageId').html(response).show()
								}
							}
						});
					return false;
				});
		}
	});
	updateTitle(forWhom, "create", "Create")
}



function showOrEditModelManuDetails(controllerName, id, forWhom, view, name) {
	jQuery.ajax({
		url: contextPath + '/' + controllerName + '/' + view + '/',
		data: { 'id': id, 'redirectTo': 'modelDialog' },
		type: 'POST',
		success: function (data) {
			var dialogView = $("#" + view + "" + forWhom + "View");

			dialogView.html(data);
			dialogView.dialog({
				autoOpen: false,
				width: 'auto',
				position: { at: 'center top' },
				modal: true
			});
			$('.ui-dialog-titlebar-close').html('')
				.append('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>');
			$("#create" + forWhom + "View").dialog('close');
			if (view == 'edit')
				$("#show" + forWhom + "View").dialog('close');
			else if (view == 'show')
				$("#edit" + forWhom + "View").dialog('close');

			dialogView.dialog('open');
		}
	});
	updateTitle(forWhom, view, name)
}

function updateTitle(type, view, name) {
	$("#" + view + "" + type + "View").dialog("option", "title", name + " " + type);
}

function updateModel(forWhom, formName) {
	$("#" + formName).ajaxSubmit({
		success: function (data) {
			$("#editModelView").dialog('close')
			$("#showModelView").html(data)
			$("#showModelView").dialog({
				autoOpen: false,
				width: 'auto',
				position: { at: 'center top' },
				modal: true
			}).dialog('open');

		},
		error: function (request, errordata, errorObject) { alert(errorObject.toString()); },
	});
}

function updateManufacturer(forWhom) {
	jQuery.ajax({
		url: $("#editManufacturerFormId").attr('action'),
		data: $("#editManufacturerFormId").serialize(),
		type: 'POST',
		success: function (data) {
			if (data.errMsg) {
				alert(data.errMsg)
			} else {
				$("#edit" + forWhom + "View").html(data)
				$("#edit" + forWhom + "View").dialog("option", "title", "Show Manufacturer");
				$("#edit" + forWhom + "View").dialog('option', 'width', 'auto');
				$("#edit" + forWhom + "View").dialog('option', 'height', 'auto');
				$("#edit" + forWhom + "View").dialog('option', 'modal', 'true');
				$("#edit" + forWhom + "View").dialog('option', 'position', ['center', 'top']);
			}
		}
	});

}


function changePowerValue(whom) {
	var namePlatePower = $("#powerNameplate" + whom + "Id").val()
	var powerDesign = $("#powerDesign" + whom + "Id").val()
	var powerUse = $("#powerUse" + whom + "Id").val()

    var powerUseV = (parseInt(namePlatePower) * 0.33);
    $("#powerUse" + whom + "Id").val(powerUseV);

	var powerDesignV = (parseInt(namePlatePower) * 0.5);
	$("#powerDesign" + whom + "Id").val(powerDesignV);

    updateHiddenPowerValues(whom, namePlatePower, powerUseV, powerDesignV);
}

function setStanderdPower(whom) {
	var namePlatePower = $("#powerNameplate" + whom + "Id").val()
	var powerDesign = $("#powerDesign" + whom + "Id").val()
	var powerUse = $("#powerUse" + whom + "Id").val()

    var powerUseV = (parseInt(namePlatePower) * 0.33);
    $("#powerUse" + whom + "Id").val(powerUseV) ;

	var powerDesignV = (parseInt(namePlatePower) * 0.5);
	$("#powerDesign" + whom + "Id").val(powerDesignV);

	updateHiddenPowerValues(whom, namePlatePower, powerUseV, powerDesignV);
}

function compareOrMerge() {
	var ids = new Array()
	$('.cbox:checkbox:checked').each(function () {
		ids.push(this.id.split("_")[2])
	})
	jQuery.ajax({
		url: contextPath + '/model/compareOrMerge',
		data: { 'ids': ids },
		type: 'POST',
		success: function (data) {
			$("#showOrMergeId").html(data)
			$("#showOrMergeId").dialog('option', 'width', 'auto')
			$("#showOrMergeId").dialog('option', 'modal', true);
			$("#showOrMergeId").dialog('option', 'position', ['center', 'top']);
			$("#showOrMergeId").dialog('open');
		}
	});

}

function mergeModel() {
	var returnStatus = confirm('This will merge the selected models and change any associated assets.');
	if (returnStatus) {
		var targetModelId
		var modelToMerge = new Array()
		$('input[name=mergeRadio]:radio:checked').each(function () {
			targetModelId = this.id.split("_")[1]
		})
		if (!targetModelId) {
			alert("Please select Target Model")
			return
		}
		$('input[name=mergeRadio]:radio:not(:checked)').each(function () {
			modelToMerge.push(this.id.split("_")[1])
		})
		var params = {};
		$(".input_" + targetModelId).each(function () {
			if (this.name != 'manufacturer' && this.name != 'createdBy' && this.name != 'updatedBy')
				params[this.name] = this.value;
		})
		params['toId'] = targetModelId;
		params['fromId'] = modelToMerge;
		jQuery.ajax({
			url: contextPath + '/model/mergeModels',
			data: params,
			type: 'POST',
			beforeSend: function (jqXHR) {
				$("#showOrMergeId").dialog('close')
				$("#messageId").html($("#spinnerId").html())
				$("#messageId").show()
			},
			success: function (data) {
				$("#spinnerId").hide();
				// Recent change that returns the new status / error / success
				if(typeof data === 'object' && data.errors && data.errors.length > 0) {
					$("#messageId").html(data.errors.join('<br/>'));
				} else {
					// The UI is expecting a string
					$("#messageId").html(data);
				}
				$(".ui-icon-refresh").click();
			},
			error: function (jqXHR, textStatus, errorThrown) {
				$("#spinnerId").hide()
				$("#messageId").hide()
				alert("An unexpected error occurred while attempting to Merge Model ")
			}

		});
	} else {
		return false
	}
}
function switchTarget(id) {

	// If any of the fields of the target were changed, make the same changes to the span
	$(".editAll:visible").children().each(function () {
		var span = $(this).parent().siblings()
		if ($(this).attr('type') == 'checkbox')
			span.children().val(span.children().val())
		else {
			var toSpan = ''
			$(this).parent().children("input").each(function (i) {
				if (i > 0)
					toSpan = toSpan + '/'
				if ($(this).siblings().length > 0 && !$(this).val())
					toSpan = toSpan + 'null'
				else
					toSpan = toSpan + $(this).val()
			})
			if (toSpan.length > 20)
				toSpan = toSpan.substring(0, 20) + '...'
			span.html(toSpan)
		}
	})

	// Trim the three non-editable fields at the top of the form
	$("#showOrMergeId div table tbody").children(":eq(1)").children().children().each(function () { trimField($(this)) })
	$("#showOrMergeId div table tbody").children(":eq(2)").children().each(function () { trimField($(this)) })
	$("#showOrMergeId div table tbody").children(":eq(3)").children().children().each(function () { trimField($(this)) })

	$(".editAll").hide()
	$(".showAll").show()
	$(".showFrom_" + id).hide()
	$(".editTarget_" + id).show()

	var field = new Array()

	var targetModelId
	var modelToMerge = new Array()
	field = ['usize', 'height', 'width', 'depth', 'weight', 'layoutStyle', 'productLine', 'modelFamily', 'endOfLifeDate', 'endOfLifeStatus',
		'powerNameplate', 'powerDesign', 'powerUse', 'description', 'bladeRows', 'bladeCount', 'bladeLabelCount', 'sourceURL', 'modelStatus']

	$('input[name=mergeRadio]:radio:checked').each(function () {
		targetModelId = this.id.split("_")[1]
	})

	$('input[name=mergeRadio]:radio:not(:checked)').each(function () {
		modelToMerge.push(this.id.split("_")[1])
	})

	for (i = 0; i < field.length; i++) {
		for (j = 0; j < modelToMerge.length; j++) {
			if ($("#" + field[i] + "_edit_" + modelToMerge[j]).val() && !$("#" + field[i] + "_edit_" + targetModelId).val()) {
				$("#" + field[i] + "_td_" + modelToMerge[j]).addClass('willRemain')
				$("#" + field[i] + "_td_" + modelToMerge[j]).removeClass('willDelete')
			} else {
				$("#" + field[i] + "_td_" + modelToMerge[j]).addClass('willDelete')
				$("#" + field[i] + "_td_" + modelToMerge[j]).removeClass('willRemain')
			}
		}
	}

	$(".col_" + targetModelId).removeClass('willDelete')
	$(".col_" + targetModelId).removeClass('willRemain')
	$(".input_" + id).each(function () {
		if ($(this).val()) {
			$(this).addClass('willRemain')
			$(this).removeClass('willDelete')
		}
		else {
			$(this).addClass('willDelete')
			$(this).removeClass('willRemain')
		}
	})
}

// Trims the contents of fields with more than 40 characters
function trimField(source) {
	if (source.html().length > 40)
		source.html(source.html().substring(0, 40) + '...')
}

function removeCol(id) {
	$(".col_" + id).remove()
}

function deleteModels() {
	var modelArr = new Array();
	$(".cbox:checkbox:checked").each(function () {
		var modelId = $(this).attr('id').split("_")[2]
		if (modelId)
			modelArr.push(modelId)
	})
	if (!modelArr) {
		alert('Please select the Model');
	} else {
		if (confirm("You are about to delete all of the selected models for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel.")) {
			jQuery.ajax({
				url: contextPath + '/model/deleteBulkModels',
				data: { 'modelLists': modelArr },
				type: 'POST',
				success: function (data) {
					$(".ui-icon-refresh").click();
					$("#messageId").show();
					$("#messageId").html(data);
					$('#deleteModelId').attr('disabled', true)
				}
			});
		}
	}
}
