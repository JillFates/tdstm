<table>
	<thead>
		<th>Name <br/> (job title)
		</th>
		<th>Company <br/>
		</th>
		<th>Role</th>
		<%--<g:each in ="${moveEventList}" var="moveEvent" >
		<th>
		  ${moveEvent.project}<br/>
		  ${moveEvent.name}<br/>
		  ${moveEvent.startTime}<br/>
		</th>
		</g:each>
	--%></thead>
	<tbody>
		<g:each in="${staffList}" status="i" var="projectStaff">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td><span style="cursor: pointer;" id="${projectStaff?.staff.id}" onClick="loadPersonDiv(this.id)">
							${projectStaff?.name}
					</span></td>
				<td>
					<span style="cursor: pointer;" id="${projectStaff?.staff.id}" onClick="loadPersonDiv(this.id)">
						${projectStaff?.company[0]}
					</span>
				</td>
				<td>
					<span style="cursor: pointer;" id="${projectStaff?.staff.id}" onClick="loadPersonDiv(this.id)">
						${projectStaff?.role}
					</span>
				</td>
			</tr>
		</g:each>

	</tbody>
</table>