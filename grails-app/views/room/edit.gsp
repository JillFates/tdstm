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
					<td class="buttonR" colspan="4">
						<input type="button" class="submit" value="Cancel" onclick="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val()', onComplete:'openRoomView(e)')}" />
						<input type="submit" class="submit" value="Update" />
					</td>
					<td class="buttonR" style="padding-left: 200px;">
						<input type="button" class="submit" value="Add Rack" onclick="createRack()" />
					</td>
				</tr>
			</tbody>
		</table>
	</div>
<div id="roomLayout" style="width: auto; overflow-x: auto; border: 2px solid black">
	<div id="room_layout" style="position:relative;width: 700px;height: 800px;overflow-x: auto; border: 0px solid black;float: left;">
		<table cellpadding="0" cellspacing="0" style="width:auto;height:auto;border:0px" id="room_layout_table">
			<g:set var="numrows" value="${1}" />
			<g:while test="${numrows < roomInstance.roomDepth / 2 }">
				<tr>
					<g:set var="numcols" value="${1}" />
					<g:while test="${numcols < roomInstance.roomWidth / 2 }">
						<td class="room_tile" numcols="${numcols++}">&nbsp;</td>
					</g:while>
				</tr ><!-- ${numrows++} -->
			</g:while>
		</table>
		<g:each in="${rackInstanceList}" var="rack">
			<g:if test="${rack.rackType == 'Rack'}">
				<div id="rack_${rack.id}" style="top:${rack.roomY}px;left:${rack.roomX}px;" onmouseout="updateXYPositions(this.id)" class="${rack.hasBelongsToMoveBundle(moveBundleId) ? 'rack_highlight' : source=='true' && rack.source == 1 ? 'rack_highlight' : target == 'true' && rack.source == 0 ? 'rack_highlight' : rack.front ? 'rack_highlight_no_'+rack.front :'rack_highlight_no_L' }">
					<span id="rackLabel_${rack.id}">${rack.tag}</span>
				</div>
			</g:if>
			<g:else>
				<div id="rack_${rack.id}" style="top:${rack.roomY}px;left:${rack.roomX}px;" onmouseout="updateXYPositions(this.id)" class="room_${rack.rackType}_${rack.front}">
					<span id="rackLabel_${rack.id}" >${rack.tag}</span>
				</div>
			</g:else>
		</g:each>
		<g:each in="${newRacks}" var="rack">
			<div id="rack_${rack}" style="top:0px;left:0px;display: none;" onmouseout="updateXYPositions(this.id)" class="rack_highlight_no_L" >
				<span id="rackLabel_${rack}">&nbsp;</span>
			</div>
		</g:each>
	</div>
	<div style="position:relative;float: right;top:-800px; margin-left: 10px;" id="rackLayout">
	<table border="0">
		<tr>
			<th>Rack<input type="hidden" id="rackCount" name="rackCount" value="50000"></th>
			<th>X</th>
			<th>Y</th>
			<th>Front</th>
			<th>A</th>
			<th>B</th>
			<th>C</th>
			<th>Type</th>
			<th>Assets</th>
		</tr>
		<g:each in="${rackInstanceList}" var="rack" status="i">
			<tr id="rackEditRow_${rack.id}" class="${(i % 2) == 0 ? 'odd' : 'even'}" >
				<td>
					<input type="hidden" name="rackId" value="${rack.id}"/>
					<input type="text" name="tag_${rack.id}" value="${rack.tag}" size="5" onchange="changeLabel(${rack.id},this.value)"/>
				</td>
				<td><input type="text" id="roomXId_${rack.id}" name="roomX_${rack.id}" value="${rack.roomX}" size="3" readonly="readonly" /></td>
				<td><input type="text" id="roomYId_${rack.id}" name="roomY_${rack.id}" value="${rack.roomY}" size="3" readonly="readonly" /></td>
				<td><g:select id="frontId_${rack.id}" name="front_${rack.id}" from="${Rack.constraints.front.inList}" value="${rack.front}" onchange="updateRackStyle(${rack.id}, this.value, jQuery('#rackTypeId_'+${rack.id}).val())"></g:select></td>
				<td><input type="text" name="powerA_${rack.id}" value="${rack.powerA}"  size="3" /></td>
				<td><input type="text" name="powerB_${rack.id}" value="${rack.powerB}" size="3" /></td>
				<td><input type="text" name="powerC_${rack.id}" value="${rack.powerC}" size="3" /></td>
				<td><g:select id="rackTypeId_${rack.id}" name="rackType_${rack.id}" from="${Rack.constraints.rackType.inList}" value="${rack.rackType}" onchange="updateRackStyle(${rack.id}, jQuery('#frontId_'+${rack.id}).val(), this.value)"></g:select></td>
				<td>${rack.assets.size()}&nbsp;&nbsp;&nbsp;
				<g:if test="${rack.assets.size() == 0}">
					<a href="javascript:verifyAndDeleteRacks(${rack.id})"><span class="clear_filter"><u>X</u></span></a>
				</g:if>
				</td>
			</tr>
		</g:each>
		<g:each in="${newRacks}" var="rack" status="i">
			<tr id="rackEditRow_${rack}" class="${(i % 2) == 0 ? 'odd' : 'even'}" style="display: none;">
				<td>
					<input type="hidden" name="rackId" value="${rack}"/>
					<input type="text" name="tag_${rack}" value="" size="5" onchange="changeLabel(${rack},this.value)"/>
				</td>
				<td><input type="text" id="roomXId_${rack}" name="roomX_${rack}" value="" size="3" readonly="readonly" /></td>
				<td><input type="text" id="roomYId_${rack}" name="roomY_${rack}" value="" size="3" readonly="readonly" /></td>
				<td><g:select id="frontId_${rack}" name="front_${rack}" from="${Rack.constraints.front.inList}" onchange="updateRackStyle(${rack}, this.value, jQuery('#rackTypeId_'+${rack}).val())"></g:select></td>
				<td><input type="text" name="powerA_${rack}" value="${new Rack().powerA}"  size="3" /></td>
				<td><input type="text" name="powerB_${rack}" value="${new Rack().powerB}" size="3" /></td>
				<td><input type="text" name="powerC_${rack}" value="${new Rack().powerC}" size="3" /></td>
				<td><g:select id="rackTypeId_${rack}" name="rackType_${rack}" from="${Rack.constraints.rackType.inList}" value="Rack" onchange="updateRackStyle(${rack}, jQuery('#frontId_'+${rack}).val(), this.value)"></g:select></td>
				<td>0&nbsp;&nbsp;&nbsp;<a href="javascript:verifyAndDeleteRacks(${rack})"><span class="clear_filter"><u>X</u></span></a></td>
			</tr>
		</g:each>
	</table>
	</div>
	</div>
</g:form>
</div>
</div>
<script type="text/javascript">
initializeRacksInRoom( ${rackInstanceList.id} )
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
function updateXYPositions(id){
	var rackId = id.split("_")[1]
	var x = $("#"+id).css("left")
	var y = $("#"+id).css("top")
	x = x.substring(0,x.indexOf('px'))
	y = y.substring(0,y.indexOf('px'))
	var top = $("#room_layout_table").css("height")
	var left = $("#room_layout_table").css("width")
	top = top.substring(0,top.indexOf('px'))
	left = left.substring(0,left.indexOf('px'))	
	var cssClass = $("#"+id).attr("class")
	if(cssClass == "room_CRAC_L" || cssClass == "room_CRAC_R"){
		if(parseInt(top) < parseInt(y) + 102){
			y = parseInt(top) - 102;
			$("#"+id).css("top",y+"px")
		}
	} else if(cssClass == "room_CRAC_T" || cssClass == "room_CRAC_B"){
		if(parseInt(left) < parseInt(x) + 100){
			x = parseInt(left) - 105;
			$("#"+id).css("left",x+"px")
		}
	} else if(cssClass == "room_DoorL_L" || cssClass == "room_DoorL_R" || cssClass == "room_DoorR_L" || cssClass == "room_DoorR_R"){
		if(parseInt(left) < parseInt(x) + 100){
			x = parseInt(left) - 60;
			$("#"+id).css("left",x+"px")
		}
		if(parseInt(top) < parseInt(y) + 102){
			y = parseInt(top) - 60;
			$("#"+id).css("top",y+"px")
		}
	}
	$("#roomXId_"+rackId).val(x)
	$("#roomYId_"+rackId).val(y)
}
function verifyAndDeleteRacks(id){
	jQuery.ajax({
		url: "verifyRackAssociatedRecords",
		data: "rackId="+id,
		type:'POST',
		success: function(data) {
			if(data != null && data != ""){
				if(confirm("Some assets used this Rack. Be sure you want to remove it before proceeding")){
					$("#rackEditRow_"+id).remove() // Remove row from table
					$("#rack_"+id).remove() // Remove the image from model panel
				}
			} else {
				$("#rackEditRow_"+id).remove() // Remove row from table
				$("#rack_"+id).remove() // Remove the image from model panel
			}
		}
	});
}
function createRack(){
	var newRackId = $("#rackCount").val()
	$("#rackEditRow_"+newRackId).show()
	$("#rack_"+newRackId).show()
	$("#rackCount").val( parseInt(newRackId)+1 )
}
function changeLabel(id,value){
	$("#rackLabel_"+id).html(value)
}
function changeRackType(id,value){
	$("#rack_"+id).html(value)
}
function updateRackStyle(id, frontValue, rackTypeValue){
	$("#rack_"+id).removeAttr("class")
	if(rackTypeValue == "Rack"){
		$("#rack_"+id).addClass("rack_highlight_no_"+frontValue)
	} else {
		$("#rack_"+id).addClass("room_"+rackTypeValue+"_"+frontValue )
	}
	updateXYPositions("rack_"+id)
}
</script>
</body>
</html>
