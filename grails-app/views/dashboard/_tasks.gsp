<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus"%>
<%@page import="com.tds.asset.AssetComment"%>
<%@page import="com.tdssrc.grails.TimeUtil"%>
<%@page import="com.tdssrc.grails.HtmlUtil"%>
<g:set var="now" value="${TimeUtil.nowGMT()}" />
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
							style="width: 50% !important;">
							${issue?.projectName}
						</td>
					</g:if>
					<td id="comment_${item?.id}"
						class="actionBar asset_details_block_task"
						data-itemId="${item?.id}" data-status="${item?.status}"
						style="width: 50% !important;">
						${item?.taskNumber?item?.taskNumber+' - ' : ''} ${item?.comment}
					</td>
					<td id="asset_${item?.id}" class="asset_details_block"
						${item?.assetName ? 'onclick="getEntityDetails(\'myIssues\',\''+item?.assetType+'\',\''+item?.assetId+'\')"' : ''}>
						${item?.assetName}
					</td>
					<td id="estFinish_${item?.id}" data-itemId="${item?.id}"
						data-status="${item?.status}"
						class="actionBar asset_details_block ${item?.dueDate && item?.dueDate < TimeUtil.nowGMT() ? 'task_overdue' : ''}">
						<tds:convertDate date="${item?.estFinish}"
							timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"
							format="MM/dd kk:mm" />
					</td>

					<td id="statusTd_${item?.id}" class="actionBar asset_details_block"
						data-itemId="${item?.id}" data-status="${item?.status}">
						${item?.status} <% // (${formatter.format(item?.score?: 0)}) %>
					</td>
				</tr>
				<tr id="showStatusId_${item?.id}"
					${(todoSize!=1||search==''||search==null) ? 'style="display: none"' :''}>
					<td nowrap="nowrap" colspan="6" class="statusButtonBar"><g:if
							test="${issue.item.status == AssetCommentStatus.READY}">
							<tds:actionButton label="Start" icon="ui-icon-play"
								id="${item?.id}"
								onclick="changeStatus('${item?.id}','${AssetCommentStatus.STARTED}', '${item?.status}', 'taskManager')" />
						</g:if> <g:if
							test="${ [AssetCommentStatus.READY, AssetCommentStatus.STARTED].contains(issue.item.status) }">
							<tds:actionButton label="Done" icon="ui-icon-check"
								id="${item?.id}"
								onclick="changeStatus('${item?.id}','${AssetCommentStatus.DONE}', '${item?.status}', 'taskManager')" />
						</g:if> <tds:actionButton label="Details..." icon="ui-icon-zoomin"
							id="${item?.id}" onclick="showAssetComment(${item?.id}, 'show')" />
						<g:if test="${item.successors > 0 || item.predecessors > 0}">
							<tds:actionButton label="View Graph" icon="ui-icon-zoomin"
								id="${item?.id}"
								onclick="window.open('${ HtmlUtil.createLink([controller:'task',action:'neighborhoodGraph', id: item?.id]) }','_blank');" />
						</g:if> <g:if
							test="${ personId != issue.item.assignedTo && issue.item.status in [AssetCommentStatus.PENDING, AssetCommentStatus.READY, AssetCommentStatus.STARTED]}">
							<tds:actionButton label="Assign To Me" icon="ui-icon-person"
								id="${item?.id}"
								onclick="assignTask('${item?.id}','${issue.item.assignedTo}', '${issue.item.status}','myTask')" />
						</g:if> <tds:hasPermission permission='CommentCrudView'>
							<g:if
								test="${issue.item.status == AssetCommentStatus.READY && !(item.category in AssetComment.moveDayCategories)}">
								<span class="delay_myTasks">Delay for:</span>
								<tds:actionButton label="1 day" icon="ui-icon-seek-next"
									id="${item?.id}"
									onclick="changeEstTime(1,'${item?.id}', this.id)" />
								<tds:actionButton label="2 days" icon="ui-icon-seek-next"
									id="${item?.id}"
									onclick="changeEstTime(2,'${item?.id}', this.id)" />
								<tds:actionButton label="7 days" icon="ui-icon-seek-next"
									id="${item?.id}"
									onclick="changeEstTime(7,'${item?.id}', this.id)" />
							</g:if>
						</tds:hasPermission></td>
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
		${taskList.size()} assigned tasks with ${timeInMin} minutes of effort. ${ dueTaskCount ? '(' +  dueTaskCount +'  are over due.)' : ''}
</span>
