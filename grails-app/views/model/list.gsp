<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="projectHeader" />
        <title>Model List</title>
        <script language="javascript" src="${resource(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>

        <link rel="stylesheet" type="text/css" href="${resource(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
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
            <h1>Model List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div>
            <div class="buttons"> 
            <g:form action="create" method="post">
				<span class="button"><g:actionSubmit class="save" action="Create" value="Create Model" /></span>
			</g:form>
			</div>
            <form name="modelForm" action="list">
                <jmesa:tableFacade id="tag" items="${modelsList}" maxRows="25" stateAttr="restore" var="modelInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
                    <jmesa:htmlTable style=" border-collapse: separate">
                        <jmesa:htmlRow highlighter="true">
                            <jmesa:htmlColumn property="modelName" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
								<g:link action="show" id="${modelInstance.id}">${modelInstance.modelName}</g:link>
							 </jmesa:htmlColumn>
							 <jmesa:htmlColumn property="manufacturerName" title="Manufacturer" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${modelInstance.manufacturer}</jmesa:htmlColumn>
    	                     <jmesa:htmlColumn property="description" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${modelInstance.description}</jmesa:htmlColumn>
        	                 <jmesa:htmlColumn property="assetType" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${modelInstance.assetType}</jmesa:htmlColumn>
            	             <jmesa:htmlColumn property="powerUse" title="Power" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${modelInstance.powerUse}</jmesa:htmlColumn>
                	         <jmesa:htmlColumn width="50px" property="noOfConnectors" sortable="true" filterable="true" title="No Of Connectors">${modelInstance.noOfConnectors}</jmesa:htmlColumn>
                             <jmesa:htmlColumn width="50px" property="assetsCount" sortable="true" filterable="true" title="Assets">${modelInstance.assetsCount}</jmesa:htmlColumn>
                             <jmesa:htmlColumn width="50px" property="sourceTDSVersion" sortable="true" filterable="true" title="Version">${modelInstance.sourceTDSVersion}</jmesa:htmlColumn>
                             <jmesa:htmlColumn width="50px" property="source" sortable="true" filterable="true" title="Source TDS">${modelInstance.source}</jmesa:htmlColumn>
                             <jmesa:htmlColumn width="50px" property="modelStatus" sortable="true" filterable="true" title="Model Status">${modelInstance.modelStatus}</jmesa:htmlColumn>
                        </jmesa:htmlRow>
                    </jmesa:htmlTable>
                </jmesa:tableFacade>
            </form>
            </div>
            <div><span>Note : Power displayed in watts.</span></div>
        </div>
<script>
	currentMenuId = "#adminMenu";
	$("#adminMenuId a").css('background-color','#003366')
</script>
    </body>
</html>
