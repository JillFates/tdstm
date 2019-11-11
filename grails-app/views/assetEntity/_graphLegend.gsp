<div id="legendDivId" class="graphPanel">
	<!-- The twistie for the asset classes section -->
	<table cellpadding="0" cellspacing="0">
		<tr id="twistieRowId">
			<td class="noPadding">
				<span id="twistieSpanId" groupType="ac" class="open pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="assetClassesContainerId">
					Asset Classes
					<svg style="fill: #0077b8;"><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
				</span>
			</td>
		</tr>
	</table>
	<!-- The list of asset classes with their icons -->
	<div id="assetClassesContainerId" class="twistieControlledDiv">
		<table cellpadding="0" cellspacing="0">
			<g:each in="${assetTypes}" var="entry" status="i">
				<g:set var="type" value="${entry.getKey()}" />
				<g:set var="names" value="${entry.getValue()}" />
				<tr>
					<td style="padding:0px; text-align: center;">
						<svg id="${names.internalName}ShapeLeftPanel"><use xlink:href="${'#' + names.internalName + 'ShapeId'}" class="node" x="18" y="15" style="fill: #1f77b4;"></use></svg>
					</td>
					<td style="padding-bottom: 8px;">
						${names.labelText ?: names.frontEndName}
					</td>
				</tr>
			</g:each>
		</table>
	</div>
	
	<!-- The twistie for the dependency section -->
	<table cellpadding="0" cellspacing="0">
		<tr><td colspan="3" class="noPadding"><br /></td></tr> <!-- Spacer -->
		<tr id="twistieRowId">
			<td class="noPadding">
				<span id="twistieSpanId" groupType="de" class="open pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="dependencyTypeContainerId">
					Dependencies
					<svg style="fill: #0077b8;"><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
				</span>
			</td>
		</tr>
	</table>
	<!-- The list of dependency line types with examples of each -->
	<div id="dependencyTypeContainerId" class="twistieControlledDiv">
		<table cellpadding="0" cellspacing="0">
			<g:set var="arrowheadOffset" value="${params.arrowheadOffset ? 45 : 40}" />
			<g:each in="${[['Standard','Valid Links'],['notApplicable','N/A'],['unresolved','Questioned'],['future','Future']]}" var="item" status="i">
				<tr><td><svg><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link ${item[0]}"></line></svg></td><td>${item[1]}</td></tr>
			</g:each>
			
			<g:if test="${params.displayBundleConflicts}">
				<tr><td><svg><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link bundleConflict"></line></svg></td><td>Bundle Conflict</td></tr>
			</g:if>
			
			<g:if test="${params.displayCycles}">
				<tr><td><svg><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link cyclical"></line></svg></td><td>Cyclical</td></tr>
			</g:if>
			
			<g:if test="${params.displayCuts}">
				<tr><td><svg><line x1="1" y1="8" x2="${arrowheadOffset}" y2="8" class="link cut"></line></svg></td><td>Suggested Splits</td></tr>
			</g:if>
		</table>
	</div>

	<!-- The twistie for the color grouping section -->
	<table id="colorKeyLabelId" class="hidden" cellpadding="0" cellspacing="0">
		<tr><td colspan="3" class="noPadding"><br /></td></tr> <!-- Spacer -->
		<tr id="twistieRowId">
			<td class="noPadding">
				<span id="twistieSpanId" groupType="hb" class="open pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="colorGroupingContainerId">
					Move Bundles
					<svg style="fill: #0077b8;"><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
				</span>
			</td>
		</tr>
	</table>
	<!-- The list of groups and their colors -->
	<div id="colorGroupingContainerId" class="twistieControlledDiv">
		<table id="colorGroupingTableId" cellpadding="0" cellspacing="0">
			<tr id="colorKeyTemplateId" class="hidden">
				<td class="bundleColorExample">
					<svg><use xlink:href="#applicationShapeId" class="node" x="18" y="15"></use></svg>
				</td>
				<td class="bundleNameLabel"></td>
			</tr>
		</table>
	</div>
</div>
