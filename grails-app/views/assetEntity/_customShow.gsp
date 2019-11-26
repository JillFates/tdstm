<g:each in="${customs}" var="custom" status="j">
	<%-- NOTE: Order necessary to maintain order. --%>
	<tr style="order: ${1000 + (j * 5)}">
		<tds:clrInputLabel field="${custom}" value="${assetEntity.(custom.field)}"/>
		<td>
			<span data-toggle="popover" data-trigger="hover" data-content="${custom.tip}" ${raw(j % 4 == 3 ? 'data-placement="bottom"': '')}>
				<tds:textAsLink text="${assetEntity?.(custom.field)}" target="_new"/>
			</span>
		</td>
	</tr>
</g:each>
