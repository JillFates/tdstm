<%-- tabOffset - allows off-setting the tabindex for the custom fields --%>
<g:set var="tabOffset" value="200"/>

<%-- trIsOpen - used to track if the TR tag needs to be closed after the loop --%>
<g:set var="trIsOpen" value="0"/>

<g:each in="${customs}" var="custom" status="j">
	<g:if test="${j % 4 == 0}">
		<tr class="prop">
		<g:set var="trIsOpen" value="1"/>
	</g:if>

	<tds:inputLabel field="${custom}" value="${assetEntityInstance.(custom.field)}"/>

	<td>
		<tds:inputControl field="${custom}" value="${assetEntityInstance.(custom.field)}" tabOffset="$tabOffset" tooltipDataPlacement="${j % 4 == 3 ? 'bottom': 'right' }"/>
	</td>

	<g:if test="${j % 4 == 3}">
		</tr>
		<g:set var="trIsOpen" value="0"/>
	</g:if>
</g:each>
<g:if test="trIsOpen == 1">
	</tr>
</g:if>
