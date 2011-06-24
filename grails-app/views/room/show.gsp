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
				<td class="buttonR">
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
				<td>
				<div style="width: 150px"><label><b>Highlight:</b></label><br /><br />
				<label><b>Bundle</b></label><br />
					<g:select id="bundleId" name="moveBundle" from="${moveBundleList}" value="${moveBundleId}" optionKey="id" optionValue="name" noSelection="${['':'Select Bundle...']}" 
						onchange="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val() +\'&moveBundleId=\'+this.value+\'&source=\'+true+\'&target=\'+true', onComplete:'openRoomView(e)')}" />
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
				<td style="padding-left: 500px;" id="rackPowerTd">
					
				</td>
			</tr>
		</tbody>
	</table>
</div>
<div id="roomLayout" style="width: 1000px; overflow-x: auto; border: 2px solid black">
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
				<g:if test="${rack.rackType == 'Rack'}">
					<div style="top:${rack.roomY ? rack.roomY : 0}px;left:${rack.roomX ? rack.roomX : 0}px;" class="${rack.hasBelongsToMoveBundle(moveBundleId) ? 'rack_highlight' : source=='true' && rack.source == 1 ? 'rack_highlight' : target == 'true' && rack.source == 0 ? 'rack_highlight' : 'rack_highlight_no' }_${rack.front}">
						<g:remoteLink controller="rackLayouts" action="save" params="[rackId:rack.id,frontView:'on',showCabling:'off',otherBundle:'on']" onSuccess="updateRackPower(${rack.id})" onComplete="jQuery('#rackLayout').html(e.responseText);">
						<div class="racktop_label">${rack.tag}</div>
						</g:remoteLink>
					</div>
				</g:if>
				<g:else>
					<div style="position:absolute;top:${rack.roomY ? rack.roomY : 0}px;left:${rack.roomX ? rack.roomX : 0}px;" class="room_${rack.rackType}_${rack.front}">
						<div class="racktop_label">${rack.tag}</div>
					</div>
				</g:else>
		</g:each>
</div>
	<div style="position:relative;top:-800px;float: right; margin-left: 50px;" id="rackLayout">
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
function updateRackPower(rackId){
	jQuery.ajax({
		url: "getRackPowerData",
		data: "rackId="+rackId,
		type:'POST',
		success: function(data) {
			$("#rackPowerTd").html(data)
		}
	});
}
</script>
</body>
</html>
