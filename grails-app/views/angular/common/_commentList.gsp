<td colspan="2" id="commentsInnerList">
	<task-comment
			[asset-id]="${asset.id}"
			[has-publish-permission]="${hasPublishPermission}"
	 		[can-edit-comments]="${canEdit}"
			[can-edit-tasks]="${canEditTasks}"
			[asset-name]="'${raw(asset.assetName).replace("'","\\'")}'"
			[asset-type]="'${asset.assetType}'">
	</task-comment>
</td>