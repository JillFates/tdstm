<div class="body">
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div style="margin-top: 89px; margin-left: 50px; width: auto;">
		<table>
			<thead>
				<tr>
					<th colspan="2"><h3>Dependency Status</h3></th>
				</tr>
			</thead>
			<tbody id="dependencyStatusTbodyId">
				<g:each in="${dependencyStatus}" status="i" var="dependencyStatus">
					<tr id="dependencyStatus_${dependencyStatus.id}">
						<td>${dependencyStatus.value}</td>
						<td><span class=" deleteDependencyStatus clear_filter"
							style="display: none; cursor: pointer;"
							onClick="deleteAssetStatus(${dependencyStatus.id},$('#dependencyStatusHiddenId').val())"><b>X</b>
						</span></td>
					</tr>
				</g:each>
		</table>
		<input type="hidden" id="dependencyStatusHiddenId" name="hiddenId"
			value="dependencyStatus" /> <span id="newDependencyStatus"
			style="display: none;"> <input type="text"
			id="dependencyStatus" name="dependencyStatus" value=""> </span> <input
			type="button" id="addDependencyStatusButtonId"
			name="createDependencyStatusType" value="EDIT"
			onclick="addAssetOptions($('#dependencyStatusHiddenId').val())"/>
	</div>
</div>