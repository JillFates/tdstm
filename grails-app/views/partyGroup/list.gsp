<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Company List</title>
        <script language="javascript" src="${resource(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
		<link rel="stylesheet" type="text/css" href="${resource(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
		<script type="text/javascript">
		function onInvokeAction(id) {
		    setExportToLimit(id, '');
		    createHiddenInputFieldsForLimitAndSubmit(id);
		}
		</script>
    </head>
    <body>

    	
        <div class="body">
            <h1>Company List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="buttons">
                <g:form>
                    <span class="button"><g:actionSubmit class="create" value="New" action="create" /></span>
                </g:form>
            </div>
            <div>
	            <form name="companyForm">
			         <jmesa:tableFacade id="tag" items="${partyGroupList}" maxRows="100" stateAttr="restore" var="partyGroupInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
			             <jmesa:htmlTable style=" border-collapse: separate">
			                 <jmesa:htmlRow highlighter="true">
			                     <jmesa:htmlColumn property="name" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									<g:link action="show" id="${partyGroupInstance.id}" title="Show  '${partyGroupInstance.name}'">${partyGroupInstance.name}</g:link>
								 </jmesa:htmlColumn>
			                     <jmesa:htmlColumn property="dateCreated" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor"><tds:convertDateTime date="${partyGroupInstance?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></jmesa:htmlColumn>
			                     <jmesa:htmlColumn property="lastUpdated" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor"><tds:convertDateTime date="${partyGroupInstance?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></jmesa:htmlColumn>
			                 </jmesa:htmlRow>
			             </jmesa:htmlTable>
			         </jmesa:tableFacade>
			     </form>
		  	</div>
        </div>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
    </body>
</html>
