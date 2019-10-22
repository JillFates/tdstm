<g:each in="${customs}" var="custom" status="j">
	<tds:clrRowDetail field="${custom}" value="${asset.(custom.field)}" />
</g:each>
