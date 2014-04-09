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
		<span class="menuButton"><a id="newCommentId" class="create" href="javascript:"
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
 * Show Comment/TASK Dialog
 **************************
 */
--%>
<div id="showCommentDialog" title="Comment/Issue detail" style="display: none;">
    <input id="assetEntityIdShow" type="hidden" value=""/>
	<div class="dialog" style="border: 1px solid #5F9FCF"><input name="id" value="" id="commentId" type="hidden" />
	<div>
	<table id="showCommentTable" style="border: 0px;">
		<tr class="prop">
			<td valign="top" class="name"><label for="comment"><b><span id="taskNumberId"></span>:</b></label></td>
			<td valign="top" class="value" colspan="3">
				<textarea cols="80" rows="2" id="commentTdId" readonly="readonly"></textarea>
			</td>
		</tr>
		<tr class = "issue" id="assignedToTrId" style="display: none">
			<td valign="top" class="name"><label for="assignedTo">Assigned:</label></td>
			<td valign="top" class="value" id="" colspan="3">
				<span id="assignedToTdId"></span>&nbsp;/&nbsp;<span id="roleTdId"></span>&nbsp;&nbsp;
				<input type="checkbox" id="hardAssignedShow" name="hardAssignedShow" value="0"
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
				<label for="hardAssignedShow" >Fixed Assignment</label>&nbsp;&nbsp;
			</td>
		</tr>
		<tr id="moveShowId" class="prop" style="display: none;">
			<td valign="top" class="name" id="eventTdId"><label for="moveEvent">Event:</label></td>
			<td valign="top" class="value" id="eventShowValueId" colspan="3"></td>
		</tr>
		<tr class="issue" id="categoryTrId">
			<td valign="top" class="name"><label for="category">Category:</label></td>
			<td valign="top" class="value" id="categoryTdId" style="width:15%"></td>
			<td>
			</td>
		</tr>
		<tr class="issue" id="workFlowShow" style="display: none">
			<td valign="top" class="name" nowrap="nowrap"><label for="workFlowShowId">WorkFlow Step:</label></td>
			<td valign="top" class="value" id="workFlowShowId"></td>
			<td valign="top" class="name" colspan="2"><input type="checkbox" id="overrideShow" name="overrideShow" value="0"
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
				<label for="override" >Overridden</label></td>
		</tr>
		<tr id="assetShowId" class="prop">
			<td valign="top" class="name" id="assetTdId"><label for="asset">Asset:</label></td>
			<td valign="top" class="value" id="assetShowValueId" colspan="3"></td>
		</tr>
		<tr class="issue" id="workFlowShow" style="display: none">
			<td valign="top" class="name"><label for="durationShowId">Duration:</label></td>
			<td valign="top" class="value"colspan="3" nowrap="nowrap">
				<span id="durationShowId" ></span>
				<span id="durationScale" ></span>
				&nbsp;&nbsp;&nbsp;&nbsp;
				<span ><label for="priorityShowId">Priority:</label></span>
				<span id="priorityShowId"></span>
				&nbsp;&nbsp;&nbsp;&nbsp;
				<span ><label for="dueDateShowId">Due Date:</label></span>
				<span id="dueDateShowId"></span>
			</td>
		</tr>
    	<tr class="prop">
			<td valign="top" class="name"><label for="status">Status:</label></td>
			<td valign="top" class="value" id="statusShowId" colspan="1" style="width: 20%">&nbsp;</td>
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
						<td id="predecessorShowTd"></td>
						<td id="successorShowTd"></td>
					</tr>
				</table>
			</td>
		</tr>
		<%-- Disabled the expand/collapse logic for the time being
			<td colspan="2">
				Dependencies: Predecessors (<span class="predecssorCount">&nbsp;</span>) &nbsp;
				<span class="Arrowcursor rightArrowShow"><img alt="" style="vertical-align: middle;" src="${resource(dir:'images',file:'triangle_down.png')}" onclick="toggleDependencies('right','Show')"/></span>
				<span class="Arrowcursor leftArrowShow" style="display:none;"><img alt="" style="vertical-align: middle;" src="${resource(dir:'images',file:'triangle_up.png')}" onclick="toggleDependencies('left','Show')"/></span>
			</td>
			<td></td>
			<td>
				Successors (<span class="successorCount">&nbsp;</span>) &nbsp;
				<span class="Arrowcursor rightArrowShow"><img alt="" class="predArrow" src="${resource(dir:'images',file:'triangle_down.png')}" onclick="toggleDependencies('right','Show')"/> </span>
				<span class="Arrowcursor leftArrowShow" style="display:none;"><img alt="" class="predArrow" src="${resource(dir:'images',file:'triangle_up.png')}" onclick="toggleDependencies('left','Show')"/></span>
			</td>
			<td></td>
		</tr>
		--%>
		<%-- ---------------------- --%>


		<tr>
			<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
			<td valign="top" class="value" id="createdById" colspan="3"></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="previousNotes">Prev. Notes:</label></td>
			<td valign="top" class="value" colspan="3">
				<div id="previousNotesShowId"></div>
			</td>
		</tr>
		<tr class = "issue" id="estStartShow" style="display: none">
			<td valign="top" class="name" nowrap="nowrap"><label for="estStartShowId">Estimated Start:</label></td>
			<td valign="top" class="value" id="estStartShowId" nowrap="nowrap"></td>
			<td valign="top" class="name" nowrap="nowrap"><label for="estFinishShowId">Estimated Finish:</label></td>
			<td valign="top" class="value" id="estFinishShowId" nowrap="nowrap"></td>
		</tr>
		<tr class = "issue" id="actStartShow" style="display: none">
			<td valign="top" class="name"><label for="actStartShowId">Actual Start:</label></td>
			<td valign="top" class="value" id="actStartShowId"></td>
			<td valign="top" class="name" nowrap="nowrap" width="10%"><label for="actFinishShowId">Actual Finish:</label></td>
			<td valign="top" class="value" id="actFinishShowId" nowrap="nowrap"></td>
		</tr >
        <tr class="prop" id="predecessorTrShowId" style="display: none">
			<td valign="top" class="name"><label for="predecessorShowId">Predecessor:</label></td>
			<td valign="top" class="value" id="predecessorShowId" colspan="3"></td>
		</tr>
		<tr class="prop" id="commentShowTrId">
			<td valign="top" class="name"><label for="commentType">Type:</label></td>
			<td valign="top" class="value" id="commentTypeTdId" colspan="3"></td>
		</tr>
		<tr class="prop" id="mustVerifyId" style="display: none">
			<td valign="top" class="name" ><label for="mustVerify">Must Verify:</label></td>
			<td valign="top" class="value" id="verifyTdId" colspan="3">
				<input type="checkbox" id="mustVerifyShowId" name="mustVerify" value="0" disabled="disabled" />
			</td>
		</tr>
	</table>
	<div id="showResolveDiv" style="display: none;" class="issue">
	</div>
	<table id="showResolveTable" style="border: 0px">
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value" colspan="6">
            	<div id="resolutionId"></div>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name" nowrap="nowrap"><label for="resolvedBy">Resolved By:</label></td>
			<td valign="top" class="value" id="resolvedById" nowrap="nowrap"></td>
		</tr>
   </table>
	</div>
   <tds:hasPermission permission='CommentCrudView'>
		<div class="buttons" style="white-space: nowrap;">
			<span class="button" class="slide"> 
			<span class="slide">
				<input class="edit" type="button" value="Edit" id="commentButtonEditId" />
			</span>
				<span id="fromAssetId" class="slide">
					<input class="delete" type="button" value="Delete" onclick="deleteComment('#commentId','#assetEntityIdShow','update')" />
				</span> 
				<span id="fromMoveEventId" style="display: none" class="slide"> 
					<input class="delete" type="button" value="Delete" onclick="deleteComment('#commentId','#assetEntityIdShow','refresh')" />
				</span>
			</span>
			<span id='actionBarId' class="slide"></span>
		</div>
	</tds:hasPermission>
</div>
</div>
<%--
/*
 **************************
 * Create Comment/TASK Dialog
 **************************
 */
--%>
<div id="createCommentDialog" title="Create Asset Comment" style="display: none;">
	<input type="hidden" name="assetEntity.id" id="createAssetCommentId" value="" />
	<g:form action="saveComment" method="post" name="createCommentForm">
		<input type="hidden" name="category" value="general" />
		<input type="hidden" id="predCount"  value="-1" />
		<input type="hidden" id="deletePredId" name="deletePredId" />
		<input type="hidden" id="commentFromId" name="commentFromId" />
		<input type="hidden" name="prevAssetId" id="prevAssetId" value="" />
  <div class="dialog" style="border: 1px solid #5F9FCF">
	<div>
		<table id="createCommentTable" style="border: 0px;">
		<tr class="prop">
			<td id="issueItemId" valign="top" class="name"><label for="comment">Task Desc:</label></td>
			<td valign="top" class="value" colspan="3">
				<textarea cols="80" rows="2" id="comment" name="comment"></textarea>
			</td>
		</tr>
		<tr class="prop" id="assignedToId" style="display: none">
			<td valign="middle" class="name"><label for="assignedTo">Assigned:</label></td>
			<td valign="middle" nowrap="nowrap" colspan="3">
				<span id="assignedCreateSpan"></span>&nbsp;/&nbsp;
				<g:select id="roleType" name="roleType" from="${staffRoles}" noSelection="['':'Unassigned']" value="" optionKey="id" optionValue="${{it.description}}" onChange="roleChange(this.value)"></g:select> &nbsp;
				<input type="checkbox" id="hardAssigned" name="hardAssigned" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;
				<label for="hardAssigned" >Fixed Assignment</label>&nbsp;&nbsp;
			</td>
		</tr>
		<tr class="prop" id="moveEventTrId" style="display: none">
			<td valign="top" class="name"><label for="moveEvent">Event:</label></td>
			<td valign="top" class="value" colspan="3">
				<g:select id="moveEvent" name="moveEvent" from="${MoveEvent.findAllByProject(Project.get(session.getAttribute('CURR_PROJ').CURR_PROJ ))}"
				 optionKey='id' optionValue="name" noSelection="['':'please select']" value="${moveEvent?.id}"></g:select>
			</td>
		</tr>
		<tr class="prop" >
			<td>
				<label id="categoryLabelId" for="category" >Category:</label>
			</td>
			<td nowrap="nowrap">
            	<g:select id="createCategory" name="createCategory" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="general"
            	noSelection="['':'please select']" onChange="updateWorkflowTransitions(jQuery('#createAssetCommentId').val(), this.value, 'workFlowTransitionId', 'predecessorId','')"></g:select>
			</td>
		</tr>
		<tr class="prop" id="workFlowTransitionTrId" style="display: none">
			<td valign="top" class="name"><label>WorkFlow Step:</label></td>
			<td valign="top" class="value" id="workFlowTransitionId" >
			<input type="checkbox" id="override" name="override" value="0"
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
			 <label for="override" >Overridden</label>
			</td>
		</tr>
		<tr id="assetEntityTrId">
        	<td valign="top" class="name"><label for="category">Asset Name:</label></td>
        	<td valign="top" nowrap="nowrap">
            	<g:select id="assetSelectCreateType" name="asset" from="['Application','Server','Database','Storage','Other']" 
							onchange="getAssetsList(this.value,'Create')"></g:select>
            	<g:select name="assetEntity" id="assetSelectCreateId" from="${servers}" optionKey="${-2}" optionValue="${1}" 
					noSelection="${['null':'Please select']}" class="assetSelect" onchange="setAssetId(this.value)"></g:select>
        	</td>
		</tr>
		<tr id="durationTrId" class="prop" style="display: none">
        	<td valign="top" class="name"><label for="duration ">Duration:</label></td>
        	<td valign="top" class="value" colspan="3">
        	  <input type="text" id="duration" name="duration" value="" size="3">
            	<g:select id="durationScale" class="ynselect" name="durationScale " from="${com.tds.asset.AssetComment.constraints.durationScale.inList}" value="m"/>
            	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              	<span id="priorityCreateSpanId" style="display: none">
	        		<label for="priority">Priority:</label>
	            	<g:select id="priority" class="ynselect" name="priority" from="${1..5}" value="3"></g:select>
            	</span>
            	&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
              	<span id="dueDateCreateSpanId" >
              		 <label for="dueDateCreateId">Due Date:</label>
	        		 <input type="text" class="dateEditRange" size="15" style="" name="dueDateCreateId" id="dueDateCreateId"
							value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
            	</span>
        	</td>
		</tr>
		<tr id="statusCreateTrId"  class="prop" style="display: none">
			<td valign="top" class="name"  ><label for="status">Status:</label></td>
			<td valign="top" class="value" colspan="3" id="statusCreateTdId">
			</td>
		</tr>
		<tr class="prop" id="predecessorHeadTrId" style="display: none">
			<td valign="top" class="name" colspan="2">
				<label>Predecessors</label>
				<a class="button" href="javascript:" onclick="addPredecessor('createCategory','','','predecessorTr','relatedIssueId','predecessorTableId','predecessorSave');"> Add </a>
			</td>
			<td valign="top" class="name" >
				<label>Successors</label>
				<a class="button" href="javascript:" onclick="addPredecessor('createCategory','','','successorTr','relatedIssueId','successorTableId', 'successorSave');"> Add </a>
			</td>
		</tr>
		<tr class="prop" id="predecessorTr" style="display: none">
			<td valign="top" class="name">
			</td>
			<td valign="top" class="value">
			   <table style="border: 0px">
			    <tbody id="predecessorTableId"></tbody>
			   </table>
			</td>
			<td valign="top" class="name">&nbsp;</td>
			<td valign="top" class="value">
			   <table style="border: 0px">
			    <tbody id="successorTableId"></tbody>
			   </table>
			</td>
		</tr>
		<tr class="prop" id="estStartTrId" style="display: none">
			<td valign="top" class="name"><label for="estStartTrId">Estimated start:</label></td>
			<td valign="top" class="value">
			      <script type="text/javascript">
				   jQuery(function($){$('.datetimeRange').datetimepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="datetimeRange" size="15" style="" name="estStart" id="estStartCreateId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
				&nbsp;&nbsp;
			<td valign="top" class="name"><label for="estFinishTrId">Estimated finish:</label></td>
			<td valign="top" class="value">
				<input type="text" class="datetimeRange" size="15" style="" name="estFinish" id="estFinishCreateId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
		</tr>
		<tr class="prop" >
			<td valign="top" class="name" id="typeCommentCreateId" ><label for="commentType">Type:</label></td>
			<td  id="commentTypeCreateTdId" class="name" nowrap="nowrap" colspan="3">
				<g:select id="commentType" name="commentType" from="${com.tds.asset.AssetComment.constraints.commentType.inList}" value="comment"
				onChange="commentChange('#createResolveDiv','createCommentForm')"></g:select>&nbsp;&nbsp;
			</td>
		</tr>
		<tr class="prop" id="mustVerifyTr" style="display: none;">
            <td valign="top" id="mustVerifyTd" style="display: none;" colspan="4">
			<input type="checkbox" id="mustVerify" name="mustVerify" value="0"
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
			<label for="mustVerifyEdit">Must Verify</label>
			</td>
		</tr>
	</table>
	</div>
	<div style="display: none;">
	<table id="taskDependencyRow">
	<tr>
		<td><g:select id="predecessorCategoryId" class="predecessor" name="predecessorCategoryCreate" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="general" noSelection="['':'please select']" onChange="fillPredecessor(this.id, this.value,'')"/></td>
		<td id="taskDependencyTdId"></td>
	 </tr>
	</table>
	</div>
	<div id="createResolveDiv" style="display: none;">
		<table id="createResolveTable" style="border: 0px">
		<tr class="prop" id="actStartTrId" style="display: none">
			<td valign="top" class="name"><label for="actStartTrId">Actual Start:</label></td>
			<td valign="top" class="value" colspan="3">
				<input type="text" class="datetimeRange" size="15" style="" name="actStart" id="actStartCreateId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
		</tr>
	</table>
    </div>
	</div>
	<tds:hasPermission permission='CommentCrudView'>
	<div class="buttons"><span class="button"> <input class="save" type="button" value="Save"
		onclick="resolveValidate('createCommentForm','createAssetCommentId','${rediectTo}','');" /></span></div>
	</tds:hasPermission>
</g:form>
</div>
<%--
/*
 **************************
 * assets select section
 **************************
 */
--%>
<div style="display: none;">
	<g:select id="Server" from="${servers}" optionKey="${-2}" optionValue="${1}" noSelection="${['null':'Please select']}"></g:select>
	<g:select id="Application" from="${applications}" optionKey="${-2}" optionValue="${1}" noSelection="${['null':'Please select']}"></g:select>
	<g:select id="Database"  from="${dbs}" optionKey="${-2}" optionValue="${1}" noSelection="${['null':'Please select']}"></g:select>
	<g:select id="Storage" from="${files}" optionKey="${-2}" optionValue="${1}" noSelection="${['null':'Please select']}"></g:select>
	<g:select id="Other" from="${networks}" optionKey="${-2}" optionValue="${1}" noSelection="${['null':'Please select']}"></g:select>
</div>
<%--
/*
 **************************
 * Edit Comment Dialog
 **************************
 */
--%>
<div id="editCommentDialog" title="Edit Comment/Issue" style="display: none;width: 800px">
<g:form action="updateComment" method="post" name="editCommentForm">
 <div class="dialog" style="border: 1px solid #5F9FCF">
	<input type="hidden" name="id" id="updateCommentId" value="" />
	<input type="hidden" name="assetName" id="assetValueId" value="" />
	<input type="hidden" name="prevAssetEdit" id="prevAssetEditId" value="" />
	<input type="hidden" id="statuWarnId" value="" />
	<input type="hidden" id="commentcloseId" name="commentcloseId" />
  <div>
	<table id="updateCommentTable" class="inner">
	   <% // TODO : Replace DB lookup in GSP with data from controller %>
		<tr class="prop">
			<td valign="top" class="name" id="commentEditTdId"><label><b>Task <span id="taskNumberEditId"></span>:</b></label></label></td>
			<td valign="top" class="value" colspan="2">
				<textarea cols="80" rows="2" id="commentEditId" name="comment"></textarea>
			</td>
		</tr>
	   <tr class="prop issue" id="assignedToTrEditId" style="display: none">
			<td valign="middle" class="name"><label for="assignedTo">Assigned To:</label></td>
			<td valign="middle" id="assignedToEditTdId" style="display: none;" class="issue"  colspan="3" nowrap="nowrap">
                <span id="assignedEditSpan"> </span>
				&nbsp;/&nbsp;
				<g:select id="roleTypeEdit" name="roleTypeEdit" from="${staffRoles}" noSelection="['':'UnAssigned']" value="" optionKey="id" optionValue="${{it.description}}" onChange="roleChange(this.value)"></g:select>
				&nbsp;
				<input type="checkbox" id="hardAssignedEdit" name="hardAssignedEdit" value="1"  checked="checked"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;
				<label for="hardAssignedEdit">Fixed Assignment</label>
				&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
		<tr class="prop" id="moveEventEditTrId" style="display: none">
			<td valign="top" class="name"><label for="moveEvent">Event:</label></td>
			<td valign="top" colspan="3">
            <g:select id="moveEventEditId" name="moveEvent" from="${MoveEvent.findAllByProject(Project.get(session.getAttribute('CURR_PROJ').CURR_PROJ ))}"
             optionKey='id' optionValue="name" noSelection="['':'please select']"></g:select>
             <% // TODO : fix so that it defaults the current value %>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name">
				<label for="category">Category:</label>
			</td>
			<td colspan="4">
				<g:select id="categoryEditId" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="general"
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
					from="${com.tds.asset.AssetComment.constraints.commentType.inList}" value=""
					 onChange="commentChange('#editResolveDiv','editCommentForm')"></g:select>
				</tds:hasPermission>
				<div style="display: none">
					<tds:hasPermission permission='CommentCrudView'>
						<input type="text" id="commentTypeEditIdReadOnly" readonly style="border: 0;"/>
					</tds:hasPermission>
				</div>
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
			<td valign="top" class="name"><label for="workFlowTransitionEditId">Workflow Step:</label></td>
			<td valign="top" class="value" colspan="3">
				<span id="workFlowTransitionEditId"></span>
				<input type="checkbox" id="overrideEdit" name="override" value="0"
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
		<tr class="prop" id="durationEditId" style="display: none">
        	<td valign="top" class="name"><label for="durationEdit ">Duration:</label></td>
        	<td valign="top" class="value" colspan="4">
				<input type="text" id="durationEdit" name="durationEdit" value="" size="3"/>
				<g:select id="durationScaleEdit" class="ynselect" name="durationScaleEdit " from="${com.tds.asset.AssetComment.constraints.durationScale.inList}" value="m"/>&nbsp;&nbsp;
				<span id="priorityEditSpanId">
		        	    <label for="priority">Priority:</label>
		            	<g:select id="priorityEdit" class="ynselect" name="priorityEdit" from="${1..5}" value=""></g:select>
	            </span> &nbsp;&nbsp;&nbsp;
	            <span id="dueDateEditSpanId">
		        	    <label for="dueDateEditId">Due Date:</label>
		        	    <script type="text/javascript" charset="utf-8">
				 		 	jQuery(function($){$('.dateEditRange').datepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                 		 </script>
		        	    <input type="text" class="dateEditRange" size="15" style="" name="dueDateEdit" id="dueDateEditId"
							value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
	            </span>
        	</td>
	        	
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="status">Status:</label></td>
			<td colspan="3" id="statusEditTrId">
			</td>
		</tr>

		<%-- Dependencies section --%>

		<tr class="prop">
			<td valign="top">
				<%-- Disabled the collapse/expand capabilities for the moment
				<span id="dependCollapsed" class="Arrowcursor rightArrowAdd"><img alt="" style="vertical-align: middle;" src="${resource(dir:'images',file:'expand-32.png')}" width="24" onclick="toggleDependencies('e','Add')"/></span>

				<span id="dependExpanded" class="Arrowcursor leftArrowAdd" style="display:none;"><img alt="" class="predArrow"  style="vertical-align: middle;" src="${resource(dir:'images',file:'collapse-32.png')}" width="24" onclick="toggleDependencies('c','Add')"/></span>
				--%>
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
					<td valign="top"><span id="predecessorEditId"></span></td>
					<td valign="top"><span id="successorEditId"></span></td>
				</tr>
			</table></td>
		<tr>
		<tr id="processDiv" style="display: none;" >
			<td></td>
			<td><img id="processingId" src="../images/processing.gif" /></td>
		</tr>

		<%-- JM 5/8/2013 - don't understand the point of this code so commented out as I believe it was replaced by the above section
		<tr><td>	
			<table class="inner">
				<tr class="prop" id="predecessorEditTr" style="display: none">
					<td valign="top" class="name"><label for="predecessorEditTd">Pred:</label></td>
					<td valign="top" class="value" id="predecessorEditTd" colspan="2">
					  <g:select id="predecessorCategoryEdit"  from="${com.tds.asset.AssetComment.constraints.category.inList}" value="general" noSelection="['':'please select']" onChange="addPredecessor('','predecessorCategoryEdit','updateCommentId','predecessorEditTr','relatedIssueEditId')"/>
					</td>
					<td id="relatedIssueEditId">
					</td>
				</tr>
				
				<tr id="processDiv" style="display: none;" >
				<td></td>
				 <td colspan="4" >
					<img id="processingId" src="../images/processing.gif" />
				 </td>
				</tr>
		        <tr class="prop" id="predecessorTrEditId" style="display: none">
					<td valign="top" class="name"></td>
					<td nowrap="nowrap" width="auto;"> <span id="predecessorEditId" style="width: 50%;float: left"></span> <span id="successorEditId" style="width: 40%;float:left;"></span>
					</td>
					
				</tr>
			</table>
		</td></tr>
		--%>

		<%-- ----------- --%>

		<tr>
			<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
			<td valign="top" class="value" id="createdByEditId" colspan="3"></td>
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
	    <tr class="prop issue" id="estStartEditTrId" style="display: none">
			<td valign="top" class="name"><label for="estStartTrId">Estimated Start:</label></td>
			<td valign="top" class="value" nowrap="nowrap">
				<script type="text/javascript" charset="utf-8">
				  jQuery(function($){$('.datetimeEditRange').datetimepicker({showOn: 'both', buttonImage: '${resource(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="datetimeEditRange" size="15" style="" name="estStart" id="estStartEditId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
			<td valign="middle" class="name"><label for="estFinishTrId">Estimated Finish:</label></td>
			<td valign="top" class="value" nowrap="nowrap">
				<input type="text" class="datetimeEditRange" size="15" style="" name="estFinishEditId" id="estFinishEditId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
		</tr>
		<tr class = "issue" id="actStartShow" style="display: none">
			<td valign="top" class="name"><label for="actStartEditId">Actual Start:</label></td>
			<td valign="top" class="value" id="actStartEditId"></td>
			<td valign="top" class="name" nowrap="nowrap" width="10%"><label for="actFinishShowId">Actual Finish:</label></td>
			<td valign="top" class="value" id="actFinishEditId" nowrap="nowrap"></td>
		</tr >
		</table>
  </div>
</div>
<tds:hasPermission permission='CommentCrudView'>
<div class="buttons"><span class="button">
	<input class="save" type="button" id="saveAndCloseBId" value="Save and close" onclick="resolveValidate('editCommentForm','updateCommentId','${rediectTo}','');" disabled="disabled"/>
	
	<input class="save" type="button" id="saveAndViewBId" value="Save and view" onclick="resolveValidate('editCommentForm','updateCommentId','','view');" disabled="disabled"/>
	</span>
	<span id="deleteCommentId" class="button">
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
