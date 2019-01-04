/**
 * Action to invoke Change status ajax call from TaskManager and MyTasks
 * @param id
 * @param status
 * @param currentStatus
 * @param from
 */
function changeStatus(id, status, currentStatus, from){
	var params = {'id':id,'status':status,'currentStatus':currentStatus,redirectTo:'taskManager'};
	var doneCss = $('#done_text_'+id).attr('class');
	var doneOnClick = $('#done_button_'+id).attr('onclick');
	updateStatus(id);

	if (from == "myTask") {
		params = {'id':id,'status':status,'currentStatus':currentStatus,redirectTo:'taskManager','view':'myTask','tab':$('#tabId').val() }
	}
	jQuery.ajax({
		url:contextPath+'/task/update',
		data: params,
		type:'POST',
		success: function(data) {
			if (typeof data.error !== 'undefined') {
				alert(data.error);
			} else {
				//alert(data.cssClass)
				if (from=="taskManager") {
					var cellId = '#status_' + id;
					if ($(cellId).length == 0) {
						cellId = '#statusTd_' + id;
					}
					$(cellId).html(data.assetComment.status)
					$(cellId).parent().removeAttr('class').addClass(data.statusCss)
					$(cellId).removeAttr('class').addClass(data.statusCss).addClass('cellWithoutBackground')
					if (status=="Started") {
						$('#start_button_'+id).remove();
					    $('#done_button_'+id).attr('onclick', doneOnClick)
					    $('#done_text_'+id).attr('class', doneCss)
					} else if (status=="Completed") {
						$('#done_button_'+id).remove();
					}
				} else {
					$('#myTaskList').html(data)
					hideStatus(id, status)
					$('#issueTrId_'+id).attr('onClick','hideStatus('+id+',"'+status+'")')
					if (status=='Started') {
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
 * Invoke API Action
 * @param commentId
 */
function invokeAction(commentId) {
    var doneCss = $('#done_text_'+commentId).attr('class');
    var doneOnClick = $('#done_button_'+commentId).attr('onclick');
    updateStatus(commentId);

    jQuery.ajax({
        url:contextPath+'/ws/task/'+commentId+'/invokeAction',
        data: {},
        type:'POST',
        success: function(data) {
            if (typeof data.error !== 'undefined') {
                alert(data.error);
            } else {
                //alert(data.cssClass)
                console.log(data);
                //if (from=="taskManager") {
					var id = commentId;
					var status = data.assetComment.status;
                    var cellId = '#status_' + id;
                    if ($(cellId).length === 0) {
                        cellId = '#statusTd_' + id;
                    }
                    $(cellId).html(data.assetComment.status)
                    $(cellId).parent().removeAttr('class').addClass(data.statusCss)
                    $(cellId).removeAttr('class').addClass(data.statusCss).addClass('cellWithoutBackground')
                    if (status==="Started") {
                        $('#start_button_'+id).remove();
                        $('#done_button_'+id).attr('onclick', doneOnClick)
                        $('#done_text_'+id).attr('class', doneCss)
                    } else if (status==="Completed") {
                        $('#done_button_'+id).remove();
                    }
                //}
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
 * Reset API Action
 * @param commentId
 */
function resetAction(commentId) {
    updateStatus(commentId);
    if (confirm("Are you sure you want to reset the action?")) {
        jQuery.ajax({
            url: contextPath + '/ws/task/' + commentId + '/resetAction',
            data: {},
            type: 'POST',
            success: function (data) {
                if (typeof data.error !== 'undefined') {
                    alert(data.error);
                } else {
                    pageSubmit();
                }
            } // No error handler, since all errors should be trapped by jquery global interceptor
        });
    }
}

/**
 * Update status bar to change buttons to prevent double-clicking
 * @param id
 */
function updateStatus(id) {
    $('#start_button_'+id).removeAttr('onclick');
    $('#done_button_'+id).removeAttr('onclick');
    $('#invoke_button_'+id).removeAttr('onclick');
    $('#reset_button_'+id).removeAttr('onclick');
    $('#start_text_'+id).attr('class', 'task_button_disabled');
    $('#done_text_'+id).attr('class', 'task_button_disabled');
    $('#invoke_text_'+id).attr('class', 'task_button_disabled');
    $('#reset_text_'+id).attr('class', 'task_button_disabled');

    $('#showCommentDialog #start_button_'+id).removeAttr('onclick');
    $('#showCommentDialog #done_button_'+id).removeAttr('onclick');
    $('#showCommentDialog #start_text_'+id).attr('class', 'task_button_disabled');
    $('#showCommentDialog #done_text_'+id).attr('class', 'task_button_disabled');
}

/**
 * Action to invoke AssignToMe ajax call from TaskManager and MyTasks
 * @param id
 * @param user
 * @param status
 * @param from
 */
function assignTask(id, user, status, from) {
	if (typeof timerBar !== 'undefined')
		timerBar.resetTimer();
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
				if (from=="taskManager") {
					 $('#assignedToName_'+id).html(data.assignedTo)
					 // $('#row_d_'+id).hide()
					if (typeof timerBar !== 'undefined')
						timerBar.resetTimer();
				} else {
					$('#assignedToNameSpan_'+id).html(data.assignedToName);
					if (typeof timerBar !== 'undefined')
						timerBar.resetTimer();
					$('#assigntome_button_'+id).remove();
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
function getActionBarGrid (spanId) {
	if (typeof timerBar !== 'undefined')
		timerBar.resetTimer();
	var id = spanId
	$('#span_'+spanId).parent().parent().find('span').each(function () {
		if ($(this).attr("id"))
			$(this).removeAttr('onclick')
	});
	$('#'+id).after("<tr id='load_d_"+id+"'><td nowrap='nowrap' colspan='13' class='statusButtonBar' ><img src='../images/spinner.gif'/></td></tr>")
	if (actionBarLoadReq)
		actionBarLoadReq.abort();
	actionBarLoadReq = jQuery.ajax({
		url: contextPath+'/task/genActionBarHTML',
		data: {'id':id},
		type:'POST',
		success: function (data, status, xhr) {
			$('#load_d_'+id).remove()
			var url = xhr.getResponseHeader('X-Login-URL');
			if (url) {
				window.location.href = url;
			} else {
				if (!$("#row_d_"+id).html() && data)
					$('#'+id).after("<tr id='row_d_"+id+"'> <td nowrap='nowrap' colspan='13' class='statusButtonBar'>"+data+"</td></tr>")
					
				$('#span_'+spanId).parent().parent().find('span').each(function(){
					if ($(this).attr("id")) {
						$(this).removeAttr('onclick')
						$(this).unbind("click").bind("click", function(){
							hideActionBarGrid("row_d_"+id,"span_"+spanId)
						});
					}
				})
			}
		},
		error: function (xhr, textStatus, errorThrown) {
			$('#load_d_'+id).remove()
			if (!$("#row_d_"+id).html()) {
				$('#'+id).after("<tr id='row_d_"+id+"'><td nowrap='nowrap' colspan='13' class='statusButtonBar'>"+
					"An unexpected error occurred while populating action bar.</td></tr>")
			}
			$('#span_'+spanId).parent().parent().find('span').each(function(){
				if ($(this).attr("id")) {
					$(this).removeAttr('onclick')
					$(this).unbind("click").bind("click", function(){
						hideActionBarGrid("row_d_"+id,"span_"+spanId)
					});
				}
			})
		}
	});
}

function getBulkActionBarGrid(taskIds) {
	jQuery.ajax({
		url: contextPath+'/task/genBulkActionBarHTML',
		data: {'id':taskIds},
		type:'POST',
		success: function(resp, status, xhr) {
			for (i=0; i<taskIds.length; i++) {
				var data = resp[taskIds[i]]
				$('#'+taskIds[i]).after("<tr id='load_d_"+taskIds[i]+"'><td nowrap='nowrap' colspan='13' class='statusButtonBar' ><img src='../images/spinner.gif'/></td></tr>")
				$('#load_d_'+taskIds[i]).remove()

				if (!$("#row_d_"+taskIds[i]).html() && data)
						$('#'+taskIds[i]).after("<tr id='row_d_"+taskIds[i]+"'> <td nowrap='nowrap' colspan='13' class='statusButtonBar'>"+data+"</td></tr>")
						
					$('#'+taskIds[i]).find('span').each(function(){
						if ($(this).attr("id")) {
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
			for (i=0; i<taskIds.length; i++) {
				$('#load_d_'+taskIds[i]).remove()
				if (!$("#row_d_"+taskIds[i]).html()) {
					$('#'+taskIds[i]).after("<tr id='row_d_"+taskIds[i]+"'><td nowrap='nowrap' colspan='13' class='statusButtonBar'>"+
							"An unexpected error occurred while populating action bar.</td></tr>")
				}
				$('#'+taskIds[i]).find('span').each(function(){
					if ($(this).attr("id")) {
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
function hideActionBars() {
	$('.jqTable').find('.statusButtonBar').each(function(){
		$(this).parent().prev().find('span').each(function(){
			var $id = $(this).attr("id");
			if ($id) {
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
		if ($(this).attr("id")) {
			$(this).removeAttr('onclick')
			$(this).off("click").on("click", function(){
				getActionBarGrid(id)
			});
		}
	})
	
	if (typeof timerBar !== 'undefined')
		timerBar.resetTimer();
}

function changeEstTime(day,commentId,id) {
	console.log(id)
	var reqId=id.split("_")
	jQuery.ajax({
		url: contextPath+'/task/changeEstTime',
		data: {'commentId':commentId,'day':day},
		type:'POST',
		success: function(resp) {
			if (resp.etext != "") {
				alert(resp.etext)
			} else {
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

function toogleGenDetails(id) {
	if ($("#rightTriangle_"+id).is(":visible")) {
		$("#rightTriangle_"+id).hide();
		$("#downTriangle_"+id).show();
		$("#predDivId_"+id).show();
	} else {
		$("#rightTriangle_"+id).show();
		$("#downTriangle_"+id).hide();
		$("#predDivId_"+id).hide();
	}
}

// sets the user's preference for the specified code to the specified value
function setUserPreference (code, value, callback) {
	jQuery.ajax({
		url: contextPath+'/ws/user/preference',
		data: {'code':code,'value':value},
		type:'POST',
		success: callback
	});
}