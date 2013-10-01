<td style="width: 10px; text-align: right;">
	${assetCount}
</td>
<td style="padding-right: 0px;">
	<g:link controller="${assetType}" action="list" params="[filter:filter]" class="links">	${title}</g:link><br /> 
	<g:if test="${ validate > 0 }">
		<g:if test="${ filter in ['physical','virtual','other'] }">
			(<g:link controller="${assetType}" action="list"
				params="[filter:filter, type:'toValidate']" class="links">
				${validate} to validate</g:link>)
		</g:if>
		<g:else>
			(<g:link controller="${assetType}" action="list"
				params="[filter:filter, validation:'Discovery']" class="links">
				${validate} to validate</g:link>)
		</g:else>
		<td style="width: 100px; padding-left: 0px;">
			<div class="dashboard_bar_base_small">
				<div class="dashboard_bar_graph_small" id="${barId}"
					style="width: 0%;"></div>
			</div>
		</td>
	</g:if> 
	<g:else>
	<td><img src="${resource(dir:'images',file:'checked-icon.png')}" /></td>
	</g:else>
</td>