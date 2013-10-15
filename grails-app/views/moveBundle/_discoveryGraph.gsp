<td class="dashboard_stat_icon_td">
	<g:link controller="${assetType}" action="list" params="[filter:filter]" class="links">
		<img src="${resource(dir:'images',file:iconName)}" height="14" />
	</g:link>
</td>
<td class="dashboard_stat_desc_td">
	<g:link controller="${assetType}" action="list" params="[filter:filter]" class="links">
		${title}
	</g:link>
</td>
<td class="dashboard_stat_td">
	<g:link controller="${assetType}" action="list" params="[filter:filter]" class="links">
		${assetCount}
	</g:link>
</td>
<g:if test="${ validate > 0 }">
	<td class="dashboard_stat_graph_td">
		<div class="dashboard_bar_base_small">
			<div class="dashboard_bar_graph_small" id="${barId}" style="width: 0%;"></div>
		</div>
		<g:if test="${ filter in ['physical','virtual','other'] }">
			<g:link controller="${assetType}" action="list" params="[filter:filter, type:'toValidate']" class="links">
				${validate} to validate
			</g:link>
		</g:if>
		<g:else>
			<g:link controller="${assetType}" action="list" params="[filter:filter, validation:'Discovery']" class="links">
				${validate} to validate
			</g:link>
		</g:else>
	</td>
</g:if> 
<g:else>
	<td class="dashboard_stat_graph_td"><img src="${resource(dir:'images',file:'checked-icon.png')}" /></td>
</g:else>
