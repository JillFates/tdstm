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
		<div style="float: left;border: 1px solid #ccc;margin-left: 3px;margin-top: 3px;">
			<table id="labelTree" cellpadding="0" cellspacing="0" style="width: 125px;margin-left: 5px;border: 0" >
				<tr>
					<td colspan="2" style="padding: 0px;"><h4>Control Panel</h4></td>
				</tr>
				<tr>
					<td style="padding: 0px;width: 30px;">Force</td>
					<td style="padding-left :5px;">
					<img src="${createLinkTo(dir:'images',file:'minus.gif')}" height="18" onclick="increaseValue('sub','forceId')"/>
					<input type="text" id="forceId" name="force" value="${force}" disabled="disabled" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;vertical-align: top;">
					<img src="${createLinkTo(dir:'images',file:'plus.gif')}" height="18" onclick="increaseValue('add','forceId')"/> 
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
					<td colspan="2" style="padding: 0px;">&nbsp;</td>
				</tr>
				<tr>
					<td colspan="2" style="padding: 0px 0px 6px 0px ;text-align: left;"><h4>Show Labels</h4></td>
				</tr>
				<tr>
					<td colspan="2" style="padding: 0px;"><input type="checkbox" name="labels" value="apps">&nbsp;Apps</td>
				</tr>
				<tr>
					<td colspan="2" style="padding: 0px;"><input type="checkbox" name="labels" value="servers">&nbsp;Servers</td>
				</tr>
				<tr>
					<td colspan="2" style="padding: 0px;"><input type="checkbox" name="labels" value="files">&nbsp;DB/Files</td>
				</tr>
				<tr>
					<td colspan="2" style="padding: 0px;text-align: center;">
					<input type="button" value="Refresh Map" onclick="getList('graph',${dependencyBundle},$('#forceId').val(), $('#linksId').val(),'labels' )"></td>
				</tr>
			</table>
		</div>
		<div id="item1" style="float: left;">
			<iframe src="../d3/force/force.html" width="1000" height="600">
			</iframe>
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

