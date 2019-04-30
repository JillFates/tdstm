<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus"%>
<%@page import="net.transitionmanager.task.AssetComment"%>
<%@page import="com.tdssrc.grails.TimeUtil"%>
<%@page import="com.tdssrc.grails.HtmlUtil"%>
<%@page defaultCodec="html" %> 

<g:set var="now" value="${TimeUtil.nowGMT()}" />
<g:set var="tableColCount" value="${project=='All'?5:4}" />
<div id="assetIssueDiv" class="taskSummaryScrolableTable">
	<table id="issueTable" cellspacing="0px">
		<thead>
			<tr id="taskThId">
				<g:if test="${project=='All' }">
					<th>Project </th>
				</g:if>
				<th>Task</th>
				<th>Related</th>
				<th>Due/Est Finish</th>
				<th>Status</th>
			</tr>
		</thead>
		<tbody id="tasksTableId">

			<g:each status="i" in="${taskList}" var="issue">
				<g:set var="item" value="${issue?.item}" />
				<tr id="issueTrId_${item?.id}" class="${issue.css}"
					style="cursor: pointer;">
					<g:if test="${project=='All' }">
						<td id="comment_${item?.id}"
							class="actionBar asset_details_block_task"
							data-itemId="${item?.id}" data-status="${item?.status}"
							style="width: auto !important;"
							action-bar-cell config-table="config.table" comment-id="${item?.id}" asset-id="${item?.assetId}" status="${item?.status}" id-prefix="issueTrId_" master="true" table-col-span="${tableColCount}">
							${issue?.projectName}
						</td>
					</g:if>
					<td id="comment_${item?.id}"
						class="actionBar asset_details_block_task"
						data-itemId="${item?.id}" data-status="${item?.status}"
						style="width: auto !important;"
						action-bar-cell config-table="config.table" comment-id="${item?.id}" asset-id="${item?.assetId}" status="${item?.status}" id-prefix="issueTrId_" master="false" table-col-span="${tableColCount}">
						${item?.taskNumber?item?.taskNumber+' - ' : ''} ${item?.comment}
					</td>
					<td id="asset_${item?.id}" class="asset_details_block"
						${item?.assetName ? 'onclick="EntityCrud.showAssetDetailView(\''+item?.assetClass+'\',\''+item?.assetId+'\')"' : ''}>
						${item?.assetName}
					</td>
					<td id="estFinish_${item?.id}" data-itemId="${item?.id}"
						data-status="${item?.status}"
						class="actionBar asset_details_block ${item?.dueDate && item?.dueDate < TimeUtil.nowGMT() ? 'task_overdue' : ''}"
						action-bar-cell config-table="config.table" comment-id="${item?.id}" asset-id="${item?.assetId}" status="${item?.status}" id-prefix="issueTrId_" master="false" table-col-span="${tableColCount}">
						<tds:convertDate date="${item?.estFinish}" format="MM/dd kk:mm" />
					</td>

					<td id="statusTd_${item?.id}" class="actionBar asset_details_block"
						data-itemId="${item?.id}" data-status="${item?.status}"
						action-bar-cell config-table="config.table" comment-id="${item?.id}" asset-id="${item?.assetId}" status="${item?.status}" id-prefix="issueTrId_" master="false" table-col-span="${tableColCount}">
						${item?.status} <% // (${formatter.format(item?.score?: 0)}) %>
					</td>
				</tr>

				<tr id="detailTdId_${item?.id}" style="display: none">
					<td colspan="6">
						<div id="detailId_${item?.id}" style="width: 100%"></div>
					</td>
				</tr>
			</g:each>
		</tbody>
	</table>
</div>
<span class="leftFloated effort">
	<g:if test="${taskList.size() == 0}">
		No active tasks were found.
	</g:if>
	<g:else>
		${taskList.size()} assigned tasks with ${TimeUtil.ago(totalDuration, TimeUtil.SHORT)} minutes of duration ${ dueTaskCount ? '(' +  dueTaskCount +'  are over due)' : ''}.
	</g:else>
</span>
