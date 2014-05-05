<g:if test="${project.customFieldsShown != 0 && project.customFieldsShown <= Project.CUSTOM_FIELD_COUNT}">
	<g:each in="${customs}" var="i" status="j">
		<g:if test="${j % 4 == 0}">
			<tr class="prop">
		</g:if>
			<td class="label ${config?.('custom'+i)}" nowrap="nowrap" ><label for="${'custom'+i}">${project.('custom'+i) ?: 'custom'+i }</label></td>
			<td class="valueNW ${config?.('custom'+i)}" width="60"><tds:textAsLink text="${assetEntity?.('custom'+i)}" target="_new"/></td>
		<g:if test="${j % 4 == 3}">
			</tr>
		</g:if>
	</g:each>
</g:if>