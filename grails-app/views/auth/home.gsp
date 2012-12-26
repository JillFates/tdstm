<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
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
<table style="border: 0">
	<tr>
		<td style="vertical-align:top">
			<div>
			<h1 style="margin-right: 0px;"><b>Recent Users</b></h1>
			<table>
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
						<td><g:link controller="person" action="show" id="${user.person?.id}">${user.person}</g:link></td>
						<td><g:link controller="userLogin" action="show" id="${user.id}">${user.username}</g:link></td>
						<td><tds:convertDateTime date="${user.lastLogin}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
						<td><tds:convertDateTime date="${user.lastPage}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
					</tr>
					</g:each>
				</tbody>
			</table>
			</div>
		</td>
		<td style="vertical-align:top">
			<div>
			<h1 style="margin-right: 0px;"><b>Current and Recent Events</b></h1>
			<table>
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
							<td><tds:convertDateTime date="${eventList?.startTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
							<td><tds:convertDateTime date="${eventList?.completionTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
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
				<h1 style="margin-right: 0px;"><b>Upcoming Move Bundles</b></h1>
				<table>
					<thead>
						<tr>
							<th>Name </th>
							<th>Start Time</th>
							<th>Completion Time</th>
						</tr>
					</thead>
					<tbody>
						<g:if test="${moveBundlesList}">
							<g:each in="${moveBundlesList}" status="i"  var="bundle">			
							<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
								<td><g:link controller="project" action="show" id="${bundle.project?.id}">${bundle.project?.name} - ${bundle.name}</g:link></td>
								<td><tds:convertDateTime date="${bundle.startTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
								<td><tds:convertDateTime date="${bundle.completionTime}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></td>
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
		<table>
			<thead>
				<tr>
					<th colspan="2">List of Party Actions</th>
				</tr>
			</thead>
			<tbody>
				<tr class="odd">
					<td><g:link controller="partyGroup" style="color:black">Company</g:link></td>
					<td><g:link controller="roleType" style="color:black">Role Type </g:link></td>
				</tr>
				<tr class="even">
					<td><g:link controller="partyRelationship" style="color:black">Party Relationship</g:link></td>
					<td><g:link controller="partyRelationshipType"
						style="color:black">Party RelationshipType</g:link></td>
				</tr>
				<tr class="odd">
					<td><g:link controller="userLogin" style="color:black">Manage Users</g:link></td>
					<td><g:link controller="refCode" style="color:black">Manage RefCode</g:link></td>
				</tr>
				<tr class="odd">
					<td><g:link controller="admin" action="orphanSummary"
						style="color:black">Manage Orphan Records</g:link></td>
					<td></td>
				</tr>
			</tbody>
		</table>
			</div>
		</td>
	</tr>
</table>
<span class="spanAnchor" style="float:right" onclick="openFlushDiv()"> Flush import data </span>
 
</div>
	<div id="flushOldBatchId" style="display: none;" title="Flush import data">
	<div id="respMsgId" style="display: none" class="message">${flash.message}</div>
		  <input type="radio" name="deleteHistory" id="doNothing" value="doNothing" checked="checked"> <label for="doNothing">Do Nothing </label><br>
		  <input type="radio" name="deleteHistory" id="overTwoMonths" value="overTwoMonths" > <label for="overTwoMonths">Over Two Months</label><br>
		  <input type="radio" name="deleteHistory" id="anyProcessed" value="anyProcessed" > <label for="anyProcessed">Any Processed</label> <br>
		  <input type="radio" name="deleteHistory" id="all" value="all" > <label for="all">All uploaded and processed data</label> <br>
		  <div id="processDivId" style="display: none">
	  		<img id="processingId" src="../images/processing.gif" />
		  </div>
		  
		  <div class="buttons">
			  <input type="button" id="processData" class="save" value="Submit" onclick="processBatch()"/> 
			  <input type="button"  class="save" value="cancel" id="processData" />
		  </div>
	</div>
</div>


<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
	
	$(document).ready(function() {
		$("#flushOldBatchId").dialog({ autoOpen: false })
	})
	
	function processBatch(){
		var value=$('input:radio[name=deleteHistory]:checked').val()
		if(value=="doNothing"){
			 $("#flushOldBatchId").dialog('close');
		} else {
			jQuery.ajax({
				url: '../admin/processOldData',
				data: {'deleteHistory':value},
				type:'POST',
				beforeSend:function(jqXHR){
					$('#processDivId').show(); 
					$("#respMsgId").hide();
					$("#processingId").show();
				},
				success: function(data) {
					$("#processingId").hide();
					$("#respMsgId").show().html(data)
				},
				error:function(jqXHR, textStatus, errorThrown){
					$("#processingId").hide();
					$("#respMsgId").show().html("An unexpected error occurred. Please close and reload form to see if the problem persists")
				}
			})
		}
	}
	
	function openFlushDiv(){
		 $("#flushOldBatchId").dialog('option', 'width', '500px')
		 $("#flushOldBatchId").dialog('option', 'position', ['center','top']);
		 $("#flushOldBatchId").dialog('open');
	}
</script>
</body>
</html>
