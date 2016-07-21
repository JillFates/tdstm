<%@page import="com.tds.asset.AssetComment"%>
<%@page import="com.tdssrc.grails.TimeUtil"%>
<%@page import="com.tdssrc.grails.StringUtil"%>
<%@page import="com.tdssrc.grails.HtmlUtil"%>
<%@page import="com.tdsops.tm.enums.domain.AssetCommentType"%>
<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
	<meta name="layout" content="topNav" />
	<title>Task Manager</title>
	<g:javascript src="asset.tranman.js" />
	<g:javascript src="asset.comment.js" />
	<g:javascript src="entity.crud.js" />
	<g:javascript src="projectStaff.js" />
	<g:javascript src="model.manufacturer.js"/>
	<jqgrid:resources />
	<g:javascript src="jqgrid-support.js" />
	<g:javascript src="TimerBar.js" />

	<g:render template="../layouts/responsiveAngularResources" />

	<link type="text/css" rel="stylesheet" href="${g.resource(dir:'css',file:'ui.datepicker.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css',file:'ui.datetimepicker.css')}" />
	<link type="text/css" rel="stylesheet" href="${resource(dir:'css/jqgrid',file:'ui.jqgrid.css')}" />

	<script type="text/javascript">
		var timerBar;
		
		$(document).ready(function() {
		
			timerBar = new TimerBar(0, 'RefreshTaskMgr', function () {
				reloadGrid();
				timerBar.resetTimer();
			});
			
			$('#assetMenu').show();
			$("#showEntityView").dialog({ autoOpen: false })
			$("#editEntityView").dialog({ autoOpen: false })
			$("#createEntityView").dialog({ autoOpen: false })
			$("#editManufacturerView").dialog({ autoOpen: false})
			$("#manufacturerShowDialog").dialog({ autoOpen: false })
			$("#modelShowDialog").dialog({ autoOpen: false })
			$("#cablingDialogId").dialog({ autoOpen:false })
			$("#createStaffDialog").dialog({ autoOpen: false })
			currentMenuId = "#assetMenu";
			$(".menu-parent-tasks-task-manager").addClass('active');
			$(".menu-parent-tasks").addClass('active');
			$("#viewGraphSpanId").css('margin-left',$(window).width()*3.3/100+'%')
			
			var event = ${filterEvent}
			var justRemaining = ${justRemaining}
			var justMyTasks = ${justMyTasks}
			var viewUnpublished = ${viewUnpublished}
			var filter = '${filter}'
			var comment = '${comment}'
			var taskNumber = '${taskNumber}'
			var assetEntity = '${assetName}'
			var assetType = '${assetType}'
			var dueDate = '${dueDate}'
			var status = '${status}'
			var assignedTo = '${assignedTo}'
			var sendNotification = '${sendNotification}'
			var role = '${role}'
			var category = '${category}'
			var sizePref = '${sizePref}'
			var status = '${status}'
			var step = "${step}"
			var windowWidth = $(window).width() - $(window).width()*5/100 ;
			var taskManagerUrl = "/assetEntity/listTasks"
			var listCaption ="Tasks: \
				<tdsactionbutton id='createTask' label='Create Task' icon='/icons/table_add.png' link='' ng-click='comments.createCommentBy(\"${AssetCommentType.TASK}\",\"\",\"\")'></tdsactionbutton>&nbsp; \
				<tdsactionbutton id='bulkEdit' label='Bulk Edit' icon='' link='' ng-click='comments.bulkEditTasks()'></tdsactionbutton>\
				<tdsactionbutton id='clearFilters' icon='' label='Clear Filters' link='" + taskManagerUrl + "'></tdsactionbutton>"

			<jqgrid:grid id="taskListId"  url="'${createLink(action: 'listTaskJSON')}'"
				colNames="'Action', 'Task', 'Description', '${modelPref['1']}', '${modelPref['2']}', 'Updated', 'Due', 'Status',
					'${modelPref['3']}', '${modelPref['4']}', '${modelPref['5']}', 'Suc.', 'Score', 'id', 'statusCss'"
				colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false, width:50, fixed:true},
					{name:'taskNumber', formatter:taskFormatter, width:60, fixed:true},
					{name:'comment', width:680, formatter:taskFormatter},
					{name:'${taskPref['1']}', formatter:assetFormatter, width:200},
					{name:'${taskPref['2']}', formatter:taskFormatter, width:200},
					{name:'updated', formatter: updatedFormatter,sortable:false,search:false},
					{name:'dueDate', formatter: dueFormatter},
					{name:'status', formatter: statusFormatter},
					{name:'${taskPref['3']}', formatter:taskFormatter, width:200},
					{name:'${taskPref['4']}', formatter:taskFormatter, width:200},
					{name:'${taskPref['5']}', formatter:taskFormatter, width:200},
					{name:'suc', formatter:taskFormatter,sortable:false,search:false, width:50},
					{name:'score', formatter:taskFormatter, search:false, width:70},
					{name:'id', hidden: true},
					{name:'statusCss', hidden: true}"
				caption="listCaption"
				rowNum="sizePref"
				scrollOffset="0"
				gridComplete="function(){ processTaskSafariColumns(); bindResize('taskListId');recompileDOM('taskListIdWrapper');}"
				postData="{moveEvent:event, justRemaining:justRemaining, justMyTasks:justMyTasks, filter:filter, comment:comment, taskNumber:taskNumber,
					assetEntity:assetEntity, assetType:assetType, dueDate:dueDate, status:status, assignedTo:assignedTo, role:role, category:category, viewUnpublished : viewUnpublished, step:step }"
				showPager="true">
				<jqgrid:filterToolbar id="taskListId" searchOnEnter="false" />
				<jqgrid:navigation id="taskListId" add="false" edit="false" del="false" search="false" refresh="false" />
				<jqgrid:refreshButton id="taskListId" />
			</jqgrid:grid>

			populateFilter();

			<g:each var="key" in="['1','2','3','4','5']">
				var taskPref= '${taskPref[key]}';
				
				$("#taskListIdGrid_"+taskPref).append("<img src=\"${resource(dir:'images',file:'select2Arrow.png')}\" class=\"selectImage customizeSelect editSelectimage_"+${key}+"\" onclick=\"showSelect('"+taskPref+"','taskList','"+${key}+"')\">");
			</g:each>
		})
		
		$.jgrid.formatter.integer.thousandsSeparator='';
		
		function checkSelectedEvent (event) {
			var moveEvent = $("#moveEventId").val();
			if (moveEvent == '0') {
				alert("Please select an event first.");
				event.preventDefault();
			}
			if ($('#viewUnpublishedCB').size() > 0) {
				var href = $('#viewtaskgraph_button_graph').attr('href');
				if (href.indexOf('&viewUnpublished=') != -1) {
					href = href.replace(/\&viewUnpublished\=[a-z]*/, '');
					$('#viewtaskgraph_button_graph').attr('href', href);
				}
				$('#viewtaskgraph_button_graph').attr('href', href + '&viewUnpublished=' + $('#viewUnpublishedCB').is(':checked'));
			}
		};

		function myCustomFormatter (cellVal,options,rowObject) {
			var editButton = '<a ng-click="comments.editCommentById(\''+options.rowId+'\',\'task\')">'+
				"<img src='${resource(dir:'icons',file:'table_edit.png')}' border='0px'/>"+"</a>&nbsp;&nbsp;"
			return editButton
		}
		function isPublishedFormatter(cellVal,options,rowObject) {
			return '<span class="cellWithoutBackground pointer" id="span_'+options.rowId+'" >' + (rowObject[17] ? rowObject[17] : "false") + '</span>';
		}
		function taskFormatter(cellVal,options,rowObject) {
			return '<span class="cellWithoutBackground pointer" id="' + options.colModel.name + '_'+options.rowId+'" action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[16]+'" status="'+rowObject[7]+'" instructions-link="'+rowObject[19]+'">' + (cellVal || cellVal == 0 ? _.escape(cellVal) :"") + '</span>';
		}
		function statusFormatter(cellVal,options,rowObject){
			return '<span id="status_'+options.rowId+'" class="cellWithoutBackground '+rowObject[13] +' " action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[16]+'" status="'+rowObject[7]+'" instructions-link="'+rowObject[19]+'">' + cellVal + '</span>';
		 }
		
		function updatedFormatter(cellVal,options,rowObject){
			 return '<span id="span_'+options.rowId+'" class="cellWithoutBackground '+rowObject[14] +'" action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[16]+'" status="'+rowObject[7]+'" instructions-link="'+rowObject[19]+'">' + cellVal + '</span>';
		}
		function dueFormatter(cellVal,options,rowObject){
			return '<span id="span_'+options.rowId+'" class=" '+rowObject[15] +'" master="true" action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[16]+'" status="'+rowObject[7]+'" instructions-link="'+rowObject[19]+'">' + cellVal + '</span>';
		}
		function assetFormatter(cellVal,options,rowObject){
			return options.colModel.name == "assetName" && cellVal ? '<span class="cellWithoutBackground pointer" onclick= "EntityCrud.showAssetDetailView(\''+rowObject[18]+'\', '+rowObject[16]+')\" >' + _.escape(cellVal) + '</span>' :
				(cellVal || cellVal == 0 ? taskFormatter(cellVal,options,rowObject) : "<span class='cellWithoutBackground pointer'></span>")
		}
		
		function instructionsLinkFormatter(cellVal,options,rowObject){
			return '<span id="status_'+options.rowId+'" class="cellWithoutBackground '+rowObject[13] +' " action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[16]+'" status="'+rowObject[19]+'" instructions-link="'+rowObject[7]+'">' + cellVal + '</span>';
		}
		
		function populateFilter(){
			$("#gs_comment").val('${comment}')
			$("#gs_taskNumber").val('${taskNumber}')
			$("#gs_assetEntity").val('${assetName}')
			$("#gs_assetType").val('${assetType}')
			$("#gs_dueDate").val('${dueDate}')
			$("#gs_status").val('${status}')
			$("#gs_assignedTo").val('${assignedTo}')
			$("#gs_role").val('${role}')
			$("#gs_category").val('${category}')
		}
		$(document).keyup(function(e) {
			// esc to stop timer
			if (e.keyCode == 27) {
				timerBar.resetTimer();
			}
		});
		
		function reloadGrid () {
			var postData = $('#taskListIdGrid').jqGrid('getGridParam', 'postData');
			postData.justRemaining = $('#justRemainingCB').is(':checked') ? 1 : 0;
			postData.justMyTasks = $('#justMyTasksCB').is(':checked') ? 1 : 0;
			postData.viewUnpublished = viewUnpublished ? 1 : 0;
			$('#taskListId').trigger('reloadGrid').trigger('click')
		}
	</script>
</head>
<body>
	<input type="hidden" id="timeBarValueId" value="0"/>
	<div id="outerBodyId" class="body" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
		<input type="hidden" id="timeBarValueId" value="0"/>
		<div class="taskTimebar hide" id="issueTimebar" >
			<div id="issueTimebarId"></div>
		</div>
		<div class="body fluid">
			<h1>Task Manager</h1>
			<g:if test="${flash.message}">
				<div class="message">${flash.message}</div>
			</g:if>
			<div class="alert alert-{{alert.type}}" ng-repeat="alert in alerts.list" ng-class="{animateShow: !alert.hidden}">
				<button type="button" class="alert-close" aria-hidden="true" ng-click="alerts.closeAlert($index)">&times;</button>
				<span ng-bind="alert.msg"></span>
			</div>
			<div id="taskMessageDiv" class="message" style="display: none;"></div>
			<input type="hidden" id="manageTaskId" value="manageTask"/>
			<form name="commentForm" id="commentForm" method="post" action="listTasks">
			<input type="hidden" name="justRemaining" id="justRemaining" value="${justRemaining}" />
			<input type="hidden" name="justMyTasks" id="justMyTasks" value="${justMyTasks}"/>
			<input type="hidden" name="viewUnpublished" id="viewUnpublished" value="${viewUnpublished}"/>
			<input type="hidden" id="myPage" value="taskManager" />
			<span id="controlRowId">
				<b>Event </b>
			 	<g:select from="${moveEvents}" name="moveEvent" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' All']}" value="${filterEvent}" onchange="submitForm()" />
				&nbsp;&nbsp;
				<span class="checkboxContainer">
					<input type="checkbox" id="justRemainingCB" class="pointer" ${ (justRemaining == '1' ? 'checked="checked"': '') } onclick="reloadGrid()" /><!--
					--><label for="justRemainingCB" class="pointer"><b>&nbsp;Just Remaining</b></label>&nbsp;&nbsp;
				</span>
				<span class="checkboxContainer">
					<input type="checkbox" id="justMyTasksCB" class="pointer" ${ (justMyTasks=='1' ? 'checked="checked"' : '') } onclick="reloadGrid()"/><!--
					--><label for="justMyTasksCB" class="pointer"><b>&nbsp;Just Mine</b></label>&nbsp;&nbsp;
				</span>
				<tds:hasPermission permission="PublishTasks">
					<span class="checkboxContainer">
						<input type="checkbox" id="viewUnpublishedCB" class="pointer" ${ (viewUnpublished=='1' ? 'checked="checked"' : '') } onclick="toggleViewUnpublished(event);"/><!--
						--><label for="viewUnpublishedCB" class="pointer"><b>&nbsp;View Unpublished</b></label>&nbsp;&nbsp;
					</span>
				</tds:hasPermission>

				<span style="float: right">
					<span style="margin-right: 30px;">
						<tdsactionbutton id="graph" label="View Task Graph" icon="/icons/tds_task_graph.png" link="/task/taskGraph?moveEventId=${filterEvent}" click="checkSelectedEvent"></tdsactionbutton>&nbsp;

						<tdsactionbutton id="timeline" label="View Timeline" icon="/icons/timeline_marker.png" link="/task/taskTimeline"></tdsactionbutton>&nbsp;
					</span>
					<g:render template="../assetEntity/progressTimerControls" model="${[timerValues:[60, 120, 180, 240, 300]]}"/>
				</span>
			</span>
			<jqgrid:wrapper id="taskListId" />
		</div>
		<input type="hidden" id="customizeFieldCount" value="6" />
		<g:each var="key" in="['1','2','3','4','5']">
			<div id="columnCustomDiv_${taskPref[key]}" style="display:none;">
				<div class="columnDiv_${key} customScroll customizeDiv">
					<input type="hidden" id="previousValue_${key}" value="${taskPref[key]}" />
					<g:each var="attribute" in="${attributesList}">
						<label><input type="radio" name="coloumnSelector_${taskPref[key]}" id="coloumnSelector_${taskPref[key]}" value="${attribute}" 
							${taskPref[key]==attribute ? 'checked' : '' } 
							onchange="setColumnAssetPref(this.value,'${key}','Task_Columns')"/> ${attribute}</label><br>
					</g:each>
				</div>
			</div>
		</g:each>
			
		<g:render template="../assetEntity/modelDialog" />
		<g:render template="../assetEntity/entityCrudDivs" />
		<g:render template="../assetEntity/dependentAdd" />
		
		<g:render template="../layouts/error"/>
	</div>
	<div id="createStaffDialog" style="display:none;" class="static-dialog">
		<g:render template="../person/createStaff" model="['forWhom':'application']"></g:render>
	</div>
 <g:render template="initAssetEntityData"/>
 <script type="text/javascript">
	function toggleCheckbox (chkbox, field) {
		$('input[name='+field+']').val(chkbox.checked ? '1' : '0')
		submitForm()
	}
	function toggleViewUnpublished (e) {
		var checkedValue = $(e.srcElement).is(':checked');
		viewUnpublished = checkedValue;
		setUserPreference('viewUnpublished', checkedValue, function () {
			reloadGrid();
		});
	}
	function submitForm () {
		$('#commentForm').submit()
	}

	function loadGrid () {
		$(".ui-icon-refresh").click()
	}
</script>
</body>

</html>