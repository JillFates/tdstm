<div class="body">
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div style="margin-top: 89px; margin-left: 50px; width: 150px;">
		<table>
			<thead>
				<tr>
					<th colspan="2"><h3>Asset Priority</h3></th>
				</tr>
			</thead>
			<tbody id="priorityStatusTbodyId">
				<g:each in="${priorityOption}" status="i" var="priorityOption">
					<tr id="priorityOption_${priorityOption.id}">
						<td>${priorityOption.value}</td>
						<td><span class=" deletePriority clear_filter"
							style="display: none; cursor: pointer;"
							onClick="deleteAssetStatus(${priorityOption.id},$('#priorityhiddenId').val())"><b>X</b>
						</span></td>
					</tr>
				</g:each>
		</table>
		<input type="hidden" id="priorityhiddenId" name="hiddenId"
			value="Priority" /> <span id="newPriorityOption"
			style="display: none;"> <input type="text" id="priorityOption"
			name="priorityOption" value=""> </span> <input type="button"
			id="addPriorityButtonId" name="createPriorityOption" value="EDIT"
			onclick="addAssetOptions($('#priorityhiddenId').val())"/>
	</div>
</div>
