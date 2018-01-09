<%-- These are the common DIVs used by the Entity Crud logic and need to be loaded by the asset lists --%>

<div id="createEntityView" style="display: none;"></div>
<div id="cloneEntityView" style="display: none;"></div>
<div id="editEntityView" style="display: none;"></div>
<div id="showEntityView" style="display: none;"></div>
<div id="cablingDialogId" style="display: none;"></div>

<div id="depCommentDialog" class="static-dialog" style="display:none">
	<textarea name="" id="depCommentTextarea" rows="5" cols="150" onkeyup="tdsCommon.validateMaxTextAreaLength(this, 'depCommentTextareaValidation', 'saveComment', 65535);"></textarea> <br />
	<label id="depCommentTextareaValidation" style="display: none;" class="error-msg">Comment maximum length is 65535 characters.</label>
	<input type="hidden" id="depCommentRowNo" name="" />
	<input type="hidden" id="depCommentType" name="" />

	<div class="buttons">
		<span class="button">
			<input id="saveComment" type="button" class="save" value="Close" onclick="EntityCrud.onDepCommentDialogClose();"/>
		</span>
		<a href="#" onclick="EntityCrud.onDepCommentDialogCancel();">Cancel</a>
	</div>
</div>

<script>
// Set the dialogs when DocumentReady and JQuery is available
( function($) {
	$("#createEntityView").dialog({ autoOpen: false });
    $("#cloneEntityView").dialog({ autoOpen: false })
	$("#editEntityView").dialog({ autoOpen: false })
	$("#showEntityView").dialog({ autoOpen: false })
	$("#cablingDialogId").dialog({ autoOpen:false })
	$("#depCommentDialog").dialog({ autoOpen: false })
})(jQuery);
</script>

