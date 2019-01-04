<%--
	Used to render a SELECT control for Models that contains OptGroup for Validate and Unvalidate model types
	@param clazz - the CSS class(es) that should be assigned
	@param tabIndex - the tabIndex for input tabbing
	@param models - the list of models to show
	@param forWhom - Edit or Create calling this 
	@param 
--%>

<select id="model" class="assetSelect modelSelect ${clazz}" name="model.id"
	onchange="setType(this.value, '${forWhom}')"
	<g:if test="tabIndex">tabIndex="${tabIndex}"</g:if>
>
	<option value="null">Unassigned</option>

	<g:if test="${models.Validated}" >
		<optgroup label="Validated" id="validated">
			<g:each in="${models.Validated}" var="m">
				<option ${m.id == assetEntity?.model?.id ? 'selected ' : ''}value="${m.id}" >${m.modelName+'?'}</option>
			</g:each>
		</optgroup>
	</g:if>

	<g:if test="${models.Unvalidated}" >
		<optgroup label="Unvalidated" id="Unvalidated">
			<g:each in="${models.Unvalidated}" var="m">
				<option ${m.id == assetEntity?.model?.id ? 'selected ' : ''}value="${m.id}" >${m.modelName+'?'}</option>
			</g:each>
		</optgroup>
	</g:if>
</select>
