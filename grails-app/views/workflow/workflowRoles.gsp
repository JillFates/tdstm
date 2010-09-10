<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="companyHeader" />
<title>Workflow Roles</title>
</head>
<body>
<div class="body">
<div class="steps_table">
	<span class="span"><b>Workflow Roles</b></span>
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
<div style="border: 1px solid #5F9FCF; margin-left: 10px;margin-right: 10px;">
<table>
	<thead>
		<tr>
			
			<th class="sortable">Roles</th>
			<g:if test="${browserTest}">
			<g:each in="${workflowTransitionsList}"  var="transition">
				<g:if test="${transition.code != 'SourceWalkthru' && transition.code != 'TargetWalkthru'}">
				<th class="verticaltext" title="${transition.name}" style="color: ${transition.header};padding: 0px; height: 102px;width:10px;" >${transition?.name}</th>
				</g:if>
			</g:each>
			</g:if>
			<g:else>
			<th style="padding-left: 0px; height: 102px" colspan="${headerCount}"><embed src="${createLinkTo(dir:'templates',file:'headerSvg_workflow.svg')}" type="image/svg+xml" width="${headerCount*21.80}" height="102px"/></th>
			</g:else>
			
		</tr>
	</thead>
	<tbody>
		<tr><td colspan="${headerCount}" class="no_records">No records found</td></tr>
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
