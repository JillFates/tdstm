
<div class="tabs">
	<ul>
		<li id="serverli"><a
			href="javascript:getList('server',${dependencyBundle})"><span>Servers(${assetEntityListSize})
			</span> </a></li>
		<li id="appli"><a
			href="javascript:getList('Apps',${dependencyBundle})">Apps(${appDependentListSize})
		</a></li>
		<li id="dbli"><a href="#item3"><a
				href="javascript:getList('database',${dependencyBundle})">DB(${dbDependentListSize})
			</a></li>
		<li id="fileli"><a
			href="javascript:getList('files',${dependencyBundle})">Files(${filesDependentListSize})
		</a></li>
		<li id="graphli" class="active"><a
			href="javascript:getList('graph',${dependencyBundle})">Map</a></li>
	</ul>
	<div class="tabInner">
	<input type="hidden" id="assetTypesId" name="assetType" value="${asset}" />
		<div id="item1" style="float: left;z-index: 10000px;">
			<div style="float: left;border: 1px solid #ccc;margin-left: 3px;margin-top: 3px;width: 331px">
				<table id="labelTree" cellpadding="0" cellspacing="0" style="margin-left: 5px;border: 0;width: 331px;" >
					<tr>
						<td colspan="2" style="padding: 0px;"><h4>Control Panel</h4></td>
						<td colspan="4" style="padding: 0px 0px 6px 0px ;text-align: left;"><h4>Show Labels</h4></td>
					</tr>
					<tr>
						<td style="padding: 0px;width: 30px;">Force</td>
						<td style="padding-left :5px;">
							<img src="${createLinkTo(dir:'images',file:'minus.gif')}" height="18" onclick="increaseValue('sub','forceId')"/>
							<input type="text" id="forceId" name="force" value="${force}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;vertical-align: top;">
							<img src="${createLinkTo(dir:'images',file:'plus.gif')}" height="18" onclick="increaseValue('add','forceId')"/>
						</td>
						<g:if test="${labels.contains('apps')}">
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" name="labels" value="apps" checked="checked">
								<span style="vertical-align: text-top;">Apps</span>&nbsp;
							</td>
						</g:if>
						<g:else>
						    <td colspan="2" style="padding: 0px;">
						    	<input type="checkbox" name="labels" value="apps" >
						    	<span style="vertical-align: text-top;">Apps</span>&nbsp;
						    </td>
						</g:else>
						<g:if test="${labels.contains('servers')}">
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" name="labels" checked="checked" value="servers">
								<span style="vertical-align: text-top;">Servers</span>&nbsp;
							</td>
						</g:if>
						<g:else>
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" name="labels" value="servers">
								<span style="vertical-align: text-top;">Servers</span>&nbsp;
							</td>
						</g:else>
						<g:if test="${labels.contains('files')}">
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" name="labels" checked="checked" value="files">
								<span style="vertical-align: text-top;">DB/Files</span>&nbsp;
							</td>
						</g:if>
						<g:else>
							<td colspan="2" style="padding: 0px;">
								<input type="checkbox" name="labels" value="files">
								<span style="vertical-align: text-top;">DB/Files</span>&nbsp;
							</td>
						</g:else>
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
						<td colspan="2" style="padding: 0px;text-align: center;">
						<input type="button" value="Refresh Map" onclick="getList('graph',${dependencyBundle},$('#forceId').val(), $('#linksId').val(),'labels' )"></td>
					</tr>
				</table>
			</div>
			<g:render template="../d3/force/force" />
		</div>
	</div>
</div>
<script type="text/javascript">
function increaseValue(action, id ){
	var value = parseInt($("#"+id).val())
	if(action=='add'){
		value += 10
	} else {
		value -= 10
	}
	$("#"+id).val( value )
}
</script>

