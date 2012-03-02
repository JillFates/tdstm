<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Move Event List</title>
        <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
		<script language="javascript" src="${createLinkTo(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
		<script type="text/javascript">
		function onInvokeAction(id) {
		    setExportToLimit(id, '');
		    createHiddenInputFieldsForLimitAndSubmit(id);
		}
		</script>
    </head>
    <body>
        <div class="body">
            <h1>Move Event List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div>
            	<form name="moveEventForm">
			         <jmesa:tableFacade id="tag" items="${moveEventInstanceList}" maxRows="25" stateAttr="restore" var="moveEventInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
			             <jmesa:htmlTable style=" border-collapse: separate">
			                 <jmesa:htmlRow highlighter="true">
			                 	 <jmesa:htmlColumn property="name" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
		                         		<g:link action="show" id="${moveEventInstance.id}">${moveEventInstance.name}</g:link>
								 </jmesa:htmlColumn>
			                     <jmesa:htmlColumn property="description" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									${moveEventInstance.description}
								 </jmesa:htmlColumn>
								 <jmesa:htmlColumn property="inProgress" title="Status" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									<g:message code="event.inProgress.${moveEventInstance?.inProgress}" />
								 </jmesa:htmlColumn>
								 <jmesa:htmlColumn property="calcMethod" title="Calculated Type" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									<g:message code="step.calcMethod.${moveEventInstance.calcMethod}" />
								 </jmesa:htmlColumn>
								 <jmesa:htmlColumn property="moveBundlesString" title="Move Bundles" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									${moveEventInstance.moveBundlesString}
								 </jmesa:htmlColumn>
			                 </jmesa:htmlRow>
			             </jmesa:htmlTable>
			         </jmesa:tableFacade>
			     </form>
            </div>
            <div class="paginateButtons">
            <tds:hasPermission permission='MoveEventEditView'>
		     	<span class="menuButton"><g:link class="create" action="create">Create New</g:link></span>
		    </tds:hasPermission>
            </div>
        </div>
<script>
	currentMenuId = "#eventMenu";
	$("#eventMenuId a").css('background-color','#003366')
</script>
    </body>
</html>
