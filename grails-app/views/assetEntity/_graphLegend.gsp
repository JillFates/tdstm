<div id="legendDivId" class="graphPanel">
	<!-- The twistie for the asset classes section -->
	<table cellpadding="0" cellspacing="0">
		<tr id="twistieRowId">
			<td class="noPadding">
				<span id="twistieSpanId" class="open pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="assetClassesContainerId">
					Asset Classes
					<svg><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
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
				<span id="twistieSpanId" class="open pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="dependencyTypeContainerId">
					Dependencies
					<svg><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
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
	
	<g:if test="${params.showDependencyGroupColors}">
		<!-- The twistie for the dependency group colors section -->
		<table cellpadding="0" cellspacing="0">
			<tr><td colspan="3" class="noPadding"><br /></td></tr> <!-- Spacer -->
			<tr id="twistieRowId">
				<td class="noPadding">
					<span id="twistieSpanId" class="open pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="dependencyGroupContainerId">
						Dependency Group Colors
						<svg><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
					</span>
				</td>
			</tr>
		</table>
		<!-- The list of dependency group colors -->
		<div id="dependencyGroupContainerId" class="twistieControlledDiv">
			<table cellpadding="0" cellspacing="0">
				<tr>
					<td>
					<span class="depLabel depGroupConflict">Conflicts</span>
					<span class="toolTipButton" title="" data-toggle="popover" data-trigger="hover" data-content="Yellow indicates that there is some conflict(s) with assets in the group. The conflict may be that two or more assets are assigned to different bundles/events or one or more assets' dependency status is set to Questioned or Unknown."></span>
					</td>
				</tr>
				<tr>
					<td>
						<span class="depLabel depGroupPending">Pending</span>
						<span class="toolTipButton" title="" data-toggle="popover" data-trigger="hover" data-content="Grey indicates that there are no outstanding conflicts but the dependency Validation needs to be set to Bundle Ready in order to become Ready to Bundle."></span>
					</td>
				</tr>
				<tr>
					<td>
						<span class="depLabel depGroupReady">Ready to Bundle</span>
						<span class="toolTipButton" title="" data-toggle="popover" data-trigger="hover" data-content="Green indicates that the assets in the group are Ready to be assigned to bundles."></span>
					</td>
				</tr>
				<tr>
					<td>
						<span class="depLabel depGroupDone">Completed</span>
						<span class="toolTipButton" title="" data-toggle="popover" data-trigger="hover" data-content="Blue indicates that the work flow process of bundling assets has been completed for the group of assets. There are no conflicts and all of the assets in the group have been assigned to common bundles (in same event)."></span>
					</td>
				</tr>
			</table>
		</div>
	</g:if>
	<!-- The twistie for the color grouping section -->
	<table id="colorKeyLabelId" class="hidden" cellpadding="0" cellspacing="0">
		<tr><td colspan="3" class="noPadding"><br /></td></tr> <!-- Spacer -->
		<tr id="twistieRowId">
			<td class="noPadding">
				<span id="twistieSpanId" class="open pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="colorGroupingContainerId">
					<p>Move Bundles</p>
					<svg><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
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
