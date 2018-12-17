<%@page import="com.tds.asset.AssetComment"%>
<%@page import="com.tdssrc.grails.TimeUtil"%>
<%@page import="com.tdssrc.grails.StringUtil"%>
<%@page import="com.tdssrc.grails.HtmlUtil"%>
<%@page import="com.tdsops.tm.enums.domain.AssetCommentType"%>
<%@page import="com.tdsops.tm.enums.domain.AssetCommentStatus"%>
<%@page import="net.transitionmanager.security.Permission"%>
<%@page import="com.tdsops.tm.enums.domain.UserPreferenceEnum"%>
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
	<g:javascript src="progressTimer.js" />
	<style>
		/*TODO: REMOVE ON COMPLETE MIGRATION */
		div.content-wrapper {
			background-color: #ecf0f5 !important;
		}
		/*TODO: TM-6499 Adding it here 'cause I don't want it as a part of the normal css*/
		.action-bar.checkboxContainer input[type=checkbox] {
			margin-left: 10px;
		}
		.action-bar.checkboxContainer label {
			margin-left: 4px;
		}
		div.action-bar-wrapper {
			margin-left: 0px;
			margin-right: 0px;
		}
		div.action-bar-wrapper #controlRowId {
			margin-bottom: 10px;
		}
		div.timeline-wrapper{
			width: 442px;
		}
		div.timeline-wrapper #timeline {
            margin-right: 21px;
        }

		div.content-wrapper div.ui-widget-header div.ui-jqgrid-view span input:disabled {
			color: #ccc !important;
		}
</style>

	<g:render template="../layouts/responsiveAngularResources" />

	<asset:stylesheet href="css/ui.datepicker.css" />
	<asset:stylesheet href="css/resources/ui.datetimepicker.css" />
	<asset:stylesheet href="css/jqgrid/ui.jqgrid.css" />

	<script type="text/javascript">

        var progressTimer;

		// Ordinal position of various properties in the rowObject
		var TASK_NUM=1;
		var STATUS=7;
		var TASK_STATUS_CLASS = 13;
		var DUE_CLASS = 14;
		var ASSET_ID = 15;
		var ASSET_TYPE = 16;
		var ASSET_CLASS = 17;
		var INST_LINK = 18;
		var EST_START_CLASS=19;
		var EST_FINISH_CLASS=20;
		var IS_PUBLISHED=21;
        var UPDATED_CLASS=22;

		$(document).ready(function() {

            progressTimer = new ProgressTimer(0, '${UserPreferenceEnum.TASKMGR_REFRESH}', function () {
                reloadGrid();
				progressTimer.resetTimer();
			});

			$('#assetMenu').show();
			$("#showEntityView").dialog({ autoOpen: false })

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
			var dueDate = '${dateResolved}'
			var status = '${status}'
            var filteredRequest = ${filteredRequest}
			var assignedTo = '${assignedTo}'
			var sendNotification = '${sendNotification}'
			var role = '${role}'
			var category = '${category}'
			var sizePref = '${sizePref}'
			var step = "${step}"
			var windowWidth = $(window).width() - $(window).width()*5/100 ;
			var taskManagerUrl = "/assetEntity/listTasks";
            /*
             * If the filteredRequest is set, then the user landed here via a task link from another page
             * with some filters. Therefore, the Clear Filters button should be enabled.
             */
            var clearFiltersDisableText = filteredRequest? '' : " disabled = 'disabled' "
			var listCaption = "<label for='lbl-task-list-title'>Tasks</label>: \
				<tdsactionbutton id='createTask' label='Create Task' icon='${assetPath(src: '../icons/table_add.png')}' link='' ng-click='comments.createCommentBy(\"${AssetCommentType.TASK}\",\"\",\"\")'></tdsactionbutton>&nbsp; \
				<tdsactionbutton id='bulkEdit' label='Bulk Edit' icon='' link='' ng-click='comments.bulkEditTasks()'></tdsactionbutton> \
				<span class=\"capBtn task_action ui-button ui-widget ui-state-default ui-corner-all ui-button-text-icon-primary action-button\"> \
				<input style=\"border: 0px; color: #2C61AA; padding:3px; font-weight: bold;\" type=\"button\" class=\"clearFilterId\" value=\"Clear Filters\" " + clearFiltersDisableText + " onclick=\"clearFilter('taskListId')\"></span>";

			<jqgrid:grid id="taskListId"  url="'${createLink(action: 'listTaskJSON')}'"
				colNames="'Action', 'Task', 'Description', '${modelPref['1']}', '${modelPref['2']}', 'Updated', 'Due Date', 'Status',
					'${modelPref['3']}', '${modelPref['4']}', '${modelPref['5']}', 'Suc.', 'Score', 'id', 'statusCss'"
				colModel="{name:'act', index: 'act' , sortable: false, formatter: myCustomFormatter, search:false, width:50, fixed:true},
					{name:'taskNumber', formatter:myLinkFormatter, width:60, fixed:true},
					{name:'comment', width:680, formatter:taskViewFormatter},
					{name:'${taskPref['1']}', formatter:${formatterMap[taskPref['1']] ?: 'taskFormatter'}, width:200},
                    {name:'${taskPref['2']}', formatter:${formatterMap[taskPref['2']] ?: 'taskFormatter'}, width:200},
					{name:'updated', formatter: updatedFormatter,sortable:false,search:false},
					{name:'dueDate', formatter: dueFormatter},
					{name:'status', formatter: statusFormatter},
					{name:'${taskPref['3']}', formatter:${formatterMap[taskPref['3']] ?: 'taskFormatter'}, width:200},
                    {name:'${taskPref['4']}', formatter:${formatterMap[taskPref['4']] ?: 'taskFormatter'}, width:200},
                    {name:'${taskPref['5']}', formatter:${formatterMap[taskPref['5']] ?: 'taskFormatter'}, width:200},
					{name:'suc', formatter:taskFormatter,sortable:false,search:false, width:50},
					{name:'score', formatter:taskFormatter, search:false, width:70},
					{name:'id', hidden: true},
					{name:'statusCss', hidden: true}"
				caption="listCaption"
				rowNum="sizePref"
				rowList="${ raw(com.tdsops.common.ui.Pagination.optionsAsText()) }"
				scrollOffset="0"
				gridComplete="function(){ processTaskSafariColumns(); bindResize('taskListId'); gridLoadComplete(); }"
				postData="{moveEvent:event, justRemaining:justRemaining, justMyTasks:justMyTasks, filter:filter, comment:comment, taskNumber:taskNumber,
					assetEntity:assetEntity, assetType:assetType, dueDate:dueDate, status:status, assignedTo:assignedTo, role:role, category:category, viewUnpublished : viewUnpublished, step:step }"
				showPager="true">
				<jqgrid:navigation id="taskListId" add="false" edit="false" del="false" search="false" refresh="false" />
				<jqgrid:refreshButton id="taskListId" />
			</jqgrid:grid>
			TDS.jqGridFilterToolbar('taskListId');

			populateFilter();

			<%-- Add the Column Selector arrow to the customizable columns --%>
			<g:each var="key" in="${taskPref.keySet().toList()}">
				$("#taskListIdGrid_${taskPref[key]}").append('<asset:image src="images/select2Arrow.png" class="selectImage customizeSelect editSelectimage_${key}" onclick="showSelect(\\\'${taskPref[key]}\\\',\\\'taskList\\\',\\\'${key}\\\')" />');
			</g:each>


		});

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

		function myCustomFormatter (cellVal, options, rowObject) {
			var value = rowObject && rowObject[TASK_NUM] ? _.escape(rowObject[TASK_NUM]) : '';
			var editButton = '<a ng-click="comments.editCommentById(\''+options.rowId+'\',\'task\')" title="Edit Task ' + value + '">' +
				'<asset:image src="icons/table_edit.png" border="0px" /></a>';
			return editButton;
		}

		function isPublishedFormatter(cellVal,options,rowObject) {
			return '<span class="cellWithoutBackground pointer" id="span_'+options.rowId+'" >' + (rowObject[IS_PUBLISHED] ? rowObject[IS_PUBLISHED] : "false") + '</span>';
		}

		function taskViewFormatter(cellVal,options,rowObject) {
			return '<span title="View Task ' +
				(cellVal || cellVal == 0 ? (cellVal && isNaN(cellVal) && cellVal.indexOf('href=') > 0 ? cellVal : _.escape(cellVal)) :"") +
				'" class="cellWithoutBackground pointer" id="' + options.colModel.name + '_'+options.rowId +
				'" action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[ASSET_ID] +
				'" status="'+rowObject[STATUS]+'" instructions-link="'+rowObject[INST_LINK]+'">' +
				(cellVal || cellVal == 0 ? (cellVal && isNaN(cellVal) && cellVal.indexOf('href=') > 0 ? cellVal : _.escape(cellVal)) :"") +
				'</span>';
		}
		function taskFormatter(cellVal,options,rowObject) {
			return '<span class="cellWithoutBackground pointer" id="' + options.colModel.name + '_'+options.rowId +
				'" action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[ASSET_ID] +
				'" status="'+rowObject[STATUS]+'" instructions-link="'+rowObject[INST_LINK]+'">' +
				(cellVal || cellVal == 0 ? (cellVal && isNaN(cellVal) && cellVal.indexOf('href=') > 0 ? cellVal : _.escape(cellVal)) :"") +
				'</span>';
		}
		function statusFormatter(cellVal,options,rowObject){
			return '<span id="status_'+options.rowId+'" class="cellWithoutBackground '+rowObject[TASK_STATUS_CLASS] +
				' " action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[ASSET_ID]+'" status="' +
				rowObject[STATUS]+'" instructions-link="'+rowObject[INST_LINK]+'">' + cellVal + '</span>';
		 }

		function updatedFormatter(cellVal,options,rowObject){
			 return '<span id="span_'+options.rowId +'" class="cellWithoutBackground ' +
                 rowObject[UPDATED_CLASS] +'" master="true" action-bar-cell config-table="config.table" comment-id="'+options.rowId +
				 '" asset-id="'+rowObject[ASSET_ID]+'" status="'+rowObject[STATUS] + '" instructions-link="'+rowObject[INST_LINK] +
				 '">' + cellVal + '</span>';
		}
		function dueFormatter(cellVal,options,rowObject){
			return '<span id="span_'+options.rowId+'" class="cellWithoutBackground ' +
				rowObject[ASSET_ID] + '" master="true" action-bar-cell config-table="config.table" comment-id="'+
				options.rowId+
				'" asset-id="'+
				rowObject[ASSET_ID]+'" status="'+rowObject[STATUS]+'" instructions-link="'+rowObject[INST_LINK]+'">' + cellVal + '</span>';
		}

        /**
		 * Cell formatter for estimated start cells.
		 * Note that this formatter is not attached statically to any column in particular.
		 * As the estimated start column is dynamic, this formatter is selected using the fromatterMap,
		 * depending on which column the user has selected to be the estimated start column.
         */
        function estStartFormatter(cellVal,options,rowObject){
            return '<span id="span_'+options.rowId+'" class="cellWithoutBackground ' +
                rowObject[EST_START_CLASS] +'" master="true" action-bar-cell config-table="config.table" comment-id="' + options.rowId+
                '" asset-id="'+ rowObject[ASSET_ID] + '" status="'+rowObject[STATUS] +
				'" instructions-link="'+rowObject[INST_LINK]+'">' + cellVal + '</span>';
        }

        /**
         * Cell formatter for estimated finish cells.
         * Note that this formatter is not attached statically to any column in particular.
         * As the estimated finish column is dynamic, this formatter is selected using the fromatterMap,
         * depending on which column the user has selected to be the estimated finish column.
         */
        function estFinishFormatter(cellVal,options,rowObject){
            return '<span id="span_'+options.rowId+'" class="cellWithoutBackground ' +
                rowObject[EST_FINISH_CLASS] +'" master="true" action-bar-cell config-table="config.table" comment-id="'+
                options.rowId+
                '" asset-id="'+
                rowObject[ASSET_ID]+'" status="'+rowObject[STATUS]+'" instructions-link="'+rowObject[INST_LINK]+'">' + cellVal + '</span>';
        }
		function assetFormatter(cellVal,options,rowObject) {
			return ( options.colModel.name == "assetName" && cellVal
				? '<span class="cellWithoutBackground pointer" onclick= "EntityCrud.showAssetDetailView(\''+rowObject[ASSET_CLASS]+'\', '+rowObject[ASSET_ID]+')\" >' + _.escape(cellVal) + '</span>'
				: (cellVal || cellVal == 0 ? taskFormatter(cellVal,options,rowObject) : "<span class='cellWithoutBackground pointer'></span>")
			);
		}

		function instructionsLinkFormatter(cellVal,options,rowObject){
			return '<span id="status_'+options.rowId+'" class="cellWithoutBackground ' + rowObject[TASK_STATUS_CLASS] +
			' " action-bar-cell config-table="config.table" comment-id="'+options.rowId+'" asset-id="'+rowObject[ASSET_ID ] +
			'" status="'+rowObject[STATUS]+'" instructions-link="'+rowObject[INST_LINK]+'">' + cellVal + '</span>';
		}

		function myLinkFormatter (cellvalue, options, rowObject) {
				var value = cellvalue ? _.escape(cellvalue) : ''
				return "<a href=\"javascript:showAssetComment(" + options.rowId + ", 'show' );\">" + value + "</a>"
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
				progressTimer.resetTimer();
			}
		});

		var initialGridLoad = false;
		function gridLoadComplete() {
		    if(!initialGridLoad) {
            	recompileDOM('taskListIdWrapper');
            	initialGridLoad = true;
            } else {
                recompileDOM('taskListIdGrid');
			}
		};

		function reloadGrid () {
			var postData = $('#taskListIdGrid').jqGrid('getGridParam', 'postData');
			postData.justRemaining = $('#justRemainingCB').is(':checked') ? 1 : 0;
			postData.justMyTasks = $('#justMyTasksCB').is(':checked') ? 1 : 0;
			postData.viewUnpublished = viewUnpublished ? 1 : 0;
			$('#taskListId').trigger('reloadGrid').trigger('click')
		};
	</script>
</head>
<body>
<tds:subHeader title="Task Manager" crumbs="['Task','Task Manager']"/>
	<input type="hidden" id="timeBarValueId" value="0"/>
	<div id="outerBodyId" ng-app="tdsComments" ng-controller="tds.comments.controller.MainController as comments">
		<input type="hidden" id="timeBarValueId" value="0"/>
		<div class="taskTimebar hide" id="issueTimebar" >
			<div id="issueTimebarId"></div>
		</div>
		<div class="fluid task-manager-wrapper">
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
            <div class="row action-bar-wrapper">
                <div class="pull-left">
                    <div id="controlRowId">
                        <label for="lbl-task-event-title">Event</label>
                        <g:select from="${moveEvents}" name="moveEvent" id="moveEventId" optionKey="id" optionValue="name" noSelection="${['0':' All']}" value="${filterEvent}" onchange="submitForm()" />
                        <span class="checkboxContainer action-bar">
                            <input type="checkbox" id="justRemainingCB" class="pointer" ${ (justRemaining == '1' ? 'checked="checked"': '') } onclick="reloadGrid()" />
                            <label for="justRemainingCB" class="pointer"><b>Just Remaining</b></label>
                        </span>
                        <span class="checkboxContainer action-bar">
                            <input type="checkbox" id="justMyTasksCB" class="pointer" ${ (justMyTasks=='1' ? 'checked="checked"' : '') } onclick="reloadGrid()"/>
                            <label for="justMyTasksCB" class="pointer"><b>Just Mine</b></label>
                        </span>
                        <tds:hasPermission permission="${Permission.TaskPublish}">
                            <span class="checkboxContainer action-bar">
                                <input type="checkbox" id="viewUnpublishedCB" class="pointer" ${ (viewUnpublished=='1' ? 'checked="checked"' : '') } onchange="toggleViewUnpublished(this);"/>
                                <label for="viewUnpublishedCB" class="pointer"><b>View Unpublished</b></label>
                            </span>
                        </tds:hasPermission>
                    </div>
                </div>
                <div class="timeline-wrapper text-right pull-right">
                    <tdsactionbutton id="graph" label="View Task Graph" icon="${assetPath(src: '../icons/tds_task_graph.png')}" link="/task/taskGraph?moveEventId=${filterEvent}" click="checkSelectedEvent"></tdsactionbutton>
                    <tdsactionbutton id="timeline" label="View Timeline" icon="${assetPath(src: '../icons/timeline_marker.png')}" link="/task/taskTimeline"></tdsactionbutton>
                    <g:render template="../assetEntity/progressTimerControls" model="${[timerValues:[60, 120, 180, 240, 300]]}"/>
                </div>
            </div>
			<jqgrid:wrapper id="taskListId" />
		</div>
		<input type="hidden" id="customizeFieldCount" value="6" />
		<g:each var="key" in="['1','2','3','4','5']">
			<div id="columnCustomDiv_${taskPref[key]}" style="display:none;">
				<div class="columnDiv_${key} customScroll customizeDiv">
					<input type="hidden" id="previousValue_${key}" value="${taskPref[key]}" />
					<g:each var="attribute" in="${assetCommentFields}">
						<label><input type="radio" name="coloumnSelector_${taskPref[key]}" id="coloumnSelector_${taskPref[key]}_${attribute.key}" value="${attribute.key}"
							${taskPref[key]==attribute.key ? 'checked' : '' }
							onchange="setColumnAssetPref(this.value,'${key}','${UserPreferenceEnum.Task_Columns}')"
							/>
							${attribute.value}
						</label>
						<br>
					</g:each>
				</div>
			</div>
		</g:each>

		<g:render template="../assetEntity/modelDialog" />
		<g:render template="../assetEntity/entityCrudDivs" />
		<g:render template="../assetEntity/dependentAdd" />

		<g:render template="../layouts/error"/>
	</div>
	<div class="tdsAssetsApp" ng-app="tdsAssets" ng-controller="tds.assets.controller.MainController as assets"></div>
	<div id="createStaffDialog" style="display:none;" class="static-dialog">
		<g:render template="../person/createStaff" model="['forWhom':'application']"></g:render>
	</div>
 <g:render template="initAssetEntityData"/>
 <script type="text/javascript">


	function toggleCheckbox (chkbox, field) {
		$('input[name='+field+']').val(chkbox.checked ? '1' : '0')
		submitForm()
	}
	function toggleViewUnpublished (element) {
		var checkedValue = $(element).is(':checked');
		viewUnpublished = checkedValue;
		setUserPreference('${UserPreferenceEnum.VIEW_UNPUBLISHED.name()}', checkedValue, function () {
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
