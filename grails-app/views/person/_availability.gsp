
<html>
<head>
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
</head>
<div class="menu4">
	<ul>
		<li><a href="#" class="mobmenu "
			onclick="loadPersonDiv(${person.id},'generalInfo')">General</a></li>
		<li><a href="#" class="mobmenu mobselect"
			onclick="loadPersonDiv(${person.id},'availability')">Availability</a></li>
		<li><a href="#" class="mobmenu"
			onclick="loadPersonDiv(${person.id},'tdsUtility')">TDS</a></li>
	</ul>
</div>


<div>
<g:form name="availabilityForm" action="updatePerson">

<script type="text/javascript" charset="utf-8">
jQuery(function($){$('.dateRange').datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});

function showCalender(id){
	jQuery(function($){$(id).datepicker({showOn: 'both', buttonImage: '${createLinkTo(dir:'images',file:'calendar.gif')}', buttonImageOnly: true,beforeShow: customRange});function customRange(input) {return null;}});
}
</script>
 <input type="hidden" name="id" value="${person.id}">
	<table style="border: 0px">
		<tbody id="blackOutDay">
			<tr>
				<td><span><b>Available , except for the following
							dates</b></span></td>
			</tr>
			  <g:each in="${blackOutdays}" var="blackOutDay" status="i">
				<tr id="dateTrId_${i}">
					<td align="center">
						<input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="availability" id="availabilityId_${i}" 
					  		value="<tds:convertDate date='${blackOutDay.exceptionDay}' timeZone='${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}'/>"  readonly="readonly"/>
					  		<a href="javascript:deleteRolesRow('dateTrId_${i}')">&nbsp;&nbsp;<span class='clear_filter'>X</span></a>
				    </td>
				</tr>
			  </g:each>
		</tbody>
	</table>
	<br /> 
	<span id="" onclick="addBlackOutDay()" style="cursor: pointer;"><b>
			Add BlackOutDay </b></span> 
	<input type="hidden" id="availableId" value="1">

	<div id="dateDivId" style="display: none">
		<input type="text" size="15" style="width: 112px; height: 14px;"
			name="available" id="availId" />
	</div>
</g:form>
 <div class="buttons">
	<input class="save" type="button" id="updateBId" value="Update" onClick="updatePerson('availabilityShow','availabilityForm')" />
	
	<input class="save" type="button" id="cancelBId" value="Cancel" onClick="$('#personGeneralViewId').dialog('close')" />
</div>
</div>

</html>