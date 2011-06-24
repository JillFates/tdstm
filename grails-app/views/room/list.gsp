

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'rackLayout.css')}" />
<title>Room List</title>
<g:javascript src="asset.tranman.js" />
 <g:javascript src="drag_drop.js" />
<script type="text/javascript">
var roomId = "${roomId}"
var viewType = "${viewType}"
if(roomId && viewType != 'list'){
${remoteFunction(action:'show', params:'\'id=\'+roomId', onComplete:'openRoomView(e)')}
}
$(document).ready(function() {
    $("#editDialog").dialog({ autoOpen: false })
    $("#createRoomDialog").dialog({ autoOpen: false })
    $("#mergeRoomDialog").dialog({ autoOpen: false })
    $("#createDialog").dialog({ autoOpen: false })
})
</script>
</head>
<body>
<div class="body" style="margin-top: 10px;">
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div id="roomListView">
<g:if test="${roomId && viewType != 'list'}">
<script type="text/javascript">
${remoteFunction(action:'show', params:'\'id=\'+roomId', onComplete:'openRoomView(e)')}
</script>
</g:if>
<g:else>
<fieldset><legend><b>Room List</b></legend>
<g:form action="create" >
<div style="float: left; width: auto;">
<table>
	<thead>
		<tr>

			<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']"><th><a href="#">Action</a></th></jsec:hasAnyRole>

			<g:sortableColumn property="location" title="Data Center" />

			<g:sortableColumn property="roomName" title="Room" />

			<th><a href="#">Rack count</a></th>

			<th><a href="#">Asset count</a></th>

		</tr>
	</thead>
	<tbody>
		<g:each in="${roomInstanceList}" status="i" var="roomInstance">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

				<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
					<td><input type="checkbox" name="checkbox_${roomInstance.id}" id="checkboxId_${roomInstance.id}" onclick="enableActions()"></td>
				</jsec:hasAnyRole>
				<td onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(e)')}">${fieldValue(bean: roomInstance, field: "location")}</td>
				
				<td onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(e)')}">${fieldValue(bean: roomInstance, field: "roomName")}</td>

				<td onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(e)')}">${fieldValue(bean: roomInstance, field: "rackCount")}</td>

				<td onclick="${remoteFunction(action:'show', params:'\'id='+roomInstance.id+'\'', onComplete:'openRoomView(e)')}">${fieldValue(bean: roomInstance, field: "assetCount")}</td>

			</tr>
		</g:each>
	</tbody>
</table>
<div class="buttons"> 
	<span class="button">
		<input type="button" class="edit" value="Create Room" onclick="$('#createRoomDialog').dialog('open');$('#mergeRoomDialog').dialog('close')"/>
		<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
			<span class="button"><input class="create" id="mergeId" type="button" value="Merge" onclick="showMergeDialog()" style="display: none;"/></span>
			<g:actionSubmit class="delete" action="delete" id="deleteId" value="Delete" style="display: none;"/>
		</jsec:hasAnyRole>
	</span>
</div>
</div>
</g:form>
</fieldset>
</g:else>
</div>
<div id="roomShowView" style="display: none;">
</div>
</div>
<div id="createDialog" title="Create Asset" style="display: none;">
<g:form action="save" controller="assetEntity" method="post" name="createForm" >

	<div class="dialog" id="createDiv" >
		<table id="createFormTbodyId"></table>
	</div>
	
	<div class="buttons">
	<input type="hidden" name="projectId" value="${projectId }" />
	<input type="hidden" id="attributeSetId" name="attributeSet.id" value="${projectId }" />
	<input type="hidden" name="redirectTo" value="room" />
	<span class="button"><input class="save" type="submit" value="Create" onclick="return validateAssetEntity('createForm');" /></span>
	</div>
</g:form></div>
<div id="editDialog" title="Edit Asset" style="display: none;">
	<g:form method="post" name="editForm">
		<input type="hidden" name="id" id="editFormId" value="" />
		<div class="dialog" id="editDiv">
		</div>
		<div class="buttons">
			<span class="button">
				<input class="save" type="button" style="font-size: 12px;" value="Update Asset" onClick="${remoteFunction(controller:'assetEntity', action:'getAssetAttributes', params:'\'assetId=\' + $(\'#editFormId\').val() ', onComplete:'callUpdateDialog(e)')}" />
			</span>
		</div>
	</g:form>
</div>
<div id="createRoomDialog" title="Create Room" style="display: none;">
	<g:form method="post" name="createRoomForm" action="save" onsubmit="return validateForm()">
		<table>
			<tbody>
				<tr>
					<td>Data Center<td/>
					<td>
						<input type="hidden" name="project.id" id="projectId" value="${projectId}">
						<input type="text" name="location" id="locationId" value="${roomInstance.location}">
					</td>
				</tr>
				<tr>
					<td>Room<td/>
					<td>
						<input type="text" name="roomName" id="roomNameId" value="${roomInstance.roomName}">
					</td>
				</tr>
				<tr>
					<td>Width<td/>
					<td>
						<input type="text" name="roomWidth" id="roomWidthId" value="${roomInstance.roomWidth}">
					</td>
				</tr>
				<tr>
					<td>Depth<td/>
					<td>
						<input type="text" name="roomDepth" id="roomDepthId" value="${roomInstance.roomDepth}">
					</td>
				</tr>
				<tr>
					<td class="buttons" colspan="4">
						<input type="submit" class="save" value="Save" />
						<input type="button" class="show" value="Cancel" onclick="$('#createRoomDialog').dialog('close');" />
					</td>
				</tr>
			</tbody>
		</table>
	</g:form>
</div>
<div id="mergeRoomDialog" title="Merge Room" style="display: none;">
	<g:form method="post" name="mergeRoomForm" action="mergeRoom">
		<table>
			<thead>
				<tr>
					<th>Data Center<input type="hidden" name="sourceRoom" id="sRoomId"> </th>
					<th>Room<input type="hidden" name="targetRoom" id="tRoomId"></th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${roomInstanceList}" status="i" var="roomInstance">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" id="mergeRow_${roomInstance.id}" onclick="submitMergeForm(this.id)">
						
						<td>${fieldValue(bean: roomInstance, field: "location")}</td>
				
						<td>${fieldValue(bean: roomInstance, field: "roomName")}</td>

					</tr>
				</g:each>
			</tbody>
		</table>
	</g:form>
</div>
<script type="text/javascript">
function openRoomView(e){
	var resp = e.responseText
	$("#roomShowView").html(resp)
	$("#roomShowView").show()
	$("#roomListView").hide()
}
function openAssetEditDialig( id ){
	$("#editFormId").val(id)
	${remoteFunction(controller:"assetEntity", action:"editShow", params:'\'id=\' + id ', onComplete:"showAssetDialog( e , 'edit')")}
}

function showEditAsset(e) {
	var assetEntityAttributes = eval('(' + e.responseText + ')')
	if (assetEntityAttributes != "") {
		$("#editDialog").dialog("close")
		$("#cablingDialogId").dialog("close")
		$('#commit').val('Generate')
		$("#generateId").click()
	} else {
		alert("Asset is not updated, Please check the required fields")
	}
}
function enableActions(){
	var inputCheckBox = $("input:checkbox")
	var enableButtons = 0
	inputCheckBox.each(function() {
		if($(this).is(":checked")){
			enableButtons ++
		}
	});
	if(enableButtons == 1){
		$("#mergeId").show()
		var checkBoxId = $("input:checked").attr('id')
		var roomId = checkBoxId.substring(11,checkBoxId.length)
		jQuery.ajax({
			url: "verifyRoomAssociatedRecords",
			data: "roomId="+roomId,
			type:'POST',
			success: function(data) {
				if(data == null || data == ""){
					$("#deleteId").show()
				}
			}
		});
	} else {
		$("#mergeId").hide()
		$("#deleteId").hide()
	}
}
function showMergeDialog(){
	var inputCheckBox = $("input:checked")
	var checkBoxId = inputCheckBox.attr('id')
	var sRoomId = inputCheckBox.attr('id').substring(11,checkBoxId.length)
	$("#mergeRoomDialog table tr").each(function() {
		var rowId = $(this).attr('id')
		if(rowId.substring(9,rowId.length) == sRoomId){
			$(this).hide()
		} else {
			$(this).show()
		}
	});
	$("#sRoomId").val(sRoomId)
	$('#createRoomDialog').dialog('close');
	$('#mergeRoomDialog').dialog('open')
}
function submitMergeForm(selectedRoom){
	var tRoomId = selectedRoom.substring(9,selectedRoom.length)
	if(tRoomId){
		$("#tRoomId").val(tRoomId)
		$("form#mergeRoomForm").submit()
	}
}
function validateForm(){
	if($("#locationId").val()!="" && $("#roomName").val()!=""){
		return true
	} else {
		alert("ERROR : Data Center and Room should not be blank")
		return false
	}
}
function createDialog(source,rack,roomName,location,position){
	$("#createDialog").dialog('option', 'width', 950)
    $("#createDialog").dialog('option', 'position', ['center','top']);
    $("#editDialog").dialog("close")
    $("#createDialog").dialog("open")
    $("#attributeSetId").val(1)
    ${remoteFunction(controller:"assetEntity",action:'getAttributes', params:'\'attribSet=\' + $("#attributeSetId").val() ', onComplete:"generateCreateForm(e);updateAssetInfo(source,rack,roomName,location,position)")}
  }
function updateAssetInfo(source,rack,roomName,location,position){
	var target = source != '1' ? 'target' : 'source'
	$("#"+target+"RackId").val(rack)
	$("#"+target+"LocationId").val(location)
	$("#"+target+"RoomId").val(roomName)
	$("#"+target+"RackPositionId").val(position)
    
}
function validateAssetEntity(formname) {
	var attributeSet = $("#attributeSetId").val();
	if(attributeSet || formname == 'editForm'){
		var assetName = document.forms[formname].assetName.value.replace(/^\s*/, "").replace(/\s*$/, "");
		
		if( !assetName ){
			alert(" Please Enter Asset Name. ");
			return false;
		} else {
			return true;
		}
	} else {
		alert(" Please select Attribute Set. ");
		return false;
	}
}
</script>
</body>
</html>
