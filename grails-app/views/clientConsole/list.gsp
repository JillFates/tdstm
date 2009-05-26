
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>PMO Dashboard</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<jq:plugin name="jquery.bgiframe.min" />
<jq:plugin name="jquery.autocomplete" />
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
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

<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />

<g:javascript>
function initialize(){
var bundleID = ${moveBundleInstance.id}; 
document.getElementById("moveBundleId").value =  bundleID;
document.getElementById('appSmeId').value="${appSmeValue}"
document.getElementById('appOwnerId').value="${appOwnerValue}"
document.getElementById('applicationId').value="${appValue}"
var time = '${timeToRefresh}';
	if(time != "" ){
	document.getElementById("selectTimedId").value = time;
	} else if(time == "" ){
	document.getElementById("selectTimedId").value = 120000;	
	}
}
</g:javascript>
<script>
	      $(document).ready(function() {
	        $("#showDialog").dialog({ autoOpen: false })
	       
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
	       var getDialogId = document.getElementById('asset')     
	      getDialogId.value=task[0].asset;
	 
	 
	$("#showDialog").dialog('option', 'width', 400)
	$("#showDialog").dialog('option', 'position', ['center','top']);
	$('#showDialog').dialog('open');
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
function setRefreshTime(e) {
	var timeRefresh = eval("(" + e.responseText + ")")
	if(timeRefresh){
		timedRefresh(timeRefresh[0].refreshTime.CLIENT_CONSOLE_REFRESH)
	}
}
var timer
function timedRefresh(timeoutPeriod) {
	if(timeoutPeriod != 'never'){
		timer = setTimeout("doAjaxCall()",timeoutPeriod);
		document.getElementById("selectTimedId").value = timeoutPeriod;
	} else {
		clearTimeout(timer)
	}
}

function doAjaxCall(){
	var moveBundle = document.getElementById("moveBundleId").value;
	var application = document.getElementById("applicationId").value;
	var appOwner = document.getElementById("appOwnerId").value;
	var appSme = document.getElementById("appSmeId").value;
	${remoteFunction(action:'getTransitions', params:'\'moveBundle=\' + moveBundle +\'&application=\'+application +\'&appOwner=\'+appOwner+\'&appSme=\'+appSme', onComplete:'updateTransitions(e);' )}
	timedRefresh(document.getElementById("selectTimedId").value)
}
function updateTransitions(e){
	var assetTransitions = eval('(' + e.responseText + ')');
	if(assetTransitions){
		var assetslength = assetTransitions.length;
		for( i = 0; i <assetslength ; i++){
			var assetTransition = assetTransitions[i]
			var action = document.getElementById("action_"+assetTransition.id)
			if(action){
				if(!assetTransition.check){
					action.style.visibility='hidden';
				} 
			}
			var application = document.getElementById("application_"+assetTransition.id)
			if(application){
				application.innerHTML = assetTransition.application
			}
			var owner = document.getElementById("owner_"+assetTransition.id)
			if(owner){
				owner.innerHTML = assetTransition.appOwner
			}
			var sme = document.getElementById("sme_"+assetTransition.id)
			if(sme){
				sme.innerHTML = assetTransition.appSme
			}
			var assetName = document.getElementById("assetName_"+assetTransition.id)
			if(assetName){
				assetName.innerHTML = assetTransition.assetName
			}
			var tdIdslength = assetTransition.tdId.length
			for(j = 0; j< tdIdslength ; j++){
				var transition = assetTransition.tdId[j]
				var transTd = document.getElementById(transition.id)
				transTd.className = transition.cssClass
			}
		}
	}
}

function changeState(){
	timedRefresh('never')
	var assetArr = new Array();
	var totalAsset = ${assetEntityList.id};
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
		${remoteFunction(action:'getList', params:'\'assetArray=\' + assetArr', onComplete:'showChangeStatusDialog(e);' )}
	}
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


</script>
</head>
<body>
<div title="Change Status" id="showDialog"
	style="background-color: #808080;display: none;">
<form name="changeStatusForm"><input type="hidden" name="asset"
	id="asset" /> <input type="hidden" name="projectId" id="projectId"
	value="${projectId}" />
	<input type="hidden" name="moveBundle" id="moveBundle"
	value="${moveBundleInstance.id}" />
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
		<td>
		<textarea rows="2" cols="1"  title="Enter Note..." name="enterNote" id="enterNote" style="width: 200%"></textarea>
		</td>
	</tr>
	<tr>
		<td></td>
		<td style="text-align: right;"><input type="button" value="Save"
			onclick="confirm('Are you sure???');submitAction()" /></td>
	</tr>
</table>
</form>
</div>
<div style="width:100%">
<div style="width: 100%;">
	<g:form	name="listForm" action="list" method="post">
	
	<table style="border: 0px;">
		<tr>
			<td valign="top" class="name"><label for="moveBundle">Move
		Bundle:</label>&nbsp;<select id="moveBundleId"
			name="moveBundle" onchange="document.listForm.submit()" >	

			<g:each status="i" in="${moveBundleInstanceList}"
				var="moveBundleInstance">
				<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
			</g:each>

		</select></td>
			<td><h1 align="center">PMO Dashboard</h1></td>
			<td style="text-align: right;">
			<input type="hidden" name="last_refresh" value="${new Date()}">
			<input type="button"
				value="Refresh" onclick="location.reload(true);"> <select
				id="selectTimedId"
				onchange="${remoteFunction(action:'setTimePreference', params:'\'timer=\'+ this.value ' , onComplete:'setRefreshTime(e)') }">
				<option value="30000">30 sec</option>
				<option value="60000">1 min</option>
				<option value="120000">2 min</option>
				<option value="300000">5 min</option>
				<option value="never">no refresh</option>
			</select></td>
		</tr>
	</table>

</div>
<div class="tableContainer">

<table cellpadding="1" cellspacing="1"  style="border:0px;">
	<thead>
	<tr>
	<td>
	<input type="button" value="State..." onclick="changeState()" title="Change State"/>
	</td>
			
			<td style="padding-left: 0px;"><select id="applicationId" name="application" onchange="document.listForm.submit();" style="width: 120px;">
				<option value="">All</option>
				<g:each in="${applicationList}" var="application">
					<option value="${application}">${application}</option>
				</g:each>
			</select></td>
			<td style="padding-left: 0px;"><select id="appOwnerId" name="appOwner"	onchange="document.listForm.submit();" style="width: 120px;">
				<option value="">All</option>
				<g:each in="${appOwnerList}" var="appOwner">
					<option value="${appOwner}">${appOwner}</option>
				</g:each>
			</select></td>
			<td style="padding-left: 0px;"><input type="hidden" id="projectId" name="projectId" value="${projectId }" />
			 <select id="appSmeId" name="appSme" onchange="document.listForm.submit();" style="width: 120px;">
				<option value="">All</option>
				<g:each in="${appSmeList}" var="appSme">
					<option value="${appSme}">${appSme}</option>
				</g:each>
			</select></td>
	<td style="padding-left: 0px;"><select style="width: 120px;visibility: hidden;"/></td>
		</tr>
		<tr>
		 <jsec:hasRole in="['ADMIN','MANAGER']">	<th style="padding-top:85px;">Actions <a href="#" onclick="selectAll()" ><u style="color:blue;">All</u></a></th></jsec:hasRole>
			
			<g:sortableColumn style="padding-top:85px;" property="application"  title="Application" params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]"/>
			
			<g:sortableColumn style="padding-top:85px;" property="app_owner" title="App Owner"  params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]" />

			<g:sortableColumn style="padding-top:85px;" property="app_sme" title="App SME" params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]"/>

			<g:sortableColumn style="padding-top:85px;" property="asset_name" title="Asset Name" params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]"/>
			
			<g:if test="${browserTest}">
			<g:each in="${processTransitionList}"  var="task">
				<th class="verticaltext" title="${task.header}">${task?.header}</th>
			</g:each>
			</g:if>
			<g:else>
			<th style="padding-left: 0px" colspan="${headerCount}"><embed src="${createLinkTo(dir:'templates',file:'headerSvg.svg')}" type="image/svg+xml" width="${headerCount*22}" height="180"/></th>
			</g:else>

		</tr>
	</thead>
	<tbody>
		<g:each in="${assetEntityList}" var="assetEntity">
			<tr>
			<td id="action_${assetEntity.id}">
				<jsec:hasRole in="['ADMIN','MANAGER']">	
					<g:if test="${assetEntity.checkVal == true}">
						<g:checkBox name="checkChange" id="checkId_${assetEntity.id}" onclick="timedRefresh('never')"></g:checkBox> 
						<g:remoteLink action="getTask" params="['assetEntity':assetEntity.id]"	onComplete="showChangeStatusDialog(e);">
							<img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}"	border="0px">
						</g:remoteLink>
					</g:if>
				</jsec:hasRole>
			</td>
			<td id="application_${assetEntity.id}">${assetEntity?.application}</td>
			<td id="owner_${assetEntity.id}">${assetEntity?.appOwner}</td>
			<td id="sme_${assetEntity.id}">${assetEntity?.appSme}</td>
			<td id="assetName_${assetEntity.id}">${assetEntity?.assetName}</td>
			<g:each in="${assetEntity.transitions}" var="transition">${transition}</g:each>
			</tr>
		</g:each>
		
	</tbody>
</table>
</g:form>
</div>
<g:javascript>
initialize();
timedRefresh(document.getElementById("selectTimedId").value)
</g:javascript>
</body>

</html>
