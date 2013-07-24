<table>
	<thead>
		<tr>
			<th style="cursor: pointer;" onclick="loadFilteredStaff('lastName','lastName')">Name</th>
			<th style="cursor: pointer;" onclick="loadFilteredStaff('company','company')">Company</th>
			<th style="cursor: pointer;" onclick="loadFilteredStaff('team','team')">Team</th>
			<g:if test="${projectId == 0}"> <th>Project</th></g:if>
			<g:else><th>${project?.name}</th></g:else>
			<g:each in ="${moveEventList}" var="moveEvent" >
			<th>
				${moveEvent.project}<br/>
				${moveEvent.name}<br/>
				${moveEvent.startTime}<br/>
			</th>
			</g:each>
		</tr>
	</thead>
	<tbody>
		<g:each in="${staffList}" status="i" var="projectStaff">
			<g:set var="inProject" value="${projectStaff.project == 1}" />
			<g:set var="roleHasVowel" value="${projectStaff.team.getAt(0).find(/[aeiouAEIOU]/)}" />
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" >
				<td nowrap="nowrap">
					<span style="cursor: pointer;" id="${projectStaff?.personId}" onClick="loadPersonDiv(this.id,'generalInfoShow')">
						${projectStaff?.fullName}
					</span>
				</td>
				<td>
					<span style="cursor: pointer;" id="${projectStaff?.personId}" onClick="loadPersonDiv(this.id,'generalInfoShow')">
						${projectStaff?.company}
					</span>
				</td>
				<td id="roleColumnId" nowrap="nowrap" title="${projectStaff.role}">
					<span style="cursor: pointer;" id="${projectStaff?.personId}" onClick="loadPersonDiv(this.id,'generalInfoShow')">
						${projectStaff?.team}
					</span>
				</td>
				<g:if test="${projectId == 0}">
					<td id="projectColumnId" nowrap="nowrap">
						multiple
					</td>
				</g:if>
				<g:else>
					<g:if test="${projectStaff.role != 'STAFF'}">
						<td id="projectColumnId" class="${(projectStaff.project==1)?('checkedStaff'):('')}" nowrap="nowrap" title="${projectStaff.fullName} is ${(inProject)?(''):('not ')}part of project ${project?.name} as a${(roleHasVowel)?('n'):('')} ${projectStaff?.team}">
							<input id="${projectStaff.personId}" type="checkbox" name="staffCheck" ${editPermission ?: 'disabled = "disabled"'}
								onClick="if(this.checked){this.value = 1} else {this.value = 0 };saveProjectStaff($(this))" 
								value="${(inProject)?(1):(0)}" 
								${(projectStaff.project==1)?('checked="checked"'):('')} />
						</td>
					</g:if>
					<g:else>
						<td id="${projectColumnId}" nowrap="nowrap" title="${projectStaff.fullName} cannot be assigned using this team">
						</td>
					</g:else>
				</g:else>
				<g:each in="${moveEventList}" var="moveEvent">
					<g:if test="${projectStaff.role != 'STAFF'}">
						<g:if test="${projectStaff.unavailableDates.tokenize(',').contains(moveEvent.startDate)}">
							<td id="${moveEvent.id}" class="unavailibleStaff" nowrap="nowrap" title="${projectStaff.fullName} is not available on ${moveEvent.startTime}">
							</td>
						</g:if>
						<g:else>
							<g:set var="inMoveEvent" value="${(projectStaff.moveEvents.tokenize(',').contains(moveEvent.id.toString()))}" />
							<td id="${moveEvent.id}" class="${(inMoveEvent)?('checkedStaff'):('')}" nowrap="nowrap" title="${moveEvent.project} - ${moveEvent.name} - ${moveEvent.startTime}">
								<input id="${projectStaff.personId}" type="checkbox" name="staffCheck" ${editPermission ?: 'disabled = "disabled"' }
									onClick="if(this.checked){this.value = 1} else {this.value = 0 };saveEventStaff($(this));" 
									value="${(inMoveEvent)?(1):(0)}" 
									${(inMoveEvent)?('checked="checked"'):('')} />
							</td>
						</g:else>
					</g:if>
					<g:else>
						<td id="${moveEvent.id}" nowrap="nowrap" title="${projectStaff.fullName} cannot be assigned using this team">
						</td>
					</g:else>
				</g:each>
				
			</tr>
		</g:each>
	</tbody>
</table>
<input type="hidden" id="orderBy" value="${orderBy?:'desc'}">
<input type="hidden" id="sortOn" value="${sortOn?:'lastName'}">
<input type="hidden" id="firstProp" value="${firstProp?:'staff'}">