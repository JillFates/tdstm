<%@page defaultCodec="none" %>
<div id="toolsContainerId">
	<!-- The top bar with various buttons and controls on it -->
	<div id="graphToolbarId">
		<div class="btn-group">
			<button id="controlPanelTabId" class="btn" onclick="GraphUtil.togglePanel(GraphUtil.PANELS.CONTROL, 'depAnalyzer')">Control Panel</button>
			<button id="dependenciesPanelTabId" class="btn" onclick="GraphUtil.togglePanel(GraphUtil.PANELS.DEPENDENCY, 'depAnalyzer')">Dependencies</button>
			<button id="legendTabId" class="btn" onclick="GraphUtil.togglePanel(GraphUtil.PANELS.LEGEND), 'depAnalyzer'">Legend</button>
		</div>
		<button id="fullscreenButtonId" class="btn btn-outline" onclick="GraphUtil.toggleFullscreen()" title="Toggles fullscreen mode">Full Screen</button>
		<div id="highlightFormId" class="noPadding graphTabButton hasMargin newHighlightForm">
			<span id="filterOptionsButtonId" class="graphButton" onclick="GraphUtil.toggleHighlightDropdown()" title="Shows additional filtering options">
				<svg style="fill: #0077b8;"><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
			</span>
			<input class="clr-input" type="text" style="border-bottom:1px solid #999; padding-right: 24px;" id="searchBoxId" name="Search Box" value="" placeholder="Enter highlighting filter" onkeydown="GraphUtil.handleSearchKeyEvent(event)"/>
			<span id="filterClearId" class="ui-icon ui-icon-closethick" onclick="GraphUtil.clearFilter('text')" title="Clear the current filter"></span>
			<span style="font-size:18px; color:#0077b8;" onclick="GraphUtil.performSearch()" title="Applies the selected filtering options to the graph"><i class="fas fa-binoculars"></i></span>
		</div>
		<div id="filterOptionsMenuId" class="hasBorders">
			<table>
				<tr>
					<td colspan="3">
						Options for highlighting in the graph
					</td>
				</tr>
				<tr>
					<td colspan="3">
						App Owner or SME:
					</td>
				</tr>
				<tr title="Highlights assets by SME or app owner" class="optionRow">
					<td colspan="3">
						<span id="highlightPersonId" class="noPadding">
							<select kendo-drop-down-list id="personHighlightSelectId"></select>
							<span id="clearPersonFilterId" class="clearFilterIcon pointer disabled" onclick="GraphUtil.clearFilter('person')" title="Clears the current person filter"></span>
						</span>
					</td>
				</tr>
				<!-- -->
				<tr>
					<td colspan="3">
						Asset Tag:
					</td>
				</tr>
				<tr title="Highlights assets by Tag" class="optionRow">
					<td colspan="3">
						<tm-asset-tag-selector id="tmHighlighttAssetSelector" asset-selector="internal.assetSelector" pre-selected-operator="'ANY'" on-change="onDependencyAnalyzerTagSelectionChange()"></tm-asset-tag-selector>
					</td>
				</tr>
			</table>
		</div>
		<span style="font-size:18px; color:#0077b8; cursor:pointer; display:inline-block; margin:0 10px 0 20px;" onclick="GraphUtil.zoomIn()" title="Zoom in"><i class="fas fa-search-plus"></i></span>
		<span style="font-size:18px; color:#0077b8; cursor:pointer; display:inline-block; margin-right:10px;" onclick="GraphUtil.zoomOut()" title="Zoom out"><i class="fas fa-search-minus"></i></span>
		<span style="font-size:18px; color:#0077b8; cursor:pointer; display:inline-block; margin-right:10px;" onclick="GraphUtil.toggleToolState(GraphUtil.LASSO_TOOL, $(this))" title="Select a region on the graph ([shift+drag] to do this manually or [shift+ctrl+drag] to add this region to the current selection)"><i class="fas fa-draw-polygon"></i></span>
		<span style="font-size:18px; color:#0077b8; cursor:pointer;" onclick="getList('graph', ${depGroup == 'onePlus' ? '\'onePlus\'' : depGroup})" title="Refreshes the graph"><i class="fas fa-hand-pointer"></i></span>


		<button id="refreshButtonId" class="btn btn-outline" style="margin-left:20px;" onclick="getList('graph', ${depGroup == 'onePlus' ? '\'onePlus\'' : depGroup})" title="Refreshes the graph"><i class="fas fa-exclamation-triangle"></i>Refresh</button>
	</div>
	<!-- The control panel div containing graph controls and settings -->
	<div id="controlPanelId" class="graphPanel">
		<form id="preferencesformId">
			<table class="labelTree savedToPrefs" cellpadding="0" cellspacing="0">

				<!-- Miscellaneous settings and controls -->
				<tr>
					<td colspan="3" class="noPadding">
						<input id="playPauseButtonId" type="button" value="Freeze Graph" class="btn btn-sm fullButton" onclick="GraphUtil.toggleFreeze()" title="Pauses or Unpauses the physics of the force graph (can also be done with the [space] key)">
					</td>
				</tr>
				<tr>
					<td colspan="3" class="noPadding">
						<input id="OptimizeButtonId" type="button" value="Maximize Graph" class="btn btn-sm fullButton" onclick="setIdealGraphPosition()" title="Adjust the position, zoom, and rotation of the grpah to fit in the window">
					</td>
				</tr>

				<!-- Suggest splits controls -->
				<tr>
					<td colspan="3" class="noPadding">
						<input type="button" id="minCutButtonId" value="Suggest Splits" class="btn btn-sm fullButton" onclick="cutAndRemove()" title="Each time clicked will present a suggested division of the group by least number of cuts">
					</td>
				</tr>
				<tr>
					<td colspan="3" class="noPadding">
						<!-- The comments between lines prevents the browser from trying to evaluate the whitespace between these divs as a space character -->
						<input type="button" value="Undo Splits" class="btn btn-sm fullButton" onclick="undoCuts()" title="Undoes any previous cuts">
					</td>
				</tr>
				<tr title="Maximum number of edges that can be cut at once">
					<td colspan="3" class="noPadding">
						<div class="clr-form-horizontal" style="padding:unset;">
							<div class="clr-form-control" style="padding:unset; margin:unset; align-items:flex-end;">
								<label for="maxEdgeCountId" class="clr-control-label" style="width: unset; margin:unset;">Max Dependencies Cut</label>
								<div class="clr-control-container" style="margin-left: 5px;">
									<div class="clr-select-wrapper">
										<g:select name="maxEdgeCount" id="maxEdgeCountId" from="${1..20}" value="${graphPrefs.maxEdgeCount ? graphPrefs.maxEdgeCount : defaultPrefs.maxEdgeCount}"></g:select>
									</div>
								</div>
							</div>
						</div>
					</td>
				</tr>

				<!-- Color By grouping selector -->
				<tr title="Sets the criteria used to determine node fill color">
					<td colspan="3" style="padding:unset; padding-bottom:5px;">
						<div class="clr-form-horizontal" style="padding:unset;">
							<div class="clr-form-control" style="padding:unset; margin:unset; align-items:flex-end;">
								<label for="colorBySelectId" class="clr-control-label" style="width: unset; margin:unset;">Color By</label>
								<div class="clr-control-container" style="margin-left: 5px;">
									<div class="clr-select-wrapper">
										<g:select class="clr-select" name="colorBy" id="colorBySelectId" from="${colorByGroupLabels?.values()}" keys="${colorByGroupLabels?.keySet()}" value="${graphPrefs.colorBy ? graphPrefs.colorBy : defaultPrefs.colorBy}"></g:select>
									</div>
								</div>
							</div>
						</div>
					</td>
				</tr>

				<!-- More display controls -->
				<tr title="If checked, bundle conflicts will be highlighted">
					<td colspan="3" style="padding: unset;">
						<div class="clr-checkbox-wrapper" style="padding: unset;">
							<input type="checkbox" id="bundleConflictsId" name="bundleConflicts" class="clr-checkbox" value="true" ${(graphPrefs.bundleConflicts) ? 'checked' : ''}>
							<label for="bundleConflictsId">Show Bundle Conflicts</label>
						</div>

					</td>
				</tr>
				<tr title="Sets the color of the background to black">
					<td colspan="3" style="padding: unset;">
						<div class="clr-checkbox-wrapper" style="padding: unset;">
							<input type="checkbox" id="blackBackgroundId" name="blackBackground" class="clr-checkbox" value="true" ${(graphPrefs.blackBackground)?('checked="checked"'):('')} onchange="rebuildMap(false)">
							<label for="blackBackgroundId">Black Background</label>
						</div>
					</td>
				</tr>

				<!-- The twistie for the hide/show graph labels section -->
				<tr id="twistieRowId">
					<td colspan="3" class="noPadding">
						<span id="twistieSpanId" class="closed pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="labelControlContainerId">
							Show Labels:
							<svg style="width: 12px; height: 12px; border-width: 0px; fill: #0077b8;"><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
						</span>
					</td>
				</tr>
			</table>
			<!-- Label checkboxes -->
			<div id="labelControlContainerId">
				<table class="labelTree" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;" >
					<g:each in="${assetTypes}" var="entry" status="i">
						<g:set var="type" value="${entry.getKey()}" />
						<g:set var="names" value="${entry.getValue()}" />
						<g:if test="${ ! names.labelHandles.equals('') }">
						<tr class="labelToggleRow">
							<td colspan="3" class="labelToggleCol">
								<div style="padding:0px;">
									<div class="clr-checkbox-wrapper">
										<input type="checkbox" id="${type}CheckboxId" name="${names.labelPreferenceName}" value="true" ${(graphPrefs[names.labelPreferenceName]) ? 'checked' : ''} class="clr-checkbox ${names.labelHandles}" onchange="rebuildMap(false)" />
										<label for="${type}CheckboxId">
											<svg id="${names.internalName}ShapeLeftPanel"><use xlink:href="#${names.internalName}ShapeId" class="node" x="15" y="15" style="fill: #1f77b4;"></use></svg>
											${names.labelText ?: names.frontEndName}
										</label>
									</div>
								</div>
							</td>
						</tr>
						</g:if>
					</g:each>
				</table>
			</div>

			<table class="labelTree" cellpadding="0" cellspacing="0">
				<!-- Spacer -->
				<tr><td colspan="3" class="noPadding"><br /></td></tr>

				<!-- The twistie for the force layout settings section -->
				<tr id="twistieRowId">
					<td colspan="3" class="noPadding">
						<span id="twistieSpanId" class="closed pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="layoutControlContainerId">
							Layout:
							<svg style="width: 12px;height: 12px;border-width: 0px; fill: #0077b8;"><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
						</span>
					</td>
				</tr>
			</table>
		</form>

		<!-- layout controls -->
		<div id="layoutControlContainerId">
			<table class="labelTree" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;" >
				<tr class="layoutControl" title="Sets the amount of force between each node">
					<td style="padding: 0px;width: 30px;">Force</td>
					<td style="padding-left :5px;">
						<asset:image src="images/minus.gif" height="18" class="pointer plusMinusIcon minus" onclick="modifyParameter('sub','forceId')"/>
						<input type="text" id="forceId" class="controlPanelprop" name="force" value="${defaults.force}" disabled="disabled" />
						<asset:image src="images/plus.gif" height="18" class="pointer plusMinusIcon plus" onclick="modifyParameter('add','forceId')"/>
					</td>
				</tr>
				<tr class="layoutControl" title="Sets the desired length for the links">
					<td style="padding: 0px;width: 30px;">Links</td>
					<td style="padding-left :5px">
						<asset:image src="images/minus.gif" height="18"  class="pointer plusMinusIcon minus" onclick="modifyParameter('sub','linkSizeId')"/>
						<input type="text" id="linkSizeId" class="controlPanelprop" name="linkSize" value="${defaults.linkSize}" disabled="disabled" />
						<asset:image src="images/plus.gif" height="18"  class="pointer plusMinusIcon plus" onclick="modifyParameter('add','linkSizeId')"/>
					</td>
				</tr>
				<tr class="layoutControl" title="Sets the decay-rate of the nodes' velocity">
					<td style="padding: 0px;width: 30px;">Friction</td>
					<td style="padding-left :5px">
						<asset:image src="images/minus.gif" height="18"  class="pointer plusMinusIcon minus" onclick="modifyParameter('sub','frictionId')"/>
						<input type="text" id="frictionId" class="controlPanelprop" name="friction" value="${defaults.friction}" disabled="disabled" />
						<asset:image src="images/plus.gif" height="18" class="pointer plusMinusIcon plus"  onclick="modifyParameter('add','frictionId')"/>
					</td>
				</tr>
				<tr class="layoutControl" title="Sets the accuracy of the forces (lower numbers will be slower, but more accurate)">
					<td style="padding: 0px;width: 30px;">Theta</td>
					<td style="padding-left :5px">
						<asset:image src="images/minus.gif" height="18" class="pointer plusMinusIcon minus" onclick="modifyParameter('sub','thetaId')"/>
						<input type="text" id="thetaId" class="controlPanelprop" name="theta" value="${defaults.theta}" disabled="disabled" />
						<asset:image src="images/plus.gif" height="18" class="pointer plusMinusIcon plus" onclick="modifyParameter('add','thetaId')"/>
					</td>
				</tr>
			</table>
		</div>

		<!-- Preference controls -->
		<table class="labelTree" cellpadding="0" cellspacing="0">

			<!-- Spacer -->
			<tr><td colspan="3" class="noPadding"><br /></td></tr>

			<!-- Save preferences button -->
			<tr>
				<td colspan="3" class="noPadding">
					<input id="updatePrefsButtonId" type="button" value="Save Preferences" class="btn btn-sm fullButton" onclick="GraphUtil.updateUserPrefs('${com.tdsops.tm.enums.domain.UserPreferenceEnum.DEP_GRAPH.name()}')">
				</td>
			</tr>
			<!-- Reset preferences button -->
			<tr>
				<td colspan="3" class="noPadding">
					<input id="resetPrefsButtonId" type="button" value="Reset Defaults" class="btn btn-sm fullButton" onclick="GraphUtil.resetToDefaults('${com.tdsops.tm.enums.domain.UserPreferenceEnum.DEP_GRAPH.name()}')">
				</td>
			</tr>
		</table>
	</div>
	<!-- The dependencies panel div containing the checkboxes -->
	<style>
		table.dependency-control-table {
			border: 0px;
		}
		table.dependency-control-table th {
			background: none;
		}
		label.dependency-control-title span {
			text-decoration: underline;
			cursor: pointer;
		}
		label.dependency-control-title i.caret-icon{
			width: 1em;
		}
		label.dependency-control-title i.caret-icon:hover {
			background-color: #cccccc;
			width: 1em;
		}
		div.dependency_panel_action_buttons {
			position: absolute;
			bottom: 5px;
		}
		td.groupingControl {
			background-color: #dddddd;
		}
	</style>
	<div id="dependenciesPanelId" class="graphPanel">
		<div id="dependecy-control">
			<label class="dependency-control-title" onclick="GraphUtil.toggleDependencyPanel('dependency-type-panel', this)"><span>Connection Type</span><i style="color: #0077b8; cursor:pointer;" class="fa fa-fw fa-caret-down"></i></label>
			<br />
			<div class="checkboxdiv_control dependency-type-panel open" >
				<table class="dependency-control-table" cellpadding="0" cellspacing="0">
					<tr>
						<th style="width: 134px;"></th>
						<th style="text-align: center;">Show</th>
						<th style="text-align: center;">Highlight</th>
					</tr>
					<tr>
						<td>All</td>
						<td style="text-align: center;"><input state='1' type="checkbox" id="dependencyTypeControl_show_all" checked name="dependencyTypeControl_show__all" onclick="GraphUtil.onSelectAllDependencyPanel('dependencyTypeControls', 'dependencyType', this);"/></td>
						<td></td>
					</tr>
					<g:each in="${dependencyType}" var="dependency">
						<tr>
							<td style="width: 134px;"><span> ${dependency}</span></td>
							<td style="text-align: center;"><input class="dependencyTypeControlsShow" parentid="dependencyTypeControl_show_all" type="checkbox" checked id="dependencyTypeControl_show_${dependency}" name="dependencyTypeControl_show_${dependency}" onclick="GraphUtil.onSelectItemShowDependencyPanel(this)" value="${dependency}"/></td>
							<td style="text-align: center;"><input class="dependencyTypeControlsHighlight" type="checkbox" id="dependencyTypeControl_highlight_${dependency}"name="dependencyTypeControl_highlight_${dependency}" onclick="GraphUtil.onSelectItemHighlightDependencyPanel(this)" value="${dependency}"/></td>
						</tr>
					</g:each>
				</table>
			</div>

			<label class="dependency-control-title" onclick="GraphUtil.toggleDependencyPanel('dependency-show-panel', this)"><span>Connection Status</span><i style="color: #0077b8; cursor:pointer;" class="fa fa-fw fa-caret-down"></i></label>
			<div class="checkboxdiv_control dependency-show-panel open">
				<table class="dependency-control-table" cellpadding="0" cellspacing="0">
					<tr>
						<th style="width: 134px;"></th>
						<th style="text-align: center;">Show</th>
						<th style="text-align: center;">Highlight</th>
					</tr>
					<tr>
						<td>All</td>
						<td style="text-align: center;"><input state='1' type="checkbox" checked id="dependencyStatusControl_show_all" name="dependencyStatusControl_show_all" onclick="GraphUtil.onSelectAllDependencyPanel('dependencyStatusControls', 'dependencyStatus', this);"/></td>
						<td></td>
					</tr>
					<g:each in="${dependencyStatus}" var="dependencyStatusInst">
						<tr>
							<td style="width: 134px;"><span > ${dependencyStatusInst}</span></td>
							<td style="text-align: center;"><input class="dependencyStatusControlsShow" parentid="dependencyStatusControl_show_all" type="checkbox" checked id="show_${dependency}" name="dependencyStatusControl_show_${dependency}" onclick="GraphUtil.onSelectItemShowDependencyPanel(this);" value="${dependencyStatusInst}"/></td>
							<td style="text-align: center;"><input class="dependencyStatusControlsHighlight" type="checkbox" id="highlight_${dependency}"name="dependencyStatusControl_highlight_${dependency}" onclick="GraphUtil.onSelectItemHighlightDependencyPanel(this)" value="${dependencyStatusInst}"/></td>
						</tr>
					</g:each>
				</table>
			</div>
		</div>
		<div class="dependency_panel_action_buttons">
			<input style="margin: unset; width: 259px;" type="button" value="Apply" class="btn btn-outline fullButton" onclick="GraphUtil.applyShowHideDependencies()">
		</div>
	</div>
	<!-- The legend div containing information about the shapes and colors used in the graph -->
	<g:include controller="assetEntity" action="graphLegend" params="${[displayMoveEvents:false, displayFuture:false, displayCycles:false, displayBundleConflicts:true, arrowheadOffset:true, displayCuts:true, legendTwistiePref:legendTwistiePref]}" />

</div>

<!-- Include the graph itself -->
<g:render template="/moveBundle/force" model="${pageScope.variables}"/>

<!-- Call the main graph function as soon as this template is added to the DOM -->
<script type="text/javascript">
	buildMap();
	$('#controlPanelId').addClass('depAnalyzer');
	$('#dependenciesPanelId').addClass('depAnalyzer');
	$('#legendDivId').addClass('depAnalyzer');
</script>
