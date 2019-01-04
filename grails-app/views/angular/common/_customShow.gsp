<g:each in="${customs}" var="custom" status="j">
	<g:if test="${j % 4 == 0}">
		<tr class="prop">
	</g:if>
		<tds:inputLabel field="${custom}" value="${asset.(custom.field)}"/>
		<td class="valueNW ${custom.imp?:''} custom-label" width="60">
			<span><tdsAngular:showValue field="${custom}" value="${asset?.(custom.field)}"/></span>
		</td>
	<g:if test="${j % 4 == 3}">
		</tr>
	</g:if>
</g:each>
