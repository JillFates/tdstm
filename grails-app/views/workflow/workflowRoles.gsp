<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="companyHeader" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'dashboard.css')}" />
<title>Workflow Roles</title>
</head>
<body>
<div class="body">
<div class="steps_table" style="text-align: left;">
	<span class="span"><b>Workflow Roles</b></span>
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px;margin: 0px 10px 10px 10px;text-align: left;">
	<span class="menuButton"><g:link class="list" action="home">Workflow List</g:link></span>
</div>
<div>
<table border="0" style="width: 400px;margin: 0px 10px 10px 20px; ">
	<tr class="prop">
		<td valign="top"  class="name">Workflow:</td>
		<td valign="top"  class="value">${workflow?.process}</td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Created On:</td>
		<td valign="top"  class="value"><tds:convertDateTime date="${workflow?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Update On:</td>
		<td valign="top"  class="value"><tds:convertDateTime date="${workflow?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" /></td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Updated By:</td>
		<td valign="top"  class="value">${workflow?.updateBy}</td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Used On:</td>
		<td valign="top"  class="value">
		<g:if test="${workflow?.process}">
			<table><ul>
				<g:each in="${Project.findAllByWorkflowCode(workflow?.process)}" var="project">
				<tr>
				<li><g:link controller="project" action="show" id="${project.id}">${project?.name}</g:link></li>
				</tr>
				</g:each>
				</ul>
			</table>
			</g:if>
		</td>
	</tr>
	<tr class="prop">
		<td valign="top"  class="name">Current Status:</td>
		<td valign="top"  class="value">${workflowTransition?.name}</td>
	</tr>
</table>
</div>
<g:form action="updateWorkflowRoles">

<div id="tableContainer" class="list" style="margin-left: 5px;margin-right: 10px;">
<table cellpadding="0" cellspacing="0" style="border:1px solid #63A242;width: 600px;">
	<thead>
		<tr>
			<th style="padding: 5px 6px">Transitions</th>
			
			<g:each in="${swimlanes}" var="swimlane">
				<th class="name" style="width: 80px;">${swimlane?.actorId}</th>
			</g:each>
		</tr>
	</thead>
	<tbody id="workflowRolesBody">
		<g:if test="${workflowTransitionsList}">
		<g:each in="${roleWiseTransitions}" var="roleWiseTransition" status="i">
			<tr id="transition_${roleWiseTransition.transition?.id}"  class="${(i % 2) == 0 ? 'odd' : 'even'}">
			<td class="name"  nowrap="nowrap">
				<g:link action="workflowRoles" params="[workflowTransition:roleWiseTransition.transition?.id]" style="font-weight: ${workflowTransition?.id == roleWiseTransition?.transition.id ? 'bold' : 'normal'}; color: ${roleWiseTransition?.transition.header ? roleWiseTransition?.transition.header : roleWiseTransition?.transition.type == 'boolean' ? '#FF8000' : '#336600'}">
				${roleWiseTransition?.transition?.name}
				</g:link>
			</td>
			<g:each in="${roleWiseTransition.transitionsMap}" var="transitionMap">
				<td id="${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.transId}" nowrap="nowrap">
					<g:if test="${workflowTransition?.id != roleWiseTransition?.transition.id }">
						<g:if test="${transitionMap.workflowTransitionMap}">
							<input type="checkbox" name="${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.id}" checked="checked"/>	
							<g:select name="flag_${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.id}" from="${WorkflowTransitionMap.constraints.flag.inList}" value="${transitionMap.workflowTransitionMap.flag}" noSelection="${['':'']}"  valueMessagePrefix="workflow.flag"></g:select>
						</g:if>
						<g:else>
							<input type="checkbox" name="${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.id}"/>
							<g:select name="flag_${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.id}" from="${WorkflowTransitionMap.constraints.flag.inList}"  noSelection="${['':'']}" valueMessagePrefix="workflow.flag"></g:select>
						</g:else>
					</g:if>
					<g:else>
						<input type="checkbox" name="${transitionMap.swimlane?.name}_${roleWiseTransition.transition?.id}" disabled="disabled"/>
					</g:else>
				</td>
			</g:each>
			</tr>
		</g:each>
		</g:if>
		<g:else>
			<tr><td colspan="40" class="no_records">No records found</td></tr>
		</g:else>
	</tbody>
</table>
</div>
<div class="buttons" style="margin-left: 10px;margin-right: 10px;text-align: left;"> 
    	<input type="hidden" name="workflow" value="${workflow?.id}" />
    	<input type="hidden" name="currentStatus" value="${workflowTransition?.id}" />
        <span class="button"><input type="submit" class="edit" value="Update" /></span>
</div>
</g:form>
</div>
</body>
</html>
