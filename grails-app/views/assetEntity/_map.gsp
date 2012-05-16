<span id="panelLink" colspan="2" style="padding: 0px;cursor: pointer;"  onclick="hidePanel()"><h4>Control Panel</h4></span>
<div id="controlPanel" style="float: left;border: 1px solid #ccc;margin-left: 3px;margin-top: 3px;width: 150px; background-color:white;position: absolute;">
				<table id="labelTree" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;width: 148px;" >
					<tr>
						<td style="padding: 0px;width: 30px;">Force</td>
						<td style="padding-left :5px;">
							<img src="${createLinkTo(dir:'images',file:'minus.gif')}" height="18" onclick="increaseValue('sub','forceId')"/>
							<input type="text" id="forceId" name="force" value="${force}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;vertical-align: top;">
							<img src="${createLinkTo(dir:'images',file:'plus.gif')}" height="18" onclick="increaseValue('add','forceId')"/>
						</td>
					</tr>
					<tr>
						<td style="padding: 0px;width: 30px;">Links</td>
						<td style="padding-left :5px">
							<img src="${createLinkTo(dir:'images',file:'minus.gif')}" height="18" onclick="increaseValue('sub','linksId')"/>
							<input type="text" id="linksId" name="links" value="${distance}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;" >
							<img src="${createLinkTo(dir:'images',file:'plus.gif')}" height="18" onclick="increaseValue('add','linksId')"/>
						</td>
					</tr>
					<tr>
						<td style="padding: 0px;width: 30px;">Friction</td>
						<td style="padding-left :5px">
							<img src="${createLinkTo(dir:'images',file:'minus.gif')}" height="18" onclick="increaseValue('sub','frictionId')"/>
							<input type="text" id="frictionId" name="friction" value="${friction}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;" >
							<img src="${createLinkTo(dir:'images',file:'plus.gif')}" height="18" onclick="increaseValue('add','frictionId')"/>
						</td>
					</tr>
					<tr>
						<td style="padding: 0px;width: 30px;">Height</td>
						<td style="padding-left :5px">
							<img src="${createLinkTo(dir:'images',file:'minus.gif')}" height="18" onclick="increaseValue('sub','heightId')"/>
							<input type="text" id="heightId" name="height" value="${height}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;" >
							<img src="${createLinkTo(dir:'images',file:'plus.gif')}" height="18" onclick="increaseValue('add','heightId')"/>
						</td>
					</tr>
					<tr>
						<td style="padding: 0px;width: 30px;">Width</td>
						<td style="padding-left :5px">
							<img src="${createLinkTo(dir:'images',file:'minus.gif')}" height="18" onclick="increaseValue('sub','widthId')"/>
							<input type="text" id="widthId" name="width" value="${width}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;" >
							<img src="${createLinkTo(dir:'images',file:'plus.gif')}" height="18" onclick="increaseValue('add','widthId')"/>
						</td>
					</tr>
					<tr>
						<td colspan="2" style="padding: 0px 0px 6px 0px ;text-align: left;"><h4>Show Labels</h4></td>
					</tr>
					<tr>
						<g:if test="${labels?.contains('apps')}">
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" name="labels" value="apps" checked="checked">
								<span style="vertical-align: text-top;">Apps</span>&nbsp;&nbsp;&nbsp;<img src="${createLinkTo(dir:'images',file:'iconApp.png')}" height="14" />
							</td>
						</g:if>
						<g:else>
						    <td colspan="2" style="padding: 0px;">
						    	<input type="checkbox" id="appLabel" name="labels" value="apps" >
						    	<span style="vertical-align: text-top;">Apps</span>&nbsp;&nbsp;&nbsp;<img src="${createLinkTo(dir:'images',file:'iconApp.png')}" height="14" />
						    </td>
						</g:else>
					</tr>
					<tr>
						<g:if test="${labels?.contains('servers')}">
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" name="labels" checked="checked" value="servers">
								<span style="vertical-align: text-top;">Servers</span>&nbsp;<img src="${createLinkTo(dir:'images',file:'iconServer.png')}" height="14" />
							</td>
						</g:if>
						<g:else>
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" id="serverLabel" name="labels" value="servers">
								<span style="vertical-align: text-top;">Servers</span>&nbsp;<img src="${createLinkTo(dir:'images',file:'iconServer.png')}" height="14" />
							</td>
						</g:else>
					</tr>
					<tr>
						<g:if test="${labels?.contains('files')}">
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" name="labels" checked="checked" value="files">
								<span style="vertical-align: text-top;">DB/Files</span>&nbsp;<img src="${createLinkTo(dir:'images',file:'iconDB.png')}" height="14" />
							</td>
						</g:if>
						<g:else>
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" id="filesLabel" name="labels" value="files">
								<span style="vertical-align: text-top;">DB/Files</span>&nbsp;<img src="${createLinkTo(dir:'images',file:'iconDB.png')}" height="14" />
							</td>
						</g:else>
					</tr>
					<tr>
						<td colspan="2" style="padding: 0px;text-align: center;">
						<input type="button" value="Refresh Map" onclick="refreshMap($('#forceId').val(), $('#linksId').val() ,$('#frictionId').val(), $('#heightId').val(), $('#widthId').val())">
						</td>
					</tr>
				</table>
			</div>
<g:render template="../d3/force/force" />