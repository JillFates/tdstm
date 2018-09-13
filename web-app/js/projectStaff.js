if(typeof angular != 'undefined'){
tds.staffing = tds.staffing || {};
tds.staffing.controller = tds.staffing.controller || {};


tds.staffing.module = angular.module('tdsProjectStaff', ['tdsCore']);



tds.staffing.controller.MainController = function(scope, http, compile, alerts){

	scope.orderBy = "asc" 

	scope.toggleSortOrder = function(sortOn , firstProp, orderBy, changed){
		var newVal = "asc"
		if(scope.orderBy == "asc"){
			newVal = "desc"
		}
		scope.orderBy = newVal
		scope.loadFilteredStaff(sortOn , firstProp, orderBy, changed)
	}


	scope.toggleCheckbox = function(source, val) {
	  if (source.attr('disabled')) {
	    source.removeAttr("disabled");
	  } else {
	    source.attr("disabled", true);
	  }
	  if(val == 1){

	  	source.parent().removeClass("uncheckedStaff");
	  	source.parent().removeClass("checkedStaff");
	  	source.parent().removeClass("checkedStaffTemp");

	  }else{
	  	source.parent().removeClass("uncheckedStaff");
	  	source.parent().addClass("checkedStaff");
	  }
	};

	scope.rollbackCheboxStatus = function(source, val){
		if(val == 0){
			source.parent().removeClass("checkedStaff");
			source.removeAttr('checked');
		}
		source.removeAttr("disabled");
	}

	scope.saveProjectStaff2 = function($event){
		var source = $($event.target)
		var row = source.parent().parent()
		var val = parseInt(source.val())
		var personId = source.attr('id')
		var roleType = source.parent().siblings('#roleColumnId').attr('title')
		var projectId = $('#project').find('[selected]').val()

		scope.toggleCheckbox(source, val );

		var params = {'personId':personId, 'val':val, 'projectId':projectId, 'roleType':roleType }

		http.post( contextPath+'/person/saveProjectStaff', params).then(
			function(response){
				if(!response.data.data.flag){
					scope.rollbackCheboxStatus(source,val);
				   alerts.addAlert({type: 'danger', msg: 'Error: ' + response.data.data.message});
				}else{
					scope.toggleCheckbox(source, val);
					source.val((val + 1) % 2);	
				}
				
			},
			function(response){
				alerts.addAlert({type: 'danger', msg: 'Error: ' + "An unexpected error occurred while attempting to update Person's MoveEvent "});
			}
		);
	};


	scope.saveEventStaff2 = function($event) {
		var source = $($event.target)
		var row = source.parent().parent()
		var val = parseInt(source.val())
		var eventId = source.parent().attr('id')
		var personId = source.attr('id')
		var roleType = row.find('#roleColumnId').attr('title')

		scope.toggleCheckbox(source, val );
		var project = row.find('#projectColumnId').children('input')
		
		var params = {'personId':personId, 'val':((val + 1) % 2), 'roleType':roleType, 'eventId':eventId }

		http.post( contextPath+'/person/saveEventStaff', params).then(
			function(response){
				if(!response.data.data.flag){
					scope.rollbackCheboxStatus(source,val);
					alerts.addAlert({type: 'danger', msg: 'Error: ' + response.data.data.message});
				}else{
					scope.toggleCheckbox(source, val);
					source.val((val + 1) % 2);		
				}
			},
			function(response) {
				tdsCommon.displayWsError(response, "Error: An unexpected error occurred while attempting to update Person's MoveEvent", alerts);
			}
		);
	};

	scope.loadFilteredStaff = function(sortOn , firstProp, orderBy, changed) {
		var role = $("#role").val()
		var location = $("#location").val()
		var project = $("#project").val()
		var scale = $("#scale").val()
		var onlyClientStaff = $("#clientStaffId").val()
		var assigned = $("#assignedId").val()
		var orderBy = orderBy ? orderBy : scope.orderBy
		var phaseArr = new Array();
		if ($("#allPhase").val() == '1') {
			phaseArr.push("all")
		} else {
			var checked = $("input[name='PhaseCheck']:checked")
			$(checked).each(function() {
				var phaseId = $(this).attr('id')//do stuff here with this
				phaseArr.push(phaseId)
			})
		}
		var data = {
				'role' : role,
				'location' : location,
				'project' : project,
				'scale' : scale,
				'phaseArr' : phaseArr,
				'assigned' :  scope.onlyAssigned,
				'onlyClientStaff' : scope.onlyClientStaff,
				'sortOn':sortOn,
				'firstProp':firstProp,
				'orderBy':orderBy
				
			};
		http.post( contextPath+'/person/loadFilteredStaff', data).then(
			function(response){
				$("#projectStaffTableId").html(compile(response.data)(scope));
				//scope.staffingTablestaffingTable = response.data;
				$("#orderBy").val(orderBy)
				$("#sortOn").val(sortOn)
				$(window).scroll();


			},
			function(response){
				tdsCommon.displayWsError(response, 'Error: An error occurred loading persons.', alerts);
				$(window).scroll();
			}
		);
		
	}

}

tds.staffing.controller.MainController.$inject = ['$scope', '$http', '$compile', 'alerts'];	
}

/*
 * for making and Ajax call to load staff list using filters.  
 */
var currentTabShow = "generalInfoShowId"
var currentHeaderShow = "generalShowHeadId"

function loadFilteredStaff(sortOn , firstProp, orderBy, changed) {

	showSpinner();

	var role = $("#role").val()
	var location = $("#location").val()
	var project = $("#project").val()
	var scale = $("#scale").val()
	var onlyClientStaff = $("#clientStaffId").val()
	var assigned = $("#assignedId").val()
	var orderBy = orderBy ? orderBy : $("#orderBy").val()
	var phaseArr = new Array();
	var eventsOption = $("#eventsOption").val()
	if ($("#allPhase").val() == '1') {
		phaseArr.push("all")
	} else {
		var checked = $("input[name='PhaseCheck']:checked")
		$(checked).each(function() {
			var phaseId = $(this).attr('id')//do stuff here with this
			phaseArr.push(phaseId)
		})
	}
	jQuery.ajax({
		url : contextPath+'/person/loadFilteredStaff',
		data : {
			'role' : role,
			'location' : location,
			'project' : project,
			'scale' : scale,
			'phaseArr' : phaseArr,
			'assigned' : assigned,
			'onlyClientStaff' : onlyClientStaff,
			'sortOn':sortOn,
			'firstProp':firstProp,
			'orderBy':orderBy,
			'eventsOption' : eventsOption
			
		},
		type: 'POST',
		success: function(data) {
			$("#projectStaffTableId").html(data);
			hideSpinner();
		},
		error: function (jqXHR, textStatus, errorThrown) {
			hideSpinner();
			alert("An error occurred (" + errorThrown + ")\nPlease reload the page to confirm if any changes were made.");
		}
	});

}

// Used to hide the process spin
function hideSpinner() {
	$('#spinner').hide();
}
function showSpinner() {
	$('#spinner').show();
}

/*
 * when check  phase's all check box check all other checkboxes .  
 */
function checkAllPhase() {
	if ($("#allPhase").val() == '1') {
		var checked = $("input[name='PhaseCheck']:not(:checked)")
		$(checked).each(function() {
			var phaseId = $(this).attr('id')//do stuff here with this
			$("#" + phaseId).attr('checked', true);
		})
	}
	loadFilteredStaff($("#sortOn").val(),$("#firstProp").val(), scope.orderBy != 'asc' ? 'asc' :'desc');
}
/*
 * when uncheck any other check box uncheck phase's all check box
 */
function unCheckAll() {
	$("#allPhase").attr('checked', false);
	loadFilteredStaff($("#sortOn").val(),$("#firstProp").val(), scope.orderBy != 'asc' ? 'asc' :'desc');
}

/*
 * to Add roles for person
 */

function addFunctions(){
	var selectHtml = $("#availableFuncsId").html().replace("funcToAdd","function")
	var id=$("#maxSize").val()
	$("#funcsTbodyId").append("<tr id='roleTrId_"+id+"'><td> "+ selectHtml +"<a href=\"javascript:deleteFuncsRow(\'roleTrId_"+id+"')\">&nbsp;&nbsp;"+"<span class=\'clear_filter\'>X</span></a> </td></tr><br/>")
	$("#maxSize").val(parseInt(id)+1)
	$("#manageFuncsId").val(parseInt($("#manageFuncsId").val())+1)
}
/*
 * to delete roles for person
 */

function deleteFuncsRow( rowId ){
	$("#"+rowId).remove()
	$("#manageFuncsId").val(parseInt($("#manageFuncsId").val())+1)
}

function changeManageFuncs(){
	$("#manageFuncsId").val(1)
}

/*
 * to add date for which a user won't be available
 */

function addBlackOutDay(){
	var id = $("#availableId").val()
	var inputHtml = $("#dateDivId").html().replace("availId","availId_"+id).replace("available","availability")
	$("#blackOutDay").append("<tr id='roleTrId_"+id+"'><td>"+inputHtml +"<a href=\"javascript:deleteFuncsRow(\'roleTrId_"+id+"')\">&nbsp;&nbsp;"+"<span class=\'clear_filter\'>X</span></a> </td></tr><br/>")
	showCalender("#availId_"+id)//$("#availabilityId_"+id).datepicker();
	$("#availableId").val(parseInt($("#availableId").val())+1)

	
}

// Used to enable/disable an element
function toggleDisabled(source) {
	if (source.is(':disabled')) {
		$(source).removeAttr("disabled");
	} else {
		$(source).attr("disabled", true);
	}
}

/*
 * Make a ajax call when user checks on checkbox for Project to add or remove the project team based on the 
 * source checkbox argument. This will also remove the user from any events associated to the project.
 */
function addRemoveProjectTeam(source, personId, projectId, teamCode) {

	var action = (source.is(':checked') ? 'add' : 'remove');

	// Disable and indicate an action for the checkbox 
	toggleChangedStyle(source);
	toggleDisabled(source);

	var personId = source.attr('id')
	var roleType = source.parent().siblings('#roleColumnId').attr('title')
	
	var params = {'personId':personId, 'projectId':projectId, 'teamCode':teamCode};
	var url = contextPath + '/person/' + action + 'ProjectTeam';
	var errorMsg = '';
	jQuery.ajax({
		url: url,
		data: params,
		type:'POST',
		async: false,
		cache: false,
		sourceElement: source,
		success: function(data) {
			console.log(data);
			if (data.status == 'error') {
				errorMsg = data.errors[0];
				if (data.errors.length > 1)
					console.log("Call to " + url + " failed : " + data.errors[1]);
			}else{
				loadFilteredStaff($("#sortOn").val(),$("#firstProp").val(), $("#orderBy").val() != 'asc' ? 'asc' :'desc' );
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			errorMsg = "An error occurred while attempting to " + action + " person team assignment to project";
			console.log("Error " + textStatus + " : " + errorThrown);
		}
	});

	if (errorMsg.length > 0) {
		alert(errorMsg);
		//revertChange(source);
		toggleDisabled(source);
	}
}



function toggleProjectStaff(source, personId, projectId, teamCode, sourceChecked){
	toggleChangedStyle(source);
	toggleDisabled(source);

	var personId = source.attr('id')
	var personId = personId.replace('staff_person_','')
	var action = sourceChecked ? "add" : "remove"
	var params = {'personId':personId, 'projectId':projectId};
	var url = contextPath + '/person/' + action + 'ProjectStaff';
	var errorMsg = '';
	jQuery.ajax({
		url: url,
		data: params,
		type:'POST',
		async: false,
		cache: false,
		sourceElement: source,
		success: function(data) {
			console.log(data);
			if (data.status == 'error') {
				errorMsg = data.errors[0];
				if (data.errors.length > 1)
					console.log("Call to " + url + " failed : " + data.errors[1]);
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			errorMsg = "An error occurred while attempting to " + action + " person assignment to project";
			console.log("Error " + textStatus + " : " + errorThrown);
		}
	});

	if (errorMsg.length > 0) {
		alert(errorMsg);
		revertChange(source);
		toggleDisabled(source);
	} else {
		loadFilteredStaff($("#sortOn").val(),$("#firstProp").val(), $("#orderBy").val() != 'asc' ? 'asc' :'desc' );
	}
}


/*
 * Make a ajax call when user checks on checkbox for Project to add or remove the project staff based on the 
 * source checkbox argument. This will also remove the user from any events associated to the project.
 */
function togPrjStaff(source, personId, projectId, teamCode){
	var sourceChecked = source.is(':checked');
	if(sourceChecked){
		toggleProjectStaff(source, personId, projectId, teamCode, sourceChecked)
	}else{
		var confirmMsg = "This action will remove the person from all assigned tasks, SME and/or Application Owner references and any Team and Event associations for the project. These changes can not be undone. Please click Confirm to proceed otherwise press Cancel."	
		$("#unselectDialog").html(confirmMsg)
		$("#overlay").css('display', 'inline')
		$("#unselectDialog").dialog({
	      buttons : {
	        "Confirm" : function() {
	         	$("#overlay").css('display', 'none')
				$(this).dialog("close");
	         	toggleProjectStaff(source, personId, projectId, teamCode, sourceChecked)
	          	

	        },
	        "Cancel" : function() {
	        	$("#overlay").css('display', 'none')
	          	$(this).dialog("close");
				toggleChangeChckboxState(source);
	        }
	      }
	    });
	    $("#unselectDialog").dialog("open");
	    $("#unselectDialog").parent().find(".ui-dialog-buttonpane").css('width', 'auto')

	}
}

/**
 * If the user cancel the button, we need to reverte to the original state
 * @param source
 */
function toggleChangeChckboxState(source){
	if(source.is(':checked') ){
		source.prop( "checked", false );
	} else {
		source.prop( "checked", true);
	}
}

/*
 * Make a ajax call when user checks on checkbox for an Event to add or remove the project staff based on the 
 * source checkbox argument. If the user wasn't previously assigned to a project, they will be done automatically.
 */
function togEvtStaff(source, personId, projectId, eventId, teamCode) {
	// Disable and indicate an action for the checkbox 
	toggleChangedStyle(source);
	toggleDisabled(source);

	var action = (source.is(':checked') ? 'add' : 'remove');
	var errorMsg = '';
	
	var params = { 'personId':personId, 'projectId':projectId, 'eventId':eventId, 'teamCode':teamCode };
	var url = contextPath + '/person/' + action + 'EventStaff';
	jQuery.ajax({
		url: url,
		data: params,
		type:'POST',
		async: false,
		cache: false,
		success: function(data) {
			console.log(data);
			if (data.status == 'error') {
				errorMsg = data.errors;
			} else {
				loadFilteredStaff($("#sortOn").val(),$("#firstProp").val(), $("#orderBy").val() != 'asc' ? 'asc' :'desc');
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			errorMsg = "An unexpected error occurred while attempting to update Person's MoveEvent ";
		}
	});

	// Deal with any errors
	if (errorMsg.length > 0) {
		alert(errorMsg);
		revertChange(source);
	}
}

/* 
 * Whenever a property is changed on the manage project staff list, give it a style to confirm that it has been modified
 */
function toggleChangedStyle (source) {
	var cssClass = $(source).is(':checked') ? 'checkedStaffTemp' : 'uncheckedStaff';
	$(source).parent().addClass(cssClass);
}

/**
 * open staff create dialog
 */
function createDialog() {
	var createdDialog = document.getElementById("createDialogForm");
	if(createdDialog) {
		createdDialog.reset();
	}
	$("#createStaffDialog").show();
	$("#createStaffDialog").dialog('option', 'width', 500);
	$("#createStaffDialog").dialog('option', 'modal', 'true');
	$("#createStaffDialog").dialog("open");
}

/**
 * Validate person form
 */
function validatePersonForm(form) {
	var emailExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,63})+$/
	var mobileExp=/^([0-9 +-])+$/
	var returnVal = true
	var allFields = $("form[name = "+form+"] input[type = 'text']");
	
	jQuery.each(allFields , function(i, field) {
		field.value= $.trim(field.value)
	});
	
	var firstName = $(
			"form[name = "+form+"] input[name = 'firstName']")
			.val()
	var email = $(
			"form[name = "+form+"] input[name = 'email']")
			.val()
	var workPhone = $(
			"form[name = "+form+"] input[name = 'workPhone']")
			.val().replace(/[\(\)\.\-\ ]/g, '')
	var mobilePhone = $(
			"form[name = "+form+"] input[name = 'mobilePhone']")
			.val().replace(/[\(\)\.\-\ ]/g, '')
	if (!firstName) {
		alert("First Name should not be blank ")
		returnVal = false
	}
	if (email && !emailExp.test(email)) {
		alert(email + " is not a valid e-mail address ")
		returnVal = false
	}
	if (workPhone && !(mobileExp.test(workPhone))) {
		alert("The Work phone number contains illegal characters.");
		returnVal = false
	}
	if (mobilePhone && !(mobileExp.test(mobilePhone))) {
		alert("The Mobile phone number contains illegal characters.");
		returnVal = false
	}
	return returnVal
}

/**
 * Ajax service function to call the person/save method and then update the select specified by fieldName
 */
function createPersonDetails(forWhom){
	if (validatePersonForm('createDialogForm')) {
		jQuery.ajax({
			url : contextPath+'/person/save',
			data : $('#createDialogForm').serialize(),
			type : 'POST',
			success : function(data) {
				if (data.errMsg) {
					alert(data.errMsg)
				} else {
					$("#createStaffDialog").dialog('close')
					if(!data.isExistingPerson){
						$('select.assetSelect').append('<option value="'+data.id+'">'+data.name+'</option>');
						$('#'+data.fieldName+' option[value="'+data.id+'"]').attr('selected','selected');
						if(!isIE7OrLesser)
							$("select.assetSelect").select2()
					}
					else
						$('#'+data.fieldName).val(data.id)
				}
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred : " + textStatus + " : " + errorThrown)
			}
		});
	}
}
 
function openPersonDiv(value, fieldName){
	if(value=="0"){
		$('#createDialogForm')[0].reset()
		$("#funcsCreateTbodyId" ).empty();
		$("#createStaffDialog").show();
		$("#createStaffDialog").dialog({ title:"Create Person", width: 500});
		$("#createStaffDialog").dialog('option', 'modal', 'true');
		$("#createStaffDialog").dialog("open");
		$("#fieldName").val( fieldName );
	}
}

function toggleSortOrder(sortOn , firstProp){
    var newVal = "asc";
    if($("#orderBy").val() == "asc"){
        newVal = "desc";
    }
    loadFilteredStaff(sortOn , firstProp, newVal);
}
