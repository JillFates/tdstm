<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<g:javascript src="admin.js" />
<title>TDS TransitionManager&trade; Admin Portal</title>
<style type="text/css">
a:hover {
	text-decoration: underline;
}
</style>
</head>
<body>
<div class="body">
<div>&nbsp;</div>
<div><g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div id="showCleanTypeMsgId" style="display: none" class="message"></div>
<table style="border: 0" class="admin-home">
	<tr>
		<td style="vertical-align:top">
			<div>
			<h1 style="margin-right: 0px;"><b>Recent Users</b></h1>
			<table class="admin-home">
				<thead>
					<tr>
						<th>Person</th>
						<th>User Name</th>
						<th>Last logged</th>
						<th>Recent page load</th>
					</tr>
				</thead>
				<tbody>
					<g:each in="${recentUsers}" status="i"  var="user">
					<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
						<td><span class="clickableText" onClick="Person.showPersonDialog(${user.person?.id},'generalInfoShow')">${user.person}</span></td>
						<td><g:link controller="userLogin" action="show" id="${user.id}">${user.username}</g:link></td>
						<td><tds:convertDateTime date="${user.lastLogin}" timeZone="${tds.timeZone()}"/></td>
						<td><tds:convertDateTime date="${user.lastPage}" timeZone="${tds.timeZone()}"/></td>
					</tr>
					</g:each>
				</tbody>
			</table>
			</div>
		</td>
		<td style="vertical-align:top">
			<div>
			<h1 style="margin-right: 0px;"><b>Current and Recent Events</b></h1>
			<table class="admin-home">
				<thead>
					<thead>
						<tr>
							<th>Event Name </th>
							<th>Status</th>
							<th>Start Time</th>
							<th>Completion Time</th>
						</tr>
					</thead>
				</thead>
				<tbody>
					<g:if test="${moveEventsList}">
						<g:each in="${moveEventsList}" status="i"  var="eventList">
						<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
							<td><g:link controller="project" action="show" id="${eventList?.moveEvent?.project?.id}">${eventList?.moveEvent?.project?.name} - ${eventList?.moveEvent?.name}</g:link></td>
							<td>${eventList?.status }</td>
							<td><tds:convertDateTime date="${eventList?.startTime}" timeZone="${tds.timeZone()}"/></td>
							<td><tds:convertDateTime date="${eventList?.completionTime}" timeZone="${tds.timeZone()}"/></td>
						</tr>
						</g:each>
					</g:if>
					<g:else>
						<tr><td class="no_records" colspan="4">No records found</td></tr>
					</g:else>
				</tbody>
			</table>
			</div>
			<br/>
			<div>
				<h1 style="margin-right: 0px;"><b>Upcoming Events</b></h1>
				<table class="admin-home">
					<thead>
						<tr>
							<th>Name </th>
							<th>Start Time</th>
							<th>Completion Time</th>
						</tr>
					</thead>
					<tbody>
						<g:if test="${upcomingBundles}">
							<g:each in="${upcomingBundles}" status="i"  var="bundle">
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
								<td><g:link controller="project" action="show" id="${bundle.project?.id}">${bundle.project?.name} - ${bundle.name}</g:link></td>
								<td><tds:convertDateTime date="${bundle.startTime}" timeZone="${tds.timeZone()}"/></td>
								<td><tds:convertDateTime date="${bundle.completionTime}" timeZone="${tds.timeZone()}"/></td>
							</tr>
							</g:each>
						</g:if>
						<g:else>
							<tr><td colspan="3" class="no_records">No records found</td></tr>
						</g:else>
					</tbody>
				</table>
			</div>
			<br />
			<div>
				<table class="admin-home">
					<thead>
						<tr>
							<th colspan="2">Misc. Admin functions</th>
						</tr>
					</thead>
					<tbody>
						<tr class="odd">
							<td><g:link controller="partyGroup" style="color:black">Company</g:link></td>
							<td><g:link controller="roleType" style="color:black">Teams (Role Type) </g:link></td>
						</tr>
						<tr class="even">
							<td><a style="color:black" href="#" onclick="openReconcileAssetsForm()">Reconcile AssetTypes</a></td>
							<td><a style="color:black" href="#" onclick="openEncryptStringForm()">Encrypt String</a></td>
						</tr>
						<tr class="odd">
							<td><g:link controller="admin" action="orphanSummary"
								style="color:black">Manage Orphan Records</g:link></td>
							<td><a style="color:black" href="#" onclick="openFlushDiv()"> Flush import data </a>
							</td>
						</tr>
						<tr class="even">
							<td><a style="color:black" href="#" onclick="openShowTypeDiv()"> Show/Clean Types </a></td>
							<td><g:link controller="admin" action="projectReport" style="color:black">Projects Summary Report</g:link></td>
						</tr>
						<tr class="odd">
							<td>
								<g:link controller="admin" action="systemInfo" style="color:black">System Info</g:link>
							</td>
							<td>
								<g:link controller="admin" action="bootstrap" target="_blank" style="color:black" >Bootstrap Menus</g:link>
							</td>
						</tr>
						<tds:hasPermission permission='ShowProjectDailyMetrics'>
						<tr class="even">
							<td colspan="2">
								<g:link controller="project" action="launchProjectDailyMetricsJob" style="color:black">Launch Project Daily Metrics Job</g:link>
							</td>
						</tr>
						</tds:hasPermission>
					</tbody>
				</table>
			</div>
			<br />
			<div id="reconcileAssetsFormId" style="display: none;">
				<table class="admin-home">
					<thead>
						<tr>
							<th colspan="2">Reconcile AssetTypes</th>
						</tr>
					</thead>
					<tbody>
						<tr class="odd">
							<td colspan="2"><span id="outOfSyncAssetCountId">Assets out of sync: ###</span></td>
						</tr>
						<tr class="even">
							<td colspan="2"><a style="color:black" href="#" onclick="reconcileAssetTypes()"><button>Reconcile</button></a></td>
						</tr>
					</tbody>
				</table>
			</div>
			<br />
			<div id="encryptStringForm" style="display: none;">
				<table>
					<thead>
						<tr>
							<th colspan="2">Encrypt String</th>
						</tr>
					</thead>
					<tbody>
						<tr class="odd">
							<td><span>To Encrypt Value</span></td>
							<td><input name="toEncryptString" id="toEncryptString" value="" style="width:250px;"></td>
						</tr>
						<tr class="odd">
							<td><span>Encrypt Salt (Optional)</span></td>
							<td><input name="encryptSalt" id="encryptSalt" value="" style="width:250px;"></td>
						</tr>
						<tr class="odd">
							<td><span>Encrypt Alghoritm</span></td>
							<td>
								<select name="encryptAlghoritm" id="encryptAlghoritm">
									<option value="DES" selected>DES</option>
									<option value="AES">AES</option>
								</select>
							</td>
						</tr>
						<tr class="odd">
							<td><span>Encrypted Value</span></td>
							<td><input name="encryptedString" id="encryptedString" value="" style="width:250px;"></td>
						</tr>

						<tr class="even">
							<td colspan="2">
								<a style="color:black" href="#" onclick="sendValueToEncrypt()"><button>Encrypt</button></a>
								<a style="color:black" href="#" onclick="closeEncryptStringForm()"><button>Close</button></a>
							</td>
						</tr>
					</tbody>
				</table>
			</div>
		</td>
	</tr>
</table>

</div>
	<div id="flushOldBatchId" class="personShow" style="display: none;" title="Flush import data">
	<div id="respMsgId" style="display: none" class="message"></div>
	<div id="processDivId" style="display: none">
		<img src="${resource(dir:'images',file:'processing.gif')}" />
	</div>
	<input type="radio" name="deleteHistory" id="doNothing" value="doNothing" checked="checked"> <label for="doNothing">Do Nothing </label><br>
	<input type="radio" name="deleteHistory" id="overTwoMonths" value="overTwoMonths" > <label for="overTwoMonths">Over Two Months</label><br>
	<input type="radio" name="deleteHistory" id="anyProcessed" value="anyProcessed" > <label for="anyProcessed">Any Processed</label> <br>
	<input type="radio" name="deleteHistory" id="all" value="all" > <label for="all">All processed AND pending data</label> <br>
	<div class="buttons">
		<input type="button" id="processData" class="save" value="Submit" onclick="processBatch()"/>
		<input type="button"  class="delete" value="Cancel" id="processData" onclick="jQuery('#flushOldBatchId').dialog('close')"/>
	</div>
	</div>
</div>
<div id="showOrCleanTypeId" title="Clean Asset Types">
	<div id="cleanProcessId" style="display: none; " >
		Processing...<img src="${resource(dir:'images',file:'processing.gif')}" />
	</div>
	<div id="cleanProcessDivId" class="cleanProcessDiv">
		<img src="${resource(dir:'images',file:'processing.gif')}" />
	</div>
	<div class="buttons">
		<input type="button" id="cleanTypes" class="save" value="Clean" onclick="cleanTypes()"/>
		<input type="button"  class="delete" value="Cancel" onclick="jQuery('#showOrCleanTypeId').dialog('close')"/>
	</div>
</div>

<script>
	currentMenuId = '#adminMenu';
	$('.menu-admin-portal').addClass('active');
	$('.menu-parent-admin').addClass('active');

	$(document).ready(function() {
		$("#flushOldBatchId").dialog({ autoOpen: false })
		$("#showOrCleanTypeId").dialog({ autoOpen: false })
	})

</script>
</body>
</html>
