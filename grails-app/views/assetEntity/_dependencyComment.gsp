<g:if test="${ forWhom == 'edit' }" >
	<a title="${ dependency.comment }" 
	 	id="commLink_${type}_${dependency.id}" href="javascript:openCommentDialog('depComment_${type}_${dependency.id}')">
	 	<g:if test="${ dependency.comment }" >
	   		<img id="comment_${dependency.id}" src="${resource(dir:'icons',file:'comment_edit.png')}" border="0px" />
	   	</g:if>
	   	<g:else>
	   		<img id="comment_${dependency.id}" src="${resource(dir:'icons',file:'comment_add.png')}" border="0px" />
	   	</g:else>
	</a>
</g:if>
<g:else>
 	<g:if test="${ dependency.comment }" >
 		<a title="${ dependency.comment }"> 
   			<img id="comment_${dependency.id}" src="${resource(dir:'icons',file:'comment.png')}" border="0px" />
   		</a>
   	</g:if>
</g:else>

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