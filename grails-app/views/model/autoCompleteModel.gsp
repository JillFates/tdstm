<table class="suggestionTable" >
	<g:each in="${models}" var="model">
		<tr id="model_${model.id}" class="trAnchor"
			onmouseover="this.style.backgroundColor='pink'"
			onmouseout="this.style.backgroundColor='#FCFCFC'"
			onclick="updateModelForAudit('${model.modelName}')">
			<td>
				${model.modelName}
			</td>
		</tr>
	</g:each>
</table>