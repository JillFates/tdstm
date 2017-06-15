<g:set var="tabOffset" value="700"/>

<g:each in="${customs}" var="custom" status="j">
	<g:if test="${j % 4 == 0}">
		<tr class="prop">
	</g:if>
		<td class="label ${custom.imp?:''}" nowrap="nowrap">
			<label for="${custom.field}">${custom.label}</label>
		</td>
		<td>
			<input type="text" id="${custom.field}" class=" ${custom.imp?:''}" name="${custom.field}" value="${assetEntityInstance.(custom.field)}" tabindex="${tabOffset + 1}"/>
		</td>
	<g:if test="${j % 4 == 3}">
		</tr>
	</g:if>
</g:each>
