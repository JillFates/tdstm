<%@page import="com.tds.asset.AssetComment"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<meta name="layout" content="projectHeader" />
        <title>Asset Comment</title>
         <g:javascript src="asset.tranman.js" />
          <g:javascript src="entity.crud.js" />
        <script language="javascript" src="${createLinkTo(dir:"plugins/jmesa-0.8/js",file:"jmesa.js")}"></script>
        <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:"plugins/jmesa-0.8/css",file:"jmesa.css")}" />
        <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.datepicker.css')}" />
        <link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'tds.css')}" />
        <script type="text/javascript">
        function onInvokeAction(id) {
            setExportToLimit(id, '');
            createHiddenInputFieldsForLimitAndSubmit(id);
        }
        function onInvokeExportAction(id) {
            var parameterString = createParameterStringForLimit(id);
            location.href = '../list?' + parameterString;
        }
        $(document).ready(function() {
        	$('#assetMenu').show();
        	$("#commentsListDialog").dialog({ autoOpen: false })
 	        $("#createCommentDialog").dialog({ autoOpen: false })
 	        $("#showCommentDialog").dialog({ autoOpen: false })
 	        $("#editCommentDialog").dialog({ autoOpen: false })
 	        $("#showEntityView").dialog({ autoOpen: false })
			$("#editEntityView").dialog({ autoOpen: false })
			$("#createEntityView").dialog({ autoOpen: false })
	    	currentMenuId = "#assetMenu";
	    	$("#assetMenuId a").css('background-color','#003366')
	    	$(".span_ready").parent().addClass("task_ready")
	    	$(".span_hold").parent().addClass("task_hold")
	    	$(".span_started").parent().addClass("task_started")
	    	$(".span_pending").parent().addClass("task_pending")
	    	$(".span_planned").parent().addClass("task_planned")
	    	$(".span_completed").parent().addClass("task_completed")
	    	$(".span_na").parent().addClass("task_na")
        });
        </script>
</head>
<body>
	<div class="body">
		<div class="body">
			<h1>Asset Comment</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div>
			<div>
			<input type="hidden" id="manageTaskId" value="manageTask"/>
			<form name="commentForm" id="commentForm" action="listComment">
				<jmesa:tableFacade id="tag" items="${assetCommentList}" maxRows="50" stateAttr="restore" var="commentInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100" >
					<jmesa:htmlTable style=" border-collapse: separate" editable="true">
						<jmesa:htmlRow highlighter="true" style="cursor: pointer;">
							<jmesa:htmlColumn property="id" sortable="false" filterable="false" cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Actions" nowrap>
				        		<a href="javascript:showAssetComment(${commentInstance?.id}, 'edit')"><img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="taskNumber" title="Task" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
								<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance.taskNumber ? commentInstance.taskNumber :''}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="description" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Description" nowrap>
								<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance.description?.size() > 40 ? commentInstance.description?.substring(0,40)+'..' : commentInstance.description}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="lastUpdated" title="Updated" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
								<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');"><tds:convertDate date="${commentInstance.lastUpdated}" format="MM/dd"  /></span>
							</jmesa:htmlColumn>
							<%--<jmesa:htmlColumn property="dueDate" title="Due" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
							 	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');"><tds:convertDate date="${commentInstance.dueDate}" format="MM/dd"/></span>
							</jmesa:htmlColumn>
							--%>
							<jmesa:htmlColumn property="commentType" title="Type" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
							 	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance.commentType}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="status" title="Status" sortable="true" filterable="true" width="100px" cellEditor="org.jmesa.view.editor.BasicCellEditor">
							 	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');" class="span_${commentInstance.status ? commentInstance.status.toLowerCase() : 'na'}">${commentInstance.status}</span>
							</jmesa:htmlColumn>
							<%--<jmesa:htmlColumn property="assignedToString" title="Assigned To" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
        	                 	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance.assignedTo}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn property="role" title="Role" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
        	                 	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance.role}</span>
							</jmesa:htmlColumn>
    	                     <jmesa:htmlColumn property="mustVerify" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
    	                     	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show')"><g:if test ="${commentInstance.mustVerify == 1}"></g:if><g:else><g:checkBox name="myVerifyBox" value="${true}" disabled="true"/></g:else></span>
    	                     </jmesa:htmlColumn>
        	                 --%><jmesa:htmlColumn property="assetName" title="Asset" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
        	                 	<span onclick="javascript:getEntityDetails('listComment', '${commentInstance.assetType}', '${commentInstance.assetEntityId}');">${commentInstance.assetName}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn width="50px" property="assetType" sortable="true" filterable="true" title="AssetType">
                	         	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance?.assetType}</span>
							</jmesa:htmlColumn>
							<jmesa:htmlColumn width="50px" property="category" sortable="true" filterable="true" title="category">
                             	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance.category}</span>
							</jmesa:htmlColumn>
						</jmesa:htmlRow>
					</jmesa:htmlTable>
				</jmesa:tableFacade>
			</form>
            </div>
            <div id="showEntityView" style="display: none;"></div>
			<div id="editEntityView" style="display: none;"></div>
			<div id="createEntityView" style="display: none;"></div>
  </div>
  
 <g:render template="commentCrud"/> 
 </div>
 </div>
 </body>
 
</html>