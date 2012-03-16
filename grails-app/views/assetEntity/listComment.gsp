<%@ page contentType="text/html;charset=ISO-8859-1" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
<meta name="layout" content="projectHeader" />
        <title>Comment List</title>
         <g:javascript src="asset.tranman.js" />
          <g:javascript src="entity.crud.js" />
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
        $(document).ready(function() {
        	$('#assetMenu').show();
        	$("#commentsListDialog").dialog({ autoOpen: false })
 	        $("#createCommentDialog").dialog({ autoOpen: false })
 	        $("#showCommentDialog").dialog({ autoOpen: false })
 	        $("#editCommentDialog").dialog({ autoOpen: false })
        });
        </script>
</head>
<body>
  <div class="body">
   <div class="body">
            <h1>Comment List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div>
            <div>
            <form name="commentForm" id="commentForm" action="listComment">
               <span > <b>Show Resolved : </b>
               <g:if test="${checked=='on'}">
                   <input type="checkBox" name="resolvedBox" id="myResolvedBox"  checked="checked"  onclick="$('#commentForm').submit();"  />
               </g:if>
               <g:else>
                    <input type="checkBox" name="resolvedBox" id="myResolvedBox" onclick="$('#commentForm').submit();"  />
               </g:else>
               </span>
               <br></br>
                <jmesa:tableFacade id="tag" items="${assetCommentList}" maxRows="25" stateAttr="restore" var="commentInstance" autoFilterAndSort="true" maxRowsIncrements="25,50,100" >
                    <jmesa:htmlTable style=" border-collapse: separate" editable="true">
		       	 		<jmesa:htmlRow highlighter="true" style="cursor: pointer;">
				        	<jmesa:htmlColumn property="id" sortable="false" filterable="false" cellEditor="org.jmesa.view.editor.BasicCellEditor" title="Actions" nowrap>
				        		<a href="javascript:showAssetComment(${commentInstance?.id}, 'edit')"><img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}" border="0px"/></a>
							</jmesa:htmlColumn>
                            <jmesa:htmlColumn property="comment" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
								<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance.comment?.size() > 40 ? commentInstance.comment?.substring(0,40)+'..' : commentInstance.comment}</span>
							 </jmesa:htmlColumn>
							 <jmesa:htmlColumn property="dateCreated" title="Date Created" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor" nowrap>
								<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');"><tds:convertDateTime date="${commentInstance.dateCreated}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"/></span>
							 </jmesa:htmlColumn>
							 <jmesa:htmlColumn property="commentType" title="Type" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
							 	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance.commentType}</span>
							 </jmesa:htmlColumn><%--
    	                     <jmesa:htmlColumn property="mustVerify" sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
    	                     	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show')"><g:if test ="${commentInstance.mustVerify == 1}"></g:if><g:else><g:checkBox name="myVerifyBox" value="${true}" disabled="true"/></g:else></span>
    	                     </jmesa:htmlColumn>
        	                 --%><jmesa:htmlColumn property="assetEntity" title="AssetName"sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
        	                 	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance.assetEntity?.assetName}</span>
        	                 </jmesa:htmlColumn>
            	             <jmesa:htmlColumn property="isResolved"  sortable="true" filterable="true" cellEditor="org.jmesa.view.editor.BasicCellEditor">
            	             	<span style="align:center;" onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');"><g:if test ="${commentInstance.commentType =='issue' && commentInstance.isResolved == 1}"><g:checkBox name="myCheckbox" value="${true}" disabled="true"/></g:if><g:else>&nbsp</g:else></span>
            	             </jmesa:htmlColumn>
                	         <jmesa:htmlColumn width="50px" property="assetEntity.assetType" sortable="true" filterable="true" title="AssetType">
                	         	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance.assetEntity.assetType}</span>
                	         </jmesa:htmlColumn>
                             <jmesa:htmlColumn width="50px" property="category" sortable="true" filterable="true" title="category">
                             	<span onclick="javascript:showAssetComment(${commentInstance?.id}, 'show');">${commentInstance.category}</span>
                             </jmesa:htmlColumn>
                            </jmesa:htmlRow>
                    </jmesa:htmlTable>
                </jmesa:tableFacade>
            </form>
            </div>
  </div>
 <g:render template="commentCrud"/> 
 <script>
	currentMenuId = "#assetMenu";
	$("#assetMenuId a").css('background-color','#003366')	
 </script>
 
</html>