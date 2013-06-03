<g:set var="tabOffset" value="50"/>

<g:if test="${project.customFieldsShown != 0 && project.customFieldsShown < 25}">
	<g:each in="${ (1..(project.customFieldsShown)) }" var="i">
		<g:if test="${i % 4 == 1}">
			<tr class="prop">
		</g:if>
			<td class="label" nowrap="nowrap">${project.('custom'+i) ?: 'Custom'+i }</td>
			<td>
				<input type="text" id="custom${i}" class="${config?.('custom'+i)}" name="custom${i}" value="${assetEntityInstance.('custom'+i)}" tabindex="${tabOffset + 1}"/>
			</td>
		<g:if test="${i % 4 == 0}">
			</tr>
		</g:if>
	</g:each>
</g:if>