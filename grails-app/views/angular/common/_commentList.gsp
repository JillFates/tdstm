<td colspan="2" id="commentsInnerList">
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
			[asset-type]="'${asset.assetType}'">
	</task-comment>
</td>
