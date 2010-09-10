<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="companyHeader" />
<title>Workflows</title>
</head>
<body>
<div class="body">
<div class="steps_table">
	<span class="span"><b>Workflows</b></span>
<div class="list" style="border: 1px solid #5F9FCF; margin-left: 10px;margin-right: 10px;">
<table>
	<thead>
		<tr>
			<g:sortableColumn property="process" title="Workflow" />

			<th class="sortable" style="font-size: 10px;">Used On</th>
			
			<g:sortableColumn property="dateCreated" title="Created On" />

			<g:sortableColumn property="lastUpdated" title="Updatated On" />
			
			<g:sortableColumn property="updateBy" title="Updated By" />
			
		</tr>
	</thead>
	<tbody>
		<g:each in="${workflowInstanceList}" status="i" var="workflows">
			<tr class="${(i % 2) == 0 ? 'odd' : 'even'}" onclick="showWorkflowList('${workflows?.id}')">

				<td nowrap="nowrap">${workflows?.process}</td>
				<td>${Project.findAllByWorkflowCode(workflows?.process)?.name.toString().replace("[","").replace("]","")}</td>
				<td nowrap="nowrap">
					<tds:convertDateTime date="${workflows?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
				</td>
				<td nowrap="nowrap">
					<tds:convertDateTime date="${workflows?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}" />
				</td>

				<td nowrap="nowrap">${workflows?.updateBy}</td>
			</tr>
		</g:each>
	</tbody>
</table>
</div>
<div class="buttons" style="margin-left: 10px;margin-right: 10px;"> 
	<g:form action="workflowList" name="workflowForm">
		<span class="button">
			<input class="save" type="button" value="Create Workflow" />
			<input type="hidden" name="workflow" id="workflowId">
		</span>
	</g:form>
</div>

</div>
<script type="text/javascript">
function showWorkflowList( workflowId ){
	$("#workflowId").val( workflowId );
	$("form[name=workflowForm]").submit();
}
</script>
</body>
</html>
