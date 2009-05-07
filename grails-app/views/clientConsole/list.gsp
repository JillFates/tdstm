
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Report List</title>
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
	if(time != "never" && time != "" ){
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
var task = eval('(' + e.responseText + ')');

var options = '';
      for (var i = 0; i < task[0].item.length; i++) {
        options += '<option value="' + task[0].item[i] + '">' + task[0].item[i] + '</option>';
      }
      $("select#taskList").html(options);
       var getDialogId = document.getElementById('asset')     
      getDialogId.value=task[0].asset;
 
 
$("#showDialog").dialog('option', 'width', 400)
$("#showDialog").dialog('option', 'position', ['center','top']);
$('#showDialog').dialog('open');
}
function submitAction(){
if(doCheck()){
document.changeStatusForm.action = "changeStatus";
document.changeStatusForm.submit();
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
		timer = setTimeout("location.reload(false);",timeoutPeriod);
		document.getElementById("selectTimedId").value = timeoutPeriod;
	} else {
		clearTimeout(timer)
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
			onclick="submitAction()" /></td>
	</tr>
	
</table>
</form>
</div>
<div style="width:100%"><br>
<div style="width: 100%;">
	<g:form	name="listForm" action="list" method="post">
	<h1 align="center">PMO Dashboard</h1>
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
			<td style="text-align: right;"><input type="button"
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
<div style="width: 99%; overflow-x: scroll;border:1px solid #5F9FCF;margin-left: 3px;">
<table cellpadding="1" cellspacing="1"  style="border:0px;">
	<thead>
	<tr>
	
			<td>&nbsp;</td>
			<td style="padding-left: 0px;"><select id="applicationId" name="application" onchange="document.listForm.submit();" style="width: 100px;">
				<option value="">All</option>
				<g:each in="${applicationList}" var="application">
					<option value="${application}">${application}</option>
				</g:each>
			</select></td>
			<td style="padding-left: 0px;"><select id="appOwnerId" name="appOwner"	onchange="document.listForm.submit();" style="width: 100px;">
				<option value="">All</option>
				<g:each in="${appOwnerList}" var="appOwner">
					<option value="${appOwner}">${appOwner}</option>
				</g:each>
			</select></td>
			<td style="padding-left: 0px;"><input type="hidden" id="projectId" name="projectId" value="${projectId }" />
			 <select id="appSmeId" name="appSme" onchange="document.listForm.submit();" style="width: 100px;">
				<option value="">All</option>
				<g:each in="${appSmeList}" var="appSme">
					<option value="${appSme}">${appSme}</option>
				</g:each>
			</select></td>
		</g:form>
		</tr>
		<tr>
			<th>Actions</th>
			<g:sortableColumn property="application"  title="Application" params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]"/>
			
			<g:sortableColumn property="app_owner" title="App Owner"  params="['projectId':projectId]" />

			<g:sortableColumn property="app_sme" title="App Sme" params="['projectId':projectId]"/>

			<g:sortableColumn property="asset_name" title="Asset Name" params="['projectId':projectId]"/>

			<g:each in="${processTransitionList}"  var="task">

				<th title="${task.title}">${task?.header}</th>

			</g:each>

		</tr>
	</thead>
	<tbody>

		<g:each in="${assetEntityList}" var="assetEntity">
			<tr>
				<td><jsec:hasRole name="ADMIN">
					<g:if test="${assetEntity.checkVal == true}">
						<g:remoteLink action="getTask"
							params="['assetEntity':assetEntity.id]"
							onComplete="showChangeStatusDialog(e);">
							<img
								src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}"
								border="0px">
						</g:remoteLink>
					</g:if>
				</jsec:hasRole>
				<td>${assetEntity?.application}</td>
				<td>${assetEntity?.appOwner}</td>
				<td>${assetEntity?.appSme}</td>
				<td>${assetEntity?.assetName}</td>
				<g:each in="${processTransitionList}" var="process">
					<td bgcolor="white"><g:each in="${assetEntity?.transitions}"
						var="transitions">
						<g:if test="${transitions == process.transId}">
							<g:if test="${Integer.parseInt(transitions) == ProjectAssetMap.find('from ProjectAssetMap where asset.id ='+assetEntity.id).currentStateId && Integer.parseInt(transitions) == 10}">
							<div style="background-color: yellow;">&nbsp;</div>
							</g:if>
							<g:else>
							<div style="background-color: green;">&nbsp;</div>
							</g:else>
						</g:if>
					</g:each></td>
				</g:each>
			</tr>
		</g:each>
	</tbody>
</table>
</div>
<g:javascript>
initialize();
timedRefresh(document.getElementById("selectTimedId").value)
</g:javascript>
</body>

</html>
