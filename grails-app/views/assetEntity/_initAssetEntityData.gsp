<%@page defaultCodec="none" %>
<%@page import="org.apache.commons.lang.StringEscapeUtils" %>
<%@page import="grails.converters.JSON" %>
<script>
    tds.core.service.commonDataService.loadCategories([{'id':'', 'name':'please select'}<g:collect in="${com.tds.asset.AssetComment.constraints.category.inList}" expr="it">,{'id':'${it}','name':'${StringEscapeUtils.escapeJavaScript(it)}'}</g:collect>]);
    tds.core.service.commonDataService.loadDurationScales([{'id':'', 'name':'please select'}<g:collect in="${com.tds.asset.AssetComment.constraints.durationScale.inList}" expr="it">,{'id':'${it}','name':'${it}'}</g:collect>]);
    tds.core.service.commonDataService.loadLoginPerson(${ session.getAttribute("LOGIN_PERSON") as JSON });
</script>
