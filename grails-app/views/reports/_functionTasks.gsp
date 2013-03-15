<table style="width: 700px; margin-left: 100px">
	<thead>
		<tr>
			<th width="100px;">Team</th>
			<th width="100px;">Team Code</th>
			<th width="150px;">Team Members</th>
			<th width="50px;">Tasks</th>
		</tr>
	</thead>
	<tbody>
		<g:each in="${bundleMap}" var="teamList" status="i" >
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td>
					${teamList.name.substring(teamList.name.lastIndexOf(':') +1).trim()}
				</td>
				<td>
					${teamList.code}
				</td>
				<td>
					<g:each in="${teamList.assignedStaff}" var="teamListtaff">
						${teamListtaff} <br /><br />
					</g:each>
				</td>
				<td>
					${teamList.tasks}
				</td>
			</tr>
		</g:each>
	</tbody>
</table>