<%@ page import="net.transitionmanager.security.Permission" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="topNav" />
<g:javascript src="admin.js" />
<g:javascript src="projectStaff.js" />
<title>TDS TransitionManager&trade; Admin Portal</title>
<style type="text/css">
a:hover {
	text-decoration: underline;
}
</style>
</head>
<body>
<tds:subHeader title="Admin Portal" crumbs="['Admin','Portal']"/>
<div class="body">
<div><g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div id="showCleanTypeMsgId" style="display: none" class="message"></div>
<table style="border: 0; margin-top: -15px;" class="admin-home">
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
				<h1 style="margin-right: 0px;"><b>Misc Administrative Functions</b></h1>
				<table class="admin-home">
					<thead>
						<tr>
							<th>Misc Admin Functions</th>
							<th>Description</th>
						</tr>
					</thead>
					<tbody>
						<tr class="odd">
							<td><g:link controller="admin" action="systemInfo" style="color:black">System Info</g:link></td>
							<td>Provides current system technical performance data</td>
						</tr>
						<tr class="even">
							<td>
								<tds:hasPermission permission="${Permission.ApplicationRestart}">
									<g:link controller="admin" action="restartAppServiceForm" style="color:black">Restart Application Service</g:link>
								</tds:hasPermission>
							</td>
							<td>Dialog that allows user to restart TransitionManager</td>
						</tr>
						<tr class="odd">
							<td><g:link controller="admin" action="projectReport" style="color:black">Projects Summary Report</g:link></td>
							<td>A report to show a summary of assets in use by each project</td>
						</tr>
						<tr class="even">
							<td>
								<tds:hasPermission permission="${Permission.AdminUtilitiesAccess}">
									<g:link controller="apiCatalog" action="manager" style="color:black">API Dictionaries</g:link>
								</tds:hasPermission>
							</td>
							<td>Manage dictionaries used by Actions</td>
						</tr>
						<tr class="odd">
							<td>
								<tds:hasPermission permission="${Permission.AdminUtilitiesAccess }">
									<g:link controller="reports" action="metricDefinitions" style="color:black">Edit Metric Definitions</g:link>
								</tds:hasPermission>
							</td>
							<td>Manage queries used in generating daily metrics</td>
						</tr>
						<tr class="even">
							<td>
								<g:link controller="roleType" style="color:black">Teams</g:link>
							</td>
							<td>Used to add/modify the Team labels for all projects on instance</td>
						</tr>
						<tr class="odd">
							<td>
								<g:link controller="admin" action="orphanSummary" style="color:black">Manage Orphan Records</g:link>
							</td>
							<td>Checks for orphaned records like assets without a project</td>
						</tr>
						<tr class="even">
							<td>
								<a style="color:black" href="#" onclick="openReconcileAssetsForm()">Reconcile AssetTypes</a>
							</td>
							<td>This process will update assets' assetType property to match the value stored in the associated model</td>
						</tr>
						<tr class="odd">
							<td>
								<tds:hasPermission permission="${Permission.ModelEdit}">
									<g:link controller="admin" action="modelConflicts" style="color:black">Model / Alias Conflicts</g:link>
								</tds:hasPermission>
							</td>
							<td>A report that identifies any Model / Alias naming conflicts</td>
						</tr>
						<tr class="even">
							<td>
								<a style="color:black" href="#" onclick="openFlushDiv()"> Flush import data </a>
							</td>
							<td>Cleans out the database from old import batches (you can select all or older than a specific date)</td>
						</tr>
						<tr class="odd">
							<td>
								<a style="color:black" href="#" onclick="openEncryptStringForm()">Encrypt String</a>
							</td>
							<td>Encrypt values that will be used for installer configuration settings (such as Database Password)</td>
						</tr>
					</tbody>
				</table>
			</div>

<%-- The following section are various popup dialogs that are used by the menu items --%>

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
									<option value="AES" selected>AES</option>
									<option value="DES">DES</option>
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
		<input type="button"  class="delete" value="Cancel" id="processData" onclick="flushDialog.dialog('close')"/>
	</div>
	</div>
</div>
<div id="showOrCleanTypeId" title="Purge Unused Asset Types">
	<div id="cleanProcessId" style="display: none; " >
		Processing...<img src="${resource(dir:'images',file:'processing.gif')}" />
	</div>
	<div id="cleanProcessDivId" class="cleanProcessDiv">
		<p>
		The following list will display existing Asset Types that are created by Device Import Process. Those that
		have no references to models or devices will be indicated with a checkmark and can be safely purged.
		</p>
		<b>Loading List</b> <img src="${resource(dir:'images',file:'processing.gif')}" />
	</div>
	<div class="buttons">
		<input type="button" id="cleanTypes" class="save" value="Purge" onclick="cleanTypes()"/>
		<input type="button"  class="delete" value="Cancel" onclick="purgeDialog.dialog('close')"/>
	</div>
</div>

<script>
	currentMenuId = '#adminMenu';
	$('.menu-admin-portal').addClass('active');
	$('.menu-parent-admin').addClass('active');

</script>
</body>
</html>
