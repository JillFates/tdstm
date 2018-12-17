<%@ page import="com.tdsops.tm.enums.domain.ValidationType" %>
<%-- Determine what the link parameters are going to be --%>
<g:set var="linkParams" value="${ [filter:filter] }" />
<g:set var="validateLinkParams" value="${ linkParams + [toValidate:ValidationType.UNKNOWN] }"/>

<td class="dashboard_stat_icon_td">
	<g:link controller="${assetType}" action="list" params="${linkParams}" class="links">
		<tds:svgIcon name="${iconName}_menu" width="17" height="17" />
	</g:link>
</td>
<td class="dashboard_stat_desc_td">
	<g:link controller="${assetType}" action="list" params="${linkParams}" class="links">
			${title}
	</g:link>
</td>
<td class="dashboard_stat_td_L">
	<g:link controller="${assetType}" action="list" params="${linkParams}" class="links">
		${assetCount}
	</g:link>
</td>
<g:if test="${ validate > 0 }">
	<td class="dashboard_stat_graph_td">
		<div class="dashboard_bar_base_small">
			<div class="dashboard_bar_graph_small" id="${barId}" style="width: 0%;"></div>
		</div>
		<g:link controller="${assetType}" action="list" params="${validateLinkParams}" class="links">
			${validate} to validate
		</g:link>
	</td>
</g:if>
<g:else>
	<td class="dashboard_stat_graph_td"><asset:image src="images/checked-icon.png" /></td>
</g:else>
