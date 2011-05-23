

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'rackLayout.css')}" />
<title>Room List</title>
</head>
<body>
<div class="body" style="margin-top: 10px;">
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div id="roomListView">
<fieldset><legend><b>Room List</b></legend>
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
	<g:form action="update" >
		<span class="button">
			<input type="button" class="edit" action="edit" value="Create Room"/>
			<span class="button"><input class="create" type="button" value="Merge" onclick="showMergeDialog()"/></span>
			<input type="button" class="delete" action="delete" value="Delete" onclick="#"/>
		</span>
	</g:form>
</div>
</div>
</fieldset>
</div>
<div id="roomShowView" style="display: none;">
<fieldset><legend><b>Room View</b></legend>
<div></div>
</fieldset>
</div>
</div>
<script type="text/javascript">
function openRoomView(e){
	var resp = e.responseText
	$("#roomShowView").html(resp)
	$("#roomShowView").show()
	$("#roomListView").hide()
}
</script>
</body>
</html>
