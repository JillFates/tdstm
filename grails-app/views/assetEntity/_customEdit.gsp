<g:set var="tabOffset" value="700"/>

<g:each in="${customs}" var="custom" status="j">
	<g:if test="${j % 4 == 0}">
		<tr class="prop">
	</g:if>

	<td class="label ${custom.imp?:''}" nowrap="nowrap">
		<label for="${custom.field}">${custom.label} <g:if test="${custom.constraints.required == true}"><span style="color: red;">*</span></g:if></label>
	</td>
	<td>
		<tds:customField field="${custom}" value="${assetEntityInstance.(custom.field)}" tabOffset="${tabOffset}"/>
	</td>

	<g:if test="${j % 4 == 3}">
		</tr>
	</g:if>
</g:each>
