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
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'dashboard.css')}" />
<g:javascript src="assetcommnet.js" />
<jq:plugin name="ui.core" />
<jq:plugin name="ui.draggable" />
<jq:plugin name="ui.resizable" />
<jq:plugin name="ui.dialog" />

<g:javascript>
function initialize(){
var bundleId = ${moveBundleInstance.id}; 
$("#moveBundleId").val(bundleId);
$("#appSmeId").val("${appSmeValue}");
$("#appOwnerId").val("${appOwnerValue}");
$("#applicationId").val("${appValue}");
var time = '${timeToRefresh}';
	if(time != "" ){
		$("#selectTimedId").val( time ) ;
	} else if(time == "" ){
		$("selectTimedId").val( 120000 );	
	}
}
</g:javascript>
<script>
	$(document).ready(function() {
		$("#changeStatusDialog").dialog({ autoOpen: false })
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
			    
		${remoteFunction(controller:'assetEntity', action:'getAutoCompleteDate', params:'\'autoCompParams=\' + autoComp ', onComplete:'updateAutoComplete(e)')} 
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
			var assetId = document.editForm.id.value
			var assetEntityParams = new Array()
			if (assetEntityAttributes) {
		   		var length = assetEntityAttributes.length
			      	for (var i=0; i < length; i ++) {
			      		var attributeCode = assetEntityAttributes[i].attributeCode
			      		var attributeValue = $('#edit'+attributeCode+'Id').val();
			      		if(assetEntityAttributes[i].frontendInput == 'select'){
				      		assetEntityParams.push(attributeCode+':'+attributeValue)
			      		} else {
			      			assetEntityParams.push(attributeCode+':'+attributeValue)
			      		}
					}
		    	}
		    ${remoteFunction(controller:'assetEntity', action:'updateAssetEntity', params:'\'assetEntityParams=\' + assetEntityParams +\'&id=\'+assetId', onComplete:'showEditAsset(e)')}
		    }
		    
		    function showEditAsset(e) {
		      var assetEntityAttributes = eval('(' + e.responseText + ')')
			  if (assetEntityAttributes != "") {
		    		var length = assetEntityAttributes.length
				      	for (var i=0; i < length; i ++) {
				      		var attribute = assetEntityAttributes[i]
				      		var tdId = $("#"+attribute.attributeCode+'_'+attribute.id)
				      		if(tdId != null ){
				      				tdId.html( attribute.value )
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
	       	$('#asset').val(task[0].asset);
	 
	 
	$("#changeStatusDialog").dialog('option', 'width', 400)
	$("#changeStatusDialog").dialog('option', 'position', ['center','top']);
	$('#changeStatusDialog').dialog('open');
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
function setRefreshTime(e) {
	var timeRefresh = eval("(" + e.responseText + ")")
	if(timeRefresh){
		timedRefresh(timeRefresh[0].refreshTime.CLIENT_CONSOLE_REFRESH)
	}
}
var timer
function timedRefresh(timeoutPeriod) {
	if(timeoutPeriod != 'never'){
		clearTimeout(timer)
		timer = setTimeout("doAjaxCall()",timeoutPeriod);
		$("#selectTimedId").val( timeoutPeriod );
	} else {
		clearTimeout(timer)
	}
}

function doAjaxCall(){
	var moveBundle = $("#moveBundleId").val();
	var application = $("#applicationId").val();
	var appOwner = $("#appOwnerId").val();
	var appSme = $("#appSmeId").val();
	${remoteFunction(action:'getTransitions', params:'\'moveBundle=\' + moveBundle +\'&application=\'+application +\'&appOwner=\'+appOwner+\'&appSme=\'+appSme', onComplete:'updateTransitions(e);' )}
	timedRefresh($("#selectTimedId").val())
}
function updateTransitions(e){
	try{
		var assetTransitions = eval('(' + e.responseText + ')');
		var assetslength = assetTransitions.length;
		var sessionStatus = isNaN(parseInt(assetslength));
		if( !sessionStatus ){
			if(assetTransitions){
				for( i = 0; i <assetslength ; i++){
					var assetTransition = assetTransitions[i]
					var action = $("#action_"+assetTransition.id)
					if(action){
						if(!assetTransition.check){
							action.html('&nbsp;');
						} 
					}
					var commentIcon = $("#icon_"+assetTransition.id)
					if(commentIcon){
						if(!assetTransition.showCommentIcon){
							commentIcon.html('&nbsp;');
						}else{
							var link = document.createElement('a');
							link.href = '#'
							link.id = assetTransition.id
							link.onclick = function(){$('#createAssetCommentId').val( assetTransition.id );new Ajax.Request('../assetEntity/listComments?id='+this.id,{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog(e,'action');}})} //;return false
							link.innerHTML = "<img src=\"../images/skin/database_table_red.png\" border=\"0px\">"
							commentIcon.html(link);
						} 
					}
					var application = $("#application_"+assetTransition.id)
					if(application){
						application.html( assetTransition.application );
					}
					var owner = $("#appOwner_"+assetTransition.id)
					if(owner){
						owner.html( assetTransition.appOwner );
					}
					var sme = $("#appSme_"+assetTransition.id)
					if(sme){
						sme.html( assetTransition.appSme );
					}
					var assetName = $("#assetName_"+assetTransition.id)
					if(assetName){
						assetName.html( assetTransition.assetName );
					}
					var tdIdslength = assetTransition.tdId.length
					for(j = 0; j< tdIdslength ; j++){
						var transition = assetTransition.tdId[j]
						var transTd = $("#"+transition.id)
						transTd.attr("class",transition.cssClass )
					}
				}
			}
		} else {
			location.reload(false);
			//timedRefresh('never')
		}
	} catch(ex){
		location.reload(false);
	}
}

function changeState(){
	timedRefresh('never')
	var assetArr = new Array();
	var totalAsset = ${assetEntityList.id};
	var j=0;
	for(i=0; i< totalAsset.size() ; i++){
		if($('#checkId_'+totalAsset[i]) != null){
			var booCheck = $('#checkId_'+totalAsset[i]).is(':checked');
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
			${remoteFunction(controller:'assetEntity',action:'saveComment', params:'\'assetEntity.id=\' + assetId +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'addCommentsToList(e)')}
		}else{
			${remoteFunction(controller:'assetEntity',action:'updateComment', params:'\'id=\' + assetId +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'updateCommentsOnList(e)')}
		}
		}else{
			alert('Please enter resolution');
			return false;
		}
	}else{
		if(formName == "createCommentForm"){
			${remoteFunction(controller:'assetEntity',action:'saveComment', params:'\'assetEntity.id=\' + assetId +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'addCommentsToList(e)')}
		}else{
			${remoteFunction(controller:'assetEntity',action:'updateComment', params:'\'id=\' + assetId +\'&comment=\'+document.forms[formName].comment.value +\'&isResolved=\'+document.forms[formName].isResolved.value +\'&resolution=\'+document.forms[formName].resolution.value +\'&commentType=\'+document.forms[formName].commentType.value +\'&mustVerify=\'+document.forms[formName].mustVerify.value', onComplete:'updateCommentsOnList(e)')}
		}
	}
}
	function setAssetId(assetId){
		$("#createAssetCommentId").val(assetId)
	}
</script>
</head>
<body>
<div title="Change Status" id="changeStatusDialog"
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
			onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)submitAction()" /></td>
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
<g:if test="${browserTest}">
<div class="tableContainerIE">
</g:if>
<g:else>
<div class="tableContainer">
</g:else>

<table cellpadding="0" cellspacing="0"  style="border:0px;">
	<thead>
	<tr>
	<jsec:hasAnyRole in="['ADMIN','MANAGER']">
	<td>
	<input type="button" value="State..." onclick="changeState()" title="Change State"/>
	</td>
	</jsec:hasAnyRole>
	<td>&nbsp;</td>
			
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
		 <jsec:hasAnyRole in="['ADMIN','MANAGER']">	<th style="padding-top:50px;">Actions <a href="#" onclick="selectAll()" ><u style="color:blue;">All</u></a></th></jsec:hasAnyRole>
			
			<th style="padding-top:50px;">Issue</th>
			
			 <g:sortableColumn style="padding-top:50px;" property="application"  title="Application" params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]"/>
			
			<g:sortableColumn style="padding-top:50px;" property="app_owner" title="App Owner"  params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]" />

			<g:sortableColumn style="padding-top:50px;" property="app_sme" title="App SME" params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]"/>

			<g:sortableColumn style="padding-top:50px;" property="asset_name" title="Asset Name" params="['projectId':projectId,'application':appValue,'appOwner':appOwnerValue,'appSme':appSmeValue]"/>
			
			<g:if test="${browserTest}">
			<g:each in="${processTransitionList}"  var="task">
				<th class="verticaltext" title="${task.header}">${task?.header}</th>
			</g:each>
			</g:if>
			<g:else>
			<th style="padding-left: 0px" colspan="${headerCount}"><embed src="${createLinkTo(dir:'templates',file:'headerSvg.svg')}" type="image/svg+xml" width="${headerCount*21.73}" height="102"/></th>
			</g:else>

		</tr>
	</thead>
	<tbody>
		<g:if test="${assetEntityList}">
		<g:each in="${assetEntityList}" var="assetEntity">
			<tr id="assetRow_${assetEntity.id}" >
			<jsec:hasAnyRole in="['ADMIN','MANAGER']">	
			<td id="action_${assetEntity.id}">
				
					<g:if test="${assetEntity.checkVal == true}">
						<g:checkBox name="checkChange" id="checkId_${assetEntity.id}" onclick="timedRefresh('never')"></g:checkBox> 
						<g:remoteLink action="getTask" params="['assetEntity':assetEntity.id]"	onComplete="showChangeStatusDialog(e);">
							<img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}"	border="0px">
						</g:remoteLink>
					</g:if>
					<g:else>&nbsp;</g:else>
				
			</td>
			</jsec:hasAnyRole>
			<td id="icon_${assetEntity.id}">
				<g:if test="${AssetComment.find('from AssetComment where assetEntity = '+assetEntity.id+' and commentType = ? and isResolved = ?',['issue',0])}">
					<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntity.id}" before="setAssetId('${assetEntity.id}');"	onComplete="listCommentsDialog( e ,'action');">
						<img src="${createLinkTo(dir:'images/skin',file:'database_table_red.png')}"	border="0px">
					</g:remoteLink>
				</g:if>
				<g:else>&nbsp;</g:else>
			</td>
			<td id="application_${assetEntity.id}" onclick="${remoteFunction(controller:'assetEntity', action:'editShow', params:'\'id=\'+'+assetEntity.id, before:'document.showForm.id.value ='+ assetEntity.id+';document.editForm.id.value = '+ assetEntity.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${assetEntity?.application}&nbsp;</td>
			<td id="appOwner_${assetEntity.id}" onclick="${remoteFunction(controller:'assetEntity', action:'editShow', params:'\'id=\'+'+assetEntity.id, before:'document.showForm.id.value ='+ assetEntity.id+';document.editForm.id.value = '+ assetEntity.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${assetEntity?.appOwner}&nbsp;</td>
			<td id="appSme_${assetEntity.id}" onclick="${remoteFunction(controller:'assetEntity', action:'editShow', params:'\'id=\'+'+assetEntity.id, before:'document.showForm.id.value ='+ assetEntity.id+';document.editForm.id.value = '+ assetEntity.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${assetEntity?.appSme}&nbsp;</td>
			<td id="assetName_${assetEntity.id}" onclick="${remoteFunction(controller:'assetEntity', action:'editShow', params:'\'id=\'+'+assetEntity.id, before:'document.showForm.id.value ='+ assetEntity.id+';document.editForm.id.value = '+ assetEntity.id+';', onComplete:'showAssetDialog(e , \'show\')')}">${assetEntity?.assetName}&nbsp;</td>
			<g:each in="${assetEntity.transitions}" var="transition" >${transition}</g:each>
			</tr>
		</g:each>
		</g:if>
		<g:else>
			<tr><td colspan="40" class="no_records">No records found</td></tr>
		</g:else>
	</tbody>
</table>
</g:form>
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
			
			<th nowrap>Resolved</th>

			<th nowrap>Must Verify</th>

		</tr>
	</thead>
	<tbody id="listCommentsTbodyId">

	</tbody>
</table>
</div>
<jsec:hasAnyRole in="['ADMIN','MANAGER']">
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
<span class="menuButton"><a class="create" href="#"
	onclick="$('#statusId').val('');$('#createResolveDiv').css('display', 'none') ;$('#createCommentDialog').dialog('option', 'width', 700);$('#createCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();">New
Comment</a></span></div>
</jsec:hasAnyRole>
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
<jsec:hasAnyRole in="['ADMIN','MANAGER']">
<div class="buttons"><span class="button"> <input
	class="edit" type="button" value="Edit"
	onclick="commentChangeEdit('editResolveDiv','editCommentForm');$('#editCommentDialog').dialog('option', 'width', 700);$('#editCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('open');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close')" />
</span> <span class="button"> <input class="delete" type="button"
	value="Delete"
	onclick="${remoteFunction(controller:'assetEntity', action:'deleteComment', params:'\'id=\' + $(\'#commentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'action\')')}" />
</span></div>
</jsec:hasAnyRole>
</div></div>
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
				
				<input type="checkbox" id="mustVerifyEditId" name="mustVerify" value="0"
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
		onclick="${remoteFunction(controller:'assetEntity', action:'deleteComment', params:'\'id=\' + $(\'#updateCommentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'action\')')}" />
	</span></div>
</g:form></div>
<div id="showDialog" title="Show Asset Entity" style="display: none;">
<g:form controller="assetEntity" action="save" method="post" name="showForm">
	<div class="dialog" id="showDiv">
	<table id="showTable">
	</table>
	</div>
	<jsec:hasAnyRole in="['ADMIN','MANAGER']">
	<div class="buttons">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundle" value="${moveBundleInstance.id}" />
	<input type="hidden" name="clientList" value="clientList" />
	 <span class="button"><input
		type="button" class="edit" value="Edit"
		onClick="return editAssetDialog()" /></span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span>
		<span class="button"><g:actionSubmit action="remove"
		class="delete"  onclick="return confirm('Are you sure?');"
		value="Remove From Project" /></span>
		</div>
		</jsec:hasAnyRole>
</g:form></div>

<div id="editDialog" title="Edit Asset Entity" style="display: none;">
<g:form method="post" name="editForm" controller="assetEntity">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundle" value="${moveBundleInstance.id}" />	
	<input type="hidden" name="clientList" value="clientList" />
	<div class="dialog" id="editDiv">
	
	</div>
	<jsec:hasAnyRole in="['ADMIN','MANAGER']">
	<div class="buttons"><span class="button">
	<input type="button" class="save" value="Update Asset Entity" onClick="${remoteFunction(controller:'assetEntity', action:'getAssetAttributes', params:'\'assetId=\' + document.editForm.id.value ', onComplete:'callUpdateDialog(e)')}" />
	</span> <span class="button"><g:actionSubmit  
		class="delete" onclick="return confirm('Are you sure?');"
		value="Delete" /></span>
		<span class="button"><g:actionSubmit action="remove"
		class="delete"  onclick="return confirm('Are you sure?');"
		value="Remove From Project" /></span>
		</div>
		</jsec:hasAnyRole>
</g:form></div>
<g:javascript>
initialize();
timedRefresh($("#selectTimedId").val())
</g:javascript>
</body>

</html>
