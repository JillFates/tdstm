<div id="commentsListDialog" title="Show Asset Comments" style="display: none;">
<br/>
	<div class="list">
		<table id="listCommentsTable">
		<thead>
		<tr>
			<th nowrap>Action</th>
			<th nowrap>Comment</th>
			<th nowrap>Comment Type</th>
			<th nowrap>Resolved</th>
			<th nowrap>Must Verify</th>
			<th nowrap>Category</th>  
			<th nowrap>Comment Code</th>                     
		</tr>
		</thead>
		<tbody id="listCommentsTbodyId">
		</tbody>
		</table>
	</div>
    <tds:hasPermission permission='CommentCrudView'>
	<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
		<span class="menuButton"><a class="create" href="#" onclick="$('#statusId').val('');$('#createResolveDiv').css('display','none');$('#createCommentDialog').dialog('option', 'width', 'auto');$('#createCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();$('#catagoryTrId').css('display', 'none');$('#dueDateTrId').css('display', 'none');$('#ownerId').css('display', 'none');$('#mustVerifyTd').css('display', 'block');" >New Comment</a></span>
	</div>
	</tds:hasPermission>
</div>
<div id="createCommentDialog" title="Create Asset Comment" style="display: none;">
	<input type="hidden" name="assetEntity.id" id="createAssetCommentId" value="" />
	<input type="hidden" name="status" id="statusId" value="" />
	<g:form action="saveComment" method="post" name="createCommentForm">
		<input type="hidden" name="category" value="general" />
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<div>
		<table id="createCommentTable" style="border: 0px;">
		<tr class="prop" id="ownerId" style="display:none">
		<% def partyList = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType='PROJ_STAFF' and p.partyIdFrom = ? and p.roleTypeCodeFrom = 'PROJECT' " ,[Party.get(Integer.parseInt(session.getAttribute( 'CURR_PROJ' ).CURR_PROJ))]).partyIdTo;%>
			<td valign="top" class="name"><label for="owner">Assigned To:</label></td>
			<td valign="top" style="width: 20%;">
				<g:select id="owner" name="owner" from="${partyList}"  value="${session.getAttribute('LOGIN_PERSON').id }" optionKey="id" noSelection="['':'please select']" ></g:select>
			</td>
		
		</tr> 
		<tr class="prop" id="catagoryTrId" style="display:none">
			<td valign="top" class="name"><label for="category">Category:</label></td>
			<td style="margin-right:30px ;" valign="top">
				<g:select id="createCategory" name="createCategory" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="general"
				noSelection="['':'please select']" ></g:select>&nbsp;&nbsp;&nbsp;&nbsp;
			</td>
		</tr>
		<tr class="prop" >
			<td valign="top" class="name"><label for="commentType">Comment Type:</label></td>
			<td style="width: 20%;">
				<g:select id="commentType" name="commentType" from="${com.tds.asset.AssetComment.constraints.commentType.inList}" value=""
				noSelection="['':'please select']" onChange="commentChange('#createResolveDiv','createCommentForm')"></g:select>&nbsp;&nbsp;&nbsp;&nbsp;
			</td>					
            <td valign="top" id="mustVerifyTd" style="display: none;">
			<input type="checkbox" id="mustVerifyEdit" name="mustVerify" value="0" 
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
			<label for="mustVerifyEdit">Must Verify</label>
			</td>
					
		</tr>
		
		<tr class="prop" id="dueDateTrId" style="display: none">
			<td valign="top" class="name"><label for="dueDate">Due Date:</label></td>
			
			<td valign="top" class="value">
				<script type="text/javascript" charset="utf-8">
                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="dueDate" id="dueDateCreateId"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			</td>
		</tr> 
		<tr class="prop">
			<td id="issueItemId" valign="top" class="name"><label for="comment">Comment:</label></td>
			<td valign="top" class="value" colspan="4">
				<textarea cols="80" rows="4" id="comment" name="comment" ></textarea>
			</td>
		</tr>
	</table>
	</div>
	<div id="createResolveDiv" style="display: none;">
		<table id="createResolveTable" style="border: 0px" >
		<tr class="prop" >
			<td valign="top" class="name"><label for="status">Status:</label></td>
			<td style="width: 20%;">
				<g:select id="statusId" name="status" from="${com.tds.asset.AssetComment.constraints.status.inList}" value="Pending"
				noSelection="['':'please select']" ></g:select>&nbsp;&nbsp;&nbsp;&nbsp;
			</td>	
		</tr>
		<tr class="prop" style="display: none;">
			<td valign="top" class="name"><label for="isResolved">Resolved:</label></td>
			<td valign="top" class="value">
				<input type="checkbox" id="isResolved" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value">
				<textarea cols="80" rows="4" id="resolution" name="resolution" ></textarea>
			</td>
		</tr> 
		
		
		</table>
            </div>
		
	</div>
	<tds:hasPermission permission='CommentCrudView'>
	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Create"
		onclick="resolveValidate('createCommentForm','createAssetCommentId');" /></span></div>
	</tds:hasPermission>
</g:form></div>
<div id="showCommentDialog" title="Show Asset Comment"
	style="display: none;">
<div class="dialog" style="border: 1px solid #5F9FCF"><input name="id" value="" id="commentId"
	type="hidden"/>
	<div>
	<table id="showCommentTable" style="border: 0px;">
		<tr>
			<td valign="top" class="name"><label for="dateCreated">Created At:</label></td>
			<td valign="top" class="value" id="dateCreatedId" style="width:20px;" ></td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
			<td valign="top" class="value" id="createdById" ></td>
			
		</tr>
		<tr>
			<td valign="top" class="name"><label for="owner">Owner:</label></td>
			<td valign="top" class="value" id="ownerTdId" ></td>
			
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="commentType">Comment Type:</label></td>
			<td valign="top" class="value" id="commentTypeTdId" ></td>
			
		</tr>
		<tr>
			<td valign="top" class="name"><label for="category">Category:</label></td>
			<td valign="top" class="value" id="categoryTdId" ></td>
			
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="commentCode">comment Code:</label></td>
			<td valign="top" class="value" id="commentCodeTdId" ></td>
			
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="mustVerify">Must Verify:</label></td>
			<td valign="top" class="value" id="verifyTdId">
				<input type="checkbox" id="mustVerifyShowId" name="mustVerify" value="0" disabled="disabled" />
			</td>
			
		</tr>
		<tr id = "assetShowId" class="prop" >
			<td valign="top" class="name"><label for="asset">Asset:</label></td>
			<td valign="top" class="value" id="assetShowValueId" style="display: none;"></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="comment">Comment:</label></td>
			<td valign="top" class="value" colspan="2">
				<textarea cols="80" rows="4" id="commentTdId" readonly="readonly"></textarea>
			</td>
		</tr>
	</table>
	</div>
	<div id="showResolveDiv" style="display: none;">
		<table id="showResolveTable" style="border: 0px">
		<tr class="prop">
			<td valign="top" class="name"><label for="previousNotes">Previous Notes:</label></td>
			<td valign="top" class="value" >
				<div id="previousNotesShowId" ></div>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="status">Status:</label></td>
			<td valign="top" class="value" id="statusShowId" ></td>
			
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value" colspan="2">
				<div id="resolutionId" ></div>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="dateResolved">Resolved At:</label></td>
			<td valign="top" class="value" id="dateResolvedId" ></td>
			<td valign="top" class="name"><label for="type">Type:</label></td>
			<td valign="top" class="value" id="typeId" ></td>
		</tr>
		<tr>
			<td valign="top" class="name" nowrap="nowrap"><label for="resolvedBy">Resolved By:</label></td>
			<td valign="top" class="value" id="resolvedById" ></td>
			<td valign="top" class="name"><label for="priority">Priority:</label></td>
			<td valign="top" class="value" id="priorityId" ></td>
		</tr>
			<tr><td valign="top" class="name"><label for="dueDate">dueDate:</label></td>
			<td valign="top" class="value" id="dueDatesId" ></td>
			<td valign="top" class="name"><label for="startedTime">StartedTime:</label></td>
			<td valign="top" class="value" id="startedTimeId" ></td>
		</tr>	
		
	</table>
	</div>
	<tds:hasPermission permission='CommentCrudView'>
	<div class="buttons"><span class="button">
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
<div id="editCommentDialog" title="Edit Asset Comment" style="display: none;">
<g:form action="updateComment" method="post" name="editCommentForm">
<div class="dialog" style="border: 1px solid #5F9FCF">
	<input type="hidden" name="id" id="updateCommentId" value=""/>
	<div>
	<table id="updateCommentTable" style="border: 0px;">
	   <tr class="prop" id="ownerEditedId">
		<% def partyList = PartyRelationship.findAll("from PartyRelationship p where p.partyRelationshipType='PROJ_STAFF' and p.partyIdFrom = ? and p.roleTypeCodeFrom = 'PROJECT' " ,[Party.get(Integer.parseInt(session.getAttribute( 'CURR_PROJ' ).CURR_PROJ))]).partyIdTo;%>
			<td valign="top" class="name"><label for="owner">Owner:</label></td>
			<td valign="top" id="ownerEditId" style="width: 20%;display: none;">
				<g:select id="ownerEditTdId" name="owner" from="${partyList}"  value="" optionKey="id"   noSelection="['':'please select']" ></g:select>
			</td>
		</tr> 
	    <tr id="dueDatesEditId" style="display: none" ><td valign="top" class="name"><label for="dueDate">DueDate:</label></td>
			<td valign="top" class="value" id="dueDatesEditId" ><script type="text/javascript" charset="utf-8">
                    jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
                  </script> <input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="dueDateEdit" id="dueDateEdit"
					value="" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
			
		</tr>
		<tr>
			<td valign="top" class="name"><label for="dateCreated">Created At:</label></td>
			<td valign="top" class="value" id="dateCreatedEditId"  />
		</tr>
		<tr>
			<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
			<td valign="top" class="value" id="createdByEditId" />
		</tr>
		
		<tr class="prop" >
			<td valign="top" class="name"><label for="commentType">Comment Type:</label></td>
			<td valign="top" style="width: 20%;" >
				<tds:hasPermission permission='CommentCrudView'>
					<g:select id="commentTypeEditId" name="commentType"
					from="${com.tds.asset.AssetComment.constraints.commentType.inList}" value=""
					 onChange="commentChange('#editResolveDiv','editCommentForm')"></g:select>&nbsp;&nbsp;&nbsp;&nbsp;			
				</tds:hasPermission>
				<div style="display: none">
				<tds:hasPermission permission='CommentCrudView'>
					<input type="text" id="commentTypeEditIdReadOnly" readonly style="border: 0;"/>&nbsp;&nbsp;&nbsp;&nbsp;
				</tds:hasPermission>				
				<input type="checkbox" id="mustVerifyEditId" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
				<label for="mustVerify">Must Verify</label>
				</div>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="category">Category:</label></td>
			<td valign="top" class="value"  ><g:select id="categoryEditId" from="${com.tds.asset.AssetComment.constraints.category.inList}" value="general"  ></g:select></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="commentCode">Comment Code:</label></td>
			<td valign="top" class="value" id="commentCodeEditId" ></td>
		</tr>
		<tr id="assetTrId" class="prop" >
			<td valign="top" class="name"><label for="asset">Asset:</label></td>
			<td valign="top" class="value" style ="display: none;" id="assetTrShowId" ></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name" id="commentEditId"><label for="comment">Comment:</label></td>
			<td valign="top" class="value" colspan="1">
				<textarea cols="80" rows="4" id="commentEditId" name="comment" ></textarea>
			</td>
		</tr>
	</table>		
	</div>
	<div id="editResolveDiv" style="display: none;">
	<table id="updateResolveTable" style="border: 0px;">
	   <tr class="prop">
			<td valign="top" class="name"><label for="notes">Previous Notes:</label></td>
			<td valign="top" class="value"><div id="previousNote" style="width: 200px"></div></td>
		</tr>
	    <tr class="prop">
			<td valign="top" class="name"><label for="notes">Note:</label></td>
			<td valign="top" class="value"><input type="text" id="note" name="note" value=""/>
			</td>
		</tr>
		<tr class="prop" >
			<td valign="top" class="name"><label for="status">Status:</label></td>
			<td style="width: 20%;">
				<g:select id="statusEditId" name="statusEdit" from="${com.tds.asset.AssetComment.constraints.status.inList}" value="Pending"
				noSelection="['':'please select']" ></g:select>&nbsp;&nbsp;&nbsp;&nbsp;
			</td>	
		</tr>
		<tr class="prop" style="display: none;">
			<td valign="top" class="name"><label for="isResolved">Resolved:</label></td>
			<td valign="top" class="value">
				<input type="checkbox" id="isResolvedEditId" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value" colspan="4">
				<textarea cols="80" rows="4" id="resolutionEditId" name="resolution" ></textarea>
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