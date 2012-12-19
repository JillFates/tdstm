<html>
<head>
<title>Walkthru&gt; Select Asset</title>
<g:javascript library="prototype" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'qvga.css')}" />
<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'walkThrough.css')}" />
<g:javascript src="betterinnerhtml.js" />
<script type="text/javascript">
var timeInterval;
function searchAssets(){
	//var aserchKey = document.selectAssetForm.search.value
	//${remoteFunction(action:'searchAssets', params:'\'room=\' + document.selectAssetForm.room.value +\'&rack=\'+document.selectAssetForm.rack.value +\'&searchKey=\'+ aserchKey +\'&viewType=\'+document.selectAssetForm.viewType.value', onComplete:'updateAssets(e)')}
	document.selectAssetForm.submit();
}
<%--function updateAssets( e ) {
	var assetsList = e.responseText;
	var result = assetsList.indexOf("No records found")
	if(result != -1 ){
		document.selectAssetForm.search.style.border = "1px solid red"
	} else {
		document.selectAssetForm.search.style.border = "1px solid #ccc"
	}
	BetterInnerHTML(getObject('assetsListBody'), assetsList);
} --%>
function showAssetMenu( assetId , assetName, bundleId, bundleName ) {
	//${remoteFunction(action:'confirmAssetBundle', params:'\'id=\' + id ', onComplete:'confirmAssetBundle(e,id)')}
	var flag = true
	var auditBundle = '${params.moveBundle}'
	if(bundleId != auditBundle){
		if(!confirm("The asset "+assetName+" is not part of the bundle "+bundleName+". Do you want to proceed?")){
			flag = false
		}
	}
	if(flag){
		document.selectAssetForm.assetId.value = assetId
		document.selectAssetForm.action = "assetMenu"
		document.selectAssetForm.submit();
	}
}
<%--function confirmAssetBundle( e , assetId ){
	var confirmMessage = e.responseText
	var flag = true
	if(confirmMessage){
		if(!confirm(confirmMessage)){
			flag = false
		}
	}
	if(flag){
		document.selectAssetForm.assetId.value = assetId
		document.selectAssetForm.action = "assetMenu"
		document.selectAssetForm.submit();
	}
} --%>
</script>
</head>
<body onload="document.selectAssetForm.search.focus()">
<DIV class=qvga_border><A name=select_asset></A>
<DIV class=title>Walkthru&gt; Select Asset</DIV>
<DIV class=input_area>
<DIV style="FLOAT: left"><A class=button
	href="startMenu">Start Over</A></DIV>
<DIV style="FLOAT: right"><A class=button
	href="selectRack?moveBundle=${params.moveBundle}&auditType=${auditType}">Rack List</A></DIV>
<BR class=clear>
<g:form method="post" action="selectAsset" name="selectAssetForm">
<TABLE>
	<TBODY>
		<g:if test="${!searchType && !searchKey }">
		<TR>
			<TD class=label>Room/Rack:</TD>
			<TD class=field>${params.room != 'null' ? params.room : 'blank'}/${params.rack != 'null' ? params.rack : 'blank'}</TD>
		</TR>
		</g:if>
		<TR>
			<TD align="center"><LABEL>View:</LABEL> 
			<input type="hidden" name="id" name="assetId" id="assetId"/>
			<input type="hidden" name="viewType" id="viewTypeId" value="${viewType}"/>
			<input type="hidden" name="moveBundle" value="${params.moveBundle}"/>
			<input type="hidden" name="location" value="${params.location}"/>
			<input type="hidden" name="room" id="roomId" value="${params.room}"/>
			<input type="hidden" name="rack" id="rackId" value="${params.rack}"/>
			<a class="button unselected" href="#" onclick="document.selectAssetForm.viewType.value='todo';document.selectAssetForm.submit();" id="todoId">ToDo</a>
			<a class="button" href="#" onclick="document.selectAssetForm.viewTypeId.value='all';document.selectAssetForm.submit();" id="allId">All</a>
			</TD>
			<TD align=right><LABEL for=search>Scan Asset:</LABEL>
				<INPUT style="width: 40px" id="assetSearchId" class="text search" size='8' name='search' value="${searchKey}"
					onkeyup="timeInterval = setTimeout('searchAssets()',500)" onkeydown="if(timeInterval){clearTimeout(timeInterval)}"/> 
			</TD>
		</TR>
		
		<TR>
			<TD colSpan=2>
			<TABLE class=grid>
				<thead>
					<TR>
						<g:sortableColumn property="${auditType}RackPosition" title="U Pos" 
						params="['moveBundle':params.moveBundle,'viewType':viewType,'location':params.location,'room':params.room,'rack':params.rack]"/>
						<g:sortableColumn property="assetTag" title="Asset Tag" 
						params="['moveBundle':params.moveBundle,'viewType':viewType,'location':params.location,'room':params.room,'rack':params.rack]"/>
						<g:sortableColumn property="assetName" title="Asset Name" 
						params="['moveBundle':params.moveBundle,'viewType':viewType,'location':params.location,'room':params.room,'rack':params.rack]"/>
					</TR>
				</thead>
				<tbody id="assetsListBody" >
				${assetsListView}
				</tbody>
			</TABLE>
			</TD>
		</TR>
	</TBODY>
</TABLE></g:form>
</DIV>
</DIV>
<script type="text/javascript">
if('${viewType}'== 'todo'){
	getObject('todoId').className = 'button'
	getObject('allId').className = 'button unselected'
}
</script>
</body>
</html>
