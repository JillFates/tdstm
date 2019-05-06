<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus"%>
<%@page import="net.transitionmanager.task.AssetComment"%>
<%@page import="com.tdssrc.grails.TimeUtil"%>
<%@page import="com.tdssrc.grails.HtmlUtil"%>
<g:set var="now" value="${TimeUtil.nowGMT()}" />

<div>
	<div style="margin-top: 3%;">
		<div style="width: 100%; float: left;">

			<div class="eventOuterDiv">
				<h4 class="leftFloated user_dashboard_head">Events&nbsp;</h4>&nbsp;
				<span class="descriptionText">Your assigned events and your team <input type="button" value="Refresh" onclick="loadEventTable()"/></span>
				<br><br>
				<div class="eventScrolableTable">
					<table class="fullWidth" cellspacing="0px" id="eventTable">
						<thead>
							<tr id="eventThId">
								<g:if test="${project=='All' }">
									<th>Project</th>
								</g:if>
								<th>Name</th>
								<th>Start Date</th>
								<th>Days</th>
								<th>Teams</th>
							</tr>
						</thead>
						<tbody id="eventTableId"></tbody>
					</table>
				</div>
			</div>
			<div class="eventNewsDiv">
				<h4 class="eventNewsHeader user_dashboard_head">Event News</h4>
				<span class="descriptionText">&nbsp;Active news for your events <input type="button" value="Refresh" onclick="loadEventNewsTable()"/></span>
				<br>
				<br>
				<div class="eventNewsScrolableTable">
					<table class="fullWidth" cellspacing="0px" id="eventNewsTable">
						<thead>
							<tr id="eventNewsThId">
								<th>Date</th>
								<th>Event</th>
								<th>News</th>
							</tr>
						</thead>
						<tbody id="eventNewsTableId"></tbody>
					</table>
				</div>
			</div>
		</div>


		<div class="fullWidth">
			<div class="taskSummaryOuterDiv">
				<h4 class="leftFloated user_dashboard_head">Task Summary&nbsp;</h4>
				<span class="descriptionText">Active tasks assigned to you <input type="button" value="Refresh" onclick="loadTasksTable()"/></span>
				<br>
				<div id="myTaskList">
				</div>
					<span class="leftFloated effort" id="taskNews"></span>
				</div>
			</div>
		</div>
	</div>
	<div class="appOuterDiv">
		<div>
			<h4 class="eventNewsHeader user_dashboard_head">Application&nbsp;</h4>
			<span class="descriptionText">Your applications as a SME or Owner <input type="button" value="Refresh" onclick="loadAppTable()"/></span>
			<br>
			<div class="appScrolableTable">
				<table class="fullWidth" cellspacing="0px" id="appTable">
					<thead>
						<tr id="appThId">
							<th>Name</th>
							<th>PlanStatus</th>
							<th>Bundle</th>
							<th>Relation</th>
						</tr>
					</thead>
					<tbody id="appTableId"></tbody>
				</table>
			</div>
		</div>
		<div class="activepplOuterDiv">
			<h4 class="activepplHeader  user_dashboard_head">Active People&nbsp;</h4>
			<span class="descriptionText">Currently active people on this project <input type="button" value="Refresh" onclick="loadActivepplTable()"/></span></span>	
			<br>
			<div class="appScrolableTable">
				<table class="fullWidth" cellspacing="0px">
					<thead>
						<tr >
							<th>Project</th>
							<th>Name</th>
						</tr>
					</thead>
					<tbody id="actpplTableId"></tbody>
				</table>
			</div>
		</div>
	</div>
	
<div id="relatedEntitiesId"></div>
