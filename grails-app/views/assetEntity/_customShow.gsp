<g:each in="${customs}" var="custom" status="j">
	<g:if test="${j % 4 == 0}">
		<tr class="prop">
	</g:if>
		<td class="label ${custom.imp?:''}" nowrap="nowrap" >
			<label for="${custom.field}">${custom.label}</label></td>
		<td class="valueNW ${custom.imp?:''}" width="60">
			<tds:textAsLink text="${assetEntity?.(custom.field)}" target="_new"/>
		</td>
	<g:if test="${j % 4 == 3}">
		</tr>
	</g:if>
</g:each>