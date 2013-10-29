<div class="buttons">
	<g:if test="${redirectTo}">
		<span class="button"><g:actionSubmit class="save" value="Save/Close" action="save" /></span>
	</g:if>
	<g:else>
		<span class="button"><input type="button" class="save" value="Save/Close" data-action='close' onclick="saveToShow($(this),'${whom}')"/> </span>
	</g:else>
	<span class="button"><input type="button" class="save" value="Save/Show" data-redirect='${redirectTo}' data-action='' onclick="saveToShow($(this),'${whom}')"/> </span>
	<span class="button"><input type="button" class="delete" value="Cancel" onclick="$('#createEntityView').dialog('close');"/> </span>
</div>