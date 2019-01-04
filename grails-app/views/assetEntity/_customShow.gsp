<g:each in="${customs}" var="custom" status="j">
	<g:if test="${j % 4 == 0}">
		<tr class="prop">
	</g:if>
		<tds:inputLabel field="${custom}" value="${assetEntity.(custom.field)}"/>
		<td class="valueNW ${custom.imp?:''}" width="60">
			<span data-toggle="popover" data-trigger="hover" data-content="${custom.tip}" ${raw(j % 4 == 3 ? 'data-placement="bottom"': '')}>
				<tds:textAsLink text="${assetEntity?.(custom.field)}" target="_new"/>
			</span>
		</td>
	<g:if test="${j % 4 == 3}">
		</tr>
	</g:if>
</g:each>
