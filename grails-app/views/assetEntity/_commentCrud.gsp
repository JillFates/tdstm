<%--
/*
 **************************
 * Comment List Dialog
 **************************
 */
--%>

<div id="commentsListDialog" title="Show Asset Comments" style="display: none;">
	<br/>
	<div class="list">
		<table id="listCommentsTable">
		<thead>
		<tr>
			<th nowrap>Action</th>
			<th nowrap>Comment</th>
			<th nowrap>Comment Type</th>
			<th nowrap>Due Date</th>
			<th nowrap>Resolved</th>
			<th nowrap>Category</th>
		</tr>
		</thead>
		<tbody id="listCommentsTbodyId">
		</tbody>
		</table>
	</div>
    <tds:hasPermission permission='CommentCrudView'>
	<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
		<span class="menuButton"><a id="newCommentId" class="create" href="#"
		onclick="$('#statusId').val('');
		$('#createResolveDiv').css('display','none');
		$('#createCommentDialog').dialog('option', 'width', 'auto');
		$('#createCommentDialog').dialog('option', 'position', ['center','top']);
		$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');
		$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');
		$('#editDialog').dialog('close');$('#createDialog').dialog('close');
		document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();
		$('#dueDateTrId').css('display', 'none');$('#assignedToId').css('display', 'none');" >New...</a></span>
	</div>
	</tds:hasPermission>
</div>
<%--
/*
 **************************
 * Show Comment Dialog
 **************************
 */
--%>
<div id="showCommentDialog" title="Comment/Issue detail" style="display: none;">
	<div class="dialog" style="border: 1px solid #5F9FCF"><input name="id" value="" id="commentId" type="hidden" />
	<div>
	<table id="showCommentTable" style="border: 0px;">
		<tr class = "issue" id="assignedToTrId" style="display: none">
			<td valign="top" class="name"><label for="assignedTo">Assigned To:</label></td>
			<td valign="top" class="value" id="" style=""> <span id="assignedToTdId"></span>
				<span id="roleTdId"></span>&nbsp;
				<input type="checkbox" id="hardAssignedShow" name="hardAssignedShow" value="0"
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
				<label for="hardAssignedShow" >Fixed Assignment</label>
			</td>
			<td valign="top" class="name" id="dueDateTrId"><label for="dueDate">Due Date:</label></td>
			<td valign="top" class="value" id="dueDateId"></td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
			<td valign="top" class="value" id="createdById" colspan="3"></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="commentType">Type:</label></td>
			<td valign="top" class="value" id="commentTypeTdId" colspan="3"></td>
		</tr>
		<tr class="issue" id="categoryTrId">
			<td valign="top" class="name"><label for="category">Category:</label></td>
			<td valign="top" class="value" id="categoryTdId"></td>
			<td valign="top" class="name"><label for="priorityShowId">Priority:</label></td>
			<td valign="top" class="value" id="priorityShowId"></td>
		</tr>
		<tr class="prop" id="mustVerifyId" style="display: none">
			<td valign="top" class="name" ><label for="mustVerify">Must Verify:</label></td>
			<td valign="top" class="value" id="verifyTdId" colspan="3">
				<input type="checkbox" id="mustVerifyShowId" name="mustVerify" value="0" disabled="disabled" />
			</td>
		</tr>
		<tr class="issue" id="workFlowShow" style="display: none">
			<td valign="top" class="name" nowrap="nowrap"><label for="workFlowShowId">WorkFlow Transition:</label></td>
			<td valign="top" class="value" id="workFlowShowId"></td>
			<td valign="top" class="name" colspan="2"><input type="checkbox" id="overRideShow" name="overRideShow" value="0"
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
				<label for="overRide" >Overridden</label></td>
		</tr>
		<tr class="prop" id="predecessorShowTr" style="display: none">
			<td valign="top" class="name"><label for="precessorShowId">Predecessor:</label></td>
			<td valign="top" class="value" id="predecessorShowTd" colspan="2"></td>
		</tr>
        <tr class="prop" id="predecessorTrShowId" style="display: none">
			<td valign="top" class="name"><label for="predecessorShowId">Predecessor:</label></td>
			<td valign="top" class="value" id="predecessorShowId" colspan="3"></td>
		</tr>
		<tr id="moveShowId" class="prop" style="display: none;">
			<td valign="top" class="name" id="eventTdId"><label for="moveEvent">Move Event:</label></td>
			<td valign="top" class="value" id="eventShowValueId" colspan="3"></td>
		</tr>
		<tr class="prop" style="display: none">
			<td valign="top" class="name"><label for="commentCode">Comment Code:</label></td>
			<td valign="top" class="value" id="commentCodeTdId" colspan="3"></td>
		</tr>
		<tr id="assetShowId" class="prop">
			<td valign="top" class="name" id="assetTdId"><label for="asset">Asset:</label></td>
			<td valign="top" class="value" id="assetShowValueId" colspan="3"></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="comment">Description:</label></td>
			<td valign="top" class="value" colspan="3">
				<textarea cols="80" rows="4" id="commentTdId" readonly="readonly"></textarea>
			</td>
		</tr>
		<tr class="issue" id="workFlowShow" style="display: none">
			<td valign="top" class="name"><label for="durationShowId">Duration:</label></td>
			<td valign="top" class="value" id="durationShowId" colspan="3"></td>
		</tr>
	</table>
	</div>
	<div id="showResolveDiv" style="display: none;" class="issue">
	<table id="showResolveTable" style="border: 0px">
		<tr class="prop">
			<td valign="top" class="name"><label for="previousNotes">Previous Notes:</label></td>
			<td valign="top" class="value" colspan="3">
				<div id="previousNotesShowId"></div>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="status">Status:</label></td>
			<td valign="top" class="value" id="statusShowId" colspan="3"></td>
		</tr>
		<tr class = "issue" id="estStartShow" style="display: none">
			<td valign="top" class="name"><label for="estStartShowId">Estimated Start:</label></td>
			<td valign="top" class="value" id="estStartShowId"></td>
			<td valign="top" class="name" nowrap="nowrap"><label for="estFinishShowId">Estimated Finish:</label></td>
			<td valign="top" class="value" id="estFinishShowId" nowrap="nowrap"></td>
		</tr>
		<tr class = "issue" id="actStartShow" style="display: none">
			<td valign="top" class="name"><label for="actStartShowId">Actual Start:</label></td>
			<td valign="top" class="value" id="actStartShowId"></td>
			<td valign="top" class="name" nowrap="nowrap"><label for="actFinishShowId">Actual Finish:</label></td>
			<td valign="top" class="value" id="actFinishShowId" nowrap="nowrap"></td>
		</tr >
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value" colspan="3">
            	<div id="resolutionId"></div>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="dateResolved">Resolved At:</label></td>
			<td valign="top" class="value" id="dateResolvedId" colspan="3"></td>
		</tr>
		<tr>
			<td valign="top" class="name" nowrap="nowrap"><label for="resolvedBy">Resolved By:</label></td>
			<td valign="top" class="value" id="resolvedById" colspan="3"></td>
		</tr>
   </table>
	</div>
   <tds:hasPermission permission='CommentCrudView'>
	<div class="buttons">
	<span class="button">
	<input class="edit" type="button" value="Edit"
      onclick="commentChangeEdit('editResolveDiv','editCommentForm');$('#editCommentDialog').dialog('option', 'width', 'auto');$('#editCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('open');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close')" />
	</span>
	<span class="button"> <input class="delete" type="button" value="Delete"
	onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment',controller:'assetEntity', params:'\'id=\' + $(\'#commentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'never\')')}" />
	</span>
	</div>
   </tds:hasPermission>
</div>
</div>
<%--
/*
 **************************
 * Create Comment Dialog
 **************************
 */
--%>
<div id="createCommentDialog" title="Create Asset Comment" style="display: none;">
	<input type="hidden" name="assetEntity.id" id="createAssetCommentId" value="" />
	<g:form action="saveComment" method="post" name="createCommentForm">
		<input type="hidden" name="category" value="general" />
		<input type="hidden" id="predCount"  value="1" />
  <div class="dialog" style="border: 1px solid #5F9FCF">
	<div>
		<table id="createCommentTable" style="border: 0px;">
		<tr class="prop" id="assignedToId" style="display: none">
		<% // TODO - the list of users should be in the model and not in the view %>
		<% def partyList = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType='PROJ_STAFF' and p.partyIdFrom = ? and p.roleTypeCodeFrom = 'PROJECT' " ,[Party.get(Integer.parseInt(session.getAttribute( 'CURR_PROJ' ).CURR_PROJ))]).partyIdTo;
		   def roleList = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType='PROJ_STAFF' and p.partyIdFrom = ? and p.roleTypeCodeFrom = 'PROJECT' " ,[Party.get(Integer.parseInt(session.getAttribute( 'CURR_PROJ' ).CURR_PROJ))]).roleTypeCodeTo;%>
			<td valign="top" class="name"><label for="assignedTo">Assigned To:</label></td>
			<td valign="top" nowrap="nowrap" colspan="2">
				<g:select id="assignedTo" name="assignedTo" from="${partyList}"  value="${session.getAttribute('LOGIN_PERSON').id }" optionKey="id" noSelection="['':'please select']"></g:select>

				<g:select id="roleType" name="roleType" from="${RoleType.list()}" noSelection="['':'UnAssigned']" value="" optionKey="id" optionValue="${{it.description.substring(it.description.lastIndexOf(':') +1).trim()}}" onChange="roleChange(this.value)"></g:select>
			</td>
			<td valign="top" class="name">
				<input type="checkbox" id="hardAssigned" name="hardAssigned" value="1"  checked="checked"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
				<label for="hardAssigned" >Fixed Assignment</label>&nbsp;&nbsp;
				<label for="dueDate">Due:</label>
				<script type="text/javascript" charset="utf-8">
                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="dueDate" id="dueDateCreateId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="commentType">Type:</label></td>
			<td style="">
				<g:select id="commentType" name="commentType" from="${com.tds.asset.AssetComment.constraints.commentType.inList}" value="comment"
				onChange="commentChange('#createResolveDiv','createCommentForm')"></g:select>
			</td>
        	<td valign="top" class="name"><label for="category">Category:</label>
            	<g:select id="createCategory" name="createCategory" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="general"
            	noSelection="['':'please select']" onChange="updateWorkflowTransitions(jQuery('#createAssetCommentId').val(), this.value, 'workFlowTransitionId', 'predecessorId','')"></g:select>
        	</td>
        	<td valign="top" class="name"><label for="priority">Priority:</label>
            	<g:select id="priority" name="priority" from="${1..5}" value="3"
            	noSelection="['':'please select']"></g:select>
        	</td>
		</tr>
		<tr class="prop" id="mustVerifyTr" style="display: none;">
            <td valign="top" id="mustVerifyTd" style="display: none;" colspan="4">
			<input type="checkbox" id="mustVerify" name="mustVerify" value="0"
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
			<label for="mustVerifyEdit">Must Verify</label>
			</td>
		</tr>
		<tr class="prop" id="workFlowTransitionTrId" style="display: none">
			<td valign="top" class="name"><label for="actStartTrId">WorkFlowTransition:</label></td>
			<td valign="top" class="value" id="workFlowTransitionId"></td>
			<td nowrap="nowrap" colspan="2">
			<input type="checkbox" id="overRide" name="overRide" value="0"
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
			 <label for="overRide" >Overridden</label>
			</td>
		</tr>
		<tr class="prop" id="predecessorHeadTrId" style="display: none">
			<td valign="top" class="name" colspan="4"><label for="actStartTrId">Predecessors</label>
			 <a class="button" href="javascript:" onclick="addPredecessor('predecessorCategory','','predecessorTr','relatedIssueId');"> Add </a>
			</td>
		</tr>
		<tr class="prop" id="predecessorTr" style="display: none">
			<td valign="top" class="name"><label for="actStartTrId">Pred:</label>
			</td>
			<td valign="top" class="value" id="workFlowTransitionId" colspan="3">
			   <table>
			    <tbody id="predecessorTableId"></tbody>
			   </table>
			</td>
		</tr>
        <tr class="prop" id="predecessorTrId" style="display: none">
			<td valign="top" class="name"><label for="predecessorId">Predecessor:</label></td>
			<td valign="top" class="value" id="predecessorId" colspan="3"></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"  ><label for="status">Status:</label></td>
			<td valign="top" class="value" colspan="3">
			<script type="text/javascript" charset="utf-8">
             </script>
				<g:select id="statusId" name="status" from="${com.tds.asset.AssetComment.constraints.status.inList}" value="Ready"
				noSelection="['':'please select']" onChange="showResolve(this.value)"></g:select>
			</td>
		</tr>
		<tr class="prop" id="assetEntityTrId">
        	<td valign="top" class="name"><label for="category">Asset Name:</label></td>
        	<td valign="top" class="value" colspan="3">
            	<span id="assetEntityInputId"></span>
        	</td>
		</tr>
		<tr class="prop" id="moveEventTrId" style="display: none">
			<td valign="top" class="name"><label for="moveEvent">Move Event:</label></td>
			<td valign="top" class="value" colspan="3">
				<g:select id="moveEvent" name="moveEvent" from="${MoveEvent.findAllByProject(Project.get(session.getAttribute('CURR_PROJ').CURR_PROJ ))}"
				 optionKey='id' optionValue="name" noSelection="['':'please select']"></g:select>
			</td>
		</tr>
		<tr class="prop">
			<td id="issueItemId" valign="top" class="name"><label for="comment">Description:</label></td>
			<td valign="top" class="value" colspan="3">
				<textarea cols="80" rows="4" id="comment" name="comment"></textarea>
			</td>
		</tr>
		<tr id="durationTrId" class="prop" style="display: none">
        	<td valign="top" class="name"><label for="duration ">Duration:</label></td>
        	<td valign="top" class="value" colspan="3">
        	  <input type="text" id="duration" name="duration" value="" style="width: 12%">
            	<g:select id="durationScale" name="durationScale " from="${com.tds.asset.AssetComment.constraints.durationScale.inList}" value="m">
            	</g:select>
        	</td>
		</tr>
	</table>
	</div>
	<div style="display: none;">
	<table id="taskDependencyRow">
	<tr>
		<td><g:select id="predecessorCategoryId" class="predecessor" name="predecessorCategoryCreate" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="general" noSelection="['':'please select']" onChange="fillPredecessor(this.id)"/></td>
		<td id="taskDependencyTdId"></td>
	 </tr>
	</table>
	</div>
	<div id="createResolveDiv" style="display: none;">
		<table id="createResolveTable" style="border: 0px">
		<tr class="prop" id="estStartTrId" style="display: none">
			<td valign="top" class="name"><label for="estStartTrId">Estimated start:</label></td>
			<td valign="top" class="value">
				<script type="text/javascript" charset="utf-8">
				 jQuery(function($){$('.datetimeRange').datetimepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="datetimeRange" size="15" style="width: 112px; height: 14px;" name="estStart" id="estStartCreateId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
			<td valign="top" class="name"><label for="estFinishTrId">Estimated finish:</label></td>
			<td valign="top" class="value">
				 <input type="text" class="datetimeRange" size="15" style="width: 112px; height: 14px;" name="estFinish" id="estFinishCreateId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
		</tr>
		<tr class="prop" id="actStartTrId" style="display: none">
			<td valign="top" class="name"><label for="actStartTrId">Actual Start:</label></td>
			<td valign="top" class="value" colspan="3">
				<input type="text" class="datetimeRange" size="15" style="width: 112px; height: 14px;" name="actStart" id="actStartCreateId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
		</tr>
		<tr class="prop" style="display: none;">
			<td valign="top" class="name"><label for="isResolved">Resolved:</label></td>
			<td valign="top" class="value" colspan="3">
				<input type="checkbox" id="isResolved" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
			</td>
		</tr>
		<tr class="prop" id="resolutionTrId" style="display: none;">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value" colspan="3">
				<textarea cols="80" rows="4" id="resolution" name="resolution" style=""></textarea>
			</td>
		</tr>
	</table>
    </div>
	</div>
	<tds:hasPermission permission='CommentCrudView'>
	<div class="buttons"><span class="button"> <input class="save" type="button" value="Create"
		onclick="resolveValidate('createCommentForm','createAssetCommentId','${rediectTo}');" /></span></div>
	</tds:hasPermission>
</g:form>
</div>

<%--
/*
 **************************
 * Edit Comment Dialog
 **************************
 */
--%>
<div id="editCommentDialog" title="Edit Comment/Issue" style="display: none;">
<g:form action="updateComment" method="post" name="editCommentForm">
 <div class="dialog" style="border: 1px solid #5F9FCF">
	<input type="hidden" name="id" id="updateCommentId" value="" />
	<input type="hidden" name="assetName" id="assetValueId" value="" />
  <div>
	<table id="updateCommentTable" style="border: 0px;">
	   <% // TODO : Replace DB lookup in GSP with data from controller %>
      <% def partyList = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType='PROJ_STAFF' and p.partyIdFrom = ? and p.roleTypeCodeFrom = 'PROJECT' " ,[Party.get(Integer.parseInt(session.getAttribute( 'CURR_PROJ' ).CURR_PROJ))]).partyIdTo;%>
	   <tr class="prop issue" id="assignedToTrEditId" style="display: none">
			<td valign="top" class="name"><label for="assignedTo">Assigned To:</label></td>
			<td valign="top" id="assignedToEditTdId" style="display: none;" class="issue"  colspan="2" nowrap="nowrap">
				<g:select id="assignedToEditId" name="assignedTo" from="${partyList}" value="" optionKey="id" noSelection="['':'please select']"></g:select>

				<g:select id="roleTypeEdit" name="roleTypeEdit" from="${RoleType.list()}" noSelection="['':'UnAssigned']" value="" optionKey="id" optionValue="${{it.description.substring(it.description.lastIndexOf(':') +1).trim()}}" onChange="roleChange(this.value)"></g:select>

				<input type="checkbox" id="hardAssignedEdit" name="hardAssignedEdit" value="1"  checked="checked"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
				<label for="hardAssignedEdit" >Fixed Assignment</label>
			</td>
			<td valign="top" class="name" id="dueDateEditId" ><label for="dueDate">DueDate:</label>
				<script type="text/javascript" charset="utf-8">
				jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
				</script><input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="dueDateEdit" id="dueDateEdit"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
			<td valign="top" class="value" id="createdByEditId" colspan="3"></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="commentType">Type:</label></td>
			<td valign="top" style="">
				<tds:hasPermission permission='CommentCrudView'>
					<g:select id="commentTypeEditId" name="commentType"
					from="${com.tds.asset.AssetComment.constraints.commentType.inList}" value=""
					 onChange="commentChange('#editResolveDiv','editCommentForm')"></g:select>
				</tds:hasPermission>
				<div style="display: none">
					<tds:hasPermission permission='CommentCrudView'>
						<input type="text" id="commentTypeEditIdReadOnly" readonly style="border: 0;"/>
					</tds:hasPermission>
				</div>
			</td>
			<td valign="top" class="name"><label for="category">Category:</label>
				<g:select id="categoryEditId" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="general"
				onChange="updateWorkflowTransitions(jQuery('#createAssetCommentId').val(), this.value, 'workFlowTransitionId', 'predecessorId',jQuery('#createAssetCommentId').html())"></g:select></td>
        	<td valign="top" class="name" ><label for="priority">Priority:</label>
            	<g:select id="priorityEdit" name="priorityEdit" from="${1..5}" value=""
            	noSelection="['':'please select']"></g:select>
        	</td>
        </tr>
		<tr class="prop" id="mustVerifyEditTr" style="display: none;">
			<td valign="top" class="name"><label for="mustVerifyEditId">Must Verify:</label></td>
			<td  valign="top" class="value" colspan="3">
				<input type="checkbox" id="mustVerifyEditId" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
			</td>
		</tr>
		<tr class="prop" id="workFlowTransitionEditTrId" style="display: none">
			<td valign="top" class="name"><label for="workFlowTransitionEditId">WorkFlow Step:</label></td>
			<td valign="top" class="value" id="workFlowTransitionEditId"></td>
			<td colspan="2">
			<input type="checkbox" id="overRideEdit" name="overRide" value="0"
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
			 <label for="overRideEdit" >Overridden</label>
			  <a class="button" href="javascript:" onclick="addPredecessor('predecessorCategoryEdit','updateCommentId','predecessorEditTr','relatedIssueEditId');"> Add </a>
			</td>
		</tr>
		<tr class="prop" id="predecessorEditTr" style="display: none">
			<td valign="top" class="name"><label for="predecessorEditTd">Predecessor:</label></td>
			<td valign="top" class="value" id="predecessorEditTd" colspan="2">
			  <g:select id="predecessorCategoryEdit"  from="${com.tds.asset.AssetComment.constraints.category.inList}" value="general" noSelection="['':'please select']" onChange="addPredecessor('predecessorCategoryEdit','updateCommentId','predecessorEditTr','relatedIssueEditId')"/>
			</td>
			<td id="relatedIssueEditId">
			</td>
		</tr>
        <tr class="prop" id="predecessorTrEditId" style="display: none">
			<td valign="top" class="name"><label for="predecessorEditId">Predecessor:</label></td>
			<td valign="top" class="value" id="predecessorEditId" colspan="3"></td>
		</tr>
		<tr class="prop" id="moveEventEditTrId" style="display: none">
			<td valign="top" class="name"><label for="moveEvent">Move Event:</label></td>
			<td valign="top" colspan="3">
            <g:select id="moveEventEditId" name="moveEvent" from="${MoveEvent.findAllByProject(Project.get(session.getAttribute('CURR_PROJ').CURR_PROJ ))}"
             optionKey='id' optionValue="name" noSelection="['':'please select']"></g:select>
             <% // TODO : fix so that it defaults the current value %>
			</td>
		</tr>
		<tr class="prop" style="display: none">
			<td valign="top" class="name"><label for="commentCode">Comment Code:</label></td>
			<td valign="top" class="value" id="commentCodeEditId" colspan="3"></td>
		</tr>
		<tr id="assetTrId" class="prop">
			<td valign="top" class="name" id="assetEditTd"><label for="asset">Asset:</label></td>
			<td valign="top" class="value"  id="assetTrShowId" colspan="3"></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" id="commentEditTdId"><label for="comment">Description:</label></td>
			<td valign="top" class="value" colspan="3">
				<textarea cols="80" rows="4" id="commentEditId" name="comment"></textarea>
			</td>
		</tr>
		<tr id="durationEditId" class="prop" style="display: none">
        	<td valign="top" class="name"><label for="durationEdit ">Duration:</label></td>
        	<td valign="top" class="value" colspan="3">
        	  <input type="text" id="durationEdit" name="durationEdit" value="" style="width: 12%">
            	<g:select id="durationScaleEdit" name="durationScaleEdit " from="${com.tds.asset.AssetComment.constraints.durationScale.inList}" value="m">
            	</g:select>
        	</td>
		</tr>
	</table>
  </div>
  <div id="editResolveDiv" style="display: none;" class="issue">
	<table id="updateResolveTable" style="border: 0px;">
		<tr class="prop">
			<td valign="top" class="name"><label for="notes">Previous Notes:</label></td>
			<td valign="top" class="value" colspan="3"><div id="previousNote" style="width: 100%;"></div></td>
		</tr>
	    <tr class="prop">
			<td valign="top" class="name"><label for="notes">Note:</label></td>
			<td valign="top" class="value" colspan="3">
			   <textarea cols="80" rows="4" id="noteEditId" name="note"></textarea>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="status">Status:</label></td>
			<td colspan="3">
			<script type="text/javascript" charset="utf-8">
             </script>
				<g:select id="statusEditId" name="statusEdit" from="${com.tds.asset.AssetComment.constraints.status.inList}" value="Pending"
				noSelection="['':'please select']" onChange="showResolve(this.value)"></g:select>
			</td>
		</tr>
	    <tr class="prop issue" id="estStartEditId" style="display: none">
			<td valign="top" class="name"><label for="estStartTrId">Estimated start:</label></td>
			<td valign="top" class="value" nowrap="nowrap">
				<script type="text/javascript" charset="utf-8">
				  jQuery(function($){$('.datetimeEditRange').datetimepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="datetimeRange" size="15" style="width: 112px; height: 14px;" name="estStart" id="estStartEditId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
			<td valign="top" class="name"><label for="estFinishTrId">Estimated finish:</label></td>
			<td valign="top" class="value" nowrap="nowrap">
                   <input type="text" class="datetimeEditRange" size="15" style="width: 112px; height: 14px;" name="estFinishEditId" id="estFinishEditId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
		</tr>
		<tr class="prop" id="actStartTrEditId" style="display: none">
			<td valign="top" class="name"><label for="actStartEditTrId">Actual Start:</label></td>
			<td valign="top" class="value" colspan="3">
				 <input type="text" class="datetimeEditRange" size="15" style="width: 112px; height: 14px;" name="actStartEditId" id="actStartEditId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
		</tr>
		<tr class="prop" style="display: none;">
			<td valign="top" class="name"><label for="isResolved">Resolved:</label></td>
			<td valign="top" class="value" colspan="3">
				<input type="checkbox" id="isResolvedEditId" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
			</td>
		</tr>
		<tr class="prop" id="resolutionEditTrId" style="display: none;">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value" colspan="3">
				<textarea cols="80" rows="4" id="resolutionEditId" name="resolution"></textarea>
			</td>
		</tr>
		</table>
  </div>
</div>
<tds:hasPermission permission='CommentCrudView'>
<div class="buttons"><span class="button">
	<input class="save" type="button" value="Update" onclick="resolveValidate('editCommentForm','updateCommentId','${rediectTo}');" />
	</span>
	<span class="button">
	<g:if test="${rediectTo}">
	<input class="delete" type="button" value="Delete"
		onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment',controller:'assetEntity', params:'\'id=\' + $(\'#updateCommentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'updateCommentsLists()')}" />
	</g:if>
	<g:else>
	<input class="delete" type="button" value="Delete"
		onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment',controller:'assetEntity', params:'\'id=\' + $(\'#updateCommentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'never\')')}" />
	</g:else>
	</span>
</div>
</tds:hasPermission>
</g:form>
</div>