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
		<div style="float: left;">
			<table cellpadding="0" cellspacing="0" border="0" style="width: 123px; z-index: 100000">
				<tr>
					<td colspan="2" style="padding: 0px;"><h4>Control Panel</h4></td>
				</tr>
				<tr>
					<td style="padding: 0px;width: 30px;">Force</td>
					<td style="padding-left :5px;">
					<strong><a href="javascript:increaseValue('sub','forceId')"><</a></strong> 
					<input type="text" id="forceId" name="force" value="0" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;">
					<strong><a href="javascript:increaseValue('add','forceId')">></a></strong> 
				</tr>
				<tr>
					<td style="padding: 0px;width: 30px;">Links</td>
					<td style="padding-left :5px">
					<strong><a href="javascript:increaseValue('sub','linksId')"><</a></strong> 
					<input type="text" id="linksId" name="links" value="0" style="width: 30px;border: 0px;background-color: #FFF;text-align: center;" >
					<strong><a href="javascript:increaseValue('add','linksId')">></a></strong> 
					</td>
				</tr>
				<tr>
					<td colspan="2" style="padding: 0px;">&nbsp;</td>
				</tr>
				<tr>
					<td colspan="2" style="padding: 0px;"><h4>Show Labels</h4></td>
				</tr>
				<tr>
					<td colspan="2" style="padding: 0px;"><input type="checkbox">
						Apps</td>
				</tr>
				<tr>
					<td colspan="2" style="padding: 0px;"><input type="checkbox">
						Servers</td>
				</tr>
				<tr>
					<td colspan="2" style="padding: 0px;"><input type="checkbox">
						DB/Files</td>
				</tr>
				<tr>
					<td colspan="2" style="padding: 0px;"><input type="button"
						value="Refresh Map"></td>
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

