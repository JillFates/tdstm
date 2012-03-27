<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<meta name="layout" content="projectHeader" />
<title>Planning Console</title>
<g:javascript src="asset.tranman.js" />
<g:javascript src="entity.crud.js" />
<script type="text/javascript">
$(document).ready(function() {
	$("#createEntityView").dialog({ autoOpen: false })
	$("#showEntityView").dialog({ autoOpen: false })
	$("#editEntityView").dialog({ autoOpen: false })
    $("#commentsListDialog").dialog({ autoOpen: false })
    $("#createCommentDialog").dialog({ autoOpen: false })
    $("#showCommentDialog").dialog({ autoOpen: false })
    $("#editCommentDialog").dialog({ autoOpen: false })
    $("#manufacturerShowDialog").dialog({ autoOpen: false })
    $("#modelShowDialog").dialog({ autoOpen: false })
     
    
});


</script>

</head>
<body>
	<input type="hidden" id="redirectTo" name="redirectTo" value="planningConsole" />
	<div class="body">
		<div style="float: left;">
			<h1>Dependency Console</h1>
		</div>
		<div style="float: left; margin-left: 50px;">
			<h1>Dependency Analysis</h1>
			<div style="float: left;">
				<h3>Connection Status</h3>
				<g:each in="${dependencyType}" var="dependency">
					<input type="checkbox" id="dType_${dependency.id}"
						name="dType_${dependency.id}" />&nbsp;&nbsp;<span
						id="dependecy_${dependency.id}"> ${dependency.value} </span>
					<br></br>
				</g:each>
			</div>
			<div style="float: left;" class="buttonR">
				<input type="submit" class="submit"
					style="float: right; margin-top: 50px" value="Generate" />
			</div>

		</div>
		<div style="clear: both;"></div>
		<div style="margin-top: 40px;">
			<div>
				<div style="margin-left: 20px; margin-bottom: 10px;">
					<h3>
						<b>Dependency Bundling</b>
					</h3>
					&nbsp;03/13/2012: There were ${unassignedAppCount}
					Dependency-bundles discovered
				</div>
				<table border="0" cellpadding="4" cellspacing="0"
					style="margin-left: 20px; width: 500px;">
					<tr class="odd">
						<td><b>Dependency Bundles</b></td>
						<td><span id="serverIds"
							style="cursor: pointer; color: grey;" onclick="getList('server')"><b>1</b>
						</span></td>
						<td><span id="serverId" style="cursor: pointer; color: grey;"
							onclick="getList('server')"><b>2</b> </span></td>
						<td><span id="serverId" style="cursor: pointer; color: grey;"
							onclick="getList('server')"><b>3</b> </span></td>
						<td><span id="serverId" style="cursor: pointer; color: grey;"
							onclick="getList('server')"><b>4</b> </span></td>
						<td><span id="serverId" style="cursor: pointer; color: grey;"
							onclick="getList('server')"><b>5</b> </span></td>
						<td><span id="serverId" style="cursor: pointer; color: grey;"
							onclick="getList('server')"><b>6</b> </span></td>
						<td><span id="serverId" style="cursor: pointer; color: grey;"
							onclick="getList('server')"><b>7</b> </span></td>
						<td><span id="serverId" style="cursor: pointer; color: grey;"
							onclick="getList('server')"><b>8</b> </span></td>
						<td><span id="serverId" style="cursor: pointer; color: grey;"
							onclick="getList('server')"><b>9</b> </span></td>
						<td><span id="serverId" style="cursor: pointer; color: grey;"
							onclick="getList('server')"><b>10</b> </span></td>
					</tr>
					<tr class="even">
						<td><b>Applications</b></td>
						<td>10</td>
						<td>20</td>
						<td>3</td>
						<td>14</td>
						<td>5</td>
						<td>2</td>
						<td>6</td>
						<td>2</td>
						<td>3</td>
						<td>1</td>
					</tr>
					<tr class="odd">
						<td><b>Physical Servers</b></td>
						<td>41</td>
						<td>12</td>
						<td>3</td>
						<td>14</td>
						<td>15</td>
						<td>16</td>
						<td>7</td>
						<td>8</td>
						<td>1</td>
						<td>0</td>
					</tr>
					<tr class="even">
						<td><b>Virtual Servers</b></td>
						<td>11</td>
						<td>52</td>
						<td>13</td>
						<td>04</td>
						<td>15</td>
						<td>16</td>
						<td>07</td>
						<td>18</td>
						<td>0</td>
						<td>0</td>
					</tr>
				</table>
			</div>
		</div>
		<div style="clear: both;"></div>
		<div id="items1" style="display: none"></div>
		<g:render template="../assetEntity/commentCrud" />
		<g:render template="../assetEntity/modelDialog" />
		<div id="createEntityView" style="display: none;"></div>
		<div id="showEntityView" style="display: none;"></div>
		<div id="editEntityView" style="display: none;"></div>
	</div>

<script type="text/javascript">
	function getList(value){
		if(value=='Apps'){
			var app = 'Apps'
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + app', onComplete:'listUpdate(e)') }
		}else if(value=='server'){
			var server = 'server'
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + server', onComplete:'listUpdate(e)') }
		}
		else if(value=='database'){
			var server = 'database'
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + server', onComplete:'listUpdate(e)') }
		}else{
			var server = 'files'
			${remoteFunction(controller:'assetEntity', action:'getLists', params:'\'entity=\' + server', onComplete:'listUpdate(e)') }
		}
	}
	function listUpdate(e){
		   var resp = e.responseText;
		   $('#items1').html(resp)
		   $('#items1').css('display','block');
    }
</script>
</body>
</html>