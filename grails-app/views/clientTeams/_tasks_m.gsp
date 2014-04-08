<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus;com.tds.asset.AssetComment" %>
<%@page import="com.tdssrc.grails.GormUtil"%>
<g:set var="colSpan" value="${isCleaner ? 1 : 4}" />
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
    	  	<g:form method="post" name="issueAssetForm" action="listTasks">
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
<% java.text.DecimalFormat formatter = new java.text.DecimalFormat("0.0000") %>

<div id="myIssueList" class="mobbodyweb" style="width: 100%">
	<input id="issueId" name="issueId" type="hidden" value="" />
	<input name="tab" id="tab_m" type="hidden" value="${tab}" />
	<div id="mydiv" onclick="this.style.display = 'none';setFocus();">
		<g:if test="${flash.message}">
			<br />
			<div class="message">
				<ul>
					${flash.message}
				</ul>
			</div>
		</g:if> 
	</div>		
	<div id="taskId" style="float: left; width:100%; margin: 1px;"></div>
	<div id="assetIssueDiv" style="float: left; width: 100%;">
		<table id="issueTable" cellspacing="0px"
			style="width: 100%; margin-left: -1px;">
			<thead>
				<tr>
					<g:sortableColumn class="sort_column" style="width:45%;"  action="listTasks" property="number_comment" title="Task" params="['tab':tab,'search':search]"></g:sortableColumn>
					<g:if test="${ ! isMoveTech}">
						<g:sortableColumn class="sort_column" style="width:25%;" action="listTasks" property="assetName" title="Related" params="['tab':tab,'search':search]"></g:sortableColumn>
						<g:sortableColumn class="sort_column" style="width:15%;" action="listTasks" property="status" title="Status" params="['tab':tab,'search':search]" defaultOrder="desc"></g:sortableColumn>
						<g:sortableColumn class="sort_column" style="width:15%;" action="listTasks" property="assigned" title="Assigned To" params="['tab':tab,'search':search]" defaultOrder="desc"></g:sortableColumn>
					</g:if>
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
						${issue?.item?.comment}
					</td>
					<g:if test="${ ! isMoveTech}">
						<td id="asset_${issue?.item?.id}" class="asset_details_block">
							${issue?.item?.assetName}
						</td>					
						<td id="statusTd_${issue?.item?.id}" class="asset_details_block">
							${issue?.item?.status}
						</td>
						<td id="assignedToName_${issue?.item?.id}" class="asset_details_block">
							${(issue?.item?.hardAssigned?'* ':'')} <span id="assignedToNameSpan_${issue?.item?.id}">${(issue?.item?.firstName?:'')+' '+((issue?.item?.lastName != null)? issue?.item?.lastName :'')}</span>
						</td>
					</g:if>
					</tr>
					<g:if test="${tab && tab == 'todo'}">
					 <tr id="showStatusId_${issue?.item?.id}" ${(todoSize!=1||search==''||search==null) ? 'style="display: none"' :''}>
						<td nowrap="nowrap" colspan="${colSpan}" class="statusButtonBar">
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
							<tds:hasPermission permission='CommentCrudView'>
								<g:if test="${issue.item.status == AssetCommentStatus.READY && !(issue?.item?.category in AssetComment.moveDayCategories)}">
									<span class="delay_myTasks">Delay for:</span>
									<tds:actionButton label="1 day" icon="ui-icon-seek-next" id="${issue?.item?.id}"  
										onclick="changeEstTime(1,'${issue?.item?.id}', this.id)"/>
									<tds:actionButton label="2 days" icon="ui-icon-seek-next" id="${issue?.item?.id}"  
										onclick="changeEstTime(2,'${issue?.item?.id}', this.id)"/>
									<tds:actionButton label="7 days" icon="ui-icon-seek-next" id="${issue?.item?.id}"  
										onclick="changeEstTime(7,'${issue?.item?.id}', this.id)"/>
								</g:if>
							</tds:hasPermission>
						</td>
					</tr>
				</g:if>
				<tr id="detailTdId_${issue?.item?.id}" style="display: none">
				<td colspan="4">
						<div id="detailId_${issue?.item?.id}"  ></div>
				</td>
				</tr>
			</g:each>
			</tbody>
		</table>
	</div>
    </g:form>
</div>