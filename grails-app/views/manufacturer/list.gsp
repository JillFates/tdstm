

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="projectHeader" />
        <title>Manufacturer List</title>
        <script language="javascript" src="${createLinkTo(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
        <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
        <script type="text/javascript">
        function onInvokeAction(id) {
            setExportToLimit(id, '');
            createHiddenInputFieldsForLimitAndSubmit(id);
        }
        function onInvokeExportAction(id) {
            var parameterString = createParameterStringForLimit(id);
            location.href = '../list?' + parameterString;
        }
        </script>
    </head>
    <body>
        <div class="body">
            <h1>Manufacturer List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="buttons">
				<g:form>
			    	<span class="button"><g:actionSubmit class="create" value="New Manufacturer" action="create" /></span>
			    </g:form>
			</div>
            <div>
            	<form name="modelForm" action="list">
	                <jmesa:tableFacade id="tag" items="${manufacturersList}" maxRows="25" stateAttr="restore" var="manufacturer" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
	                    <jmesa:htmlTable style=" border-collapse: separate">
	                        <jmesa:htmlRow highlighter="true">
	                            <jmesa:htmlColumn property="name" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									<g:link action="show" id="${manufacturer.id}">${manufacturer.name}</g:link>
								 </jmesa:htmlColumn>
								 <jmesa:htmlColumn property="aka" title="AKA" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${manufacturer.aka}</jmesa:htmlColumn>
	                            <jmesa:htmlColumn property="description" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${manufacturer.description}</jmesa:htmlColumn>
	                            <jmesa:htmlColumn property="modelsCount" title="Models" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${manufacturer.modelsCount}</jmesa:htmlColumn>
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
