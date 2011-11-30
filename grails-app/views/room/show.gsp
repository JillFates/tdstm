<html>
<body>
<div class="body" style="width:98%;">
<div id="roomListView"></div>
<input type="hidden" id="redirectTo" value="room"/>
<div class="dialog" style="border: 1px solid black;">
	<table style="width: 100%; border: none;border-spacing:0px;">
		<tbody>
			<tr>
				<td class="buttonR" style="vertical-align:top;width:240px;">
				<div>
				<h1 style="margin: 0px;">Room View</h1><br />
				<g:select id="roomId" name="id" from="${roomInstanceList}" value="${roomInstance.id}" optionKey="id" optionValue="${{it.location +' / '+it.roomName}}" onchange="getRackDetails()"/>
				<input type="hidden" id="selectedRackId" value="">
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
				<td style="vertical-align:top;width:180px;">
				<div style="width: 150px"><label><b>Highlight:</b></label><br /><br />
				<label><b>Bundle</b></label><br />
					<g:select id="bundleId" name="moveBundleId" from="${MoveBundle.findAllByProject(project)}" value="${moveBundleList.id}" optionKey="id" optionValue="name" noSelection="${['all':'All']}" multiple="multiple" size="3"
					  onChange="getRackDetails()"/>
				</div>
				</td>
				<td style="vertical-align:top;width:50px;">
				<div style="width: 50px">
				<label for="sourceView" style="display:none">
					<g:if test="${source == 'true'}">
					<input type="hidden" name="sourceView" id="sourceView" value="" checked="checked" onclick="getRackDetails()" />&nbsp;Source
					</g:if>
					<g:else>
					<input type="hidden" name="sourceView" id="sourceView" value="" onclick="getRackDetails()" />&nbsp;Source
					</g:else>
					</label><br />
				<label for="targetView" style="display:none">
					<g:if test="${target == 'true'}">
					<input type="hidden" name="targetView" id="targetView" value="" checked="checked" onclick="getRackDetails()" />&nbsp;Target
					</g:if>
					<g:else>
					<input type="hidden" name="targetView" id="targetView" value="" onclick="getRackDetails()" />&nbsp;Target
					</g:else>
					</label><br />
				</div>
				</td>
				<td class="cap_tab" style="vertical-align:top;width:250px;">
					<div style="float: left;">
						<table class="cap_tab" style="width: auto; padding: 1px; border: none;">
							<tr>
								<td class="cap_tab"><b>Capacity View:</b></td>
							</tr>
							<tr>
								<td class="cap_tab">
									<select name="capacityView" size="1" onchange="capacityView()" id="capacityViewId">
										<option label="None" value="None">None</option>
										<option label="Space" value="Space">Space</option>
										<option label="Power" value="Power">Power</option>
										<option label="Heat" value="Heat" disabled="disabled">Heat</option>
										<option label="Weight" value="Weight" disabled="disabled">Weight</option>
										<option label="Ethernet" value="Ethernet" disabled="disabled">Ethernet</option>
										<option label="Fiber" value="Fiber" disabled="disabled">Fiber</option>
									</select>
								</td>
							</tr>
							<tr>
								<td>
									<label for="Used" ><input type="radio" name="capacityType" id="Used" value="Used" onclick="capacityView()"/>&nbsp;Used&nbsp;<br /></label>
									<label for="Remaining" ><input type="radio" name="capacityType" id="Remaining" checked="checked" value="Remaining" onclick="capacityView()"/>&nbsp;Remaining<br/></label>
									<label for="otherBundle" >
										<g:if test="${moveBundleList.id?.contains('all')}">
											<input type="checkbox" name="otherBundle" id="otherBundle" disabled="disabled"  checked="checked" onclick="getRackLayout( $('#selectedRackId').val() )"/>
										</g:if><g:else>
											<input type="checkbox" name="otherBundle" id="otherBundle" onclick="getRackLayout( $('#selectedRackId').val() )"/>
										</g:else>
										&nbsp;w/ other bundles</label>
								</td>
							</tr>
						</table>
					</div>
					<div  id="scale_div" style="float: left;display: none;" >
						<table class="scale_tab" style="width: auto; padding: 0px; border: none;border-collapse: collapse;">
							<tr>
								<td class="cap_tab rack_cap20" id="cap20">&nbsp;</td>
							</tr>
							<tr>
								<td class="cap_tab rack_cap32" id="cap32">&nbsp;</td>
							</tr>
							<tr>
								<td class="cap_tab rack_cap44" id="cap44">&nbsp;</td>
							</tr>
							<tr>
								<td class="cap_tab rack_cap56" id="cap56">&nbsp;</td>
							</tr>
							<tr>
								<td class="cap_tab rack_cap68" id="cap68">&nbsp;</td>
							</tr>
							<tr>
								<td class="cap_tab rack_cap80" id="cap80">&nbsp;</td>
							</tr>
							<tr>
								<td class="cap_tab rack_cap100" id="cap100">&nbsp;</td>
							</tr>
						</table>
					</div>
				</td>
				<td style="vertical-align:top;width:310px;padding:0px;" id="rackPowerTd">
				</td>
			</tr>
		</tbody>
	</table>
</div>
<div id="roomLayout_body" style="width: 1100px; overflow-x: auto; border: 2px solid black">
	<input id="selectedRackId" type="hidden">
	<g:set var="numrows" value="1" />
	<g:set var="tilerows" value="${roomInstance.roomDepth / 2}" />
	<g:set var="numcols" value="1" />
	<g:set var="tilecols" value="${roomInstance.roomWidth / 2}" />

	<div id="room_layout" style="position:relative;width: 650px;height: 800px;overflow-x: auto; border: 0px solid black;float: left;">
		<table id="room_layout_table" cellpadding="0" cellspacing="0" style="width:${tilecols * 42}px;height:auto;border:0px">
			<g:while test="${numrows <= tilerows }">
				<tr>
					<g:set var="numcols" value="1" />
					<g:while test="${numcols <= tilecols }">
						<td class="room_tile" numcols="${numcols++}">&nbsp;</td>
					</g:while>
				</tr ><!-- ${numrows++} -->
			</g:while>
			<tr>
			<td colspan="${tilecols}">Floor ${roomInstance.roomWidth}ft x ${roomInstance.roomDepth}ft = ${roomInstance.roomWidth * roomInstance.roomDepth} sqft</td>
			</tr>
		</table>
			<g:each in="${Rack.findAllByRoom(roomInstance)}" var="rack" status='i'>
				<g:if test="${rack.rackType == 'Rack'}">
					<a href="#" onclick="getRackLayout(${rack.id })">
	                 <g:if test="${rack?.model?.layoutStyle == null}">			
					    <div id="rack_${rack.id}" style="top:${rack.roomY ? rack.roomY : 0}px;left:${rack?.roomX ? rack.roomX : 0}px;" class="${rack.hasBelongsToMoveBundle(moveBundleList.id) ? 'rack_highlight_'+rack.front : source=='true' && rack.source == 1 ? 'rack_highlight_'+rack.front : target == 'true' && rack.source == 0 ? 'rack_highlight_'+rack.front : rack.front ? 'rack_highlight_no_'+rack.front :'rack_highlight_no_'+rack.front }">
					 </g:if>
					 <g:else>
					     <div id="rack_${rack.id}" style="top:${rack.roomY ? rack.roomY : 0}px;left:${rack.roomX ? rack.roomX : 0}px;" class="${rack.model?.layoutStyle}">
					 </g:else>
						<div id="rack_div_${i}" class="racktop_label" onclick="$('#selectedRackId').val(this.id)">${rack.tag}</div>
					</div>
					</a>
				</g:if>
				<g:else>
					<div id="rack_${rack.id}" style="position:absolute;top:${rack.roomY ? rack.roomY : 0}px;left:${rack.roomX ? rack.roomX : 0}px;" class="room_${rack.rackType}_${rack.front}">
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
capacityView()
function updateRackPower(rackId){
	$("#selectedRackId").val(rackId)
	var capacityView = $("#capacityViewId").val()
	var capacityType = $('input[name=capacityType]:checked').val()
	var moveBundleId = ''
	$("#bundleId option:selected").each(function () {
		moveBundleId +="moveBundleId="+$(this).val()+"&"
   	});
	var otherBundle = $("#otherBundle").is(":checked") ? 'on' : ''
	jQuery.ajax({
		url: "getRackPowerData",
		data: moveBundleId+"roomId="+$('#roomId').val()+"&rackId="+rackId+"&capacityView="+capacityView+"&capacityType="+capacityType+"&otherBundle="+otherBundle,
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
function capacityView(){
	var capacityView = $("#capacityViewId").val()
	var capacityType = $('input[name=capacityType]:checked').val()
	var roomId = "${roomInstance.id}"
	jQuery.ajax({
		url: "getCapacityView",
		data: "roomId="+roomId+"&capacityView="+capacityView+"&capacityType="+capacityType,
		type:'POST',
		success: function(data) {
			if(data != "None"){
				var racks = data.racks
				$("#cap20").addClass("rack_cap20").html(data.view["cap20"])
				$("#cap32").addClass("rack_cap32").html(data.view["cap32"])
				$("#cap44").addClass("rack_cap44").html(data.view["cap44"])
				$("#cap56").addClass("rack_cap56").html(data.view["cap56"])
				$("#cap68").addClass("rack_cap68").html(data.view["cap68"])
				$("#cap80").addClass("rack_cap80").html(data.view["cap80"])
				$("#cap100").addClass("rack_cap100").html(data.view["cap100"])
				
				$("#scale_div").show()
				for(i=0; i< racks.length; i++){
					$("#rack_"+racks[i]).removeClass("rack_cap20")
					$("#rack_"+racks[i]).removeClass("rack_cap32")
					$("#rack_"+racks[i]).removeClass("rack_cap44")
					$("#rack_"+racks[i]).removeClass("rack_cap56")
					$("#rack_"+racks[i]).removeClass("rack_cap68")
					$("#rack_"+racks[i]).removeClass("rack_cap80")
					$("#rack_"+racks[i]).removeClass("rack_cap100")
					$("#rack_"+racks[i]).addClass(data.rackData[racks[i]])
				}
			} else {
				$(".rack_cap20").removeClass("rack_cap20")
				$(".rack_cap32").removeClass("rack_cap32")
				$(".rack_cap44").removeClass("rack_cap44")
				$(".rack_cap56").removeClass("rack_cap56")
				$(".rack_cap68").removeClass("rack_cap68")
				$(".rack_cap80").removeClass("rack_cap80")
				$(".rack_cap100").removeClass("rack_cap100")
				$("#scale_div").hide()
			}
			 updateRackPower($("#selectedRackId").val())
		}
	});
	$("#editDialog").dialog("close")
	$("#createRoomDialog").dialog("close")
	$("#mergeRoomDialog").dialog("close")
	$("#createDialog").dialog("close")
	$("#listDialog").dialog("close")
}
function getRackDetails(){
	var bundles = new Array()
	$("#bundleId option:selected").each(function () {
		bundles.push($(this).val())
   	});
   	var otherBundle = $("#otherBundle").val()
	${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val()+\'&moveBundleId=\'+bundles+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')+\'&otherBundle=\'+otherBundle', onComplete:'openRoomView(e)')}
}
function getRackLayout( rackId ){
	if(rackId){
		var otherBundle = $("#otherBundle").is(":checked") ? 'on' : ''
		var moveBundleId = ''
		$("#bundleId option:selected").each(function () {
			moveBundleId +="moveBundleId="+$(this).val()+"&"
	   	});
		new Ajax.Request('../rackLayouts/save',{asynchronous:true,evalScripts:true,onSuccess:function(e){updateRackPower( rackId )},onComplete:function(e){jQuery('#rackLayout').html(e.responseText);},parameters:moveBundleId+'rackId='+rackId+'&backView=off&showCabling=off&otherBundle='+otherBundle+'&bundleName=on&hideIcons=on'});return false;
	}
}
</script>
</body>
</html>
