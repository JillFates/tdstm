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
function unCheckAll() {
	$("#allPhase").attr('checked', false);
	loadFilteredStaff();
}
