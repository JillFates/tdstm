<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="companyHeader" />
        <title>Model List</title>
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
            <h1>Model List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div>
            <form name="modelForm" action="list">
                <jmesa:tableFacade id="tag" items="${modelsList}" maxRows="15" exportTypes="csv,excel" stateAttr="restore" var="modelInstance" autoFilterAndSort="true" >
                    <jmesa:htmlTable style=" border-collapse: separate">
                        <jmesa:htmlRow>
                            <jmesa:htmlColumn property="modelName" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
								<g:link action="show" id="${modelInstance.id}">${modelInstance.modelName}</g:link>
							 </jmesa:htmlColumn>
							 <jmesa:htmlColumn property="manufacturer.name" title="Manufacturer" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${modelInstance.manufacturer}</jmesa:htmlColumn>
                            <jmesa:htmlColumn property="description" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${modelInstance.description}</jmesa:htmlColumn>
                            <jmesa:htmlColumn property="assetType" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${modelInstance.assetType}</jmesa:htmlColumn>
                            <jmesa:htmlColumn property="powerUse" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${modelInstance.powerUse}</jmesa:htmlColumn>
                            <jmesa:htmlColumn width="50px" property="id" sortable="false" filterable="false" title="No Of Connectors">${ModelConnector.countByModel(modelInstance)}</jmesa:htmlColumn>
                        </jmesa:htmlRow>
                    </jmesa:htmlTable>
                </jmesa:tableFacade>
            </form>
            </div>
            <div class="buttons"> 
            <g:form action="create" method="post">
				<span class="button"><g:actionSubmit class="save" action="Create" value="Create Model" /></span>
			</g:form>
			</div>
        </div>
    </body>
</html>
