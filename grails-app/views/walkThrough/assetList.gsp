<html>
<head>
<title>Walkthru&gt; Select Asset</title>
<g:javascript library="prototype" />
<g:javascript library="jquery" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'qvga.css')}" />
<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'walkThrough.css')}" />
<script type="text/javascript">
var timeInterval;
function searchAssets(){
	var aserchKey = $('#assetSearchId').val()
	${remoteFunction(action:'searchAssets', params:'\'room=\' + $(\'#roomId\').val() +\'&rack=\'+$(\'#rackId\').val() +\'&searchKey=\'+ aserchKey +\'&viewType=\'+$(\'#viewTypeId\').val()', onComplete:'updateAssets(e)')}
}
function updateAssets( e ) {
	var assetsList = e.responseText;
	var result = assetsList.indexOf("No records found")
	if(result != -1 ){
		$('#assetSearchId').css("border","1px solid red")
	} else {
		$('#assetSearchId').css("border","1px solid #ccc")
	}
	var assetsListBody = $("#assetsListBody");
	assetsListBody.html( assetsList );
}
function showAssetMenu( id ) {
	${remoteFunction(action:'confirmAssetBundle', params:'\'id=\' + id ', onComplete:'confirmAssetBundle(e,id)')}
}
function confirmAssetBundle( e , assetId ){
	var confirmMessage = e.responseText
	var flag = true
	if(confirmMessage){
		if(!confirm(confirmMessage)){
			flag = false
		}
	}
	if(flag){
		$("#assetId").val(assetId);
		$("form#selectAssetForm").attr("action","assetMenu")
		$("form#selectAssetForm").submit();
	}
}
</script>
</head>
<body onload="$('#assetSearchId').focus()">
<DIV class=qvga_border><A name=select_asset></A>
<DIV class=title>Walkthru&gt; Select Asset</DIV>
<DIV class=input_area>
<DIV style="FLOAT: left"><A class=button
	href="startMenu">Start Over</A></DIV>
<DIV style="FLOAT: right"><A class=button
	href="selectRack?moveBundle=${params.moveBundle}&auditType=${auditType}">Rack List</A></DIV>
<BR class=clear>
<TABLE>
	<TBODY>
	<g:form method="post" action="selectAsset" name="selectAssetForm"> 
		<TR>
			<TD class=label>Room/Rack:</TD>
			<TD class=field>${params.room}/${params.rack}</TD>
		</TR>
		<TR>
			<input type="hidden" name="id" id="assetId">
			<input type="hidden" name="viewType" id="viewTypeId" value="${params.viewType}">
			<input type="hidden" name="moveBundle" value="${params.moveBundle}">
			<input type="hidden" name="location" value="${params.location}">
			<input type="hidden" name="room" id="roomId" value="${params.room}">
			<input type="hidden" name="rack" id="rackId" value="${params.rack}">
			<TD align=middle><LABEL>View:</LABEL> 
			<a class="button unselected" href="#" onclick="$('#viewTypeId').val('todo');$('form#selectAssetForm').submit();" id="todoId">ToDo</a>
			<a class="button" href="#" onclick="$('#viewTypeId').val('all');$('form#selectAssetForm').submit();" id="allId">All</a>
			</TD>
			<TD align=right><LABEL for=assetSearch>Scan Asset:</LABEL>
				<INPUT style="width: 40px" id="assetSearchId" class="text search" size='8' name='assetSearch' 
					onkeyup="timeInterval = setTimeout('searchAssets()',500)" onkeydown="if(timeInterval){clearTimeout(timeInterval)}"> 
			</TD>
		</TR>
		</g:form>
		<TR>
			<TD colSpan=2>
			<TABLE class=grid>
				<thead>
					<TR>
						<g:sortableColumn property="uposition" title="U Pos" 
						params="['moveBundle':params.moveBundle,'viewType':params.viewType,'location':params.location,'room':params.room,'rack':params.rack]"/>
						<g:sortableColumn property="usize" title="Size" 
						params="['moveBundle':params.moveBundle,'viewType':params.viewType,'location':params.location,'room':params.room,'rack':params.rack]"/>
						<g:sortableColumn property="assetTag" title="Asset Tag" 
						params="['moveBundle':params.moveBundle,'viewType':params.viewType,'location':params.location,'room':params.room,'rack':params.rack]"/>
					</TR>
				</thead>
				<tbody id="assetsListBody">
				${assetsListView}
				</tbody>
			</TABLE>
			</TD>
		</TR>
	</TBODY>
</TABLE>
</DIV>
</DIV>
<script type="text/javascript">
if('${params.viewType}'== 'todo'){
	$("#allId").attr('class','button unselected')
	$("#todoId").attr('class','button')
}
</script>
</body>
</html>
