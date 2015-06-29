<div id="toolsContainerId">
	<span id="panelLink" colspan="2" style="padding: 0px;">
		<table id="mapReferenceId">
			<tr>
				<td onclick="openPanel('controlPanel')"><h4>Control Panel</h4></td>
				<td onclick="openPanel('legendDivId')"><h4>Legend</h4></td>
				<td id="fullscreenButtonId" onclick="GraphUtil.toggleFullscreen()" title="Toggles fullscreen mode"><h4>Fullscreen</h4></td>
			</tr>
		</table>
	</span>
	<div id="controlPanel" style="display: ${(showControls=='controls')?('block'):('none')};">
		<table id="labelTree" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;" >
			<tr>
				<td style="padding: 3px 3px;" colspan="2"><h3>Control Panel</h3></td>
			</tr>
			<tr>
				<td colspan="3" style="padding: 0px;" >
					<input type="button" id="minCutButtonId" value="Minimum Cut" class="pointer" onclick="cutAndRemove()" title="Performs a min cut to split one or more applications off from the main group">&nbsp;
					<input type="button" value="Undo Cuts" class="pointer" onclick="undoCuts()" title="Undoes any previous cuts">
				</td>
			</tr>
			<tr title="Maximum number of edges that can be cut at once">
				<td colspan="3" style="padding: 0px;">
					<input type="text" name="maxEdgeCount" id="maxEdgeCountId" value="4" class="pointer" size="2" maxlength="2" style="width:20px;"/>
					<label for="maxEdgeCountId" style="vertical-align: text-top;">&nbsp;Maximum edges in cut</label>
				</td>
			</tr>
			<tr title="Maximum number of attempts at finding the optimal cut. Higher values may produce better cuts but will take longer">
				<td colspan="3" style="padding: 0px;">
					<input type="text" name="maxCutAttempts" id="maxCutAttemptsId" value="200" class="pointer" size="5" maxlength="5" style="width:20px;"/>
					<label for="maxCutAttemptsId" style="vertical-align: text-top;">&nbsp;Maximum cut attempts</label>
				</td>
			</tr>
			
			<tr>
				<td colspan="3" style="padding: 0px;">
					<br />
				</td>
			</tr>
			
			<tr title="Sets the color of the background to black">
				<td colspan="3" style="padding-left :0px">
					<input type="checkbox" id="blackBackgroundId" name="blackBackground" class="pointer" ${(defaults.blackBackground)?('checked="checked"'):('')} style="border: 0px;background-color: #FFF;" onchange="rebuildMap(false, $('#forceId').val(), $('#linkSizeId').val(), $('#frictionId').val(), $('#thetaId').val());">
					&nbsp;Black Background
				</td>
			</tr>
			
			<tr>
				<td colspan="3" style="padding: 0px;">
					<br />
				</td>
			</tr>
			
			<tr>
				<td colspan="2" style="padding: 0px 0px 6px 0px ;text-align: left;"><h4>Show Labels:</h4></td>
			</tr>
			<tr class="labelToggleRow">
				<td>
					<input type="checkbox" id="Application" name="labels" value="apps" ${( labelMap.Application=='true' ) ? 'checked' : ''} class="pointer application" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleApplicationLabel');">
				</td>
				<td colspan="2" style="padding: 0px;" class="labelToggleCol">
					<svg><use xlink:href="${'#applicationShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
					<label for="Application">Applications &nbsp;&nbsp;&nbsp;</label>
				</td>
			</tr>
			<tr class="labelToggleRow">
				<td>
					<input type="checkbox" name="labels" id="Server" ${(labelMap.Server=='true') ? 'checked' : ''} value="servers" class="pointer serverPhysical serverVirtual" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleServerLabel');"/>
				</td>
				<td colspan="2" style="padding: 0px;" class="labelToggleCol">
					<svg><use xlink:href="${'#serverPhysicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
					<label for="Server">Servers</label>
				</td>
			</tr>
			<tr class="labelToggleRow">
				<td>
					<input type="checkbox" name="labels" id="Database" ${(labelMap.Database=='true') ? 'checked' : ''} value="databases" class="pointer database" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleDatabaseLabel');"/>
				</td>
				<td colspan="2" style="padding: 0px;" class="labelToggleCol">
					<svg><use xlink:href="${'#databaseShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
					<label for="Database">Databases</label>
				</td>
			</tr>
			<tr class="labelToggleRow">
				<td>
					<input type="checkbox" name="labels" id="StoragePhysical" ${(labelMap.StoragePhysical=='true') ? 'checked' : ''} value="storagePhysical" class="pointer storagePhysical" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleStoragePhysicalLabel');"/>
				</td>
				<td colspan="2" style="padding: 0px;" class="labelToggleCol">
					<svg><use xlink:href="${'#storagePhysicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
					<label for="StoragePhysical">Storage Devices</label>
				</td>
			</tr>
			<tr class="labelToggleRow">
				<td>
					<input type="checkbox" name="labels" id="Files" ${(labelMap.Files=='true') ? 'checked' : ''} value="files" class="pointer storageLogical" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleFilesLabel');"/>
				</td>
				<td colspan="2" style="padding: 0px;" class="labelToggleCol">
					<svg><use xlink:href="${'#storageLogicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
					<label for="Files">Logical Storage</label>
				</td>
			</tr>
			<tr class="labelToggleRow">
				<td>
					<input type="checkbox" name="labels" id="Network" ${(labelMap.Network=='true') ? 'checked' : ''} value="networks" class="pointer networkPhysical networkLogical" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleNetworkLabel');"/>
				</td>
				<td colspan="2" style="padding: 0px;" class="labelToggleCol">
					<svg><use xlink:href="${'#networkPhysicalShapeId'}" class="node" x="15" y="15" style="fill: #1f77b4; stroke: #666666;"></use></svg>
					<label for="Network">Network</label>
				</td>
			</tr>
			
			<tr>
				<td colspan="3" style="padding: 0px;">
					<br />
				</td>
			</tr>
			
			<tr>
				<td colspan="3" style="padding: 0px;">
					<input type="button" value="Defaults"  class="pointer" onclick="resetToDefaults()">&nbsp;
					<input id="playPauseButtonId" type="button" value="Freeze"  class="pointer" onclick="stopMap()">
				</td>
			</tr>
			<tr title="Sets the amount of force between each node">
				<td style="padding: 0px;width: 30px;">Force</td>
				<td style="padding-left :5px;">
					<img src="${resource(dir:'images',file:'minus.gif')}" height="18" class="pointer plusMinusIcon" onclick="modifyParameter('sub','forceId')"/>
					<input type="text" id="forceId" class="controlPanelprop" name="force" value="${(multiple)?(-30):(defaults.force)}" disabled="disabled">
					<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer plusMinusIcon" onclick="modifyParameter('add','forceId')"/>
				</td>
			</tr>
			<tr title="Sets the desired length for the links">
				<td style="padding: 0px;width: 30px;">Links</td>
				<td style="padding-left :5px">
					<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer plusMinusIcon" onclick="modifyParameter('sub','linkSizeId')"/>
					<input type="text" id="linkSizeId" class="controlPanelprop" name="linkSize" value="${defaults.linkSize}" disabled="disabled" >
					<img src="${resource(dir:'images',file:'plus.gif')}" height="18"  class="pointer plusMinusIcon" onclick="modifyParameter('add','linkSizeId')"/>
				</td>
			</tr>
			<tr title="Sets the decay-rate of the nodes' velocity">
				<td style="padding: 0px;width: 30px;">Friction</td>
				<td style="padding-left :5px">
					<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer plusMinusIcon" onclick="modifyParameter('sub','frictionId')"/>
					<input type="text" id="frictionId" class="controlPanelprop" name="friction" value="${defaults.friction}" >
					<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer plusMinusIcon"  onclick="modifyParameter('add','frictionId')"/>
				</td>
			</tr>
			<tr title="Sets the accuracy of the forces (lower numbers will be slower, but more accurate)">
				<td style="padding: 0px;width: 30px;">Theta</td>
				<td style="padding-left :5px" class="pointer plusMinusIcon">
					<img src="${resource(dir:'images',file:'minus.gif')}" height="18" class="pointer plusMinusIcon" onclick="modifyParameter('sub','thetaId')"/>
					<input type="text" id="thetaId" class="controlPanelprop" name="theta" value="${defaults.theta}" disabled="disabled" >
					<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer plusMinusIcon" onclick="modifyParameter('add','thetaId')"/>
				</td>
			</tr>
		</table>
	</div>
	<g:include controller="assetEntity" action="graphLegend" params="${[displayMoveEvents:false, displayFuture:false, displayCycles:false]}" />
</div>
<g:render template="../moveBundle/force" model="${pageScope.variables}"/>

<script type="text/javascript">
	buildMap(null, null, null, null, -1, -1);
	GraphUtil.resetGraphSize();
</script>