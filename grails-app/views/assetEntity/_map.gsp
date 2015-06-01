<span id="panelLink" colspan="2" style="padding: 0px;">
	<table id="mapReferenceId">
		<tr>
			<td onclick="openPanel('controlPanel')"><h4>Control Panel</h4></td>
			<td onclick="openPanel('legendDivId')"><h4>Legend</h4></td>
		</tr>
	</table>
</span>
<div id="controlPanel" style="width: 160px;display: ${(showControls=='controls')?('block'):('none')};">
	<table id="labelTree" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;" >
		<tr>
			<td style="padding: 3px 3px;" colspan="2"><h3>Control Panel</h3></td>
		</tr>
		<tr>
			<td style="padding: 0px;text-align: center;">
				<input type="button" value="Defaults"  class="pointer" onclick="resetToDefaults()">
			</td>
		
			<td colspan="2" style="padding: 0px;text-align: center;">
				<input id="playPauseButtonId" type="button" value="Freeze"  class="pointer" onclick="stopMap()">
			</td>
		</tr>
		<tr>
			<td style="padding: 0px;text-align: center;" title="Performs a min cut to split one or more applications off from the main group">
				<input type="button" id="minCutButtonId" value="Min Cut" class="pointer" onclick="cutAndRemove()">
			</td>
			<td colspan="2" style="padding: 0px;text-align: center;" title="Undoes any previous cuts">
				<input type="button" value="Undo Cuts" class="pointer" onclick="undoCuts()">
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
			<td colspan="2" style="padding: 0px 0px 6px 0px ;text-align: left;"><h4>Show Labels</h4></td>
		</tr>
		<tr>
			<td><img src="${resource(dir:'images',file:'iconApp.png')}" height="14" /></td>
			<td colspan="2" style="padding: 0px;">
				<input type="checkbox" id="Application" name="labels" value="apps" ${( labelMap.Application=='true' ) ? 'checked' : ''} class="pointer application" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleApplicationLabel');">
				<label for="Application" style="vertical-align: text-top;">Applications &nbsp;&nbsp;&nbsp;</label>
			</td>
		</tr>
		<tr>
			<td><img src="${resource(dir:'images',file:'iconServer.png')}" height="14" /></td>
			<td colspan="2" style="padding: 0px;">
				<input type="checkbox" name="labels" id="Server" ${(labelMap.Server=='true') ? 'checked' : ''} value="servers" class="pointer serverPhysical serverVirtual" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleServerLabel');"/>
				<label for="Server" style="vertical-align: text-top;">Servers</label>
			</td>
		</tr>
		<tr>
			<td><img src="${resource(dir:'images',file:'iconDB.png')}" height="14" /></td>
			<td colspan="2" style="padding: 0px;">
				<input type="checkbox" name="labels" id="Database" ${(labelMap.Database=='true') ? 'checked' : ''} value="databases" class="pointer database" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleDatabaseLabel');"/>
				<label for="Database" style="vertical-align: text-top;">Databases</label>
			</td>
		</tr>
		<tr>
			<td><img src="${resource(dir:'images',file:'iconStorage.png')}" height="21" /></td>
			<td colspan="2" style="padding: 0px;">
				<input type="checkbox" name="labels" id="StoragePhysical" ${(labelMap.StoragePhysical=='true') ? 'checked' : ''} value="storagePhysical" class="pointer storagePhysical" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleStoragePhysicalLabel');"/>
				<label for="StoragePhysical" style="vertical-align: text-top;">Storage Devices</label>
			</td>
		</tr>
		<tr>
			<td><img src="${resource(dir:'images',file:'iconStorage.png')}" height="21" /></td>
			<td colspan="2" style="padding: 0px;">
				<input type="checkbox" name="labels" id="Files" ${(labelMap.Files=='true') ? 'checked' : ''} value="files" class="pointer storageLogical" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleFilesLabel');"/>
				<label for="Files" style="vertical-align: text-top;">Logical Storage</label>
			</td>
		</tr>
		<tr>
			<td><img src="${resource(dir:'images',file:'iconNetwork.png')}" height="16" /></td>
			<td colspan="2" style="padding: 0px;">
				<input type="checkbox" name="labels" id="Network" ${(labelMap.Network=='true') ? 'checked' : ''} value="networks" class="pointer networkPhysical networkLogical" onchange="rebuildMap(false);depConsoleLabelUserpref($(this),'dependencyConsoleNetworkLabel');"/>
				<label for="Network" style="vertical-align: text-top;">Network</label>
			</td>
		</tr>
		<tr title="Sets the color of the background to black">
			<td colspan="2" style="padding-left :0px">
				Black Background&nbsp;&nbsp;&nbsp;<input type="checkbox" id="blackBackgroundId" name="blackBackground" ${(defaults.blackBackground)?('checked="checked"'):('')} style="border: 0px;background-color: #FFF;" onchange="rebuildMap(false, $('#forceId').val(), $('#linkSizeId').val(), $('#frictionId').val(), $('#thetaId').val(), $(widthId).val(), $(heightId).val());">
			</td>
		</tr>
		<tr title="Sets the amount of force between each node">
			<td style="padding: 0px;width: 30px;">Force</td>
			<td style="padding-left :5px;">
				<img src="${resource(dir:'images',file:'minus.gif')}" height="18" class="pointer" onclick="modifyParameter('sub','forceId')"/>
				<input type="text" id="forceId" class="controlPanelprop" name="force" value="${(multiple)?(-30):(defaults.force)}" disabled="disabled">
				<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer" onclick="modifyParameter('add','forceId')"/>
			</td>
		</tr>
		<tr title="Sets the desired length for the links">
			<td style="padding: 0px;width: 30px;">Links</td>
			<td style="padding-left :5px">
				<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer" onclick="modifyParameter('sub','linkSizeId')"/>
				<input type="text" id="linkSizeId" class="controlPanelprop" name="linkSize" value="${defaults.linkSize}" disabled="disabled" >
				<img src="${resource(dir:'images',file:'plus.gif')}" height="18"  class="pointer" onclick="modifyParameter('add','linkSizeId')"/>
			</td>
		</tr>
		<tr title="Sets the decay-rate of the nodes' velocity">
			<td style="padding: 0px;width: 30px;">Friction</td>
			<td style="padding-left :5px">
				<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer" onclick="modifyParameter('sub','frictionId')"/>
				<input type="text" id="frictionId" class="controlPanelprop" name="friction" value="${defaults.friction}" >
				<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer"  onclick="modifyParameter('add','frictionId')"/>
			</td>
		</tr>
		<tr title="Sets the accuracy of the forces (lower numbers will be slower, but more accurate)">
			<td style="padding: 0px;width: 30px;">Theta</td>
			<td style="padding-left :5px">
				<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer" onclick="modifyParameter('sub','thetaId')"/>
				<input type="text" id="thetaId" class="controlPanelprop" name="theta" value="${defaults.theta}" disabled="disabled" >
				<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer"  onclick="modifyParameter('add','thetaId')"/>
			</td>
		</tr>
		<tr title="Sets the height of the SVG in pixels">
			<td style="padding: 0px;width: 30px;">Height</td>
			<td style="padding-left :5px">
				<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer" onclick="modifyParameter('sub','heightId')"/>
				<input type="text" id="heightId" class="controlPanelprop" name="height" value="${defaults.height}" disabled="disabled" >
				<img src="${resource(dir:'images',file:'plus.gif')}" height="18"  class="pointer" onclick="modifyParameter('add','heightId')"/>
			</td>
		</tr>
		<tr title="Sets the width of the SVG in pixels">
			<td style="padding: 0px;width: 30px;">Width</td>
			<td style="padding-left :5px">
				<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer" onclick="modifyParameter('sub','widthId')"/>
				<input type="text" id="widthId" class="controlPanelprop" name="width" value="${defaults.width}" disabled="disabled">
				<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer"  onclick="modifyParameter('add','widthId')"/>
			</td>
		</tr>
		
	</table>
</div>
<div id="legendDivId" style="display: ${(showControls=='legend')?('block'):('none')};">
	<table id="legendId" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;width: 140px;" >
		<tr><td style="padding: 3px 3px;" colspan="2"><h3>Legend</h3></td></tr>
			<tr><td colspan="2"><span style="color: blue;"><h4>Nodes:</h4></span></td></tr>
			<tr>
				<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconApp.png')}" height="14" /></td>
				<td><span style="vertical-align: text-top;">Applications</span></td>
			</tr>
			<tr>
				<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconServer.png')}" height="14" /></td>
				<td><span style="vertical-align: text-top;">Servers</span></td>
			</tr>
			<tr>
				<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconDB.png')}" height="14" /></td>
				<td><span style="vertical-align: text-top;">Databases</span></td>
			</tr>
			<tr>
				<td nowrap="nowrap"><img src="${resource(dir:'images',file:'iconStorage.png')}" height="21" /></td>
				<td><span style="vertical-align: text-top;">Storage Devices</span></td>
			</tr>
			<tr>
				<td nowrap="nowrap"><img src="${resource(dir:'images',file:'iconStorage.png')}" height="21" /></td>
				<td><span style="vertical-align: text-top;">Logical Storage</span></td>
			</tr>
			<tr>
				<td nowrap="nowrap"><img src="${resource(dir:'images',file:'iconNetwork.png')}" height="16" /></td>
				<td><span style="vertical-align: text-top;">Network</span></td>
			</tr>
			<tr><td width="5px"><hr style="width: 30px;height: 1px;background-color:rgb(56,56,56);"></hr></td><td>Valid Links</td></tr>
			<tr><td><hr style="width: 30px;height: 1px;background-color:red;"></hr></td><td>Questioned</td></tr>
			<tr><td><hr style="width: 30px;height: 1px;background-color:rgb(224,224,224);"></hr></td><td>N/A</td></tr>
			<tr><td nowrap="nowrap" colspan="2"><span style="color: Gray;"><h4>Events:</h4></span></td></tr>
			<g:each in="${eventColorCode}" var="color">
				<tr>
					<td>
						<input type="text" size="1" style="background-color: ${color.value};height:5px;width:5px;" />
					</td>
					<td nowrap="nowrap">
						${color.key}
					</td>
					
				</tr>
			</g:each>
			<tr>
				<td>
					<input type="text" size="1" style="border: 2px solid red;height:5px;width:5px;" />
				</td>
				<td nowrap="nowrap">
						No Event
				</td>
			</tr>
	</table>
</div>

<g:render template="../moveBundle/force" model="${pageScope.variables}"/>

<script type="text/javascript">
	var fullWidth = $('.main_bottom').width() - ($('#svgContainerId').offset().left * 2);
	var finalWidth = Math.max(fullWidth, $('#width').val());
	$('#width').val(finalWidth);
	$('#widthId').val($('#width').val());
	buildMap(null, null, null, null, finalWidth, $('#height').val());
</script>