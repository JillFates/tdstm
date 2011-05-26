<html>
<body>
<div class="body">
<div id="roomListView">
<span class="span">
<b> Room View </b>
</span>
<div class="dialog" style="border: 1px solid black;">
	<table style="width: auto; border: none">
		<tbody>
			<tr>
				<td class="buttonR">
				<div>
				<g:select id="roomId" name="id" from="${roomInstanceList}" value="${roomInstance.id}" optionKey="id" optionValue="${{it.roomName +' / '+it.location}}" onchange="${remoteFunction(action:'show', params:'\'id=\'+this.value+\'&moveBundleId=\'+$(\'#bundleId\').val()+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')', onComplete:'openRoomView(e)')}"/>
				<br />
				<g:form action="list">
				<input type="hidden" name="viewType" value="list"/>
				<input type="submit" class="submit" value="List"/>
				<input type="Button" class="submit" value="Edit" onclick="${remoteFunction(action:'edit', params:'\'id=\'+$(\'#roomId\').val()', onComplete:'openRoomView(e)')}"/>
				</g:form>
				</div>
				</td>
				<td>
				<div style="width: 150px"><label><b>Highlight:</b></label><br />
				<br />
				<label><b>Bundle</b></label><br />
				<g:select id="bundleId" name="moveBundle" from="${moveBundleList}" value="${moveBundleId}" optionKey="id" optionValue="name" noSelection="${['':'Select Bundle...']}" 
					onchange="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val() +\'&moveBundleId=\'+this.value+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')', onComplete:'openRoomView(e)')}"/>
					</div>
				</td>
				<td class="buttonR">
				<div style="width: 150px">
				<label for="sourceView">
					<g:if test="${source == 'true'}">
						<input type="checkbox" name="sourceView" id="sourceView" value="" checked="checked" onclick="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val() +\'&moveBundleId=\'+$(\'#bundleId\').val()+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')', onComplete:'openRoomView(e)')}"/>&nbsp;Source
					</g:if>
					<g:else>
						<input type="checkbox" name="sourceView" id="sourceView" value="" onclick="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val() +\'&moveBundleId=\'+$(\'#bundleId\').val()+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')', onComplete:'openRoomView(e)')}"/>&nbsp;Source
					</g:else>
					</label><br />
				<label for="targetView">
					<g:if test="${target == 'true'}">
						<input type="checkbox" name="targetView" id="targetView" value="" checked="checked" onclick="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val() +\'&moveBundleId=\'+$(\'#bundleId\').val()+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')', onComplete:'openRoomView(e)')}"/>&nbsp;Target
					</g:if>
					<g:else>
						<input type="checkbox" name="targetView" id="targetView" value="" onclick="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val() +\'&moveBundleId=\'+$(\'#bundleId\').val()+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')', onComplete:'openRoomView(e)')}"/>&nbsp;Target
					</g:else>
					</label><br />
				</div>
				</td>
			</tr>
		</tbody>
	</table>
</div>
<div id="roomLayout"
	style="width: 100%; overflow-x: auto; border: 1px solid black">
<div id="room_layout" style="float: left;width: 800px;overflow-x: auto; border: 1px solid black">
<table border="0">
	<tr>
		<td style="vertical-align: top;" nowrap="nowrap"><b>Current Room :</b><br/>
		<table class="roomLayoutTable" cellpadding="0" cellspacing="0">
			<g:each in="${Rack.findAllByRoom(roomInstance)}" var="rack">
				<tr><td nowrap="nowrap" class="${rack.hasBelongsToMoveBundle(moveBundleId) ? 'highlight' : source=='true' && rack.source == 1 ? 'highlight' : target == 'true' && rack.source == 0 ? 'highlight' : '' }">
				<g:remoteLink controller="rackLayouts" action="save" params="[rackId:rack.id,frontView:'on',showCabling:'off']" onComplete="jQuery('#rackLayout').html(e.responseText);">
				${rack.tag}
				</g:remoteLink>
				</td></tr>
			</g:each>
		</table>
		</td>
		<g:each in="${roomInstanceList}" var="room">
			<g:if test="${room.id != roomInstance.id}">
			<td  style="vertical-align: top;" nowrap="nowrap"><b>${room} :</b><br/>
				<table class="roomLayoutTable" cellpadding="0" cellspacing="0">
					<g:each in="${Rack.findAllByRoom(room)}" var="rack">
						<tr><td nowrap="nowrap" class="${rack.hasBelongsToMoveBundle(moveBundleId) ? 'highlight' : source=='true' && rack.source == 1 ? 'highlight' : target == 'true' && rack.source == 0 ? 'highlight' : '' }">${rack.tag}</td></tr>
					</g:each>
				</table>
			</td>
			</g:if>
		</g:each>
	</tr>
</table>
</div>
<div style="float: left; margin-left: 50px;" id="rackLayout">
<table cellpadding=2 class="rack_elevation back">
	<tr>
		<th>U</th>
		<th>Device</th>
		<th>Bundle</th>
	</tr>

	<tr>
		<td class='empty'>42</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>41</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>40</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>39</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>38</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>37</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>36</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>35</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>34</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>33</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>32</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>31</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>30</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>29</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>28</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>27</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>26</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>25</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>24</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>23</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>22</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>21</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>20</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>19</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>18</td>
		<td rowspan='1' class='empty'>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>17</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>16</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>15</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>14</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>13</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>12</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>11</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>10</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>9</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>8</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>7</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>6</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>5</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>4</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>3</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>2</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>
	<tr>
		<td class='empty'>1</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>

	</tr>

</table>
</div>
</div>
</div>
</div>
</body>
</html>
