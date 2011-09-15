<html>
<body>
<div class="body">
<div id="roomListView">
<span class="span">
<b> Data Center / Room View </b>
</span>
<div class="dialog" style="border: 1px solid black;">
	<table style="width: auto; border: none">
		<tbody>
			<tr>
				<td class="buttonR" style="vertical-align:top">
				<div>
				<g:select id="roomId" name="id" from="${roomInstanceList}" value="${roomInstance.id}" optionKey="id" optionValue="${{it.location +' / '+it.roomName}}" onchange="${remoteFunction(action:'show', params:'\'id=\'+this.value+\'&moveBundleId=\'+$(\'#bundleId\').val()+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')', onComplete:'openRoomView(e)')}" />
				<br />

				<g:form action="list">
				<input type="hidden" name="viewType" value="list" />
				<input type="submit" class="submit" value="List" />
				<jsec:hasAnyRole in="['ADMIN','SUPERVISOR','PROJECT_ADMIN']">
					<input type="Button" class="submit" value="Edit" onclick="${remoteFunction(action:'edit', params:'\'id=\'+$(\'#roomId\').val()', onComplete:'openRoomView(e)')}" />
				</jsec:hasAnyRole>

				</g:form>
				</div>
				</td>
				<td style="vertical-align:top">
				<div style="width: 150px"><label><b>Highlight:</b></label><br /><br />
				<label><b>Bundle</b></label><br />
					<g:select id="bundleId" name="moveBundle" from="${moveBundleList}" value="${moveBundleId}" optionKey="id" optionValue="name" noSelection="${['':'All']}" 
						onchange="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val() +\'&moveBundleId=\'+this.value', onComplete:'openRoomView(e)')}" />
				</div>
				</td>
				<td class="buttonR">
				<div style="width: 150px">
				<label for="sourceView">
					<g:if test="${source == 'true'}">
					<input type="checkbox" name="sourceView" id="sourceView" value="" checked="checked" onclick="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val() +\'&moveBundleId=\'+$(\'#bundleId\').val()+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')', onComplete:'openRoomView(e)')}" />&nbsp;Source
					</g:if>
					<g:else>
					<input type="checkbox" name="sourceView" id="sourceView" value="" onclick="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val() +\'&moveBundleId=\'+$(\'#bundleId\').val()+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')', onComplete:'openRoomView(e)')}" />&nbsp;Source
					</g:else>
					</label><br />
				<label for="targetView">
					<g:if test="${target == 'true'}">
					<input type="checkbox" name="targetView" id="targetView" value="" checked="checked" onclick="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val() +\'&moveBundleId=\'+$(\'#bundleId\').val()+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')', onComplete:'openRoomView(e)')}" />&nbsp;Target
					</g:if>
					<g:else>
					<input type="checkbox" name="targetView" id="targetView" value="" onclick="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val() +\'&moveBundleId=\'+$(\'#bundleId\').val()+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')', onComplete:'openRoomView(e)')}" />&nbsp;Target
					</g:else>
					</label><br />
				</div>
				</td>
				<td class="cap_tab" style="width:150px">
					<table class="cap_tab" style="width: auto; padding:1px; border: none;">
					<tr><td class="cap_tab" rowspan="2"><b>Capacity View:</b>&nbsp;<select name="capacityView" size="1">
						<option label="None" value="None"></option>
						<option label="Space" value="Space"></option>
						<option label="Power" value="Power"></option>
						<option label="Heat" disabled="disabled"></option>
						<option label="Weight" disabled="disabled"></option>
						<option label="Ethernet" value="Ethernet" disabled="disabled"></option>
						<option label="Fiber" value="Fiber" disabled="disabled"></option>
						</select>
					</td><td class="cap_tab rack_cap20" id="#cap20">&nbsp;</td></tr>
					<tr><td class="cap_tab rack_cap32" id="#cap32">&nbsp;</td></tr>
					<tr><td class="cap_tab" rowspan="3">
						<input type="radio" name="capacityType" value="Used" />&nbsp;Used&nbsp;<br />
						<input type="radio" name="capacityType" checked="checked" value="Remaining" />&nbsp;Remaining
					</td><td class="cap_tab rack_cap44" id="#cap44">&nbsp;</td></tr>
					<tr><td class="cap_tab rack_cap56" id="#cap56">&nbsp;</td></tr>
					<tr><td class="cap_tab rack_cap68" id="#cap68">&nbsp;</td></tr>
					<tr><td class="cap_tab" >&nbsp;</td><td class="cap_tab rack_cap80" id="#cap80">&nbsp;</td></tr>
					</table>
				</td>
				<td style="padding-left: 50px; align:right;" id="rackPowerTd">
				</td>
			</tr>
		</tbody>
	</table>
</div>
<div id="roomLayout" style="width: 1100px; overflow-x: auto; border: 2px solid black">
	<input id="selectedRackId" type="hidden">
	<div id="room_layout" style="position:relative;width: 650px;height: 800px;overflow-x: auto; border: 0px solid black;float: left;">
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
			<g:each in="${Rack.findAllByRoom(roomInstance)}" var="rack" status='i'>
				<g:if test="${rack.rackType == 'Rack'}">
					<g:remoteLink controller="rackLayouts" action="save" params="[moveBundleId:moveBundleId,rackId:rack.id,backView:'off',showCabling:'off',otherBundle:'on',bundleName:'on',hideIcons:'on']" onSuccess="updateRackPower(${rack.id})" onComplete="jQuery('#rackLayout').html(e.responseText);">
					<div style="top:${rack.roomY ? rack.roomY : 0}px;left:${rack.roomX ? rack.roomX : 0}px;" class="${rack.hasBelongsToMoveBundle(moveBundleId) ? 'rack_highlight_'+rack.front : source=='true' && rack.source == 1 ? 'rack_highlight_'+rack.front : target == 'true' && rack.source == 0 ? 'rack_highlight_'+rack.front : rack.front ? 'rack_highlight_no_'+rack.front :'rack_highlight_no_'+rack.front }">
						<div id="rack_div_${i}" class="racktop_label" onclick="$('#selectedRackId').val(this.id)">${rack.tag}</div>
					</div>
					</g:remoteLink>
				</g:if>
				<g:else>
					<div style="position:absolute;top:${rack.roomY ? rack.roomY : 0}px;left:${rack.roomX ? rack.roomX : 0}px;" class="room_${rack.rackType}_${rack.front}">
						<div class="racktop_label" >${rack.tag}</div>
					</div>
				</g:else>
		</g:each>
</div>
	<div style="float: right; margin-left: 20px;width: 350px;" id="rackLayout">
<table cellpadding=2 class="rack_elevation back">
	<tr><th>U</th><th>Device</th><th>Bundle</th></tr>
	<tr><td class='rack_upos'>42</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>41</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>40</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>39</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>38</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>37</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>36</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>35</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>34</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>33</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>32</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>31</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>30</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>29</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>28</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>27</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>26</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>25</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>24</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>23</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>22</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>21</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>20</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>19</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>18</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>17</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>16</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>15</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>14</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>13</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>12</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>11</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>10</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>9</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>8</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>7</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>6</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>5</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>4</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>3</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>2</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
	<tr><td class='rack_upos'>1</td><td rowspan='1' class='empty'>&nbsp;</td><td>&nbsp;</td></tr>
</table>
</div>
</div>
</div>
</div>
<script type="text/javascript">
initializeRacksInRoom( [] )
function updateRackPower(rackId){
	jQuery.ajax({
		url: "getRackPowerData",
		data: "rackId="+rackId,
		type:'POST',
		success: function(data) {
			$("#rackPowerTd").html(data)
		}
	});
	$("#editDialog").dialog("close")
	$("#createRoomDialog").dialog("close")
	$("#mergeRoomDialog").dialog("close")
	$("#createDialog").dialog("close")
	$("#listDialog").dialog("close")
}
function capacityView{
}
</script>
</body>
</html>
