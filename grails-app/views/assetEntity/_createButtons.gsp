<%-- This template is used at the bottom of all of the Asset Create forms --%>
<div class="buttons">
	<span class="button"><input type="button" class="save" value="Save" data-action='' onclick="EntityCrud.saveToShow($(this),'${assetClass}')"/> </span>
	<span class="button"><input type="button" class="cancel" value="Cancel" onclick="$('#showEntityView').dialog('close')"/></span>
</div>