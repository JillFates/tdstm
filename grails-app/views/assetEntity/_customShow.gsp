<g:each in="${customs}" var="custom" status="j">
	<g:if test="${j % 4 == 0}">
		<tr class="prop">
	</g:if>
		<td class="label ${custom.imp?:''}" nowrap="nowrap" >
			<label for="${custom.field}" data-toggle="popover" data-trigger="hover" data-content="${custom.tip}">${custom.label}</label></td>
		<td class="valueNW ${custom.imp?:''}" width="60">
			<span data-toggle="popover" data-trigger="hover" data-content="${custom.tip}">
				<tds:textAsLink text="${assetEntity?.(custom.field)}" target="_new"/>
			</span>
		</td>
	<g:if test="${j % 4 == 3}">
		</tr>
	</g:if>
</g:each>