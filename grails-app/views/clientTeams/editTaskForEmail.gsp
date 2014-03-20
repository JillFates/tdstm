<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
	<meta name="layout" content="projectHeader" />
	<title>Task Detail</title>
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datepicker.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
	<g:javascript src="asset.tranman.js"/>
	<g:javascript src="asset.comment.js"/>
	<g:javascript src="entity.crud.js" />
</head>
<body>
<h1 style='margin-left: 7%;'>Task Edit</h1>
<div id="editCommentDialog" title="Edit Comment/Issue" >
<g:form action="updateComment" method="post" name="editCommentForm">
 <div class="dialog" style="border: 1px solid #5F9FCF;margin-left: 7%;width: 82%;">
	<input type="hidden" name="id" id="updateCommentId" value="${assetComment?.id}" />
	<input type="hidden" name="assetName" id="assetValueId" value="${assetComment?.assetEntity?.id }" />
	<input type="hidden" id="statuWarnId" value="${statusWarn}" />
	<input type="hidden" id="commentcloseId" name="commentcloseId" value="close"/>
	<input type="hidden" id="deletePredId" name="deletePredId" />
	<input type="hidden" id="predCount"  value="-1" />
  <div>
	<table id="updateCommentTable" class="inner">
	   <% // TODO : Replace DB lookup in GSP with data from controller %>
		<tr class="prop">
			<td valign="top" class="name" id="commentEditTdId"><label><b>Task <span id="taskNumberEditId"></span>:</b></label> #:<b>${assetComment.taskNumber}</b></td>
			<td valign="top" class="value" colspan="2">
				<textarea cols="80" rows="2" id="commentEditId" name="comment">${assetComment.comment}</textarea>
			</td>
		</tr>
		<g:if test="${assignedTo}">
	   <tr class="prop issue" id="assignedToTrEditId">
			<td valign="middle" class="name"><label for="assignedTo">Assigned To:</label></td>
			<td valign="middle" id="assignedToEditTdId" class="issue"  colspan="3" nowrap="nowrap">
                <span id="assignedEditSpan">${assignedTo} </span>
				&nbsp;/&nbsp;
				<g:select id="roleTypeEdit" name="role" from="${staffRoles}" noSelection="['':'UnAssigned']" value="${assetComment.role}" optionKey="id" optionValue="${{it.description}}" onChange="roleChange(this.value)"></g:select>
				&nbsp;
				<input type="checkbox" id="hardAssignedEdit" name="hardAssignedEdit"  ${assetComment.hardAssigned==1 ? 'checked="checked"' : '' }
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;
				<label for="hardAssignedEdit">Fixed Assignment</label>
				&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
		</g:if>
		<g:if test="${assetComment?.moveEvent}">
			<tr class="prop" id="moveEventEditTrId">
				<td valign="top" class="name"><label for="moveEvent">Event:</label></td>
				<td valign="top" colspan="3">
	            <g:select id="moveEventEditId" name="moveEvent" from="${MoveEvent.findAllByProject(Project.get(session.getAttribute('CURR_PROJ').CURR_PROJ ))}"
	             optionKey='id' optionValue="name" noSelection="['':'please select']" value="${assetComment.moveEvent.id }"></g:select>
	             <% // TODO : fix so that it defaults the current value %>
				</td>
			</tr>
		</g:if>
		<tr class="prop">
			<td valign="top" class="name">
				<label for="category">Category:</label>
			</td>
			<td colspan="4">
				<g:select id="categoryEditId" name="categoryEditId" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="${assetComment.category}"
				onChange="updateWorkflowTransitions(jQuery('#assetValueId').val(), this.value, 'workFlowTransitionEditId', 'predecessorId',jQuery('#updateCommentId').val())"></g:select>
			</td>
        </tr>
        <tr id="commentTypeEditTrId">
            <td valign="top" class="name" id="commentTypeEditTdId"> 
               <label for="commentType">Type:</label>
            </td>
			<td valign="top" id="typeListTdId">
				<tds:hasPermission permission='CommentCrudView'>
					<g:select id="commentTypeEditId" name="commentType"
					from="${com.tds.asset.AssetComment.constraints.commentType.inList}" value="${assetComment?.commentType}"
					 onChange="commentChange('#editResolveDiv','editCommentForm')"></g:select>
				</tds:hasPermission>
				<div style="display: none">
					<tds:hasPermission permission='CommentCrudView'>
						<input type="text" id="commentTypeEditIdReadOnly" readonly style="border: 0;" value="${assetComment?.commentType}"/>
					</tds:hasPermission>
				</div>
			</td>
        </tr>
        <g:if test="${ assetComment.commentType=='instruction'}">
			<tr class="prop" id="mustVerifyEditTr"">
				<td valign="top" class="name"><label for="mustVerifyEditId">Must Verify:</label></td>
				<td  valign="top" class="value" colspan="3">
					<input type="checkbox" id="mustVerifyEditId" name="mustVerify" ${assetComment.mustVerify != 0 ? 'checked="checked"' : '' }
						onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
				</td>
			</tr>
		</g:if>
			<tr class="prop" id="workFlowTransitionEditTrId">
				<td valign="top" class="name"><label for="workFlowTransitionEditId">Workflow Step:</label></td>
				<td valign="top" class="value" colspan="3">
					<span id="workFlowTransitionEditId"></span>
					<input type="checkbox" id="overrideEdit" name="override" ${assetComment.workflowOverride != 0 ? 'checked="checked"' : '' }
						onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
					 <label for="overrideEdit">Overridden</label>
				</td>
			</tr>
		<tr id="assetTrId">
			<td valign="top" class="name" id="assetEditTd"><label for="asset">Asset:</label></td>
			<td valign="top">
            	<g:select id="assetSelectEditType" name="asset" from="['Application','Server','Database','Storage','Other']" 
							onchange="getAssetsList(this.value,'Edit')"></g:select>
				<g:select name="assetEntity" id="assetSelectEditId" from="${servers}" optionKey="${-2}" optionValue="${1}" 
					noSelection="${['null':'Please select']}" class="assetSelect" onchange="setAssetEditId(this.value)"></g:select>
        	</td>
		</tr>
		<g:if test="${assetComment.commentType=='issue' }">
		<tr class="prop" id="durationEditId">
        	<td valign="top" class="name"><label for="durationEdit ">Duration:</label></td>
        	<td valign="top" class="value" colspan="4">
				<input type="text" id="durationEdit" name="duration" value="${assetComment.duration?:'' }" size="3"/>
				<g:select id="durationScaleEdit" class="ynselect" name="durationScale" from="${com.tds.asset.AssetComment.constraints.durationScale.inList}" value="${assetComment.durationScale?:'m' }"/>&nbsp;&nbsp;
				<span id="priorityEditSpanId">
		        	    <label for="priority">Priority:</label>
		            	<g:select id="priorityEdit" class="ynselect" name="priority" from="${1..5}" value="${assetComment.priority?:'' }"></g:select>
	            </span> &nbsp;&nbsp;&nbsp;
	            <span id="dueDateEditSpanId">
		        	    <label for="dueDateEditId">Due Date:</label>
		        	    <script type="text/javascript" charset="utf-8">
				 		 	jQuery(function($){$('.dateEditRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                 		 </script>
		        	    <input type="text" class="dateEditRange" size="15" style="" name="dueDate" id="dueDateEditId"
							value="${dueDate }" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
	            </span>
        	</td>
	        	
		</tr>
		</g:if>
		<tr class="prop">
			<td valign="top" class="name"><label for="status">Status:</label></td>
			<td colspan="3" id="statusEditTrId">
			</td>
		</tr>

		<%-- Dependencies section --%>

		<tr class="prop">
			<td valign="top">
				Dependencies:
			</td>
			<td colspan="3"><table class="inner">
				<tr>
					<td width="50%">
						Predecessor Tasks <a class="button" id="predAddButton" href="javascript:" 
						onclick="addPredecessor('categoryEditId', '','updateCommentId', 'predecessorEditTr', 'relatedIssueEditId', 'predecessorEditTableId', 'predecessorEdit');">
						&nbsp;&nbsp;<img src="${resource(dir:'/images', file:'add16.png')}" style="vertical-align: middle;" /> Add
						</a>
					</td>
					<td width="50%"> 
						Successor Tasks <a class="button" id="succAddButton" href="javascript:"  
						onclick="addPredecessor('categoryEditId', '','updateCommentId','predecessorEditTr', 'relatedIssueEditId', 'successorEditTableId', 'successorEdit');">
						&nbsp;&nbsp;<img src="${resource(dir:'/images', file:'add16.png')}" style="vertical-align: middle;" /> Add
						</a>
					</td>
				</tr>
				<tr>
					<td valign="top"><span id="predecessorEditId"></span>&nbsp;</td>
					<td valign="top"><span id="successorEditId"></span>&nbsp;</td>
				</tr>
			</table></td>
		<tr>
		<tr id="processDiv" style="display: none;" >
			<td></td>
			<td><img id="processingId" src="../images/processing.gif" /></td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
			<td valign="top" class="value" id="createdByEditId" colspan="3">${personCreateObj} at ${dtCreated}</td>
		</tr>
	</table>
  </div>
  <div id="editResolveDiv" class="issue">
	<table id="updateResolveTable" style="border: 0px;">
		<tr class="prop">
			<td valign="top" class="name"><label for="notes">Previous Notes:</label></td>
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
	    <tr class="prop">
			<td valign="top" class="name"><label for="notes">Note:</label></td>
			<td valign="top" class="value" colspan="3">
			   <textarea cols="80" rows="4" id="noteEditId" name="note"></textarea>
			</td>
		</tr>
	    <tr class="prop issue" id="estStartEditTrId">
			<td valign="top" class="name"><label for="estStartTrId">Estimated Start:</label></td>
			<td valign="top" class="value" nowrap="nowrap">
				<script type="text/javascript" charset="utf-8">
				  jQuery(function($){$('.datetimeEditRange').datetimepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="datetimeEditRange" size="15" style="" name="estStart" id="estStartEditId"
					value="${etStart}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
			<td valign="middle" class="name"><label for="estFinishTrId">Estimated Finish:</label></td>
			<td valign="top" class="value" nowrap="nowrap">
				<input type="text" class="datetimeEditRange" size="15" style="" name="estFinish" id="estFinishEditId"
					value="${etFinish}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
		</tr>
		<g:if test="${assetComment.commentType=='issue'}">
		<tr class = "issue" id="actStartShow">
			<td valign="top" class="name"><label for="actStartEditId">Actual Start:</label></td>
			<td valign="top" class="value" id="actStartShowId">${atStart}</td>
			<td valign="top" class="name" nowrap="nowrap" width="10%"><label for="actFinishShowId">Actual Finish:</label></td>
			<td valign="top" class="value" id="actFinish" nowrap="nowrap">${dtResolved }</td>
		</tr >
		</g:if>
		</table>
  </div>
  <tds:hasPermission permission='CommentCrudView'>
<div class="buttons"><span class="button">
	<%--<input class="save" type="button" id="saveAndCloseBId" value="Save and close" onclick="resolveValidate('editCommentForm','updateCommentId','${rediectTo}','');" disabled="disabled"/>
	
	<input class="save" type="button" id="saveAndViewBId" value="Save and view" onclick="resolveValidate('editCommentForm','updateCommentId','','email');" disabled="disabled"/>
	</span>--%>
	<g:actionSubmit id="updateCloseId" class="save" value="Update/Show" action="updateEmailComment" onclick="return validatePredSucc();"/></span>
	<span id="deleteCommentId" class="button">
	<input class="delete" type="button" value="Delete"
		onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment',controller:'assetEntity', params:'\'id=\' + $(\'#updateCommentId\').val() +\'&assetEntity=\'+$(\'#assetValueId\').val() ', onComplete:'redirectToListTasks()')}" />
	</span>
</div>
<div style="display: none;">
	<table id="taskDependencyRow">
	<tr>
		<td><g:select id="predecessorCategoryId" class="predecessor" name="predecessorCategoryCreate" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="general" noSelection="['':'please select']" onChange="fillPredecessor(this.id, this.value,'')"/></td>
		<td id="taskDependencyTdId"></td>
	 </tr>
	</table>
</div>
</tds:hasPermission>
</div>
<input type="hidden" id="taskDependencyId" name="taskDependency" value=""/>
<input type="hidden" id="taskSuccessorId" name="taskSuccessor" value=""/>
<input type="hidden" id="deletedPredsId" name="deletedPreds" value=""/>
</g:form>
</div>
<div style="display: none;">
	<g:select id="Server" from="${servers}" optionKey="${-2}" optionValue="${1}" noSelection="${['null':'Please select']}"></g:select>
	<g:select id="Application" from="${applications}" optionKey="${-2}" optionValue="${1}" noSelection="${['null':'Please select']}"></g:select>
	<g:select id="Database"  from="${dbs}" optionKey="${-2}" optionValue="${1}" noSelection="${['null':'Please select']}"></g:select>
	<g:select id="Storage" from="${files}" optionKey="${-2}" optionValue="${1}" noSelection="${['null':'Please select']}"></g:select>
	<g:select id="Other" from="${networks}" optionKey="${-2}" optionValue="${1}" noSelection="${['null':'Please select']}"></g:select>
</div>
<script type="text/javascript">
$(document).ready(function() {
	if(${assetComment.commentType=='issue'}){
		updateStatusSelect('${assetComment.id}');
		if( '${assetComment.assetEntity}' ){
			console.log("if")
		       updateWorkflowTransitions('${assetComment.assetEntity?.id}', '${assetComment?.category}', "workFlowTransitionEditId", "predecessorEditId",'${assetComment.id}')
		}else{
			console.log("else")
		       updateWorkflowTransitions('', '${assetComment?.category}', "workFlowTransitionEditId", "predecessorEditId",'${assetComment.id}')
		}
		updateAssignedToList('assignedToEdit','assignedEditSpan','${assetComment.id}');
		loadEditPredecessor('${assetComment.id}');
		loadEditSucccessor('${assetComment.id}');
	}

	if('${assetComment.assetEntity}'){
   	 	assignAssetType('${assetType}',"Edit");
		$('#assetSelectEditId').val('${assetComment.assetEntity?.id}')
		
	 }else{
		$('#assetSelectEditType').val('Server')
		$('#assetSelectEditId').val('')
	}
	if(!isIE7OrLesser)
		$("select.assetSelect").select2();
});
function validatePredSucc(){
	var objId = ''
		if ($("#updateCommentId").val()) { objId = $("#updateCommentId").val() }
	
	var predArr = new Array();
	var succArr = new Array();
	$('select[name="predecessorEdit"]').each(function(){
		var predId = $(this).attr('id').split('_')[1]
		if($(this).val())
			predArr.push(predId+"_"+$(this).val())
    });
	$('select[name="successorEdit"]').each(function(){
		var succId = $(this).attr('id').split('_')[1]
		if($(this).val())
			succArr.push(succId+"_"+$(this).val())
    });
	 predArr = removeDuplicateElement( predArr )
	 succArr = removeDuplicateElement( succArr )
	 predArr = removeByElement( predArr, objId )
	 succArr = removeByElement( succArr, objId )
	 $("#taskSuccessorId").val(succArr)
	 $("#taskDependencyId").val(predArr)
	 $("#deletedPredsId").val( $('#deletePredId').val() )
	
	return true
}
</script>
</body>
</html>