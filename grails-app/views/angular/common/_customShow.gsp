<g:each in="${customs}" var="custom" status="j">
	<tds:clrRowDetail style="order: ${1000 + (5 * j)}" field="${custom}" value="${asset.(custom.field)}" />
</g:each>
