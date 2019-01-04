<%-- This template is used at the bottom of all of the Asset Create forms --%>
<div class="buttons">
    <button type="button" class="btn btn-default save" data-action='' onclick="EntityCrud.saveToShow($(this),'${assetClass}')"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> Save</button>
	<button type="button" class="btn btn-default cancel" onclick="EntityCrud.closeCreateModal();"><span class="glyphicon glyphicon-ban-circle" aria-hidden="true"></span> Cancel</button>
    <input type="submit" id="assetUpdateSubmit" style="display:none;">
</div>
