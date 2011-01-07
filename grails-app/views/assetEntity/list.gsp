
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Asset Entity List</title>


<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />

<g:javascript src="asset.tranman.js" />

<script type="text/javascript">
	      $(document).ready(function() {
	        $("#showDialog").dialog({ autoOpen: false })
	        $("#editDialog").dialog({ autoOpen: false })
	        $("#createDialog").dialog({ autoOpen: false })
	        $("#commentsListDialog").dialog({ autoOpen: false })
	        $("#createCommentDialog").dialog({ autoOpen: false })
	        $("#showCommentDialog").dialog({ autoOpen: false })
	        $("#editCommentDialog").dialog({ autoOpen: false })
	        $("#filterPane").draggable()
})
</script>
<script type="text/javascript">	
	   		
	    	function createDialog(){
		      $("#createDialog").dialog('option', 'width', 950)
		      $("#createDialog").dialog('option', 'position', ['center','top']);
		      if($('#createFormTbodyId')){
			      $('#createFormTbodyId').css('display','none');
			      $('#attributeSetId').val('');
		      }
		      $("#createDialog").dialog("open")
		      $("#editDialog").dialog("close")
		      $("#showDialog").dialog("close")
		      $('#createCommentDialog').dialog('close');
		      $('#commentsListDialog').dialog('close');
		      $('#editCommentDialog').dialog('close');
		      $('#showCommentDialog').dialog('close');
		      $("#attributeSetId").val(1)
		      ${remoteFunction(action:'getAttributes', params:'\'attribSet=\' + $("#attributeSetId").val() ', onComplete:'generateCreateForm(e)')}
		    }
		    
		    function editAssetDialog() {
		      $("#showDialog").dialog("close")
		      $("#editDialog").dialog('option', 'width', 'auto')
		      $("#editDialog").dialog('option', 'position', ['center','top']);
		      $("#editDialog").dialog("open")
		
		    }
		    
		    function showEditAsset(e) {
		      var assetEntityAttributes = eval('(' + e.responseText + ')')
			  if (assetEntityAttributes != "") {
			  		var trObj = $("#assetRow_"+assetEntityAttributes[0].id)
			  		trObj.css('background','#65a342');
		    		var length = assetEntityAttributes.length
				      	for (var i=0; i < length; i ++) {
				      		var attribute = assetEntityAttributes[i]
				      		var tdId = $("#"+attribute.attributeCode+'_'+attribute.id)
				      		if(tdId != null ){
				      				tdId.html( attribute.value )
				      		}
				      	}
				  $("#editDialog").dialog("close")
				} else {
					alert("Asset Entity is not updated")
				}
      		}
		    
      		function validateAssetEntity(formname) {
      			var attributeSet = $("#attributeSetId").val();
      			if(attributeSet || formname == 'editForm'){
      				var assetName = document.forms[formname].assetName.value;
      				var assetTag = document.forms[formname].assetTag.value;
	      			if( !assetName ){
	      				alert(" Please Enter Asset Name. ");
	      				return false;
	      			} else if( !assetTag ){
	      				alert(" Please Enter Asset Tag. ");
	      				return false;
	      			} else {
	      				return true;
	      			}
      			} else {
      				alert(" Please select Attribute Set. ");
	      			return false;
      			}
      		}
      		function showAssetDetails( assetId ){
      			${remoteFunction(action:'editShow', params:'\'id=\'+assetId', before:'document.showForm.id.value = assetId;document.editForm.id.value = assetId;', onComplete:"showAssetDialog(e , 'show')")}
      		}
	    </script>
<filterpane:includes />
</head>
<body>

<div class="body">
<h1>Asset Entity List</h1>
<g:if test="${flash.message}">
	<div class="message">${flash.message}</div>
</g:if>
<input type="hidden" id="role" value="role"/>
<div class="list">
<table id="assetEntityTable">
	<thead>
		<tr>

			<th>Actions</th>			

			<g:sortableColumn property="application" title="Application" params="${filterParams}"/>
			
			<g:sortableColumn property="assetName" title="Asset Name" params="${filterParams}"/>
			
			<g:sortableColumn property="model" title="Model" params="${filterParams}"/>

			<g:sortableColumn property="sourceLocation" title="Source Location" params="${filterParams}"/>

			<g:sortableColumn property="sourceRack" title="Source Rack/Cab" params="${filterParams}"/>

			<g:sortableColumn property="targetLocation"	title="Target Location" params="${filterParams}"/>
			
			<g:sortableColumn property="targetRack"	title="Target Rack/Cab" params="${filterParams}"/>
			

			<g:sortableColumn property="assetType" title="Asset Type" params="${filterParams}"/>

			<g:sortableColumn property="assetTag" title="Asset Tag" params="${filterParams}"/>

			<g:sortableColumn property="serialNumber" title="Serial #" params="${filterParams}"/>
			
			<g:sortableColumn property="moveBundle" title="Move Bundle &nbsp;" params="${filterParams}"/>


		</tr>
	</thead>
	<tbody>
		<g:each in="${assetEntityInstanceList}" status="i"
			var="assetEntityInstance">
			<tr id="assetRow_${assetEntityInstance.id}" 
				onmouseover="style.backgroundColor='#87CEEE';"
				onmouseout="style.backgroundColor='white';" >

				<td><g:remoteLink controller="assetEntity" action="editShow" id="${assetEntityInstance.id}" before="document.showForm.id.value = ${assetEntityInstance.id};document.editForm.id.value = ${assetEntityInstance.id};" onComplete="showAssetDialog( e , 'edit');">
					<img src="${createLinkTo(dir:'images/skin',file:'database_edit.png')}" border="0px"/>
				</g:remoteLink>
				<span id="icon_${assetEntityInstance.id}">
				<g:if test="${AssetComment.find('from AssetComment where assetEntity = ? and commentType = ? and isResolved = ?',[assetEntityInstance,'issue',0])}">
					<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntityInstance.id}" before="setAssetId('${assetEntityInstance.id}');" onComplete="listCommentsDialog(e,'never');">
						<img src="${createLinkTo(dir:'i',file:'db_table_red.png')}" border="0px"/>
					</g:remoteLink>
				</g:if>
				<g:elseif test="${AssetComment.findByAssetEntity(assetEntityInstance)}">
				<g:remoteLink controller="assetEntity" action="listComments" id="${assetEntityInstance.id}" before="setAssetId('${assetEntityInstance.id}');" onComplete="listCommentsDialog(e,'never');">
					<img src="${createLinkTo(dir:'i',file:'db_table_bold.png')}" border="0px"/>
				</g:remoteLink>
				</g:elseif>
				<g:else>
				<a href="#" onclick="$('#createAssetCommentId').val(${assetEntityInstance.id});$('#statusId').val('new');$('#createCommentDialog').dialog('option', 'width', 'auto');$('#createCommentDialog').dialog('open');$('#commentsListDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();">
					<img src="${createLinkTo(dir:'i',file:'db_table_light.png')}" border="0px"/>
				</a>
				</g:else></span>
				</td>
				
				<td id="application_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )"> ${fieldValue(bean:assetEntityInstance, field:'application')} </td>

				<td><a href="#" id="assetName_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${fieldValue(bean:assetEntityInstance, field:'assetName')}</a> </td>

				<td id="model_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${fieldValue(bean:assetEntityInstance, field:'model')}</td>

				<td id="sourceLocation_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${fieldValue(bean:assetEntityInstance, field:'sourceLocation')}</td>

				<td id="sourceRack_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${fieldValue(bean:assetEntityInstance, field:'sourceRack')}</td>

				<td id="targetLocation_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${fieldValue(bean:assetEntityInstance, field:'targetLocation')}</td>
				
				<td id="targetRack_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${fieldValue(bean:assetEntityInstance, field:'targetRack')}</td>

				<td id="assetType_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${fieldValue(bean:assetEntityInstance, field:'assetType')}</td>

				<td id="assetTag_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${fieldValue(bean:assetEntityInstance, field:'assetTag')}</td>

				<td id="serialNumber_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${fieldValue(bean:assetEntityInstance, field:'serialNumber')}</td>
				
				<td id="moveBundle_${assetEntityInstance.id}" onclick="showAssetDetails( ${assetEntityInstance.id} )">${fieldValue(bean:assetEntityInstance, field:'moveBundle')}</td>

			</tr>
		</g:each>
	</tbody>
</table>

</div>

<div class="paginateButtons">
<g:form name="paginateRows" action="list">
<table>
<tr>
<td style="width: 770px;">
<g:paginate total="${assetEntityCount == null ? AssetEntity.findAll('from AssetEntity where project = '+projectId).size() :  assetEntityCount }" params="${filterParams}"/>
<filterpane:filterButton textKey="fp.tag.filterButton.text" appliedTextKey="fp.tag.filterButton.appliedText" text="Filter Me" appliedText="Change Filter" />
<filterpane:isNotFiltered>Pure and Unfiltered!</filterpane:isNotFiltered>
<filterpane:isFiltered>Filter Applied!</filterpane:isFiltered>
</td>
<td >
Rows per Page:&nbsp;<g:select  from="[25,50,100,200]" id="rowVal" name="rowVal" value="${maxVal}" onchange="document.filterForm.rowVal.value = this.value;document.filterForm.submit();"></g:select></td>
</table>
</g:form>
</div>
<filterpane:filterPane domainBean="AssetEntity"  excludeProperties="sourceRackPosition,targetRackPosition,railType,ipAddress,hinfo,newOrOld,remoteMgmtPort,truck,priority,cart,shelf,dateCreated,project.name" />
<div class="buttons"><g:form>
	<span class="button"><input type="button"
		value="New Asset Entity" class="create" onClick="createDialog()" /></span>
</g:form></div>
</div>

<div id="createDialog" title="Create Asset Entity" style="display: none;">
<g:form action="save" method="post" name="createForm" >

	<div class="dialog" id="createDiv" >

		<table style="border: 0px;">
			<tr class="prop">
				<td valign="top" class="name" ><label for="attributeSet">Attribute Set:</label><span style="padding-left: 46px;"><g:select optionKey="id" from="${com.tdssrc.eav.EavAttributeSet.list()}" id="attributeSetId" name="attributeSet.id" value="${assetEntityInstance?.attributeSet?.id}" noSelection="['':'select']" 
				 onchange="${remoteFunction(action:'getAttributes', params:'\'attribSet=\' + this.value ', onComplete:'generateCreateForm(e)')}"></g:select></span> </td>
			</tr>
		</table>
		<table id="createFormTbodyId"></table>

	</div>
	
	<div class="buttons"><input type="hidden" name="projectId"
		value="${projectId }" /> <span class="button"><input
		class="save" type="submit" value="Create"
		onclick="return validateAssetEntity('createForm');" /></span></div>
</g:form></div>
<div id="showDialog" title="Show Asset Entity" style="display: none;">
<g:form action="save" method="post" name="showForm">
	<div class="dialog" id="showDiv">
	
	</div>
	<div class="buttons">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	 <span class="button"><input
		type="button" class="edit" value="Edit"
		onClick="return editAssetDialog()" /></span> <span class="button"><g:actionSubmit 
		class="delete" onclick="return confirm('Delete Asset, are you sure?');"
		value="Delete" /></span>
		<span class="button"><g:actionSubmit action="remove"
		class="delete"  onclick="return confirm('Remove Asset from project, are you sure?');"
		value="Remove From Project" /></span>
		</div>
</g:form></div>

<div id="editDialog" title="Edit Asset Entity" style="display: none;">
<g:form method="post" name="editForm">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<div class="dialog" id="editDiv">
	
	</div>
	<div class="buttons"><span class="button">
	<input type="button" class="save" value="Update Asset Entity" onClick="if(validateAssetEntity('editForm'))  ${remoteFunction(action:'getAssetAttributes', params:'\'assetId=\' + document.editForm.id.value ', onComplete:'callUpdateDialog(e)')}" />
	</span> <span class="button"><input type="button"
		class="delete" onclick="return editDialogDeleteRemove('delete')"
		value="Delete" /></span>
		<span class="button"><input type="button"
		class="delete"  onclick="return editDialogDeleteRemove('remove');"
		value="Remove From Project" /></span>
		</div>
</g:form></div>

<div id="commentsListDialog" title="Show Asset Comments" style="display: none;">
<br/>
	<div class="list">
		<table id="listCommentsTable">
		<thead>
	        <tr >
	                        
	          <th nowrap>Action</th>
	          
	          <th nowrap>Comment</th>
	                        
	          <th nowrap>Comment Type</th>
	          
	          <th nowrap>Resolved</th>
	                        
	          <th nowrap>Must Verify</th>
	          
	          <th nowrap>Category</th>  
	          
	          <th nowrap>Comment Code</th>                     
	                   	    
	        </tr>
	    </thead>
		<tbody id="listCommentsTbodyId">
		
		</tbody>
		</table>
	</div>
	<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
		<span class="menuButton"><a class="create" href="#" onclick="$('#statusId').val('');$('#createResolveDiv').css('display','none');$('#createCommentDialog').dialog('option', 'width', 'auto');$('#createCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();" >New Comment</a></span>
	</div>
</div>
<div id="createCommentDialog" title="Create Asset Comment"
	style="display: none;"><input type="hidden" name="assetEntity.id"
	id="createAssetCommentId" value=""/> <input type="hidden"
	name="status" id="statusId" value=""/> <g:form
	action="saveComment" method="post" name="createCommentForm">
	<input type="hidden" name="category" value="general"/>
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<div>
	<table id="createCommentTable" style="border: 0px;">
		
			<tr class="prop" >
				<td valign="top" class="name"><label for="commentType">Comment
				Type:</label></td>
				<td valign="top" style="width: 20%;" ><g:select id="commentType"
					name="commentType"
					from="${AssetComment.constraints.commentType.inList}" value=""
					noSelection="['':'please select']" onChange="commentChange('#createResolveDiv','createCommentForm')"></g:select>&nbsp;&nbsp;&nbsp;&nbsp;			
				
				<input type="checkbox"
					id="mustVerifyEdit" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
					<label for="mustVerify">Must
				Verify</label>
				</td>				
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value">
				<textarea cols="80" rows="5" id="comment" name="comment" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea>
				</td>
			</tr>
		
	</table>
	</div>
	<div id="createResolveDiv" style="display: none;">
		<table id="createResolveTable" style="border: 0px" >
            <tr class="prop">
            	<td valign="top" class="name">
                <label for="isResolved">Resolved:</label>
                </td>
                <td valign="top" class="value">
                <input type="checkbox" id="isResolved" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
                </td>
            </tr>
          
            <tr class="prop">
				<td valign="top" class="name">
                <label for="resolution">Resolution:</label>
                </td>
				<td valign="top" class="value">
                <textarea cols="80" rows="5" id="resolution" name="resolution" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea>
                </td>
            </tr> 
                
            </table>
            </div>
		
	</div>
	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Create"
		onclick="resolveValidate('createCommentForm','createAssetCommentId');" /></span></div>
</g:form></div>
<div id="showCommentDialog" title="Show Asset Comment"
	style="display: none;">
<div class="dialog" style="border: 1px solid #5F9FCF"><input name="id" value="" id="commentId"
	type="hidden"/>
	<div>
<table id="showCommentTable" style="border: 0px;">
	
	<tr>
	<td valign="top" class="name"><label for="dateCreated">Created
			At:</label></td>
			<td valign="top" class="value" id="dateCreatedId" ></td>
	</tr>
		<tr>
	<td valign="top" class="name"><label for="createdBy">Created
			By:</label></td>
			<td valign="top" class="value" id="createdById" ></td>
	</tr>
		
		<tr class="prop">
			<td valign="top" class="name"><label for="commentType">Comment
			Type:</label></td>
			<td valign="top" class="value" id="commentTypeTdId" ></td>
		</tr>
		
		<tr>
	<td valign="top" class="name"><label for="category">Category:
			</label></td>
			<td valign="top" class="value" id="categoryTdId" ></td>
	</tr>
	
	<tr class="prop">
	<td valign="top" class="name"><label for="commentCode">comment
			Code:</label></td>
			<td valign="top" class="value" id="commentCodeTdId" ></td>
	</tr>
	
		<tr class="prop">
			<td valign="top" class="name"><label for="mustVerify">Must
			Verify:</label></td>
			<td valign="top" class="value" id="verifyTdId"><input
				type="checkbox" id="mustVerifyShowId" name="mustVerify" value="0"
				disabled="disabled" /></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="comment">Comment:</label>
			</td>
			<td valign="top" class="value" ><textarea cols="80" rows="5"
					id="commentTdId" readonly="readonly"></textarea> </td>
		</tr>
		</table>
		</div>
		<div id="showResolveDiv" style="display: none;">
		<table id="showResolveTable" style="border: 0px">
		<tr class="prop">
			<td valign="top" class="name"><label for="isResolved">Is
			Resolved:</label></td>
			<td valign="top" class="value" id="resolveTdId"><input
				type="checkbox" id="isResolvedId" name="isResolved" value="0"
				disabled="disabled" /></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label>
			</td>
			<td valign="top" class="value" ><textarea cols="80" rows="5"
					id="resolutionId" readonly="readonly"></textarea> </td>
		</tr>
			<tr>
	<td valign="top" class="name"><label for="dateResolved">Resolved
			At:</label></td>
			<td valign="top" class="value" id="dateResolvedId" ></td>
	</tr>
		<tr>
	<td valign="top" class="name"><label for="resolvedBy">Resolved
			By:</label></td>
			<td valign="top" class="value" id="resolvedById" ></td>
	</tr>
	
</table>
</div>
<div class="buttons"><span class="button"> <input
	class="edit" type="button" value="Edit"
	onclick="commentChangeEdit('editResolveDiv','editCommentForm');$('#editCommentDialog').dialog('option', 'width', 'auto');$('#editCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('open');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close')" />
</span> <span class="button"> <input class="delete" type="button"
	value="Delete"
	onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment', params:'\'id=\' + $(\'#commentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'never\')')}" />
</span></div>
</div></div>
<div id="editCommentDialog" title="Edit Asset Comment"
	style="display: none;"><g:form action="updateComment"
	method="post" name="editCommentForm">
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<input type="hidden" name="id" id="updateCommentId" value=""/>
	<div>
	<table id="updateCommentTable" style="border: 0px;">
		
		
			<tr>
	<td valign="top" class="name"><label for="dateCreated">Created
			At:</label></td>
			<td valign="top" class="value" id="dateCreatedEditId"  />
	</tr>
		<tr>
	<td valign="top" class="name"><label for="createdBy">Created
			By:</label></td>
			<td valign="top" class="value" id="createdByEditId" />
	</tr>
			<tr class="prop" >
				<td valign="top" class="name"><label for="commentType">Comment
				Type:</label></td>
				<td valign="top" style="width: 20%;" >
				<jsec:hasAnyRole in="['ADMIN','PROJ_MGR']">
				<g:select id="commentTypeEditId"
					name="commentType"
					from="${AssetComment.constraints.commentType.inList}" value=""
					 onChange="commentChange('#editResolveDiv','editCommentForm')"></g:select>&nbsp;&nbsp;&nbsp;&nbsp;			
				</jsec:hasAnyRole>
				<jsec:lacksAllRoles in="['ADMIN','PROJ_MGR']">
				
				<input type="text" id="commentTypeEditId" name="commentType" readonly style="border: 0;"/>&nbsp;&nbsp;&nbsp;&nbsp;
				</jsec:lacksAllRoles>				
				<input type="checkbox" id="mustVerifyEditId" name="mustVerify" value="0"
					onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
					<label for="mustVerify">Must
				Verify</label>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="category">Category:</label>
				</td>
				<td valign="top" class="value" id="categoryEditId" ></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="commentCode">Comment Code:</label>
				</td>
				<td valign="top" class="value" id="commentCodeEditId" ></td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="commentEditId" name="comment" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea></td>
			</tr>
			</table>
			
			</div>
			<div id="editResolveDiv" style="display: none;">
		<table id="updateResolveTable" style="border: 0px;">
            <tr class="prop">
            	<td valign="top" class="name">
                <label for="isResolved">Resolved:</label>
                </td>
                <td valign="top" class="value">
                <input type="checkbox" id="isResolvedEditId" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
                </td>
            </tr>
          
            <tr class="prop">
				<td valign="top" class="name">
                <label for="resolution">Resolution:</label>
                </td>
				<td valign="top" class="value">
                <textarea cols="80" rows="5" id="resolutionEditId" name="resolution" onkeydown="textCounter(this.id,255)"  onkeyup="textCounter(this.id,255)"></textarea>
                </td>
            </tr> 
               <tr>
	<td valign="top" class="name"><label for="dateResolved">Resolved
			At:</label></td>
			<td valign="top" class="value" id="dateResolvedEditId" ></td>
	</tr>
		<tr>
	<td valign="top" class="name"><label for="resolvedBy">Resolved
			By:</label></td>
			<td valign="top" class="value" id="resolvedByEditId"  ></td>
	</tr>
            </table>
            </div>
		
		

	</div>

	<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Update"
		onclick="resolveValidate('editCommentForm','updateCommentId');" />
	</span> <span class="button"> <input class="delete" type="button"
		value="Delete"
		onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)${remoteFunction(action:'deleteComment', params:'\'id=\' + $(\'#updateCommentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'never\')')}" />
	</span></div>
</g:form>
</div>
<script type="text/javascript">
$('#assetMenu').show();
$('#reportsMenu').hide();
</script>
</body>
</html>
