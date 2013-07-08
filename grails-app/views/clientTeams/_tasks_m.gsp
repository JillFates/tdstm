<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus" %>
<%@page import="com.tdssrc.grails.GormUtil"%>
<%--
/*
 **************************
 * Menu
 **************************
 */
--%>
<div class="menu4">
	<ul>
		<g:if test="${tab && tab == 'todo'}">
			<li onclick="setTab('todo')"><g:link elementId="taskLinkId" class="mobmenu mobselect"
					action="listTasks" params='["tab":"todo"]'>Ready Tasks: ${todoSize}
				</g:link></li>
			<li onclick="setTab('all')"><g:link elementId="taskLinkAllId" class="mobmenu" 
					action="listTasks" params='["tab":"all"]'>All Tasks: ${allSize}
				</g:link></li>
		</g:if>
		<g:if test="${tab && tab == 'all'}">
			<li onclick="setTab('todo')"><g:link elementId="taskLinkId" class="mobmenu"
					action="listTasks" params='["tab":"todo"]'>Ready Tasks: ${todoSize}
				</g:link></li>
			<li onclick="setTab('all')"><g:link elementId="taskLinkAllId" class="mobmenu mobselect"
					action="listTasks" params='["tab":"all"]'>All Tasks: ${allSize}
				</g:link></li>
		</g:if>
		<li>
    	  	<g:form method="post" name="issueAssetForm" action="showIssue">
			<input type="text" size="08" value="${search}" id="search" 
				name="search" autocorrect="off" autocapitalize="off" 
				onfocus="changeAction()" onblur="retainAction()" />
			<input type="hidden" name="sort" value="${sort}">
			<input type="hidden" name="order" value="${order}">
		</li>
	</ul>
</div>
<div class="issueTimebar" id="issueTimebar">
	<div id="issueTimebarId"></div>
</div>
<div id="detailId"
	style="display: none; position: absolute; width: 320px; margin-top: 40px;">
</div>
<%--
/*
 **************************
 * My Issues List 
 **************************
 */
--%>
<div style="width: 100%;" id="myIssueList" class="mobbodyweb">
	<input id="issueId" name="issueId" type="hidden" value="" />
	<input name="tab" id="tab_m" type="hidden" value="${tab}" />
	<div id="mydiv" onclick="this.style.display = 'none';setFocus();">
		<g:if test="${flash.message}">
			<br />
			<div class="message"><ul>${flash.message}</ul></div>
		</g:if> 
	</div>		
	<div id="taskId" style="float: left; width:100%; margin: 1px;"></div>
	<div id="assetIssueDiv" style="float: left; width: 100%;">
		<table id="issueTable" cellspacing="0px" style="width:100%;">
			<thead>
				<tr>
					<g:sortableColumn class="sort_column" style="width:45%;"  action="listTasks" property="number_comment" title="Task" params="['tab':tab,'search':search]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="width:35%;" action="listTasks" property="assetName" title="Related" params="['tab':tab,'search':search]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="width:20%;" action="listTasks" property="status" title="Status" params="['tab':tab,'search':search]" defaultOrder="desc"></g:sortableColumn>
				</tr>
			</thead>
			<tbody>
				<g:each status="i" in="${taskList}" var="issue">
					<g:if test="${tab && tab == 'todo'}">
						<tr id="issueTrId_${issue?.item?.id}" class="${issue.css}"
							style="cursor: pointer;"
							onclick="openStatus(${issue?.item?.id},'${issue?.item?.status}')">
				  	</g:if>
				  	<g:else>
						<tr id="issueTr_${issue?.item?.id}" class="${issue.css}"
							style="cursor: pointer;"
							onclick="issueDetails(${issue?.item?.id},'${issue?.item?.status}')">
			  	</g:else>
					<td id="comment_${issue?.item?.id}"
						class="asset_details_block_task">
						${issue?.item?.taskNumber?issue?.item?.taskNumber+' - ' : ''}
						${com.tdssrc.grails.StringUtil.ellipsis(issue?.item?.comment,25)}
					</td>
					<td id="asset_${issue?.item?.id}" class="asset_details_block">
						${issue?.item?.assetName}
					</td>
					
					<td id="statusTd_${issue?.item?.id}"id="statusTd_${issue?.item?.id}" class="asset_details_block">
						${issue?.item?.status}
					</td>
					</tr>
					<g:if test="${tab && tab == 'todo'}">
					 <tr id="showStatusId_${issue?.item?.id}" ${(todoSize!=1||search==''||search==null) ? 'style="display: none"' :''}>
						<td nowrap="nowrap" colspan="3" class="statusButtonBar">
							<g:if test="${issue.item.status == AssetCommentStatus.READY}"> 
							<tds:actionButton label="Start" icon="ui-icon-play" id="${issue?.item?.id}"  
								onclick="changeStatus('${issue?.item?.id}','${AssetCommentStatus.STARTED}', '${issue?.item?.status}', 'taskManager')"/>
							</g:if>
							<g:if test="${ [AssetCommentStatus.READY, AssetCommentStatus.STARTED].contains(issue.item.status) }"> 
							<tds:actionButton label="Done" icon="ui-icon-check" id="${issue?.item?.id}"  
								onclick="changeStatus('${issue?.item?.id}','${AssetCommentStatus.DONE}', '${issue?.item?.status}', 'taskManager')"/>
							</g:if>
							<tds:actionButton label="Details..." icon="ui-icon-zoomin" id="${issue?.item?.id}"  
								onclick="issueDetails(${issue?.item?.id},'${issue?.item?.status}')"/>
							<g:if test="${ personId != issue.item.assignedTo && issue.item.status in [AssetCommentStatus.PENDING, AssetCommentStatus.READY, AssetCommentStatus.STARTED]}">
							<tds:actionButton label="Assign To Me" icon="ui-icon-person" id="${issue?.item?.id}"  
								onclick="assignTask('${issue?.item?.id}','${issue.item.assignedTo}', '${issue.item.status}','myTask')"/>
							</g:if>
						</td>
					</tr>
				</g:if>
				<tr id="detailTdId_${issue?.item?.id}" style="display: none">
				<td colspan="3">
				   <div id="detailId_${issue?.item?.id}"  > </div>
				</td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>
    </g:form>
</div>