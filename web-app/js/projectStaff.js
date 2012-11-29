/*
 * for making and Ajax call to load staff list using filters.  
 */
var currentTabShow = "generalInfoShowId"
var currentHeaderShow = "generalShowHeadId"
function loadFilteredStaff(sortOn , firstProp, orderBy) {
	var role = $("#role").val()
	var location = $("#location").val()
	var project = $("#project").val()
	var scale = $("#scale").val()
	var assigned = $("#assignedId").val()
	var orderBy = orderBy ? orderBy : $("#orderBy").val()
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
	jQuery.ajax({
		url : '../person/loadFilteredStaff',
		data : {
			'role' : role,
			'location' : location,
			'project' : project,
			'scale' : scale,
			'phaseArr' : phaseArr,
			'assigned' : assigned,
			'sortOn':sortOn,
			'firstProp':firstProp,
			'orderBy':orderBy
			
		},
		type : 'POST',
		success : function(data) {
			$("#projectStaffTableId").html(data)
		}
	});

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
	loadFilteredStaff($("#sortOn").val(),$("#firstProp").val(), $("#orderBy").val() != 'asc' ? 'asc' :'desc');
}
/*
 * when uncheck any other check box uncheck phase's all check box
 */
function unCheckAll() {
	$("#allPhase").attr('checked', false);
	loadFilteredStaff($("#sortOn").val(),$("#firstProp").val(), $("#orderBy").val() != 'asc' ? 'asc' :'desc');
}
/*
 * To open person's general info , Availabilty and TDS utility dialog
 */
function loadPersonDiv(personId,renderPage,redirectTo){
	jQuery.ajax({
		url : '../person/loadGeneral',
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
			$(".person").hide()
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

function addRoles(){
	var selectHtml = $("#availableRolesId").html().replace("roleToAdd","role")
	var id=$("#maxSize").val()
	$("#rolesTbodyId").append("<tr id='roleTrId_"+id+"'><td> "+ selectHtml +"<a href=\"javascript:deleteRolesRow(\'roleTrId_"+id+"')\">&nbsp;&nbsp;"+"<span class=\'clear_filter\'>X</span></a> </td></tr><br/>")
	$("#maxSize").val(parseInt(id)+1)
	$("#manageRolesId").val(parseInt($("#manageRolesId").val())+1)
}
/*
 * to delete roles for person
 */

function deleteRolesRow( rowId ){
	$("#"+rowId).remove()
	$("#manageRolesId").val(parseInt($("#manageRolesId").val())+1)
}

function changeManageRole(){
	$("#manageRolesId").val(1)
}

/*
 * to add date for which a user won't be available
 */

function addBlackOutDay(){
	var id = $("#availableId").val()
	var inputHtml = $("#dateDivId").html().replace("availId","availId_"+id).replace("available","availability")
	$("#blackOutDay").append("<tr id='roleTrId_"+id+"'><td>"+inputHtml +"<a href=\"javascript:deleteRolesRow(\'dateTrId_"+id+"')\">&nbsp;&nbsp;"+"<span class=\'clear_filter\'>X</span></a> </td></tr><br/>")
	showCalender("#availId_"+id)//$("#availabilityId_"+id).datepicker();
	$("#availableId").val(parseInt($("#availableId").val())+1)

	
}
/*
 * Make a ajax call when user checks on checkbox for moveEvent
 */
function saveEventStaff(id){
	var val = $("[id='"+id+"']").val()
	var params = {'id':id, 'val':val }
	jQuery.ajax({
		url:'../person/saveEventStaff',
		data: params,
		type:'POST',
		success: function(data) {
			if(data=="false"){
			   alert("An unexpected error occurred while attempting to update Person's MoveEvent  ")
			}
			loadFilteredStaff($("#sortOn").val(),$("#firstProp").val(), $("#orderBy").val() != 'asc' ? 'asc' :'desc');
		},
		error: function(jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while attempting to update Person's MoveEvent ")
		}
	});
}
/*
 * Make a ajax call when user checks on checkbox for Project to save project staff
 */
function saveProjectStaff(id){
	var val = $("[id='"+id+"']").val()
	var params = {'id':id, 'val':val }
	jQuery.ajax({
		url:'../person/saveProjectStaff',
		data: params,
		type:'POST',
		success: function(data) {
			if(data=="false"){
			   alert("An unexpected error occurred while attempting to update Person's MoveEvent  ")
			}
			loadFilteredStaff($("#sortOn").val(),$("#firstProp").val(), $("#orderBy").val() != 'asc' ? 'asc' :'desc');
		},
		error: function(jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while attempting to update Person's MoveEvent ")
		}
	});
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
		field.value=field.value.trim()
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

