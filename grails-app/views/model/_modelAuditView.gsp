<div>
<input type="hidden" id="manufacturer" value="${modelInstance?.manufacturer}">
<table> 
 <tr><td colspan="2"><b>Model Details View</b></td></tr>
	<tr class="prop">
		<td class="label">Name</td>
		<td class="label">
			${modelInstance?.modelName}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Size</td>
		<td class="label">
			${modelInstance?.usize}
		</td>
	</tr>
	<tr class="prop">
		<td class="label">Manufacturer</td>
		<td class="label">
			${modelInstance?.manufacturer}
		</td>
	</tr>
	</table>
	<div class="buttons noWrapDiv">
		<input type="button" class="edit" value="Edit" onclick="editModelAudit('${modelInstance.modelName}')" /> 
		<g:form action="edit" controller="model" target="new">
			<input name="id" type="hidden" id="show_modelId" value="${modelInstance.id}"/>
			<span class="button">
				<input type="submit" class="edit" value="More..."></input>
			</span>
		</g:form>
	</div>
</div>
