<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus" %>
<%@page import="com.tds.asset.AssetComment" %>
<%@page import="com.tdssrc.grails.TimeUtil"%>
<%@page import="com.tdssrc.grails.HtmlUtil"%>
<g:set var="now" value="${TimeUtil.nowGMT()}" />

<div>
	<div style="margin-top:3%;postion:absolute;">
		<div>
		 	<h4 style="float:left;">Upcoming Events</h4><br><br>
		         <table style="height: 50%;width: 50%;">
		          <thead>
		          		<tr>
			           <th>Name</th>
			           <th>Project</th>
			           <th>Start Date</th>
			           <th>Days</th>
			           <th>Teams</th>
			        </tr>
		        	   </thead>
		        	   <tbody>
		         	   <g:each in="${upcomingEvents.keySet()}" var="event">
		         	   <g:set var="moveEvent" value="${upcomingEvents[event].moveEvent}"/>
		         	   		<tr>
			          	   		<td><g:link action="index" parmas="[moveEvent:'${moveEvent.id}']">${moveEvent.name}</g:link></td>
			          	   		<td>${moveEvent.project.name}</td>
			          	   		<td>${moveEvent.eventTimes.start}</td>
			          	   		<td>${upcomingEvents[event]?.daysToGo+' days'}</td>
			          	   		<td>${upcomingEvents[event]?.teams}</td>
		         	   		</tr>
		         	   	</g:each>
		        	   </tbody>
		         </table>
		   </div>
		 <div style="font-size:18px; margin-top:3%;">
  			 <h4 style="float:left;">Task Summary </h4><br><br>
	    	 <div id="myTaskList">
	           	<div id="assetIssueDiv" style="float: left; width: 50%;">
					<table id="issueTable" cellspacing="0px" style="width: 100%; margin-left: -1px;">
						<thead>
							<tr>
								<th>Task</th>
								<th>Related</th>
								<th>Due/Est Finish</th>
								<th>status</th>
							</tr>
						</thead>
						<tbody>
							<g:each status="i" in="${taskList}" var="issue">
								<g:set var="item" value="${issue?.item}"/>
								<tr id="issueTrId_${item?.id}" class="${issue.css}" style="cursor: pointer;">
									<td id="comment_${item?.id}" class="actionBar asset_details_block_task" 
										data-itemId="${item?.id}" data-status="${item?.status}" style="width: 50% !important;">
										${item?.taskNumber?item?.taskNumber+' - ' : ''}
										${item?.comment}
									</td>
									<td id="asset_${item?.id}" class="asset_details_block"
									${item?.assetName ? 'onclick="getEntityDetails(\'myIssues\',\''+item?.assetType+'\',\''+item?.assetId+'\')"' : ''}>
										${item?.assetName} 
									</td>
									<td id="estFinish_${item?.id}" data-itemId="${item?.id}" data-status="${item?.status}"
										class="actionBar asset_details_block ${item?.dueDate && item?.dueDate < TimeUtil.nowGMT() ? 'task_overdue' : ''}">
											<tds:convertDate date="${item?.estFinish}" timeZone="${request.getSession().getAttribute('CURR_TZ')?.CURR_TZ}"
												format="MM/dd kk:mm" />
									</td>
									
									<td id="statusTd_${item?.id}" class="actionBar asset_details_block"
										data-itemId="${item?.id}" data-status="${item?.status}">
										${item?.status}<% // (${formatter.format(item?.score?: 0)}) %>
									</td>
								</tr>
								<tr id="showStatusId_${item?.id}" ${(todoSize!=1||search==''||search==null) ? 'style="display: none"' :''}>
									<td nowrap="nowrap" colspan="6" class="statusButtonBar">
										<g:if test="${issue.item.status == AssetCommentStatus.READY}"> 
										<tds:actionButton label="Start" icon="ui-icon-play" id="${item?.id}"  
											onclick="changeStatus('${item?.id}','${AssetCommentStatus.STARTED}', '${item?.status}', 'taskManager')"/>
										</g:if>
										<g:if test="${ [AssetCommentStatus.READY, AssetCommentStatus.STARTED].contains(issue.item.status) }"> 
										<tds:actionButton label="Done" icon="ui-icon-check" id="${item?.id}"  
											onclick="changeStatus('${item?.id}','${AssetCommentStatus.DONE}', '${item?.status}', 'taskManager')"/>
										</g:if>
			
										<tds:actionButton label="Details..." icon="ui-icon-zoomin" id="${item?.id}"  
											onclick="issueDetails(${item?.id},'${item?.status}')"/>
			
										<g:if test="${item.successors > 0 || item.predecessors > 0}">
											<tds:actionButton label="View Graph" icon="ui-icon-zoomin" id="${item?.id}"  
												onclick="window.open('${ HtmlUtil.createLink([controller:'task',action:'neighborhoodGraph', id: item?.id]) }','_blank');"  
											/>
										</g:if>
										<g:if test="${ personId != issue.item.assignedTo && issue.item.status in [AssetCommentStatus.PENDING, AssetCommentStatus.READY, AssetCommentStatus.STARTED]}">
										<tds:actionButton label="Assign To Me" icon="ui-icon-person" id="${item?.id}"  
											onclick="assignTask('${item?.id}','${issue.item.assignedTo}', '${issue.item.status}','myTask')"/>
										</g:if>
										<tds:hasPermission permission='CommentCrudView'>
											<g:if test="${issue.item.status == AssetCommentStatus.READY && !(item.category in AssetComment.moveDayCategories)}">
												<span class="delay_myTasks">Delay for:</span>
												<tds:actionButton label="1 day" icon="ui-icon-seek-next" id="${item?.id}"  
													onclick="changeEstTime(1,'${item?.id}', this.id)"/>
												<tds:actionButton label="2 days" icon="ui-icon-seek-next" id="${item?.id}"  
													onclick="changeEstTime(2,'${item?.id}', this.id)"/>
												<tds:actionButton label="7 days" icon="ui-icon-seek-next" id="${item?.id}"  
													onclick="changeEstTime(7,'${item?.id}', this.id)"/>
											</g:if>
										</tds:hasPermission>
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
			<b style="float:left;">${taskList.size()} assigned tasks with ${timeInMin} minutes of effort.</b>
		</div>
	</div>
	</div>
</div>
<div style="margin-top:-13.5%;">
	<div style="font-size: 18px;postion:absolute; margin-left: 51%;width: 50%;margin-top: -13.6%;">
		 			<h4 style="margin-left: -17%; width: 50%;">Event News</h4><br>
			     <div>
		           <table style="width: 100%;">
			           <thead>
			           		<tr>
					           <th>Event</th>
					           <th>Date</th>
					           <th>News</th>
					        </tr>
		          	   </thead>
		          	   <tbody>
			          	   <g:each in="${newsList}" var="news">
			          	   		<tr>
				          	   		<td><g:link action="index" parmas="[moveEvent:'${moveEvent.id}']">${news.moveEvent.name}</g:link></td>
				          	   		<td>${news.dateCreated}</td>
				          	   		<td>${news.message}</td>
			          	   		</tr>
			          	   	</g:each>
		          	   </tbody>
		           </table>
			     </div>
			</div>
	<div style="font-size: 18px; margin-left: 51%;width: 50%;margin-top:3%;">
	<h4 style="margin-left: -17%; width: 50%;">Application</h4><br>
	           <table style="width: 100%;">
		           <thead>
		           		<tr>
				           <th>Name</th>
				           <th>PlanStatus</th>
				           <th>Relation</th>
				           <th>Bundle</th>
				        </tr>
	          	   </thead>
	          	   <tbody>
		          	   <g:each in="${appList}" var="app">
		          	   		<tr onclick="getEntityDetails('myIssues','${app.assetType}',${app.id})">
			          	   		<td>${app.assetName}</td>
			          	   		<td>${app.planStatus}</td>
			          	   		<td>${relationList[app.id]}</td>
			          	   		<td>${app.moveBundle}</td>
		          	   		</tr>
		          	   	</g:each>
	          	   </tbody>
	           </table>
	</div>
	<div style="font-size: 18px; margin-left: 51%;width: 50%;margin-top:3%;">
	 <h4 style="float:left;">Active People</h4><br><br>
	     <div>
	           <table style="width: 100%;">
		           <thead>
		           		<tr>
				           <th>Name</th>
				           <th>Project</th>
				        </tr>
	          	   </thead>
	          	   <tbody>
		          	   <g:each in="${recentLogin.keySet()}" var="per">
		          	   		<tr>
			          	   		<td>${recentLogin[per].name.lastNameFirst}</td>
			          	   		<td>${recentLogin[per].project.name}</td>
		          	   		</tr>
		          	   	</g:each>
	          	   </tbody>
	           </table>
	     </div>
	</div>
</div>
</div>