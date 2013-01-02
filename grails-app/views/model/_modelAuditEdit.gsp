<g:form name="modelAuditEdit" action="updateModel" method="post" >
	<div>
	<input type="hidden" name="id" value="${modelInstance?.id}"> 
	<input type="hidden" name="redirectTo" value="assetAudit"> 
		<table>
			<tr><td colspan="2"><b>Model Audit Edit</b></td></tr>
			<tr>
				<td>Model Name:</td>
				<td>${modelInstance?.modelName}</td>
			</tr>

			<tr>
				<td>Usize:</td>
				<td><g:select id="usizeId" name="usize"
						from="${modelInstance.constraints.usize.inList}"
						value="${modelInstance.usize}"></g:select></td>
			</tr>
			<tr>
				<td>Manufacturer</td>
				<td>
					${modelInstance.manufacturer}
				</td>
			</tr>
		</table>
	</div>
	<div class="buttons">
		<input type="button" class="edit" value="Update" onclick="updateModelAudit()" /> 
		<g:form action="edit" controller="model" target="new">
			<input  type="hidden" id="show_modelId"/>
			<span class="button">
				<input type="submit" class="edit" value="Edit"></input>
			</span>
		</g:form>
	</div>
</g:form>