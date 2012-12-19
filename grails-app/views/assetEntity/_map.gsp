<span id="panelLink" colspan="2" style="padding: 0px;cursor: pointer;"  onclick="hidePanel()"><h4>Control Panel</h4></span>
<div id="controlPanel" style="float: left;border: 1px solid #ccc;margin-left: 3px;margin-top: 3px;width: 150px; background-color:white;position: absolute;display: none;">
				<table id="labelTree" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;width: 148px;" >
					<tr>
						<td style="padding: 0px;width: 30px;">Force</td>
						<td style="padding-left :5px;">
							<img src="${resource(dir:'images',file:'minus.gif')}" height="18" class="pointer" onclick="increaseValue('sub','forceId')"/>
							<input type="text" id="forceId" name="force" value="${force}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;vertical-align: top;">
							<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer" onclick="increaseValue('add','forceId')"/>
						</td>
					</tr>
					<tr>
						<td style="padding: 0px;width: 30px;">Links</td>
						<td style="padding-left :5px">
							<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer" onclick="increaseValue('sub','linksId')"/>
							<input type="text" id="linksId" name="links" value="${distance}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;" >
							<img src="${resource(dir:'images',file:'plus.gif')}" height="18"  class="pointer" onclick="increaseValue('add','linksId')"/>
						</td>
					</tr>
					<tr>
						<td style="padding: 0px;width: 30px;">Friction</td>
						<td style="padding-left :5px">
							<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer" onclick="increaseValue('sub','frictionId')"/>
							<input type="text" id="frictionId" name="friction" value="${friction}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;" >
							<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer"  onclick="increaseValue('add','frictionId')"/>
						</td>
					</tr>
					<tr>
						<td style="padding: 0px;width: 30px;">Height</td>
						<td style="padding-left :5px">
							<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer" onclick="increaseValue('sub','heightId')"/>
							<input type="text" id="heightId" name="height" value="${height}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;" >
							<img src="${resource(dir:'images',file:'plus.gif')}" height="18"  class="pointer" onclick="increaseValue('add','heightId')"/>
						</td>
					</tr>
					<tr>
						<td style="padding: 0px;width: 30px;">Width</td>
						<td style="padding-left :5px">
							<img src="${resource(dir:'images',file:'minus.gif')}" height="18"  class="pointer" onclick="increaseValue('sub','widthId')"/>
							<input type="text" id="widthId" name="width" value="${width}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;" >
							<img src="${resource(dir:'images',file:'plus.gif')}" height="18" class="pointer"  onclick="increaseValue('add','widthId')"/>
						</td>
					</tr>
					<tr>
						<td colspan="2" style="padding: 0px 0px 6px 0px ;text-align: left;"><h4>Show Labels</h4></td>
					</tr>
					<tr>
						<g:if test="${labels?.contains('apps')}">
						   <td><img src="${resource(dir:'images',file:'iconApp.png')}" height="14" /></td>
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" id="apps" name="labels" value="apps" checked="checked" class="pointer">
								<label for="apps" style="vertical-align: text-top;">Apps &nbsp;&nbsp;&nbsp;
								</label>
							</td>
						</g:if>
						<g:else>
						  	<td><img src="${resource(dir:'images',file:'iconApp.png')}" height="14" /></td>
						    <td colspan="2" style="padding: 0px;">
						    	<input type="checkbox" id="appLabel" name="labels" value="apps" class="pointer"/>
						    	<label for="appLabel" style="vertical-align: text-top;">Apps &nbsp;&nbsp;&nbsp;
								</label>
						    </td>
						</g:else>
					</tr>
					<tr>
						<g:if test="${labels?.contains('servers')}">
						 	<td><img src="${resource(dir:'images',file:'iconServer.png')}" height="14" /></td>
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" name="labels" id="servers" checked="checked" value="servers" class="pointer"/>
								<label for="servers" style="vertical-align: text-top;">Servers
								</label>
							</td>
						</g:if>
						<g:else>
							<td><img src="${resource(dir:'images',file:'iconServer.png')}" height="14" /></td>
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" id="serverLabel" name="labels" value="servers" title= "Servers" class="pointer"/>
								<label for="serverLabel" style="vertical-align: text-top;">Servers
								</label>
							</td>
						</g:else>
					</tr>
					<tr>
						<g:if test="${labels?.contains('databases')}">
							<td><img src="${resource(dir:'images',file:'iconDB.png')}" height="14" /></td>
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" name="labels" id="dbs" checked="checked" value="databases" class="pointer"/>
								<label for="dbs" style="vertical-align: text-top;">DB
								</label>
							</td>
						</g:if>
						<g:else>
							<td><img src="${resource(dir:'images',file:'iconDB.png')}" height="14" /></td>
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" id="dbsLabel" name="labels" value="databases" class="pointer"/>
								<label for="dbsLabel" style="vertical-align: text-top;">DB
								</label>
							</td>
						</g:else>
					</tr>
					<tr>
						<g:if test="${labels?.contains('files')}">
							<td><img src="${resource(dir:'images',file:'iconDB.png')}" height="14" /></td>
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" name="labels" id="filesLabel" checked="checked" value="files" class="pointer"/>
								<label for="filesLabel" style="vertical-align: text-top;">Storage
								</label>
							</td>
						</g:if>
						<g:else>
							<td><img src="${resource(dir:'images',file:'iconStorage.png')}" height="21" /></td>
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" id="filesLabel" name="labels" value="files" class="pointer"/>
								<label for="filesLabel" style="vertical-align: text-top;">Storage
								</label>
							</td>
						</g:else>
					</tr>
					<tr>
						<td colspan="2" style="padding: 0px;text-align: center;">
						<input type="button" value="Refresh Map"  class="pointer" onclick="refreshMap($('#forceId').val(), $('#linksId').val() ,$('#frictionId').val(), $('#heightId').val(), $('#widthId').val())">
						</td>
					</tr>
				</table>
			</div>
			<div id="legendDivId" style="float: left;border: 1px solid #ccc;margin-left: 3px;margin-top: 3px;width: 178px; background-color:white;position: absolute;">
			<table id="legendId" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;width: 140px;" >
				<tr><td style="padding: 3px 3px;" colspan="2"><h3>Legend</h3></td></tr>
					<tr><td colspan="2"><span style="color: blue;"><h4>Nodes:</h4></span></td></tr>
					<tr>
						<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconApp.png')}"
							height="14" /></td><td><span style="vertical-align: text-top;">Apps</span></td>
					</tr>
					<tr>
						<td nowrap="nowrap" ><img src="${resource(dir:'images',file:'iconServer.png')}"
							height="14" /></td><td><span style="vertical-align: text-top;">Servers</span></td>
					</tr>
					<tr>
						<td nowrap="nowrap" ><img
							src="${resource(dir:'images',file:'iconDB.png')}" height="14" /></td>
							<td><span style="vertical-align: text-top;">DB</span></td>
					</tr>
					<tr>
						<td nowrap="nowrap"><img
							src="${resource(dir:'images',file:'iconStorage.png')}" height="21" /></td>
							<td><span style="vertical-align: text-top;">Storage</span></td>
					</tr>
					<tr><td width="5px"><hr style="width: 30px;color:rgb(56,56,56);"></hr></td><td>Valid Links</td></tr>
					<tr><td><hr style="width: 30px;color:red;"></hr></td><td>Questioned</td></tr>
					<tr><td><hr style="width: 30px;color:rgb(224,224,224);"></hr></td><td>N/A</td></tr>
					<tr><td nowrap="nowrap" colspan="2"><span style="color: Gray;"><h4>Move Events:</h4></span></td></tr>
					<g:each in="${eventColorCode}" var="color">
						<tr>
						   <td><input type="text" size="1"
								style="background-color: ${color.value};height:5px;width:5px;" />
							</td>
							<td nowrap="nowrap">
								${color.key}
							</td>
							
						</tr>
					</g:each>
					<tr><td><input type="text" size="1"
								style="border: 2px solid red;height:5px;width:5px;" />
						</td>
						<td nowrap="nowrap">
								No Event
						</td>
					</tr>
			</table>
			</div>
			
<g:render template="../d3/force/force" model="[file_name:file_name]"/>
