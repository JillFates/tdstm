<div class="menu4">
	<ul>
		<g:if test="${tab && tab == 'todo'}">
			<li><g:link elementId="taskLinkId"  class="mobmenu mobselect" action="listComment" params='["tab":"todo","search":search]'>My Tasks: ${todoSize}</g:link></li>
			<li><g:link elementId="taskLinkAllId"  class="mobmenu" action="listComment" params='["tab":"all","search":search]'>All Tasks: ${allSize}</g:link></li>
		</g:if>
		<g:if test="${tab && tab == 'all'}">
			<li><g:link elementId="taskLinkId"  class="mobmenu" action="listComment" params='["tab":"todo","search":search]'>My Tasks: ${todoSize}</g:link></li>
			<li><g:link elementId="taskLinkAllId"  class="mobmenu mobselect" action="listComment" params='["tab":"all","search":search]'>All Tasks: ${allSize}</g:link></li>
		</g:if>
	</ul>
	<div class="tab_search">
      	<g:form method="post" name="issueAssetForm" action="showIssue">
		<input type="hidden" name="sort" value="${sort}">
		<input type="hidden" name="order" value="${order}">
		<input type="text" size="08" value="${search}" id="search" name="search" autocorrect="off" autocapitalize="off" onfocus="changeAction()" onblur="retainAction()" />
	</div>
</div>
<div class="issueTimebar" id="issueTimebar"><div id="issueTimebarId" ></div></div>
<div id="detailId"  style="display: none;position: absolute;width: 420px;margin-top: 40px;" > </div>
<div id="myIssueList" class="mobbodyweb" style="width: 220px;">
	<input id="issueId" name="issueId" type="hidden" value="" />
	<input name="tab" id="tab_m" type="hidden" value="${tab}" />								              	
	<div id="mydiv" onclick="this.style.display = 'none';setFocus();">						            
		<g:if test="${flash.message}">
			<br />
			<div class="message"><ul>${flash.message}</ul></div>
		</g:if> 
	</div>		
	<div id="taskId" style="float:left; width:220px; margin:2px 0; "></div>
	<div id="assetIssueDiv" style="float:left;width:220px;">
		<table id="issueTable" style="width:220px;" cellspacing="0px">
			<thead>
				<tr>
					<g:sortableColumn class="sort_column" style="width:60px;"  action="listComment" property="number_comment" title="Task" params="['tab':tab,'search':search]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="width:60px;" action="listComment" property="assetEntity" title="Related" params="['tab':tab,'search':search]"></g:sortableColumn>
					<g:sortableColumn class="sort_column" style="width:60px;" action="listComment" property="status" title="Status" params="['tab':tab,'search':search]"></g:sortableColumn>
				</tr>
			</thead>
			<tbody>
			<g:each status="i" in="${listComment}" var="issue" >
				<g:if test="${tab && tab == 'todo'}">
				<tr id="issueTrId_${issue?.item?.id}" class="${issue.css}" style="cursor: pointer;" onclick="openStatus(${issue?.item?.id},'${issue?.item?.status}')">
			  	</g:if>
			  	<g:else>
				<tr id="issueTr_${issue?.item?.id}" class="${issue.css}" style="cursor: pointer;" onclick="issueDetails(${issue?.item?.id})">
			  	</g:else>
					<td id="comment_${issue?.item?.id}" class="asset_details_block">
						${issue?.item?.taskNumber ? issue?.item?.taskNumber+' - ' : ''}${com.tdssrc.grails.StringUtil.ellipsis(issue?.item?.comment,25)}
					</td>
					<td id="asset_${issue?.item?.id}"class="asset_details_block">${issue?.item?.assetName}</td>
					<td id="statusTd_${issue?.item?.id}"id="statusTd_${issue?.item?.id}" class="asset_details_block">${issue?.item?.status}</td>
				</tr>
				<g:if test="${tab && tab == 'todo'}">
				<tr id="showStatusId_${issue?.item?.id}" style="display: none;" nowrap="nowrap"> 
					<td nowrap="nowrap" colspan="3" class="statusButtonBar" >
						<a class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary task_action" style="margin-left: 5px"
						 id="started_${issue?.item?.id}" onclick="changeStatus('${issue?.item?.id}','Started','${userId}')">
							<span class="ui-button-icon-primary ui-icon ui-icon-play task_icon"></span>
							<span class="ui-button-text task_button">Start</span>
						</a>
						<a class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary task_action"
						 onclick="changeStatus('${issue?.item?.id}','Completed','${userId}')" style="margin-left: 5px">
							<span class="ui-button-icon-primary ui-icon ui-icon-check task_icon"></span>
							<span class="ui-button-text task_button">Complete</span>
						</a>
						<a class="ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary task_action" style="margin-left: 5px" onclick="issueDetails(${issue?.item?.id})">
							<span class="ui-button-icon-primary ui-icon ui-icon-play task_icon"></span>
							<span class="ui-button-text task_button">Details..</span>
						</a>
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
