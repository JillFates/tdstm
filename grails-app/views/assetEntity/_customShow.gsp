<g:if test="${project.customFieldsShown != 0 && project.customFieldsShown < 25}">
	<g:each in="${ (1..(project.customFieldsShown)) }" var="i">
		<g:if test="${i % 4 == 1}">
			<tr class="prop">
		</g:if>
			<td class="label ${config?.('custom'+i)}" nowrap="nowrap" >${project.('custom'+i) ?: 'custom'+i }</td>
			<td class="valueNW ${config?.('custom'+i)}" width="60"><tds:textAsLink text="${assetEntity?.('custom'+i)}" target="_new"/></td>
		<g:if test="${i % 4 == 0}">
			</tr>
		</g:if>
	</g:each>
</g:if>