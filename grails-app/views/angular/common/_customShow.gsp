<g:each in="${customs}" var="custom" status="j">
	<g:if test="${j % 4 == 0}">
		<tr class="prop">
	</g:if>
		<tds:inputLabel field="${custom}" value="${asset.(custom.field)}"/>
		<td class="valueNW ${custom.imp?:''} custom-label" width="60">
            <g:if test="${custom.control == 'Date' && asset?.(custom.field)}">
                {{'${asset?.(custom.field)}' | tdsDate: userDateFormat}}
            </g:if>
            <g:elseif test="${custom.control == 'DateTime' && asset?.(custom.field) }">
                {{'${asset?.(custom.field)}' | tdsDateTime: userTimeZone}}
            </g:elseif>
            <g:elseif test="${custom.control == 'Number' && asset?.(custom.field) }">
                {{'${asset?.(custom.field)}' | tdsNumber: {allowNegatives: false, decimalPlaces: 2, maxRange: 10000, minRange: 0, required: 0, useThousandsSeparator: true} }}
            </g:elseif>
            <g:else>
                <span><tds:textAsLink text="${asset?.(custom.field)}" target="_new"/></span>
            </g:else>
		</td>
	<g:if test="${j % 4 == 3}">
		</tr>
	</g:if>
</g:each>