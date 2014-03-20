<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
	<meta name="layout" content="projectHeader" />
	<title>Task Detail</title>
	<g:javascript src="asset.tranman.js"/>
	<g:javascript src="entity.crud.js" />
	<g:javascript src="cabling.js"/>
</head>
<body>
<div id="showTaskForEmailId" title="Comment/Issue detail">
	<h1 style='margin-left: 7%;'>Task Detail</h1>
	<div class="dialog" style="border: 1px solid #5F9FCF;margin-left: 7%;width: 82%;">
	<input name="id" value="${assetComment?.id}" id="commentId" type="hidden" />
	<input type="hidden" name="assetName" id="assetValueId" value="${assetComment?.assetEntity?.id }" />
	<div>
	<table id="showCommentTable" style="border: 0px;">
		<tr class="prop">
			<td valign="top" class="name"><label for="comment"><b><span id="taskNumberId">Task #:${assetComment?.taskNumber}</span>:</b></label></td>
			<td valign="top" class="value" colspan="3">
				<textarea cols="80" rows="2" id="commentTdId" readonly="readonly">${assetComment?.comment}</textarea>
			</td>
		</tr>
		<g:if test="${assignedTo}">
			<tr class = "issue" id="assignedToTrId">
				<td valign="top" class="name"><label for="assignedTo">Assigned:</label></td>
				<td valign="top" class="value" id="" colspan="3">
					<span id="assignedToTdId">${assignedTo}</span>&nbsp;/&nbsp;<span id="roleTdId">${roles}</span>&nbsp;&nbsp;
					<input type="checkbox" id="hardAssignedShow" name="hardAssignedShow" ${assetComment?.hardAssigned==1 ?' checked="checked" ' : ''}
						disabled='disabled'/>&nbsp;&nbsp;
					<label for="hardAssignedShow" >Fixed Assignment</label>&nbsp;&nbsp;
				</td>
			</tr>
		</g:if>
		<g:if test="${assetComment?.moveEvent}">
			<tr id="moveShowId" class="prop">
				<td valign="top" class="name" id="eventTdId"><label for="moveEvent">Event:</label></td>
				<td valign="top" class="value" id="eventShowValueId" colspan="3">${assetComment?.moveEvent}</td>
			</tr>
		</g:if>
		<tr class="issue" id="categoryTrId">
			<td valign="top" class="name"><label for="category">Category:</label></td>
			<td valign="top" class="value" id="categoryTdId" style="width:15%">${assetComment?.category}</td>
			<td>
			</td>
		</tr>
		<g:if test = "${workflow }">
			<tr class="issue" id="workFlowShow">
				<td valign="top" class="name" nowrap="nowrap"><label for="workFlowShowId">WorkFlow Step:</label></td>
				<td valign="top" class="value" id="workFlowShowId">${workflow }</td>
				<td valign="top" colspan="2">
				<input type="checkbox" id="overrideShow" name="overrideShow" ${assetComment?.workflowOverride ? 'checked="checked"' : ''}
					disabled='disabled' />&nbsp;&nbsp;
					<label for="override" >Overridden</label>&nbsp;&nbsp;</td>
			</tr>
		</g:if>
		<tr id="assetShowId" class="prop">
			<td valign="top" class="name" id="assetTdId"><label for="asset">Asset:</label></td>
			<td valign="top" class="value" id="assetShowValueId" colspan="3"><a href="javascript:getEntityDetails('listTask','${assetType}', '${assetId}')">${assetName}</a></td>
		</tr>
		<g:if test = "${workflow}">
			<tr class="issue" id="workFlowShow">
				<td valign="top" class="name"><label for="durationShowId">Duration:</label></td>
				<td valign="top" class="value"colspan="3" nowrap="nowrap">
					<span id="durationShowId" >${assetComment.duration ? assetComment.duration :''}</span>
					<span id="durationScale" >${assetComment.durationScale ? assetComment.durationScale :''}</span>
					&nbsp;&nbsp;&nbsp;&nbsp;
					<span ><label for="priorityShowId">Priority:</label></span>
					<span id="priorityShowId">${assetComment.priority}</span>
					&nbsp;&nbsp;&nbsp;&nbsp;
					<span ><label for="dueDateShowId">Due Date:</label></span>
					<span id="dueDateShowId">${assetComment.dueDate}</span>
				</td>
			</tr>
		</g:if>
    	<tr class="prop">
			<td valign="top" class="name"><label for="status">Status:</label></td>
			<td valign="top" class="value" id="statusShowId" colspan="1" style="width: 20%">&nbsp;${assetComment?.status}</td>
		</tr>


		<%-- Dependencies Section --%> 

		<tr>
			<td valign="top">Dependencies:</td>
			<td valign="top" colspan="3">
				<table class="inner">
					<tr>
						<td width="50%">Predecessors:</td>
						<td width="50%">Successors:</td>
					</tr>
					<tr>
						<td id="predecessorShowTd">${predecessorTable}</td>
						<td id="successorShowTd">${successorTable}</td>
					</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
			<td valign="top" class="value" id="createdById" colspan="3">${personCreateObj} at ${dtCreated}</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="previousNotes">Prev. Notes:</label></td>
			<td valign="top" class="value" colspan="3">
				<div id="previousNotesShowId">
					<table style="border:0px">
					<g:each in="${notes}" var='note'>
	      		    	<tr>
	      		    		<td>${note[0] }</td>
	      		    		<td>${note[1] }</td>
	      		    		<td><span>${note[2] }</span></td></tr>
	      		    	</g:each>
					</table>
				</div>
			</td>
		</tr>
		<g:if test="${ assetComment?.commentType=='issue' }">
		<tr class = "issue" id="estStartShow">
			<td valign="top" class="name" nowrap="nowrap"><label for="estStartShowId">Estimated Start:</label></td>
			<td valign="top" class="value" id="estStartShowId" nowrap="nowrap">${etStart}</td>
			<td valign="top" class="name" nowrap="nowrap"><label for="estFinishShowId">Estimated Finish:</label></td>
			<td valign="top" class="value" id="estFinishShowId" nowrap="nowrap">${etFinish}</td>
		</tr>
		<tr class = "issue" id="actStartShow" >
			<td valign="top" class="name"><label for="actStartShowId">Actual Start:</label></td>
			<td valign="top" class="value" id="actStartShowId">${atStart}</td>
			<td valign="top" class="name" nowrap="nowrap" width="10%"><label for="actFinishShowId">Actual Finish:</label></td>
			<td valign="top" class="value" id="actFinishShowId" nowrap="nowrap">${atFinsih}</td>
		</tr >
		</g:if>
        <tr class="prop" id="predecessorTrShowId" style="display: none">
			<td valign="top" class="name"><label for="predecessorShowId">Predecessor:</label></td>
			<td valign="top" class="value" id="predecessorShowId" colspan="3"></td>
		</tr>
		<g:if test="${ assetComment?.commentType!='issue' }">
		<tr class="prop" id="commentShowTrId">
			<td valign="top" class="name"><label for="commentType">Type:</label></td>
			<td valign="top" class="value" id="commentTypeTdId" colspan="3">${assetComment?.commentType }</td>
		</tr>
		</g:if>
		<g:if test="${ assetComment?.commentType=='instruction'}">
			<tr class="prop" id="mustVerifyId" style="display: none">
				<td valign="top" class="name" ><label for="mustVerify">Must Verify:</label>${assetComment?.mustVerify }</td>
				<td valign="top" class="value" id="verifyTdId" colspan="3">
					<input type="checkbox" id="mustVerifyShowId" name="mustVerify" ${assetComment.mustVerify != 0 ? 'checked="checked"' : '' } disabled="disabled" />
				</td>
			</tr>
		</g:if>
	</table>
	<g:if test="${ assetComment?.commentType=='issue' }">
		<div id="showResolveDiv" class="issue"></div>
	</g:if>
	<table id="showResolveTable" style="border: 0px">
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value" colspan="6">
            	<div id="resolutionId">${assetComment?.resolution }</div>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name" nowrap="nowrap"><label for="resolvedBy">Resolved By:</label></td>
			<td valign="top" class="value" id="resolvedById" nowrap="nowrap">${(personResolvedObj!=null)? personResolvedObj : '' }</td>
		</tr>
   </table>
	</div>
   <tds:hasPermission permission='CommentCrudView'>
		<div class="buttons" style="white-space: nowrap;">
			<span class="button" class="slide"> 
			<span class="slide">
				<a href="${createLink(controller:'clientTeams',  action:'editTaskForEmail', params:['id': assetComment?.id],  absolute:"true")}">
				<input class="edit" type="button" value="Edit" id="commentButtonEditId" /></a>
			</span>
				<input class="delete" type="button" value="Delete"
					onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment',controller:'assetEntity', params:'\'id=\' + $(\'#commentId\').val() +\'&assetEntity=\'+$(\'#assetValueId\').val() ', onComplete:'redirectToListTasks()')}" />
			</span>
			<span id='actionBarId' class="slide"></span>
		</div>
	</tds:hasPermission>
</div>
</div>
<g:render template="../assetEntity/commentCrud"/>
<g:render template="../assetEntity/modelDialog"/>
<div id="createEntityView" style="display: none;"></div>
<div id="showEntityView" style="display: none;"></div>
<div id="editEntityView" style="display: none;"></div>
<div id="editManufacturerView" style="display: none;"></div>
<div id="cablingDialogId" style="display: none;"></div>
<g:render template="../assetEntity/newDependency" model="['forWhom':'Server', entities:servers]"></g:render>
<script>
	currentMenuId = "#teamMenuId";
	$("#teamMenuId a").css('background-color','#003366')
	$(document).ready(function() {
		$("#createEntityView").dialog({ autoOpen: false })
		$("#showEntityView").dialog({ autoOpen: false })
		$("#editEntityView").dialog({ autoOpen: false })
		$("#commentsListDialog").dialog({ autoOpen: false })
		$("#createCommentDialog").dialog({ autoOpen: false })
		$("#showCommentDialog").dialog({ autoOpen: false })
		$("#editCommentDialog").dialog({ autoOpen: false })
		$("#manufacturerShowDialog").dialog({ autoOpen: false })
		$("#modelShowDialog").dialog({ autoOpen: false })
		$("#editManufacturerView").dialog({ autoOpen: false})
		$("#cablingDialogId").dialog({ autoOpen:false })
	});
</script>
</body>
</html>