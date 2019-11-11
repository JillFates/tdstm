<g:each in="${customs}" var="custom" status="j">
	<tr>
		<tds:clrInputLabel field="${custom}" value="${assetEntity.(custom.field)}"/>
		<td>
			<span data-toggle="popover" data-trigger="hover" data-content="${custom.tip}" ${raw(j % 4 == 3 ? 'data-placement="bottom"': '')}>
				<tds:textAsLink text="${assetEntity?.(custom.field)}" target="_new"/>
			</span>
		</td>
	</tr>
</g:each>
