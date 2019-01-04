/*
 * TDS Project utils
 */

 var Project = function() {

 	var initialPartners = {};
	var companyPartners = [];
	var partnersIdx = 0;
	var activeSelectIdxs = [];
	var activeStaffSelects = [];
	var clientId = '';
	var initialStaffValues = {};

	var loadCompanyPartners = function(data) {
		companyPartners = data;
	}

	var createCompanyPartnersSelect = function(elemId, defaultValue) {
		var selectElem = $(elemId);
		selectElem.select2({
			placeholder: "None",
			width: "75%",
			allowClear: true,
			data: companyPartners
		});
		if (defaultValue) {
			selectElem.select2("val", defaultValue);
		}
	}

	var addPartnerSelect = function(containerId, defaultValue) {
		partnersIdx++;
		activeSelectIdxs.push(partnersIdx);
		var partnerContainerId = "companyPartnerContainer" + partnersIdx;
		var selectContainerId = "companyPartnerSelect" + partnersIdx;
		var content = "<div class='partner_select_row' id='" + partnerContainerId + "'><input id='" + selectContainerId + "' name='projectPartners'>"
		content += "<a href='javascript:Project.deleteCompanyPartner(" + partnersIdx + ");'><span class='clear_filter'> X </span></a></div>"
		$(containerId).append(content);
		createCompanyPartnersSelect("#" + selectContainerId, defaultValue);
	}

	var deleteCompanyPartner = function(partnerIdx) {
		var deletePartner = true,
			resetManager = true;
		if (initialPartners[$("#companyPartnerSelect" + partnerIdx).select2("val")]) {
			deletePartner = confirm("Removing a partner will remove all the partner's staff associated with the project. Click 'Okay' to continue otherwise click 'Cancel'");
		}

		// Do not delete partner if it was not his parent selected.
		if($("#companyPartnerSelect" + partnerIdx).select2("data") != null && $('#projectManagerId').select2('data') != null && $('#projectManagerId').select2('data').id) {
			var selectedDeletePartner = $("#companyPartnerSelect" + partnerIdx).select2("data").text;
			var selectedManager = $('#projectManagerId').select2('data').text;
			resetManager = selectedManager.indexOf(selectedDeletePartner) != -1;
		}

		if (deletePartner) {
			$("#companyPartnerContainer" + partnerIdx).remove();
			var index = activeSelectIdxs.indexOf(partnerIdx);
			if (index > -1) { activeSelectIdxs.splice(index, 1); }
			if(resetManager) {
				resetSelectPartners();
			}
		}
	}

	var validSelectedPartners = function() {
		var ids = selectPartnersIds();
		var valuesSoFar = {};
		for (var i = 0; i < ids.length; ++i) {
			var value = ids[i];
			if ((value == "") || Object.prototype.hasOwnProperty.call(valuesSoFar, value)) {
				alert("There are duplicated partners selected.");
				return false;
			}
			valuesSoFar[value] = true;
		}
		return true;
	}

	var selectPartnersIds = function() {
		var result = [];
		var id;
		for (var i=0; i < activeSelectIdxs.length; i++) {
			id = $("#companyPartnerSelect" + activeSelectIdxs[i]).select2("val")
			if (id && (id != "")) {
				result.push(id);
			}			
		}
		return result;
	}

	var initCompanyPartnersSelects = function(containerId, partners) {
		for (var i=0; i < partners.length; i++) {
			initialPartners[partners[i]] = partners[i];
			addPartnerSelect(containerId, partners[i]);
		}
	}

	 /**
	  * Get the selected client id on int format always.
	  * Sending string will results on a false positive param.
 	  * @returns {Number}
      */
	var getSelectedClientId = function() {
		 if ($("#clientId").select2("data") != null && $("#clientId").select2("data").id) {
		 	clientId = $("#clientId").select2("data").id;
	 	} else if(!clientId) {
			clientId = 0;
		}
		return parseInt(clientId);
	}

	var createStaffSelect = function(containerId, initialValue) {

		$(containerId).select2({
			placeholder: "Please Select",
			width: "75%",
			minimumInputLength: 0,
			allowClear: true,
			ajax: { // instead of writing the function to execute the request we use Select2's convenient helper
				url: tdsCommon.createAppURL('/project/fetchStaffList'),
				dataType: 'json',
				quietMillis: 600,
				type: 'POST',
				params: { // extra parameters that will be passed to ajax
					contentType: "application/json; charset=utf-8",
				},
				data: function (term, page) {
					return JSON.stringify({
						q: term, // search term
						partners: selectPartnersIds(),
						role: 'PROJ_MGR',
						client: getSelectedClientId()
					});
				},
				results: function (data, page) { // parse the results into the format expected by Select2.
					// since we are using custom formatting functions we do not need to alter the remote JSON data
					var results = data.staffList;
					if(!results) { // why the data object returns 6 properties if we are just waiting one?
						results = [];
					}
					return { results: results };
				},
				cache: true
			},
			initSelection : function (element, callback) {
				var value = initialStaffValues[$(element).val()];
				if (value != null) {
					callback(value);	
				}
			},
			//formatResult: repoFormatResult, 
			//formatSelection: repoFormatSelection,  
			dropdownCssClass: "bigdrop", // apply css that makes the dropdown taller
			escapeMarkup: function (m) { return m; } // we do not want to escape markup since we are displaying html in results
		});
		if (initialValue != null) {
			if (initialStaffValues[initialValue.id] == null) {
				initialStaffValues[initialValue.id] = initialValue;	
			}
			$(containerId).select2("val", initialValue.id);
		}
		activeStaffSelects.push(containerId);
	}

	var repoFormatResult = function(data) {
		return data.name;
	}

	var repoFormatSelection = function(data) {
		return data.name;
	}

	var setActiveClientId = function(id) {
		clientId = id;
		resetSelectPartners();
	}

	var resetSelectPartners = function() {
		// Reset all active staff selects
		for (var i=0; i < activeStaffSelects.length; i++) {
			$(activeStaffSelects[i]).select2("val", "")
		}		
	}

	var showTimeZoneSelect = function(timezoneId) {
		new Ajax.Request(tdsCommon.createAppURL('/project/showTimeZoneSelect?timezone=' + $("#" + timezoneId).val()), {asynchronous:true,evalScripts:true,
			onSuccess:function(e){
				var timeZonePopup = $("#timeZoneSelectPopup")
				timeZonePopup.html(e.responseText)
				timeZonePopup.dialog('option', 'width', 'auto');
				timeZonePopup.dialog('option', 'modal', 'true');
				timeZonePopup.dialog("open");
			}
		})
	}

	var setTimeZone = function(selectTimezoneId, timezoneId) {
		$("#" + timezoneId).val($("#" + selectTimezoneId).val());
		$("#timeZoneSelectPopup").html("")
		$('#timeZoneSelectPopup').dialog('close')
	}

	 /*
	  * validate the text area size
	  */
	 function textCounter(fieldId, maxlimit) {
		 var value = $("#"+fieldId).val()
		 if (value.length > maxlimit) { // if too long...trim it!
			 $("#"+fieldId).val(value.substring(0, maxlimit));
			 return false;
		 } else {
			 return true;
		 }
	 }

	return {
		loadCompanyPartners: loadCompanyPartners,
		addPartnerSelect: addPartnerSelect,
		deleteCompanyPartner: deleteCompanyPartner,
		initCompanyPartnersSelects: initCompanyPartnersSelects,
		createStaffSelect: createStaffSelect,
		setActiveClientId: setActiveClientId,
		validSelectedPartners: validSelectedPartners,
		showTimeZoneSelect: showTimeZoneSelect,
		setTimeZone: setTimeZone,
		textCounter: textCounter
	}

 }();
