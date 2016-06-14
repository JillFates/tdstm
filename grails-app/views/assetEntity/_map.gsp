<div id="toolsContainerId">
	<span id="panelLink" colspan="2" class="noPadding">
		<div id="mapReferenceId">
			<div id="controlPanelTabId" class="graphPanelTab" onclick="GraphUtil.togglePanel('control')"><h4>Control Panel</h4></div><!-- This comment prevents the browser from trying to evaluate the whitespace between these divs as a space character
			--><div id="legendTabId" class="graphPanelTab" onclick="GraphUtil.togglePanel('legend')"><h4>Legend</h4></div><!--
			--><div id="fullscreenButtonId" class="graphButton graphTabButton" onclick="GraphUtil.toggleFullscreen()" title="Toggles fullscreen mode"><h4>Fullscreen</h4></div><!--
			--><div id="refreshButtonId" class="graphButton graphTabButton" onclick="getList('graph', ${depGroup == 'onePlus' ? '\'onePlus\'' : depGroup})" title="Refreshes the graph"><h4>Refresh</h4></div>
		</div>
	</span>
	<div id="controlPanel" class="graphPanel">
		<form id="preferencesformId">
			<table class="labelTree savedToPrefs" cellpadding="0" cellspacing="0">
				
				<!-- Miscellaneous settings and controls -->
				<tr>
					<td colspan="3" class="noPadding">
						<input id="playPauseButtonId" type="button" value="Freeze Graph" class="pointer fullButton graphButton" onclick="GraphUtil.toggleFreeze()">
					</td>
				</tr>
				<tr>
					<td colspan="3" class="noPadding">
						<input id="OptimizeButtonId" type="button" value="Maximize Graph" class="pointer fullButton graphButton" onclick="setIdealGraphPosition()">
					</td>
				</tr>
				
				<!-- Suggest splits controls -->
				<tr>
					<td colspan="3" class="noPadding" >
						<input type="button" id="minCutButtonId" value="Suggest Splits" class="pointer halfButton graphButton" onclick="cutAndRemove()" title="Each time clicked will present a suggested division of the group by least number of cuts">&nbsp;<!--
						--><input type="button" value="Undo Splits" class="pointer halfButton graphButton" onclick="undoCuts()" title="Undoes any previous cuts">
					</td>
				</tr>
				<tr title="Maximum number of edges that can be cut at once">
					<td colspan="3" class="noPadding">
						<g:select name="maxEdgeCount" id="maxEdgeCountId" from="${1..20}" value="${graphPrefs.maxEdgeCount ? graphPrefs.maxEdgeCount : defaultPrefs.maxEdgeCount}"></g:select><!--
						--><label for="maxEdgeCountId" style="vertical-align: text-top;">&nbsp;Max dependencies cut</label>
					</td>
				</tr>
				
				<tr>
					<td colspan="3" class="noPadding">
						<br />
					</td>
				</tr>
				
				<tr title="Sets the criteria used to determine node fill color">
					<td colspan="3" style="padding-left :0px">
						<label>Color By:</label>
						<div id="colorByFormId"><!--
							--><span class="checkboxContainer pointer"><!--
								--><input type="radio" id="colorByDepGroupId" name="colorBy" class="pointer" value="group" onchange="rebuildMap(false);" ${(graphPrefs.colorBy == 'group')?('checked="checked"'):('')}><!--
								--><label for="colorByDepGroupId" class="pointer">&nbsp;Group</label><!--
							--></span><!--
							--><span class="checkboxContainer pointer">
								<input type="radio" id="colorByMoveBundleId" name="colorBy" class="pointer" value="bundle" onchange="rebuildMap(false);" ${(graphPrefs.colorBy == 'bundle')?('checked="checked"'):('')}><!--
								--><label for="colorByMoveBundleId" class="pointer">&nbsp;Bundle</label><!--
							--></span><!--
							--><span class="checkboxContainer pointer">
								<input type="radio" id="colorByMoveEventId" name="colorBy" class="pointer" value="event" onchange="rebuildMap(false);" ${(graphPrefs.colorBy == 'event')?('checked="checked"'):('')}><!--
								--><label for="colorByMoveEventId" class="pointer">&nbsp;Event</label>
							</span>
						</div>
					</td>
				</tr>
				<tr title="If checked, bundle conflicts will be highlighted">
					<td colspan="3" style="padding-left :0px">
						<span class="checkboxContainer">
							<input type="checkbox" id="bundleConflictsId" name="bundleConflicts" class="pointer" value="true" ${(graphPrefs.bundleConflicts) ? 'checked' : ''}><!--
							--><label for="bundleConflictsId" class="pointer">&nbsp;Show Bundle Conflicts</label>
						</span>
						
					</td>
				</tr>
				<tr title="Sets the color of the background to black">
					<td colspan="3" style="padding-left :0px">
						<span class="checkboxContainer">
							<input type="checkbox" id="blackBackgroundId" name="blackBackground" class="pointer" value="true" ${(graphPrefs.blackBackground)?('checked="checked"'):('')} onchange="rebuildMap(false)"><!--
							--><label for="blackBackgroundId" class="pointer">&nbsp;Black Background</label>
						</span>
					</td>
				</tr>
				
				
				
				<tr>
					<td colspan="3" class="noPadding">
						<br />
					</td>
				</tr>
				
				<!-- Label checkboxes -->
				<tr id="twistieRowId">
					<td colspan="3" class="noPadding">
						<span id="twistieSpanId" class="closed pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="labelControlContainerId">
							Show Labels:&nbsp;<!--
							--><svg style="width: 12px;height: 12px;border-width: 0px;"><g transform="rotate(90 6 6)"><g id="twistieId"><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
						</span>
					</td>
				</tr>
			</table>
			<div id="labelControlContainerId">
				<table class="labelTree" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;" >
					<g:each in="${assetTypes}" var="entry" status="i">
						<g:set var="type" value="${entry.getKey()}" />
						<g:set var="names" value="${entry.getValue()}" />
						<g:if test="${ ! names.labelHandles.equals('') }">
						<tr class="labelToggleRow">
							<td colspan="3" class="labelToggleCol">
								<div style="padding:0px;">
									<div class="checkboxContainer">
										<input type="checkbox" id="${type}CheckboxId" name="${names.labelPreferenceName}" value="true" ${(graphPrefs[names.labelPreferenceName]) ? 'checked' : ''} class="pointer ${names.labelHandles}" onchange="rebuildMap(false)" /><!--
										--><label for="${type}CheckboxId" class="pointer">
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
				<tr>
					<td colspan="3" class="noPadding">
						<br />
					</td>
				</tr>
				
				<tr id="twistieRowId">
					<td colspan="3" class="noPadding">
						<span id="twistieSpanId" class="closed pointer" onclick="GraphUtil.toggleGraphTwistie($(this))" for="layoutControlContainerId">
							Layout:&nbsp;<!--
							--><svg style="width: 12px;height: 12px;border-width: 0px;"><g transform="rotate(90 6 6)"><g id="twistieId" class=""><path d="M10 6 L4 10 L4 2 Z" class="link NotApplicable"></g></g></svg>
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
						<img src="${resource(dir:'images',file:'minus.gif')}" height="18" class="pointer plusMinusIcon minus" onclick="modifyParameter('sub','forceId')"/>
						<input type="text" id="forceId" class="controlPanelprop" name="force" value="${defaults.force}" disabled="disabled" />
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
					<input id="updatePrefsButtonId" type="button" value="Save Preferences" class="pointer fullButton graphButton" onclick="GraphUtil.updateUserPrefs('depGraph')">
				</td>
			</tr>
			<tr>
				<td colspan="3" class="noPadding">
					<input id="resetPrefsButtonId" type="button" value="Reset Defaults" class="pointer fullButton graphButton" onclick="GraphUtil.resetToDefaults('depGraph')">
				</td>
			</tr>
		</table>
	</div>
	<g:include controller="assetEntity" action="graphLegend" params="${[displayMoveEvents:false, displayFuture:false, displayCycles:false, displayBundleConflicts:true, arrowheadOffset:true, displayCuts:true]}" />
</div>
<g:render template="../moveBundle/force" model="${pageScope.variables}"/>

<script type="text/javascript">
	buildMap();
</script>