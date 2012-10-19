
<html>
<head>
<link type="text/css" rel="stylesheet"
	href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
</head>
<div class="menu4">
	<ul>
		<li><a href="#" class="mobmenu "
			onclick="loadPersonDiv(${person.id},'generalInfoShow')">General</a></li>
		<li><a href="#" class="mobmenu mobselect"
			onclick="loadPersonDiv(${person.id},'availabilityShow')">Availability</a></li>
		<li><a href="#" class="mobmenu"
			onclick="loadPersonDiv(${person.id},'tdsUtilityShow')">TDS</a></li>
	</ul>
</div>

<div>
	<table style="border: 0px">
		<tbody id="blackOutDay">
		<tr><td><span><b>Available , except for the following dates</b></span></td></tr>
		  <g:each in="${blackOutdays}" var="blackOutDay">
			<tr >
				<td >
					<input type="text" class="dateRange" size="15" style="width: 112px; height: 14px;" name="retireDate" id="retireDate" 
					  value="<tds:convertDate date='${blackOutDay.exceptionDay}' timeZone='${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}'/>"  readonly="readonly"/>
				</td>
			</tr>
		  </g:each>
		</tbody>
	</table>
</div>
 <div class="buttons" >
	<input class="save" type="button" id="updateBId" value="Edit" onClick="loadPersonDiv(${person.id},'availability')" />
	
	<input class="save" type="button" id="cancelBId" value="Cancel" onClick="$('#personGeneralViewId').dialog('close')" />
</div>
</html>