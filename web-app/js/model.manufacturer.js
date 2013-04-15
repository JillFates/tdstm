/**
 * To add AKA text field to add AKA for model and manufacturer (common method for both)
 */

function addAka(){
	var trId = $("#manageAkaId").val() 
	var spanId = "errSpan_"+trId
	var textHtml = $("#akaDiv").html().replace(/errSpan/g,"errSpan_"+trId)
	$("#addAkaTableId").append("<tr id='akaId_"+trId+"'><td nowrap='nowrap'>"+textHtml+
		"<a href=\"javascript:deleteAkaRow(\'akaId_"+trId+"')\"><span class='clear_filter'><u>X</u></span></a>"+
		"<br><div style='display:none' class='errors' id='errSpan_"+trId+"'></div>"+
		"</td></tr>")
	$("#manageAkaId").val(parseInt(trId)-1)
}
/**
 * Used to delete text field from DOM and if it was persistent the maintain id to send at controller
 * @param id : id of tr to remove .
 * @param save : a flag to make sure whether to maintain deleted id .
 */
function deleteAkaRow(id, save){
	$("#"+id).remove()
	if(save){
		var deletedId = id.split("_")[1]
		$("#deletedAka").val() ? $("#deletedAka").val($("#deletedAka").val()+","+deletedId) : $("#deletedAka").val(deletedId)
	}
}
/**
 * Method to use validate AKA field whether it already exist in DB or not .
 * @param value: aka value
 * @param itemId : model or manufaturerId 
 * @param spanId : span id where to display error message if AKA exist.
 * @param forWhom : to determine which controller we need to send the requst.
 */
function validateAKA(value,itemId,spanId,forWhom){
	var makeAjaxCall = avoidDuplicate(spanId)
	if(makeAjaxCall){
		var params = {'name':value,'id':itemId}
		jQuery.ajax({
			url : $("#contextPath").val()+'/'+forWhom+'/validateAKA',
			data: params,
			complete: function(e) { 
				if(e.responseText){
					$("#"+spanId).html("Duplicate AKA "+e.responseText+" already exist.")
					$("#"+spanId).css('display','block')
				}else{
					$("#"+spanId).html("")
					$("#"+spanId).css('display','none')
				}
				
			}
		});
	}
}
/**
 * 
 * @param spanId : where to show error message
 * @returns {Boolean} 
 */
function avoidDuplicate(spanId){
	var textValues = new Array();
	var flag = true
    $("input.akaValidate").each(function() {
        doesExisit = ($.inArray($(this).val(), textValues) == -1) ? false : true;
        if (!doesExisit) {
            textValues.push($(this).val())
            $("#"+spanId).html("")
			$("#"+spanId).css('display','none')
        } else {
        	$("#"+spanId).html("Duplicate AKA "+$(this).val()+" already Entered.")
			$("#"+spanId).css('display','block')
			flag = false
            return false;
        }
    });
	return flag
}

/**
 * convert values from Amps to Watts OR Watts to Amps
 * @param value
 * @param name
 */
function convertPowerType(value,name){
	
	if(value=="Watts" && name =="powerType"){
		$('#powerUseId').val( $('#powerUseIdH').val() );
		$('#powerNameplateId').val( $('#powerNameplateIdH').val() );
		$('#powerDesignId').val( $('#powerDesignIdH').val() );
	} else if(value=="Amps" && name == "powerType"){
		var preference = $('#powerUseIdH').val()/110;
		$('#powerUseId').val(preference.toFixed(1));

		preference = $('#powerNameplateIdH').val()/110;
		$('#powerNameplateId').val(preference.toFixed(1));

		preference = $('#powerDesignIdH').val()/110;
		$('#powerDesignId').val(preference.toFixed(1));
	}
}

function createModelManuDetails(controllerName,forWhom){
	jQuery.ajax({
		url : $("#contextPath").val()+'/'+controllerName+'/create',
		type : 'POST',
		success : function(data) {
			 $("#create"+forWhom+"View").html(data);
			 $("#create"+forWhom+"View").dialog('option', 'width', 'auto')
			 $("#create"+forWhom+"View").dialog('option', 'position', ['center','top']);
			 $("#show"+forWhom+"View").dialog('close');
			 $("#edit"+forWhom+"View").dialog('close');
			 $("#create"+forWhom+"View").dialog('open');
		}
	});
	updateTitle(forWhom,"create","Create")
	}

function showOrEditModelManuDetails(controllerName,id,forWhom,view, name){
	jQuery.ajax({
		url : $("#contextPath").val()+'/'+controllerName+'/'+view+'/',
		data : {'id' : id},
		type : 'POST',
		success : function(data) {
			 $("#"+view+""+forWhom+"View").html(data);
			 $("#"+view+""+forWhom+"View").dialog('option', 'width', 'auto')
			 $("#"+view+""+forWhom+"View").dialog('option', 'position', ['center','top']);
			 $("#create"+forWhom+"View").dialog('close');
			 if(view=='edit')
				 $("#show"+forWhom+"View").dialog('close');
			 else if(view=='show')
				 $("#edit"+forWhom+"View").dialog('close');
			 
			 $("#"+view+""+forWhom+"View").dialog('open');
		}
	});
	updateTitle(forWhom,view, name)
}

function updateTitle( type, view, name){
	$("#"+view+""+type+"View").dialog( "option", "title", name+" "+type );
}

function updateModel(forWhom, formName){
	$("#"+formName).ajaxSubmit({
		success: function(data) { 
			$("#editModelView").dialog('close')
			$("#showModelView").html(data) 
			$("#showModelView").dialog('option', 'width', 'auto')
			$("#showModelView").dialog('option', 'position', ['center','top']);
			$("#showModelView").dialog('open');
		},
		error: function(request, errordata, errorObject) { alert(errorObject.toString()); },
		});
}

function updateManufacturer(forWhom){
	jQuery.ajax({
		url: $("#editManufacturerFormId").attr('action'),
		data: $("#editManufacturerFormId").serialize(),
		type:'POST',
		success: function(data) {
			if(data.errMsg){
				alert(data.errMsg)
			}else{
				$("#edit"+forWhom+"View").dialog('close')
				$("#show"+forWhom+"View").html(data)
				$("#show"+forWhom+"View").dialog('option', 'width', 'auto')
				$("#show"+forWhom+"View").dialog('option', 'position', ['center','top']);
				$("#show"+forWhom+"View").dialog('open');
			}
		}
	});
	
}