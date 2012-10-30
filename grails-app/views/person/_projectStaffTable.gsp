<table>
	<thead>
		<th>Name <br/> (job title)
		</th>
		<th>Company <br/>
		</th>
		<th>Role</th>
		<g:each in ="${moveEventList}" var="moveEvent" >
		<th>
		  ${moveEvent.project}<br/>
		  ${moveEvent.name}<br/>
		  ${moveEvent.startTime}<br/>
		</th>
		</g:each>
	</thead>
	<tbody>
		<g:each in="${staffList}" status="i" var="projectStaff">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
				<td><span style="cursor: pointer;" id="${projectStaff?.staff.id}" onClick="loadPersonDiv(this.id,'generalInfoShow')">
							${projectStaff?.name}
					</span></td>
				<td>
					<span style="cursor: pointer;" id="${projectStaff?.staff.id}" onClick="loadPersonDiv(this.id,'generalInfoShow')">
						${projectStaff?.company[0]}
					</span>
				</td>
				<td>
					<span style="cursor: pointer;" id="${projectStaff?.staff.id}" onClick="loadPersonDiv(this.id,'generalInfoShow')">
						${projectStaff?.role.description.substring(projectStaff?.role.description.lastIndexOf(':') +1).trim()}
					</span>
				</td>
				<g:each in="${eventCheckStatus}" var="moveEvent">
				  <g:each in="${moveEvent[projectStaff?.staff.id+'_'+projectStaff?.role+'_'+projectStaff?.project]}" var ="eventStatus"> 
					 <td>
					 	<input id="${eventStatus['id']}" type="checkbox" name="staffCheck" 
						  onClick="if(this.checked){this.value = 1} else {this.value = 0 };saveEventStaff(this.id);" value="0"
						  ${eventStatus['status']} />
					 </td>
				  </g:each>
				</g:each>
			</tr>
		</g:each>
	</tbody>
</table>

