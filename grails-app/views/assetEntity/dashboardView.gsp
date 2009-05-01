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
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'qvga.css')}" />

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
	   var browser=navigator.appName;
	   var 	assetId = assetId;
	   var	rows = new Array();
	   if(browser != 'Microsoft Internet Explorer') {
			rows = document.getElementsByName('assetDetailRow')	
		} else {
		    rows = document.getElementById('assetsTbody').childNodes
		}
	   for(i = 0 ; i<rows.length ; i++){
	   		rows[i].style.backgroundColor = '#FFFFFF'
	   }
	   document.getElementById('assetDetailRow_'+assetId).style.backgroundColor = '#65a342';
	   timedRefresh('never')
	   ${remoteFunction(action:'assetDetails', params:'\'assetId=\'+ assetId ' , onComplete:'getAssetDetail(e)') }
   
   }
   function getAssetDetail(e){
	   	var asset = eval("(" + e.responseText + ")")
	   	document.assetdetailsForm.asset.value = asset[0].assetDetails.assetDetail.id
	   	document.assetdetailsForm.currentState.value = asset[0].assetDetails.currentState
	    var tableBody = '<table style=\'border:0\' ><thead><tr><th>Asset Details </th></tr></thead><tbody>'+
		'<tr><td><b>Asset Name : </b>'+asset[0].assetDetails.assetDetail.assetName+'</td></tr>'+
		'<tr><td><b>Model : </b>'+asset[0].assetDetails.assetDetail.model+'</td></tr>'+
		'<tr><td><b>Rack: </b>'+asset[0].assetDetails.assetDetail.sourceRack+'</td></tr>'+
		'<tr><td><b>Status : </b>'+asset[0].assetDetails.currentState+'</td></tr>'+
		'<tr><td><b>Assigned : </b>'+asset[0].assetDetails.teamName+'</td></tr>'+
		'<tr><td><b>Recent Changes: </b></td></tr>'
		for(i=0;i<asset[0].recentChanges.length; i++){
			tableBody += '<tr><td>'+asset[0].recentChanges[i]+'</td></tr>'
		}
		tableBody += '</tbody></table>'
	    var selectObj = document.getElementById('asset')
	   	selectObj.innerHTML = tableBody
	   	createOptions(asset[0].statesList)
	   	var teamObj = document.getElementById("assignToId")
	   	var sourceObj = document.getElementById("sourceAssignTo")
	   	var targetObj = document.getElementById("targetAssignTo")
   		var l = teamObj.length
	   	while (l > 1) {
			l--
		    teamObj.remove(l)
		}
		var sourceLength = asset[0].sourceTeams.length
	    for (var i=0; i < sourceLength; i++) {
	      var team = asset[0].sourceTeams[i]
	      var popt = document.createElement('option');
		  popt.innerHTML = team.name
	      popt.value = "s/"+team.id
	      try {
	      sourceObj.appendChild(popt, null) // standards compliant; doesn't work in IE
	      } catch(ex) {
	      sourceObj.appendChild(popt) // IE only
	      }
		}
		var targetLength = asset[0].targetTeams.length
	    for (var i=0; i < targetLength; i++) {
	      var team = asset[0].targetTeams[i]
	      var popt = document.createElement('option');
		  popt.innerHTML = team.name
	      popt.value = "t/"+team.id
	      try {
	      targetObj.appendChild(popt, null) // standards compliant; doesn't work in IE
	      } catch(ex) {
	      targetObj.appendChild(popt) // IE only
	      }
		}
   	}
   
   	function bundleChange(){  
	   var bundleID = ${moveBundleInstance.id}; 
	   document.getElementById("moveBundleId").value =  bundleID; 
   	}
   	function setComment(e){
	   	var commentStatus = eval("(" + e.responseText + ")")
	   	if(commentStatus[0]){
	   		document.assetdetailsForm.validateComment.value = commentStatus[0].status
	   	}else {
	   		document.assetdetailsForm.validateComment.value = ""
	   	}
   	}
   	function setCommentValidation(){
	   	if(document.assetdetailsForm.validateComment.value == 'true'){
	   		if(document.assetdetailsForm.comment.value == ''){
	   			alert("A comment is required")
	   		}
	   	}
   	}
   	function timedRefresh(timeoutPeriod) {
   		if(timeoutPeriod != 'never'){
		setTimeout("location.reload(false);",timeoutPeriod);
		document.getElementById("timeToRefresh").value = timeoutPeriod;
		}
		document.getElementById("selectTimedId").value = timeoutPeriod; 
	}
	function updateAsset(e){
		var asset = eval("(" + e.responseText + ")")
		if(asset[0]){
		createOptions(asset[0].statesList)
		document.getElementById('priorityCol_'+asset[0].assetEntity.id).innerHTML = asset[0].assetEntity.priority 
		document.getElementById('statusCol_'+asset[0].assetEntity.id).innerHTML = asset[0].status
		}
		timedRefresh(document.getElementById('timeToRefresh').value)
	}
	function createOptions(statesList){
		var statusObj = document.getElementById("stateSelectId")
   		var l = statusObj.length
	   	while (l > 1) {
			l--
		    statusObj.remove(l)
		}
		var length = statesList.length
	    for (var i=0; i < length; i++) {
	      var state = statesList[i]
	      var popt = document.createElement('option');
		  popt.innerHTML = state.label
	      popt.value = state.id
	      try {
	      statusObj.appendChild(popt, null) // standards compliant; doesn't work in IE
	      } catch(ex) {
	      statusObj.appendChild(popt) // IE only
	      }
		}
	}
    </script>
</head>

<body >
<div class="body" >

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
		Bundle:</label>&nbsp;<select id="moveBundleId"
			name="moveBundle" onchange="document.dashboardForm.submit()" >	

			<g:each status="i" in="${moveBundleInstanceList}"
				var="moveBundleInstance">
				<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
			</g:each>

		</select></td>
	<td style="text-align: right;"><input type="button" value="Refresh" onclick="location.reload(true);">
	<select id="selectTimedId" onchange="timedRefresh(this.value)">
	<option value="60000">1 min</option> 
	<option value="120000">2 min</option> 
	<option value="180000">3 min</option> 
	<option value="240000">4 min</option> 
	<option value="300000">5 min</option> 
	<option value="never">Never</option> 
	</select> 
	<input type="hidden" id="timeToRefresh">
	</td>
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
    <td valign="top" style="border-right: 1px solid #333333;padding: 0px;width: 100px;">
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
    <div style="width:750px; float:left; overflow:auto;">
	<table width="100%" style="border: 0;" cellspacing="0" cellpadding="0">
	  <tr>
	    <g:each in="${bundleTeams}" var="bundleTeam">
			<td style="padding: 0px;border-right: 1px solid #333333">
			<table style="border: 0;" >
			<th nowrap>${bundleTeam?.team?.name }</th>
			<tr><td nowrap>${bundleTeam?.members}</td></tr>
			<tr><td class="odd">${bundleTeam?.team?.currentLocation}&nbsp;</td> </tr>
			<tr><td nowrap>Sap1</td> </tr>
			<tr><td class="odd">${bundleTeam?.unrackedAssets} of ${bundleTeam?.sourceAssets}</td> </tr>
			<tr><td nowrap>${bundleTeam?.rerackedAssets} of ${bundleTeam?.targetAssets}</td> </tr>
			<tr><td nowrap class="odd">2/22m</td> </tr>
			</table>
			</td>
			</g:each>
			<td style="padding: 0px;border-right: 1px solid #333333">
			<table style="border: 0;" >
			<th nowrap>${supportTeam?.cleaning.name}</th>
			<tr><td nowrap>${supportTeam?.cleaningMembers}</td></tr>
			<tr><td class="odd">${supportTeam?.cleaning.currentLocation}&nbsp;</td> </tr>
			<tr><td nowrap>Exchg3</td> </tr>
			<tr><td class="odd">${supportTeam.sourceCleaned} of ${supportTeam.totalAssets}</td> </tr>
			<tr><td nowrap>N/A</td> </tr>
			<tr><td nowrap class="odd">1/10m</td> </tr>
			</table>
			</td>
			<td style="padding: 0px;">
			<table style="border: 0;" >
			<th nowrap>${supportTeam?.transport.name}</th>
			<tr><td nowrap>${supportTeam?.transportMembers}</td></tr>
			<tr><td class="odd">${supportTeam?.transport.currentLocation}&nbsp;</td> </tr>
			<tr><td nowrap>Exchg3</td> </tr>
			<tr><td class="odd">${supportTeam.sourceMover} of ${supportTeam.totalAssets}</td> </tr>
			<tr><td nowrap>${supportTeam.targetMover} of ${supportTeam.totalAssets}</td> </tr>
			<tr><td nowrap class="odd">1/10m</td> </tr>
			</table>
			</td>
	  </tr>
	</table>
    </div>
    </td> 
    <td valign="top" style="border-right: 1px solid #333333;padding: 0px;width: 100px;">
     <div style=" float:left;">  
     <table style="width:100px; border: 0">
			<th nowrap>TOTALS:</th>
			<tr><td>&nbsp;</td> </tr>
			<tr><td class="odd">&nbsp;</td> </tr>
			<tr><td>&nbsp;</td> </tr>
			<tr><td class="odd">${totalUnracked} of ${totalAsset}</td> </tr>
			<tr><td nowrap>${totalReracked} of ${totalAsset}</td> </tr>
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
		<div class="list" >
		<g:form name="assetListForm">		
		
		<table>
			<thead>
				<tr>
					<!-- <g:sortableColumn property="assetName" title="Asset Name" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="status" title="Status" />
					<g:sortableColumn property="team" title="Team" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="statTimer" title="Stat Timer" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="loc" title="Loc" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="issues" title="Issues" /> -->
					<th>Priority</th>
					<th>Asset Tag</th>
					<th>Asset Name</th>
					<th>Status</th>
					<th>Team</th>
					<th>Start</th>
					<th>Loc</th>
					<th>Issues</th>
				</tr>
			</thead>
			<tbody id="assetsTbody">
				<g:each status="i" in="${assetsList}" var="assetsList">
					<tr onclick="assetDetails('${assetsList?.asset.id}')" name="assetDetailRow" id="assetDetailRow_${assetsList?.asset.id}" class="asset_${assetsList?.status}">
						<td id="priorityCol_${assetsList?.asset.id}">${assetsList?.asset.priority}</td>
						<td>${assetsList?.asset.assetTag}</td>
						<td>${assetsList?.asset.assetName}</td>
						<td id="statusCol_${assetsList?.asset.id}">${assetsList?.status}</td>
						<td>${assetsList?.asset.sourceTeam.name}</td>
						<td><tds:convertDateTime date="${assetsList.asset.moveBundle.startTime}" /></td>
						<td>${assetsList.asset.sourceTeam.currentLocation}</td>
						<td>not required</td>
					</tr>
					
				</g:each>
			</tbody>
		</table>
		
		</g:form>
		</div>
		</td>
		<td valign="top" style="padding: 0px;">
		<div id="assetDetails" style="border: 1px solid #5F9FCF;width: 200px;">
		<div id="asset" >
		<table style='border:0' ><thead><tr><th>Asset Details </th></tr></thead><tbody>
		<tr><td><b>Asset Name : </b></td></tr>
		<tr><td><b>Model :</b></td></tr>
		<tr><td><b>Rack: </b></td></tr>
		<tr><td><b>Status : </b></td></tr>
		<tr><td><b>Assigned : </b></td></tr>
		<tr><td><b>Recent Changes: </b></td></tr>
		</tbody></table>
		</div>
		<div>
		<g:form name="assetdetailsForm" >
		<table style="border: 0">
			<tbody>
			<tr><td> <b>Change :</b></td>
			<td> <select id="stateSelectId" name="state" style="width: 100px" onchange="${remoteFunction(action:'getFlag', params:'\'toState=\'+ this.value +\'&fromState=\'+document.getElementById(\'currentStateId\').value', onComplete:'setComment(e)')}">
			<option value="">Status</option>
			</select> </td>
			</tr>
			<tr><td><input type="hidden" name="asset" id="assetId" value=""> &nbsp;</td>
			<td>
			<!-- <select id="priorityId" name="priority" style="width: 100px">
			<option value="1">High</option>
			<option value="2">Normal</option>
			<option value="3">Low</option>
			</select> -->
			<g:select id="priorityId" name="priority" from="${AssetEntity.constraints.priority.inList}" style="width: 100px" noSelection="['':'Priority ']"></g:select>
			 </td>
			</tr>
			<tr><td><input type="hidden" name="currentState" id="currentStateId" value="">&nbsp;</td>
			<td> <select id="assignToId" name="assignTo" style="width: 100px">
			<option value="">Move Team</option>
			<optgroup label="Source" id="sourceAssignTo"></optgroup>
			<optgroup label="Target" id="targetAssignTo"></optgroup>
			</select> </td>
			</tr>
			<tr>
			<td colspan="2" style="text-align: center;" >
			<input type="hidden" value="" id="validateCommentId" name="validateComment">
			<textarea name="comment" name="comment" cols="25" rows="2" ></textarea> </td>
			</tr>
			<tr>
			<td colspan="2" style="text-align: center;" class="buttonR">
			 <input type="button" value="Cancle" onclick="timedRefresh(document.getElementById('timeToRefresh').value)">
			<g:submitToRemote  action="createTransition" value="Submit" before="setCommentValidation();" onComplete="updateAsset(e)"/></td>
			</tr>
			</tbody>
		</table>
		</g:form>
		</div>
		</div>
		</td>
	</tr>
</table>
</td></tr></table>
</div>
<script type="text/javascript">
bundleChange();
timedRefresh(document.getElementById("selectTimedId").value)
</script>
</div>
</body>
</html>