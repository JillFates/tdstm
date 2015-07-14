<div id="toolsContainerId">
	<span id="panelLink" colspan="2" class="noPadding">
		<div id="mapReferenceId">
			<div id="controlPanelTabId" class="graphPanelTab" onclick="GraphUtil.togglePanel('control')"><h4>Control Panel</h4></div><!-- This comment prevents the browser from trying to evaluate the whitespace between these divs as a space character
			--><div id="legendTabId" class="graphPanelTab" onclick="GraphUtil.togglePanel('legend')"><h4>Legend</h4></div>
			<div id="fullscreenButtonId" onclick="GraphUtil.toggleFullscreen()" title="Toggles fullscreen mode"><h4>Fullscreen</h4></div>
		</div>
	</span>
	<div id="controlPanel" class="graphPanel">
		<form id="preferencesformId">
			<table class="labelTree savedToPrefs" cellpadding="0" cellspacing="0">
				
				<!-- Miscellaneous settings and controls -->
				<tr>
					<td colspan="3" class="noPadding">
						<input id="playPauseButtonId" type="button" value="Freeze Graph" class="pointer fullButton" onclick="GraphUtil.toggleFreeze()">
					</td>
				</tr>
				<tr>
					<td colspan="3" class="noPadding">
						<input id="OptimizeButtonId" type="button" value="Optimize Graph" class="pointer fullButton" onclick="setIdealGraphPosition()">
					</td>
				</tr>
				
				<tr title="Sets the criteria used to determine node fill color">
					<td colspan="3" style="padding-left :0px">
						Color By:&nbsp;
						<div id="colorByFormId">
							<input type="radio" id="colorByDepGroupId" name="colorBy" class="pointer" value="group" onchange="rebuildMap(false);" ${(graphPrefs.colorBy == 'group')?('checked="checked"'):('')}>
							<label for="colorByDepGroupId" class="pointer">Group&nbsp;</label>
							<input type="radio" id="colorByMoveBundleId" name="colorBy" class="pointer" value="bundle" onchange="rebuildMap(false);" ${(graphPrefs.colorBy == 'bundle')?('checked="checked"'):('')}>
							<label for="colorByMoveBundleId" class="pointer">Bundle&nbsp;</label>
							<input type="radio" id="colorByMoveEventId" name="colorBy" class="pointer" value="event" onchange="rebuildMap(false);" ${(graphPrefs.colorBy == 'event')?('checked="checked"'):('')}>
							<label for="colorByMoveEventId" class="pointer">Event</label>
						</div>
					</td>
				</tr>
				<tr title="If checked, bundle conflicts will be highlighted">
					<td colspan="3" style="padding-left :0px">
						<input type="checkbox" id="bundleConflictsId" name="bundleConflicts" class="pointer" value="true" ${(graphPrefs.bundleConflicts) ? 'checked' : ''}>
						<label for="bundleConflictsId" class="pointer">&nbsp;Show Bundle Conflicts</label>
						
					</td>
				</tr>
				<tr title="Sets the color of the background to black">
					<td colspan="3" style="padding-left :0px">
						<input type="checkbox" id="blackBackgroundId" name="blackBackground" class="pointer" value="true" ${(graphPrefs.blackBackground)?('checked="checked"'):('')} onchange="rebuildMap(false)">
						<label for="blackBackgroundId" class="pointer">&nbsp;Black Background</label>
					</td>
				</tr>
				
				<tr>
					<td colspan="3" class="noPadding">
						<br />
					</td>
				</tr>
				
				<!-- Label checkboxes -->
				<tr>
					<td colspan="2" style="padding: 0px 0px 6px 0px ;text-align: left;"><h4>Show Labels:</h4></td>
				</tr>
				<tr class="labelToggleRow">
					<td>
						<input type="checkbox" id="Application" name="appLbl" value="true" ${(graphPrefs.appLbl) ? 'checked' : ''} class="pointer application" onchange="rebuildMap(false)">
					</td>
					<td colspan="2" class="labelToggleCol noPadding">
						<label for="Application" class="pointer">
							<svg><use xlink:href="${'#applicationShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
							Applications
						</label>
					</td>
				</tr>
				<tr class="labelToggleRow">
					<td>
						<input type="checkbox" name="srvLbl" id="Server" ${(graphPrefs.srvLbl) ? 'checked' : ''} value="true" class="pointer serverPhysical serverVirtual" onchange="rebuildMap(false)"/>
					</td>
					<td colspan="2" class="labelToggleCol noPadding">
						<label for="Server" class="pointer">
							<svg><use xlink:href="${'#serverPhysicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
							Servers
						</label>
					</td>
				</tr>
				<tr class="labelToggleRow">
					<td>
						<input type="checkbox" name="dbLbl" id="Database" ${(graphPrefs.dbLbl) ? 'checked' : ''} value="true" class="pointer database" onchange="rebuildMap(false)"/>
					</td>
					<td colspan="2" class="labelToggleCol noPadding">
						<label for="Database" class="pointer">
							<svg><use xlink:href="${'#databaseShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
							Databases
						</label>
					</td>
				</tr>
				<tr class="labelToggleRow">
					<td>
						<input type="checkbox" name="spLbl" id="StoragePhysical" ${(graphPrefs.spLbl) ? 'checked' : ''} value="true" class="pointer storagePhysical" onchange="rebuildMap(false)"/>
					</td>
					<td colspan="2" class="labelToggleCol noPadding">
						<label for="StoragePhysical" class="pointer">
							<svg><use xlink:href="${'#storagePhysicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
							Storage Devices
						</label>
					</td>
				</tr>
				<tr class="labelToggleRow">
					<td>
						<input type="checkbox" name="slLbl" id="Files" ${(graphPrefs.slLbl) ? 'checked' : ''} value="true" class="pointer storageLogical" onchange="rebuildMap(false)"/>
					</td>
					<td colspan="2" class="labelToggleCol noPadding">
						<label for="Files" class="pointer">
							<svg><use xlink:href="${'#storageLogicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
							Logical Storage
						</label>
					</td>
				</tr>
				<tr class="labelToggleRow">
					<td>
						<input type="checkbox" name="netLbl" id="Network" ${(graphPrefs.netLbl) ? 'checked' : ''} value="true" class="pointer networkPhysical networkLogical" onchange="rebuildMap(false)"/>
					</td>
					<td colspan="2" class="labelToggleCol noPadding">
						<label for="Network" class="pointer">
							<svg><use xlink:href="${'#networkPhysicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
							Network
						</label>
					</td>
				</tr>
				
				<tr>
					<td colspan="3" class="noPadding">
						<br />
					</td>
				</tr>
				
				<!-- Suggest splits controls -->
				<tr>
					<td colspan="3" class="noPadding" >
						<input type="button" id="minCutButtonId" value="Suggest Splits" class="pointer halfButton" onclick="cutAndRemove()" title="Each time clicked will present a suggested division of the group by least number of cuts">
						<input type="button" value="Undo Splits" class="pointer halfButton" onclick="undoCuts()" title="Undoes any previous cuts">
					</td>
				</tr>
				<tr title="Maximum number of edges that can be cut at once">
					<td colspan="3" class="noPadding">
						<g:select name="maxEdgeCount" id="maxEdgeCountId" from="${1..20}" value="${graphPrefs.maxEdgeCount ? graphPrefs.maxEdgeCount : 4}"></g:select>
						<label for="maxEdgeCountId" style="vertical-align: text-top;">&nbsp;Max dependencies cut</label>
					</td>
				</tr>
				<tr>
					<td colspan="3" class="noPadding">
						<br />
					</td>
				</tr>
				
				<tr id="twistieRowId" class="closed">
					<td colspan="3" class="noPadding">
						Layout:&nbsp;<svg class="pointer" style="width: 12px;height: 12px;border-width: 0px;" onclick="GraphUtil.toggleGraphTwistie()"><g transform="rotate(90 6 6)"><g id="twistieId" class=""><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
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
						<img src="${resource(dir:'images',file:'minus.gif')}" height="18" class="pointer plusMinusIcon minus" onclick="modifyParameter('sub','forceId')"/>
						<input type="text" id="forceId" class="controlPanelprop" name="force" value="${(multiple)?(-30):(defaults.force)}" disabled="disabled" />
						<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer plusMinusIcon plus" onclick="modifyParameter('add','forceId')"/>
					</td>
				</tr>
				<tr class="layoutControl" title="Sets the desired length for the links">
					<td style="padding: 0px;width: 30px;">Links</td>
					<td style="padding-left :5px">
						<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer plusMinusIcon minus" onclick="modifyParameter('sub','linkSizeId')"/>
						<input type="text" id="linkSizeId" class="controlPanelprop" name="linkSize" value="${defaults.linkSize}" disabled="disabled" />
						<img src="${resource(dir:'images',file:'plus.gif')}" height="18"  class="pointer plusMinusIcon plus" onclick="modifyParameter('add','linkSizeId')"/>
					</td>
				</tr>
				<tr class="layoutControl" title="Sets the decay-rate of the nodes' velocity">
					<td style="padding: 0px;width: 30px;">Friction</td>
					<td style="padding-left :5px">
						<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer plusMinusIcon minus" onclick="modifyParameter('sub','frictionId')"/>
						<input type="text" id="frictionId" class="controlPanelprop" name="friction" value="${defaults.friction}" disabled="disabled" />
						<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer plusMinusIcon plus"  onclick="modifyParameter('add','frictionId')"/>
					</td>
				</tr>
				<tr class="layoutControl" title="Sets the accuracy of the forces (lower numbers will be slower, but more accurate)">
					<td style="padding: 0px;width: 30px;">Theta</td>
					<td style="padding-left :5px">
						<img src="${resource(dir:'images',file:'minus.gif')}" height="18" class="pointer plusMinusIcon minus" onclick="modifyParameter('sub','thetaId')"/>
						<input type="text" id="thetaId" class="controlPanelprop" name="theta" value="${defaults.theta}" disabled="disabled" />
						<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer plusMinusIcon plus" onclick="modifyParameter('add','thetaId')"/>
					</td>
				</tr>
			</table>
		</div>
		
		<!-- Preference controls -->
		<table class="labelTree" cellpadding="0" cellspacing="0">
				
			<tr>
				<td colspan="3" class="noPadding">
					<br />
				</td>
			</tr>
			
			<tr>
				<td colspan="3" class="noPadding">
					<input id="updatePrefsButtonId" type="button" value="Save Preferences" class="pointer fullButton" onclick="GraphUtil.updateUserPrefs('depGraph')">
				</td>
			</tr>
			<tr>
				<td colspan="3" class="noPadding">
					<input id="resetPrefsButtonId" type="button" value="Reset Defaults" class="pointer fullButton" onclick="GraphUtil.resetToDefaults('depGraph')">
				</td>
			</tr>
		</table>
	</div>
	<g:include controller="assetEntity" action="graphLegend" params="${[displayMoveEvents:false, displayFuture:false, displayCycles:false, displayBundleConflicts:true, arrowheadOffset:true, displayCuts:true]}" />
</div>
<g:render template="../moveBundle/force" model="${pageScope.variables}"/>

<script type="text/javascript">
	buildMap(null, null, null, null, -1, -1);
	GraphUtil.resetGraphSize();
</script>