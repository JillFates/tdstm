<html>
<body>
<div class="body">
<div id="roomListView">
<span class="span">
<b> Room Edit View </b>
</span>
<g:form action="update" onsubmit="return submitForm(this)">
	<div class="dialog"  style="border: 1px solid black;">
		<table style="width: auto; border: none">
			<tbody>
				<tr>
					<td>Data Center<br/>
						<input type="hidden" name="id" id="roomId" value="${roomInstance.id}">
						<input type="text" name="location" id="locationId" value="${roomInstance.location}">
					</td>
					<td>Room<br/>
						<input type="text" name="roomName" id="roomNameId" value="${roomInstance.roomName}">
					</td>
					<td>Width<br/>
						<input type="text" name="roomWidth" id="roomWidthId" value="${roomInstance.roomWidth}">
					</td>
					<td>Depth<br/>
						<input type="text" name="roomDepth" id="roomDepthId" value="${roomInstance.roomDepth}">
					</td>
				</tr>
				<tr>
					<td class="buttonR">
						<input type="button" class="submit" value="Cancel" onclick="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val()', onComplete:'openRoomView(e)')}" />
						<input type="submit" class="submit" value="Update" />
					</td>
				</tr>
			</tbody>
		</table>
	</div>
<div id="roomLayout" style="width: 1250px; overflow-x: auto; border: 2px solid black">
	<div id="room_layout" style="position:relative;width: 700px;height: 800px;overflow-x: auto; border: 0px solid black">
		<table cellpadding="0" cellspacing="0" style="width:auto;height:auto;border:0px">
			<g:set var="numrows" value="${1}" />
			<g:while test="${numrows < roomInstance.roomDepth / 2 }">
				<tr>
					<g:set var="numcols" value="${1}" />
					<g:while test="${numcols < roomInstance.roomWidth / 2 }">
						<td style="height:40px;width:40px;border:1px solid black;padding:0px" numcols="${numcols++}">&nbsp;</td>
					</g:while>
				</tr ><!-- ${numrows++} -->
			</g:while>
			</table>
				<g:each in="${Rack.findAllByRoom(roomInstance)}" var="rack">
			<div style="position:absolute;background-color:white;border:1px solid black;width:60px;height:40px;top:${rack.roomY}px;left:${rack.roomX}px;" class="${rack.hasBelongsToMoveBundle(moveBundleId) ? 'highlight' : source=='true' && rack.source == 1 ? 'highlight' : target == 'true' && rack.source == 0 ? 'highlight' : '' }">
				<a href="#" onclick="$('#room_layout').css('width',700);$('#rackShowRow_'+${rack.id}).hide();$('#rackEditRow_'+${rack.id}).show()">
				${rack.tag}
				</a>
			</div>
		</g:each>
	</div>
	<div style="position:relative;top:-800px;float: right; margin-left: 10px;" id="rackLayout">
	<table border="0">
		<tr>
			<th>Rack</th>
			<th>X</th>
			<th>Y</th>
			<th>Front</th>
			<th>A</th>
			<th>B</th>
			<th>C</th>
			<th>Assets</th>
		</tr>
		<g:each in="${Rack.findAllByRoom(roomInstance)}" var="rack" status="i">
			<tr id="rackEditRow_${rack.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}" >
				<td><input type="text" name="tag_${rack.id}" value="${rack.tag}" size="5" /></td>
				<td><input type="text" name="roomX_${rack.id}" value="${rack.roomX}" size="3" /></td>
				<td><input type="text" name="roomY_${rack.id}" value="${rack.roomY}" size="3" /></td>
				<td>&nbsp;</td>
				<td><input type="text" name="powerA_${rack.id}" value="${rack.powerA}"  size="3" /></td>
				<td><input type="text" name="powerB_${rack.id}" value="${rack.powerB}" size="3" /></td>
				<td><input type="text" name="powerC_${rack.id}" value="${rack.powerC}" size="3" /></td>
				<td>${rack.assets.size()}</td>
			</tr>
		</g:each>
	</table>
	</div>
	</div>
</g:form>
</div>
</div>
<script type="text/javascript">
function submitForm(form){
 	if($("#locationId").val() == '') {
 		alert("Please enter location")
 	} else if($("#roomNameId").val() == '') {
 		alert("Please enter room")
 	} else {
		jQuery.ajax({
			url: $(form).attr('action'),
			data: $(form).serialize(),
			type:'POST',
			success: function(data) {
				$("#roomShowView").html(data)
				$("#roomShowView").show()
				$("#roomListView").hide()
			}
		});
 	}
 	return false;
 }
</script>
</body>
</html>
