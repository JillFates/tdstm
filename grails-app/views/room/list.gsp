

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

			<th><a href="#">Action</a></th>

			<g:sortableColumn property="location" title="Data Center" />

			<g:sortableColumn property="roomName" title="Room" />

			<th><a href="#">Rack count</a></th>

			<th><a href="#">Asset count</a></th>

		</tr>
	</thead>
	<tbody>
		<g:each in="${roomInstanceList}" status="i" var="roomInstance">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

				<td><input type="checkbox" name="checkbox_${roomInstance.id}" id="checkboxId_${roomInstance.id}"></td>

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
		<input type="button" class="edit" action="edit" value="Create Room"/>
		<span class="button"><input class="create" type="button" value="Merge" onclick="showMergeDialog()"/></span>
		<g:actionSubmit class="delete" action="delete" value="Delete" />
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
</script>
</body>
</html>
