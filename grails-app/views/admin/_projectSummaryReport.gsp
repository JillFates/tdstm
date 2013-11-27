<div>
		<h3>Generated for ${person} on <tds:convertDateTime  timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" date="${time}" />.</h3>
		<table>
			<thead>
				<tr>
					<th>Client</th>
					<th>Partner</th>
					<th>Project</th>
					<th>Start</th>
					<th>Finish</th>
					<th>Staff</th>
					<th>Events</th>
					<th>Servers</th>
					<th>Applications</th>
					<th>Databases</th>
					<th>Storage</th>
					<th nowrap>Total Assets Count</th>
					<th>Project Description</th>
				</tr>
			</thead>
			<tbody>
				<g:if test="${!results.projects}">
				<tr>
				<td colspan="11" style="text-align:center;color:red; ">Records Not Found</td>
				</tr>
				</g:if>
				<g:each in="${results.projects}" var="project" status="i">
					<g:set var="summaryMap" value="${results.summaryMap}"/>
					<tr class="${i%2==0 ? 'even' : 'odd' }">
						<td>
							${project.client.name}
						</td>
						<td>${summaryMap[project.id].patner?.partyIdTo}</td>
						<td>
							${project.projectCode}
						</td>
						<td>
							 <tds:convertDate timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" date="${project.startDate}" /> 
						</td>
						<td>
							<tds:convertDate timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" date="${project.completionDate}" />
						</td>
						<td>${summaryMap[project.id].staffCount.size()?:''}</td>
						<td>${summaryMap[project.id].eventCount?:''}</td>
						<td>${summaryMap[project.id].assetCount?:''}</td>
						<td>${summaryMap[project.id].appCount?:''}</td>
						<td>${summaryMap[project.id].dbCount?:''}</td>
						<td>${summaryMap[project.id].fileCount?:''}</td>
						<td>${summaryMap[project.id].totalAssetCount?:''}</td>
						<td>${project.description}</td>
					</tr>
				</g:each>
			</tbody>
		</table>
</div>
