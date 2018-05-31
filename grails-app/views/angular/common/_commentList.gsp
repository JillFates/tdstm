<td colspan="2" id="commentsInnerList">
	<h1 id="tasksAndCommentsLabelId">Tasks and Comments:</h1>
	<task-comment
			[asset-id]="${asset.id}"
			[pref-value]="${prefValue}"
			[view-unpublished-value]="${viewUnpublishedValue}"
			[has-publish-permission]="${hasPublishPermission}"
	 		[can-edit-comments]="${canEdit}"
			[can-edit-tasks]="${canEditTasks}">
	</task-comment>
</td>