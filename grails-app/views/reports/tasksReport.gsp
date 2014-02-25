<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Move Results Report</title>
<script type="text/javascript">
	$(document).ready(function() {
		currentMenuId = "#reportsMenu";
		$("#reportsMenuId a").css('background-color', '#003366')
	});
</script>
</head>
<body>
	<div class="body">
		<h1>Task Report</h1>
		<g:if test="${flash.message}">
			<div class="message">
				${flash.message}
			</div>
		</g:if>
		<table>
			<thead>
				<tr>
					<th>Task #</th>
					<th>Task Description</th>
					<th>Related to</th>
					<th>Predecessor Task(s)</th>
					<th>Responsible Resource</th>
					<th>Team</th>
					<th>Status</th>
					<th>Date Planned</th>
					<th>Date Required</th>
					<th>Comments</th>
					<th>Duration</th>
					<th>Estimated Start</th>
					<th>Actual Time</th>
					<th>Estimated Finish</th>
					<th>Actual Finish</th>
					<th>WorkFlow Step</th>
					<th>Created on</th>
					<th>Created by</th>
					<th>Event</th>
				</tr>
			</thead>
			<tbody>
				<g:each in="${taskList}" var="task" status="i">
					<tr class="${i%2==0 ? 'even' : 'odd' }">
						<td>
							${task.taskNumber}
						</td>
						<td>
							${task.comment}
						</td>
						<td>
							${task.assetEntity?.assetName}
						</td>
						<td><g:each in="${task.taskDependencies}" var="dep">
								${dep.predecessor == null ? '' : dep.predecessor.taskNumber + ' ' + dep.predecessor.comment.toString()}
								<br />
							</g:each></td>
						<td>
							${task.assignedTo}
						</td>
						<td>
							${task.role}
						</td>
						<td>
							${task.status}
						</td>
						<td>NA</td>
						<td>NA</td>
						<td><g:each in="${task.notes}" var="note">
								${note.note}
							</g:each></td>
						<td>
							${task.duration}
						</td>
						<td><tds:convertDate date="${task.estStart}" /></td>
						<td><tds:convertDate date="${task.actStart}"  /></td>
						<td><tds:convertDate date="${task.estFinish }" /></td>
						<td><tds:convertDate date="${task.dateResolved }"/></td>
						<td>NA</td>
						<td>
							<tds:convertDate date="${task.dateCreated }"/>
						</td>
						<td>${task.createdBy}</td>
						<td>
							${task.moveEvent }
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</body>
</html>
