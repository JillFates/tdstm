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
		
		var params = {'personId':personId, 'val':val, 'roleType':roleType, 'eventId':eventId }

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
			function(response){
				alerts.addAlert({type: 'danger', msg: 'Error: ' + "An unexpected error occurred while attempting to update Person's MoveEvent "});
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


			},
			function(response){
				alerts.addAlert({type: 'danger', msg: 'Error: ' + "An error occurred loading persons."});
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
 * To open person's general info , Availabilty and TDS utility dialog
 */
function loadPersonDiv(personId,renderPage,redirectTo){
	jQuery.ajax({
		url : contextPath+'/person/loadGeneral',
		data : {
			'personId' : personId,'tab':renderPage
		},
		type : 'POST',
		success : function(data) {
			if(redirectTo == 'edit'){
				currentTabShow = currentTabShow.replace('Show','Edit')
				currentHeaderShow = currentHeaderShow.replace('Show','Edit')
			}
			$("#personGeneralViewId").html(data)
			$("#personGeneralViewId").dialog('option', 'width', '420px')
			$("#personGeneralViewId").dialog('option', 'position', ['center','top']);
			$("#"+currentTabShow).show()
			$(".mobmenu").removeClass("mobselect")
			$("#"+currentHeaderShow).addClass("mobselect")
			$("#personGeneralViewId").dialog('open');
		}
	});
	
}
function switchTab(id,divId,header){
	$(".person").hide()
	currentTabShow = divId
	currentHeaderShow = header
	$(".mobmenu").removeClass("mobselect")
	$("#"+currentHeaderShow).addClass("mobselect")
	$("#"+currentTabShow).show()
}

/*
 * to make a Ajax call to update person info.
 */
function updatePerson(tab,form){
	var validate = validatePersonForm(form)
	if(validate) {
		var params = $('#'+form).serialize()
		params+= "&tab=" + tab;
		jQuery.ajax({
			url:$('#'+form).attr('action'),
			data: params,
			type:'POST',
			success: function(data) {
				$('#personGeneralViewId').html(data)
				currentTabShow = currentTabShow.replace('Edit','Show')
				currentHeaderShow = currentHeaderShow.replace('Edit','Show')
				$(".person").hide()
				$("#"+currentTabShow).show()
				$(".mobmenu").removeClass("mobselect")
				$("#"+currentHeaderShow).addClass("mobselect")
			},
			error: function(jqXHR, textStatus, errorThrown) {
				alert("An unexpected error occurred while attempting to update Person ")
			}
		});
	}
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


/* 
 * Whenever a property is changed on the manage project staff list, give it a style to confirm that it has been modified
 */
function toggleChangedStyle (source) {
	if(source.val() == 0)
		source.parent().addClass('uncheckedStaff')
	else
		source.parent().addClass('checkedStaffTemp')
}

/*
 * To Close dialog and set global variable again on default.
 */
function closePersonDiv(divId){
	currentTabShow = "generalInfoShowId"
	currentHeaderShow = "generalShowHeadId"
	$('#'+divId).dialog('close')
}

/**
 * open staff create dialog
 */
function createDialog() {
	$("#createStaffDialog").show()
	$("#createStaffDialog").dialog('option', 'width', 500)
	$("#createStaffDialog").dialog("open")

}

/**
 * Validate person form
 */
function validatePersonForm(form) {
	var emailExp = /^([0-9a-zA-Z]+([_.-]?[0-9a-zA-Z]+)*@[0-9a-zA-Z]+[0-9,a-z,A-Z,.,-]+\.[a-zA-Z]{2,4})+$/
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
 
function openPersonDiv(value, fieldName){
	if(value=="0"){
		$('#createDialogForm')[0].reset()
		$("#createStaffDialog").show()
		$("#createStaffDialog").dialog({ title:"Create Person", width: 500})
		$("#createStaffDialog").dialog("open")
		$("#fieldName").val( fieldName )
	}
}