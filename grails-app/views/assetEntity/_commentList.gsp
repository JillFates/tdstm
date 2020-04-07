<%-- TODO: Determine where this is comming from and set the max width on it... --%>

<div id="commentsInnerList" class="tds-table">
	<div class="clr-row">
		<div id="tasksAndCommentsLabelId" class="grid-label clr-col-4">
			<strong>Tasks and Comments:</strong>
		</div>
	</div>
	<comment-inner-list asset-id="${asset.id}" pref-value="${prefValue}" view-unpublished-value="${viewUnpublishedValue}" has-publish-permission="${hasPublishPermission}" can-edit-comments="${canEdit}" can-edit-tasks="${canEditTasks}"></comment-inner-list>
</div>
<script>
	recompileDOM('commentsInnerList');
</script>
