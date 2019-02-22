<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Task Details</title>
	<asset:stylesheet href="css/ui.datepicker.css" />
</head>
<body>
	<a name="top"></a>
	<div id="spinner" class="spinner" style="display: none;"><asset:image src="images/spinner.gif" alt="Spinner" /></div>
	<div class="mainbody" id="mainbody">
	 	<div id="mydiv" onclick="this.style.display = 'none';">
 			<g:if test="${flash.message}">
				<div style="color: red; font-size:15px"><ul>${flash.message}</ul></div>
			</g:if>
		</div>
	<g:form name="issueUpdateForm" controller="task" action="update">
		<a name="comments"></a>
		<input id="issueId" name="id" type="hidden" value="${assetComment.id}" />
		<input id="redirectTo" name="redirectTo" type="hidden" value="taskList" />
		<g:set var="predCount" value="${assetComment.taskDependencies?.size()}" />
		<g:set var="sucCount" value="${successor.size()}" />
		<table style="margin-left: -2px;">
			<tr>
				<td class="heading" colspan="2"><a class="heading" href="#comments">Task details:</a></td>
			</tr>
			<tr>
				<td colspan="2">
			</td>
			</tr>
			<tr>
				<td valign="top" class="name"><label for="comment">Task:</label></td>
				<td>
					<input type="text" title="Edit Comment..." id="editComment_${assetComment.id}" name="comment" value="${assetComment.comment}" style="width: 100%" />
				</td>
			</tr>
			<g:if test="${predCount > 0 || sucCount > 0}">
				<tr>
					<td valign="middle" class="name"><label>Dependencies:</label></td>
					<td valign="top" class="name">
					<div id="predsTable_${assetComment.id}" class='assetImage' ${predCount <= 5 && sucCount <= 5 ? 'style="display:none;"' : '' }>
						<h4 onclick="javascript:toogleGenDetails('${assetComment.id}')">Dependency details
						<asset:image src="images/triangle_right.png" />
						<asset:image src="images/triangle_down.png" />
						</h4>
					</div>
					<div id="predDivId_${assetComment.id}" ${predCount > 5 || sucCount > 5 ? 'style="display:none;"' : ''}>
					<div style="width:400px; float:left">
						<fieldset>
						<legend>Predecessors</legend>
						<g:each in="${assetComment.taskDependencies}" var="task">
							<span class="${task.predecessor?.status ? 'task_'+task.predecessor?.status?.toLowerCase() : 'task_na'}" onclick="showAssetCommentMyTasks(${task.predecessor.id})">
							${task.predecessor.taskNumber}:${task.predecessor.comment} (${task.predecessor.category})
							</span>
							<br/>
						</g:each>
						</fieldset>
					</div>
					<div style="width:400px; float:left; margin-left:30px;">
						<fieldset>
						<legend>Successors</legend>
						<g:each in="${successor}" var="task">
							<span class="${task.assetComment?.status ? 'task_'+task.assetComment?.status?.toLowerCase() : 'task_na'}" onclick="showAssetCommentMyTasks(${task.assetComment.id})">
							${task.assetComment.taskNumber}:${task.assetComment.comment} (${task.assetComment.category})
							</span>
							<br/>
						</g:each>
						</fieldset>
					</div>
					</div>
					</td>
				</tr>
			</g:if>
			<tr class="prop" id="teamId"  >
				<td valign="top" class="name"><label for="team">Team:</label></td>
				<td valign="top" class="value" id="team_${assetComment.id}" nowrap="nowrap">${assetComment.role}</td>
			</tr>
			<tr class="prop issue" id="assignedToTrEditId" >
				<td valign="top" class="name"><label for="assignedTo">Assigned:</label></td>
				<td valign="top" id="assignedToEditTdId" >
					${raw(assignToSelect)}
				</td>
			</tr>
			<g:if test="${assetComment.estStart}">
				<tr class="prop issue" id="estStartShowId"  >
					<td valign="top" class="name"><label for="estStart">Est. Start:</label></td>
					<td valign="top" class="value" id="estStartShowId_${assetComment.id}" nowrap="nowrap">
					<tds:convertDate date="${assetComment.estStart}" format="M/d kk:mm" /></td>
				</tr>
			</g:if>
			<g:if test="${assetComment.estFinish}">
				<tr class="prop issue" id="estFinishShowId"  >
					<td valign="top" class="name"><label for="estFinish">Est. Finish:</label></td>
					<td valign="top" class="value" id="estFinishShowId_${assetComment.id}" nowrap="nowrap">
					<tds:convertDate date="${assetComment.estFinish}" format="M/d kk:mm" /></td>
				</tr>
			</g:if>
			<tr class="prop issue" id="dueDateShowId"  >
				<td valign="top" class="name"><label for="dueDateCreateId">Due Date:</label></td>
				<td>
					<script type="text/javascript" charset="utf-8">
						jQuery(function($){ $(".dateEditRange").kendoDatePicker({ animation: false, format:tdsCommon.kendoDateFormat() }); });
            		</script>
	          		<input type="text" class="dateEditRange" size="15" style="" name="dueDateCreateId" id="dueDateCreateId"
							value="${dueDate}" />
				</td>
            </tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="category">Category:</label></td>
				<td valign="top" class="value"><g:select id="categoryEditId_${assetComment.id}" name="category" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(com.tds.asset.AssetComment).category.inList}" value="${assetComment.category}"></g:select>
				<g:if test="${assetComment.moveEvent}">
		   		  <span style="margin-left:60px;">Move Event:</span>
		   		  <span style="margin-left:10px;">${assetComment?.moveEvent.name}</span>
		   		</g:if>
				</td>
			</tr>
			<tr>
				<g:if test="${assetComment.assetEntity}">
		   			<td>Asset:</td><td><span class="assetLink" onclick="EntityCrud.showAssetDetailView('${assetComment.assetEntity?.assetClass}','${assetComment?.assetEntity.id}')">&nbsp;${assetComment?.assetEntity.assetName}</span></td>
		   		</g:if>
		   	</tr>
			<tr>
		   		  <td>Instructions Link:</td><td style="width: 1%"><tds:textAsLink text="${assetComment?.instructionsLink}" target="instructions"/></td>
		   	</tr>
		   	<tr class="prop">
				<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
				<td valign="top" class="value"><span id="categoryEditId">${assetComment?.createdBy} on
				<tds:convertDate date="${assetComment?.dateCreated}" format="M/d" /></span></td>
			</tr>
			<tr class="prop" >
				<td valign="top" class="name">
					<label for="status">Status:</label>
					<input id="currentStatus_${assetComment.id}" name="currentStatus" type="hidden" value="${assetComment.status}" />
				</td>
				<td id="statusEditTrId_${assetComment.id}">
					<g:if test="${statusWarn==1}">
						<g:select id="statusEditId_${assetComment.id}" name="status" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(com.tds.asset.AssetComment).status.inList}" value="${assetComment.status}"
						noSelection="['':'please select']" onChange="showResolve()" disabled="true"></g:select>
					</g:if>
					<g:else>
						<g:select id="statusEditId_${assetComment.id}" name="status" from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(com.tds.asset.AssetComment).status.inList}" value="${assetComment.status}"
						noSelection="['':'please select']" onChange="showResolve()"></g:select>
					</g:else>
				</td>
			</tr>
			 <tr class="prop">
				<td valign="top" class="name"><label for="notes">Previous Notes:</label></td>
				<td valign="top" class="value"><div id="previousNote">
				<table style="table-layout: fixed; width: 100%;border: 1px solid green;" >
                   <g:each in="${notes}" var="note" status="i" >
                    <tr>
	                    <td>${note[0]}</td>
	                    <td>${note[1]}</td>
                        <td style="word-wrap: break-word">${note[2]}</td>
                     </tr>
                   </g:each>
				 </table>
				</div></td>
			</tr>
		    <tr class="prop" id="noteId_${assetComment.id}">
				<td valign="top" class="name"><label for="notes">Note:</label></td>
				<td valign="top" class="value">
				   <textarea cols="80" rows="4" id="noteEditId_${assetComment.id}" name="note" style="width:100%;padding:0px;"></textarea>
				</td>
			</tr>
			<tr class="prop" id="resolutionId_${assetComment.id}" style="display: none;">
				<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
				<td valign="top" class="value">
					<textarea cols="100" rows="4" style="width:100%;padding:0px;" id="resolutionEditId_${assetComment.id}" name="resolution" >${assetComment.resolution}</textarea>
				</td>
			</tr>
			<g:if test="${assetComment.resolvedBy}">
				<tr class="prop">
					<td valign="top" class="name"><label for="resolution">Resolved By:</label></td>
					<td valign="top" class="value">
						<span id="resolvedByTd" >${assetComment.resolvedBy} on
						<tds:convertDate date="${assetComment?.dateResolved}" format="M/d" /></span>
					</td>
				</tr>
			</g:if>
			<tr>
			    <td class="buttonR" >
					<button type="button" class="btn btn-default save"  onclick="cancelButton(${assetComment.id})"><span class="glyphicon glyphicon-ban-circle" aria-hidden="true"></span> Cancel</button>
				</td>
				<td class="buttonR" style="text-align:right;padding: 5px 3px;">
					<button type="button" class="btn btn-default save"  onclick="validateComment(${assetComment.id})"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> Update Task</button>
				</td>
			</tr>
		</table>
</g:form>

		<%-- <div class="clear" style="margin:4px;"></div>
		<a name="detail"></a>
	 	<div>

			<table style="width:420px;">
			<tr>
				<td class="heading"><a href="#detail">Asset Details</a></td>
				<td><span style="float:right;"><a href="#top">Top</a></span></td>
			</tr>
			<tr><td colspan=2>

				<dl>
				<dt>Comment:</dt><dd>&nbsp;${assetComment.comment}</dd>
				<dt>Assigned to:</dt><dd>&nbsp;${assetComment?.assignedTo}</dd>
				<dt>Due Date:</dt><dd>&nbsp;<tds:convertDate date="${assetComment?.dueDate}" /></dd>
				<dt>Created by:</dt><dd>&nbsp;${assetComment?.createdBy} at ${assetComment?.dateCreated} </dd>
				<dt>Comment Type:</dt><dd>&nbsp;${assetComment?.commentType}</dd>
		   		<dt>Category:</dt><dd>&nbsp;${assetComment?.category}</dd>
		   		<g:if test="${assetComment.assetEntity}">
		   		  <dt>Asset Entity:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
		   		</g:if>
		   		<g:if test="${assetComment.moveEvent}">
		   		  <dt>Event:</dt><dd>&nbsp;${assetComment?.moveEvent.name}</dd>
		   		</g:if>
		   		<dt>Status:</dt><dd>&nbsp;${assetComment?.status}</dd>
		   		<dt>Resolution:</dt><dd>&nbsp;${assetComment?.resolution}</dd>
		   		<dt>Resolved At:</dt><dd>&nbsp;${assetComment?.dateResolved}</dd>
				<dt>Resolved By:</dt><dd>&nbsp;${assetComment?.resolvedBy}</dd>

				</dl>
		</tr>
		</table>
		</div>
		--%>
		<div class="clear" style="margin:4px;"></div>
		<a name="detail" ></a>
		<g:if test="${assetComment?.assetEntity}">
			<div style="float: left;width:100%">
				<table style="float: left;margin-left: -2px;">
				<tr>
					<td class="heading"><a href="#detail">${assetComment?.assetEntity?.assetType} Highlights</a></td>
					<td><span style="float:right;"><a href="#top">Top</a></span></td>
				</tr>
				<tr class="prop"><td colspan=2>
				<dl>
	      	<g:if test="${assetComment?.assetEntity?.assetType=='Application'}">
	            <dt>Application Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
							<dt>Validation:</dt><dd>&nbsp;${assetComment?.assetEntity.validation}</dd>
							<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
							<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
		      </g:if>
		      <g:elseif test="${assetComment?.assetEntity?.assetType=='Database'}">
		          <dt>Database Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
							<dt>DB Size:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
							<dt>DB Format:</dt><dd>&nbsp;${assetComment?.assetEntity.dbFormat}</dd>
							<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
	        </g:elseif>
	        <g:elseif test="${assetComment?.assetEntity?.assetType=='Files'}">
	            <dt>Storage Name:</dt><dd>&nbsp;${assetComment?.assetEntity.assetName}</dd>
							<dt>Storage Size:</dt><dd>&nbsp;${assetComment?.assetEntity.size}</dd>
							<dt>Storage Format:</dt><dd>&nbsp;${assetComment?.assetEntity.fileFormat}</dd>
							<dt>Bundle:</dt><dd>&nbsp;${assetComment?.assetEntity.moveBundle}</dd>
	        </g:elseif>
	        <g:else>
							<dt>Asset Tag:</dt><dd>&nbsp;${assetComment?.assetEntity?.assetTag}</dd>
							<dt>Asset Name:</dt><dd>&nbsp;${assetComment?.assetEntity?.assetName}</dd>
							<dt>Model:</dt><dd>&nbsp;${assetComment?.assetEntity?.model}</dd>
							<dt>Serial #:</dt><dd>&nbsp;${assetComment?.assetEntity?.serialNumber}</dd>
							<dt>Current Loc/Pos:</dt><dd>&nbsp;${assetComment?.assetEntity.sourceRack}/${assetComment?.assetEntity.sourceRackPosition}</dd>
						  <dt>Target Loc/Pos:</dt><dd>&nbsp;${assetComment?.assetEntity.targetRack}/${assetComment?.assetEntity.targetRackPosition}</dd>
							<dt>Source Room:</dt><dd>&nbsp;${assetComment?.assetEntity.sourceRoom}</dd>
							<dt>Target Room:</dt><dd>&nbsp;${assetComment?.assetEntity.targetRoom}</dd>
							<g:if test="${location == 'source'}">
						   	<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
								<dt>Rail Type:</dt><dd>&nbsp;${assetComment?.assetEntity.railType}</dd>
							</g:if>
							<g:else>
						   		<dt>Truck:</dt><dd>&nbsp;${assetComment?.assetEntity.truck}</dd>
						   		<dt>Cart/Shelf:</dt><dd>&nbsp;${assetComment?.assetEntity.cart}/${assetComment?.assetEntity.shelf}</dd>
						   		<dt>Plan Status:</dt><dd>&nbsp;${assetComment?.assetEntity.planStatus}</dd>
								<dt>Rail Type:</dt><dd>&nbsp;${assetComment?.assetEntity.railType}</dd>
							</g:else>
					</g:else>
						<g:each in="${customs}" var="custom" status="i" >
							<g:if test="${assetComment?.assetEntity?.(custom.field)}">
								<dt>${project.(custom.field) ?: custom.field }:</dt>
								<dd>&nbsp;<tds:textAsLink text="${assetComment?.assetEntity.(custom.field)}" target="_new"/></dd>
							</g:if>
						</g:each>
				</dl>
			</tr>
			</table>

			</div>
		</g:if>
</div>
<script type="text/javascript">
$( function() {
	var objId = ${assetComment.id}
	if($('#statusEditId_'+objId).val()=='Completed'){
		$('#noteId_'+objId).hide()
		$('#resolutionId_'+objId).show()
	}

	// Disable the AssignTo SELECT if user doensn't have permission
	var editAssignTo=${assignmentPerm ? 'true' : 'false'}
	if (!editAssignTo) {
		$('#assignedToEditId_'+objId).attr('disabled', 'disabled');
	}

	/*
	var updatePerm = ${permissionForUpdate}
	$("#editComment_"+objId).keypress(function(e){
		if(updatePerm && e.keyCode==13){e.preventDefault(); validateComment(objId); }
		else if(!updatePerm && e.keyCode==13) {e.preventDefault(); }
	});
	*/
});

function showResolve(){
	if ($('#statusEditId_'+${assetComment.id}).val()=='Completed') {
		$('#noteId_'+${assetComment.id}).hide()
		$('#resolutionId_'+${assetComment.id}).show()
	} else {
		$('#noteId_'+${assetComment.id}).show()
		$('#resolutionId_'+${assetComment.id}).hide()
	}
}
function validateComment(objId){
	var status = $('#statusEditId_'+objId).val()
	var params = {'comment':$('#editComment_'+objId).val(), 'resolution':$('#resolutionEditId_'+objId).val(),
		'category':$('#categoryEditId_'+objId).val(), 'assignedTo':$('#assignedToEditId_'+objId).val(),
		'status':$('#statusEditId_'+objId).val(),'currentStatus':$('#currentStatus_'+objId).val(),
		'note':$('#noteEditId_'+objId).val(),'id':objId,'view':'myTask', 'tab': $('#tabId').val(),
		'dueDate':$("#dueDateCreateId").val()
	}
	jQuery.ajax({
		url: tdsCommon.createAppURL('/task/update'),
		data: params,
		type:'POST',
		success: function(data) {
			if (typeof data.error !== 'undefined') {
				alert(data.error);
			} else {
				$('#myTaskList').html(data)
				$('#showStatusId_'+objId).show()
				$('#issueTrId_'+objId).each(function(){
					$(this).removeAttr('onclick')
					$(this).unbind("click").bind("click", function(){
						hideStatus(objId,status)
					});
				})
				if (status=='Started') {
					$('#started_'+objId).hide()
				}

				if (typeof timerBar !== 'undefined')
					timerBar.Restart();
			}
		},
		error: function(jqXHR, textStatus, errorThrown) {
			alert("An unexpected error occurred while attempting to update task/comment")
		}
	});
}
function truncate( text ){
	var trunc = text
	if(text){
		if(text.length > 50){
			trunc = trunc.substring(0, 50);
			trunc += '...'
		}
	}
	return trunc;
}
</script>
<script>
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366')
</script>
</body>
</html>
