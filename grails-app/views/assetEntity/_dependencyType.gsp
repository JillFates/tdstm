<div class="body">
	<g:if test="${flash.message}">
		<div class="message">${flash.message}</div>
	</g:if>
	<div style="margin-top: 89px; margin-left: 50px; width: auto;">
		<table>
			<thead>
				<tr>
					<th colspan="2"><h3>Dependency Type</h3></th>
				</tr>
			</thead>
			<tbody id="dependencyTypeTbodyId">
				<g:each in="${dependencyType}" status="i" var="dependencyType">
					<tr id="dependencyType_${dependencyType.id}">
						<td>${dependencyType.value}</td>
						<td><span class=" deleteDependency clear_filter"
							style="display: none; cursor: pointer;"
							onClick="deleteAssetStatus(${dependencyType.id},$('#dependencyHiddenId').val())"><b>X</b>
						</span></td>
					</tr>
				</g:each>
		</table>
		<input type="hidden" id="dependencyHiddenId" name="hiddenId" value="dependency" /> 
		<span id="newDependency" style="display: none;">
		 <input type="text" id="dependencyType" name="dependencyType" value=""> </span> <input type="button" id="addDependencyButtonId" name="createDependencyType" value="EDIT"
			onclick="addAssetOptions($('#dependencyHiddenId').val())"/>
	</div>
</div>