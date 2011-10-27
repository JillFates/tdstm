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
	<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
		<span class="menuButton"><a class="create" href="#" onclick="$('#statusId').val('');$('#createResolveDiv').css('display','none');$('#createCommentDialog').dialog('option', 'width', 'auto');$('#createCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();" >New Comment</a></span>
	</div>
</div>
<div id="createCommentDialog" title="Create Asset Comment" style="display: none;">
	<input type="hidden" name="assetEntity.id" id="createAssetCommentId" value="" />
	<input type="hidden" name="status" id="statusId" value="" />
	<g:form action="saveComment" method="post" name="createCommentForm">
		<input type="hidden" name="category" value="general" />
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<div>
		<table id="createCommentTable" style="border: 0px;">
		<tr class="prop" >
			<td valign="top" class="name"><label for="commentType">Comment Type:</label></td>
			<td valign="top" style="width: 20%;">
				<g:select id="commentType" name="commentType" from="${com.tds.asset.AssetComment.constraints.commentType.inList}" value=""
				noSelection="['':'please select']" onChange="commentChange('#createResolveDiv','createCommentForm')"></g:select>&nbsp;&nbsp;&nbsp;&nbsp;			

			<input type="checkbox" id="mustVerifyEdit" name="mustVerify" value="0"
				onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
			<label for="mustVerify">Must Verify</label>
			</td>				
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="comment">Comment:</label></td>
			<td valign="top" class="value">
				<textarea cols="80" rows="3" id="comment" name="comment" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea>
			</td>
		</tr>
	</table>
	</div>
	<div id="createResolveDiv" style="display: none;">
		<table id="createResolveTable" style="border: 0px" >
		<tr class="prop">
			<td valign="top" class="name"><label for="isResolved">Resolved:</label></td>
			<td valign="top" class="value">
				<input type="checkbox" id="isResolved" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value">
				<textarea cols="80" rows="3" id="resolution" name="resolution" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea>
			</td>
		</tr> 
		</table>
            </div>
		
	</div>
	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Create"
		onclick="resolveValidate('createCommentForm','createAssetCommentId');" /></span></div>
</g:form></div>
<div id="showCommentDialog" title="Show Asset Comment"
	style="display: none;">
<div class="dialog" style="border: 1px solid #5F9FCF"><input name="id" value="" id="commentId"
	type="hidden"/>
	<div>
	<table id="showCommentTable" style="border: 0px;">
		<tr>
			<td valign="top" class="name"><label for="dateCreated">Created At:</label></td>
			<td valign="top" class="value" id="dateCreatedId" ></td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
			<td valign="top" class="value" id="createdById" ></td>
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
		<tr class="prop">
			<td valign="top" class="name"><label for="comment">Comment:</label></td>
			<td valign="top" class="value" >
				<textarea cols="80" rows="3" id="commentTdId" readonly="readonly"></textarea>
			</td>
		</tr>
	</table>
	</div>
	<div id="showResolveDiv" style="display: none;">
		<table id="showResolveTable" style="border: 0px">
		<tr class="prop">
			<td valign="top" class="name"><label for="isResolved">Is Resolved:</label></td>
			<td valign="top" class="value" id="resolveTdId">
				<input type="checkbox" id="isResolvedId" name="isResolved" value="0" disabled="disabled" />
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value" >
				<textarea cols="80" rows="3" id="resolutionId" readonly="readonly"></textarea>
			</td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="dateResolved">Resolved At:</label></td>
			<td valign="top" class="value" id="dateResolvedId" ></td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="resolvedBy">Resolved By:</label></td>
			<td valign="top" class="value" id="resolvedById" ></td>
		</tr>	
	</table>
	</div>
	<div class="buttons"><span class="button">
	<input class="edit" type="button" value="Edit"
		onclick="commentChangeEdit('editResolveDiv','editCommentForm');$('#editCommentDialog').dialog('option', 'width', 'auto');$('#editCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('open');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close')" />
	</span>
	<span class="button"> <input class="delete" type="button" value="Delete"
	onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment',controller:'assetEntity', params:'\'id=\' + $(\'#commentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'never\')')}" />
	</span>
	</div>
</div>
</div>
<div id="editCommentDialog" title="Edit Asset Comment" style="display: none;">
<g:form action="updateComment" method="post" name="editCommentForm">
<div class="dialog" style="border: 1px solid #5F9FCF">
	<input type="hidden" name="id" id="updateCommentId" value=""/>
	<div>
	<table id="updateCommentTable" style="border: 0px;">
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
				<jsec:hasAnyRole in="['ADMIN','PROJ_MGR']">
					<g:select id="commentTypeEditId" name="commentType"
					from="${com.tds.asset.AssetComment.constraints.commentType.inList}" value=""
					 onChange="commentChange('#editResolveDiv','editCommentForm')"></g:select>&nbsp;&nbsp;&nbsp;&nbsp;			
				</jsec:hasAnyRole>
				<jsec:lacksAllRoles in="['ADMIN','PROJ_MGR']">
					<input type="text" id="commentTypeEditId" name="commentType" readonly style="border: 0;"/>&nbsp;&nbsp;&nbsp;&nbsp;
				</jsec:lacksAllRoles>				
				<input type="checkbox" id="mustVerifyEditId" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
				<label for="mustVerify">Must Verify</label>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="category">Category:</label></td>
			<td valign="top" class="value" id="categoryEditId" ></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="commentCode">Comment Code:</label></td>
			<td valign="top" class="value" id="commentCodeEditId" ></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="comment">Comment:</label></td>
			<td valign="top" class="value">
				<textarea cols="80" rows="3" id="commentEditId" name="comment" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea>
			</td>
		</tr>
	</table>		
	</div>
	<div id="editResolveDiv" style="display: none;">
	<table id="updateResolveTable" style="border: 0px;">
		<tr class="prop">
			<td valign="top" class="name"><label for="isResolved">Resolved:</label></td>
			<td valign="top" class="value">
				<input type="checkbox" id="isResolvedEditId" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
			</td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value">
				<textarea cols="80" rows="3" id="resolutionEditId" name="resolution" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea>
			</td>
		</tr> 
		<tr>
			<td valign="top" class="name"><label for="dateResolved">Resolved At:</label></td>
			<td valign="top" class="value" id="dateResolvedEditId" ></td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="resolvedBy">Resolved By:</label></td>
			<td valign="top" class="value" id="resolvedByEditId" ></td>
		</tr>
	</table>
	</div>
</div>

<div class="buttons"><span class="button">
	<input class="save" type="button" value="Update" onclick="resolveValidate('editCommentForm','updateCommentId');" />
	</span>
	<span class="button"> <input class="delete" type="button" value="Delete"
		onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment',controller:'assetEntity', params:'\'id=\' + $(\'#updateCommentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'never\')')}" />
	</span>
</div>
</g:form>
</div>