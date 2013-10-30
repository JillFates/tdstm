
<table>
<g:set var="rack_id" value="${rack.id?:rackId }"></g:set>
	<tr>
		<td>Name : <span id="sourceId"></span></td>
		<td><input type="text" id="tagDiv_${rack_id}" size="14" value="${rack.tag}"  onchange="changeLabel(${rack_id},this.value);changeRackDetails(${rack_id},this.value,'tag'); "/></td>
	</tr>
	<tr>
		<td>X/Y/Facing:</td>
		<td><input id="roomXDivId_${rack_id}" size="2" value="${rack.roomX}"  onkeyup="changeRackPosition(${rack_id},this.value, 'left')" onchange="changeRackRoomDetails(${rack_id},this.value,'roomX')"/> 
			<input id="roomYDivId_${rack_id}" size="2" value="${rack.roomY}"  onkeyup="changeRackPosition(${rack_id},this.value, 'top')" onchange="changeRackRoomDetails(${rack_id},this.value,'roomY')"/> 
			<g:select id="frontIdDiv_${rack}" from="${Rack.constraints.front.inList}" onchange="updateRackStyle(${rack_id}, this.value, jQuery('#rackTypeId_'+${rack_id}).val());changeRackRoomDetails(${rack_id},this.value,'front');" style="width:40px;"></g:select></td>
	</tr>
	<tr>
		<td>Power A/B/C:(${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE!="Watts"?"Amps":"W"})<span id="unitsId"></span></td>
		<td><input id="powerAId" size="2"  value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE != 'Watts' ? rack.powerA ? (rack.powerA / 120).toFloat().round(1) : 0.0 : rack.powerA ? Math.round(rack.powerA):0}" onchange="changeRackDetails(${rack_id},this.value,'powerA')"/>
			<input id="powerBId" size="2"  value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE != 'Watts' ? rack.powerB ? (rack.powerB / 120).toFloat().round(1) : 0.0 : rack.powerB ? Math.round(rack.powerB):0}" onchange="changeRackDetails(${rack_id},this.value,'powerB')"/> 
			<input id="powerCId" size="2"  value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE != 'Watts' ? rack.powerC ? (rack.powerC / 120).toFloat().round(1) : 0.0 : rack.powerC ? Math.round(rack.powerC):0}" onchange="changeRackDetails(${rack_id},this.value,'powerC')"/>
		</td>
	</tr>
	<tr>
		<td>Type :</td>
		<td><g:select id="rackTypeDivId_${rack_id}" from="${Rack.constraints.rackType.inList}" value="${rack.rackType}" 
			onchange=" changeRackRoomDetails(${rack_id},this.value,'rackType'); updateRackStyle(${rack_id}, jQuery('#frontId_'+${rack_id}).val(), this.value); " style="width:100px;">
		</g:select></td>
	</tr>
	<tr>
		<td>Model:</td>
		<td><span id="modelSpanDiv_${rack_id}"><g:select class="rackModel" id="modelDiv_${rack_id}"  from="${modelList}" noSelection="[null:'Select Model']" value="${rack.model?.id}" optionKey="id" optionValue="${{it.manufacturer.name+' / '+it.modelName} }" onchange="changeRackDetails(${rack_id},this.value,'model')"></g:select></span></td>
	</tr>
	<tr>
		<td>Assets:</td>
		<g:set var="rackAssertVal" value="${rack.assets ?rack.assets.size():0}"/>
		<td><span id="assetsId">${rackAssertVal}</span>
		<g:if test="${rackAssertVal == 0}">
		<a class="deleteId" href="javascript:verifyAndDeleteRacks(${rack_id})"><span style="color: red; margin-left: 31px;">Delete</span></a>
		</g:if>
		</td>
	</tr>
</table>
