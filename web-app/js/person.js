function compareOrMerge(){
	var ids = new Array()
	$('.cbox:checkbox:checked').each(function(){
		ids.push(this.id.split("_")[2])
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
		var modelToMerge = new Array()
		$('input[name=mergeRadio]:radio:checked').each(function(){
			targetModelId = this.id.split("_")[1]
		})
		if(!targetModelId){
			alert("Please select Target Model")
			return
		}
		$('input[name=mergeRadio]:radio:not(:checked)').each(function(){
			modelToMerge.push(this.id.split("_")[1])
		})
		var params = {};
		$(".input_"+targetModelId).each(function(){
			if(this.name!='manufacturer' && this.name!='createdBy' && this.name!='updatedBy')
				params[this.name] = this.value;
		})
		params['toId'] = targetModelId;
		params['fromId'] = modelToMerge;
		jQuery.ajax({
			url: contextPath+'/person/mergePerson',
			data: params,
			type:'POST',
			beforeSend: function(jqXHR){
				$("#showOrMergeId").dialog('close')
				$("#messageId").html($("#spinnerId").html())
				$("#messageId").show()
			},
			success: function(data) {
				$("#spinnerId").hide()
				$("#messageId").html(data)
				$(".ui-icon-refresh").click()
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

function switchTarget(targetId){
	
	$(".editAll:visible").children().each(function(){
		var span = $(this).parent().siblings()
		if($(this).attr('type') == 'checkbox'){
			span.children().val(span.children().val())
		}else {
			var toSpan = ''
			$(this).parent().children("input").each(function(i){
				if($(this).siblings().length > 0 && ! $(this).val())
					toSpan = toSpan + 'null'
				else
					toSpan = toSpan + $(this).val()
			})
			if(toSpan.length > 20)
				toSpan = toSpan.substring(0, 20) + '...'
			span.html(toSpan)
		}
	})
	
	$(".editAll").hide()
	$(".showAll").show() 
	$(".editTarget_"+targetId).show()
	$(".showFrom_"+targetId).hide()
	
}