<div id="legendDivId" class="graphPanel">
	<table id="legendId" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;" >
		
		<tr><td style="padding: 3px 3px;" colspan="2"><h3>Legend</h3></td></tr>
		<tr><td colspan="2"><h4>Asset Classes</h4></td></tr>
		
		<g:each in="${assetTypes.keySet()}" var="type">
			<tr>
				<td style="padding:0px;">
					<svg style="width: 30px;height: 30px;border-width: 0px;"><use xlink:href="${'#' + assetTypes[type] + 'ShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
				</td>
				<td>
					${type}
				</td>
			</tr>
		</g:each>
		
		<tr><td colspan="2"><h4>Dependencies</h4></td></tr>
		
		<g:set var="arrowheadOffset" value="${params.arrowheadOffset ? 45 : 30}" />
		<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link Standard"></line></svg></td><td>Valid Links</td></tr>
		<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link notApplicable"></line></svg></td><td>N/A</td></tr>
		<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link unresolved"></line></svg></td><td>Questioned</td></tr>
		<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link future"></line></svg></td><td>Future</td></tr>
		
		<g:if test="${params.displayBundleConflicts}">
			<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link bundleConflict"></line></svg></td><td>Bundle Conflict</td></tr>
		</g:if>
		
		<g:if test="${params.displayFuture}">
			<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link Future"></line></svg></td><td>Future</td></tr>
		</g:if>
		
		<g:if test="${params.displayCycles}">
			<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link cyclical" style="stroke-dasharray: none;"></line></svg></td><td>Cycle</td></tr>
		</g:if>
		
		<g:if test="${params.displayCuts}">
			<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link cut" style="stroke-dasharray: none;"></line></svg></td><td>Cut Dependency</td></tr>
		</g:if>
		
		
		<tr id="colorKeyLabelId" class="hidden"><td colspan="2"><h4>Move Bundles</h4></td></tr>
		<tr id="colorKeyTemplateId" class="hidden">
			<td class="bundleColorExample">
				<svg style="width: 30px;height: 30px;border-width: 0px;"><use xlink:href="#applicationShapeId" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
			</td>
			<td class="bundleNameLabel"></td>
		</tr>
		
	</table>
</div>