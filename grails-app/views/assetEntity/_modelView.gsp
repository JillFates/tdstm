<%--<g:select id = "models" name="model.id" class="assetSelect" noSelection="${[null:' Unassigned']}" from="${models}" optionKey="id" optionValue="modelName"  value="${modelsList}"  
 optionValue="${{it.modelName+' '+(it.modelStatus =='new' || !it.modelStatus ? '?' :'')}}" onChange="setType(this.value,'${forWhom}')"/>--%>

<select id="model" class="assetSelect modelSelect ${clazz}" name="model.id"
		onchange="setType(this.value, '${forWhom}')">
	<option value="null" selected="selected">Unassigned</option>
	<g:if test="${models.Validated}" >
		<optgroup label="Validated" id="validated">
			<g:each in="${models.Validated}" var="m">
				<option value="${m.id}"
					${m.id == assetEntity?.model?.id ? 'selected="selected"' : ''}>
					${m.modelName}
				</option>
			</g:each>
		</optgroup>
	</g:if>
	<g:if test="${models.Unvalidated}" >
		<optgroup label="Unvalidated" id="Unvalidated">
			<g:each in="${models.Unvalidated}" var="m">
				<option value="${m.id}"
					${m.id == assetEntity?.model?.id ? 'selected="selected"' : ''}>
					${m.modelName+'?'}
				</option>
			</g:each>
		</optgroup>
	</g:if>
</select>