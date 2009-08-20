<html>
<head>
<title>Walkthru&gt; Select Rack</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'walkThrough.css')}" />
<script type="text/javascript">
/*-----------------------------------------------------------------
	function to load the Bundles for selected Project via AJAX
	@author : Lokanath Reddy
	@params : Racks list object as JSON
*------------------------------------------------------------------*/
function updateRacks( e ) {
	var racksDetails = e.responseText;
	var racksListBody = $("#racksListBody");
	racksListBody.html( racksDetails );
}
var timeInterval;
function searchRacks(){
	${remoteFunction(action:'getRacksByLocation', params:'\'location=\' + $(\'#locationId\').val() +\'&viewType=\'+$(\'#viewTypeId\').val() +\'&searchKey=\'+$(\'#searchId\').val()', onComplete:'updateRacks(e)')}
}
</script>
</head>
<body onload="$('#searchId').focus()">
<div class="qvga_border">
<a name="select_rack"></a> 
<div class="title">Walkthru&gt; Select Rack</div>
<div class="input_area">
<a class="button" href="startMenu">Start Over</a>
<table>
<g:form method="post" action="selectRack" name="selectRackForm"> 
<tr>
   <td class="label">Location:</td>
   <td class="field">
      <select name="location" id="locationId" class="select"
      	onchange="${remoteFunction(action:'getRacksByLocation', params:'\'location=\' + this.value +\'&viewType=\'+$(\'#viewTypeId\').val() ', onComplete:'updateRacks(e)')}">
	      <g:each in="${locationsList}" var="locationsList">
	         <option value="${locationsList.location}">${locationsList.location}</option>
	      </g:each>
      </select>
   </td>
</tr>

<tr>
	<td align="center" style="margin-top:8px; margin-bottom:8px;">
		<label>View:</label> 
		<input type="hidden" name="viewType" id="viewTypeId" value="${viewType}">
		<input type="hidden" name="moveBundle" value="${moveBundle}">
		<input type="hidden" name="auditType" value="${auditType}">
		<a class="button unselected" href="#" onclick="$('#viewTypeId').val('todo');$('form#selectRackForm').submit();" id="todoId">ToDo</a>
		<a class="button" href="#" onclick="$('#viewTypeId').val('all');$('form#selectRackForm').submit();" id="allId">All</a>
	</td>
	<td align="right">
		<label for="search">Search:</label><input type="text" class="text search" size=8 name="search" id="searchId"  
		onkeyup="timeInterval = setTimeout('searchRacks()',500)" onkeydown="if(timeInterval){clearTimeout(timeInterval)}"> 
	</td>
</tr>
</g:form>
<tr><td colspan="2">
    <table class="grid" >
    	<thead>
        <tr>
        	<g:sortableColumn property="room" title="Room" params="['moveBundle':moveBundle,'auditType':auditType,'viewType':viewType]"/>
        	<g:sortableColumn property="rack" title="Rack" params="['moveBundle':moveBundle,'auditType':auditType,'viewType':viewType]"/>
        	<g:sortableColumn property="total" title="Remaining" params="['moveBundle':moveBundle,'auditType':auditType,'viewType':viewType]"/>
        </tr>
        </thead>
        <tbody id="racksListBody">
        	${rackListView}
		</tbody>
    </table>
</td></tr>
</table>
</div>
</div>
<script type="text/javascript">
$("#locationId").val('${auditLocation}')
if('${viewType}'== 'todo'){
	$("#allId").attr('class','button unselected')
	$("#todoId").attr('class','button')
}
</script>
</body>
</html>
		