<div>
	<h3>Generated for ${person} on <tds:convertDateTime  date="${time}" />.</h3>
	<table>
		<thead>
			<tr>
				<th>Client</th>
				<th>Partners</th>
				<th>Project</th>
				<th>Start</th>
				<th>Finish</th>
				<th>Staff</th>
				<th>Events</th>
				<th>Devices</th>
				<th>Applications</th>
				<th>Databases</th>
				<th>Storage</th>
				<th nowrap>Total Assets Count</th>
				<th>Project Description</th>
				<th>Servers Total</th>
				<th>Servers In Planning</th>
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
					<td>${project.partnerNames?:''}</td>
					<td>${project.projName?:''}</td>
					<td><tds:convertDate  date="${project.startDate}" /></td>
					<td><tds:convertDate  date="${project.completionDate}" /></td>
					<td>${project.staffCount?:''}</td>
					<td>${project.eventCount?:''}</td>
					<td>${project.assetCount?:''}</td>
					<td>${project.appCount?:''}</td>
					<td>${project.dbCount?:''}</td>
					<td>${project.filesCount?:''}</td>
					<td>${project.totalAssetCount?:''}</td>
					<td>${project.description?:''}</td>
					<td>${project.totalServCount?:''}</td>
					<td>${project.inPlanningServCount?:''}</td>
				</tr>
			</g:each>
		</tbody>
	</table>
</div>
