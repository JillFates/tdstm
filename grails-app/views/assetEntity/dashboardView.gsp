<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Supervisor Console</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
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
<g:javascript src="assetcommnet.js" />
<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />
<jq:plugin name="ui.datetimepicker" />

<style>
td .odd {
	background: #DDDDDD;
	nowrap
}
</style>

<script>
	$(document).ready(function() {
		$("#commentsListDialog").dialog({ autoOpen: false })
	    $("#createCommentDialog").dialog({ autoOpen: false })
	    $("#showCommentDialog").dialog({ autoOpen: false })
	    $("#editCommentDialog").dialog({ autoOpen: false })
	    $("#showChangeStatusDialog").dialog({ autoOpen: false })	        
	})
</script>
<script type="text/javascript">

function showChangeStatusDialog(e){
timedRefresh('never')
var task = eval('(' + e.responseText + ')');
var taskLen = task[0].item.length;
var options = '';
if(taskLen == 0){
alert('Sorry but there were no common states for the assets selected');
return false;
}else{

      for (var i = 0; i < taskLen; i++) {
        options += '<option value="' + task[0].item[i].state + '">' + task[0].item[i].label + '</option>';
      }
      $("select#taskList").html(options);
      if(taskLen > 1 && task[0].item[0].state == "Hold"){
      document.getElementById('taskList').options[1].selected = true;
      }
       var getDialogId = document.getElementById('assetVal')     
      getDialogId.value=task[0].asset;
 
 
$("#showChangeStatusDialog").dialog('option', 'width', 400)
$("#showChangeStatusDialog").dialog('option', 'position', ['center','top']);
$('#showChangeStatusDialog').dialog('open');
}
}

function submitAction(){
if(doCheck()){
document.changeStatusForm.action = "changeStatus";
document.changeStatusForm.submit();
timedRefresh(document.getElementById("selectTimedId").value)
}else{
return false;
}
}
function doCheck(){
var taskVal = document.getElementById('taskList').value;
var noteVal = document.getElementById('enterNote').value;
if((taskVal == "Hold")&&(noteVal == "")){
alert('Please Enter Note');
return false;
}else{
return true;
}
}
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
	   	var cls = rows[i].getAttribute("class")
	   	var holdId = rows[i].getAttribute("value")
	   	var showImg = document.getElementById("image_"+holdId)
	   	 showImg.style.visibility = 'hidden';
	   }
	   var rowColor =  document.getElementById('image_'+assetId)
	   rowColor.style.visibility = 'visible';
	   timedRefresh('never')
	   ${remoteFunction(action:'assetDetails', params:'\'assetId=\'+ assetId ' , onComplete:'getAssetDetail(e)') }
   
   }
   function getAssetDetail(e){
	   	var asset = eval("(" + e.responseText + ")")
	    var tableBody = '<table style=\'border:0\' cellpadding=\'0\' cellspacing=\'0\' ><thead><tr><th>Asset Details </th></tr></thead><tbody>'+
		'<tr><td><b>Name: </b>'+asset[0].assetDetails.assetDetail.assetName+'</td></tr>'+
		'<tr><td><b>Model: </b>'+asset[0].assetDetails.assetDetail.model+'</td></tr>'+
		'<tr><td><b>Rack: </b>'+asset[0].assetDetails.assetDetail.sourceRack+'</td></tr>'+
		'<tr><td><b>Status: </b>'+asset[0].assetDetails.currentState+'</td></tr>'+
		'<tr><td><b>Issue: </b></td></tr>'+
		'<tr><td><b>Time: </b></td></tr>'+
		'<tr><td><b>Assigned: </b>'+asset[0].assetDetails.teamName+'</td></tr>'+
		'<tr><td><b>Recent Changes: </b></td></tr>'
		for(i=0;i<asset[0].recentChanges.length; i++){
			tableBody += '<tr><td>'+asset[0].recentChanges[i]+'</td></tr>'
		}
		tableBody += '</tbody></table>'
	    var selectObj = document.getElementById('asset')
	   	selectObj.innerHTML = tableBody
	   	createStateOptions(asset[0].statesList)
	   	createAssighToOptions(asset[0].sourceTeams,asset[0].targetTeams)
	   	document.assetdetailsForm.reset();
	   	document.assetdetailsForm.asset.value = asset[0].assetDetails.assetDetail.id
	   	document.assetdetailsForm.currentState.value = asset[0].assetDetails.state
   	}
   	function createStateOptions(statesList){
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
   	function createAssighToOptions(sourceTeams,targetTeams){
   		var teamObj = document.getElementById("assignToId")
	   	var sourceObj = document.getElementById("sourceAssignTo")
	   	var targetObj = document.getElementById("targetAssignTo")
   		var l = teamObj.length
	   	while (l > 1) {
			l--
		    teamObj.remove(l)
		}
		var sourceLength = sourceTeams.length
	    for (var i=0; i < sourceLength; i++) {
	      var team = sourceTeams[i]
	      var popt = document.createElement('option');
		  popt.innerHTML = team.name
	      popt.value = "s/"+team.id
	      try {
	      sourceObj.appendChild(popt, null) // standards compliant; doesn't work in IE
	      } catch(ex) {
	      sourceObj.appendChild(popt) // IE only
	      }
		}
		var targetLength = targetTeams.length
	    for (var i=0; i < targetLength; i++) {
	      var team = targetTeams[i]
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
	   var time = '${timeToRefresh}';
	   if(time != '' ){
	   document.getElementById("selectTimedId").value = time;
	   } 
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
	   	if(document.assetdetailsForm.validateComment.value == 'true' || document.assetdetailsForm.state.value == 'Hold'){
	   		if(document.assetdetailsForm.comment.value == ''){
	   			alert("A comment is required")
	   		}
	   	}
   	}
   	var timer
   	function timedRefresh(timeoutPeriod) {
   		if(timeoutPeriod != 'never'){
		timer = setTimeout("location.reload(true);",timeoutPeriod);
		document.getElementById("selectTimedId").value = timeoutPeriod;
		} else {
		clearTimeout(timer)
		}
	}
	function setRefreshTime(e) {
		var timeRefresh = eval("(" + e.responseText + ")")
		if(timeRefresh){
			timedRefresh(timeRefresh[0].refreshTime.SUPER_CONSOLE_REFRESH)
		}
	}
	function updateAsset(e){
		var asset = eval("(" + e.responseText + ")")		
		if(asset[0]){
		createStateOptions(asset[0].statesList)
		createAssighToOptions(asset[0].sourceTeams,asset[0].targetTeams)
		if(asset[0].checkVal == false){
		var spanEle = document.getElementById('spanId_'+asset[0].assetEntity.id);
		spanEle.innerHTML = "&nbsp;&nbsp;&nbsp;";
		}		
		document.getElementById('priorityCol_'+asset[0].assetEntity.id).innerHTML = asset[0].assetEntity.priority 
		document.getElementById('statusCol_'+asset[0].assetEntity.id).innerHTML = asset[0].status
		document.getElementById('source_'+asset[0].assetEntity.id).innerHTML = asset[0].sourceTeam
		document.getElementById('target_'+asset[0].assetEntity.id).innerHTML = asset[0].targetTeam
		document.getElementById('assetDetailRow_'+asset[0].assetEntity.id).className = asset[0].cssClass ;
		if(asset[0].status == "Hold"){
		var link = document.createElement('a');
		link.href = '#'
		link.onclick = function(){document.getElementById('createAssetCommentId').value = asset[0].assetEntity.id ;new Ajax.Request('listComments?id='+asset[0].assetEntity.id,{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog(e);}})} //;return false
		link.innerHTML = "<img src=\"/tds/images/skin/database_table_red.png\" border=\"0px\">"
		document.getElementById('icon_'+asset[0].assetEntity.id).appendChild(link);
		}
		document.assetdetailsForm.priority.value = "";
		document.assetdetailsForm.comment.value = "";
		}
		timedRefresh(document.getElementById('selectTimedId').value);
	}
	
var isFirst = true;
function selectAll(){
timedRefresh('never')
var totalCheck = document.getElementsByName('checkChange');
if(isFirst){
for(i=0;i<totalCheck.length;i++){
totalCheck[i].checked = true;
}
isFirst = false;
}else{
for(i=0;i<totalCheck.length;i++){
totalCheck[i].checked = false;
}
isFirst = true;
}
}
function changeState(){
timedRefresh('never')
var assetArr = new Array();
var totalAsset = ${assetsList?.asset.id};
var j=0;
for(i=0; i< totalAsset.size() ; i++){
if(document.getElementById('checkId_'+totalAsset[i]) != null){
var booCheck = document.getElementById('checkId_'+totalAsset[i]).checked;
if(booCheck == true){
assetArr[j] = totalAsset[i];
j++;
}
}
}
if(j == 0){
alert('Please select the Asset');
}else{
${remoteFunction(action:'getList', params:'\'assetArray=\' + assetArr ', onComplete:'showChangeStatusDialog(e);' )}
}
}	
    </script>
</head>

<body>

<div title="Change Status" id="showChangeStatusDialog"
	style="background-color: #808080; display: none;">
<form name="changeStatusForm"><input type="hidden"
	name="assetVal" id="assetVal" /> <input type="hidden" name="projectId"
	id="projectId" value="${projectId}" /> <input type="hidden"
	name="moveBundle" id="moveBundle" value="${moveBundleInstance.id}" />
<table style="border: 0px; width: 100%">
	<tr>
		<td width="40%"><strong>Change status for selected
		devices to:</strong></td>
		<td width="60%"></td>
	</tr>
	<tr>
		<td><select id="taskList" name="taskList" style="width: 250%">

		</select></td>
	</tr>
	<tr>
		<td><textarea rows="2" cols="1" title="Enter Note..."
			name="enterNote" id="enterNote" style="width: 200%"></textarea></td>
	</tr>
	<tr>
		<td></td>
		<td style="text-align: right;"><input type="button" value="Save"
			onclick="confirm('Are you sure???');submitAction()" /></td>
	</tr>
</table>
</form>
</div>
<div class="body"><g:form method="get" name="dashboardForm"
	controller="assetEntity" action="dashboardView">
	<input type="hidden" name="projectId" value="${projectId}">

	<div class="dialog">
	<table style="border: 0px;">
		<tr class="prop">
			<td style="vertical-align: bottom;" class="name"><label
				for="moveBundle">Move Bundle:</label>&nbsp;<select id="moveBundleId"
				name="moveBundle" onchange="document.dashboardForm.submit()">

				<g:each status="i" in="${moveBundleInstanceList}"
					var="moveBundleInstance">
					<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
				</g:each>

			</select></td>
			<td>
			<h1 align="right">Supervisor Console</h1>
			</td>
			<td style="text-align: right; vertical-align: bottom;"><input
				type="button" value="Refresh" onclick="location.reload(true);">
			<select id="selectTimedId"
				onchange="${remoteFunction(action:'setTimePreference', params:'\'timer=\'+ this.value ' , onComplete:'setRefreshTime(e)') }">
				<option value="60000">1 min</option>
				<option value="120000">2 min</option>
				<option value="180000">3 min</option>
				<option value="240000">4 min</option>
				<option value="300000">5 min</option>
				<option value="never">Never</option>
			</select></td>
		</tr>
	</table>
	</div>

</g:form>
<div style="width: 100%; float: left; border: 1px solid #cccccc;">
<table style="border: 0px">
	<tr>
		<td>
		<div style="width: 100%; float: left; border-left: 1px solid #333333;">
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td valign="top"
					style="border-right: 1px solid #333333; padding: 0px; width: 15%;">
				<div style="width: 100%;; float: left;">
				<table style="border: 0; width: 100%;">
					<th>TEAMS:</th>
					<tr>
						<td>Names</td>
					</tr>
					<tr>
						<td class="odd">Location</td>
					</tr>
					<tr>
						<td>Asset</td>
					</tr>
					<tr>
						<td class="odd" nowrap>Source <span
							style="font: 9px bold verdana, arial, helvetica, sans-serif;">(Avail/Done/Total)</span>
						</td>
					</tr>
					<tr>
						<td nowrap>Target <span
							style="font: 9px bold verdana, arial, helvetica, sans-serif;">(Avail/Done/Total)</span>
						</td>
					</tr>
				</table>
				</div>
				</td>
				<td valign="top"
					style="border-right: 1px solid #333333; padding: 0px; width: 45%;">
				<div style="width: 750px; float: left; overflow: auto;">
				<table width="100%" style="border: 0;" cellspacing="0"
					cellpadding="0">
					<tr>
						<g:each in="${bundleTeams}" var="bundleTeam">
							<td style="padding: 0px; border-right: 1px solid #333333">
							<table style="border: 0;">
								<th nowrap>${bundleTeam?.team?.name }&nbsp;</th>
								<tr>
									<td nowrap>${bundleTeam?.members}&nbsp;</td>
								</tr>
								<tr>
									<td class="odd">${bundleTeam?.team?.currentLocation}&nbsp;</td>
								</tr>
								<tr>
									<td nowrap>Sap1</td>
								</tr>
								<tr>
									<td class="odd">${bundleTeam?.sourceAvailassets} /
									${bundleTeam?.unrackedAssets} / ${bundleTeam?.sourceAssets}</td>
								</tr>
								<tr>
									<td nowrap>${bundleTeam?.targetAvailAssets} /
									${bundleTeam?.rerackedAssets} / ${bundleTeam?.targetAssets}</td>
								</tr>
							</table>
							</td>
						</g:each>
						<td style="padding: 0px; border-right: 1px solid #333333">
						<table style="border: 0;">
							<th nowrap>${supportTeam?.cleaning.name}&nbsp;</th>
							<tr>
								<td nowrap>${supportTeam?.cleaningMembers}&nbsp;</td>
							</tr>
							<tr>
								<td class="odd">${supportTeam?.cleaning.currentLocation}&nbsp;</td>
							</tr>
							<tr>
								<td nowrap>Exchg3</td>
							</tr>
							<tr>
								<td class="odd">${supportTeam.sourceCleaned} /
								${supportTeam.totalAssets}</td>
							</tr>
							<tr>
								<td nowrap>N/A</td>
							</tr>
						</table>
						</td>
						<td style="padding: 0px;">
						<table style="border: 0;">
							<th nowrap>${supportTeam?.transport.name}&nbsp;</th>
							<tr>
								<td nowrap>${supportTeam?.transportMembers}&nbsp;</td>
							</tr>
							<tr>
								<td class="odd">${supportTeam?.transport.currentLocation}&nbsp;</td>
							</tr>
							<tr>
								<td nowrap>Exchg3</td>
							</tr>
							<tr>
								<td class="odd">${supportTeam.sourceMover} /
								${supportTeam.totalAssets}</td>
							</tr>
							<tr>
								<td nowrap>${supportTeam.targetMover} /
								${supportTeam.totalAssets}</td>
							</tr>
						</table>
						</td>
					</tr>
				</table>
				</div>
				</td>
				<td valign="top"
					style="border-right: 1px solid #333333; padding: 0px; width: 15%;">
				<div style="float: left; width: 100%;">
				<table style="width: 100%; border: 0">
					<th nowrap>TOTALS:</th>
					<tr>
						<td>&nbsp;</td>
					</tr>
					<tr>
						<td class="odd">&nbsp;</td>
					</tr>
					<tr>
						<td>&nbsp;</td>
					</tr>
					<tr>
						<td class="odd">${totalSourceAvail} / ${totalUnracked} /
						${totalAsset}</td>
					</tr>
					<tr>
						<td nowrap>${totalTargetAvail} / ${totalReracked} /
						${totalAsset}</td>
					</tr>
				</table>
				</div>
				</td>
			</tr>

		</table>
		</div>
		</td>
	</tr>



	<tr>
		<td>

		<table style="border: 0px;">

			<tr>
				<td valign="top" style="padding: 0px;">
				<div class="list"><g:form name="assetListForm">
					
					<table>
						<thead>

							<tr
								onmouseover="document.getElementById('tdId').style.background = 'white';">
								<td id="tdId"><input id="state" type="button"
									value="State..." onclick="changeState()" title="Change State" />
								</td>
							</tr>
							<tr>
								<!-- <g:sortableColumn property="assetName" title="Asset Name" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="status" title="Status" />
					<g:sortableColumn property="team" title="Team" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="statTimer" title="Stat Timer" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="loc" title="Loc" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
					<g:sortableColumn property="issues" title="Issues" /> -->
								<jsec:hasRole in="['ADMIN','MANAGER']"><th>Actions <a href="#" onclick="selectAll()"><u
									style="color: blue;">All</u></a></th></jsec:hasRole>
								<th>Priority</th>
								<th>Asset Tag</th>
								<th>Asset Name</th>
								<th>Status</th>
								<th>Source Team</th>
								<th>Target Team</th>
								<th>Loc</th>
								<th>Issues</th>
							</tr>
						</thead>
						<tbody id="assetsTbody">
							<g:each status="i" in="${assetsList}" var="assetsList">

								<tr name="assetDetailRow"
									id="assetDetailRow_${assetsList?.asset.id}"
									class="${assetsList?.cssClass}" value="${assetsList?.asset.id}">

									<jsec:hasRole in="['ADMIN','MANAGER']">
									<td><jsec:hasRole name="ADMIN">
										<g:if test="${assetsList.checkVal == true}">
<span id="spanId_${assetsList?.asset.id}">
											<g:checkBox name="checkChange"
												id="checkId_${assetsList?.asset.id}"
												onclick="timedRefresh('never')"></g:checkBox>
</span>

										</g:if>
									</jsec:hasRole>
									 <span style="visibility: hidden;"
										id="image_${assetsList?.asset.id}"><img
										src="${createLinkTo(dir:'images',file:'row_arrow.gif')}"
										border="0px"></span>
										
										</td></jsec:hasRole>
									<td id="priorityCol_${assetsList?.asset.id}"
										onclick="assetDetails('${assetsList?.asset.id}')">${assetsList?.asset.priority}</td>
									<td onclick="assetDetails('${assetsList?.asset.id}')">${assetsList?.asset.assetTag}</td>
									<td onclick="assetDetails('${assetsList?.asset.id}')">${assetsList?.asset.assetName}</td>
									<td onclick="assetDetails('${assetsList?.asset.id}')"
										id="statusCol_${assetsList?.asset.id}">${assetsList?.status}</td>
									<td onclick="assetDetails('${assetsList?.asset.id}')"
										id="source_${assetsList?.asset.id}">${assetsList?.asset.sourceTeam?.name}</td>
									<td onclick="assetDetails('${assetsList?.asset.id}')"
										id="target_${assetsList?.asset.id}">${assetsList?.asset.targetTeam?.name}</td>
									<td onclick="assetDetails('${assetsList?.asset.id}')">${assetsList?.asset?.sourceTeam?.currentLocation}</td>
									<td id="icon_${assetsList?.asset.id}"><g:if
										test="${AssetComment.findByAssetEntityAndCommentType(assetsList?.asset,'issue')}">
										<g:remoteLink controller="assetEntity" action="listComments"
											id="${assetsList?.asset.id}"
											before="document.getElementById('createAssetCommentId').value = ${assetsList?.asset.id};"
											onComplete="listCommentsDialog( e );">
											<img
												src="${createLinkTo(dir:'images/skin',file:'database_table_red.png')}"
												border="0px">
										</g:remoteLink>
									</g:if> <g:else>
										<g:if
											test="${AssetComment.findByAssetEntity(assetsList?.asset)}">
											<g:remoteLink controller="assetEntity" action="listComments"
												id="${assetsList?.asset.id}"
												before="document.getElementById('createAssetCommentId').value = ${assetsList?.asset.id};"
												onComplete="listCommentsDialog( e );">
												<img
													src="${createLinkTo(dir:'images/skin',file:'database_table_bold.png')}"
													border="0px">
											</g:remoteLink>
										</g:if>
									</g:else></td>
								</tr>

							</g:each>
						</tbody>
					</table>

				</g:form></div>
				</td>
				<td valign="top" style="padding: 0px;">
				<div id="assetDetails"
					style="border: 1px solid #5F9FCF; width: 200px;">
				<div id="asset">
				<table style="border: 0px" cellpadding="0" cellspacing="0">
					<thead>
						<tr>
							<th>Asset Details</th>
						</tr>
					</thead>
					<tbody>
						<tr>
							<td><b>Name: </b></td>
						</tr>
						<tr>
							<td><b>Model:</b></td>
						</tr>
						<tr>
							<td><b>Rack: </b></td>
						</tr>
						<tr>
							<td><b>Status: </b></td>
						</tr>
						<tr>
							<td><b>Issue: </b></td>
						</tr>
						<tr>
							<td><b>Time: </b></td>
						</tr>
						<tr>
							<td><b>Assigned: </b></td>
						</tr>
						<tr>
							<td><b>Recent Changes: </b></td>
						</tr>
					</tbody>
				</table>
				</div>
				<div><g:form name="assetdetailsForm">
					<table style="border: 0">
						<tbody>
							<tr>
								<td><b>Change:</b></td>
								<td><select id="stateSelectId" name="state"
									style="width: 100px"
									onchange="${remoteFunction(action:'getFlag', params:'\'toState=\'+ this.value +\'&fromState=\'+document.getElementById(\'currentStateId\').value', onComplete:'setComment(e)')}">
									<option value="">Status</option>
								</select></td>
							</tr>
							<tr>
								<td><input type="hidden" name="asset" id="assetId" value="">
								&nbsp;</td>
								<td><g:select id="priorityId" name="priority"
									from="${AssetEntity.constraints.priority.inList}"
									style="width: 100px" noSelection="['':'Priority ']"></g:select>
								</td>
							</tr>
							<tr>
								<td><input type="hidden" name="currentState"
									id="currentStateId" value="">&nbsp;</td>
								<td><select id="assignToId" name="assignTo"
									style="width: 100px">
									<option value="">Move Team</option>
									<optgroup label="Source" id="sourceAssignTo"></optgroup>
									<optgroup label="Target" id="targetAssignTo"></optgroup>
								</select></td>
							</tr>
							<tr>
								<td colspan="2" style="text-align: center;"><input
									type="hidden" value="" id="validateCommentId"
									name="validateComment"> <textarea name="comment"
									name="comment" cols="25" rows="2"></textarea></td>
							</tr>
							<tr>
								<td colspan="2" style="text-align: center;" class="buttonR">
								<input type="reset" value="Cancel"
									onclick="location.reload(true);timedRefresh(document.getElementById('selectTimedId').value)">
								<input type="button" value="Submit"
									onclick="setCommentValidation();${remoteFunction(action:'createTransition', params:'\'asset=\' + document.assetdetailsForm.asset.value +\'&state=\'+document.assetdetailsForm.state.value +\'&priority=\'+document.assetdetailsForm.priority.value +\'&assignTo=\'+document.assetdetailsForm.assignTo.value +\'&comment=\'+document.assetdetailsForm.comment.value ', onComplete:'updateAsset(e)')}" /></td>
							</tr>
						</tbody>
					</table>
				</g:form></div>
				</div>
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</div>
<div id="commentsListDialog" title="Show Asset Comments"
	style="display: none;"><br>
<div class="list">
<table id="listCommentsTable">
	<thead>
		<tr>

			<th nowrap>Action</th>

			<th nowrap>Comment</th>

			<th nowrap>Comment Type</th>

			<th nowrap>Must Verify</th>

		</tr>
	</thead>
	<tbody id="listCommentsTbodyId">

	</tbody>
</table>
</div>
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
<span class="menuButton"><a class="create" href="#"
	onclick="document.getElementById('statusId').value = '';$('#createCommentDialog').dialog('option', 'width', 700);$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();">New
Comment</a></span></div>
</div>
<div id="createCommentDialog" title="Create Asset Comment"
	style="display: none;"><input type="hidden" name="assetEntity.id"
	id="createAssetCommentId" value=""> <input type="hidden"
	name="status" id="statusId" value=""> <g:form
	action="saveComment" method="post" name="createCommentForm">
	<div class="dialog">
	<table id="createCommentTable">
		<tbody>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="comment" name="comment"></textarea></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="commentType">Comment
				Type:</label></td>
				<td valign="top" class="value"><g:select id="commentType"
					name="commentType"
					from="${AssetComment.constraints.commentType.inList}" value=""
					noSelection="['':'please select']"></g:select></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="mustVerify">Must
				Verify:</label></td>
				<td valign="top" class="value"><input type="checkbox"
					id="mustVerify" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
				</td>
			</tr>
		</tbody>
	</table>
	</div>
	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Create"
		onclick="${remoteFunction(action:'saveComment', params:'\'assetEntity.id=\' + document.getElementById(\'createAssetCommentId\').value +\'&comment=\'+document.createCommentForm.comment.value +\'&commentType=\'+document.createCommentForm.commentType.value +\'&mustVerify=\'+document.createCommentForm.mustVerify.value', onComplete:'addCommentsToList(e)')}" /></span></div>
</g:form></div>
<div id="showCommentDialog" title="Show Asset Comment"
	style="display: none;">
<div class="dialog"><input name="id" value="" id="commentId"
	type="hidden">
<table id="showCommentTable">
	<tbody>
		<tr class="prop">
			<td valign="top" class="name"><label for="comment">Comment:</label>
			</td>
			<td valign="top" class="value" id="commentTdId" />
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="commentType">Comment
			Type:</label></td>
			<td valign="top" class="value" id="commentTypeTdId" />
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="mustVerify">Must
			Verify:</label></td>
			<td valign="top" class="value" id="verifyTdId"><input
				type="checkbox" id="mustVerifyShowId" name="mustVerify" value="0"
				disabled="disabled" /></td>
		</tr>
	</tbody>
</table>
</div>
<div class="buttons"><span class="button"> <input
	class="edit" type="button" value="Edit"
	onclick="$('#editCommentDialog').dialog('option', 'width', 700);$('#createCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('open');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close')" />
</span> <span class="button"> <input class="delete" type="button"
	value="Delete"
	onclick="${remoteFunction(action:'deleteComment', params:'\'id=\' + document.getElementById(\'commentId\').value +\'&assetEntity=\'+document.getElementById(\'createAssetCommentId\').value ', onComplete:'listCommentsDialog(e)')}" />
</span></div>
</div>
<div id="editCommentDialog" title="Edit Asset Comment"
	style="display: none;"><g:form action="updateComment"
	method="post" name="editCommentForm">
	<div class="dialog"><input type="hidden" name="id" value="">
	<table id="updateCommentTable">
		<tbody>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="comment" name="comment"></textarea></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="commentType">Comment
				Type:</label></td>
				<td valign="top" class="value"><g:select id="commentType"
					name="commentType"
					from="${AssetComment.constraints.commentType.inList}" value=""
					noSelection="['':'please select']"></g:select></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="mustVerify">Must
				Verify:</label></td>
				<td valign="top" class="value"><input type="checkbox"
					id="mustVerifyEdit" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />
				</td>
			</tr>
		</tbody>
	</table>
	</div>
	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Update"
		onclick="${remoteFunction(action:'updateComment', params:'\'id=\' + document.editCommentForm.id.value +\'&comment=\'+document.editCommentForm.comment.value +\'&commentType=\'+document.editCommentForm.commentType.value +\'&mustVerify=\'+document.editCommentForm.mustVerify.value', onComplete:'updateCommentsOnList(e)')}" />
	</span> <span class="button"> <input class="delete" type="button"
		value="Delete"
		onclick="${remoteFunction(action:'deleteComment', params:'\'id=\' + document.editCommentForm.id.value +\'&assetEntity=\'+document.getElementById(\'createAssetCommentId\').value ', onComplete:'listCommentsDialog(e)')}" />
	</span></div>
</g:form></div>
<script type="text/javascript">
bundleChange();
timedRefresh(document.getElementById("selectTimedId").value)
</script></div>
</body>
</html>
