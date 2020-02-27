<%-- tabOffset - allows off-setting the tabindex for the custom fields --%>
<g:set var="tabOffset" value="200"/>

<g:each in="${customs}" var="custom" status="j">
<%-- TODO: Need to wrap the label and controls with clarity control wrappers --%>
    <div class="clr-form-control">
        <tdsAngular:inputLabel field="${custom}" value="${assetEntityInstance.(custom.field)}"/>
        <tdsAngular:inputControl field="${custom}" value="${assetEntityInstance.(custom.field)}"
                                 ngmodel="model.asset.${custom.field}" tabOffset="$tabOffset" customIndex="${j}"/>
    </div>
</g:each>
