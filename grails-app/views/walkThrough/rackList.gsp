<html>
<head>
<title>Walkthru&gt; Select Rack</title>
<g:javascript library="prototype" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'walkThrough.css')}" />
<g:javascript src="betterinnerhtml.js" />
<script type="text/javascript">
/*-----------------------------------------------------------------
	function to load the Bundles for selected Project via AJAX
	@author : Lokanath Reddy
	@params : Racks list object as JSON
*------------------------------------------------------------------*/
function updateRacks( e , type ) {
	var racksDetails = e.responseText;
	BetterInnerHTML(getObject('racksListBodyId'), racksDetails);
}
var timeInterval;
function searchRacks(){
	//${remoteFunction(action:'getRacksByLocation', params:'\'location=\' + document.selectRackForm.location.value +\'&viewType=\'+document.selectRackForm.viewType.value +\'&searchKey=\'+document.selectRackForm.search.value', onComplete:'updateRacks(e, \'search\')')}
	
	document.selectRackForm.submit();
}
function showAssets(bundle, location, room, rack){
	window.location.href='selectAsset?moveBundle='+bundle+'&location='+location+'&room='+room+'&rack='+rack
}
</script>
</head>
<body onload="document.selectRackForm.search.focus();">
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
      	onchange="document.selectRackForm.submit()">
	      <g:each in="${locationsList}" var="locationsList">
	         <option value="${locationsList.location}">${locationsList.location}</option>
	      </g:each>
      </select>
   </td>
</tr>

<tr>
	<td align="center" style="margin-top:8px; margin-bottom:8px;">
		<input type="hidden" name="viewType" id="viewTypeId" value="${viewType}">
		<input type="hidden" name="moveBundle" value="${moveBundle}">
		<input type="hidden" name="auditType" value="${auditType}">
		<label>View:</label>
		<a name="todoId" class="button unselected"  href="#" onclick="document.selectRackForm.viewType.value='todo';document.selectRackForm.submit();" id="todoId">ToDo</a>
		<a class="button" name="allId" href="#" onclick="document.selectRackForm.viewType.value = 'all';document.selectRackForm.submit();"  id="allId"  >All</a>
	</td>
	<td align="right">
		<label for="search">Search:</label>
		<input type="text" class="text search" size=8 name="search" id="searchId" value="${search}"
		onkeyup="timeInterval = setTimeout('searchRacks()',500)" onkeydown="if(timeInterval){clearTimeout(timeInterval)}"> 
	</td>
</tr>
<tr><td colspan="2">
    <table class="grid" id="racksListBody">
    	<thead>
        <tr>
        	<g:sortableColumn property="room" title="Room" params="['moveBundle':moveBundle,'auditType':auditType,'viewType':viewType]"/>
        	<g:sortableColumn property="rack" title="Rack" params="['moveBundle':moveBundle,'auditType':auditType,'viewType':viewType]"/>
        	<g:sortableColumn property="total" title="Remaining" params="['moveBundle':moveBundle,'auditType':auditType,'viewType':viewType]"/>
        </tr>
        </thead>
        <tbody id="racksListBodyId">
        	${rackListView}
		</tbody>
    </table>
</td></tr>
</g:form>
</table>
</div>
</div>
<script type="text/javascript">
document.selectRackForm.location.value = '${auditLocation}'
if('${viewType}'== 'todo'){
	getObject('todoId').className = 'button'
	getObject('allId').className = 'button unselected'
		
}
</script>
</body>
</html>
		