

<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta name="layout" content="projectHeader" />
  <title>MoveBundle List</title>
<script language="javascript" src="${createLinkTo(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
<link rel="stylesheet" type="text/css" href="${createLinkTo(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
<script type="text/javascript">
function onInvokeAction(id) {
    setExportToLimit(id, '');
    createHiddenInputFieldsForLimitAndSubmit(id);
}
</script>
</head>
<body>

<div class="body">
  <h1>MoveBundle List</h1>
  <g:if test="${flash.message}">
    <div class="message">${flash.message}</div>
  </g:if>
  <div>
    <form name="projectForm" action="list">
         <jmesa:tableFacade id="tag" items="${moveBundleInstanceList}" maxRows="25" stateAttr="restore" var="moveBundle" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
             <jmesa:htmlTable style=" border-collapse: separate">
                 <jmesa:htmlRow highlighter="true">
                     <jmesa:htmlColumn property="name" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
						<g:link params="[projectId:projectId]" action="show" id="${moveBundle?.id}">${moveBundle?.name}</g:link>
					 </jmesa:htmlColumn>
					 <jmesa:htmlColumn property="description" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${moveBundle?.description}</jmesa:htmlColumn>
					 <jmesa:htmlColumn property="operationalOrder" title="Order" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${moveBundle?.operationalOrder}</jmesa:htmlColumn>
					 <jmesa:htmlColumn property="assetQty" title="Asset Qty" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">${moveBundle?.assetQty}</jmesa:htmlColumn>
                     <jmesa:htmlColumn property="dateCreated" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor"><tds:convertDateTime date="${moveBundle?.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></jmesa:htmlColumn>
                     <jmesa:htmlColumn property="lastUpdated" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor"><tds:convertDateTime date="${moveBundle?.lastUpdated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></jmesa:htmlColumn>
                 </jmesa:htmlRow>
             </jmesa:htmlTable>
         </jmesa:tableFacade>
     </form>
  </div>
  <div class="buttons"> 
  <g:form>
	<input type="hidden" id="projectId" name="projectId" value="${projectId}"/>
  	<span class="button"><g:actionSubmit	class="save" action="Create" value="Create" /></span>
  </g:form>
  </div>
</div>
</body>
</html>
