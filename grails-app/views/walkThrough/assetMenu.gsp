<html>
<head>
<title>Walkthru&gt; Select Asset</title>
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'walkThrough.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'contextmenu.css')}" />
<g:javascript src="contextmenu.js" />
<g:javascript src="betterinnerhtml.js" />

<script type="text/javascript">
SimpleContextMenu.setup({'preventDefault':true, 'preventForms':false});
SimpleContextMenu.attach('container', 'myMenu');

window.onbeforeunload = validExit;

function validExit() {
	var mustSave = document.auditForm.mustSave.value;
	if(mustSave == 'true') {
		return "You have unsaved changes that will be lost. Are you sure you want to do this?";
	}
}
function validChanges() {
	var mustSave = document.auditForm.mustSave.value;
	var flag = true
	if(mustSave == 'true') {
		flag = confirm( "Changes have not been saved and will be lost.  Are you sure?" );
	}
	return flag
}
function createXMLHttpRequest(){
	try {
	    return new ActiveXObject("Msxml2.XMLHTTP");
	} catch (err1) {
	    try {
	        return new ActiveXObject("Microsoft.XMLHTTP");
	    } catch (err2){
		    try{
			    return new XMLHttpRequest();
		    } catch(err3){
		        alert("XMLHttpRequest not supported")
		        return null
	    	}
	    }
	}
}
function filterByCommentType(val) {
	document.commentsViewForm.commentType.value = val;
	document.commentsViewForm.sort.value = 'desc';
	document.commentsViewForm.orderType.value = 'comment';
	document.commentsViewForm.action = "getComments"
	document.commentsViewForm.submit()
	<%--var assetId = document.commentForm.assetId.value;
	var commentType = document.commentsViewForm.commentType.value;
	var sort = document.commentsViewForm.sort.value;
	var orderType = document.commentsViewForm.orderType.value;
	sendCommentRequest()
	${remoteFunction(action:'getComments', params:'\'id=\' + document.commentForm.assetId.value +\'&commentType=\'+document.commentsViewForm.commentType.value +\'&sort=\'+document.commentsViewForm.sort.value +\'&orderType=\'+document.commentsViewForm.orderType.value', onComplete:'updateViewComment( e )')}; --%>
	return false;	
  }
function missingAsset( type, id, message ){
	if( validChanges() == true ) {
		document.auditForm.mustSave.value = 'false'
		if(confirm(message)){
			document.auditForm.action = "missingAsset"
			document.auditForm.submit(); 
		<%--var xmlHttpReq = createXMLHttpRequest()
		xmlHttpReq.open("post", "missingAsset?id="+id+"&type="+type, false);
		xmlHttpReq.send(null);
		var serverResponse = xmlHttpReq.responseText;
		updateMissingAsset(xmlHttpReq,type,id)
		${remoteFunction(action:'missingAsset', params:'\'id=\' + id +\'&type=\'+type', onComplete:'updateMissingAsset(e,type,id)')} --%>
		}
	}
}
function updateMissingAsset( e, type, id ){
	if(e.responseText == "success"){
		var link = "<a href='#' class='button big' onclick=\"missingAsset";
		if(type == 'create'){
			link +="('resolve','"+id+"', 'Resolve missing asset issue. Are you sure?')\">Missing Asset Found </a>";
		} else {
			link +="('create','"+id+"', 'Mark asset as missing. Are you sure?')\" >Mark Asset Missing </a>";
		}
		BetterInnerHTML(getObject('missingAsset'),link);
	}
}
function commentSelect( cmtVal ) {
	if ( cmtVal == 'Select Common Comment' ) {
		document.commentForm.comments.value = '';
	} else {
		document.commentForm.comments.value = cmtVal;
	}
}
    
function callUpdateComment( e, type ) {
	var data = eval('(' + e.responseText + ')');
	if ( data ) {
		<%--var assetId = document.commentForm.assetId.value;
		var comment = escape(document.commentForm.comments.value);
		var xmlHttpReq = createXMLHttpRequest()
		xmlHttpReq.open("post", "saveComment?id="+assetId+"&comment="+comment+"&commentType="+type, false);
		xmlHttpReq.send(null);
		var serverResponse = xmlHttpReq.responseText;
		callAssetMenu() --%>
		document.commentForm.commentType.value = type
		document.commentForm.submit()
		<%-- ${remoteFunction(action:'saveComment', params:'\'id=\' + document.commentForm.assetId.value +\'&comment=\'+escape(document.commentForm.comments.value) +\'&commentType=\'+type', onComplete:'callAssetMenu()')}; --%>
		return true;
	} else {
		alert( type +" already exists. " );
		return false;
    }
}
    
function validateCommentSelect() {
	var commentValue = document.commentForm.comments.value;
	if ( commentValue ) {
		return true;
	} else {
		alert(" Please Select Common Comment ");
		return false;
	}
}
var mustSave = false;
function setMustSave( changed, actual, type, attribute ){
	if( changed != actual ) {
		mustSave = true;
		document.auditForm.mustSave.value = mustSave;
		getObject("front1CompleteId").style.backgroundColor = 'green';
		getObject("front1CompleteId").style.color = '#FFF';
		getObject("front1SaveId").style.backgroundColor = 'green'
		getObject("front1SaveId").style.color = '#FFF';
		getObject("front2CompleteId").style.backgroundColor = 'green';
		getObject("front2CompleteId").style.color = '#FFF';
		getObject("front2SaveId").style.backgroundColor = 'green'
		getObject("front2SaveId").style.color = '#FFF';
		getObject("rearCompleteId").style.backgroundColor = 'green';
		getObject("rearCompleteId").style.color = '#FFF';
		getObject("rearSaveId").style.backgroundColor = 'green'
		getObject("rearSaveId").style.color = '#FFF';
		getObject("mainSaveId").style.backgroundColor = 'green'
		getObject("mainSaveId").style.color = '#FFF';
		getObject("mainCompleteId").style.backgroundColor = 'green'
		getObject("mainCompleteId").style.color = '#FFF';
		document.auditForm.generalCommentId.value = (document.auditForm.generalCommentId.value + attribute+" form "+actual+" to "+ changed +", " )
	}
}
//To move the option to Up/Down in list boxes
function moveOption( objectId, actual, type, actionType ){
	var optionList = getObject(objectId+'Id').options;
    var selectedIndex = getObject(objectId+'Id').selectedIndex
    var selectedObject = getObject(objectId+'Id')
    var selectedValue = selectedObject.value
    if( actionType != 'down'){
    	if(selectOption(selectedObject, Number(selectedValue)+1) != true) {
    		selectOption(selectedObject, Number(selectedValue)+8)
    	}
    } else {
    	if(selectOption(selectedObject, Number(selectedValue)-1) != true ){
    		selectOption(selectedObject, Number(selectedValue)-8)
    	}		
    }
    setMustSave( getObject(objectId+'Id').value, actual, type ,objectId )
}
//To change the selected option in listBoxes
function selectOption(selectedObject, selectedValue) {
	var flag = false
	for (var x = 0; x < selectedObject.length; x++) { 
		if (selectedObject.options[x].value == (selectedValue) ) { 
			selectedObject.options[x].selected = true; 
			flag = true
	    } 
	}
	return flag 
}

function callAssetMenu() {
	document.commentForm.selectCmts.value = '';
	document.commentForm.comments.value = '';
	location.href = "#asset_menu";
}

function populateComments() {
	if(validChanges() == true ) {
		location.href="#view_comments"
		document.auditForm.mustSave.value = 'false'
		document.commentsViewForm.commentType.value = 'all';
		document.commentsViewForm.sort.value = 'desc';
		document.commentsViewForm.orderType.value = 'commentType';
		<%--var assetId = document.commentForm.assetId.value;
		var commentType = document.commentsViewForm.commentType.value;
		var sort = document.commentsViewForm.sort.value;
		var orderType = document.commentsViewForm.orderType.value;
		sendCommentRequest() --%>
		document.commentsViewForm.action = "getComments"
		document.commentsViewForm.submit()
	}
	<%--${remoteFunction(action:'getComments', params:'\'id=\' + document.commentForm.assetId.value +\'&commentType=\'+document.commentsViewForm.commentType.value +\'&sort=\'+document.commentsViewForm.sort.value +\'&orderType=\'+document.commentsViewForm.orderType.value', onComplete:'updateViewComment( e )')}; --%>
	
}

function updateViewComment( e ) {
	var assetComments = e.responseText;
	document.commentForm.selectCmts.value = '';
	document.commentForm.comments.value = '';
	BetterInnerHTML(getObject('listCommentsTbodyId'),assetComments);
}

function sortCommentList(orderType) {
	var sortOrder = document.commentsViewForm.sort.value;
	document.commentsViewForm.orderType.value = orderType;
	if ( sortOrder == 'desc') {
		document.commentsViewForm.sort.value = 'asc';
	} else {
		document.commentsViewForm.sort.value = 'desc';
	}
	document.commentsViewForm.action = "getComments"
	document.commentsViewForm.submit()
	//sendCommentRequest()
	<%--${remoteFunction(action:'getComments', params:'\'id=\' + document.commentForm.assetId.value +\'&commentType=\'+document.commentsViewForm.commentType.value+\'&sort=\'+document.commentsViewForm.sort.value+\'&orderType=\'+document.commentsViewForm.orderType.value', onComplete:'updateViewComment( e )')}; --%>
}
<%--
function getModels(){
	var manufacturer = document.auditForm.manufacturerId.value
	var device = document.auditForm.kvmDeviceId.value
	//${remoteFunction(action:'getModels', params:'\'manufacturer=\' + manufacturer +\'&device=\'+device ', onComplete:'updateModels( e )')};
}
function updateModels( e ){
	var models = eval('(' + e.responseText + ')');
	var length = models.length
	if(length > 0){
		document.auditForm.modelTdId.innerHtml = "<select type=\"text\" name=\"model\" id=\"modelId\" />"
		var modelObj = document.auditForm.modelTdId
		for(i = 0; i < length; i++){
			var model = models[i]
			var option = document.createElement("option")
			option.value = model.name
			option.innerHTML = model.name
			modelObj.append(option)
		} 
	} else {
		document.auditForm.modelTdId.innerHtml = "<input type=\"text\" name=\"model\" id=\"modelId\" >"
	}
} --%>
function sendCommentRequest(){
	var assetId = document.commentForm.assetId.value;
	var commentType = document.commentsViewForm.commentType.value;
	var sort = document.commentsViewForm.sort.value;
	var orderType = document.commentsViewForm.orderType.value;
	var xmlHttpReq = createXMLHttpRequest()
	xmlHttpReq.open("post", "getComments?id="+assetId+"&commentType="+commentType+"&sort="+sort+"&orderType="+orderType, false);
	xmlHttpReq.send(null);
	var serverResponse = xmlHttpReq.responseText;
	updateViewComment( xmlHttpReq )
}
function checkComments(type) {
	var assetId = document.commentForm.assetId.value;
	var comment = escape(document.commentForm.comments.value);
	var xmlHttpReq = createXMLHttpRequest()
	xmlHttpReq.open("post", "validateComments?id="+assetId+"&comment="+comment+"&commentType="+type, false);
	xmlHttpReq.send(null);
	var serverResponse = xmlHttpReq.responseText;
	callUpdateComment( xmlHttpReq, type)
	<%--${remoteFunction(action:'validateComments', params:'\'id=\' + document.commentForm.assetId.value +\'&comment=\'+escape(document.commentForm.comments.value) +\'&commentType=\'+document.commentForm.commentType.value', onComplete:'callUpdateComment( e, \'comment\' )')} --%>
}
</script>
</head>
<body>
	<g:form action="saveAndCompleteAudit" method="post" name="auditForm" >
		<div class=qvga_border>
		<a name="asset_menu"></a>
		<div class=title>Walkthru&gt; Asset Menu</div>
		<div class=input_area>
		<input type="hidden" name="id" value="${assetEntity.id}" />
		<input type="hidden" name="submitType" id="submitTypeId">
		<input type="hidden" name="room" value="${room}">
		<input type="hidden" name="rack" value="${rack}">
		<input type="hidden" name="location" value="${location}">
		<input type="hidden" name="moveBundle" value="${moveBundle}">
		<input type="hidden" name="generalComment" id="generalCommentId" value="Asset changed: ">
		<input type="hidden" id="mustSave" name="mustSave" value=""/>
		<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
		<div style="FLOAT: right"><a class=button href="selectAsset?moveBundle=${moveBundle}&location=${location}&room=${room}&rack=${rack}">Asset List</a></div>
		<table>
			<tbody>
				<tr>
					<td class=label>Asset Tag:</td>
					<td class=field>${assetEntity?.assetTag}</td>
				</tr>
				<tr>
					<td class=label>Asset Name:</td>
					<td class=field>${assetEntity?.assetName}</td>
				</tr>
			</trody>
		</table>
		<div style="MARGIN-TOP: 15px" align=center>
			<a class="button big" href="#asset_front1">Front Audit</a> <BR style="MARGIN-TOP: 6px">
			<a class="button big" href="#asset_rear1">Rear Audit</a> <BR style="MARGIN-TOP: 6px">
			<a class="button big"  onclick="populateComments();">Issues/Comments</a> <BR style="MARGIN-TOP: 6px">
			<span id="missingAsset" >
				<g:if test="${AssetComment.find('from AssetComment where assetEntity = '+ assetEntity?.id +' and commentType = ? and isResolved = ? and commentCode = ?' ,['issue',0,'ASSET_MISSING'])}">
					<input name="type" value="resolve" type="hidden"/>
					<a href="#" class="button big" onclick="missingAsset('resolve', '${assetEntity?.id}','Resolve missing asset issue. Are you sure?')">Missing Asset Found</a>
				</g:if>
				<g:else>
					<input name="type" value="create" type="hidden"/>
					<a href="#" class="button big" onclick="missingAsset('create', '${assetEntity?.id}','Mark asset as missing. Are you sure?')">Mark Asset Missing </a>
				</g:else>
			</span>
			<div style="MARGIN-TOP: 10px">
				<div class=thefield align=center>
					<a class="button" id="mainSaveId" href="#select_asset" onClick="document.auditForm.submitType.value='save';document.auditForm.mustSave.value='false';document.auditForm.submit();">Save</a>&nbsp;&nbsp;&nbsp;
			        <a class="button" id="mainCompleteId" href="#select_asset"  onClick="document.auditForm.submitType.value='complete';document.auditForm.mustSave.value='false';document.auditForm.submit();">Completed</a>
				</div>
			</div>
		</div>
      </div>
      </div>
		<div class="gap"></div>
	<!-- Walkthru Asset:Front pg 1-->
		<div class="qvga_border">
			<a name="asset_front1"></a>
			<div class="title">Walkthru&gt; Front (1)</div>
			<div class="input_area">
			<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
			<div style="float:right;"><a class="button" href="#asset_menu">Asset Menu</a></div>
			<br class="clear"/>
			<table>
			<tr>
				<td class="label">Asset Tag:</td>
				<td class="field">${assetEntity?.assetTag}</td>
			</tr>
			
			<tr>
				<td class="label">Asset Name:</td>
				<td class="field">
					<input type="text" class="text" name="assetName" value="${assetEntity?.assetName}" size=20 maxsize=50 
							onchange="setMustSave(this.value,'${assetEntity?.assetName}','front1', this.name)">
				</td>
			</tr>
			
			<tr>
				<td class="label">Serial #:</td>
				<td class="field">
					<input type="text" class="text" name="serialNumber" value="${assetEntity?.serialNumber}" size=20 maxsize=50 
							onchange="setMustSave(this.value,'${assetEntity?.serialNumber}','front1', this.name)">
				</td>
			</tr>
			
			<tr>
				<td class="label">Device Type:</td>
				<td class="field">
				
				<!-- <g:select from="${com.tdssrc.eav.EavAttributeOption.findAllByAttribute(com.tdssrc.eav.EavAttribute.findByAttributeCode('assetType'))?.value}" noSelection="['':'Select']" id="kvmDeviceId" name="kvmDevice" value="${assetEntity?.kvmDevice}" 
						onchange="setMustSave(this.value,'${assetEntity?.kvmDevice}','front1', this.name);"/> -->
					<input type="text" value="${assetEntity?.kvmDevice}" id="kvmDeviceId" name="kvmDevice" 
								onchange="setMustSave(this.value,'${assetEntity?.kvmDevice}','front1', this.name);">
			</tr>
			
			<tr>
				<td class="label">Manufacturer:</td>
				<td class="field">
				<g:if test="${Manufacturer.list()}">
				<!--  <g:select name="manufacturer" from="${Manufacturer.list()}" value="${assetEntity?.manufacturer}" id="manufacturerId" 
					noSelection="['':'']"  onchange="setMustSave(this.value,'${assetEntity?.manufacturer}','front1', this.name);getModels();"/> -->
					<input type="text" name="manufacturer" id="manufacturerId" value="${assetEntity?.manufacturer}" onchange="setMustSave(this.value,'${assetEntity?.manufacturer}','front1', this.name);">
					</input>
				</g:if>
				<g:else>
				<input type="text" name="manufacturer" id="manufacturerId" value="${assetEntity?.manufacturer}" onchange="setMustSave(this.value,'${assetEntity?.manufacturer}','front1', this.name);">
				</g:else>
				</td>
			</tr>
			
			<tr>
				<td class="label">Model:</td>
				<td class="field" id="modelTdId">
				<g:if test="${Model.list()}">
				<!-- <g:select name="model" from="${Model.list()}" id="modelId" noSelection="['':'']" 
					value="${assetEntity?.model}" onchange="setMustSave(this.value,'${assetEntity?.model}','front1', this.name)"/> -->
					<input type="text" id="modelId" name="model" value="${assetEntity?.model}" onchange="setMustSave(this.value,'${assetEntity?.model}','front1', this.name)" ></input>
				</g:if>
				 <g:else>
				<input type="text" name="model" id="modelId" value="${assetEntity?.model}" onchange="setMustSave(this.value,'${assetEntity?.model}','front1', this.name)">
				</g:else>
				</td>
			</tr>
			
			<tr>
				<td class="label">Rails:</td>
				<td class="field">
				 <!-- <refcode:select domain="railType" noSelection="['':'']" id="railTypeId" name="railType" value="${assetEntity?.railType}" 
				onchange="setMustSave(this.value,'${assetEntity?.railType}','front1', this.name)"/> -->
				<input type="text" name="railType" value="${assetEntity?.railType}" id="railTypeId" onchange="setMustSave(this.value,'${assetEntity?.railType}','front1', this.name)" >
				</input>
				</td>
			</tr>
			</table>
			
			<div style="margin-top:10px;">
			   <div class="thefield" align="center">
			      <a class="button" href="#asset_front2">Next</a>&nbsp;&nbsp;&nbsp;
			      <a class="button" href="#select_asset" id="front1SaveId" onClick="document.auditForm.submitType.value='save';document.auditForm.mustSave.value='false';document.auditForm.submit();">Save</a>&nbsp;&nbsp;&nbsp;
			      <a class="button" href="#select_asset" id="front1CompleteId" onClick="document.auditForm.submitType.value='complete';document.auditForm.mustSave.value='false';document.auditForm.submit();">Completed</a>
			   </div>
			</div>
			
			</div>
		</div>
		<!-- end of Walkthru Asset:Front (1) -->
		<div class="gap"></div>
		<!-- Walkthru Asset:Front pg 2-->
		<div class="qvga_border">
			<a name="asset_front2"></a>
			<div class="title">Walkthru&gt; Front (2)</div>
			<div class="input_area">
			
			<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
			<div style="float:right;"><a class="button" href="#asset_menu">Asset Menu</a></div>
			<br class="clear"/>
			
			<table>
			<tr>
				<td class="label">Asset Tag:</td>
				<td class="field">${assetEntity?.assetTag}</td>
			</tr>
			<tr>
				<td class="label">Source Rack:</td>
				<td class="field">${assetEntity?.sourceRack}</td>
			</tr>
			
			<tr>
				<td class="label">U-Position:</td>
				<td class="field" nowrap>
				<g:select name="sourceRackPosition" from="${[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,'Undefined']}" 
							id="sourceRackPositionId" value="${assetEntity?.sourceRackPosition}" onchange="setMustSave(this.value,'${assetEntity?.sourceRackPosition}','front2', this.name)"/>
				<img src="${createLinkTo(dir:'images',file:'plus.gif')}" height="18" onclick="moveOption('sourceRackPosition','${assetEntity?.sourceRackPosition}','front2','up')"/>
				<img src="${createLinkTo(dir:'images',file:'minus.gif')}" height="18" onclick="moveOption('sourceRackPosition','${assetEntity?.sourceRackPosition}','front2','down')"/>
				</td>
			</tr>
			
			<tr>
				<td class="label">U-Size:</td>
				<td class="field">
				<g:select name="usize" from="${[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,'Undefined']}"   
							id="usizeId" value="${assetEntity?.usize}" onchange="setMustSave(this.value,'${assetEntity?.usize}','front2', this.name)"/>
				<img src="${createLinkTo(dir:'images',file:'plus.gif')}" height="18" onclick="moveOption('usize','${assetEntity?.usize}','front2','up')"/>
				<img src="${createLinkTo(dir:'images',file:'minus.gif')}" height="18" onclick="moveOption('usize','${assetEntity?.usize}','front2','down')"/>
				</td>
			</tr>
			
			<tr>
				<td class="label">Need Asset Tag?</td>
				<td class="field">
					<input type="radio" name="needAssetTag" id="needAssetTagYes" value="Y" onclick="setMustSave(this.value,'${assetEntity?.usize}','front2', this.name)"><label for="needAssetTagYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="needAssetTag" id="needAssetTagNo" value="N" onclick="setMustSave(this.value,'${assetEntity?.usize}','front2', this.name)" checked><label for="needAssetTagNo">No</label>
				</td>
			</tr>
			<tr>
				<td class="label">Has Amber Lights?</td>
				<td class="field">
					<input type="radio" name="hasAmber" id="hasAmberYes" value="Y" onclick="setMustSave(this.value,'${assetEntity?.usize}','front2', this.name)" ><label for="hasAmberYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="hasAmber" id="hasAmberNo" value="N" onclick="setMustSave(this.value,'${assetEntity?.usize}','front2', this.name)" checked><label for="hasAmberNo">No</label>
				</td>
			</tr>
			<tr>
				<td class="label">Stuff Stacked On Top?</td>
				<td class="field">
					<input type="radio" name="stuffOnTop" id="stuffOnTopYes" onclick="setMustSave(this.value,'${assetEntity?.usize}','front2', this.name)" value="Y"><label for="stuffOnTopYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="stuffOnTop" id="stuffOnTopNo" onclick="setMustSave(this.value,'${assetEntity?.usize}','front2', this.name)" value="N" checked><label for="stuffOnTopNo">No</label>
				</td>
			</tr>
			<tr>
				<td class="label">Is Powered OFF?</td>
				<td class="field">
					<input type="radio" name="poweredOff" id="poweredOffYes" onclick="setMustSave(this.value,'${assetEntity?.usize}','front2', this.name)" value="Y" ><label for="poweredOffYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="poweredOff" id="poweredOffNo" onclick="setMustSave(this.value,'${assetEntity?.usize}','front2', this.name)" value="N" checked><label for="poweredOffNo">No</label>
				</td>
			</tr>
			</table>
			
			<div style="margin-top:20px;">
			   <div class="thefield" align="center">
			      <a class="button" href="#asset_rear1">Next</a>&nbsp;&nbsp;
			      <a class="button" href="#asset_front1">Back</a>&nbsp;&nbsp;
			      <a class="button" href="#select_asset" id="front2SaveId"  onClick="document.auditForm.submitType.value='save';document.auditForm.mustSave.value='false';document.auditForm.submit();">Save</a>&nbsp;&nbsp;&nbsp;
			      <a class="button" href="#select_asset"  id="front2CompleteId" onClick="document.auditForm.submitType.value='complete';document.auditForm.mustSave.value='false';document.auditForm.submit();">Completed</a>
			   </div>
			</div>
			
			</div>
		</div>
		<!-- end of Walkthru Asset:Front (2) -->
		
		<div class="gap"></div>
		
		<!-- Walkthru Asset:Rear  -->
		<div class="qvga_border">
		<a name="asset_rear1"></a>
		<div class="title">Walkthru&gt; Rear</div>
		<div class="input_area">
		
		<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
		<div style="float:right;"><a class="button" href="#asset_menu">Asset Menu</a></div>
		<br class="clear"/>
		
		<table>
		<tr>
			<td class="label">Asset Tag:</td>
			<td class="field">${assetEntity?.assetTag}</td>
		</tr>
		
		<tr>
			<td class="label">PDU Qty/Type:</td>
			<td class="field">
			<g:select name="powerPort" from="${[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,24,32,40,48,56,64,72,80,88,96]}" value="${assetEntity?.powerPort}"
				onchange="setMustSave(this.value,'${assetEntity?.powerPort}','rear', this.name)"/>
			&nbsp;&nbsp;
				<!-- <refcode:select domain="powerType" noSelection="['':'Unknown']" id="powerType" name="powerType" value="${assetEntity?.powerType}" 
				onchange="setMustSave(this.value,'${assetEntity?.powerType}','rear', this.name)"/> -->
				<input type="text" id="powerType" name="powerType" value="${assetEntity?.powerType}" onchange="setMustSave(this.value,'${assetEntity?.powerType}','rear', this.name)" ></input>
				
			</td>
		</tr>
		
		<tr>
			<td class="label">NIC Qty:</td>
			<td class="field">
			<g:select name="nicPort" from="${[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,24,32,40,48,56,64,72,80,88,96]}" value="${assetEntity?.nicPort}"
						id="nicPortId" onchange="setMustSave(this.value,'${assetEntity?.nicPort}','rear', this.name)"/>
		     	<img src="${createLinkTo(dir:'images',file:'plus.gif')}" height="18" onclick="moveOption('nicPort','${assetEntity?.nicPort}','rear','up')"/>
			 	<img src="${createLinkTo(dir:'images',file:'minus.gif')}" height="18" onclick="moveOption('nicPort','${assetEntity?.nicPort}','rear','down')"/>
			</td>
		</tr>
		
		<tr>
			<td class="label">Fiber Qty/Type:</td>
			<td class="field">
				<g:select name="hbaPort" from="${[0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,24,32,40,48,56,64,72,80,88,96]}" value="${assetEntity?.hbaPort}"
						onchange="setMustSave(this.value,'${assetEntity?.hbaPort}','rear', this.name)"/>
		        &nbsp;&nbsp;
			<!-- <refcode:select domain="fiberCabinet" noSelection="['':'']" id="fiberCabinet" name="fiberCabinet" value="${assetEntity?.fiberCabinet}" 
				onchange="setMustSave(this.value,'${assetEntity?.fiberCabinet}','rear', this.name)"/> -->
				<input type="text" id="fiberCabinet" name="fiberCabinet" value="${assetEntity?.fiberCabinet}" onchange="setMustSave(this.value,'${assetEntity?.fiberCabinet}','rear', this.name)" ></input>
			</td>
		</tr>
		</table>
		<table>
		<tr>
		        <td class="label"><label for=attachedKVM>Attached to KVM:</label></td>
		        <td class="field">
		        <g:if test="${assetEntity?.kvmDevice || assetEntity?.kvmPort }">
						<input type="checkbox" name=attachedKVM id="attachedKVM" onclick="setMustSave(this.value,'${assetEntity?.powerPort}','rear', this.name)" checked="checked">
				</g:if>
				<g:else>
						<input type="checkbox" name=attachedKVM onclick="setMustSave(this.value,'${assetEntity?.powerPort}','rear', this.name)" id="attachedKVM">
				</g:else>
		        </td>
		</tr>
		<tr>
		        <td class="label"><label for="hasILO">Has ILO Mgmt:</label></td>
		        <td class="field">
		        <g:if test="${assetEntity?.remoteMgmtPort}">
					<input type="checkbox" name=hasILO id="hasILO" onclick="setMustSave(this.value,'${assetEntity?.powerPort}','rear', this.name)" checked="checked">
				</g:if>
				<g:else>
					<input type="checkbox" name=hasILO onclick="setMustSave(this.value,'${assetEntity?.powerPort}','rear', this.name)" id="hasILO">
				</g:else>
		        </td>
		</tr>
		<tr>
		        <td class="label">Has Obstruction?</td>
		        <td class="field">
		                <input type="radio" name="moveCables" id="moveCablesYes" onclick="setMustSave(this.value,'${assetEntity?.powerPort}','rear', this.name)" value="Y"><label for="moveCablesYes">Yes</label>
		                &nbsp;&nbsp;
		                <input type="radio" name="moveCables" id="moveCablesNo" value="N" onclick="setMustSave(this.value,'${assetEntity?.powerPort}','rear', this.name)" checked><label for="moveCablesNo">No</label>
		        </td>
		</tr>
		</table>      
		
		<div style="margin-top:20px;">
		   <div class="thefield" align="center">
		      <a class="button" href="#asset_front2">Back</a>&nbsp;&nbsp;
		      <a class="button" href="#select_asset" id="rearSaveId" onClick="document.auditForm.submitType.value='save';document.auditForm.mustSave.value='false';document.auditForm.submit();">Save</a>&nbsp;&nbsp;&nbsp;
			  <a class="button" href="#select_asset" id="rearCompleteId" onClick="document.auditForm.submitType.value='complete';document.auditForm.mustSave.value='false';document.auditForm.submit();">Completed</a>
		   </div>
		</div>
		
		</div>
		</div>
		</g:form>
		<!-- end of Walkthru Asset:Back -->
		
	    <div class="gap"></div>
		<!-- Walkthru Asset:Comments -->
		<div class="qvga_border">
		<a name="comments"></a>
		<div class="title">Walkthru&gt; Issues &amp; Comments</div>
		<div class="input_area">
		
		<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
		<div style="float:right;"><a class="button" href="#asset_menu">Asset Menu</a></div>
		<br class="clear"/>
		<g:form name="commentForm" action="saveComment" method="post">	
			<table>
			<tr>
				<input type="hidden" name="assetId" id="assetId" value="${assetEntity.id}" />
				<input type="hidden" name="id" value="${assetEntity.id}" />
				<input type="hidden" name="commentType" id="commentType" value="comment" />
				<input type="hidden" name="instructionType" id="instructionType" value="instruction" />
				<input type="hidden" name="issueType" id="issueType" value="issue" />
				<input type="hidden" name="room" value="${room}">
				<input type="hidden" name="rack" value="${rack}">
				<input type="hidden" name="location" value="${location}">
				<input type="hidden" name="moveBundle" value="${moveBundle}">
				<td class="label">Asset Tag:</td>
				<td class="field">${assetEntity.assetTag}</td>
			</tr>
			</table>
			<select id="selectCmts" name="selectCmts" style="width: 200px;" onChange="return commentSelect(this.value);">
					<option value="">Select Common Comment</option>
				<g:each in="${walkthruComments}" status="i" var="messages">
    				<option value="${messages}">${messages}</option>
				</g:each>
			</select>
			<br/>
			
			<textarea name="comments" id="comments" rows="6" cols="8" value=""></textarea>
			</textarea>
			<br />
			<br />
			
			<label>Save As:</label>
			<br />
			<div style="float:center;">
			   	<a class="button"  href="#comments" onclick="var booConfirm = validateCommentSelect();if(booConfirm) checkComments('comment');">COMMENT</a>&nbsp;&nbsp;
				<a class="button"  href="#comments" onclick="var booConfirm = validateCommentSelect();if(booConfirm) checkComments('instruction');">INSTRUCTION</a>&nbsp;&nbsp;
				<a class="button"  href="#comments" onclick="var booConfirm = validateCommentSelect();if(booConfirm) checkComments('issue');">ISSUE</a>
			</div>
			<br class="clear"/>
			
			<br />
			
			<a class="button" href="#view_comments" onclick="populateComments();">View Issues &amp; Comments</a>
		</g:form>
		</div>
		</div>
		<!-- end of Walkthru Asset -Comments-->
		
		<div class="gap"></div>
		<!-- Walkthru Asset:View Comments -->
		<div class="qvga_border">
		<a name="view_comments"></a>
		<div class="title">Walkthru&gt; View Issue&amp;Comments</div>
		<div class="input_area">
		
		<div style="FLOAT: left"><a class=button href="#asset_menu">Asset Menu</a></div>
		<div style="float:right;"><a class="button" href="#comments">Add Comments</a></div>
		<br class="clear"/>
		<g:form action="issuesandcommentsview" name="commentsViewForm" method="post">
		<input type="hidden" name="commentType" id="commentType" value="${commentType}"/>
		<input type="hidden" name="sort" id="sort" value="${sort}"/>
		<input type="hidden" name="orderType" id="orderType" value="commentType"/>
		<input type="hidden" name="id" value="${assetEntity.id}" />
		<input type="hidden" name="room" value="${room}">
		<input type="hidden" name="rack" value="${rack}">
		<input type="hidden" name="location" value="${location}">
		<input type="hidden" name="moveBundle" value="${moveBundle}">
		<table>
		
			<tr>
			    <th class="container" onclick="sortCommentList('commentType');">Type</th>
				<th onclick="sortCommentList('comment');">Text</th>
				<th>Rsvld</th>
			</tr>
		<tbody id="listCommentsTbodyId">
			<g:if test="${commentListView}">
				${commentListView}
			</g:if>
			<g:else>
				<g:each in="${AssetComment.findAll('from AssetComment where assetEntity = '+ assetEntity?.id +' order by commentType')}" status="i" var="assetCommentsInstance">
					<g:if test="${assetCommentsInstance.commentType == 'issue'}">
					<tr class="comment_font"><td>Iss</td><td>${assetCommentsInstance.comment}</td><td>
						<g:if test="${assetCommentsInstance.isResolved == 1}">
							<input type="checkbox" checked disabled/><br/>
						</g:if>
						<g:else>
							<input type="checkbox" disabled/><br/>
						</g:else>
					</td></tr>
					</g:if>
					<g:elseif test="${assetCommentsInstance.commentType == 'comment'}">
						<tr class="comment_font"><td>Cmnt</td><td>${assetCommentsInstance.comment}</td><td>&nbsp;</td></tr>
					</g:elseif>
					<g:elseif test="${assetCommentsInstance.commentType == 'instruction'}">
						<tr class="comment_font"><td>Inst</td><td>${assetCommentsInstance.comment}</td><td>&nbsp;</td></tr>
					</g:elseif>
					<g:else>
						<tr class="comment_font"><td colSpan="3" align="center" class="norecords_display">No records found</td></tr>
					</g:else>
				</g:each>
			</g:else>	
		</tbody>
		</table>
		</g:form>
        </div>
        </div>
		<div class="gap"></div>
		
			<ul id="myMenu" class="SimpleContextMenu">
				<li><a href="#" onclick="return filterByCommentType('all');">All</a></li>
				<li><a href="#" onclick="return filterByCommentType('comment');">Comment</a></li>
				<li><a href="#" onclick="return filterByCommentType('instruction');">Instruction</a></li>
				<li><a href="#" onclick="return filterByCommentType('issue');">Issue</a></li>
		   </ul>
	<script type="text/javascript">
	if('${commentCodes.needAssetTag}'){
		getObject('needAssetTagYes').checked = true
	}
	if('${commentCodes.amberLights}'){
		getObject('hasAmberYes').checked = true
	}
	if('${commentCodes.stackedOnTop}'){
		getObject("stuffOnTopYes").checked = true
	}
	if('${commentCodes.poweredOff}'){
		getObject("poweredOffYes").checked = true
	}
	if('${commentCodes.cablesMoved}'){
		getObject("moveCablesYes").checked =true
	}
	getObject("manufacturerId").value = "${assetEntity?.manufacturer}"
	getObject("modelId").value = "${assetEntity?.model}"
	if("${viewType}" == "view_comments"){
		location.href = "#view_comments"
	}
	</script>
</body>
</html>
