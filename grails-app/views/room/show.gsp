<html>
<body>
<div class="body">
<div id="roomListView">
<fieldset><legend><b>Room View</b></legend>
<div class="dialog"><g:form action="save" name="rackLayoutCreate"
	method="post" target="_blank" onsubmit="return submitForm(this)"
	style="border: 1px solid black; width: 100%">
	<table style="width: auto; border: none">
		<tbody>
			<tr>
				<td class="buttonR">
				<div><select id="roomId" name="room"
					onchange="getRackDetails(this.id)" style="width: 150px">
					<g:each in="${roomInstanceList}" var="room">
						<option value="${room?.id}">${room?.roomName} /
						${room?.location}</option>
					</g:each>
				</select> <br />
				<input type="Button" class="submit" value="List" /> <input
					type="Button" class="submit" value="Edit" /></div>
				</td>

				<td>
				<div style="width: 150px"><label><b>Highlight:</b></label><br />
				<br />
				<label><b>Bundle</b></label><br />
				<select id="bundleId" name="moveBundle" multiple="multiple" size="3"
					onchange="getRoomDetails(this.id)" style="width: 150px">
					<option value="all" selected="selected">All</option>
					<g:each in="${moveBundleList}" var="moveBundle">
						<option value="${moveBundle?.id}">${moveBundle?.name}</option>
					</g:each>
				</select></div>
				</td>

				<td class="buttonR">
				<div style="width: 150px"><label for="sourceView"><input
					type="checkbox" name="sourceView" id="sourceView" />&nbsp;Source</label>&nbsp<br />
				<label for="targetView"><input type="checkbox"
					name="targetView" id="targetView" />&nbsp;Target</label><br />
				</div>
				</td>

			</tr>
		</tbody>
	</table>
</g:form></div>
<div id="rackLayout"
	style="width: 100%; overflow-x: auto; border: 1px solid black">
<div id="room_layout" style="float: left;width: 800px;overflow-x: auto; border: 1px solid black">
<table border="0">
	<tr>
		<td style="vertical-align: top;">
		<table class="roomLayoutTable" cellpadding="0" cellspacing="0">
			<g:each in="${Rack.findAllByRoom(roomInstance)}" var="rack">
				<tr><td nowrap="nowrap">${rack.tag}</td></tr>
			</g:each>
		</table>
		</td>
		<g:each in="${roomInstanceList}" var="room">
			<td  style="vertical-align: top;">
				<table class="roomLayoutTable" cellpadding="0" cellspacing="0">
					<g:each in="${Rack.findAllByRoom(room)}" var="rack">
						<tr><td nowrap="nowrap">${rack.tag}</td></tr>
					</g:each>
				</table>
			</td>
		</g:each>
	</tr>
</table>
</div>
<div style="float: left; margin-left: 50px;">
<table cellpadding=2 class="rack_elevation back">
	<tr>
		<th>U</th>
		<th>Device</th>
		<th>Bundle</th>
		<th>Cabling</th>
	</tr>

	<tr>
		<td class='empty'>42</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>41</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>40</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>39</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>38</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>37</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>36</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>35</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>34</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>33</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>32</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>31</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>30</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>29</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>28</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>27</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>26</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>25</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>24</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>23</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>22</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>21</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>20</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>19</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>18</td>
		<td rowspan='1' class='empty'>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>17</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>16</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>15</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>14</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>13</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>12</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>11</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>10</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>9</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>8</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>7</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>6</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>5</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>4</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>3</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>2</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>
	<tr>
		<td class='empty'>1</td>
		<td rowspan=1 class=empty>&nbsp;</td>
		<td>&nbsp;</td>
		<td>&nbsp;</td>
	</tr>

</table>
</div>
</div>
</fieldset>
</div>
</div>
</body>
</html>
