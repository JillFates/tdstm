<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="companyHeader" />
<title>Workflow</title>
</head>
<body>
<div class="body">
<div class="steps_table">
	<span class="span"><b>Workflow</b></span>
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px;margin: 0px 10px 10px 10px;">
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
</table>
</div>
<div class="list" style="border: 1px solid #5F9FCF; margin-left: 10px;margin-right: 10px;">
<table>
	<thead>
		<tr>
			
			<th class="sortable">Step</th>
			
			<th class="sortable">Label</th>
			
			<th class="sortable">Dashboard Label</th>
			
			<th class="sortable">Sequence</th>
			
			<th class="sortable">Type</th>
			
			<th class="sortable">Start</th>
			
			<th class="sortable">Color</th>
			
			<th class="sortable">Header</th>
			
		</tr>
	</thead>
	<tbody>
		<g:if test="${workflowTransitionsList}">
		<g:each in="${workflowTransitionsList}" status="i" var="transitions">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" onclick="showWorkflowRoles('${transitions.id}')">

				<td nowrap="nowrap">${transitions?.code}</td>
				
				<td nowrap="nowrap">${transitions?.name}</td>
				
				<td nowrap="nowrap">${transitions?.dashboardLabel}</td>
				
				<td nowrap="nowrap">${transitions?.transId}</td>
				
				<td nowrap="nowrap"><g:message code="workflow.type.${transitions?.type}" /></td>

				<td nowrap="nowrap">${transitions?.predecessor}</td>

				<td nowrap="nowrap">${transitions?.color}</td>
				
				<td nowrap="nowrap">${transitions?.header}</td>
				

			</tr>
		</g:each>
		</g:if>
		<g:else>
		<tr><td colspan="8" class="no_records">No records found</td></tr>
		</g:else>
	</tbody>
</table>
</div>
<div class="buttons" style="margin-left: 10px;margin-right: 10px;"> 
	<g:form action="workflowRoles" name="workflowRolesForm">
		<span class="button">
			<input type="hidden" name="workflowTransition" id="workflowTransitionId">
		</span>
	</g:form>
	<g:form onsubmit="return false">
    	<input type="hidden" name="id" value="${workflow?.id}" />
        <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
        <span class="button"><g:actionSubmit class="delete" onclick="return confirm('WARNING: Deleting this Workflow will remove any Projects and any related data?');" value="Delete" /></span>
	</g:form>
</div>
</div>
<script type="text/javascript">
function showWorkflowRoles( workflowTransitionId ){
	$("#workflowTransitionId").val( workflowTransitionId );
	$("form[name=workflowRolesForm]").submit();
}
</script>
</body>
</html>
