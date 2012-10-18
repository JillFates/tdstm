/*
 * for making and Ajax call to load staff list using filters.  
 */

function loadFilteredStaff() {
	var role = $("#role").val()
	var location = $("#location").val()
	var project = $("#project").val()
	var scale = $("#scale").val()
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
			'phaseArr' : phaseArr
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
	loadFilteredStaff();
}
/*
 * when uncheck any other check box uncheck phase's all check box
 */
function unCheckAll() {
	$("#allPhase").attr('checked', false);
	loadFilteredStaff();
}
/*
 * To open person's general info , Availabilty and TDS utility dialog
 */
function loadPersonDiv(id,tab){
	jQuery.ajax({
		url : '../person/loadGeneral',
		data : {
			'personId' : id,'tab':tab
		},
		type : 'POST',
		success : function(data) {
			
			$("#personGeneralViewId").html(data)
			$("#personGeneralViewId").dialog('option', 'width', 'auto')
			$("#personGeneralViewId").dialog('option', 'height', 'auto')
			$("#personGeneralViewId").dialog('option', 'position', ['center','top']);
			$("#personGeneralViewId").dialog('open');
		}
	});
	
}

/*
 * to make a Ajax call to update person info.
 */
function updatePerson(){
	jQuery.ajax({
		url:$('#personDialogForm').attr('action'),
		data: $('#personDialogForm').serialize(),
		type:'POST',
		success: function(data) {
				$('#personGeneralViewId').dialog('close')
				loadFilteredStaff()
		}
	});
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