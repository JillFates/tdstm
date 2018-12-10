<div>
	<h3>Generated for ${person} on <tds:convertDateTime  date="${time}" />.</h3>
	<table>
		<thead>
			<tr>
				<th>Client</th>
				<th>Project</th>
				<th>Start</th>
				<th>Finish</th>
				<th>Staff</th>
				<th>Partner(s)</th>
				<th># Events</th>
				<th>Servers<br>(All)</th>
				<th>Servers<br>(In Planning)</th>
				<th>Physical Devices<br>(All)</th>
				<th>Applications<br>(All)</th>
				<th>Databases<br>(All)</th>
				<th>Logical Storage<br>(All)</th>
				<th># Assets<br>(All)</th>
				<th>Project Description</th>
			</tr>
		</thead>
		<tbody>
			<g:if test="${!results}">
				<tr>
					<td colspan="11" style="text-align:center;color:red; ">Records Not Found</td>
				</tr>
			</g:if>
			<g:each in="${results}" var="project" status="i">
				<tr class="${i%2==0 ? 'even' : 'odd' }">
					<td>${project.clientName?:''}</td>
					<td>${project.projName?:''}</td>
					<td><tds:convertDate  date="${project.startDate}" /></td>
					<td><tds:convertDate  date="${project.completionDate}" /></td>
					<td>${project.staffCount?:''}</td>
					<td>${project.partnerNames?:''}</td>
					<td>${project.eventCount?:''}</td>
					<td>${project.totalServCount?:''}</td>
					<td>${project.inPlanningServCount?:''}</td>
					<td>${project.deviceCount?:''}</td>
					<td>${project.appCount?:''}</td>
					<td>${project.dbCount?:''}</td>
					<td>${project.filesCount?:''}</td>
					<td>${project.totalAssetCount?:''}</td>
					<td>${project.description?:''}</td>
				</tr>
			</g:each>
		</tbody>
	</table>
</div>
