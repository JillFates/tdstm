<g:if test="${assetCommentList.size() > 0 }">	
	<td colspan="2">
		<div class="list">
		<table id="listCommentsTables">
		<thead>
		<tr>
			<th nowrap class="headerwidth3"></th>
			<th nowrap class="headerwidth3">#</th>
			<th nowrap>Task/comment</th>
			<th nowrap class="headerwidth12">Status&nbsp;(&nbsp;
			<input type="checkbox" name="showAll" id="showAll" class="showAllChecked" ${prefValue && prefValue == 'TRUE' ?  'checked="checked"'  : ''} />
			&nbsp;<label for="showAll">All )</label></th>
			<th nowrap class="headerwidth6">Category</th>  
			<th nowrap class="headerwidth20">Assigned To</th>
		</tr>
		</thead>
		<tbody id="listCommentsTbodyIds">
		<g:each status="i" in="${assetCommentList}"  var="commentList">
		<tr style="cursor: pointer;" class="${commentList.status == 'Completed' || commentList.status=='Pending' ? 'resolved' : 'ready' }">
			<td><a href ="javascript:showComment(${commentList.id},'edit')" ><img src="${resource(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a></td>
			<td onclick="javascript:showComment(${commentList.id},'show')" style="text-align: center;">${commentList.taskNumber ?:'c'}</td>
			<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.comment}</td>
			<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.status}</td>
			<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.category}</td>
			<td onclick="javascript:showComment(${commentList.id},'show')" >${commentList.assignedTo}/${commentList.role}</td>
		</tr>
		</g:each>
			</tbody>
			</table>
		</div>
	</td>
</g:if>