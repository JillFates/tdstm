<%-- This template is used at the bottom of all of the Asset Create forms --%>
<div class="buttons">
	<span class="button"><input type="button" class="save" value="Save" data-redirect='${redirectTo}' data-action='' onclick="saveToShow($(this),'${whom}')"/> </span>
	<span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#createEntityView').dialog('close');"/></span>
</div>