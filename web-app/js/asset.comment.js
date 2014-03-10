
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
	
	$('#showCommentDialog #start_button_'+id).removeAttr('onclick')
	$('#showCommentDialog #done_button_'+id).removeAttr('onclick')
	$('#showCommentDialog #start_text_'+id).attr('class', 'task_button_disabled')
	$('#showCommentDialog #done_text_'+id).attr('class', 'task_button_disabled')

	if(from == "myTask"){ params = {'id':id,'status':status,'currentStatus':currentStatus,redirectTo:'taskManager','view':'myTask','tab':$('#tabId').val() }}
	jQuery.ajax({
		url:contextPath+'/task/update',
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
				$("#showCommentTable #statusShowId").html(data.assetComment.status)
				$("#showCommentTable #statusShowId").removeAttr('class').addClass(data.statusCss)
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
	
	$('#showCommentDialog #assigntome_button_'+id).removeAttr('onclick')
	$('#showCommentDialog #assigntome_text_'+id).attr('class', 'task_button_disabled')
 	
	jQuery.ajax({
		url: contextPath+'/task/assignToMe',
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
				$("#showCommentDialog #assignedToTdId").html(data.assignedTo)
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
var actionBarLoadReq
function getActionBarGrid(spanId){
   if(B2 != ''){ B2.Pause() }
   var id = spanId
   $('#span_'+spanId).parent().parent().find('span').each(function(){
		if($(this).attr("id")){
			$(this).removeAttr('onclick')
		}
   });
   $('#'+id).after("<tr id='load_d_"+id+"'><td nowrap='nowrap' colspan='13' class='statusButtonBar' ><img src='../images/spinner.gif'/></td></tr>")
   if(actionBarLoadReq)actionBarLoadReq.abort();
   actionBarLoadReq = jQuery.ajax({
							url: contextPath+'/task/genActionBarHTML',
							data: {'id':id},
							type:'POST',
							success: function(data, status, xhr) {
									$('#load_d_'+id).remove()
									var url = xhr.getResponseHeader('X-Login-URL');
									if (url) {
										alert("Your session has expired and need to login again.");
										window.location.href = url;
									} else {
										if(!$("#row_d_"+id).html() && data)
											$('#'+id).after("<tr id='row_d_"+id+"'> <td nowrap='nowrap' colspan='13' class='statusButtonBar'>"+data+"</td></tr>")
											
										$('#span_'+spanId).parent().parent().find('span').each(function(){
											if($(this).attr("id")){
												$(this).removeAttr('onclick')
												$(this).unbind("click").bind("click", function(){
													hideActionBarGrid("row_d_"+id,"span_"+spanId)
											    });
											}
										})
									 }
							},
				    		error: function(xhr, textStatus, errorThrown) {
				    			$('#load_d_'+id).remove()
				    			if(!$("#row_d_"+id).html()){
									$('#'+id).after("<tr id='row_d_"+id+"'><td nowrap='nowrap' colspan='13' class='statusButtonBar'>"+
											"An unexpected error occurred while populating action bar.</td></tr>")
				    			}
				    			$('#span_'+spanId).parent().parent().find('span').each(function(){
									if($(this).attr("id")){
										$(this).removeAttr('onclick')
										$(this).unbind("click").bind("click", function(){
											hideActionBarGrid("row_d_"+id,"span_"+spanId)
									    });
									}
								})
				    		}
						});
}
function getBulkActionBarGrid(taskIds){
jQuery.ajax({
	url: contextPath+'/task/genBulkActionBarHTML',
	data: {'id':taskIds},
	type:'POST',
	success: function(resp, status, xhr) {
		   for(i=0; i<taskIds.length; i++){
			   var data = resp[taskIds[i]]
			   $('#'+taskIds[i]).after("<tr id='load_d_"+taskIds[i]+"'><td nowrap='nowrap' colspan='13' class='statusButtonBar' ><img src='../images/spinner.gif'/></td></tr>")
			   $('#load_d_'+taskIds[i]).remove()
			   
			   if(!$("#row_d_"+taskIds[i]).html() && data)
					$('#'+taskIds[i]).after("<tr id='row_d_"+taskIds[i]+"'> <td nowrap='nowrap' colspan='13' class='statusButtonBar'>"+data+"</td></tr>")
					
				$('#'+taskIds[i]).find('span').each(function(){
					if($(this).attr("id")){
						$(this).removeAttr('onclick')
						var thisId = this.id.split('_')[1]
						$(this).unbind("click").bind("click", function(){
							hideActionBarGrid("row_d_"+thisId,this.id)
					    });
					}
				})
		   }
		   $('.bulkEdit').removeAttr("disabled");
	},
	error: function(xhr, textStatus, errorThrown) {
		for(i=0; i<taskIds.length; i++){
			$('#load_d_'+taskIds[i]).remove()
			if(!$("#row_d_"+taskIds[i]).html()){
				$('#'+taskIds[i]).after("<tr id='row_d_"+taskIds[i]+"'><td nowrap='nowrap' colspan='13' class='statusButtonBar'>"+
						"An unexpected error occurred while populating action bar.</td></tr>")
			}
			$('#'+taskIds[i]).find('span').each(function(){
				if($(this).attr("id")){
					$(this).removeAttr('onclick')
					var thisId = this.id.split('_')[1]
					$(this).unbind("click").bind("click", function(){
						hideActionBarGrid("row_d_"+thisId,this.id)
				    });
				}
			})
		}
	}
});
}

/**
 * 
 */
function hideActionBars(){
	$('.jqTable').find('.statusButtonBar').each(function(){
		$(this).parent().prev().find('span').each(function(){
			var $id = $(this).attr("id");
			if($id){
				var id = $id.split("_")[1];
				$(this).removeAttr('onclick')
				$(this).off("click").on("click", function(){
					getActionBarGrid(id)
			    });
			}
		});
		$(this).parent().remove();
	});
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
			$(this).off("click").on("click", function(){
				getActionBarGrid(id)
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
			if($("#myPage").val() == 'taskManager'){
				timedUpdate(timeUpdate[0].updateTime.TASKMGR_REFRESH)
			} else {
				timedUpdate(timeUpdate[0].updateTime.MYTASKS_REFRESH)
			}
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
function changeEstTime(day,commentId,id){
	console.log(id)
	var reqId=id.split("_")
	jQuery.ajax({
		url: contextPath+'/task/changeEstTime',
		data: {'commentId':commentId,'day':day},
		type:'POST',
		success: function(resp) {
			if(resp.etext !=""){
				alert(resp.etext)
			}else {
					$("#"+id).removeAttr('onclick')
					$("#"+reqId[0]+"_text_"+reqId[2]).removeAttr('class')
					$("#"+reqId[0]+"_text_"+reqId[2]).attr('class', 'task_button_disabled')
					
					$('#estStartShowId').html(resp.estStart)
					$('#estFinishShowId').html(resp.estFinish)
					$('#showCommentDialog #'+id).removeAttr('onclick')
					$('#showCommentDialog #'+reqId[0]+"_text_"+reqId[2]).removeAttr('class')
					$('#showCommentDialog #'+reqId[0]+"_text_"+reqId[2]).attr('class', 'task_button_disabled')
			}
		}
	});
}

function toogleGenDetails(id){
	if($("#rightTriangle_"+id).is(":visible")){
		$("#rightTriangle_"+id).hide();
		$("#downTriangle_"+id).show();
		$("#predDivId_"+id).show();
	}else {
		$("#rightTriangle_"+id).show();
		$("#downTriangle_"+id).hide();
		$("#predDivId_"+id).hide();
	}
}