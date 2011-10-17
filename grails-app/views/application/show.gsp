<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

<g:javascript src="asset.tranman.js" />


<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />

<script type="text/javascript">

$(document).ready(function() {
	$("#editAppsView").dialog({ autoOpen: false })
})
</script>
<title>Show Application</title>
</head>
<body>
	<div class="body">
		<div class="dialog">
			<table>
				<tbody>
					<tr class="prop">
						<td>Name:</td>
						<td>${applicationInstance.assetName}</td>

						<td>Desc:</td>
						<td colspan="4">This Application Support the XYZ Business</td>

					</tr>

					<tr class="prop">
						<td>Type:</td>
						<td>${applicationInstance.assetType}</td>

						<td>Support:</td>
						<td>${applicationInstance.supportType}</td>

						<td>Function:</td>
						<td>${applicationInstance.appFunction}</td>

						<td>ConCurren:</td>
						<td>${applicationInstance.userConcurrent}</td>

					</tr>

					<tr class="prop">
						<td>Vendor:</td>
						<td>${applicationInstance.appVendor}</td>

						<td>SME1:</td>
						<td>${applicationInstance.sme}</td>

						<td>Enviorn:</td>
						<td>${applicationInstance.environment}</td>

						<td>User Loc:</td>
						<td>${applicationInstance.userLocations}</td>

					</tr>

					<tr class="prop">
						<td>Version:</td>
						<td>${applicationInstance.appVersion}</td>

						<td>SME2:</td>
						<td>${applicationInstance.sme2}</td>


						<td>criticality:</td>
						<td>${applicationInstance.criticality}</td>


						<td>ConCurren:</td>
						<td>${applicationInstance.userConcurrent}</td>


					</tr>


					<tr class="prop">
						<td>Tech. :</td>
						<td>${applicationInstance.appTech}</td>


						<td>Bus Unit:</td>
						<td>${applicationInstance.businessUnit}</td>

						<td>Bundle:</td>
						<td>${applicationInstance.moveBundle}</td>


						<td>Use Freq:</td>
						<td>${applicationInstance.useFrequency}</td>


					</tr>

					<tr class="prop">
						<td>Source:</td>
						<td>${applicationInstance.appSource}</td>


						<td>Owner:</td>
						<td>${applicationInstance.owner}</td>


						<td>Plan Status:</td>
						<td>${applicationInstance.planStatus}</td>

						<td>DR RPO:</td>
						<td>${applicationInstance.drRpoDesc}</td>

					</tr>

					<tr class="prop">
						<td>License:</td>
						<td>${applicationInstance.license}</td>


						<td>Retire:</td>
						<td>Retire</td>
						<td>&nbsp;</td>
						<td>&nbsp;</td>

						<td>DR RTO:</td>
						<td>${applicationInstance.drRtoDesc}</td>

					</tr>

					<tr>
						<td>&nbsp;</td>
						<td>&nbsp;</td>
						<td>Main Exp.:</td>
						<td>12/1/2011</td>

						<td>&nbsp;</td>
						<td>&nbsp;</td>

						<td>DR RTO:</td>
						<td>${applicationInstance.drRtoDesc}</td>


					</tr>

				</tbody>
			</table>
		</div>
		<div style="float: inherit;">
			<div style="float: left; margin-bottom: 10px;">
				<h1>Supports:</h1>
				<table style="width: 400px;">
					<thead>
						<tr>
						<th>Frequency</th>
						<th>Asset</th>
						<th>Type</th>
						<th>Status</th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${supportAssets}" var="support">
						<tr>
							<td>${support?.comment}</td>
							<td>${support?.asset}</td>
							<td><g:select id="selectId" value=${support.type}
									from="${AssetDependency.constraints.type.inList}" />
							</td>
							<td><g:select id="typeId" value=${support.status}
									from="${AssetDependency.constraints.status.inList}" />
							</td>
						</tr>
					</g:each>
					</tbody>
				</table>
			</div>
			<div style="float: left; margin-left: 10px;">
				<h1>Is dependent on:</h1>
				<table style="width: 400px;">
					<thead>
						<tr>
						<th>Frequency</th>
						<th>Asset</th>
						<th>Type</th>
						<th>Status</th>
						</tr>
					</thead>
					<tbody>
					<g:each in="${dependentAssets}" var="dependent">
						<tr>
							<td>${dependent.dataFlowFreq}</td>
							<td>${dependent.dependent}</td>
							<td>${support.type}</td>
							<td>${support.status}</td>
						</tr>
					</g:each>
					</tbody>
				</table>
			</div>
		</div>
		<div class="buttons">
			<g:form>
				<input type="hidden" name="id" value="${applicationInstance?.id}" />
				<span class="button"><input type="button" class="edit" value="Edit" onclick="editApp(${applicationInstance?.id})"/>  </span>
				<span class="button"><g:actionSubmit class="delete"
						onclick="return confirm('Are you sure?');" value="Delete" /> </span>
			</g:form>
		</div>
	</div>
	<div id="editAppsView" style="display: none;" title="Edit Application">
	</div>
</body>
</html>
<script type="text/javascript">
$('#assetMenu').show();
$('#reportsMenu').hide();
function editApp(value){
	var resp = value
	${remoteFunction(action:'edit', params:'\'id=\' + resp ', onComplete:"editAppView(e)")}
	
}
d
function editAppView(e){
     var resps = e.responseText;
     $("#editAppsView").html(resps);
	 $("#editAppsView").dialog('option', 'width', 'auto')
	 $("#editAppsView").dialog('option', 'position', ['center','top']);
	 $("#editAppsView").dialog('open');
	 $("#appShowView").dialog('close');
}

</script>
