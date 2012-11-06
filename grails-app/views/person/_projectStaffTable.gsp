<table>
	<thead>
		<th>Name <br/> (job title)
		</th>
		<th>Company <br/>
		</th>
		<th>Role</th>
		<g:if test="${projectId=='0'}"> <th>Project</th></g:if>
		<g:else><th>${project?.name}</th></g:else>
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
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" >
				<td nowrap="nowrap"><span style="cursor: pointer;" id="${projectStaff?.staff.id}" onClick="loadPersonDiv(this.id,'generalInfoShow')">
							${projectStaff?.name}
					</span></td>
				<td>
					<span style="cursor: pointer;" id="${projectStaff?.staff.id}" onClick="loadPersonDiv(this.id,'generalInfoShow')">
						${projectStaff?.company[0]}
					</span>
				</td>
				<td nowrap="nowrap">
					<span style="cursor: pointer;" id="${projectStaff?.staff.id}" onClick="loadPersonDiv(this.id,'generalInfoShow')">
						${projectStaff?.role.description.substring(projectStaff?.role.description.lastIndexOf(':') +1).trim()}
					</span>
				</td>
				<g:if test="${projectId=='0'}">
				 <td style="cursor: pointer;" nowrap="nowrap">
				    <span  id="projectList_${projectStaff?.staff.id}_${projectStaff?.role}"  onClick="loadPersonDiv(this.id,'generalInfoShow')" title="${projectStaff?.staffProject.toString().replace('[','').replace(']','')}" >
						${projectStaff?.staffProject?.size() > 1 ? 'mutiple' : projectStaff?.staffProject[0]}
					</span>
				 </td>
				</g:if>
				<g:else>
				<g:each in="${staffCheckStatus}" var="projectStaffs">
				  <g:each in="${projectStaffs[projectStaff?.staff.id+'_'+projectStaff?.role+'_'+projectStaff?.project]}" var ="staffStatus"> 
					 <td nowrap="nowrap">
					 	<input id="${staffStatus['id']}" type="checkbox" name="staffCheck" 
						  onClick="if(this.checked){this.value = 1} else {this.value = 0 };saveProjectStaff(this.id);" value="0"
						  ${staffStatus['status']} />
					 </td>
				  </g:each>
				</g:each>
				</g:else>
				<g:each in="${eventCheckStatus}" var="moveEvent">
				  <g:each in="${moveEvent[projectStaff?.staff.id+'_'+projectStaff?.role+'_'+projectStaff?.project]}" var ="eventStatus"> 
					 <td nowrap="nowrap">
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

