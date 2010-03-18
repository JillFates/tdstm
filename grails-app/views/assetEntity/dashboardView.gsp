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
<g:javascript src="assetcrud.js" />
<g:javascript src="assetcommnet.js" />
<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />
<jq:plugin name="jquery.scrollfollow" />

<style>
td .odd {
	background: #DDDDDD;
	nowrap
}
</style>

<script>
	$(document).ready(function() {
	    $("#showDialog").dialog({ autoOpen: false })
	    $("#editDialog").dialog({ autoOpen: false })
		$("#commentsListDialog").dialog({ autoOpen: false })
	    $("#createCommentDialog").dialog({ autoOpen: false })
	    $("#showCommentDialog").dialog({ autoOpen: false })
	    $("#editCommentDialog").dialog({ autoOpen: false })
	    $("#showChangeStatusDialog").dialog({ autoOpen: false })
	    $('#filterDialog').dialog({ autoOpen: false })
	    $( '#floatMenu' ).scrollFollow({
		    speed: 50
		});
	})
</script>
<script type="text/javascript">

	function editAssetDialog() {
		timedRefresh('never')
		$("#showDialog").dialog("close")
		$("#editDialog").dialog('option', 'width', 600)
		$("#editDialog").dialog('option', 'position', ['center','top']);
		$("#editDialog").dialog("open")
	}
	
	function showEditAsset(e) {
		var assetEntityAttributes = eval('(' + e.responseText + ')')
		if (assetEntityAttributes != "") {
			var trObj = $("#assetDetailRow_"+assetEntityAttributes[0].id)
			//trObj.style.background = '#65a342'
			var length = assetEntityAttributes.length
			for (var i=0; i < length; i ++) {
				var attribute = assetEntityAttributes[i]
				var tdId = $("#"+attribute.attributeCode+'_'+attribute.id)
				if(tdId != null ){
					tdId.html(attribute.value)
				}
			}
			$("#editDialog").dialog("close")
		} else {
			alert("Asset Entity is not updated")
		}
	} 		

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
			$('#taskList').children().eq(1).attr('selected',true);
		}
		$('#assetVal').val(task[0].asset);
		var showAll = $("#showAllCheckbox").is(':checked');
		if(showAll){
			$('#showAllInChangeStatus').val('show');
		}
		$("#showChangeStatusDialog").dialog('option', 'width', 400)
		$("#showChangeStatusDialog").dialog('option', 'position', ['center','top']);
		$('#showChangeStatusDialog').dialog('open');
		}
	}

	function submitAction(){
		if(doCheck()){
			document.changeStatusForm.action = "changeStatus";
			document.changeStatusForm.submit();
			timedRefresh($("#selectTimedId").val())
		}else{
			return false;
		}
	}
	function doCheck(){
		var taskVal = $('#taskList').val();
		var noteVal = $('#enterNote').val();
		if((taskVal == "Hold")&&(noteVal == "")){
			alert('Please Enter Note');
			return false;
		}else{
			return true;
		}
	}
   	function assetDetails(assetId) {
		var assetId = assetId;
	   	var rows = $('#assetsTbody').children('tr')
	   	for(i = 0 ; i<rows.length ; i++){
			var rowVal = rows[i].getAttribute("value")
		   	$("#image_"+rowVal).css('visibility','hidden');
	   	}
	   	$('#image_'+assetId).css('visibility','visible');
	   	timedRefresh('never')
	   	${remoteFunction(action:'assetDetails', params:'\'assetId=\'+ assetId ' , onComplete:'getAssetDetail(e)') }
	}
   	function getAssetDetail(e){
	   	var asset = eval("(" + e.responseText + ")")
	    var tableBody = '<table style=\'border:0\' cellpadding=\'0\' cellspacing=\'0\' ><thead><tr><th colspan="2">Asset Details </th></tr></thead><tbody>'+
		'<tr><td><b>Name: </b>'+asset[0].assetDetails.assetDetail.assetName+'</td></tr>'+
		'<tr><td><b>Model: </b>'+asset[0].assetDetails.assetDetail.model+'</td></tr>'+
		'<tr><td><b>Src Rack: </b>'+asset[0].assetDetails.srcRack+'</td></tr>'+
		'<tr><td><b>Tgt Rack: </b>'+asset[0].assetDetails.tgtRack+'</td></tr>'+
		'<tr><td><b>Status: </b>'+asset[0].assetDetails.currentState+'</td></tr>'+
		'<tr><td><b>Recent Changes: </b></td><td><a href="#" id="moreLinkId" onclick="displayMore();return false;"><b>More</b></a>'+
		'<a href="#" id="lessLinkId" style="display:none" onclick="displayLess(); return false;"><b>Less</b></a></td></tr>' +
		'<tr><td colspan="2">&nbsp;'+asset[0].sinceTimeElapsed+' Since last action </td></tr>'+
		'<tr><td colspan="2">' +
		'<div id=\'recentChangesLess\'><table style=\'border: 0px\' cellpadding=\'0\' cellspacing=\'0\'><tbody>'
		for(i=0;i<asset[0].recentChanges.length && i<3; i++){
			tableBody += '<tr><td class='+asset[0].recentChanges[i].cssClass+'>'+asset[0].recentChanges[i].transition+'</td></tr>'
		}
		tableBody += '<tbody></table></div>'
		tableBody += '<div id=\'recentChangesMore\' style=\"display: none;\"><table style=\'border: 0px\'  cellpadding=\'0\' cellspacing=\'0\' ><tbody>'
		for(i=0;i<asset[0].recentChanges.length ; i++){
			tableBody += '<tr><td class='+asset[0].recentChanges[i].cssClass+'>'+asset[0].recentChanges[i].transition+'</td></tr>'
		}
		tableBody += '<tbody></table></div></td></tr>'
		tableBody += '</tbody></table>'
	    var selectObj = $("#asset")
	   	selectObj.html(tableBody)
	   	createStateOptions(asset[0].statesList)
	   	createAssighToOptions(asset[0].sourceTeams,asset[0].targetTeams)
	   	document.assetdetailsForm.reset();
	   	$("#assetId").val( asset[0].assetDetails.assetDetail.id )
	   	$("#currentStateId").val( asset[0].assetDetails.state )
   	}
   	function resetAssetDetails(){
   		var tableBody = '<table style=\'border:0\' cellpadding=\'0\' cellspacing=\'0\' ><thead><tr><th colspan="2">Asset Details </th></tr></thead><tbody>'+
		'<tr><td><b>Name: </b></td></tr><tr><td><b>Model: </b></td></tr><tr><td><b>Rack: </b></td></tr><tr><td><b>Status: </b></td></tr>'+
		'<tr><td><b>Recent Changes: </b></td></tr><tbody></table>'
	   	$("#asset").html(tableBody);
	   	$("#stateSelectId").html("<option value=''>Status</option>");
	   	$("#assignToId").html("<option value=''>Move Team</option><optgroup label='Source' id='sourceAssignTo'></optgroup><optgroup label='Target' id='targetAssignTo'></optgroup>")
	   	var rows = $('#assetsTbody').children('tr')
	   	for(i = 0 ; i<rows.length ; i++){
		var rowVal = rows[i].getAttribute("value")
		   	$("#image_"+rowVal).css('visibility','hidden');
	   	}
	   	var seconds = new Date().getTime() - $("#lastRefreshId").val();
	   	var refreshTime = $('#selectTimedId').val() - seconds
	   	if( !isNaN(refreshTime) ){
	   		timedRefresh( refreshTime )
	   	}
   	}
   	function displayLess(){
   		$("#recentChangesMore").hide()
   		$("#recentChangesLess").show()
   		$("#moreLinkId").show()
   		$("#lessLinkId").hide()
   	}
   	function displayMore(){
   		$("#recentChangesMore").show()
   		$("#recentChangesLess").hide()
   		$("#moreLinkId").hide()
   		$("#lessLinkId").show()
   	}
   	
   	function createStateOptions(statesList){
		var statusObj = $("#stateSelectId")
		statusObj.html("<option value=''>Status</option>")
		var length = statesList.length
	    for (var i=0; i < length; i++) {
	      var state = statesList[i]
	      var popt = document.createElement('option');
		  popt.innerHTML = state.label
	      popt.value = state.id
	      try {
	      statusObj.append(popt, null) // standards compliant; doesn't work in IE
	      } catch(ex) {
	      statusObj.append(popt) // IE only
	      }
		}
	}
   	function createAssighToOptions(sourceTeams,targetTeams){
   		var teamObj = $("#assignToId")
	   	var sourceObj = $("#sourceAssignTo")
	   	var targetObj = $("#targetAssignTo")
	   	sourceObj.html("")
	   	targetObj.html("")
		var sourceLength = sourceTeams.length
	    for (var i=0; i < sourceLength; i++) {
	      var team = sourceTeams[i]
	      var popt = document.createElement('option');
		  popt.innerHTML = team.name
	      popt.value = "s/"+team.id
	      try {
	      sourceObj.append(popt, null) // standards compliant; doesn't work in IE
	      } catch(ex) {
	      sourceObj.append(popt) // IE only
	      }
		}
		var targetLength = targetTeams.length
	    for (var i=0; i < targetLength; i++) {
	      var team = targetTeams[i]
	      var popt = document.createElement('option');
		  popt.innerHTML = team.name
	      popt.value = "t/"+team.id
	      try {
	      targetObj.append(popt, null) // standards compliant; doesn't work in IE
	      } catch(ex) {
	      targetObj.append(popt) // IE only
	      }
		}
   	}
   	function bundleChange(){
	   var bundleId = ${moveBundleInstance.id}; 
	   $("#moveBundleId").val( bundleId );
	   var time = '${timeToRefresh}';
	   if(time != '' ){
	   		$("#selectTimedId").val( time );
	   } 
	   var showAll = '${showAll}'
	   var team = '${params.team}'
	   var status = '${params.assetStatus}'
	   var filter = '${params.myForm}'
	   if(showAll == 'show' && !team && !status && filter != 'filterForm'){
	   		$("#showAllCheckbox").attr('checked',true)
	   }
	   $("#filterStateId").val('${params.currentState}');
	   $("#filterApplicationId").val('${params.application}');
	   $("#filterAppSmeId").val('${params.appSme}');
	   $("#filterAppOwnerId").val('${params.appOwner}');
	   
   	}
   	function setComment(e){
	   	var commentStatus = eval("(" + e.responseText + ")")
	   	if(commentStatus[0]){
	   		$("#validateCommentId").val( commentStatus[0].status )
	   	}else {
	   		$("#validateCommentId").val("")
	   	}
   	}
   	function setCommentValidation(){
	   	if($("#validateCommentId").val() == 'true' || $("#stateSelectId").val() == 'Hold'){
	   		if($("#commentId").val() == ''){
	   			alert("A comment is required")
	   		}
	   	}
   	}
   	var timer
   	function timedRefresh(timeoutPeriod) {
   		if(timeoutPeriod != 'never'){
			clearTimeout(timer);
			timer = setTimeout("pageReload()",timeoutPeriod);
		} else {
			clearTimeout(timer)
		}
	}
	function pageReload(){
		$("#showAllId").val('${showAll}');
		if('${params.myForm}'){
			document.forms['${params.myForm}'].submit() ;
		} else {
			window.location = document.URL;
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
				var spanEle = $('#spanId_'+asset[0].assetEntity.id);
				spanEle.html("&nbsp;&nbsp;&nbsp;");
			}		
	
			$('#priority_'+asset[0].assetEntity.id).html( asset[0].assetEntity.priority )
			$('#statusCol_'+asset[0].assetEntity.id).html( asset[0].status )
			$('#source_'+asset[0].assetEntity.id).html( asset[0].sourceTeam )
			$('#target_'+asset[0].assetEntity.id).html( asset[0].targetTeam )
			$('#assetDetailRow_'+asset[0].assetEntity.id).removeAttr( "class" ) ;
			$('#assetDetailRow_'+asset[0].assetEntity.id).addClass(asset[0].cssClass)
			if(asset[0].assetComment != null){
				var link = document.createElement('a');
				link.href = '#'
				link.onclick = function(){$('#createAssetCommentId').val( asset[0].assetEntity.id ) ;new Ajax.Request('listComments?id='+asset[0].assetEntity.id,{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog(e,'never');}})} //;return false
				link.innerHTML = "<img src=\"../images/skin/database_table_red.png\" border=\"0px\">"
				var iconObj = $('#icon_'+asset[0].assetEntity.id);
				iconObj.html(link)
			}
			$("#currentStateId").val( asset[0].statusName )
			$("#priorityId").val("");
			$("#commentId").val("")
			$('#statusCol_'+asset[0].assetEntity.id).click();
		}
		timedRefresh($('#selectTimedId').val());
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
			if($('#checkId_'+totalAsset[i]) != null){
				var booCheck = $('#checkId_'+totalAsset[i]).is(':checked');
				if(booCheck){
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
	
	function showAll(){
		var showAll = $("#showAllCheckbox").is(':checked');
		if(showAll){
			$("#showAllId").val('show');
		}
		$("#teamId").val('');
		$("#assetLocationId").val('');
		$("#assetStatusId").val('');
		$("form#dashboardForm").submit();
	}
	function createNewAssetComment( asset ){
		setAssetId( asset );
		$('#statusId').val('new');
		$('#createCommentDialog').dialog('option', 'width', 700);
		$('#createCommentDialog').dialog('open');
		$('#commentsListDialog').dialog('close');
		$('#editCommentDialog').dialog('close');
		$('#showCommentDialog').dialog('close');
		$('#showDialog').dialog('close');
		$('#editDialog').dialog('close');
		$('#createDialog').dialog('close');
		$('#filterDialog').dialog('close');
		document.createCommentForm.mustVerify.value=0;
		document.createCommentForm.reset();
	}
	function showfilterDialog(){
		timedRefresh('never')
		$('#createCommentDialog').dialog('close');
		$('#commentsListDialog').dialog('close');
		$('#editCommentDialog').dialog('close');
		$('#showCommentDialog').dialog('close');
		$('#showDialog').dialog('close');
		$('#editDialog').dialog('close');
		$('#createDialog').dialog('close');
		$('#filterDialog').dialog('open');
	}
	<%--/* --------------------------------------------
	*	Function to get assets by Team
	*---------------------------------------------*/
	function filterByTeam( team ){
		$("#showAllId").val('show');
		$("#teamId").val( team );
		$("form#dashboardForm").submit();
	}--%>
	/* --------------------------------------------
	*	Function to get assets by Team
	*---------------------------------------------*/
	function filterByDataPoints(assetLocation, team, assetStatus){
		$("#showAllId").val('show');
		$("#teamId").val( team );
		$("#assetLocationId").val( assetLocation );
		$("#assetStatusId").val( assetStatus );
		$("form#dashboardForm").submit();
	}
	function submitFormWithBundle(){
		$("#showAllId").val('');
		$("#teamId").val('');
		$("#assetLocationId").val('');
		$("#assetStatusId").val('');
		$("form#dashboardForm").submit();
	}
	function vpWidth() {
		return window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
	}

	/*------------------------------------------
	 * function to call ConsoleController.invokeSnapshot
	 *-----------------------------------------*/
	function createSnapshot(){
		var moveBundle = $("#moveBundleId").val()
		${remoteFunction(controller:'console', action:'invokeSnapshot', params:'\'moveBundle=\' +moveBundle',onLoad="disableCreateSnapShot()", onComplete:"updateSanpShot( e );")}
	}
	function disableCreateSnapShot(){
		$('#createSnapshotId').attr('disabled', 'true')
	}
	function updateSanpShot( e ){
		
		$('#createSnapshotId').removeAttr("disabled")
	}
    </script>
</head>

<body>

<div title="Change Status" id="showChangeStatusDialog"
	style="background-color: #808080; display: none;">
<form name="changeStatusForm" method="post">
	<input type="hidden"
	name="assetVal" id="assetVal" /> <input type="hidden" name="projectId"
	id="projectId" value="${projectId}" /> <input type="hidden"
	name="moveBundle" id="moveBundle" value="${moveBundleInstance.id}" />
	<input type="hidden" name="showAll" id="showAllInChangeStatus">
		<table style="border: 0px; width: 100%">
	<input type="hidden" id="role" value="${role}"/>
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
			onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)submitAction()" /></td>
	</tr>
</table>
</form>
</div>
<div class="body" style="width: 98%;">
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<g:form method="post" name="dashboardForm" controller="assetEntity" action="dashboardView">
	<input type="hidden" name="projectId" value="${projectId}">
	<input type="hidden" name="showAll" id="showAllId">
	<input type="hidden" name="team" id="teamId" value="${params.team}">
	<input type="hidden" name="assetLocation" id="assetLocationId" value="${params.assetLocation}">
	<input type="hidden" name="assetStatus" id="assetStatusId" value="${params.assetStatus}">
	<input type="hidden" name="myForm" value="dashboardForm">
	<div class="dialog">
	<table style="border: 0px;">
		<tr class="prop">
			<td style="vertical-align: bottom;width:30%" class="name"><label
				for="moveBundle">Move Bundle:</label>&nbsp;<select id="moveBundleId"
				name="moveBundle" onchange="submitFormWithBundle()">

				<g:each status="i" in="${moveBundleInstanceList}"
					var="moveBundleInstance">
					<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
				</g:each>

			</select>
			<input type="button" value="Create Snapshot" id="createSnapshotId" onclick="createSnapshot()">
			</td>
			<td style="width:40%">
			<h1 align="center">Supervisor Console</h1>
			</td>
			<td style="text-align: right; vertical-align: bottom;width:30%">
			<input type="hidden" id="lastRefreshId" name="lastRefresh" value="${new Date().getTime()}">
			<input type="button" value="Refresh" onclick="pageReload();">
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
<table style="border: 0px;">
	<tr>
		<td>
		<div style="width: 100%; float: left; border-left: 1px solid #333333;">
		<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td valign="top"
					style="border-right: 1px solid #333333; padding: 0px; width: 15%;">
				<div style="width: 175px; float: left;">
				<table style="border: 0; width: 175px;">
					<th>TEAMS:</th>
					<tr>
						<td>Names</td>
					</tr>
					<tr>
						<td class="odd">Location</td>
					</tr>
					<tr>
						<td>Latest Asset</td>
					</tr>
					<tr>
						<td class="odd" nowrap>Source <span
							style="font: 9px bold verdana, arial, helvetica, sans-serif;">(Pending/Avail/Done/Total)</span>
						</td>
					</tr>
					<tr>
						<td nowrap>Target <span
							style="font: 9px bold verdana, arial, helvetica, sans-serif;">(Pending/Avail/Done/Total)</span>
						</td>
					</tr>
				</table>
				</div>
				</td>
				<td valign="top"
					style="border-right: 1px solid #333333; padding: 0px; width: 1%;">
				<div style="float: left; overflow: auto;" id="midDiv">
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
									<td nowrap class="odd">${bundleTeam?.team?.currentLocation}&nbsp;</td>
								</tr>
								<tr>
									<td nowrap>
									<g:if test="${bundleTeam?.team?.latestAsset}">
									<a href="#" onclick="assetDetails('${bundleTeam?.team?.latestAsset?.id}')"><u> ${bundleTeam?.team?.latestAsset?.assetTag}</u> </a>
									</g:if>&nbsp;
									</td>
								</tr>
								<tr>
									<td nowrap class="odd">
										<a href="#" onclick="filterByDataPoints('source','${bundleTeam?.team?.id}','source_pend')"><u>${bundleTeam?.sourcePendAssets}</u></a> /
										<a href="#" onclick="filterByDataPoints('source','${bundleTeam?.team?.id}','source_avail')"><u>${bundleTeam?.sourceAvailassets}</u></a> /
										<a href="#" onclick="filterByDataPoints('source','${bundleTeam?.team?.id}','source_done')"><u>${bundleTeam?.unrackedAssets}</u></a> / 
										<a href="#" onclick="filterByDataPoints('source','${bundleTeam?.team?.id}','')"><u>${bundleTeam?.sourceAssets}</u></a>
										<jsec:lacksAllRoles in="['MANAGER','OBSERVER']"> 
										<a title="Relogin as.."  href="#" onclick="window.open('../moveTech/login?username=mt-${moveBundleInstance.id}-${bundleTeam?.team?.id}-s','mtwindow','menubar=1,resizable=1,width=320,height=480'); ">@</a>
										</jsec:lacksAllRoles>
									</td>
								</tr>
								<tr>
									<td nowrap>
										<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_pend')"><u>${bundleTeam?.targetPendAssets}</u> </a> /
										<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_avail')"><u>${bundleTeam?.targetAvailAssets}</u> </a> /
										<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_done')"><u> ${bundleTeam?.rerackedAssets}</u> </a> / 
										<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','')"><u>${bundleTeam?.targetAssets}</u></a>
										<jsec:lacksAllRoles in="['MANAGER','OBSERVER']"> 
										<a title="Relogin as.." href="#" onclick="window.open('../moveTech/login?username=mt-${moveBundleInstance.id}-${bundleTeam?.team?.id}-t','mtwindow','menubar=1,resizable=1,width=320,height=480'); ">@</a>
										</jsec:lacksAllRoles>
									</td>
								</tr>
							</table>
							</td>
						</g:each>
						<td style="padding: 0px; border-right: 1px solid #333333">
						<table style="border: 0;">
							<th nowrap>Cleaning &nbsp;</th>
							<tr>
								<td nowrap>${supportTeam?.cleaningMembers}&nbsp;</td>
							</tr>
							<tr>
								<td nowrap class="odd">${supportTeam?.cleaning?.currentLocation}&nbsp;</td>
							</tr>
							<tr>
								<td nowrap>
									<g:if test="${supportTeam?.cleaning?.latestAsset}">
									<a href="#" onclick="assetDetails('${supportTeam?.cleaning?.latestAsset?.id}')"> ${supportTeam?.cleaning?.latestAsset?.assetTag}</a>
									</g:if>&nbsp;
								</td>
							</tr>
							<tr>
								<td nowrap class="odd">
								<a href="#" onclick="filterByDataPoints('source','','source_pend_clean')"><u> ${supportTeam.sourcePendCleaned}</u> </a> /
								<a href="#" onclick="filterByDataPoints('source','','source_avail_clean')"><u> ${supportTeam.sourceAvailCleaned}</u> </a> /
								<a href="#" onclick="filterByDataPoints('source','','source_done_clean')"><u> ${supportTeam.sourceCleaned}</u> </a>/
								<a href="#" onclick="filterByDataPoints('source','','')"><u> ${supportTeam.totalAssets}</u> </a> 
								<jsec:lacksAllRoles in="['MANAGER','OBSERVER']"> 
									<a title="Relogin as.." href="../moveTech/login?username=ct-${moveBundleInstance.id}-${supportTeam.cleaning?.id}-t">@</a>
								</jsec:lacksAllRoles> 
								</td>
							</tr>
							<tr>
								<td nowrap>N/A</td>
							</tr>
						</table>
						</td>
						<td style="padding: 0px;">
						<table style="border: 0;">
							<th nowrap>Transport &nbsp;</th>
							<tr>
								<td nowrap>${supportTeam?.transportMembers}&nbsp;</td>
							</tr>
							<tr>
								<td nowrap class="odd">${supportTeam?.transport?.currentLocation}&nbsp;</td>
							</tr>
							<tr>
								<td nowrap>&nbsp;</td>
							</tr>
							<tr>
								<td nowrap class="odd">
								<a href="#" onclick="filterByDataPoints('source','','source_pend_trans')"><u> ${supportTeam.sourceTransportPend}</u> </a> /
								<a href="#" onclick="filterByDataPoints('source','','source_avail_trans')"><u> ${supportTeam.sourceTransportAvail}</u> </a> /
								<a href="#" onclick="filterByDataPoints('source','','source_done_trans')"><u> ${supportTeam.sourceMover}</u> </a> /
								<a href="#" onclick="filterByDataPoints('source','','')"><u> ${supportTeam.totalAssets}</u> </a> 
								</td>
							</tr>
							<tr>
								<td nowrap>
								<a href="#" onclick="filterByDataPoints('target','','target_pend_trans')"><u> ${supportTeam.targetTransportPend}</u> </a> /
								<a href="#" onclick="filterByDataPoints('target','','target_avail_trans')"><u> ${supportTeam.targetTransportAvail}</u> </a> /
								<a href="#" onclick="filterByDataPoints('target','','target_done_trans')"><u> ${supportTeam.targetMover}</u> </a> /
								<a href="#" onclick="filterByDataPoints('target','','')"><u> ${supportTeam.totalAssets}</u> </a> 
								</td>
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
						<td nowrap class="odd">
							<a href="#" onclick="filterByDataPoints('source','','source_pend')"><u> ${totalSourcePending}</u> </a> /
							<a href="#" onclick="filterByDataPoints('source','','source_avail')"><u> ${totalSourceAvail}</u> </a> /
							<a href="#" onclick="filterByDataPoints('source','','source_done')"><u> ${totalUnracked}</u> </a> / 
							<a href="#" onclick="filterByDataPoints('source','','')"><u>${totalAsset}</u> </a>
						</td>
					</tr>
					<tr>
						<td nowrap>
							<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_pend')"><u>${totalTargetPending}</u></a> /
							<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_avail')"><u>${totalTargetAvail}</u></a> /
							<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_done')"><u>${totalReracked}</u></a> / 
							<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','')"><u>${totalAsset}</u> </a>
						</td>
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
							<tr	id="rowId" onmouseover="$('#rowId').css('background','white');">
								<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJ_MGR']">
								<td id="tdId"><input id="state" type="button"
									value="State..." onclick="changeState()" title="Change State" />
								</td>
								</jsec:hasAnyRole>
								<td style="vertical-align: middle;" colspan="3">
									<label for="showAllCheckbox"><input type="checkbox" onclick="showAll()" id="showAllCheckbox"/>&nbsp;Show All&nbsp;</label>
									&nbsp;&nbsp;<input type="button" onclick="showfilterDialog()" id="filterButtonId" value="Filter"/>
									<g:if test="${params.application || params.appOwner || params.appSme || params.currentState || params.assetLocation || params.assetStatus }">
									&nbsp;<a href="#"
											 onclick="document.filterForm.reset();$('#filterShowAllId').val('');document.filterForm.submit();"><span class="clear_filter"><u>X</u></span></a>
									</g:if>
									<g:if test="${totalAssetsOnHold > 0}">
									&nbsp;&nbsp;<input type="button" class="onhold_button" onclick="submitFormWithBundle()" id="onHoldButtonId" value="On Hold (${totalAssetsOnHold})"/>
									</g:if>
								</td>
							</tr>
							<tr>
								<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJ_MGR']">
									<th>Actions <jsec:hasAnyRole in="['ADMIN','PROJ_MGR']"><a href="#" onclick="selectAll()"><u
									style="color: blue;">All</u></a></jsec:hasAnyRole></th></jsec:hasAnyRole>
								<g:sortableColumn property="priority" title="Priority" 
									params='["projectId":projectId,"moveBundle":moveBundleInstance.id,"showAll":showAll,
											application:params.application,appOwner:params.appOwner,appSme:params.appSme,
											currentState:params.currentState,team:params.team,assetLocation:params.assetLocation,
											assetStatus:params.assetStatus]'/>
								<g:sortableColumn property="assetTag" title="Asset Tag" 
									params='["projectId":projectId,"moveBundle":moveBundleInstance.id,"showAll":showAll,
											application:params.application,appOwner:params.appOwner,appSme:params.appSme,
											currentState:params.currentState,team:params.team,assetLocation:params.assetLocation,
											assetStatus:params.assetStatus]'/>
								<g:sortableColumn property="assetName" title="Asset Name" 
									params='["projectId":projectId,"moveBundle":moveBundleInstance.id,"showAll":showAll,
											application:params.application,appOwner:params.appOwner,appSme:params.appSme,
											currentState:params.currentState,team:params.team,assetLocation:params.assetLocation,
											assetStatus:params.assetStatus]'/>
								<g:sortableColumn property="currentState" title="Status" 
									params='["projectId":projectId,"moveBundle":moveBundleInstance.id,"showAll":showAll,
											application:params.application,appOwner:params.appOwner,appSme:params.appSme,
											currentState:params.currentState,team:params.team,assetLocation:params.assetLocation,
											assetStatus:params.assetStatus]'/>
								<g:sortableColumn property="sourceTeam" title="Source Team" 
									params='["projectId":projectId,"moveBundle":moveBundleInstance.id,"showAll":showAll,
											application:params.application,appOwner:params.appOwner,appSme:params.appSme,
											currentState:params.currentState,team:params.team,assetLocation:params.assetLocation,
											assetStatus:params.assetStatus]'/>
								<g:sortableColumn property="targetTeam" title="Target Team" 
									params='["projectId":projectId,"moveBundle":moveBundleInstance.id,"showAll":showAll,
											application:params.application,appOwner:params.appOwner,appSme:params.appSme,
											currentState:params.currentState,team:params.team,assetLocation:params.assetLocation,
											assetStatus:params.assetStatus]'/>
								<th>Issues</th>
							</tr>
						</thead>
						<tbody id="assetsTbody">
						<g:if test="${assetsList}">
							<g:each status="i" in="${assetsList}" var="assetsList">

								<tr name="assetDetailRow"
									id="assetDetailRow_${assetsList?.asset.id}"
									class="${assetsList?.cssClass}" value="${assetsList?.asset.id}">

									<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJ_MGR']">
									<td><jsec:hasAnyRole in="['ADMIN','PROJ_MGR']">
										<g:if test="${assetsList.checkVal == true}">
										<span id="spanId_${assetsList?.asset.id}">
											<g:checkBox name="checkChange"
												id="checkId_${assetsList?.asset.id}"
												onclick="timedRefresh('never')"></g:checkBox>
										</span>

										</g:if>
									</jsec:hasAnyRole>
									<g:remoteLink controller="assetEntity" action="editShow" id="${assetsList?.asset.id}" before="document.showForm.id.value = ${assetsList?.asset.id};document.editForm.id.value = ${assetsList?.asset.id};" onComplete="showAssetDialog( e , 'show');">
										<img src="${createLinkTo(dir:'images',file:'asset_view.png')}" border="0px">
									</g:remoteLink>
									 <span style="visibility: hidden;"
										id="image_${assetsList?.asset.id}"><img
										src="${createLinkTo(dir:'images',file:'row_arrow.gif')}"
										border="0px"></span>
										
										</td></jsec:hasAnyRole>
									<td id="priority_${assetsList?.asset.id}"
										onclick="assetDetails('${assetsList?.asset.id}')">${assetsList?.asset.priority}</td>
									<td id="assetTag_${assetsList?.asset.id}" onclick="assetDetails('${assetsList?.asset.id}')">${assetsList?.asset.assetTag}</td>
									<td  id="assetName_${assetsList?.asset.id}" onclick="assetDetails('${assetsList?.asset.id}')">${assetsList?.asset.assetName}</td>
									<td onclick="assetDetails('${assetsList?.asset.id}')"
										id="statusCol_${assetsList?.asset.id}">${assetsList?.status}</td>
									<td onclick="assetDetails('${assetsList?.asset.id}')"
										id="source_${assetsList?.asset.id}"><g:if test="${assetsList?.asset.sourceTeam}">${ProjectTeam.findById(assetsList?.asset.sourceTeam)?.name}</g:if></td>
									<td onclick="assetDetails('${assetsList?.asset.id}')"
										id="target_${assetsList?.asset.id}"><g:if test="${assetsList?.asset.targetTeam}"> ${ProjectTeam.findById(assetsList?.asset.targetTeam)?.name}</g:if></td>
									<td id="icon_${assetsList?.asset.id}"><g:if
										test="${AssetComment.find('from AssetComment where assetEntity = '+ assetsList?.asset?.id +' and commentType = ? and isResolved = ?',['issue',0])}">
										<g:remoteLink controller="assetEntity" action="listComments"
											id="${assetsList?.asset.id}"
											before='setAssetId(${assetsList?.asset.id});'
											onComplete="listCommentsDialog( e ,'never' );">
											<img
												src="${createLinkTo(dir:'images/skin',file:'database_table_red.png')}"
												border="0px">
										</g:remoteLink>
									</g:if>
									<g:elseif test="${AssetComment.find('from AssetComment where assetEntity = '+ assetsList?.asset?.id)}">
										<g:remoteLink controller="assetEntity" action="listComments" id="${assetsList?.asset.id}" before="setAssetId(${assetsList?.asset.id});" onComplete="listCommentsDialog( e ,'never' ); ">
											<img src="${createLinkTo(dir:'images/skin',file:'database_table_bold.png')}" border="0px">
										</g:remoteLink>
									</g:elseif>
									<g:else>
									<a onclick="createNewAssetComment(${assetsList?.asset.id});">
										<img src="${createLinkTo(dir:'images/skin',file:'database_table_light.png')}" border="0px">
									</a>
									</g:else>
									</td>
								</tr>
							</g:each>
							</g:if>
						<g:else>
							<tr><td colspan="8" class="no_records">No records found</td></tr>
						</g:else>
						</tbody>
					</table>

				</g:form></div>
				</td>
				<td valign="top" style="padding: 0px;width:250px;">
				<div id="floatMenu" style="position:relative;">
				<div id="assetDetails"
					style="border: 1px solid #5F9FCF; width: 250px;">
				<div id="asset">
				<table style="border: 0px;width: 250px;" cellpadding="0" cellspacing="0">
					<thead>
						<tr>
							<th colspan="2">Asset Details</th>
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
							<td><b>Src Rack: </b></td>
						</tr>
						<tr>
							<td><b>Tgt Rack: </b></td>
						</tr>
						<tr>
							<td><b>Status: </b></td>
						</tr>
						<tr>
							<td><b>Recent Changes: </b></td>
						</tr>
					</tbody>
				</table>
				</div>
				<div>
				<input type="hidden" name="asset" id="assetId" value="">
				<input type="hidden" name="currentState" id="currentStateId" value="">
				<input type="hidden" value="" id="validateCommentId" name="validateComment">
				<g:form name="assetdetailsForm">
					<table style="border: 0">
						<tbody>
							<tr>
								<td><b>Change:</b></td>
								<td><select id="stateSelectId" name="state"
									style="width: 100px"
									onchange="${remoteFunction(action:'getFlag', params:'\'toState=\'+ this.value +\'&fromState=\'+$(\'#currentStateId\').val()', onComplete:'setComment(e)')}">
									<option value="">Status</option>
								</select></td>
							</tr>
							<tr>
								<td>
								&nbsp;</td>
								<td><g:select id="priorityId" name="priority"
									from="${AssetEntity.constraints.priority.inList}"
									style="width: 100px" noSelection="['':'Priority ']"></g:select>
								</td>
							</tr>
							<tr>
								<td>&nbsp;</td>
								<td><select id="assignToId" name="assignTo"
									style="width: 100px">
									<option value="">Move Team</option>
									<optgroup label="Source" id="sourceAssignTo"></optgroup>
									<optgroup label="Target" id="targetAssignTo"></optgroup>
								</select></td>
							</tr>
							<tr>
								<td colspan="2" style="text-align: center;">
									<textarea name="comment" id="commentId" cols="25" rows="2"></textarea></td>
							</tr>
							<tr>
								<td colspan="2" style="text-align: center;" class="buttonR">
								<input type="button" value="Cancel"
									onclick="resetAssetDetails()">
								<input type="button" value="Submit"
									onclick="setCommentValidation();${remoteFunction(action:'createTransition', params:'\'asset=\' + $(\'#assetId\').val() +\'&state=\'+ $(\'#stateSelectId\').val() +\'&priority=\'+ $(\'#priorityId\').val() +\'&assignTo=\'+$(\'#assignToId\').val() +\'&comment=\'+$(\'#commentId\').val() ', onComplete:'updateAsset(e)')}" /></td>
							</tr>
						</tbody>
					</table>
				</g:form></div>
				</div></div>
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</div>
<div id="commentsListDialog" title="Show Asset Comments"
	style="display: none;" ><br>
<div class="list">
<table id="listCommentsTable">
	<thead>
		<tr>
			<g:if test="${role}">
			<th nowrap>Action</th>
			</g:if>
			
			<th nowrap>Comment</th>

			<th nowrap>Comment Type</th>
			
			<th nowrap>Resolved</th>

			<th nowrap>Must Verify</th>
			
			<th nowrap>Category</th>  
	          
	        <th nowrap>Comment Code</th> 

		</tr>
	</thead>
	<tbody id="listCommentsTbodyId">

	</tbody>
</table>
</div>
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
<span class="menuButton"><a class="create" href="#"
	onclick="$('#statusId').val('');$('#createResolveDiv').hide();$('#createCommentDialog').dialog('option', 'width', 700);$('#createCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();">New
Comment</a></span></div>
</div>
<div id="createCommentDialog" title="Create Asset Comment"
	style="display: none;"><input type="hidden" name="assetEntity.id"
	id="createAssetCommentId" value=""> <input type="hidden"
	name="status" id="statusId" value=""> <g:form
	action="saveComment" method="post" name="createCommentForm">
	<input type="hidden" name="category" value="moveday"/>
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<div>
	<table id="createCommentTable" style="border: 0px">
		
			<tr class="prop" >
				<td valign="top" class="name"><label for="commentType">Comment
				Type:</label></td>
				<td valign="top" style="width: 20%;" ><g:select id="commentType"
					name="commentType"
					from="${AssetComment.constraints.commentType.inList}" value=""
					noSelection="['':'please select']" onChange="commentChange('#createResolveDiv','createCommentForm')"></g:select>&nbsp;&nbsp;&nbsp;&nbsp;			
				
				<input type="checkbox"
					id="mustVerifyEdit" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
					<label for="mustVerify">Must Verify</label>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="comment" name="comment"></textarea></td>
			</tr>
		
	</table>
	</div>
	<div id="createResolveDiv" style="display: none;">
		<table id="createResolveTable" style="border: 0px" >
            <tr class="prop">
            	<td valign="top" class="name">
                <label for="isResolved">Resolved:</label>
                </td>
                <td valign="top" class="value">
                <input type="checkbox" id="isResolved" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
                </td>
            </tr>
          
            <tr class="prop">
				<td valign="top" class="name">
                <label for="resolution">Resolution:</label>
                </td>
				<td valign="top" class="value">
                <textarea cols="80" rows="5" id="resolution" name="resolution" ></textarea>
                </td>
            </tr> 
                
            </table>
            </div>
		
	</div>
	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Create"
		onclick="resolveValidate('createCommentForm','createAssetCommentId');" /></span></div>
</g:form></div>
<div id="showCommentDialog" title="Show Asset Comment"
	style="display: none;">
<div class="dialog" style="border: 1px solid #5F9FCF"><input name="id" value="" id="commentId"
	type="hidden">
	<div>
<table id="showCommentTable" style="border: 0px">
	
	<tr>
	<td valign="top" class="name"><label for="dateCreated">Created
			At:</label></td>
			<td valign="top" class="value" id="dateCreatedId" />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="createdBy">Created
			By:</label></td>
			<td valign="top" class="value" id="createdById" />
	</tr>
		
		<tr class="prop">
			<td valign="top" class="name"><label for="commentType">Comment
			Type:</label></td>
			<td valign="top" class="value" id="commentTypeTdId" />
		</tr>
		
		<tr>
	<td valign="top" class="name"><label for="category">Category:
			</label></td>
			<td valign="top" class="value" id="categoryTdId" />
	</tr>
	<tr class="prop">
	<td valign="top" class="name"><label for="commentCode">comment
			Code:</label></td>
			<td valign="top" class="value" id="commentCodeTdId" />
	</tr>
	
		<tr class="prop">
			<td valign="top" class="name"><label for="mustVerify">Must
			Verify:</label></td>
			<td valign="top" class="value" id="verifyTdId"><input
				type="checkbox" id="mustVerifyShowId" name="mustVerify" value="0"
				disabled="disabled" /></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="comment">Comment:</label>
			</td>
			<td valign="top" class="value" ><textarea cols="80" rows="5"
					id="commentTdId" readonly="readonly"></textarea> </td>
		</tr>
		</table>
		</div>
		<div id="showResolveDiv" style="display: none;">
		<table id="showResolveTable" style="border: 0px">
		<tr class="prop">
			<td valign="top" class="name"><label for="isResolved">Is
			Resolved:</label></td>
			<td valign="top" class="value" id="resolveTdId"><input
				type="checkbox" id="isResolvedId" name="isResolved" value="0"
				disabled="disabled" /></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label>
			</td>
			<td valign="top" class="value" ><textarea cols="80" rows="5"
					id="resolutionId" readonly="readonly"></textarea> </td>
		</tr>
			<tr>
	<td valign="top" class="name"><label for="dateResolved">Resolved
			At:</label></td>
			<td valign="top" class="value" id="dateResolvedId" />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="resolvedBy">Resolved
			By:</label></td>
			<td valign="top" class="value" id="resolvedById" />
	</tr>
	
</table>
</div>
 <div class="buttons"><span class="button"> <input
	class="edit" type="button" value="Edit"
	onclick="commentChangeEdit('editResolveDiv','editCommentForm');$('#editCommentDialog').dialog('option', 'width', 700);$('#editCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('open');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close')" />
</span> <span class="button"> <input class="delete" type="button"
	value="Delete"
	onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment', params:'\'id=\' + $(\'#commentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e ,\'never\')')}" />
</span> </div>
</div>
</div>

<div id="editCommentDialog" title="Edit Asset Comment"
	style="display: none;"><g:form action="updateComment"
	method="post" name="editCommentForm">
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<input type="hidden" name="id" id="updateCommentId" value="">
	<div>
	<table id="updateCommentTable" style="border: 0px">
		
		
			<tr>
	<td valign="top" class="name"><label for="dateCreated">Created
			At:</label></td>
			<td valign="top" class="value" id="dateCreatedEditId"  />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="createdBy">Created
			By:</label></td>
			<td valign="top" class="value" id="createdByEditId" />
	</tr>
			<tr class="prop" >
				<td valign="top" class="name"><label for="commentType">Comment
				Type:</label></td>
				<td valign="top" style="width: 20%;" >
				<jsec:hasAnyRole in="['ADMIN','PROJ_MGR']">
				<g:select id="commentTypeEditId"
					name="commentType"
					from="${AssetComment.constraints.commentType.inList}" value=""
					 onChange="commentChange('#editResolveDiv','editCommentForm')"></g:select>&nbsp;&nbsp;&nbsp;&nbsp;			
				</jsec:hasAnyRole>
				<jsec:lacksAllRoles in="['ADMIN','PROJ_MGR']">
				
				<input type="text" id="commentTypeEditId" name="commentType" readonly style="border: 0;">&nbsp;&nbsp;&nbsp;&nbsp;
				</jsec:lacksAllRoles>					
				
				<input type="checkbox"
					id="mustVerifyEditId" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
					<label for="mustVerify">Must
				Verify</label>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="category">Category:</label>
				</td>
				<td valign="top" class="value"	id="categoryEditId" />
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="commentCode">Comment Code:</label>
				</td>
				<td valign="top" class="value" id="commentCodeEditId" />
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="commentEditId" name="comment"></textarea></td>
			</tr>
			</table>
			
			</div>
			<div id="editResolveDiv" style="display: none;">
		<table id="updateResolveTable" style="border: 0px">
            <tr class="prop">
            	<td valign="top" class="name">
                <label for="isResolved">Resolved:</label>
                </td>
                <td valign="top" class="value">
                <input type="checkbox" id="isResolvedEditId" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
                </td>
            </tr>
          
            <tr class="prop">
				<td valign="top" class="name">
                <label for="resolution">Resolution:</label>
                </td>
				<td valign="top" class="value">
                <textarea cols="80" rows="5" id="resolutionEditId" name="resolution" ></textarea>
                </td>
            </tr> 
               <tr>
	<td valign="top" class="name"><label for="dateResolved">Resolved
			At:</label></td>
			<td valign="top" class="value" id="dateResolvedEditId" />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="resolvedBy">Resolved
			By:</label></td>
			<td valign="top" class="value" id="resolvedByEditId"  />
	</tr>
            </table>
            </div>
		
		

	</div>

	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Update"
		onclick="resolveValidate('editCommentForm','updateCommentId');" />
	</span> <span class="button"> <input class="delete" type="button"
		value="Delete"
		onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment', params:'\'id=\' + $(\'#updateCommentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'never\')')}" />
	</span></div>
</g:form></div>
<div id="showDialog" title="Show Asset Entity" style="display: none;">
<g:form action="save" method="post" name="showForm">
	<div class="dialog" id="showDiv">
	
	</div>
	<div class="buttons">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundle" value="${moveBundleInstance.id}" />
	 <span class="button"><input
		type="button" class="edit" value="Edit"
		onClick="return editAssetDialog()" /></span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Delete Asset, are you sure?');"
		value="Delete" /></span>
		<span class="button"><g:actionSubmit action="remove"
		class="delete"  onclick="return confirm('Remove Asset from project, are you sure?');"
		value="Remove From Project" /></span>
		</div>
</g:form></div>

<div id="editDialog" title="Edit Asset Entity" style="display: none;">
<g:form method="post" name="editForm">
	<input type="hidden" name="id" id="editFormId" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundle" value="${moveBundleInstance.id}" />
	<div class="dialog" id="editDiv">
	
	</div>
	<div class="buttons"><span class="button">
	<input type="button" class="save" value="Update Asset Entity" onClick="${remoteFunction(action:'getAssetAttributes', params:'\'assetId=\' + $(\'#editFormId\').val() ', onComplete:'callUpdateDialog(e)')}" />
	</span> <span class="button"><input type="button"
		class="delete" onclick="return editDialogDeleteRemove('delete')"
		value="Delete" /></span>
		<span class="button"><input type="button"
		class="delete"  onclick="return editDialogDeleteRemove('remove');"
		value="Remove From Project" /></span>
		</div>
</g:form></div>

<div id="filterDialog" title="Filter" style="display: none;">
	<g:form name="filterForm" action="dashboardView">
		<input type="hidden" name="projectId" value="${projectId}" />
		<input type="hidden" name="moveBundle" value="${moveBundleInstance.id}" />
		<input type="hidden" name="showAll" id="filterShowAllId" value="${showAll}">
		<input type="hidden" name="myForm" value="filterForm">
		<table>
			<tr>
				<td>Application : </td>
				<td >
					<select name="application" id="filterApplicationId" style="width: 120px;">
						<option value="" selected="selected">All</option>
						<g:each in="${applicationList}" var="application">
							<option value="${application[0] ? application[0] : 'blank'}">${application[0] ? application[0] : 'blank'}&nbsp;(${application[1]})</option>
						</g:each>
					</select>	
				</td>
			</tr>
			<tr>
				<td>App Owner : </td>
				<td >
					<select name="appOwner" id="filterAppOwnerId" style="width: 120px;">
						<option value="" selected="selected">All</option>
						<g:each in="${appOwnerList}" var="appOwner">
							<option value="${appOwner[0] ? appOwner[0] : 'blank'}">${appOwner[0] ? appOwner[0] : 'blank'}&nbsp;(${appOwner[1]})</option>
						</g:each>
					</select>
				</td>
			</tr>
			<tr>
				<td>App SME : </td>
				<td >
					<select name="appSme" id="filterAppSmeId" style="width: 120px;">
						<option value="" selected="selected">All</option>
						<g:each in="${appSmeList}" var="appSme">
							<option value="${appSme[0] ? appSme[0] : 'blank'}">${appSme[0] ? appSme[0] : 'blank'}&nbsp;(${appSme[1]})</option>
						</g:each>
					</select>
				</td>
			</tr>
			<tr>
				<td>Current State : </td>
				<td >
					<select name="currentState" id="filterStateId" style="width: 120px;" >
						<option value="" selected="selected">All</option>
						<g:each in="${transitionStates}" var="transitionState">
							<option value="${transitionState.state}">${transitionState.stateLabel}</option>
						</g:each>
					</select>
				</td>
			</tr>
			<tr>
				<td colspan="2" style="text-align: center;"><input type="reset" value="Cancel" onclick="$('#filterDialog').dialog('close');">
				<input type="reset" value="Clear">
				<input type="submit" value="Apply" onclick="$('#filterShowAllId').val('show')">				
				</td>
			</tr>
		</table>
	</g:form>
</div>

<script type="text/javascript">
bundleChange();
$("#midDiv").css('width',vpWidth() - 340)
timedRefresh($("#selectTimedId").val())
</script>
</div>
</body>
</html>
