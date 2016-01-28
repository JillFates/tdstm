<div id="legendDivId" class="graphPanel">
	<table id="legendId" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;" >
		
		<tr><td colspan="2"><h4>Asset Classes</h4></td></tr>
		
		<g:each in="${assetTypes}" var="entry" status="i">
			<g:set var="type" value="${entry.getKey()}" />
			<g:set var="names" value="${entry.getValue()}" />
			<tr>
				<td style="padding:0px;">
					<svg style="width: 35px; height: 35px; border-width: 0px;"><use xlink:href="${'#' + names.internalName + 'ShapeId'}" class="node" x="17" y="15" style="fill: #1f77b4;"></use></svg>
				</td>
				<td style="padding-bottom: 8px;">
					${names.labelText ?: names.frontEndNamePlural}
				</td>
			</tr>
		</g:each>
		
		<tr><td colspan="2"><h4>Dependencies</h4></td></tr>
		
		<g:set var="arrowheadOffset" value="${params.arrowheadOffset ? 45 : 40}" />
		<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link Standard"></line></svg></td><td>Valid Links</td></tr>
		<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link notApplicable"></line></svg></td><td>N/A</td></tr>
		<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link unresolved"></line></svg></td><td>Questioned</td></tr>
		<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link future"></line></svg></td><td>Future</td></tr>
		
		<g:if test="${params.displayBundleConflicts}">
			<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link bundleConflict"></line></svg></td><td>Bundle Conflict</td></tr>
		</g:if>
		
		<g:if test="${params.displayCycles}">
			<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link cyclical"></line></svg></td><td>Cyclical</td></tr>
		</g:if>
		
		<g:if test="${params.displayCuts}">
			<tr><td><svg style="width: 34px;height: 16px;border-width: 0px;"><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link cut"></line></svg></td><td>Suggested Splits</td></tr>
		</g:if>
		
		
		<tr id="colorKeyLabelId" class="hidden"><td colspan="2"><h4>Move Bundles</h4></td></tr>
		<tr id="colorKeyTemplateId" class="hidden">
			<td class="bundleColorExample">
				<svg style="width: 30px;height: 30px;border-width: 0px;"><use xlink:href="#applicationShapeId" class="node" x="0" y="0" style="fill: #1f77b4;"></use></svg>
			</td>
			<td class="bundleNameLabel"></td>
		</tr>
		
	</table>
</div>