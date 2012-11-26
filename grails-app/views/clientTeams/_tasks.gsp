<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus" %>
<%@page import="com.tdssrc.grails.GormUtil"%>
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
	<input name="tab" id="tabId" type="hidden" value="${tab}" />
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
	<div id="taskId" style="float: left; margin: 2px 0;"></div>
	<div id="assetIssueDiv" style="float: left; width: 100%;">
		<table id="issueTable" cellspacing="0px"
			style="width: 100%; margin-left: -1px;">
			<thead>
				<tr>
					<g:sortableColumn class="sort_column" style="" action="listTasks" property="number_comment" title="Task" params="['tab':tab,'search':search]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="" action="listTasks" property="assetName" title="Related" params="['tab':tab,'search':search]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="" action="listTasks" property="lastUpdated" title="Updated" params="['tab':tab,'search':search]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="" action="listTasks" property="estFinish" title="Due/Est Finish" params="['tab':tab,'search':search]" defaultOrder="desc"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="" action="listTasks" property="status" title="Status" params="['tab':tab,'search':search]" defaultOrder="desc"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="" action="listTasks" property="assignedTo" title="Assigned To" params="['tab':tab,'search':search]" ></g:sortableColumn>
				</tr>
			</thead>
			<tbody>
				<g:each status="i" in="${taskList}" var="issue">
					<tr id="issueTrId_${issue?.item?.id}" class="${issue.css}"
						style="cursor: pointer;"
						onclick="openStatus(${issue?.item?.id},'${issue?.item?.status}')">
						<td id="comment_${issue?.item?.id}"
							class="asset_details_block_task">
							${issue?.item?.taskNumber?issue?.item?.taskNumber+' - ' : ''}
							${com.tdssrc.grails.StringUtil.ellipsis(issue?.item?.comment,50)}
						</td>
						<td id="asset_${issue?.item?.id}" class="asset_details_block">
							${issue?.item?.assetName}
						</td>
						<td id="lastUpdated_${issue?.item?.id}" class="asset_details_block">
							<tds:elapsedAgo start="${issue?.item?.lastUpdated}" end="${GormUtil.convertInToGMT(new Date(), null)}"/>
						</td>
						<td id="estFinish_${issue?.item?.id}" class="asset_details_block">
								<tds:convertDate date="${issue?.item?.estFinish}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"
									format="MM/dd kk:mm" />
						</td>
						<td id="statusTd_${issue?.item?.id}" class="asset_details_block">
							${issue?.item?.status}<% // (${formatter.format(issue?.item?.score?: 0)}) %>
						</td>
						<td id="assignedToName_${issue?.item?.id}" class="asset_details_block">
							${(issue?.item?.hardAssigned?'* ':'')} <span id="assignedToNameSpan_${issue?.item?.id}">${issue?.item?.firstName+' '+issue?.item?.lastName}</span>
						</td>
					</tr>
					<tr id="showStatusId_${issue?.item?.id}" ${(todoSize!=1||search==''||search==null) ? 'style="display: none"' :''}>
						<td nowrap="nowrap" colspan="6" class="statusButtonBar">
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

					<tr id="detailTdId_${issue?.item?.id}" style="display: none">
						<td colspan="6">
							<div id="detailId_${issue?.item?.id}" style="width: 100%">
							</div>
						</td>
					</tr>
				</g:each>
			</tbody>
		</table>
	</div>
</div>
<script type="text/javascript">
	if('${tab}'=="todo"){
		$("#toDOSpanId").html(${todoSize})
		$("#toDOAllSpanId").html(${allSize})
	}else{
		$("#allToDoSpanId").html(${todoSize})
		$("#allSpanId").html(${allSize})
	}
</script>