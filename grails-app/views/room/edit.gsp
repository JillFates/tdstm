<%@page import="net.transitionmanager.asset.Rack" %>
<html>
<head>
</head>
<body>

	<div class="body">
		<div id="roomListView">
			<span class="span"> <b>Data Center Room Edit View </b>
			</span>
			<g:form action="update" onsubmit="return submitForm(this)">
				<div class="dialog" style="border: 1px solid black;">
					<table style="width: auto; border: none">
						<tbody>
							<tr>
								<td>Data Center<br /> <input type="hidden" name="id"
									id="roomId" value="${roomInstance.id}"> <input
									type="text" name="location" id="locationId"
									value="${roomInstance.location}">
								</td>
								<td>Room<br /> <input type="text" name="roomName"
									id="roomNameId" value="${roomInstance.roomName}">
								</td>
								<td>Width (ft)<br /> <input type="text" name="roomWidth"
									id="roomWidthId" value="${roomInstance.roomWidth}" size="4"
									onblur="roundValue(this.value,'roomWidthId')">
								</td>
								<td style="width: 78px;">Depth (ft)<br /> <input type="text" name="roomDepth"
									id="roomDepthId" value="${roomInstance.roomDepth}" size="4"
									onblur="roundValue(this.value,'roomDepthId')">
								</td>
								<td style="width: 190px;">Address<br /> <input type="text" name="address"
									id="addressId" value="${roomInstance?.address}" size="20">
								</td>
								<td style="width: 120px;">City <br /> <input type="text" name="city" id="cityId"
									value="${roomInstance?.city}" size="10">
								</td>
								<td style="width: 79px;">stateProv <br /> <input type="text" name="stateProv"
									id="stateProvId" value="${roomInstance?.stateProv}" size="4">
								</td>
								<td style="width: 80px;">Postal Code <br /> <input type="text"
									name="postalCode" id="postalCodeId"
									value="${roomInstance?.postalCode}" size="4">
								</td>
								<td>Country<br /> <input type="text" name="country"
									id="countryId" value="${roomInstance?.country}" size="5">
								</td>
							</tr>
							<tr>
								<td class="buttonR" colspan="3">
									<button type="button" value="Cancel" class="btn btn-default" onclick="${remoteFunction(action:'show', params:'\'id=\'+$(\'#roomId\').val()', onComplete:'openRoomView(XMLHttpRequest)')}"><span class="glyphicon glyphicon-ban-circle" aria-hidden="true"></span> Cancel</button>
									<button type="submit" value="Update" class="btn btn-default"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span> Update</button>
								</td>
								<td class="buttonR"
									style="vertical-align: top;" colspan="6"
									nowrap="nowrap"><span> <label for="addTargetRoom"><b>Target
												room:</b></label> <input type="checkbox" id="addTargetRoom"
										name="addTargetRoom" ${roomInstance?.source== 0 ? 'checked="checked"' : ''}/>&nbsp;
								</span> <span> <label for="showAll"><b>Show All:</b></label> <input
										type="checkbox" id="showAll" name="showAll"
										${prefVal && prefVal == 'TRUE' ?  'value="1" checked="checked"' :  ' value="0"' }
										onclick="if(this.checked){this.value = 1} else {this.value = 0 }; showRackTable(this.value)" />&nbsp;
								</span> <span> <label for="showRoomObjects"><b>Draggable:</b></label>
										<input type="checkbox" id="showRoomObjects"
										name="showRoomObjects"
										${draggableRack == 'on'? 'checked' :'checked' }
										onclick="enableDraggableRack()" />&nbsp;
										<button type="button" value="Power Connect" class="btn btn-default" onclick="assignPowersForRoom(${roomInstance.id})"><span class="glyphicon glyphicon-off" aria-hidden="true"></span> Connect all Power</button>
								</span> <span> <b>Add to Room:</b>&nbsp;
										<button type="button" value="Rack" class="btn btn-default" onclick="createRack(this.value)"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Rack</button>
										<button type="button" value="UPS" class="btn btn-default" onclick="createRack(this.value)"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> UPS</button>
										<button type="button" value="CRAC" class="btn btn-default" onclick="createRack(this.value)"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> CRAC</button>
										<button type="button" value="Object" class="btn btn-default" onclick="createRack(this.value)"><span class="glyphicon glyphicon-plus" aria-hidden="true"></span> Object</button>
								</span></td>
							</tr>
						</tbody>
					</table>
				</div>
				<div id="roomLayout"
					style="width: 600px; overflow-x: auto; border: 2px solid black; position: relative">
					<g:set var="numrows" value="${1}" />
					<g:set var="tilerows" value="${roomInstance.roomDepth / 2}" />
					<g:set var="numcols" value="${1}" />
					<g:set var="tilecols" value="${roomInstance.roomWidth / 2}" />

					<div id="room_layout"
						style="position: relative; width: 700px; height: 800px; overflow-x: auto; border: 0px solid black; float: left;">
						<table id="room_layout_table" cellpadding="0" cellspacing="0"
							style="width:${tilecols *42}px; height:${tilerows *42}px; border:0px;">
							<g:while test="${numrows <= tilerows }">
								<tr>
									<g:set var="numcols" value="${1}" />
									<g:while test="${numcols <= tilecols }">
										<td onclick="deselectAll()" class="room_tile"
											numcols="${numcols++}">&nbsp;</td>
									</g:while>
								</tr>
								<!-- ${numrows++} -->
							</g:while>
						</table>
						<g:each in="${rackInstanceList}" var="rack">
							<g:if test="${rack.rackType == 'Rack'}">
								<g:if test="${ ! rack.model?.layoutStyle }">
									<div align="center" id="rack_${rack.id}"
										style="top:${rack.roomY}px; left:${rack.roomX}px;"
										onmouseout="updateXYPositions(this.id)"
										class="${ rack.front ? 'rack_highlight_no_'+rack.front :'rack_highlight_no_L' } dragRack draggable rackDiv">
								</g:if>
								<g:else>
									<div align="center" id="rack_${rack.id}"
										style="top:${rack.roomY}px; left:${rack.roomX}px;"
										onmouseout="updateXYPositions(this.id)"
										class="${rack.model?.layoutStyle}_${rack.front} dragRack draggable">
								</g:else>
								<span id="rackLabel_${rack.id}"><br> ${rack.tag}</br></span>
					</div>
					<div align="center" id="rackDetailDiv_${rack.id}"
						class="rackDetailDiv divShadow"></div>
					</g:if>
					<g:else>
						<div align="center" id="rack_${rack.id}"
							style="top:${rack.roomY}px;left:${rack.roomX}px;"
							onmouseout="updateXYPositions(this.id)"
							class="room_${rack.rackType}_${rack.front} dragRack draggable">
							<span id="rackLabel_${rack.id}"
								style="background-color: white; z-index: 1;"><br> ${rack.tag}</br></span>
						</div>
					</g:else>
					</g:each>
					<g:each in="${newRacks}" var="rack">
						<div align="center" id="rack_${rack}"
							style="top: 0px; left: 0px; display: none;"
							onmouseout="updateXYPositions(this.id)"
							class="rack_highlight_no_L  draggable" onclick="showRoomObjectsDiv(this.id)">
							<span id="rackLabel_${rack}"
								style="background-color: white; z-index: 1;" ><br>&nbsp;</br></span>
						</div>
						<div id="rackDetailDivv_${rack}" class="rackDetailDiv divShadow"
							style="top: -503px; left: 75px;"></div>
					</g:each>
				</div>
		</div>
		<div style="left: 580px;" id="roomObjects" >
			<div style="background-color: rgb(95, 159, 207); height: 26px; padding-top: 6px; padding-left: 7px; color: white; cursor: pointer;"><span class="glyphicon glyphicon-move"></span></div>
			<div>
				<table border="0" id="rackTableId">
					<tr>
						<th>Rack<input type="hidden" id="rackCount" name="rackCount"
							value="50000"></th>
						<th>X</th>
						<th>Y</th>
						<th>Front</th>
						<th>A(${tds.powerTypeShort()})
						</th>
						<th>B(${tds.powerTypeShort()})
						</th>
						<th>C(${tds.powerTypeShort()})
						</th>
						<th>Type</th>
						<th>Model</th>
						<th>Assets</th>
					</tr>
					<g:each in="${rackInstanceList}" var="rack" status="i">
						<tr id="rackEditRow_${rack.id}"
							class="${(i % 2) == 0 ? 'odd' : 'even'} rowShow">
							<td><input type="hidden" name="rackId" value="${rack.id}" />
								${rack.source == 1 ? 'S' : 'T' } <input type="text"
								class="focusShadow" id="tag_${rack.id}" name="tag_${rack.id}"
								value="${rack.tag}" size="10"
								onchange="changeLabel(${rack.id},this.value)" /></td>
							<td><input type="text" class="focusShadow"
								id="roomXId_${rack.id}" name="roomX_${rack.id}"
								value="${rack.roomX}" size="3"
								onkeyup="changeRackPosition(${rack.id},this.value, 'left')" /></td>
							<td><input type="text" class="focusShadow"
								id="roomYId_${rack.id}" name="roomY_${rack.id}"
								value="${rack.roomY}" size="3"
								onkeyup="changeRackPosition(${rack.id},this.value, 'top')" /></td>
							<td><g:select id="frontId_${rack.id}" name="front_${rack.id}"
									from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(Rack).front.inList}" value="${rack.front}"
									onchange="updateRackStyle(${rack.id}, this.value, jQuery('#rackTypeId_'+${rack.id}).val())"
									style="width:40px;"/></td>
							<td><input type="text" class="focusShadow" id="powerA_${rack.id}" name="powerA_${rack.id}" value="${tds.rackPower(power: rack.powerA)}" size="3" /></td>
							<td><input type="text" class="focusShadow" id="powerB_${rack.id}" name="powerB_${rack.id}" value="${tds.rackPower(power: rack.powerB)}" size="3" /></td>
							<td><input type="text" class="focusShadow" id="powerC_${rack.id}" name="powerC_${rack.id}" value="${tds.rackPower(power: rack.powerC)}" size="3" /></td>
							<td><g:select id="rackTypeId_${rack.id}"
									name="rackType_${rack.id}"
									from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(Rack).rackType.inList}"
									value="${rack.rackType}"
									onchange="updateRackStyle(${rack.id}, jQuery('#frontId_'+${rack.id}).val(), this.value)"
									style="width:100px;"/></td>
							<td><span id="modelSpan_${rack.id}"><g:select
										class="rackModel" id="model_${rack.id}" name="model_${rack.id}"
										from="${modelList}" noSelection="[null:'Select Model']"
										value="${rack.model?.id}" optionKey="id"
										optionValue="${{it.manufacturer.name+' / '+it.modelName} }"/></span>
							</td>
							<td>
								${rack.assets.size()}&nbsp;&nbsp;&nbsp; <g:if
									test="${rack.assets.size() == 0}">
									<a href="javascript:verifyAndDeleteRacks(${rack.id})"><span
										class="clear_filter"><u>X</u></span></a>
								</g:if>
							</td>
						</tr>
					</g:each>
					<g:each in="${newRacks}" var="rack" status="i">
						<tr id="rackEditRow_${rack}"
							class="${(i % 2) == 0 ? 'odd' : 'even'}" style="display: none;">
							<td><input type="hidden" name="rackId" value="${rack}" /> <input
								type="text" class="focusShadow" id="tag_${rack}"
								name="tag_${rack}" value="" size="10"
								onchange="changeLabel(${rack},this.value)" /></td>
							<td><input type="text" class="focusShadow"
								id="roomXId_${rack}" name="roomX_${rack}" value="" size="3"
								onkeyup="changeRackPosition(${rack},this.value, 'left')" /></td>
							<td><input type="text" class="focusShadow"
								id="roomYId_${rack}" name="roomY_${rack}" value="" size="3"
								onkeyup="changeRackPosition(${rack},this.value, 'top')" /></td>
							<td><g:select id="frontId_${rack}" name="front_${rack}"
									from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(Rack).front.inList}"
									onchange="updateRackStyle(${rack}, this.value, jQuery('#rackTypeId_'+${rack}).val())"
									style="width:40px;"/></td>
							<td><input type="text" class="focusShadow" id="newPowerA_${rack}" name="powerA_${rack}" value="${tds.rackPower(powerProperty: 'powerA')}" size="3" /></td>
							<td><input type="text" class="focusShadow" id="newPowerB_${rack}" name="powerB_${rack}" value="${tds.rackPower(powerProperty: 'powerB')}" size="3" /></td>
							<td><input type="text" class="focusShadow" id="newPowerC_${rack}" name="powerC_${rack}" value="${tds.rackPower(powerProperty: 'powerC')}" size="3" /></td>
							<td><g:select id="rackTypeId_${rack}" name="rackType_${rack}"
									from="${com.tdssrc.grails.GormUtil.getConstrainedProperties(Rack).rackType.inList}" value="Rack"
									onchange="updateRackStyle(${rack}, jQuery('#frontId_'+${rack}).val(), this.value)"
									style="width:100px;"/></td>
							<td><span id="modelSpan_${rack}"><g:select
										class="rackModel" id="model_${rack}" name="model_${rack}"
										from="${modelList}" noSelection="[null:'Select Model']"
										value="${defaultRackModel?.id}" optionKey="id"
										optionValue="${{it.manufacturer.name+' / '+it.modelName} }"/></span></td>
							<td>0&nbsp;&nbsp;&nbsp;<a
								href="javascript:verifyAndDeleteRacks(${rack})"><span
									class="clear_filter"><u>X</u></span></a></td>
						</tr>
					</g:each>
				</table>
			</div>
		</div>
		</g:form>
	</div>
	</div>
	<script type="text/javascript">

	var isClicking = false;

$(document).ready(function() {

	var prefVal = '${prefVal}';
	if(prefVal=='FALSE'){
		$("#roomObjects").hide();
	}
	$(".focusShadow").bind("focus", function(e) {
		addShadowCss($(this).attr("id"));
	});
	$(".focusShadow").bind("blur", function(e) {
		delShadowCss($(this).attr("id"));
	});
	$(".dragRack").mousedown(function(event){
		isClicking = true;
	});
	$(".dragRack").mouseup(function(event){
		if(isClicking) {
			selectRack($(this), event.shiftKey);
		} else {
			$(".objectSelected").each(function(i) {
				delShadowCss($(this).attr("id"));
			});
			showRoomObjectsDiv($(this).attr("id"));
		}
		isClicking = false;
	});

	$("#roomObjects").height($("#rackTableId").height());

});

// Selects @param rack, a clicked rack, and deselects all other racks if @param multi is false
function selectRack (rack, multi) {
	var selected = rack.hasClass('objectSelected');

	if(! multi){
		deselectAll();
		if($("#showAll").val() == '0')
			$("#roomObjects").hide();
	} else {
		$("#roomObjects").show();
	}

	if(selected)
		delShadowCss(rack.attr("id"));
	else
		addShadowCss(rack.attr("id"));

	if($("#showAll").val()=='0' && $(".objectSelected").length > 0)
		showRackTable();
	else
		$("#roomObjects").hide();
}

function showRoomObjectsDiv(rackId){
	var draggable = $("#showAll:checkbox");
	if( draggable.val() != 1){
		var id=rackId.split("_")[1];
		$.ajax({
			url: tdsCommon.createAppURL("/room/roomObject"),
			data:{'id':id},
			type:'POST',
			datatype:'json',
			success: function(data) {
				var x = $("#"+rackId).css("left");
				var y = $("#"+rackId).css("top");
				x = x.substring(0,x.indexOf('px'));
				y = y.substring(0,y.indexOf('px'));
				$(".rackDetailDiv").hide();
				$("#rackDetailDiv_"+id).html(data);
				$("#rackDetailDivv_"+id).html(data);
				$("#rackDetailDiv_"+id).css("left",(parseInt(x)+70)+"px");
				$("#rackDetailDiv_"+id).css("top",(parseInt(y)+50)+"px");
				$("#rackDetailDiv_"+id).show("fold", {horizFirst: true }, 1000);
				$("#rackDetailDivv_"+id).show("fold", {horizFirst: true }, 1000);
				updateXYPositions(rackId)
			}
		});
	}
}

function showRackTable(){
   $(".rackDetailDiv").hide();
   var rackIdArr = [];

   $('.objectSelected').each(function(){
 		var rackId = $(this).attr('id').split("_")[1];
 		rackIdArr.push(rackId)
   });
   if($("#showAll").val() == "1"){
	    $('.rowShow').show();
	    $("#roomObjects").show();
   } else {
	    $('.rowShow').hide();
   }
   for(i=0; i<rackIdArr.length; i++){
	   $("#rackEditRow_"+rackIdArr[i]).show();
   }
   $("#roomObjects").height($("#rackTableId").height());
}

/**
 * Multi div drag function function.
 */
$(".draggable").draggable({
	start: function(event, ui) {
		posTopArray = [];
		posLeftArray = [];
		isClicking = false;

		if(! $(this).hasClass("objectSelected"))
			selectRack($(this), false);

		$(".objectSelected").each(function(i) {
			thiscsstop = $(this).css('top');
			if (thiscsstop == 'auto') thiscsstop = 0;

			thiscssleft = $(this).css('left');
			if (thiscssleft == 'auto') thiscssleft = 0;

			posTopArray[i] = parseInt(thiscsstop);
			posLeftArray[i] = parseInt(thiscssleft);
		});

		begintop = $(this).offset().top;
		beginleft = $(this).offset().left;
	},
	drag: function(event, ui) {
		var topdiff = $(this).offset().top - begintop;
		var leftdiff = $(this).offset().left - beginleft;

		if ($(this).hasClass("objectSelected")) {
			$(".objectSelected").each(function(i) {
				$(this).css('top', posTopArray[i] + topdiff);
				$(this).css('left', posLeftArray[i] + leftdiff);

				var rackId = $(this).attr("id").split("_")[1];

				$("#roomXId_"+rackId).val(posLeftArray[i] + leftdiff);
			 	$("#roomYId_"+rackId).val(posTopArray[i] + topdiff);
			});
		}
	}
});

/**
 *  using this method for NUDGING : move div(s) 1 px to all direction using arrow keys.
 */
$(document).bind('keydown',function(evt) {
	var focusedClass = $("*:focus").parent().parent().attr('class');
	if(focusedClass && focusedClass.indexOf('objectRowSelected') == -1){
	 $(".objectSelected").each(function(i) {
		 var x = $(this).css('top');
		 var y = $(this).css('left');
		 var top = x.substring(0,x.indexOf('px'));
	     var left = y.substring(0,y.indexOf('px'));
	     var rackId = $(this).attr("id").split("_")[1];
		 switch(evt.keyCode) {
		    case 37:
		    	evt.preventDefault();
		    	$(this).css('left', (parseInt(left)-1)+"px");

                $("#roomXId_"+rackId).val((parseInt(left)-1));
            	$("#roomYId_"+rackId).val(top);
		        break;
			case 38:
				evt.preventDefault();
				$(this).css('top', (parseInt(top)-1)+"px");

                $("#roomXId_"+rackId).val(left);
            	$("#roomYId_"+rackId).val((parseInt(top)-1));
			    break;
			case 39:
				evt.preventDefault();
				$(this).css('left', (parseInt(left)+1)+"px");

                $("#roomXId_"+rackId).val((parseInt(left)+1));
            	$("#roomYId_"+rackId).val(top);
			    break;
			case 40:
				evt.preventDefault();
				$(this).css('top', (parseInt(top)+1)+"px");

                $("#roomXId_"+rackId).val(left);
            	$("#roomYId_"+rackId).val((parseInt(top)+1));
			    break;
			    break;
			}
     });
     }
});

function enableDraggableRack(){
	  var showDrag = $("#showRoomObjects").is(':checked');
	  var drag = 'off';
	  if(showDrag){
		$("#roomObjects").draggable({
			start: function() {
				$("#roomObjects").css('margin-left','0px' )
		    }
	  	});
		drag = 'on'
	  } else {
		$("#roomObjects").css({ 'top':'170','left':'280px' });
		$("#roomObjects").draggable('destroy')
	  }
	  jQuery.ajax({
		  url:tdsCommon.createAppURL("/room/setDraggableRackPref"),
		  data:"prefVal="+drag
	  });
}
enableDraggableRack();
initializeRacksInRoom( ${rackInstanceList.id} );
function submitForm(form){

 	if($("#locationId").val() == '') {
 		alert("Please enter location")
 	} else if($("#roomNameId").val() == '') {
 		alert("Please enter room")
 	} else {
		jQuery.ajax({
			url: tdsCommon.createAppURL("$(form).attr('action')"),
			data: $(form).serialize(),
			type:'POST',
			success: function(data) {
				$("#roomShowView").html(data);
				$("#roomShowView").show();
				$("#roomListView").hide();
			 	$("#room_layout").css("height","auto")
			}
		});
 	}
 	return false;
 }
function updateXYPositions(id){
	var rackId = id.split("_")[1];

	var width = $("#"+id).css("width");

	var x = $("#"+id).css("left");
	var y = $("#"+id).css("top");
	x = x.substring(0,x.indexOf('px'));
	y = y.substring(0,y.indexOf('px'));

	var left = $("#room_layout_table").css("width");
	left = left.substring(0,left.indexOf('px'))	- parseInt(width);

	if(parseInt(left) <= parseInt(x)){
		x = left-3
		$("#"+id).css("left",x+"px")
	}

	$("#roomXId_"+rackId).val(x);
	$("#roomYId_"+rackId).val(y);
	$("#roomXDivId_"+rackId).val(x);
	$("#roomYDivId_"+rackId).val(y);

}

function addShadowCss(id, event){
	var rackId = id.split("_")[1];

	$("#rackEditRow_"+rackId).addClass("objectRowSelected");
	$("#rack_"+rackId).addClass("objectSelected");

}
function deselectAll(){
	$(".objectRowSelected").removeClass("objectRowSelected");
	$(".objectSelected").removeClass("objectSelected");
	$(".multiSelect").removeClass("multiSelect");
	$(".rackDetailDiv").hide("fold", {horizFirst: true }, 1000);
	$(".rackDetailDivv").hide("fold", {horizFirst: true }, 1000);
}
function delShadowCss(id){
	var rackId = id.split("_")[1];
	$("#rack_"+rackId).removeClass("objectSelected");
	$("#rackEditRow_"+rackId).removeClass("objectRowSelected");
}
function verifyAndDeleteRacks(id){
	jQuery.ajax({
		url: tdsCommon.createAppURL("verifyRackAssociatedRecords"),
		data: "rackId="+id,
		type:'POST',
		success: function(data) {
			if(data != null && data != ""){
				if(confirm("Some assets used this Rack. Be sure you want to remove it before proceeding")){
					$("#rackEditRow_"+id).remove(); // Remove row from table
					$("#rack_"+id).hide("explode", 1000); // Remove the image from model panel
					$("#rackDetailDiv_"+id).hide("explode", 1000);
					} }else {
				$("#rackEditRow_"+id).remove(); // Remove row from table
				$("#rack_"+id).hide("explode", 1000); // Remove the image from model panel
				$("#rackDetailDiv_"+id).hide("explode", 1000);
				$("#rackDetailDivv_"+id).hide("explode", 1000);
			}
		}
	});
}
function createRack(value){
	var newRackId = $("#rackCount").val();
	$("#rackEditRow_"+newRackId).show();

	$("#rackCount").val( parseInt(newRackId)+1 );
	if(value=="CRAC"){
		$("#rackTypeId_"+newRackId).val('CRAC');
		updateRackStyle(newRackId,'L','CRAC');
		$("#newPowerA_"+newRackId).val('0');
		$("#newPowerB_"+newRackId).val('0')
	}else if(value=="UPS"){
		$("#rackTypeId_"+newRackId).val('UPS');
		updateRackStyle(newRackId,'L','UPS');
		$("#newPowerA_"+newRackId).val('0');
		$("#newPowerB_"+newRackId).val('0');
	}else if(value=="Object"){
		$("#rackTypeId_"+newRackId).val('Object');
		updateRackStyle(newRackId,'L','Object');
		$("#newPowerA_"+newRackId).val('0');
		$("#newPowerB_"+newRackId).val('0');
	}
	$("#rack_"+newRackId).show();
	$("#rack_"+newRackId).effect( "shake", {times:3}, 1000 );
	$("#roomObjects").height($("#rackTableId").height())
}

function changeLabel(id,value){
	$("#rackLabel_"+id).html(value)
}

function changeRackType(id,value){
	$("#rack_"+id).html(value)
}

function objectSelectedOn(id){
	$("#rack_"+id).addClass("objectSelected")
}

function objectSelectedOff(id){
	$("#rack_"+id).removeClass("objectSelected")
}

function updateRackStyle(id, frontValue, rackTypeValue){
	$("#rack_"+id).removeAttr("class");
	if(rackTypeValue == "Rack"){
		$("#rack_"+id).addClass("rack_highlight_no_"+frontValue);
	} else {
		$("#rack_"+id).addClass("room_"+rackTypeValue+"_"+frontValue );
	}
	updateXYPositions("rack_"+id)
}

function changeRackPosition(rackId, value, position){
	if(!isNaN(value)){
		$("#rack_"+rackId).css(position,value+"px");
		$("#rackDetailDiv_"+rackId).css(position,value+"px");
		$("#rackDetailDiv_"+rackId).css(position,value+"px");
	} else {
		alert("Please enter Numerics")
	}
}

function changeRackRoomDetails(rackId,value,type){
$("#"+type+"Id_"+rackId).val(value)
}
function changeRackDetails(rackId,value,type){
$("#"+type+"_"+rackId).val(value)
}
function roundValue(value,id){
	var chndValue = Math.round(value);
	$("#"+id).val( chndValue )
}

</script>
</body>
</html>
