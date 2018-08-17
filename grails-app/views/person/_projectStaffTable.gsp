<%@page import="net.transitionmanager.domain.Person" %>
<table id="staffingTable">
	<thead>
		<tr id="headerRowId">
			<th style="cursor: pointer;" onclick="toggleSortOrder('fullName','fullName')">Name</th>
			<th style="cursor: pointer;" onclick="toggleSortOrder('company','company')">Company</th>
			<th style="cursor: pointer;" onclick="toggleSortOrder('team','team')">Team</th>
			<g:if test="${projectId == 0}"> <th>Project</th></g:if>
			<g:else><th>${project?.name}</th></g:else>
			<g:each in="${moveEventList}" var="moveEvent" >
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
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" >
				<td nowrap="nowrap" class="js-staffFullName">
					<span style="cursor: pointer;" id="${projectStaff?.personId}" onClick="Person.showPersonDialog(this.id,'generalInfoShow')">
						${projectStaff?.fullName}
					</span>
				</td>
				<td class="js-staffCompany">
					<span style="cursor: pointer;" id="${projectStaff?.personId}" onClick="Person.showPersonDialog(this.id,'generalInfoShow')">
						${projectStaff?.company}
					</span>
				</td>
				<td id="roleColumnId" class="js-staffRole" nowrap="nowrap">
					<span style="cursor: pointer;" id="${projectStaff?.personId}" onClick="Person.showPersonDialog(this.id,'generalInfoShow')">
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

						<td id="projectColumnId"
							onClick="clkCB(event, $(this),${projectStaff.personId},${project.id}, null, '${projectStaff.role}', 'addRemoveProjectTeam');"
							class="js-staffProject ${(projectStaff.project==1 ? 'checkedStaff' :'' )}"
							nowrap="nowrap">
							<input id="${projectStaff.personId}" type="checkbox" name="staffCheck" ${editPermission ? '' : 'disabled = "disabled"'}
								value="${inProjectValue}"
								${(projectStaff.project==1 ? 'checked="checked"' : '')} />
						</td>
					</g:if>
					<g:else>
						<td id="${projectColumnId}" nowrap="nowrap"
							onClick="clkCB(event, $(this), ${projectStaff.personId}, ${project.id}, null, '${projectStaff.role}', 'togPrjStaff');"
						>

							<input id="staff_person_${projectStaff.personId}" type="checkbox" name="staffChangeCheck" ${editPermission ? '' : 'disabled = "disabled"'}
								value="${inProjectValue}"
								${(projectStaff.project==1 ? 'checked="checked"' : '')} />

						</td>
					</g:else>
				</g:else>
				<g:each in="${moveEventList}" var="moveEvent">
					<g:if test="${projectStaff.role != 'STAFF'}">
						<g:if test="${projectStaff.unavailableDates.tokenize(',').contains(moveEvent.startDate)}">
							<td id="${moveEvent.id}" class="unavailibleStaff" nowrap="nowrap" title="Not available on ${moveEvent.startTime}">
								<input type="checkbox" disabled />
							</td>
						</g:if>
						<g:else>
							<g:set var="inMoveEvent" value="${(projectStaff.moveEvents.tokenize(',').contains(moveEvent.id.toString()))}" />
							<td id="${moveEvent.id}" class="${( inMoveEvent ? 'checkedStaff' : '' )}" nowrap="nowrap"
								onClick="clkCB(event, $(this), ${projectStaff.personId}, ${project.id}, ${moveEvent.id},'${projectStaff.role}', 'togEvtStaff');">
								<input id="${projectStaff.personId}" type="checkbox" name="staffCheck" ${(editPermission ? '' : 'disabled = "disabled"') }
									   value="${(inMoveEvent ? '1' : '0' )}" ${ ( inMoveEvent ? 'checked="checked"' : '' )} />
							</td>
						</g:else>
					</g:if>
					<g:else>
						<td>&nbsp;</td>
					</g:else>
				</g:each>

			</tr>
		</g:each>
	</tbody>
</table>
<input type="hidden" id="orderBy" value="${orderBy?:'asc'}">
<input type="hidden" id="sortOn" value="${sortOn?:'fullName'}">
<input type="hidden" id="firstProp" value="${firstProp?:'staff'}">
