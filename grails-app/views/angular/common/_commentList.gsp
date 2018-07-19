<td colspan="2" id="commentsInnerList">
	<h1>Tasks and Comments:</h1>
	<task-comment
			[asset-id]="${asset.id}"
			[has-publish-permission]="${hasPublishPermission}"
	 		[can-edit-comments]="${canEdit}"
			[can-edit-tasks]="${canEditTasks}"
			[asset-name]="'${asset.assetName}'"
			[asset-type]="'${asset.assetType}'">
	</task-comment>
</td>