
<table>
<g:set var="rack_id" value="${rack.id?:rackId }"></g:set>
	<tr>
		<td>Name : <span id="sourceId"></span></td>
		<td><input type="text" id="tag_${rack_id}" size="14" value="${rack.tag}"  onchange="changeLabel(${rack_id},this.value)"/></td>
	</tr>
	<tr>
		<td>X/Y/Facing:</td>
		<td><input id="roomXId_${rack_id}" class="govav" size="2" value="${rack.roomX}" name="roomXId_${rack_id}" onkeyup="changeRackPosition(${rack_id},this.value, 'left')"/> 
			<input id="roomYId_${rack_id}" class="vinod" size="1" value="${rack.roomY}" name="roomYId_${rack_id}" onkeyup="changeRackPosition(${rack_id},this.value, 'top')"/> 
			<g:select id="frontId_${rack}" name="front_${rack}" size="1" from="${Rack.constraints.front.inList}" onchange="updateRackStyle(${rack}, this.value, jQuery('#rackTypeId_'+${rack}).val())" style="width:40px;"></g:select></td>
	</tr>
	<tr>
		<td>Power A/B/C:(${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE!="Watts"?"Amps":"W"})<span id="unitsId"></span></td>
		<td><input id="powerAId" size="2" name="powerA_${rack_id}" value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE != 'Watts' ? rack.powerA ? (rack.powerA / 110).toFloat().round(1) : 0.0 : rack.powerA ? Math.round(rack.powerA):0}"/>
			<input id="powerBId" size="2" name="powerA_${rack_id}" value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE != 'Watts' ? rack.powerB ? (rack.powerB / 110).toFloat().round(1) : 0.0 : rack.powerB ? Math.round(rack.powerB):0}"/> 
			<input id="powerCId" size="1" name="powerA_${rack_id}" value="${session.getAttribute('CURR_POWER_TYPE')?.CURR_POWER_TYPE != 'Watts' ? rack.powerC ? (rack.powerC / 110).toFloat().round(1) : 0.0 : rack.powerC ? Math.round(rack.powerC):0}"/>
		</td>
	</tr>
	<tr>
		<td>Type :</td>
		<td><input id="typeId" size="14" name="rackType_${rack_id}" value="${rack.rackType}"></input></td>
	</tr>
	<tr>
		<td>Model:</td>
		<td><g:select name="rackType_${rack_id}"
				from="${Rack.constraints.rackType.inList}" value="${rack.rackType}"
				onchange="updateRackStyle(${rack_id}, jQuery('#frontId_'+${rack_id}).val(), this.value)"
				style="width:100px;"></g:select></td>
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
