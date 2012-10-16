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
				<g:if test="${personHasPermission }">
					<td><g:remoteLink controller="person" action="editStaff"
							id="${projectStaff?.staff.id}"
							params="[role:projectStaff?.role.id]"
							onComplete="editPersonDialog( e );">
							${projectStaff?.name}
						</g:remoteLink></td>
				</g:if>
				<g:else>
					<td><g:remoteLink controller="person" action="editStaff"
							id="${projectStaff?.staff.id}"
							params="[role:projectStaff?.role.id]"
							onComplete="showPersonDialog( e );">
							${projectStaff?.name}
						</g:remoteLink></td>
				</g:else>
				<td>
					${projectStaff?.company[0]}
				</td>
				<td>
					${projectStaff?.role}
				</td>
			</tr>
		</g:each>

	</tbody>
</table>