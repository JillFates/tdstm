<%@page import="net.transitionmanager.task.AssetComment"%>
<%@page import="net.transitionmanager.manufacturer.Manufacturer" %>
<%@page import="net.transitionmanager.model.Model" %>
<%@page import="net.transitionmanager.project.Project" %>
<html>
<head>
<title>Walkthru&gt; Asset Menu</title>
<asset:stylesheet href="css/qvga.css" />
<asset:stylesheet href="css/walkThrough.css" />
<asset:stylesheet href="css/contextmenu.css" />
<g:javascript src="contextmenu.js" />
<g:javascript src="betterinnerhtml.js" />

<script type="text/javascript">
if("${assetBundle}" && "${assetBundle}" != "${moveBundle}"){
	if(!confirm("The asset '${assetEntity?.assetName}' is not part of the bundle '${assetEntity?.moveBundle?.name}'. Do you want to proceed?")){
		window.location.href="selectRack?moveBundle=${moveBundle}&auditType=${auditType}"
	}
}
SimpleContextMenu.setup({'preventDefault':true, 'preventForms':false});
SimpleContextMenu.attach('container', 'myMenu');

window.onbeforeunload = validExit;

function validExit() {
	var mustSave = getObject("mustSaveId").value;
	if(mustSave == 'true') {
		return "You have unsaved changes that will be lost. Are you sure you want to do this?";
	}
}
function validChanges() {
	var mustSave = getObject("mustSaveId").value;
	var flag = true
	if(mustSave == 'true') {
		flag = confirm( "Changes have not been saved and will be lost.  Are you sure?" );
	}
	if(flag){
		getObject("mustSaveId").value = 'false'
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
<%--
function filterByCommentType(val) {
	document.commentsViewForm.commentType.value = val;
	document.commentsViewForm.sort.value = 'desc';
	document.commentsViewForm.orderType.value = 'comment';
	document.commentsViewForm.action = "retrieveComments"
	document.commentsViewForm.submit()
	var assetId = document.commentForm.assetId.value;
	var commentType = document.commentsViewForm.commentType.value;
	var sort = document.commentsViewForm.sort.value;
	var orderType = document.commentsViewForm.orderType.value;
	sendCommentRequest()
	${remoteFunction(action:'retrieveComments', params:'\'id=\' + document.commentForm.assetId.value +\'&commentType=\'+document.commentsViewForm.commentType.value +\'&sort=\'+document.commentsViewForm.sort.value +\'&orderType=\'+document.commentsViewForm.orderType.value', onComplete:'updateViewComment( XMLHttpRequest )')};
	return false;
} --%>
function missingAsset( type, id, message ){
	if( validChanges() == true ) {
		getObject("mustSaveId").value = 'false'
		if(confirm(message)){
			document.auditForm.action = "missingAsset"
			document.auditForm.submit();
		<%--var xmlHttpReq = createXMLHttpRequest()
		xmlHttpReq.open("post", "missingAsset?id="+id+"&type="+type, false);
		xmlHttpReq.send(null);
		var serverResponse = xmlHttpReq.responseText;
		updateMissingAsset(xmlHttpReq,type,id)
		${remoteFunction(action:'missingAsset', params:'\'id=\' + id +\'&type=\'+type', onComplete:'updateMissingAsset(XMLHttpRequest,type,id)')} --%>
		}
	}
}
<%--
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
} --%>
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
		getObject("mustSaveId").value = mustSave;
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
		if(attribute != "needAssetTag" && attribute != "hasAmber" && attribute != "stuffOnTop" && attribute != "poweredOff" && attribute != "moveCables" ){
			document.auditForm.generalComment.value = (document.auditForm.generalComment.value + attribute+" form "+actual+" to "+ changed +", " )
		}
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
<%--
function callAssetMenu() {
	document.commentForm.selectCmts.value = '';
	document.commentForm.comments.value = '';
	location.href = "#asset_menu";
}

function populateComments() {
	if(validChanges() == true ) {
		location.href="#view_comments"
		getObject("mustSaveId").value = 'false'
		document.commentsViewForm.commentType.value = 'all';
		document.commentsViewForm.sort.value = 'desc';
		document.commentsViewForm.orderType.value = 'commentType';
		var assetId = document.commentForm.assetId.value;
		var commentType = document.commentsViewForm.commentType.value;
		var sort = document.commentsViewForm.sort.value;
		var orderType = document.commentsViewForm.orderType.value;
		sendCommentRequest()
		document.commentsViewForm.action = "retrieveComments"
		document.commentsViewForm.submit()
		return true
	} else {
		return false
	}
	//${remoteFunction(action:'retrieveComments', params:'\'id=\' + document.commentForm.assetId.value +\'&commentType=\'+document.commentsViewForm.commentType.value +\'&sort=\'+document.commentsViewForm.sort.value +\'&orderType=\'+document.commentsViewForm.orderType.value', onComplete:'updateViewComment( XMLHttpRequest )')};

}

function updateViewComment( e ) {
	var assetComments = e.responseText;
	document.commentForm.selectCmts.value = '';
	document.commentForm.comments.value = '';
	BetterInnerHTML(getObject('listCommentsTbodyId'),assetComments);
}
--%>
function sortCommentList(orderType) {
	var sortOrder = document.commentsViewForm.sort.value;
	document.commentsViewForm.orderType.value = orderType;
	if ( sortOrder == 'desc') {
		document.commentsViewForm.sort.value = 'asc';
	} else {
		document.commentsViewForm.sort.value = 'desc';
	}
	var sort = document.commentsViewForm.sort.value
	var assetId = document.commentsViewForm.id.value
	var room = document.commentsViewForm.room.value
	var rack = document.commentsViewForm.rack.value
	var location = document.commentsViewForm.location.value
	var moveBundle = document.commentsViewForm.moveBundle.value
	window.location.href="retrieveComments?commentType=all&sort="+sort+"&orderType="+orderType+"&id="+assetId+"&room="+room+"&rack="+rack+"&location="+location+"&moveBundle="+moveBundle+"#view_comments"
	//document.commentsViewForm.action = "retrieveComments"
	//document.commentsViewForm.submit()
	//sendCommentRequest()
	<%--${remoteFunction(action:'retrieveComments', params:'\'id=\' + document.commentForm.assetId.value +\'&commentType=\'+document.commentsViewForm.commentType.value+\'&sort=\'+document.commentsViewForm.sort.value+\'&orderType=\'+document.commentsViewForm.orderType.value', onComplete:'updateViewComment( e )')}; --%>
}
<%--
function getModels(){
	var manufacturer = document.auditForm.manufacturerId.value
	var device = document.auditForm.kvmDeviceId.value
	//${remoteFunction(action:'retrieveModels', params:'\'manufacturer=\' + manufacturer +\'&device=\'+device ', onComplete:'updateModels( XMLHttpRequest )')};
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
}
function sendCommentRequest(){
	var assetId = document.commentForm.assetId.value;
	var commentType = document.commentsViewForm.commentType.value;
	var sort = document.commentsViewForm.sort.value;
	var orderType = document.commentsViewForm.orderType.value;
	var xmlHttpReq = createXMLHttpRequest()
	xmlHttpReq.open("post", "retrieveComments?id="+assetId+"&commentType="+commentType+"&sort="+sort+"&orderType="+orderType, false);
	xmlHttpReq.send(null);
	var serverResponse = xmlHttpReq.responseText;
	updateViewComment( xmlHttpReq )
}--%>
function checkComments(type) {
	var assetId = document.commentForm.assetId.value;
	var comment = escape(document.commentForm.comments.value);
	var xmlHttpReq = createXMLHttpRequest()
	xmlHttpReq.open("post", "validateComments?id="+assetId+"&comment="+comment+"&commentType="+type, false);
	xmlHttpReq.send(null);
	var serverResponse = xmlHttpReq.responseText;
	callUpdateComment( xmlHttpReq, type)
	<%--${remoteFunction(action:'validateComments', params:'\'id=\' + document.commentForm.assetId.value +\'&comment=\'+escape(document.commentForm.comments.value) +\'&commentType=\'+document.commentForm.commentType.value', onComplete:'callUpdateComment( XMLHttpRequest, \'comment\' )')} --%>
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
		<input type="hidden" name="hasRemoteMgmt" value="${assetEntity.hasRemoteMgmt}">
		<input type="hidden" name="generalComment" id="generalCommentId" value="Asset changed: ">
		<input type="hidden" id="mustSaveId" name="mustSave" value=""/>
		<g:if test="${location != assetEntity.sourceLocationName}">
			<input type="hidden" id="sourceLocation" name="sourceLocation" value="${location}"/>
		</g:if>
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
			</tbody>
		</table>
		<div style="MARGIN-TOP: 15px" align=center>
			<g:if test="${AssetComment.find('from AssetComment where assetEntity = '+ assetEntity?.id +' and commentType = ? and dateResolved = null and commentCode = ?' ,['issue','ASSET_MISSING'])}">
				<input name="type" value="resolve" type="hidden"/>
				<a href="#" class="button big" onclick="missingAsset('resolve', '${assetEntity?.id}','Resolve missing asset issue. Are you sure?')">Missing Asset Found</a>
			</g:if>
			<g:else>
				<a class="button big" href="#asset_front1">Front Audit</a> <BR /><BR />
				<a class="button big" href="#asset_rear1">Rear Audit</a> <BR /><BR />
				<a class="button big" href="retrieveComments?commentType=all&sort=desc&orderType=commentType&id=${assetEntity?.id}&room=${room}&rack=${rack}&location=${location}&moveBundle=${moveBundle}#view_comments"
					onclick="return validChanges();">Issues/Comments</a> <BR /><BR />
				<input name="type" value="create" type="hidden"/>
				<a href="#" class="button big" onclick="missingAsset('create', '${assetEntity?.id}','Mark asset as missing. Are you sure?')">Mark Asset Missing </a>
				 <BR /><BR />
				 <g:if test="${request.getHeader ( 'User-Agent' ).contains ( 'MSIE' )}">
				<a class="button big" href="#generate_label" >Generate Label</a>
				</g:if>
				<div style="MARGIN-TOP: 10px">
					<div class=thefield align=center>
						<a class="button" id="mainSaveId" href="#select_asset" onClick="document.auditForm.submitType.value='save';getObject('mustSaveId').value='false';document.auditForm.submit();">Save</a>&nbsp;&nbsp;&nbsp;
				        <a class="button" id="mainCompleteId" href="#select_asset"  onClick="document.auditForm.submitType.value='complete';getObject('mustSaveId').value='false';document.auditForm.submit();">Completed</a>
					</div>
				</div>
			</g:else>
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
				<td class="label">Name:</td>
				<td class="field">
					<input type="text" class="text" name="assetName" value="${assetEntity?.assetName}" size=20 maxlength="50"
							onchange="setMustSave(this.value,'${assetEntity?.assetName}','front1', this.name)">
				</td>
			</tr>

			<tr>
				<td class="label">Serial #:</td>
				<td class="field">
					<input type="text" class="text" name="serialNumber" value="${assetEntity?.serialNumber}" size=20 maxlength=50
							onchange="setMustSave(this.value,'${assetEntity?.serialNumber}','front1', this.name)">
				</td>
			</tr>

			<tr>
				<td class="label">Type:</td>
				<td class="field">

				 <g:select from="${com.tdssrc.eav.EavAttributeOption.findAllByAttribute(com.tdssrc.eav.EavAttribute.findByAttributeCode('assetType'))?.value}" noSelection="['':'Undef']" id="kvmDeviceId" name="assetType" value="${assetEntity?.assetType}"
						onchange="setMustSave(this.value,'${assetEntity?.assetType}','front1', this.name);"/>
			</tr>

			<tr>
				<td class="label">Manufact:</td>
				<td class="field">
				<g:select name="manufacturer.id" from="${Manufacturer.list()}" value="${assetEntity?.manufacturer?.id}" id="manufacturerId" optionKey="id"
					noSelection="['':'Unassigned']"  onchange="setMustSave(this.value,'${assetEntity?.manufacturer.id}','front1', this.name);getModels( this.value );"/>
				</td>
			</tr>

			<tr>
				<td class="label">Model:</td>
				<td class="field" id="modelTdId">
				<g:if test="${assetEntity?.manufacturer}">
				<g:select name="model.id" from="${Model.findAllByManufacturer( assetEntity?.manufacturer )}" value="${assetEntity?.model.id}" id="modelId" optionKey="id" noSelection="['':'Unassigned']"
					onchange="setMustSave(this.value,'${assetEntity?.model}','front1', this.name)"/>
				</g:if>
				<g:else>
				<g:select name="model" from="${assetEntity?.manufacturer}" id="modelId" noSelection="['':'Unassigned']"
					value="${assetEntity?.model}" onchange="setMustSave(this.value,'${assetEntity?.model.id}','front1', this.name)"/>
				</g:else>
				</td>
			</tr>

			<tr>
				<td class="label">Rails:</td>
				<td class="field">
				 <g:select noSelection="['':'Undef']" from="${com.tdssrc.eav.EavAttributeOption.findAllByAttribute(com.tdssrc.eav.EavAttribute.findByAttributeCode('railType'))?.value}" id="railTypeId" name="railType" value="${assetEntity?.railType}"
				 onchange="setMustSave(this.value,'${assetEntity?.railType}','front1', this.name)"/>
				<!-- <input type="text" name="railType" value="${assetEntity?.railType}" id="railTypeId" onchange="setMustSave(this.value,'${assetEntity?.railType}','front1', this.name)" >-->
				</td>
			</tr>
			</table>

			<div style="margin-top:10px;">
			   <div class="thefield" align="center">
			      <a class="button" href="#asset_front2">Next</a>&nbsp;&nbsp;&nbsp;
			      <a class="button" href="#select_asset" id="front1SaveId" onClick="document.auditForm.submitType.value='save';getObject('mustSaveId').value='false';document.auditForm.submit();">Save</a>&nbsp;&nbsp;&nbsp;
			      <a class="button" href="#select_asset" id="front1CompleteId" onClick="document.auditForm.submitType.value='complete';getObject('mustSaveId').value='false';document.auditForm.submit();">Completed</a>
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
				<td class="label">Room:</td>
				<td class="field"><input type="text" name="sourceRoom" value="${assetEntity?.sourceRoomName}" size="8" maxlength="50" onchange="setMustSave(this.value,'${assetEntity?.sourceRoomName}','front2', this.name)"/></td>
			</tr>
			<tr>
				<td class="label">Rack:</td>
				<td class="field"><input type="text" name="sourceRack" value="${assetEntity?.sourceRackName}" size="8" maxlength="50" onchange="setMustSave(this.value,'${assetEntity?.sourceRackName}','front2', this.name)"/></td>
			</tr>

			<tr>
				<td class="label">U-Position:</td>
				<td class="field" nowrap>
				<g:select name="sourceRackPosition" from="${[1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,'Undef']}"
							id="sourceRackPositionId" value="${assetEntity?.sourceRackPosition}" onchange="setMustSave(this.value,'${assetEntity?.sourceRackPosition}','front2', this.name)"/>
				<asset:image src="images/plus.gif" height="18" onclick="moveOption('sourceRackPosition','${assetEntity?.sourceRackPosition}','front2','up')"/>
				<asset:image src="images/minus.gif" height="18" onclick="moveOption('sourceRackPosition','${assetEntity?.sourceRackPosition}','front2','down')"/>
				</td>
			</tr>

			<tr>
				<td class="label">Need Asset Tag?</td>
				<td class="field">
					<input type="radio" name="needAssetTag" id="needAssetTagYes" value="Y" onclick="setMustSave(this.value,'${assetEntity?.model?.usize}','front2', this.name)"><label for="needAssetTagYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="needAssetTag" id="needAssetTagNo" value="N" onclick="setMustSave(this.value,'${assetEntity?.model?.usize}','front2', this.name)" checked><label for="needAssetTagNo">No</label>
				</td>
			</tr>
			<tr>
				<td class="label">Has Amber Lights?</td>
				<td class="field">
					<input type="radio" name="hasAmber" id="hasAmberYes" value="Y" onclick="setMustSave(this.value,'${assetEntity?.model?.usize}','front2', this.name)" ><label for="hasAmberYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="hasAmber" id="hasAmberNo" value="N" onclick="setMustSave(this.value,'${assetEntity?.model?.usize}','front2', this.name)" checked><label for="hasAmberNo">No</label>
				</td>
			</tr>
			<tr>
				<td class="label">Stuff Stacked On Top?</td>
				<td class="field">
					<input type="radio" name="stuffOnTop" id="stuffOnTopYes" onclick="setMustSave(this.value,'${assetEntity?.model?.usize}','front2', this.name)" value="Y"><label for="stuffOnTopYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="stuffOnTop" id="stuffOnTopNo" onclick="setMustSave(this.value,'${assetEntity?.model?.usize}','front2', this.name)" value="N" checked><label for="stuffOnTopNo">No</label>
				</td>
			</tr>
			<tr>
				<td class="label">Is Powered OFF?</td>
				<td class="field">
					<input type="radio" name="poweredOff" id="poweredOffYes" onclick="setMustSave(this.value,'${assetEntity?.model?.usize}','front2', this.name)" value="Y" ><label for="poweredOffYes">Yes</label>
					&nbsp;&nbsp;
					<input type="radio" name="poweredOff" id="poweredOffNo" onclick="setMustSave(this.value,'${assetEntity?.model?.usize}','front2', this.name)" value="N" checked><label for="poweredOffNo">No</label>
				</td>
			</tr>
			</table>

			<div style="margin-top:10px;">
			   <div class="thefield" align="center">
			      <a class="button" href="#asset_rear1">Next</a>&nbsp;&nbsp;
			      <a class="button" href="#asset_front1">Back</a>&nbsp;&nbsp;
			      <a class="button" href="#select_asset" id="front2SaveId"  onClick="document.auditForm.submitType.value='save';getObject('mustSaveId').value='false';document.auditForm.submit();">Save</a>&nbsp;&nbsp;&nbsp;
			      <a class="button" href="#select_asset"  id="front2CompleteId" onClick="document.auditForm.submitType.value='complete';getObject('mustSaveId').value='false';document.auditForm.submit();">Completed</a>
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
		</table>
		<table>
		<tr>
		        <td class="label">Has Obstruction?</td>
		        <td class="field">
		                <input type="radio" name="hasObstruction" id="hasObstructionYes" onclick="setMustSave(this.value,'0','rear', this.name)" value="Y"><label for="hasObstructionYes">Yes</label>
		                &nbsp;&nbsp;
		                <input type="radio" name="hasObstruction" id="hasObstructionNo" value="N" onclick="setMustSave(this.value,'1','rear', this.name)" checked><label for="hasObstructionNo">No</label>
		        </td>
		</tr>
		</table>

		<div style="margin-top:20px;">
		   <div class="thefield" align="center">
		      <a class="button" href="#asset_front2">Back</a>&nbsp;&nbsp;
		      <a class="button" href="#select_asset" id="rearSaveId" onClick="document.auditForm.submitType.value='save';getObject('mustSaveId').value='false';document.auditForm.submit();">Save</a>&nbsp;&nbsp;&nbsp;
			  <a class="button" href="#select_asset" id="rearCompleteId" onClick="document.auditForm.submitType.value='complete';getObject('mustSaveId').value='false';document.auditForm.submit();">Completed</a>
		   </div>
		</div>

		</div>
		</div>
		<!-- end of Walkthru Asset:Back -->
		</g:form>
		<g:if test="${request.getHeader ( 'User-Agent' ).contains ( 'MSIE' )}">
		<div class="gap"></div>

		<!-- Walkthru Asset:Generate Label  -->
		<div class="qvga_border">
		<a name="generate_label"></a>
		<div class="title">Walkthru&gt; Generate Label</div>
		<div class="input_area">

		<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
		<div style="float:right;"><a class="button" href="#asset_menu">Asset Menu</a></div>
		<br class="clear"/>
		<object id="TF" classid="clsid:18D87050-AAC9-4e1a-AFF2-9D2304F88F7C" codebase="${resource(dir:'resource',file:'TFORMer60.cab')}"></object>

		<g:form name="assetTagLabelForm">
		<table>
		<tr>
			<input type="hidden" name="urlPath" id="urlPath" value="<g:resource dir="resource" file="assetTag_label.tff" absolute="true"/>"/>
			<input type= "hidden" id="RepPath" name="RepPath">
	      	<input type= "hidden" name="PrjName" id="PrjName">
	        <input type= "hidden" name="FormName" id="FormName">
			<td class="label">Asset Tag:</td>
			<td class="field">${assetEntity?.assetTag}</td>
		</tr>
		<tr>
			<td class="label">Asset Name:</td>
			<td class="field">${assetEntity?.assetName}</td>
		</tr>
		<tr>
			<td class="label">Serial Nunber:</td>
			<td class="field">${assetEntity?.serialNumber}</td>
		</tr>
		<tr>
			<td class="label">Rack:</td>
			<td class="field">${assetEntity?.sourceRackName}</td>
		</tr>
		<tr>
			<td class="label">U-Position:</td>
			<td class="field">${assetEntity?.sourceRackPosition}</td>
		</tr>

		<tr>
			<td class="label">Manufacturer:</td>
			<td class="field">${assetEntity?.manufacturer}</td>
		</tr>
		<tr>
			<td class="label">Model:</td>
			<td class="field">${assetEntity?.model}</td>
		</tr>
		<tr>
			<td class="label">Printer</td>
			<td class="field"><select type= "hidden" id="Printers" name="Printers"  onChange="javascript:mySelect(this);"/>
          				<input type= "hidden" name="PrinterName" id="PrinterName"></td>
		</tr>
		</table>
		<div style="margin-top:20px;">
		   <div class="thefield" align="center">
		      <a class="button" id="printButton" href="javascript:startprintjob();">Print</a>
		   </div>
		</div>
		</g:form>
		</div>
		<script type="text/javascript">
		//=============================================================================
		// PRINT HERE
		//=============================================================================
		function startprintjob()
		{
		var job = window.TF.CreateJob();
		var form = window.document.assetTagLabelForm;
		var jobdata = job.NewJobDataRecordSet();
		    job.RepositoryName = document.getElementById('urlPath').value;
		    job.ProjectName = form.PrjName.value;
		    job.FormName = form.FormName.value;
		    job.PrinterName = form.PrinterName.value;
		    // THIS IS THE PLACE TO ADD YOUR DATA
		    jobdata.ClearRecords();
		   	jobdata.AddNewRecord();
		   	jobdata.SetDataField('assetName', "${assetEntity?.assetName}");
		   	jobdata.SetDataField('assetTag', "${assetEntity?.assetTag}");
		   	jobdata.SetDataField('rack', "${assetEntity?.sourceRackName}");
		   	jobdata.SetDataField('upos', "${assetEntity?.sourceRackPosition}");

		    // now we print one copy of the label with default settings
		    try
		    {
		    	job.PrintForm();
		    }
		    catch (e)
		    {
			    alert ("TFORMer returned an error!" +
			           "\nError description: " + e.description +
			           "\nError name: " + e.name +
			           "\nError number: " + e.number +
			           "\nError message: " + e.message);
		    }

		}
		//=============================================================================
		//The selected dprinter has changed
		//=============================================================================
		function mySelect(x)
		{
			document.getElementById("PrinterName").value = x.options[x.selectedIndex].value;

		}
		//=============================================================================
		// Add a new option to select element
		//=============================================================================
		function AddOption (selElement, text, value)
		{
		  opt = new Option(text, value, false, true);
		  selElement.options[selElement.length] = opt;
		}

		//=============================================================================
		// Set default data for TFORMer Runtime Properties
		//=============================================================================
		function InitData()
		{
			var form = window.document.assetTagLabelForm;
			var path = window.location.href;
			var i = -1;
			// the following code evaluates the path to the demo repository
			for (n=1; n<=3; n++)
			{
				i = path.lastIndexOf('/');
				if (i != -1)
				{
					path = path.substring(0,i)                              // one directory level up
				}
			}
			if (path.substr (0, 8) == "file:///")			                // do not use URL-style for Repository file name - remove file:///
			    path = path.substr (8);
		    path= unescape(path);	                        				// unescape!
		    form.RepPath.value 	= path + '/Demo Repository/Demos.tfr';  	// repository name
		    form.PrjName.value 	= 'TFORMer_Runtime_Examples';				// project name
		    form.FormName.value = 'BarcodeLabels';							// form name
		    form.PrinterName.value = ''
			// get list of installed printers
			var dropdown = document.getElementById("Printers");
			window.TF.RefreshOSPrinters();
			var def = 0;
			for (i = 0; i < window.TF.GetOSPrintersCount(); i++)
			{
			  AddOption (dropdown, window.TF.GetOSPrinter(i), window.TF.GetOSPrinter(i));
			  if (window.TF.GetOSPrinter(i) == window.TF.GetOSDefaultPrinter())
			    def = i;
			}
			dropdown.options[def].selected = true;
		}


		InitData()
		</script>
		</div>
		</g:if>
	    <div class="gap"></div>
		<!-- Walkthru Asset:Comments -->
		<div class="qvga_border">
		<a name="comments"></a>
		<div class="title">Walkthru&gt; Issues &amp; Comments</div>
		<div class="input_area">

		<div style="FLOAT: left"><a class=button href="startMenu">Start Over</a></div>
		<div style="float:right;"><a class="button" href="#asset_menu">Asset Menu</a></div>
		<br class="clear"/>
		<g:form name="commentForm" action="saveComment" method="get">
			<table>
			<tr>
				<td class="label">Asset Tag:
				<input type="hidden" name="assetId" id="assetId" value="${assetEntity.id}" />
				<input type="hidden" name="id" value="${assetEntity.id}" />
				<input type="hidden" name="commentType" id="commentType" value="comment" />
				<input type="hidden" name="instructionType" id="instructionType" value="instruction" />
				<input type="hidden" name="issueType" id="issueType" value="issue" />
				<input type="hidden" name="room" value="${room}" />
				<input type="hidden" name="rack" value="${rack}" />
				<input type="hidden" name="location" value="${location}" />
				<input type="hidden" name="moveBundle" value="${moveBundle}" />
				</td>
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

			<textarea name="comments" id="comments" rows="6" cols="8" ></textarea>
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

			<a class="button" href="retrieveComments?commentType=all&sort=desc&orderType=commentType&id=${assetEntity?.id}&room=${room}&rack=${rack}&location=${location}&moveBundle=${moveBundle}#view_comments">View Issues &amp; Comments</a>
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
		<g:form action="issuesandcommentsview" name="commentsViewForm" method="get">
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
						<g:if test="${assetCommentsInstance.isResolved()}">
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
				<li><a href="retrieveComments?commentType=all&sort=desc&orderType=comment&id=${assetEntity.id}&room=${room}&rack=${rack}&location=${location}&moveBundle=${moveBundle}#view_comments">All</a></li>
				<li><a href="retrieveComments?commentType=comment&sort=desc&orderType=comment&id=${assetEntity.id}&room=${room}&rack=${rack}&location=${location}&moveBundle=${moveBundle}#view_comments">Comment</a></li>
				<li><a href="retrieveComments?commentType=instruction&sort=desc&orderType=comment&id=${assetEntity.id}&room=${room}&rack=${rack}&location=${location}&moveBundle=${moveBundle}#view_comments">Instruction</a></li>
				<li><a href="retrieveComments?commentType=issue&sort=desc&orderType=comment&id=${assetEntity.id}&room=${room}&rack=${rack}&location=${location}&moveBundle=${moveBundle}#view_comments">Issue</a></li>
		   </ul>

	<script type="text/javascript">
	if('${commentCodes.needAssetTag}'){
		getObject('needAssetTagYes').checked = true
		getObject('needAssetTagNo').checked = false
	}
	if('${commentCodes.amberLights}'){
		getObject('hasAmberYes').checked = true
		getObject('hasAmberNo').checked = false
	}
	if('${commentCodes.stackedOnTop}'){
		getObject("stuffOnTopYes").checked = true
		getObject("stuffOnTopNo").checked = false
	}
	if('${commentCodes.poweredOff}'){
		getObject("poweredOffYes").checked = true
		getObject("poweredOffNo").checked = false
	}
	if('${commentCodes.cablesMoved}'){
		getObject("hasObstructionYes").checked =true
		getObject("hasObstructionNo").checked = false
	}

	if("${location}" != "${assetEntity.sourceLocationName}"){
		setMustSave( "${location}", "${assetEntity.sourceLocationName}", "", "sourceLocation");
	}
	/*
	* Script for Manufacturer and Model.
	*/
	function getXmlhttp(){
		var xmlhttp;
		if (window.XMLHttpRequest) {// code for IE7+, Firefox, Chrome, Opera, Safari
		  	xmlhttp=new XMLHttpRequest();
		} else {// code for IE6, IE5
			xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
		}
		return xmlhttp
	}
	function getModels( manufacturerId ){
		var xmlhttp = getXmlhttp()
		xmlhttp.onreadystatechange=function() {
			if (xmlhttp.readyState==4 && xmlhttp.status==200) {
				try{
				var modelsList = eval('(' + xmlhttp.responseText + ')')
				var inputField = "<select name='model.id' id='modelId' onchange='setMustSave(this.value,'${assetEntity?.model.id}','front1', this.name)'/><option value=\'\'>Unassigned</option>"
				for(i=0; i<modelsList.length; i++){
					var model = modelsList[i]
					inputField += '<option value=\''+model.id+'\'>'+model.modelName+'</option>'
				}
				inputField +="</select>"
				getObject("modelTdId").innerHTML = inputField
				}catch(e){
					//alert(e.message)
				}
		    }
		}
		xmlhttp.open("POST","/model/retrieveModelsListAsJSON?manufacturer="+manufacturerId,true);
		xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
		xmlhttp.send();
	}
</script>

</body>
</html>
