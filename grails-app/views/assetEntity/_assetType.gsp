<div class="body">
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div style="margin-top: 89px; margin-left: 50px; width: auto;">
		<table id="assetTypeTableId">
			<thead>
				<tr>
					<th colspan="2"><h3>Asset Type</h3></th>
				</tr>
			</thead>
			<tbody id="assetTypeTbodyId">
				<g:each in="${assetType}" status="i" var="assetTypeIt">
					<tr id="assetType_${assetTypeIt.id}">
						<td>${assetTypeIt.value}</td>
						<td>
							<g:if test="${assetTypeIt.canDelete}">
								<span class=" deleteAssetType clear_filter"
								      style="display: none; cursor: pointer;"
								      onClick="deleteAssetStatus(${assetTypeIt.id}, $('#assetTypeHiddenId').val())"><b>X</b>
								</span>
							</g:if>
						</td>
					</tr>
				</g:each>
		</table>
		<input type="hidden" id="assetTypeHiddenId" name="hiddenId" value="assetType" />
		<span id="newAssetType" style="display: none;">
			<input type="text" id="assetType" name="assetType" maxlength="20" value="">
		</span> 
		<input type="button" id="addAssetTypeButtonId" name="createAssetType" value="EDIT" onclick="addAssetOptions($('#assetTypeHiddenId').val())"/>
	</div>
</div>
