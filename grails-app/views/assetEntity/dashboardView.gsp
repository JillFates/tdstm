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
<g:javascript src="assetcommnet.js" />
<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />

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
	    
	})
</script>
<script type="text/javascript">

	function showAssetDialog( e , action ) {
		timedRefresh('never')
		$('#createCommentDialog').dialog('close');
		$('#commentsListDialog').dialog('close');
		$('#editCommentDialog').dialog('close');
		$('#showCommentDialog').dialog('close');
		$("#showChangeStatusDialog").dialog('close');
		var browser=navigator.appName;
      	var assetEntityAttributes = eval('(' + e.responseText + ')');
      	var autoComp = new Array()      			
      	var showDiv = document.getElementById("showDiv");      			
      	var editDiv = document.getElementById("editDiv");
      	var stb = document.getElementById('showTbodyId')
		if(stb != null){
			showDiv.removeChild(stb)
		}
      	var etb = document.getElementById('editTbodyId')
		if(etb != null){
			editDiv.removeChild(etb)
		}
      	// create tbody for CreateTable
      	var stbody = document.createElement('table');
		stbody.id = "showTbodyId"
      			
      	var etbody = document.createElement('table');
		etbody.id = "editTbodyId"
		// Rebuild the select
		if (assetEntityAttributes) {
			var length = assetEntityAttributes.length
			var halfLength = getLength(length) 
			var str = document.createElement('tr');
			var etr = document.createElement('tr');
			var stdLeft = document.createElement('td');
			stdLeft.style.width = '50%'
			var etdLeft = document.createElement('td');
			var stdRight = document.createElement('td');
			stdRight.style.width = '50%'
			var etdRight = document.createElement('td');
			var stableLeft = document.createElement('table');
			var etableLeft = document.createElement('table');
			stableLeft.style.width = '50%'
			stableLeft.style.border = '0'
			etableLeft.style.width = '50%'
			etableLeft.style.border = '0'
			var stableRight = document.createElement('table');
			var etableRight = document.createElement('table');
			stableRight.style.width = '50%'
			stableRight.style.border = '0'
			etableRight.style.width = '50%'
			etableRight.style.border = '0'
			for (var i=0; i < halfLength; i++ ) {
				var attributeLeft = assetEntityAttributes[i]
				var strLeft = document.createElement('tr');
				var etrLeft = document.createElement('tr');
				// td for Show page
				var inputTdLeft = document.createElement('td');
				var labelTdLeft = document.createElement('td');
				labelTdLeft.noWrap = 'nowrap'
				var labelLeft = document.createTextNode(attributeLeft.label);
				labelTdLeft.appendChild( labelLeft )
				var inputFieldLeft = document.createTextNode(attributeLeft.value);
				inputTdLeft.appendChild( inputFieldLeft )
				labelTdLeft.style.background = '#f3f4f6 '
				labelTdLeft.style.width = '25%'
				inputTdLeft.style.width = '25%'
				strLeft.appendChild( labelTdLeft )
				strLeft.appendChild( inputTdLeft )
				// td for Edit page
				var inputTdELeft = document.createElement('td');
				var labelTdELeft = document.createElement('td');
				labelTdELeft.noWrap = 'nowrap'
				var labelELeft = document.createTextNode(attributeLeft.label);
				labelTdELeft.appendChild( labelELeft )
				var inputFieldELeft = getInputType(attributeLeft);
				inputFieldELeft.value = attributeLeft.value;
				inputFieldELeft.id = 'edit'+attributeLeft.attributeCode+'Id';
				inputTdELeft.appendChild( inputFieldELeft )
				labelTdELeft.style.background = '#f3f4f6 '
					
				labelTdELeft.style.width = '25%'
				inputTdELeft.style.width = '25%'
				etrLeft.appendChild( labelTdELeft )
				etrLeft.appendChild( inputTdELeft )
				stableLeft.appendChild( strLeft )
				etableLeft.appendChild( etrLeft )
			}
				      	
			for (var i=halfLength; i < length; i++ ) {
				var attributeRight = assetEntityAttributes[i]
				var strRight = document.createElement('tr');
				var etrRight = document.createElement('tr');
				// td for Show page
				var inputTdRight = document.createElement('td');
				var labelTdRight = document.createElement('td');
				labelTdRight.noWrap = 'nowrap'
				var labelRight = document.createTextNode(attributeRight.label);
				labelTdRight.appendChild( labelRight )
				var inputFieldRight = document.createTextNode(attributeRight.value);
				inputTdRight.appendChild( inputFieldRight )
				labelTdRight.style.background = '#f3f4f6 '
				labelTdRight.style.width = '25%'
				inputTdRight.style.width = '25%'
				strRight.appendChild( labelTdRight )
				strRight.appendChild( inputTdRight )
					      
				// td for Edit page
				var inputTdERight = document.createElement('td');
				var labelTdERight = document.createElement('td');
				labelTdERight.noWrap = 'nowrap'
				var labelERight = document.createTextNode(attributeRight.label);
				labelTdERight.appendChild( labelERight )
					      
				var inputFieldERight = getInputType(attributeRight);
				inputFieldERight.value = attributeRight.value;
				inputFieldERight.id = 'edit'+attributeRight.attributeCode+'Id';
				inputTdERight.appendChild( inputFieldERight )
				labelTdERight.style.background = '#f3f4f6 '
				labelTdERight.style.width = '25%'
				inputTdERight.style.width = '25%'
				etrRight.appendChild( labelTdERight )
				etrRight.appendChild( inputTdERight )
				stableRight.appendChild( strRight )
				etableRight.appendChild( etrRight )
				      	
			}
			for (var i=0; i < length; i++ ) {
				var attribute = assetEntityAttributes[i]
				if(attribute.frontendInput == 'autocomplete'){
					autoComp.push(attribute.attributeCode)
				}
			}
			stdLeft.appendChild( stableLeft )
			etdLeft.appendChild( etableLeft )
			stdRight.appendChild( stableRight )
			etdRight.appendChild( etableRight )
			str.appendChild( stdLeft )
			etr.appendChild( etdLeft )
			str.appendChild( stdRight )
			etr.appendChild( etdRight )
			stbody.appendChild( str )
			etbody.appendChild( etr )
		}
		showDiv.appendChild(stbody)
		showDiv.innerHTML += "";
		
		editDiv.appendChild( etbody )
		if(browser == 'Microsoft Internet Explorer') {
			editDiv.innerHTML += "";
		} 
			    
		${remoteFunction(action:'getAutoCompleteDate', params:'\'autoCompParams=\' + autoComp ', onComplete:'updateAutoComplete(e)')} 
		if(action == 'edit'){
			$("#editDialog").dialog('option', 'width', 600)
			$("#editDialog").dialog('option', 'position', ['center','top']);
			$("#editDialog").dialog("open")
			$("#showDialog").dialog("close")
		} else if(action == 'show'){
			$("#showDialog").dialog('option', 'width', 600)
			$("#showDialog").dialog('option', 'position', ['center','top']);
			$("#showDialog").dialog("open")
			$("#editDialog").dialog("close")
		}
	}
		    function getLength( length ){
      			var isOdd = (length%2 != 0) ? true : false
      			var halfLength
      			if(isOdd){
      				length += 1;
      				halfLength = length / 2 
      			} else {
      				halfLength = length / 2 
      			}
      			return halfLength; 
      		}
      		
      		// function to construct the frontendInput tag
      		function getInputType( attribute ){
      			var name = attribute.attributeCode
      			var type = attribute.frontendInput
      			var options = attribute.options
      			var inputField
      			if(type == 'select'){
					inputField = document.createElement('select');
					inputField.name = name ;
						var inputOption = document.createElement('option');
						inputOption.value = ''
						inputOption.innerHTML = 'please select'
						inputField.appendChild(inputOption)
						if (options) {
					      var length = options.length
					      for (var i=0; i < length; i++) {
						      var optionObj = options[i]
						      var popt = document.createElement('option');
						      popt.innerHTML = optionObj.option
						      popt.value = optionObj.option
						      if(attribute.value == optionObj.option){
							      popt.selected = true
						      }
						      try {
						      	inputField.appendChild(popt, null) // standards compliant; doesn't work in IE
						      } catch(ex) {
						      	inputField.appendChild(popt) // IE only
						      }
					      }
					   }						
				} else {
      			 	inputField = document.createElement('input');      			 	
					inputField.type = "text";										
					inputField.name = name;
				}
				return inputField; 
      		}
      		
      		function editAssetDialog() {
			timedRefresh('never')
		      $("#showDialog").dialog("close")
		      $("#editDialog").dialog('option', 'width', 600)
		      $("#editDialog").dialog('option', 'position', ['center','top']);
		      $("#editDialog").dialog("open")
		
		    }

	function updateAutoComplete(e){

      			var data = eval('(' + e.responseText + ')');
      		
      			if (data) {
      			
				      var length = data.length
				     
				      for (var i=0; i < length; i ++ ) {
					      var attribData = data[i]
					    
					      var code = "edit"+attribData.attributeCode+"Id"
					      var codeValue = attribData.value;
				  			$("#"+code).autocomplete(codeValue);
					  }
					 
				}
				      			
      		}
      		
      		function callUpdateDialog( e ) {
		    timedRefresh('never')
		    	var assetEntityAttributes = eval('(' + e.responseText + ')');
				var assetId = $("#editFormId").val()
		    	var assetEntityParams = new Array()
		    	if (assetEntityAttributes) {
		    		var length = assetEntityAttributes.length
				      	for (var i=0; i < length; i ++) {
				      		var attributeCode = assetEntityAttributes[i].attributeCode
				      		var attributeValue = $('#edit'+attributeCode+'Id').val()
				      		if(assetEntityAttributes[i].frontendInput == 'select'){
					      		assetEntityParams.push(attributeCode+':'+attributeValue)
				      		} else {
				      			assetEntityParams.push(attributeCode+':'+attributeValue)
				      		}
				      	}
		    	}
		    ${remoteFunction(action:'updateAssetEntity', params:'\'assetEntityParams=\' + assetEntityParams +\'&id=\'+assetId', onComplete:'showEditAsset(e)')}
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
		'<tr><td><b>Rack: </b>'+asset[0].assetDetails.assetDetail.sourceRack+'</td></tr>'+
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
	   if(showAll == 'show'){
	   		$("#showAllCheckbox").attr('checked',true)
	   }
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
			timer = setTimeout("location.reload(true);",timeoutPeriod);
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
	function resolveValidate(formName,idVal){
		var type = 	document.forms[formName].commentType.value;
		if(type != "issue"){
			document.forms[formName].isResolved.value = 0;
		}
		var resolveBoo = document.forms[formName].isResolved.checked;
		var resolveVal = document.forms[formName].resolution.value;
		var assetId = $("#"+idVal).val()
		if(type == ""){
			alert('Please select comment type');
			return false;
		}else if(resolveBoo){
			if(resolveVal != ""){
			if(formName == "createCommentForm"){
				${remoteFunction(action:'saveComment', params:'\'assetEntity.id=\' + assetId +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'addCommentsToList(e)')}
			}else{
				${remoteFunction(action:'updateComment', params:'\'id=\' + assetId +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'updateCommentsOnList(e)')}
			}
			}else{
				alert('Please enter resolution');
				return false;
			}
		}else{
			if(formName == "createCommentForm"){
				${remoteFunction(action:'saveComment', params:'\'assetEntity.id=\' + assetId +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'addCommentsToList(e)')}
			}else{
				${remoteFunction(action:'updateComment', params:'\'id=\' + assetId +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'updateCommentsOnList(e)')}
			}
		}
	}
	function setAssetId(assetId){
		$("#createAssetCommentId").val(assetId)
	}
	function showAll(){
		var showAll = $("#showAllCheckbox").is(':checked');
		if(showAll){
			$("#showAllId").val('show');
		}
		$("form#dashboardForm").submit();
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
	<input type="hidden" name="showAll" id="showAllInChangeStatus">
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
			onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)submitAction()" /></td>
	</tr>
</table>
</form>
</div>
<div class="body">
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<g:form method="get" name="dashboardForm"
	controller="assetEntity" action="dashboardView">
	<input type="hidden" name="projectId" value="${projectId}">
	<input type="hidden" name="showAll" id="showAllId">
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
						<td>Latest Asset</td>
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
									<td nowrap>
									<g:if test="${bundleTeam?.team?.latestAsset}">
									<a href="#" onclick="assetDetails('${bundleTeam?.team?.latestAsset?.id}')"> ${bundleTeam?.team?.latestAsset?.assetTag}</a>
									</g:if>&nbsp;
									</td>
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
								<td nowrap>
									<g:if test="${supportTeam?.cleaning?.latestAsset}">
									<a href="#" onclick="assetDetails('${supportTeam?.cleaning?.latestAsset?.id}')"> ${supportTeam?.cleaning?.latestAsset?.assetTag}</a>
									</g:if>&nbsp;
								</td>
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
								<td nowrap>&nbsp;</td>
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
							<tr	id="rowId" onmouseover="$('#rowId').css('background','white');">
								<jsec:hasRole name="ADMIN">
								<td id="tdId"><input id="state" type="button"
									value="State..." onclick="changeState()" title="Change State" />
								</td>
								</jsec:hasRole>
								<td style="vertical-align: middle;" colspan="2">
									<input type="checkbox" onclick="showAll()" id="showAllCheckbox"/>&nbsp;Show All
								</td>
							</tr>
							<tr>
								<jsec:hasAnyRole in="['ADMIN','SUPERVISOR']"><th>Actions <jsec:hasRole name="ADMIN"><a href="#" onclick="selectAll()"><u
									style="color: blue;">All</u></a></jsec:hasRole></th></jsec:hasAnyRole>
								<g:sortableColumn property="priority" title="Priority" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
								<g:sortableColumn property="assetTag" title="Asset Tag" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
								<g:sortableColumn property="assetName" title="Asset Name" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
								<g:sortableColumn property="currentState" title="Status" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
								<g:sortableColumn property="sourceTeam" title="Source Team" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
								<g:sortableColumn property="targetTeam" title="Target Team" params='["projectId":projectId,"moveBundle":moveBundleInstance.id]'/>
								<th>Issues</th>
							</tr>
						</thead>
						<tbody id="assetsTbody">
						<g:if test="${assetsList}">
							<g:each status="i" in="${assetsList}" var="assetsList">

								<tr name="assetDetailRow"
									id="assetDetailRow_${assetsList?.asset.id}"
									class="${assetsList?.cssClass}" value="${assetsList?.asset.id}">

									<jsec:hasAnyRole in="['ADMIN','SUPERVISOR']">
									<td><jsec:hasRole name="ADMIN">
										<g:if test="${assetsList.checkVal == true}">
										<span id="spanId_${assetsList?.asset.id}">
											<g:checkBox name="checkChange"
												id="checkId_${assetsList?.asset.id}"
												onclick="timedRefresh('never')"></g:checkBox>
										</span>

										</g:if>
									</jsec:hasRole>
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
									<g:else>
										<g:if test="${AssetComment.find('from AssetComment where assetEntity = '+ assetsList?.asset?.id)}">
										<g:remoteLink controller="assetEntity" action="listComments" id="${assetsList?.asset.id}" before="setAssetId(${assetsList?.asset.id});" onComplete="listCommentsDialog( e ,'never' ); ">
											<img src="${createLinkTo(dir:'images/skin',file:'database_table_bold.png')}" border="0px">
										</g:remoteLink>
										</g:if>
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
				<td valign="top" style="padding: 0px;">
				<div id="assetDetails"
					style="border: 1px solid #5F9FCF; width: 200px;">
				<div id="asset">
				<table style="border: 0px" cellpadding="0" cellspacing="0">
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
							<td><b>Rack: </b></td>
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
								<input type="reset" value="Cancel"
									onclick="timedRefresh($('#selectTimedId').val())">
								<input type="button" value="Submit"
									onclick="setCommentValidation();${remoteFunction(action:'createTransition', params:'\'asset=\' + $(\'#assetId\').val() +\'&state=\'+ $(\'#stateSelectId\').val() +\'&priority=\'+ $(\'#priorityId\').val() +\'&assignTo=\'+$(\'#assignToId\').val() +\'&comment=\'+$(\'#commentId\').val() ', onComplete:'updateAsset(e)')}" /></td>
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
	style="display: none;" ><br>
<div class="list">
<table id="listCommentsTable">
	<thead>
		<tr>

			<th nowrap>Action</th>

			<th nowrap>Comment</th>

			<th nowrap>Comment Type</th>
			
			<th nowrap>Resolved</th>

			<th nowrap>Must Verify</th>

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
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span>
		<span class="button"><g:actionSubmit action="remove"
		class="delete"  onclick="return confirm('Are you sure?');"
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
	</span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span>
		<span class="button"><g:actionSubmit action="remove"
		class="delete"  onclick="return confirm('Are you sure?');"
		value="Remove From Project" /></span>
		</div>
</g:form></div>

<script type="text/javascript">
bundleChange();

timedRefresh($("#selectTimedId").val())
</script>
</div>
</body>
</html>
