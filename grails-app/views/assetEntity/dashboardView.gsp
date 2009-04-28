<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Supervisor Dashboard</title>
<g:javascript library="jquery" />
<g:javascript library="prototype" />

<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.core.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.dialog.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.theme.css')}" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.datetimepicker.css')}" />

<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />
<jq:plugin name="ui.datetimepicker" />
<style>
	td .odd{ background:#DDDDDD;nowrap }
	</style>
	
<script type="text/javascript">
   function assetDetails(assetId) {   
   var assetId = assetId; 
   ${remoteFunction(action:'assetDetails', params:'\'assetId=\'+ assetId ' , onComplete:'getAssetDetail(e)') }
   
   }
   function getAssetDetail(e){
   var asset = eval("(" + e.responseText + ")")	
    var tableBody = '<table ><thead><tr><th>Asset Details </th></tr></thead><tbody>'+
'<tr><td>'+asset[0].assetDetail.assetName+'</td></tr>'+
'<tr><td>'+asset[0].assetDetail.model+'</td></tr>'+
'<tr><td>'+asset[0].assetDetail.sourceRack+'</td></tr>'+
'<tr><td>'+asset[0].teamName+'</td></tr>'+
'</tbody></table>'
    	var selectObj = document.getElementById('asset')
   	selectObj.innerHTML = tableBody
   }
   
   function bundleChange(){  
   var bundleID = ${moveBundleInstance.id}; 
   document.getElementById("moveBundleId").value =  bundleID; 
 
   }
   
    </script>


</head>

<body>
<div class="body">

<h1>Supervisor Dashboard</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if> <g:form method="get" name="dashboardForm" controller="assetEntity"
	action="dashboardView">
	<input type="hidden" name="projectId" value="${projectId}">	

	<div class="dialog">
<table style="border: 0px;">
	<tr class="prop">
		<td valign="top" class="name"><label for="moveBundle">Move
		Bundle:</label></td>
		<td valign="top" class="value"><select id="moveBundleId"
			name="moveBundle" onchange="document.dashboardForm.submit()" >	

			<g:each status="i" in="${moveBundleInstanceList}"
				var="moveBundleInstance">
				<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
			</g:each>

		</select></td>
	</tr>
	</table>
	</div>
	
</g:form>
<div style="width: 100%; float: left; border: 1px solid #cccccc;">
 <table style="border: 0px">
<tr><td>
<div style="width:100%; float:left; border-left:1px solid #333333;">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td valign="top" style="border-right: 1px solid #333333;padding: 0px;">
     <div style="width:100px; float:left;">  
     <table style="border: 0">
			<th>TEAMS:</th>
			<tr><td>Names</td> </tr>
			<tr><td class="odd">Location</td> </tr>
			<tr><td>Asset</td> </tr>
			<tr><td class="odd">Source</td> </tr>
			<tr><td >Target</td> </tr>
			<tr><td class="odd">Queue</td> </tr>
			</table>
    </div>
    </td>
    <td valign="top" style="border-right: 1px solid #333333;padding: 0px;">
    <div style="width:720px; float:left; overflow:auto;">
	<table width="100%" style="border: 0;" cellspacing="0" cellpadding="0">
	  <tr>
	    <g:each in="${bundleTeams}" var="bundleTeam">
			<td style="padding: 0px;border-right: 1px solid #333333">
			<table style="border: 0;" >
			<th nowrap>${bundleTeam?.team?.name }</th>
			<tr><td nowrap>${bundleTeam?.members}</td></tr>
			<tr><td class="odd">${bundleTeam?.team?.currentLocation }&nbsp;</td> </tr>
			<tr><td nowrap>Sap1</td> </tr>
			<tr><td class="odd">17 of 50</td> </tr>
			<tr><td nowrap>17 of 50</td> </tr>
			<tr><td nowrap class="odd">2/22m</td> </tr>
			</table>
			</td>
			</g:each>
			<td style="padding: 0px;border-right: 1px solid #333333">
			<table style="border: 0;" >
			<th nowrap>Cleaner</th>
			<tr><td nowrap>Gunderson</td></tr>
			<tr><td class="odd">Dallas1</td> </tr>
			<tr><td nowrap>Exchg3</td> </tr>
			<tr><td class="odd">${completed.sourceCleaned} of ${completed.totalAssets}</td> </tr>
			<tr><td nowrap>${completed.targetCleaned} of ${completed.totalAssets}</td> </tr>
			<tr><td nowrap class="odd">1/10m</td> </tr>
			</table>
			</td>
			<td style="padding: 0px;">
			<table style="border: 0;" >
			<th nowrap>Mover</th>
			<tr><td nowrap>Gunderson</td></tr>
			<tr><td class="odd">Dallas1</td> </tr>
			<tr><td nowrap>Exchg3</td> </tr>
			<tr><td class="odd">${completed.sourceMover} of ${completed.totalAssets}</td> </tr>
			<tr><td nowrap>${completed.targetMover} of ${completed.totalAssets}</td> </tr>
			<tr><td nowrap class="odd">1/10m</td> </tr>
			</table>
			</td>
	  </tr>
	</table>
    </div>
    </td> 
    <td valign="top" style="border-right: 1px solid #333333;padding: 0px;">
     <div style=" float:left;">  
     <table style="width:100px; border: 0">
			<th nowrap>TOTALS:</th>
			<tr><td>&nbsp;</td> </tr>
			<tr><td class="odd">&nbsp;</td> </tr>
			<tr><td>&nbsp;</td> </tr>
			<tr><td class="odd">51 of 154</td> </tr>
			<tr><td nowrap>17 of 50</td> </tr>
			<tr><td class="odd">11/92m</td> </tr>
			</table>
    </div>
    </td>
  </tr>
 
</table>
</div>
</td></tr>

<tr><td>

<table style="border: 0px;">
	<tr>
		<td valign="top" style="padding: 0px;">
		<div class="list">
		<g:form name="assetListForm">		
		
		<table>
			<thead>
				<tr>
					<g:sortableColumn property="assetName" title="Asset Name" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="status" title="Status" />
					<g:sortableColumn property="team" title="Team" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="statTimer" title="Stat Timer" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="loc" title="Loc" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="issues" title="Issues" />
				</tr>
			</thead>
			<tbody id="assetsTbody">

				<g:each status="i" in="${totalAsset}" var="totalAsset">
					<tr onclick="assetDetails('${totalAsset.id}')">
						<td>${totalAsset.assetName}</td>
						<td>HOLD!</td>
						<td>${totalAsset.sourceTeam.name}</td>
						<td>${totalAsset.moveBundle.startTime}</td>
						<td>${totalAsset.sourceTeam.currentLocation}</td>
						<td>not required</td>
					</tr>
				</g:each>

			</tbody>
		</table>
		
		</g:form>
		</div>
		
		</td>
		
		<td valign="top" style="padding: 0px;">
		<div id="asset">
		<table>
			<thead>
				<tr>
					<th>Asset Details</th>
				</tr>
			</thead>
			<tbody>				
			</tbody>
		</table>
		</div>
		</td>
	</tr>
</table>
</td></tr></table>
</div>
<script type="text/javascript">
bundleChange()
</script>

</div>
</body>
</html>