<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Supervisor Console</title>
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />

<g:javascript src="asset.tranman.js" />

<style type="text/css">
td .odd {
	background: #DDDDDD;
	nowrap
}
</style>
<link rel="stylesheet" type="text/css" href="${createLinkTo(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
<script language="javascript" src="${createLinkTo(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
<script type="text/javascript">
function onInvokeAction(id) {
    setExportToLimit(id, '');
    createHiddenInputFieldsForLimitAndSubmit(id);
}
</script>
<script type="text/javascript">
	$(document).ready(function() {
	    $("#showDialog").dialog({ autoOpen: false })
	    $("#editDialog").dialog({ autoOpen: false })
		$("#commentsListDialog").dialog({ autoOpen: false })
	    $("#createCommentDialog").dialog({ autoOpen: false })
	    $("#showCommentDialog").dialog({ autoOpen: false })
	    $("#editCommentDialog").dialog({ autoOpen: false })
	    $("#showChangeStatusDialog").dialog({ autoOpen: false })
	    $('#filterDialog').dialog({ autoOpen: false })
	    $("#manufacturerShowDialog").dialog({ autoOpen: false })
	    $("#modelShowDialog").dialog({ autoOpen: false })
	    $( '#floatMenu' ).scrollFollow({
		    speed: 50
		});
	})
</script>
<script type="text/javascript">

	function editAssetDialog() {
		timedUpdate('never')
		$("#showDialog").dialog("close")
		$("#editDialog").dialog('option', 'width', 'auto')
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
			timedUpdate($("#selectTimedId").val())
		} else {
			alert("Asset is not updated, Please check the required fields")
		}
	} 		

	function showChangeStatusDialog(e){
		timedUpdate('never')
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
			timedUpdate($("#selectTimedId").val())
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
	   	$("span.selected").hide()
	   	$('#image_'+assetId).show()
	   	timedUpdate('never')
	   	${remoteFunction(action:'assetDetails', params:'\'assetId=\'+ assetId ' , onComplete:'getAssetDetail(e)') }
	}
   	function getAssetDetail(e){
	   	var asset = eval("(" + e.responseText + ")")
	    var tableBody = '<table style=\'border:0\' cellpadding=\'0\' cellspacing=\'0\' ><thead><tr><th colspan="2">Asset Details </th></tr></thead><tbody>'+
		'<tr><td><b>Name: </b>'+asset[0].assetDetails.assetDetail.assetName+'</td></tr>'+
		'<tr><td><b>Model: </b>'+asset[0].assetDetails.model+'</td></tr>'+
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
	   	createAssighToOptions(asset[0].sourceTeamMts,asset[0].targetTeamMts)
	   	document.assetdetailsForm.reset();
		if(asset[0].assetDetails.currentState == "Hold"){
		   	$("#setHoldTimerTr").show()
		   	$("#holdTimeId").val(asset[0].holdTimer)
	   	} else {
	   		$("#setHoldTimerTr").hide()
	   	}
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
	   	$("#holdTimeId").val("");
	   	$("span.selected").hide()
	   	var seconds = new Date().getTime() - $("#lastUpdateId").val();
	   	var updateTime = $('#selectTimedId').val() - seconds
	   	if( !isNaN(updateTime) ){
	   		timedUpdate( updateTime )
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
   	function createAssighToOptions(sourceTeamMts,targetTeamMts){
   		var teamObj = $("#assignToId")
	   	var sourceObj = $("#sourceAssignTo")
	   	var targetObj = $("#targetAssignTo")
	   	sourceObj.html("")
	   	targetObj.html("")
		var sourceLength = sourceTeamMts.length
	    for (var i=0; i < sourceLength; i++) {
	      var team = sourceTeamMts[i]
	      var popt = document.createElement('option');
		  popt.innerHTML = team.name
	      popt.value = "s/"+team.id
	      try {
	      sourceObj.append(popt, null) // standards compliant; doesn't work in IE
	      } catch(ex) {
	      sourceObj.append(popt) // IE only
	      }
		}
		var targetLength = targetTeamMts.length
	    for (var i=0; i < targetLength; i++) {
	      var team = targetTeamMts[i]
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
	   var time = '${timeToUpdate}';
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
   	   	var returnVal = true;
   	   	var holdTime = $("#holdTimeId").val()
	   	if($("#validateCommentId").val() == 'true' || $("#stateSelectId").val() == 'Hold' || holdTime ){
	   		if($("#commentId").val() == ''){
	   			alert("A comment is required")
	   			returnVal = false;
	   		} else if( holdTime ) {
	   			var objRegExp  = /^(0[1-9]|1[012])[/](0[1-9]|[12][0-9]|3[01])[/](19|20)\d\d ([0-1][0-9]|[2][0-3])(:([0-5][0-9])){1,2} ([APap][Mm])$/;
	   	      	if( !objRegExp.test(holdTime) ){
	   	      	 	$("#holdTimeId").addClass("field_error");
	   	          	alert("Date should be in 'mm/dd/yyyy HH:MM AM/PM' format");
	   	          	returnVal  =  false;
	   	      	}
	   		}
	   	}
	   	if( returnVal ) $("#holdTimeId").removeClass("field_error");
	   	return returnVal;
   	}
   	var timer
   	function timedUpdate(timeoutPeriod) {
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
	function setUpdateTime(e) {
		var timeUpdate = eval("(" + e.responseText + ")")
		if(timeUpdate){
			timedUpdate(timeUpdate[0].updateTime.SUPER_CONSOLE_REFRESH)
		}
	}
	function updateAsset(e){
		var asset = eval("(" + e.responseText + ")")
		if(asset[0]){
			createStateOptions(asset[0].statesList)
			createAssighToOptions(asset[0].sourceTeamMts,asset[0].targetTeamMts)
			if(asset[0].checkVal == false){
				var spanEle = $('#spanId_'+asset[0].assetEntity.id);
				spanEle.html("&nbsp;&nbsp;&nbsp;");
			}		

			$('#priority_'+asset[0].assetEntity.id).html( asset[0].assetEntity.priority )
			$('#statusCol_'+asset[0].assetEntity.id).html( asset[0].status )
			$('#source_'+asset[0].assetEntity.id).html( asset[0].sourceTeamMt )
			$('#target_'+asset[0].assetEntity.id).html( asset[0].targetTeamMt )
			$('#assetDetailRow_'+asset[0].assetEntity.id).removeAttr( "class" ) ;
			$('#assetDetailRow_'+asset[0].assetEntity.id).addClass(asset[0].cssClass)
			if(asset[0].assetComment != null){
				var link = document.createElement('a');
				link.href = '#'
				link.onclick = function(){$('#createAssetCommentId').val( asset[0].assetEntity.id ) ;new Ajax.Request('listComments?id='+asset[0].assetEntity.id,{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog(e,'never');}})} //;return false
				link.innerHTML = "<img src=\"../i/db_table_red.png\" border=\"0px\" />"
				var iconObj = $('#icon_'+asset[0].assetEntity.id);
				iconObj.html(link)
			}
			$("#currentStateId").val( asset[0].statusName )
			$("#priorityId").val("");
			$("#commentId").val("")
			$("#holdTimeId").val("")
			$('#statusCol_'+asset[0].assetEntity.id).click();
		}
		timedUpdate($('#selectTimedId').val());
	}
	
	var isFirst = true;
	function selectAll(){
		timedUpdate('never')
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
		timedUpdate('never')
		var assetArr = new Array();
		var totalAsset = ${assetBeansList?.id};
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
	
	function showAllAssets(){
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
		timedUpdate('never');
		$('#statusId').val('new');
		$('#createCommentDialog').dialog('option', 'width', 'auto');
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
		timedUpdate('never')
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
	function submitFormWithBundle( showAll ){
		$("#showAllId").val( showAll );
		$("#teamId").val('');
		$("#assetLocationId").val('');
		$("#assetStatusId").val('');
		$("form#dashboardForm").submit();
	}
	function vpWidth() {
		return window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
	}
	<%--
	/*------------------------------------------
	 * function to call ConsoleController.invokeSnapshot
	 *-----------------------------------------*/
	function createSnapshot(){
		var moveBundle = $("#moveBundleId").val()
		$('#createSnapshotId').attr('disabled', 'true');
		jQuery.ajax({
	        type:"GET",
	        async : true,
	        cache: false,
	        url:"../console/invokeSnapshot?moveBundle="+moveBundle,
	        success:function(e){$('#createSnapshotId').removeAttr("disabled");}
		});
	} --%>
    </script>
</head>

<body>

<div title="Change Status" id="showChangeStatusDialog"
	style="background-color: #808080; display: none;">
<form name="changeStatusForm" method="post">
	<input type="hidden" name="assetVal" id="assetVal" />
	<input type="hidden" name="projectId" id="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundle" id="moveBundle" value="${moveBundleInstance.id}" />
	<input type="hidden" name="showAll" id="showAllInChangeStatus" />
	<input type="hidden" id="role" value="${role}"/>
		<table style="border: 0px; width: 100%">
	<tr>
		<td width="40%"><strong>Change status for selected devices to:</strong></td>
		<td width="60%"></td>
	</tr>
	<tr>
		<td><select id="taskList" name="taskList" style="width: 250%">

		</select></td>
	</tr>
	<tr>
		<td><textarea rows="2" cols="1" title="Enter Note..."
			name="enterNote" id="enterNote" style="width: 200%" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea></td>
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
	<input type="hidden" name="projectId" value="${projectId}"/>
	<input type="hidden" name="showAll" id="showAllId"/>
	<input type="hidden" name="team" id="teamId" value="${params.team}"/>
	<input type="hidden" name="assetLocation" id="assetLocationId" value="${params.assetLocation}"/>
	<input type="hidden" name="assetStatus" id="assetStatusId" value="${params.assetStatus}"/>
	<input type="hidden" name="myForm" value="dashboardForm"/>
	<div class="dialog">
	<table style="border: 0px;">
		<tr class="prop">
			<td style="vertical-align: bottom;width:30%" class="name"><label
				for="moveBundle">Move Bundle:</label>&nbsp;<select id="moveBundleId"
				name="moveBundle" onchange="submitFormWithBundle('show')">

				<g:each status="i" in="${moveBundleInstanceList}"
					var="moveBundleInstance">
					<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
				</g:each>

			</select>
			</td>
			<td style="vertical-align: bottom;width:100px;" class="name">
				<label for="teamType">Team Types:</label>&nbsp;
				<g:select name="teamType" from="${['MOVE', 'ADMIN']}" id="teamTypeId" valueMessagePrefix="bundle.teamType" value="${teamType}" onchange="submitFormWithBundle('show')"/>
			</td>
			<td style="width:40%">
			<h1 align="center">Supervisor Console</h1>
			</td>
			<td style="text-align: right; vertical-align: bottom;width:30%">
			<input type="hidden" id="lastUpdateId" name="lastUpdate" value="${new Date().getTime()}"/>
			<input type="button" value="Update:" onclick="pageReload();"/>
			<select id="selectTimedId"
				onchange="${remoteFunction(action:'setTimePreference', params:'\'timer=\'+ this.value ' , onComplete:'setUpdateTime(e)') }">
				<option value="30000">30s</option>
				<option value="60000">1m</option>
				<option value="120000">2m</option>
				<option value="300000">5m</option>
				<option value="600000">10m</option>
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
					style="border-right: 1px solid #333333; padding: 0px; width: 100px;">
				<table style="border: 0; width: 100%;">
					<tr><th>TEAMS:</th>
					</tr>
					<tr>
						<td>Names</td>
					</tr>
					<tr>
						<td class="odd">Location</td>
					</tr>
					<tr>
						<td nowrap="nowrap">Latest Asset</td>
					</tr>
					<tr>
						<td class="odd" nowrap>Source </td>
					</tr>
					<tr>
						<td nowrap>Target </td>
					</tr>
				</table>
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
								<tr><th nowrap class="teamstatus_${bundleTeam.headColor}" style="border: 1px solid #ccc;">${bundleTeam?.team?.name }&nbsp;</th>
								<tr>
								<tr>
									<td nowrap>${bundleTeam?.members}&nbsp;</td>
								</tr>
								<tr>
									<td nowrap class="odd">${bundleTeam?.team?.currentLocation}&nbsp;</td>
								</tr>
								<tr>
									<td nowrap>
									<g:if test="${bundleTeam?.team?.latestAsset}">
									<a href="#" onclick="assetDetails('${bundleTeam?.team?.latestAsset?.id}')">
										<u> ${bundleTeam?.team?.latestAsset?.assetTag} </u>
										<g:if test="${bundleTeam?.eventActive == 'true' && bundleTeam?.elapsedTime}">
										 &nbsp;(${bundleTeam?.elapsedTime} ago)
										</g:if>
									</a>
									</g:if>&nbsp;
									</td>
								</tr>
								<tr>
									<td nowrap class="odd">
										<g:if test="${bundleTeam.team?.role == 'CLEANER'}">
											<a href="#" onclick="filterByDataPoints('source','','source_pend_clean')"><u>${bundleTeam?.sourcePendAssets}<span style="font-weight: normal;">pend</span></u></a> +
											<a href="#" onclick="filterByDataPoints('source','','source_avail_clean')"><u style="color: green;">${bundleTeam?.sourceAvailassets}<span style="font-weight: normal;">rdy</span></u></a> +
											<a href="#" onclick="filterByDataPoints('source','','source_done_clean')"><u>${bundleTeam?.unrackedAssets}<span style="font-weight: normal;">dn</span></u></a> = 
											<a href="#" onclick="filterByDataPoints('source','','')"><u>${bundleTeam?.sourceAssets}</u></a>
											<jsec:lacksAllRoles in="['MANAGER','OBSERVER']"> 
											<a title="Login as.." href="../moveTech/signIn?username=ct-${moveBundleInstance.id}-${bundleTeam?.team?.id}-s">@</a>
											</jsec:lacksAllRoles>
										</g:if>
										<g:else>
											<a href="#" onclick="filterByDataPoints('source','${bundleTeam?.team?.id}','source_pend')"><u>${bundleTeam?.sourcePendAssets}<span style="font-weight: normal;">pend</span></u></a> +
											<a href="#" onclick="filterByDataPoints('source','${bundleTeam?.team?.id}','source_avail')"><u style="color: green;">${bundleTeam?.sourceAvailassets}<span style="font-weight: normal;">rdy</span></u></a> +
											<a href="#" onclick="filterByDataPoints('source','${bundleTeam?.team?.id}','source_done')"><u>${bundleTeam?.unrackedAssets}<span style="font-weight: normal;">dn</span></u></a> = 
											<a href="#" onclick="filterByDataPoints('source','${bundleTeam?.team?.id}','')"><u>${bundleTeam?.sourceAssets}</u></a>
											<jsec:lacksAllRoles in="['MANAGER','OBSERVER']"> 
											<a title="Login as.." href="#" onclick="window.open('../clientTeams/home?bundleId=${moveBundleInstance.id}&teamId=${bundleTeam?.team?.id}&location=source','mtwindow','menubar=1,resizable=1,scrollbars=1,width=320,height=480'); ">@</a>
											</jsec:lacksAllRoles>
										</g:else>
									</td>
								</tr>
								<tr>
									<td nowrap>
										<g:if test="${bundleTeam.team?.role != 'CLEANER'}">
										<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_pend')"><u>${bundleTeam?.targetPendAssets}<span style="font-weight: normal;">pend</span></u> </a> +
										<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_avail')"><u style="color: green;">${bundleTeam?.targetAvailAssets}<span style="font-weight: normal;">rdy</span></u> </a> +
										<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_done')"><u> ${bundleTeam?.rerackedAssets}<span style="font-weight: normal;">dn</span></u> </a> = 
										<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','')"><u>${bundleTeam?.targetAssets}</u></a>
										<jsec:lacksAllRoles in="['MANAGER','OBSERVER']"> 
										<a title="Login as.." href="#" onclick="window.open('../clientTeams/home?bundleId=${moveBundleInstance.id}&teamId=${bundleTeam?.team?.id}&location=target','mtwindow','menubar=1,resizable=1,scrollbars=1,width=320,height=480'); ">@</a>
										</jsec:lacksAllRoles>
										</g:if>
										<g:else>
											<span style="text-align: center;">N/A</span>
										</g:else>
									</td>
								</tr>
							</table>
							</td>
						</g:each>
						<g:if test="${teamType != 'ADMIN'}">
							<td style="padding: 0px;">
							<table style="border: 0;">
								<tr><th nowrap class="teamstatus_pending" style='border: 1px solid #ccc;'>Transport &nbsp;</th>
								</tr>
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
									<a href="#" onclick="filterByDataPoints('source','','source_pend_trans')"><u> ${supportTeam.sourceTransportPend}<span style="font-weight: normal;">pend</span></u> </a> +
									<a href="#" onclick="filterByDataPoints('source','','source_avail_trans')"><u style="color: green;"> ${supportTeam.sourceTransportAvail}<span style="font-weight: normal;">rdy</span></u> </a> +
									<a href="#" onclick="filterByDataPoints('source','','source_done_trans')"><u> ${supportTeam.sourceMover}<span style="font-weight: normal;">dn</span></u> </a> =
									<a href="#" onclick="filterByDataPoints('source','','')"><u> ${supportTeam.totalAssets}</u> </a> 
									</td>
								</tr>
								<tr>
									<td nowrap>
									<a href="#" onclick="filterByDataPoints('target','','target_pend_trans')"><u> ${supportTeam.targetTransportPend}<span style="font-weight: normal;">pend</span></u> </a> +
									<a href="#" onclick="filterByDataPoints('target','','target_avail_trans')"><u style="color: green;"> ${supportTeam.targetTransportAvail}<span style="font-weight: normal;">rdy</span></u> </a> +
									<a href="#" onclick="filterByDataPoints('target','','target_done_trans')"><u> ${supportTeam.targetMover}<span style="font-weight: normal;">dn</span></u> </a> =
									<a href="#" onclick="filterByDataPoints('target','','')"><u> ${supportTeam.totalAssets}</u> </a> 
									</td>
								</tr>
							</table>
							</td>
						</g:if>
					</tr>
				</table>
				</div>
				</td>
				<td valign="top"
					style="border-right: 1px solid #333333; padding: 0px; width: 250px;">
				<table style="width: 100%; border: 0">
					<tr><th nowrap>TOTALS:</th>
					</tr>
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
							<g:if test="${teamType != 'ADMIN'}">
							<a href="#" onclick="filterByDataPoints('source','','source_pend')"><u> ${totalSourcePending}<span style="font-weight: normal;">pend</span></u> </a> +
							<a href="#" onclick="filterByDataPoints('source','','source_avail')"><u style="color: green;"> ${totalSourceAvail}<span style="font-weight: normal;">rdy</span></u> </a> +
							<a href="#" onclick="filterByDataPoints('source','','source_done')"><u> ${totalUnracked}<span style="font-weight: normal;">dn</span></u> </a> = 
							<a href="#" onclick="filterByDataPoints('source','','')"><u>${totalAsset}</u> </a>
							</g:if>&nbsp;
						</td>
					</tr>
					<tr>
						<td nowrap>
							<g:if test="${teamType != 'ADMIN'}">
							<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_pend')"><u>${totalTargetPending}<span style="font-weight: normal;">pend</span></u></a> +
							<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_avail')"><u style="color: green;">${totalTargetAvail}<span style="font-weight: normal;">rdy</span></u></a> +
							<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','target_done')"><u>${totalReracked}<span style="font-weight: normal;">dn</span></u></a> = 
							<a href="#" onclick="filterByDataPoints('target','${bundleTeam?.team?.id}','')"><u>${totalAsset}</u> </a>
							</g:if>&nbsp;
						</td>
					</tr>
				</table>
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
				<div id="assetsTbody">
				<g:form name="assetListForm" controller="assetEntity" action="dashboardView" method='get'>
					<table>
						<thead>
							<tr	id="rowId" onmouseover="$('#rowId').css('background','white');">
								<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJ_MGR']">
								<td id="tdId">
								<input id="state" type="button" value="State..." onclick="changeState()" title="Change State" />
								<input type="hidden" name="projectId" value="${projectId}" />
								<input type="hidden" name="moveBundle" value="${moveBundleInstance.id}" />
								<input type="hidden" name="showAll" value="${showAll}"/>
								<input type="hidden" name="application" value="${params.application}"/>
								<input type="hidden" name="appOwner" value="${params.appOwner}"/>
								<input type="hidden" name="currentState" value="${params.currentState}"/>
								<input type="hidden" name="team" value="${params.team}"/>
								<input type="hidden" name="assetLocation" value="${params.assetLocation}"/>
								<input type="hidden" name="assetStatus" value="${params.assetStatus}"/>
								<jsec:hasAnyRole in="['ADMIN','PROJ_MGR']">
									<input id="state" type="button" value="All.." onclick="selectAll()" title="Select All" />
								</jsec:hasAnyRole>
								</td>
								</jsec:hasAnyRole>
								<td style="vertical-align: middle;" colspan="3">
									<label for="showAllCheckbox"><input type="checkbox" onclick="showAllAssets()" id="showAllCheckbox"/>&nbsp;Show All&nbsp;</label>
									&nbsp;&nbsp;<input type="button" onclick="showfilterDialog()" id="filterButtonId" value="Filter"/>
									&nbsp;&nbsp;<span>Viewing: ${assetBeansList?.size()}</span>
									<g:if test="${params.application || params.appOwner || params.appSme || params.currentState || params.assetLocation || params.assetStatus }">
									&nbsp;<a href="#"
											 onclick="document.filterForm.reset();$('#filterShowAllId').val('');document.filterForm.submit();"><span class="clear_filter"><u>X</u></span></a>
									</g:if>
									<g:if test="${totalAssetsOnHold > 0}">
									&nbsp;&nbsp;<input type="button" class="onhold_button" onclick="submitFormWithBundle('')" id="onHoldButtonId" value="On Hold (${totalAssetsOnHold})"/>
									</g:if>									
								</td>
							</tr>
							</thead>
					</table>
					<jmesa:tableFacade id="tag" items="${assetBeansList}" maxRows="25" stateAttr="restore" var="assetEntityBean" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
			             <jmesa:htmlTable style=" border-collapse: separate" editable="true">
			                 <jmesa:htmlRow>
			                 	<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJ_MGR']">
			                 	 <jmesa:htmlColumn property="Actions" title="" sortable="false" filterable="false" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap class="${assetEntityBean?.cssClass}">
		                         	<jsec:hasAnyRole in="['ADMIN','PROJ_MGR']">
										<g:if test="${assetEntityBean.checkVal == true}">
											<span id="spanId_${assetEntityBean.id}">
												<g:checkBox name="checkChange" id="checkId_${assetEntityBean.id}" onclick="timedUpdate('never')"></g:checkBox>
											</span>
										</g:if>
									</jsec:hasAnyRole>
									<g:remoteLink controller="assetEntity" action="editShow" id="${assetEntityBean.id}" before="document.showForm.id.value = ${assetEntityBean.id};document.editForm.id.value = ${assetEntityBean.id};" onComplete="showAssetDialog( e , 'show');">
										<img src="${createLinkTo(dir:'images',file:'asset_view.png')}" border="0px" />
									</g:remoteLink>
									<span style="display: none;" id="image_${assetEntityBean.id}" class="selected" >
										<img src="${createLinkTo(dir:'images',file:'row_arrow.gif')}" border="0px"/>
									</span>
		                         </jmesa:htmlColumn>
								</jsec:hasAnyRole>
	 		                     <jmesa:htmlColumn property="priority" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									<div id="priority_${assetEntityBean.id}" style="width: 100%"
										onclick="assetDetails('${assetEntityBean.id}')">${assetEntityBean.priority}&nbsp;</div>
									<input type="hidden" class="input" value="${assetEntityBean?.cssClass}">
								 </jmesa:htmlColumn>
								 <jmesa:htmlColumn property="assetTag" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									<div id="assetTag_${assetEntityBean.id}" onclick="assetDetails('${assetEntityBean.id}')" style="width: 100%">${assetEntityBean.assetTag}&nbsp;</div>
								 </jmesa:htmlColumn>
								 <jmesa:htmlColumn property="assetName" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									<div id="assetName_${assetEntityBean.id}" onclick="assetDetails('${assetEntityBean.id}')" style="width: 100%">${assetEntityBean.assetName}&nbsp;</div>
								 </jmesa:htmlColumn>
								 <jmesa:htmlColumn property="status" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									<div onclick="assetDetails('${assetEntityBean.id}')"	id="statusCol_${assetEntityBean.id}" style="width: 100%">${assetEntityBean.status}&nbsp;</div>
								 </jmesa:htmlColumn>
								 <g:if test="${teamType != 'ADMIN'}">
								 <jmesa:htmlColumn property="sourceTeamMt" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									<div onclick="assetDetails('${assetEntityBean.id}')"	id="source_${assetEntityBean.id}" style="width: 100%">${assetEntityBean.sourceTeamMt}&nbsp;</div>
								 </jmesa:htmlColumn>
								 <jmesa:htmlColumn property="targetTeamMt" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									<div onclick="assetDetails('${assetEntityBean.id}')" id="target_${assetEntityBean.id}" style="width: 100%">${assetEntityBean.targetTeamMt}&nbsp;</div>
								 </jmesa:htmlColumn>
								 </g:if>
								 <jmesa:htmlColumn property="commentType" title="Issues" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									<div id="icon_${assetEntityBean.id}">
										<g:if test="${assetEntityBean.commentType == 'issue'}">
											<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntityBean.id}" before='setAssetId(${assetEntityBean.id});'	onComplete="listCommentsDialog( e ,'never' );">
												<img src="${createLinkTo(dir:'i',file:'db_table_red.png')}"	border="0px"/>
											</g:remoteLink>
										</g:if>
										<g:elseif test="${assetEntityBean.commentType == 'comment'}">
											<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntityBean.id}" before="setAssetId(${assetEntityBean.id});" onComplete="listCommentsDialog( e ,'never' ); ">
												<img src="${createLinkTo(dir:'i',file:'db_table_bold.png')}" border="0px"/>
											</g:remoteLink>
										</g:elseif>
										<g:else>
										<a onclick="createNewAssetComment(${assetEntityBean.id});">
											<img src="${createLinkTo(dir:'i',file:'db_table_light.png')}" border="0px"/>
										</a>
										</g:else>
									</div>
								 </jmesa:htmlColumn>
			                 </jmesa:htmlRow>
			             </jmesa:htmlTable>
			         </jmesa:tableFacade>
				</g:form>
				
				</div>
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
				<input type="hidden" name="asset" id="assetId" value=""/>
				<input type="hidden" name="currentState" id="currentStateId" value=""/>
				<input type="hidden" value="" id="validateCommentId" name="validateComment"/>
				<g:form name="assetdetailsForm">
					<table style="border: 0">
						<tbody>
							<tr id="setHoldTimerTr" style="display: none;">
								<td nowrap><b>Hold Time :</b></td>
								<td>
									<script type="text/javascript">
					                    $(document).ready(function(){
					                      $("#holdTimeId").datetimepicker();
					                    });
					                 </script> 
					                 <input type="text" class="dateRange" size="15" style="width: 125px; height: 14px;" id="holdTimeId" name="holdTime"/>
								</td>
							</tr>
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
									<textarea name="comment" id="commentId" cols="25" rows="2" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea></td>
							</tr>
							<tr>
								<td colspan="2" style="text-align: center;" class="buttonR">
								<input type="button" value="Cancel"	onclick="resetAssetDetails()"/>
								<input type="button" value="Submit"
									onclick="if(setCommentValidation())${remoteFunction(action:'createTransition', params:'\'asset=\' + $(\'#assetId\').val() +\'&state=\'+ $(\'#stateSelectId\').val() +\'&priority=\'+ $(\'#priorityId\').val() +\'&assignTo=\'+$(\'#assignToId\').val() +\'&comment=\'+$(\'#commentId\').val()+\'&holdTime=\'+$(\'#holdTimeId\').val() ', onComplete:'updateAsset(e)')}" /></td>
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
	style="display: none;" ><br/>
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
	onclick="$('#statusId').val('');$('#createResolveDiv').hide();$('#createCommentDialog').dialog('option', 'width', 'auto');$('#createCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();">New
Comment</a></span></div>
</div>
<div id="createCommentDialog" title="Create Asset Comment" style="display: none;">
<input type="hidden" name="assetEntity.id" id="createAssetCommentId" value=""/> 
<input type="hidden" name="status" id="statusId" value=""/> 
<g:form	action="saveComment" method="post" name="createCommentForm">
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
				
				<input type="checkbox" id="mustVerifyEdit" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
					<label for="mustVerify">Must Verify</label>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="comment" name="comment" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea></td>
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
                <textarea cols="80" rows="5" id="resolution" name="resolution" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)" ></textarea>
                </td>
            </tr> 
                
            </table>
            </div>
		
	</div>
	<div class="buttons"><span class="button"> 
	<input class="save" type="button" value="Create"
		onclick="resolveValidate('createCommentForm','createAssetCommentId');" /></span></div>
</g:form></div>
<div id="showCommentDialog" title="Show Asset Comment"
	style="display: none;">
<div class="dialog" style="border: 1px solid #5F9FCF">
<input name="id" value="" id="commentId" type="hidden"/>
	<div>
<table id="showCommentTable" style="border: 0px;">
	
	<tr>
	<td valign="top" class="name"><label for="dateCreated">Created
			At:</label></td>
			<td valign="top" class="value" id="dateCreatedId" ></td>
	</tr>
		<tr>
	<td valign="top" class="name"><label for="createdBy">Created
			By:</label></td>
			<td valign="top" class="value" id="createdById" ></td>
	</tr>
		
		<tr class="prop">
			<td valign="top" class="name"><label for="commentType">Comment
			Type:</label></td>
			<td valign="top" class="value" id="commentTypeTdId" ></td>
		</tr>
		
		<tr>
	<td valign="top" class="name"><label for="category">Category:
			</label></td>
			<td valign="top" class="value" id="categoryTdId" ></td>
	</tr>
	<tr class="prop">
	<td valign="top" class="name"><label for="commentCode">comment
			Code:</label></td>
			<td valign="top" class="value" id="commentCodeTdId" ></td>
	</tr>
	
		<tr class="prop">
			<td valign="top" class="name"><label for="mustVerify">Must
			Verify:</label></td>
			<td valign="top" class="value" id="verifyTdId">
			<input type="checkbox" id="mustVerifyShowId" name="mustVerify" value="0" disabled="disabled" />
			</td>
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
			<td valign="top" class="value" id="resolveTdId">
			<input type="checkbox" id="isResolvedId" name="isResolved" value="0"
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
			<td valign="top" class="value" id="dateResolvedId" ></td>
	</tr>
		<tr>
	<td valign="top" class="name"><label for="resolvedBy">Resolved
			By:</label></td>
			<td valign="top" class="value" id="resolvedById" ></td>
	</tr>
	
</table>
</div>
 <div class="buttons"><span class="button"> 
 <input class="edit" type="button" value="Edit"
	onclick="commentChangeEdit('editResolveDiv','editCommentForm');$('#editCommentDialog').dialog('option', 'width', 'auto');$('#editCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('open');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close')" />
</span> <span class="button"> 
<input class="delete" type="button" value="Delete"
	onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment', params:'\'id=\' + $(\'#commentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e ,\'never\')')}" />
</span> </div>
</div>
</div>

<div id="editCommentDialog" title="Edit Asset Comment"
	style="display: none;"><g:form action="updateComment"
	method="post" name="editCommentForm">
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<input type="hidden" name="id" id="updateCommentId" value=""/>
	<div>
	<table id="updateCommentTable" style="border: 0px">
		
		
			<tr>
	<td valign="top" class="name"><label for="dateCreated">Created
			At:</label></td>
			<td valign="top" class="value" id="dateCreatedEditId"  ></td>
	</tr>
		<tr>
	<td valign="top" class="name"><label for="createdBy">Created
			By:</label></td>
			<td valign="top" class="value" id="createdByEditId" ></td>
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
				
				<input type="text" id="commentTypeEditId" name="commentType" readonly style="border: 0;"/>&nbsp;&nbsp;&nbsp;&nbsp;
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
				<td valign="top" class="value"	id="categoryEditId" ></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="commentCode">Comment Code:</label>
				</td>
				<td valign="top" class="value" id="commentCodeEditId" ></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="commentEditId" name="comment" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea></td>
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
                <textarea cols="80" rows="5" id="resolutionEditId" name="resolution" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea>
                </td>
            </tr> 
               <tr>
	<td valign="top" class="name"><label for="dateResolved">Resolved
			At:</label></td>
			<td valign="top" class="value" id="dateResolvedEditId" ></td>
	</tr>
		<tr>
	<td valign="top" class="name"><label for="resolvedBy">Resolved
			By:</label></td>
			<td valign="top" class="value" id="resolvedByEditId"  ></td>
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
<div id="showDialog" title="Show Asset" style="display: none;">
<g:form action="save" method="post" name="showForm">
	<div class="dialog" id="showDiv">
	
	</div>
	<div class="buttons">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundleId" value="${moveBundleInstance.id}" />
	<input type="hidden" name="showAll" value="${showAll}"/>
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

<div id="editDialog" title="Edit Asset" style="display: none;">
<g:form method="post" name="editForm">
	<input type="hidden" name="id" id="editFormId" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundleId" value="${moveBundleInstance.id}" />
	<input type="hidden" name="showAll" value="${showAll}"/>
	<div class="dialog" id="editDiv">
	
	</div>
	<div class="buttons"><span class="button">
	<input type="button" class="save" value="Update Asset" onClick="${remoteFunction(action:'getAssetAttributes', params:'\'assetId=\' + $(\'#editFormId\').val() ', onComplete:'callUpdateDialog(e)')}" />
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
		<input type="hidden" name="showAll" id="filterShowAllId" value="${showAll}"/>
		<input type="hidden" name="myForm" value="filterForm"/>
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
				<td colspan="2" style="text-align: center;"><input type="reset" value="Cancel" onclick="$('#filterDialog').dialog('close');"/>
				<input type="reset" value="Clear"/>
				<input type="submit" value="Apply" onclick="$('#filterShowAllId').val('show')"/>				
				</td>
			</tr>
		</table>
	</g:form>
</div>
<div id="manufacturerShowDialog" title="Show Manufacturer">
	<div class="dialog">
		<table>
	    	<tbody>
				<tr class="prop">
	            	<td valign="top" class="name">Name:</td>
					<td valign="top" class="value" id="showManuName"></td>
				</tr>
	            <tr>
	 				<td valign="top" class="name">AKA:</td>
					<td valign="top" class="value"  id="showManuAka"></td>
				</tr>
	            <tr class="prop">
	            	<td valign="top" class="name">Description:</td>
					<td valign="top" class="value" id="showManuDescription"></td>
				</tr>
			</tbody>
		</table>
	</div>
	<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
	<div class="buttons">
	    <g:form controller="manufacturer" action="edit" target="new">
	        <input type="hidden" name="id" id="manufacturerId" />
	        <span class="button"><input type="submit" class="edit" value="Edit" onclick="$('#manufacturerShowDialog').dialog('close')"/></span>
	    </g:form>
	</div>
	</jsec:hasAnyRole>
</div>
<div id="modelShowDialog"  title="Show Model">
<div class="dialog">
<table>
	<tbody>
		<tr>
			<td valign="top" class="name">Manufacturer:</td>
			<td valign="top" class="value" id="showManufacturer"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Model Name:</td>
			<td valign="top" class="value" id="showModelName"></td>
		</tr>
		<tr>
			<td valign="top" class="name">AKA:</td>
			<td valign="top" class="value" id="showModelAka"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Asset Type:</td>
			<td valign="top" class="value" id="showModelAssetType"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Usize:</td>
			<td valign="top" class="value" id="showModelUsize"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Power (typical):</td>
			<td valign="top" class="value" id="showModelPower"></td>
		</tr>
		<tr>
			<td valign="top" class="name">Front image:</label></td>
        	<td valign="top" class="value" id="showModelFrontImage"></td>
		</tr>
		<tr>
        	<td valign="top" class="name">Rear image:</td>
        	<td valign="top" class="value" id="showModelRearImage"></td>
        </tr>
        <tr>
        	<td valign="top" class="name">Use Image:</td>
	        <td valign="top" class="value" id="showModelUseImage"></td>
        </tr>
		<tr id="showModelBladeRowsTr">
			<td valign="top" class="name">Blade Rows:</td>
			<td valign="top" class="value" id="showModelBladeRows"></td>
		</tr>
		<tr id="showModelBladeCountTr">
			<td valign="top" class="name">Blade Count:</td>
			<td valign="top" class="value" id="showModelBladeCount"></td>
		</tr>
		<tr id="showModelBladLabelCountTr">
			<td valign="top" class="name">Blade Label Count:</td>
			<td valign="top" class="value" id="showModelBladLabelCount"></td>
		</tr>
		<tr id="showModelBladeHeightTr">
			<td valign="top" class="name">Blade Height:</td>
			<td valign="top" class="value" id="showModelBladeHeight"></td>
		</tr>
		<tr>
        	<td valign="top" class="name">Source TDS:</td>
	        <td valign="top" class="value" id="showModelSourceTds"></td>
        </tr>
		<tr>
			<td valign="top" class="name">Notes:</td>
			<td valign="top" class="value" id="showModelNotes"></td>
		</tr>
	</tbody>
</table>
</div>
<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
<div class="buttons"> 
	<g:form action="edit" controller="model" target="new">
		<input name="id" type="hidden" id="modelId"/>
		<span class="button">
			<input type="submit" class="edit" value="Edit"></input>
		</span>
	</g:form>
</div>
</jsec:hasAnyRole>
</div>
<script type="text/javascript">
bundleChange();
$("#midDiv").css('width',vpWidth() - 340)
timedUpdate($("#selectTimedId").val())

$("tbody.tbody tr").removeAttr("class")
var inputs = $("tbody.tbody tr input.input")
for(i=1;i<=inputs.length;i++){
	$("#tag_row"+i).addClass(inputs[i-1].value)
}
</script>
</div>
</body>
</html>
