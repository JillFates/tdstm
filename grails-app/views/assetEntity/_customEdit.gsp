<%-- tabOffset - allows off-setting the tabindex for the custom fields --%>
<g:set var="tabOffset" value="200"/>

<g:each in="${customs}" var="custom">
	<div class="clr-form-control">
		<tds:inputLabel field="${custom}" value="${assetEntityInstance.(custom.field)}"/>
		<tds:inputControl field="${custom}" value="${assetEntityInstance.(custom.field)}" tabOffset="$tabOffset"/>
	</div>
</g:each>
