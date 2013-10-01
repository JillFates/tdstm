<a title="${ dependency.comment }" 
 	${forWhom == 'edit'? 'id="commLink_'+type+'_'+dependency.id+'" href="javascript:openCommentDialog(\'depComment_'+type+'_'+dependency.id+'\')" ': ''}>
 	<g:if test="${ dependency.comment }" >
   		<img id="comment_${dependency.id}" src="${resource(dir:'i',file:'db_table_bold.png')}" border="0px" />
   	</g:if>
   	<g:else>
   		<img id="comment_${dependency.id}" src="${resource(dir:'i',file:'db_table_light.png')}" border="0px" />
   	</g:else>
</a>
<g:if test="${forWhom == 'edit'}">
 	<input type="hidden" name="comment_${type}_${dependency.id}" id="comment_${type}_${dependency.id}" value="${dependency.comment}">
 	<div id="depComment_${type}_${dependency.id}" class="depComDiv" style="display:none" >
		<textarea rows="5" cols="50" name="dep_comment_${type}_${dependency.id}" id="dep_comment_${type}_${dependency.id}"> ${dependency.comment} </textarea>
		<div class="buttons">
		<span class="button"><input type="button" class="save" value="Save" 
			onclick="saveDepComment('dep_comment_${type}_${dependency.id}', 'comment_${type}_${dependency.id}', 
				'depComment_${type}_${dependency.id}', 'commLink_${type}_${dependency.id}')"/> 
		</span>
		</div>
  	</div>
</g:if>