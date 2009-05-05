<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Report List</title>
<g:javascript library="prototype"/>
<g:javascript library="jquery"/>
<g:javascript>
function initialize(){
document.getElementById('appSmeId').value="${appSmeValue}"
document.getElementById('appOwnerId').value="${appOwnerValue}"
document.getElementById('applicationId').value="${appValue}"
}
function callRelatedRec(){
var projectId=document.getElementById('projectId').value
var appValue=document.getElementById('applicationId').value
var appOwnerValue=document.getElementById('appOwnerId').value
var appSmeValue=document.getElementById('appSmeId').value
document.listForm.method="post"
document.listForm.action="searchFilters?applicationVal="+appValue+"&appOwnerVal="+appOwnerValue+"&appSmeVal="+appSmeValue+"&projectId="+projectId;
document.listForm.submit();
}
</g:javascript>
</head>
<body>
<g:form name="listForm" />
<div class="body"><br>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div style="width:900px;overflow-x:scroll">
<input type="hidden" id="projectId" name="projectId"
		value="${projectId }" />
	<table cellpadding="1" cellspacing="1">
<select id="applicationId" onchange="callRelatedRec();">
<option value="All">All</option>	
<g:each in="${applicationList}" var="application">
<option value="${application}" >${application}</option>
</g:each> 	
</select>	
	
<select id="appOwnerId" onchange="callRelatedRec();">
<option value="All">All</option>	
<g:each in="${appOwnerList}" var="appOwner">
<option value="${appOwner}">${appOwner}</option>
</g:each> 	
</select>	
	
<select id="appSmeId" onchange="callRelatedRec();">	
<option value="All">All</option>
<g:each in="${appSmeList}" var="appSme">
<option value="${appSme}">${appSme}</option>
</g:each> 	
</select>	


		<thead>
			<tr>
				<g:sortableColumn property="application" title="Application" />

				<g:sortableColumn property="appOwner" title="App Owner" />

				<g:sortableColumn property="appSme" title="App Sme" />

				<g:sortableColumn property="assetName" title="Asset Name" />
				
				<th>AppStartup</th>
				<th>AppFinished</th>
				<th>AppVerifying</th>
				<th>AppVerified</th>
                <th>Cleaned</th>
                <th>Completed</th>
                <th>DBFinished</th>
                <th>DBStarted</th>
				
				<th>EndTransit</th>
				<th>Hold</th>
				<th>InTransit</th>
				<th>NetVerifying</th>
				<th>NetVerified</th>
				<th>OffTruck</th>
				<th>OnCart</th>
				<th>OnTruck</th>
				<th>PoweredDown</th>
				<th>PoweredOn</th>
				
				<th>QAVerified</th>
				<th>Ready</th>
				<th>Release</th>
				<th>Reracked</th>
				<th>Reracking</th>
				<th>SANVerifying</th>
				<th>SANVerified</th>
				<th>Staged</th>
				<th>Terminated</th>
				<th>Unracking</th>
				<th>Unracked</th>

			</tr>
		</thead>
		<tbody>
		
		
		
		
			<g:each in="${assetEntityList}" status="i" var="assetEntity">
				<tr>

					<td>${assetEntity.application}</td>
					<td>${assetEntity.appOwner}</td>
					<td>${assetEntity.appSme}</td>
					<td>${assetEntity.assetName}</td> 
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'AppStartup')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'AppFinished')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'AppVerifying')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'AppVerified')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'Cleaned')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'Completed')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'DBFinished')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'DBStarted')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'EndTransit')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'Hold')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'InTransit')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'NetVerifying')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'NetVerified')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'OffTruck')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'OnCart')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'OnTruck')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'PoweredDown')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'PoweredOn')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'QAVerified')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'Ready')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'Release')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'Reracked')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'Reracking')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'SANVerifying')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'SANVerified')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'Staged')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'Terminated')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'Unracking')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					
					<g:if test="${AssetTransition.findByAssetEntityAndStateTo(assetEntity,'Unracked')==null}">
					<td bgcolor="white"></td>
					</g:if>
					<g:else>
					<td bgcolor="green"></td>
					</g:else>
					</tr>
			</g:each>
		</tbody>
	</table>
</div>
<g:javascript>
initialize();
</g:javascript>
</body>

</html>
