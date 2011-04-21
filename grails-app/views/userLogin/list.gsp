

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="companyHeader" />
        <title>UserLogin List</title>
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
            <h1>UserLogin List</h1>
            <div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
            <span class="menuButton"><g:link class="create" action="create" params="[companyId:companyId]">New UserLogin</g:link></span>
        </div>
        <br/>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div>
            	<form name="userForm">
			         <jmesa:tableFacade id="tag" items="${userLoginInstanceList}" maxRows="25" stateAttr="restore" var="userLoginInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100">
			             <jmesa:htmlTable style=" border-collapse: separate">
			                 <jmesa:htmlRow highlighter="true">
			                 	 <jmesa:htmlColumn property="username" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
		                         		<g:link action="show" id="${userLoginInstance.id}" params="[companyId:companyId]">${userLoginInstance.username}</g:link>
								 </jmesa:htmlColumn>
			                     <jmesa:htmlColumn property="personDetails" title="Person" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
									${fieldValue(bean:userLoginInstance, field:'person')}
								 </jmesa:htmlColumn>
								 <jmesa:htmlColumn property="lastLogin" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor">
									<tds:convertDateTime date="${userLoginInstance?.lastLogin}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
  								 </jmesa:htmlColumn>
			                     <jmesa:htmlColumn property="createdDate" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor">
			                     	<tds:convertDateTime date="${userLoginInstance?.createdDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
								 </jmesa:htmlColumn>
			                     <jmesa:htmlColumn property="expiryDate" sortable="true" filterable="true" pattern="MM/dd/yyyy hh:mm a" cellEditor="org.jmesa.view.editor.DateCellEditor">
			                     	<tds:convertDateTime date="${userLoginInstance?.expiryDate}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/>
			                     </jmesa:htmlColumn>
			                 </jmesa:htmlRow>
			             </jmesa:htmlTable>
			         </jmesa:tableFacade>
			     </form>
            </div>
        </div>
    </body>
</html>
