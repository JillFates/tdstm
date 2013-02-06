
/**
 * Action to invoke Change status ajax call from TaskManager and MyTasks
 * @param id
 * @param status
 * @param currentStatus
 * @param from
 */
function changeStatus(id, status, currentStatus, from){
	var params = {'id':id,'status':status,'currentStatus':currentStatus,redirectTo:'taskManager'}

	// Disable status change buttons to prevent double-clicking
	$('#start_button_'+id).removeAttr('onclick')
	$('#done_button_'+id).removeAttr('onclick')
 	$('#start_text_'+id).attr('class', 'task_button_disabled')
	$('#done_text_'+id).attr('class', 'task_button_disabled')

	if(from == "myTask"){ params = {'id':id,'status':status,'currentStatus':currentStatus,redirectTo:'taskManager','view':'myTask','tab':$('#tabId').val() }}
	jQuery.ajax({
		url: '../task/update',
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
				    $('#status_'+id).removeAttr('class').addClass(data.statusCss).addClass('cellWithoutBackground')
				    if(status=="Started"){ 
					    // $('#startTdId_'+id).hide() 
					}else if(status=="Completed"){
						//$('#startTdId_'+id).hide()
						//$('#doneTdId_'+id).hide()
					}
				}else{
					 $('#myTaskList').html(data)
					 hideStatus(id, status)
					 $('#issueTrId_'+id).attr('onClick','hideStatus('+id+',"'+status+'")')
					 if(status=='Started'){
					 	$('#started_'+id).hide()
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
	$('#assigntome_button_'+id).removeAttr('onclick')
	$('#assigntome_text_'+id).attr('class', 'task_button_disabled')
 	
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
					 // $('#row_d_'+id).hide()
					if(B2 != '' && taskManagerTimePref != 0){ B2.Restart(taskManagerTimePref);}
				}else{
					 $('#assignedToNameSpan_'+id).html(data.assignedTo)
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
function getActionBarGrid(spanId){
   if(B2 != ''){ B2.Pause() }
   var id = spanId//spanId.split('_')[1]
   var trId =id// $('#'+spanId).parent().parent().attr('id')
   if($('#row_d_'+id).html() == null ){
   jQuery.ajax({
		url: '../task/genActionBarHTML',
		data: {'id':id},
		type:'POST',
		success: function(data) {
				$('#span_'+spanId).parent().parent().find('span').each(function(){
					if($(this).attr("id")){
						$(this).removeAttr('onclick')
						$(this).unbind("click").bind("click", function(){
							hideActionBar("row_d_"+id,"span_"+spanId)
					    });
					}
				})
				//$('#span_'+spanId).attr('onClick','hideActionBar("row_d_'+id+'", '+spanId+')')
				$('#'+trId).after("<tr id='row_d_"+id+"'> <td nowrap='nowrap' colspan='13' class='statusButtonBar'>"+data+"</td></tr>")
			}
		});
   }
}
/**
 * Used to hide the action bar in Task Manager
 * @param rowId
 * @param spanId
 */
function hideActionBarGrid(rowId,spanId){
	var id = spanId.split('_')[1]
	$('#'+rowId).remove()
	$('#'+spanId).parent().parent().find('span').each(function(){
		if($(this).attr("id")){
			$(this).removeAttr('onclick')
			$(this).unbind("click").bind("click", function(){
				getActionBar(id)
		    });
		}
	})
	if(B2 != '' && taskManagerTimePref != 0){ B2.Restart(taskManagerTimePref) }
}
/**
 * TODO : Remove this once verified in tmdev
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
				$('#'+spanId).parent().parent().find('span').each(function(){
					if($(this).attr("id")){
						$(this).removeAttr('onclick')
						$(this).unbind("click").bind("click", function(){
							hideActionBar("row_d_"+id,spanId)
					    });
					}
				})
				$('#'+trId).after("<tr id='row_d_"+id+"'> <td nowrap='nowrap' colspan='13' class='statusButtonBar'>"+data+"</td></tr>")
			}
		});
   }
}
/**
 * TODO : Remove this once verified in tmdev
 * Used to hide the action bar in Task Manager
 * @param rowId
 * @param spanId
 */
function hideActionBar(rowId,spanId){
	$('#'+rowId).remove()
	$('#'+spanId).parent().parent().find('span').each(function(){
		if($(this).attr("id")){
			$(this).removeAttr('onclick')
			$(this).unbind("click").bind("click", function(){
				getActionBar(spanId)
		    });
		}
	})
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
/**
 * Use to display time bar at task manager and my task page.
 * 
 */

function zxcAnimate(mde,obj,srt){
	this.to=null;
	this.obj=typeof(obj)=='object'?obj:document.getElementById(obj);
	this.mde=mde.replace(/\W/g,'');
	this.data=[srt||0];
	return this;
}

zxcAnimate.prototype.animate=function(srt,fin,ms,scale,c){
	clearTimeout(this.to);
	this.time=ms||this.time||0;
	this.neg=srt<0||fin<0;
	this.data=[srt,srt,fin];
	this.mS=this.time*(!scale?1:Math.abs((fin-srt)/(scale[1]-scale[0])));
	this.c=typeof(c)=='string'?c.charAt(0).toLowerCase():this.c?this.c:'';
	this.inc=Math.PI/(2*this.mS);
	this.srttime=new Date().getTime();
	this.cng();
}

zxcAnimate.prototype.cng=function(){
	var oop=this,ms=new Date().getTime()-this.srttime;
	this.data[0]=(this.c=='s')?(this.data[2]-this.data[1])*Math.sin(this.inc*ms)+this.data[1]:(this.c=='c')?this.data[2]-(this.data[2]-this.data[1])*Math.cos(this.inc*ms):(this.data[2]-this.data[1])/this.mS*ms+this.data[1];
	this.apply();
	if (ms<this.mS) this.to=setTimeout(function(){oop.cng()},10);
	else {
		this.data[0]=this.data[2];
		this.apply();
	 if (this.Complete) this.Complete(this);
	}
}

zxcAnimate.prototype.apply=function(){
	if (isFinite(this.data[0])){
		if (this.data[0]<0&&!this.neg) this.data[0]=0;
		if (this.mde!='opacity') this.obj.style[this.mde]=Math.floor(this.data[0])+'px';
		else zxcOpacity(this.obj,this.data[0]);
	}
}

function zxcOpacity(obj,opc){
	if (opc<0||opc>100) return;
	obj.style.filter='alpha(opacity='+opc+')';
	obj.style.opacity=obj.style.MozOpacity=obj.style.WebkitOpacity=obj.style.KhtmlOpacity=opc/100-.001;
}