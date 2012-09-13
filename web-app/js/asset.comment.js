var B1 = []
var B2 = []
var taskManagerTimePref = "60"

/**
 * Action to invoke Change status ajax call from TaskManager and MyTasks
 * @param id
 * @param status
 * @param currentStatus
 * @param from
 */
function changeStatus(id, status, currentStatus, from){
	var params = {'id':id,'status':status,'currentStatus':currentStatus,redirectTo:'taskManager'}
	if(from == "myTask"){ params = {'id':id,'status':status,'currentStatus':currentStatus,redirectTo:'taskManager','view':'myTask','tab':$('#tabId').val() }}
	jQuery.ajax({
		url: '../assetEntity/updateComment',
		data: params,
		type:'POST',
		success: function(data) {
			if (typeof data.error !== 'undefined') {
				alert(data.error);
			} else {
				//alert(data.cssClass)
				if(from=="taskManager"){
					$('#status_'+id).html(data.assetComment.status)
					$('#status_'+id).parent().removeAttr('class').addClass(data.statusCss)
				    if(status=="Started"){ 
					    $('#startTdId_'+id).hide() 
					}else if(status=="Completed"){
						$('#startTdId_'+id).hide()
						$('#doneTdId_'+id).hide()
					}
				}else{
					 $('#myTaskList').html(data)
					 hideStatus(id, status)
					 $('#issueTrId_'+id).attr('onClick','hideStatus('+id+',"'+status+'")')
					 if(status=='Started'){
					 	$('#started_'+id).hide()
					 }
					 if(B1 != '' && taskManagerTimePref != 0) {
					 	B1.Restart(taskManagerTimePref);
					 } else { 
					 	B1.Pause(0);
					 }
				}
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while attempting to update task/comment")
		}
	});
}

/**
 * Action to invoke AssignToMe ajax call from TaskManager and MyTasks
 * @param id
 * @param user
 * @param status
 * @param from
 */
function assignTask(id, user, status, from){
	if(B2 != ''){  B2.Pause(); }
	if(B1 != ''){ B1.Pause(); }
	jQuery.ajax({
		url: '../task/assignToMe',
		data: {'id':id, 'user':user, 'status':status},
		type:'POST',
		success: function(data) {
			if (data.errorMsg) {
				alert(data.errorMsg);
			} else {
				if(from=="taskManager"){
					 $('#assignedToName_'+id).html(data.assignedTo)
					 $('#assignMeId_'+id).hide()
					 $('#row_d_'+id).hide()
					if(B2 != '' && taskManagerTimePref != 0){ B2.Restart(taskManagerTimePref);}
				}else{
					 $('#assignedToNameSpan_'+id).html(data.assignedTo)
					 $('#assignToMeId_'+id).hide()
					 if(B1 != '' && taskManagerTimePref != 0){ 
					 	B1.Restart(taskManagerTimePref); 
					 }else { 
					  	B1.Pause(0);
					 }
				}
			}
			
		},
		error: function(jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred. Please close and reload form to see if the problem persists")
		}
	});
}
/**
 * Used to show the action bar in Task Manager
 * @param spanId
 */
function getActionBar(spanId){
   if(B2 != ''){ B2.Pause() }
   var id = spanId.split('_')[1]
   var trId = $('#'+spanId).parent().parent().attr('id')
   if($('#row_d_'+id).html() == null ){
   jQuery.ajax({
		url: '../task/genActionBarHTML',
		data: {'id':id},
		type:'POST',
		success: function(data) {
			 $('#'+trId).after("<tr id='row_d_"+id+"'> <td nowrap='nowrap' colspan='13' class='statusButtonBar'>"+data+"</td></tr>")
			 $('#'+spanId).parent().parent().find('span').attr('onClick','hideActionBar("row_d_'+id+'","'+spanId+'")')
			}
		});
   }
}
/**
 * Used to hide the action bar in Task Manager
 * @param rowId
 * @param spanId
 */
function hideActionBar(rowId,spanId){
	$('#'+rowId).remove()
	$('#'+spanId).parent().parent().find('span').attr('onClick','getActionBar("'+spanId+'")')
	if(B2 != '' && taskManagerTimePref != 0){ B2.Restart(taskManagerTimePref) }
}
/**
 * Updated timer preferences
 * @param data
 */
function changeTimebarPref(data){
	 var timeUpdate = eval("(" + data.responseText + ")")
		if(timeUpdate){
			timedUpdate(timeUpdate[0].updateTime.MY_TASK)
		}
}
/**
 * updated bar preferences
 * @param timeoutPeriod
 */
function timedUpdate(timeoutPeriod) {
	 taskManagerTimePref = timeoutPeriod
	 if(B1 != ''){
		 if(taskManagerTimePref != 0){
			 B1.Start(timeoutPeriod);
		 } else {
			 B1.Pause(0);
		 }
	 } else {
		 if(taskManagerTimePref != 0){
			 B2.Start(timeoutPeriod);
		 } else {
			 B2.Pause(0);
		 }
	 }
}