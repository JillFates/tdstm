/*
 * Javascript functions used by the Entity CRUD forms and Lists
 */
var EntityCrud = (function ($) {

	// Overrides the default select2-input matcher.
	$.fn.select2.defaults.matcher = matchStart

	var pub = {};	// public methods

	var stArray = ['source', 'target'];

	// The default quiet period for Select2 before issuing searches
	var quietMillis = 600;

	// ---
	// The following are a few handy accessors for the form variables
	// ---

	var modelFilteringData = {
		'id': null,
		'manufacturerId': null,
		'assetType': null,
		'term': null
	};

	var selectedModel = null;

	var assetFormName = "createEditAssetForm";

	// ------------------
	// Private Methods
	// ------------------

	/**
	 * Private method used to validate common fields on any of the asset create/edit forms
	 * @return true if valid
	 **/
	var validateCommonFields = function (formId, alertErrors) {
		var ok = true;
		var errors = '';
		alertErrors = typeof alertErrors !== 'undefined' ? alertErrors : true
		// Validate that asset name is not blank
		var fieldVal = $('#' + formId + ' #assetName').val();
		if (fieldVal == '') {
			errors += 'Please provide a name for the asset';
			ok = false;
		}

		ok = validateCustomFields(formId, alertErrors, errors, ok);

		// Commented out old validation error messages
		/*if (!ok && alertErrors && errors.length > 0) {
			alert(errors);
		}*/

		return ok;
	};


	/**
	 * Validations for Field Spec input fields.
	 * Simulates a submit of the form, this forces browser to run HTML5 built-in form validation.
	 * - Validates required fields.
	 * - Validates text input fields min-max length.
	 *
	 * @param errors
	 * @param formId
	 * @param isFormValid
	 * @param alertErrors
	 */
	function validateCustomFields(formId, alertErrors, errors, isFormValid) {

		var form = $('#'+formId)[0];

		// Prevent form for being submitted.
		$(form).submit(function (event) {
			event.preventDefault();
		});

		// Submit hidden button clicked by jquery only if form is not valid. See _editButton.gsp
		if(!form.checkValidity()){
			$('#assetUpdateSubmit').click();
			return false;
		}
		return isFormValid;

	};
	/**
	 * Private method used to validate the Database asset create/edit forms
	 * @return true if valid
	 **/
	var validateDBForm = function (form, alertErrors) {
		alertErrors = typeof alertErrors !== 'undefined' ? alertErrors : true;
		return validateCommonFields(form, alertErrors);
	};

    /**
     * Private method used to validate the Storage asset create/edit forms
     * @return true if valid
     **/
    var validateStorageForm = function (form) {
        return validateCommonFields(form);
    };


    /**
	 * Used to validate the Server/Device asset create/edit forms
	 * @return true if valid
	 **/
	var validateDeviceForm = function (form) {
		var ok = validateCommonFields(form);
		ok = ok && validateNewRoom(form, 'S') && validateNewRoom(form, 'T');
		return ok
	};

	var validateNewRoom = function (form, sourceTarget) {
		var ok = true;
		var rmCtrl = $('#roomSelect' + sourceTarget);
		var roomId = pub.selectOptionSelected(rmCtrl);
		if (roomId == -1) {
			var type = ((sourceTarget == 'S') ? 'Source' : 'Target');
			var location = $('#location' + type + 'Id' ).val();
			var room = $('#room' + type + 'Id').val();
			var rack = $('#rack' + type + 'Id').val();
			if (tds.utils.stringUtils.empty(location) ||
				tds.utils.stringUtils.empty(room) ) {
				alert("Location and Room Name must be defined for 'Add Room'")
				ok = false;
			}
		} else {
			var rackId = $('#' + ((sourceTarget == 'S') ? 'rackSourceId' : 'rackTargetId')).val();
			if (rackId == -1) {
				var type = ((sourceTarget == 'S') ? 'Source' : 'Target');
				var rack = $('#rack' + type + 'Id').val();
				if (tds.utils.stringUtils.empty(rack)) {
					alert("Rack name must be defined for 'Add Rack'")
					ok = false;
				}
			}
		}
		return ok;
	}

	var validateAppForm = function (form) {

		var ok = validateCommonFields(form);
		if (ok) {
			ok = false;
			if ($('#' + form + ' #sme1').val() == '0' || $('#' + form + ' #sme2').val() == '0' || $('#' + form + ' #appOwner').val() == '0') {
				alert("Please unselect the 'Add Person' option from SME, SME2 or Application Owner properties")
			} else {
				var msg = '';
				// Check to see if the durations have legit numbers
				var fname = ['Shutdown', 'Startup', 'Testing'];
				// Hack because of inconsistency in the field ids
				var c = 0;
				_(fname).forEach(function (name) {
					var fs = '#' + form + ' input[name=' + name.toLowerCase() + 'Duration]';
					var field = $(fs);
					if (field.length > 0) {
						if (field.val() != '' && isNaN(field.val())) {
							msg = (msg != '' ? msg + ', ' : '') + name;
							c++;
						}
					} else {
						console.log('validateAppForm() Unable to locate property ' + fs);
					}
				});
				if (msg != '') {
					alert("Please make sure that the " + msg + ' Duration field' + (c > 1 ? 's have' : ' has a') + ' numeric value' + (c > 1 ? 's' : ''));
				} else {
					ok = true;
				}
			}
		}
		return ok;
	};

	// ------------------
	// Public Methods
	// ------------------

	pub.getCreateModal = function () { return $('#createEntityView'); };
	pub.getCreateCloneModal = function() { return $('#cloneEntityView'); }
	pub.getEditModal = function () { return $('#editEntityView'); };
	pub.getShowModal = function () { return $('#showEntityView'); };

	pub.closeCreateModal = function () {
		pub.getCreateModal().dialog('close');
	};
	pub.closeEditModal = function () {
		pub.getEditModal().dialog('close');
	};
	pub.closeShowModal = function () {
		pub.getShowModal().dialog('close');
	};

	// Used to access the assetType within the CRUD pages
	pub.getAssetType = function () {
		return $('#currentAssetType').val();
	};

	// Get the initial rack id for S)ource or T)arget
	pub.getRackId = function (sourceTarget) {
		var id = $('#deviceRackId' + sourceTarget).val();
		return id;
	}

	// Get the initial chassis id for S)ource or T)arget
	pub.getChassisId = function (sourceTarget) {
		var id = $('#deviceChassisId' + sourceTarget).val();
		return id;
	}

	// Common method used to return the selected option in a select control
	// TODO : refactor this to shared lib
	pub.selectOptionSelected = function (selectCtrl) {
		var val = '';
		if (selectCtrl.jquery) {
			// JQuery found object
			val = selectCtrl.val();
		} else {
			// Select found by other means
			var si = selectCtrl.selectedIndex;
			if (si > -1)
				val = selectCtrl.options[si].value;
		}
		return val;
	};

	// Creates a Select2 control for an Asset Name selector used in Depenedencies
	pub.assetNameSelect2 = function (element) {
	    var currentSelector = '';
		element.select2({
			minimumInputLength: 0,
			width: '100%',
			initSelection: function (element, callback) {
				var data = { id: element.val(), text: element.data("asset-name") };
				callback(data);
			},
			placeholder: "Please select",
			ajax: {
				url: tdsCommon.createAppURL('/assetEntity/assetListForSelect2'),
				dataType: 'json',
				quietMillis: quietMillis,
				data: function (term, page) {
                    currentSelector = $(this)[0];
                    var selectElement = $(this).select2('data');
                    var selectValue = '';
                    if(selectElement && selectElement !== null) {
                        selectValue = $(this).select2('data').text;
                    }
					return {
						q: term,
                        value: selectValue,
						max: 25,
						page: $(this).data('select2').resultsPage,
						assetClassOption: $(this).data("asset-type"),
					};
				},
				results: function (data, page) {
                    $(currentSelector).data('select2').resultsPage = data.page + 1;
					var more = (data.page * 25) < data.total;
					return { results: data.results, more: more};
				}
			}
		});
	};

	pub.getSearchQuietMillis = function() {
        return quietMillis;
	};

	// Used on populate the Manufacturer SELECT to toggle various form fields
	// TODO : JPM 9/2014 : This is subject to change with the change in behavior with AssetType selector
	pub.selectManufacturer = function (assetType, forWhom) {
		EntityCrud.toggleAssetTypeFields(assetType);

		new Ajax.Request(
			tdsCommon.createAppURL('/assetEntity/retrieveManufacturersList?assetType=' + assetType + '&forWhom=' + forWhom),
			{
				asynchronous: true,
				evalScripts: true,
				onComplete: function (e) { showManufacView(e, forWhom); }
			}
		);
	};

	// Update Model Selector based on manufacturer
	pub.updateModelSelect = function (manuId, forWhom) {
		var assetType = $("#assetType" + forWhom + "Id").val();
		new Ajax.Request(
			tdsCommon.createAppURL('/assetEntity/retrieveModelsList?assetType=' + assetType + '&manufacturer=' + manuId + '&forWhom=' + forWhom),
			{
				asynchronous: true,
				evalScripts: true,
				onComplete: function (e) { showModelView(e, forWhom); }
			}
		);
	};

	pub.showRackOrBladeFields = function (rackBlade) {
		_(stArray).forEach(function (stValue) {
			// Show Rack select if room is selected; Rack name if New Room or New Rack; or nothing if Room or Rack are not selected
			_(['S', 'T']).forEach(function (stValue) {
				var rmCtrl = $('#roomSelect' + stValue);
				var roomId = pub.selectOptionSelected(rmCtrl);
				var sOrT = (stValue == 'S' ? 'source' : 'target');
				var elPrefix = '#' + sOrT;
				var rbcName = rackBlade.toLowerCase() + tdsCommon.capitalize(sOrT) + 'Id';
				var newrbcName = "new" + tdsCommon.capitalize(rbcName);
				var oldrbcName = "old" + tdsCommon.capitalize(rbcName);
				var rackBladeCtrl = $('#' + rbcName);
				var rackBladePosCtrl = $(elPrefix + rackBlade + 'PositionId');
				switch (roomId) {
					case '0':
						// Unselected
						$('.use' + rackBlade + stValue).hide();
						$(".newRoom" + stValue).hide();
						$(".newRack" + stValue).hide();
						rackBladePosCtrl.hide();

						if (rackBlade == 'Rack') {
							$('#' + rbcName).attr('name', rbcName);
							$('#' + newrbcName).attr('name', newrbcName);
						}
						break;
					case '-1':
						// Create room - room  and input fields
						$(".newRoom" + stValue).show();
						if (rackBlade == 'Rack') {
							$(".newRack" + stValue).show();
							$('.use' + rackBlade + stValue).hide();
							rackBladePosCtrl.hide();
							rackBladePosCtrl.show();

							$('#' + rbcName).attr('name', oldrbcName);
							$('#' + newrbcName).attr('name', rbcName);
						}
						break;
					default:
						$(".newRoom" + stValue).hide();
						$(".newRack" + stValue).hide();
						$('.use' + rackBlade + stValue).show();
						// Now show the chassis position based on if a chassis is selected
						var rbVal = pub.selectOptionSelected(rackBladeCtrl);
						var optionId = parseInt(rbVal);
						if (optionId > 0) {
							rackBladePosCtrl.show();
						} else {
							rackBladePosCtrl.hide();
						}
						if (rackBlade == 'Rack') {
							$('#' + rbcName).attr('name', rbcName);
							$('#' + newrbcName).attr('name', newrbcName);
						}
				}
			});
		});

	}

	// Used to hide fields only for VMs
	pub.hideNonVMFields = function () {
        $(".nonVMLabel").hide();
	}

	// Used to display Chassis fields appropriately
	pub.showChassisFields = function () {
		$(".positionLabel").show();
		$(".bladeLabel").show();
		_(stArray).forEach(function (stValue) {
			// Only show Chassis select if room is selected
			_(['S', 'T']).forEach(function (stValue) {
				var rmCtrl = $('#roomSelect' + stValue);
				var roomId = pub.selectOptionSelected(rmCtrl);
				var sOrT = (stValue == 'S' ? 'source' : 'target');
				var elPrefix = '#' + sOrT;
				var chassisCtrl = $(elPrefix + 'ChassisSelectId');
				var bladePosCtrl = $(elPrefix + 'BladePositionId');
				switch (roomId) {
					case '-1':
						// Show room input fields
						$(".newRoom" + stValue).show();
						$(".useBlade" + stValue).hide();
						bladePosCtrl.hide();
						break;
					case '0':
						$(".useBlade" + stValue).hide();
						$(".newRoom" + stValue).hide();
						bladePosCtrl.hide();
						break;
					default:
						$(".useBlade" + stValue).show();
						$(".newRoom" + stValue).hide();
						// Now show the chassis position based on if a chassis is selected
						var optionId = parseInt(chassisCtrl.val());
						if (optionId > 0) {
							bladePosCtrl.show();
						} else {
							bladePosCtrl.hide();
						}
				}
			});
		});
	};

	// Used to hide Chassis fields
	pub.hideChassisFields = function () {
		$(".bladeLabel").hide();
		$(".useBladeS").hide();
		$(".useBladeT").hide();
	};

	// Used to display the Rack fields
	pub.showRackFields = function () {
		$(".positionLabel").show();
		$(".rackLabel").show();
		pub.showRackOrBladeFields('Rack');
	}

	// Used to hide Rack fields
	pub.hideRackFields = function () {
		$(".newRackS").hide();
		$(".newRackT").hide();
		$(".useRackS").hide();
		$(".useRackT").hide();
		$(".rackLabel").hide();
	};

	// Used to display VM fields
	pub.showVMFields = function () {
		$(".vmLabel").show();
		$(".positionLabel").hide();
	};

	// Used to hide VM fields
	pub.hideVMFields = function () {
		$(".vmLabel").hide();
	};

	// Used to hide and show the appropriate fields based on the asset type (Blade, VM or other types)
	pub.toggleAssetTypeFields = function (assetType) {
		switch (assetType) {
			case 'Blade':
				pub.hideRackFields();
				pub.hideVMFields();
				pub.showChassisFields();
				populateRackOrChassisSelect(false);
				break;
			case 'VM':
				pub.hideRackFields();
				pub.hideChassisFields();
				pub.showVMFields();
				pub.hideNonVMFields()
				break;
			default:
				// Rackable device
				pub.hideChassisFields();
				pub.hideVMFields();
				pub.showRackFields();
				populateRackOrChassisSelect(true);
		}
	};

	var populateRackOrChassisSelect = function (checkRask) {
		_(['S', 'T']).forEach(function (stValue) {
			var rmCtrl = $('#roomSelect' + stValue);
			var roomId = pub.selectOptionSelected(rmCtrl);

			if ((roomId != null) && (roomId != "") && (roomId != "0") && (roomId != "-1")) {
				if (checkRask) {
					var rackSelect = $(stValue == 'S' ? '#rackSourceId' : '#rackTargetId');
					if (rackSelect.children('option').length <= 2) {
						pub.fetchRackSelectForRoom(roomId, stValue, 'Edit');
					}
				} else {
					var chassisSelect = $(stValue == 'S' ? '#sourceChassisSelectId' : '#targetChassisSelectId');
					if (chassisSelect.children('option').length <= 1) {
						pub.fetchChassisSelectForRoom(roomId, stValue, 'Edit');
					}
				}
			}

		});
	}

	// Used to retrieve a SELECT control populated with the appropriate Rack for the specified room and source/target and assign to the appropriate form control
	pub.fetchRackSelectForRoom = function (roomId, sourceTarget, forWhom) {
		var rackId = pub.getRackId(sourceTarget);
		var rsName = '#rack' + (sourceTarget == 'S' ? 'Source' : 'Target') + 'Id';
		var selectCtrl = $(rsName);
		if (selectCtrl.length) {
			jQuery.ajax({
				url: tdsCommon.createAppURL('/assetEntity/retrieveRackSelectForRoom'),
				data: { 'roomId': roomId, 'rackId': rackId, 'sourceTarget': sourceTarget, 'forWhom': forWhom },
				type: 'POST',
				success: function (resp) {
					selectCtrl.html(resp);
					EntityCrud.showRackFields();

					if (!isIE7OrLesser)
						selectCtrl.select2();
				},
				error: function (xhr, ajaxOptions, thrownError) {
					alert(xhr.status + " " + thrownError);
					selectCtrl.children().remove().end().append('<option selected value="0">Unable to load</option>');
					selectCtrl.val(0);
				}
			});
		} else {
			alert('ERROR: fetchRackSelectForRoom() unable to locate select id ' + rsName);
		}
	};

	// Used to retrieve a SELECT control populated with the appropriate Blade Chassis for the specified room and source/target and assign to the appropriate form control
	pub.fetchChassisSelectForRoom = function (roomId, sourceTarget, forWhom) {
		var id = pub.getChassisId(sourceTarget);
		var selectCtrl = $('#' + (sourceTarget == 'S' ? 'source' : 'target') + 'ChassisSelectId');

		jQuery.ajax({
			url: tdsCommon.createAppURL('/assetEntity/retrieveChassisSelectForRoom'),
			data: { 'roomId': roomId, 'id': id, 'sourceTarget': sourceTarget, 'forWhom': forWhom },
			type: 'POST',
			success: function (resp) {
				selectCtrl.html(resp);
				pub.showChassisFields();

				if (!isIE7OrLesser)
					selectCtrl.select2();
			},
			error: function (xhr, ajaxOptions, thrownError) {
				alert(xhr.status + " " + thrownError);
				selectCtrl.children().remove().end().append('<option selected value="0">Unable to load</option>');
				selectCtrl.val(0);
			}
		});
	};

	// Handle form changes when the user selects a room
	pub.updateOnRoomSelection = function (selectCtrl, sourceTarget, forWhom) {
		var selectId = pub.selectOptionSelected(selectCtrl);
		var assetType = pub.getAssetType();
		switch (assetType) {
			case 'Blade':
				if (parseInt(selectId) > 0)
					pub.fetchChassisSelectForRoom(selectId, sourceTarget, forWhom);
				break;
			case 'VM':
				break;
			default:
				if (parseInt(selectId) > 0)
					pub.fetchRackSelectForRoom(selectId, sourceTarget, forWhom);
		}
		pub.toggleAssetTypeFields(assetType);
		if (selectId == -1) {
			var elPrefix = "#" + (sourceTarget == 'S' ? 'source' : 'target');
			$(elPrefix + 'LocationId').focus();
		}
	};

	// Called after the user selects a chassis
	pub.updateOnChassisSelection = function (selectCtrl, sourceTarget, forWhom) {
		pub.showChassisFields();
	};

	// Updates the form after the user chooses a rack
	pub.updateOnRackSelection = function (selectCtrl, sourceTarget, forWhom) {
		var selectId = pub.selectOptionSelected(selectCtrl);
		var elPrefix = "#" + (sourceTarget == 'S' ? 'source' : 'target');
		var rackName = $(elPrefix + 'RackId');
		var newRack = $(".newRack" + sourceTarget);
		var rackPosition = $(elPrefix + 'RackPositionId');

		switch (selectId) {
			case '0':
				newRack.hide();
				rackPosition.hide();
				rackPosition.val('');	// clear out current position
				break;
			case '-1':
				newRack.show();
				rackPosition.show();
				rackName.focus();
				break;
			default:
				newRack.hide();
				rackPosition.show().focus();
		}
	};

	// private singleton variable to control the saveToShow function from being called repeatedly
	var assetCreateInvoked = false;

	/**
	 * Used to save newly created assets from the Create forms. After validating that the fields are
	 * okay it will make an Ajax call to create the asset.
	 *
	 * @param me - the form that is being processed
	 * @param forWhom - string indicating which form is being processed (note that this is inconsistent with the update metho)
	 */
	pub.saveToShow = function (button, assetClass) {
		if (assetCreateInvoked) {
			alert("Please only click the save button once. Your save request is being processed.");
			return false;
		} else {
			assetCreateInvoked = true;
		}

		// var action = button.data('action');
		var redirect = button.data('redirect');

		if (redirect)
			redirect = redirect.split("_")[0]
		// TODO : JPM 10/2014 : Determine purpose - has to do with the close/show behavior
		$('#showView').val('showView')

		//var type = assetClass;
		var formName = assetFormName;
		var validateOkay = pub.validateForm(assetClass, formName, true);
		if (validateOkay)
			validateOkay = pub.validateDependencies(formName)
		if (validateOkay) {
			var url = $('#createEditAssetForm').attr('action');
			jQuery.ajax({
				url: url,
				data: $('#createEditAssetForm').serialize(),
				type: 'POST',
				success: function (resp) {
					if (resp.status == 'error') {
						alert(resp.errors);
						assetCreateInvoked = false;
						return false;
					} else {
						$angularScope = getCurrentAngularContext();
						if ($angularScope) {
							$angularScope.onSubmitAssetTags(resp.data.asset.id);
						}

						pub.showAssetDetailView(assetClass, resp.data.asset.id);

						/*
						// Go to the show view
						if($('.ui-icon-refresh').length);
							$('.ui-icon-refresh').click();
						if (redirect=='room')
							getRackLayout( $('#selectedRackId').val() );
						*/
					}
					assetCreateInvoked = false;
					$(document).trigger('entityAssetCreated', resp.data);
				},
				error: function (jqXHR, textStatus, errorThrown) {
					var err = jqXHR.responseText;
					console.log("error 2")
					alert("The following error occurred while attempting to create asset : " + err);
					assetCreateInvoked = false;
					return false;
				},
				// This function is executed after the interceptor displays the error message, to perform additional actions.
				successWithErrors: function (resp) {
					assetCreateInvoked = false;
				},
			});
		} else {
			assetCreateInvoked = false;
		}
		return true;
	};


	pub.validateForm = function (assetClass, formName, alertErrors) {
		var validateOkay = true;
		alertErrors = typeof alertErrors !== 'undefined' ? alertErrors : true
		switch (assetClass) {
			case 'APPLICATION':
				validateOkay = validateAppForm(formName);
				break;

			case 'DATABASE':
				validateOkay = validateDBForm(formName);
				break;

			case 'STORAGE':
				validateOkay = validateStorageForm(formName);
				break;

			case 'DEVICE':
				validateOkay = validateDeviceForm(formName);
				break;

			default:
				alert('ERROR: validateForm() - unsupported case for ' + assetClass);
		}
		return validateOkay;

	}

	/**
	 * Invoke the angular scope from a non angular section to load the Asset Tags for the View
	 */
	pub.loadAssetTags = function(assetId) {
		// Getting the Context
		var $angularScope;
		if (typeof getCurrentAngularContext !== "undefined") {
			$angularScope = getCurrentAngularContext();
			if ($angularScope) {
				// Asset Main Controller
				$angularScope.loadAssetTags(assetId  || $('#assetId').val());
			}
		}
	};

	// Private variable used to prevent multiple clicks from invoking multiple updates
	var assetUpdateInvoked = false;

	/**
	 * Used to call Ajax Update for given asset form and then load the show view of the asset
	 * @param me - the button that the user clicked to get here
	 * @param forWhom - the asset class of the form (app, files, database)
	 **/
	pub.performAssetUpdate = function (buttonClicked, assetClass) {

		if (assetUpdateInvoked) {
			alert("Please only click the update button once. Your update request is being processed.");
			return false;
		} else {
			assetUpdateInvoked = true;
			buttonClicked.disabled = true;
		}

		var act = buttonClicked.data('action');
		var type = 'Server';
		var redirect = buttonClicked.data('redirect');

		$('#updateView').val('updateView');

		var validateOkay = false;
		var formName = assetFormName;
		switch (assetClass) {
			case 'APPLICATION':
				type = 'Application';
				validateOkay = validateAppForm(formName);
				break;

			case 'STORAGE':
				type = 'Storage';
				validateOkay = validateStorageForm(formName);
				break;

			case 'DATABASE':
				type = 'Database';
				validateOkay = validateDBForm(formName);
				break;

			case 'DEVICE':
				validateOkay = validateDeviceForm(formName);
				break;

			default:
				alert("Unsupported case for assetClass '" + assetClass + "' in performAssetUpdate()");

		}

		if (validateOkay)
			validateOkay = pub.validateDependencies(formName);

		if (validateOkay) {
			// Verify the

			var formObj = $('#' + formName);
			if (formObj.length == 0) {
				alert("Unable to locate form " + formName);
				return false;
			}
			var assetId = $("#" + formName + " :input[name='id']").val();
			var url = formObj.attr('action') + '/' + assetId;
			var data = formObj.serialize();
			jQuery.ajax({
				url: url,
				data: data,
				type: 'POST',
				//async: false,
				complete: function (resp, status) {
					buttonClicked.one(function () {
						pub.EntityCrud.performAssetUpdate(buttonClicked, assetClass);
					});
					assetUpdateInvoked = false;
					buttonClicked.disabled = false;
				},
				success: function (resp, dataType) {
					pub.showAssetDetailView(assetClass, resp.data.asset.id);
					$(document).trigger('entityAssetUpdated', resp.data);
					// If Asset get saved properly, save the Asset Tags
					$angularScope = getCurrentAngularContext();
					if ($angularScope) {
						$angularScope.onSubmitAssetTags(resp.data.asset.id);
					}
				},
				error: function (jqXHR, textStatus, errorThrown) {
					var err = jqXHR.responseText;
					alert("An error occurred while updating Asset." + err.substring(err.indexOf("<span>") + 6, err.indexOf("</span>")));
					buttonClicked.one(function () {
						pub.EntityCrud.performAssetUpdate(buttonClicked, assetClass);
					});
					assetUpdateInvoked = false;
					buttonClicked.disabled = false;
					return false;
				}
			});
		} else {
			assetUpdateInvoked = false;
		}
		return true;
	};

	// Used to format the results of the model shown in the Model select (below) so that validated models are emphasized with BOLD and the other italics
	var modelSelectFormatResult = function (item) {
		if (item.text) {
			if (item.isValid) {
				return '<b>' + _.escape(item.text) + '</b>';
			} else {
				return '<i>' + _.escape(item.text) + '</i>';
			}
		} else {
			return '';
		}
	};
	// Used to format the item selected of the model in the Model select (below) so that validated models are emphasized with BOLD and the other italics
	var modelSelectFormatSelection = function (item) {
		if (item.text) {
			if ((('isValid' in item) && item.isValid) || item.text.indexOf('?') == -1) {
				return '<b>' + _.escape(item.text) + '</b>';
			} else {
				return '<i>' + _.escape(item.text) + '</i>';
			}
		} else {
			return '';
		}
	};

	// Initializes the various controls for the selection of manufacturer and model with the assetType filter
	pub.initializeUI = function (modelId, modelName, manufacturerId, manufacturerName) {

		// Initialize the model filtering parameters
		modelFilteringData.id = $('#hiddenModel').val();
		modelFilteringData.manufacturerId = $('#hiddenManufacturer').val();
		modelFilteringData.assetType = $('#currentAssetType').val();
		modelFilteringData.id = $('#currentAssetType').val();

		$('#assetTypeFilterUnSet').click(function () {
			$('#assetTypeLabel').toggle();
			$('#assetTypeSelectContainer').toggle();
		});

		$('#assetTypeFilterSet').click(function () {
			$('#assetTypeLabel').toggle();
			$('#assetTypeSelectContainer').toggle();
			$('#assetTypeFilterSet').toggle();
			$('#assetTypeFilterSet2').toggle();
		});

		$('#assetTypeFilterSet2').click(function () {
			$('#assetTypeLabel').toggle();
			$('#assetTypeSelectContainer').toggle();
			$('#assetTypeFilterSet').toggle();
			$('#assetTypeFilterSet2').toggle();
		});

		$("#assetTypeSelect").select2({
			placeholder: "Device type filter",
			minimumInputLength: 0,
			allowClear: true,
			width: "100%",
			allowClear: true,
			formatAjaxError: tdsCommon.select2AjaxErrorHandler,
			ajax: {
				url: tdsCommon.createAppURL('/assetEntity/assetTypesOf'),
				quietMillis: quietMillis,
				dataType: 'json',
				data: function (term, page) {
					return {
						"term": term,
						"manufacturerId": modelFilteringData.manufacturerId
					};
				},
				results: function (data, page) {
					return { results: data.data.assetTypes };
				}
			},
			initSelection: function (element, callback) {
				var at = EntityCrud.getAssetType();
				if (at != "") {
					callback({ id: at, text: at });
				}
			}
		}).select2('val', []);

		$("#assetTypeSelect").on("change", function (event) {
			$('#assetTypeFilterSet').show();
			$('#assetTypeFilterUnSet').hide();
			$('#currentAssetType').val(event.val);
			modelFilteringData.assetType = event.val;
			modelFilteringData.id = event.val;
			if ((selectedModel != null && selectedModel.assetType != null && event.val != selectedModel.assetType.toString()) || (event.val == null)) {
				if (selectedModel != null) {
					clearSelectedModel(selectedModel.manufacturerId, selectedModel.manufacturerName, event.val);
				} else {
					clearSelectedModel(null, null, event.val);
				}
			} else {
				pub.toggleAssetTypeFields(event.val);
			}
		});

		$("#modelSelect").select2({
			placeholder: "Model",
			minimumInputLength: 0,
			dropdownAutoWidth: true,
			width: "100%",
			allowClear: true,
			// Specify format function for dropdown item
			formatResult: modelSelectFormatResult,
			// Specify format function for selected item
			formatSelection: modelSelectFormatSelection,
			formatAjaxError: tdsCommon.select2AjaxErrorHandler,
			ajax: {
				url: tdsCommon.createAppURL('/assetEntity/modelsOf'),
				quietMillis: quietMillis,
				dataType: 'json',
				data: function (term, page) {
					modelFilteringData.term = term;
					return modelFilteringData;
				},
				results: function (data, page) {
					return { results: data.data.models };
				},
			},
			initSelection: function (element, callback) {
				if (modelId != "") {
					callback({ id: modelId, text: modelName });
				}
			},
		}).select2('val', []);

		$('#modelSelect').on("change", function (event) {
			//console.log("in the #modelSelect on change event");
			if (event.added) {
				var at = event.added.assetType;
				selectedModel = event.added;

				var modelId = selectedModel.id;
				var manuId = selectedModel.manufacturerId;
				var manuName = selectedModel.manufacturerName;

				$('#currentAssetType').val(at);
				//$('#asset_model').val(modelId);
				$('#hiddenModel').val(modelId);
				$('#hiddenManufacturer').val();

				pub.toggleAssetTypeFields(at);

				// Put trailing questionmark for non-validated models
				var modelText = selectedModel.name + (selectedModel.isValid ? '' : ' ?');
				$('#modelSelect').select2('data', { 'id': selectedModel.id, 'text': modelText });
				$("#manufacturerSelect").select2('data', { "id": manuId, "text": manuName });
				$("#assetTypeSelect").select2('data', { "id": at, "text": at });
				modelFilteringData.assetType = at;
				modelFilteringData.id = at;
				modelFilteringData.manufacturerId = manuId;
				$(document).trigger('selectedAssetModelChanged', selectedModel);
			} else {
				clearSelectedModel(selectedModel.manufacturerId);
			}
		});

		$("#manufacturerSelect").select2({
			placeholder: "Manufacturer filter",
			minimumInputLength: 0,
			dropdownAutoWidth: true,
			width: "100%",
			allowClear: true,
			formatAjaxError: tdsCommon.select2AjaxErrorHandler,
			ajax: {
				url: tdsCommon.createAppURL('/assetEntity/manufacturer'),
				quietMillis: quietMillis,
				dataType: 'json',
				data: function (term, page) {
					return {
						"term": term,
						"assetType": modelFilteringData.assetType
					};
				},
				results: function (data, page) {
					return { results: data.data.manufacturers };
				}
			},
			initSelection: function (element, callback) {
				if (manufacturerId != "") {
					callback({ id: manufacturerId, text: manufacturerName });
				}
			}
		}).select2('val', []);

		$('#manufacturerSelect').on("change", function (event) {
			modelFilteringData.manufacturerId = event.val;
			$('#hiddenManufacturer').val(event.val);

			if ((selectedModel != null && selectedModel.manufacturerId != null && event.val != selectedModel.manufacturerId.toString()) || (event.val == null)) {
				clearSelectedModel(event.val, '', null);
			}
		});
	};

	var clearSelectedModel = function (manufacturerId, manufacturerName, assetType) {
		selectedModel = {
			"id": null,
			"text": null,
			"assetType": assetType,
			"manufacturerId": manufacturerId,
			"manufacturerName": manufacturerName,
			"usize": null
		}

		$("#modelSelect").select2('val', '');
		$('#hiddenModel').val('');
		if (assetType) {
			pub.toggleAssetTypeFields(assetType);
		}
		$(document).trigger('selectedAssetModelChanged', selectedModel);
	}

	pub.setManufacturerValues = function (modelId, modelName, assetType, manufacturerId, manufacturerName) {
		selectedModel = {
			"id": modelId,
			"text": modelName,
			"assetType": assetType,
			"manufacturerId": manufacturerId,
			"manufacturerName": manufacturerName,
			"usize": null
		}
		$("#assetTypeSelect").val('');
		$(document).trigger('selectedAssetModelChanged', selectedModel);
	};

	// Validates the user input for the dependency section of the form
	// TODO : JPM 10/2014 - It would be nice to include which asset dependency is in error for validateDependencies
	pub.validateDependencies = function (formName) {
		var valid = true;
		$('#' + formName + ' input[name^="asset_"]').each(function () {
			if ($(this).val() == 'null' || $(this).val() == '')
				valid = false;
		});
		if (!valid)
			alert("Please select a valid asset for all dependencies");

		return valid;
	}


	// Used to populate the hidden form fields with the values from the JQGrid form fields
	// TODO : JPM 10/2014 - See if we can eliminate loadFormFromJQGridFilters
	pub.loadFormFromJQGridFilters = function () {
		$("#asset_assetName").val($('#gs_assetName').val())
		$("#asset_assetType").val($('#gs_assetType').val())
		$("#asset_model").val($('#gs_model').val())
		$("#asset_sourceRack").val($('#gs_sourceRack').val())
		$("#asset_targetRack").val($('#gs_targetRack').val())
		$("#asset_serialNumber").val($('#gs_serialNumber').val())
		$("#asset_planStatus").val($('#gs_planStatus').val())
		$("#asset_moveBundle").val($('#gs_moveBundle').val())
		$("#asset_assetTag").val($('#gs_assetTag').val())
	};

	// Used to change the Select2 Asset Name SELECT when the Class SELECT is changed
	// TODO : JPM 9/2014 : Is this still being used?
	pub.updateDependentAssetNameSelect = function (name) {
		var split = name.split("_");
		var classSelect = $("select[name='entity_" + split[1] + "_" + split[2] + "']");
		var nameSelect = $("input[name='asset_" + split[1] + "_" + split[2] + "']");
		nameSelect.data("asset-type", classSelect.val());
		nameSelect.select2("val", "");
	};

	// Private method called by fetchAssetEditView to actually display the form once it is retrieved
	var presentAssetEditView = function (html, fieldHelpType, source, rackOrChassisId, roomId, location, position, isBlade) {
		var editModal = pub.getEditModal();

		if (editModal.length) {
			pub.closeShowModal();
			pub.closeCreateModal();
			editModal.html(html);
			editModal.dialog('option', 'width', 'auto');
			editModal.dialog('option', 'modal', 'true');
			editModal.dialog('option', 'position', ['center', 'top']);
			editModal.dialog('open');

			if (typeof timerBar !== 'undefined')
				timerBar.Pause();

			updateAssetTitle(fieldHelpType);
			if (rackOrChassisId)
				updateAssetInfo(source, rackOrChassisId, roomId, location, position, 'edit', isBlade);
			return true;
		} else {
			console.log("EntityCrud.presentAssetEditView() Error: Unable to access editEntityView DIV");
			return false;
		}
	}
	// Private method used by showAssetEditView to fetch the edit view for the asset class appropriately by
	// calling the controller/edit/assetId controller method and then invoking presentAssetEditView to display.
	var fetchAssetEditView = function (controller, fieldType, assetId, source, rackOrBladeId, roomId, location, position, isBlade, callback) {
		var url = tdsCommon.createAppURL('/' + controller + '/edit/' + assetId);
		jQuery.ajax({
			url: url,
			type: 'POST',
			success: function (resp) {
				// Load the edit entity view
				if (callback) {
					callback();
				}
				return presentAssetEditView(resp, fieldType, source, rackOrBladeId, roomId, location, position, isBlade);
			},
			error: function (jqXHR, textStatus, errorThrown) {
				var err = jqXHR.responseText;
				alert("An error occurred while loading the asset edit form." + err.substring(err.indexOf("<span>") + 6, err.indexOf("</span>")));
				return false;
			}
		});
	};
	// Private method used by fetchAssetDependencyEditView to fetch the edit view for the asset dependencies
	var fetchAssetDependencyEditView = function (assetA, assetB, action) {
		var editModal = pub.getEditModal();

		if (editModal.length) {
			pub.closeShowModal();
		}

		var assetDependencies = {
			assetAId: assetA.id,
			assetBId: assetB.id
		};
		var url = tdsCommon.createAppURL('/ws/asset/dependencies');
		jQuery.ajax({
			url: url,
			type: 'POST',
			data: assetDependencies,
			success: function (resp) {
				var currentScope = angular.element('.body').scope();
				if(currentScope) {
					if(currentScope.commentsScope) {
						currentScope.commentsScope.$emit('viewAssetDependency', resp.data, action);
					} else {
						currentScope.$emit('viewAssetDependency', resp.data, action);
					}
				}
			},
			error: function (jqXHR, textStatus, errorThrown) {
				var err = jqXHR.responseText;
				alert("An error occurred while loading the asset edit form." + err.substring(err.indexOf("<span>") + 6, err.indexOf("</span>")));
				return false;
			}
		});
	};
	// Used to display the various asset class edit modal views
	// This replaces editEntity()
	pub.showAssetEditView = function (assetClass, assetId, source, rackOrBladeId, roomId, location, position, isBlade, callback) {
		assetUpdateInvoked = false;	// Reset the update invoked flag
		switch (assetClass) {
			case "APPLICATION":
				return fetchAssetEditView('application', 'Application', assetId, source, rackOrBladeId, roomId, location, position, isBlade, callback);
				break;
			case "DATABASE":
				return fetchAssetEditView('database', 'Database', assetId, source, rackOrBladeId, roomId, location, position, isBlade, callback);
				break;
			case "STORAGE":
				return fetchAssetEditView('files', 'Logical Storage', assetId, source, rackOrBladeId, roomId, location, position, isBlade, callback);
				break;
			case "DEVICE":
				return fetchAssetEditView('assetEntity', 'Device', assetId, source, rackOrBladeId, roomId, location, position, isBlade, callback);
				break;
			default:
				alert("Error in editEntity() - unsupported case for assetClass '" + assetClass + "'");
				return false;
		}
	};
	// Used to display the Asset Dependency modal view
	pub.showAssetDependencyEditView = function (assetA, assetB, type) {
		return fetchAssetDependencyEditView(assetA, assetB, type);
	};

	// Displays the detail view of the asset from ajax call in model popup
	var presentAssetShowView = function (html, fieldHelpType) {
		var showModal = pub.getShowModal();

		if (showModal.length) {
			pub.closeCreateModal();
			pub.closeEditModal();
			showModal.html(html);
			showModal.dialog('option', 'width', 'auto');
			showModal.dialog('option', 'modal', 'true');
			showModal.dialog('option', 'position', ['center', 'top']);
			showModal.dialog('open');
			if (typeof timerBar !== 'undefined')
				timerBar.Pause();
			updateAssetTitle(fieldHelpType);

			$('div.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>');
			$('[data-toggle="popover"]').popover();

			return true;
		}
		return false;
	};

    // Displays the detail view of the asset from ajax call in model popup
    var presentCloneShowView = function (html, fieldHelpType) {
        var showModal = pub.getCreateCloneModal();

        if (showModal.length) {
            showModal.html(html);
            showModal.dialog('option', 'width', 'auto');
            showModal.dialog('option', 'modal', 'true');
            showModal.dialog('option', 'position', ['center', 'top']);
            showModal.dialog('open');
            if (typeof timerBar !== 'undefined')
                timerBar.Pause();
            updateAssetTitle(fieldHelpType);

            $('div.ui-dialog.ui-widget').find('button.ui-dialog-titlebar-close').html('<span class="ui-button-icon-primary ui-icon ui-icon-closethick"></span>');
            $('[data-toggle="popover"]').popover();

            return true;
        }
        return false;
    };


	// Private method used by showAssetEditView to fetch the edit view for the asset class appropriately by
	// calling the controller/edit/assetId controller method and then invoking presentAssetEditView to display.
	var fetchAssetShowView = function (controller, fieldHelpType, assetId) {
		var url = tdsCommon.createAppURL('/' + controller + '/show/' + assetId);
		jQuery.ajax({
			url: url,
			type: 'POST',
			success: function (resp) {
				if (typeof resp === 'object') {
					if (resp.status == 'error') {
						alert("An error occurred: " + resp.errors);
						return false;
					}
					console.log("fetchAssetShowView() had unexpected response");
					return false;
				}
				// Load the edit entity view
				return presentAssetShowView(resp, fieldHelpType);
			},
			error: function (jqXHR, textStatus, errorThrown) {
				var err = jqXHR.responseText;
				alert("An error occurred while loading the asset show view." + err.substring(err.indexOf("<span>") + 6, err.indexOf("</span>")));
				return false;
			}
		});
	};

    /**
	 * Opens the Clone Dialog that holds the info for the clonning Asset
     * @param assetId
     * @param fieldHelpType
     */
    var fetchCloneView = function (assetId, fieldHelpType) {
        var url = tdsCommon.createAppURL('/assetEntity/cloneEntity/' + assetId);
        jQuery.ajax({
            url: url,
            type: 'POST',
            success: function (resp) {
                if (typeof resp === 'object') {
                    if (resp.status == 'error') {
                        alert("An error occurred: " + resp.errors);
                        return false;
                    }
                    console.log("fetchCloneView() had unexpected response");
                    return false;
                }
                // Load the edit entity view
                return presentCloneShowView(resp, fieldHelpType);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                var err = jqXHR.responseText;
                alert("An error occurred while loading the asset show view." + err.substring(err.indexOf("<span>") + 6, err.indexOf("</span>")));
                return false;
            }
        });
    };

    /**
	 * Public method to verify that an Asset is Unique or not
     * @param assetId
     * @param assetName
     */
    pub.isAssetUnique = function(assetToValid, callback) {
        var url = tdsCommon.createAppURL('/ws/asset/checkForUniqueName');
        var xhr = jQuery.ajax({
            url: url,
            type: 'POST',
            data: assetToValid,
            dataType: 'json',
            success: function (resp) {
                return callback(resp);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                return false;
            }
        });

        xhr.loadingRequest = true;
        return xhr;
	};

    /**
     * Public method to Create the new Asset from the Clone feature.
     * @param assetId
     * @param assetName
     */
    pub.cloneAsset = function(assetToClone, callback) {
        var url = tdsCommon.createAppURL('/ws/asset/clone');
        var xhr = jQuery.ajax({
            url: url,
            type: 'POST',
            data: assetToClone,
            dataType: 'json',
            success: function (resp) {
                return callback(resp);
            },
            error: function (jqXHR, textStatus, errorThrown) {
                return false;
            }
        });

        xhr.loadingRequest = true;
        return xhr;
    };

	// Called from the page to popup the Asset Entity details dialog
	// getEntityDetails
	pub.showAssetDetailView = function (assetClass, assetId) {
		/*
			Handling Angular Scope (root scope) outside Angular. This is
			far from ideal, but it's required for keeping track of the current
			asset when the user edits a comment from the asset modal.
		*/
		var $asset = angular.element("#" + assetId)
		if ($asset.scope()) {
			var $rootScope = $asset.scope().$root
			$rootScope.$apply(function () {
				$rootScope.selectedAsset = assetId
			})
		}

		switch (assetClass) {
			case "APPLICATION":
				return fetchAssetShowView('application', 'Application', assetId);
				break;
			case "DATABASE":
				return fetchAssetShowView('database', 'Database', assetId);
				break;
			case "STORAGE":
				return fetchAssetShowView('files', 'Logical Storage', assetId);
				break;
			case "DEVICE":
				return fetchAssetShowView('assetEntity', 'Device', assetId);
				break;
			default:
				alert("Error in EntityCrud.showAssetDetailView() - Unsupported case for assetClass '" + assetClass + "'");
				return false;
		}

	};

    /**
	 * Opens The clone views that allow the user to copy over an entire asset based on a Name plus Id
     * @param assetClass
     * @param assetId
     * @returns {boolean}
     */
	pub.cloneAssetView = function (assetClass, assetName, assetId) {

		var assetType = assetClass.toLowerCase();
		assetType = assetType.charAt(0).toUpperCase() + assetType.slice(1);

        return fetchCloneView(assetId, assetName);

    };

	// Private method called by fetchAssetCreateView to present the various asset create modal window
	var presentAssetCreateView = function (html, fieldHelpType, source, rackOrChassisId, roomId, location, position, isBlade) {
		var createModal = pub.getCreateModal();
		if (createModal.length) {
			createModal.html(html);
			createModal.dialog('option', 'width', 'auto');
			createModal.dialog('option', 'modal', 'true');
			createModal.dialog('option', 'position', ['center', 'top']);
			createModal.dialog('open');
			pub.closeEditModal();
			pub.closeShowModal();

			if (typeof timerBar !== 'undefined')
				timerBar.Pause();
			pub.populateAssetEditView(fieldHelpType, source, rackOrChassisId, roomId, location, position, 'create', isBlade);
			return true;
		} else {
			console.log("presentAssetCreateView() error - unable to access the create modal DIV");
			return false;
		}
	};
	pub.populateAssetEditView = function (fieldHelpType, source, rackOrChassisId, roomId, location, position, forWhom, isBlade) {
		updateAssetTitle(fieldHelpType);
		if (fieldHelpType == 'Device')
			updateAssetInfo(source, rackOrChassisId, roomId, location, position, forWhom, isBlade);

	}
	// Private method used by showAssetCreateView
	function fetchAssetCreateView(controller, fieldHelpType, source, rackOrChassisId, roomId, location, position, isBlade) {
		var url = tdsCommon.createAppURL('/' + controller + '/create');
		jQuery.ajax({
			url: url,
			type: 'POST',
			data: {
				initialAssetType: (isBlade ? 'Blade' : '')
			},
			success: function (resp) {
				if (typeof resp === 'object') {
					if (resp.status == 'error') {
						alert("The following error occurred: " + resp.errors);
						return false;
					}
					console.log("fetchAssetCreateView() had unexpected response");
					return false;
				}
				// Load the edit entity view
				return presentAssetCreateView(resp, fieldHelpType, source, rackOrChassisId, roomId, location, position, isBlade);
			},
			error: function (jqXHR, textStatus, errorThrown) {
				var err = jqXHR.responseText;
				alert("An error occurred while loading the asset show view." + err.substring(err.indexOf("<span>") + 6, err.indexOf("</span>")));
				return false;
			}
		});
	};
	// Called from the page to popup the Asset Entity Create dialog
	pub.showAssetCreateView = function (assetClass, source, rackOrChassisId, roomId, location, position, isBlade) {
		assetCreateInvoked = false;
		switch (assetClass) {
			case "APPLICATION":
				return fetchAssetCreateView('application', 'Application', source, rackOrChassisId, roomId, location, position, isBlade);
				break;
			case "DATABASE":
				return fetchAssetCreateView('database', 'Database', source, rackOrChassisId, roomId, location, position, isBlade);
				break;
			case "STORAGE":
				return fetchAssetCreateView('files', 'Logical Storage', source, rackOrChassisId, roomId, location, position, isBlade);
				break;
			case "DEVICE":
				return fetchAssetCreateView('assetEntity', 'Device', source, rackOrChassisId, roomId, location, position, isBlade);
				break;
			default:
				alert("Error in EntityCrud.showAssetDetailView() - Unsupported case for assetClass '" + assetClass + "'");
				return false;
		}
	};

	// Used to dynamically add a new dependency row to the dependency formin either support or dependent tables (see _dependent.gsp)
	// @param supportDepend - the dependency type [support|dependent]
	// @param forWhom - used to indicate edit or create? views
	pub.addAssetDependencyRow = function (supportDepend) {
		var addedCounter = $("#" + supportDepend + "Added");
		if (addedCounter.length == 0)
			console.log('EntityCrud.addAssetDependencyRow() unable to locate #' + supportDepend + 'Added');

		var rowNum = addedCounter.val();

		// We tick negatively so that new dependencies have negative id suffix vs actual (positive) ids for existing dependencies
		addedCounter.val(parseInt(rowNum) - 1);

		var fieldSuffix = supportDepend + "_" + rowNum;
		var rowId = 'row_' + supportDepend.substring(0, 1) + '_' + rowNum;

		// Update all of the properties names that are from _dependentAdd.gsp to include the new row information
		var rowData = $("#assetDependencyRow tr").html()
			.replace(/FIELD_SUFFIX/g, fieldSuffix)
			.replace(/ROW_ID/g, rowId);

		// Stuff the new row html into the table
		var dependencyList = $('#' + supportDepend + 'List');
		if (dependencyList.length)
			dependencyList.prepend("<tr id='" + rowId + "'>" + rowData + '</tr>');
		else
			console.log('EntityCrud.addAssetDependencyRow() unable to locate #' + supportDepend + 'List');

		// Initialize some of the newly created form fields
		$("#comment_" + fieldSuffix).val('');
		$("#dep_comment_" + fieldSuffix).val('');
		$("#depComment_" + fieldSuffix).dialog({ autoOpen: false });

		// Update the asset select control
		var assetSelect = $("#asset_" + fieldSuffix);
		if (assetSelect.length) {
			assetSelect.addClass("scrollSelect");
			assetSelect.attr("data-asset-type", $("#entity_" + fieldSuffix).val());
			if (!isIE7OrLesser) {
				pub.assetNameSelect2(assetSelect);
			}
		} else {
			console.log('EntityCrud.addAssetDependencyRow() unable to locate #dep_' + fieldSuffix);
		}

	};

	// Used to delete a row from the support or dependent row in the edit/create dependency lists
	// @param rowDomId - the DOM id for the row in the table
	// @param forWhomId - the supportAdded|dependAdded form variable that contains the counter?
	//pub.deleteAssetDependencyRow = function( rowDomId, forWhomId ) {
	pub.deleteAssetDependencyRow = function (rowDomId) {
		$("#" + rowDomId).remove();

		// If row being deleted is a previousl persisted dependency (suffix > 0) then need to add the id to the delete list
		var id = rowDomId.split('_')[3];
		if (id && parseInt(id) > 0) {
			var deletedDepList = $("#deletedDep");
			var ddVal = deletedDepList.val();
			deletedDepList.val((ddVal ? ddVal + ',' : '') + id);
		}
		// Note that we never tick the addedCounter because if there were a combination of rows added, deleted, added we don't want to duplicate any
	};

	/**
	 * Used to update the selection of the move bundle when the user selects a new asset
	 * @param assetId - the id of the asset the user selected
	 * @param depId
	 */
	pub.updateDependentBundle = function (assetId, assetDomId, assetBundleId) {
		var splittedDep = assetDomId.split("_");
		jQuery.ajax({
			url: tdsCommon.createAppURL('/assetEntity/retrieveChangedBundle'),
			data: { 'assetId': assetId, 'dependentId': splittedDep[2], 'type': splittedDep[1] },
			type: 'POST',
			success: function (resp) {
				$("#moveBundle_" + splittedDep[1] + "_" + splittedDep[2]).val(resp.id);
				pub.changeDependentBundleColor(assetDomId, assetBundleId, resp.id, '');
			}
		});
	};

	pub.changeDependentBundleColor = function (depId, assetId, assetBundleId, status) {
		var splittedDep = depId.split("_");
		var bundleObj = $("#moveBundle_" + splittedDep[1] + "_" + splittedDep[2]);
		var status = status != '' ? status : $("#status_" + splittedDep[1] + "_" + splittedDep[2]).val();
		var assetId = assetId != '' ? assetId : bundleObj.val();
		bundleObj.removeAttr("class").removeAttr("style");

		if (assetId != assetBundleId && status == 'Validated') {
			bundleObj.css('background-color', 'red');
		} else {
			if (status != 'Questioned' && status != 'Validated')
				bundleObj.addClass('dep-Unknown');
			else
				bundleObj.addClass('dep-' + status);
		}
	};

	/**
	 * Used to open the Comment dialog for a specific Asset Dependency
	 * @param dialogId
	 */
	pub.openDepCommentDialog = function (typeAndId) {
		// The typeAndId id is formatted as Type_DependencyID
		var type = typeAndId.split('_')[0];
		var rowNo = typeAndId.split('_')[1];
		var modal = $('#depCommentDialog');

		var suffix = type + '_' + rowNo;
		var hiddenInput = $('input:hidden[name=comment_' + suffix + ']');
		var textarea = $('#depCommentTextarea');
		// Populate the textarea from the hidden field
		if (!hiddenInput.val())
			hiddenInput.val('');
		textarea.val(hiddenInput.val());

		// Save the type/id back so the window know which dependency to update on close
		$('#depCommentType').val(type);
		$('#depCommentRowNo').val(rowNo);

		var depAssetText = 'Unselected';
		var depAssetSelect = $('#asset_' + suffix);
		if (depAssetSelect.length && depAssetSelect.select2('data') != null) {
			if (!isIE7OrLesser) {
				depAssetText = depAssetSelect.select2('data').text;
			} else {
				depAssetText = depAssetSelect.find('option:selected').text();
			}
		}
		var title = 'Comment for ' + depAssetText + ' (' + tdsCommon.capitalize(type) + ')';

		modal.dialog('option', 'width', 'auto');
		modal.dialog('option', 'modal', 'true');
		modal.dialog('option', 'position', 'absolute');
		modal.dialog('option', 'title', title);
		modal.dialog('open');
		textarea.focus();
	};

	/**
	 * Used to update a particular Asset Dependency after the user closes the Dependency Comment modal dialog
	 * @param dialogId - the DOM id of the modal dialog
	 * @param textareaId - the DOM id of the textarea
	 * @param hiddenInputId - the DOM id of the hidden input that is used to pass the comment value as part of the post
	 * @param iconLinkId - the DOM id of the A tag that contains the icon that is changed accordingly
	 */
	pub.onDepCommentDialogClose = function () {
		var textarea = $('#depCommentTextarea');

		if (textarea.val().length > 65535) {
			alert("The length of the comment exceeds the allowed maximum of 65535 characters.")
		} else {
            var modal = $('#depCommentDialog');
            var type = $('#depCommentType').val();
            var rowNo = $('#depCommentRowNo').val();
            var hiddenInput = $('input:hidden[name=comment_' + type + '_' + rowNo + ']')
            hiddenInput.val(textarea.val());
            modal.dialog('close');
            // Update the icon based on if there is content
            var iconMode = hiddenInput.val() ? 'edit' : 'add';
            $('#commLink_' + type + '_' + rowNo).html('<img border="0px" src="' + tdsCommon.createAppURL('/icons/comment_' + iconMode + '.png">'));
		}


	};

	/**
	 * Used to close the Asset Dependency comment without updating the input hidden field
	 * @param dialogId - the DOM id of the modal dialog
	 * @param textareaId - the DOM id of the textarea
	 */
	pub.onDepCommentDialogCancel = function () {
		var modal = $('#depCommentDialog');
		var textarea = $('#depCommentTextarea');
		modal.dialog('close');
		textarea.val('');
	};


	//
	// Return the pub object to make available to the public
	//
	return pub;

})(jQuery); //passed 'jQuery' global variable into local parameter '$'


//
// Older global functions
//

var title = document.title;

function changeDocTitle(newTitle) {
	$(document).attr('title', newTitle);
	$(document).keyup(function (e) {
		if (e.keyCode == 27) {
			$(document).attr('title', title);
		}
	});
	$(".ui-dialog .ui-dialog-titlebar-close").click(function () {
		$(document).attr('title', title);
		if (typeof timerBar !== 'undefined')
			timerBar.attemptResume();
	});
	$("#deps").tooltip({
		position: {
			my: "center bottom-20",
			at: "center top",
			using: function (position, feedback) {
				$(this).css(position);
				$("<div>")
					.addClass("arrow")
					.addClass(feedback.vertical)
					.addClass(feedback.horizontal)
					.appendTo(this);
			}
		}
	});
}

function isValidDate(date) {
	var returnVal = true;
	var objRegExp = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d$/;
	if (date && !objRegExp.test(date)) {
		alert("Date should be in 'mm/dd/yyyy HH:MM AM/PM' format");
		returnVal = false;
	}
	return returnVal;
}

function updateAssetTitle(type) {
	EntityCrud.getCreateModal().dialog("option", "title", type + ' Create');
	EntityCrud.getShowModal().dialog("option", "title", type + ' Detail');
	EntityCrud.getEditModal().dialog("option", "title", type + ' Edit');
    EntityCrud.getCreateCloneModal().dialog("option", "title", 'Clone ' + type);
}

function showManufacView(e, forWhom) {
	var resp = e.responseText;
	if (forWhom == 'Edit')
		$("#manufacturerEditId").html(resp);
	else
		$("#manufacturerCreateId").html(resp);

	$("#manufacturers").removeAttr("multiple")
	if (!isIE7OrLesser)
		$("select.assetSelect").select2()
}


function showModelView(e, forWhom) {
	var resp = e.responseText;
	$("#model" + forWhom + "Id").html(resp);
	$("#models").removeAttr("multiple")
	if (forWhom == "assetAudit") {
		$("#models").attr("onChange", "editModelAudit(this.value)")
	}
	if (!isIE7OrLesser) {
		$("select.assetSelect").select2()
	}
}

function showEditDeviceViewFromAudit(assetClass, entityId) {
	closeEditAuditView();
	EntityCrud.showAssetEditView(assetClass, entityId);
}

//DEPRECATED
function showComment(commentId, action, commentType) {
	console.log('DEPRECATED: showComment');
	var id = id
	var objDom = $('[ng-app]');
	var injector = angular.element(objDom).injector();
	injector.invoke(function ($rootScope, commentUtils) {
		if (action == 'edit') {
			$rootScope.$broadcast('editComment', commentUtils.commentTO(commentId, commentType));
		} else {
			$rootScope.$broadcast('viewComment', commentUtils.commentTO(commentId, commentType), 'show');
		}
	});
}

function submitRemoteForm() {
	jQuery.ajax({
		url: $('#editAssetsFormId').attr('action'),
		data: $('#editAssetsFormId').serialize(),
		type: 'POST',
		success: function (data) {
			var assetName = $("#assetName").val();
			$('#items1').html(data);
			$("#messageId").html("Entity " + assetName + " Updated.");
			$("#messageId").show();
			EntityCrud.closeEditModal();
		},
		error: function (jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while updating asset.")
		}
	});
	return false;
}
function deleteAsset(id, value) {
	var redirectTo = 'dependencyConsole'
	if (value == 'server') {
		new Ajax.Request(contextPath + '/assetEntity/delete?id=' + id + '&dstPath=' + redirectTo, {
			asynchronous: true, evalScripts: true,
			onComplete: function (data) {
				EntityCrud.closeEditModal();
				EntityCrud.closeShowModal();
				$('#items1').html(data.responseText);
			}
		})
	} else if (value == 'app') {
		new Ajax.Request(contextPath + '/application/delete?id=' + id + '&dstPath=' + redirectTo, {
			asynchronous: true, evalScripts: true,
			onComplete: function (data) {
				EntityCrud.closeEditModal();
				EntityCrud.closeShowModal();
				$('#items1').html(data.responseText);
			}
		})
	} else if (value == 'database') {
		new Ajax.Request(contextPath + '/database/delete?id=' + id + '&dstPath=' + redirectTo, {
			asynchronous: true, evalScripts: true,
			onComplete: function (data) {
				EntityCrud.closeEditModal();
				EntityCrud.closeShowModal();
				$('#items1').html(data.responseText);
			}
		})
	} else {
		new Ajax.Request(contextPath + '/files/delete?id=' + id + '&dstPath=' + redirectTo, {
			asynchronous: true, evalScripts: true,
			onComplete: function (data) {
				EntityCrud.closeEditModal();
				EntityCrud.closeShowModal();
				$('#items1').html(data.responseText);
			}
		})
	}

}
function reloadDependencyGroupsSection() {
	var moveBundleId = $("#planningBundleSelectId").val();
	var processTab = jQuery('#processDiv');
	processTab.attr("style", "display:block");
	processTab.attr("style", "margin-left: 180px");
	var assetTab = jQuery('#dependencyTableId');
	assetTab.attr("style", "display:none");
	assetTab.attr("style", "display:none");
	jQuery('#items1').css("display", "none");
	$('#upArrow').css('display', 'none');

	// Partially Remove Prototype
	if(window.Prototype) {
		delete Object.prototype.toJSON;
		delete Array.prototype.toJSON;
		delete Hash.prototype.toJSON;
		delete String.prototype.toJSON;
	}

	var postData = {
		bundle: $('#planningBundleSelectId').val(),
		tagIds: getTagsIds($('#tmHighlightGroupSelector').find("#asset-tag-selector").data("kendoMultiSelect").dataItems()),
		tagMatch: $('#tmHighlightGroupSelector').find(".asset-tag-selector-operator-switch").attr('checked') ? 'ALL' : 'ANY',
		assignedGroup: ($('#assignedGroupCB').is(':checked'))? 1:0
	}

	$.ajax({
		type: "POST",
		data: JSON.stringify(postData),
		contentType : 'application/json; charset=utf-8',
		dataType : "JSON",
		url: contextPath + '/moveBundle/dependencyBundleDetails',
		complete: function (jqXHR) {
			$('#dependencyBundleDetailGroupContainer').html(jqXHR.responseText)
			var processTab = jQuery('#processDiv');
			processTab.attr("style", "display:none");
			$('#upArrow').css('display', 'inline');
			$('#downArrow').css('display', 'none');
            setTimeout( function () {
                $('#dependencyDivId').css('overflow-x', 'scroll');
            }, 200);
		}
	});
}
function submitCheckBox() {
	if ($(".checkboxdiv_control input:checked").length == 0) {
		alert("One or more status values must be selected.")
		return
	}
	var moveBundleId = $("#planningBundleSelectId").val();
	var items = $('#checkBoxForm').serialize() + "&bundle=" + moveBundleId;

	var assetTab = jQuery('#dependencyTableId');
	assetTab.attr("style", "display:none");
	jQuery('#items1').css("display", "none");
	$('#upArrow').css('display', 'none')
	hideDependencyControlDiv();

	$.post(contextPath + '/moveBundle/generateDependency', items, function (data) {
		var progressBar = tds.ui.progressBar(data.data.key, 5000,
			function () {
				var assetTab = jQuery('#dependencyBundleDetailsId');
				assetTab.attr("style", "display:block");
				$('#upArrow').css('display', 'inline');
				$('#downArrow').css('display', 'none');
				reloadDependencyGroupsSection()
			}, function () {
				location.reload();
			},
			"Generating Dependency Groups");
	});

}
var isFirst = true;
function selectAll() {
	var totalCheck = $("input[name=checkBox]");
	if ($('#selectId').is(":checked")) {
		for (i = 0; i < totalCheck.size(); i++) {
			totalCheck[i].checked = true;
		}
		isFirst = false;
	} else {
		for (i = 0; i < totalCheck.size(); i++) {
			totalCheck[i].checked = false;
		}
		isFirst = true;
	}
}
function changeMoveBundle(assetType, totalAsset, assignBundle, tagIds) {
	if (!assignBundle) {
		$("#saveBundleId").attr("disabled", "disabled");
	}

	var assetArr = new Array();
	var j = 0;

	for (i = 0; i < totalAsset.size(); i++) {
		if ($('#checkId_' + totalAsset[i]) != null) {
			var booCheck = $('#checkId_' + totalAsset[i]).is(':checked');
			if (booCheck) {
				assetArr[j] = totalAsset[i];
				j++;
			}
		}
	}

	if (j == 0) {
		alert('Please select the Asset');
	} else {
		$('#plannedMoveBundleList').val(assignBundle);
		$('#bundleSession').val(assignBundle);
		$('#assetsTypeId').val(assetType);

		for (var x = 0; x < assetArr.size(); x++) {
			$('#changeBundle').append('<input type="hidden" class="tag-hidden-val" name="assets" value="' + assetArr[x] + '" />');
		}

		var objDom = $('[ng-app]');
		var injector = angular.element(objDom).injector();
		injector.invoke(function ($rootScope, commentUtils) {
			$rootScope.$broadcast('sessionAssignmentTagSelector', tagIds);
			recompileDOM('tmAssignmentTagSelector');
		});

		$('#moveBundleSelectId').dialog('open')
	}
}
function submitMoveForm() {

	$.ajax({
		type: "POST",
		data: $('#changeBundle').serialize(),
		url: $('#changeBundle').attr('action'),
		complete: function (jqXHR) {
			$('#changeBundle').children('input[name="assets"]').remove();
			$('#moveBundleSelectId').dialog("close");
			$('#items1').html(jqXHR.responseText);
			$('#allBundles').attr('checked', 'false');
			$('#planningBundle').attr('checked', 'true');
			$("#plannedMoveBundleList").html($("#moveBundleList_planning").html())
		}
	});
}

function updateToRefresh() {
	jQuery.ajax({
		url: $('#editAssetsFormId').attr('action'),
		data: $('#editAssetsFormId').serialize(),
		type: 'POST',
		success: function (data) {
			EntityCrud.closeEditModal();
			$("#taskMessageDiv").html(data)
			$("#taskMessageDiv").show()
			loadGrid();
		}
	});
}

function selectAllAssets() {
	$('#deleteAsset').attr('disabled', false)
	var totalCheck = document.getElementsByName('assetCheckBox');
	if ($('#selectAssetId').is(":checked")) {
		for (i = 0; i < totalCheck.length; i++) {
			totalCheck[i].checked = true;
		}
		isFirst = false;
	} else {
		for (i = 0; i < totalCheck.length; i++) {
			totalCheck[i].checked = false;
			$('#deleteAsset').attr('disabled', true)
		}
		isFirst = true;
	}
}

function deleteAssets(action) {
	var assetArr = new Array();
	$(".cbox:checkbox:checked").each(function () {
		var assetId = $(this).attr('id').split("_")[2]
		if (assetId)
			assetArr.push(assetId)
	})
	if (!assetArr) {
		alert('Please select the Asset');
	} else {
		if (confirm("You are about to delete all of the selected assets for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel.")) {
			jQuery.ajax({
				url: tdsCommon.createAppURL('/assetEntity/deleteBulkAsset'),
				data: { 'assetLists': assetArr, 'type': action },
				type: 'POST',
				success: function (data) {
					$(".ui-icon-refresh").click();
					$("#messageId").show();
					$("#messageId").html(data.resp);
					$('#deleteAssetId').attr('disabled', true)
				}
			});
		}
	}
}

function bulkDeleteDependencies() {
    var dependencyArr = new Array();
    $(".cbox:checkbox:checked").each(function () {
        var depId = $(this).attr('id').split("_")[2]
        if (depId)
            dependencyArr.push(depId)
    })
    if (!dependencyArr) {
        alert('Please select a dependency');
    } else {
        if (confirm("You are about to delete all of the selected dependencies for which there is no undo. Are you sure? Click OK to delete otherwise press Cancel.")) {
            jQuery.ajax({
                url: tdsCommon.createAppURL('/wsAsset/bulkDeleteDependencies'),
                data: { 'dependencyIds': dependencyArr },
                type: 'POST',
                success: function (data) {
                    $(".ui-icon-refresh").click();
                    $("#messageId").show();
                    $("#messageId").html(data.resp);
                    $('#deleteAssetId').attr('disabled', true)
                }
            });
        }
    }
}

function enableButton(list) {
	var assetArr = new Array();
	var j = 0;
	for (i = 0; i < list.size(); i++) {
		if ($('#checkId_' + list[i]) != null) {
			var booCheck = $('#checkId_' + list[i]).is(':checked');
			if (booCheck) {
				assetArr[j] = list[i];
				j++;
			}
		}
	} if (j == 0) {
		$('#deleteAsset').attr('disabled', true)
	} else {
		$('#deleteAsset').attr('disabled', false)
	}
}

function getAuditDetails(redirectTo, assetType, value) {
	new Ajax.Request(contextPath + '/assetEntity/show?id=' + value + '&redirectTo=' + redirectTo, {
		asynchronous: true, evalScripts: true,
		onComplete: function (e) {
			$("#auditDetailViewId").html(e.responseText)
			$("#auditDetailViewId").show()
		}
	}
	)
}

function closeEditAuditView() {
	$("#auditDetailViewId").html('')
	$("#auditDetailViewId").hide()
}

function editAudit(redirectTo, source, assetType, value) {
	new Ajax.Request(contextPath + '/assetEntity/edit?id=' + value + '&redirectTo=' + redirectTo + '&source=' + source + '&assetType=' + assetType,
		{
			asynchronous: true, evalScripts: true,
			onComplete: function (e) {
				$("#auditDetailViewId").html(e.responseText)
				if (source == 0) {
					$("#auditLocationId").attr("name", "targetLocation")
					$("#auditRoomId").attr("name", "targetRoom")
					$("#auditRackId").attr("name", "targetRack")
					$("#auditPosId").attr("name", "targetRackPosition")
				}
				$("#auditDetailViewId").show()
			}
		}
	)
}

function updateAudit() {
	jQuery.ajax({
		url: $('#editAssetsAuditFormId').attr('action'),
		data: $('#editAssetsAuditFormId').serialize(),
		type: 'POST',
		success: function (data) {
			if (data.errMsg) {
				alert(data.errMsg)
			} else {
				getRackLayout($('#selectedRackId').val())
				$("#auditDetailViewId").html(data)
			}
		}
	});
}

function deleteAudit(id, value) {
	new Ajax.Request(contextPath + '/assetEntity/delete?id=' + id + '&dstPath=assetAudit', {
		asynchronous: true, evalScripts: true,
		onComplete: function (data) {
			$("#auditDetailViewId").hide()
			window.location.reload()
		}
	}
	)
}

function showModelAudit(id) {
	new Ajax.Request(contextPath + '/model/show?id=' + id + '&redirectTo=assetAudit', {
		asynchronous: true, evalScripts: true,
		onComplete: function (data) {
			$("#modelAuditId").html(data.responseText)
			$("#modelAuditId").show()
		}
	}
	)

}

function editModelAudit(val) {
	if (val) {
		var manufacturer = $("#manufacturersAuditId").val()
		new Ajax.Request(contextPath + '/model/retrieveModelDetailsByName?modelName=' + val + '&manufacturerName=' + manufacturer, {
			asynchronous: true, evalScripts: true,
			onComplete: function (data) {
				$("#modelAuditId").html(data.responseText)
				$("#modelAuditId").show()
				$("#autofillIdModel").hide()

			}
		})
	}
}

function updateModelAudit() {
	jQuery.ajax({
		url: $('#modelAuditEdit').attr('action'),
		data: $('#modelAuditEdit').serialize(),
		type: 'POST',
		success: function (data) {
			tds.Alerts.addAlert({ type: 'success', msg: 'Model Updated', closeIn: 1500 });
		}
	});
}

function createAuditPage(type, source, rack, roomId, location, position, locationName, roomName) {
	new Ajax.Request(contextPath + '/assetEntity/create?redirectTo=assetAudit' + '&assetType=' + type + '&source=' + source, {
		asynchronous: true, evalScripts: true,
		onComplete: function (data) {
			$("#auditDetailViewId").html(data.responseText)
			$("#auditDetailViewId").show()
			$("#auditLocationName").val(locationName)
			$("#auditRoomName").val(roomName)
			EntityCrud.populateAssetEditView('Device', source, rack, roomId, location, position, 'create');
		}
	}
	)
}

function createBladeAuditPage(source, blade, position, manufacturer, assetType, assetEntityId, moveBundleId) {
	new Ajax.Request(contextPath + '/assetEntity/create?redirectTo=assetAudit' + '&assetType=' + assetType + '&source=' + source, {
		asynchronous: true, evalScripts: true,
		onComplete: function (data) {
			$("#auditDetailViewId").html(data.responseText)
			$("#BladeChassisId").val(blade)
			$("#bladePositionId").val(position)
			$("#assetTypeCreateId").val(assetType)
			$("#moveBundleId").val(moveBundleId)
			$("#sourceId").val(source)
			$(".bladeLabel").show()
			$(".rackLabel").hide()
			$("#auditDetailViewId").show()
		}
	}
	)
}

function saveAuditPref(val, id) {
	new Ajax.Request(contextPath + '/room/show?id=' + id + '&auditView=' + val, {
		asynchronous: true, evalScripts: true,
		onComplete: function (data) {
			openRoomView(data)
		}
	}
	)
}

var manuLoadRequest
var modelLoadRequest

function getAlikeManu(val) {
	if (manuLoadRequest) manuLoadRequest.abort();
	manuLoadRequest = jQuery.ajax({
		url: tdsCommon.createAppURL('/manufacturer/autoCompleteManufacturer'),
		data: { 'value': val },
		type: 'POST',
		success: function (data) {
			$("#autofillId").html(data)
			$("#autofillId").show()
		}
	});

}

function getAlikeModel(val) {
	if (modelLoadRequest) modelLoadRequest.abort()
	var manufacturer = $("#manufacturersAuditId").val()
	modelLoadRequest = jQuery.ajax({
		url: tdsCommon.createAppURL('/model/autoCompleteModel'),
		data: { 'value': val, 'manufacturer': manufacturer },
		type: 'POST',
		success: function (data) {
			$("#autofillIdModel").html(data)
			$("#autofillIdModel").show()
		}
	});
}
function updateManu(name) {
	$("#manufacturersAuditId").val(name)
	$("#autofillId").hide()
	$("#modelsAuditId").val("")
}

function updateModelForAudit(name) {
	$("#modelsAuditId").val(name)
	$("#modelsAuditId").focus()
	$("#autofillIdModel").hide()
	$("#modelsAuditId").attr('onBlur', 'getAssetType("' + name + '")')
}

function getAssetType(val) {
	new Ajax.Request(contextPath + '/model/retrieveModelType?value=' + val, {
		asynchronous: true, evalScripts: true,
		onComplete: function (data) {
			$("#assetTypeEditId").val(data.responseText)
			editModelAudit("" + val + "")
		}
	}
	)
}

function setType(id, forWhom) {
	new Ajax.Request(contextPath + '/assetEntity/retrieveAssetModelType?id=' + id, {
		asynchronous: true, evalScripts: true,
		onComplete: function (data) {
			$("#assetType" + forWhom + "Id").val(data.responseText)
			if (!isIE7OrLesser)
				$("select.assetSelect").select2()
			EntityCrud.toggleAssetTypeFields(data.responseText)
		}
	}
	)
}

function populateDependency(assetId, whom, thisDialog) {
	$(".updateDep").attr('disabled', 'disabled')
	jQuery.ajax({
		url: tdsCommon.createAppURL('/assetEntity/populateDependency'),
		data: { 'id': assetId, 'whom': thisDialog },
		type: 'POST',
		success: function (data) {
			$("#" + whom + "DependentId").html(data)
			$(".updateDep").removeAttr('disabled')
			if (!isIE7OrLesser)
				$("select.assetSelect").select2();
		},
		error: function (jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while populating dependent asset.")
		}
	});
}

/*function updateModel(rackId,value){
	var val = value;
	new Ajax.Request('contextPath+/assetEntity/retrieveModelsList?='+val,{asynchronous:true,evalScripts:true,onComplete:function(e){populateModelSelect(e,rackId);}})
}
function populateModelSelect(e,rackId){
    var resp = e.responseText;
    resp = resp.replace("model.id","model_"+rackId+"").replace("Unassigned","Select Model")
    $("#modelSpan_"+rackId).html(resp);
}*/

var getTagsIds = function(tags) {
	var tagIds = [];
	tags.each( function(a){
		tagIds.push(parseInt(a.id));
	})
	return tagIds;
};

function onDependencyFiltersChange() {
	reloadDependencyGroupsSection();
};

function showDependencyControlDiv() {
	$("#checkBoxDiv").dialog('option', 'width', '480px')
	$("#checkBoxDiv").dialog('option', 'modal', 'true');
	$("#checkBoxDiv").dialog('option', 'position', ['center', 'top']);
	$("#checkBoxDiv").dialog('open')
	$("#checkBoxDivId").show();
}

function hideDependencyControlDiv() {
	$("#checkBoxDiv").dialog('close');
}



/**
 * function is used to make hard assgined check box enabled - disabled based on criteria
 * @param value : value of select
 * @param gid : id of select
 */
function changeHard(value, gid) {
	if (value.indexOf('@') == 0) {
		$("#" + gid + "Fixed").removeAttr("checked").attr("disabled", "disabled").val(0);
	} else {
		$("#" + gid + "Fixed").removeAttr("disabled");
	}
}

function shufflePerson(sFrom, sTo) {
	var sFromVal = $("#" + sFrom).val()
	var sToVal = $("#" + sTo).val()
	if (sFromVal != '0' && sToVal != '0') {
		$("#" + sFrom).val(sToVal)
		$("#" + sTo).val(sFromVal)
		if (!isIE7OrLesser)
			$("select.assetSelect").select2();
	}
}

$(document).ready(function () {
	$(window).keydown(function (event) {
		if (event.keyCode == 13) {
			if (event.srcElement && event.srcElement.type == "textarea") {
				return;
			}
			event.preventDefault();
			var activeSup = $('[id^=depComment_support_]:visible')
			var activeDep = $('[id^=depComment_dependent_]:visible')
			// NOTE : Order of the condition is MOST important as different div's open on another div
			if (activeSup.find(".save").length > 0) {
				activeSup.find(".save").click()
				$('.ui-dialog').focus()
			} else if (activeDep.find(".save").length > 0) {
				activeDep.find(".save").click()
				$('.ui-dialog').focus()
			} else if ($("#updateCloseId").length > 0) {
				$("#updateCloseId").click();
			} else if ($("#updatedId").length > 0) {
				$("#updatedId").click();
			}

		}
		$("[id^=gs_]").keydown(function (event) {
			$(".clearFilterId").removeAttr("disabled");
		});

	});
});

function toogleRack(value, source) {
	if (value == '-1')
		$(".newRack" + source).show()
	else
		$(".newRack" + source).hide()
}

function changeBundleSelect() {
	if ($("#plannedMoveBundleList").val()) {
		$("#saveBundleId").removeAttr("disabled");
	} else {
		$("#saveBundleId").attr("disabled", "disabled");
	}
}
function setColumnAssetPref(value, key, type) {
	jQuery.ajax({
		url: tdsCommon.createAppURL('/application/columnAssetPref'),
		data: { 'columnValue': value, 'from': key, 'previousValue': $("#previousValue_" + key).val(), 'type': type },
		type: 'POST',
		success: function (resp) {
			// console.log('success');
			if (resp) {
				if (type != 'Task_Columns')
					window.location.reload()
				else
					submitForm()
			}
		}
	});
}
var columnPref = ''
function showSelect(column, type, key) {
	if (column != columnPref) {
		$("#" + type + "IdGrid_" + column).append($("#columnCustomDiv_" + column).html());
	}
	$(".columnDiv_" + key).show();
	columnPref = column
}

// TODO : JPM 10/2014 : This click function should ONLY be applied to certain pages. What is it for?
$(document).click(function (e) {
	var customizeCount = $("#customizeFieldCount").val()
	if (!customizeCount)
		customizeCount = 5;

	for (var i = 1; i <= customizeCount; i++) {
		if ($(".columnDiv_" + i + ":visible").length) {
			if (!$(e.target).is(".editSelectimage_" + i)) {
				$(".columnDiv_" + i).hide();
			}
		}
	}
});

var lastScroll = 0;
$(window).scroll(function (event) {
	//Sets the current scroll position
	var st = $(this).scrollTop();
	//Determines up-or-down scrolling
	if (st > lastScroll) {
		//Replace this with your function call for downward-scrolling
		$(".customScroll").hide();
	}
	else {
		//Replace this with your function call for upward-scrolling
		$(".customScroll").hide();
	}
	//Updates scroll position
	lastScroll = st;
});

function toggleJustPlanning($me) {
	var isChecked = $me.is(":checked")
	jQuery.ajax({
		url: tdsCommon.createAppURL('/ws/user/preference'),
		contentType: "application/json",
		dataType: 'json',
		data: JSON.stringify({ 'value': isChecked, 'code': 'ASSET_JUST_PLANNING' }),
		type: 'POST',
		success: function (data) {
			window.location.reload()
		}
	});
}
function clearFilter(gridId) {
	$("[id^=gs_]").val('');
	var data = new Object();
	$("[id^=gs_]").each(function () {
		data[$(this).attr("name")] = '';//{assetName='',appOwner:'',environment:'',....}
	});
    // When clearing out the filters, also clear the filter param.
    data["filter"] = ''
	$("#" + gridId + "Grid").setGridParam({ postData: data });
	$('.ui-icon-refresh').click();
	$(".clearFilterId").attr("disabled", "disabled");
}
function updateAssetInfo(source, rackOrBladeId, roomId, location, position, forWhom, isBlade) {
	var target = source != '1' ? 'target' : 'source'
	var type = source != '1' ? 'T' : 'S'
	var roomType = source != '1' ? 'Target' : 'Source'

	$("#" + target + "LocationId").val(location)
	$("#roomSelect" + type).val(roomId)
	if (isBlade) {
		$('#deviceChassisId' + type).val(rackOrBladeId);
		$('#' + target + 'BladePositionId').val(position);
		EntityCrud.fetchChassisSelectForRoom(roomId, type, forWhom);
		EntityCrud.toggleAssetTypeFields('Blade');
	} else {
		$("#" + target + "RackPositionId").val(position)
		$('#deviceRackId' + type).val(rackOrBladeId);
		EntityCrud.fetchRackSelectForRoom(roomId, type, forWhom);
	}

	if (!isIE7OrLesser)
		$("select.assetSelect").select2();
}

function updateAuditModelPanel(selectedModel) {
	var valid = ((selectedModel != null) && (selectedModel.id != null) && (selectedModel.id != ''));
	if (selectedModel != null) {
		$('#modelAuditPanel_usize').val(selectedModel.usize);
		$('#modelAuditPanel_modelName').html(selectedModel.text);
		$('#modelAuditPanel_manufacturerName').html(selectedModel.manufacturerName);
		$('#modelAuditPanel_updateModelId').val(selectedModel.id);
		$('#modelAuditPanel_editModelId').val(selectedModel.id);
	} else {
		$('#modelAuditPanel_usize').val('');
		$('#modelAuditPanel_modelName').html('');
		$('#modelAuditPanel_manufacturerName').html('');
		$('#modelAuditPanel_updateModelId').val('');
		$('#modelAuditPanel_editModelId').val('');
	}
	if (valid) {
		$('#modelAuditPanel').show();
	} else {
		$('#modelAuditPanel').hide();
	}
}


/*
 * This function executes an alternative matcher for the select2-input.
 * It will try to find exact matches for the first words and a partial
 * match for the last.
 *
 * The search is case-insensitive.
 *
 * @param userInput: user's input
 * @param dropdownElement: current element in the dropdown to be compared.
 *
 * @return true: they match, false: otherwise.
 */
function matchStart (userInput, dropdownElement) {

	// Checks the input is not empty.
	if(userInput && userInput.trim().length > 0){
		// Splits the dropdown element into an array of strings.
		var dropdownElementTokens = dropdownElement.toUpperCase().trim().split(/\W+/)
		// Splits the user's input into an array of strings.
		var inputTokens = userInput.toUpperCase().trim().split(/\W+/)

		// Determines how many exact matches should be found.
		var exactMatches = inputTokens.length - 1
		// Iterates over the first words looking for exact matches.
		for (var i = 0; i < exactMatches; i++) {
			// Determines if there's any match for the current word.
			var index = dropdownElementTokens.indexOf(inputTokens[i])
			// Checks if there was a match.
			if (index > -1){
				// Removes the current match to avoid duplicates.
				dropdownElementTokens.splice(index, 1)
			} else {
				// If there was no match for any of the words in the user's input, we discard the current word.
				return false
			}
		}

			// The last word in the user's input.
		var partialMatch = inputTokens[exactMatches]

		// Iterate over the remaining words.
		for (var t = 0; t < dropdownElementTokens.length; t++) {
			// Check if the last words matches the beginning of the current word.
			if (dropdownElementTokens[t].indexOf(partialMatch) == 0) {
				return true
			}
		}
		// We exhausted all remaining words and we didn't find a match.
		return false
	} else {
		// If the user's input is empty, all elements in the dropdown are still valid.
		return true
	}


}

