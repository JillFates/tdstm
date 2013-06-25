function compareOrMerge(){
	var ids = new Array()
	$('.cbox:checkbox:checked').each(function(){
		ids.push(this.id.split("_")[1])
	})
	jQuery.ajax({
		url: contextPath+'/person/compareOrMerge',
		data: {'ids':ids},
		type:'POST',
		success: function(data) {
			$("#showOrMergeId").html(data)
			$("#showOrMergeId").dialog('option', 'width', 'auto')
			$("#showOrMergeId").dialog('option', 'position', ['center','top']);
			$("#showOrMergeId").dialog('open')
		}
	});
}

function mergePerson(){
    var returnStatus =  confirm('This will merge the selected Person');
	if(returnStatus ){
		var targetModelId 
		var modelToMerge
		$('input[name=mergeRadio]:radio:checked').each(function(){
			targetModelId = this.id.split("_")[1]
		})
		if(!targetModelId){
			alert("Please select Target Model")
			return
		}
		$('input[name=mergeRadio]:radio:not(:checked)').each(function(){
			modelToMerge = this.id.split("_")[1]
		})
		
		jQuery.ajax({
			url: contextPath+'/person/mergePerson',
			data: {'toId':targetModelId, 'fromId':modelToMerge},
			type:'POST',
			beforeSend: function(jqXHR){
				$("#showOrMergeId").dialog('close')
				$("#messageId").html($("#spinnerId").html())
				$("#messageId").show()
			},
			success: function(data) {
				$("#spinnerId").hide()
				$("#messageId").html(data)
				window.location.reload();
			},
			error: function(jqXHR, textStatus, errorThrown) {
				$("#spinnerId").hide()
				$("#messageId").hide()
				alert("An unexpected error occurred while attempting to Merge Persons")
			}
			
		});
	} else {
		return false
	}
}