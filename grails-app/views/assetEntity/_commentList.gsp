<%-- TODO: Determine where this is comming from and set the max width on it... --%>

<div id="commentsInnerList">
	<h1 id="tasksAndCommentsLabelId">Tasks and Comments:</h1>
	<comment-inner-list asset-id="${asset.id}" pref-value="${prefValue}" view-unpublished-value="${viewUnpublishedValue}" has-publish-permission="${hasPublishPermission}" can-edit-comments="${canEdit}" can-edit-tasks="${canEditTasks}"></comment-inner-list>
</div>
<script>
	recompileDOM('commentsInnerList');
</script>
