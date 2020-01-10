<div class="clr-col-12">
	<task-comment
			[show-comment]="${showComment}"
			[show-task]="${showTask}"
			[asset-id]="${asset.id}"
			[has-publish-permission]="${hasPublishPermission}"
	 		[can-edit-comments]="${canEdit}"
			[can-edit-tasks]="${canEditTasks}"
			[asset-name]="'${(raw(asset.assetName).replace("'","\\'")).replace("\"","\"")}'"
			[asset-class]="'${asset.assetClass}'"
			[user-id]="'${currentUserId}'"
			[asset-type]="'${asset.assetType}'"
			<g:if test="${showTask}">(taskCount)=updateTaskCount($event)</g:if>
			<g:if test="${showComment}">(commentCount)=updateCommentCount($event)</g:if>
			>
	</task-comment>
</div>
