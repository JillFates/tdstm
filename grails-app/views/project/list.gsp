<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Project List</title>
<script language="javascript" src="${createLinkTo(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
<link rel="stylesheet" type="text/css" href="${createLinkTo(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
<script type="text/javascript">
function onInvokeAction(id) {
    setExportToLimit(id, '');
    createHiddenInputFieldsForLimitAndSubmit(id);
}
function onInvokeExportAction(id) {
    var parameterString = createParameterStringForLimit(id);
    location.href = 'list?' + parameterString;
}
</script>
</head>
<body>

<div class="body"><br/>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<div>
	<div class="buttons"> 
		<g:form action="create" method="post">
		<span class="button"><g:actionSubmit class="save" action="Create" value="Create Project" /></span>
		<span class="button"><input type="button" class="save" onclick="javascript:location.href='../projectUtil/createDemo'" value="Create Demo Project" /></span>
		<span class="button"><g:actionSubmit class="save" action="List" value="Show Completed Projects"/></span>
		</g:form>
	</div>
	<form name="projectForm" action="list">
         <jmesa:tableFacade id="tag" items="${projectList}" maxRows="25" stateAttr="restore" var="projectInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
             <jmesa:htmlTable style=" border-collapse: separate">
                 <jmesa:htmlRow highlighter="true">
                     <jmesa:htmlColumn property="projectCode" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
						<g:link controller="project" action="addUserPreference" params="['selectProject':projectInstance.projectCode]">${projectInstance.projectCode}</g:link>
					 </jmesa:htmlColumn>
					 <jmesa:htmlColumn property="name" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${projectInstance.name}</jmesa:htmlColumn>
                     <jmesa:htmlColumn property="dateCreated" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor"><tds:convertDateTime date="${projectInstance?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></jmesa:htmlColumn>
                     <jmesa:htmlColumn property="lastUpdated" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor"><tds:convertDateTime date="${projectInstance?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></jmesa:htmlColumn>
                     <jmesa:htmlColumn property="comment" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${projectInstance.comment}</jmesa:htmlColumn>
                 </jmesa:htmlRow>
             </jmesa:htmlTable>
         </jmesa:tableFacade>
     </form>
</div>
</div>
<script>
	currentMenuId = "#projectMenu";
	$("#projectMenuId a").css('background-color','#003366')
	
</script>
</body>
</html>
