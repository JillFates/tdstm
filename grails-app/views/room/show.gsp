<%@page expressionCodec="none" %>
<%@page import="net.transitionmanager.asset.Rack" %>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page import="com.tdssrc.grails.HtmlUtil"%>
<html>
<body>
<div class="body" style="width:98%;">
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<g:if test="${flash.error}">
	<div class="errors">${flash.error}</div>
</g:if>

<div id="roomListView"></div>
<input type="hidden" id="redirectTo" value="room"/>
<input type="hidden" id="fromRoomOrRack" value="room"/>
<div class="dialog" style="border: 1px solid black;width: 1051px;overflow-x: auto;">
	<table style="width: 100%; border: none;border-spacing:0px;">
		<tbody>
			<tr>
				<td class="buttonR" style="vertical-align:top;width:220px;">
					<div>
						<h1 style="margin: 0px;">Data Center Room View</h1><br />
						<input type="hidden" id="roomTypeForCabling" value="${roomInstance.source}"/>
						<g:select id="roomId" name="id" from="${roomInstanceList}" value="${roomInstance.id}" optionKey="id" optionValue="${{it.location +' / '+it.roomName}}" onchange="getRackDetails()"/>
						<input type="hidden" id="selectedRackId" value="">
						<br />
						<g:form action="list">
							<span>${roomInstance.getRoomAddress('span')}</span>&nbsp;
							<a href="http://maps.google.com/maps?q=${roomInstance.getRoomAddress('link')}" target="_blank"> Map...</a><br />
							<input type="hidden" name="viewType" value="list" />
							<button type="submit" class="btn btn-default" ><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> List</button>
							<tds:hasPermission permission="${Permission.RoomEdit}">
								<button type="button" value="Edit" class="btn btn-default" onclick="${remoteFunction(action:'edit', params:'\'id=\'+$(\'#roomId\').val()', onComplete:'openRoomView(XMLHttpRequest)')}"><span class="glyphicon glyphicon-list-alt" aria-hidden="true"></span> Edit</button>
							</tds:hasPermission>
							<tds:hasPermission permission="${Permission.AssetEdit}">
								<input type="checkbox" id="auditCheckId" ${auditPref=='1' ? 'checked="checked"  value="1"' : 'value="0"'}
								onclick="if(this.checked){this.value = 1} else {this.value = 0 }; saveAuditPref(this.value, ${roomInstance.id})" />
								<label for="auditCheckId"><b>Audit</b></label>
							</tds:hasPermission>
							<br/>
						</g:form>
					</div>
				</td>
				<td style="vertical-align:top;width:150px;">
					<div style="width: 150px"><label><b>Highlight : </b></label><br /><br />
						<g:if test="${browserTestiPad}">
							<g:select id="bundleId" name="moveBundleId" from="${bundleList}" value="${moveBundleId=='taskReady'? 'taskReady' :moveBundleList.id}" optionKey ="${-2}" optionValue ="${1}" noSelection="${['all':'All']}"
								onChange="getRackDetails('ipad')"
							/>
						</g:if>
						<g:else>
							<g:select id="bundleId" name="moveBundleId" from="${bundleList}" value="${moveBundleId=='taskReady'? 'taskReady' :moveBundleList.id}" optionKey ="${-2}" optionValue ="${1}" noSelection="${['all':'All']}"
							multiple="multiple" size="3" onChange="getRackDetails()"/>
						</g:else>
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
									<label for="Used" ><input type="radio" name="capacityType" id="Used" value="Used" ${capacityType=='Used' ? 'checked="checked"' : ''} onclick="capacityView()"/>&nbsp;Used&nbsp;</label>
									<label for="Remaining" ><input type="radio" name="capacityType" id="Remaining" ${capacityType=='Remaining' ? 'checked="checked"' : ''} value="Remaining" onclick="capacityView()"/>&nbsp;Remaining<br/></label>
									<label for="otherBundle" >
										<g:if test="${moveBundleList.id?.contains('all')}">
											<input type="checkbox" name="otherBundle" id="otherBundle" disabled="disabled"  checked="checked" onclick="getRackLayout( $('#selectedRackId').val() )"/>
										</g:if><g:else>
											<input type="checkbox" name="otherBundle" id="otherBundle" onclick="getRackLayout( $('#selectedRackId').val() )" checked="checked" disabled="disabled"/>
										</g:else>
										&nbsp;w/ other bundles</label>
								</td>
							</tr>
						</table>
					</div>
					<div  id="scale_div" style="float: left;display: none;" >
						<table class="scale_tab" style="width: auto; padding: 0px; border: none;border-collapse: collapse;">
							<tr><td class="cap_tab rack_cap20" id="cap20">&nbsp;</td></tr>
							<tr><td class="cap_tab rack_cap32" id="cap32">&nbsp;</td></tr>
							<tr><td class="cap_tab rack_cap44" id="cap44">&nbsp;</td></tr>
							<tr><td class="cap_tab rack_cap56" id="cap56">&nbsp;</td></tr>
							<tr><td class="cap_tab rack_cap68" id="cap68">&nbsp;</td></tr>
							<tr><td class="cap_tab rack_cap80" id="cap80">&nbsp;</td></tr>
							<tr><td class="cap_tab rack_cap100" id="cap100">&nbsp;</td></tr>
						</table>
					</div>
				</td>
				<td style="vertical-align:top;width:397px;padding:0px;" id="rackPowerTd">
				</td>
			</tr>
		</tbody>
	</table>
</div>
<div id="roomLayout_body" style="border: 2px solid black">
	<g:set var="numrows" value="${1}" />
	<g:set var="tilerows" value="${roomInstance.roomDepth / 2}" />
	<g:set var="numcols" value="${1}" />
	<g:set var="tilecols" value="${roomInstance.roomWidth / 2}" />

	<div id="room_layout" style="position:relative; height:800px; overflow:auto; border:0px; float:left;margin-right:10px">
		<table id="room_layout_table" cellpadding="0" cellspacing="0" style="width:${tilecols *42}px; height:${tilerows *42}px; border:0px;">
			<g:while test="${numrows <= tilerows }">
				<tr>
					<g:set var="numcols" value="${1}" />
					<g:while test="${numcols <= tilecols }">
						<td class="room_tile" numcols="${numcols++}">&nbsp;</td>
					</g:while>
				</tr ><!-- ${numrows++} -->
			</g:while>
		</table>
		<g:each in="${Rack.findAllByRoom(roomInstance)}" var="rack" status='i'>
			<g:if test="${rack.rackType == 'Rack'}">
				<a href="#" onclick="getRackLayout(${rack.id })">
					<g:if test="${ ! rack.model?.layoutStyle }">
					<div id="rack_${rack.id}" style="top:${rack.roomY ? rack.roomY : 0}px;left:${rack?.roomX ? rack.roomX : 0}px;" class="${rack.hasBelongsToMoveBundle(moveBundleList.id) ? 'rack_highlight_'+rack.front : statusList[rack.id] ? 'rack_highlight_'+rack.front+' '+statusList[rack.id] : source=='true' && rack.source == 1 ? 'rack_highlight_'+rack.front : target == 'true' && rack.source == 0 ? 'rack_highlight_'+rack.front : rack.front ? 'rack_highlight_no_'+rack.front :'rack_highlight_no_'+rack.front } adjustRack">
				</g:if>
				<g:else>
					<div id="rack_${rack.id}" style="top:${rack.roomY ? rack.roomY : 0}px;left:${rack.roomX ? rack.roomX : 0}px;" class="${rack.model?.layoutStyle}_${rack.front}">
				</g:else>
						<span id="cap_count_${rack.id}" class="capCount" >&nbsp;</span>
						<div id="rack_div_${i}" class="racktop_label" onclick="$('#selectedRackId').val(${rack.id})">${HtmlUtil.escape(rack.tag)}</div>
					</div>
				</a>
			</g:if>
			<g:else>
				<div id="rack_${rack.id}" style="position:absolute;top:${rack.roomY ? rack.roomY : 0}px;left:${rack.roomX ? rack.roomX : 0}px;" class="room_${rack.rackType}_${rack.front}">
					<div class="racktop_label" >${rack.tag}</div>
				</div>
			</g:else>
		</g:each>
		<span>Floor ${roomInstance.roomWidth}ft x ${roomInstance.roomDepth}ft = ${roomInstance.roomWidth * roomInstance.roomDepth} sqft</span>
	</div>
	<div id="rackLayout">
		<table style="display:none" cellpadding=2 class="rack_elevation back">
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
	<div id="auditDetailViewId" class="table.rack_elevation.back" style="display: none;">
	</div>
</div>

<script type="text/javascript">
$(document).ready(function() {
	$("#capacityViewId").val('${capacityView}');
	$("#createEntityView").dialog({autoOpen: false});
	$("#showEntityView").dialog({autoOpen: false});
    $("#editEntityView").dialog({autoOpen: false});
    $("#editDialog").dialog({ autoOpen: false });
    $("#createRoomDialog").dialog({ autoOpen: false });
    $("#mergeRoomDialog").dialog({ autoOpen: false });
    $("#listDialog").dialog({ autoOpen: false });
    $("#cablingDialogId").dialog({ autoOpen: false });
    $("#manufacturerShowDialog").dialog({ autoOpen: false });
	$("#modelShowDialog").dialog({ autoOpen: false });
})
initializeRacksInRoom( [] );
capacityView();


function updateRackPower(rackId){
	$("#selectedRackId").val(rackId)
	$("#redirectTo").val("room_"+rackId)
	var capacityView = $("#capacityViewId").val()
	var capacityType = $('input[name=capacityType]:checked').val()
	var moveBundleId = ''
	var otherBundle = $("#otherBundle").is(":checked") ? 'on' : ''
	jQuery.ajax({
		url: tdsCommon.createAppURL("/room/retrieveRackPowerData"),
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
		url: tdsCommon.createAppURL("/room/retrieveCapacityView"),
		data: "roomId="+roomId+"&capacityView="+capacityView+"&capacityType="+capacityType,
		type:'POST',
		success: function(data) {
			if(data != "None"){
				var racks = data.racks
				$(".adjustRack").each(function(){
 					var divId = $(this).attr('id').split("_")[1]
 					var rackCount = data.rackCountMap[$(this).attr('id')]
 					if(rackCount || rackCount == 0){
 		 		   	 	$('#cap_count_'+divId).html(rackCount)
 		 		   	 	$('#cap_count_'+divId).show()
 					}else {
 						$('#cap_count_'+divId).html("&nbsp;")
 						$('#cap_count_'+divId).hide()

 					}

				})
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
				$(".adjustRack").each(function(){
 					var divId = $(this).attr('id').split("_")[1]
 		 		    $('#cap_count_'+divId).html("&nbsp;")

				})
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
function getRackDetails(browser){
	var capacityView = $("#capacityViewId").val()
	var capacityType = $('input[name=capacityType]:checked').val()
	var bundles = new Array()
	$("#bundleId option:selected").each(function () {
		bundles.push($(this).val())
   	});
   	var otherBundle = $("#otherBundle").val()
	${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val()+\'&moveBundleId=\'+bundles+\'&source=\'+$(\'#sourceView\').is(\':checked\')+\'&target=\'+$(\'#targetView\').is(\':checked\')+\'&otherBundle=\'+otherBundle+\'&capView=\'+capacityView+\'&capType=\'+capacityType', onComplete:'openRoomView(XMLHttpRequest,browser)')}
}

function createAssetPage(type,source,rack,roomName,location,position){
	var room = $("#redirectTo").val();
	   ${remoteFunction(action:'create',controller:'assetEntity',params:'\'redirectTo=\'+room', onComplete:'createEntityView(XMLHttpRequest,type,source,rack,roomName,location,position)')}
}
</script>
</body>
</html>
