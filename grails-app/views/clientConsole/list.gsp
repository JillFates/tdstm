<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<meta name="layout" content="projectHeader" />
<title>Asset Tracking</title>

<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'jquery.autocomplete.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.accordion.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.resizable.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.slider.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'ui.tabs.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'dashboard.css')}" />
<link type="text/css" rel="stylesheet" href="${createLinkTo(dir:'css',file:'tableTheme.css')}" />

<g:javascript src="asset.tranman.js" />

<style type="text/css" media="screen">
	.tranCell {
		width: 9px !important;
	}
</style>

</head>
<body >
<div title="Change Status" id="changeStatusDialog"
	style="background-color: #808080;display: none;">
<form name="changeStatusForm"><input type="hidden" name="asset"
	id="asset" /> <input type="hidden" name="projectId" id="projectId"
	value="${projectId}" />
	<input type="hidden" name="moveBundle" id="moveBundle"
	value="${moveBundleInstance?.id}" />
<table style="border: 0px; width: 100%">
	<tr>
		<td width="40%"><strong>Change status for selected
		devices to:</strong></td>
		<td width="60%"></td>
	</tr>
	<tr>
		<td><select id="taskList" name="taskList" style="width: 250%"></select></td>
	</tr>
	<tr>
		<td>
		<textarea rows="2" cols="1"  title="Enter Note..." name="enterNote" id="enterNote" style="width: 200%"></textarea>
		</td>
	</tr>
	<tr>
		<td></td>
		<td style="text-align: right;"><input type="button" value="Save"
			onclick="var booConfirm = confirm('Are you sure?');if(booConfirm)submitAction()" /></td>
	</tr>
</table>
</form>
</div>
<div style="width:100%">
<g:form	name="listForm" action="list" method="post">
<div id="consoleHeader" style="width: 100%;">
	
	<input type="hidden" id="role" value="${role}"/>
	<input type="hidden" id="lastPoolTimeId" value="${lastPoolTime}"/>
	<input type="hidden" id="projectId" name="projectId" value="${projectId }" />
	<table style="border: 0px;">
		<tr>
			<td>
				<span style="padding-left: 10px;">
					<label for="moveEvent"><b>Event:</b></label>&nbsp;
					<select id="moveEventId" name="moveEvent" onchange="$('#moveBundleId').val('');document.listForm.submit()">
						<g:each status="i" in="${moveEventsList}" var="moveEventInstance">
							<option value="${moveEventInstance?.id}">${moveEventInstance?.name}</option>
						</g:each>
					</select>
				</span>
				<span>
					<label for="moveBundle"><b>Bundle:</b></label>&nbsp;
					<select id="moveBundleId" name="moveBundle" onchange="document.listForm.submit()" >
						<option value="all">All</option>	
						<g:each status="i" in="${moveBundleInstanceList}" var="moveBundleInstance">
							<option value="${moveBundleInstance?.id}">${moveBundleInstance?.name}</option>
						</g:each>
					</select>
				</span>
				<span>
					<input id="assetsInViewOffsetId" type="hidden" value="${params.offset}">
					<input id="sortById" type="hidden" value="${params.sort}">
					<input id="orderById" type="hidden" value="${params.order}">
					<label for="assetsInViewId"><b>Assets:</b></label>&nbsp;
					<select id="assetsInViewId" name="assetsInView" onchange="document.listForm.submit()" >
						<option value="all">All</option>
						<option value="25">25</option>
						<option value="50">50</option>
						<option value="100">100</option>
					</select>
				</span>
				<g:if test="${totalAssets > assetsInView }">
					<br/><br/>
					<span class="pmo_paginateButtons">
							<g:paginate total="${totalAssets}" params="${params }"/>
					</span>
				</g:if>				
			</td>
			<td style="padding: 0px;"><h1>PMO Asset Tracking</h1></td>
			<g:if test="${isAdmin || isProjManager || isManager}">
			<td style="text-align: left;width: 400px;">
				<span>
					<input type="button" name="bulkEdit" id="bulkEditId" value="Bulk Edit" class="bulkedit_inactive" onclick="performBulkEdit()"/>
				</span>
				&nbsp;&nbsp;
				<span style="display: none;" id="bulkTaskSpanId">
					<input type="button" name="bulkPending" id="bulkPendingId" value="Pending" onclick="changeAction('pending')"/>
					<input type="button" name="bulkDone" id="bulkDoneId" value="Done" onclick="changeAction('done')"/>
					<input type="button" name="bulkUndo" id="bulkUndoId" value="Undo" onclick="changeAction('void')"/>
					<input type="button" name="bulkNa" id="bulkNaId" value="N/A" onclick="changeAction('NA')"/>
					<input type="hidden" name="bulkAction" id="bulkActionId" value="done"/>
				</span>
			</td>
			</g:if>
			
			<td style="text-align: right;">
			<input type="hidden" name="last_update" value="${new Date()}"/>
			<input type="hidden" name="myForm" value="listForm"/>
			<input type="button" id="updateId"
				value="Update:" onclick="pageReload();"/> <select
				id="selectTimedId"
				onchange="${remoteFunction(action:'setTimePreference', params:'\'timer=\'+ this.value ' , onComplete:'setUpdateTime(e)') }">
				<option value="30000">30s</option>
				<option value="60000">1m</option>
				<option value="120000">2m</option>
				<option value="300000">5m</option>
				<option value="600000">10m</option>
				<option value="never">Never</option>
			</select></td>
		</tr>
	</table>

</div>
<g:if test="${browserTest}">
<div id="tableContainer" class="tableContainerIE" style="margin-left: 5px">
</g:if>
<g:else>
<div id="tableContainer" class="tableContainer" style="margin-left: 5px">
</g:else>

<table cellpadding="0" cellspacing="0" style="border:0px;">
	<thead>
		<g:form action="list">
		<tr>
			<th style="padding-top:35px;">
				<span>Actions</span><br />
				<g:if test="${isAdmin || isManager || isProjManager}"> 
					<input type="button" value="State..." onclick="changeState()" title="Change State" style="width: 80px;"/><br />
					<a href="#" onclick="selectAll()" ><u style="color:blue;">All</u></a>
				</g:if>
			</th>
			
			<th style="padding-top:35px;" >
				<tds:sortableLink id="column1Label" style="border:0px;" property="${columns?.column1.field}"  title="${columns?.column1.label}" params="['projectId':projectId, moveEvent:moveEventInstance?.id, 'moveBundle':moveBundleInstance?.id,'column1':column1Value,'column2':column2Value,'column3':column3Value,'column4':column4Value, 'assetsInView':assetsInView, 'offset':params.offset]"/>
				<span id="column1Select" style="display: none;"><g:select from="${com.tdssrc.eav.EavEntityAttribute.findAll()?.attribute}" optionKey="attributeCode" optionValue="frontendLabel" name="column1Attribute"  value="${columns?.column1.field}"></g:select></span>
				<span id="column1Edit"><img src="${createLinkTo(dir:'i',file:'db_edit.png')}" border="0px" onclick="changeLabelToSelect()"/></span>
				<span id="column1Save" style="display: none;"><input type="submit" value="Save"/>&nbsp;<input type="button" value="X" onclick="changeToLabel('1')"/></span> 
				<br />
					
				<select id="column1Id" name="column1" onchange="document.listForm.submit();" style="width: 120px;">
					<option value="" selected="selected">All</option>
					<g:each in="${column1List}" var="column1Obj">
						<option value="${column1Obj.key ? column1Obj.key : 'blank'}">${column1Obj.key ? column1Obj.key : 'blank'}&nbsp;(${column1Obj.value})</option>
					</g:each>
				</select>
			</th>
			<th style="padding-top:35px;">
				<tds:sortableLink id="column2Label" style="border:0px;" property="${columns?.column2.field}"  title="${columns?.column2.label}" params="['projectId':projectId,moveEvent:moveEventInstance?.id, 'moveBundle':moveBundleInstance?.id, 'column1':column1Value,'column2':column2Value,'column3':column3Value,'column4':column4Value, 'assetsInView':assetsInView, 'offset':params.offset]" />
				<span id="column2Select" style="display: none;"><g:select from="${com.tdssrc.eav.EavEntityAttribute.findAll()?.attribute}" optionKey="attributeCode" optionValue="frontendLabel" name="column2Attribute" value="${columns?.column2.field}"></g:select></span>
				<span id="column2Edit"><img src="${createLinkTo(dir:'i',file:'db_edit.png')}" border="0px" onclick="changeLabelToSelect()"/></span>
				<span id="column2Save" style="display: none;"><input type="submit" value="Save"/>&nbsp;<input type="button" value="X" onclick="changeToLabel('2')"/></span> 
				<br />

				<select id="column2Id" name="column2"	onchange="document.listForm.submit();" style="width: 120px;">
					<option value="" selected="selected">All</option>
					<g:each in="${column2List}" var="column2Obj">
						<option value="${column2Obj.key ? column2Obj.key : 'blank'}">${column2Obj.key ? column2Obj.key : 'blank'}&nbsp;(${column2Obj.value})</option>	
					</g:each>
				</select>
			</th>
			<th style="padding-top:35px;">
				<tds:sortableLink id="column3Label" style="border:0px;" property="${columns?.column3.field}"  title="${columns?.column3.label}" params="['projectId':projectId, moveEvent:moveEventInstance?.id, 'moveBundle':moveBundleInstance?.id, 'column1':column1Value,'column2':column2Value,'column3':column3Value,'column4':column4Value, 'assetsInView':assetsInView, 'offset':params.offset]"/>
				<span id="column3Select" style="display: none;"><g:select from="${com.tdssrc.eav.EavEntityAttribute.findAll()?.attribute}" optionKey="attributeCode" optionValue="frontendLabel" name="column3Attribute" value="${columns?.column3.field}" ></g:select></span>
				<span id="column3Edit"><img src="${createLinkTo(dir:'i',file:'db_edit.png')}" border="0px" onclick="changeLabelToSelect()"/></span>
				<span id="column3Save" style="display: none;"><input type="submit" value="Save"/>&nbsp;<input type="button" value="X" onclick="changeToLabel('3')"/></span> 
				<br />

				<select id="column3Id" name="column3" onchange="document.listForm.submit();" style="width: 120px;">
					<option value="" selected="selected">All</option>
					<g:each in="${column3List}" var="column3Obj">
						<option value="${column3Obj.key ? column3Obj.key : 'blank'}">${column3Obj.key ? column3Obj.key : 'blank'}&nbsp;(${column3Obj.value})</option>	
					</g:each>
				</select>
			</th>
			<th style="padding-top:35px;">
				<tds:sortableLink id="column4Label" style="border:0px;" property="${columns?.column4.field}"  title="${columns?.column4.label}" params="['projectId':projectId, moveEvent:moveEventInstance?.id, 'moveBundle':moveBundleInstance?.id, 'column1':column1Value,'column2':column2Value,'column3':column3Value,'column4':column4Value, 'assetsInView':assetsInView, 'offset':params.offset]"/>
				<span id="column4Select" style="display: none;"><g:select from="${com.tdssrc.eav.EavEntityAttribute.findAll()?.attribute}" optionKey="attributeCode" optionValue="frontendLabel" name="column4Attribute" value="${columns?.column4.field}" ></g:select></span>
				<span id="column4Edit"><img src="${createLinkTo(dir:'i',file:'db_edit.png')}" border="0px" onclick="changeLabelToSelect()"/></span>
				<span id="column4Save" style="display: none;"><input type="submit" value="Save"/>&nbsp;<input type="button" value="X" onclick="changeToLabel('4')"/></span> 
				<br />

				<select id="column4Id" name="column4" onchange="document.listForm.submit();" style="width: 120px;">
					<option value="" selected="selected">All</option>
					<g:each in="${column4List}" var="column4Obj">
						<option value="${column4Obj.key ? column4Obj.key : 'blank'}">${column4Obj.key ? column4Obj.key : 'blank'}&nbsp;(${column4Obj.value})</option>	
					</g:each>
				</select>
			</th>

			<g:if test="${browserTest}">
			<g:each in="${processTransitionList}"  var="task">
				<th class="verticaltext" title="${task.header}" style="color: ${task.fillColor}" onclick="bulkTransitionsByHeader('${task.transId}')">${task?.header}</th>
			</g:each>
			</g:if>
			<g:else>
			<th style="padding-left: 0px; height: 102px" colspan="${headerCount}"><embed src="${createLinkTo(dir:'templates',file:'headerSvg_'+projectId+'.svg')}" type="image/svg+xml" width="${headerCount*21.80}" height="102px"/></th>
			</g:else>
		</tr>
	</g:form>
	</thead>
	<tbody id="assetListTbody" onclick="catchevent(event)">
		<g:if test="${assetEntityList}">
		<g:each in="${assetEntityList}" var="assetEntity">
			<tr id="assetRow_${assetEntity.id}">
			<td>
			<g:if test="${isAdmin || isManager || isProjManager}">
			<span id="action_${assetEntity.id}">
				<g:if test="${assetEntity.checkVal == true}">
					<g:checkBox name="checkChange" id="checkId_${assetEntity.id}" onclick="timedUpdate('never')"></g:checkBox>
						<img id="task_${assetEntity.id}"src="${createLinkTo(dir:'i',file:'db_edit.png')}" border="0px" />
				</g:if>
				<g:else>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</g:else>
			</span>
			</g:if>
			<img id="asset_${assetEntity.id}" src="${createLinkTo(dir:'images',file:'asset_view.png')}" border="0px" />
			<span id="icon_${assetEntity.id}">
				<g:if test="${AssetComment.find('from AssetComment where assetEntity = '+assetEntity.id+' and commentType = ? and isResolved = ?',['issue',0])}">
						<img id="comment_${assetEntity.id}" src="${createLinkTo(dir:'i',file:'db_table_red.png')}" border="0px" />
				</g:if>
				<g:else>
					<g:if test="${AssetComment.find('from AssetComment where assetEntity = '+assetEntity.id)}">
						<img id="comment_${assetEntity.id}" src="${createLinkTo(dir:'i',file:'db_table_bold.png')}" border="0px" />
					</g:if>
					<g:else>
					<g:if test="${isAdmin || isManager || isProjManager}">
						<img src="${createLinkTo(dir:'i',file:'db_table_light.png')}" border="0px" onclick="createNewAssetComment(${assetEntity.id});"/>
					</g:if>
					</g:else>
			</g:else>
			</span>
			</td>
			<td  id="${assetEntity.id}_column1">${assetEntity.asset[columns?.column1.field]}&nbsp;</td>
			<td id="${assetEntity.id}_column2">${assetEntity.asset[columns?.column2.field]}&nbsp;</td>
			<td id="${assetEntity.id}_column3">${assetEntity.asset[columns?.column3.field]}&nbsp;</td>
			<td id="${assetEntity.id}_column4">${assetEntity.asset[columns?.column4.field]}&nbsp;</td>
			<g:each in="${assetEntity.transitions}" var="transition">${transition}</g:each>
			</tr>
		</g:each>
		</g:if>
		<g:else>
			<tr><td colspan="40" class="no_records">No records found</td></tr>
		</g:else>
	</tbody>
</table></div>
</g:form>
<div id="commentsListDialog" title="Show Asset Comments"
	style="display: none;"><br/>
<div class="list">
<table id="listCommentsTable" >
	<thead>
		<tr>
			<g:if test="${role}">
			<th nowrap>Action</th>
			</g:if>
			
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
<g:if test="${isAdmin || isManager || isProjManager}">
<div class="nav" style="border: 1px solid #CCCCCC; height: 11px">
<span class="menuButton"><a class="create" href="#"
	onclick="$('#statusId').val('');$('#createResolveDiv').css('display', 'none') ;$('#createCommentDialog').dialog('option', 'width', 700);$('#createCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('open');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('close');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close');document.createCommentForm.mustVerify.value=0;document.createCommentForm.reset();">New
Comment</a></span></div>
</g:if>
</div>
<div id="createCommentDialog" title="Create Asset Comment"
	style="display: none;"><input type="hidden" name="assetEntity.id"
	id="createAssetCommentId" value=""/> <input type="hidden"
	name="status" id="statusId" value=""/> 
	<input type="hidden" id="newAssetCommentId" value=""/>
	<g:form	action="saveComment" method="post" name="createCommentForm">
	<input type="hidden" name="category" value="moveday"/>
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<div>
	<table id="createCommentTable" style="border: 0px">
		
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
					<label for="mustVerify">Must Verify</label>
				</td>
			</tr>
			<tr class="prop">
				<td valign="top" class="name"><label for="comment">Comment:</label>
				</td>
				<td valign="top" class="value"><textarea cols="80" rows="5"
					id="comment" name="comment"></textarea></td>
			</tr>
		
	</table>
	</div>
	<div id="createResolveDiv" style="display: none;">
		<table id="createResolveTable" style="border: 0px" >
            <tr class="prop">
            	<td valign="top" class="name"><label for="isResolved">Resolved:</label></td>
                <td valign="top" class="value"><input type="checkbox" id="isResolved" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/></td>
            </tr>
            <tr class="prop">
		<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
		<td valign="top" class="value"><textarea cols="80" rows="5" id="resolution" name="resolution" ></textarea></td>
            </tr> 
            </table>
            </div>
		
	</div>
	<div class="buttons"><span class="button">
	<input class="save" type="button" value="Create" onclick="resolveValidate('createCommentForm','createAssetCommentId');" />
	</span></div>
</g:form></div>
<div id="showCommentDialog" title="Show Asset Comment" style="display: none;">
<div class="dialog" style="border: 1px solid #5F9FCF"><input name="id" value="" id="commentId"
	type="hidden"/>
	<div>
<table id="showCommentTable" style="border: 0px">
	
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
			<td valign="top" class="name"><label for="isResolved">Is Resolved:</label></td>
			<td valign="top" class="value" id="resolveTdId"><input type="checkbox" id="isResolvedId" name="isResolved" value="0" disabled="disabled" /></td>
		</tr>
		<tr class="prop">
			<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
			<td valign="top" class="value" ><textarea cols="80" rows="5" id="resolutionId" readonly="readonly"></textarea> </td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="dateResolved">Resolved At:</label></td>
			<td valign="top" class="value" id="dateResolvedId" ></td>
		</tr>
		<tr>
			<td valign="top" class="name"><label for="resolvedBy">Resolved By:</label></td>
			<td valign="top" class="value" id="resolvedById" ></td>
		</tr>
</table>
</div>
<g:if test="${isAdmin || isManager || isProjManager}">
<div class="buttons">
<span class="button">
<input class="edit" type="button" value="Edit" onclick="commentChangeEdit('editResolveDiv','editCommentForm');$('#editCommentDialog').dialog('option', 'width', 700);$('#editCommentDialog').dialog('option', 'position', ['center','top']);$('#createCommentDialog').dialog('close');$('#showCommentDialog').dialog('close');$('#editCommentDialog').dialog('open');$('#showDialog').dialog('close');$('#editDialog').dialog('close');$('#createDialog').dialog('close')" />
</span>
<span class="button"> <input class="delete" type="button" value="Delete"
	onclick="${remoteFunction(controller:'assetEntity', action:'deleteComment', params:'\'id=\' + $(\'#commentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'action\')')}" />
</span></div>
</g:if>
</div></div>
<div id="editCommentDialog" title="Edit Asset Comment" style="display: none;">
	<g:form action="updateComment" method="post" name="editCommentForm">
	<div class="dialog" style="border: 1px solid #5F9FCF">
	<input type="hidden" name="id" id="updateCommentId" value=""/>
	<div>
	<table id="updateCommentTable" style="border: 0px">
		<tr>
		<td valign="top" class="name"><label for="dateCreated">Created At:</label></td>
		<td valign="top" class="value" id="dateCreatedEditId"></td>
	</tr>
	<tr>
		<td valign="top" class="name"><label for="createdBy">Created By:</label></td>
		<td valign="top" class="value" id="createdByEditId"></td>
	</tr>
	<tr class="prop" >
		<td valign="top" class="name"><label for="commentType">Comment Type:</label></td>
		<td valign="top" style="width: 20%;" >
			<g:if test="${isAdmin || isProjManager}">
				<g:select id="commentTypeEditId" name="commentType" from="${AssetComment.constraints.commentType.inList}" value="" onChange="commentChange('#editResolveDiv','editCommentForm')">
				</g:select>&nbsp;&nbsp;&nbsp;&nbsp;
			</g:if>
			<g:else>
				<input type="text" id="commentTypeEditId" name="commentType" readonly style="border: 0;"/>&nbsp;&nbsp;&nbsp;&nbsp;
			</g:else>
				<input type="checkbox" id="mustVerifyEditId" name="mustVerify" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }" />&nbsp;&nbsp;
				<label for="mustVerify">Must Verify</label>
		</td>
	</tr>
	<tr class="prop">
		<td valign="top" class="name"><label for="category">Category:</label></td>
		<td valign="top" class="value" id="categoryEditId" ></td>
	</tr>
	<tr class="prop">
		<td valign="top" class="name"><label for="commentCode">Comment Code:</label></td>
		<td valign="top" class="value" id="commentCodeEditId" ></td>
	</tr>
	<tr class="prop">
		<td valign="top" class="name"><label for="comment">Comment:</label></td>
		<td valign="top" class="value"><textarea cols="80" rows="5" id="commentEditId" name="comment"></textarea></td>
	</tr>
</table>
</div>
<div id="editResolveDiv" style="display: none;">
<table id="updateResolveTable" style="border: 0px">
	<tr class="prop">
            	<td valign="top" class="name"><label for="isResolved">Resolved:</label></td>
                <td valign="top" class="value">
                <input type="checkbox" id="isResolvedEditId" name="isResolved" value="0" onclick="if(this.checked){this.value = 1} else {this.value = 0 }"/>
		</td>
	</tr>
	<tr class="prop">
		<td valign="top" class="name"><label for="resolution">Resolution:</label></td>
		<td valign="top" class="value"><textarea cols="80" rows="5" id="resolutionEditId" name="resolution" ></textarea></td>
	</tr>
	<tr>
		<td valign="top" class="name"><label for="dateResolved">Resolved At:</label></td>
		<td valign="top" class="value" id="dateResolvedEditId"></td>
	</tr>
	<tr>
		<td valign="top" class="name"><label for="resolvedBy">Resolved By:</label></td>
		<td valign="top" class="value" id="resolvedByEditId"></td>
	</tr>
</table>
</div>
</div>
<div class="buttons"><span class="button"> <input
		class="save" type="button" value="Update"
		onclick="resolveValidate('editCommentForm','updateCommentId');" />
	</span> <span class="button"> <input class="delete" type="button"
		value="Delete"
		onclick="${remoteFunction(controller:'assetEntity', action:'deleteComment', params:'\'id=\' + $(\'#updateCommentId\').val() +\'&assetEntity=\'+$(\'#createAssetCommentId\').val() ', onComplete:'listCommentsDialog(e,\'action\')')}" />
	</span></div>
</g:form></div>
<div id="showDialog" title="Show Asset Entity" style="display: none;">
<g:form controller="assetEntity" action="save" method="post" name="showForm">
	<div class="dialog" id="showDiv">
	<table id="showTable">
	</table>
	</div>
	<g:if test="${isAdmin || isManager || isProjManager}">
	<div class="buttons">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundle" value="${moveBundleInstance?.id}" />
	<input type="hidden" name="clientList" value="clientList" />
	<span class="button"><input type="button" class="edit" value="Edit" onClick="return editAssetDialog()" /></span>
	<span class="button"><g:actionSubmit class="delete" onclick="return confirm('Delete Asset, are you sure?');" value="Delete" /></span>
	<span class="button"><g:actionSubmit action="remove" class="delete"  onclick="return confirm('Remove Asset from project, are you sure?');" value="Remove From Project" /></span>
	</div>
	</g:if>
</g:form></div>

<div id="editDialog" title="Edit Asset Entity" style="display: none;">
<g:form method="post" name="editForm" controller="assetEntity">
	<input type="hidden" name="id" value="" />
	<input type="hidden" name="projectId" value="${projectId}" />
	<input type="hidden" name="moveBundle" value="${moveBundleInstance?.id}" />
	<input type="hidden" name="clientList" value="clientList" />
	<div class="dialog" id="editDiv">

	</div>
	<g:if test="${isAdmin || isManager || isProjManager}">
	<div class="buttons"><span class="button">
	<input type="button" class="save" value="Update Asset Entity" onClick="${remoteFunction(controller:'assetEntity', action:'getAssetAttributes', params:'\'assetId=\' + document.editForm.id.value ', onComplete:'callUpdateDialog(e)')}" />
	</span> <span class="button"><input type="button" class="delete" onclick="return editDialogDeleteRemove('delete')" value="Delete" /></span>
		<span class="button"><input type="button" class="delete" onclick="return editDialogDeleteRemove('remove');" value="Remove From Project" /></span>
		</div>
	</g:if>
</g:form></div>
<div class="contextMenu" id="myMenu"></div>
<div class="contextMenu" id="transitionMenu" style="visibility: hidden;">
	<ul>
        <li id="done">Done</li>
        <li id="NA">N/A</li>
        <li id="pending">Pending</li>
        <li id="void">Undo</li>
        <li id="ready">Ready</li>
        <li id="noOptions">No Options</li>
    </ul>
</div></div>

<script type="text/javascript" src="/tdstm/js/jquery.fixedheadertable.1.1.2.js"></script>

<script type="text/javascript">
/*<![CDATA[*/
	var timeInterval;
	var fieldId;
	var hasTimedOut = false;
	$(document).ready(function() {
		var windowWidth = $(window).width() - 10;
		var windowHeight = $(window).height() - $('.header').height() - $('#consoleHeader').height() - 25;
		if ($.browser.msie == true) {
			windowWidth -= 20;
			windowHeight -= 10;
		}
		
		$(window).resize(function() {
			if(hasTimedOut != false) {
				clearTimeout(hasTimedOut);
			}
			hasTimedOut = setTimeout(function() {
				var windowWidth = $(window).width() - 10;
				var windowHeight = $(window).height() - $('.header').height() - $('#consoleHeader').height() - 25;
				if ($.browser.msie == true) {
					windowWidth -= 20;
					windowHeight -= 10;
				}

				$('#tableContainer').css({'width': windowWidth+'px', 'height': windowHeight+'px'});
			}, 100);
		});
		
		$('#tableContainer').css({'width': windowWidth+'px', 'height': windowHeight+'px'});
		if(!$.browser.msie) {
			jQuery('#tableContainer').fixedHeaderTable({autoResize:true, footer:false});
			$('.fht_table_body thead select').remove();
			$('.fht_table_body thead input').remove();
		}
		
		$('body').click(function(){
			$(".cell-selected").removeClass('cell-selected');
		});
		
		$("#moveBundleId").val(${moveBundleInstance?.id});
		$("#moveEventId").val(${moveEventInstance?.id});
		$("#column4Id").val("${column4Value}");
		$("#column3Id").val("${column3Value}");
		$("#column2Id").val("${column2Value}");
		$("#column1Id").val("${column1Value}");
		$("#assetsInViewId").val("${assetsInView}");
		if($("#assetsInViewId")[0].selectedIndex == -1)
			$("#assetsInViewId")[0].selectedIndex = 0;
		
		timedUpdate($("#selectTimedId").val());
		
		var time = '${timeToUpdate}';
		if(time != "" ){
			$("#selectTimedId").val( time ) ;
		} else if(time == "" ){
			$("selectTimedId").val( 120000 );	
		}
		
		$("#changeStatusDialog").dialog({ autoOpen: false })
		$("#showDialog").dialog({ autoOpen: false })
		$("#editDialog").dialog({ autoOpen: false })
		$("#commentsListDialog").dialog({ autoOpen: false })
		$("#createCommentDialog").dialog({ autoOpen: false })
	    $("#showCommentDialog").dialog({ autoOpen: false })
	    $("#editCommentDialog").dialog({ autoOpen: false })
	    $("#showChangeStatusDialog").dialog({ autoOpen: false })
		var role = "${role}";
		// Show menu when #myDiv is clicked
		if(role) {
			var actionId
			$('tbody#assetListTbody').contextMenu('transitionMenu', {
				onContextMenu: function(e) {
					return($(e.target).is('td.tranCell') && !$(e.target).is('td.asset_hold'));
				},
				onShowMenu: function(e, menu) {
					$(".cell-selected").removeClass('cell-selected');
					$(e.target).addClass('cell-selected');
					actionId = $(e.target).attr("id") 
					${remoteFunction(action:'getMenuList', params:'\'id=\' + actionId ', onComplete:'updateMenu(e,menu)')};
					return menu;
				},
				bindings: {
	        		'done': function(t) {
			       		${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + actionId +\'&type=done\'', onComplete:'updateTransitionRow(e)' )};
			        },
			        'ready': function(t) {
			          ${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + actionId +\'&type=ready\'', onComplete:'updateTransitionRow(e)' )};
			        },
			        'NA': function(t) {
			          ${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + actionId +\'&type=NA\'', onComplete:'updateTransitionRow(e)' )};
			        },
			        'pending': function(t) {
			          ${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + actionId +\'&type=pending\'', onComplete:'updateTransitionRow(e)' )};
			        },
			        'void': function(t) {
			          	if(confirm("Undo this specific task and any dependent (workflow) transitions. Are you sure?")){
			          		${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + actionId +\'&type=void\'', onComplete:'updateTransitionRow(e)' )};
						} else {
			          		return false
			         	}
			        },
			        'noOptions': function(t){
			        	$(".cell-selected").attr('class',$("#cssClassId").val());
			        }
		      	}
	    	});
		}
		$("tbody#assetListTbody tr td").click(function () {
	    	var tdId = $(this).attr("id")
	    	if(!isNaN(tdId.split("_")[1])){
		        if($("#bulkEditId").hasClass("bulkedit_active")){
				    var action = $("#bulkActionId").val()
				    switch (action){
					    case "pending" :
					    	${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + tdId +\'&type=pending\'', onComplete:'updateTransitionRow(e)' )};
						break;
						case "done" :
							${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + tdId +\'&type=done\'', onComplete:'updateTransitionRow(e)' )};
						break;
						case "NA" :
							${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + tdId +\'&type=NA\'', onComplete:'updateTransitionRow(e)' )};
						break;
						case "void" :
							${remoteFunction(action:'createTransitionForNA', params:'\'actionId=\' + tdId +\'&type=void\'', onComplete:'updateTransitionRow(e)' )};
						break;	
				    }
		        } else {
		        	${remoteFunction(controller:'assetEntity', action:'showStatus', params:'\'id=\'+tdId', onComplete:'window.status = e.responseText')}
		        }
	    	}
	    });
		$("tbody#assetListTbody tr td").mouseout(function(){
			window.status = ""
		}); 
	});
	
	/*------------------------------------------------------------
	 * update the menu for transition 
	 *------------------------------------------------------------*/
	function updateMenu(e,menu){
		var actionType = e.responseText
		if( actionType == "noTransMenu"){
			$('#pending, #void, #ready, #noOptions', menu ).remove()
		} else if( actionType == "naMenu") {
			$('#NA, #void, #ready, #noOptions', menu ).remove()
		} else if( actionType == "doneMenu") {
			$('#done, #void, #ready, #noOptions', menu ).remove()
		} else if( actionType == "readyMenu") {
			$('#NA, #done, #void, #pending, #noOptions', menu ).remove()
		} else if( actionType == "voidMenu") {
			$('#NA, #done, #ready, #pending, #noOptions', menu ).remove()
		} else if( actionType == "doMenu") {
			$('#NA, #ready, #pending, #void, #noOptions', menu ).remove()
		} else {
			$('#NA, #done, #ready, #pending, #void', menu ).remove()
		}
		menu.show()
	}
	/*-------------------------------------------------------------
	 * update the row as per user transition transition 
	 *------------------------------------------------------------*/
	function updateTransitionRow( e ){
		var assetTransitions = eval('(' + e.responseText + ')');
		var length = assetTransitions.length;
		if(length > 0){
			for( i=0; i<length; i++ ) {
				var transition = assetTransitions[i];
				$("#"+transition.id).attr("class",transition.cssClass );
				$("#"+transition.id).addClass('tranCell');
			}
		}
	}
	function editAssetDialog() {
		timedUpdate('never')
		$("#showDialog").dialog("close")
		$("#editDialog").dialog('option', 'width', 900)
		$("#editDialog").dialog('option', 'position', ['center','top']);
		$("#editDialog").dialog("open")
		
	}
		    
	function showEditAsset(e) {
   		var assetEntityAttributes = eval('(' + e.responseText + ')')
			if (assetEntityAttributes != "") {
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

	function showChangeStatusDialog(e){
		timedUpdate('never')
		var task = eval('(' + e.responseText + ')');
		var taskLen = task[0].item.length;
		var options = '';
		if(taskLen == 0){
			alert('Sorry but there were no common states for the assets selected');
			return false;
		}else{
	      	for (var i = 0; i < taskLen; i++) {
	        	options += '<option value="' + task[0].item[i].state + '">' + task[0].item[i].label + '</option>';
	      	}
	      	$("select#taskList").html(options);
	      	if(taskLen > 1 && task[0].item[0].state == "Hold"){
	      		$('#taskList').children().eq(1).attr('selected',true);
	      	}
	       	$('#asset').val(task[0].asset);
			$("#changeStatusDialog").dialog('option', 'width', 400)
			$("#changeStatusDialog").dialog('option', 'position', ['center','top']);
			$('#changeStatusDialog').dialog('open');
			$('#createCommentDialog').dialog('close');
			$('#commentsListDialog').dialog('close');
			$('#editCommentDialog').dialog('close');
			$('#showCommentDialog').dialog('close');
			$('#showDialog').dialog('close');
			$('#editDialog').dialog('close');
			$('#createDialog').dialog('close');
		}
	}
	
	function submitAction(){
		if(doCheck()){
			document.changeStatusForm.action = "changeStatus";
			document.changeStatusForm.submit();
			timedUpdate($("#selectTimedId").val())
		}else{
			return false;
		}
	}
	
	function doCheck(){
		var taskVal = $('#taskList').val();
		var noteVal = $('#enterNote').val();
		if((taskVal == "Hold")&&(noteVal == "")){
			alert('Please Enter Note');
			return false;
		}else{
			return true;
		}
	}
	
	function setUpdateTime(e) {
		var timeUpdate = eval("(" + e.responseText + ")")
		if(timeUpdate){
			timedUpdate(timeUpdate[0].updateTime.CLIENT_CONSOLE_REFRESH)
		}
	}
	
	var timer
	function timedUpdate(timeoutPeriod) {
		if(timeoutPeriod != 'never'){
			clearTimeout(timer)
			timer = setTimeout("doAjaxCall()",timeoutPeriod);
			$("#selectTimedId").val( timeoutPeriod );
		} else {
			clearTimeout(timer)
		}
	}
	function pageReload(){
		if('${myForm}'){
			document.forms['${myForm}'].submit() ;
		} else {
			window.location.href = document.URL;
		}
	}
	
	function doAjaxCall(){
		var moveEvent = $("#moveEventId").val();
		var moveBundle = $("#moveBundleId").val();
		var c1f = "${columns.column1.field}"
		var c1v = "${column1Value}"
		var c2f = "${columns.column2.field}"
		var c2v = "${column2Value}"
		var c3f = "${columns.column3.field}"
		var c3v = "${column3Value}"
		var c4f = "${columns.column4.field}"
		var c4v = "${column4Value}"
		var offset = $("#assetsInViewOffsetId").val()
		var max = $("#assetsInViewId").val()
		var sort = $("#sortById").val()
		var order = $("#orderById").val()
		var lastPoolTime = $("#lastPoolTimeId").val();
		${remoteFunction(action:'getTransitions', params:'\'moveBundle=\' + moveBundle +\'&moveEvent=\'+moveEvent +\'&c1f=\'+c1f+\'&c2f=\'+c2f+\'&c3f=\'+c3f+\'&c4f=\'+c4f+\'&c1v=\'+c1v+\'&c2v=\'+c2v+\'&c3v=\'+c3v+\'&c4v=\'+c4v+\'&lastPoolTime=\'+lastPoolTime+\'&offset=\'+offset+\'&max=\'+max+\'&sort=\'+sort+\'&order=\'+order', onFailure:"handleErrors()", onComplete:'updateTransitions(e);' )}
		timedUpdate($("#selectTimedId").val())
	}
	var doUpdate = true
	function handleErrors(){
		if( !doUpdate ){
			clearTimeout(timer);
		}
		doUpdate = false
		$("#updateId").css("color","red");
		alert("Sorry, there is a problem receiving updates to this page. Try reloading to resolve.");
	}
	function updateTransitions(e){
		try{
			var assetEntityCommentList = eval('(' + e.responseText + ')');
			var assetTransitions = assetEntityCommentList[0].assetEntityList;
			var assetComments = assetEntityCommentList[0].assetCommentsList;
			var assetslength = assetTransitions.length;
			var assetCommentsLength = assetComments.length;
			var sessionStatus = isNaN(parseInt(assetslength));
			if( !sessionStatus ){
				if(assetTransitions){
					for( i = 0; i <assetslength ; i++){
						var assetTransition = assetTransitions[i]
						var action = $("#action_"+assetTransition.id)
						if(action){
							if(!assetTransition.check){
								action.html('&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
							}
						}
						var application = $("#application_"+assetTransition.id)
						if(application){
							application.html( assetTransition.application );
						}
						var owner = $("#appOwner_"+assetTransition.id)
						if(owner){
							owner.html( assetTransition.appOwner );
						}
						var sme = $("#appSme_"+assetTransition.id)
						if(sme){
							sme.html( assetTransition.appSme );
						}
						var assetName = $("#assetName_"+assetTransition.id)
						if(assetName){
							assetName.html( assetTransition.assetName );
						}
						var tdIdslength = assetTransition.tdId.length
						for(j = 0; j< tdIdslength ; j++){
							var transition = assetTransition.tdId[j]
							var transTd = $("#"+transition.id)
							transTd.attr("class",transition.cssClass )
							transTd.addClass('tranCell');
						}
					}
				}
				if(assetComments){
					for( i = 0; i <assetCommentsLength ; i++){
						var assetComment = assetComments[i]
						var commentIcon = $("#icon_"+assetComment.assetEntityId)
						if(commentIcon){
							var link = document.createElement('a');
							link.href = '#';
							link.id = assetComment.assetEntityId;
							if ( assetComment.type == "database_table_light.png" ) {
								link.onclick = function(){$('#newAssetCommentId').val(this.id);createNewAssetComment('');};
							} else {
								link.onclick = function(){$('#createAssetCommentId').val(this.id);new Ajax.Request('../assetEntity/listComments?id='+this.id,{asynchronous:true,evalScripts:true,onComplete:function(e){listCommentsDialog(e,'action');}})} //;return false
							}
							link.innerHTML = "<img src=\"../i/"+assetComment.type+"\" border=\"0px\" />";
							commentIcon.html(link);
						}
					}
				}
				$("#lastPoolTimeId").val(assetEntityCommentList[0].lastPoolTime)
			} else {
				location.reload(false);
			//timedUpdate('never')
			}
		} catch(ex){
		//location.reload(false);
			if( doUpdate ){
				handleErrors();
			}
		}
	}

	function changeState(){
		timedUpdate('never')
		var assetArr = new Array();
		var totalAsset = ${assetEntityList.id};
		var j=0;
		for(i=0; i< totalAsset.size() ; i++){
			if($('#checkId_'+totalAsset[i]) != null){
				var booCheck = $('#checkId_'+totalAsset[i]).is(':checked');
				if(booCheck == true){
					assetArr[j] = totalAsset[i];
					j++;
				}
			}
		}	
		if(j == 0){
			alert('Please select the Asset');
		}else{
			${remoteFunction(action:'getList', params:'\'assetArray=\' + assetArr', onComplete:'showChangeStatusDialog(e);' )}
		}
	}
	
	var isFirst = true;
	function selectAll(){
		timedUpdate('never')
		var totalCheck = document.getElementsByName('checkChange');
		if(isFirst){
			for(i=0;i<totalCheck.length;i++){
				totalCheck[i].checked = true;
			}
			isFirst = false;
		}else{
			for(i=0;i<totalCheck.length;i++){
				totalCheck[i].checked = false;
			}
			isFirst = true;
		}
	}
	
	function showAssetDetails( assetId ){
		document.editForm.id.value=assetId
		//${remoteFunction(controller:'assetEntity', action:'editShow', params:'\'id=\'+ assetId', before:'document.showForm.id.value ='+ assetId+';', onComplete:'showAssetDialog(e , \'show\')')}
	}
	function vpWidth(type) {
		var data
		if(type == "width"){
			data  = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;
		} else {
			data  = window.innerHeight || document.documentElement.clientHeight || document.body.clientHeight;
		}
		return data
	}

	function createNewAssetComment(asset){
		if(asset) {
			setAssetId( asset );
		} else {
			setAssetId( $('#newAssetCommentId').val() );
		}
		$('#statusId').val('new');
		$('#createCommentDialog').dialog('option', 'width', 700);
		$('#createCommentDialog').dialog('open');
		$('#commentsListDialog').dialog('close');
		$('#editCommentDialog').dialog('close');
		$('#showCommentDialog').dialog('close');
		$('#showDialog').dialog('close');
		$('#editDialog').dialog('close');
		$('#createDialog').dialog('close');
		$('#changeStatusDialog').dialog('close');
		document.createCommentForm.mustVerify.value=0;
		document.createCommentForm.reset();
	}
	//To catch the event and call the specific remotefunction 
	function catchevent(event) {
		var oSource = event.target
		var srcElement = event.srcElement ? event.srcElement : event.target;
		eventSrcID=(srcElement)?srcElement.id:'undefined';
		var idArray = eventSrcID.split('_')
		if( idArray.length = 2  ) {
			var assetId = idArray[1]
			if( idArray[0] == "comment"  ) {
				${remoteFunction(controller:'assetEntity', action:'listComments', params:'\'id=\'+assetId', before:'setAssetId(assetId);',onComplete:'listCommentsDialog( e ,\"action\");')}
			}else if( idArray[0] == "asset"  ) {
				${remoteFunction(controller:'assetEntity', action:'editShow', params:'\'id=\'+assetId', before:'showAssetDetails(assetId);',	onComplete:'showAssetDialog( e ,\"show\");')}	
			}else if( idArray[0] == "task" ) {
				${remoteFunction( action:"getTask", params:'\'assetEntity=\'+assetId', onComplete:'showChangeStatusDialog(e);')}
			}
		}
	}
	var bulkEdit = true;
	function performBulkEdit(){
		var bulkEditButton = $("#bulkEditId");
		if(bulkEdit){
			alert("You are now in bulk edit mode. Select the state then the cells you want to change. Remember to turn off Bulk Edit when done." )
			bulkEditButton.removeClass("bulkedit_inactive")
			bulkEditButton.addClass("bulkedit_active")
			bulkEdit = false
			/*------- show Done as default ----------*/
			changeAction( "done" )
			
			$("#bulkTaskSpanId").show();
		} else {
			bulkEditButton.removeClass("bulkedit_active")
			bulkEditButton.addClass("bulkedit_inactive")
			bulkEdit = true
			$("#bulkTaskSpanId").hide();			
		}
	}
	function changeAction( action ){
		switch ( action ){
			case "pending" :
				$("#bulkDoneId").removeClass("bulkDone_active")
				$("#bulkNaId").removeClass("bulkNa_active")
				$("#bulkUndoId").removeClass("bulkPending_active")
				$("#bulkPendingId").addClass("bulkPending_active")
				$("#bulkActionId").val("pending")
			break;
			case "done" :
				$("#bulkNaId").removeClass("bulkNa_active")
				$("#bulkPendingId").removeClass("bulkPending_active")
				$("#bulkUndoId").removeClass("bulkPending_active")
				$("#bulkDoneId").addClass("bulkDone_active")
				$("#bulkActionId").val("done")
			break;
			case "void" :
				$("#bulkDoneId").removeClass("bulkDone_active")
				$("#bulkNaId").removeClass("bulkNa_active")
				$("#bulkPendingId").removeClass("bulkPending_active")
				$("#bulkUndoId").addClass("bulkPending_active")
				$("#bulkActionId").val("void")
			break;
			case "NA" :
				$("#bulkPendingId").removeClass("bulkPending_active")
				$("#bulkDoneId").removeClass("bulkDone_active")
				$("#bulkUndoId").removeClass("bulkPending_active")
				$("#bulkNaId").addClass("bulkNa_active")
				$("#bulkActionId").val("NA")
			break;	
		}
	}

	/* 
	 * Function to switch the Labels to Select list when user click on edit icon.
	 */
	function changeLabelToSelect(){
		$("#column1Label").hide()
		$("#column1Select").show()
		$("#column1Edit").hide()
		$("#column1Save").show()
		$("#column2Label").hide()
		$("#column2Select").show()
		$("#column2Edit").hide()
		$("#column2Save").show()
		$("#column3Label").hide()
		$("#column3Select").show()
		$("#column3Edit").hide()
		$("#column3Save").show()
		$("#column4Label").hide()
		$("#column4Select").show()
		$("#column4Edit").hide()
		$("#column4Save").show()
	}
	/*
	* Function to switch the Select list to Label when user click on 'X' button.
	*/
	function changeToLabel( colId ){
		/*var value = ""
		switch (colId) {
		case "1" :
			value = "${columns?.column1.field}"
			break;
		case "2" :
			value = "${columns?.column2.field}"
			break;
		case "3" :
			value = "${columns?.column3.field}"
			break;
		case "4" :
			value = "${columns?.column4.field}"
			break;
		}
		$("#column"+colId+"Label").show()
		$("#column"+colId+"Select select").val(value)
		$("#column"+colId+"Select").hide()
		$("#column"+colId+"Edit").show()
		$("#column"+colId+"Save").hide()*/
		
		$("#column1Label").show()
		$("#column1Select select").val("${columns?.column1.field}")
		$("#column1Select").hide()
		$("#column1Edit").show()
		$("#column1Save").hide()
		$("#column2Label").show()
		$("#column2Select select").val("${columns?.column2.field}")
		$("#column2Select").hide()
		$("#column2Edit").show()
		$("#column2Save").hide()
		$("#column3Label").show()
		$("#column3Select select").val("${columns?.column3.field}")
		$("#column3Select").hide()
		$("#column3Edit").show()
		$("#column3Save").hide()
		$("#column4Label").show()
		$("#column4Select select").val("${columns?.column4.field}")
		$("#column4Select").hide()
		$("#column4Edit").show()
		$("#column4Save").hide()
	}
	/*
	* 	Bulk edit of transitions by letting the project manager click on column head to transition the displayed assets to that step.
	*/
	function bulkTransitionsByHeader( transId ){
		if($("#bulkEditId").hasClass("bulkedit_active")){
			var eventId = $("#moveEventId").val();
			var bundleId = $("#moveBundleId").val();
			var type = $("#bulkActionId").val();
			var c1f = "${columns.column1.field}"
			var c1v = "${column1Value}"
			var c2f = "${columns.column2.field}"
			var c2v = "${column2Value}"
			var c3f = "${columns.column3.field}"
			var c3v = "${column3Value}"
			var c4f = "${columns.column4.field}"
			var c4v = "${column4Value}"
			var offset = $("#assetsInViewOffsetId").val()
			var max = $("#assetsInViewId").val()
			var sort = $("#sortById").val()
			var order = $("#orderById").val()
			
			${remoteFunction(action:'getAssetsCountForBulkTransition', 
								params:'\'transId=\' + transId +\'&bundleId=\'+bundleId+\'&eventId=\'+eventId+\'&type=\'+type+\'&c1f=\'+c1f+\'&c2f=\'+c2f+\'&c3f=\'+c3f+\'&c4f=\'+c4f+\'&c1v=\'+c1v+\'&c2v=\'+c2v+\'&c3v=\'+c3v+\'&c4v=\'+c4v+\'&offset=\'+offset+\'&max=\'+max+\'&sort=\'+sort+\'&order=\'+order', 
								onComplete:'doBulkTransitionsByHeader(e,transId)' )};
		}
	}
	function doBulkTransitionsByHeader( e, transId){
		var message = e.responseText
		if(confirm( message )){
			var eventId = $("#moveEventId").val();
			var bundleId = $("#moveBundleId").val();
			var type = $("#bulkActionId").val();
			var type = $("#bulkActionId").val()
			var c1f = "${columns.column1.field}"
			var c1v = "${column1Value}"
			var c2f = "${columns.column2.field}"
			var c2v = "${column2Value}"
			var c3f = "${columns.column3.field}"
			var c3v = "${column3Value}"
			var c4f = "${columns.column4.field}"
			var c4v = "${column4Value}"
			var offset = $("#assetsInViewOffsetId").val()
			var max = $("#assetsInViewId").val()
			var sort = $("#sortById").val()
			var order = $("#orderById").val()
			${remoteFunction(action:'doBulkTransitionsByHeader', 
							params:'\'transId=\' + transId +\'&bundleId=\'+bundleId+\'&eventId=\'+eventId+\'&type=\'+type+\'&c1f=\'+c1f+\'&c2f=\'+c2f+\'&c3f=\'+c3f+\'&c4f=\'+c4f+\'&c1v=\'+c1v+\'&c2v=\'+c2v+\'&c3v=\'+c3v+\'&c4v=\'+c4v+\'&offset=\'+offset+\'&max=\'+max+\'&sort=\'+sort+\'&order=\'+order', 
							onComplete:'doAjaxCall()' )};
		}
	}
/*]]>*/
</script>
</body>
</html>
