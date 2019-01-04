<td colspan="2" id="commentsInnerList">
	<h1 id="tasksAndCommentsLabelId">Tasks and Comments:</h1>
	<comment-inner-list asset-id="${asset.id}" pref-value="${prefValue}" view-unpublished-value="${viewUnpublishedValue}" has-publish-permission="${hasPublishPermission}" can-edit-comments="${canEdit}" can-edit-tasks="${canEditTasks}"></comment-inner-list>
</td>
<script>
	recompileDOM('commentsInnerList');
</script>
