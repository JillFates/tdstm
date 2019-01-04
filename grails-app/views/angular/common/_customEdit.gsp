<%-- tabOffset - allows off-setting the tabindex for the custom fields --%>
<g:set var="tabOffset" value="200"/>

<g:each in="${customs}" var="custom" status="j">
	<g:if test="${j % 4 == 0}">
		<tr class="prop">
		<g:set var="trIsOpen" value="1"/>
	</g:if>
	<tdsAngular:inputLabel field="${custom}" value="${assetEntityInstance.(custom.field)}"/>
	<td>
		<tdsAngular:inputControl field="${custom}" value="${assetEntityInstance.(custom.field)}" ngmodel="model.asset.${custom.field}" tabOffset="$tabOffset"/>
	</td>
	<g:if test="${j % 4 == 3}">
		</tr>
	</g:if>
</g:each>
