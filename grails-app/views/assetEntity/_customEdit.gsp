<g:set var="tabOffset" value="700"/>
<%-- tabOffset - allows off-setting the tabindex for the custom fields --%>

<g:set var="trIsOpen" value="0"/>
<%-- trIsOpen - used to track if the TR tag needs to be closed after the loop --%>

<g:each in="${customs}" var="custom" status="j">
	<g:if test="${j % 4 == 0}">
		<tr class="prop">
		<g:set var="trIsOpen" value="1"/>
	</g:if>

	<td class="label ${custom.imp?:''}" nowrap="nowrap">
		<label for="${custom.field}">${custom.label}
			<tds:ifInputRequired field="${custom}">
				<span style="color: red;">*</span>
			</tds:ifInputRequired>
		</label>
	</td>
	<td>
		<tds:inputControl field="${custom}" value="${assetEntityInstance.(custom.field)}" tabOffset="$tabOffset"/>
	</td>

	<g:if test="${j % 4 == 3}">
		</tr>
		<g:set var="trIsOpen" value="0"/>
	</g:if>
</g:each>
<g:if test="trIsOpen == 1">
	</tr>
</g:if>